package com.thiagofernendorech.toneforge.ui.fragments.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;

import com.thiagofernendorech.toneforge.AudioBackgroundService;
import com.thiagofernendorech.toneforge.LatencyManager;
import com.thiagofernendorech.toneforge.ToneForgeMidiManager;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;

import java.util.List;
import java.util.Map;

/**
 * Presenter para o SettingsFragment
 * Gerencia a lógica de negócio das configurações do app
 */
public class SettingsPresenter extends BasePresenter<SettingsContract.View> implements SettingsContract.Presenter {

    private static final String PREFS_NAME = "toneforge_prefs";
    private static final String KEY_DARK_THEME = "dark_theme_enabled";
    private static final String KEY_VIBRATION = "vibration_enabled";
    private static final String KEY_AUTO_SAVE = "auto_save_enabled";
    private static final String KEY_AUDIO_BACKGROUND = "audio_background_enabled";
    private static final String KEY_MIDI_ENABLED = "midi_enabled";

    private Context context;
    private SharedPreferences prefs;
    private LatencyManager latencyManager;
    private ToneForgeMidiManager midiManager;

    public SettingsPresenter(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.latencyManager = LatencyManager.getInstance(context);
        this.midiManager = ToneForgeMidiManager.getInstance(context);
    }

    @Override
    public void attachView(SettingsContract.View view) {
        super.attachView(view);
        setupLatencyListener();
        setupMidiListener();
    }

    @Override
    public void detachView() {
        if (latencyManager != null) {
            latencyManager.setLatencyChangeListener(null);
        }
        if (midiManager != null) {
            midiManager.setLearnListener(null);
        }
        super.detachView();
    }

    @Override
    public void loadSettings() {
        if (!isViewAttached()) return;

        SettingsContract.View view = getView();
        
        // Carregar configurações salvas
        boolean darkTheme = prefs.getBoolean(KEY_DARK_THEME, true);
        boolean vibration = prefs.getBoolean(KEY_VIBRATION, false);
        boolean autoSave = prefs.getBoolean(KEY_AUTO_SAVE, true);
        boolean audioBackground = prefs.getBoolean(KEY_AUDIO_BACKGROUND, false);
        boolean midiEnabled = prefs.getBoolean(KEY_MIDI_ENABLED, false);

        // Atualizar UI
        view.setDarkThemeEnabled(darkTheme);
        view.setVibrationEnabled(vibration);
        view.setAutoSaveEnabled(autoSave);
        view.setAudioBackgroundEnabled(audioBackground || AudioBackgroundService.isServiceRunning(context));
        view.setMidiEnabled(midiEnabled);

        // Carregar configurações de latência
        updateLatencyUI();
        
        // Carregar configurações MIDI
        updateMidiUI();
    }

    @Override
    public void setDarkTheme(boolean enabled) {
        if (!isViewAttached()) return;

        SettingsContract.View view = getView();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_DARK_THEME, enabled);
        editor.apply();

        view.setDarkThemeEnabled(enabled);
        view.showMessage(enabled ? "Tema escuro ativado" : "Tema claro ativado");
    }

    @Override
    public void setVibration(boolean enabled) {
        if (!isViewAttached()) return;

        SettingsContract.View view = getView();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_VIBRATION, enabled);
        editor.apply();

        view.setVibrationEnabled(enabled);
        view.showMessage(enabled ? "Vibração ativada" : "Vibração desativada");
    }

    @Override
    public void setAutoSave(boolean enabled) {
        if (!isViewAttached()) return;

        SettingsContract.View view = getView();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_AUTO_SAVE, enabled);
        editor.apply();

        view.setAutoSaveEnabled(enabled);
        view.showMessage(enabled ? "Salvar gravações automaticamente" : "Salvar manualmente");
    }

    @Override
    public void setAudioBackground(boolean enabled) {
        if (!isViewAttached()) return;

        SettingsContract.View view = getView();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_AUDIO_BACKGROUND, enabled);
        editor.apply();

        if (enabled) {
            AudioBackgroundService.startService(context);
            view.showMessage("Serviço de áudio em background ativado");
        } else {
            AudioBackgroundService.stopService(context);
            view.showMessage("Serviço de áudio em background desativado");
        }

        view.setAudioBackgroundEnabled(enabled);
    }

    @Override
    public void setLatencyMode(int mode) {
        if (!isViewAttached()) return;

        SettingsContract.View view = getView();
        latencyManager.setLatencyMode(mode);
        updateLatencyUI();

        String modeName = latencyManager.getModeName(mode);
        view.showMessage("Modo de latência alterado para: " + modeName);
    }

    @Override
    public void setMidiLearnMode(boolean enabled) {
        if (!isViewAttached()) return;

        SettingsContract.View view = getView();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_MIDI_ENABLED, enabled);
        editor.apply();

        if (enabled) {
            midiManager.startLearnMode("general");
        } else {
            midiManager.stopLearnMode();
        }

        view.setMidiEnabled(enabled);
        updateMidiUI();
    }

    @Override
    public void scanMidiDevices() {
        if (!isViewAttached()) return;

        SettingsContract.View view = getView();
        MidiManager midiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        if (midiManager != null) {
            MidiDeviceInfo[] devices = midiManager.getDevices();
            view.showMessage("Encontrados " + devices.length + " dispositivos MIDI");
            updateMidiUI();
        }
    }

    @Override
    public void onLatencyInfoRequested() {
        if (!isViewAttached()) return;
        SettingsContract.View view = getView();
        view.showLatencyInfoDialog();
    }

    @Override
    public void onMidiMappingsRequested() {
        if (!isViewAttached()) return;
        SettingsContract.View view = getView();
        view.showMidiMappingsDialog();
    }

    @Override
    public void clearMidiMappings() {
        if (!isViewAttached()) return;
        SettingsContract.View view = getView();
        view.showClearMappingsConfirmDialog();
    }

    @Override
    public void onAboutRequested() {
        if (!isViewAttached()) return;
        SettingsContract.View view = getView();
        view.showAboutInfo("ToneForge v1.0", "Desenvolvido por Thiago F. Rech");
    }

    @Override
    public void onResume() {
        if (!isViewAttached()) return;

        SettingsContract.View view = getView();
        // Atualizar estado do serviço de áudio em background
        boolean isServiceRunning = AudioBackgroundService.isServiceRunning(context);
        view.setAudioBackgroundEnabled(isServiceRunning);

        // Atualizar configurações de latência
        updateLatencyUI();
        
        // Atualizar configurações MIDI
        updateMidiUI();
    }

    /**
     * Configura o listener de mudanças de latência
     */
    private void setupLatencyListener() {
        if (latencyManager != null) {
            latencyManager.setLatencyChangeListener(new LatencyManager.LatencyChangeListener() {
                @Override
                public void onLatencyModeChanged(int newMode) {
                    if (isViewAttached()) {
                        SettingsContract.View view = getView();
                        view.showMessage("Pipeline de áudio reiniciando...");
                        updateLatencyUI();
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
        }
    }

    /**
     * Configura o listener de eventos MIDI
     */
    private void setupMidiListener() {
        if (midiManager != null) {
            midiManager.setLearnListener(new ToneForgeMidiManager.MidiLearnListener() {
                @Override
                public void onLearnModeChanged(int mode) {
                    if (isViewAttached()) {
                        SettingsContract.View view = getView();
                        boolean enabled = mode == ToneForgeMidiManager.LEARN_MODE_ACTIVE;
                        view.setMidiEnabled(enabled);
                        view.showMessage(enabled ? "Modo de aprendizado MIDI ativado" : "Modo de aprendizado MIDI desativado");
                    }
                }

                @Override
                public void onMidiMessageReceived(int type, int channel, int data1, int data2) {
                    // Feedback visual pode ser implementado aqui
                }

                @Override
                public void onMappingCreated(String parameter, ToneForgeMidiManager.MidiMapping mapping) {
                    if (isViewAttached()) {
                        SettingsContract.View view = getView();
                        view.showMessage("Mapeamento criado: " + parameter);
                    }
                }

                @Override
                public void onMappingRemoved(String parameter) {
                    if (isViewAttached()) {
                        SettingsContract.View view = getView();
                        view.showMessage("Mapeamento removido: " + parameter);
                    }
                }
            });
        }
    }

    /**
     * Atualiza a UI com as informações de latência atuais
     */
    private void updateLatencyUI() {
        if (!isViewAttached()) return;

        SettingsContract.View view = getView();
        int currentMode = latencyManager.getCurrentMode();
        String modeName = latencyManager.getModeName(currentMode);
        String details = "Latência Estimada: " + latencyManager.getEstimatedLatency() + " ms";

        view.setLatencyMode(currentMode);
        view.updateLatencyInfo("Modo Atual: " + modeName, details);
    }

    /**
     * Atualiza a UI com as informações MIDI atuais
     */
    private void updateMidiUI() {
        if (!isViewAttached()) return;

        SettingsContract.View view = getView();
        boolean isEnabled = midiManager.getLearnMode() == ToneForgeMidiManager.LEARN_MODE_ACTIVE;
        String status = isEnabled ? "MIDI Habilitado" : "MIDI Desabilitado";
        
        MidiManager systemMidiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        String deviceInfo = "Nenhum dispositivo";
        if (systemMidiManager != null) {
            MidiDeviceInfo[] devices = systemMidiManager.getDevices();
            if (devices.length > 0) {
                deviceInfo = devices.length + " dispositivo(s) encontrado(s)";
            }
        }

        view.setMidiEnabled(isEnabled);
        view.updateMidiStatus(status, deviceInfo);
    }
} 