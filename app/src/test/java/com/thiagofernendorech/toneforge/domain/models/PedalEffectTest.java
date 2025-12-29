package com.thiagofernendorech.toneforge.domain.models;

import com.thiagofernendorech.toneforge.PedalEffect;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Testes unitarios para PedalEffect
 * Testa valores padrao, factory methods e metodos utilitarios
 */
public class PedalEffectTest {

    private PedalEffect pedal;

    @Before
    public void setUp() {
        pedal = new PedalEffect();
    }

    // === TESTES DE VALORES PADRAO ===

    @Test
    public void testDefaultValues_shouldBeEnabled() {
        assertTrue(pedal.isEnabled());
    }

    @Test
    public void testDefaultValues_mainValueShouldBeHalf() {
        assertEquals(0.5f, pedal.getMainValue(), 0.001f);
    }

    @Test
    public void testDefaultValues_positionShouldBeZero() {
        assertEquals(0, pedal.getPosition());
    }

    // === TESTES DE CONSTRUTOR COM PARAMETROS ===

    @Test
    public void testConstructorWithParams_shouldSetName() {
        PedalEffect effect = new PedalEffect("Distortion", "Distortion", "DRIVE", 0);
        assertEquals("Distortion", effect.getName());
    }

    @Test
    public void testConstructorWithParams_shouldSetCategory() {
        PedalEffect effect = new PedalEffect("Distortion", "Distortion", "DRIVE", 0);
        assertEquals("Distortion", effect.getCategory());
    }

    @Test
    public void testConstructorWithParams_shouldSetMainKnobLabel() {
        PedalEffect effect = new PedalEffect("Distortion", "Distortion", "DRIVE", 0);
        assertEquals("DRIVE", effect.getMainKnobLabel());
    }

    @Test
    public void testConstructorWithParams_shouldSetTypeLowercase() {
        PedalEffect effect = new PedalEffect("Distortion", "Distortion", "DRIVE", 0);
        assertEquals("distortion", effect.getType());
    }

    // === TESTES DE FACTORY METHODS ===

    @Test
    public void testCreateOverdrive_shouldHaveCorrectName() {
        PedalEffect overdrive = PedalEffect.createOverdrive();
        assertEquals("Overdrive", overdrive.getName());
        assertEquals("Distortion", overdrive.getCategory());
        assertEquals("DRIVE", overdrive.getMainKnobLabel());
    }

    @Test
    public void testCreateDistortion_shouldHaveCorrectName() {
        PedalEffect distortion = PedalEffect.createDistortion();
        assertEquals("Distortion", distortion.getName());
        assertEquals("Distortion", distortion.getCategory());
        assertEquals("DIST", distortion.getMainKnobLabel());
    }

    @Test
    public void testCreateDelay_shouldHaveCorrectName() {
        PedalEffect delay = PedalEffect.createDelay();
        assertEquals("Delay", delay.getName());
        assertEquals("Time", delay.getCategory());
        assertEquals("TIME", delay.getMainKnobLabel());
    }

    @Test
    public void testCreateReverb_shouldHaveCorrectName() {
        PedalEffect reverb = PedalEffect.createReverb();
        assertEquals("Reverb", reverb.getName());
        assertEquals("Time", reverb.getCategory());
        assertEquals("ROOM", reverb.getMainKnobLabel());
    }

    @Test
    public void testCreateChorus_shouldHaveCorrectName() {
        PedalEffect chorus = PedalEffect.createChorus();
        assertEquals("Chorus", chorus.getName());
        assertEquals("Modulation", chorus.getCategory());
        assertEquals("DEPTH", chorus.getMainKnobLabel());
    }

    @Test
    public void testCreateFlanger_shouldHaveCorrectName() {
        PedalEffect flanger = PedalEffect.createFlanger();
        assertEquals("Flanger", flanger.getName());
        assertEquals("Modulation", flanger.getCategory());
        assertEquals("DEPTH", flanger.getMainKnobLabel());
    }

    @Test
    public void testCreatePhaser_shouldHaveCorrectName() {
        PedalEffect phaser = PedalEffect.createPhaser();
        assertEquals("Phaser", phaser.getName());
        assertEquals("Modulation", phaser.getCategory());
        assertEquals("DEPTH", phaser.getMainKnobLabel());
    }

    @Test
    public void testCreateEqualizer_shouldHaveCorrectName() {
        PedalEffect eq = PedalEffect.createEqualizer();
        assertEquals("EQ", eq.getName());
        assertEquals("Filter", eq.getCategory());
        assertEquals("FREQ", eq.getMainKnobLabel());
    }

    @Test
    public void testCreateCompressor_shouldHaveCorrectName() {
        PedalEffect compressor = PedalEffect.createCompressor();
        assertEquals("Compressor", compressor.getName());
        assertEquals("Dynamics", compressor.getCategory());
        assertEquals("RATIO", compressor.getMainKnobLabel());
    }

    @Test
    public void testCreateGain_shouldHaveCorrectName() {
        PedalEffect gain = PedalEffect.createGain();
        assertEquals("Gain", gain.getName());
        assertEquals("Dynamics", gain.getCategory());
        assertEquals("LEVEL", gain.getMainKnobLabel());
    }

    // === TESTES DE SETTERS COM CLAMPING ===

    @Test
    public void testSetMainValue_shouldClampToMax() {
        pedal.setMainValue(1.5f);
        assertEquals(1.0f, pedal.getMainValue(), 0.001f);
    }

    @Test
    public void testSetMainValue_shouldClampToMin() {
        pedal.setMainValue(-0.5f);
        assertEquals(0.0f, pedal.getMainValue(), 0.001f);
    }

    @Test
    public void testSetMainValue_validValue_shouldAccept() {
        pedal.setMainValue(0.75f);
        assertEquals(0.75f, pedal.getMainValue(), 0.001f);
    }

    @Test
    public void testSetSecondaryKnob1Value_shouldClampToMax() {
        pedal.setSecondaryKnob1Value(1.5f);
        assertEquals(1.0f, pedal.getSecondaryKnob1Value(), 0.001f);
    }

    @Test
    public void testSetSecondaryKnob1Value_shouldClampToMin() {
        pedal.setSecondaryKnob1Value(-0.5f);
        assertEquals(0.0f, pedal.getSecondaryKnob1Value(), 0.001f);
    }

    @Test
    public void testSetSecondaryKnob2Value_shouldClampToMax() {
        pedal.setSecondaryKnob2Value(2.0f);
        assertEquals(1.0f, pedal.getSecondaryKnob2Value(), 0.001f);
    }

    @Test
    public void testSetSecondaryKnob1Value_withNull_shouldBeNull() {
        pedal.setSecondaryKnob1Value(null);
        assertNull(pedal.getSecondaryKnob1Value());
    }

    // === TESTES DE SECONDARY KNOBS ===

    @Test
    public void testHasSecondaryKnobs_withNoLabels_shouldBeFalse() {
        PedalEffect gain = PedalEffect.createGain();
        assertFalse(gain.hasSecondaryKnobs());
    }

    @Test
    public void testHasSecondaryKnobs_withLabels_shouldBeTrue() {
        PedalEffect delay = PedalEffect.createDelay();
        assertTrue(delay.hasSecondaryKnobs());
    }

    @Test
    public void testSecondaryKnobLabels_forDelay() {
        PedalEffect delay = PedalEffect.createDelay();
        assertEquals("FEEDBACK", delay.getSecondaryKnob1Label());
        assertEquals("MIX", delay.getSecondaryKnob2Label());
    }

    @Test
    public void testSecondaryKnobDefaults_shouldBeHalf() {
        PedalEffect delay = PedalEffect.createDelay();
        assertEquals(0.5f, delay.getSecondaryKnob1Value(), 0.001f);
        assertEquals(0.5f, delay.getSecondaryKnob2Value(), 0.001f);
    }

    // === TESTES DE PERCENTAGE CONVERSION ===

    @Test
    public void testGetMainKnobValue_shouldReturnPercentage() {
        pedal.setMainValue(0.5f);
        assertEquals(50f, pedal.getMainKnobValue(), 0.001f);
    }

    @Test
    public void testSetMainKnobValue_shouldConvertFromPercentage() {
        pedal.setMainKnobValue(75f);
        assertEquals(0.75f, pedal.getMainValue(), 0.001f);
    }

    @Test
    public void testSecondaryKnob1ValueAsPercentage() {
        pedal.setSecondaryKnob1Value(0.8f);
        assertEquals(80f, pedal.getSecondaryKnob1ValueAsPercentage(), 0.001f);
    }

    @Test
    public void testSetSecondaryKnob1ValueAsPercentage() {
        pedal.setSecondaryKnob1ValueAsPercentage(60f);
        assertEquals(0.6f, pedal.getSecondaryKnob1Value(), 0.001f);
    }

    @Test
    public void testSecondaryKnob1ValueAsPercentage_whenNull_shouldReturn50() {
        pedal.setSecondaryKnob1Value(null);
        assertEquals(50f, pedal.getSecondaryKnob1ValueAsPercentage(), 0.001f);
    }

    // === TESTES DE ENABLED STATE ===

    @Test
    public void testSetEnabled_shouldUpdateState() {
        pedal.setEnabled(false);
        assertFalse(pedal.isEnabled());

        pedal.setEnabled(true);
        assertTrue(pedal.isEnabled());
    }

    // === TESTES DE POSITION ===

    @Test
    public void testSetPosition_shouldUpdateValue() {
        pedal.setPosition(5);
        assertEquals(5, pedal.getPosition());
    }

    // === TESTES DE DEFAULT PEDALBOARD ===

    @Test
    public void testCreateDefaultPedalboard_shouldReturn10Pedals() {
        List<PedalEffect> pedalboard = PedalEffect.createDefaultPedalboard();
        assertEquals(10, pedalboard.size());
    }

    @Test
    public void testCreateDefaultPedalboard_shouldContainGain() {
        List<PedalEffect> pedalboard = PedalEffect.createDefaultPedalboard();
        assertTrue(pedalboard.stream().anyMatch(p -> "Gain".equals(p.getName())));
    }

    @Test
    public void testCreateDefaultPedalboard_shouldContainDistortion() {
        List<PedalEffect> pedalboard = PedalEffect.createDefaultPedalboard();
        assertTrue(pedalboard.stream().anyMatch(p -> "Distortion".equals(p.getName())));
    }

    @Test
    public void testCreateDefaultPedalboard_shouldContainDelay() {
        List<PedalEffect> pedalboard = PedalEffect.createDefaultPedalboard();
        assertTrue(pedalboard.stream().anyMatch(p -> "Delay".equals(p.getName())));
    }

    @Test
    public void testCreateDefaultPedalboard_shouldContainReverb() {
        List<PedalEffect> pedalboard = PedalEffect.createDefaultPedalboard();
        assertTrue(pedalboard.stream().anyMatch(p -> "Reverb".equals(p.getName())));
    }

    @Test
    public void testCreateDefaultPedalboard_allShouldBeEnabled() {
        List<PedalEffect> pedalboard = PedalEffect.createDefaultPedalboard();
        assertTrue(pedalboard.stream().allMatch(PedalEffect::isEnabled));
    }

    // === TESTES DE TOSTRING ===

    @Test
    public void testToString_shouldContainName() {
        PedalEffect delay = PedalEffect.createDelay();
        String result = delay.toString();
        assertTrue(result.contains("name='Delay'"));
    }

    @Test
    public void testToString_shouldContainCategory() {
        PedalEffect delay = PedalEffect.createDelay();
        String result = delay.toString();
        assertTrue(result.contains("category='Time'"));
    }

    @Test
    public void testToString_shouldContainEnabled() {
        String result = pedal.toString();
        assertTrue(result.contains("enabled=true"));
    }

    // === TESTES DE SETTERS BASICOS ===

    @Test
    public void testSetName_shouldUpdateName() {
        pedal.setName("CustomEffect");
        assertEquals("CustomEffect", pedal.getName());
    }

    @Test
    public void testSetCategory_shouldUpdateCategory() {
        pedal.setCategory("Custom");
        assertEquals("Custom", pedal.getCategory());
    }

    @Test
    public void testSetType_shouldUpdateType() {
        pedal.setType("custom_type");
        assertEquals("custom_type", pedal.getType());
    }

    @Test
    public void testSetSubtitle_shouldUpdateSubtitle() {
        pedal.setSubtitle("A great effect");
        assertEquals("A great effect", pedal.getSubtitle());
    }

    @Test
    public void testSetColor_shouldUpdateColor() {
        pedal.setColor("#FF0000");
        assertEquals("#FF0000", pedal.getColor());
    }

    @Test
    public void testSetMainKnobLabel_shouldUpdateLabel() {
        pedal.setMainKnobLabel("VOLUME");
        assertEquals("VOLUME", pedal.getMainKnobLabel());
    }

    @Test
    public void testSetIconResourceId_shouldUpdateId() {
        pedal.setIconResourceId(123);
        assertEquals(123, pedal.getIconResourceId());
    }

    @Test
    public void testSetBackgroundColor_shouldUpdateColor() {
        pedal.setBackgroundColor(0xFFFF0000);
        assertEquals(0xFFFF0000, pedal.getBackgroundColor());
    }
}
