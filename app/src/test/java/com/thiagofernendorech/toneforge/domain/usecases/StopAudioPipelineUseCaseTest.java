package com.thiagofernendorech.toneforge.domain.usecases;

import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para StopAudioPipelineUseCase
 * Testa todos os cenarios de parada do pipeline de audio
 */
public class StopAudioPipelineUseCaseTest {

    @Mock
    private AudioEngineInterface mockAudioEngine;

    private StopAudioPipelineUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new StopAudioPipelineUseCase(mockAudioEngine);
    }

    // === TESTES DE SUCESSO ===

    @Test
    public void execute_whenPipelineRunning_shouldReturnSuccess() {
        // Arrange
        when(mockAudioEngine.isPipelineRunning())
            .thenReturn(true)   // Primeira verificacao
            .thenReturn(false); // Segunda verificacao apos parar

        // Act
        StopAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Pipeline parado com sucesso", result.getMessage());
    }

    @Test
    public void execute_whenPipelineRunning_shouldCallStopPipeline() {
        // Arrange
        when(mockAudioEngine.isPipelineRunning())
            .thenReturn(true)
            .thenReturn(false);

        // Act
        useCase.execute();

        // Assert
        verify(mockAudioEngine).stopPipeline();
    }

    @Test
    public void execute_whenPipelineRunning_shouldVerifyPipelineStopped() {
        // Arrange
        when(mockAudioEngine.isPipelineRunning())
            .thenReturn(true)
            .thenReturn(false);

        // Act
        useCase.execute();

        // Assert
        verify(mockAudioEngine, times(2)).isPipelineRunning();
    }

    // === TESTES QUANDO PIPELINE JA ESTAVA PARADO ===

    @Test
    public void execute_whenPipelineAlreadyStopped_shouldReturnSuccess() {
        // Arrange
        when(mockAudioEngine.isPipelineRunning()).thenReturn(false);

        // Act
        StopAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Pipeline ja estava parado", result.getMessage());
    }

    @Test
    public void execute_whenPipelineAlreadyStopped_shouldNotCallStopPipeline() {
        // Arrange
        when(mockAudioEngine.isPipelineRunning()).thenReturn(false);

        // Act
        useCase.execute();

        // Assert
        verify(mockAudioEngine, never()).stopPipeline();
    }

    @Test
    public void execute_whenPipelineAlreadyStopped_shouldCheckRunningOnce() {
        // Arrange
        when(mockAudioEngine.isPipelineRunning()).thenReturn(false);

        // Act
        useCase.execute();

        // Assert
        verify(mockAudioEngine, times(1)).isPipelineRunning();
    }

    // === TESTES DE FALHA ===

    @Test
    public void execute_whenStopFails_shouldReturnFailure() {
        // Arrange
        when(mockAudioEngine.isPipelineRunning())
            .thenReturn(true)   // Primeira verificacao
            .thenReturn(true);  // Segunda verificacao - ainda rodando

        // Act
        StopAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Pipeline nao parou corretamente", result.getMessage());
    }

    @Test
    public void execute_whenStopThrowsException_shouldReturnFailure() {
        // Arrange
        when(mockAudioEngine.isPipelineRunning()).thenReturn(true);
        doThrow(new RuntimeException("Audio device busy")).when(mockAudioEngine).stopPipeline();

        // Act
        StopAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Erro ao parar pipeline"));
        assertTrue(result.getMessage().contains("Audio device busy"));
    }

    @Test
    public void execute_whenStopThrowsNullPointerException_shouldReturnFailure() {
        // Arrange
        when(mockAudioEngine.isPipelineRunning()).thenReturn(true);
        doThrow(new NullPointerException("null reference")).when(mockAudioEngine).stopPipeline();

        // Act
        StopAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Erro ao parar pipeline"));
    }

    // === TESTES DA CLASSE RESULT ===

    @Test
    public void result_success_shouldHaveCorrectValues() {
        // Act
        StopAudioPipelineUseCase.Result result = StopAudioPipelineUseCase.Result.success("Test message");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Test message", result.getMessage());
    }

    @Test
    public void result_failure_shouldHaveCorrectValues() {
        // Act
        StopAudioPipelineUseCase.Result result = StopAudioPipelineUseCase.Result.failure("Error message");

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Error message", result.getMessage());
    }

    // === TESTES DE ORDEM DE CHAMADAS ===

    @Test
    public void execute_shouldCheckRunningBeforeStopping() {
        // Arrange
        when(mockAudioEngine.isPipelineRunning())
            .thenReturn(true)
            .thenReturn(false);

        // Act
        useCase.execute();

        // Assert
        var inOrder = inOrder(mockAudioEngine);
        inOrder.verify(mockAudioEngine).isPipelineRunning();
        inOrder.verify(mockAudioEngine).stopPipeline();
        inOrder.verify(mockAudioEngine).isPipelineRunning();
    }

    // === TESTES DE MULTIPLAS EXECUCOES ===

    @Test
    public void execute_calledTwice_whenPipelineStopsFirstTime_shouldNotStopSecondTime() {
        // Arrange - Primeira execucao: pipeline rodando -> para
        // Segunda execucao: pipeline ja parado
        when(mockAudioEngine.isPipelineRunning())
            .thenReturn(true)   // Primeira verificacao
            .thenReturn(false)  // Verificacao apos parar
            .thenReturn(false); // Segunda execucao

        // Act
        StopAudioPipelineUseCase.Result result1 = useCase.execute();
        StopAudioPipelineUseCase.Result result2 = useCase.execute();

        // Assert
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        verify(mockAudioEngine, times(1)).stopPipeline();
    }

    @Test
    public void execute_calledMultipleTimes_shouldBeIdempotent() {
        // Arrange
        when(mockAudioEngine.isPipelineRunning()).thenReturn(false);

        // Act
        StopAudioPipelineUseCase.Result result1 = useCase.execute();
        StopAudioPipelineUseCase.Result result2 = useCase.execute();
        StopAudioPipelineUseCase.Result result3 = useCase.execute();

        // Assert - todas devem ser sucesso
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        assertTrue(result3.isSuccess());
        verify(mockAudioEngine, never()).stopPipeline();
    }

    // === TESTES DE CENARIOS ESPECIFICOS ===

    @Test
    public void execute_whenSecondCheckThrowsException_shouldReturnFailure() {
        // Arrange
        when(mockAudioEngine.isPipelineRunning())
            .thenReturn(true)
            .thenThrow(new RuntimeException("State check failed"));

        // Act
        StopAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Erro ao parar pipeline"));
    }
}
