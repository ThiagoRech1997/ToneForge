#include "audio_engine.h"
#include <cmath>
#include <cstring>
#include <atomic>
#include <thread>
#include <chrono>
#include <vector>
#include <mutex>
#include <string>

// Mutexes para proteção thread-safe
static std::mutex audioEngineMutex;
static std::mutex oversamplingMutex;
static std::mutex delayMutex;
static std::mutex looperMutex;
static std::mutex effectsMutex;
static std::mutex tunerMutex;

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
            printf("LooperTrack: Tamanho inicial inválido, usando %d\n", MIN_BUFFER_SIZE);
        }
    }
    
    void resizeBuffer(int newSize) {
        if (newSize > 0 && newSize <= MAX_BUFFER_SIZE) {
            std::lock_guard<std::mutex> lock(looperMutex);
            buffer.resize(newSize, 0.0f);
            printf("LooperTrack: Buffer redimensionado para %d amostras\n", newSize);
        } else {
            printf("LooperTrack: Tamanho de buffer inválido: %d\n", newSize);
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
                printf("LooperTrack: Buffer cheio, gravação parada\n");
            } else {
                currentLength = std::max(currentLength, currentPosition);
            }
            
            position.store(currentPosition);
            length.store(currentLength);
        } else {
            printf("LooperTrack: Índice de posição inválido: %d, resetando\n", currentPosition);
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
static std::atomic<bool> tunerActive{false};
static std::atomic<float> detectedFrequency{0.0f};
static std::vector<float> tunerBuffer;
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
    
    // Inicializar buffers de oversampling
    int oversampleSize = 4096 * oversamplingFactor.load();
    oversampleBufferSize.store(oversampleSize);
    oversampleBuffer.resize(oversampleSize);
    downsampleBuffer.resize(oversampleSize);
    
    // Inicializar buffers de delay
    int currentSampleRate = sampleRate.load();
    int delaySize = (int)(MAX_DELAY_TIME * currentSampleRate);
    MAX_DELAY_SAMPLES.store(delaySize);
    delayBufferSize.store(delaySize);
    delayBuffer.resize(delaySize, 0.0f);
    delayBufferIndex.store(0);
    
    // Inicializar buffer de reverb
    reverbBuffer.resize(REVERB_BUFFER_SIZE.load(), 0.0f);
    reverbIndex.store(0);
    
    // Inicializar buffers de modulação
    int modBufferSize = currentSampleRate; // 1 segundo
    chorusBufferSize.store(modBufferSize);
    chorusBuffer.resize(modBufferSize, 0.0f);
    chorusBufferIndex.store(0);
    
    flangerBufferSize.store(modBufferSize);
    flangerBuffer.resize(modBufferSize, 0.0f);
    flangerBufferIndex.store(0);
    
    phaserBufferSize.store(modBufferSize);
    phaserBuffer.resize(modBufferSize, 0.0f);
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
    
    printf("initAudioEngine: inicializado com taxa de amostragem %d Hz\n", currentSampleRate);
}

void cleanupAudioEngine() {
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

void setDelay(float time, float feedback) {
    delayTime = time;
    delayFeedback = feedback;
    int newDelayBufferSize = (int)(time * sampleRate);
    if (newDelayBufferSize > MAX_DELAY_SAMPLES) {
        newDelayBufferSize = MAX_DELAY_SAMPLES.load();
    }
    delayBufferSize.store(newDelayBufferSize);
    delayBuffer.resize(delayBufferSize.load(), 0.0f);
}

void setDelayTime(float timeMs) {
    delayTimeMs = timeMs;
    if (!delaySyncBPM) {
        // Converter ms para segundos e calcular samples
        delayTime = timeMs / 1000.0f;
        int newDelayBufferSize = (int)(delayTime.load() * sampleRate);
        if (newDelayBufferSize > MAX_DELAY_SAMPLES) {
            newDelayBufferSize = MAX_DELAY_SAMPLES.load();
        }
        delayBufferSize.store(newDelayBufferSize);
        delayBuffer.resize(delayBufferSize.load(), 0.0f);
    } else {
        // Calcular tempo baseado no BPM
        float beatLength = 60.0f / delayBPM.load();
        delayTime = beatLength / 4.0f; // Divisão por 4 (semínima)
        int newDelayBufferSize = (int)(delayTime.load() * sampleRate);
        if (newDelayBufferSize > MAX_DELAY_SAMPLES) {
            newDelayBufferSize = MAX_DELAY_SAMPLES.load();
        }
        delayBufferSize.store(newDelayBufferSize);
        delayBuffer.resize(delayBufferSize.load(), 0.0f);
    }
}

void setDelaySyncBPM(bool sync) {
    delaySyncBPM = sync;
    if (sync) {
        // Calcular tempo baseado no BPM
        float beatTime = 60.0f / delayBPM.load(); // segundos por batida
        delayTime = beatTime; // 1/4 nota
        int newDelayBufferSize = (int)(delayTime.load() * sampleRate);
        if (newDelayBufferSize > MAX_DELAY_SAMPLES) {
            newDelayBufferSize = MAX_DELAY_SAMPLES.load();
        }
        delayBufferSize.store(newDelayBufferSize);
    } else {
        // Usar tempo em ms
        setDelayTime(delayTimeMs);
    }
}

void setDelayBPM(int bpm) {
    delayBPM = bpm;
    if (delaySyncBPM) {
        // Recalcular tempo de delay baseado no novo BPM
        float beatLength = 60.0f / bpm;
        delayTime = beatLength / 4.0f; // Divisão por 4 (semínima)
        int newDelayBufferSize = (int)(delayTime.load() * sampleRate);
        if (newDelayBufferSize > MAX_DELAY_SAMPLES) {
            newDelayBufferSize = MAX_DELAY_SAMPLES.load();
        }
        delayBufferSize.store(newDelayBufferSize);
        delayBuffer.resize(delayBufferSize.load(), 0.0f);
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
        printf("setSampleRate: taxa de amostragem inválida: %d (deve estar entre %d e %d)\n", 
               rate, MIN_SAMPLE_RATE, MAX_SAMPLE_RATE);
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
    
    // Ajustar buffers de delay baseados na nova taxa
    int newDelaySamples = (int)(MAX_DELAY_TIME * rate);
    MAX_DELAY_SAMPLES.store(newDelaySamples);
    
    std::lock_guard<std::mutex> delayLock(delayMutex);
    if (newDelaySamples > 0 && newDelaySamples <= MAX_BUFFER_SIZE) {
        delayBuffer.resize(newDelaySamples, 0.0f);
        delayBufferSize.store(newDelaySamples);
        delayBufferIndex.store(0);
        printf("setSampleRate: Buffer de delay redimensionado para %d amostras\n", newDelaySamples);
    } else {
        printf("setSampleRate: Tamanho de buffer de delay inválido: %d\n", newDelaySamples);
    }
    
    // Ajustar buffer de reverb baseado na nova taxa
    int newReverbSize = std::min(rate / 10, MAX_BUFFER_SIZE); // 100ms de reverb
    REVERB_BUFFER_SIZE.store(newReverbSize);
    reverbBuffer.resize(newReverbSize, 0.0f);
    reverbIndex.store(0);
    
    // Ajustar buffers de modulação
    int modBufferSize = std::min(rate, MAX_BUFFER_SIZE); // 1 segundo, limitado
    std::lock_guard<std::mutex> effectsLock(effectsMutex);
    
    chorusBufferSize.store(modBufferSize);
    chorusBuffer.resize(modBufferSize, 0.0f);
    chorusBufferIndex.store(0);
    
    flangerBufferSize.store(modBufferSize);
    flangerBuffer.resize(modBufferSize, 0.0f);
    flangerBufferIndex.store(0);
    
    phaserBufferSize.store(modBufferSize);
    phaserBuffer.resize(modBufferSize, 0.0f);
    phaserBufferIndex.store(0);
    
    // Ajustar buffer de reverb de cauda do looper
    if (looperReverbTailDecay.load() > 0.0f) {
        int reverbTailSize = std::min((int)(looperReverbTailDecay.load() * rate), MAX_BUFFER_SIZE);
        looperReverbTailSize.store(reverbTailSize);
        looperReverbTailBuffer.resize(reverbTailSize, 0.0f);
        looperReverbTailIndex.store(0);
        looperReverbTailDecayCoeff.store(expf(-1.0f / (looperReverbTailDecay.load() * rate)));
    }
    
    // Ajustar buffers do looper
    int looperMaxSamples = std::min(rate * 30, MAX_BUFFER_SIZE); // 30 segundos, limitado
    LOOPER_MAX_SAMPLES.store(looperMaxSamples);
    for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
        looperTracks[i].resizeBuffer(looperMaxSamples);
    }
    
    // Ajustar buffers de oversampling
    std::lock_guard<std::mutex> oversamplingLock(oversamplingMutex);
    int oversampleSize = std::min(4096 * oversamplingFactor.load(), MAX_BUFFER_SIZE);
    oversampleBufferSize.store(oversampleSize);
    oversampleBuffer.resize(oversampleSize);
    downsampleBuffer.resize(oversampleSize);
    
    printf("setSampleRate: Taxa de amostragem alterada para %d Hz\n", rate);
    printf("setSampleRate: Buffers ajustados - Delay: %d, Reverb: %d, Mod: %d, Looper: %d\n",
           newDelaySamples, newReverbSize, modBufferSize, looperMaxSamples);
}

void startMetronome(int bpm) {
    metronomeBpm = bpm;
    metronomeSamplesPerBeat = (int)((60.0 / metronomeBpm) * metronomeSampleRate);
    metronomeSampleCounter = 0;
    metronomeBeatCount = 0;
    metronomeActive = true;
    printf("Metronome: iniciado com %d BPM, volume %.2f\n", bpm, metronomeVolume.load());
}

void stopMetronome() {
    metronomeActive = false;
    metronomeSampleCounter = 0;
    metronomeBeatCount = 0;
}

void setMetronomeVolume(float volume) {
    metronomeVolume = std::max(0.0f, std::min(1.0f, volume));
    printf("Metronome: volume alterado para %.2f\n", metronomeVolume.load());
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
            printf("startLooperRecording: aguardando quantização (%d samples)\n", samplesToNextGrid);
            return; // Aguardar quantização
        }
    }
    
    looperRecording = true;
    looperPlaying = false;
    looperWriteIndex = 0;
    
    // Resetar contadores de fade
    looperFadeInCounter = 0;
            looperFadeOutCounter.store(0);
    
    printf("startLooperRecording: iniciando gravação\n");
    
    // Encontrar próxima faixa disponível
    bool foundTrack = false;
    for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
        if (!looperTracks[i].active) {
            currentTrack = i;
            foundTrack = true;
            printf("startLooperRecording: usando track %d (disponível)\n", i);
            break;
        }
    }
    
    // Se não encontrou faixa disponível, usar a primeira
    if (!foundTrack) {
        currentTrack = 0;
        printf("startLooperRecording: usando track 0 (primeira)\n");
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
    
    printf("startLooperRecording: track %d configurada para gravação\n", currentTrack.load());
}

void stopLooperRecording() {
    looperRecording = false;
    printf("stopLooperRecording: looperWriteIndex=%d, currentTrack=%d\n", looperWriteIndex.load(), currentTrack.load());
    if (looperWriteIndex > 0) {
        looperTracks[currentTrack].length.store(looperWriteIndex.load());
        printf("stopLooperRecording: definido length=%d para track %d\n", looperWriteIndex.load(), currentTrack.load());
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
    
    // Aplicar delay com verificações de segurança
    if (delayEnabled.load()) {
        std::lock_guard<std::mutex> lock(delayMutex);
        
        int currentDelaySize = delayBufferSize.load();
        int currentDelayIndex = delayBufferIndex.load();
        
        if (currentDelaySize > 0 && currentDelaySize <= MAX_BUFFER_SIZE) {
            // Verificar se o índice está dentro dos limites
            if (currentDelayIndex >= 0 && currentDelayIndex < currentDelaySize) {
                float delayedSample = delayBuffer[currentDelayIndex];
                output += delayedSample * delayFeedback.load();
                
                // Atualizar buffer de delay com verificação de limites
                delayBuffer[currentDelayIndex] = output;
                currentDelayIndex = (currentDelayIndex + 1) % currentDelaySize;
                delayBufferIndex.store(currentDelayIndex);
            } else {
                // Resetar índice se estiver fora dos limites
                delayBufferIndex.store(0);
                printf("processSample: Índice de delay fora dos limites, resetando\n");
            }
        } else {
            printf("processSample: Tamanho de buffer de delay inválido: %d\n", currentDelaySize);
        }
    }
    
    // Aplicar reverb com verificações de segurança
    if (reverbEnabled.load()) {
        std::lock_guard<std::mutex> lock(effectsMutex);
        
        int currentReverbSize = REVERB_BUFFER_SIZE.load();
        int currentReverbIndex = reverbIndex.load();
        
        if (currentReverbSize > 0 && currentReverbSize <= MAX_BUFFER_SIZE) {
            // Verificar se o índice está dentro dos limites
            if (currentReverbIndex >= 0 && currentReverbIndex < currentReverbSize) {
                float reverbSample = reverbBuffer[currentReverbIndex];
                output = output * (1.0f - reverbRoomSize.load()) + reverbSample * reverbRoomSize.load();
                
                // Atualizar buffer de reverb com verificação de limites
                reverbBuffer[currentReverbIndex] = output;
                currentReverbIndex = (currentReverbIndex + 1) % currentReverbSize;
                reverbIndex.store(currentReverbIndex);
            } else {
                // Resetar índice se estiver fora dos limites
                reverbIndex.store(0);
                printf("processSample: Índice de reverb fora dos limites, resetando\n");
            }
        } else {
            printf("processSample: Tamanho de buffer de reverb inválido: %d\n", currentReverbSize);
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

void processBuffer(float* input, float* output, int numSamples) {
    // Verificação rápida para evitar processamento desnecessário
    if (input == nullptr || output == nullptr || numSamples <= 0) {
        printf("processBuffer: Parâmetros inválidos - input: %p, output: %p, numSamples: %d\n", 
               input, output, numSamples);
        return;
    }
    
    // Verificar se há efeitos ativos para evitar processamento desnecessário
    bool hasActiveEffects = gainEnabled.load() || distortionEnabled.load() || delayEnabled.load() || 
                           reverbEnabled.load() || chorusEnabled.load() || flangerEnabled.load() || 
                           phaserEnabled.load() || eqEnabled.load() || compressorEnabled.load();
    
    if (!hasActiveEffects) {
        // Se não há efeitos ativos, apenas copiar o buffer
        memcpy(output, input, numSamples * sizeof(float));
        return;
    }
    
    // Verificar oversampling de forma thread-safe
    bool oversampling = oversamplingEnabled.load();
    int factor = oversamplingFactor.load();
    
    // Validar fator de oversampling
    if (factor < 1 || factor > MAX_OVERSAMPLING_FACTOR) {
        printf("processBuffer: Fator de oversampling inválido: %d, usando 1x\n", factor);
        factor = 1;
        oversampling = false;
    }
    
    if (!oversampling || factor <= 1) {
        // Processamento normal sem oversampling
        for (int i = 0; i < numSamples; ++i) {
            output[i] = processSample(input[i]);
        }
    } else {
        // Processamento com oversampling otimizado
        int oversampledSize = numSamples * factor;
        
        // Verificar se o tamanho não excede os limites
        if (oversampledSize > MAX_BUFFER_SIZE) {
            printf("processBuffer: Tamanho de buffer oversampled muito grande: %d, limitando\n", oversampledSize);
            oversampledSize = MAX_BUFFER_SIZE;
            numSamples = oversampledSize / factor;
        }
        
        // Garantir que os buffers tenham tamanho suficiente com proteção thread-safe
        std::lock_guard<std::mutex> lock(oversamplingMutex);
        if (oversampleBuffer.size() < oversampledSize) {
            oversampleBuffer.resize(oversampledSize);
            downsampleBuffer.resize(oversampledSize);
            oversampleBufferSize.store(oversampledSize);
            printf("processBuffer: Buffers de oversampling redimensionados para %d\n", oversampledSize);
        }
        
        // Upsampling
        upsample(input, oversampleBuffer.data(), numSamples);
        
        // Processar na taxa alta
        for (int i = 0; i < oversampledSize; ++i) {
            downsampleBuffer[i] = processSample(oversampleBuffer[i]);
        }
        
        // Downsampling
        downsample(downsampleBuffer.data(), output, numSamples);
    }
}

void startTuner() {
    std::lock_guard<std::mutex> lock(tunerMutex);
    tunerActive = true;
    tunerBuffer.clear();
}

void stopTuner() {
    std::lock_guard<std::mutex> lock(tunerMutex);
    tunerActive = false;
    tunerBuffer.clear();
    detectedFrequency = 0.0f;
}

bool isTunerActive() { return tunerActive; }
float getDetectedFrequency() { return detectedFrequency; }

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
    if (!tunerActive || numSamples <= 0 || input == nullptr) return;
    std::lock_guard<std::mutex> lock(tunerMutex);
    tunerBuffer.insert(tunerBuffer.end(), input, input + numSamples);
    int windowSize = tunerSampleRate / 10; // 100ms
    if ((int)tunerBuffer.size() >= windowSize) {
        float freq = detectPitch(tunerBuffer.data(), windowSize, tunerSampleRate);
        // Filtro de média móvel para suavizar
        freqHistory[freqHistoryIdx] = freq;
        freqHistoryIdx = (freqHistoryIdx + 1) % FREQ_SMOOTH_SIZE;
        float sum = 0.0f; int count = 0;
        for (int i = 0; i < FREQ_SMOOTH_SIZE; ++i) {
            if (freqHistory[i] > 0.0f) { sum += freqHistory[i]; count++; }
        }
        detectedFrequency = (count > 0) ? (sum / count) : 0.0f;
        tunerBuffer.erase(tunerBuffer.begin(), tunerBuffer.begin() + windowSize/2); // overlap
    }
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

// Funções para controlar Oversampling
void setOversamplingEnabled(bool enabled) {
    std::lock_guard<std::mutex> lock(oversamplingMutex);
    oversamplingEnabled = enabled;
    printf("setOversamplingEnabled: %s\n", enabled ? "true" : "false");
}

void setOversamplingFactor(int factor) {
    std::lock_guard<std::mutex> lock(oversamplingMutex);
    
    // Validar fator de oversampling
    if (factor < 1 || factor > MAX_OVERSAMPLING_FACTOR) {
        printf("setOversamplingFactor: Fator inválido %d, deve estar entre 1 e %d\n", 
               factor, MAX_OVERSAMPLING_FACTOR);
        return;
    }
    
    // Verificar se o novo fator não causará problemas de memória
    int currentSampleRate = sampleRate.load();
    int newBufferSize = 4096 * factor;
    
    if (newBufferSize > MAX_BUFFER_SIZE) {
        printf("setOversamplingFactor: Buffer muito grande %d, limitando fator\n", newBufferSize);
        factor = MAX_BUFFER_SIZE / 4096;
        newBufferSize = 4096 * factor;
    }
    
    oversamplingFactor = factor;
    
    // Redimensionar buffers se necessário
    if (oversampleBuffer.size() < newBufferSize) {
        oversampleBuffer.resize(newBufferSize);
        downsampleBuffer.resize(newBufferSize);
        oversampleBufferSize.store(newBufferSize);
        printf("setOversamplingFactor: Buffers redimensionados para %d amostras\n", newBufferSize);
    }
    
    printf("setOversamplingFactor: %dx oversampling configurado\n", factor);
}

bool isOversamplingEnabled() {
    return oversamplingEnabled.load();
}

int getOversamplingFactor() {
    return oversamplingFactor.load();
}

// Implementações das novas funções do looper avançado

int getLooperLength() {
    int length = looperTracks[currentTrack].length;
    printf("getLooperLength: currentTrack=%d, length=%d\n", currentTrack.load(), length);
    return length;
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
    int activeTracks = 0;
    
    for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
        if (looperTracks[i].active && looperTracks[i].length > 0) {
            activeTracks++;
            if (looperTracks[i].length > maxLength) {
                maxLength = looperTracks[i].length;
            }
        }
    }
    
    // Log para debug
    printf("getLooperMix: activeTracks=%d, maxLength=%d\n", activeTracks, maxLength);
    
    if (maxLength == 0) {
        *outLength = 0;
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
        // Limitar para evitar clipping
        if (sum > 1.0f) sum = 1.0f;
        if (sum < -1.0f) sum = -1.0f;
        mix[i] = sum;
    }
    
    *outLength = maxLength;
    printf("getLooperMix: retornando mix com %d samples\n", maxLength);
    return mix;
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

// Métodos getter para parâmetros de efeitos
extern "C" float getGainNative() {
    return currentGain.load();
}

extern "C" float getDistortionNative() {
    return distortionAmount.load();
}

extern "C" float getDelayTimeNative() {
    return delayTime.load();
}

extern "C" float getDelayFeedbackNative() {
    return delayFeedback.load();
}

extern "C" float getReverbRoomSizeNative() {
    return reverbRoomSize.load();
}

extern "C" float getReverbDampingNative() {
    return reverbDamping.load();
}

extern "C" bool isOversamplingEnabledNative() {
    return oversamplingEnabled.load();
}

extern "C" int getOversamplingFactorNative() {
    return oversamplingFactor.load();
}



