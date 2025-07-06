package com.thiagofernendorech.toneforge.ui.fragments.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.thiagofernendorech.toneforge.LatencyManager;
import com.thiagofernendorech.toneforge.R;
import com.thiagofernendorech.toneforge.ToneForgeMidiManager;
import com.thiagofernendorech.toneforge.ui.base.BaseFragment;

import java.util.Map;

/**
 * Fragment refatorado para configurações do app
 * Implementa a arquitetura MVP para melhor separação de responsabilidades
 */
public class SettingsFragmentRefactored extends BaseFragment<SettingsPresenter> 
        implements SettingsContract.View {

    @Override
    protected SettingsPresenter createPresenter() {
        return new SettingsPresenter(requireContext());
    }

    // Componentes de UI
    private Switch switchDarkTheme, switchVibration, switchAutoSave, switchAudioBackground, switchMidiEnabled;
    private RadioGroup radioGroupLatency;
    private RadioButton radioLowLatency, radioBalanced, radioStability;
    private TextView textLatencyInfo, textLatencyDetails, textMidiStatus, textMidiDevice;
    private Button buttonLatencyInfo, buttonMidiScan, buttonMidiMappings, buttonAbout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupListeners();
        
        // Carregar configurações
        presenter.loadSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof com.thiagofernendorech.toneforge.MainActivity) {
            ((com.thiagofernendorech.toneforge.MainActivity) getActivity()).updateHeaderTitle("Configurações");
        }
        if (presenter != null) {
            presenter.onResume();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initializeViews(View view) {
        // Switches principais
        switchDarkTheme = view.findViewById(R.id.switchDarkTheme);
        switchVibration = view.findViewById(R.id.switchVibration);
        switchAutoSave = view.findViewById(R.id.switchAutoSave);
        switchAudioBackground = view.findViewById(R.id.switchAudioBackground);
        switchMidiEnabled = view.findViewById(R.id.switchMidiEnabled);

        // Componentes de latência
        radioGroupLatency = view.findViewById(R.id.radioGroupLatency);
        radioLowLatency = view.findViewById(R.id.radioLowLatency);
        radioBalanced = view.findViewById(R.id.radioBalanced);
        radioStability = view.findViewById(R.id.radioStability);
        textLatencyInfo = view.findViewById(R.id.textLatencyInfo);
        textLatencyDetails = view.findViewById(R.id.textLatencyDetails);
        buttonLatencyInfo = view.findViewById(R.id.buttonLatencyInfo);

        // Componentes MIDI
        textMidiStatus = view.findViewById(R.id.textMidiStatus);
        textMidiDevice = view.findViewById(R.id.textMidiDevice);
        buttonMidiScan = view.findViewById(R.id.buttonMidiScan);
        buttonMidiMappings = view.findViewById(R.id.buttonMidiMappings);

        // Botão sobre
        buttonAbout = view.findViewById(R.id.settingsAboutButton);
    }

    /**
     * Configura os listeners dos componentes
     */
    private void setupListeners() {
        // Switches principais
        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setDarkTheme(isChecked);
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setVibration(isChecked);
        });

        switchAutoSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setAutoSave(isChecked);
        });

        switchAudioBackground.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setAudioBackground(isChecked);
        });

        // Latência
        radioGroupLatency.setOnCheckedChangeListener((group, checkedId) -> {
            if (presenter == null) return;
            
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
            
            presenter.setLatencyMode(selectedMode);
        });

        buttonLatencyInfo.setOnClickListener(v -> {
            if (presenter != null) presenter.onLatencyInfoRequested();
        });

        // MIDI
        switchMidiEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setMidiLearnMode(isChecked);
        });

        buttonMidiScan.setOnClickListener(v -> {
            if (presenter != null) presenter.scanMidiDevices();
        });

        buttonMidiMappings.setOnClickListener(v -> {
            if (presenter != null) presenter.onMidiMappingsRequested();
        });

        // Sobre
        buttonAbout.setOnClickListener(v -> {
            if (presenter != null) presenter.onAboutRequested();
        });
    }

    // ===== IMPLEMENTAÇÃO DA INTERFACE VIEW =====

    @Override
    public void setDarkThemeEnabled(boolean enabled) {
        if (switchDarkTheme != null) {
            switchDarkTheme.setChecked(enabled);
        }
    }

    @Override
    public void setVibrationEnabled(boolean enabled) {
        if (switchVibration != null) {
            switchVibration.setChecked(enabled);
        }
    }

    @Override
    public void setAutoSaveEnabled(boolean enabled) {
        if (switchAutoSave != null) {
            switchAutoSave.setChecked(enabled);
        }
    }

    @Override
    public void setAudioBackgroundEnabled(boolean enabled) {
        if (switchAudioBackground != null) {
            switchAudioBackground.setChecked(enabled);
        }
    }

    @Override
    public void setLatencyMode(int mode) {
        if (radioGroupLatency == null) return;

        int radioButtonId;
        switch (mode) {
            case LatencyManager.MODE_LOW_LATENCY:
                radioButtonId = R.id.radioLowLatency;
                break;
            case LatencyManager.MODE_BALANCED:
                radioButtonId = R.id.radioBalanced;
                break;
            case LatencyManager.MODE_STABILITY:
                radioButtonId = R.id.radioStability;
                break;
            default:
                return;
        }

        radioGroupLatency.check(radioButtonId);
    }

    @Override
    public void updateLatencyInfo(String modeName, String details) {
        if (textLatencyInfo != null) {
            textLatencyInfo.setText(modeName);
        }
        if (textLatencyDetails != null) {
            textLatencyDetails.setText(details);
        }
    }

    @Override
    public void setMidiEnabled(boolean enabled) {
        if (switchMidiEnabled != null) {
            switchMidiEnabled.setChecked(enabled);
        }
    }

    @Override
    public void updateMidiStatus(String status, String deviceInfo) {
        if (textMidiStatus != null) {
            textMidiStatus.setText(status);
        }
        if (textMidiDevice != null) {
            textMidiDevice.setText(deviceInfo);
        }
    }

    @Override
    public void showLatencyInfoDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Informações sobre Latência")
                .setMessage(
                    "• Baixa Latência: Menor latência, pode causar dropouts\n" +
                    "• Equilibrado: Latência e estabilidade balanceadas\n" +
                    "• Estabilidade: Maior estabilidade, latência mais alta\n\n" +
                    "A mudança de modo reiniciará o pipeline de áudio."
                )
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void showMidiMappingsDialog() {
        if (getContext() == null) return;

        ToneForgeMidiManager midiManager = ToneForgeMidiManager.getInstance(requireContext());
        Map<String, ToneForgeMidiManager.MidiMapping> mappings = midiManager.getAllMappings();

        if (mappings.isEmpty()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Mapeamentos MIDI")
                    .setMessage("Nenhum mapeamento configurado.\n\nPara criar mapeamentos:\n1. Ative o modo de aprendizado\n2. Ajuste um parâmetro no app\n3. Envie um comando MIDI")
                    .setPositiveButton("OK", null)
                    .setNegativeButton("Limpar Todos", (dialog, which) -> {
                        if (presenter != null) presenter.clearMidiMappings();
                    })
                    .show();
        } else {
            StringBuilder message = new StringBuilder("Mapeamentos ativos:\n\n");
            for (Map.Entry<String, ToneForgeMidiManager.MidiMapping> entry : mappings.entrySet()) {
                ToneForgeMidiManager.MidiMapping mapping = entry.getValue();
                message.append("• ").append(entry.getKey())
                        .append(" → CC ").append(mapping.controller)
                        .append(" (Canal ").append(mapping.channel).append(")\n");
            }

            new AlertDialog.Builder(requireContext())
                    .setTitle("Mapeamentos MIDI")
                    .setMessage(message.toString())
                    .setPositiveButton("OK", null)
                    .setNegativeButton("Limpar Todos", (dialog, which) -> {
                        if (presenter != null) presenter.clearMidiMappings();
                    })
                    .show();
        }
    }

    @Override
    public void showClearMappingsConfirmDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Limpar Mapeamentos")
                .setMessage("Tem certeza que deseja remover todos os mapeamentos MIDI?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    ToneForgeMidiManager midiManager = ToneForgeMidiManager.getInstance(requireContext());
                    // Limpar todos os mapeamentos
                    Map<String, ToneForgeMidiManager.MidiMapping> mappings = midiManager.getAllMappings();
                    for (String parameter : mappings.keySet()) {
                        midiManager.removeMapping(parameter);
                    }
                    showMessage("Todos os mapeamentos foram removidos");
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void showAboutInfo(String version, String developer) {
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Sobre o ToneForge")
                .setMessage(version + "\n" + developer + "\n\n" +
                        "Um aplicativo completo para músicos que oferece:\n" +
                        "• Afinador preciso\n" +
                        "• Metrônomo configurável\n" +
                        "• Gravação de áudio\n" +
                        "• Efeitos de áudio\n" +
                        "• Looper multi-faixa\n" +
                        "• Suporte MIDI")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void showMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean isViewActive() {
        return isAdded() && getActivity() != null && !isDetached();
    }

    @Override
    public void showLoading() {
        // Implementação básica - pode ser expandida com ProgressBar
        if (getContext() != null) {
            Toast.makeText(getContext(), "Carregando...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void hideLoading() {
        // Implementação básica - pode ser expandida com ProgressBar
    }
} 