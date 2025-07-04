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
    
    // Novas funções para looper avançado
    int getLooperLength();
    int getLooperPosition();
    void setLooperPosition(int position);
    void setLooperTrackVolume(int trackIndex, float volume);
    void setLooperTrackMuted(int trackIndex, bool muted);
    void setLooperTrackSoloed(int trackIndex, bool soloed);
    void removeLooperTrack(int trackIndex);
    void setLooperBPM(int bpm);
    void setLooperSyncEnabled(bool enabled);

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

    // Funções para Oversampling
    void setOversamplingEnabled(bool enabled);
    void setOversamplingFactor(int factor);
    bool isOversamplingEnabled();
    int getOversamplingFactor();

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

    void setDelayTime(float timeMs);
    void setDelaySyncBPM(bool sync);
    void setDelayBPM(int bpm);

    void setReverbType(int type); // 0=Hall, 1=Plate, 2=Spring

    float* getLooperMix(int* outLength);
    
    // Função para carregar áudio no looper
    void loadLooperFromAudio(const float* audioData, int length);

    // Funcionalidades especiais do looper
    void setLooperReverse(bool enabled);
    void setLooperSpeed(float speed); // 0.5 = metade da velocidade, 2.0 = dobro
    void setLooperPitchShift(float semitones); // -12 a +12 semitons
    void setLooperStutter(bool enabled, float rate); // rate em Hz
    bool isLooperReverseEnabled();
    float getLooperSpeed();
    float getLooperPitchShift();
    bool isLooperStutterEnabled();
    float getLooperStutterRate();
    
    // Funcionalidade de Slicing
    void setLooperSlicingEnabled(bool enabled);
    void setLooperSlicePoints(const int* points, int numPoints);
    void setLooperSliceLength(int length);
    bool isLooperSlicingEnabled();
    int getLooperSliceLength();
    int getLooperNumSlices();
    void setLooperSliceOrder(const int* order, int numSlices);
    void randomizeLooperSlices();
    void reverseLooperSlices();
    
    // Funções de edição do looper
    void cutLooperRegion(float start, float end);
    void applyLooperFadeIn(float start, float end);
    void applyLooperFadeOut(float start, float end);
    
    // === FASE 5: EFEITOS AVANÇADOS PARA LOOPER ===
    
    // Compressão automática para looper
    void setLooperAutoCompression(bool enabled);
    void setLooperCompressionThreshold(float threshold); // dB
    void setLooperCompressionRatio(float ratio);
    void setLooperCompressionAttack(float attack); // ms
    void setLooperCompressionRelease(float release); // ms
    bool isLooperAutoCompressionEnabled();
    float getLooperCompressionThreshold();
    float getLooperCompressionRatio();
    float getLooperCompressionAttack();
    float getLooperCompressionRelease();
    
    // Normalização automática
    void setLooperAutoNormalization(bool enabled);
    void setLooperNormalizationTarget(float target); // dB
    bool isLooperAutoNormalizationEnabled();
    float getLooperNormalizationTarget();
    
    // Filtros para looper
    void setLooperLowPassFilter(bool enabled);
    void setLooperLowPassFrequency(float frequency); // Hz
    void setLooperHighPassFilter(bool enabled);
    void setLooperHighPassFrequency(float frequency); // Hz
    bool isLooperLowPassEnabled();
    bool isLooperHighPassEnabled();
    float getLooperLowPassFrequency();
    float getLooperHighPassFrequency();
    
    // Reverb de cauda entre loops
    void setLooperReverbTail(bool enabled);
    void setLooperReverbTailDecay(float decay); // segundos
    void setLooperReverbTailMix(float mix);
    bool isLooperReverbTailEnabled();
    float getLooperReverbTailDecay();
    float getLooperReverbTailMix();
    
    // === FASE 6: INTEGRAÇÃO AVANÇADA ===
    
    // Quantização
    void setLooperQuantization(bool enabled);
    void setLooperQuantizationGrid(float gridSize); // em batidas (1/4, 1/8, 1/16, etc.)
    bool isLooperQuantizationEnabled();
    float getLooperQuantizationGrid();
    
    // Fade In/Out automático
    void setLooperAutoFadeIn(bool enabled);
    void setLooperAutoFadeOut(bool enabled);
    void setLooperFadeInDuration(float duration); // segundos
    void setLooperFadeOutDuration(float duration); // segundos
    bool isLooperAutoFadeInEnabled();
    bool isLooperAutoFadeOutEnabled();
    float getLooperFadeInDuration();
    float getLooperFadeOutDuration();
    
    // Integração MIDI
    void setLooperMidiEnabled(bool enabled);
    void setLooperMidiChannel(int channel);
    void setLooperMidiCCMapping(int ccNumber, int function); // 0=record, 1=play, 2=stop, etc.
    bool isLooperMidiEnabled();
    int getLooperMidiChannel();
    void processLooperMidiMessage(int status, int data1, int data2);
    
    // Notificações
    void setLooperNotificationEnabled(bool enabled);
    void setLooperNotificationControls(bool showControls);
    bool isLooperNotificationEnabled();
    bool isLooperNotificationControlsEnabled();
    void updateLooperNotificationState();

    // Declarações para o metrônomo
    void setMetronomeVolume(float volume);
    void setMetronomeTimeSignature(int beats);
    bool isMetronomeActive();
}

#endif //TONEFORGE_AUDIO_ENGINE_H
