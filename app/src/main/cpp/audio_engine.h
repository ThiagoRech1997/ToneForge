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

    // Ativação/desativação de efeitos
    void setGainEnabled(bool enabled);
    void setDistortionEnabled(bool enabled);
    void setDelayEnabled(bool enabled);
    void setReverbEnabled(bool enabled);

    float getDelayTime();
    float getDelayFeedback();
    float getReverbRoomSize();
    float getReverbDamping();

    void setEffectOrder(const char** order, int count);

    void setDistortionType(int type);
    void setDistortionMix(float mix);
    void setDelayMix(float mix);
    void setReverbMix(float mix);

    void setChorusEnabled(bool enabled);
    void setChorusDepth(float depth);
    void setChorusRate(float rate);
    void setChorusMix(float mix);
}

#endif //TONEFORGE_AUDIO_ENGINE_H
