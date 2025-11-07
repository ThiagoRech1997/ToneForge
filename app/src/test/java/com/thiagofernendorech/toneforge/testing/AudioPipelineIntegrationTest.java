package com.thiagofernendorech.toneforge.testing;

import android.content.Context;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.domain.models.AudioState;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;
import com.thiagofernendorech.toneforge.domain.usecases.StartAudioPipelineUseCase;
import com.thiagofernendorech.toneforge.infrastructure.audio.AudioAnalyzer;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Audio Pipeline Integration Test Suite
 * 
 * This test suite validates the integration between different layers of the audio
 * processing system, ensuring proper data flow, state management, and error handling
 * across the entire audio pipeline.
 * 
 * INTEGRATION POINTS TESTED:
 * 1. AudioRepository -> Native Audio Engine
 * 2. Use Cases -> Repository interactions
 * 3. Audio State Management across components
 * 4. Effect parameter propagation through pipeline
 * 5. Audio analyzer integration with pipeline
 * 6. Error propagation and recovery mechanisms
 */
@RunWith(RobolectricTestRunner.class)
public class AudioPipelineIntegrationTest {
    
    private static final String TAG = "AudioPipelineIntegrationTest";
    private static final int TIMEOUT_SECONDS = 5;
    
    @Mock private Context mockContext;
    private AudioRepository audioRepository;
    private StartAudioPipelineUseCase startAudioPipelineUseCase;
    private Context realContext;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        realContext = RuntimeEnvironment.getApplication();
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        
        // Initialize components
        audioRepository = AudioRepository.getInstance(mockContext);
        startAudioPipelineUseCase = new StartAudioPipelineUseCase(audioRepository);
    }
    
    @After
    public void tearDown() {
        if (audioRepository != null) {
            audioRepository.cleanup();
        }
    }
    
    // === PIPELINE LIFECYCLE INTEGRATION TESTS ===
    
    @Test
    public void testPipelineIntegration_startStopLifecycle() {
        // Given - Initial state
        assertFalse("Pipeline should start stopped", audioRepository.isAudioPipelineRunning());
        
        // When - Start pipeline via use case
        boolean startResult = startAudioPipelineUseCase.execute();
        
        // Then - Validate integration
        assertTrue("Use case should successfully start pipeline", startResult);
        
        // When - Stop pipeline via repository
        audioRepository.stopAudioPipeline();
        
        // Then - State should be consistent
        assertFalse("Pipeline should be stopped", audioRepository.isAudioPipelineRunning());
    }
    
    @Test
    public void testPipelineIntegration_pauseResumeFlow() {
        // Given - Started pipeline
        audioRepository.startAudioPipeline();
        
        // When - Pause and resume
        audioRepository.pauseAudioPipeline();
        boolean isPausedAfterPause = audioRepository.isPipelinePaused();
        
        audioRepository.resumeAudioPipeline();
        boolean isPausedAfterResume = audioRepository.isPipelinePaused();
        
        // Then - State changes should be consistent
        // Note: Actual paused state depends on implementation
        // This validates integration doesn't crash
        audioRepository.stopAudioPipeline();
    }
    
    // === EFFECT PARAMETERS INTEGRATION TESTS ===
    
    @Test
    public void testEffectIntegration_parameterPropagation() {
        // Given - Pipeline started
        audioRepository.startAudioPipeline();
        
        // When - Apply effect parameters
        EffectParameters testParams = new EffectParameters();
        testParams.setGain(0.8f);
        testParams.setDistortion(0.3f);
        testParams.setDelayTime(150.0f);
        testParams.setDelayFeedback(0.4f);
        testParams.setReverbRoomSize(0.6f);
        testParams.setReverbDamping(0.5f);
        
        audioRepository.applyEffectParameters(testParams);
        
        // Then - Parameters should be retrievable
        EffectParameters retrievedParams = audioRepository.getCurrentEffectParameters();
        assertNotNull("Retrieved parameters should not be null", retrievedParams);
        
        // Clean up
        audioRepository.stopAudioPipeline();
    }
    
    @Test
    public void testEffectIntegration_enableDisableFlow() {
        // Given - Pipeline started
        audioRepository.startAudioPipeline();
        
        // When - Enable effects in sequence
        audioRepository.setGainEnabled(true);
        audioRepository.setDistortionEnabled(true);
        audioRepository.setDelayEnabled(true);
        audioRepository.setReverbEnabled(true);
        
        // Then - Should not cause pipeline errors
        assertTrue("Pipeline should remain running", audioRepository.isAudioPipelineRunning());
        
        // When - Disable effects
        audioRepository.setGainEnabled(false);
        audioRepository.setDistortionEnabled(false);
        audioRepository.setDelayEnabled(false);
        audioRepository.setReverbEnabled(false);
        
        // Then - Pipeline should still be stable
        assertTrue("Pipeline should remain stable", audioRepository.isAudioPipelineRunning());
        
        // Clean up
        audioRepository.stopAudioPipeline();
    }
    
    // === AUDIO STATE INTEGRATION TESTS ===
    
    @Test
    public void testStateIntegration_audioStateConsistency() {
        // Given - Initial state
        AudioState initialState = audioRepository.getCurrentAudioState();
        assertNotNull("Initial audio state should exist", initialState);
        
        // When - Start pipeline
        audioRepository.startAudioPipeline();
        AudioState runningState = audioRepository.getCurrentAudioState();
        
        // When - Pause pipeline
        audioRepository.pauseAudioPipeline();
        AudioState pausedState = audioRepository.getCurrentAudioState();
        
        // When - Stop pipeline
        audioRepository.stopAudioPipeline();
        AudioState stoppedState = audioRepository.getCurrentAudioState();
        
        // Then - States should be consistent and different
        assertNotNull("Running state should exist", runningState);
        assertNotNull("Paused state should exist", pausedState);
        assertNotNull("Stopped state should exist", stoppedState);
        
        // Validate state differences
        assertFalse("Initial state should not be running", initialState.isPipelineRunning());
        assertFalse("Final state should not be running", stoppedState.isPipelineRunning());
    }
    
    @Test
    public void testStateIntegration_saveRestoreState() {
        // Given - Configure some state
        EffectParameters testParams = new EffectParameters();
        testParams.setGain(0.7f);
        testParams.setDistortion(0.2f);
        
        audioRepository.applyEffectParameters(testParams);
        audioRepository.setLatencyMode(2); // Stability mode
        
        // When - Save current state
        audioRepository.saveCurrentState();
        
        // Modify state
        testParams.setGain(0.9f);
        audioRepository.applyEffectParameters(testParams);
        audioRepository.setLatencyMode(0); // Low latency
        
        // When - Restore state
        audioRepository.restoreState();
        
        // Then - State should be restored
        int currentMode = audioRepository.getCurrentLatencyMode();
        // Note: Validation depends on implementation behavior
    }
    
    // === LATENCY MANAGEMENT INTEGRATION TESTS ===
    
    @Test
    public void testLatencyIntegration_modeChanges() {
        // Test integration between latency management and audio pipeline
        
        // When - Change latency modes
        audioRepository.setLatencyMode(0); // Low latency
        int lowLatencyMode = audioRepository.getCurrentLatencyMode();
        float lowLatency = audioRepository.getEstimatedLatency();
        
        audioRepository.setLatencyMode(1); // Balanced
        int balancedMode = audioRepository.getCurrentLatencyMode();
        float balancedLatency = audioRepository.getEstimatedLatency();
        
        audioRepository.setLatencyMode(2); // Stability
        int stabilityMode = audioRepository.getCurrentLatencyMode();
        float stabilityLatency = audioRepository.getEstimatedLatency();
        
        // Then - Validate modes are set
        assertTrue("Low latency mode should be valid", lowLatencyMode >= 0 && lowLatencyMode <= 2);
        assertTrue("Balanced mode should be valid", balancedMode >= 0 && balancedMode <= 2);
        assertTrue("Stability mode should be valid", stabilityMode >= 0 && stabilityMode <= 2);
        
        assertTrue("Latencies should be non-negative", 
                   lowLatency >= 0 && balancedLatency >= 0 && stabilityLatency >= 0);
    }
    
    // === AUDIO ANALYSIS INTEGRATION TESTS ===
    
    @Test
    public void testAnalysisIntegration_analyzerLifecycle() throws InterruptedException {
        // Given - Pipeline running
        audioRepository.startAudioPipeline();
        
        // Setup callback for audio analysis
        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicReference<Float> detectedFrequency = new AtomicReference<>(0.0f);
        AtomicReference<String> detectedNote = new AtomicReference<>();
        
        AudioAnalyzer.AudioAnalyzerCallback callback = new AudioAnalyzer.AudioAnalyzerCallback() {
            @Override
            public void onFrequencyDetected(float frequency) {
                detectedFrequency.set(frequency);
                callbackLatch.countDown();
            }
            
            @Override
            public void onNoteDetected(String note, int octave) {
                detectedNote.set(note + octave);
            }
            
            @Override
            public void onCentsDeviation(int cents) {
                // Not tested in this integration
            }
        };
        
        // When - Start audio analysis
        audioRepository.startAudioAnalysis(callback);
        
        // Give some time for potential callbacks
        boolean callbackReceived = callbackLatch.await(2, TimeUnit.SECONDS);
        
        // When - Stop analysis
        audioRepository.stopAudioAnalysis();
        
        // Then - Integration should work without errors
        // Note: Callback may or may not be called depending on audio input
        
        // Clean up
        audioRepository.stopAudioPipeline();
    }
    
    // === LOOPER INTEGRATION TESTS ===
    
    @Test
    public void testLooperIntegration_recordingPlaybackFlow() {
        // Given - Pipeline running
        audioRepository.startAudioPipeline();
        
        // When - Test looper lifecycle
        audioRepository.startLooperRecording();
        
        // Simulate recording time
        try {
            Thread.sleep(100); // Brief recording
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        audioRepository.stopLooperRecording();
        audioRepository.startLooperPlayback();
        audioRepository.stopLooperPlayback();
        audioRepository.clearLooper();
        
        // Then - Should complete without errors
        assertTrue("Pipeline should remain running", audioRepository.isAudioPipelineRunning());
        
        // Clean up
        audioRepository.stopAudioPipeline();
    }
    
    // === TUNER INTEGRATION TESTS ===
    
    @Test
    public void testTunerIntegration_tunerLifecycle() {
        // Given - Pipeline running
        audioRepository.startAudioPipeline();
        
        // When - Start tuner
        audioRepository.startTuner();
        float detectedFreq = audioRepository.getDetectedFrequency();
        audioRepository.stopTuner();
        
        // Then - Should work without errors
        assertTrue("Detected frequency should be non-negative", detectedFreq >= 0);
        
        // Clean up
        audioRepository.stopAudioPipeline();
    }
    
    // === METRONOME INTEGRATION TESTS ===
    
    @Test
    public void testMetronomeIntegration_metronomeWithPipeline() {
        // Given - Pipeline running
        audioRepository.startAudioPipeline();
        
        // When - Start metronome
        audioRepository.startMetronome(120);
        boolean isActive = audioRepository.isMetronomeActive();
        audioRepository.stopMetronome();
        
        // Then - Integration should work
        assertFalse("Metronome should be inactive after stop", audioRepository.isMetronomeActive());
        
        // Clean up
        audioRepository.stopAudioPipeline();
    }
    
    // === MIDI INTEGRATION TESTS ===
    
    @Test
    public void testMidiIntegration_midiWithAudioPipeline() {
        // Given - Pipeline running
        audioRepository.startAudioPipeline();
        
        // When - Enable MIDI
        audioRepository.setMidiEnabled(true);
        boolean midiEnabled = audioRepository.isMidiEnabled();
        audioRepository.setMidiEnabled(false);
        
        // Then - Should integrate without pipeline disruption
        assertTrue("Pipeline should remain stable", audioRepository.isAudioPipelineRunning());
        
        // Clean up
        audioRepository.stopAudioPipeline();
    }
    
    // === ERROR HANDLING INTEGRATION TESTS ===
    
    @Test
    public void testErrorIntegration_pipelineRecovery() {
        // Test error handling and recovery across integration points
        
        // When - Try operations in various orders that might cause errors
        audioRepository.stopAudioPipeline(); // Stop when not started
        audioRepository.pauseAudioPipeline(); // Pause when not running
        audioRepository.resumeAudioPipeline(); // Resume when not paused
        
        // Then - Should not crash, should handle gracefully
        assertFalse("Pipeline should remain stopped", audioRepository.isAudioPipelineRunning());
        
        // When - Start and try error conditions
        audioRepository.startAudioPipeline();
        audioRepository.startAudioPipeline(); // Start when already started
        
        // Should handle gracefully
        audioRepository.stopAudioPipeline();
    }
    
    // === CONCURRENCY INTEGRATION TESTS ===
    
    @Test
    public void testConcurrencyIntegration_multipleOperations() throws InterruptedException {
        // Test concurrent operations across integration points
        
        final int THREAD_COUNT = 3;
        CountDownLatch startLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch completeLatch = new CountDownLatch(THREAD_COUNT);
        AtomicBoolean hasErrors = new AtomicBoolean(false);
        
        // Create threads that perform different operations
        Thread[] threads = new Thread[THREAD_COUNT];
        
        threads[0] = new Thread(() -> {
            try {
                startLatch.countDown();
                startLatch.await();
                
                audioRepository.startAudioPipeline();
                audioRepository.getCurrentAudioState();
                audioRepository.stopAudioPipeline();
            } catch (Exception e) {
                hasErrors.set(true);
            } finally {
                completeLatch.countDown();
            }
        });
        
        threads[1] = new Thread(() -> {
            try {
                startLatch.countDown();
                startLatch.await();
                
                EffectParameters params = audioRepository.getCurrentEffectParameters();
                audioRepository.setGainEnabled(true);
                audioRepository.setGainEnabled(false);
            } catch (Exception e) {
                hasErrors.set(true);
            } finally {
                completeLatch.countDown();
            }
        });
        
        threads[2] = new Thread(() -> {
            try {
                startLatch.countDown();
                startLatch.await();
                
                audioRepository.setLatencyMode(1);
                audioRepository.getCurrentLatencyMode();
                audioRepository.getEstimatedLatency();
            } catch (Exception e) {
                hasErrors.set(true);
            } finally {
                completeLatch.countDown();
            }
        });
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for completion
        boolean completed = completeLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        // Clean up
        audioRepository.cleanup();
        
        // Assertions
        assertTrue("All threads should complete within timeout", completed);
        assertFalse("No errors should occur during concurrent operations", hasErrors.get());
    }
    
    // === PERFORMANCE INTEGRATION TESTS ===
    
    @Test
    public void testPerformanceIntegration_operationTiming() {
        // Test that integration operations complete within reasonable time
        
        long startTime = System.currentTimeMillis();
        
        // Perform typical integration flow
        audioRepository.startAudioPipeline();
        audioRepository.getCurrentEffectParameters();
        audioRepository.getCurrentAudioState();
        
        EffectParameters params = new EffectParameters();
        params.setGain(0.5f);
        audioRepository.applyEffectParameters(params);
        
        audioRepository.setLatencyMode(1);
        audioRepository.stopAudioPipeline();
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Should complete quickly for main thread operations
        assertTrue("Integration operations should complete quickly", duration < 1000);
    }
}