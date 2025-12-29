package com.thiagofernendorech.toneforge.domain.models;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Testes unitarios para EffectParameters
 * Testa valores padrao, getters/setters e metodos utilitarios
 */
public class EffectParametersTest {

    private EffectParameters params;

    @Before
    public void setUp() {
        params = new EffectParameters();
    }

    // === TESTES DE VALORES PADRAO ===

    @Test
    public void testDefaultValues_gainShouldBeHalf() {
        assertEquals(0.5f, params.getGain(), 0.001f);
    }

    @Test
    public void testDefaultValues_gainShouldBeEnabled() {
        assertTrue(params.isGainEnabled());
    }

    @Test
    public void testDefaultValues_distortionShouldBeZero() {
        assertEquals(0.0f, params.getDistortion(), 0.001f);
    }

    @Test
    public void testDefaultValues_distortionShouldBeDisabled() {
        assertFalse(params.isDistortionEnabled());
    }

    @Test
    public void testDefaultValues_delayShouldBeDisabled() {
        assertFalse(params.isDelayEnabled());
        assertEquals(0.0f, params.getDelayTime(), 0.001f);
        assertEquals(0.0f, params.getDelayFeedback(), 0.001f);
        assertEquals(0.0f, params.getDelayMix(), 0.001f);
    }

    @Test
    public void testDefaultValues_reverbShouldBeDisabled() {
        assertFalse(params.isReverbEnabled());
        assertEquals(0.0f, params.getReverbRoomSize(), 0.001f);
        assertEquals(0.0f, params.getReverbDamping(), 0.001f);
        assertEquals(0.0f, params.getReverbMix(), 0.001f);
    }

    @Test
    public void testDefaultValues_chorusShouldBeDisabled() {
        assertFalse(params.isChorusEnabled());
    }

    @Test
    public void testDefaultValues_flangerShouldBeDisabled() {
        assertFalse(params.isFlangerEnabled());
    }

    @Test
    public void testDefaultValues_phaserShouldBeDisabled() {
        assertFalse(params.isPhaserEnabled());
    }

    @Test
    public void testDefaultValues_eqShouldBeDisabled() {
        assertFalse(params.isEqEnabled());
    }

    @Test
    public void testDefaultValues_compressorShouldBeDisabled() {
        assertFalse(params.isCompressorEnabled());
    }

    @Test
    public void testDefaultValues_compressorThresholdShouldBeNegative20() {
        assertEquals(-20.0f, params.getCompressorThreshold(), 0.001f);
    }

    @Test
    public void testDefaultValues_compressorRatioShouldBeTwo() {
        assertEquals(2.0f, params.getCompressorRatio(), 0.001f);
    }

    // === TESTES DE SETTERS ===

    @Test
    public void testSetGain_shouldUpdateValue() {
        params.setGain(0.8f);
        assertEquals(0.8f, params.getGain(), 0.001f);
    }

    @Test
    public void testSetGainEnabled_shouldUpdateState() {
        params.setGainEnabled(false);
        assertFalse(params.isGainEnabled());
    }

    @Test
    public void testSetDistortion_shouldUpdateValue() {
        params.setDistortion(0.7f);
        assertEquals(0.7f, params.getDistortion(), 0.001f);
    }

    @Test
    public void testSetDelayParameters_shouldUpdateAllValues() {
        params.setDelayTime(500.0f);
        params.setDelayFeedback(0.5f);
        params.setDelayMix(0.3f);
        params.setDelayEnabled(true);

        assertEquals(500.0f, params.getDelayTime(), 0.001f);
        assertEquals(0.5f, params.getDelayFeedback(), 0.001f);
        assertEquals(0.3f, params.getDelayMix(), 0.001f);
        assertTrue(params.isDelayEnabled());
    }

    @Test
    public void testSetReverbParameters_shouldUpdateAllValues() {
        params.setReverbRoomSize(0.8f);
        params.setReverbDamping(0.4f);
        params.setReverbMix(0.6f);
        params.setReverbEnabled(true);

        assertEquals(0.8f, params.getReverbRoomSize(), 0.001f);
        assertEquals(0.4f, params.getReverbDamping(), 0.001f);
        assertEquals(0.6f, params.getReverbMix(), 0.001f);
        assertTrue(params.isReverbEnabled());
    }

    // === TESTES DE CONTAGEM DE EFEITOS ATIVOS ===

    @Test
    public void testGetActiveEffectsCount_defaultShouldBeOne() {
        // Apenas gain esta ativo por padrao
        assertEquals(1, params.getActiveEffectsCount());
    }

    @Test
    public void testGetActiveEffectsCount_withMultipleEnabled() {
        params.setDistortionEnabled(true);
        params.setDelayEnabled(true);
        params.setReverbEnabled(true);

        // gain (default) + distortion + delay + reverb = 4
        assertEquals(4, params.getActiveEffectsCount());
    }

    @Test
    public void testGetActiveEffectsCount_withAllEnabled() {
        params.setGainEnabled(true);
        params.setDistortionEnabled(true);
        params.setDelayEnabled(true);
        params.setReverbEnabled(true);
        params.setChorusEnabled(true);
        params.setFlangerEnabled(true);
        params.setPhaserEnabled(true);
        params.setEqEnabled(true);
        params.setCompressorEnabled(true);

        assertEquals(9, params.getActiveEffectsCount());
    }

    @Test
    public void testGetActiveEffectsCount_withNoneEnabled() {
        params.setGainEnabled(false);
        assertEquals(0, params.getActiveEffectsCount());
    }

    // === TESTES DE hasActiveEffects ===

    @Test
    public void testHasActiveEffects_defaultShouldBeTrue() {
        assertTrue(params.hasActiveEffects());
    }

    @Test
    public void testHasActiveEffects_withNoneEnabled() {
        params.setGainEnabled(false);
        assertFalse(params.hasActiveEffects());
    }

    // === TESTES DE RESET ===

    @Test
    public void testReset_shouldRestoreDefaults() {
        // Modificar varios parametros
        params.setGain(0.9f);
        params.setDistortion(0.8f);
        params.setDistortionEnabled(true);
        params.setDelayEnabled(true);

        // Reset
        params.reset();

        // Verificar valores padrao
        assertEquals(0.5f, params.getGain(), 0.001f);
        assertEquals(0.0f, params.getDistortion(), 0.001f);
        assertFalse(params.isDistortionEnabled());
        assertFalse(params.isDelayEnabled());
        assertTrue(params.isGainEnabled());
    }

    // === TESTES DE COPYFROM ===

    @Test
    public void testCopyFrom_shouldCopyAllValues() {
        EffectParameters source = new EffectParameters();
        source.setGain(0.75f);
        source.setDistortion(0.5f);
        source.setDistortionEnabled(true);
        source.setDelayTime(300.0f);
        source.setDelayEnabled(true);

        EffectParameters target = new EffectParameters();
        target.copyFrom(source);

        assertEquals(0.75f, target.getGain(), 0.001f);
        assertEquals(0.5f, target.getDistortion(), 0.001f);
        assertTrue(target.isDistortionEnabled());
        assertEquals(300.0f, target.getDelayTime(), 0.001f);
        assertTrue(target.isDelayEnabled());
    }

    @Test
    public void testCopyFrom_withNull_shouldNotThrow() {
        params.setGain(0.8f);
        params.copyFrom(null);
        // Valor original deve ser mantido
        assertEquals(0.8f, params.getGain(), 0.001f);
    }

    // === TESTES DE TOSTRING ===

    @Test
    public void testToString_shouldContainGainValue() {
        String result = params.toString();
        assertTrue(result.contains("gain=0.5"));
    }

    @Test
    public void testToString_shouldContainActiveEffectsCount() {
        String result = params.toString();
        assertTrue(result.contains("activeEffects="));
    }

    // === TESTES DE EQ ===

    @Test
    public void testEqParameters_shouldUpdateAllValues() {
        params.setEqLow(0.3f);
        params.setEqMid(0.5f);
        params.setEqHigh(0.7f);
        params.setEqEnabled(true);

        assertEquals(0.3f, params.getEqLow(), 0.001f);
        assertEquals(0.5f, params.getEqMid(), 0.001f);
        assertEquals(0.7f, params.getEqHigh(), 0.001f);
        assertTrue(params.isEqEnabled());
    }

    // === TESTES DE MODULATION EFFECTS ===

    @Test
    public void testChorusParameters_shouldUpdateAllValues() {
        params.setChorusDepth(0.6f);
        params.setChorusRate(0.4f);
        params.setChorusMix(0.5f);
        params.setChorusEnabled(true);

        assertEquals(0.6f, params.getChorusDepth(), 0.001f);
        assertEquals(0.4f, params.getChorusRate(), 0.001f);
        assertEquals(0.5f, params.getChorusMix(), 0.001f);
        assertTrue(params.isChorusEnabled());
    }

    @Test
    public void testFlangerParameters_shouldUpdateAllValues() {
        params.setFlangerDepth(0.7f);
        params.setFlangerRate(0.3f);
        params.setFlangerMix(0.4f);
        params.setFlangerEnabled(true);

        assertEquals(0.7f, params.getFlangerDepth(), 0.001f);
        assertEquals(0.3f, params.getFlangerRate(), 0.001f);
        assertEquals(0.4f, params.getFlangerMix(), 0.001f);
        assertTrue(params.isFlangerEnabled());
    }

    @Test
    public void testPhaserParameters_shouldUpdateAllValues() {
        params.setPhaserDepth(0.5f);
        params.setPhaserRate(0.6f);
        params.setPhaserMix(0.7f);
        params.setPhaserEnabled(true);

        assertEquals(0.5f, params.getPhaserDepth(), 0.001f);
        assertEquals(0.6f, params.getPhaserRate(), 0.001f);
        assertEquals(0.7f, params.getPhaserMix(), 0.001f);
        assertTrue(params.isPhaserEnabled());
    }

    // === TESTES DE COMPRESSOR ===

    @Test
    public void testCompressorParameters_shouldUpdateAllValues() {
        params.setCompressorThreshold(-10.0f);
        params.setCompressorRatio(4.0f);
        params.setCompressorAttack(5.0f);
        params.setCompressorRelease(50.0f);
        params.setCompressorEnabled(true);

        assertEquals(-10.0f, params.getCompressorThreshold(), 0.001f);
        assertEquals(4.0f, params.getCompressorRatio(), 0.001f);
        assertEquals(5.0f, params.getCompressorAttack(), 0.001f);
        assertEquals(50.0f, params.getCompressorRelease(), 0.001f);
        assertTrue(params.isCompressorEnabled());
    }
}
