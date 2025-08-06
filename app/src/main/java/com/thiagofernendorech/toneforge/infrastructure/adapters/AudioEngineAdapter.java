package com.thiagofernendorech.toneforge.infrastructure.adapters;

import com.thiagofernendorech.toneforge.AudioEngine;
import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;
import com.thiagofernendorech.toneforge.domain.models.AudioState;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;

/**
 * Adapter para integrar AudioEngine existente com a interface do domínio
 * Implementa o padrão Adapter para manter compatibilidade
 */
public class AudioEngineAdapter implements AudioEngineInterface {
    
    private final AudioEngine audioEngine;
    
    public AudioEngineAdapter() {
        this.audioEngine = AudioEngine.getInstance();
    }
    
    @Override
    public boolean startPipeline() {
        try {
            AudioEngine.startAudioPipeline();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public void stopPipeline() {
        try {
            AudioEngine.stopAudioPipeline();
        } catch (Exception e) {
            // Log error silently
        }
    }
    
    @Override
    public void pausePipeline() {
        // Implementar quando AudioEngine suportar
        // AudioEngine.pauseAudioPipeline();
    }
    
    @Override
    public void resumePipeline() {
        // Implementar quando AudioEngine suportar
        // AudioEngine.resumeAudioPipeline();
    }
    
    @Override
    public boolean isPipelineRunning() {
        // Por enquanto, verificar se engine está inicializado
        return audioEngine != null && AudioEngine.isNativeLibraryLoaded();
    }
    
    @Override
    public boolean isNativeLibraryLoaded() {
        return AudioEngine.isNativeLibraryLoaded();
    }
    
    @Override
    public void applyEffectParameters(EffectParameters parameters) {
        if (parameters.getGain() != null) {
            setGain(parameters.getGain());
        }
        if (parameters.getDistortion() != null) {
            setDistortion(parameters.getDistortion());
        }
        if (parameters.getDelayTime() != null && parameters.getDelayFeedback() != null) {
            setDelay(parameters.getDelayTime(), parameters.getDelayFeedback());
        }
        if (parameters.getReverbRoomSize() != null && parameters.getReverbDamping() != null) {
            setReverb(parameters.getReverbRoomSize(), parameters.getReverbDamping());
        }
    }
    
    @Override
    public EffectParameters getCurrentEffectParameters() {
        EffectParameters parameters = new EffectParameters();
        
        // Por enquanto retorna valores padrão
        // Implementar getters no AudioEngine quando disponível
        parameters.setGain(0.5f);
        parameters.setDistortion(0.0f);
        parameters.setDelayTime(0.0f);
        parameters.setDelayFeedback(0.0f);
        parameters.setReverbRoomSize(0.0f);
        parameters.setReverbDamping(0.0f);
        
        return parameters;
    }
    
    @Override
    public void setGain(float gain) {
        // Implementar quando AudioEngine suportar métodos específicos
        // AudioEngine.setGain(gain);
    }
    
    @Override
    public void setDistortion(float distortion) {
        // Implementar quando AudioEngine suportar métodos específicos
        // AudioEngine.setDistortion(distortion);
    }
    
    @Override
    public void setDelay(float delayTime, float feedback) {
        // Implementar quando AudioEngine suportar métodos específicos
        // AudioEngine.setDelay(delayTime, feedback);
    }
    
    @Override
    public void setReverb(float roomSize, float damping) {
        // Implementar quando AudioEngine suportar métodos específicos
        // AudioEngine.setReverb(roomSize, damping);
    }
    
    @Override
    public AudioState getCurrentState() {
        AudioState state = new AudioState();
        state.setPipelineRunning(isPipelineRunning());
        state.setPipelinePaused(false); // Implementar quando disponível
        state.setCurrentLatencyMode(1); // Implementar quando disponível
        state.setOversamplingEnabled(false); // Implementar quando disponível
        return state;
    }
    
    @Override
    public void setLatencySettings(int bufferSize, int sampleRate) {
        // Implementar quando AudioEngine suportar
        // AudioEngine.setBufferSize(bufferSize);
        // AudioEngine.setSampleRate(sampleRate);
    }
    
    @Override
    public void startLooperRecording() {
        audioEngine.startLooperRecording();
    }
    
    @Override
    public void stopLooperRecording() {
        audioEngine.stopLooperRecording();
    }
    
    @Override
    public void startLooperPlayback() {
        audioEngine.startLooperPlayback();
    }
    
    @Override
    public void stopLooperPlayback() {
        audioEngine.stopLooperPlayback();
    }
    
    @Override
    public void clearLooper() {
        audioEngine.clearLooper();
    }
    
    @Override
    public void startTuner() {
        audioEngine.startTuner();
    }
    
    @Override
    public void stopTuner() {
        audioEngine.stopTuner();
    }
    
    @Override
    public float getDetectedFrequency() {
        return audioEngine.getDetectedFrequency();
    }
    
    @Override
    public void startMetronome(int bpm) {
        audioEngine.startMetronome(bpm);
    }
    
    @Override
    public void stopMetronome() {
        audioEngine.stopMetronome();
    }
    
    @Override
    public boolean isMetronomeActive() {
        return audioEngine.isMetronomeActive();
    }
    
    @Override
    public void cleanup() {
        // Implementar limpeza quando AudioEngine suportar
    }
} 