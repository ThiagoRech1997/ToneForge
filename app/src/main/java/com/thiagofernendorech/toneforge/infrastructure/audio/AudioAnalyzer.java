package com.thiagofernendorech.toneforge;

import android.media.audiofx.Visualizer;
import android.util.Log;
import java.util.Arrays;

public class AudioAnalyzer {
    private static final String TAG = "AudioAnalyzer";
    
    private Visualizer visualizer;
    private boolean isInitialized = false;
    private AudioAnalyzerCallback callback;
    
    // Análise de espectro
    private float[] spectrumData;
    private float[] waveformData;
    private int spectrumSize = 256;
    private int waveformSize = 256;
    
    // VU Meter
    private float currentRMS = 0.0f;
    private float peakLevel = 0.0f;
    private float averageLevel = 0.0f;
    private static final float RMS_ALPHA = 0.1f; // Fator de suavização
    
    // Detecção de clipping
    private boolean isClipping = false;
    private int clippingCount = 0;
    private static final float CLIPPING_THRESHOLD = 0.95f;
    private static final int CLIPPING_DURATION_MS = 100;
    
    // Análise de frequência
    private float dominantFrequency = 0.0f;
    private float[] frequencyBands = new float[8]; // 8 bandas de frequência
    
    public interface AudioAnalyzerCallback {
        void onSpectrumData(float[] spectrum);
        void onWaveformData(float[] waveform);
        void onVUMeterData(float rms, float peak, float average);
        void onClippingDetected(boolean clipping);
        void onFrequencyAnalysis(float dominantFreq, float[] bands);
    }
    
    public AudioAnalyzer() {
        spectrumData = new float[spectrumSize];
        waveformData = new float[waveformSize];
    }
    
    public void initialize(int audioSessionId) {
        if (isInitialized) {
            return;
        }
        
        try {
            visualizer = new Visualizer(audioSessionId);
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            
            // Configurar captura de espectro
            visualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    @Override
                    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                        processWaveformData(waveform);
                    }
                    
                    @Override
                    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                        processSpectrumData(fft);
                    }
                },
                Visualizer.getMaxCaptureRate() / 2, // 50% da taxa máxima
                true, // Capturar waveform
                true  // Capturar FFT
            );
            
            visualizer.setEnabled(true);
            isInitialized = true;
            Log.d(TAG, "AudioAnalyzer inicializado com sucesso");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar AudioAnalyzer: " + e.getMessage());
        }
    }
    
    private void processSpectrumData(byte[] fft) {
        if (fft == null || fft.length < spectrumSize * 2) {
            return;
        }
        
        // Converter bytes para valores float
        for (int i = 0; i < spectrumSize; i++) {
            float real = fft[2 * i];
            float imag = fft[2 * i + 1];
            float magnitude = (float) Math.sqrt(real * real + imag * imag);
            spectrumData[i] = magnitude / 128.0f; // Normalizar
        }
        
        // Calcular bandas de frequência
        calculateFrequencyBands();
        
        // Detectar frequência dominante
        detectDominantFrequency();
        
        // Calcular VU Meter
        calculateVUMeter();
        
        // Detectar clipping
        detectClipping();
        
        // Notificar callback
        if (callback != null) {
            callback.onSpectrumData(spectrumData.clone());
            callback.onFrequencyAnalysis(dominantFrequency, frequencyBands.clone());
        }
    }
    
    private void processWaveformData(byte[] waveform) {
        if (waveform == null || waveform.length < waveformSize) {
            return;
        }
        
        // Converter bytes para valores float
        for (int i = 0; i < waveformSize; i++) {
            waveformData[i] = waveform[i] / 128.0f; // Normalizar
        }
        
        // Notificar callback
        if (callback != null) {
            callback.onWaveformData(waveformData.clone());
        }
    }
    
    private void calculateFrequencyBands() {
        // Dividir espectro em 8 bandas de frequência
        int samplesPerBand = spectrumSize / 8;
        
        for (int band = 0; band < 8; band++) {
            float sum = 0.0f;
            int startIndex = band * samplesPerBand;
            int endIndex = startIndex + samplesPerBand;
            
            for (int i = startIndex; i < endIndex && i < spectrumSize; i++) {
                sum += spectrumData[i];
            }
            
            frequencyBands[band] = sum / samplesPerBand;
        }
    }
    
    private void detectDominantFrequency() {
        float maxMagnitude = 0.0f;
        int maxIndex = 0;
        
        for (int i = 0; i < spectrumSize / 2; i++) { // Apenas metade do espectro (frequências positivas)
            if (spectrumData[i] > maxMagnitude) {
                maxMagnitude = spectrumData[i];
                maxIndex = i;
            }
        }
        
        // Converter índice para frequência (assumindo 48kHz)
        dominantFrequency = (maxIndex * 48000.0f) / spectrumSize;
    }
    
    private void calculateVUMeter() {
        // Calcular RMS do waveform
        float sum = 0.0f;
        for (float sample : waveformData) {
            sum += sample * sample;
        }
        float rms = (float) Math.sqrt(sum / waveformSize);
        
        // Aplicar suavização
        currentRMS = RMS_ALPHA * rms + (1.0f - RMS_ALPHA) * currentRMS;
        
        // Atualizar peak
        if (rms > peakLevel) {
            peakLevel = rms;
        } else {
            peakLevel *= 0.995f; // Decaimento lento do peak
        }
        
        // Calcular média
        averageLevel = 0.5f * (currentRMS + averageLevel);
        
        // Notificar callback
        if (callback != null) {
            callback.onVUMeterData(currentRMS, peakLevel, averageLevel);
        }
    }
    
    private void detectClipping() {
        boolean wasClipping = isClipping;
        
        // Verificar se há clipping no waveform
        for (float sample : waveformData) {
            if (Math.abs(sample) > CLIPPING_THRESHOLD) {
                clippingCount++;
                break;
            }
        }
        
        // Determinar se está clipping baseado na duração
        isClipping = clippingCount > 0;
        
        // Resetar contador se não há clipping
        if (!isClipping) {
            clippingCount = 0;
        }
        
        // Notificar mudança de estado
        if (isClipping != wasClipping && callback != null) {
            callback.onClippingDetected(isClipping);
        }
    }
    
    public void setCallback(AudioAnalyzerCallback callback) {
        this.callback = callback;
    }
    
    public float[] getSpectrumData() {
        return spectrumData != null ? spectrumData.clone() : new float[0];
    }
    
    public float[] getWaveformData() {
        return waveformData != null ? waveformData.clone() : new float[0];
    }
    
    public float getCurrentRMS() {
        return currentRMS;
    }
    
    public float getPeakLevel() {
        return peakLevel;
    }
    
    public float getAverageLevel() {
        return averageLevel;
    }
    
    public boolean isClipping() {
        return isClipping;
    }
    
    public float getDominantFrequency() {
        return dominantFrequency;
    }
    
    public float[] getFrequencyBands() {
        return frequencyBands.clone();
    }
    
    public void reset() {
        currentRMS = 0.0f;
        peakLevel = 0.0f;
        averageLevel = 0.0f;
        isClipping = false;
        clippingCount = 0;
        dominantFrequency = 0.0f;
        Arrays.fill(frequencyBands, 0.0f);
    }
    
    public void release() {
        if (visualizer != null) {
            visualizer.setEnabled(false);
            visualizer.release();
            visualizer = null;
        }
        isInitialized = false;
        Log.d(TAG, "AudioAnalyzer liberado");
    }
} 