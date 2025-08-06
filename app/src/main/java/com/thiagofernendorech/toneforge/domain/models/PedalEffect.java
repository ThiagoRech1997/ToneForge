package com.thiagofernendorech.toneforge;

import java.io.Serializable;

/**
 * Representa um efeito como um pedal real
 * Contém todos os parâmetros visuais e funcionais de um pedal
 */
public class PedalEffect implements Serializable {
    
    private String name;
    private String type;
    private String category;
    private String subtitle;
    private String color;
    private boolean enabled;
    private float mainValue;
    private String mainKnobLabel;
    private int iconResourceId;
    private int backgroundColor;
    private int position;
    
    // Knobs secundários (opcionais)
    private Float secondaryKnob1Value;
    private String secondaryKnob1Label;
    private Float secondaryKnob2Value;
    private String secondaryKnob2Label;
    
    // Construtor padrão
    public PedalEffect() {
        this.enabled = true;
        this.mainValue = 0.5f;
        this.position = 0;
    }
    
    // Construtor principal
    public PedalEffect(String name, String category, String mainKnobLabel, int iconResourceId) {
        this();
        this.name = name;
        this.type = name.toLowerCase();
        this.category = category;
        this.mainKnobLabel = mainKnobLabel;
        this.iconResourceId = iconResourceId;
        this.backgroundColor = getDefaultBackgroundColor();
        this.color = getDefaultColorHex();
    }
    
    // Construtor com knobs secundários
    public PedalEffect(String name, String category, String mainKnobLabel, int iconResourceId,
                       String secondaryKnob1Label, String secondaryKnob2Label) {
        this(name, category, mainKnobLabel, iconResourceId);
        this.secondaryKnob1Label = secondaryKnob1Label;
        this.secondaryKnob2Label = secondaryKnob2Label;
        this.secondaryKnob1Value = 0.5f;
        this.secondaryKnob2Value = 0.5f;
    }
    
    // Factory methods para criar pedais específicos
    public static PedalEffect createOverdrive() {
        return new PedalEffect("Overdrive", "Distortion", "DRIVE", R.drawable.ic_volume_up, "TONE", "LEVEL");
    }
    
    public static PedalEffect createDistortion() {
        return new PedalEffect("Distortion", "Distortion", "DIST", R.drawable.ic_volume_up, "TONE", "LEVEL");
    }
    
    public static PedalEffect createDelay() {
        return new PedalEffect("Delay", "Time", "TIME", R.drawable.ic_av_timer, "FEEDBACK", "MIX");
    }
    
    public static PedalEffect createReverb() {
        return new PedalEffect("Reverb", "Time", "ROOM", R.drawable.ic_waves, "DAMPING", "MIX");
    }
    
    public static PedalEffect createChorus() {
        return new PedalEffect("Chorus", "Modulation", "DEPTH", R.drawable.ic_waves, "RATE", "MIX");
    }
    
    public static PedalEffect createFlanger() {
        return new PedalEffect("Flanger", "Modulation", "DEPTH", R.drawable.ic_rotate_left, "RATE", "FEEDBACK");
    }
    
    public static PedalEffect createPhaser() {
        return new PedalEffect("Phaser", "Modulation", "DEPTH", R.drawable.ic_target, "RATE", "FEEDBACK");
    }
    
    public static PedalEffect createEqualizer() {
        return new PedalEffect("EQ", "Filter", "FREQ", R.drawable.ic_graphic_eq, "GAIN", "Q");
    }
    
    public static PedalEffect createCompressor() {
        return new PedalEffect("Compressor", "Dynamics", "RATIO", R.drawable.ic_mic, "ATTACK", "RELEASE");
    }
    
    public static PedalEffect createGain() {
        return new PedalEffect("Gain", "Dynamics", "LEVEL", R.drawable.ic_volume_up);
    }
    
    // Getters e Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public float getMainValue() { return mainValue; }
    public void setMainValue(float mainValue) { this.mainValue = Math.max(0.0f, Math.min(1.0f, mainValue)); }
    
    public String getMainKnobLabel() { return mainKnobLabel; }
    public void setMainKnobLabel(String mainKnobLabel) { this.mainKnobLabel = mainKnobLabel; }
    
    public int getIconResourceId() { return iconResourceId; }
    public void setIconResourceId(int iconResourceId) { this.iconResourceId = iconResourceId; }
    
    public int getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(int backgroundColor) { this.backgroundColor = backgroundColor; }
    
    // Knobs secundários
    public boolean hasSecondaryKnobs() {
        return secondaryKnob1Label != null || secondaryKnob2Label != null;
    }
    
    public Float getSecondaryKnob1Value() { return secondaryKnob1Value; }
    public void setSecondaryKnob1Value(Float value) { 
        this.secondaryKnob1Value = value != null ? Math.max(0.0f, Math.min(1.0f, value)) : null;
    }
    
    public String getSecondaryKnob1Label() { return secondaryKnob1Label; }
    public void setSecondaryKnob1Label(String label) { this.secondaryKnob1Label = label; }
    
    public Float getSecondaryKnob2Value() { return secondaryKnob2Value; }
    public void setSecondaryKnob2Value(Float value) { 
        this.secondaryKnob2Value = value != null ? Math.max(0.0f, Math.min(1.0f, value)) : null;
    }
    
    public String getSecondaryKnob2Label() { return secondaryKnob2Label; }
    public void setSecondaryKnob2Label(String label) { this.secondaryKnob2Label = label; }
    
    // Novos campos para interface moderna
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    
    // Métodos de conveniência para compatibilidade com nova interface
    public float getMainKnobValue() { return mainValue * 100f; }
    public void setMainKnobValue(float value) { this.mainValue = value / 100f; }
    
    public float getSecondaryKnob1ValueAsPercentage() { 
        return secondaryKnob1Value != null ? secondaryKnob1Value * 100f : 50f; 
    }
    public void setSecondaryKnob1ValueAsPercentage(float value) { 
        this.secondaryKnob1Value = value / 100f; 
    }
    
    public float getSecondaryKnob2ValueAsPercentage() { 
        return secondaryKnob2Value != null ? secondaryKnob2Value * 100f : 50f; 
    }
    public void setSecondaryKnob2ValueAsPercentage(float value) { 
        this.secondaryKnob2Value = value / 100f; 
    }
    
    // Métodos utilitários
    private int getDefaultBackgroundColor() {
        switch (category) {
            case "Distortion":
                return 0xFFDC2626; // Vermelho
            case "Modulation":
                return 0xFF2563EB; // Azul
            case "Time":
                return 0xFF059669; // Verde
            case "Filter":
                return 0xFFCA8A04; // Amarelo
            case "Dynamics":
                return 0xFF7C3AED; // Roxo
            case "Ambient":
                return 0xFF4F46E5; // Índigo
            default:
                return 0xFF374151; // Cinza padrão
        }
    }
    
    private String getDefaultColorHex() {
        switch (category) {
            case "Distortion":
                return "#DC2626"; // Vermelho
            case "Modulation":
                return "#2563EB"; // Azul
            case "Time":
                return "#059669"; // Verde
            case "Filter":
                return "#CA8A04"; // Amarelo
            case "Dynamics":
                return "#7C3AED"; // Roxo
            case "Ambient":
                return "#4F46E5"; // Índigo
            default:
                return "#374151"; // Cinza padrão
        }
    }
    
    /**
     * Cria lista padrão de pedais para uma pedaleira
     */
    public static java.util.List<PedalEffect> createDefaultPedalboard() {
        java.util.List<PedalEffect> pedals = new java.util.ArrayList<>();
        
        pedals.add(createGain());
        pedals.add(createOverdrive());
        pedals.add(createDistortion());
        pedals.add(createChorus());
        pedals.add(createFlanger());
        pedals.add(createPhaser());
        pedals.add(createEqualizer());
        pedals.add(createCompressor());
        pedals.add(createDelay());
        pedals.add(createReverb());
        
        return pedals;
    }
    
    /**
     * Converte para string para debug
     */
    @Override
    public String toString() {
        return "PedalEffect{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", enabled=" + enabled +
                ", mainValue=" + mainValue +
                ", mainKnobLabel='" + mainKnobLabel + '\'' +
                '}';
    }
} 