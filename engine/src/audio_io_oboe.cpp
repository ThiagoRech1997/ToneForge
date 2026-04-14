#include "toneforge/audio_io.h"
#include "toneforge/audio_engine.h"
#include "toneforge/recorder.h"

#include <oboe/Oboe.h>
#include <android/log.h>
#include <algorithm>
#include <atomic>
#include <cstring>
#include <memory>
#include <mutex>
#include <vector>

#define LOG_TAG "ToneForgeAudioIO"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {

static const char* performanceModeToText(oboe::PerformanceMode mode) {
    switch (mode) {
        case oboe::PerformanceMode::None:        return "None";
        case oboe::PerformanceMode::PowerSaving: return "PowerSaving";
        case oboe::PerformanceMode::LowLatency:  return "LowLatency";
        default:                                 return "Unknown";
    }
}

static const char* sharingModeToText(oboe::SharingMode mode) {
    switch (mode) {
        case oboe::SharingMode::Exclusive: return "Exclusive";
        case oboe::SharingMode::Shared:    return "Shared";
        default:                           return "Unknown";
    }
}

static const char* audioApiToText(oboe::AudioApi api) {
    switch (api) {
        case oboe::AudioApi::Unspecified: return "Unspecified";
        case oboe::AudioApi::OpenSLES:    return "OpenSLES";
        case oboe::AudioApi::AAudio:      return "AAudio";
        default:                          return "Unknown";
    }
}

static void logStreamDiagnostics(const char* tag, oboe::AudioStream* stream,
                                 oboe::PerformanceMode requestedPerf,
                                 oboe::SharingMode requestedShare) {
    if (!stream) return;
    const auto perf  = stream->getPerformanceMode();
    const auto share = stream->getSharingMode();
    const bool perfGranted  = (perf  == requestedPerf);
    const bool shareGranted = (share == requestedShare);
    auto xrunResult = stream->getXRunCount();
    const int xrunCount = xrunResult ? xrunResult.value() : -1;
    LOGI("%s: api=%s sr=%d fmt=%d ch=%d framesPerBurst=%d bufferCapacity=%d "
         "perfMode=%s(%s) sharing=%s(%s) xrun=%d",
         tag,
         audioApiToText(stream->getAudioApi()),
         stream->getSampleRate(),
         (int)stream->getFormat(),
         stream->getChannelCount(),
         stream->getFramesPerBurst(),
         stream->getBufferCapacityInFrames(),
         performanceModeToText(perf),  perfGranted  ? "granted" : "DEGRADED",
         sharingModeToText(share),     shareGranted ? "granted" : "DEGRADED",
         xrunCount);
    if (!perfGranted) {
        LOGW("%s: PerformanceMode solicitado LowLatency mas Oboe entregou %s — "
             "provável fallback fora do fast path",
             tag, performanceModeToText(perf));
    }
    if (!shareGranted) {
        LOGW("%s: SharingMode solicitado Exclusive mas Oboe entregou %s — "
             "fast path exclusivo negado",
             tag, sharingModeToText(share));
    }
}

// Driver-callback pattern: output stream é o driver, lê input stream sincronamente
// dentro do callback. É o padrão recomendado pelo sample LiveEffect do Oboe para
// processamento de instrumento em tempo real, e permite que o DSP existente
// (processBuffer) seja chamado sem mudanças.
class OboeEngine : public oboe::AudioStreamDataCallback,
                   public oboe::AudioStreamErrorCallback {
public:
    oboe::Result start(int requestedSampleRate, int requestedFramesPerCallback) {
        std::lock_guard<std::mutex> lock(mLifecycleMutex);
        if (mIsRunning.load()) {
            LOGW("start() chamado mas engine já está rodando");
            return oboe::Result::OK;
        }

        // Output stream (driver). Low-latency, STEREO, float, device-preferred.
        // Nota: pedimos Stereo porque o fast path do AAudio em devices MediaTek
        // (ex.: Moto G9 Play / Helio G80) nega Mono/LowLatency/Exclusive. O DSP
        // continua mono internamente — duplicamos o sample processado pros
        // dois canais dentro do callback (ver onAudioReady).
        oboe::AudioStreamBuilder outputBuilder;
        outputBuilder.setDirection(oboe::Direction::Output)
                ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
                ->setSharingMode(oboe::SharingMode::Exclusive)
                ->setFormat(oboe::AudioFormat::Float)
                ->setChannelCount(oboe::ChannelCount::Stereo)
                ->setDataCallback(this)
                ->setErrorCallback(this);
        if (requestedSampleRate > 0) outputBuilder.setSampleRate(requestedSampleRate);
        if (requestedFramesPerCallback > 0) outputBuilder.setFramesPerDataCallback(requestedFramesPerCallback);

        auto result = outputBuilder.openStream(mOutputStream);
        if (result != oboe::Result::OK) {
            LOGE("openStream(output) falhou: %s", oboe::convertToText(result));
            return result;
        }

        const int sampleRate = mOutputStream->getSampleRate();
        const int framesPerBurst = mOutputStream->getFramesPerBurst();

        // Input stream espelhando a config do output para evitar resample in-callback.
        oboe::AudioStreamBuilder inputBuilder;
        inputBuilder.setDirection(oboe::Direction::Input)
                ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
                ->setSharingMode(oboe::SharingMode::Exclusive)
                ->setFormat(oboe::AudioFormat::Float)
                ->setChannelCount(oboe::ChannelCount::Mono)
                ->setSampleRate(sampleRate)
                ->setInputPreset(oboe::InputPreset::Unprocessed);

        result = inputBuilder.openStream(mInputStream);
        if (result != oboe::Result::OK) {
            LOGE("openStream(input) falhou: %s", oboe::convertToText(result));
            mOutputStream->close();
            mOutputStream.reset();
            return result;
        }

        // Propaga sample rate efetivo para o DSP existente.
        setSampleRate(sampleRate);

        // Pre-aloca scratch de input/output mono para o PIOR caso que o Oboe
        // pode pedir num callback (bufferCapacityInFrames). O callback NUNCA
        // redimensiona o scratch — se numFrames exceder, o callback simplesmente
        // falha graceful (ver onAudioReady). Evita malloc no hot path.
        const int inputCapacity = std::max(
            mInputStream->getBufferCapacityInFrames(),
            mOutputStream->getBufferCapacityInFrames());
        const size_t scratchSize = static_cast<size_t>(std::max(inputCapacity, framesPerBurst * 8));
        mInputScratch.assign(scratchSize, 0.0f);
        mMonoOutputScratch.assign(scratchSize, 0.0f);

        // Buffer capacity sugerida: 2x burst reduz xruns sem inflar latência.
        mOutputStream->setBufferSizeInFrames(framesPerBurst * 2);

        mXrunCount.store(0);
        mIsRunning.store(true);

        result = mInputStream->requestStart();
        if (result != oboe::Result::OK) {
            LOGE("requestStart(input) falhou: %s", oboe::convertToText(result));
            stopLocked();
            return result;
        }

        result = mOutputStream->requestStart();
        if (result != oboe::Result::OK) {
            LOGE("requestStart(output) falhou: %s", oboe::convertToText(result));
            stopLocked();
            return result;
        }

        // Telemetria detalhada — serve pra diagnosticar quando o device cai
        // fora do fast path (ver TFR-50).
        logStreamDiagnostics("Oboe output", mOutputStream.get(),
                             oboe::PerformanceMode::LowLatency,
                             oboe::SharingMode::Exclusive);
        logStreamDiagnostics("Oboe input", mInputStream.get(),
                             oboe::PerformanceMode::LowLatency,
                             oboe::SharingMode::Exclusive);

        LOGI("Oboe engine iniciado: sampleRate=%d framesPerBurst=%d", sampleRate, framesPerBurst);
        return oboe::Result::OK;
    }

    void stop() {
        std::lock_guard<std::mutex> lock(mLifecycleMutex);
        stopLocked();
    }

    bool isRunning() const {
        return mIsRunning.load();
    }

    double getLatencyMs() {
        std::lock_guard<std::mutex> lock(mLifecycleMutex);
        if (!mInputStream || !mOutputStream) return -1.0;
        auto outLatency = mOutputStream->calculateLatencyMillis();
        auto inLatency = mInputStream->calculateLatencyMillis();
        if (!outLatency || !inLatency) return -1.0;
        return outLatency.value() + inLatency.value();
    }

    int getXrunCount() const {
        return mXrunCount.load();
    }

    int getSampleRate() {
        std::lock_guard<std::mutex> lock(mLifecycleMutex);
        if (!mOutputStream) return 0;
        return mOutputStream->getSampleRate();
    }

    // oboe::AudioStreamDataCallback — invocado na thread de áudio.
    // HOT PATH: zero locks, zero allocs, zero I/O.
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream* outputStream,
                                          void* audioData,
                                          int32_t numFrames) override {
        auto* out = static_cast<float*>(audioData);
        const int32_t outputChannels = outputStream->getChannelCount();

        // Scratches são pré-alocados em start() para o pior caso. Se o Oboe
        // pedir mais do que isso num callback (não deveria acontecer), não
        // redimensionamos em runtime — só saímos com silêncio pra manter o
        // callback lock-free. Qualquer caso anômalo cai num xrun controlado.
        if (static_cast<int32_t>(mInputScratch.size()) < numFrames ||
            static_cast<int32_t>(mMonoOutputScratch.size()) < numFrames) {
            std::memset(out, 0,
                        static_cast<size_t>(numFrames) * outputChannels * sizeof(float));
            mXrunCount.fetch_add(1, std::memory_order_relaxed);
            return oboe::DataCallbackResult::Continue;
        }
        float* in = mInputScratch.data();
        float* monoOut = mMonoOutputScratch.data();

        // Read não-bloqueante do input stream. Se faltar dado (startup), zera e segue.
        auto readResult = mInputStream->read(in, numFrames, 0 /* timeoutNs */);
        int32_t inputFrames = 0;
        if (readResult && readResult.value() > 0) {
            inputFrames = readResult.value();
        }
        if (inputFrames < numFrames) {
            // Zera o resto para não injetar lixo no DSP.
            for (int32_t i = inputFrames; i < numFrames; ++i) in[i] = 0.0f;
        }

        // Alimenta o tuner com uma cópia dos samples de entrada, se ativo.
        // O tuner legado (Java) usava um AudioRecord dedicado; aqui o pipeline
        // C++ já tem os samples, então basta encadear. Fase 3.Tuner.
        if (isTunerActive()) {
            processTunerBuffer(in, numFrames);
        }

        // DSP é mono. Escrevemos num scratch mono e depois expandimos pra
        // interleaved multi-canal. Se o output for mono (ch=1), copiamos
        // direto; se for stereo (ch=2), duplicamos L=R.
        processBuffer(in, monoOut, numFrames, numFrames, numFrames);

        if (outputChannels == 1) {
            std::memcpy(out, monoOut, static_cast<size_t>(numFrames) * sizeof(float));
        } else {
            // Interleaved: out[0]=L0, out[1]=R0, out[2]=L1, out[3]=R1, ...
            for (int32_t i = 0; i < numFrames; ++i) {
                const float s = monoOut[i];
                float* frame = out + static_cast<size_t>(i) * outputChannels;
                frame[0] = s;
                frame[1] = s;
                // Se por algum motivo o device expuser >2 canais, zera o resto.
                for (int32_t c = 2; c < outputChannels; ++c) frame[c] = 0.0f;
            }
        }

        // Captura pós-FX para o recorder (se ativo). O recorder espera mono,
        // então alimentamos com o scratch mono, não com o buffer interleaved.
        // Fase 3.Recorder.
        if (recorder_is_active()) {
            recorder_feed(monoOut, numFrames);
        }

        return oboe::DataCallbackResult::Continue;
    }

    // oboe::AudioStreamErrorCallback
    void onErrorAfterClose(oboe::AudioStream* /*stream*/, oboe::Result error) override {
        LOGE("onErrorAfterClose: %s", oboe::convertToText(error));
        mIsRunning.store(false);
        // xrun: incrementa contador para telemetria do benchmark.
        if (error == oboe::Result::ErrorDisconnected) {
            mXrunCount.fetch_add(1);
        }
    }

private:
    void stopLocked() {
        mIsRunning.store(false);
        if (mOutputStream) {
            mOutputStream->stop();
            mOutputStream->close();
            mOutputStream.reset();
        }
        if (mInputStream) {
            mInputStream->stop();
            mInputStream->close();
            mInputStream.reset();
        }
        LOGI("Oboe engine parado");
    }

    std::shared_ptr<oboe::AudioStream> mOutputStream;
    std::shared_ptr<oboe::AudioStream> mInputStream;
    std::vector<float> mInputScratch;
    // Scratch mono pra saída do DSP antes de expandir pra stereo interleaved.
    // Pré-alocado em start() — nunca redimensionado no hot path.
    std::vector<float> mMonoOutputScratch;
    std::atomic<bool> mIsRunning{false};
    std::atomic<int> mXrunCount{0};
    std::mutex mLifecycleMutex;
};

OboeEngine& getEngine() {
    static OboeEngine sEngine;
    return sEngine;
}

} // namespace

extern "C" {

int audio_engine_start(int sampleRate, int framesPerCallback) {
    auto result = getEngine().start(sampleRate, framesPerCallback);
    return static_cast<int>(result);
}

void audio_engine_stop() {
    getEngine().stop();
}

bool audio_engine_is_running() {
    return getEngine().isRunning();
}

double audio_engine_get_latency_ms() {
    return getEngine().getLatencyMs();
}

int audio_engine_get_xrun_count() {
    return getEngine().getXrunCount();
}

int audio_engine_get_sample_rate() {
    return getEngine().getSampleRate();
}

} // extern "C"
