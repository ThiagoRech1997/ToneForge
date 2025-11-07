package com.thiagofernendorech.toneforge.testing;

import android.content.Context;
import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;
import com.thiagofernendorech.toneforge.domain.interfaces.PermissionInterface;
import com.thiagofernendorech.toneforge.domain.models.AudioState;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.ui.navigation.NavigationController;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

/**
 * Clean Architecture Mock Factory for Dependency Injection Testing
 * 
 * This factory provides pre-configured mocks for all major interfaces and components
 * to support testing during the architecture refactoring process.
 * 
 * Features:
 * - Interface-based mocks for Clean Architecture compliance
 * - Realistic default behaviors for audio components
 * - Error scenario mocks for negative testing
 * - Thread-safe mock configurations
 */
public class CleanArchitectureMockFactory {
    
    // === AUDIO ENGINE MOCKS ===
    
    /**
     * Creates a mock AudioEngineInterface with working default behaviors
     * Suitable for positive test scenarios
     */
    public static AudioEngineInterface createWorkingAudioEngine() {
        AudioEngineInterface mock = mock(AudioEngineInterface.class);
        
        // Default successful behaviors
        when(mock.startAudioPipeline()).thenReturn(true);
        when(mock.stopAudioPipeline()).thenReturn(true);
        when(mock.isAudioPipelineRunning()).thenReturn(false);
        when(mock.isPipelinePaused()).thenReturn(false);
        
        // Default audio parameters
        EffectParameters defaultParams = new EffectParameters();
        defaultParams.setGain(0.5f);
        defaultParams.setDistortion(0.0f);
        defaultParams.setDelayTime(0.0f);
        defaultParams.setDelayFeedback(0.0f);
        defaultParams.setReverbRoomSize(0.0f);
        defaultParams.setReverbDamping(0.0f);
        when(mock.getCurrentEffectParameters()).thenReturn(defaultParams);
        
        // Default audio state
        AudioState defaultState = new AudioState();
        defaultState.setPipelineRunning(false);
        defaultState.setPipelinePaused(false);
        defaultState.setCurrentLatencyMode(1);
        defaultState.setOversamplingEnabled(false);
        when(mock.getCurrentAudioState()).thenReturn(defaultState);
        
        // Effect enable/disable
        doNothing().when(mock).setGainEnabled(anyBoolean());
        doNothing().when(mock).setDistortionEnabled(anyBoolean());
        doNothing().when(mock).setDelayEnabled(anyBoolean());
        doNothing().when(mock).setReverbEnabled(anyBoolean());
        
        return mock;
    }
    
    /**
     * Creates a mock AudioEngineInterface that fails operations
     * Suitable for error scenario testing
     */
    public static AudioEngineInterface createFailingAudioEngine() {
        AudioEngineInterface mock = mock(AudioEngineInterface.class);
        
        // Failing behaviors
        when(mock.startAudioPipeline()).thenReturn(false);
        when(mock.stopAudioPipeline()).thenReturn(false);
        when(mock.isAudioPipelineRunning()).thenThrow(new RuntimeException("Audio engine error"));
        
        // Return null/empty for error scenarios
        when(mock.getCurrentEffectParameters()).thenReturn(null);
        when(mock.getCurrentAudioState()).thenReturn(null);
        
        return mock;
    }
    
    /**
     * Creates a mock AudioEngineInterface with slow responses
     * Suitable for performance testing
     */
    public static AudioEngineInterface createSlowAudioEngine() {
        AudioEngineInterface mock = mock(AudioEngineInterface.class);
        
        // Slow responses
        when(mock.startAudioPipeline()).thenAnswer(invocation -> {
            Thread.sleep(100); // 100ms delay
            return true;
        });
        
        when(mock.getCurrentAudioState()).thenAnswer(invocation -> {
            Thread.sleep(50); // 50ms delay
            return new AudioState();
        });
        
        return mock;
    }
    
    // === PERMISSION MANAGER MOCKS ===
    
    /**
     * Creates a mock PermissionInterface with all permissions granted
     */
    public static PermissionInterface createGrantedPermissionManager() {
        PermissionInterface mock = mock(PermissionInterface.class);
        
        when(mock.hasAudioPermission()).thenReturn(true);
        when(mock.hasMicrophonePermission()).thenReturn(true);
        when(mock.hasStoragePermission()).thenReturn(true);
        
        doNothing().when(mock).requestAudioPermission();
        doNothing().when(mock).requestMicrophonePermission();
        doNothing().when(mock).requestStoragePermission();
        
        return mock;
    }
    
    /**
     * Creates a mock PermissionInterface with all permissions denied
     */
    public static PermissionInterface createDeniedPermissionManager() {
        PermissionInterface mock = mock(PermissionInterface.class);
        
        when(mock.hasAudioPermission()).thenReturn(false);
        when(mock.hasMicrophonePermission()).thenReturn(false);
        when(mock.hasStoragePermission()).thenReturn(false);
        
        return mock;
    }
    
    /**
     * Creates a mock PermissionInterface with mixed permission states
     */
    public static PermissionInterface createMixedPermissionManager() {
        PermissionInterface mock = mock(PermissionInterface.class);
        
        when(mock.hasAudioPermission()).thenReturn(true);
        when(mock.hasMicrophonePermission()).thenReturn(false);
        when(mock.hasStoragePermission()).thenReturn(true);
        
        return mock;
    }
    
    // === AUDIO REPOSITORY MOCKS ===
    
    /**
     * Creates a mock AudioRepository with comprehensive default behaviors
     * Maintains compatibility with existing singleton pattern during transition
     */
    public static AudioRepository createMockAudioRepository() {
        AudioRepository mock = mock(AudioRepository.class);
        
        // Audio pipeline operations
        when(mock.startAudioPipeline()).thenReturn(true);
        when(mock.isAudioPipelineRunning()).thenReturn(false);
        doNothing().when(mock).stopAudioPipeline();
        doNothing().when(mock).pauseAudioPipeline();
        doNothing().when(mock).resumeAudioPipeline();
        
        // Effect parameters
        EffectParameters defaultParams = new EffectParameters();
        defaultParams.setGain(0.5f);
        when(mock.getCurrentEffectParameters()).thenReturn(defaultParams);
        doNothing().when(mock).applyEffectParameters(any(EffectParameters.class));
        
        // Effect enable/disable
        doNothing().when(mock).setGainEnabled(anyBoolean());
        doNothing().when(mock).setDistortionEnabled(anyBoolean());
        doNothing().when(mock).setDelayEnabled(anyBoolean());
        doNothing().when(mock).setReverbEnabled(anyBoolean());
        
        // Audio state
        AudioState defaultState = new AudioState();
        when(mock.getCurrentAudioState()).thenReturn(defaultState);
        doNothing().when(mock).saveCurrentState();
        doNothing().when(mock).restoreState();
        
        // Latency management
        when(mock.getCurrentLatencyMode()).thenReturn(1);
        when(mock.getEstimatedLatency()).thenReturn(10.0f);
        doNothing().when(mock).setLatencyMode(anyInt());
        
        // MIDI
        when(mock.isMidiEnabled()).thenReturn(false);
        doNothing().when(mock).setMidiEnabled(anyBoolean());
        
        // Cleanup
        doNothing().when(mock).cleanup();
        
        return mock;
    }
    
    // === NAVIGATION CONTROLLER MOCKS ===
    
    /**
     * Creates a mock NavigationController for testing navigation flows
     */
    public static NavigationController createMockNavigationController() {
        NavigationController mock = mock(NavigationController.class);
        
        doNothing().when(mock).navigateToHome();
        doNothing().when(mock).navigateToEffects();
        doNothing().when(mock).navigateToLooper();
        doNothing().when(mock).navigateToTuner();
        doNothing().when(mock).navigateToMetronome();
        doNothing().when(mock).navigateToRecorder();
        doNothing().when(mock).navigateToSettings();
        doNothing().when(mock).navigateToLearning();
        doNothing().when(mock).navigateToLoopLibrary();
        
        return mock;
    }
    
    // === CONTEXT MOCKS ===
    
    /**
     * Creates a mock Android Context for testing
     */
    public static Context createMockContext() {
        Context mock = mock(Context.class);
        when(mock.getApplicationContext()).thenReturn(mock);
        return mock;
    }
    
    // === SCENARIO-BASED MOCK COMBINATIONS ===
    
    /**
     * Mock configuration for successful audio processing scenarios
     */
    public static class SuccessfulAudioScenario {
        public final AudioEngineInterface audioEngine;
        public final PermissionInterface permissions;
        public final AudioRepository repository;
        public final Context context;
        
        public SuccessfulAudioScenario() {
            this.audioEngine = createWorkingAudioEngine();
            this.permissions = createGrantedPermissionManager();
            this.repository = createMockAudioRepository();
            this.context = createMockContext();
        }
    }
    
    /**
     * Mock configuration for error scenarios
     */
    public static class ErrorScenario {
        public final AudioEngineInterface audioEngine;
        public final PermissionInterface permissions;
        public final AudioRepository repository;
        public final Context context;
        
        public ErrorScenario() {
            this.audioEngine = createFailingAudioEngine();
            this.permissions = createDeniedPermissionManager();
            this.repository = createMockAudioRepository();
            this.context = createMockContext();
        }
    }
    
    /**
     * Mock configuration for performance testing scenarios
     */
    public static class PerformanceScenario {
        public final AudioEngineInterface audioEngine;
        public final PermissionInterface permissions;
        public final AudioRepository repository;
        public final Context context;
        
        public PerformanceScenario() {
            this.audioEngine = createSlowAudioEngine();
            this.permissions = createGrantedPermissionManager();
            this.repository = createMockAudioRepository();
            this.context = createMockContext();
        }
    }
    
    /**
     * Mock configuration for mixed permission scenarios
     */
    public static class MixedPermissionScenario {
        public final AudioEngineInterface audioEngine;
        public final PermissionInterface permissions;
        public final AudioRepository repository;
        public final Context context;
        
        public MixedPermissionScenario() {
            this.audioEngine = createWorkingAudioEngine();
            this.permissions = createMixedPermissionManager();
            this.repository = createMockAudioRepository();
            this.context = createMockContext();
        }
    }
    
    // === HELPER METHODS ===
    
    /**
     * Resets all mocks in a scenario to clear previous interactions
     */
    public static void resetScenario(Object scenario) {
        if (scenario instanceof SuccessfulAudioScenario) {
            SuccessfulAudioScenario s = (SuccessfulAudioScenario) scenario;
            Mockito.reset(s.audioEngine, s.permissions, s.repository, s.context);
        } else if (scenario instanceof ErrorScenario) {
            ErrorScenario s = (ErrorScenario) scenario;
            Mockito.reset(s.audioEngine, s.permissions, s.repository, s.context);
        }
        // Add other scenario types as needed
    }
    
    /**
     * Configures a mock AudioRepository with specific behaviors
     */
    public static void configureAudioRepository(AudioRepository mockRepository, 
                                              boolean pipelineStartSuccess,
                                              boolean isRunning) {
        when(mockRepository.startAudioPipeline()).thenReturn(pipelineStartSuccess);
        when(mockRepository.isAudioPipelineRunning()).thenReturn(isRunning);
    }
    
    /**
     * Configures a mock AudioEngineInterface with specific audio parameters
     */
    public static void configureAudioParameters(AudioEngineInterface mockEngine,
                                               float gain, float distortion,
                                               float delayTime, float reverbRoom) {
        EffectParameters params = new EffectParameters();
        params.setGain(gain);
        params.setDistortion(distortion);
        params.setDelayTime(delayTime);
        params.setReverbRoomSize(reverbRoom);
        
        when(mockEngine.getCurrentEffectParameters()).thenReturn(params);
    }
}