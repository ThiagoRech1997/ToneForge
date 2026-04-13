#include "toneforge/audio_io.h"
#include "toneforge/audio_engine.h"

#include <oboe/Oboe.h>
#include <android/log.h>
#include <atomic>
#include <memory>
#include <mutex>
#include <vector>

#define LOG_TAG "ToneForgeAudioIO"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {

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

        // Output stream (driver). Low-latency, mono, float, device-preferred.
        oboe::AudioStreamBuilder outputBuilder;
        outputBuilder.setDirection(oboe::Direction::Output)
                ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
                ->setSharingMode(oboe::SharingMode::Exclusive)
                ->setFormat(oboe::AudioFormat::Float)
                ->setChannelCount(oboe::ChannelCount::Mono)
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

        // Pre-aloca scratch de input — evita malloc no callback.
        mInputScratch.assign(static_cast<size_t>(framesPerBurst) * 8, 0.0f);

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
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream* outputStream,
                                          void* audioData,
                                          int32_t numFrames) override {
        auto* out = static_cast<float*>(audioData);

        // Garante scratch suficiente (raramente re-aloca; fora do caminho quente).
        if (static_cast<int32_t>(mInputScratch.size()) < numFrames) {
            mInputScratch.resize(static_cast<size_t>(numFrames));
        }
        float* in = mInputScratch.data();

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

        // Chama o DSP existente sem cópia intermediária.
        processBuffer(in, out, numFrames, numFrames, numFrames);

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
