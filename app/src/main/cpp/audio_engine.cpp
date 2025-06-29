#include "audio_engine.h"
#include <cmath>
#include <cstring>

// Parâmetros dos efeitos
static float currentGain = 1.0f;
static float distortionAmount = 0.0f;
static float delayTime = 0.0f;
static float delayFeedback = 0.0f;
static float reverbRoomSize = 0.0f;
static float reverbDamping = 0.0f;

// Buffer de delay (máximo 1 segundo a 48kHz)
static const int MAX_DELAY_SAMPLES = 48000;
static float delayBuffer[MAX_DELAY_SAMPLES];
static int delayBufferIndex = 0;
static int delayBufferSize = 0;

// Buffer de reverb simples
static const int REVERB_BUFFER_SIZE = 4096;
static float reverbBuffer[REVERB_BUFFER_SIZE];
static int reverbIndex = 0;

// Taxa de amostragem (será configurada)
static int sampleRate = 48000;

void initAudioEngine() {
    // Limpar buffers
    memset(delayBuffer, 0, sizeof(delayBuffer));
    memset(reverbBuffer, 0, sizeof(reverbBuffer));
    delayBufferIndex = 0;
    reverbIndex = 0;
    
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
    memset(delayBuffer, 0, sizeof(delayBuffer));
    memset(reverbBuffer, 0, sizeof(reverbBuffer));
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
}

void setReverb(float roomSize, float damping) {
    reverbRoomSize = roomSize;
    reverbDamping = damping;
}

float processSample(float input) {
    float output = input;
    
    // Aplicar distorção
    if (distortionAmount > 0.0f) {
        float drive = 1.0f + distortionAmount * 10.0f;
        output = tanh(output * drive) / tanh(drive);
    }
    
    // Aplicar delay
    if (delayTime > 0.0f && delayBufferSize > 0) {
        float delayedSample = delayBuffer[delayBufferIndex];
        output += delayedSample * delayFeedback;
        
        delayBuffer[delayBufferIndex] = output;
        delayBufferIndex = (delayBufferIndex + 1) % delayBufferSize;
    }
    
    // Aplicar reverb simples
    if (reverbRoomSize > 0.0f) {
        float reverbSample = reverbBuffer[reverbIndex];
        output += reverbSample * reverbRoomSize * (1.0f - reverbDamping);
        
        reverbBuffer[reverbIndex] = output;
        reverbIndex = (reverbIndex + 1) % REVERB_BUFFER_SIZE;
    }
    
    // Aplicar ganho final
    output *= currentGain;
    
    // Limitar para evitar clipping
    if (output > 1.0f) output = 1.0f;
    if (output < -1.0f) output = -1.0f;
    
    return output;
}

void processBuffer(float* input, float* output, int numSamples) {
    for (int i = 0; i < numSamples; i++) {
        output[i] = processSample(input[i]);
    }
}

