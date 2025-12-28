package com.thiagofernendorech.toneforge.domain.usecases;

import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para ApplyEffectParametersUseCase
 * Testa todos os cenarios de aplicacao de parametros de efeitos
 */
public class ApplyEffectParametersUseCaseTest {

    @Mock
    private AudioEngineInterface mockAudioEngine;

    private ApplyEffectParametersUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new ApplyEffectParametersUseCase(mockAudioEngine);
    }

    // === TESTES DE SUCESSO - execute() ===

    @Test
    public void execute_withValidParameters_shouldReturnSuccess() {
        // Arrange
        EffectParameters params = new EffectParameters();
        params.setGain(0.8f);

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.isPipelineRunning()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(params);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.execute(params);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Parametros aplicados com sucesso", result.getMessage());
        assertNotNull(result.getAppliedParameters());
    }

    @Test
    public void execute_withValidParameters_shouldCallApplyEffectParameters() {
        // Arrange
        EffectParameters params = new EffectParameters();

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.isPipelineRunning()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(params);

        // Act
        useCase.execute(params);

        // Assert
        verify(mockAudioEngine).applyEffectParameters(params);
    }

    @Test
    public void execute_shouldVerifyParametersApplied() {
        // Arrange
        EffectParameters params = new EffectParameters();
        params.setDistortion(0.5f);
        params.setDistortionEnabled(true);

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.isPipelineRunning()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(params);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.execute(params);

        // Assert
        verify(mockAudioEngine).getCurrentEffectParameters();
        assertEquals(0.5f, result.getAppliedParameters().getDistortion(), 0.001f);
        assertTrue(result.getAppliedParameters().isDistortionEnabled());
    }

    // === TESTES DE FALHA - PARAMETROS NULOS ===

    @Test
    public void execute_withNullParameters_shouldReturnFailure() {
        // Act
        ApplyEffectParametersUseCase.Result result = useCase.execute(null);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Parametros nao podem ser nulos", result.getMessage());
        assertNull(result.getAppliedParameters());
    }

    @Test
    public void execute_withNullParameters_shouldNotCallAudioEngine() {
        // Act
        useCase.execute(null);

        // Assert
        verifyNoInteractions(mockAudioEngine);
    }

    // === TESTES DE FALHA - BIBLIOTECA NATIVA ===

    @Test
    public void execute_withoutNativeLibrary_shouldReturnFailure() {
        // Arrange
        EffectParameters params = new EffectParameters();
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(false);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.execute(params);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Biblioteca nativa nao carregada", result.getMessage());
    }

    @Test
    public void execute_withoutNativeLibrary_shouldNotApplyParameters() {
        // Arrange
        EffectParameters params = new EffectParameters();
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(false);

        // Act
        useCase.execute(params);

        // Assert
        verify(mockAudioEngine, never()).applyEffectParameters(any());
    }

    // === TESTES DE FALHA - PIPELINE NAO ATIVO ===

    @Test
    public void execute_withPipelineNotRunning_shouldReturnFailure() {
        // Arrange
        EffectParameters params = new EffectParameters();
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.isPipelineRunning()).thenReturn(false);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.execute(params);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Pipeline de audio nao esta ativo", result.getMessage());
    }

    // === TESTES DE FALHA - ERRO AO APLICAR ===

    @Test
    public void execute_whenApplyThrowsException_shouldReturnFailure() {
        // Arrange
        EffectParameters params = new EffectParameters();
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.isPipelineRunning()).thenReturn(true);
        doThrow(new RuntimeException("Native error")).when(mockAudioEngine).applyEffectParameters(any());

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.execute(params);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Erro ao aplicar parametros"));
        assertTrue(result.getMessage().contains("Native error"));
    }

    @Test
    public void execute_whenGetCurrentReturnsNull_shouldReturnFailure() {
        // Arrange
        EffectParameters params = new EffectParameters();
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.isPipelineRunning()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(null);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.execute(params);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Erro ao verificar parametros aplicados", result.getMessage());
    }

    // === TESTES DE executeForEffect() - SUCESSO ===

    @Test
    public void executeForEffect_withGain_shouldEnableGain() {
        // Arrange
        EffectParameters currentParams = new EffectParameters();
        currentParams.setGainEnabled(false);

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(currentParams);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("gain", true);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("gain"));
        assertTrue(result.getMessage().contains("ativado"));
    }

    @Test
    public void executeForEffect_withGanho_shouldEnableGain() {
        // Arrange
        EffectParameters currentParams = new EffectParameters();

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(currentParams);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("ganho", true);

        // Assert
        assertTrue(result.isSuccess());
    }

    @Test
    public void executeForEffect_withDistortion_shouldEnableDistortion() {
        // Arrange
        EffectParameters currentParams = new EffectParameters();

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(currentParams);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("distortion", true);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getAppliedParameters().isDistortionEnabled());
    }

    @Test
    public void executeForEffect_withDelay_shouldEnableDelay() {
        // Arrange
        EffectParameters currentParams = new EffectParameters();

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(currentParams);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("delay", true);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getAppliedParameters().isDelayEnabled());
    }

    @Test
    public void executeForEffect_withReverb_shouldEnableReverb() {
        // Arrange
        EffectParameters currentParams = new EffectParameters();

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(currentParams);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("reverb", true);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getAppliedParameters().isReverbEnabled());
    }

    @Test
    public void executeForEffect_withChorus_shouldEnableChorus() {
        // Arrange
        EffectParameters currentParams = new EffectParameters();

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(currentParams);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("chorus", true);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getAppliedParameters().isChorusEnabled());
    }

    @Test
    public void executeForEffect_withFlanger_shouldEnableFlanger() {
        // Arrange
        EffectParameters currentParams = new EffectParameters();

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(currentParams);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("flanger", true);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getAppliedParameters().isFlangerEnabled());
    }

    @Test
    public void executeForEffect_withPhaser_shouldEnablePhaser() {
        // Arrange
        EffectParameters currentParams = new EffectParameters();

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(currentParams);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("phaser", true);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getAppliedParameters().isPhaserEnabled());
    }

    @Test
    public void executeForEffect_withEq_shouldEnableEq() {
        // Arrange
        EffectParameters currentParams = new EffectParameters();

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(currentParams);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("eq", true);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getAppliedParameters().isEqEnabled());
    }

    @Test
    public void executeForEffect_withCompressor_shouldEnableCompressor() {
        // Arrange
        EffectParameters currentParams = new EffectParameters();

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(currentParams);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("compressor", true);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getAppliedParameters().isCompressorEnabled());
    }

    @Test
    public void executeForEffect_disabling_shouldDisableEffect() {
        // Arrange
        EffectParameters currentParams = new EffectParameters();
        currentParams.setDistortionEnabled(true);

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(currentParams);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("distortion", false);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("desativado"));
        assertFalse(result.getAppliedParameters().isDistortionEnabled());
    }

    // === TESTES DE executeForEffect() - FALHA ===

    @Test
    public void executeForEffect_withNullName_shouldReturnFailure() {
        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect(null, true);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Nome do efeito nao pode ser vazio", result.getMessage());
    }

    @Test
    public void executeForEffect_withEmptyName_shouldReturnFailure() {
        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("", true);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Nome do efeito nao pode ser vazio", result.getMessage());
    }

    @Test
    public void executeForEffect_withUnknownEffect_shouldReturnFailure() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(new EffectParameters());

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("unknownEffect", true);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Efeito desconhecido"));
    }

    @Test
    public void executeForEffect_withoutNativeLibrary_shouldReturnFailure() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(false);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("gain", true);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Biblioteca nativa nao carregada", result.getMessage());
    }

    @Test
    public void executeForEffect_withNullCurrentParams_shouldCreateNewParams() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(null);

        // Act
        ApplyEffectParametersUseCase.Result result = useCase.executeForEffect("gain", true);

        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.getAppliedParameters());
    }

    // === TESTES DE CASE INSENSITIVITY ===

    @Test
    public void executeForEffect_shouldBeCaseInsensitive() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(new EffectParameters());

        // Act
        ApplyEffectParametersUseCase.Result resultLower = useCase.executeForEffect("delay", true);

        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(new EffectParameters());
        ApplyEffectParametersUseCase.Result resultUpper = useCase.executeForEffect("DELAY", true);

        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(new EffectParameters());
        ApplyEffectParametersUseCase.Result resultMixed = useCase.executeForEffect("DeLaY", true);

        // Assert
        assertTrue(resultLower.isSuccess());
        assertTrue(resultUpper.isSuccess());
        assertTrue(resultMixed.isSuccess());
    }

    // === TESTES DA CLASSE RESULT ===

    @Test
    public void result_success_shouldHaveCorrectValues() {
        // Arrange
        EffectParameters params = new EffectParameters();
        params.setGain(0.7f);

        // Act
        ApplyEffectParametersUseCase.Result result = ApplyEffectParametersUseCase.Result.success("Test message", params);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Test message", result.getMessage());
        assertSame(params, result.getAppliedParameters());
    }

    @Test
    public void result_failure_shouldHaveCorrectValues() {
        // Act
        ApplyEffectParametersUseCase.Result result = ApplyEffectParametersUseCase.Result.failure("Error message");

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Error message", result.getMessage());
        assertNull(result.getAppliedParameters());
    }

    // === TESTES DE ARGUMENTO CAPTOR ===

    @Test
    public void execute_shouldPassCorrectParametersToAudioEngine() {
        // Arrange
        EffectParameters params = new EffectParameters();
        params.setGain(0.9f);
        params.setDistortion(0.6f);
        params.setDistortionEnabled(true);

        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.isPipelineRunning()).thenReturn(true);
        when(mockAudioEngine.getCurrentEffectParameters()).thenReturn(params);

        ArgumentCaptor<EffectParameters> captor = ArgumentCaptor.forClass(EffectParameters.class);

        // Act
        useCase.execute(params);

        // Assert
        verify(mockAudioEngine).applyEffectParameters(captor.capture());
        EffectParameters capturedParams = captor.getValue();
        assertEquals(0.9f, capturedParams.getGain(), 0.001f);
        assertEquals(0.6f, capturedParams.getDistortion(), 0.001f);
        assertTrue(capturedParams.isDistortionEnabled());
    }
}
