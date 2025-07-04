#include "audio_engine.h"
#include <cmath>
#include <cstring>
#include <atomic>
#include <thread>
#include <chrono>
#include <vector>
#include <mutex>
#include <string>

// Configurações de Oversampling
static int oversamplingFactor = 2; // 2x, 4x, 8x
static bool oversamplingEnabled = true;
static std::vector<float> oversampleBuffer;
static std::vector<float> downsampleBuffer;
static int oversampleBufferSize = 0;

// Parâmetros dos efeitos
static float currentGain = 1.0f;
static float distortionAmount = 0.0f;
static float delayTime = 0.0f;
static float delayFeedback = 0.0f;
static float delayTimeMs = 200.0f;  // Tempo em milissegundos
static bool delaySyncBPM = false;   // Sincronizar com BPM
static int delayBPM = 120;          // BPM para sincronização
static float reverbRoomSize = 0.0f;
static float reverbDamping = 0.0f;

// Buffer de delay (máximo 1 segundo a 48kHz)
static const int MAX_DELAY_SAMPLES = 48000;
static const float MAX_DELAY_TIME = 1.0f; // 1 segundo
static const int SAMPLE_RATE = 48000;
static std::vector<float> delayBuffer;
static int delayBufferIndex = 0;
static int delayBufferSize = 0;

// Buffer de reverb simples
static const int REVERB_BUFFER_SIZE = 4096;
static std::vector<float> reverbBuffer;
static int reverbIndex = 0;

// Taxa de amostragem (será configurada)
static int sampleRate = 48000;

// --- Metrônomo ---
static std::atomic<bool> metronomeActive{false};
static int metronomeBpm = 120;
static int metronomeSampleRate = 48000;
static int metronomeSamplesPerBeat = 24000; // 120 BPM default
static int metronomeSampleCounter = 0;
static bool metronomeClick = false;
static int metronomeBeatCount = 0; // Contador de batidas para acentos
static int metronomeTimeSignature = 4; // Compasso 4/4 por padrão
static float metronomeVolume = 0.6f; // Volume do metrônomo
static int metronomeClickDuration = 150; // Duração do click em samples

// --- Looper ---
static const int LOOPER_MAX_SAMPLES = 48000 * 30; // até 30 segundos a 48kHz
static const int LOOPER_MAX_TRACKS = 8; // máximo 8 faixas

// Estrutura para uma faixa do looper
struct LooperTrack {
    float buffer[LOOPER_MAX_SAMPLES];
    int length = 0;
    float volume = 1.0f;
    bool muted = false;
    bool soloed = false;
    bool active = false;
};

static LooperTrack looperTracks[LOOPER_MAX_TRACKS];
static int currentTrack = 0;
static int looperLength = 0;
static int looperWriteIndex = 0;
static int looperReadIndex = 0;
static bool looperRecording = false;
static bool looperPlaying = false;
static bool looperSyncEnabled = false;
static int looperBPM = 120;
static int looperSampleRate = 48000;

// Funcionalidades especiais do looper
static bool looperReverse = false;
static float looperSpeed = 1.0f;
static float looperPitchShift = 0.0f;
static bool looperStutter = false;
static float looperStutterRate = 4.0f; // 4 Hz por padrão
static float looperStutterPhase = 0.0f;
static int looperStutterCounter = 0;

// Funcionalidade de Slicing
static bool looperSlicingEnabled = false;
static const int MAX_SLICES = 16;
static int slicePoints[MAX_SLICES];
static int numSlicePoints = 0;
static int sliceLength = 0; // comprimento de cada slice
static int sliceOrder[MAX_SLICES]; // ordem de reprodução dos slices
static int currentSliceIndex = 0;
static int slicePlaybackPosition = 0;
static bool sliceRandomized = false;

// === FASE 5: EFEITOS AVANÇADOS PARA LOOPER ===

// Compressão automática para looper
static bool looperAutoCompressionEnabled = false;
static float looperCompressionThreshold = -20.0f; // dB
static float looperCompressionRatio = 4.0f;
static float looperCompressionAttack = 10.0f; // ms
static float looperCompressionRelease = 100.0f; // ms
static float looperCompressionEnvelope = 0.0f;
static float looperCompressionGain = 1.0f;

// Normalização automática
static bool looperAutoNormalizationEnabled = false;
static float looperNormalizationTarget = -3.0f; // dB
static float looperNormalizationGain = 1.0f;
static float looperPeakLevel = 0.0f;

// Filtros para looper
static bool looperLowPassEnabled = false;
static float looperLowPassFrequency = 8000.0f; // Hz
static float looperLowPassX1 = 0.0f, looperLowPassX2 = 0.0f;
static float looperLowPassY1 = 0.0f, looperLowPassY2 = 0.0f;

static bool looperHighPassEnabled = false;
static float looperHighPassFrequency = 80.0f; // Hz
static float looperHighPassX1 = 0.0f, looperHighPassX2 = 0.0f;
static float looperHighPassY1 = 0.0f, looperHighPassY2 = 0.0f;

// Reverb de cauda entre loops
static bool looperReverbTailEnabled = false;
static float looperReverbTailDecay = 2.0f; // segundos
static float looperReverbTailMix = 0.3f;
static std::vector<float> looperReverbTailBuffer;
static int looperReverbTailIndex = 0;
static int looperReverbTailSize = 0;
static float looperReverbTailDecayCoeff = 0.0f;

// --- Afinador (Tuner) ---
static bool tunerActive = false;
static float detectedFrequency = 0.0f;
static std::vector<float> tunerBuffer;
static int tunerSampleRate = 48000;
static std::mutex tunerMutex;

// Parâmetros para filtro de média móvel
static const int FREQ_SMOOTH_SIZE = 5;
static float freqHistory[FREQ_SMOOTH_SIZE] = {0};
static int freqHistoryIdx = 0;

// Flags de ativação dos efeitos
static bool gainEnabled = true;
static bool distortionEnabled = true;
static bool delayEnabled = true;
static bool reverbEnabled = true;

static std::vector<std::string> effectOrder = {"Ganho", "Distorção", "Chorus", "Flanger", "Phaser", "EQ", "Compressor", "Delay", "Reverb"};

static int distortionType = 0; // 0=Soft, 1=Hard, 2=Fuzz, 3=Overdrive
static float distortionMix = 1.0f;
static float delayMix = 1.0f;
static float reverbMix = 1.0f;

// Chorus
static bool chorusEnabled = false;
static float chorusDepth = 0.02f; // 20 ms
static float chorusRate = 1.0f;   // 1 Hz
static float chorusMix = 0.5f;
static int chorusSampleRate = 48000;
static int chorusBufferSize = 48000; // 1 segundo
static std::vector<float> chorusBuffer(chorusBufferSize, 0.0f);
static int chorusBufferIndex = 0;
static float chorusPhase = 0.0f;

// Flanger
static bool flangerEnabled = false;
static float flangerDepth = 0.003f; // 3 ms
static float flangerRate = 0.3f;   // 0.3 Hz
static float flangerFeedback = 0.5f;
static float flangerMix = 0.5f;
static int flangerSampleRate = 48000;
static int flangerBufferSize = 48000; // 1 segundo
static std::vector<float> flangerBuffer(flangerBufferSize, 0.0f);
static int flangerBufferIndex = 0;
static float flangerPhase = 0.0f;

// Phaser
static bool phaserEnabled = false;
static float phaserDepth = 0.8f;    // Profundidade da modulação (0-1)
static float phaserRate = 0.5f;     // Taxa de modulação em Hz
static float phaserFeedback = 0.6f; // Feedback do phaser
static float phaserMix = 0.5f;      // Mix dry/wet
static int phaserSampleRate = 48000;
static int phaserBufferSize = 48000; // 1 segundo
static std::vector<float> phaserBuffer(phaserBufferSize, 0.0f);
static int phaserBufferIndex = 0;
static float phaserPhase = 0.0f;
static float phaserLfo = 0.0f;      // Valor atual do LFO

// Equalizer (EQ)
static bool eqEnabled = false;
static float eqLowGain = 0.0f;      // Ganho para graves (60Hz)
static float eqMidGain = 0.0f;      // Ganho para médios (1kHz)
static float eqHighGain = 0.0f;     // Ganho para agudos (8kHz)
static float eqMix = 1.0f;          // Mix dry/wet
static int eqSampleRate = 48000;

// Filtros do EQ (estados dos filtros)
static float eqLowX1 = 0.0f, eqLowX2 = 0.0f, eqLowY1 = 0.0f, eqLowY2 = 0.0f;
static float eqMidX1 = 0.0f, eqMidX2 = 0.0f, eqMidY1 = 0.0f, eqMidY2 = 0.0f;
static float eqHighX1 = 0.0f, eqHighX2 = 0.0f, eqHighY1 = 0.0f, eqHighY2 = 0.0f;

// Compressor
static bool compressorEnabled = false;
static float compressorThreshold = -20.0f;  // dB
static float compressorRatio = 4.0f;        // 4:1
static float compressorAttack = 10.0f;      // ms
static float compressorRelease = 100.0f;    // ms
static float compressorMix = 1.0f;          // Mix dry/wet
static int compressorSampleRate = 48000;

// Estado do compressor
static float compressorEnvelope = 0.0f;     // Envelope detector
static float compressorGain = 1.0f;         // Gain reduction

static int reverbType = 0; // 0=Hall, 1=Plate, 2=Spring

// === FASE 6: INTEGRAÇÃO AVANÇADA ===

// Quantização
static bool looperQuantizationEnabled = false;
static float looperQuantizationGrid = 0.25f; // 1/4 de batida por padrão
static int looperQuantizationSamples = 0; // samples por grid
static int looperQuantizationCounter = 0; // contador para alinhamento

// Fade In/Out automático
static bool looperAutoFadeInEnabled = false;
static bool looperAutoFadeOutEnabled = false;
static float looperFadeInDuration = 0.1f; // segundos
static float looperFadeOutDuration = 0.1f; // segundos
static int looperFadeInSamples = 0;
static int looperFadeOutSamples = 0;
static int looperFadeInCounter = 0;
static int looperFadeOutCounter = 0;

// Integração MIDI
static bool looperMidiEnabled = false;
static int looperMidiChannel = 0; // canal 1 (0-based)
static int looperMidiCCMapping[128]; // mapeamento CC -> função
static bool looperMidiCCActive[128]; // estado dos CCs

// Notificações
static bool looperNotificationEnabled = false;
static bool looperNotificationControlsEnabled = false;
static std::string looperNotificationState = "stopped"; // stopped, recording, playing

void initAudioEngine() {
    // Inicializar buffers de oversampling
    oversampleBufferSize = 4096 * oversamplingFactor;
    oversampleBuffer.resize(oversampleBufferSize);
    downsampleBuffer.resize(oversampleBufferSize);
    
    // Inicializar outros buffers
    delayBufferSize = (int)(MAX_DELAY_TIME * SAMPLE_RATE);
    delayBuffer.resize(delayBufferSize, 0.0f);
    delayBufferIndex = 0;
    
    reverbBuffer.resize(REVERB_BUFFER_SIZE, 0.0f);
    reverbIndex = 0;
    
    // Inicializar buffers de modulação
    chorusBuffer.resize(chorusBufferSize, 0.0f);
    flangerBuffer.resize(flangerBufferSize, 0.0f);
    phaserBuffer.resize(phaserBufferSize, 0.0f);
    
    // Inicializar reverb de cauda do looper
    looperReverbTailSize = (int)(looperReverbTailDecay * SAMPLE_RATE);
    looperReverbTailBuffer.resize(looperReverbTailSize, 0.0f);
    looperReverbTailIndex = 0;
    looperReverbTailDecayCoeff = expf(-1.0f / (looperReverbTailDecay * SAMPLE_RATE));
    
    // Resetar fases
    chorusPhase = 0.0f;
    flangerPhase = 0.0f;
    phaserPhase = 0.0f;
    phaserLfo = 0.0f;
    
    // Resetar parâmetros
    currentGain = 1.0f;
    distortionAmount = 0.0f;
    delayTime = 0.0f;
    delayFeedback = 0.0f;
    reverbRoomSize = 0.0f;
    reverbDamping = 0.0f;
    
    // Resetar efeitos avançados do looper
    looperCompressionEnvelope = 0.0f;
    looperCompressionGain = 1.0f;
    looperNormalizationGain = 1.0f;
    looperPeakLevel = 0.0f;
    looperLowPassX1 = looperLowPassX2 = looperLowPassY1 = looperLowPassY2 = 0.0f;
    looperHighPassX1 = looperHighPassX2 = looperHighPassY1 = looperHighPassY2 = 0.0f;
    
    // Resetar integração avançada (Fase 6)
    looperQuantizationCounter = 0;
    looperFadeInCounter = 0;
    looperFadeOutCounter = 0;
    
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
    delayBufferSize = (int)(time * sampleRate);
    if (delayBufferSize > MAX_DELAY_SAMPLES) {
        delayBufferSize = MAX_DELAY_SAMPLES;
    }
    delayBuffer.resize(delayBufferSize, 0.0f);
}

void setDelayTime(float timeMs) {
    delayTimeMs = timeMs;
    if (!delaySyncBPM) {
        // Converter ms para segundos e calcular samples
        delayTime = timeMs / 1000.0f;
        delayBufferSize = (int)(delayTime * sampleRate);
        if (delayBufferSize > MAX_DELAY_SAMPLES) {
            delayBufferSize = MAX_DELAY_SAMPLES;
        }
    }
}

void setDelaySyncBPM(bool sync) {
    delaySyncBPM = sync;
    if (sync) {
        // Calcular tempo baseado no BPM
        float beatTime = 60.0f / delayBPM; // segundos por batida
        delayTime = beatTime; // 1/4 nota
        delayBufferSize = (int)(delayTime * sampleRate);
        if (delayBufferSize > MAX_DELAY_SAMPLES) {
            delayBufferSize = MAX_DELAY_SAMPLES;
        }
    } else {
        // Usar tempo em ms
        setDelayTime(delayTimeMs);
    }
}

void setDelayBPM(int bpm) {
    delayBPM = bpm;
    if (delaySyncBPM) {
        // Recalcular tempo baseado no novo BPM
        float beatTime = 60.0f / delayBPM; // segundos por batida
        delayTime = beatTime; // 1/4 nota
        delayBufferSize = (int)(delayTime * sampleRate);
        if (delayBufferSize > MAX_DELAY_SAMPLES) {
            delayBufferSize = MAX_DELAY_SAMPLES;
        }
    }
}

void setReverb(float roomSize, float damping) {
    reverbRoomSize = roomSize;
    reverbDamping = damping;
}

void setSampleRate(int rate) {
    metronomeSampleRate = rate;
    metronomeSamplesPerBeat = (int)((60.0 / metronomeBpm) * metronomeSampleRate);
}

void startMetronome(int bpm) {
    metronomeBpm = bpm;
    metronomeSamplesPerBeat = (int)((60.0 / metronomeBpm) * metronomeSampleRate);
    metronomeSampleCounter = 0;
    metronomeBeatCount = 0;
    metronomeActive = true;
    printf("Metronome: iniciado com %d BPM, volume %.2f\n", bpm, metronomeVolume);
}

void stopMetronome() {
    metronomeActive = false;
    metronomeSampleCounter = 0;
    metronomeBeatCount = 0;
}

void setMetronomeVolume(float volume) {
    metronomeVolume = std::max(0.0f, std::min(1.0f, volume));
    printf("Metronome: volume alterado para %.2f\n", metronomeVolume);
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
    looperFadeOutCounter = 0;
    
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
    memset(looperTracks[currentTrack].buffer, 0, sizeof(looperTracks[currentTrack].buffer));
    looperTracks[currentTrack].length = 0;
    looperTracks[currentTrack].volume = 1.0f;
    looperTracks[currentTrack].muted = false;
    looperTracks[currentTrack].soloed = false;
    looperTracks[currentTrack].active = true;
    
    // Atualizar estado da notificação
    looperNotificationState = "recording";
    
    printf("startLooperRecording: track %d configurada para gravação\n", currentTrack);
}

void stopLooperRecording() {
    looperRecording = false;
    printf("stopLooperRecording: looperWriteIndex=%d, currentTrack=%d\n", looperWriteIndex, currentTrack);
    if (looperWriteIndex > 0) {
        looperTracks[currentTrack].length = looperWriteIndex;
        printf("stopLooperRecording: definido length=%d para track %d\n", looperWriteIndex, currentTrack);
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
        looperFadeOutCounter = looperFadeOutSamples;
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
        memset(looperTracks[i].buffer, 0, sizeof(looperTracks[i].buffer));
        looperTracks[i].length = 0;
        looperTracks[i].volume = 1.0f;
        looperTracks[i].muted = false;
        looperTracks[i].soloed = false;
        looperTracks[i].active = false;
    }
}

bool isLooperRecording() { return looperRecording; }
bool isLooperPlaying() { return looperPlaying; }

void setGainEnabled(bool enabled) { gainEnabled = enabled; }
void setDistortionEnabled(bool enabled) { distortionEnabled = enabled; }
void setDelayEnabled(bool enabled) { delayEnabled = enabled; }
void setReverbEnabled(bool enabled) { reverbEnabled = enabled; }

void setDistortionType(int type) { distortionType = type; }
void setDistortionMix(float mix) { distortionMix = mix; }
void setDelayMix(float mix) { delayMix = mix; }
void setReverbMix(float mix) { reverbMix = mix; }

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
    float dry = input;
    for (const std::string& effect : effectOrder) {
        if (effect == "Ganho") {
            if (gainEnabled) output *= currentGain;
        } else if (effect == "Chorus") {
            if (chorusEnabled) {
                chorusBuffer[chorusBufferIndex] = output;
                float lfo = (sinf(chorusPhase * 2.0f * 3.14159265f) + 1.0f) * 0.5f;
                float delaySamples = chorusDepth * chorusSampleRate * lfo;
                int readIdx = chorusBufferIndex - (int)delaySamples;
                if (readIdx < 0) readIdx += chorusBufferSize;
                float delayed = chorusBuffer[readIdx % chorusBufferSize];
                output = (1.0f - chorusMix) * output + chorusMix * delayed;
                chorusBufferIndex = (chorusBufferIndex + 1) % chorusBufferSize;
                chorusPhase += chorusRate / (float)chorusSampleRate;
                if (chorusPhase > 1.0f) chorusPhase -= 1.0f;
            }
        } else if (effect == "Flanger") {
            if (flangerEnabled) {
                // Flanger: delay modulado por LFO + feedback
                flangerBuffer[flangerBufferIndex] = output + flangerFeedback * flangerBuffer[flangerBufferIndex];
                float lfo = (sinf(flangerPhase * 2.0f * 3.14159265f) + 1.0f) * 0.5f;
                float delaySamples = 1.0f + flangerDepth * flangerSampleRate * lfo; // mínimo 1 sample
                int readIdx = flangerBufferIndex - (int)delaySamples;
                if (readIdx < 0) readIdx += flangerBufferSize;
                float delayed = flangerBuffer[readIdx % flangerBufferSize];
                output = (1.0f - flangerMix) * output + flangerMix * delayed;
                flangerBufferIndex = (flangerBufferIndex + 1) % flangerBufferSize;
                flangerPhase += flangerRate / (float)flangerSampleRate;
                if (flangerPhase > 1.0f) flangerPhase -= 1.0f;
            }
        } else if (effect == "Phaser") {
            if (phaserEnabled) {
                // Phaser: filtros passa-tudo em série com modulação
                float lfo = sinf(phaserPhase * 2.0f * 3.14159265f);
                float modDepth = phaserDepth * 0.5f; // 0-0.5 para evitar instabilidade
                
                // Aplicar filtros passa-tudo em série (4 estágios)
                float filtered = output;
                for (int stage = 0; stage < 4; ++stage) {
                    float freq = 200.0f + 2000.0f * (stage / 3.0f); // 200Hz a 2kHz
                    freq *= (1.0f + modDepth * lfo); // Modular a frequência
                    
                    // Filtro passa-tudo simples
                    float w0 = 2.0f * 3.14159265f * freq / phaserSampleRate;
                    float alpha = sinf(w0) * 0.5f;
                    float b0 = 1.0f - alpha;
                    float b1 = -2.0f * cosf(w0);
                    float b2 = 1.0f + alpha;
                    float a0 = 1.0f + alpha;
                    float a1 = -2.0f * cosf(w0);
                    float a2 = 1.0f - alpha;
                    
                    // Normalizar
                    b0 /= a0; b1 /= a0; b2 /= a0;
                    a1 /= a0; a2 /= a0; a0 = 1.0f;
                    
                    // Aplicar filtro (implementação simplificada)
                    static float x1[4] = {0}, x2[4] = {0}, y1[4] = {0}, y2[4] = {0};
                    float y = b0 * filtered + b1 * x1[stage] + b2 * x2[stage] 
                             - a1 * y1[stage] - a2 * y2[stage];
                    x2[stage] = x1[stage];
                    x1[stage] = filtered;
                    y2[stage] = y1[stage];
                    y1[stage] = y;
                    filtered = y;
                }
                
                // Aplicar feedback
                phaserBuffer[phaserBufferIndex] = filtered + phaserFeedback * phaserBuffer[phaserBufferIndex];
                float wet = phaserBuffer[phaserBufferIndex];
                
                // Mix dry/wet
                output = (1.0f - phaserMix) * output + phaserMix * wet;
                
                // Atualizar buffer e fase
                phaserBufferIndex = (phaserBufferIndex + 1) % phaserBufferSize;
                phaserPhase += phaserRate / (float)phaserSampleRate;
                if (phaserPhase > 1.0f) phaserPhase -= 1.0f;
            }
        } else if (effect == "EQ") {
            if (eqEnabled) {
                // Equalizer: 3 filtros passa-banda (low, mid, high)
                float filtered = output;
                
                // Filtro passa-baixa (graves - 60Hz)
                if (eqLowGain != 0.0f) {
                    float freq = 60.0f;
                    float w0 = 2.0f * 3.14159265f * freq / eqSampleRate;
                    float alpha = sinf(w0) / (2.0f * 0.707f); // Q = 0.707
                    float b0 = (1.0f - cosf(w0)) / 2.0f;
                    float b1 = 1.0f - cosf(w0);
                    float b2 = (1.0f - cosf(w0)) / 2.0f;
                    float a0 = 1.0f + alpha;
                    float a1 = -2.0f * cosf(w0);
                    float a2 = 1.0f - alpha;
                    
                    // Normalizar
                    b0 /= a0; b1 /= a0; b2 /= a0;
                    a1 /= a0; a2 /= a0; a0 = 1.0f;
                    
                    float y = b0 * filtered + b1 * eqLowX1 + b2 * eqLowX2 - a1 * eqLowY1 - a2 * eqLowY2;
                    eqLowX2 = eqLowX1; eqLowX1 = filtered;
                    eqLowY2 = eqLowY1; eqLowY1 = y;
                    
                    // Aplicar ganho
                    float lowBand = y * (eqLowGain > 0 ? (1.0f + eqLowGain) : (1.0f / (1.0f - eqLowGain)));
                    filtered = filtered + (lowBand - y);
                }
                
                // Filtro passa-banda (médios - 1kHz)
                if (eqMidGain != 0.0f) {
                    float freq = 1000.0f;
                    float w0 = 2.0f * 3.14159265f * freq / eqSampleRate;
                    float alpha = sinf(w0) / (2.0f * 0.707f);
                    float b0 = alpha;
                    float b1 = 0.0f;
                    float b2 = -alpha;
                    float a0 = 1.0f + alpha;
                    float a1 = -2.0f * cosf(w0);
                    float a2 = 1.0f - alpha;
                    
                    // Normalizar
                    b0 /= a0; b1 /= a0; b2 /= a0;
                    a1 /= a0; a2 /= a0; a0 = 1.0f;
                    
                    float y = b0 * filtered + b1 * eqMidX1 + b2 * eqMidX2 - a1 * eqMidY1 - a2 * eqMidY2;
                    eqMidX2 = eqMidX1; eqMidX1 = filtered;
                    eqMidY2 = eqMidY1; eqMidY1 = y;
                    
                    // Aplicar ganho
                    float midBand = y * (eqMidGain > 0 ? (1.0f + eqMidGain) : (1.0f / (1.0f - eqMidGain)));
                    filtered = filtered + (midBand - y);
                }
                
                // Filtro passa-alta (agudos - 8kHz)
                if (eqHighGain != 0.0f) {
                    float freq = 8000.0f;
                    float w0 = 2.0f * 3.14159265f * freq / eqSampleRate;
                    float alpha = sinf(w0) / (2.0f * 0.707f);
                    float b0 = (1.0f + cosf(w0)) / 2.0f;
                    float b1 = -(1.0f + cosf(w0));
                    float b2 = (1.0f + cosf(w0)) / 2.0f;
                    float a0 = 1.0f + alpha;
                    float a1 = -2.0f * cosf(w0);
                    float a2 = 1.0f - alpha;
                    
                    // Normalizar
                    b0 /= a0; b1 /= a0; b2 /= a0;
                    a1 /= a0; a2 /= a0; a0 = 1.0f;
                    
                    float y = b0 * filtered + b1 * eqHighX1 + b2 * eqHighX2 - a1 * eqHighY1 - a2 * eqHighY2;
                    eqHighX2 = eqHighX1; eqHighX1 = filtered;
                    eqHighY2 = eqHighY1; eqHighY1 = y;
                    
                    // Aplicar ganho
                    float highBand = y * (eqHighGain > 0 ? (1.0f + eqHighGain) : (1.0f / (1.0f - eqHighGain)));
                    filtered = filtered + (highBand - y);
                }
                
                // Mix dry/wet
                output = (1.0f - eqMix) * output + eqMix * filtered;
            }
        } else if (effect == "Distorção") {
            if (distortionEnabled && distortionAmount > 0.0f) {
                float distorted = output;
                float drive = 1.0f + distortionAmount * 10.0f;
                switch (distortionType) {
                    case 0: // Soft Clip
                        distorted = tanh(distorted * drive) / tanh(drive);
                        break;
                    case 1: // Hard Clip
                        distorted = std::max(-1.0f, std::min(1.0f, distorted * drive));
                        break;
                    case 2: // Fuzz
                        distorted = sinf(distorted * drive);
                        break;
                    case 3: // Overdrive
                        if (distorted > 0)
                            distorted = 1.0f - expf(-distorted * drive);
                        else
                            distorted = -1.0f + expf(distorted * drive);
                        break;
                }
                output = (1.0f - distortionMix) * output + distortionMix * distorted;
            }
        } else if (effect == "Delay") {
            if (delayEnabled && delayTime > 0.0f && delayBufferSize > 0) {
                float delayedSample = delayBuffer[delayBufferIndex];
                float wet = output + delayedSample * delayFeedback;
                output = (1.0f - delayMix) * output + delayMix * wet;
                delayBuffer[delayBufferIndex] = wet;
                delayBufferIndex = (delayBufferIndex + 1) % delayBufferSize;
            }
        } else if (effect == "Reverb") {
            if (reverbEnabled && reverbRoomSize > 0.0f) {
                float wet = 0.0f;
                switch (reverbType) {
                    case 0: // Hall
                        wet = reverbBuffer[reverbIndex];
                        break;
                    case 1: // Plate
                        wet = 0.6f * reverbBuffer[reverbIndex] + 0.4f * reverbBuffer[(reverbIndex + REVERB_BUFFER_SIZE/2) % REVERB_BUFFER_SIZE];
                        break;
                    case 2: // Spring
                        wet = sinf(reverbBuffer[reverbIndex]) * 0.7f + 0.3f * reverbBuffer[(reverbIndex + REVERB_BUFFER_SIZE/4) % REVERB_BUFFER_SIZE];
                        break;
                }
                // Mix dry/wet
                output = (1.0f - reverbMix) * output + reverbMix * wet;
                reverbBuffer[reverbIndex] = output + reverbRoomSize * reverbBuffer[reverbIndex];
                reverbIndex = (reverbIndex + 1) % REVERB_BUFFER_SIZE;
            }
        } else if (effect == "Compressor") {
            if (compressorEnabled) {
                // Compressor: detector de envelope + controle de ganho
                float inputLevel = fabs(output);
                float thresholdLinear = powf(10.0f, compressorThreshold / 20.0f);
                
                // Detector de envelope
                float attackCoeff = expf(-1.0f / (compressorAttack * 0.001f * compressorSampleRate));
                float releaseCoeff = expf(-1.0f / (compressorRelease * 0.001f * compressorSampleRate));
                
                if (inputLevel > compressorEnvelope) {
                    compressorEnvelope = attackCoeff * compressorEnvelope + (1.0f - attackCoeff) * inputLevel;
                } else {
                    compressorEnvelope = releaseCoeff * compressorEnvelope + (1.0f - releaseCoeff) * inputLevel;
                }
                
                // Calcular redução de ganho
                float gainReduction = 1.0f;
                if (compressorEnvelope > thresholdLinear) {
                    float overThreshold = compressorEnvelope / thresholdLinear;
                    float dbOver = 20.0f * log10f(overThreshold);
                    float dbReduction = dbOver * (1.0f - 1.0f / compressorRatio);
                    gainReduction = powf(10.0f, -dbReduction / 20.0f);
                }
                
                // Aplicar compressão
                float compressed = output * gainReduction;
                
                // Mix dry/wet
                output = (1.0f - compressorMix) * output + compressorMix * compressed;
            }
        }
    }
    // Somar metrônomo
    output += getMetronomeSample();
    // Looper: gravação
    if (looperRecording && looperWriteIndex < LOOPER_MAX_SAMPLES) {
        looperTracks[currentTrack].buffer[looperWriteIndex++] = output;
        looperTracks[currentTrack].active = true;
        
        // Log a cada 10000 samples para debug
        if (looperWriteIndex % 10000 == 0) {
            printf("looperRecording: gravados %d samples na track %d\n", looperWriteIndex, currentTrack);
        }
    }
    
    // Looper: reprodução de múltiplas faixas
    if (looperPlaying) {
        float looperOutput = 0.0f;
        bool hasSoloedTrack = false;
        
        // Verificar se há alguma faixa em solo
        for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
            if (looperTracks[i].soloed && looperTracks[i].active) {
                hasSoloedTrack = true;
                break;
            }
        }
        
        // Aplicar stutter se ativado
        bool shouldPlayStutter = true;
        if (looperStutter) {
            looperStutterPhase += looperStutterRate / looperSampleRate;
            if (looperStutterPhase >= 1.0f) {
                looperStutterPhase -= 1.0f;
            }
            shouldPlayStutter = (looperStutterPhase < 0.5f); // 50% duty cycle
        }
        
        if (shouldPlayStutter) {
            // Calcular posição de leitura com speed, reverse e slicing
            int readPos = looperReadIndex;
            
            // Aplicar slicing se ativado
            if (looperSlicingEnabled && numSlicePoints > 0) {
                // Calcular qual slice estamos reproduzindo
                int totalSliceLength = sliceLength * numSlicePoints;
                int sliceIndex = (looperReadIndex / sliceLength) % numSlicePoints;
                int sliceOffset = looperReadIndex % sliceLength;
                
                // Usar a ordem definida para os slices
                int actualSliceIndex = sliceOrder[sliceIndex];
                readPos = slicePoints[actualSliceIndex] + sliceOffset;
            } else {
                // Comportamento normal sem slicing
                if (looperReverse) {
                    // Reprodução reversa
                    int maxLength = 0;
                    for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
                        if (looperTracks[i].active && looperTracks[i].length > maxLength) {
                            maxLength = looperTracks[i].length;
                        }
                    }
                    if (maxLength > 0) {
                        readPos = maxLength - 1 - looperReadIndex;
                    }
                }
            }
            
            // Aplicar speed (interpolação linear simples)
            float speedPos = readPos * looperSpeed;
            int pos1 = (int)speedPos;
            int pos2 = pos1 + 1;
            float frac = speedPos - pos1;
            
            // Reproduzir faixas
            for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
                if (looperTracks[i].active && looperTracks[i].length > 0) {
                    // Verificar se deve reproduzir esta faixa
                    bool shouldPlay = true;
                    if (hasSoloedTrack) {
                        shouldPlay = looperTracks[i].soloed;
                    } else {
                        shouldPlay = !looperTracks[i].muted;
                    }
                    
                    if (shouldPlay) {
                        float trackSample = 0.0f;
                        
                        // Interpolação linear para speed
                        if (pos1 < looperTracks[i].length && pos2 < looperTracks[i].length) {
                            float sample1 = looperTracks[i].buffer[pos1];
                            float sample2 = looperTracks[i].buffer[pos2];
                            trackSample = sample1 + frac * (sample2 - sample1);
                        } else if (pos1 < looperTracks[i].length) {
                            trackSample = looperTracks[i].buffer[pos1];
                        }
                        
                        // Aplicar pitch shift (simplificado - apenas mudança de velocidade)
                        if (looperPitchShift != 0.0f) {
                            // Para uma implementação mais avançada, seria necessário um pitch shifter
                            // Por enquanto, apenas aplicamos uma pequena modulação
                            float pitchMod = 1.0f + (looperPitchShift / 12.0f) * 0.1f;
                            trackSample *= pitchMod;
                        }
                        
                        trackSample *= looperTracks[i].volume;
                        looperOutput += trackSample;
                    }
                }
            }
        }
        
        // === APLICAR EFEITOS AVANÇADOS DO LOOPER (FASE 5) ===
        float processedLooperOutput = looperOutput;
        
        // 1. Compressão automática
        if (looperAutoCompressionEnabled && processedLooperOutput != 0.0f) {
            float inputLevel = fabs(processedLooperOutput);
            float thresholdLinear = powf(10.0f, looperCompressionThreshold / 20.0f);
            
            // Detector de envelope
            float attackCoeff = expf(-1.0f / (looperCompressionAttack * 0.001f * looperSampleRate));
            float releaseCoeff = expf(-1.0f / (looperCompressionRelease * 0.001f * looperSampleRate));
            
            if (inputLevel > looperCompressionEnvelope) {
                looperCompressionEnvelope = attackCoeff * looperCompressionEnvelope + (1.0f - attackCoeff) * inputLevel;
            } else {
                looperCompressionEnvelope = releaseCoeff * looperCompressionEnvelope + (1.0f - releaseCoeff) * inputLevel;
            }
            
            // Calcular redução de ganho
            float gainReduction = 1.0f;
            if (looperCompressionEnvelope > thresholdLinear) {
                float overThreshold = looperCompressionEnvelope / thresholdLinear;
                float dbOver = 20.0f * log10f(overThreshold);
                float dbReduction = dbOver * (1.0f - 1.0f / looperCompressionRatio);
                gainReduction = powf(10.0f, -dbReduction / 20.0f);
            }
            
            processedLooperOutput *= gainReduction;
        }
        
        // 2. Normalização automática
        if (looperAutoNormalizationEnabled) {
            // Detectar pico durante a reprodução
            float currentPeak = fabs(processedLooperOutput);
            if (currentPeak > looperPeakLevel) {
                looperPeakLevel = currentPeak;
            }
            
            // Aplicar normalização se necessário
            if (looperPeakLevel > 0.0f) {
                float targetLinear = powf(10.0f, looperNormalizationTarget / 20.0f);
                looperNormalizationGain = targetLinear / looperPeakLevel;
                looperNormalizationGain = std::min(looperNormalizationGain, 1.0f); // Não amplificar
            }
            
            processedLooperOutput *= looperNormalizationGain;
        }
        
        // 3. Filtros
        if (looperLowPassEnabled) {
            // Filtro passa-baixa (Butterworth de 2ª ordem)
            float freq = looperLowPassFrequency;
            float w0 = 2.0f * 3.14159265f * freq / looperSampleRate;
            float alpha = sinf(w0) / (2.0f * 0.707f); // Q = 0.707
            
            float b0 = (1.0f - cosf(w0)) / 2.0f;
            float b1 = 1.0f - cosf(w0);
            float b2 = (1.0f - cosf(w0)) / 2.0f;
            float a0 = 1.0f + alpha;
            float a1 = -2.0f * cosf(w0);
            float a2 = 1.0f - alpha;
            
            // Normalizar
            b0 /= a0; b1 /= a0; b2 /= a0;
            a1 /= a0; a2 /= a0; a0 = 1.0f;
            
            float y = b0 * processedLooperOutput + b1 * looperLowPassX1 + b2 * looperLowPassX2 
                     - a1 * looperLowPassY1 - a2 * looperLowPassY2;
            looperLowPassX2 = looperLowPassX1; looperLowPassX1 = processedLooperOutput;
            looperLowPassY2 = looperLowPassY1; looperLowPassY1 = y;
            processedLooperOutput = y;
        }
        
        if (looperHighPassEnabled) {
            // Filtro passa-alta (Butterworth de 2ª ordem)
            float freq = looperHighPassFrequency;
            float w0 = 2.0f * 3.14159265f * freq / looperSampleRate;
            float alpha = sinf(w0) / (2.0f * 0.707f); // Q = 0.707
            
            float b0 = (1.0f + cosf(w0)) / 2.0f;
            float b1 = -(1.0f + cosf(w0));
            float b2 = (1.0f + cosf(w0)) / 2.0f;
            float a0 = 1.0f + alpha;
            float a1 = -2.0f * cosf(w0);
            float a2 = 1.0f - alpha;
            
            // Normalizar
            b0 /= a0; b1 /= a0; b2 /= a0;
            a1 /= a0; a2 /= a0; a0 = 1.0f;
            
            float y = b0 * processedLooperOutput + b1 * looperHighPassX1 + b2 * looperHighPassX2 
                     - a1 * looperHighPassY1 - a2 * looperHighPassY2;
            looperHighPassX2 = looperHighPassX1; looperHighPassX1 = processedLooperOutput;
            looperHighPassY2 = looperHighPassY1; looperHighPassY1 = y;
            processedLooperOutput = y;
        }
        
        // 4. Reverb de cauda
        if (looperReverbTailEnabled) {
            float reverbTail = looperReverbTailBuffer[looperReverbTailIndex];
            processedLooperOutput = (1.0f - looperReverbTailMix) * processedLooperOutput + 
                                   looperReverbTailMix * reverbTail;
            
            // Atualizar buffer de reverb
            looperReverbTailBuffer[looperReverbTailIndex] = processedLooperOutput * looperReverbTailDecayCoeff;
            looperReverbTailIndex = (looperReverbTailIndex + 1) % looperReverbTailSize;
        }
        
        // === APLICAR EFEITOS DA FASE 6 ===
        
        // 5. Fade In/Out automático
        if (looperAutoFadeInEnabled && looperFadeInCounter < looperFadeInSamples) {
            float fadeFactor = (float)looperFadeInCounter / looperFadeInSamples;
            processedLooperOutput *= fadeFactor;
            looperFadeInCounter++;
        }
        
        if (looperAutoFadeOutEnabled && looperFadeOutCounter > 0) {
            float fadeFactor = (float)looperFadeOutCounter / looperFadeOutSamples;
            processedLooperOutput *= fadeFactor;
            looperFadeOutCounter--;
        }
        
        output += processedLooperOutput;
        looperReadIndex++;
        
        // Resetar posição de leitura quando chegar ao fim do loop
        int maxLength = 0;
        for (int i = 0; i < LOOPER_MAX_TRACKS; i++) {
            if (looperTracks[i].active && looperTracks[i].length > maxLength) {
                maxLength = looperTracks[i].length;
            }
        }
        
        if (maxLength > 0) {
            if (looperSlicingEnabled && numSlicePoints > 0) {
                // Com slicing: resetar quando completar todos os slices
                int totalSliceLength = sliceLength * numSlicePoints;
                if (looperReadIndex >= totalSliceLength) {
                    looperReadIndex = 0;
                }
            } else {
                // Sem slicing: resetar quando chegar ao fim do loop
                if (looperReadIndex >= maxLength) {
                    looperReadIndex = 0;
                }
            }
        }
    }
    // Limitar para evitar clipping
    if (output > 1.0f) output = 1.0f;
    if (output < -1.0f) output = -1.0f;
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
    if (!oversamplingEnabled || oversamplingFactor <= 1) {
        // Processamento normal sem oversampling
        for (int i = 0; i < numSamples; ++i) {
            output[i] = processSample(input[i]);
        }
    } else {
        // Processamento com oversampling
        int oversampledSize = numSamples * oversamplingFactor;
        
        // Garantir que os buffers tenham tamanho suficiente
        if (oversampleBuffer.size() < oversampledSize) {
            oversampleBuffer.resize(oversampledSize);
            downsampleBuffer.resize(oversampledSize);
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
    oversamplingEnabled = enabled;
}

void setOversamplingFactor(int factor) {
    if (factor == 1 || factor == 2 || factor == 4 || factor == 8) {
        oversamplingFactor = factor;
        // Reinicializar buffers se necessário
        if (oversampleBufferSize > 0) {
            oversampleBufferSize = 4096 * oversamplingFactor;
            oversampleBuffer.resize(oversampleBufferSize);
            downsampleBuffer.resize(oversampleBufferSize);
        }
    }
}

bool isOversamplingEnabled() {
    return oversamplingEnabled;
}

int getOversamplingFactor() {
    return oversamplingFactor;
}

// Implementações das novas funções do looper avançado

int getLooperLength() {
    int length = looperTracks[currentTrack].length;
    printf("getLooperLength: currentTrack=%d, length=%d\n", currentTrack, length);
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
        memset(looperTracks[trackIndex].buffer, 0, sizeof(looperTracks[trackIndex].buffer));
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
        memset(looperTracks[i].buffer, 0, sizeof(looperTracks[i].buffer));
        looperTracks[i].length = 0;
        looperTracks[i].volume = 1.0f;
        looperTracks[i].muted = false;
        looperTracks[i].soloed = false;
        looperTracks[i].active = false;
    }
    
    // Carregar o áudio na primeira faixa
    memcpy(looperTracks[0].buffer, audioData, length * sizeof(float));
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
    float* buffer = looperTracks[targetTrack].buffer;
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
    float* buffer = looperTracks[targetTrack].buffer;
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
    float* buffer = looperTracks[targetTrack].buffer;
    for (int i = 0; i < fadeLength; ++i) {
        float fadeFactor = 1.0f - (static_cast<float>(i) / fadeLength);
        buffer[startSample + i] *= fadeFactor;
    }
}



