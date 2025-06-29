#ifndef TONEFORGE_AUDIO_ENGINE_H
#define TONEFORGE_AUDIO_ENGINE_H

extern "C" {
    // Controles básicos
    void setGain(float gain);
    void setDistortion(float amount);
    void setDelay(float time, float feedback);
    void setReverb(float roomSize, float damping);
    
    // Processamento de áudio
    float processSample(float input);
    void processBuffer(float* input, float* output, int numSamples);
    
    // Inicialização e limpeza
    void initAudioEngine();
    void cleanupAudioEngine();

    // Metrônomo
    void startMetronome(int bpm);
    void stopMetronome();
    bool isMetronomeActive();
    void setSampleRate(int rate);

    // Looper
    void startLooperRecording();
    void stopLooperRecording();
    void startLooperPlayback();
    void stopLooperPlayback();
    void clearLooper();
    bool isLooperRecording();
    bool isLooperPlaying();

    // Afinador
    void startTuner();
    void stopTuner();
    bool isTunerActive();
    float getDetectedFrequency();
    void processTunerBuffer(const float* input, int numSamples);
}

#endif //TONEFORGE_AUDIO_ENGINE_H
