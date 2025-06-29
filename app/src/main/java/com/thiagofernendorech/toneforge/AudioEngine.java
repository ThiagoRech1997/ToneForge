package com.thiagofernendorech.toneforge;

public class AudioEngine {
    static {
        System.loadLibrary("toneforge");
    }

    public static native void setGainEnabled(boolean enabled);
    public static native void setGainLevel(float level);
    public static native void setDistortionEnabled(boolean enabled);
    public static native void setDistortionLevel(float level);
    public static native void setDelayEnabled(boolean enabled);
    public static native void setDelayLevel(float level);
    public static native void setReverbEnabled(boolean enabled);
    public static native void setReverbLevel(float level);

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
} 