package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.util.Log;

public class AudioEngine {
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

    // Gravador
    public static native void startRecording();
    public static native void stopRecording();
    public static native void playLastRecording();
    public static native void stopPlayback();

    // Metrônomo
    public static native void startMetronome(int bpm);
    public static native void stopMetronome();

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
} 