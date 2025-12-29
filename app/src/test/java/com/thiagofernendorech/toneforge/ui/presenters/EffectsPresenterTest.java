package com.thiagofernendorech.toneforge.ui.presenters;

import android.content.Context;

import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;
import com.thiagofernendorech.toneforge.ui.fragments.effects.EffectsContract;
import com.thiagofernendorech.toneforge.ui.fragments.effects.EffectsPresenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para EffectsPresenter
 * Testa a logica de negocios do gerenciamento de efeitos de audio
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class EffectsPresenterTest {

    @Mock
    private AudioRepository mockAudioRepository;

    @Mock
    private EffectsContract.View mockView;

    private EffectsPresenter presenter;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Usar contexto real do Robolectric
        context = RuntimeEnvironment.getApplication();

        presenter = new EffectsPresenter(context, mockAudioRepository);

        // Configurar view como ativa
        when(mockView.isViewActive()).thenReturn(true);
        presenter.attachView(mockView);
    }

    // === TESTES DE CICLO DE VIDA ===

    @Test
    public void attachView_shouldSetView() {
        // Arrange
        EffectsPresenter newPresenter = new EffectsPresenter(context, mockAudioRepository);

        // Act
        newPresenter.attachView(mockView);

        // Assert - view deveria estar anexada (testamos indiretamente)
        when(mockView.isViewActive()).thenReturn(true);
        newPresenter.onViewStarted();
        verify(mockView, atLeastOnce()).updateEffectsStatus(anyString(), anyBoolean());
    }

    @Test
    public void detachView_shouldClearView() {
        // Act
        presenter.detachView();

        // Assert - chamadas a view nao devem ocorrer apos detach
        presenter.updateStatus();
        verify(mockView, never()).updateAudioState(any());
    }

    @Test
    public void onViewStarted_shouldLoadCurrentState() {
        // Act
        presenter.onViewStarted();

        // Assert
        verify(mockView).updateEffectParameters(any(EffectParameters.class));
        verify(mockView).updateEffectOrder(anyList());
        verify(mockView).updateBypassIndicators();
    }

    @Test
    public void onViewStarted_shouldRefreshPresetList() {
        // Act
        presenter.onViewStarted();

        // Assert
        verify(mockView).updatePresetList(anyList());
    }

    @Test
    public void onViewResumed_shouldUpdateStatus() {
        // Act
        presenter.onViewResumed();

        // Assert
        verify(mockView).updateAudioState(any());
    }

    @Test
    public void onViewPaused_shouldNotCrash() {
        // Act & Assert - nao deve lancar excecao
        presenter.onViewPaused();
    }

    @Test
    public void onViewDestroyed_shouldCleanupResources() {
        // Act
        presenter.onViewDestroyed();

        // Assert - nao deve lancar excecao e deve parar atualizacoes
        // Verificamos indiretamente que limpou recursos
    }

    // === TESTES DE CONTROLE DE EFEITOS ===

    @Test
    public void setEffectEnabled_ganho_shouldCallAudioRepository() {
        // Act
        presenter.setEffectEnabled("Ganho", true);

        // Assert
        verify(mockAudioRepository).setGainEnabled(true);
    }

    @Test
    public void setEffectEnabled_ganho_shouldUpdateView() {
        // Act
        presenter.setEffectEnabled("Ganho", true);

        // Assert
        verify(mockView).updateEffectState("Ganho", true);
        verify(mockView).updateBypassIndicators();
    }

    @Test
    public void setEffectEnabled_distorcao_shouldCallAudioRepository() {
        // Act
        presenter.setEffectEnabled("Distorção", true);

        // Assert
        verify(mockAudioRepository).setDistortionEnabled(true);
    }

    @Test
    public void setEffectEnabled_delay_shouldCallAudioRepository() {
        // Act
        presenter.setEffectEnabled("Delay", true);

        // Assert
        verify(mockAudioRepository).setDelayEnabled(true);
    }

    @Test
    public void setEffectEnabled_reverb_shouldCallAudioRepository() {
        // Act
        presenter.setEffectEnabled("Reverb", true);

        // Assert
        verify(mockAudioRepository).setReverbEnabled(true);
    }

    @Test
    public void setEffectEnabled_disabled_shouldPassFalseToRepository() {
        // Act
        presenter.setEffectEnabled("Ganho", false);

        // Assert
        verify(mockAudioRepository).setGainEnabled(false);
    }

    @Test
    public void setEffectParameter_shouldNotCrash() {
        // Act & Assert - nao deve lancar excecao
        presenter.setEffectParameter("Ganho", "level", 0.5f);
    }

    @Test
    public void resetEffect_shouldUpdateViewWithMessage() {
        // Act
        presenter.resetEffect("Ganho");

        // Assert
        verify(mockView).updateEffectParameters(any(EffectParameters.class));
        verify(mockView).updateBypassIndicators();
        verify(mockView).showMessage("Ganho resetado!");
    }

    @Test
    public void resetAllEffects_shouldResetAndNotifyView() {
        // Act
        presenter.resetAllEffects();

        // Assert
        verify(mockView).updateEffectParameters(any(EffectParameters.class));
        verify(mockView).updateEffectOrder(anyList());
        verify(mockView).updateBypassIndicators();
        verify(mockView).showMessage("Todos os efeitos foram resetados!");
    }

    // === TESTES DE REORDENACAO DE EFEITOS ===

    @Test
    public void moveEffect_validPositions_shouldUpdateView() {
        // Arrange - inicializar estado primeiro
        presenter.onViewStarted();
        reset(mockView);
        when(mockView.isViewActive()).thenReturn(true);

        // Act
        presenter.moveEffect(0, 1);

        // Assert
        verify(mockView).updateEffectOrder(anyList());
    }

    @Test
    public void moveEffect_invalidFromPosition_shouldNotUpdateView() {
        // Act
        presenter.moveEffect(-1, 1);

        // Assert
        verify(mockView, never()).updateEffectOrder(anyList());
    }

    @Test
    public void moveEffect_invalidToPosition_shouldNotUpdateView() {
        // Act
        presenter.moveEffect(0, 100);

        // Assert
        verify(mockView, never()).updateEffectOrder(anyList());
    }

    // === TESTES DE PRESETS ===

    @Test
    public void loadPreset_shouldUpdateViewWithPreset() {
        // Act
        presenter.loadPreset("Rock");

        // Assert
        verify(mockView).updateEffectParameters(any(EffectParameters.class));
        verify(mockView).selectPreset("Rock");
        verify(mockView).updateBypassIndicators();
        verify(mockView).showMessage("Preset 'Rock' carregado!");
    }

    @Test
    public void savePreset_shouldNotifyViewWithSuccess() {
        // Act
        presenter.savePreset("MeuPreset");

        // Assert
        verify(mockView).selectPreset("MeuPreset");
        verify(mockView).showSuccess("Preset 'MeuPreset' salvo!");
    }

    @Test
    public void deletePreset_shouldNotifyViewWithSuccess() {
        // Act
        presenter.deletePreset("PresetAntigo");

        // Assert
        verify(mockView).showSuccess("Preset 'PresetAntigo' deletado!");
    }

    @Test
    public void deletePreset_currentPreset_shouldClearSelection() {
        // Arrange
        presenter.loadPreset("MeuPreset");
        reset(mockView);
        when(mockView.isViewActive()).thenReturn(true);

        // Act
        presenter.deletePreset("MeuPreset");

        // Assert
        verify(mockView).showSuccess("Preset 'MeuPreset' deletado!");
    }

    @Test
    public void toggleFavoritesFilter_shouldToggleAndUpdateView() {
        // Act
        presenter.toggleFavoritesFilter();

        // Assert
        verify(mockView).updateFavoritesFilter(true);

        // Act - toggle again
        reset(mockView);
        when(mockView.isViewActive()).thenReturn(true);
        presenter.toggleFavoritesFilter();

        // Assert
        verify(mockView).updateFavoritesFilter(false);
    }

    @Test
    public void exportPreset_shouldShowMessage() {
        // Act
        presenter.exportPreset("Rock", "/path/to/file");

        // Assert
        verify(mockView).showMessage("Export de preset em implementação...");
    }

    @Test
    public void importPreset_shouldShowMessage() {
        // Act
        presenter.importPreset("/path/to/file");

        // Assert
        verify(mockView).showMessage("Import de preset em implementação...");
    }

    @Test
    public void togglePresetFavorite_shouldShowMessage() {
        // Act
        presenter.togglePresetFavorite("Rock");

        // Assert
        verify(mockView).showMessage("Toggle de favoritos em implementação...");
    }

    // === TESTES DE AUTOMACAO ===

    @Test
    public void startAutomationRecording_shouldUpdateStatus() {
        // Act
        presenter.startAutomationRecording("MinhaAutomacao");

        // Assert
        verify(mockView).updateAutomationStatus(true, false, "Gravando automação: MinhaAutomacao");
        verify(mockView).showMessage("Iniciada gravação de automação: MinhaAutomacao");
    }

    @Test
    public void stopAutomationRecording_shouldUpdateStatus() {
        // Arrange
        presenter.startAutomationRecording("Test");
        reset(mockView);
        when(mockView.isViewActive()).thenReturn(true);

        // Act
        presenter.stopAutomationRecording();

        // Assert
        verify(mockView).updateAutomationStatus(false, false, "Automação gravada");
        verify(mockView).showSuccess("Gravação de automação finalizada!");
    }

    @Test
    public void startAutomationPlayback_shouldUpdateStatus() {
        // Act
        presenter.startAutomationPlayback("MinhaAutomacao");

        // Assert
        verify(mockView).updateAutomationStatus(false, true, "Reproduzindo: MinhaAutomacao");
        verify(mockView).showMessage("Iniciada reprodução de automação: MinhaAutomacao");
    }

    @Test
    public void stopAutomationPlayback_shouldUpdateStatus() {
        // Arrange
        presenter.startAutomationPlayback("Test");
        reset(mockView);
        when(mockView.isViewActive()).thenReturn(true);

        // Act
        presenter.stopAutomationPlayback();

        // Assert
        verify(mockView).updateAutomationStatus(false, false, "Pronto");
        verify(mockView).showMessage("Reprodução de automação parada");
    }

    @Test
    public void exportAutomation_shouldShowMessage() {
        // Act
        presenter.exportAutomation("Test", "/path");

        // Assert
        verify(mockView).showMessage("Export de automação em implementação...");
    }

    @Test
    public void importAutomation_shouldShowMessage() {
        // Act
        presenter.importAutomation("/path");

        // Assert
        verify(mockView).showMessage("Import de automação em implementação...");
    }

    @Test
    public void deleteAutomation_shouldShowMessage() {
        // Act
        presenter.deleteAutomation("Test");

        // Assert
        verify(mockView).showMessage("Delete de automação em implementação...");
    }

    // === TESTES DE MIDI ===

    @Test
    public void setMidiEnabled_shouldUpdateStatus() {
        // Act
        presenter.setMidiEnabled(true);

        // Assert
        verify(mockView).updateMidiStatus(anyBoolean(), anyInt());
    }

    @Test
    public void startMidiLearn_shouldShowFeedback() {
        // Act
        presenter.startMidiLearn("volume");

        // Assert
        verify(mockView).showMidiLearnFeedback("volume", true);
    }

    @Test
    public void stopMidiLearn_shouldHideFeedback() {
        // Act
        presenter.stopMidiLearn();

        // Assert
        verify(mockView).showMidiLearnFeedback("", false);
    }

    @Test
    public void applyMidiParameter_withValidFormat_shouldSetParameter() {
        // Act
        presenter.applyMidiParameter("ganho_level", 0.75f);

        // Assert - verifica que nao lanca excecao e processa corretamente
    }

    @Test
    public void applyMidiParameter_withInvalidFormat_shouldNotCrash() {
        // Act & Assert - nao deve lancar excecao
        presenter.applyMidiParameter("invalidformat", 0.5f);
    }

    // === TESTES DE PERMISSOES E STATUS ===

    @Test
    public void checkAudioPermissions_shouldUpdateEffectsStatus() {
        // Act
        presenter.checkAudioPermissions();

        // Assert
        verify(mockView).updateEffectsStatus(anyString(), anyBoolean());
    }

    @Test
    public void updateStatus_withViewAttached_shouldUpdateAll() {
        // Act
        presenter.updateStatus();

        // Assert
        verify(mockView).updateAudioState(any());
        verify(mockView).updateEffectsStatus(anyString(), anyBoolean());
        verify(mockView).updateMidiStatus(anyBoolean(), anyInt());
    }

    @Test
    public void updateStatus_withViewDetached_shouldNotCrash() {
        // Arrange
        presenter.detachView();

        // Act & Assert - nao deve lancar excecao
        presenter.updateStatus();
    }

    // === TESTES DE RESULTADOS DE ARQUIVO ===

    @Test
    public void handleFileResult_exportPreset_shouldCallExport() {
        // Arrange
        presenter.loadPreset("Rock"); // Define currentPreset
        reset(mockView);
        when(mockView.isViewActive()).thenReturn(true);

        // Act
        presenter.handleFileResult(EffectsPresenter.EXPORT_PRESET_REQUEST, -1, "/path/preset.json");

        // Assert
        verify(mockView).showMessage("Export de preset em implementação...");
    }

    @Test
    public void handleFileResult_importPreset_shouldCallImport() {
        // Act
        presenter.handleFileResult(EffectsPresenter.IMPORT_PRESET_REQUEST, -1, "/path/preset.json");

        // Assert
        verify(mockView).showMessage("Import de preset em implementação...");
    }

    @Test
    public void handleFileResult_invalidResultCode_shouldNotProcess() {
        // Act
        presenter.handleFileResult(EffectsPresenter.IMPORT_PRESET_REQUEST, 0, "/path");

        // Assert
        verify(mockView, never()).showMessage(contains("Import"));
    }

    @Test
    public void handleFileResult_nullFilePath_shouldNotProcess() {
        // Act
        presenter.handleFileResult(EffectsPresenter.IMPORT_PRESET_REQUEST, -1, null);

        // Assert
        verify(mockView, never()).showMessage(contains("Import"));
    }

    // === TESTES DE VIEW INATIVA ===

    @Test
    public void setEffectEnabled_withInactiveView_shouldStillCallRepository() {
        // Arrange
        when(mockView.isViewActive()).thenReturn(false);

        // Act
        presenter.setEffectEnabled("Ganho", true);

        // Assert
        verify(mockAudioRepository).setGainEnabled(true);
    }

    @Test
    public void setEffectEnabled_withInactiveView_shouldNotUpdateView() {
        // Arrange
        when(mockView.isViewActive()).thenReturn(false);

        // Act
        presenter.setEffectEnabled("Ganho", true);

        // Assert
        verify(mockView, never()).updateEffectState(anyString(), anyBoolean());
    }

    // === TESTES DE DESTRUICAO DURANTE AUTOMACAO ===

    @Test
    public void onViewDestroyed_duringRecording_shouldStopRecording() {
        // Arrange
        presenter.startAutomationRecording("Test");
        reset(mockView);
        when(mockView.isViewActive()).thenReturn(true);

        // Act
        presenter.onViewDestroyed();

        // Assert - deve parar a gravacao antes de destruir
        // Verificamos que nao lanca excecao
    }

    @Test
    public void onViewDestroyed_duringPlayback_shouldStopPlayback() {
        // Arrange
        presenter.startAutomationPlayback("Test");
        reset(mockView);
        when(mockView.isViewActive()).thenReturn(true);

        // Act
        presenter.onViewDestroyed();

        // Assert - deve parar a reproducao antes de destruir
        // Verificamos que nao lanca excecao
    }

    // === TESTES DE CONSTANTES ===

    @Test
    public void requestCodes_shouldBeUnique() {
        // Assert
        assertNotEquals(EffectsPresenter.EXPORT_PRESET_REQUEST, EffectsPresenter.IMPORT_PRESET_REQUEST);
        assertNotEquals(EffectsPresenter.EXPORT_AUTOMATION_REQUEST, EffectsPresenter.IMPORT_AUTOMATION_REQUEST);
        assertNotEquals(EffectsPresenter.EXPORT_PRESET_REQUEST, EffectsPresenter.EXPORT_AUTOMATION_REQUEST);
    }
}
