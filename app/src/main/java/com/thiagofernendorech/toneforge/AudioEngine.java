package com.thiagofernendorech.toneforge;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;

public class AudioEngine {
    static {
        System.loadLibrary("toneforge");
    }

    // Pipeline de áudio em tempo real
    private static AudioPipeline audioPipeline;
    private static boolean isAudioPipelineRunning = false;

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

    // Pipeline de áudio em tempo real
    public static void startAudioPipeline() {
        if (isAudioPipelineRunning) return;
        
        initAudioEngine();
        audioPipeline = new AudioPipeline();
        audioPipeline.start();
        isAudioPipelineRunning = true;
    }

    public static void stopAudioPipeline() {
        if (!isAudioPipelineRunning) return;
        
        if (audioPipeline != null) {
            audioPipeline.stop();
            audioPipeline = null;
        }
        cleanupAudioEngine();
        isAudioPipelineRunning = false;
    }

    public static boolean isAudioPipelineRunning() {
        return isAudioPipelineRunning;
    }

    // Classe interna para o pipeline de áudio
    private static class AudioPipeline {
        private static final int SAMPLE_RATE = 48000;
        private static final int BUFFER_SIZE = 2048;
        private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
        private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;

        private AudioRecord audioRecord;
        private AudioTrack audioTrack;
        private Thread audioThread;
        private boolean isRunning = false;
        private Handler uiHandler;

        private float[] inputBuffer;
        private float[] outputBuffer;

        public AudioPipeline() {
            uiHandler = new Handler(Looper.getMainLooper());
            inputBuffer = new float[BUFFER_SIZE];
            outputBuffer = new float[BUFFER_SIZE];
        }

        public void start() {
            if (isRunning) return;

            // Configurar AudioRecord (captura)
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, 
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT);
            
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT, 
                Math.max(minBufferSize, BUFFER_SIZE * 4));

            // Configurar AudioTrack (reprodução)
            int minTrackBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, 
                CHANNEL_CONFIG, AUDIO_FORMAT);
            
            audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                .setAudioFormat(new android.media.AudioFormat.Builder()
                    .setEncoding(AUDIO_FORMAT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(CHANNEL_CONFIG)
                    .build())
                .setBufferSizeInBytes(Math.max(minTrackBufferSize, BUFFER_SIZE * 4))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();

            audioRecord.startRecording();
            audioTrack.play();
            isRunning = true;

            // Thread de processamento de áudio
            audioThread = new Thread(() -> {
                while (isRunning && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    try {
                        // Capturar áudio
                        int read = audioRecord.read(inputBuffer, 0, BUFFER_SIZE, AudioRecord.READ_BLOCKING);
                        
                        if (read > 0) {
                            // Processar áudio com efeitos
                            processBuffer(inputBuffer, outputBuffer, read);
                            
                            // Reproduzir áudio processado
                            audioTrack.write(outputBuffer, 0, read, AudioTrack.WRITE_BLOCKING);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            });
            audioThread.start();
        }

        public void stop() {
            isRunning = false;
            
            if (audioThread != null) {
                audioThread.interrupt();
                audioThread = null;
            }
            
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
            
            if (audioTrack != null) {
                audioTrack.stop();
                audioTrack.release();
                audioTrack = null;
            }
        }
    }
} 