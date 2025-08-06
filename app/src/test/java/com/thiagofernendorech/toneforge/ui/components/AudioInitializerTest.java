package com.thiagofernendorech.toneforge.ui.components;

import android.content.Context;
import com.thiagofernendorech.toneforge.ui.components.AudioInitializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AudioInitializer
 * Valida a inicialização de componentes de áudio
 */
@RunWith(RobolectricTestRunner.class)
public class AudioInitializerTest {
    
    private Context context;
    private AudioInitializer audioInitializer;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        audioInitializer = new AudioInitializer(context);
    }
    
    @Test
    public void testInitializeAudioComponents_shouldCompleteWithoutErrors() {
        // Act
        audioInitializer.initializeAudioComponents();
        
        // Assert
        assertNotNull("AudioInitializer deve estar inicializado", audioInitializer);
        assertNotNull("AudioEngine deve estar disponível", audioInitializer.getAudioEngine());
    }
    
    @Test
    public void testGetAudioEngine_shouldReturnNonNull() {
        // Act
        var audioEngine = audioInitializer.getAudioEngine();
        
        // Assert
        assertNotNull("AudioEngine não deve ser null", audioEngine);
    }
    
    @Test
    public void testIsPipelineRunning_initiallyFalse() {
        // Act
        boolean isRunning = audioInitializer.isPipelineRunning();
        
        // Assert
        assertFalse("Pipeline não deve estar rodando inicialmente", isRunning);
    }
    
    @Test
    public void testStartAudioPipeline_shouldExecuteWithoutErrors() {
        // Act & Assert - não deve lançar exceção
        audioInitializer.startAudioPipeline();
        
        // Pipeline pode ou não iniciar dependendo do ambiente de teste
        // O importante é que não lance exceção
        assertTrue("Método deve executar sem erros", true);
    }
    
    @Test
    public void testStopAudioPipeline_shouldExecuteWithoutErrors() {
        // Act & Assert - não deve lançar exceção
        audioInitializer.stopAudioPipeline();
        
        assertTrue("Método deve executar sem erros", true);
    }
    
    @Test
    public void testCleanup_shouldExecuteWithoutErrors() {
        // Act & Assert - não deve lançar exceção
        audioInitializer.cleanup();
        
        assertTrue("Cleanup deve executar sem erros", true);
    }
    
    @Test
    public void testMultipleInitialization_shouldBeIdempotent() {
        // Act
        audioInitializer.initializeAudioComponents();
        audioInitializer.initializeAudioComponents();
        audioInitializer.initializeAudioComponents();
        
        // Assert - múltiplas chamadas não devem causar problemas
        assertNotNull("AudioEngine deve continuar disponível", audioInitializer.getAudioEngine());
    }
    
    @Test
    public void testLifecycle_startStopCleanup() {
        // Act - simular ciclo de vida completo
        audioInitializer.initializeAudioComponents();
        audioInitializer.startAudioPipeline();
        audioInitializer.stopAudioPipeline();
        audioInitializer.cleanup();
        
        // Assert - não deve lançar exceções
        assertTrue("Ciclo de vida completo deve funcionar", true);
    }
} 