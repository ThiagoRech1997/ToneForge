package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.util.Log;

public class AudioEngine {
    public static final int SAMPLE_RATE = 48000; // Taxa de amostragem padrão
    
    static {
        System.loadLibrary("toneforge");
    }

    private static AudioEngine instance;
    private boolean isInitialized = false;
    private boolean isOversamplingEnabled = false;
    private int oversamplingFactor = 1;
    private LatencyManager latencyManager;

    private AudioEngine() {
        // Construtor privado para singleton
    }

    public static synchronized AudioEngine getInstance() {
        if (instance == null) {
            instance = new AudioEngine();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (isInitialized) {
            return;
        }
        
        // Inicializar LatencyManager
        latencyManager = LatencyManager.getInstance(context);
        
        // Aplicar configurações de latência
        applyLatencySettings();
        
        // Inicializar motor de áudio nativo
        initAudioEngine();
        isInitialized = true;
        Log.d("AudioEngine", "AudioEngine inicializado com configurações de latência");
    }
    
    private void applyLatencySettings() {
        if (latencyManager != null) {
            // Aplicar oversampling baseado no modo de latência
            if (latencyManager.isAutoOversamplingEnabled()) {
                setOversamplingEnabled(true);
                setOversamplingFactor(latencyManager.getOversamplingFactor());
            } else {
                setOversamplingEnabled(false);
            }
            
            Log.d("AudioEngine", "Configurações de latência aplicadas - " +
                  "Modo: " + latencyManager.getModeName(latencyManager.getCurrentMode()) + 
                  ", Oversampling: " + (isOversamplingEnabled ? "Sim (" + oversamplingFactor + "x)" : "Não"));
        }
    }
    
    public void updateLatencySettings() {
        if (latencyManager != null) {
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
            Log.d("AudioEngine", "Pipeline de áudio não estava ativo. Iniciando...");
            startAudioPipeline();
        } else {
            Log.d("AudioEngine", "Pipeline de áudio já está ativo.");
        }
    }

    // Métodos JNI existentes
    public static native void setGainEnabled(boolean enabled);
    public static native void setGainLevel(float level);
    public static native void setDistortionEnabled(boolean enabled);
    public static native void setDistortionLevel(float level);
    public static native void setDelayEnabled(boolean enabled);
    public static native void setDelayLevel(float level);
    public static native void setReverbEnabled(boolean enabled);
    public static native void setReverbLevel(float level);

    // Novos métodos JNI para processamento de áudio
    public static native void processBuffer(float[] input, float[] output, int numSamples);
    public static native void initAudioEngine();
    public static native void cleanupAudioEngine();

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
    public static native boolean isOversamplingEnabled();
    public static native int getOversamplingFactor();

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
} 