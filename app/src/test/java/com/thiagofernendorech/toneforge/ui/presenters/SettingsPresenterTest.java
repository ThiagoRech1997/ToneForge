package com.thiagofernendorech.toneforge.ui.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;

import com.thiagofernendorech.toneforge.AudioBackgroundService;
import com.thiagofernendorech.toneforge.LatencyManager;
import com.thiagofernendorech.toneforge.LogManager;
import com.thiagofernendorech.toneforge.ToneForgeMidiManager;
import com.thiagofernendorech.toneforge.ui.fragments.settings.SettingsContract;
import com.thiagofernendorech.toneforge.ui.fragments.settings.SettingsPresenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para SettingsPresenter
 * Testa a logica de gerenciamento de configuracoes do app
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class SettingsPresenterTest {

    private static final String PREFS_NAME = "toneforge_prefs";

    @Mock
    private SettingsContract.View mockView;

    @Mock
    private LatencyManager mockLatencyManager;

    @Mock
    private ToneForgeMidiManager mockMidiManager;

    @Mock
    private LogManager mockLogManager;

    private SettingsPresenter presenter;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Usar contexto real do Robolectric
        context = RuntimeEnvironment.getApplication();

        // Configurar view como ativa
        when(mockView.isViewActive()).thenReturn(true);

        // Criar presenter com mocks injetados via static mocking
        try (MockedStatic<LatencyManager> latencyMock = mockStatic(LatencyManager.class);
             MockedStatic<ToneForgeMidiManager> midiMock = mockStatic(ToneForgeMidiManager.class)) {

            latencyMock.when(() -> LatencyManager.getInstance(any(Context.class))).thenReturn(mockLatencyManager);
            midiMock.when(() -> ToneForgeMidiManager.getInstance(any(Context.class))).thenReturn(mockMidiManager);

            presenter = new SettingsPresenter(context);
            presenter.attachView(mockView);
        }
    }

    // === TESTES DE CICLO DE VIDA ===

    @Test
    public void attachView_shouldSetupListeners() {
        // Assert - verifica que listeners foram configurados
        verify(mockLatencyManager).setLatencyChangeListener(any());
        verify(mockMidiManager).setLearnListener(any());
    }

    @Test
    public void detachView_shouldClearListeners() {
        // Act
        presenter.detachView();

        // Assert
        verify(mockLatencyManager).setLatencyChangeListener(null);
        verify(mockMidiManager).setLearnListener(null);
    }

    // === TESTES DE DARK THEME ===

    @Test
    public void setDarkTheme_enabled_shouldUpdateView() {
        // Act
        presenter.setDarkTheme(true);

        // Assert
        verify(mockView).setDarkThemeEnabled(true);
        verify(mockView).showMessage("Tema escuro ativado");
    }

    @Test
    public void setDarkTheme_disabled_shouldUpdateView() {
        // Act
        presenter.setDarkTheme(false);

        // Assert
        verify(mockView).setDarkThemeEnabled(false);
        verify(mockView).showMessage("Tema claro ativado");
    }

    @Test
    public void setDarkTheme_withInactiveView_shouldNotUpdate() {
        // Arrange
        when(mockView.isViewActive()).thenReturn(false);

        // Act
        presenter.setDarkTheme(true);

        // Assert
        verify(mockView, never()).setDarkThemeEnabled(anyBoolean());
    }

    // === TESTES DE VIBRACAO ===

    @Test
    public void setVibration_enabled_shouldUpdateView() {
        // Act
        presenter.setVibration(true);

        // Assert
        verify(mockView).setVibrationEnabled(true);
        verify(mockView).showMessage("Vibração ativada");
    }

    @Test
    public void setVibration_disabled_shouldUpdateView() {
        // Act
        presenter.setVibration(false);

        // Assert
        verify(mockView).setVibrationEnabled(false);
        verify(mockView).showMessage("Vibração desativada");
    }

    // === TESTES DE AUTO SAVE ===

    @Test
    public void setAutoSave_enabled_shouldUpdateView() {
        // Act
        presenter.setAutoSave(true);

        // Assert
        verify(mockView).setAutoSaveEnabled(true);
        verify(mockView).showMessage("Salvar gravações automaticamente");
    }

    @Test
    public void setAutoSave_disabled_shouldUpdateView() {
        // Act
        presenter.setAutoSave(false);

        // Assert
        verify(mockView).setAutoSaveEnabled(false);
        verify(mockView).showMessage("Salvar manualmente");
    }

    // === TESTES DE AUDIO BACKGROUND ===

    @Test
    public void setAudioBackground_enabled_shouldShowMessage() {
        try (MockedStatic<AudioBackgroundService> serviceMock = mockStatic(AudioBackgroundService.class)) {
            // Act
            presenter.setAudioBackground(true);

            // Assert
            serviceMock.verify(() -> AudioBackgroundService.startService(any(Context.class)));
            verify(mockView).showMessage("Serviço de áudio em background ativado");
        }
    }

    @Test
    public void setAudioBackground_disabled_shouldShowMessage() {
        try (MockedStatic<AudioBackgroundService> serviceMock = mockStatic(AudioBackgroundService.class)) {
            // Act
            presenter.setAudioBackground(false);

            // Assert
            serviceMock.verify(() -> AudioBackgroundService.stopService(any(Context.class)));
            verify(mockView).showMessage("Serviço de áudio em background desativado");
        }
    }

    // === TESTES DE LATENCIA ===

    @Test
    public void setLatencyMode_shouldUpdateAndShowMessage() {
        // Arrange
        when(mockLatencyManager.getModeName(1)).thenReturn("Baixa Latência");
        when(mockLatencyManager.getCurrentMode()).thenReturn(1);
        when(mockLatencyManager.getEstimatedLatency()).thenReturn(5.0f);

        // Act
        presenter.setLatencyMode(1);

        // Assert
        verify(mockLatencyManager).setLatencyMode(1);
        verify(mockView).showMessage("Modo de latência alterado para: Baixa Latência");
    }

    @Test
    public void onLatencyInfoRequested_shouldShowDialog() {
        // Act
        presenter.onLatencyInfoRequested();

        // Assert
        verify(mockView).showLatencyInfoDialog();
    }

    // === TESTES DE MIDI ===

    @Test
    public void setMidiLearnMode_enabled_shouldStartLearnMode() {
        // Arrange
        when(mockMidiManager.getLearnMode()).thenReturn(ToneForgeMidiManager.LEARN_MODE_ACTIVE);

        // Act
        presenter.setMidiLearnMode(true);

        // Assert
        verify(mockMidiManager).startLearnMode("general");
    }

    @Test
    public void setMidiLearnMode_disabled_shouldStopLearnMode() {
        // Arrange
        when(mockMidiManager.getLearnMode()).thenReturn(ToneForgeMidiManager.LEARN_MODE_OFF);

        // Act
        presenter.setMidiLearnMode(false);

        // Assert
        verify(mockMidiManager).stopLearnMode();
    }

    @Test
    public void onMidiMappingsRequested_shouldShowDialog() {
        // Act
        presenter.onMidiMappingsRequested();

        // Assert
        verify(mockView).showMidiMappingsDialog();
    }

    @Test
    public void clearMidiMappings_shouldShowConfirmDialog() {
        // Act
        presenter.clearMidiMappings();

        // Assert
        verify(mockView).showClearMappingsConfirmDialog();
    }

    // === TESTES DE ABOUT ===

    @Test
    public void onAboutRequested_shouldShowAboutInfo() {
        // Act
        presenter.onAboutRequested();

        // Assert
        verify(mockView).showAboutInfo("ToneForge v1.0", "Desenvolvido por Thiago F. Rech");
    }

    // === TESTES DE RESUME ===

    @Test
    public void onResume_shouldUpdateServiceStatus() {
        // Arrange
        when(mockLatencyManager.getCurrentMode()).thenReturn(0);
        when(mockLatencyManager.getModeName(0)).thenReturn("Normal");
        when(mockLatencyManager.getEstimatedLatency()).thenReturn(10.0f);
        when(mockMidiManager.getLearnMode()).thenReturn(ToneForgeMidiManager.LEARN_MODE_OFF);

        try (MockedStatic<AudioBackgroundService> serviceMock = mockStatic(AudioBackgroundService.class)) {
            serviceMock.when(() -> AudioBackgroundService.isServiceRunning(any(Context.class))).thenReturn(true);

            // Act
            presenter.onResume();

            // Assert
            verify(mockView).setAudioBackgroundEnabled(true);
        }
    }

    @Test
    public void onResume_withInactiveView_shouldNotUpdate() {
        // Arrange
        when(mockView.isViewActive()).thenReturn(false);

        // Act
        presenter.onResume();

        // Assert
        verify(mockView, never()).setAudioBackgroundEnabled(anyBoolean());
    }

    // === TESTES DE LOGGING ===

    @Test
    public void setVerboseLogging_enabled_shouldShowMessage() {
        try (MockedStatic<LogManager> logMock = mockStatic(LogManager.class)) {
            logMock.when(() -> LogManager.getInstance(any(Context.class))).thenReturn(mockLogManager);

            // Act
            presenter.setVerboseLogging(true);

            // Assert
            verify(mockLogManager).setVerboseLogging(true);
            verify(mockView).setVerboseLoggingEnabled(true);
            verify(mockView).showMessage("Logging verboso ativado");
        }
    }

    @Test
    public void setVerboseLogging_disabled_shouldShowMessage() {
        try (MockedStatic<LogManager> logMock = mockStatic(LogManager.class)) {
            logMock.when(() -> LogManager.getInstance(any(Context.class))).thenReturn(mockLogManager);

            // Act
            presenter.setVerboseLogging(false);

            // Assert
            verify(mockLogManager).setVerboseLogging(false);
            verify(mockView).setVerboseLoggingEnabled(false);
            verify(mockView).showMessage("Logging verboso desativado");
        }
    }

    @Test
    public void setDebugLogging_enabled_shouldSetDebugLevel() {
        try (MockedStatic<LogManager> logMock = mockStatic(LogManager.class)) {
            logMock.when(() -> LogManager.getInstance(any(Context.class))).thenReturn(mockLogManager);

            // Act
            presenter.setDebugLogging(true);

            // Assert
            verify(mockLogManager).setLogLevel(LogManager.LEVEL_DEBUG);
            verify(mockView).setDebugLoggingEnabled(true);
            verify(mockView).showMessage("Logging de debug ativado");
        }
    }

    @Test
    public void setDebugLogging_disabled_shouldSetInfoLevel() {
        try (MockedStatic<LogManager> logMock = mockStatic(LogManager.class)) {
            logMock.when(() -> LogManager.getInstance(any(Context.class))).thenReturn(mockLogManager);

            // Act
            presenter.setDebugLogging(false);

            // Assert
            verify(mockLogManager).setLogLevel(LogManager.LEVEL_INFO);
            verify(mockView).setDebugLoggingEnabled(false);
            verify(mockView).showMessage("Logging de debug desativado");
        }
    }

    @Test
    public void setLogLevel_shouldShowCorrectName() {
        try (MockedStatic<LogManager> logMock = mockStatic(LogManager.class)) {
            logMock.when(() -> LogManager.getInstance(any(Context.class))).thenReturn(mockLogManager);

            // Act
            presenter.setLogLevel(LogManager.LEVEL_WARN);

            // Assert
            verify(mockLogManager).setLogLevel(LogManager.LEVEL_WARN);
            verify(mockView).setLogLevel(LogManager.LEVEL_WARN);
            verify(mockView).showMessage("Nível de log alterado para: Aviso");
        }
    }

    @Test
    public void setLogLevel_error_shouldShowCorrectName() {
        try (MockedStatic<LogManager> logMock = mockStatic(LogManager.class)) {
            logMock.when(() -> LogManager.getInstance(any(Context.class))).thenReturn(mockLogManager);

            // Act
            presenter.setLogLevel(LogManager.LEVEL_ERROR);

            // Assert
            verify(mockView).showMessage("Nível de log alterado para: Erro");
        }
    }

    @Test
    public void setLogLevel_info_shouldShowCorrectName() {
        try (MockedStatic<LogManager> logMock = mockStatic(LogManager.class)) {
            logMock.when(() -> LogManager.getInstance(any(Context.class))).thenReturn(mockLogManager);

            // Act
            presenter.setLogLevel(LogManager.LEVEL_INFO);

            // Assert
            verify(mockView).showMessage("Nível de log alterado para: Informação");
        }
    }

    @Test
    public void setLogLevel_debug_shouldShowCorrectName() {
        try (MockedStatic<LogManager> logMock = mockStatic(LogManager.class)) {
            logMock.when(() -> LogManager.getInstance(any(Context.class))).thenReturn(mockLogManager);

            // Act
            presenter.setLogLevel(LogManager.LEVEL_DEBUG);

            // Assert
            verify(mockView).showMessage("Nível de log alterado para: Debug");
        }
    }

    @Test
    public void setLogLevel_unknown_shouldShowUnknown() {
        try (MockedStatic<LogManager> logMock = mockStatic(LogManager.class)) {
            logMock.when(() -> LogManager.getInstance(any(Context.class))).thenReturn(mockLogManager);

            // Act
            presenter.setLogLevel(999);

            // Assert
            verify(mockView).showMessage("Nível de log alterado para: Desconhecido");
        }
    }

    // === TESTES DE VIEW INATIVA PARA OUTROS METODOS ===

    @Test
    public void onAboutRequested_withInactiveView_shouldNotShow() {
        // Arrange
        when(mockView.isViewActive()).thenReturn(false);

        // Act
        presenter.onAboutRequested();

        // Assert
        verify(mockView, never()).showAboutInfo(anyString(), anyString());
    }

    // === TESTES DE LATENCY LISTENER ===

    @Test
    public void latencyListener_onModeChanged_shouldUpdateUI() {
        // Arrange
        ArgumentCaptor<LatencyManager.LatencyChangeListener> listenerCaptor =
            ArgumentCaptor.forClass(LatencyManager.LatencyChangeListener.class);
        verify(mockLatencyManager).setLatencyChangeListener(listenerCaptor.capture());
        LatencyManager.LatencyChangeListener listener = listenerCaptor.getValue();

        when(mockLatencyManager.getCurrentMode()).thenReturn(2);
        when(mockLatencyManager.getModeName(2)).thenReturn("Ultra Baixa");
        when(mockLatencyManager.getEstimatedLatency()).thenReturn(3.0f);

        // Act
        listener.onLatencyModeChanged(2);

        // Assert
        verify(mockView).showMessage("Pipeline de áudio reiniciando...");
    }

    // === TESTES DE MIDI LISTENER ===

    @Test
    public void midiListener_onLearnModeChanged_shouldUpdateView() {
        // Arrange
        ArgumentCaptor<ToneForgeMidiManager.MidiLearnListener> listenerCaptor =
            ArgumentCaptor.forClass(ToneForgeMidiManager.MidiLearnListener.class);
        verify(mockMidiManager).setLearnListener(listenerCaptor.capture());
        ToneForgeMidiManager.MidiLearnListener listener = listenerCaptor.getValue();

        // Act
        listener.onLearnModeChanged(ToneForgeMidiManager.LEARN_MODE_ACTIVE);

        // Assert
        verify(mockView).setMidiEnabled(true);
        verify(mockView).showMessage("Modo de aprendizado MIDI ativado");
    }

    @Test
    public void midiListener_onMappingCreated_shouldShowMessage() {
        // Arrange
        ArgumentCaptor<ToneForgeMidiManager.MidiLearnListener> listenerCaptor =
            ArgumentCaptor.forClass(ToneForgeMidiManager.MidiLearnListener.class);
        verify(mockMidiManager).setLearnListener(listenerCaptor.capture());
        ToneForgeMidiManager.MidiLearnListener listener = listenerCaptor.getValue();

        ToneForgeMidiManager.MidiMapping mockMapping = mock(ToneForgeMidiManager.MidiMapping.class);

        // Act
        listener.onMappingCreated("volume", mockMapping);

        // Assert
        verify(mockView).showMessage("Mapeamento criado: volume");
    }

    @Test
    public void midiListener_onMappingRemoved_shouldShowMessage() {
        // Arrange
        ArgumentCaptor<ToneForgeMidiManager.MidiLearnListener> listenerCaptor =
            ArgumentCaptor.forClass(ToneForgeMidiManager.MidiLearnListener.class);
        verify(mockMidiManager).setLearnListener(listenerCaptor.capture());
        ToneForgeMidiManager.MidiLearnListener listener = listenerCaptor.getValue();

        // Act
        listener.onMappingRemoved("volume");

        // Assert
        verify(mockView).showMessage("Mapeamento removido: volume");
    }
}
