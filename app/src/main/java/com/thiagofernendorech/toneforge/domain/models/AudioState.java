package com.thiagofernendorech.toneforge.domain.models;

/**
 * Modelo que representa o estado atual do sistema de áudio
 */
public class AudioState {
    
    private boolean pipelineRunning;
    private boolean pipelinePaused;
    private int currentLatencyMode;
    private boolean oversamplingEnabled;
    private int oversamplingFactor;
    private boolean backgroundAudioEnabled;
    private boolean midiEnabled;
    private boolean tunerActive;
    private boolean metronomeActive;
    private boolean looperRecording;
    private boolean looperPlaying;
    private boolean automationRecording;
    private boolean automationPlaying;
    
    // Construtores
    public AudioState() {
        // Valores padrão
        this.pipelineRunning = false;
        this.pipelinePaused = false;
        this.currentLatencyMode = 1; // Equilibrado
        this.oversamplingEnabled = false;
        this.oversamplingFactor = 1;
        this.backgroundAudioEnabled = false;
        this.midiEnabled = false;
        this.tunerActive = false;
        this.metronomeActive = false;
        this.looperRecording = false;
        this.looperPlaying = false;
        this.automationRecording = false;
        this.automationPlaying = false;
    }
    
    // Getters
    public boolean isPipelineRunning() {
        return pipelineRunning;
    }
    
    public boolean isPipelinePaused() {
        return pipelinePaused;
    }
    
    public int getCurrentLatencyMode() {
        return currentLatencyMode;
    }
    
    public boolean isOversamplingEnabled() {
        return oversamplingEnabled;
    }
    
    public int getOversamplingFactor() {
        return oversamplingFactor;
    }
    
    public boolean isBackgroundAudioEnabled() {
        return backgroundAudioEnabled;
    }
    
    public boolean isMidiEnabled() {
        return midiEnabled;
    }
    
    public boolean isTunerActive() {
        return tunerActive;
    }
    
    public boolean isMetronomeActive() {
        return metronomeActive;
    }
    
    public boolean isLooperRecording() {
        return looperRecording;
    }
    
    public boolean isLooperPlaying() {
        return looperPlaying;
    }
    
    public boolean isAutomationRecording() {
        return automationRecording;
    }
    
    public boolean isAutomationPlaying() {
        return automationPlaying;
    }
    
    // Setters
    public void setPipelineRunning(boolean pipelineRunning) {
        this.pipelineRunning = pipelineRunning;
    }
    
    public void setPipelinePaused(boolean pipelinePaused) {
        this.pipelinePaused = pipelinePaused;
    }
    
    public void setCurrentLatencyMode(int currentLatencyMode) {
        this.currentLatencyMode = currentLatencyMode;
    }
    
    public void setOversamplingEnabled(boolean oversamplingEnabled) {
        this.oversamplingEnabled = oversamplingEnabled;
    }
    
    public void setOversamplingFactor(int oversamplingFactor) {
        this.oversamplingFactor = oversamplingFactor;
    }
    
    public void setBackgroundAudioEnabled(boolean backgroundAudioEnabled) {
        this.backgroundAudioEnabled = backgroundAudioEnabled;
    }
    
    public void setMidiEnabled(boolean midiEnabled) {
        this.midiEnabled = midiEnabled;
    }
    
    public void setTunerActive(boolean tunerActive) {
        this.tunerActive = tunerActive;
    }
    
    public void setMetronomeActive(boolean metronomeActive) {
        this.metronomeActive = metronomeActive;
    }
    
    public void setLooperRecording(boolean looperRecording) {
        this.looperRecording = looperRecording;
    }
    
    public void setLooperPlaying(boolean looperPlaying) {
        this.looperPlaying = looperPlaying;
    }
    
    public void setAutomationRecording(boolean automationRecording) {
        this.automationRecording = automationRecording;
    }
    
    public void setAutomationPlaying(boolean automationPlaying) {
        this.automationPlaying = automationPlaying;
    }
    
    // Métodos utilitários
    
    /**
     * Verifica se o sistema está ocupado com alguma operação
     * @return true se alguma operação está ativa
     */
    public boolean isBusy() {
        return looperRecording || automationRecording || tunerActive;
    }
    
    /**
     * Verifica se alguma reprodução está ativa
     * @return true se alguma reprodução está ativa
     */
    public boolean isPlaying() {
        return looperPlaying || automationPlaying || metronomeActive;
    }
    
    /**
     * Obtém uma descrição do estado atual
     * @return descrição do estado
     */
    public String getStatusDescription() {
        if (!pipelineRunning) {
            return "Pipeline parado";
        }
        if (pipelinePaused) {
            return "Pipeline pausado";
        }
        if (looperRecording) {
            return "Gravando loop";
        }
        if (automationRecording) {
            return "Gravando automação";
        }
        if (looperPlaying) {
            return "Reproduzindo loop";
        }
        if (automationPlaying) {
            return "Reproduzindo automação";
        }
        if (metronomeActive) {
            return "Metrônomo ativo";
        }
        if (tunerActive) {
            return "Afinador ativo";
        }
        return "Pronto";
    }
    
    @Override
    public String toString() {
        return "AudioState{" +
                "pipelineRunning=" + pipelineRunning +
                ", pipelinePaused=" + pipelinePaused +
                ", currentLatencyMode=" + currentLatencyMode +
                ", oversamplingEnabled=" + oversamplingEnabled +
                ", oversamplingFactor=" + oversamplingFactor +
                ", backgroundAudioEnabled=" + backgroundAudioEnabled +
                ", midiEnabled=" + midiEnabled +
                ", tunerActive=" + tunerActive +
                ", metronomeActive=" + metronomeActive +
                ", looperRecording=" + looperRecording +
                ", looperPlaying=" + looperPlaying +
                ", automationRecording=" + automationRecording +
                ", automationPlaying=" + automationPlaying +
                '}';
    }
} 