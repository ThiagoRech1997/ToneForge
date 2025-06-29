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

    void setFlangerEnabled(bool enabled);
    void setFlangerDepth(float depth);
    void setFlangerRate(float rate);
    void setFlangerFeedback(float feedback);
    void setFlangerMix(float mix);

    void setPhaserEnabled(bool enabled);
    void setPhaserDepth(float depth);
    void setPhaserRate(float rate);
    void setPhaserFeedback(float feedback);
    void setPhaserMix(float mix);

    void setEQEnabled(bool enabled);
    void setEQLow(float gain);
    void setEQMid(float gain);
    void setEQHigh(float gain);
    void setEQMix(float mix);

    void setCompressorEnabled(bool enabled);
    void setCompressorThreshold(float threshold);
    void setCompressorRatio(float ratio);
    void setCompressorAttack(float attack);
    void setCompressorRelease(float release);
    void setCompressorMix(float mix);
}

#endif //TONEFORGE_AUDIO_ENGINE_H
