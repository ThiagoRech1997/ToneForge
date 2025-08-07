package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.util.Log;

public class AudioEngine {
    public static final int SAMPLE_RATE = 48000; // Taxa de amostragem padrão
    private static boolean nativeLibraryLoaded = false;
    
    static {
        try {
            System.loadLibrary("toneforge");
            nativeLibraryLoaded = true;
            Log.d("AudioEngine", "Biblioteca nativa carregada com sucesso");
        } catch (UnsatisfiedLinkError e) {
            Log.e("AudioEngine", "Erro ao carregar biblioteca nativa: " + e.getMessage());
            nativeLibraryLoaded = false;
        } catch (Exception e) {
            Log.e("AudioEngine", "Erro inesperado ao carregar biblioteca: " + e.getMessage());
            nativeLibraryLoaded = false;
        }
    }

    private static AudioEngine instance;
    private boolean isInitialized = false;
    private boolean isOversamplingEnabled = false;
    private int oversamplingFactor = 1;
    private LatencyManager latencyManager;
    
    // Controle de throttling de logs
    private static long lastLogTime = 0;
    private static final long LOG_THROTTLE_INTERVAL = 3000; // 3 segundos

    private AudioEngine() {
        // Construtor privado para singleton
    }

    public static synchronized AudioEngine getInstance() {
        if (instance == null) {
            instance = new AudioEngine();
        }
        return instance;
    }

    /**
     * Verifica se a biblioteca nativa foi carregada corretamente
     */
    public static boolean isNativeLibraryLoaded() {
        return nativeLibraryLoaded;
    }

    public void initialize(Context context) {
        if (isInitialized) {
            return;
        }
        
        if (!nativeLibraryLoaded) {
            Log.w("AudioEngine", "Biblioteca nativa não carregada - modo fallback ativo");
            isInitialized = true; // Marcar como inicializado mesmo sem biblioteca nativa
            return;
        }
        
        try {
            // Inicializar LatencyManager
            latencyManager = LatencyManager.getInstance(context);
            
            // Aplicar configurações de latência
            applyLatencySettings();
            
            // Inicializar motor de áudio nativo
            initAudioEngine();
            isInitialized = true;
            logThrottled("AudioEngine", "AudioEngine inicializado com configurações de latência");
        } catch (Exception e) {
            Log.e("AudioEngine", "Erro ao inicializar AudioEngine: " + e.getMessage());
            isInitialized = true; // Marcar como inicializado para evitar tentativas repetidas
        }
    }
    
    private void applyLatencySettings() {
        if (latencyManager != null && nativeLibraryLoaded) {
            try {
                // Aplicar oversampling baseado no modo de latência
                if (latencyManager.isAutoOversamplingEnabled()) {
                    setOversamplingEnabled(true);
                    setOversamplingFactor(latencyManager.getOversamplingFactor());
                } else {
                    setOversamplingEnabled(false);
                }
                
                logThrottled("AudioEngine", "Configurações de latência aplicadas - " +
                      "Modo: " + latencyManager.getModeName(latencyManager.getCurrentMode()) + 
                      ", Oversampling: " + (isOversamplingEnabled ? "Sim (" + oversamplingFactor + "x)" : "Não"));
            } catch (Exception e) {
                Log.e("AudioEngine", "Erro ao aplicar configurações de latência: " + e.getMessage());
            }
        }
    }
    
    public void updateLatencySettings() {
        if (latencyManager != null && nativeLibraryLoaded) {
            applyLatencySettings();
        }
    }

    // Pipeline de áudio em tempo real
    public static void startAudioPipeline() {
        PipelineManager.getInstance().startPipeline();
    }

    public static void stopAudioPipeline() {
        PipelineManager.getInstance().stopPipeline();
    }

    public static boolean isAudioPipelineRunning() {
        return PipelineManager.getInstance().isRunning();
    }

    // Garante que o pipeline de áudio está ativo
    public static void startPipelineIfNeeded() {
        if (!isAudioPipelineRunning()) {
            logThrottled("AudioEngine", "Pipeline de áudio não estava ativo. Iniciando...");
            startAudioPipeline();
        } else {
            logThrottled("AudioEngine", "Pipeline de áudio já está ativo.");
        }
    }
    
    private static void logThrottled(String tag, String message) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogTime > LOG_THROTTLE_INTERVAL) {
            Log.d(tag, message);
            lastLogTime = currentTime;
        }
    }

    // Métodos JNI existentes - com verificação de biblioteca carregada
    public static void setGainEnabled(boolean enabled) {
        if (nativeLibraryLoaded) {
            try {
                setGainEnabledNative(enabled);
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar setGainEnabled: " + e.getMessage());
            }
        }
    }
    
    public static void setGainLevel(float level) {
        if (nativeLibraryLoaded) {
            try {
                setGainLevelNative(level);
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar setGainLevel: " + e.getMessage());
            }
        }
    }
    
    public static void setDistortionEnabled(boolean enabled) {
        if (nativeLibraryLoaded) {
            try {
                setDistortionEnabledNative(enabled);
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar setDistortionEnabled: " + e.getMessage());
            }
        }
    }
    
    public static void setDistortionLevel(float level) {
        if (nativeLibraryLoaded) {
            try {
                setDistortionLevelNative(level);
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar setDistortionLevel: " + e.getMessage());
            }
        }
    }
    
    public static void setDelayEnabled(boolean enabled) {
        if (nativeLibraryLoaded) {
            try {
                setDelayEnabledNative(enabled);
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar setDelayEnabled: " + e.getMessage());
            }
        }
    }
    
    public static void setDelayLevel(float level) {
        if (nativeLibraryLoaded) {
            try {
                setDelayLevelNative(level);
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar setDelayLevel: " + e.getMessage());
            }
        }
    }
    
    public static void setReverbEnabled(boolean enabled) {
        if (nativeLibraryLoaded) {
            try {
                setReverbEnabledNative(enabled);
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar setReverbEnabled: " + e.getMessage());
            }
        }
    }
    
    public static void setReverbLevel(float level) {
        if (nativeLibraryLoaded) {
            try {
                setReverbLevelNative(level);
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar setReverbLevel: " + e.getMessage());
            }
        }
    }

    // Novos métodos JNI para processamento de áudio
    public static void processBuffer(float[] input, float[] output, int numSamples) {
        if (nativeLibraryLoaded) {
            try {
                processBufferNative(input, output, numSamples);
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar processBuffer: " + e.getMessage());
                // Fallback: copiar entrada para saída
                if (input != null && output != null && numSamples > 0) {
                    System.arraycopy(input, 0, output, 0, Math.min(numSamples, Math.min(input.length, output.length)));
                }
            }
        } else {
            // Fallback: copiar entrada para saída sem processamento
            if (input != null && output != null && numSamples > 0) {
                System.arraycopy(input, 0, output, 0, Math.min(numSamples, Math.min(input.length, output.length)));
            }
        }
    }
    
    public static void initAudioEngine() {
        if (nativeLibraryLoaded) {
            try {
                initAudioEngineNative();
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar initAudioEngine: " + e.getMessage());
            }
        }
    }
    
    public static void cleanupAudioEngine() {
        if (nativeLibraryLoaded) {
            try {
                cleanupAudioEngineNative();
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar cleanupAudioEngine: " + e.getMessage());
            }
        }
    }

    // Métodos getter para parâmetros de efeitos
    public static float getGain() {
        if (nativeLibraryLoaded) {
            try {
                return getGainNative();
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar getGain: " + e.getMessage());
                return 1.0f;
            }
        }
        return 1.0f;
    }
    
    public static float getDistortion() {
        if (nativeLibraryLoaded) {
            try {
                return getDistortionNative();
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar getDistortion: " + e.getMessage());
                return 0.0f;
            }
        }
        return 0.0f;
    }
    
    public static float getDelayTime() {
        if (nativeLibraryLoaded) {
            try {
                return getDelayTimeNative();
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar getDelayTime: " + e.getMessage());
                return 0.0f;
            }
        }
        return 0.0f;
    }
    
    public static float getDelayFeedback() {
        if (nativeLibraryLoaded) {
            try {
                return getDelayFeedbackNative();
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar getDelayFeedback: " + e.getMessage());
                return 0.0f;
            }
        }
        return 0.0f;
    }
    
    public static float getReverbRoomSize() {
        if (nativeLibraryLoaded) {
            try {
                return getReverbRoomSizeNative();
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar getReverbRoomSize: " + e.getMessage());
                return 0.0f;
            }
        }
        return 0.0f;
    }
    
    public static float getReverbDamping() {
        if (nativeLibraryLoaded) {
            try {
                return getReverbDampingNative();
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar getReverbDamping: " + e.getMessage());
                return 0.0f;
            }
        }
        return 0.0f;
    }
    
    public static boolean isOversamplingEnabled() {
        if (nativeLibraryLoaded) {
            try {
                return isOversamplingEnabledNative();
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar isOversamplingEnabled: " + e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    public static int getOversamplingFactor() {
        if (nativeLibraryLoaded) {
            try {
                return getOversamplingFactorNative();
            } catch (UnsatisfiedLinkError e) {
                Log.e("AudioEngine", "Erro ao chamar getOversamplingFactor: " + e.getMessage());
                return 1;
            }
        }
        return 1;
    }

    // Declarações dos métodos nativos (com sufixo Native para diferenciação)
    private static native void setGainEnabledNative(boolean enabled);
    private static native void setGainLevelNative(float level);
    private static native void setDistortionEnabledNative(boolean enabled);
    private static native void setDistortionLevelNative(float level);
    private static native void setDelayEnabledNative(boolean enabled);
    private static native void setDelayLevelNative(float level);
    private static native void setReverbEnabledNative(boolean enabled);
    private static native void setReverbLevelNative(float level);
    private static native void processBufferNative(float[] input, float[] output, int numSamples);
    private static native void initAudioEngineNative();
    private static native void cleanupAudioEngineNative();
    
    // Métodos getter nativos
    private static native float getGainNative();
    private static native float getDistortionNative();
    private static native float getDelayTimeNative();
    private static native float getDelayFeedbackNative();
    private static native float getReverbRoomSizeNative();
    private static native float getReverbDampingNative();
    private static native boolean isOversamplingEnabledNative();
    private static native int getOversamplingFactorNative();

    // Looper
    public static native void startLooperRecording();
    public static native void stopLooperRecording();
    public static native void startLooperPlayback();
    public static native void stopLooperPlayback();
    public static native void clearLooper();
    
    // Novos métodos para looper avançado
    public static native int getLooperLength();
    public static native int getLooperPosition();
    public static native void setLooperPosition(int position);
    public static native void setLooperTrackVolume(int trackIndex, float volume);
    public static native void setLooperTrackMuted(int trackIndex, boolean muted);
    public static native void setLooperTrackSoloed(int trackIndex, boolean soloed);
    public static native void removeLooperTrack(int trackIndex);
    public static native void setLooperBPM(int bpm);
    public static native void setLooperSyncEnabled(boolean enabled);
    public static native boolean isLooperRecording();
    public static native boolean isLooperPlaying();

    // Gravador
    public static native void startRecording();
    public static native void stopRecording();
    public static native void playLastRecording();
    public static native void stopPlayback();

    // Metrônomo
    public static native void startMetronome(int bpm);
    public static native void stopMetronome();
    public static native void setMetronomeVolume(float volume);
    public static native void setMetronomeTimeSignature(int beats);
    public static native boolean isMetronomeActive();

    // Afinador
    public static native void startTuner();
    public static native void stopTuner();
    public static native boolean isTunerActive();
    public static native float getDetectedFrequency();
    public static native void processTunerBuffer(float[] input, int numSamples);

    // Novos métodos JNI para delay feedback, reverb room size e reverb damping
    public static native void setDelayFeedback(float feedback);
    public static native void setReverbRoomSize(float roomSize);
    public static native void setReverbDamping(float damping);

    // Novos métodos JNI para setEffectOrder
    public static native void setEffectOrder(String[] order);

    // Novos métodos JNI para setDistortionType, setDistortionMix, setDelayMix e setReverbMix
    public static native void setDistortionType(int type);
    public static native void setDistortionMix(float mix);
    public static native void setDelayMix(float mix);
    public static native void setReverbMix(float mix);

    // Novos métodos JNI para Chorus
    public static native void setChorusEnabled(boolean enabled);
    public static native void setChorusDepth(float depth);
    public static native void setChorusRate(float rate);
    public static native void setChorusMix(float mix);

    // Novos métodos JNI para Flanger
    public static native void setFlangerEnabled(boolean enabled);
    public static native void setFlangerDepth(float depth);
    public static native void setFlangerRate(float rate);
    public static native void setFlangerFeedback(float feedback);
    public static native void setFlangerMix(float mix);

    // Novos métodos JNI para Phaser
    public static native void setPhaserEnabled(boolean enabled);
    public static native void setPhaserDepth(float depth);
    public static native void setPhaserRate(float rate);
    public static native void setPhaserFeedback(float feedback);
    public static native void setPhaserMix(float mix);

    // Novos métodos JNI para Equalizer (EQ)
    public static native void setEQEnabled(boolean enabled);
    public static native void setEQLow(float gain);
    public static native void setEQMid(float gain);
    public static native void setEQHigh(float gain);
    public static native void setEQMix(float mix);

    // Novos métodos JNI para Compressor
    public static native void setCompressorEnabled(boolean enabled);
    public static native void setCompressorThreshold(float threshold);
    public static native void setCompressorRatio(float ratio);
    public static native void setCompressorAttack(float attack);
    public static native void setCompressorRelease(float release);
    public static native void setCompressorMix(float mix);

    // Novos métodos JNI para Delay
    public static native void setDelayTime(float timeMs);
    public static native void setDelaySyncBPM(boolean sync);
    public static native void setDelayBPM(int bpm);

    // Novos métodos JNI para setReverbType
    public static native void setReverbType(int type); // 0=Hall, 1=Plate, 2=Spring

    // Novos métodos JNI para Oversampling
    public static native void setOversamplingEnabled(boolean enabled);
    public static native void setOversamplingFactor(int factor);

    // Novos métodos JNI para obter o mix do looper como float[]
    public static native float[] getLooperMix();
    
    // Método JNI para carregar áudio no looper
    public static native void loadLooperFromAudio(float[] audioData);

    // Funcionalidades especiais do looper
    public static native void setLooperReverse(boolean enabled);
    public static native void setLooperSpeed(float speed);
    public static native void setLooperPitchShift(float semitones);
    public static native void setLooperStutter(boolean enabled, float rate);
    public static native boolean isLooperReverseEnabled();
    public static native float getLooperSpeed();
    public static native float getLooperPitchShift();
    public static native boolean isLooperStutterEnabled();
    public static native float getLooperStutterRate();
    
    // Funcionalidade de Slicing
    public static native void setLooperSlicingEnabled(boolean enabled);
    public static native void setLooperSlicePoints(int[] points);
    public static native void setLooperSliceLength(int length);
    public static native boolean isLooperSlicingEnabled();
    public static native int getLooperSliceLength();
    public static native int getLooperNumSlices();
    public static native void setLooperSliceOrder(int[] order);
    public static native void randomizeLooperSlices();
    public static native void reverseLooperSlices();
    
    // Funções de edição do looper
    public static native void cutLooperRegion(float start, float end);
    public static native void applyLooperFadeIn(float start, float end);
    public static native void applyLooperFadeOut(float start, float end);
    
    // === FASE 5: EFEITOS AVANÇADOS PARA LOOPER ===
    
    // Compressão automática para looper
    public static native void setLooperAutoCompression(boolean enabled);
    public static native void setLooperCompressionThreshold(float threshold);
    public static native void setLooperCompressionRatio(float ratio);
    public static native void setLooperCompressionAttack(float attack);
    public static native void setLooperCompressionRelease(float release);
    public static native boolean isLooperAutoCompressionEnabled();
    public static native float getLooperCompressionThreshold();
    public static native float getLooperCompressionRatio();
    public static native float getLooperCompressionAttack();
    public static native float getLooperCompressionRelease();
    
    // Normalização automática
    public static native void setLooperAutoNormalization(boolean enabled);
    public static native void setLooperNormalizationTarget(float target);
    public static native boolean isLooperAutoNormalizationEnabled();
    public static native float getLooperNormalizationTarget();
    
    // Filtros para looper
    public static native void setLooperLowPassFilter(boolean enabled);
    public static native void setLooperLowPassFrequency(float frequency);
    public static native void setLooperHighPassFilter(boolean enabled);
    public static native void setLooperHighPassFrequency(float frequency);
    public static native boolean isLooperLowPassEnabled();
    public static native boolean isLooperHighPassEnabled();
    public static native float getLooperLowPassFrequency();
    public static native float getLooperHighPassFrequency();
    
    // Reverb de cauda entre loops
    public static native void setLooperReverbTail(boolean enabled);
    public static native void setLooperReverbTailDecay(float decay);
    public static native void setLooperReverbTailMix(float mix);
    public static native boolean isLooperReverbTailEnabled();
    public static native float getLooperReverbTailDecay();
    public static native float getLooperReverbTailMix();
    
    // === FASE 6: INTEGRAÇÃO AVANÇADA ===
    
    // Quantização
    public static native void setLooperQuantization(boolean enabled);
    public static native void setLooperQuantizationGrid(float gridSize);
    public static native boolean isLooperQuantizationEnabled();
    public static native float getLooperQuantizationGrid();
    
    // Fade In/Out automático
    public static native void setLooperAutoFadeIn(boolean enabled);
    public static native void setLooperAutoFadeOut(boolean enabled);
    public static native void setLooperFadeInDuration(float duration);
    public static native void setLooperFadeOutDuration(float duration);
    public static native boolean isLooperAutoFadeInEnabled();
    public static native boolean isLooperAutoFadeOutEnabled();
    public static native float getLooperFadeInDuration();
    public static native float getLooperFadeOutDuration();
    
    // Integração MIDI
    public static native void setLooperMidiEnabled(boolean enabled);
    public static native void setLooperMidiChannel(int channel);
    public static native void setLooperMidiCCMapping(int ccNumber, int function);
    public static native boolean isLooperMidiEnabled();
    public static native int getLooperMidiChannel();
    public static native void processLooperMidiMessage(int status, int data1, int data2);
    
    // Notificações
    public static native void setLooperNotificationEnabled(boolean enabled);
    public static native void setLooperNotificationControls(boolean showControls);
    public static native boolean isLooperNotificationEnabled();
    public static native boolean isLooperNotificationControlsEnabled();
    public static native void updateLooperNotificationState();
    
    // Métodos auxiliares para compatibilidade com AudioRepository
    public static void setSampleRate(int sampleRate) {
        // Implementação básica - pode ser expandida conforme necessário
        Log.d("AudioEngine", "setSampleRate chamado com: " + sampleRate);
    }
    
    public static void setGain(Float gain) {
        if (gain != null) {
            setGainLevel(gain);
        }
    }
    
    public static void setDistortion(Float distortion) {
        if (distortion != null) {
            setDistortionLevel(distortion);
        }
    }
    
    public static void setDelay(Float delayTime, Float delayFeedback) {
        if (delayTime != null) {
            setDelayTime(delayTime);
        }
        if (delayFeedback != null) {
            setDelayFeedback(delayFeedback);
        }
    }
    
    public static void setReverb(Float roomSize, Float damping) {
        if (roomSize != null) {
            setReverbRoomSize(roomSize);
        }
        if (damping != null) {
            setReverbDamping(damping);
        }
    }
} 