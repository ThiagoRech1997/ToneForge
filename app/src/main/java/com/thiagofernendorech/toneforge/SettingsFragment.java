package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Switch;
import android.widget.Button;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.graphics.Typeface;
import android.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import java.util.List;
import java.util.Map;
import android.media.midi.MidiManager;
import android.media.midi.MidiDeviceInfo;

public class SettingsFragment extends Fragment {
    private static final String PREFS_NAME = "toneforge_prefs";
    private static final String KEY_AUDIO_BACKGROUND = "audio_background_enabled";
    private Switch switchAudioBackground;
    private LatencyManager latencyManager;
    private RadioGroup radioGroupLatency;
    private RadioButton radioLowLatency, radioBalanced, radioStability;
    private TextView textLatencyInfo, textLatencyDetails;
    private Button buttonLatencyInfo;
    private Switch switchOversampling;
    private Spinner spinnerOversamplingFactor;
    
    // Componentes MIDI
    private Switch switchMidiEnabled;
    private TextView textMidiStatus, textMidiDevice;
    private Button buttonMidiScan, buttonMidiMappings;
    private ToneForgeMidiManager midiManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Switch switchDarkTheme = view.findViewById(R.id.switchDarkTheme);
        Switch switchVibration = view.findViewById(R.id.switchVibration);
        Switch switchAutoSave = view.findViewById(R.id.switchAutoSave);
        Button aboutButton = view.findViewById(R.id.settingsAboutButton);

        // Inicializar componentes de oversampling
        switchOversampling = view.findViewById(R.id.switchOversampling);
        spinnerOversamplingFactor = view.findViewById(R.id.spinnerOversamplingFactor);
        
        // Inicializar componentes de áudio em background
        switchAudioBackground = view.findViewById(R.id.switchAudioBackground);
        
        // Inicializar componentes de latência
        latencyManager = LatencyManager.getInstance(requireContext());
        radioGroupLatency = view.findViewById(R.id.radioGroupLatency);
        radioLowLatency = view.findViewById(R.id.radioLowLatency);
        radioBalanced = view.findViewById(R.id.radioBalanced);
        radioStability = view.findViewById(R.id.radioStability);
        textLatencyInfo = view.findViewById(R.id.textLatencyInfo);
        textLatencyDetails = view.findViewById(R.id.textLatencyDetails);
        buttonLatencyInfo = view.findViewById(R.id.buttonLatencyInfo);
        
        // Inicializar componentes MIDI
        midiManager = ToneForgeMidiManager.getInstance(requireContext());
        switchMidiEnabled = view.findViewById(R.id.switchMidiEnabled);
        textMidiStatus = view.findViewById(R.id.textMidiStatus);
        textMidiDevice = view.findViewById(R.id.textMidiDevice);
        buttonMidiScan = view.findViewById(R.id.buttonMidiScan);
        buttonMidiMappings = view.findViewById(R.id.buttonMidiMappings);
        
        // Carregar preferência salva
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean audioBackgroundEnabled = prefs.getBoolean(KEY_AUDIO_BACKGROUND, false);
        // Atualizar switch conforme preferência OU estado real do serviço
        boolean isServiceRunning = AudioBackgroundService.isServiceRunning(requireContext());
        switchAudioBackground.setChecked(audioBackgroundEnabled || isServiceRunning);

        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Ativar/desativar tema escuro
            Toast.makeText(getContext(), isChecked ? "Tema escuro ativado" : "Tema claro ativado", Toast.LENGTH_SHORT).show();
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Ativar/desativar vibração
            Toast.makeText(getContext(), isChecked ? "Vibração ativada" : "Vibração desativada", Toast.LENGTH_SHORT).show();
        });

        switchAutoSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Ativar/desativar salvamento automático
            Toast.makeText(getContext(), isChecked ? "Salvar gravações automaticamente" : "Salvar manualmente", Toast.LENGTH_SHORT).show();
        });

        switchAudioBackground.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_AUDIO_BACKGROUND, isChecked);
            editor.apply();
            if (isChecked) {
                // Iniciar serviço de áudio em background
                AudioBackgroundService.startService(requireContext());
                Toast.makeText(getContext(), getString(R.string.audio_background_enabled), Toast.LENGTH_SHORT).show();
            } else {
                // Parar serviço de áudio em background
                AudioBackgroundService.stopService(requireContext());
                Toast.makeText(getContext(), getString(R.string.audio_background_disabled), Toast.LENGTH_SHORT).show();
            }
        });

        aboutButton.setOnClickListener(v -> {
            // Exibir versão e informações do app
            Toast.makeText(getContext(), "ToneForge v1.0\nDesenvolvido por Thiago F. Rech", Toast.LENGTH_LONG).show();
        });

        setupLatencyControls();
        setupOversamplingControls();
        setupMidiControls();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Atualizar switch conforme estado real do serviço ao voltar para a tela
        if (switchAudioBackground != null) {
            boolean isServiceRunning = AudioBackgroundService.isServiceRunning(requireContext());
            switchAudioBackground.setChecked(isServiceRunning);
        }
    }

    private void setupLatencyControls() {
        // Configurar estado inicial
        updateLatencyUI();
        
        // Configurar listener para mudanças de modo
        radioGroupLatency.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedMode;
            if (checkedId == R.id.radioLowLatency) {
                selectedMode = LatencyManager.MODE_LOW_LATENCY;
            } else if (checkedId == R.id.radioBalanced) {
                selectedMode = LatencyManager.MODE_BALANCED;
            } else if (checkedId == R.id.radioStability) {
                selectedMode = LatencyManager.MODE_STABILITY;
            } else {
                return;
            }
            
            // Aplicar novo modo
            latencyManager.setLatencyMode(selectedMode);
            updateLatencyUI();
            
            // Mostrar feedback
            String modeName = latencyManager.getModeName(selectedMode);
            Toast.makeText(requireContext(), 
                getString(R.string.latency_mode_changed, modeName), 
                Toast.LENGTH_SHORT).show();
        });
        
        // Configurar listener para mudanças de latência
        latencyManager.setLatencyChangeListener(new LatencyManager.LatencyChangeListener() {
            @Override
            public void onLatencyModeChanged(int newMode) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateLatencyUI();
                        Toast.makeText(requireContext(), 
                            getString(R.string.latency_pipeline_restarting), 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            }
            
            @Override
            public void onBufferSizeChanged(int newBufferSize) {
                // Atualizar UI se necessário
            }
            
            @Override
            public void onSampleRateChanged(int newSampleRate) {
                // Atualizar UI se necessário
            }
        });
        
        // Configurar botão de informações
        buttonLatencyInfo.setOnClickListener(v -> showLatencyInfoDialog());
        
        // Adicionar tooltips (comentado até implementar TooltipManager)
        // TooltipManager.addTooltip(radioGroupLatency, getString(R.string.tooltip_latency_mode));
        // TooltipManager.addTooltip(buttonLatencyInfo, getString(R.string.tooltip_latency_info));
    }
    
    private void updateLatencyUI() {
        int currentMode = latencyManager.getCurrentMode();
        
        // Atualizar seleção do RadioGroup
        switch (currentMode) {
            case LatencyManager.MODE_LOW_LATENCY:
                radioLowLatency.setChecked(true);
                break;
            case LatencyManager.MODE_BALANCED:
                radioBalanced.setChecked(true);
                break;
            case LatencyManager.MODE_STABILITY:
                radioStability.setChecked(true);
                break;
        }
        
        // Atualizar informações de latência
        String modeName = latencyManager.getModeName(currentMode);
        textLatencyInfo.setText(getString(R.string.latency_current_mode, modeName));
        
        float estimatedLatency = latencyManager.getEstimatedLatency();
        textLatencyDetails.setText(getString(R.string.latency_estimated, estimatedLatency));
    }
    
    private void showLatencyInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.latency_settings));
        
        // Criar layout para o diálogo
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        // Informações detalhadas do modo atual
        TextView currentInfo = new TextView(requireContext());
        currentInfo.setText(latencyManager.getCurrentModeInfo());
        currentInfo.setTextColor(getResources().getColor(R.color.white));
        currentInfo.setTextSize(14);
        currentInfo.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(currentInfo);
        
        // Separador
        View separator1 = new View(requireContext());
        separator1.setBackgroundColor(getResources().getColor(R.color.dark_gray));
        separator1.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2));
        separator1.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2));
        layout.addView(separator1);
        
        // Recomendações de uso
        TextView recommendationsTitle = new TextView(requireContext());
        recommendationsTitle.setText(getString(R.string.latency_recommendations));
        recommendationsTitle.setTextColor(getResources().getColor(R.color.green));
        recommendationsTitle.setTextSize(16);
        recommendationsTitle.setTypeface(null, Typeface.BOLD);
        recommendationsTitle.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT));
        recommendationsTitle.setPadding(0, 16, 0, 8);
        layout.addView(recommendationsTitle);
        
        TextView recommendations = new TextView(requireContext());
        recommendations.setText(latencyManager.getUsageRecommendations());
        recommendations.setTextColor(getResources().getColor(R.color.white));
        recommendations.setTextSize(14);
        recommendations.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(recommendations);
        
        // Separador
        View separator2 = new View(requireContext());
        separator2.setBackgroundColor(getResources().getColor(R.color.dark_gray));
        separator2.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2));
        layout.addView(separator2);
        
        // Informações do dispositivo
        TextView deviceTitle = new TextView(requireContext());
        deviceTitle.setText(getString(R.string.latency_device_info));
        deviceTitle.setTextColor(getResources().getColor(R.color.green));
        deviceTitle.setTextSize(16);
        deviceTitle.setTypeface(null, Typeface.BOLD);
        deviceTitle.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT));
        deviceTitle.setPadding(0, 16, 0, 8);
        layout.addView(deviceTitle);
        
        TextView deviceInfo = new TextView(requireContext());
        StringBuilder deviceInfoText = new StringBuilder();
        deviceInfoText.append(getString(R.string.latency_minimum_possible, 
            latencyManager.getMinimumLatency())).append("\n");
        
        if (latencyManager.isLowLatencySupported()) {
            deviceInfoText.append(getString(R.string.latency_low_supported));
        } else {
            deviceInfoText.append(getString(R.string.latency_low_not_supported));
        }
        
        deviceInfo.setText(deviceInfoText.toString());
        deviceInfo.setTextColor(getResources().getColor(R.color.white));
        deviceInfo.setTextSize(14);
        deviceInfo.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(deviceInfo);
        
        builder.setView(layout);
        builder.setPositiveButton("OK", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setupOversamplingControls() {
        // Implemente a lógica para configurar os controles de oversampling
    }

    private void setupMidiControls() {
        // Configurar estado inicial
        updateMidiUI();
        
        // Configurar switch MIDI
        switchMidiEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            midiManager.setMidiEnabled(isChecked);
            updateMidiUI();
            
            String message = isChecked ? getString(R.string.midi_enabled) : getString(R.string.midi_disabled);
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
        
        // Configurar botão de scan
        buttonMidiScan.setOnClickListener(v -> {
            midiManager.scanMidiDevices();
            updateMidiUI();
            Toast.makeText(requireContext(), "Dispositivos MIDI escaneados", Toast.LENGTH_SHORT).show();
        });
        
        // Configurar botão de mapeamentos
        buttonMidiMappings.setOnClickListener(v -> showMidiMappingsDialog());
        
        // Configurar listener MIDI
        midiManager.setLearnListener(new ToneForgeMidiManager.MidiLearnListener() {
            @Override
            public void onLearnModeChanged(int mode) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateMidiUI();
                        String statusText;
                        switch (mode) {
                            case ToneForgeMidiManager.LEARN_MODE_WAITING:
                                statusText = getString(R.string.midi_learn_waiting);
                                break;
                            case ToneForgeMidiManager.LEARN_MODE_ACTIVE:
                                statusText = getString(R.string.midi_learn_active);
                                break;
                            default:
                                statusText = getString(R.string.midi_learn_off);
                                break;
                        }
                        textMidiStatus.setText(statusText);
                    });
                }
            }
            
            @Override
            public void onMidiMessageReceived(int type, int channel, int data1, int data2) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String message = getString(R.string.midi_message_received, type, data1, data2);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    });
                }
            }
            
            @Override
            public void onMappingCreated(String parameter, ToneForgeMidiManager.MidiMapping mapping) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String message = getString(R.string.midi_mapping_created, parameter);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        updateMidiUI();
                    });
                }
            }
            
            @Override
            public void onMappingRemoved(String parameter) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String message = getString(R.string.midi_mapping_removed, parameter);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        updateMidiUI();
                    });
                }
            }
        });
    }
    
    private void updateMidiUI() {
        // Atualizar switch
        switchMidiEnabled.setChecked(midiManager.isMidiEnabled());
        
        // Atualizar status
        if (midiManager.isMidiEnabled()) {
            textMidiStatus.setText(getString(R.string.midi_enabled));
        } else {
            textMidiStatus.setText(getString(R.string.midi_disabled));
        }
        
        // Atualizar informações do dispositivo
        List<MidiDeviceInfo> devices = midiManager.getAvailableDevices();
        if (devices.isEmpty()) {
            textMidiDevice.setText(getString(R.string.midi_no_devices));
        } else {
            String deviceName = devices.get(0).getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
            textMidiDevice.setText(getString(R.string.midi_device_connected) + ": " + deviceName);
        }
    }
    
    private void showMidiMappingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.midi_mappings));
        
        // Criar layout para o diálogo
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        Map<String, ToneForgeMidiManager.MidiMapping> mappings = midiManager.getAllMappings();
        
        if (mappings.isEmpty()) {
            TextView noMappings = new TextView(requireContext());
            noMappings.setText(getString(R.string.midi_no_mappings));
            noMappings.setTextColor(getResources().getColor(R.color.white));
            noMappings.setTextSize(16);
            noMappings.setGravity(android.view.Gravity.CENTER);
            noMappings.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.addView(noMappings);
        } else {
            // Listar mapeamentos
            for (Map.Entry<String, ToneForgeMidiManager.MidiMapping> entry : mappings.entrySet()) {
                LinearLayout mappingRow = new LinearLayout(requireContext());
                mappingRow.setOrientation(LinearLayout.HORIZONTAL);
                mappingRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT));
                mappingRow.setPadding(0, 8, 0, 8);
                
                TextView parameterName = new TextView(requireContext());
                parameterName.setText(entry.getKey());
                parameterName.setTextColor(getResources().getColor(R.color.white));
                parameterName.setTextSize(14);
                parameterName.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                mappingRow.addView(parameterName);
                
                ToneForgeMidiManager.MidiMapping mapping = entry.getValue();
                TextView mappingInfo = new TextView(requireContext());
                mappingInfo.setText("CC" + mapping.controller + " Ch" + (mapping.channel + 1));
                mappingInfo.setTextColor(getResources().getColor(R.color.green));
                mappingInfo.setTextSize(12);
                mappingInfo.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT));
                mappingRow.addView(mappingInfo);
                
                Button removeButton = new Button(requireContext());
                removeButton.setText("×");
                removeButton.setTextColor(getResources().getColor(R.color.white));
                removeButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.dark_gray)));
                removeButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT));
                removeButton.setOnClickListener(v -> {
                    midiManager.removeMapping(entry.getKey());
                    ((AlertDialog) v.getTag()).dismiss();
                    showMidiMappingsDialog(); // Recarregar diálogo
                });
                removeButton.setTag(builder.create());
                mappingRow.addView(removeButton);
                
                layout.addView(mappingRow);
            }
        }
        
        // Botões de ação
        LinearLayout buttonLayout = new LinearLayout(requireContext());
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(android.view.Gravity.CENTER);
        buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonLayout.setPadding(0, 16, 0, 0);
        
        Button clearAllButton = new Button(requireContext());
        clearAllButton.setText(getString(R.string.midi_learn_clear_all));
        clearAllButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            getResources().getColor(R.color.dark_gray)));
        clearAllButton.setTextColor(getResources().getColor(R.color.white));
        clearAllButton.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT));
        clearAllButton.setOnClickListener(v -> showClearMappingsConfirmDialog());
        buttonLayout.addView(clearAllButton);
        
        layout.addView(buttonLayout);
        
        builder.setView(layout);
        builder.setPositiveButton("OK", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void showClearMappingsConfirmDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.midi_learn_clear_confirm_title))
            .setMessage(getString(R.string.midi_learn_clear_confirm_message))
            .setPositiveButton(getString(R.string.midi_learn_clear_confirm_yes), (dialog, which) -> {
                // Limpar todos os mapeamentos
                Map<String, ToneForgeMidiManager.MidiMapping> mappings = midiManager.getAllMappings();
                for (String parameter : mappings.keySet()) {
                    midiManager.removeMapping(parameter);
                }
                Toast.makeText(requireContext(), "Todos os mapeamentos MIDI foram removidos", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(getString(R.string.midi_learn_clear_confirm_no), null)
            .show();
    }
} 