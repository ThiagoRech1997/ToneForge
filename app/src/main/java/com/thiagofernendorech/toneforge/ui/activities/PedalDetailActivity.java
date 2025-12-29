package com.thiagofernendorech.toneforge;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.thiagofernendorech.toneforge.ui.widgets.RealisticKnob;

public class PedalDetailActivity extends AppCompatActivity {
    
    private PedalEffect currentPedal;
    private AudioEngine audioEngine;
    
    // Views principais
    private TextView pedalDetailTitle;
    private TextView pedalDetailSubtitle;
    private TextView pedalVisualName;
    private ImageView pedalTypeIcon;
    private LinearLayout pedalVisualContainer;
    private View powerLed;
    private View clippingLed;
    private SwitchCompat pedalMainSwitch;
    
    // Knobs
    private RealisticKnob knobMain;
    private RealisticKnob knobSecondary1;
    private RealisticKnob knobSecondary2;
    private TextView knobMainLabel;
    private TextView knobSecondary1Label;
    private TextView knobSecondary2Label;
    
    // Controles avançados
    private LinearLayout advancedControlsContainer;
    
    // Informações
    private TextView pedalPositionInfo;
    private TextView pedalCpuUsage;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedal_detail);
        setTitle("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
            }
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        
        // Inicializar AudioEngine
        audioEngine = AudioEngine.getInstance();
        
        // Obter dados do pedal passados pela Intent
        loadPedalFromIntent();
        
        // Inicializar views
        initializeViews();
        
        // Configurar pedal
        setupPedalVisual();
        setupKnobs();
        setupAdvancedControls();
        setupEventListeners();
        
        // Animar entrada
        animateEntry();
    }
    
    private void loadPedalFromIntent() {
        Intent intent = getIntent();
        String pedalType = intent.getStringExtra("pedal_type");
        int pedalPosition = intent.getIntExtra("pedal_position", 0);
        
        // Criar ou carregar pedal específico
        currentPedal = createPedalFromType(pedalType);
        currentPedal.setPosition(pedalPosition);
    }
    
    private PedalEffect createPedalFromType(String type) {
        PedalEffect pedal = new PedalEffect();
        pedal.setName(type.toUpperCase());
        pedal.setType(type);
        pedal.setEnabled(true);
        
        switch (type.toLowerCase()) {
            case "gain":
                setupGainPedal(pedal);
                break;
            case "overdrive":
                setupOverdrivePedal(pedal);
                break;
            case "distortion":
                setupDistortionPedal(pedal);
                break;
            case "chorus":
                setupChorusPedal(pedal);
                break;
            case "delay":
                setupDelayPedal(pedal);
                break;
            case "reverb":
                setupReverbPedal(pedal);
                break;
            default:
                setupDefaultPedal(pedal);
                break;
        }
        
        return pedal;
    }
    
    private void setupGainPedal(PedalEffect pedal) {
        pedal.setMainKnobLabel("GAIN");
        pedal.setSecondaryKnob1Label("TONE");
        pedal.setSecondaryKnob2Label("LEVEL");
        pedal.setColor("#FF6B6B"); // Vermelho
        pedal.setSubtitle("Clean Boost / Overdrive");
    }
    
    private void setupOverdrivePedal(PedalEffect pedal) {
        pedal.setMainKnobLabel("DRIVE");
        pedal.setSecondaryKnob1Label("TONE");
        pedal.setSecondaryKnob2Label("LEVEL");
        pedal.setColor("#FF8E53"); // Laranja
        pedal.setSubtitle("Vintage Tube Overdrive");
    }
    
    private void setupDistortionPedal(PedalEffect pedal) {
        pedal.setMainKnobLabel("DISTORTION");
        pedal.setSecondaryKnob1Label("TONE");
        pedal.setSecondaryKnob2Label("LEVEL");
        pedal.setColor("#E74C3C"); // Vermelho escuro
        pedal.setSubtitle("High-Gain Distortion");
    }
    
    private void setupChorusPedal(PedalEffect pedal) {
        pedal.setMainKnobLabel("DEPTH");
        pedal.setSecondaryKnob1Label("RATE");
        pedal.setSecondaryKnob2Label("MIX");
        pedal.setColor("#3498DB"); // Azul
        pedal.setSubtitle("Stereo Chorus");
    }
    
    private void setupDelayPedal(PedalEffect pedal) {
        pedal.setMainKnobLabel("TIME");
        pedal.setSecondaryKnob1Label("FEEDBACK");
        pedal.setSecondaryKnob2Label("MIX");
        pedal.setColor("#2ECC71"); // Verde
        pedal.setSubtitle("Digital Delay");
    }
    
    private void setupReverbPedal(PedalEffect pedal) {
        pedal.setMainKnobLabel("DECAY");
        pedal.setSecondaryKnob1Label("TONE");
        pedal.setSecondaryKnob2Label("MIX");
        pedal.setColor("#9B59B6"); // Roxo
        pedal.setSubtitle("Hall Reverb");
    }
    
    private void setupDefaultPedal(PedalEffect pedal) {
        pedal.setMainKnobLabel("LEVEL");
        pedal.setSecondaryKnob1Label("PARAM1");
        pedal.setSecondaryKnob2Label("PARAM2");
        pedal.setColor("#95A5A6"); // Cinza
        pedal.setSubtitle("Generic Effect");
    }
    
    private void initializeViews() {
        // Header
        pedalDetailTitle = findViewById(R.id.pedalDetailTitle);
        pedalDetailSubtitle = findViewById(R.id.pedalDetailSubtitle);
        
        // Visual do pedal
        pedalVisualName = findViewById(R.id.pedalVisualName);
        pedalTypeIcon = findViewById(R.id.pedalTypeIcon);
        pedalVisualContainer = findViewById(R.id.pedalVisualContainer);
        powerLed = findViewById(R.id.powerLed);
        clippingLed = findViewById(R.id.clippingLed);
        pedalMainSwitch = findViewById(R.id.pedalMainSwitch);
        
        // Knobs
        knobMain = findViewById(R.id.knobMain);
        knobSecondary1 = findViewById(R.id.knobSecondary1);
        knobSecondary2 = findViewById(R.id.knobSecondary2);
        knobMainLabel = findViewById(R.id.knobMainLabel);
        knobSecondary1Label = findViewById(R.id.knobSecondary1Label);
        knobSecondary2Label = findViewById(R.id.knobSecondary2Label);
        
        // Controles avançados
        advancedControlsContainer = findViewById(R.id.advancedControlsContainer);
        
        // Informações
        pedalPositionInfo = findViewById(R.id.pedalPositionInfo);
        pedalCpuUsage = findViewById(R.id.pedalCpuUsage);
        
        // Botão voltar
        findViewById(R.id.btnBackToPedalboard).setOnClickListener(v -> finish());
    }
    
    private void setupPedalVisual() {
        // Configurar textos
        pedalDetailTitle.setText(currentPedal.getName());
        pedalDetailSubtitle.setText(currentPedal.getSubtitle());
        pedalVisualName.setText(currentPedal.getName());
        
        // Configurar cor do pedal
        int color = android.graphics.Color.parseColor(currentPedal.getColor());
        pedalVisualContainer.getBackground().setTint(color);
        
        // Configurar labels dos knobs
        knobMainLabel.setText(currentPedal.getMainKnobLabel());
        knobSecondary1Label.setText(currentPedal.getSecondaryKnob1Label());
        knobSecondary2Label.setText(currentPedal.getSecondaryKnob2Label());
        
        // Configurar estado inicial
        pedalMainSwitch.setChecked(currentPedal.isEnabled());
        updateLeds();
        
        // Configurar posição
        pedalPositionInfo.setText(String.format("%dº pedal (%s)", 
            currentPedal.getPosition() + 1, currentPedal.getType()));
        
        // CPU simulado
        pedalCpuUsage.setText("8%");
    }
    
    private void setupKnobs() {
        // Knob principal
        knobMain.setProgress(currentPedal.getMainKnobValue() / 100f);
        knobMain.setOnKnobChangeListener((progress, value) -> {
            currentPedal.setMainKnobValue(value);
            applyEffectToAudio();
            updateClippingLed(value > 80); // Simular clipping
        });
        
        // Knobs secundários
        knobSecondary1.setProgress(currentPedal.getSecondaryKnob1ValueAsPercentage() / 100f);
        knobSecondary1.setOnKnobChangeListener((progress, value) -> {
            currentPedal.setSecondaryKnob1ValueAsPercentage(value);
            applyEffectToAudio();
        });
        
        knobSecondary2.setProgress(currentPedal.getSecondaryKnob2ValueAsPercentage() / 100f);
        knobSecondary2.setOnKnobChangeListener((progress, value) -> {
            currentPedal.setSecondaryKnob2ValueAsPercentage(value);
            applyEffectToAudio();
        });
    }
    
    private void setupAdvancedControls() {
        // Adicionar controles específicos baseado no tipo
        switch (currentPedal.getType().toLowerCase()) {
            case "distortion":
                addDistortionAdvancedControls();
                break;
            case "delay":
                addDelayAdvancedControls();
                break;
            case "chorus":
                addChorusAdvancedControls();
                break;
            default:
                addGenericAdvancedControls();
                break;
        }
    }
    
    private void addDistortionAdvancedControls() {
        // Controles específicos para distorção
        addSliderControl("Tipo de Distorção", new String[]{"Soft Clip", "Hard Clip", "Fuzz"});
        addSliderControl("Mix (Dry/Wet)", 0, 100, 100);
    }
    
    private void addDelayAdvancedControls() {
        // Controles específicos para delay
        addSliderControl("Sincronizar BPM", true);
        addSliderControl("BPM", 60, 200, 120);
        addSliderControl("Tap Tempo", false);
    }
    
    private void addChorusAdvancedControls() {
        // Controles específicos para chorus
        addSliderControl("Forma de Onda", new String[]{"Sine", "Triangle", "Square"});
        addSliderControl("Stereo Width", 0, 100, 50);
    }
    
    private void addGenericAdvancedControls() {
        // Controles genéricos
        addSliderControl("Entrada", 0, 100, 75);
        addSliderControl("Saída", 0, 100, 75);
    }
    
    private void addSliderControl(String label, int min, int max, int defaultValue) {
        // Implementar slider customizado
        // Código simplificado - seria implementado com SeekBar
    }
    
    private void addSliderControl(String label, String[] options) {
        // Implementar seletor de opções
        // Código simplificado - seria implementado com Spinner
    }
    
    private void addSliderControl(String label, boolean defaultValue) {
        // Implementar switch
        // Código simplificado - seria implementado com Switch
    }
    
    private void setupEventListeners() {
        // Switch principal
        pedalMainSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentPedal.setEnabled(isChecked);
            updateLeds();
            applyEffectToAudio();
            
            // Animação do LED
            animateLedChange();
        });
        
        // Presets rápidos
        findViewById(R.id.btnPresetClean).setOnClickListener(v -> applyPreset("clean"));
        findViewById(R.id.btnPresetCrunch).setOnClickListener(v -> applyPreset("crunch"));
        findViewById(R.id.btnPresetHeavy).setOnClickListener(v -> applyPreset("heavy"));
    }
    
    private void applyPreset(String presetName) {
        switch (presetName) {
            case "clean":
                knobMain.setProgress(0.2f);
                knobSecondary1.setProgress(0.6f);
                knobSecondary2.setProgress(0.5f);
                break;
            case "crunch":
                knobMain.setProgress(0.6f);
                knobSecondary1.setProgress(0.4f);
                knobSecondary2.setProgress(0.7f);
                break;
            case "heavy":
                knobMain.setProgress(0.9f);
                knobSecondary1.setProgress(0.3f);
                knobSecondary2.setProgress(0.8f);
                break;
        }
        
        // Atualizar valores no pedal
        currentPedal.setMainKnobValue(knobMain.getProgress() * 100);
        currentPedal.setSecondaryKnob1Value(knobSecondary1.getProgress() * 100);
        currentPedal.setSecondaryKnob2Value(knobSecondary2.getProgress() * 100);
        
        applyEffectToAudio();
    }
    
    private void applyEffectToAudio() {
        if (audioEngine == null) return;
        
        // Aplicar configurações do pedal ao AudioEngine
        switch (currentPedal.getType().toLowerCase()) {
            case "gain":
                AudioEngine.setGainEnabled(currentPedal.isEnabled());
                AudioEngine.setGainLevel(currentPedal.getMainValue());
                break;
            case "distortion":
                AudioEngine.setDistortionEnabled(currentPedal.isEnabled());
                AudioEngine.setDistortionLevel(currentPedal.getMainValue());
                break;
            case "delay":
                AudioEngine.setDelayEnabled(currentPedal.isEnabled());
                AudioEngine.setDelayTime(currentPedal.getMainKnobValue() * 10);
                AudioEngine.setDelayFeedback(currentPedal.getSecondaryKnob1ValueAsPercentage() / 100f);
                AudioEngine.setDelayMix(currentPedal.getSecondaryKnob2ValueAsPercentage() / 100f);
                break;
            case "chorus":
                AudioEngine.setChorusEnabled(currentPedal.isEnabled());
                AudioEngine.setChorusDepth(currentPedal.getMainValue());
                AudioEngine.setChorusRate(currentPedal.getSecondaryKnob1ValueAsPercentage() / 100f);
                AudioEngine.setChorusMix(currentPedal.getSecondaryKnob2ValueAsPercentage() / 100f);
                break;
            case "reverb":
                AudioEngine.setReverbEnabled(currentPedal.isEnabled());
                AudioEngine.setReverbLevel(currentPedal.getMainValue());
                AudioEngine.setReverbRoomSize(currentPedal.getSecondaryKnob1ValueAsPercentage() / 100f);
                AudioEngine.setReverbDamping(currentPedal.getSecondaryKnob2ValueAsPercentage() / 100f);
                break;
            // Adicionar outros efeitos conforme necessário
        }
    }
    
    private void updateLeds() {
        // LED de power
        if (currentPedal.isEnabled()) {
            powerLed.setBackgroundResource(R.drawable.led_power_active);
        } else {
            powerLed.setBackgroundResource(R.drawable.led_power_inactive);
        }
    }
    
    private void updateClippingLed(boolean clipping) {
        if (clipping && currentPedal.isEnabled()) {
            clippingLed.setBackgroundResource(R.drawable.led_clipping_active);
        } else {
            clippingLed.setBackgroundResource(R.drawable.led_clipping_inactive);
        }
    }
    
    private void animateLedChange() {
        ObjectAnimator.ofFloat(powerLed, "scaleX", 1f, 1.2f, 1f).setDuration(200).start();
        ObjectAnimator.ofFloat(powerLed, "scaleY", 1f, 1.2f, 1f).setDuration(200).start();
    }
    
    private void animateEntry() {
        // Animação suave de entrada
        pedalVisualContainer.setAlpha(0f);
        pedalVisualContainer.setScaleX(0.8f);
        pedalVisualContainer.setScaleY(0.8f);
        
        pedalVisualContainer.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Retornar resultado com configurações atualizadas
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updated_pedal", currentPedal);
        setResult(Activity.RESULT_OK, resultIntent);
    }
} 