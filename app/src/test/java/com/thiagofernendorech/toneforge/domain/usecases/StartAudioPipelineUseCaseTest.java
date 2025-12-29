package com.thiagofernendorech.toneforge.domain.usecases;

import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;
import com.thiagofernendorech.toneforge.domain.interfaces.PermissionInterface;
import com.thiagofernendorech.toneforge.domain.models.AudioState;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para StartAudioPipelineUseCase
 * Testa todos os cenarios de inicializacao do pipeline de audio
 */
public class StartAudioPipelineUseCaseTest {

    @Mock
    private AudioEngineInterface mockAudioEngine;

    @Mock
    private PermissionInterface mockPermissionManager;

    private StartAudioPipelineUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new StartAudioPipelineUseCase(mockAudioEngine, mockPermissionManager);
    }

    // === TESTES DE SUCESSO ===

    @Test
    public void execute_withAllConditionsMet_shouldReturnSuccess() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline()).thenReturn(true);

        AudioState expectedState = new AudioState();
        expectedState.setPipelineRunning(true);
        when(mockAudioEngine.getCurrentState()).thenReturn(expectedState);

        // Act
        StartAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Pipeline iniciado com sucesso", result.getMessage());
        assertNotNull(result.getAudioState());
        assertTrue(result.getAudioState().isPipelineRunning());
    }

    @Test
    public void execute_withAllConditionsMet_shouldCallPermissionCheck() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline()).thenReturn(true);
        when(mockAudioEngine.getCurrentState()).thenReturn(new AudioState());

        // Act
        useCase.execute();

        // Assert
        verify(mockPermissionManager).hasAudioPermission();
    }

    @Test
    public void execute_withAllConditionsMet_shouldCallNativeLibraryCheck() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline()).thenReturn(true);
        when(mockAudioEngine.getCurrentState()).thenReturn(new AudioState());

        // Act
        useCase.execute();

        // Assert
        verify(mockAudioEngine).isNativeLibraryLoaded();
    }

    @Test
    public void execute_withAllConditionsMet_shouldCallStartPipeline() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline()).thenReturn(true);
        when(mockAudioEngine.getCurrentState()).thenReturn(new AudioState());

        // Act
        useCase.execute();

        // Assert
        verify(mockAudioEngine).startPipeline();
    }

    @Test
    public void execute_withAllConditionsMet_shouldGetCurrentState() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline()).thenReturn(true);
        when(mockAudioEngine.getCurrentState()).thenReturn(new AudioState());

        // Act
        useCase.execute();

        // Assert
        verify(mockAudioEngine).getCurrentState();
    }

    // === TESTES DE FALHA - PERMISSAO ===

    @Test
    public void execute_withoutAudioPermission_shouldReturnFailure() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(false);

        // Act
        StartAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Permissão de áudio necessária", result.getMessage());
        assertNull(result.getAudioState());
    }

    @Test
    public void execute_withoutAudioPermission_shouldNotCallAudioEngine() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(false);

        // Act
        useCase.execute();

        // Assert
        verify(mockAudioEngine, never()).isNativeLibraryLoaded();
        verify(mockAudioEngine, never()).startPipeline();
        verify(mockAudioEngine, never()).getCurrentState();
    }

    // === TESTES DE FALHA - BIBLIOTECA NATIVA ===

    @Test
    public void execute_withoutNativeLibrary_shouldReturnFailure() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(false);

        // Act
        StartAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Biblioteca nativa não carregada", result.getMessage());
        assertNull(result.getAudioState());
    }

    @Test
    public void execute_withoutNativeLibrary_shouldNotStartPipeline() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(false);

        // Act
        useCase.execute();

        // Assert
        verify(mockAudioEngine, never()).startPipeline();
        verify(mockAudioEngine, never()).getCurrentState();
    }

    // === TESTES DE FALHA - PIPELINE ===

    @Test
    public void execute_whenPipelineStartFails_shouldReturnFailure() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline()).thenReturn(false);

        // Act
        StartAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Falha ao iniciar pipeline", result.getMessage());
        assertNull(result.getAudioState());
    }

    @Test
    public void execute_whenPipelineStartFails_shouldNotGetState() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline()).thenReturn(false);

        // Act
        useCase.execute();

        // Assert
        verify(mockAudioEngine, never()).getCurrentState();
    }

    // === TESTES DE EXCECAO ===

    @Test
    public void execute_whenPipelineThrowsException_shouldReturnFailureWithMessage() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline()).thenThrow(new RuntimeException("Audio device unavailable"));

        // Act
        StartAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Erro ao iniciar pipeline"));
        assertTrue(result.getMessage().contains("Audio device unavailable"));
        assertNull(result.getAudioState());
    }

    @Test
    public void execute_whenPipelineThrowsNullPointerException_shouldReturnFailure() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline()).thenThrow(new NullPointerException("null context"));

        // Act
        StartAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Erro ao iniciar pipeline"));
        assertNull(result.getAudioState());
    }

    // === TESTES DA CLASSE RESULT ===

    @Test
    public void result_success_shouldHaveCorrectValues() {
        // Arrange
        AudioState state = new AudioState();
        state.setPipelineRunning(true);

        // Act
        StartAudioPipelineUseCase.Result result = StartAudioPipelineUseCase.Result.success("Success message", state);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Success message", result.getMessage());
        assertSame(state, result.getAudioState());
    }

    @Test
    public void result_failure_shouldHaveCorrectValues() {
        // Act
        StartAudioPipelineUseCase.Result result = StartAudioPipelineUseCase.Result.failure("Error message");

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Error message", result.getMessage());
        assertNull(result.getAudioState());
    }

    @Test
    public void result_successWithNullState_shouldBeAllowed() {
        // Act
        StartAudioPipelineUseCase.Result result = StartAudioPipelineUseCase.Result.success("Success", null);

        // Assert
        assertTrue(result.isSuccess());
        assertNull(result.getAudioState());
    }

    // === TESTES DE ORDEM DE CHAMADAS ===

    @Test
    public void execute_shouldCheckPermissionFirst() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline()).thenReturn(true);
        when(mockAudioEngine.getCurrentState()).thenReturn(new AudioState());

        // Act
        useCase.execute();

        // Assert - verify order
        var inOrder = inOrder(mockPermissionManager, mockAudioEngine);
        inOrder.verify(mockPermissionManager).hasAudioPermission();
        inOrder.verify(mockAudioEngine).isNativeLibraryLoaded();
        inOrder.verify(mockAudioEngine).startPipeline();
        inOrder.verify(mockAudioEngine).getCurrentState();
    }

    // === TESTES DE ESTADO DO AUDIOSTATE ===

    @Test
    public void execute_shouldReturnStateWithCorrectLatencyMode() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline()).thenReturn(true);

        AudioState expectedState = new AudioState();
        expectedState.setPipelineRunning(true);
        expectedState.setCurrentLatencyMode(2);
        when(mockAudioEngine.getCurrentState()).thenReturn(expectedState);

        // Act
        StartAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertEquals(2, result.getAudioState().getCurrentLatencyMode());
    }

    @Test
    public void execute_shouldReturnStateWithOversamplingSettings() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline()).thenReturn(true);

        AudioState expectedState = new AudioState();
        expectedState.setPipelineRunning(true);
        expectedState.setOversamplingEnabled(true);
        expectedState.setOversamplingFactor(2);
        when(mockAudioEngine.getCurrentState()).thenReturn(expectedState);

        // Act
        StartAudioPipelineUseCase.Result result = useCase.execute();

        // Assert
        assertTrue(result.getAudioState().isOversamplingEnabled());
        assertEquals(2, result.getAudioState().getOversamplingFactor());
    }

    // === TESTES DE MULTIPLAS EXECUCOES ===

    @Test
    public void execute_calledTwice_shouldCallPipelineTwice() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline()).thenReturn(true);
        when(mockAudioEngine.getCurrentState()).thenReturn(new AudioState());

        // Act
        useCase.execute();
        useCase.execute();

        // Assert
        verify(mockAudioEngine, times(2)).startPipeline();
    }

    @Test
    public void execute_firstSucceedsThenFails_shouldReturnCorrectResults() {
        // Arrange
        when(mockPermissionManager.hasAudioPermission()).thenReturn(true);
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.startPipeline())
            .thenReturn(true)
            .thenReturn(false);
        when(mockAudioEngine.getCurrentState()).thenReturn(new AudioState());

        // Act
        StartAudioPipelineUseCase.Result result1 = useCase.execute();
        StartAudioPipelineUseCase.Result result2 = useCase.execute();

        // Assert
        assertTrue(result1.isSuccess());
        assertFalse(result2.isSuccess());
    }
}
