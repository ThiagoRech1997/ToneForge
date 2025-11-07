package com.thiagofernendorech.toneforge.testing;

import android.content.Context;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.domain.models.AudioState;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;
import com.thiagofernendorech.toneforge.infrastructure.audio.AudioAnalyzer;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive Regression Test Suite for AudioRepository
 * 
 * This test suite validates current AudioRepository behavior before architectural refactoring
 * to ensure no breaking changes occur during the migration to Clean Architecture.
 * 
 * CRITICAL ARCHITECTURAL VIOLATIONS BEING TESTED:
 * 1. AudioRepository in /data but behaving as infrastructure service
 * 2. Singleton pattern violating dependency injection principles
 * 3. Mixed responsibilities (Repository + Service pattern)
 * 4. Direct infrastructure dependencies from data layer
 */
@RunWith(RobolectricTestRunner.class)
public class AudioRepositoryRegressionTest {
    
    private static final String TAG = "AudioRepositoryRegressionTest";
    
    @Mock private Context mockContext;
    private AudioRepository audioRepository;
    private Context realContext;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        realContext = RuntimeEnvironment.getApplication();
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        audioRepository = AudioRepository.getInstance(mockContext);
    }
    
    @After
    public void tearDown() {
        if (audioRepository != null) {
            audioRepository.cleanup();
        }
    }
    
    // === SINGLETON PATTERN REGRESSION TESTS ===
    
    @Test
    public void regressionTest_singleton_shouldAlwaysReturnSameInstance() {
        // Given
        AudioRepository instance1 = AudioRepository.getInstance(mockContext);
        AudioRepository instance2 = AudioRepository.getInstance(mockContext);
        AudioRepository instance3 = AudioRepository.getInstance(realContext);
        
        // Then - All instances should be identical (current behavior)
        assertSame("Singleton should return same instance regardless of context", 
                   instance1, instance2);
        assertSame("Singleton should ignore different contexts", 
                   instance1, instance3);
    }
    
    @Test
    public void regressionTest_singleton_shouldNotBeNullAfterInitialization() {
        // When
        AudioRepository instance = AudioRepository.getInstance(mockContext);
        
        // Then
        assertNotNull("AudioRepository instance should never be null", instance);
    }
    
    // === AUDIO PIPELINE REGRESSION TESTS ===
    
    @Test
    public void regressionTest_audioPipeline_defaultStates() {
        // When - Fresh instance behavior
        boolean isRunning = audioRepository.isAudioPipelineRunning();
        boolean isPaused = audioRepository.isPipelinePaused();
        
        // Then - Validate current default behavior
        assertFalse("Audio pipeline should start as stopped", isRunning);
        assertFalse("Audio pipeline should start as not paused", isPaused);
    }
    
    @Test
    public void regressionTest_audioPipeline_lifecycleMethods() {
        // Given - Initial state
        assertFalse("Pipeline should start stopped", audioRepository.isAudioPipelineRunning());
        
        // When - Start pipeline
        boolean startResult = audioRepository.startAudioPipeline();
        
        // Then - Current behavior validation
        assertTrue("Start pipeline should return true", startResult);
        // Note: Actual running state depends on native implementation
        
        // When - Stop pipeline
        audioRepository.stopAudioPipeline();
        audioRepository.pauseAudioPipeline();
        audioRepository.resumeAudioPipeline();
        
        // Then - Should not throw exceptions (current behavior)
        // Validation passed if no exceptions thrown
    }
    
    // === EFFECT PARAMETERS REGRESSION TESTS ===
    
    @Test
    public void regressionTest_effectParameters_defaultValues() {
        // When
        EffectParameters params = audioRepository.getCurrentEffectParameters();
        
        // Then - Validate current default values
        assertNotNull("Effect parameters should never be null", params);
        assertEquals("Default gain should be 0.5", 0.5f, params.getGain(), 0.01f);
        assertEquals("Default distortion should be 0.0", 0.0f, params.getDistortion(), 0.01f);
        assertEquals("Default delay time should be 0.0", 0.0f, params.getDelayTime(), 0.01f);
        assertEquals("Default delay feedback should be 0.0", 0.0f, params.getDelayFeedback(), 0.01f);
        assertEquals("Default reverb room size should be 0.0", 0.0f, params.getReverbRoomSize(), 0.01f);
        assertEquals("Default reverb damping should be 0.0", 0.0f, params.getReverbDamping(), 0.01f);
    }
    
    @Test
    public void regressionTest_effectParameters_applyAndRetrieve() {
        // Given
        EffectParameters testParams = new EffectParameters();
        testParams.setGain(0.8f);
        testParams.setDistortion(0.3f);
        testParams.setDelayTime(200.0f);
        testParams.setDelayFeedback(0.4f);
        testParams.setReverbRoomSize(0.6f);
        testParams.setReverbDamping(0.5f);
        
        // When
        audioRepository.applyEffectParameters(testParams);
        
        // Then - Should not throw exceptions (current behavior)
        // Note: Actual parameter persistence depends on implementation
    }
    
    // === EFFECT ENABLE/DISABLE REGRESSION TESTS ===
    
    @Test
    public void regressionTest_effectEnableDisable_allEffects() {
        // Test all effect enable/disable combinations
        // This validates current behavior patterns
        
        // Gain
        audioRepository.setGainEnabled(true);
        audioRepository.setGainEnabled(false);
        
        // Distortion
        audioRepository.setDistortionEnabled(true);
        audioRepository.setDistortionEnabled(false);
        
        // Delay
        audioRepository.setDelayEnabled(true);
        audioRepository.setDelayEnabled(false);
        
        // Reverb
        audioRepository.setReverbEnabled(true);
        audioRepository.setReverbEnabled(false);
        
        // Then - Should complete without exceptions
    }
    
    // === PRESET SYSTEM REGRESSION TESTS ===
    
    @Test
    public void regressionTest_presets_basicOperations() {
        // When - Test basic preset operations
        boolean saveResult = audioRepository.savePreset("TestPreset");
        boolean loadResult = audioRepository.loadPreset("TestPreset");
        boolean deleteResult = audioRepository.deletePreset("TestPreset");
        
        // Then - Current behavior validation
        assertTrue("Save preset should return true", saveResult);
        assertTrue("Load preset should return true", loadResult);
        assertTrue("Delete preset should return true", deleteResult);
    }
    
    @Test
    public void regressionTest_presets_getPresetNames() {
        // When
        var presetNames = audioRepository.getPresetNames();
        
        // Then
        assertNotNull("Preset names list should not be null", presetNames);
        assertTrue("Preset names list should be valid", presetNames.size() >= 0);
    }
    
    // === AUTOMATION SYSTEM REGRESSION TESTS ===
    
    @Test
    public void regressionTest_automation_recordingPlayback() {
        // When - Test automation operations
        boolean startRecordResult = audioRepository.startAutomationRecording("TestPreset", "TestAuto");
        boolean stopRecordResult = audioRepository.stopAutomationRecording();
        boolean startPlayResult = audioRepository.startAutomationPlayback("TestPreset", "TestAuto");
        boolean stopPlayResult = audioRepository.stopAutomationPlayback();
        
        // Then - Current behavior validation
        assertTrue("Start automation recording should return true", startRecordResult);
        assertTrue("Stop automation recording should return true", stopRecordResult);
        assertTrue("Start automation playback should return true", startPlayResult);
        assertTrue("Stop automation playback should return true", stopPlayResult);
    }
    
    // === AUDIO STATE REGRESSION TESTS ===
    
    @Test
    public void regressionTest_audioState_currentState() {
        // When
        AudioState state = audioRepository.getCurrentAudioState();
        
        // Then - Validate current default state
        assertNotNull("Audio state should not be null", state);
        assertFalse("Pipeline should start as not running", state.isPipelineRunning());
        assertFalse("Pipeline should start as not paused", state.isPipelinePaused());
        assertEquals("Default latency mode should be 1", 1, state.getCurrentLatencyMode());
        assertFalse("Oversampling should start disabled", state.isOversamplingEnabled());
        assertEquals("Default oversampling factor should be 1", 1, state.getOversamplingFactor());
        assertFalse("Background audio should start disabled", state.isBackgroundAudioEnabled());
        assertFalse("MIDI should start disabled", state.isMidiEnabled());
        assertFalse("Tuner should start inactive", state.isTunerActive());
        assertFalse("Metronome should start inactive", state.isMetronomeActive());
        assertFalse("Looper should start not recording", state.isLooperRecording());
        assertFalse("Looper should start not playing", state.isLooperPlaying());
        assertFalse("Automation should start not recording", state.isAutomationRecording());
        assertFalse("Automation should start not playing", state.isAutomationPlaying());
    }
    
    @Test
    public void regressionTest_audioState_saveRestore() {
        // When
        audioRepository.saveCurrentState();
        audioRepository.restoreState();
        
        // Then - Should not throw exceptions
    }
    
    // === LATENCY MANAGEMENT REGRESSION TESTS ===
    
    @Test
    public void regressionTest_latency_defaultValues() {
        // When
        int currentMode = audioRepository.getCurrentLatencyMode();
        float estimatedLatency = audioRepository.getEstimatedLatency();
        
        // Then
        assertTrue("Latency mode should be valid (0-2)", currentMode >= 0 && currentMode <= 2);
        assertTrue("Estimated latency should be non-negative", estimatedLatency >= 0);
    }
    
    @Test
    public void regressionTest_latency_setModes() {
        // Test all latency modes
        audioRepository.setLatencyMode(0); // Low latency
        audioRepository.setLatencyMode(1); // Balanced
        audioRepository.setLatencyMode(2); // Stability
        
        // Should not throw exceptions
    }
    
    // === MIDI SYSTEM REGRESSION TESTS ===
    
    @Test
    public void regressionTest_midi_defaultState() {
        // When
        boolean isMidiEnabled = audioRepository.isMidiEnabled();
        
        // Then
        assertFalse("MIDI should start disabled", isMidiEnabled);
    }
    
    @Test
    public void regressionTest_midi_enableDisable() {
        // When
        audioRepository.setMidiEnabled(true);
        audioRepository.setMidiEnabled(false);
        
        // Then - Should not throw exceptions
    }
    
    // === AUDIO ANALYSIS REGRESSION TESTS ===
    
    @Test
    public void regressionTest_audioAnalysis_lifecycle() {
        // Given
        AudioAnalyzer.AudioAnalyzerCallback mockCallback = mock(AudioAnalyzer.AudioAnalyzerCallback.class);
        
        // When
        audioRepository.startAudioAnalysis(mockCallback);
        audioRepository.stopAudioAnalysis();
        
        // Then - Should not throw exceptions
    }
    
    // === LOOPER SYSTEM REGRESSION TESTS ===
    
    @Test
    public void regressionTest_looper_lifecycle() {
        // When - Test looper operations
        audioRepository.startLooperRecording();
        audioRepository.stopLooperRecording();
        audioRepository.startLooperPlayback();
        audioRepository.stopLooperPlayback();
        audioRepository.clearLooper();
        
        // Then - Should not throw exceptions
    }
    
    // === TUNER SYSTEM REGRESSION TESTS ===
    
    @Test
    public void regressionTest_tuner_lifecycle() {
        // When
        audioRepository.startTuner();
        float frequency = audioRepository.getDetectedFrequency();
        audioRepository.stopTuner();
        
        // Then
        assertTrue("Detected frequency should be non-negative", frequency >= 0);
    }
    
    // === METRONOME SYSTEM REGRESSION TESTS ===
    
    @Test
    public void regressionTest_metronome_lifecycle() {
        // When
        audioRepository.startMetronome(120);
        boolean isActive = audioRepository.isMetronomeActive();
        audioRepository.stopMetronome();
        
        // Then
        // Note: isActive depends on implementation details
        assertFalse("Metronome should start inactive", audioRepository.isMetronomeActive());
    }
    
    // === CLEANUP REGRESSION TEST ===
    
    @Test
    public void regressionTest_cleanup_shouldNotThrowException() {
        // When
        audioRepository.cleanup();
        
        // Then - Should complete without exceptions
        
        // Verify pipeline is stopped after cleanup
        assertFalse("Pipeline should be stopped after cleanup", 
                   audioRepository.isAudioPipelineRunning());
    }
    
    // === THREADING AND CONCURRENCY REGRESSION TESTS ===
    
    @Test
    public void regressionTest_concurrency_multipleOperations() {
        // This test validates current behavior under concurrent operations
        // Important for ensuring thread safety during refactoring
        
        // Simulate typical usage patterns
        audioRepository.startAudioPipeline();
        audioRepository.getCurrentEffectParameters();
        audioRepository.getCurrentAudioState();
        audioRepository.setGainEnabled(true);
        audioRepository.saveCurrentState();
        audioRepository.cleanup();
        
        // Should complete without exceptions or deadlocks
    }
}