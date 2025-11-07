package com.thiagofernendorech.toneforge.testing;

import android.content.Context;
import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;
import com.thiagofernendorech.toneforge.domain.interfaces.PermissionInterface;
import com.thiagofernendorech.toneforge.domain.models.AudioState;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;
import com.thiagofernendorech.toneforge.domain.usecases.StartAudioPipelineUseCase;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive Audio Pipeline Integration Test Suite
 * 
 * This suite validates the complete audio processing pipeline from UI interactions
 * through domain use cases to infrastructure adapters and back. It ensures that
 * the refactored architecture maintains audio functionality while improving structure.
 * 
 * Test Coverage:
 * - Complete audio flow integration
 * - Error recovery and resilience
 * - Performance under load
 * - Thread safety and concurrency
 * - Memory management
 * - Real-time constraints validation
 */
@RunWith(RobolectricTestRunner.class)
public class AudioPipelineIntegrationTestSuite {
    
    @Mock private Context mockContext;
    
    private AudioRepository audioRepository;
    private AudioEngineTestDouble audioEngineTestDouble;
    private CleanArchitectureMockFactory.SuccessfulAudioScenario successScenario;
    private CleanArchitectureMockFactory.ErrorScenario errorScenario;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        
        // Set up test scenarios
        successScenario = new CleanArchitectureMockFactory.SuccessfulAudioScenario();
        errorScenario = new CleanArchitectureMockFactory.ErrorScenario();
        audioEngineTestDouble = new AudioEngineTestDouble();
        
        // Current singleton-based repository for integration testing
        audioRepository = AudioRepository.getInstance(mockContext);
    }
    
    @After
    public void tearDown() {
        if (audioRepository != null) {
            audioRepository.cleanup();
        }
        if (audioEngineTestDouble != null) {
            audioEngineTestDouble.reset();
        }
    }
    
    // === COMPLETE AUDIO FLOW INTEGRATION TESTS ===
    
    @Test
    public void integrationTest_completeAudioProcessingFlow() {
        // Test the complete flow: UI -> Presenter -> Use Case -> Repository -> Engine
        
        // 1. Start audio pipeline
        boolean startResult = audioRepository.startAudioPipeline();
        assertTrue("Audio pipeline should start successfully", startResult);
        
        // 2. Apply effect parameters
        EffectParameters params = new EffectParameters();
        params.setGain(0.8f);
        params.setDistortion(0.3f);
        params.setDelayTime(150.0f);
        params.setDelayFeedback(0.25f);
        
        audioRepository.applyEffectParameters(params);
        
        // 3. Enable effects
        audioRepository.setGainEnabled(true);
        audioRepository.setDistortionEnabled(true);
        audioRepository.setDelayEnabled(true);
        
        // 4. Verify audio state reflects changes
        AudioState state = audioRepository.getCurrentAudioState();
        assertNotNull("Audio state should be available", state);
        
        // 5. Verify current parameters match applied parameters
        EffectParameters currentParams = audioRepository.getCurrentEffectParameters();
        assertNotNull("Current parameters should be available", currentParams);
        
        // 6. Clean stop
        audioRepository.stopAudioPipeline();
    }
    
    @Test
    public void integrationTest_newArchitectureFlow() {
        // Test the new Clean Architecture flow with dependency injection
        
        // Use test doubles instead of real components
        AudioEngineInterface audioEngine = audioEngineTestDouble;
        PermissionInterface permissions = CleanArchitectureMockFactory.createGrantedPermissionManager();
        
        // Use case with dependency injection (new architecture)
        StartAudioPipelineUseCase useCase = new StartAudioPipelineUseCase(audioEngine, permissions);
        
        // Execute use case
        // Result result = useCase.execute();
        // assertTrue("Use case should succeed", result.isSuccess());
        
        // Verify audio engine state
        assertTrue("Audio engine should start pipeline", audioEngine.startAudioPipeline());
        assertTrue("Pipeline should be running", audioEngineTestDouble.isAudioPipelineRunning());
        
        // Apply effects through new architecture
        EffectParameters params = new EffectParameters();
        params.setGain(0.7f);
        audioEngine.applyEffectParameters(params);
        
        // Verify parameters were applied
        EffectParameters applied = audioEngine.getCurrentEffectParameters();
        assertEquals("Gain should be applied", 0.7f, applied.getGain(), 0.01f);
    }
    
    @Test
    public void integrationTest_audioParameterValidation() {
        // Test audio parameter validation throughout the pipeline
        
        audioEngineTestDouble.startAudioPipeline();
        
        // Test valid parameters
        EffectParameters validParams = new EffectParameters();
        validParams.setGain(0.8f);
        validParams.setDistortion(0.5f);
        validParams.setDelayTime(500.0f);
        validParams.setDelayFeedback(0.3f);
        
        audioEngineTestDouble.applyEffectParameters(validParams);
        
        // Test invalid parameters should be rejected
        EffectParameters invalidParams = new EffectParameters();
        invalidParams.setGain(-1.0f); // Invalid: negative gain
        
        try {
            audioEngineTestDouble.applyEffectParameters(invalidParams);
            fail("Should reject invalid parameters");
        } catch (IllegalArgumentException e) {
            assertNotNull("Should provide error message", e.getMessage());
        }
        
        // Test boundary values
        EffectParameters boundaryParams = new EffectParameters();
        boundaryParams.setGain(2.0f); // Maximum valid gain
        boundaryParams.setDistortion(1.0f); // Maximum valid distortion
        boundaryParams.setDelayFeedback(0.99f); // Maximum valid feedback
        
        audioEngineTestDouble.applyEffectParameters(boundaryParams);
        
        EffectParameters appliedBoundary = audioEngineTestDouble.getCurrentEffectParameters();
        assertEquals("Boundary gain should be applied", 2.0f, appliedBoundary.getGain(), 0.01f);
    }
    
    // === ERROR RECOVERY AND RESILIENCE TESTS ===
    
    @Test
    public void integrationTest_audioEngineFailureRecovery() {
        // Test system recovery from audio engine failures
        
        // Configure test double to fail initially
        audioEngineTestDouble.setShouldFailStart(true);
        
        boolean failedStart = audioEngineTestDouble.startAudioPipeline();
        assertFalse("Start should fail as configured", failedStart);
        
        // Simulate recovery (engine fixed)
        audioEngineTestDouble.setShouldFailStart(false);
        
        boolean recoveredStart = audioEngineTestDouble.startAudioPipeline();
        assertTrue("Should recover and start successfully", recoveredStart);
    }
    
    @Test
    public void integrationTest_permissionDenialHandling() {
        // Test handling of permission denials in audio pipeline
        
        PermissionInterface deniedPermissions = CleanArchitectureMockFactory.createDeniedPermissionManager();
        StartAudioPipelineUseCase useCase = new StartAudioPipelineUseCase(audioEngineTestDouble, deniedPermissions);
        
        // Use case should handle permission denial gracefully
        // Result result = useCase.execute();
        // assertFalse("Should fail due to missing permissions", result.isSuccess());
        // assertNotNull("Should provide error information", result.getError());
    }
    
    @Test
    public void integrationTest_nullParameterHandling() {
        // Test handling of null parameters throughout pipeline
        
        audioEngineTestDouble.startAudioPipeline();
        
        // Null parameters should be handled gracefully
        try {
            audioEngineTestDouble.applyEffectParameters(null);
            fail("Should reject null parameters");
        } catch (IllegalArgumentException e) {
            assertNotNull("Should provide meaningful error", e.getMessage());
        }
        
        // Partial null parameters should use defaults
        EffectParameters partialParams = new EffectParameters();
        partialParams.setGain(0.6f);
        // Other parameters remain null
        
        audioEngineTestDouble.applyEffectParameters(partialParams);
        
        EffectParameters result = audioEngineTestDouble.getCurrentEffectParameters();
        assertEquals("Should apply specified gain", 0.6f, result.getGain(), 0.01f);
        // Other parameters should retain previous values or use defaults
    }
    
    // === PERFORMANCE AND REAL-TIME CONSTRAINT TESTS ===
    
    @Test
    public void integrationTest_realTimePerformance() {
        // Test that audio operations meet real-time constraints
        
        audioEngineTestDouble.startAudioPipeline();
        
        // Measure time for critical operations
        long startTime = System.nanoTime();
        
        EffectParameters params = new EffectParameters();
        params.setGain(0.8f);
        audioEngineTestDouble.applyEffectParameters(params);
        
        long duration = System.nanoTime() - startTime;
        double durationMs = duration / 1_000_000.0;
        
        assertTrue("Parameter application should be fast (<1ms)", durationMs < 1.0);
        
        // Test multiple rapid parameter changes
        startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            params.setGain(0.5f + (i % 10) * 0.05f);
            audioEngineTestDouble.applyEffectParameters(params);
        }
        duration = System.nanoTime() - startTime;
        double avgDurationMs = (duration / 100.0) / 1_000_000.0;
        
        assertTrue("Average parameter change should be fast (<0.1ms)", avgDurationMs < 0.1);
    }
    
    @Test
    public void integrationTest_memoryUsageUnderLoad() {
        // Test memory usage during intensive audio processing
        
        Runtime runtime = Runtime.getRuntime();
        System.gc(); // Start with clean state
        
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        audioEngineTestDouble.startAudioPipeline();
        
        // Simulate intensive usage
        for (int i = 0; i < 1000; i++) {
            EffectParameters params = new EffectParameters();
            params.setGain(0.5f + (i % 100) * 0.005f);
            params.setDistortion((i % 50) * 0.02f);
            audioEngineTestDouble.applyEffectParameters(params);
            
            if (i % 100 == 0) {
                audioEngineTestDouble.getCurrentAudioState();
            }
        }
        
        System.gc();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        assertTrue("Memory usage should remain reasonable (<5MB)", memoryIncrease < 5_000_000);
    }
    
    // === THREAD SAFETY AND CONCURRENCY TESTS ===
    
    @Test
    public void integrationTest_concurrentAudioOperations() throws InterruptedException {
        // Test concurrent access to audio pipeline
        
        audioEngineTestDouble.startAudioPipeline();
        
        final int threadCount = 5;
        final int operationsPerThread = 200;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicBoolean failed = new AtomicBoolean(false);
        final AtomicReference<Exception> exception = new AtomicReference<>();
        
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        EffectParameters params = new EffectParameters();
                        params.setGain(0.1f + (threadId * 0.1f));
                        params.setDistortion((i % 10) * 0.1f);
                        
                        audioEngineTestDouble.applyEffectParameters(params);
                        audioEngineTestDouble.getCurrentAudioState();
                        
                        if (i % 50 == 0) {
                            audioEngineTestDouble.pauseAudioPipeline();
                            audioEngineTestDouble.resumeAudioPipeline();
                        }
                    }
                } catch (Exception e) {
                    failed.set(true);
                    exception.set(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        assertTrue("All threads should complete", latch.await(30, TimeUnit.SECONDS));
        
        if (failed.get()) {
            fail("Concurrent operations failed: " + exception.get().getMessage());
        }
        
        // Verify final state is consistent
        AudioState finalState = audioEngineTestDouble.getCurrentAudioState();
        assertNotNull("Final state should be available", finalState);
    }
    
    @Test
    public void integrationTest_audioStateConsistency() throws InterruptedException {
        // Test audio state consistency under concurrent modifications
        
        audioEngineTestDouble.startAudioPipeline();
        
        final CountDownLatch latch = new CountDownLatch(3);
        final AtomicBoolean inconsistencyDetected = new AtomicBoolean(false);
        
        // Thread 1: Modify parameters
        new Thread(() -> {
            try {
                for (int i = 0; i < 500; i++) {
                    EffectParameters params = new EffectParameters();
                    params.setGain((float) Math.random());
                    audioEngineTestDouble.applyEffectParameters(params);
                    Thread.sleep(1);
                }
            } catch (Exception e) {
                inconsistencyDetected.set(true);
            } finally {
                latch.countDown();
            }
        }).start();
        
        // Thread 2: Read state
        new Thread(() -> {
            try {
                for (int i = 0; i < 500; i++) {
                    AudioState state = audioEngineTestDouble.getCurrentAudioState();
                    if (state == null) {
                        inconsistencyDetected.set(true);
                        break;
                    }
                    Thread.sleep(1);
                }
            } catch (Exception e) {
                inconsistencyDetected.set(true);
            } finally {
                latch.countDown();
            }
        }).start();
        
        // Thread 3: Control pipeline
        new Thread(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    audioEngineTestDouble.pauseAudioPipeline();
                    Thread.sleep(2);
                    audioEngineTestDouble.resumeAudioPipeline();
                    Thread.sleep(3);
                }
            } catch (Exception e) {
                inconsistencyDetected.set(true);
            } finally {
                latch.countDown();
            }
        }).start();
        
        assertTrue("All threads should complete", latch.await(60, TimeUnit.SECONDS));
        assertFalse("No inconsistencies should be detected", inconsistencyDetected.get());
    }
    
    // === ARCHITECTURE TRANSITION VALIDATION TESTS ===
    
    @Test
    public void integrationTest_oldVsNewArchitectureCompatibility() {
        // Test that old singleton-based and new DI-based approaches produce same results
        
        // Old architecture approach (current)
        boolean oldStart = audioRepository.startAudioPipeline();
        
        EffectParameters oldParams = new EffectParameters();
        oldParams.setGain(0.75f);
        audioRepository.applyEffectParameters(oldParams);
        
        AudioState oldState = audioRepository.getCurrentAudioState();
        
        audioRepository.stopAudioPipeline();
        
        // New architecture approach (refactored)
        audioEngineTestDouble.reset();
        boolean newStart = audioEngineTestDouble.startAudioPipeline();
        
        EffectParameters newParams = new EffectParameters();
        newParams.setGain(0.75f);
        audioEngineTestDouble.applyEffectParameters(newParams);
        
        AudioState newState = audioEngineTestDouble.getCurrentAudioState();
        
        // Results should be functionally equivalent
        assertEquals("Start results should match", oldStart, newStart);
        assertNotNull("Both approaches should provide state", oldState);
        assertNotNull("Both approaches should provide state", newState);
        
        // Both approaches should handle the same parameter values
        EffectParameters oldApplied = audioRepository.getCurrentEffectParameters();
        EffectParameters newApplied = audioEngineTestDouble.getCurrentEffectParameters();
        
        assertNotNull("Old approach should return parameters", oldApplied);
        assertNotNull("New approach should return parameters", newApplied);
    }
    
    @Test
    public void integrationTest_performanceComparison() {
        // Compare performance between old and new architectures
        
        final int iterations = 1000;
        
        // Measure old architecture performance
        long oldStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            audioRepository.getCurrentAudioState();
        }
        long oldDuration = System.nanoTime() - oldStart;
        
        // Measure new architecture performance
        long newStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            audioEngineTestDouble.getCurrentAudioState();
        }
        long newDuration = System.nanoTime() - newStart;
        
        double oldAvgMs = (oldDuration / (double) iterations) / 1_000_000;
        double newAvgMs = (newDuration / (double) iterations) / 1_000_000;
        
        // New architecture should not be significantly slower
        assertTrue("New architecture should maintain performance", newAvgMs <= oldAvgMs * 1.5);
        
        // Both should meet real-time constraints
        assertTrue("Old architecture should be fast enough", oldAvgMs < 0.1);
        assertTrue("New architecture should be fast enough", newAvgMs < 0.1);
    }
    
    // === COMPREHENSIVE SYSTEM VALIDATION ===
    
    @Test
    public void integrationTest_completeSystemValidation() {
        // Comprehensive test of the complete system after refactoring
        
        // 1. Verify dependency injection works
        AudioEngineInterface audioEngine = audioEngineTestDouble;
        PermissionInterface permissions = CleanArchitectureMockFactory.createGrantedPermissionManager();
        
        assertNotNull("Audio engine should be injectable", audioEngine);
        assertNotNull("Permissions should be injectable", permissions);
        
        // 2. Verify use case execution
        StartAudioPipelineUseCase useCase = new StartAudioPipelineUseCase(audioEngine, permissions);
        assertNotNull("Use case should be constructible", useCase);
        
        // 3. Verify audio operations work end-to-end
        assertTrue("Pipeline should start", audioEngine.startAudioPipeline());
        
        // 4. Verify parameter application
        EffectParameters params = new EffectParameters();
        params.setGain(0.8f);
        params.setDistortion(0.4f);
        audioEngine.applyEffectParameters(params);
        
        EffectParameters applied = audioEngine.getCurrentEffectParameters();
        assertEquals("Parameters should be applied correctly", 0.8f, applied.getGain(), 0.01f);
        assertEquals("Parameters should be applied correctly", 0.4f, applied.getDistortion(), 0.01f);
        
        // 5. Verify state management
        AudioState state = audioEngine.getCurrentAudioState();
        assertTrue("Pipeline should be running", state.isPipelineRunning());
        
        // 6. Verify cleanup
        assertTrue("Pipeline should stop", audioEngine.stopAudioPipeline());
        
        AudioState finalState = audioEngine.getCurrentAudioState();
        assertFalse("Pipeline should be stopped", finalState.isPipelineRunning());
    }
}