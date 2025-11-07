package com.thiagofernendorech.toneforge.testing;

import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;
import com.thiagofernendorech.toneforge.domain.models.AudioState;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test Double Implementation of AudioEngineInterface
 * 
 * This test double provides a realistic implementation of the audio engine
 * for testing purposes without requiring actual audio hardware or native libraries.
 * 
 * Features:
 * - Thread-safe state management
 * - Realistic audio parameter validation
 * - Configurable failure modes for error testing
 * - Performance monitoring capabilities
 * - Memory-efficient operation
 */
public class AudioEngineTestDouble implements AudioEngineInterface {
    
    // === STATE MANAGEMENT ===
    
    private final AtomicBoolean pipelineRunning = new AtomicBoolean(false);
    private final AtomicBoolean pipelinePaused = new AtomicBoolean(false);
    private final AtomicInteger sampleRate = new AtomicInteger(44100);
    private final AtomicInteger bufferSize = new AtomicInteger(512);
    
    // === EFFECT PARAMETERS ===
    
    private volatile float gain = 0.5f;
    private volatile float distortion = 0.0f;
    private volatile float delayTime = 0.0f;
    private volatile float delayFeedback = 0.0f;
    private volatile float reverbRoomSize = 0.0f;
    private volatile float reverbDamping = 0.0f;
    
    // === EFFECT ENABLE STATES ===
    
    private volatile boolean gainEnabled = false;
    private volatile boolean distortionEnabled = false;
    private volatile boolean delayEnabled = false;
    private volatile boolean reverbEnabled = false;
    
    // === CONFIGURATION FLAGS ===
    
    private volatile boolean shouldFailStart = false;
    private volatile boolean shouldFailStop = false;
    private volatile boolean shouldThrowExceptions = false;
    private volatile long operationDelayMs = 0;
    
    // === PERFORMANCE MONITORING ===
    
    private volatile long totalOperations = 0;
    private volatile long totalProcessingTime = 0;
    private volatile long lastOperationTime = 0;
    
    // === CONSTRUCTOR ===
    
    public AudioEngineTestDouble() {
        // Initialize with safe defaults
    }
    
    // === AUDIO PIPELINE OPERATIONS ===
    
    @Override
    public boolean startAudioPipeline() {
        recordOperation();
        simulateDelay();
        
        if (shouldThrowExceptions) {
            throw new RuntimeException("Simulated audio engine start failure");
        }
        
        if (shouldFailStart) {
            return false;
        }
        
        if (pipelineRunning.compareAndSet(false, true)) {
            pipelinePaused.set(false);
            return true;
        }
        
        // Already running
        return true;
    }
    
    @Override
    public boolean stopAudioPipeline() {
        recordOperation();
        simulateDelay();
        
        if (shouldThrowExceptions) {
            throw new RuntimeException("Simulated audio engine stop failure");
        }
        
        if (shouldFailStop) {
            return false;
        }
        
        pipelineRunning.set(false);
        pipelinePaused.set(false);
        return true;
    }
    
    @Override
    public boolean isAudioPipelineRunning() {
        recordOperation();
        
        if (shouldThrowExceptions) {
            throw new RuntimeException("Simulated audio engine query failure");
        }
        
        return pipelineRunning.get() && !pipelinePaused.get();
    }
    
    @Override
    public boolean isPipelinePaused() {
        recordOperation();
        return pipelinePaused.get();
    }
    
    @Override
    public void pauseAudioPipeline() {
        recordOperation();
        simulateDelay();
        
        if (pipelineRunning.get()) {
            pipelinePaused.set(true);
        }
    }
    
    @Override
    public void resumeAudioPipeline() {
        recordOperation();
        simulateDelay();
        
        if (pipelineRunning.get()) {
            pipelinePaused.set(false);
        }
    }
    
    // === EFFECT PARAMETERS ===
    
    @Override
    public EffectParameters getCurrentEffectParameters() {
        recordOperation();
        
        if (shouldThrowExceptions) {
            throw new RuntimeException("Simulated parameter retrieval failure");
        }
        
        EffectParameters params = new EffectParameters();
        params.setGain(gain);
        params.setDistortion(distortion);
        params.setDelayTime(delayTime);
        params.setDelayFeedback(delayFeedback);
        params.setReverbRoomSize(reverbRoomSize);
        params.setReverbDamping(reverbDamping);
        
        return params;
    }
    
    @Override
    public void applyEffectParameters(EffectParameters parameters) {
        recordOperation();
        simulateDelay();
        
        if (shouldThrowExceptions) {
            throw new RuntimeException("Simulated parameter application failure");
        }
        
        if (parameters == null) {
            throw new IllegalArgumentException("Effect parameters cannot be null");
        }
        
        // Apply parameters with validation
        if (parameters.getGain() != null) {
            float gainValue = parameters.getGain();
            if (gainValue < 0.0f || gainValue > 2.0f) {
                throw new IllegalArgumentException("Gain must be between 0.0 and 2.0");
            }
            this.gain = gainValue;
        }
        
        if (parameters.getDistortion() != null) {
            float distortionValue = parameters.getDistortion();
            if (distortionValue < 0.0f || distortionValue > 1.0f) {
                throw new IllegalArgumentException("Distortion must be between 0.0 and 1.0");
            }
            this.distortion = distortionValue;
        }
        
        if (parameters.getDelayTime() != null) {
            float delayTimeValue = parameters.getDelayTime();
            if (delayTimeValue < 0.0f || delayTimeValue > 2000.0f) {
                throw new IllegalArgumentException("Delay time must be between 0.0 and 2000.0 ms");
            }
            this.delayTime = delayTimeValue;
        }
        
        if (parameters.getDelayFeedback() != null) {
            float feedbackValue = parameters.getDelayFeedback();
            if (feedbackValue < 0.0f || feedbackValue > 0.99f) {
                throw new IllegalArgumentException("Delay feedback must be between 0.0 and 0.99");
            }
            this.delayFeedback = feedbackValue;
        }
        
        if (parameters.getReverbRoomSize() != null) {
            float roomSizeValue = parameters.getReverbRoomSize();
            if (roomSizeValue < 0.0f || roomSizeValue > 1.0f) {
                throw new IllegalArgumentException("Reverb room size must be between 0.0 and 1.0");
            }
            this.reverbRoomSize = roomSizeValue;
        }
        
        if (parameters.getReverbDamping() != null) {
            float dampingValue = parameters.getReverbDamping();
            if (dampingValue < 0.0f || dampingValue > 1.0f) {
                throw new IllegalArgumentException("Reverb damping must be between 0.0 and 1.0");
            }
            this.reverbDamping = dampingValue;
        }
    }
    
    // === EFFECT ENABLE/DISABLE ===
    
    @Override
    public void setGainEnabled(boolean enabled) {
        recordOperation();
        this.gainEnabled = enabled;
    }
    
    @Override
    public void setDistortionEnabled(boolean enabled) {
        recordOperation();
        this.distortionEnabled = enabled;
    }
    
    @Override
    public void setDelayEnabled(boolean enabled) {
        recordOperation();
        this.delayEnabled = enabled;
    }
    
    @Override
    public void setReverbEnabled(boolean enabled) {
        recordOperation();
        this.reverbEnabled = enabled;
    }
    
    // === AUDIO STATE ===
    
    @Override
    public AudioState getCurrentAudioState() {
        recordOperation();
        
        if (shouldThrowExceptions) {
            throw new RuntimeException("Simulated audio state retrieval failure");
        }
        
        AudioState state = new AudioState();
        state.setPipelineRunning(pipelineRunning.get());
        state.setPipelinePaused(pipelinePaused.get());
        state.setCurrentLatencyMode(1); // Balanced mode
        state.setOversamplingEnabled(false);
        state.setOversamplingFactor(1);
        state.setSampleRate(sampleRate.get());
        state.setBufferSize(bufferSize.get());
        state.setBackgroundAudioEnabled(false);
        state.setMidiEnabled(false);
        state.setTunerActive(false);
        state.setMetronomeActive(false);
        state.setLooperRecording(false);
        state.setLooperPlaying(false);
        state.setAutomationRecording(false);
        state.setAutomationPlaying(false);
        state.setErrorCount(0);
        state.setLastError(null);
        state.setUptime(System.currentTimeMillis() - lastOperationTime);
        state.setTotalSamplesProcessed(totalOperations * bufferSize.get());
        
        return state;
    }
    
    // === TEST CONFIGURATION METHODS ===
    
    /**
     * Configure the test double to simulate start failures
     */
    public void setShouldFailStart(boolean shouldFail) {
        this.shouldFailStart = shouldFail;
    }
    
    /**
     * Configure the test double to simulate stop failures
     */
    public void setShouldFailStop(boolean shouldFail) {
        this.shouldFailStop = shouldFail;
    }
    
    /**
     * Configure the test double to throw exceptions
     */
    public void setShouldThrowExceptions(boolean shouldThrow) {
        this.shouldThrowExceptions = shouldThrow;
    }
    
    /**
     * Configure operation delay for performance testing
     */
    public void setOperationDelay(long delayMs) {
        this.operationDelayMs = delayMs;
    }
    
    /**
     * Reset the test double to initial state
     */
    public void reset() {
        pipelineRunning.set(false);
        pipelinePaused.set(false);
        gain = 0.5f;
        distortion = 0.0f;
        delayTime = 0.0f;
        delayFeedback = 0.0f;
        reverbRoomSize = 0.0f;
        reverbDamping = 0.0f;
        gainEnabled = false;
        distortionEnabled = false;
        delayEnabled = false;
        reverbEnabled = false;
        shouldFailStart = false;
        shouldFailStop = false;
        shouldThrowExceptions = false;
        operationDelayMs = 0;
        totalOperations = 0;
        totalProcessingTime = 0;
        lastOperationTime = System.currentTimeMillis();
    }
    
    // === PERFORMANCE MONITORING ===
    
    /**
     * Get total number of operations performed
     */
    public long getTotalOperations() {
        return totalOperations;
    }
    
    /**
     * Get average processing time per operation
     */
    public double getAverageProcessingTime() {
        return totalOperations == 0 ? 0 : (double) totalProcessingTime / totalOperations;
    }
    
    /**
     * Get current effect enable states for testing
     */
    public boolean isGainEnabled() { return gainEnabled; }
    public boolean isDistortionEnabled() { return distortionEnabled; }
    public boolean isDelayEnabled() { return delayEnabled; }
    public boolean isReverbEnabled() { return reverbEnabled; }
    
    /**
     * Get current parameter values for testing
     */
    public float getCurrentGain() { return gain; }
    public float getCurrentDistortion() { return distortion; }
    public float getCurrentDelayTime() { return delayTime; }
    public float getCurrentDelayFeedback() { return delayFeedback; }
    public float getCurrentReverbRoomSize() { return reverbRoomSize; }
    public float getCurrentReverbDamping() { return reverbDamping; }
    
    // === PRIVATE HELPER METHODS ===
    
    private void recordOperation() {
        totalOperations++;
        lastOperationTime = System.currentTimeMillis();
    }
    
    private void simulateDelay() {
        if (operationDelayMs > 0) {
            try {
                long startTime = System.currentTimeMillis();
                Thread.sleep(operationDelayMs);
                totalProcessingTime += (System.currentTimeMillis() - startTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}