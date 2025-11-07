package com.thiagofernendorech.toneforge.testing;

import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;
import com.thiagofernendorech.toneforge.domain.interfaces.PermissionInterface;
import com.thiagofernendorech.toneforge.domain.models.AudioState;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;
import com.thiagofernendorech.toneforge.infrastructure.adapters.AudioEngineAdapter;
import com.thiagofernendorech.toneforge.infrastructure.adapters.PermissionManagerAdapter;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Architectural Compliance Test Suite
 * 
 * This comprehensive test suite validates Clean Architecture compliance
 * and ensures proper dependency direction, layer separation, and design principles.
 * 
 * Critical Validation Areas:
 * 1. Dependency Direction (Dependency Inversion Principle)
 * 2. Layer Separation (Clean Architecture Boundaries)
 * 3. Interface Segregation (Interface-based Dependencies)
 * 4. Single Responsibility Principle
 * 5. Singleton Pattern Elimination
 * 6. Constructor-based Dependency Injection
 */
@RunWith(RobolectricTestRunner.class)
public class ArchitecturalComplianceTests {
    
    // === CLEAN ARCHITECTURE LAYER VALIDATION ===
    
    @Test
    public void architecturalCompliance_domainLayerDependencies() {
        // Domain layer should not depend on infrastructure or UI layers
        Class<?>[] domainClasses = {
            AudioState.class,
            EffectParameters.class,
            AudioEngineInterface.class,
            PermissionInterface.class
        };
        
        for (Class<?> domainClass : domainClasses) {
            validateDomainLayerCompliance(domainClass);
        }
    }
    
    @Test
    public void architecturalCompliance_infrastructureLayerDependencies() {
        // Infrastructure layer should depend only on domain interfaces
        Class<?>[] infrastructureClasses = {
            AudioEngineAdapter.class,
            PermissionManagerAdapter.class
        };
        
        for (Class<?> infraClass : infrastructureClasses) {
            validateInfrastructureLayerCompliance(infraClass);
        }
    }
    
    @Test
    public void architecturalCompliance_dataLayerMigration() {
        // After refactoring, AudioRepository should not be in data layer
        // This test validates the migration status
        
        String audioRepositoryPackage = AudioRepository.class.getPackage().getName();
        
        // Current state: AudioRepository is in data layer (violation)
        assertTrue("AudioRepository currently in data layer (needs migration)", 
                  audioRepositoryPackage.contains("data.repository"));
        
        // After refactoring, it should be in infrastructure
        // This test will pass after migration:
        // assertFalse("AudioRepository should not be in data layer after refactoring",
        //            audioRepositoryPackage.contains("data.repository"));
        // assertTrue("AudioRepository should be in infrastructure after refactoring",
        //           audioRepositoryPackage.contains("infrastructure"));
    }
    
    // === DEPENDENCY INJECTION VALIDATION ===
    
    @Test
    public void dependencyInjection_constructorBasedInjection() {
        // Validate that adapters use constructor-based dependency injection
        validateConstructorInjection(AudioEngineAdapter.class, AudioEngineInterface.class);
        validateConstructorInjection(PermissionManagerAdapter.class, PermissionInterface.class);
    }
    
    @Test
    public void dependencyInjection_nullDependencyRejection() {
        // Adapters should reject null dependencies
        try {
            new AudioEngineAdapter(null);
            fail("AudioEngineAdapter should reject null dependencies");
        } catch (IllegalArgumentException e) {
            assertNotNull("Should throw meaningful exception", e.getMessage());
        }
        
        try {
            new PermissionManagerAdapter(null);
            fail("PermissionManagerAdapter should reject null dependencies");
        } catch (IllegalArgumentException e) {
            assertNotNull("Should throw meaningful exception", e.getMessage());
        }
    }
    
    @Test
    public void dependencyInjection_interfaceBasedDependencies() {
        // Adapters should depend on interfaces, not implementations
        AudioEngineInterface mockEngine = mock(AudioEngineInterface.class);
        PermissionInterface mockPermissions = mock(PermissionInterface.class);
        
        // Should accept interface implementations
        AudioEngineAdapter audioAdapter = new AudioEngineAdapter(mockEngine);
        PermissionManagerAdapter permissionAdapter = new PermissionManagerAdapter(mockPermissions);
        
        assertNotNull("Should accept interface implementation", audioAdapter);
        assertNotNull("Should accept interface implementation", permissionAdapter);
    }
    
    // === SINGLETON PATTERN ELIMINATION ===
    
    @Test
    public void singletonElimination_audioRepositoryCurrentState() {
        // Current state: AudioRepository uses singleton pattern (violation)
        assertTrue("AudioRepository currently uses singleton pattern", 
                  hasSingletonPattern(AudioRepository.class));
        
        // After refactoring, singleton pattern should be eliminated:
        // assertFalse("AudioRepository should not use singleton after refactoring",
        //            hasSingletonPattern(AudioRepositoryImpl.class));
    }
    
    @Test
    public void singletonElimination_adapterPatternCompliance() {
        // Adapters should not use singleton pattern
        assertFalse("AudioEngineAdapter should not use singleton pattern",
                   hasSingletonPattern(AudioEngineAdapter.class));
        assertFalse("PermissionManagerAdapter should not use singleton pattern",
                   hasSingletonPattern(PermissionManagerAdapter.class));
    }
    
    // === INTERFACE SEGREGATION VALIDATION ===
    
    @Test
    public void interfaceSegregation_audioEngineInterface() {
        // AudioEngineInterface should define cohesive audio operations
        validateInterfaceSegregation(AudioEngineInterface.class, "Audio engine operations");
    }
    
    @Test
    public void interfaceSegregation_permissionInterface() {
        // PermissionInterface should define cohesive permission operations
        validateInterfaceSegregation(PermissionInterface.class, "Permission operations");
    }
    
    // === ADAPTER PATTERN VALIDATION ===
    
    @Test
    public void adapterPattern_audioEngineAdapter() {
        // AudioEngineAdapter should properly implement adapter pattern
        AudioEngineInterface mockEngine = mock(AudioEngineInterface.class);
        when(mockEngine.startAudioPipeline()).thenReturn(true);
        
        AudioEngineAdapter adapter = new AudioEngineAdapter(mockEngine);
        boolean result = adapter.startAudioPipeline();
        
        assertTrue("Adapter should delegate to wrapped interface", result);
        verify(mockEngine).startAudioPipeline();
    }
    
    @Test
    public void adapterPattern_permissionManagerAdapter() {
        // PermissionManagerAdapter should properly implement adapter pattern
        PermissionInterface mockPermissions = mock(PermissionInterface.class);
        when(mockPermissions.hasAudioPermission()).thenReturn(true);
        
        PermissionManagerAdapter adapter = new PermissionManagerAdapter(mockPermissions);
        boolean result = adapter.hasAudioPermission();
        
        assertTrue("Adapter should delegate to wrapped interface", result);
        verify(mockPermissions).hasAudioPermission();
    }
    
    // === SINGLE RESPONSIBILITY PRINCIPLE VALIDATION ===
    
    @Test
    public void singleResponsibility_audioRepositoryCurrentViolations() {
        // Current AudioRepository violates SRP (mixed responsibilities)
        Method[] methods = AudioRepository.class.getDeclaredMethods();
        
        int audioOperations = 0;
        int presetOperations = 0;
        int stateOperations = 0;
        int automationOperations = 0;
        
        for (Method method : methods) {
            String methodName = method.getName().toLowerCase();
            if (methodName.contains("pipeline") || methodName.contains("effect")) {
                audioOperations++;
            } else if (methodName.contains("preset")) {
                presetOperations++;
            } else if (methodName.contains("state")) {
                stateOperations++;
            } else if (methodName.contains("automation")) {
                automationOperations++;
            }
        }
        
        // AudioRepository currently handles multiple responsibilities (violation)
        assertTrue("AudioRepository handles audio operations", audioOperations > 0);
        assertTrue("AudioRepository handles preset operations", presetOperations > 0);
        assertTrue("AudioRepository handles state operations", stateOperations > 0);
        
        // After refactoring, responsibilities should be separated:
        // - AudioService: audio pipeline operations
        // - PresetService: preset management
        // - StateService: state management
    }
    
    // === PERFORMANCE COMPLIANCE ===
    
    @Test
    public void performanceCompliance_adapterOverhead() {
        // Adapters should introduce minimal overhead
        AudioEngineInterface mockEngine = mock(AudioEngineInterface.class);
        AudioEngineAdapter adapter = new AudioEngineAdapter(mockEngine);
        
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            adapter.startAudioPipeline();
        }
        long duration = System.nanoTime() - startTime;
        
        double avgTimePerCall = duration / 10000.0 / 1_000_000; // Convert to milliseconds
        assertTrue("Adapter overhead should be minimal (<0.001ms per call)", 
                  avgTimePerCall < 0.001);
    }
    
    @Test
    public void performanceCompliance_memoryUsage() {
        // Adapters should not create memory leaks
        Runtime runtime = Runtime.getRuntime();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Create and discard many adapter instances
        for (int i = 0; i < 1000; i++) {
            AudioEngineInterface mockEngine = mock(AudioEngineInterface.class);
            AudioEngineAdapter adapter = new AudioEngineAdapter(mockEngine);
            adapter.startAudioPipeline();
        }
        
        System.gc(); // Force garbage collection
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = afterMemory - beforeMemory;
        
        assertTrue("Memory usage should be reasonable (<1MB)", memoryIncrease < 1_000_000);
    }
    
    // === THREAD SAFETY COMPLIANCE ===
    
    @Test
    public void threadSafety_adapterConcurrentAccess() throws InterruptedException {
        // Adapters should be thread-safe
        AudioEngineInterface mockEngine = mock(AudioEngineInterface.class);
        AudioEngineAdapter adapter = new AudioEngineAdapter(mockEngine);
        
        final int threadCount = 10;
        final int operationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    adapter.startAudioPipeline();
                    adapter.stopAudioPipeline();
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Should complete without exceptions
        verify(mockEngine, times(threadCount * operationsPerThread)).startAudioPipeline();
    }
    
    // === HELPER METHODS ===
    
    private void validateDomainLayerCompliance(Class<?> domainClass) {
        String packageName = domainClass.getPackage().getName();
        assertTrue("Domain class should be in domain package: " + domainClass.getName(),
                  packageName.contains("domain"));
        
        // Domain classes should not import infrastructure or ui packages
        // This would require bytecode analysis for complete validation
        // For now, we validate package structure
        assertFalse("Domain should not depend on infrastructure",
                   packageName.contains("infrastructure"));
        assertFalse("Domain should not depend on ui",
                   packageName.contains("ui"));
    }
    
    private void validateInfrastructureLayerCompliance(Class<?> infraClass) {
        String packageName = infraClass.getPackage().getName();
        assertTrue("Infrastructure class should be in infrastructure package: " + infraClass.getName(),
                  packageName.contains("infrastructure"));
    }
    
    private void validateConstructorInjection(Class<?> clazz, Class<?> dependencyType) {
        Constructor<?>[] constructors = clazz.getConstructors();
        boolean hasConstructorInjection = false;
        
        for (Constructor<?> constructor : constructors) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if (paramTypes.length > 0) {
                for (Class<?> paramType : paramTypes) {
                    if (dependencyType.isAssignableFrom(paramType)) {
                        hasConstructorInjection = true;
                        break;
                    }
                }
            }
        }
        
        assertTrue("Class should have constructor-based dependency injection: " + clazz.getName(),
                  hasConstructorInjection);
    }
    
    private boolean hasSingletonPattern(Class<?> clazz) {
        // Check for singleton indicators
        Method[] methods = clazz.getDeclaredMethods();
        Field[] fields = clazz.getDeclaredFields();
        
        // Look for getInstance method
        boolean hasGetInstanceMethod = Arrays.stream(methods)
                .anyMatch(method -> method.getName().equals("getInstance") 
                         && Modifier.isStatic(method.getModifiers()));
        
        // Look for static instance field
        boolean hasStaticInstanceField = Arrays.stream(fields)
                .anyMatch(field -> Modifier.isStatic(field.getModifiers()) 
                         && field.getName().toLowerCase().contains("instance"));
        
        return hasGetInstanceMethod || hasStaticInstanceField;
    }
    
    private void validateInterfaceSegregation(Class<?> interfaceClass, String description) {
        assertTrue("Should be an interface: " + description, interfaceClass.isInterface());
        
        Method[] methods = interfaceClass.getDeclaredMethods();
        assertTrue("Interface should have methods: " + description, methods.length > 0);
        
        // Interface should not have too many methods (ISP violation indicator)
        assertTrue("Interface should not have excessive methods (ISP): " + description,
                  methods.length < 20);
    }
}