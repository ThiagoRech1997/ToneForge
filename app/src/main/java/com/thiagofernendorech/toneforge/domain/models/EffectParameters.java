package com.thiagofernendorech.toneforge.domain.models;

/**
 * Modelo que representa os parâmetros de todos os efeitos de áudio
 */
public class EffectParameters {
    
    // Gain
    private Float gain;
    private Boolean gainEnabled;
    
    // Distortion
    private Float distortion;
    private Boolean distortionEnabled;
    
    // Delay
    private Float delayTime;
    private Float delayFeedback;
    private Float delayMix;
    private Boolean delayEnabled;
    
    // Reverb
    private Float reverbRoomSize;
    private Float reverbDamping;
    private Float reverbMix;
    private Boolean reverbEnabled;
    
    // Chorus
    private Float chorusDepth;
    private Float chorusRate;
    private Float chorusMix;
    private Boolean chorusEnabled;
    
    // Flanger
    private Float flangerDepth;
    private Float flangerRate;
    private Float flangerMix;
    private Boolean flangerEnabled;
    
    // Phaser
    private Float phaserDepth;
    private Float phaserRate;
    private Float phaserMix;
    private Boolean phaserEnabled;
    
    // EQ
    private Float eqLow;
    private Float eqMid;
    private Float eqHigh;
    private Boolean eqEnabled;
    
    // Compressor
    private Float compressorThreshold;
    private Float compressorRatio;
    private Float compressorAttack;
    private Float compressorRelease;
    private Boolean compressorEnabled;
    
    // Construtor
    public EffectParameters() {
        // Inicializar com valores padrão neutros
        setDefaultValues();
    }
    
    /**
     * Define valores padrão para todos os parâmetros
     */
    private void setDefaultValues() {
        // Gain
        this.gain = 0.5f;
        this.gainEnabled = true;
        
        // Distortion
        this.distortion = 0.0f;
        this.distortionEnabled = false;
        
        // Delay
        this.delayTime = 0.0f;
        this.delayFeedback = 0.0f;
        this.delayMix = 0.0f;
        this.delayEnabled = false;
        
        // Reverb
        this.reverbRoomSize = 0.0f;
        this.reverbDamping = 0.0f;
        this.reverbMix = 0.0f;
        this.reverbEnabled = false;
        
        // Chorus
        this.chorusDepth = 0.0f;
        this.chorusRate = 0.0f;
        this.chorusMix = 0.0f;
        this.chorusEnabled = false;
        
        // Flanger
        this.flangerDepth = 0.0f;
        this.flangerRate = 0.0f;
        this.flangerMix = 0.0f;
        this.flangerEnabled = false;
        
        // Phaser
        this.phaserDepth = 0.0f;
        this.phaserRate = 0.0f;
        this.phaserMix = 0.0f;
        this.phaserEnabled = false;
        
        // EQ
        this.eqLow = 0.0f;
        this.eqMid = 0.0f;
        this.eqHigh = 0.0f;
        this.eqEnabled = false;
        
        // Compressor
        this.compressorThreshold = -20.0f;
        this.compressorRatio = 2.0f;
        this.compressorAttack = 10.0f;
        this.compressorRelease = 100.0f;
        this.compressorEnabled = false;
    }
    
    // Getters
    public Float getGain() { return gain; }
    public Boolean isGainEnabled() { return gainEnabled; }
    
    public Float getDistortion() { return distortion; }
    public Boolean isDistortionEnabled() { return distortionEnabled; }
    
    public Float getDelayTime() { return delayTime; }
    public Float getDelayFeedback() { return delayFeedback; }
    public Float getDelayMix() { return delayMix; }
    public Boolean isDelayEnabled() { return delayEnabled; }
    
    public Float getReverbRoomSize() { return reverbRoomSize; }
    public Float getReverbDamping() { return reverbDamping; }
    public Float getReverbMix() { return reverbMix; }
    public Boolean isReverbEnabled() { return reverbEnabled; }
    
    public Float getChorusDepth() { return chorusDepth; }
    public Float getChorusRate() { return chorusRate; }
    public Float getChorusMix() { return chorusMix; }
    public Boolean isChorusEnabled() { return chorusEnabled; }
    
    public Float getFlangerDepth() { return flangerDepth; }
    public Float getFlangerRate() { return flangerRate; }
    public Float getFlangerMix() { return flangerMix; }
    public Boolean isFlangerEnabled() { return flangerEnabled; }
    
    public Float getPhaserDepth() { return phaserDepth; }
    public Float getPhaserRate() { return phaserRate; }
    public Float getPhaserMix() { return phaserMix; }
    public Boolean isPhaserEnabled() { return phaserEnabled; }
    
    public Float getEqLow() { return eqLow; }
    public Float getEqMid() { return eqMid; }
    public Float getEqHigh() { return eqHigh; }
    public Boolean isEqEnabled() { return eqEnabled; }
    
    public Float getCompressorThreshold() { return compressorThreshold; }
    public Float getCompressorRatio() { return compressorRatio; }
    public Float getCompressorAttack() { return compressorAttack; }
    public Float getCompressorRelease() { return compressorRelease; }
    public Boolean isCompressorEnabled() { return compressorEnabled; }
    
    // Setters
    public void setGain(Float gain) { this.gain = gain; }
    public void setGainEnabled(Boolean gainEnabled) { this.gainEnabled = gainEnabled; }
    
    public void setDistortion(Float distortion) { this.distortion = distortion; }
    public void setDistortionEnabled(Boolean distortionEnabled) { this.distortionEnabled = distortionEnabled; }
    
    public void setDelayTime(Float delayTime) { this.delayTime = delayTime; }
    public void setDelayFeedback(Float delayFeedback) { this.delayFeedback = delayFeedback; }
    public void setDelayMix(Float delayMix) { this.delayMix = delayMix; }
    public void setDelayEnabled(Boolean delayEnabled) { this.delayEnabled = delayEnabled; }
    
    public void setReverbRoomSize(Float reverbRoomSize) { this.reverbRoomSize = reverbRoomSize; }
    public void setReverbDamping(Float reverbDamping) { this.reverbDamping = reverbDamping; }
    public void setReverbMix(Float reverbMix) { this.reverbMix = reverbMix; }
    public void setReverbEnabled(Boolean reverbEnabled) { this.reverbEnabled = reverbEnabled; }
    
    public void setChorusDepth(Float chorusDepth) { this.chorusDepth = chorusDepth; }
    public void setChorusRate(Float chorusRate) { this.chorusRate = chorusRate; }
    public void setChorusMix(Float chorusMix) { this.chorusMix = chorusMix; }
    public void setChorusEnabled(Boolean chorusEnabled) { this.chorusEnabled = chorusEnabled; }
    
    public void setFlangerDepth(Float flangerDepth) { this.flangerDepth = flangerDepth; }
    public void setFlangerRate(Float flangerRate) { this.flangerRate = flangerRate; }
    public void setFlangerMix(Float flangerMix) { this.flangerMix = flangerMix; }
    public void setFlangerEnabled(Boolean flangerEnabled) { this.flangerEnabled = flangerEnabled; }
    
    public void setPhaserDepth(Float phaserDepth) { this.phaserDepth = phaserDepth; }
    public void setPhaserRate(Float phaserRate) { this.phaserRate = phaserRate; }
    public void setPhaserMix(Float phaserMix) { this.phaserMix = phaserMix; }
    public void setPhaserEnabled(Boolean phaserEnabled) { this.phaserEnabled = phaserEnabled; }
    
    public void setEqLow(Float eqLow) { this.eqLow = eqLow; }
    public void setEqMid(Float eqMid) { this.eqMid = eqMid; }
    public void setEqHigh(Float eqHigh) { this.eqHigh = eqHigh; }
    public void setEqEnabled(Boolean eqEnabled) { this.eqEnabled = eqEnabled; }
    
    public void setCompressorThreshold(Float compressorThreshold) { this.compressorThreshold = compressorThreshold; }
    public void setCompressorRatio(Float compressorRatio) { this.compressorRatio = compressorRatio; }
    public void setCompressorAttack(Float compressorAttack) { this.compressorAttack = compressorAttack; }
    public void setCompressorRelease(Float compressorRelease) { this.compressorRelease = compressorRelease; }
    public void setCompressorEnabled(Boolean compressorEnabled) { this.compressorEnabled = compressorEnabled; }
    
    // Métodos utilitários
    
    /**
     * Reseta todos os parâmetros para valores padrão
     */
    public void reset() {
        setDefaultValues();
    }
    
    /**
     * Copia os parâmetros de outro objeto EffectParameters
     * @param other objeto a ser copiado
     */
    public void copyFrom(EffectParameters other) {
        if (other == null) return;
        
        this.gain = other.gain;
        this.gainEnabled = other.gainEnabled;
        this.distortion = other.distortion;
        this.distortionEnabled = other.distortionEnabled;
        this.delayTime = other.delayTime;
        this.delayFeedback = other.delayFeedback;
        this.delayMix = other.delayMix;
        this.delayEnabled = other.delayEnabled;
        this.reverbRoomSize = other.reverbRoomSize;
        this.reverbDamping = other.reverbDamping;
        this.reverbMix = other.reverbMix;
        this.reverbEnabled = other.reverbEnabled;
        this.chorusDepth = other.chorusDepth;
        this.chorusRate = other.chorusRate;
        this.chorusMix = other.chorusMix;
        this.chorusEnabled = other.chorusEnabled;
        this.flangerDepth = other.flangerDepth;
        this.flangerRate = other.flangerRate;
        this.flangerMix = other.flangerMix;
        this.flangerEnabled = other.flangerEnabled;
        this.phaserDepth = other.phaserDepth;
        this.phaserRate = other.phaserRate;
        this.phaserMix = other.phaserMix;
        this.phaserEnabled = other.phaserEnabled;
        this.eqLow = other.eqLow;
        this.eqMid = other.eqMid;
        this.eqHigh = other.eqHigh;
        this.eqEnabled = other.eqEnabled;
        this.compressorThreshold = other.compressorThreshold;
        this.compressorRatio = other.compressorRatio;
        this.compressorAttack = other.compressorAttack;
        this.compressorRelease = other.compressorRelease;
        this.compressorEnabled = other.compressorEnabled;
    }
    
    /**
     * Conta quantos efeitos estão ativos
     * @return número de efeitos ativos
     */
    public int getActiveEffectsCount() {
        int count = 0;
        if (gainEnabled != null && gainEnabled) count++;
        if (distortionEnabled != null && distortionEnabled) count++;
        if (delayEnabled != null && delayEnabled) count++;
        if (reverbEnabled != null && reverbEnabled) count++;
        if (chorusEnabled != null && chorusEnabled) count++;
        if (flangerEnabled != null && flangerEnabled) count++;
        if (phaserEnabled != null && phaserEnabled) count++;
        if (eqEnabled != null && eqEnabled) count++;
        if (compressorEnabled != null && compressorEnabled) count++;
        return count;
    }
    
    /**
     * Verifica se algum efeito está ativo
     * @return true se algum efeito está ativo
     */
    public boolean hasActiveEffects() {
        return getActiveEffectsCount() > 0;
    }
    
    @Override
    public String toString() {
        return "EffectParameters{" +
                "gain=" + gain + " (enabled=" + gainEnabled + "), " +
                "distortion=" + distortion + " (enabled=" + distortionEnabled + "), " +
                "delay=" + delayTime + "/" + delayFeedback + "/" + delayMix + " (enabled=" + delayEnabled + "), " +
                "reverb=" + reverbRoomSize + "/" + reverbDamping + "/" + reverbMix + " (enabled=" + reverbEnabled + "), " +
                "activeEffects=" + getActiveEffectsCount() +
                '}';
    }
} 