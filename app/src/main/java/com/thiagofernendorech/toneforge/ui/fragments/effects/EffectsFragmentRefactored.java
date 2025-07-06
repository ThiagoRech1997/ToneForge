package com.thiagofernendorech.toneforge.ui.fragments.effects;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.thiagofernendorech.toneforge.R;
import com.thiagofernendorech.toneforge.ui.base.BaseFragment;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;
import com.thiagofernendorech.toneforge.domain.models.AudioState;
import com.thiagofernendorech.toneforge.EffectOrderAdapter;
import com.thiagofernendorech.toneforge.TooltipManager;
import java.util.List;
import java.util.ArrayList;

/**
 * EffectsFragment refatorado usando arquitetura MVP
 * Versão limpa e modular do fragment original
 */
public class EffectsFragmentRefactored extends BaseFragment<EffectsPresenter> implements EffectsContract.View {
    
    // === UI COMPONENTS ===
    
    // Status
    private TextView statusText;
    
    // Efeito: Ganho
    private Switch switchGain;
    private SeekBar seekGain;
    
    // Efeito: Distorção
    private Switch switchDistortion;
    private SeekBar seekDistortion;
    private SeekBar seekDistortionMix;
    private Spinner spinnerDistortionType;
    
    // Efeito: Delay
    private Switch switchDelay;
    private SeekBar seekDelayTime;
    private SeekBar seekDelayFeedback;
    private SeekBar seekDelayMix;
    private Switch switchDelaySyncBPM;
    private SeekBar seekDelayBPM;
    
    // Efeito: Reverb
    private Switch switchReverb;
    private SeekBar seekReverb;
    private SeekBar seekReverbRoomSize;
    private SeekBar seekReverbDamping;
    private SeekBar seekReverbMix;
    private Spinner spinnerReverbType;
    
    // Efeito: Chorus
    private Switch switchChorus;
    private SeekBar seekChorusDepth;
    private SeekBar seekChorusRate;
    private SeekBar seekChorusFeedback;
    private SeekBar seekChorusMix;
    
    // Efeito: Flanger
    private Switch switchFlanger;
    private SeekBar seekFlangerDepth;
    private SeekBar seekFlangerRate;
    private SeekBar seekFlangerFeedback;
    private SeekBar seekFlangerMix;
    
    // Efeito: Phaser
    private Switch switchPhaser;
    private SeekBar seekPhaserDepth;
    private SeekBar seekPhaserRate;
    private SeekBar seekPhaserFeedback;
    private SeekBar seekPhaserMix;
    
    // Efeito: EQ
    private Switch switchEQ;
    private SeekBar seekEQLow;
    private SeekBar seekEQMid;
    private SeekBar seekEQHigh;
    private SeekBar seekEQMix;
    
    // Efeito: Compressor
    private Switch switchCompressor;
    private SeekBar seekCompressorThreshold;
    private SeekBar seekCompressorRatio;
    private SeekBar seekCompressorAttack;
    private SeekBar seekCompressorRelease;
    private SeekBar seekCompressorMix;
    
    // Presets
    private Spinner presetSpinner;
    private EditText presetNameEdit;
    private Button savePresetButton, deletePresetButton;
    private Button btnFavorites, btnExportPreset, btnImportPreset;
    private ArrayAdapter<String> presetAdapter;
    
    // Automação
    private EditText automationNameEdit;
    private Button btnStartRecording, btnStopRecording;
    private Button btnStartPlayback, btnStopPlayback;
    private TextView automationStatusText;
    private SeekBar automationProgressBar;
    private TextView automationProgressText;
    private Spinner automationSpinner;
    private ArrayAdapter<String> automationAdapter;
    private Button btnExportAutomation, btnImportAutomation, btnDeleteAutomation;
    
    // Ordem dos efeitos
    private RecyclerView effectsOrderRecycler;
    private EffectOrderAdapter effectOrderAdapter;
    private ArrayList<String> effectOrder;
    
    // Reset buttons
    private Button btnResetAll;
    
    // MIDI
    private TextView midiStatusText;
    private Switch switchMidiEnabled;
    
    @Override
    protected EffectsPresenter createPresenter() {
        AudioRepository audioRepository = AudioRepository.getInstance(requireContext());
        return new EffectsPresenter(requireContext(), audioRepository);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_effects, container, false);
        
        initializeViews(view);
        setupClickListeners();
        setupSeekBarListeners();
        setupSpinnerListeners();
        setupRecyclerView();
        setupTooltips(view);
        
        return view;
    }
    
    @Override
    protected void onViewReady() {
        super.onViewReady();
        if (presenter != null) {
            presenter.onViewStarted();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.onViewResumed();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (presenter != null) {
            presenter.onViewPaused();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.onViewDestroyed();
        }
    }
    
    // === INICIALIZAÇÃO ===
    
    /**
     * Inicializa todas as views do fragment
     */
    private void initializeViews(View view) {
        // Status
        statusText = view.findViewById(R.id.effectsStatusText);
        
        // Ganho
        switchGain = view.findViewById(R.id.switchGain);
        seekGain = view.findViewById(R.id.seekGain);
        
        // Distorção
        switchDistortion = view.findViewById(R.id.switchDistortion);
        seekDistortion = view.findViewById(R.id.seekDistortion);
        seekDistortionMix = view.findViewById(R.id.seekDistortionMix);
        spinnerDistortionType = view.findViewById(R.id.spinnerDistortionType);
        
        // Delay
        switchDelay = view.findViewById(R.id.switchDelay);
        seekDelayTime = view.findViewById(R.id.seekDelayTime);
        seekDelayFeedback = view.findViewById(R.id.seekDelayFeedback);
        seekDelayMix = view.findViewById(R.id.seekDelayMix);
        switchDelaySyncBPM = view.findViewById(R.id.switchDelaySyncBPM);
        seekDelayBPM = view.findViewById(R.id.seekDelayBPM);
        
        // Reverb
        switchReverb = view.findViewById(R.id.switchReverb);
        seekReverb = view.findViewById(R.id.seekReverb);
        seekReverbRoomSize = view.findViewById(R.id.seekReverbRoomSize);
        seekReverbDamping = view.findViewById(R.id.seekReverbDamping);
        seekReverbMix = view.findViewById(R.id.seekReverbMix);
        spinnerReverbType = view.findViewById(R.id.spinnerReverbType);
        
        // Chorus
        switchChorus = view.findViewById(R.id.switchChorus);
        seekChorusDepth = view.findViewById(R.id.seekChorusDepth);
        seekChorusRate = view.findViewById(R.id.seekChorusRate);
        // seekChorusFeedback = view.findViewById(R.id.seekChorusFeedback); // ID não existe
        seekChorusMix = view.findViewById(R.id.seekChorusMix);
        
        // Flanger
        switchFlanger = view.findViewById(R.id.switchFlanger);
        seekFlangerDepth = view.findViewById(R.id.seekFlangerDepth);
        seekFlangerRate = view.findViewById(R.id.seekFlangerRate);
        seekFlangerFeedback = view.findViewById(R.id.seekFlangerFeedback);
        seekFlangerMix = view.findViewById(R.id.seekFlangerMix);
        
        // Phaser
        switchPhaser = view.findViewById(R.id.switchPhaser);
        seekPhaserDepth = view.findViewById(R.id.seekPhaserDepth);
        seekPhaserRate = view.findViewById(R.id.seekPhaserRate);
        seekPhaserFeedback = view.findViewById(R.id.seekPhaserFeedback);
        seekPhaserMix = view.findViewById(R.id.seekPhaserMix);
        
        // EQ
        switchEQ = view.findViewById(R.id.switchEQ);
        seekEQLow = view.findViewById(R.id.seekEQLow);
        seekEQMid = view.findViewById(R.id.seekEQMid);
        seekEQHigh = view.findViewById(R.id.seekEQHigh);
        seekEQMix = view.findViewById(R.id.seekEQMix);
        
        // Compressor
        switchCompressor = view.findViewById(R.id.switchCompressor);
        seekCompressorThreshold = view.findViewById(R.id.seekCompressorThreshold);
        seekCompressorRatio = view.findViewById(R.id.seekCompressorRatio);
        seekCompressorAttack = view.findViewById(R.id.seekCompressorAttack);
        seekCompressorRelease = view.findViewById(R.id.seekCompressorRelease);
        seekCompressorMix = view.findViewById(R.id.seekCompressorMix);
        
        // Presets
        presetSpinner = view.findViewById(R.id.presetSpinner);
        presetNameEdit = view.findViewById(R.id.presetNameEdit);
        savePresetButton = view.findViewById(R.id.savePresetButton);
        deletePresetButton = view.findViewById(R.id.deletePresetButton);
        // btnFavorites = view.findViewById(R.id.btnFavorites); // ID não existe
        btnExportPreset = view.findViewById(R.id.btnExportPreset);
        btnImportPreset = view.findViewById(R.id.btnImportPreset);
        
        // Automação
        automationNameEdit = view.findViewById(R.id.automationNameEdit);
        btnStartRecording = view.findViewById(R.id.btnStartRecording);
        btnStopRecording = view.findViewById(R.id.btnStopRecording);
        btnStartPlayback = view.findViewById(R.id.btnStartPlayback);
        btnStopPlayback = view.findViewById(R.id.btnStopPlayback);
        automationStatusText = view.findViewById(R.id.automationStatusText);
        automationProgressBar = view.findViewById(R.id.automationProgressBar);
        automationProgressText = view.findViewById(R.id.automationProgressText);
        automationSpinner = view.findViewById(R.id.automationSpinner);
        btnExportAutomation = view.findViewById(R.id.btnExportAutomation);
        btnImportAutomation = view.findViewById(R.id.btnImportAutomation);
        btnDeleteAutomation = view.findViewById(R.id.btnDeleteAutomation);
        
        // Ordem dos efeitos
        effectsOrderRecycler = view.findViewById(R.id.effectsOrderRecycler);
        
        // Reset
        btnResetAll = view.findViewById(R.id.btnResetAll);
        
        // MIDI
        // midiStatusText = view.findViewById(R.id.midiStatusText); // ID não existe
        switchMidiEnabled = view.findViewById(R.id.switchMidiEnabled);
        
        // Inicializar adapters
        effectOrder = new ArrayList<>();
        presetAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
        automationAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
        
        if (presetSpinner != null) {
            presetSpinner.setAdapter(presetAdapter);
        }
        if (automationSpinner != null) {
            automationSpinner.setAdapter(automationAdapter);
        }
    }
    
    /**
     * Configura listeners de clique
     */
    private void setupClickListeners() {
        // Switches dos efeitos
        if (switchGain != null) {
            switchGain.setOnCheckedChangeListener((buttonView, isChecked) -> 
                presenter.setEffectEnabled("Ganho", isChecked));
        }
        
        if (switchDistortion != null) {
            switchDistortion.setOnCheckedChangeListener((buttonView, isChecked) -> 
                presenter.setEffectEnabled("Distorção", isChecked));
        }
        
        if (switchDelay != null) {
            switchDelay.setOnCheckedChangeListener((buttonView, isChecked) -> 
                presenter.setEffectEnabled("Delay", isChecked));
        }
        
        if (switchReverb != null) {
            switchReverb.setOnCheckedChangeListener((buttonView, isChecked) -> 
                presenter.setEffectEnabled("Reverb", isChecked));
        }
        
        if (switchChorus != null) {
            switchChorus.setOnCheckedChangeListener((buttonView, isChecked) -> 
                presenter.setEffectEnabled("Chorus", isChecked));
        }
        
        if (switchFlanger != null) {
            switchFlanger.setOnCheckedChangeListener((buttonView, isChecked) -> 
                presenter.setEffectEnabled("Flanger", isChecked));
        }
        
        if (switchPhaser != null) {
            switchPhaser.setOnCheckedChangeListener((buttonView, isChecked) -> 
                presenter.setEffectEnabled("Phaser", isChecked));
        }
        
        if (switchEQ != null) {
            switchEQ.setOnCheckedChangeListener((buttonView, isChecked) -> 
                presenter.setEffectEnabled("EQ", isChecked));
        }
        
        if (switchCompressor != null) {
            switchCompressor.setOnCheckedChangeListener((buttonView, isChecked) -> 
                presenter.setEffectEnabled("Compressor", isChecked));
        }
        
        // Botões de preset
        if (savePresetButton != null) {
            savePresetButton.setOnClickListener(v -> {
                String presetName = presetNameEdit != null ? presetNameEdit.getText().toString().trim() : "";
                if (!presetName.isEmpty()) {
                    presenter.savePreset(presetName);
                } else {
                    showSavePresetDialog();
                }
            });
        }
        
        if (deletePresetButton != null) {
            deletePresetButton.setOnClickListener(v -> {
                String selectedPreset = getSelectedPreset();
                if (selectedPreset != null && !selectedPreset.isEmpty()) {
                    showDeletePresetConfirmation(selectedPreset);
                }
            });
        }
        
        if (btnFavorites != null) {
            btnFavorites.setOnClickListener(v -> presenter.toggleFavoritesFilter());
        }
        
        if (btnExportPreset != null) {
            btnExportPreset.setOnClickListener(v -> showExportPresetDialog());
        }
        
        if (btnImportPreset != null) {
            btnImportPreset.setOnClickListener(v -> openFilePicker(EffectsPresenter.IMPORT_PRESET_REQUEST));
        }
        
        // Botões de automação
        if (btnStartRecording != null) {
            btnStartRecording.setOnClickListener(v -> {
                String automationName = automationNameEdit != null ? automationNameEdit.getText().toString().trim() : "";
                if (!automationName.isEmpty()) {
                    presenter.startAutomationRecording(automationName);
                } else {
                    showSaveAutomationDialog();
                }
            });
        }
        
        if (btnStopRecording != null) {
            btnStopRecording.setOnClickListener(v -> presenter.stopAutomationRecording());
        }
        
        if (btnStartPlayback != null) {
            btnStartPlayback.setOnClickListener(v -> {
                String selectedAutomation = getSelectedAutomation();
                if (selectedAutomation != null && !selectedAutomation.isEmpty()) {
                    presenter.startAutomationPlayback(selectedAutomation);
                }
            });
        }
        
        if (btnStopPlayback != null) {
            btnStopPlayback.setOnClickListener(v -> presenter.stopAutomationPlayback());
        }
        
        if (btnExportAutomation != null) {
            btnExportAutomation.setOnClickListener(v -> showExportAutomationDialog());
        }
        
        if (btnImportAutomation != null) {
            btnImportAutomation.setOnClickListener(v -> openFilePicker(EffectsPresenter.IMPORT_AUTOMATION_REQUEST));
        }
        
        if (btnDeleteAutomation != null) {
            btnDeleteAutomation.setOnClickListener(v -> {
                String selectedAutomation = getSelectedAutomation();
                if (selectedAutomation != null && !selectedAutomation.isEmpty()) {
                    presenter.deleteAutomation(selectedAutomation);
                }
            });
        }
        
        // Reset
        if (btnResetAll != null) {
            btnResetAll.setOnClickListener(v -> presenter.resetAllEffects());
        }
        
        // MIDI
        if (switchMidiEnabled != null) {
            switchMidiEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> 
                presenter.setMidiEnabled(isChecked));
        }
    }
    
    /**
     * Configura listeners dos SeekBars
     */
    private void setupSeekBarListeners() {
        setupSeekBarListener(seekGain, "Ganho", "level", 100.0f);
        setupSeekBarListener(seekDistortion, "Distorção", "amount", 100.0f);
        setupSeekBarListener(seekDistortionMix, "Distorção", "mix", 100.0f);
        
        setupSeekBarListener(seekDelayTime, "Delay", "time", 1.0f);
        setupSeekBarListener(seekDelayFeedback, "Delay", "feedback", 100.0f);
        setupSeekBarListener(seekDelayMix, "Delay", "mix", 100.0f);
        setupSeekBarListener(seekDelayBPM, "Delay", "bpm", 1.0f);
        
        setupSeekBarListener(seekReverb, "Reverb", "level", 100.0f);
        setupSeekBarListener(seekReverbRoomSize, "Reverb", "roomsize", 100.0f);
        setupSeekBarListener(seekReverbDamping, "Reverb", "damping", 100.0f);
        setupSeekBarListener(seekReverbMix, "Reverb", "mix", 100.0f);
        
        setupSeekBarListener(seekChorusDepth, "Chorus", "depth", 100.0f);
        setupSeekBarListener(seekChorusRate, "Chorus", "rate", 20.0f);
        setupSeekBarListener(seekChorusFeedback, "Chorus", "feedback", 100.0f);
        setupSeekBarListener(seekChorusMix, "Chorus", "mix", 100.0f);
        
        setupSeekBarListener(seekFlangerDepth, "Flanger", "depth", 100.0f);
        setupSeekBarListener(seekFlangerRate, "Flanger", "rate", 20.0f);
        setupSeekBarListener(seekFlangerFeedback, "Flanger", "feedback", 100.0f);
        setupSeekBarListener(seekFlangerMix, "Flanger", "mix", 100.0f);
        
        setupSeekBarListener(seekPhaserDepth, "Phaser", "depth", 100.0f);
        setupSeekBarListener(seekPhaserRate, "Phaser", "rate", 20.0f);
        setupSeekBarListener(seekPhaserFeedback, "Phaser", "feedback", 100.0f);
        setupSeekBarListener(seekPhaserMix, "Phaser", "mix", 100.0f);
        
        setupSeekBarListener(seekEQLow, "EQ", "low", 50.0f, -12.0f);
        setupSeekBarListener(seekEQMid, "EQ", "mid", 50.0f, -12.0f);
        setupSeekBarListener(seekEQHigh, "EQ", "high", 50.0f, -12.0f);
        setupSeekBarListener(seekEQMix, "EQ", "mix", 100.0f);
        
        setupSeekBarListener(seekCompressorThreshold, "Compressor", "threshold", 1.0f);
        setupSeekBarListener(seekCompressorRatio, "Compressor", "ratio", 1.0f);
        setupSeekBarListener(seekCompressorAttack, "Compressor", "attack", 1.0f);
        setupSeekBarListener(seekCompressorRelease, "Compressor", "release", 1.0f);
        setupSeekBarListener(seekCompressorMix, "Compressor", "mix", 100.0f);
    }
    
    /**
     * Configura listener para um SeekBar específico
     */
    private void setupSeekBarListener(SeekBar seekBar, String effectName, String parameterName, float scale) {
        setupSeekBarListener(seekBar, effectName, parameterName, scale, 0.0f);
    }
    
    /**
     * Configura listener para um SeekBar específico com offset
     */
    private void setupSeekBarListener(SeekBar seekBar, String effectName, String parameterName, float scale, float offset) {
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && presenter != null) {
                        float value = (progress / scale) + offset;
                        presenter.setEffectParameter(effectName, parameterName, value);
                    }
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }
    
    /**
     * Configura listeners dos Spinners
     */
    private void setupSpinnerListeners() {
        if (spinnerDistortionType != null) {
            spinnerDistortionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    presenter.setEffectParameter("Distorção", "type", position);
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
        
        if (spinnerReverbType != null) {
            spinnerReverbType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    presenter.setEffectParameter("Reverb", "type", position);
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
        
        if (presetSpinner != null) {
            presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String presetName = (String) parent.getItemAtPosition(position);
                    if (presetName != null && !presetName.isEmpty()) {
                        presenter.loadPreset(presetName);
                    }
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }
    
    /**
     * Configura RecyclerView para ordem dos efeitos
     */
    private void setupRecyclerView() {
        if (effectsOrderRecycler != null) {
            effectOrderAdapter = new EffectOrderAdapter(effectOrder, null);
            effectsOrderRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            effectsOrderRecycler.setAdapter(effectOrderAdapter);
            
            // Configurar drag & drop
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, 
                                    RecyclerView.ViewHolder target) {
                    int fromPosition = viewHolder.getAdapterPosition();
                    int toPosition = target.getAdapterPosition();
                    presenter.moveEffect(fromPosition, toPosition);
                    return true;
                }
                
                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    // Não implementar swipe
                }
            });
            
            itemTouchHelper.attachToRecyclerView(effectsOrderRecycler);
        }
    }
    
    /**
     * Configura tooltips para todos os controles
     */
    private void setupTooltips(View view) {
        // Implementar tooltips usando TooltipManager
        // Por ora, implementar para alguns controles principais
        
        if (seekGain != null) {
            seekGain.setOnLongClickListener(v -> {
                showTooltip("Controla o nível de ganho do sinal");
                return true;
            });
        }
        
        if (switchGain != null) {
            switchGain.setOnLongClickListener(v -> {
                showTooltip("Liga/desliga o efeito de ganho");
                return true;
            });
        }
        
        // Adicionar mais tooltips conforme necessário
    }
    
    // === IMPLEMENTAÇÃO DA INTERFACE EffectsContract.View ===
    
    @Override
    public void updateEffectsStatus(String status, boolean isActive) {
        if (statusText != null) {
            statusText.setText(status);
            statusText.setTextColor(getResources().getColor(
                isActive ? R.color.green : R.color.red));
        }
    }
    
    @Override
    public void updateAudioState(AudioState audioState) {
        // Atualizar UI baseado no estado do áudio
        String statusDescription = audioState.getStatusDescription();
        boolean isActive = audioState.isPipelineRunning();
        updateEffectsStatus(statusDescription, isActive);
    }
    
    @Override
    public void updateEffectParameters(EffectParameters parameters) {
        // Atualizar todos os controles com os novos parâmetros
        updateSeekBarValue(seekGain, parameters.getGain(), 100.0f);
        updateSwitchValue(switchGain, parameters.isGainEnabled());
        
        updateSeekBarValue(seekDistortion, parameters.getDistortion(), 100.0f);
        updateSwitchValue(switchDistortion, parameters.isDistortionEnabled());
        
        updateSeekBarValue(seekDelayTime, parameters.getDelayTime(), 1.0f);
        updateSeekBarValue(seekDelayFeedback, parameters.getDelayFeedback(), 100.0f);
        updateSeekBarValue(seekDelayMix, parameters.getDelayMix(), 100.0f);
        updateSwitchValue(switchDelay, parameters.isDelayEnabled());
        
        updateSeekBarValue(seekReverb, parameters.getReverbRoomSize(), 100.0f);
        updateSeekBarValue(seekReverbRoomSize, parameters.getReverbRoomSize(), 100.0f);
        updateSeekBarValue(seekReverbDamping, parameters.getReverbDamping(), 100.0f);
        updateSeekBarValue(seekReverbMix, parameters.getReverbMix(), 100.0f);
        updateSwitchValue(switchReverb, parameters.isReverbEnabled());
        
        // Continuar para outros efeitos...
    }
    
    @Override
    public void updateEffectState(String effectName, boolean enabled) {
        // Atualizar switch específico
        Switch effectSwitch = getEffectSwitch(effectName);
        updateSwitchValue(effectSwitch, enabled);
    }
    
    @Override
    public void updateBypassIndicators() {
        // Atualizar indicadores visuais de bypass
        // Implementar atualização de cores/ícones dos efeitos
    }
    
    @Override
    public void updateEffectOrder(List<String> effectOrder) {
        this.effectOrder.clear();
        this.effectOrder.addAll(effectOrder);
        if (effectOrderAdapter != null) {
            effectOrderAdapter.notifyDataSetChanged();
        }
    }
    
    @Override
    public void updatePresetList(List<String> presets) {
        presetAdapter.clear();
        presetAdapter.addAll(presets);
        presetAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void selectPreset(String presetName) {
        if (presetSpinner != null) {
            int position = presetAdapter.getPosition(presetName);
            if (position >= 0) {
                presetSpinner.setSelection(position);
            }
        }
    }
    
    @Override
    public void showSavePresetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Salvar Preset");
        
        final EditText input = new EditText(getContext());
        input.setHint("Nome do preset");
        builder.setView(input);
        
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String presetName = input.getText().toString().trim();
            if (!presetName.isEmpty()) {
                presenter.savePreset(presetName);
            }
        });
        
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    
    @Override
    public void showDeletePresetConfirmation(String presetName) {
        new AlertDialog.Builder(getContext())
            .setTitle("Deletar Preset")
            .setMessage("Tem certeza que deseja deletar o preset '" + presetName + "'?")
            .setPositiveButton("Deletar", (dialog, which) -> presenter.deletePreset(presetName))
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    @Override
    public void showExportPresetDialog() {
        String presetName = getSelectedPreset();
        if (presetName != null && !presetName.isEmpty()) {
            openFileCreator(presetName + ".preset", EffectsPresenter.EXPORT_PRESET_REQUEST);
        }
    }
    
    @Override
    public void updateFavoriteButton(boolean isFavorite) {
        if (btnFavorites != null) {
            btnFavorites.setText(isFavorite ? "★" : "☆");
        }
    }
    
    @Override
    public void updateFavoritesFilter(boolean showFavoritesOnly) {
        if (btnFavorites != null) {
            btnFavorites.setBackgroundColor(getResources().getColor(
                showFavoritesOnly ? android.R.color.holo_blue_light : android.R.color.transparent));
        }
    }
    
    @Override
    public void updateAutomationStatus(boolean isRecording, boolean isPlaying, String statusText) {
        if (automationStatusText != null) {
            this.automationStatusText.setText(statusText);
        }
        
        // Atualizar botões baseado no estado
        if (btnStartRecording != null) btnStartRecording.setEnabled(!isRecording && !isPlaying);
        if (btnStopRecording != null) btnStopRecording.setEnabled(isRecording);
        if (btnStartPlayback != null) btnStartPlayback.setEnabled(!isRecording && !isPlaying);
        if (btnStopPlayback != null) btnStopPlayback.setEnabled(isPlaying);
    }
    
    @Override
    public void updateAutomationProgress(int progress, String timeText) {
        if (automationProgressBar != null) {
            automationProgressBar.setProgress(progress);
        }
        if (automationProgressText != null) {
            automationProgressText.setText(timeText);
        }
    }
    
    @Override
    public void updateAutomationList(List<String> automations) {
        automationAdapter.clear();
        automationAdapter.addAll(automations);
        automationAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void showSaveAutomationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Salvar Automação");
        
        final EditText input = new EditText(getContext());
        input.setHint("Nome da automação");
        builder.setView(input);
        
        builder.setPositiveButton("Gravar", (dialog, which) -> {
            String automationName = input.getText().toString().trim();
            if (!automationName.isEmpty()) {
                presenter.startAutomationRecording(automationName);
            }
        });
        
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    
    @Override
    public void showExportAutomationDialog() {
        String automationName = getSelectedAutomation();
        if (automationName != null && !automationName.isEmpty()) {
            openFileCreator(automationName + ".automation", EffectsPresenter.EXPORT_AUTOMATION_REQUEST);
        }
    }
    
    @Override
    public void updateMidiStatus(boolean enabled, int deviceCount) {
        if (midiStatusText != null) {
            midiStatusText.setText("MIDI: " + (enabled ? "Ativo" : "Inativo") + 
                                 " (" + deviceCount + " dispositivos)");
        }
        updateSwitchValue(switchMidiEnabled, enabled);
    }
    
    @Override
    public void showMidiLearnFeedback(String parameterName, boolean isActive) {
        String message = isActive ? 
            "MIDI Learn ativo para: " + parameterName : 
            "MIDI Learn desativado";
        showMessage(message);
    }
    
    @Override
    public void showTooltip(String message) {
        if (getContext() != null) {
            TooltipManager.showTooltip(getContext(), getView(), message);
        }
    }
    
    @Override
    public void requestAudioPermission() {
        if (getActivity() != null) {
            ActivityCompat.requestPermissions(getActivity(), 
                new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }
    
    @Override
    public void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, requestCode);
    }
    
    @Override
    public void openFileCreator(String fileName, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, requestCode);
    }
    
    // === MÉTODOS UTILITÁRIOS ===
    
    /**
     * Atualiza valor de um SeekBar sem disparar listeners
     */
    private void updateSeekBarValue(SeekBar seekBar, Float value, float scale) {
        if (seekBar != null && value != null) {
            seekBar.setProgress((int) (value * scale));
        }
    }
    
    /**
     * Atualiza valor de um Switch sem disparar listeners
     */
    private void updateSwitchValue(Switch switchView, Boolean value) {
        if (switchView != null && value != null) {
            switchView.setChecked(value);
        }
    }
    
    /**
     * Obtém o Switch de um efeito específico
     */
    private Switch getEffectSwitch(String effectName) {
        switch (effectName.toLowerCase()) {
            case "ganho": return switchGain;
            case "distorção": return switchDistortion;
            case "delay": return switchDelay;
            case "reverb": return switchReverb;
            case "chorus": return switchChorus;
            case "flanger": return switchFlanger;
            case "phaser": return switchPhaser;
            case "eq": return switchEQ;
            case "compressor": return switchCompressor;
            default: return null;
        }
    }
    
    /**
     * Obtém o preset selecionado
     */
    private String getSelectedPreset() {
        if (presetSpinner != null && presetSpinner.getSelectedItem() != null) {
            return presetSpinner.getSelectedItem().toString();
        }
        return null;
    }
    
    /**
     * Obtém a automação selecionada
     */
    private String getSelectedAutomation() {
        if (automationSpinner != null && automationSpinner.getSelectedItem() != null) {
            return automationSpinner.getSelectedItem().toString();
        }
        return null;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        String filePath = null;
        if (data != null && data.getData() != null) {
            filePath = data.getData().toString();
        }
        
        presenter.handleFileResult(requestCode, resultCode, filePath);
    }
} 