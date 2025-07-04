package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.SharedPreferences;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ToneForge app components.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class ExampleUnitTest {
    
    @Mock
    private Context mockContext;
    
    @Mock
    private SharedPreferences mockPrefs;
    
    @Mock
    private SharedPreferences.Editor mockEditor;
    
    @Before
    public void setUp() {
        // Configurar mocks para SharedPreferences
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getSharedPreferences("latency_prefs", Context.MODE_PRIVATE)).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putInt("latency_mode", 1)).thenReturn(mockEditor);
        when(mockEditor.apply()).thenReturn();
        when(mockPrefs.getInt("latency_mode", 1)).thenReturn(1);
    }
    
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    
    @Test
    public void testLatencyManager_LowLatencyMode() {
        // Teste do modo de baixa latência
        LatencyManager latencyManager = LatencyManager.getInstance(mockContext);
        latencyManager.setLatencyMode(LatencyManager.MODE_LOW_LATENCY);
        
        assertEquals(LatencyManager.MODE_LOW_LATENCY, latencyManager.getCurrentMode());
        assertEquals("Baixa Latência", latencyManager.getModeName(LatencyManager.MODE_LOW_LATENCY));
        assertEquals(512, latencyManager.getBufferSize());
        assertEquals(48000, latencyManager.getSampleRate());
        assertFalse(latencyManager.isAutoOversamplingEnabled());
        assertEquals(1, latencyManager.getOversamplingFactor());
        
        // Verificar cálculo de latência: (512 / 48000) * 1000 * 2 = 21.33ms
        float expectedLatency = (float) 512 / 48000 * 1000 * 2;
        assertEquals(expectedLatency, latencyManager.getEstimatedLatency(), 0.1f);
    }
    
    @Test
    public void testLatencyManager_BalancedMode() {
        // Teste do modo equilibrado
        LatencyManager latencyManager = LatencyManager.getInstance(mockContext);
        latencyManager.setLatencyMode(LatencyManager.MODE_BALANCED);
        
        assertEquals(LatencyManager.MODE_BALANCED, latencyManager.getCurrentMode());
        assertEquals("Equilibrado", latencyManager.getModeName(LatencyManager.MODE_BALANCED));
        assertEquals(1024, latencyManager.getBufferSize());
        assertEquals(48000, latencyManager.getSampleRate());
        assertTrue(latencyManager.isAutoOversamplingEnabled());
        assertEquals(2, latencyManager.getOversamplingFactor());
        
        // Verificar cálculo de latência: (1024 / 48000) * 1000 * 2 = 42.67ms
        float expectedLatency = (float) 1024 / 48000 * 1000 * 2;
        assertEquals(expectedLatency, latencyManager.getEstimatedLatency(), 0.1f);
    }
    
    @Test
    public void testLatencyManager_StabilityMode() {
        // Teste do modo de estabilidade
        LatencyManager latencyManager = LatencyManager.getInstance(mockContext);
        latencyManager.setLatencyMode(LatencyManager.MODE_STABILITY);
        
        assertEquals(LatencyManager.MODE_STABILITY, latencyManager.getCurrentMode());
        assertEquals("Estabilidade", latencyManager.getModeName(LatencyManager.MODE_STABILITY));
        assertEquals(2048, latencyManager.getBufferSize());
        assertEquals(44100, latencyManager.getSampleRate());
        assertTrue(latencyManager.isAutoOversamplingEnabled());
        assertEquals(4, latencyManager.getOversamplingFactor());
        
        // Verificar cálculo de latência: (2048 / 44100) * 1000 * 2 = 92.88ms
        float expectedLatency = (float) 2048 / 44100 * 1000 * 2;
        assertEquals(expectedLatency, latencyManager.getEstimatedLatency(), 0.1f);
    }
    
    @Test
    public void testLatencyManager_ModeDescriptions() {
        // Teste das descrições dos modos
        LatencyManager latencyManager = LatencyManager.getInstance(mockContext);
        assertTrue(latencyManager.getModeDescription(LatencyManager.MODE_LOW_LATENCY).contains("performance"));
        assertTrue(latencyManager.getModeDescription(LatencyManager.MODE_BALANCED).contains("recomendado"));
        assertTrue(latencyManager.getModeDescription(LatencyManager.MODE_STABILITY).contains("gravação"));
    }
    
    @Test
    public void testLatencyManager_InvalidMode() {
        // Teste de modo inválido
        LatencyManager latencyManager = LatencyManager.getInstance(mockContext);
        latencyManager.setLatencyMode(999); // Modo inválido
        
        // Deve manter o modo anterior (não deve mudar)
        int currentMode = latencyManager.getCurrentMode();
        assertNotEquals(999, currentMode);
        assertEquals("Desconhecido", latencyManager.getModeName(999));
    }
    
    @Test
    public void testLatencyManager_LatencyProgression() {
        // Teste que verifica se a latência aumenta conforme o modo
        LatencyManager latencyManager = LatencyManager.getInstance(mockContext);
        latencyManager.setLatencyMode(LatencyManager.MODE_LOW_LATENCY);
        float lowLatency = latencyManager.getEstimatedLatency();
        
        latencyManager.setLatencyMode(LatencyManager.MODE_BALANCED);
        float balancedLatency = latencyManager.getEstimatedLatency();
        
        latencyManager.setLatencyMode(LatencyManager.MODE_STABILITY);
        float stabilityLatency = latencyManager.getEstimatedLatency();
        
        // Verificar progressão: baixa < equilibrada < estabilidade
        assertTrue("Latência de baixa latência deve ser menor que equilibrada", lowLatency < balancedLatency);
        assertTrue("Latência equilibrada deve ser menor que estabilidade", balancedLatency < stabilityLatency);
    }
}