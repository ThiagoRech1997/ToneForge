package com.thiagofernendorech.toneforge.domain.usecases;

import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para TunerUseCase
 * Testa operacoes do afinador e calculo de notas musicais
 */
public class TunerUseCaseTest {

    @Mock
    private AudioEngineInterface mockAudioEngine;

    private TunerUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new TunerUseCase(mockAudioEngine);
    }

    // === TESTES DE startTuner() ===

    @Test
    public void startTuner_withNativeLibraryLoaded_shouldReturnSuccess() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);

        // Act
        TunerUseCase.Result result = useCase.startTuner();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Afinador iniciado", result.getMessage());
    }

    @Test
    public void startTuner_shouldCallAudioEngineStartTuner() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);

        // Act
        useCase.startTuner();

        // Assert
        verify(mockAudioEngine).startTuner();
    }

    @Test
    public void startTuner_withoutNativeLibrary_shouldReturnFailure() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(false);

        // Act
        TunerUseCase.Result result = useCase.startTuner();

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Biblioteca nativa nao carregada", result.getMessage());
    }

    @Test
    public void startTuner_withoutNativeLibrary_shouldNotCallStartTuner() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(false);

        // Act
        useCase.startTuner();

        // Assert
        verify(mockAudioEngine, never()).startTuner();
    }

    @Test
    public void startTuner_whenThrowsException_shouldReturnFailure() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        doThrow(new RuntimeException("Device error")).when(mockAudioEngine).startTuner();

        // Act
        TunerUseCase.Result result = useCase.startTuner();

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Erro ao iniciar afinador"));
    }

    // === TESTES DE stopTuner() ===

    @Test
    public void stopTuner_shouldReturnSuccess() {
        // Act
        TunerUseCase.Result result = useCase.stopTuner();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Afinador parado", result.getMessage());
    }

    @Test
    public void stopTuner_shouldCallAudioEngineStopTuner() {
        // Act
        useCase.stopTuner();

        // Assert
        verify(mockAudioEngine).stopTuner();
    }

    @Test
    public void stopTuner_whenThrowsException_shouldReturnFailure() {
        // Arrange
        doThrow(new RuntimeException("Stop error")).when(mockAudioEngine).stopTuner();

        // Act
        TunerUseCase.Result result = useCase.stopTuner();

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Erro ao parar afinador"));
    }

    // === TESTES DE getReading() - NOTAS BASICAS ===

    @Test
    public void getReading_withA4_shouldReturnA4() {
        // Arrange - A4 = 440Hz
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(440.0f);

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertTrue(reading.isValid());
        assertTrue(reading.hasSignal());
        assertEquals("A", reading.getNoteName());
        assertEquals(4, reading.getOctave());
        assertEquals("A4", reading.getFullNoteName());
        assertEquals(0, reading.getCents(), 5); // Permitir pequena margem
    }

    @Test
    public void getReading_withE2_shouldReturnE2() {
        // Arrange - E2 = 82.41Hz
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(82.41f);

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertTrue(reading.isValid());
        assertTrue(reading.hasSignal());
        assertEquals("E", reading.getNoteName());
        assertEquals(2, reading.getOctave());
    }

    @Test
    public void getReading_withC4_shouldReturnC4() {
        // Arrange - C4 (middle C) = 261.63Hz
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(261.63f);

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertTrue(reading.isValid());
        assertEquals("C", reading.getNoteName());
        assertEquals(4, reading.getOctave());
    }

    @Test
    public void getReading_withGuitarStandardTuning_shouldReturnCorrectNotes() {
        // Guitar standard tuning frequencies (aproximadas)
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);

        // E2 = 82.41 Hz (corda 6)
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(82.41f);
        assertEquals("E", useCase.getReading().getNoteName());

        // A2 = 110 Hz (corda 5)
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(110.0f);
        assertEquals("A", useCase.getReading().getNoteName());

        // D3 = 146.83 Hz (corda 4)
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(146.83f);
        assertEquals("D", useCase.getReading().getNoteName());

        // G3 = 196 Hz (corda 3)
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(196.0f);
        assertEquals("G", useCase.getReading().getNoteName());

        // B3 = 246.94 Hz (corda 2)
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(246.94f);
        assertEquals("B", useCase.getReading().getNoteName());

        // E4 = 329.63 Hz (corda 1)
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(329.63f);
        assertEquals("E", useCase.getReading().getNoteName());
    }

    // === TESTES DE getReading() - CENTS ===

    @Test
    public void getReading_withSlightlySharpNote_shouldReturnPositiveCents() {
        // Arrange - A4 ligeiramente alta (445Hz ao inves de 440Hz)
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(445.0f);

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertTrue(reading.getCents() > 0);
        assertFalse(reading.isInTune());
    }

    @Test
    public void getReading_withSlightlyFlatNote_shouldReturnNegativeCents() {
        // Arrange - A4 ligeiramente baixa (435Hz ao inves de 440Hz)
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(435.0f);

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertTrue(reading.getCents() < 0);
        assertFalse(reading.isInTune());
    }

    @Test
    public void getReading_withPerfectPitch_shouldBeInTune() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(440.0f);

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertTrue(reading.isInTune());
        assertEquals(0, reading.getTuningDirection());
    }

    // === TESTES DE getReading() - DIRECAO DE AFINACAO ===

    @Test
    public void getReading_whenNoteIsFlat_shouldIndicateRaiseString() {
        // Arrange - Nota baixa demais
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(430.0f); // Bem abaixo de 440

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertEquals(1, reading.getTuningDirection()); // Precisa subir
    }

    @Test
    public void getReading_whenNoteIsSharp_shouldIndicateLowerString() {
        // Arrange - Nota alta demais
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(450.0f); // Bem acima de 440

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertEquals(-1, reading.getTuningDirection()); // Precisa baixar
    }

    // === TESTES DE getReading() - SEM SINAL ===

    @Test
    public void getReading_withZeroFrequency_shouldReturnNoSignal() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(0.0f);

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertTrue(reading.isValid());
        assertFalse(reading.hasSignal());
        assertEquals("--", reading.getFullNoteName());
    }

    @Test
    public void getReading_withNegativeFrequency_shouldReturnNoSignal() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(-10.0f);

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertFalse(reading.hasSignal());
    }

    @Test
    public void getReading_withVeryLowFrequency_shouldReturnNoSignal() {
        // Arrange - Frequencia abaixo do limite audivel para instrumentos (20Hz)
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(15.0f);

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertFalse(reading.hasSignal());
    }

    @Test
    public void getReading_withNoSignal_tuningDirectionShouldBeZero() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(0.0f);

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertEquals(0, reading.getTuningDirection());
    }

    // === TESTES DE getReading() - FALHAS ===

    @Test
    public void getReading_withoutNativeLibrary_shouldReturnInvalid() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(false);

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertFalse(reading.isValid());
        assertEquals("Biblioteca nativa nao carregada", reading.getErrorMessage());
    }

    @Test
    public void getReading_whenThrowsException_shouldReturnInvalid() {
        // Arrange
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenThrow(new RuntimeException("Sensor error"));

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertFalse(reading.isValid());
        assertTrue(reading.getErrorMessage().contains("Erro ao obter frequencia"));
    }

    // === TESTES DE NOTAS SUSTENIDAS ===

    @Test
    public void getReading_withCSharp_shouldReturnCSharp() {
        // Arrange - C#4 = 277.18Hz
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(277.18f);

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertEquals("C#", reading.getNoteName());
        assertEquals(4, reading.getOctave());
    }

    @Test
    public void getReading_withFSharp_shouldReturnFSharp() {
        // Arrange - F#4 = 369.99Hz
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(369.99f);

        // Act
        TunerUseCase.TunerReading reading = useCase.getReading();

        // Assert
        assertEquals("F#", reading.getNoteName());
    }

    // === TESTES DA CLASSE RESULT ===

    @Test
    public void result_success_shouldHaveCorrectValues() {
        // Act
        TunerUseCase.Result result = TunerUseCase.Result.success("Test message");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Test message", result.getMessage());
    }

    @Test
    public void result_failure_shouldHaveCorrectValues() {
        // Act
        TunerUseCase.Result result = TunerUseCase.Result.failure("Error message");

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Error message", result.getMessage());
    }

    // === TESTES DA CLASSE TUNERREADING ===

    @Test
    public void tunerReading_valid_shouldHaveCorrectValues() {
        // Act
        TunerUseCase.TunerReading reading = TunerUseCase.TunerReading.valid(440.0f, "A", 4, 0);

        // Assert
        assertTrue(reading.isValid());
        assertTrue(reading.hasSignal());
        assertEquals(440.0f, reading.getFrequency(), 0.001f);
        assertEquals("A", reading.getNoteName());
        assertEquals(4, reading.getOctave());
        assertEquals(0, reading.getCents());
        assertNull(reading.getErrorMessage());
    }

    @Test
    public void tunerReading_noSignal_shouldHaveCorrectValues() {
        // Act
        TunerUseCase.TunerReading reading = TunerUseCase.TunerReading.noSignal();

        // Assert
        assertTrue(reading.isValid());
        assertFalse(reading.hasSignal());
        assertEquals(0f, reading.getFrequency(), 0.001f);
        assertNull(reading.getNoteName());
    }

    @Test
    public void tunerReading_invalid_shouldHaveCorrectValues() {
        // Act
        TunerUseCase.TunerReading reading = TunerUseCase.TunerReading.invalid("Error");

        // Assert
        assertFalse(reading.isValid());
        assertFalse(reading.hasSignal());
        assertEquals("Error", reading.getErrorMessage());
    }

    // === TESTES DE OITAVAS ===

    @Test
    public void getReading_shouldCalculateCorrectOctaves() {
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);

        // A2 = 110Hz
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(110.0f);
        assertEquals(2, useCase.getReading().getOctave());

        // A3 = 220Hz
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(220.0f);
        assertEquals(3, useCase.getReading().getOctave());

        // A4 = 440Hz
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(440.0f);
        assertEquals(4, useCase.getReading().getOctave());

        // A5 = 880Hz
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(880.0f);
        assertEquals(5, useCase.getReading().getOctave());
    }

    // === TESTES DE isInTune() ===

    @Test
    public void isInTune_withCentsWithinRange_shouldReturnTrue() {
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(440.0f); // Exatamente A4

        TunerUseCase.TunerReading reading = useCase.getReading();
        assertTrue(reading.isInTune());
    }

    @Test
    public void isInTune_withCentsOutsideRange_shouldReturnFalse() {
        when(mockAudioEngine.isNativeLibraryLoaded()).thenReturn(true);
        when(mockAudioEngine.getDetectedFrequency()).thenReturn(450.0f); // Muito acima de A4

        TunerUseCase.TunerReading reading = useCase.getReading();
        assertFalse(reading.isInTune());
    }
}
