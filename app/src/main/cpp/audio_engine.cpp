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

// --- Looper ---
static const int LOOPER_MAX_SAMPLES = 48000 * 30; // até 30 segundos a 48kHz
static float looperBuffer[LOOPER_MAX_SAMPLES];
static int looperLength = 0;
static int looperWriteIndex = 0;
static int looperReadIndex = 0;
static bool looperRecording = false;
static bool looperPlaying = false;

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
    metronomeActive = true;
}

void stopMetronome() {
    metronomeActive = false;
    metronomeSampleCounter = 0;
}

bool isMetronomeActive() {
    return metronomeActive;
}

// Gera um click de curta duração (ex: 100 samples) no início de cada batida
static float getMetronomeSample() {
    if (!metronomeActive) return 0.0f;
    if (metronomeSampleCounter == 0) {
        metronomeClick = true;
    }
    float click = 0.0f;
    if (metronomeClick && metronomeSampleCounter < 100) {
        // Pulso simples
        click = 0.8f * sinf(2.0f * 3.14159f * 1000.0f * metronomeSampleCounter / metronomeSampleRate);
    }
    if (metronomeSampleCounter >= 100) {
        metronomeClick = false;
    }
    metronomeSampleCounter++;
    if (metronomeSampleCounter >= metronomeSamplesPerBeat) {
        metronomeSampleCounter = 0;
    }
    return click;
}

void startLooperRecording() {
    looperRecording = true;
    looperPlaying = false;
    looperWriteIndex = 0;
    looperLength = 0;
    memset(looperBuffer, 0, sizeof(looperBuffer));
}

void stopLooperRecording() {
    looperRecording = false;
    if (looperWriteIndex > 0) {
        looperLength = looperWriteIndex;
    }
    looperReadIndex = 0;
}

void startLooperPlayback() {
    if (looperLength > 0) {
        looperPlaying = true;
        looperReadIndex = 0;
    }
}

void stopLooperPlayback() {
    looperPlaying = false;
}

void clearLooper() {
    looperRecording = false;
    looperPlaying = false;
    looperLength = 0;
    looperWriteIndex = 0;
    looperReadIndex = 0;
    memset(looperBuffer, 0, sizeof(looperBuffer));
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
        looperBuffer[looperWriteIndex++] = output;
    }
    // Looper: reprodução
    if (looperPlaying && looperLength > 0) {
        output += looperBuffer[looperReadIndex++];
        if (looperReadIndex >= looperLength) looperReadIndex = 0;
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

