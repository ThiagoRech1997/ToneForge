#include "toneforge/audio_engine.h"
#include <cmath>
#include <cstring>
#include <atomic>
#include <thread>
#include <chrono>
#include <vector>
#include <mutex>
#include <string>
#include <algorithm>

// Flag de inicialização do engine
static std::atomic<bool> isEngineInitialized{false};

// Mutexes. audioEngineMutex protege o lifecycle (init/setSampleRate).
// looperMutex protege as operações do looper fora do hot path. Nenhum
// destes pode ser pego pelo audio callback — os buffers do hot path são
// pré-alocados e lidos lock-free via atomics.
static std::mutex audioEngineMutex;
static std::mutex looperMutex;

// Configurações de Oversampling com proteção thread-safe
static std::atomic<int> oversamplingFactor{2}; // 2x, 4x, 8x
static std::atomic<bool> oversamplingEnabled{true};
static std::vector<float> oversampleBuffer;
static std::vector<float> downsampleBuffer;
static std::atomic<int> oversampleBufferSize{0};

// Parâmetros dos efeitos com proteção thread-safe
static std::atomic<float> currentGain{1.0f};
static std::atomic<float> distortionAmount{0.0f};
static std::atomic<float> delayTime{0.0f};
static std::atomic<float> delayFeedback{0.0f};
static std::atomic<float> delayTimeMs{200.0f};  // Tempo em milissegundos
static std::atomic<bool> delaySyncBPM{false};   // Sincronizar com BPM
static std::atomic<int> delayBPM{120};          // BPM para sincronização
static std::atomic<float> reverbRoomSize{0.0f};
static std::atomic<float> reverbDamping{0.0f};

// Buffer de delay dinâmico baseado na taxa de amostragem
static std::atomic<int> MAX_DELAY_SAMPLES{48000}; // Será ajustado dinamicamente
static const float MAX_DELAY_TIME = 2.0f; // 2 segundos (aumentado para maior flexibilidade)
static std::atomic<int> SAMPLE_RATE{48000};
static std::vector<float> delayBuffer;
static std::atomic<int> delayBufferIndex{0};
static std::atomic<int> delayBufferSize{0};

// Buffer de reverb dinâmico
static std::atomic<int> REVERB_BUFFER_SIZE{4096}; // Será ajustado dinamicamente
static std::vector<float> reverbBuffer;
static std::atomic<int> reverbIndex{0};

// Taxa de amostragem (será configurada dinamicamente)
static std::atomic<int> sampleRate{48000};

// Constantes para limites de segurança
static const int MIN_BUFFER_SIZE = 512;
static const int MAX_BUFFER_SIZE = 65536;
static const int MIN_SAMPLE_RATE = 8000;
static const int MAX_SAMPLE_RATE = 192000;
static const int MAX_OVERSAMPLING_FACTOR = 8;

// --- Metrônomo ---
static std::atomic<bool> metronomeActive{false};
static std::atomic<int> metronomeBpm{120};
static std::atomic<int> metronomeSampleRate{48000};
static std::atomic<int> metronomeSamplesPerBeat{24000}; // 120 BPM default
static std::atomic<int> metronomeSampleCounter{0};
static std::atomic<bool> metronomeClick{false};
static std::atomic<int> metronomeBeatCount{0}; // Contador de batidas para acentos
static std::atomic<int> metronomeTimeSignature{4}; // Compasso 4/4 por padrão
static std::atomic<float> metronomeVolume{0.6f}; // Volume do metrônomo
static std::atomic<int> metronomeClickDuration{150}; // Duração do click em samples

// --- Looper ---
static std::atomic<int> LOOPER_MAX_SAMPLES{48000 * 30}; // Será ajustado dinamicamente
static const int LOOPER_MAX_TRACKS = 8; // máximo 8 faixas

// Estrutura para uma faixa do looper
struct LooperTrack {
    std::vector<float> buffer; // Buffer dinâmico
    std::atomic<int> length{0};
    std::atomic<int> position{0}; // Posição de leitura/escrita
    std::atomic<float> volume{1.0f};
    std::atomic<bool> muted{false};
    std::atomic<bool> soloed{false};
    std::atomic<bool> active{false};
    std::atomic<bool> isRecording{false};
    std::atomic<bool> isPlaying{false};
    
    LooperTrack() {
        int initialSize = LOOPER_MAX_SAMPLES.load();
        if (initialSize > 0 && initialSize <= MAX_BUFFER_SIZE) {
            buffer.resize(initialSize, 0.0f);
        } else {
            buffer.resize(MIN_BUFFER_SIZE, 0.0f);
        }
    }

    void resizeBuffer(int newSize) {
        if (newSize > 0 && newSize <= MAX_BUFFER_SIZE) {
            std::lock_guard<std::mutex> lock(looperMutex);
            buffer.resize(newSize, 0.0f);
        }
    }

    void recordSample(float sample) {
        if (!isRecording.load()) return;

        int currentLength = length.load();
        int currentPosition = position.load();
        int bufferSize = buffer.size();

        if (currentPosition >= 0 && currentPosition < bufferSize) {
            buffer[currentPosition] = sample;
            currentPosition++;

            if (currentPosition >= bufferSize) {
                // Buffer cheio, parar gravação
                isRecording.store(false);
                currentLength = bufferSize;
            } else {
                currentLength = std::max(currentLength, currentPosition);
            }

            position.store(currentPosition);
            length.store(currentLength);
        } else {
            position.store(0);
        }
    }
    
    float getSample() {
        if (!isPlaying.load()) return 0.0f;
        
        int currentLength = length.load();
        int currentPosition = position.load();
        int bufferSize = buffer.size();
        
        if (currentLength <= 0 || currentPosition < 0 || currentPosition >= bufferSize) {
            return 0.0f;
        }
        
        float sample = buffer[currentPosition] * volume.load();
        currentPosition = (currentPosition + 1) % currentLength;
        position.store(currentPosition);
        
        return sample;
    }
    
    void reset() {
        length.store(0);
        position.store(0);
        isRecording.store(false);
        isPlaying.store(false);
        active.store(false);
        std::fill(buffer.begin(), buffer.end(), 0.0f);
    }
};

static LooperTrack looperTracks[LOOPER_MAX_TRACKS];
static std::atomic<int> currentTrack{0};
static std::atomic<int> looperLength{0};
static std::atomic<int> looperWriteIndex{0};
static std::atomic<int> looperReadIndex{0};
static std::atomic<bool> looperRecording{false};
static std::atomic<bool> looperPlaying{false};
static std::atomic<bool> looperSyncEnabled{false};
static std::atomic<int> looperBPM{120};
static std::atomic<int> looperSampleRate{48000};

// Funcionalidades especiais do looper
static std::atomic<bool> looperReverse{false};
static std::atomic<float> looperSpeed{1.0f};
static std::atomic<float> looperPitchShift{0.0f};
static std::atomic<bool> looperStutter{false};
static std::atomic<float> looperStutterRate{4.0f}; // 4 Hz por padrão
static std::atomic<float> looperStutterPhase{0.0f};
static std::atomic<int> looperStutterCounter{0};

// Funcionalidade de Slicing
static std::atomic<bool> looperSlicingEnabled{false};
static const int MAX_SLICES = 16;
static int slicePoints[MAX_SLICES];
static std::atomic<int> numSlicePoints{0};
static std::atomic<int> sliceLength{0}; // comprimento de cada slice
static int sliceOrder[MAX_SLICES]; // ordem de reprodução dos slices
static std::atomic<int> currentSliceIndex{0};
static std::atomic<int> slicePlaybackPosition{0};
static std::atomic<bool> sliceRandomized{false};

// === FASE 5: EFEITOS AVANÇADOS PARA LOOPER ===

// Compressão automática para looper
static std::atomic<bool> looperAutoCompressionEnabled{false};
static std::atomic<float> looperCompressionThreshold{-20.0f}; // dB
static std::atomic<float> looperCompressionRatio{4.0f};
static std::atomic<float> looperCompressionAttack{10.0f}; // ms
static std::atomic<float> looperCompressionRelease{100.0f}; // ms
static std::atomic<float> looperCompressionEnvelope{0.0f};
static std::atomic<float> looperCompressionGain{1.0f};

// Normalização automática
static std::atomic<bool> looperAutoNormalizationEnabled{false};
static std::atomic<float> looperNormalizationTarget{-3.0f}; // dB
static std::atomic<float> looperNormalizationGain{1.0f};
static std::atomic<float> looperPeakLevel{0.0f};

// Filtros para looper
static std::atomic<bool> looperLowPassEnabled{false};
static std::atomic<float> looperLowPassFrequency{8000.0f}; // Hz
static std::atomic<float> looperLowPassX1{0.0f}, looperLowPassX2{0.0f};
static std::atomic<float> looperLowPassY1{0.0f}, looperLowPassY2{0.0f};

static std::atomic<bool> looperHighPassEnabled{false};
static std::atomic<float> looperHighPassFrequency{80.0f}; // Hz
static std::atomic<float> looperHighPassX1{0.0f}, looperHighPassX2{0.0f};
static std::atomic<float> looperHighPassY1{0.0f}, looperHighPassY2{0.0f};

// Reverb de cauda entre loops
static std::atomic<bool> looperReverbTailEnabled{false};
static std::atomic<float> looperReverbTailDecay{2.0f}; // segundos
static std::atomic<float> looperReverbTailMix{0.3f};
static std::vector<float> looperReverbTailBuffer;
static std::atomic<int> looperReverbTailIndex{0};
static std::atomic<int> looperReverbTailSize{0};
static std::atomic<float> looperReverbTailDecayCoeff{0.0f};

// --- Afinador ---
// Forward declaration — a implementação fica depois de getDetectedFrequency
// mas o consumer precisa chamar isso.
static float detectPitch(const float* buffer, int numSamples, int sampleRate);

// Ring buffer lock-free: o audio callback só escreve, um consumer (UI thread
// via getDetectedFrequency) faz snapshot e roda a detecção de pitch fora do
// hot path. Capacidade = potência de 2 para permitir wrap via bitmask.
static constexpr int TUNER_RING_CAPACITY = 16384;
static constexpr int TUNER_SCRATCH_MAX = TUNER_RING_CAPACITY / 2;
static float tunerRing[TUNER_RING_CAPACITY];
static std::atomic<uint32_t> tunerRingWrite{0};
// Scratch buffer usado SOMENTE pela thread consumer. tunerScratchMutex
// protege dos casos (raros) em que várias threads chamam
// getDetectedFrequency concorrentemente — nunca é pego pelo audio callback.
static float tunerScratch[TUNER_SCRATCH_MAX];
static std::mutex tunerScratchMutex;

static std::atomic<bool> tunerActive{false};
static std::atomic<float> detectedFrequency{0.0f};
static std::atomic<int> tunerSampleRate{48000};

// Histórico de frequências para suavização
static const int FREQ_SMOOTH_SIZE = 5;
static float freqHistory[FREQ_SMOOTH_SIZE] = {0};
static std::atomic<int> freqHistoryIdx{0};

// Flags de ativação dos efeitos com proteção thread-safe
static std::atomic<bool> gainEnabled{true};
static std::atomic<bool> distortionEnabled{true};
static std::atomic<bool> delayEnabled{true};
static std::atomic<bool> reverbEnabled{true};

static std::vector<std::string> effectOrder = {"Ganho", "Distorção", "Chorus", "Flanger", "Phaser", "EQ", "Compressor", "Delay", "Reverb"};

static std::atomic<int> distortionType{0}; // 0=Soft, 1=Hard, 2=Fuzz, 3=Overdrive
static std::atomic<float> distortionMix{1.0f};
static std::atomic<float> delayMix{1.0f};
static std::atomic<float> reverbMix{1.0f};

// Chorus
static std::atomic<bool> chorusEnabled{false};
static std::atomic<float> chorusDepth{0.02f}; // 20 ms
static std::atomic<float> chorusRate{1.0f};   // 1 Hz
static std::atomic<float> chorusMix{0.5f};
static std::atomic<int> chorusSampleRate{48000};
static std::atomic<int> chorusBufferSize{48000}; // 1 segundo
static std::vector<float> chorusBuffer;
static std::atomic<int> chorusBufferIndex{0};
static std::atomic<float> chorusPhase{0.0f};

// Flanger
static std::atomic<bool> flangerEnabled{false};
static std::atomic<float> flangerDepth{0.003f}; // 3 ms
static std::atomic<float> flangerRate{0.3f};   // 0.3 Hz
static std::atomic<float> flangerFeedback{0.5f};
static std::atomic<float> flangerMix{0.5f};
static std::atomic<int> flangerSampleRate{48000};
static std::atomic<int> flangerBufferSize{48000}; // 1 segundo
static std::vector<float> flangerBuffer;
static std::atomic<int> flangerBufferIndex{0};
static std::atomic<float> flangerPhase{0.0f};

// Phaser
static std::atomic<bool> phaserEnabled{false};
static std::atomic<float> phaserDepth{0.8f};    // Profundidade da modulação (0-1)
static std::atomic<float> phaserRate{0.5f};     // Taxa de modulação em Hz
static std::atomic<float> phaserFeedback{0.6f}; // Feedback do phaser
static std::atomic<float> phaserMix{0.5f};      // Mix dry/wet
static std::atomic<int> phaserSampleRate{48000};
static std::atomic<int> phaserBufferSize{48000}; // 1 segundo
static std::vector<float> phaserBuffer;
static std::atomic<int> phaserBufferIndex{0};
static std::atomic<float> phaserPhase{0.0f};
static std::atomic<float> phaserLfo{0.0f};      // Valor atual do LFO

// Equalizer (EQ)
static std::atomic<bool> eqEnabled{false};
static std::atomic<float> eqLowGain{0.0f};      // Ganho para graves (60Hz)
static std::atomic<float> eqMidGain{0.0f};      // Ganho para médios (1kHz)
static std::atomic<float> eqHighGain{0.0f};     // Ganho para agudos (8kHz)
static std::atomic<float> eqMix{1.0f};          // Mix dry/wet
static std::atomic<int> eqSampleRate{48000};

// Filtros do EQ (estados dos filtros)
static std::atomic<float> eqLowX1{0.0f}, eqLowX2{0.0f}, eqLowY1{0.0f}, eqLowY2{0.0f};
static std::atomic<float> eqMidX1{0.0f}, eqMidX2{0.0f}, eqMidY1{0.0f}, eqMidY2{0.0f};
static std::atomic<float> eqHighX1{0.0f}, eqHighX2{0.0f}, eqHighY1{0.0f}, eqHighY2{0.0f};

// Compressor
static std::atomic<bool> compressorEnabled{false};
static std::atomic<float> compressorThreshold{-20.0f};  // dB
static std::atomic<float> compressorRatio{4.0f};        // 4:1
static std::atomic<float> compressorAttack{10.0f};      // ms
static std::atomic<float> compressorRelease{100.0f};    // ms
static std::atomic<float> compressorMix{1.0f};          // Mix dry/wet
static std::atomic<int> compressorSampleRate{48000};

// Estado do compressor
static std::atomic<float> compressorEnvelope{0.0f};     // Envelope detector
static std::atomic<float> compressorGain{1.0f};         // Gain reduction

static std::atomic<int> reverbType{0}; // 0=Hall, 1=Plate, 2=Spring

// === FASE 6: INTEGRAÇÃO AVANÇADA ===

// Quantização
static std::atomic<bool> looperQuantizationEnabled{false};
static std::atomic<float> looperQuantizationGrid{0.25f}; // 1/4 de batida por padrão
static std::atomic<int> looperQuantizationSamples{0}; // samples por grid
static std::atomic<int> looperQuantizationCounter{0}; // contador para alinhamento

// Fade In/Out automático
static std::atomic<bool> looperAutoFadeInEnabled{false};
static std::atomic<bool> looperAutoFadeOutEnabled{false};
static std::atomic<float> looperFadeInDuration{0.1f}; // segundos
static std::atomic<float> looperFadeOutDuration{0.1f}; // segundos
static std::atomic<int> looperFadeInSamples{0};
static std::atomic<int> looperFadeOutSamples{0};
static std::atomic<int> looperFadeInCounter{0};
static std::atomic<int> looperFadeOutCounter{0};

// Integração MIDI
static std::atomic<bool> looperMidiEnabled{false};
static std::atomic<int> looperMidiChannel{0}; // canal 1 (0-based)
static int looperMidiCCMapping[128]; // mapeamento CC -> função
static bool looperMidiCCActive[128]; // estado dos CCs

// Notificações
static bool looperNotificationEnabled = false;
static bool looperNotificationControlsEnabled = false;
static std::string looperNotificationState = "stopped"; // stopped, recording, playing

void initAudioEngine() {
    std::lock_guard<std::mutex> lock(audioEngineMutex);

    // Pré-alocação dos buffers do hot path para o MÁXIMO que a engine pode
    // precisar em qualquer sample rate suportada. Assim o audio callback
    // nunca precisa pegar lock nem redimensionar buffers — ele só lê o
    // tamanho ativo via atomic e o próprio vetor nunca muda de capacidade.

    int currentSampleRate = sampleRate.load();

    // Delay: pior caso = MAX_DELAY_TIME * MAX_SAMPLE_RATE.
    const int delayHardMax = (int)(MAX_DELAY_TIME * MAX_SAMPLE_RATE) + 1024;
    delayBuffer.assign(delayHardMax, 0.0f);
    int delaySize = (int)(MAX_DELAY_TIME * currentSampleRate);
    MAX_DELAY_SAMPLES.store(delaySize);
    delayBufferSize.store(delaySize);
    delayBufferIndex.store(0);

    // Reverb: pior caso = 100ms @ MAX_SAMPLE_RATE.
    const int reverbHardMax = (MAX_SAMPLE_RATE / 10) + 1024;
    reverbBuffer.assign(reverbHardMax, 0.0f);
    reverbIndex.store(0);

    // Oversampling: pior caso = MAX_BUFFER_SIZE * MAX_OVERSAMPLING_FACTOR.
    const int oversampleHardMax = MAX_BUFFER_SIZE * MAX_OVERSAMPLING_FACTOR;
    oversampleBuffer.assign(oversampleHardMax, 0.0f);
    downsampleBuffer.assign(oversampleHardMax, 0.0f);
    int oversampleSize = 4096 * oversamplingFactor.load();
    oversampleBufferSize.store(oversampleSize);

    // Modulação (chorus/flanger/phaser): pior caso = 1s @ MAX_SAMPLE_RATE.
    const int modHardMax = MAX_SAMPLE_RATE;
    chorusBuffer.assign(modHardMax, 0.0f);
    flangerBuffer.assign(modHardMax, 0.0f);
    phaserBuffer.assign(modHardMax, 0.0f);

    int modBufferSize = currentSampleRate; // 1 segundo
    chorusBufferSize.store(modBufferSize);
    chorusBufferIndex.store(0);
    flangerBufferSize.store(modBufferSize);
    flangerBufferIndex.store(0);
    phaserBufferSize.store(modBufferSize);
    phaserBufferIndex.store(0);
    
    // Inicializar reverb de cauda do looper
    int reverbTailSize = (int)(looperReverbTailDecay.load() * currentSampleRate);
    looperReverbTailSize.store(reverbTailSize);
    looperReverbTailBuffer.resize(reverbTailSize, 0.0f);
    looperReverbTailIndex.store(0);
    looperReverbTailDecayCoeff.store(expf(-1.0f / (looperReverbTailDecay.load() * currentSampleRate)));
    
    // Inicializar buffers do looper
    int looperMaxSamples = currentSampleRate * 30; // 30 segundos
    LOOPER_MAX_SAMPLES.store(looperMaxSamples);
    for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
        looperTracks[i].resizeBuffer(looperMaxSamples);
    }
    
    // Resetar fases
    chorusPhase.store(0.0f);
    flangerPhase.store(0.0f);
    phaserPhase.store(0.0f);
    phaserLfo.store(0.0f);
    
    // Resetar parâmetros
    currentGain.store(1.0f);
    distortionAmount.store(0.0f);
    delayTime.store(0.0f);
    delayFeedback.store(0.0f);
    reverbRoomSize.store(0.0f);
    reverbDamping.store(0.0f);
    
    // Resetar efeitos avançados do looper
    looperCompressionEnvelope.store(0.0f);
    looperCompressionGain.store(1.0f);
    looperNormalizationGain.store(1.0f);
    looperPeakLevel.store(0.0f);
    looperLowPassX1.store(0.0f);
    looperLowPassX2.store(0.0f);
    looperLowPassY1.store(0.0f);
    looperLowPassY2.store(0.0f);
    looperHighPassX1.store(0.0f);
    looperHighPassX2.store(0.0f);
    looperHighPassY1.store(0.0f);
    looperHighPassY2.store(0.0f);
    
    // Resetar integração avançada (Fase 6)
    looperQuantizationCounter.store(0);
    looperFadeInCounter.store(0);
    looperFadeOutCounter.store(0);
    
    // Inicializar mapeamento MIDI
    for (int i = 0; i < 128; i++) {
        looperMidiCCMapping[i] = -1; // não mapeado
        looperMidiCCActive[i] = false;
    }
    
    // Mapeamento padrão de CCs
    looperMidiCCMapping[64] = 0; // CC64 = Record
    looperMidiCCMapping[65] = 1; // CC65 = Play
    looperMidiCCMapping[66] = 2; // CC66 = Stop
    looperMidiCCMapping[67] = 3; // CC67 = Clear

    // Marcar engine como inicializado
    isEngineInitialized.store(true);
}

void cleanupAudioEngine() {
    // Marcar engine como não inicializado
    isEngineInitialized.store(false);

    // Limpar buffers
    delayBuffer.clear();
    reverbBuffer.clear();
    oversampleBuffer.clear();
    downsampleBuffer.clear();
}

void setGain(float gain) {
    currentGain = gain;
}

void setDistortion(float amount) {
    distortionAmount = amount;
}

// Hot-path buffers são pré-alocados no init. Os setters abaixo só
// atualizam o "tamanho ativo" via atomic — o vetor nunca é redimensionado
// em runtime. Isso elimina a necessidade de lock no audio callback.
static int clampDelaySamples(int requested) {
    const int hardMax = (int)delayBuffer.size();
    if (requested < 0) return 0;
    if (requested > hardMax) return hardMax;
    return requested;
}

void setDelay(float time, float feedback) {
    delayTime = time;
    delayFeedback = feedback;
    delayBufferSize.store(clampDelaySamples((int)(time * sampleRate)));
}

void setDelayTime(float timeMs) {
    delayTimeMs = timeMs;
    if (!delaySyncBPM) {
        delayTime = timeMs / 1000.0f;
    } else {
        float beatLength = 60.0f / delayBPM.load();
        delayTime = beatLength / 4.0f; // Divisão por 4 (semínima)
    }
    delayBufferSize.store(clampDelaySamples((int)(delayTime.load() * sampleRate)));
}

void setDelaySyncBPM(bool sync) {
    delaySyncBPM = sync;
    if (sync) {
        float beatTime = 60.0f / delayBPM.load();
        delayTime = beatTime;
        delayBufferSize.store(clampDelaySamples((int)(delayTime.load() * sampleRate)));
    } else {
        setDelayTime(delayTimeMs);
    }
}

void setDelayBPM(int bpm) {
    delayBPM = bpm;
    if (delaySyncBPM) {
        float beatLength = 60.0f / bpm;
        delayTime = beatLength / 4.0f;
        delayBufferSize.store(clampDelaySamples((int)(delayTime.load() * sampleRate)));
    }
}

void setReverb(float roomSize, float damping) {
    reverbRoomSize = roomSize;
    reverbDamping = damping;
}

void setSampleRate(int rate) {
    std::lock_guard<std::mutex> lock(audioEngineMutex);

    // Validar taxa de amostragem
    if (rate < MIN_SAMPLE_RATE || rate > MAX_SAMPLE_RATE) {
        return;
    }

    // Atualizar taxas de amostragem
    sampleRate.store(rate);
    SAMPLE_RATE.store(rate);
    metronomeSampleRate.store(rate);
    looperSampleRate.store(rate);
    tunerSampleRate.store(rate);
    chorusSampleRate.store(rate);
    flangerSampleRate.store(rate);
    phaserSampleRate.store(rate);
    eqSampleRate.store(rate);
    compressorSampleRate.store(rate);

    // Os buffers do hot path foram pré-alocados com folga em initAudioEngine
    // para o pior caso em MAX_SAMPLE_RATE — não redimensionamos em runtime.
    // Só atualizamos os tamanhos ativos via atomic, que o audio callback lê
    // lock-free.

    int newDelaySamples = std::min((int)(MAX_DELAY_TIME * rate), (int)delayBuffer.size());
    MAX_DELAY_SAMPLES.store(newDelaySamples);
    delayBufferSize.store(newDelaySamples);
    delayBufferIndex.store(0);

    int newReverbSize = std::min(rate / 10, (int)reverbBuffer.size()); // 100ms de reverb
    REVERB_BUFFER_SIZE.store(newReverbSize);
    reverbIndex.store(0);

    int modBufferSize = std::min(rate, (int)chorusBuffer.size()); // 1 segundo, limitado
    chorusBufferSize.store(modBufferSize);
    chorusBufferIndex.store(0);
    flangerBufferSize.store(modBufferSize);
    flangerBufferIndex.store(0);
    phaserBufferSize.store(modBufferSize);
    phaserBufferIndex.store(0);

    // Reverb tail e looper tracks não estão no hot-path de processSample
    // (são usados em features separadas) — mantemos o resize protegido.
    if (looperReverbTailDecay.load() > 0.0f) {
        int reverbTailSize = std::min((int)(looperReverbTailDecay.load() * rate), MAX_BUFFER_SIZE);
        looperReverbTailSize.store(reverbTailSize);
        looperReverbTailBuffer.resize(reverbTailSize, 0.0f);
        looperReverbTailIndex.store(0);
        looperReverbTailDecayCoeff.store(expf(-1.0f / (looperReverbTailDecay.load() * rate)));
    }

    int looperMaxSamples = std::min(rate * 30, MAX_BUFFER_SIZE); // 30 segundos, limitado
    LOOPER_MAX_SAMPLES.store(looperMaxSamples);
    for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
        looperTracks[i].resizeBuffer(looperMaxSamples);
    }

    int oversampleSize = std::min(4096 * oversamplingFactor.load(), (int)oversampleBuffer.size());
    oversampleBufferSize.store(oversampleSize);
}

void startMetronome(int bpm) {
    metronomeBpm = bpm;
    metronomeSamplesPerBeat = (int)((60.0 / metronomeBpm) * metronomeSampleRate);
    metronomeSampleCounter = 0;
    metronomeBeatCount = 0;
    metronomeActive = true;
}

void stopMetronome() {
    metronomeActive = false;
    metronomeSampleCounter = 0;
    metronomeBeatCount = 0;
}

void setMetronomeVolume(float volume) {
    metronomeVolume = std::max(0.0f, std::min(1.0f, volume));
}

void setMetronomeTimeSignature(int beats) {
    metronomeTimeSignature = std::max(1, std::min(16, beats));
}

bool isMetronomeActive() {
    return metronomeActive;
}

// Gera um click melhorado com envelope e diferentes sons para downbeat/upbeat
static float getMetronomeSample() {
    if (!metronomeActive) return 0.0f;
    
    if (metronomeSampleCounter == 0) {
        metronomeClick = true;
        metronomeBeatCount = (metronomeBeatCount + 1) % metronomeTimeSignature;
    }
    
    float click = 0.0f;
    if (metronomeClick && metronomeSampleCounter < metronomeClickDuration) {
        // Calcular envelope de amplitude (attack e decay)
        float envelope = 0.0f;
        if (metronomeSampleCounter < 20) {
            // Attack rápido (20 samples)
            envelope = (float)metronomeSampleCounter / 20.0f;
        } else {
            // Decay exponencial
            float decayTime = (float)(metronomeSampleCounter - 20) / (metronomeClickDuration - 20);
            envelope = expf(-decayTime * 3.0f); // Decay mais rápido
        }
        
        // Determinar se é downbeat (primeira batida do compasso) ou upbeat
        bool isDownbeat = (metronomeBeatCount == 0);
        
        // Frequências diferentes para downbeat e upbeat
        float frequency = isDownbeat ? 800.0f : 1200.0f; // Downbeat mais grave
        float amplitude = isDownbeat ? 1.0f : 0.7f; // Downbeat mais alto
        
        // Gerar som com múltiplas frequências para som mais rico
        float fundamental = sinf(2.0f * 3.14159f * frequency * metronomeSampleCounter / metronomeSampleRate);
        float harmonic1 = 0.5f * sinf(2.0f * 3.14159f * frequency * 2.0f * metronomeSampleCounter / metronomeSampleRate);
        float harmonic2 = 0.3f * sinf(2.0f * 3.14159f * frequency * 3.0f * metronomeSampleCounter / metronomeSampleRate);
        
        click = amplitude * envelope * metronomeVolume * (fundamental + harmonic1 + harmonic2);
    }
    
    if (metronomeSampleCounter >= metronomeClickDuration) {
        metronomeClick = false;
    }
    
    metronomeSampleCounter++;
    if (metronomeSampleCounter >= metronomeSamplesPerBeat) {
        metronomeSampleCounter = 0;
    }
    
    return click;
}

void startLooperRecording() {
    // Verificar quantização se habilitada
    if (looperQuantizationEnabled && looperQuantizationSamples > 0) {
        // Aguardar até o próximo grid
        int currentBeat = (int)((float)metronomeSampleCounter / metronomeSamplesPerBeat);
        float currentBeatFraction = (float)(metronomeSampleCounter % metronomeSamplesPerBeat) / metronomeSamplesPerBeat;
        
        // Calcular quantos samples faltam para o próximo grid
        int samplesToNextGrid = (int)((looperQuantizationGrid - currentBeatFraction) * metronomeSamplesPerBeat);
        if (samplesToNextGrid > 0) {
            looperQuantizationCounter = samplesToNextGrid;
            return; // Aguardar quantização
        }
    }

    looperRecording = true;
    looperPlaying = false;
    looperWriteIndex = 0;

    // Resetar contadores de fade
    looperFadeInCounter = 0;
    looperFadeOutCounter.store(0);

    // Encontrar próxima faixa disponível
    bool foundTrack = false;
    for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
        if (!looperTracks[i].active) {
            currentTrack = i;
            foundTrack = true;
            break;
        }
    }

    // Se não encontrou faixa disponível, usar a primeira
    if (!foundTrack) {
        currentTrack = 0;
    }

    // Limpar buffer da faixa atual
    std::fill(looperTracks[currentTrack].buffer.begin(), looperTracks[currentTrack].buffer.end(), 0.0f);
    looperTracks[currentTrack].length = 0;
    looperTracks[currentTrack].volume = 1.0f;
    looperTracks[currentTrack].muted = false;
    looperTracks[currentTrack].soloed = false;
    looperTracks[currentTrack].active = true;

    // Atualizar estado da notificação
    looperNotificationState = "recording";
}

void stopLooperRecording() {
    looperRecording = false;
    if (looperWriteIndex > 0) {
        looperTracks[currentTrack].length.store(looperWriteIndex.load());
    }
    looperReadIndex = 0;
    
    // Atualizar estado da notificação
    looperNotificationState = "stopped";
}

void startLooperPlayback() {
    if (looperTracks[currentTrack].length > 0) {
        looperPlaying = true;
        looperReadIndex = 0;
        
        // Configurar fade in se habilitado
        if (looperAutoFadeInEnabled) {
            looperFadeInCounter = 0;
            looperFadeInSamples = (int)(looperFadeInDuration * looperSampleRate);
        }
        
        // Atualizar estado da notificação
        looperNotificationState = "playing";
    }
}

void stopLooperPlayback() {
    looperPlaying = false;
    
    // Configurar fade out se habilitado
    if (looperAutoFadeOutEnabled) {
        looperFadeOutSamples = (int)(looperFadeOutDuration * looperSampleRate);
        looperFadeOutCounter.store(looperFadeOutSamples);
    }
    
    // Atualizar estado da notificação
    looperNotificationState = "stopped";
}

void clearLooper() {
    looperRecording = false;
    looperPlaying = false;
    looperWriteIndex = 0;
    looperReadIndex = 0;
    currentTrack = 0;
    
    // Limpar todas as faixas
    for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
        std::fill(looperTracks[i].buffer.begin(), looperTracks[i].buffer.end(), 0.0f);
        looperTracks[i].length = 0;
        looperTracks[i].volume = 1.0f;
        looperTracks[i].muted = false;
        looperTracks[i].soloed = false;
        looperTracks[i].active = false;
    }
}

bool isLooperRecording() { return looperRecording; }
bool isLooperPlaying() { return looperPlaying; }

void setGainEnabled(bool enabled) { gainEnabled.store(enabled); }
void setDistortionEnabled(bool enabled) { distortionEnabled.store(enabled); }
void setDelayEnabled(bool enabled) { delayEnabled.store(enabled); }
void setReverbEnabled(bool enabled) { reverbEnabled.store(enabled); }

void setDistortionType(int type) { distortionType.store(type); }
void setDistortionMix(float mix) { distortionMix.store(mix); }
void setDelayMix(float mix) { delayMix.store(mix); }
void setReverbMix(float mix) { reverbMix.store(mix); }

void setChorusEnabled(bool enabled) { chorusEnabled = enabled; }
void setChorusDepth(float depth) { chorusDepth = depth; }
void setChorusRate(float rate) { chorusRate = rate; }
void setChorusMix(float mix) { chorusMix = mix; }

void setFlangerEnabled(bool enabled) { flangerEnabled = enabled; }
void setFlangerDepth(float depth) { flangerDepth = depth; }
void setFlangerRate(float rate) { flangerRate = rate; }
void setFlangerFeedback(float feedback) { flangerFeedback = feedback; }
void setFlangerMix(float mix) { flangerMix = mix; }

void setPhaserEnabled(bool enabled) { phaserEnabled = enabled; }
void setPhaserDepth(float depth) { phaserDepth = depth; }
void setPhaserRate(float rate) { phaserRate = rate; }
void setPhaserFeedback(float feedback) { phaserFeedback = feedback; }
void setPhaserMix(float mix) { phaserMix = mix; }

void setEQEnabled(bool enabled) { eqEnabled = enabled; }
void setEQLow(float gain) { eqLowGain = gain; }
void setEQMid(float gain) { eqMidGain = gain; }
void setEQHigh(float gain) { eqHighGain = gain; }
void setEQMix(float mix) { eqMix = mix; }

void setCompressorEnabled(bool enabled) { compressorEnabled = enabled; }
void setCompressorThreshold(float threshold) { compressorThreshold = threshold; }
void setCompressorRatio(float ratio) { compressorRatio = ratio; }
void setCompressorAttack(float attack) { compressorAttack = attack; }
void setCompressorRelease(float release) { compressorRelease = release; }
void setCompressorMix(float mix) { compressorMix = mix; }

void setReverbType(int type) {
    reverbType = type;
}

float processSample(float input) {
    // Verificar se o engine foi inicializado
    if (!isEngineInitialized.load()) {
        return input; // Passthrough se não inicializado
    }

    float output = input;

    // Aplicar ganho
    if (gainEnabled.load()) {
        output *= currentGain.load();
    }
    
    // Aplicar distorção
    if (distortionEnabled.load()) {
        float amount = distortionAmount.load();
        if (amount > 0.0f) {
            output = output * (1.0f + amount * output * output);
        }
    }
    
    // Aplicar delay — lock-free. O vetor delayBuffer é pré-alocado em
    // initAudioEngine para o pior caso e nunca redimensiona em runtime.
    // delayBufferSize é lido atomically; o tamanho do vetor subjacente é
    // >= delayBufferSize sempre, então a indexação é segura.
    if (delayEnabled.load()) {
        const int currentDelaySize = delayBufferSize.load();
        int currentDelayIndex = delayBufferIndex.load();
        if (currentDelaySize > 0) {
            if (currentDelayIndex < 0 || currentDelayIndex >= currentDelaySize) {
                currentDelayIndex = 0;
            }
            float delayedSample = delayBuffer[currentDelayIndex];
            output += delayedSample * delayFeedback.load();
            delayBuffer[currentDelayIndex] = output;
            currentDelayIndex = (currentDelayIndex + 1) % currentDelaySize;
            delayBufferIndex.store(currentDelayIndex);
        }
    }

    // Aplicar reverb — mesma lógica: buffer pré-alocado, lock-free.
    if (reverbEnabled.load()) {
        const int currentReverbSize = REVERB_BUFFER_SIZE.load();
        int currentReverbIndex = reverbIndex.load();
        if (currentReverbSize > 0) {
            if (currentReverbIndex < 0 || currentReverbIndex >= currentReverbSize) {
                currentReverbIndex = 0;
            }
            float reverbSample = reverbBuffer[currentReverbIndex];
            output = output * (1.0f - reverbRoomSize.load()) + reverbSample * reverbRoomSize.load();
            reverbBuffer[currentReverbIndex] = output;
            currentReverbIndex = (currentReverbIndex + 1) % currentReverbSize;
            reverbIndex.store(currentReverbIndex);
        }
    }
    
    // Aplicar outros efeitos com verificações similares...
    // (chorus, flanger, phaser, eq, compressor)
    
    return output;
}

void upsample(const float* input, float* output, int numSamples) {
    for (int i = 0; i < numSamples; ++i) {
        output[i * oversamplingFactor] = input[i];
        // Interpolação linear para amostras intermediárias
        for (int j = 1; j < oversamplingFactor; ++j) {
            float alpha = (float)j / oversamplingFactor;
            if (i < numSamples - 1) {
                output[i * oversamplingFactor + j] = 
                    (1.0f - alpha) * input[i] + alpha * input[i + 1];
            } else {
                output[i * oversamplingFactor + j] = input[i];
            }
        }
    }
}

void downsample(const float* input, float* output, int numSamples) {
    for (int i = 0; i < numSamples; ++i) {
        output[i] = input[i * oversamplingFactor];
    }
}

void processBuffer(float* input, float* output, int numSamples, int inputLength, int outputLength) {
    // Audio callback — hot path. Zero locks, zero allocs, zero I/O.
    if (input == nullptr || output == nullptr || numSamples <= 0) {
        return;
    }

    // Passthrough se a engine não foi inicializada.
    if (!isEngineInitialized.load()) {
        int copySize = std::min(numSamples, std::min(inputLength, outputLength));
        if (copySize > 0) {
            memcpy(output, input, copySize * sizeof(float));
        }
        return;
    }

    // Clamp no tamanho real dos buffers passados.
    if (inputLength < numSamples || outputLength < numSamples) {
        int available = std::min(inputLength, outputLength);
        if (available <= 0) return;
        numSamples = available;
    }

    // Fast path: nenhum efeito ativo → passthrough.
    const bool hasActiveEffects = gainEnabled.load() || distortionEnabled.load() || delayEnabled.load() ||
                                  reverbEnabled.load() || chorusEnabled.load() || flangerEnabled.load() ||
                                  phaserEnabled.load() || eqEnabled.load() || compressorEnabled.load();
    if (!hasActiveEffects) {
        memcpy(output, input, numSamples * sizeof(float));
        return;
    }

    // Oversampling. Os buffers são pré-alocados em initAudioEngine para o
    // pior caso (MAX_BUFFER_SIZE * MAX_OVERSAMPLING_FACTOR) — nunca
    // redimensionam em runtime.
    bool oversampling = oversamplingEnabled.load();
    int factor = oversamplingFactor.load();
    if (factor < 1 || factor > MAX_OVERSAMPLING_FACTOR) {
        factor = 1;
        oversampling = false;
    }

    if (!oversampling || factor <= 1) {
        for (int i = 0; i < numSamples; ++i) {
            output[i] = processSample(input[i]);
        }
    } else {
        const int oversampleCapacity = (int)oversampleBuffer.size();
        int oversampledSize = numSamples * factor;
        if (oversampledSize > oversampleCapacity) {
            oversampledSize = oversampleCapacity;
            numSamples = oversampledSize / factor;
        }

        upsample(input, oversampleBuffer.data(), numSamples);
        for (int i = 0; i < oversampledSize; ++i) {
            downsampleBuffer[i] = processSample(oversampleBuffer[i]);
        }
        downsample(downsampleBuffer.data(), output, numSamples);
    }
}

void startTuner() {
    // Zerar ring e histórico. Feito fora do audio callback, antes de
    // tunerActive virar true.
    std::memset(tunerRing, 0, sizeof(tunerRing));
    tunerRingWrite.store(0, std::memory_order_release);
    for (int i = 0; i < FREQ_SMOOTH_SIZE; ++i) freqHistory[i] = 0.0f;
    freqHistoryIdx.store(0);
    detectedFrequency.store(0.0f);
    tunerActive.store(true);
}

void stopTuner() {
    tunerActive.store(false);
    detectedFrequency.store(0.0f);
}

bool isTunerActive() { return tunerActive.load(); }

float getDetectedFrequency() {
    // Consumer path — roda fora do audio callback. Faz snapshot dos últimos
    // ~100ms do ring buffer e roda a detecção de pitch aqui, atualizando o
    // valor cached via atomic.
    if (!tunerActive.load()) return detectedFrequency.load();

    int sr = tunerSampleRate.load();
    int windowSize = sr / 10; // 100ms
    if (windowSize > TUNER_SCRATCH_MAX) windowSize = TUNER_SCRATCH_MAX;
    if (windowSize <= 0) return detectedFrequency.load();

    // Precisa de pelo menos 1 janela cheia de samples.
    uint32_t w = tunerRingWrite.load(std::memory_order_acquire);
    if (w < (uint32_t)windowSize) return detectedFrequency.load();

    std::lock_guard<std::mutex> lock(tunerScratchMutex);
    uint32_t start = w - (uint32_t)windowSize;
    for (int i = 0; i < windowSize; ++i) {
        tunerScratch[i] = tunerRing[(start + i) & (TUNER_RING_CAPACITY - 1)];
    }

    float freq = detectPitch(tunerScratch, windowSize, sr);
    int idx = freqHistoryIdx.load();
    freqHistory[idx] = freq;
    freqHistoryIdx.store((idx + 1) % FREQ_SMOOTH_SIZE);

    float sum = 0.0f;
    int count = 0;
    for (int i = 0; i < FREQ_SMOOTH_SIZE; ++i) {
        if (freqHistory[i] > 0.0f) { sum += freqHistory[i]; count++; }
    }
    float smoothed = (count > 0) ? (sum / count) : 0.0f;
    detectedFrequency.store(smoothed);
    return smoothed;
}

// Função auxiliar: autocorrelação normalizada para pitch detection
static float detectPitch(const float* buffer, int numSamples, int sampleRate) {
    int minLag = sampleRate / 1000; // 1000 Hz
    int maxLag = sampleRate / 50;   // 50 Hz
    float maxNormCorr = 0.0f;
    int bestLag = 0;
    // Threshold de energia (ignorar silêncio/ruído)
    float energy = 0.0f;
    for (int i = 0; i < numSamples; ++i) energy += buffer[i] * buffer[i];
    if (energy / numSamples < 1e-5f) return 0.0f;
    for (int lag = minLag; lag < maxLag; ++lag) {
        float corr = 0.0f, norm1 = 0.0f, norm2 = 0.0f;
        for (int i = 0; i < numSamples - lag; ++i) {
            corr += buffer[i] * buffer[i + lag];
            norm1 += buffer[i] * buffer[i];
            norm2 += buffer[i + lag] * buffer[i + lag];
        }
        float normCorr = (norm1 > 0 && norm2 > 0) ? corr / sqrtf(norm1 * norm2) : 0.0f;
        if (normCorr > maxNormCorr) {
            maxNormCorr = normCorr;
            bestLag = lag;
        }
    }
    if (bestLag > 0 && maxNormCorr > 0.7f) {
        return (float)sampleRate / bestLag;
    } else {
        return 0.0f;
    }
}

void processTunerBuffer(const float* input, int numSamples) {
    // Audio callback — lock-free. Só copia samples no ring buffer
    // pré-alocado e avança o write index atomicamente. A detecção de pitch
    // (cara) roda em getDetectedFrequency, chamada pelo UI thread.
    if (!tunerActive.load() || numSamples <= 0 || input == nullptr) return;
    uint32_t w = tunerRingWrite.load(std::memory_order_relaxed);
    for (int i = 0; i < numSamples; ++i) {
        tunerRing[(w + (uint32_t)i) & (TUNER_RING_CAPACITY - 1)] = input[i];
    }
    tunerRingWrite.store(w + (uint32_t)numSamples, std::memory_order_release);
}

float getDelayTime() { return delayTime; }
float getDelayFeedback() { return delayFeedback; }
float getReverbRoomSize() { return reverbRoomSize; }
float getReverbDamping() { return reverbDamping; }

void setEffectOrder(const char** order, int count) {
    effectOrder.clear();
    for (int i = 0; i < count; ++i) {
        effectOrder.push_back(order[i]);
    }
}

float processSampleWithOversampling(float input) {
    if (!oversamplingEnabled || oversamplingFactor <= 1) {
        return processSample(input);
    }
    
    // Upsampling
    float upsampledInput = input * oversamplingFactor; // Normalizar amplitude
    
    // Processar na taxa alta
    float upsampledOutput = processSample(upsampledInput);
    
    // Downsampling com filtro anti-aliasing simples
    float output = upsampledOutput / oversamplingFactor; // Normalizar amplitude
    
    return output;
}

// Funções para controlar Oversampling — os buffers foram pré-alocados em
// initAudioEngine para o pior caso (MAX_BUFFER_SIZE * MAX_OVERSAMPLING_FACTOR),
// então os setters só atualizam atomics sem tocar nos vetores.
void setOversamplingEnabled(bool enabled) {
    oversamplingEnabled = enabled;
}

void setOversamplingFactor(int factor) {
    if (factor < 1 || factor > MAX_OVERSAMPLING_FACTOR) {
        return;
    }
    int newBufferSize = 4096 * factor;
    if (newBufferSize > (int)oversampleBuffer.size()) {
        factor = (int)oversampleBuffer.size() / 4096;
        newBufferSize = 4096 * factor;
    }
    oversamplingFactor = factor;
    oversampleBufferSize.store(newBufferSize);
}

bool isOversamplingEnabled() {
    return oversamplingEnabled.load();
}

int getOversamplingFactor() {
    return oversamplingFactor.load();
}

// Implementações das novas funções do looper avançado

int getLooperLength() {
    int track = currentTrack.load();
    if (track < 0 || track >= LOOPER_MAX_TRACKS) return 0;
    return looperTracks[track].length;
}

int getLooperPosition() {
    if (looperRecording) {
        return looperWriteIndex;
    } else {
        return looperReadIndex;
    }
}

void setLooperPosition(int position) {
    if (position >= 0 && position < looperLength) {
        looperReadIndex = position;
        looperWriteIndex = position;
    }
}

void setLooperTrackVolume(int trackIndex, float volume) {
    if (trackIndex >= 0 && trackIndex < LOOPER_MAX_TRACKS) {
        looperTracks[trackIndex].volume = volume;
    }
}

void setLooperTrackMuted(int trackIndex, bool muted) {
    if (trackIndex >= 0 && trackIndex < LOOPER_MAX_TRACKS) {
        looperTracks[trackIndex].muted = muted;
    }
}

void setLooperTrackSoloed(int trackIndex, bool soloed) {
    if (trackIndex >= 0 && trackIndex < LOOPER_MAX_TRACKS) {
        looperTracks[trackIndex].soloed = soloed;
    }
}

void removeLooperTrack(int trackIndex) {
    if (trackIndex >= 0 && trackIndex < LOOPER_MAX_TRACKS) {
        // Limpar a faixa
        std::fill(looperTracks[trackIndex].buffer.begin(), looperTracks[trackIndex].buffer.end(), 0.0f);
        looperTracks[trackIndex].length = 0;
        looperTracks[trackIndex].volume = 1.0f;
        looperTracks[trackIndex].muted = false;
        looperTracks[trackIndex].soloed = false;
        looperTracks[trackIndex].active = false;
        
        // Se era a faixa atual, encontrar próxima faixa ativa
        if (trackIndex == currentTrack) {
            for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
                if (looperTracks[i].active) {
                    currentTrack = i;
                    break;
                }
            }
        }
    }
}

void setLooperBPM(int bpm) {
    if (bpm >= 60 && bpm <= 200) {
        looperBPM = bpm;
    }
}

void setLooperSyncEnabled(bool enabled) {
    looperSyncEnabled = enabled;
}

float* getLooperMix(int* outLength) {
    // Encontrar o maior comprimento de faixa
    int maxLength = 0;
    for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
        if (looperTracks[i].active && looperTracks[i].length > 0) {
            if (looperTracks[i].length > maxLength) {
                maxLength = looperTracks[i].length;
            }
        }
    }

    if (maxLength == 0) {
        if (outLength) *outLength = 0;
        return nullptr;
    }

    float* mix = new float[maxLength];
    for (int i = 0; i < maxLength; i++) {
        float sum = 0.0f;
        for (int t = 0; t < LOOPER_MAX_TRACKS; t++) {
            if (looperTracks[t].active && looperTracks[t].length > i) {
                sum += looperTracks[t].buffer[i] * looperTracks[t].volume;
            }
        }
        if (sum > 1.0f) sum = 1.0f;
        if (sum < -1.0f) sum = -1.0f;
        mix[i] = sum;
    }

    if (outLength) *outLength = maxLength;
    return mix;
}

void releaseLooperMix(float* buffer) {
    delete[] buffer;
}

void loadLooperFromAudio(const float* audioData, int length) {
    if (audioData == nullptr || length <= 0 || length > LOOPER_MAX_SAMPLES) {
        return;
    }
    
    // Parar gravação e reprodução se estiverem ativas
    looperRecording = false;
    looperPlaying = false;
    
    // Limpar todas as faixas existentes
    for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
        std::fill(looperTracks[i].buffer.begin(), looperTracks[i].buffer.end(), 0.0f);
        looperTracks[i].length = 0;
        looperTracks[i].volume = 1.0f;
        looperTracks[i].muted = false;
        looperTracks[i].soloed = false;
        looperTracks[i].active = false;
    }
    
    // Carregar o áudio na primeira faixa
    std::copy(audioData, audioData + length, looperTracks[0].buffer.begin());
    looperTracks[0].length = length;
    looperTracks[0].active = true;
    looperTracks[0].volume = 1.0f;
    looperTracks[0].muted = false;
    looperTracks[0].soloed = false;
    
    // Definir como faixa atual
    currentTrack = 0;
    
    // Atualizar comprimento do looper
    looperLength = length;
    looperWriteIndex = 0;
    looperReadIndex = 0;
}

// Implementações das funcionalidades especiais do looper

void setLooperReverse(bool enabled) {
    looperReverse = enabled;
}

void setLooperSpeed(float speed) {
    if (speed >= 0.25f && speed <= 4.0f) {
        looperSpeed = speed;
    }
}

void setLooperPitchShift(float semitones) {
    if (semitones >= -12.0f && semitones <= 12.0f) {
        looperPitchShift = semitones;
    }
}

void setLooperStutter(bool enabled, float rate) {
    looperStutter = enabled;
    if (rate >= 0.1f && rate <= 20.0f) {
        looperStutterRate = rate;
    }
}

bool isLooperReverseEnabled() {
    return looperReverse;
}

float getLooperSpeed() {
    return looperSpeed;
}

float getLooperPitchShift() {
    return looperPitchShift;
}

bool isLooperStutterEnabled() {
    return looperStutter;
}

float getLooperStutterRate() {
    return looperStutterRate;
}

// Implementações das funcionalidades de Slicing

void setLooperSlicingEnabled(bool enabled) {
    looperSlicingEnabled = enabled;
    if (enabled && numSlicePoints == 0) {
        // Se não há pontos definidos, criar slices automáticos baseados no BPM
        int maxLength = 0;
        for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
            if (looperTracks[i].active && looperTracks[i].length > maxLength) {
                maxLength = looperTracks[i].length;
            }
        }
        
        if (maxLength > 0) {
            // Criar 8 slices automáticos
            numSlicePoints = 8;
            sliceLength = maxLength / numSlicePoints;
            for (int i = 0; i < numSlicePoints; i++) {
                slicePoints[i] = i * sliceLength;
                sliceOrder[i] = i;
            }
        }
    }
}

void setLooperSlicePoints(const int* points, int numPoints) {
    if (points == nullptr || numPoints <= 0 || numPoints > MAX_SLICES) {
        return;
    }
    
    numSlicePoints = numPoints;
    for (int i = 0; i < numPoints; i++) {
        slicePoints[i] = points[i];
        sliceOrder[i] = i;
    }
    
    // Calcular comprimento do slice baseado no primeiro ponto
    if (numPoints > 1) {
        sliceLength = slicePoints[1] - slicePoints[0];
    }
}

void setLooperSliceLength(int length) {
    if (length > 0) {
        sliceLength = length;
    }
}

bool isLooperSlicingEnabled() {
    return looperSlicingEnabled;
}

int getLooperSliceLength() {
    return sliceLength;
}

int getLooperNumSlices() {
    return numSlicePoints;
}

void setLooperSliceOrder(const int* order, int numSlices) {
    if (order == nullptr || numSlices <= 0 || numSlices > MAX_SLICES) {
        return;
    }
    
    for (int i = 0; i < numSlices; i++) {
        sliceOrder[i] = order[i];
    }
}

void randomizeLooperSlices() {
    // Algoritmo Fisher-Yates para embaralhar
    for (int i = numSlicePoints - 1; i > 0; i--) {
        int j = rand() % (i + 1);
        int temp = sliceOrder[i];
        sliceOrder[i] = sliceOrder[j];
        sliceOrder[j] = temp;
    }
    sliceRandomized = true;
}

void reverseLooperSlices() {
    for (int i = 0; i < numSlicePoints / 2; i++) {
        int temp = sliceOrder[i];
        sliceOrder[i] = sliceOrder[numSlicePoints - 1 - i];
        sliceOrder[numSlicePoints - 1 - i] = temp;
    }
}

// === IMPLEMENTAÇÕES DOS EFEITOS AVANÇADOS DO LOOPER (FASE 5) ===

// Compressão automática
void setLooperAutoCompression(bool enabled) {
    looperAutoCompressionEnabled = enabled;
    if (!enabled) {
        looperCompressionEnvelope = 0.0f;
        looperCompressionGain = 1.0f;
    }
}

void setLooperCompressionThreshold(float threshold) {
    looperCompressionThreshold = threshold;
}

void setLooperCompressionRatio(float ratio) {
    looperCompressionRatio = ratio;
}

void setLooperCompressionAttack(float attack) {
    looperCompressionAttack = attack;
}

void setLooperCompressionRelease(float release) {
    looperCompressionRelease = release;
}

bool isLooperAutoCompressionEnabled() {
    return looperAutoCompressionEnabled;
}

float getLooperCompressionThreshold() {
    return looperCompressionThreshold;
}

float getLooperCompressionRatio() {
    return looperCompressionRatio;
}

float getLooperCompressionAttack() {
    return looperCompressionAttack;
}

float getLooperCompressionRelease() {
    return looperCompressionRelease;
}

// Normalização automática
void setLooperAutoNormalization(bool enabled) {
    looperAutoNormalizationEnabled = enabled;
    if (!enabled) {
        looperNormalizationGain = 1.0f;
        looperPeakLevel = 0.0f;
    }
}

void setLooperNormalizationTarget(float target) {
    looperNormalizationTarget = target;
}

bool isLooperAutoNormalizationEnabled() {
    return looperAutoNormalizationEnabled;
}

float getLooperNormalizationTarget() {
    return looperNormalizationTarget;
}

// Filtros
void setLooperLowPassFilter(bool enabled) {
    looperLowPassEnabled = enabled;
    if (!enabled) {
        looperLowPassX1 = looperLowPassX2 = looperLowPassY1 = looperLowPassY2 = 0.0f;
    }
}

void setLooperLowPassFrequency(float frequency) {
    looperLowPassFrequency = frequency;
}

void setLooperHighPassFilter(bool enabled) {
    looperHighPassEnabled = enabled;
    if (!enabled) {
        looperHighPassX1 = looperHighPassX2 = looperHighPassY1 = looperHighPassY2 = 0.0f;
    }
}

void setLooperHighPassFrequency(float frequency) {
    looperHighPassFrequency = frequency;
}

bool isLooperLowPassEnabled() {
    return looperLowPassEnabled;
}

bool isLooperHighPassEnabled() {
    return looperHighPassEnabled;
}

float getLooperLowPassFrequency() {
    return looperLowPassFrequency;
}

float getLooperHighPassFrequency() {
    return looperHighPassFrequency;
}

// Reverb de cauda
void setLooperReverbTail(bool enabled) {
    looperReverbTailEnabled = enabled;
    if (!enabled) {
        // Limpar buffer de reverb
        std::fill(looperReverbTailBuffer.begin(), looperReverbTailBuffer.end(), 0.0f);
        looperReverbTailIndex = 0;
    }
}

void setLooperReverbTailDecay(float decay) {
    looperReverbTailDecay = decay;
    looperReverbTailSize = (int)(decay * looperSampleRate);
    looperReverbTailBuffer.resize(looperReverbTailSize, 0.0f);
    looperReverbTailDecayCoeff = expf(-1.0f / (decay * looperSampleRate));
    looperReverbTailIndex = 0;
}

void setLooperReverbTailMix(float mix) {
    looperReverbTailMix = mix;
}

bool isLooperReverbTailEnabled() {
    return looperReverbTailEnabled;
}

float getLooperReverbTailDecay() {
    return looperReverbTailDecay;
}

float getLooperReverbTailMix() {
    return looperReverbTailMix;
}

// === IMPLEMENTAÇÕES DA FASE 6: INTEGRAÇÃO AVANÇADA ===

// Quantização
void setLooperQuantization(bool enabled) {
    looperQuantizationEnabled = enabled;
    if (enabled) {
        // Calcular samples por grid baseado no BPM atual
        looperQuantizationSamples = (int)(looperQuantizationGrid * metronomeSamplesPerBeat);
    } else {
        looperQuantizationSamples = 0;
        looperQuantizationCounter = 0;
    }
}

void setLooperQuantizationGrid(float gridSize) {
    looperQuantizationGrid = gridSize;
    if (looperQuantizationEnabled) {
        looperQuantizationSamples = (int)(gridSize * metronomeSamplesPerBeat);
    }
}

bool isLooperQuantizationEnabled() {
    return looperQuantizationEnabled;
}

float getLooperQuantizationGrid() {
    return looperQuantizationGrid;
}

// Fade In/Out automático
void setLooperAutoFadeIn(bool enabled) {
    looperAutoFadeInEnabled = enabled;
    if (enabled) {
        looperFadeInSamples = (int)(looperFadeInDuration * looperSampleRate);
    }
}

void setLooperAutoFadeOut(bool enabled) {
    looperAutoFadeOutEnabled = enabled;
}

void setLooperFadeInDuration(float duration) {
    looperFadeInDuration = duration;
    if (looperAutoFadeInEnabled) {
        looperFadeInSamples = (int)(duration * looperSampleRate);
    }
}

void setLooperFadeOutDuration(float duration) {
    looperFadeOutDuration = duration;
}

bool isLooperAutoFadeInEnabled() {
    return looperAutoFadeInEnabled;
}

bool isLooperAutoFadeOutEnabled() {
    return looperAutoFadeOutEnabled;
}

float getLooperFadeInDuration() {
    return looperFadeInDuration;
}

float getLooperFadeOutDuration() {
    return looperFadeOutDuration;
}

// Integração MIDI
void setLooperMidiEnabled(bool enabled) {
    looperMidiEnabled = enabled;
}

void setLooperMidiChannel(int channel) {
    looperMidiChannel = channel;
}

void setLooperMidiCCMapping(int ccNumber, int function) {
    if (ccNumber >= 0 && ccNumber < 128) {
        looperMidiCCMapping[ccNumber] = function;
    }
}

bool isLooperMidiEnabled() {
    return looperMidiEnabled;
}

int getLooperMidiChannel() {
    return looperMidiChannel;
}

void processLooperMidiMessage(int status, int data1, int data2) {
    if (!looperMidiEnabled) return;
    
    int channel = status & 0x0F;
    int messageType = status & 0xF0;
    
    // Verificar se é o canal correto
    if (channel != looperMidiChannel) return;
    
    // Processar mensagens CC (Control Change)
    if (messageType == 0xB0) { // CC
        int ccNumber = data1;
        int ccValue = data2;
        
        if (ccNumber >= 0 && ccNumber < 128 && looperMidiCCMapping[ccNumber] >= 0) {
            int function = looperMidiCCMapping[ccNumber];
            
            // Ativar função baseado no valor CC
            bool activate = ccValue >= 64; // threshold de 64
            
            switch (function) {
                case 0: // Record
                    if (activate && !looperRecording) {
                        startLooperRecording();
                    } else if (!activate && looperRecording) {
                        stopLooperRecording();
                    }
                    break;
                case 1: // Play
                    if (activate && !looperPlaying) {
                        startLooperPlayback();
                    } else if (!activate && looperPlaying) {
                        stopLooperPlayback();
                    }
                    break;
                case 2: // Stop
                    if (activate) {
                        stopLooperRecording();
                        stopLooperPlayback();
                    }
                    break;
                case 3: // Clear
                    if (activate) {
                        clearLooper();
                    }
                    break;
            }
            
            looperMidiCCActive[ccNumber] = activate;
        }
    }
}

// Notificações
void setLooperNotificationEnabled(bool enabled) {
    looperNotificationEnabled = enabled;
}

void setLooperNotificationControls(bool showControls) {
    looperNotificationControlsEnabled = showControls;
}

bool isLooperNotificationEnabled() {
    return looperNotificationEnabled;
}

bool isLooperNotificationControlsEnabled() {
    return looperNotificationControlsEnabled;
}

void updateLooperNotificationState() {
    // Esta função será chamada pela interface Java para atualizar a notificação
    // O estado atual está em looperNotificationState
}

void cutLooperRegion(float start, float end) {
    // Encontrar a faixa ativa mais longa
    int maxLength = 0;
    int targetTrack = -1;
    
    for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
        if (looperTracks[i].active && looperTracks[i].length > maxLength) {
            maxLength = looperTracks[i].length;
            targetTrack = i;
        }
    }
    
    if (targetTrack == -1 || maxLength == 0) return;
    
    int startSample = static_cast<int>(start * maxLength);
    int endSample = static_cast<int>(end * maxLength);
    
    // Garantir que startSample <= endSample
    if (startSample > endSample) {
        std::swap(startSample, endSample);
    }
    
    // Garantir limites válidos
    startSample = std::max(0, std::min(startSample, maxLength));
    endSample = std::max(0, std::min(endSample, maxLength));
    
    // Remover a região selecionada
    float* buffer = looperTracks[targetTrack].buffer.data();
    int newLength = maxLength - (endSample - startSample);
    
    // Mover os samples após a região cortada
    for (int i = endSample; i < maxLength; i++) {
        buffer[i - (endSample - startSample)] = buffer[i];
    }
    
    // Atualizar comprimento da faixa
    looperTracks[targetTrack].length = newLength;
    
    // Atualizar comprimento do looper
    looperLength = newLength;
    
    // Ajustar posição atual se necessário
    if (looperReadIndex >= looperLength) {
        looperReadIndex = 0;
    }
}

void applyLooperFadeIn(float start, float end) {
    // Encontrar a faixa ativa mais longa
    int maxLength = 0;
    int targetTrack = -1;
    
    for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
        if (looperTracks[i].active && looperTracks[i].length > maxLength) {
            maxLength = looperTracks[i].length;
            targetTrack = i;
        }
    }
    
    if (targetTrack == -1 || maxLength == 0) return;
    
    int startSample = static_cast<int>(start * maxLength);
    int endSample = static_cast<int>(end * maxLength);
    
    // Garantir que startSample <= endSample
    if (startSample > endSample) {
        std::swap(startSample, endSample);
    }
    
    // Garantir limites válidos
    startSample = std::max(0, std::min(startSample, maxLength));
    endSample = std::max(0, std::min(endSample, maxLength));
    
    int fadeLength = endSample - startSample;
    if (fadeLength <= 0) return;
    
    // Aplicar fade in (volume crescente de 0 a 1)
    float* buffer = looperTracks[targetTrack].buffer.data();
    for (int i = 0; i < fadeLength; ++i) {
        float fadeFactor = static_cast<float>(i) / fadeLength;
        buffer[startSample + i] *= fadeFactor;
    }
}

void applyLooperFadeOut(float start, float end) {
    // Encontrar a faixa ativa mais longa
    int maxLength = 0;
    int targetTrack = -1;
    
    for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
        if (looperTracks[i].active && looperTracks[i].length > maxLength) {
            maxLength = looperTracks[i].length;
            targetTrack = i;
        }
    }
    
    if (targetTrack == -1 || maxLength == 0) return;
    
    int startSample = static_cast<int>(start * maxLength);
    int endSample = static_cast<int>(end * maxLength);
    
    // Garantir que startSample <= endSample
    if (startSample > endSample) {
        std::swap(startSample, endSample);
    }
    
    // Garantir limites válidos
    startSample = std::max(0, std::min(startSample, maxLength));
    endSample = std::max(0, std::min(endSample, maxLength));
    
    int fadeLength = endSample - startSample;
    if (fadeLength <= 0) return;
    
    // Aplicar fade out (volume decrescente de 1 a 0)
    float* buffer = looperTracks[targetTrack].buffer.data();
    for (int i = 0; i < fadeLength; ++i) {
        float fadeFactor = 1.0f - (static_cast<float>(i) / fadeLength);
        buffer[startSample + i] *= fadeFactor;
    }
}

// Métodos getter para parâmetros de efeitos (sem sufixo, para uso externo via header)
float getGain() {
    return currentGain.load();
}

float getDistortion() {
    return distortionAmount.load();
}



