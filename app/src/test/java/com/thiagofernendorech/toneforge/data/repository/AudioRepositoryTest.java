package com.thiagofernendorech.toneforge.data.repository;

import android.content.Context;
import com.thiagofernendorech.toneforge.domain.models.AudioState;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o AudioRepository
 * Valida as operações de dados e integração com managers
 */
@RunWith(RobolectricTestRunner.class)
public class AudioRepositoryTest {
    
    @Mock
    private Context mockContext;
    
    private AudioRepository audioRepository;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        audioRepository = AudioRepository.getInstance(mockContext);
    }
    
    @Test
    public void testGetInstance_shouldReturnSameInstance() {
        // Act
        AudioRepository instance1 = AudioRepository.getInstance(mockContext);
        AudioRepository instance2 = AudioRepository.getInstance(mockContext);
        
        // Assert
        assertSame(instance1, instance2);
    }
    
    @Test
    public void testStartAudioPipeline_shouldReturnTrue() {
        // Act
        boolean result = audioRepository.startAudioPipeline();
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testIsAudioPipelineRunning_shouldReturnFalse() {
        // Act
        boolean result = audioRepository.isAudioPipelineRunning();
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testStopAudioPipeline_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.stopAudioPipeline();
    }
    
    @Test
    public void testPauseAudioPipeline_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.pauseAudioPipeline();
    }
    
    @Test
    public void testResumeAudioPipeline_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.resumeAudioPipeline();
    }
    
    @Test
    public void testApplyEffectParameters_shouldNotThrowException() {
        // Arrange
        EffectParameters parameters = new EffectParameters();
        parameters.setGain(0.8f);
        parameters.setDistortion(0.3f);
        
        // Act & Assert - should not throw exception
        audioRepository.applyEffectParameters(parameters);
    }
    
    @Test
    public void testGetCurrentEffectParameters_shouldReturnValidParameters() {
        // Act
        EffectParameters parameters = audioRepository.getCurrentEffectParameters();
        
        // Assert
        assertNotNull(parameters);
        assertEquals(0.5f, parameters.getGain(), 0.01f);
        assertEquals(0.0f, parameters.getDistortion(), 0.01f);
        assertEquals(0.0f, parameters.getDelayTime(), 0.01f);
        assertEquals(0.0f, parameters.getDelayFeedback(), 0.01f);
        assertEquals(0.0f, parameters.getReverbRoomSize(), 0.01f);
        assertEquals(0.0f, parameters.getReverbDamping(), 0.01f);
    }
    
    @Test
    public void testSetGainEnabled_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.setGainEnabled(true);
        audioRepository.setGainEnabled(false);
    }
    
    @Test
    public void testSetDistortionEnabled_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.setDistortionEnabled(true);
        audioRepository.setDistortionEnabled(false);
    }
    
    @Test
    public void testSetDelayEnabled_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.setDelayEnabled(true);
        audioRepository.setDelayEnabled(false);
    }
    
    @Test
    public void testSetReverbEnabled_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.setReverbEnabled(true);
        audioRepository.setReverbEnabled(false);
    }
    
    @Test
    public void testLoadPreset_shouldReturnTrue() {
        // Act
        boolean result = audioRepository.loadPreset("Test Preset");
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testSavePreset_shouldReturnTrue() {
        // Act
        boolean result = audioRepository.savePreset("Test Preset");
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testDeletePreset_shouldReturnTrue() {
        // Act
        boolean result = audioRepository.deletePreset("Test Preset");
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testGetPresetNames_shouldReturnValidList() {
        // Act
        List<String> presets = audioRepository.getPresetNames();
        
        // Assert
        assertNotNull(presets);
        assertTrue(presets.size() >= 0);
    }
    
    @Test
    public void testStartAutomationRecording_shouldReturnTrue() {
        // Act
        boolean result = audioRepository.startAutomationRecording("Preset", "Automation");
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testStopAutomationRecording_shouldReturnTrue() {
        // Act
        boolean result = audioRepository.stopAutomationRecording();
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testStartAutomationPlayback_shouldReturnTrue() {
        // Act
        boolean result = audioRepository.startAutomationPlayback("Preset", "Automation");
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testStopAutomationPlayback_shouldReturnTrue() {
        // Act
        boolean result = audioRepository.stopAutomationPlayback();
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testGetCurrentAudioState_shouldReturnValidState() {
        // Act
        AudioState state = audioRepository.getCurrentAudioState();
        
        // Assert
        assertNotNull(state);
        assertFalse(state.isPipelineRunning());
        assertFalse(state.isPipelinePaused());
        assertEquals(1, state.getCurrentLatencyMode());
        assertFalse(state.isOversamplingEnabled());
        assertEquals(1, state.getOversamplingFactor());
        assertFalse(state.isBackgroundAudioEnabled());
        assertFalse(state.isMidiEnabled());
        assertFalse(state.isTunerActive());
        assertFalse(state.isMetronomeActive());
        assertFalse(state.isLooperRecording());
        assertFalse(state.isLooperPlaying());
        assertFalse(state.isAutomationRecording());
        assertFalse(state.isAutomationPlaying());
    }
    
    @Test
    public void testSaveCurrentState_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.saveCurrentState();
    }
    
    @Test
    public void testRestoreState_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.restoreState();
    }
    
    @Test
    public void testSetLatencyMode_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.setLatencyMode(0); // Baixa latência
        audioRepository.setLatencyMode(1); // Equilibrado
        audioRepository.setLatencyMode(2); // Estabilidade
    }
    
    @Test
    public void testGetCurrentLatencyMode_shouldReturnValidMode() {
        // Act
        int mode = audioRepository.getCurrentLatencyMode();
        
        // Assert
        assertTrue(mode >= 0 && mode <= 2);
    }
    
    @Test
    public void testGetEstimatedLatency_shouldReturnValidValue() {
        // Act
        float latency = audioRepository.getEstimatedLatency();
        
        // Assert
        assertTrue(latency >= 0);
    }
    
    @Test
    public void testSetMidiEnabled_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.setMidiEnabled(true);
        audioRepository.setMidiEnabled(false);
    }
    
    @Test
    public void testIsMidiEnabled_shouldReturnFalse() {
        // Act
        boolean result = audioRepository.isMidiEnabled();
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testStartAudioAnalysis_shouldNotThrowException() {
        // Arrange
        AudioAnalyzer.AudioAnalyzerCallback callback = new AudioAnalyzer.AudioAnalyzerCallback() {
            @Override
            public void onFrequencyDetected(float frequency) {}
            
            @Override
            public void onNoteDetected(String note, int octave) {}
            
            @Override
            public void onCentsDeviation(int cents) {}
        };
        
        // Act & Assert - should not throw exception
        audioRepository.startAudioAnalysis(callback);
    }
    
    @Test
    public void testStopAudioAnalysis_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.stopAudioAnalysis();
    }
    
    @Test
    public void testStartLooperRecording_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.startLooperRecording();
    }
    
    @Test
    public void testStopLooperRecording_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.stopLooperRecording();
    }
    
    @Test
    public void testStartLooperPlayback_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.startLooperPlayback();
    }
    
    @Test
    public void testStopLooperPlayback_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.stopLooperPlayback();
    }
    
    @Test
    public void testClearLooper_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.clearLooper();
    }
    
    @Test
    public void testStartTuner_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.startTuner();
    }
    
    @Test
    public void testStopTuner_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.stopTuner();
    }
    
    @Test
    public void testGetDetectedFrequency_shouldReturnValidValue() {
        // Act
        float frequency = audioRepository.getDetectedFrequency();
        
        // Assert
        assertTrue(frequency >= 0);
    }
    
    @Test
    public void testStartMetronome_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.startMetronome(120);
    }
    
    @Test
    public void testStopMetronome_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.stopMetronome();
    }
    
    @Test
    public void testIsMetronomeActive_shouldReturnFalse() {
        // Act
        boolean result = audioRepository.isMetronomeActive();
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testCleanup_shouldNotThrowException() {
        // Act & Assert - should not throw exception
        audioRepository.cleanup();
    }
} 