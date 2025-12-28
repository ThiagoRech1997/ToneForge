package com.thiagofernendorech.toneforge.domain.models;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Testes unitarios para AudioState
 * Testa valores padrao, getters/setters e metodos utilitarios
 */
public class AudioStateTest {

    private AudioState state;

    @Before
    public void setUp() {
        state = new AudioState();
    }

    // === TESTES DE VALORES PADRAO ===

    @Test
    public void testDefaultValues_pipelineShouldNotBeRunning() {
        assertFalse(state.isPipelineRunning());
    }

    @Test
    public void testDefaultValues_pipelineShouldNotBePaused() {
        assertFalse(state.isPipelinePaused());
    }

    @Test
    public void testDefaultValues_latencyModeShouldBeBalanced() {
        // 1 = Equilibrado (Balanced)
        assertEquals(1, state.getCurrentLatencyMode());
    }

    @Test
    public void testDefaultValues_oversamplingShouldBeDisabled() {
        assertFalse(state.isOversamplingEnabled());
    }

    @Test
    public void testDefaultValues_oversamplingFactorShouldBeOne() {
        assertEquals(1, state.getOversamplingFactor());
    }

    @Test
    public void testDefaultValues_backgroundAudioShouldBeDisabled() {
        assertFalse(state.isBackgroundAudioEnabled());
    }

    @Test
    public void testDefaultValues_midiShouldBeDisabled() {
        assertFalse(state.isMidiEnabled());
    }

    @Test
    public void testDefaultValues_tunerShouldBeInactive() {
        assertFalse(state.isTunerActive());
    }

    @Test
    public void testDefaultValues_metronomeShouldBeInactive() {
        assertFalse(state.isMetronomeActive());
    }

    @Test
    public void testDefaultValues_looperShouldNotBeRecording() {
        assertFalse(state.isLooperRecording());
    }

    @Test
    public void testDefaultValues_looperShouldNotBePlaying() {
        assertFalse(state.isLooperPlaying());
    }

    @Test
    public void testDefaultValues_automationShouldNotBeRecording() {
        assertFalse(state.isAutomationRecording());
    }

    @Test
    public void testDefaultValues_automationShouldNotBePlaying() {
        assertFalse(state.isAutomationPlaying());
    }

    // === TESTES DE SETTERS ===

    @Test
    public void testSetPipelineRunning_shouldUpdateState() {
        state.setPipelineRunning(true);
        assertTrue(state.isPipelineRunning());
    }

    @Test
    public void testSetPipelinePaused_shouldUpdateState() {
        state.setPipelinePaused(true);
        assertTrue(state.isPipelinePaused());
    }

    @Test
    public void testSetLatencyMode_shouldUpdateMode() {
        state.setCurrentLatencyMode(0); // Low latency
        assertEquals(0, state.getCurrentLatencyMode());

        state.setCurrentLatencyMode(2); // Stability
        assertEquals(2, state.getCurrentLatencyMode());
    }

    @Test
    public void testSetOversamplingEnabled_shouldUpdateState() {
        state.setOversamplingEnabled(true);
        assertTrue(state.isOversamplingEnabled());
    }

    @Test
    public void testSetOversamplingFactor_shouldUpdateValue() {
        state.setOversamplingFactor(4);
        assertEquals(4, state.getOversamplingFactor());
    }

    @Test
    public void testSetBackgroundAudioEnabled_shouldUpdateState() {
        state.setBackgroundAudioEnabled(true);
        assertTrue(state.isBackgroundAudioEnabled());
    }

    @Test
    public void testSetMidiEnabled_shouldUpdateState() {
        state.setMidiEnabled(true);
        assertTrue(state.isMidiEnabled());
    }

    @Test
    public void testSetTunerActive_shouldUpdateState() {
        state.setTunerActive(true);
        assertTrue(state.isTunerActive());
    }

    @Test
    public void testSetMetronomeActive_shouldUpdateState() {
        state.setMetronomeActive(true);
        assertTrue(state.isMetronomeActive());
    }

    @Test
    public void testSetLooperRecording_shouldUpdateState() {
        state.setLooperRecording(true);
        assertTrue(state.isLooperRecording());
    }

    @Test
    public void testSetLooperPlaying_shouldUpdateState() {
        state.setLooperPlaying(true);
        assertTrue(state.isLooperPlaying());
    }

    @Test
    public void testSetAutomationRecording_shouldUpdateState() {
        state.setAutomationRecording(true);
        assertTrue(state.isAutomationRecording());
    }

    @Test
    public void testSetAutomationPlaying_shouldUpdateState() {
        state.setAutomationPlaying(true);
        assertTrue(state.isAutomationPlaying());
    }

    // === TESTES DE isBusy ===

    @Test
    public void testIsBusy_defaultShouldBeFalse() {
        assertFalse(state.isBusy());
    }

    @Test
    public void testIsBusy_whenLooperRecording_shouldBeTrue() {
        state.setLooperRecording(true);
        assertTrue(state.isBusy());
    }

    @Test
    public void testIsBusy_whenAutomationRecording_shouldBeTrue() {
        state.setAutomationRecording(true);
        assertTrue(state.isBusy());
    }

    @Test
    public void testIsBusy_whenTunerActive_shouldBeTrue() {
        state.setTunerActive(true);
        assertTrue(state.isBusy());
    }

    @Test
    public void testIsBusy_whenLooperPlaying_shouldBeFalse() {
        // Looper playing is not considered "busy" (just playing, not recording)
        state.setLooperPlaying(true);
        assertFalse(state.isBusy());
    }

    // === TESTES DE isPlaying ===

    @Test
    public void testIsPlaying_defaultShouldBeFalse() {
        assertFalse(state.isPlaying());
    }

    @Test
    public void testIsPlaying_whenLooperPlaying_shouldBeTrue() {
        state.setLooperPlaying(true);
        assertTrue(state.isPlaying());
    }

    @Test
    public void testIsPlaying_whenAutomationPlaying_shouldBeTrue() {
        state.setAutomationPlaying(true);
        assertTrue(state.isPlaying());
    }

    @Test
    public void testIsPlaying_whenMetronomeActive_shouldBeTrue() {
        state.setMetronomeActive(true);
        assertTrue(state.isPlaying());
    }

    @Test
    public void testIsPlaying_whenMultipleActive_shouldBeTrue() {
        state.setLooperPlaying(true);
        state.setMetronomeActive(true);
        assertTrue(state.isPlaying());
    }

    // === TESTES DE getStatusDescription ===

    @Test
    public void testGetStatusDescription_whenPipelineStopped_shouldReturnStopped() {
        assertEquals("Pipeline parado", state.getStatusDescription());
    }

    @Test
    public void testGetStatusDescription_whenPipelinePaused_shouldReturnPaused() {
        state.setPipelineRunning(true);
        state.setPipelinePaused(true);
        assertEquals("Pipeline pausado", state.getStatusDescription());
    }

    @Test
    public void testGetStatusDescription_whenLooperRecording_shouldReturnRecording() {
        state.setPipelineRunning(true);
        state.setLooperRecording(true);
        assertEquals("Gravando loop", state.getStatusDescription());
    }

    @Test
    public void testGetStatusDescription_whenAutomationRecording_shouldReturnRecording() {
        state.setPipelineRunning(true);
        state.setAutomationRecording(true);
        assertEquals("Gravando automação", state.getStatusDescription());
    }

    @Test
    public void testGetStatusDescription_whenLooperPlaying_shouldReturnPlaying() {
        state.setPipelineRunning(true);
        state.setLooperPlaying(true);
        assertEquals("Reproduzindo loop", state.getStatusDescription());
    }

    @Test
    public void testGetStatusDescription_whenAutomationPlaying_shouldReturnPlaying() {
        state.setPipelineRunning(true);
        state.setAutomationPlaying(true);
        assertEquals("Reproduzindo automação", state.getStatusDescription());
    }

    @Test
    public void testGetStatusDescription_whenMetronomeActive_shouldReturnActive() {
        state.setPipelineRunning(true);
        state.setMetronomeActive(true);
        assertEquals("Metrônomo ativo", state.getStatusDescription());
    }

    @Test
    public void testGetStatusDescription_whenTunerActive_shouldReturnActive() {
        state.setPipelineRunning(true);
        state.setTunerActive(true);
        assertEquals("Afinador ativo", state.getStatusDescription());
    }

    @Test
    public void testGetStatusDescription_whenPipelineRunningOnly_shouldReturnReady() {
        state.setPipelineRunning(true);
        assertEquals("Pronto", state.getStatusDescription());
    }

    // === TESTES DE TOSTRING ===

    @Test
    public void testToString_shouldContainPipelineRunning() {
        String result = state.toString();
        assertTrue(result.contains("pipelineRunning=false"));
    }

    @Test
    public void testToString_shouldContainLatencyMode() {
        String result = state.toString();
        assertTrue(result.contains("currentLatencyMode=1"));
    }

    // === TESTES DE METODOS ADICIONAIS (para compatibilidade) ===

    @Test
    public void testSetSampleRate_shouldNotThrow() {
        // Apenas verifica que nao lanca excecao
        state.setSampleRate(44100);
        state.setSampleRate(48000);
    }

    @Test
    public void testSetBufferSize_shouldNotThrow() {
        // Apenas verifica que nao lanca excecao
        state.setBufferSize(256);
        state.setBufferSize(512);
    }

    @Test
    public void testSetErrorCount_shouldNotThrow() {
        // Apenas verifica que nao lanca excecao
        state.setErrorCount(0);
        state.setErrorCount(10);
    }

    @Test
    public void testSetLastError_shouldNotThrow() {
        // Apenas verifica que nao lanca excecao
        state.setLastError(null);
        state.setLastError("Error message");
    }

    @Test
    public void testSetUptime_shouldNotThrow() {
        // Apenas verifica que nao lanca excecao
        state.setUptime(0);
        state.setUptime(1000);
    }

    @Test
    public void testSetTotalSamplesProcessed_shouldNotThrow() {
        // Apenas verifica que nao lanca excecao
        state.setTotalSamplesProcessed(0);
        state.setTotalSamplesProcessed(1000000);
    }

    // === TESTES DE PRIORIDADE DE STATUS ===

    @Test
    public void testStatusPriority_looperRecordingTakesPrecedence() {
        state.setPipelineRunning(true);
        state.setLooperRecording(true);
        state.setLooperPlaying(true);
        state.setMetronomeActive(true);

        // Gravando loop tem prioridade sobre outros status
        assertEquals("Gravando loop", state.getStatusDescription());
    }

    @Test
    public void testStatusPriority_automationRecordingOverPlaying() {
        state.setPipelineRunning(true);
        state.setAutomationRecording(true);
        state.setAutomationPlaying(true);

        // Gravando automacao tem prioridade sobre reproduzindo
        assertEquals("Gravando automação", state.getStatusDescription());
    }
}
