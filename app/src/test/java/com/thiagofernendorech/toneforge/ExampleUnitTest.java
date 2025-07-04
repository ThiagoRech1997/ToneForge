package com.thiagofernendorech.toneforge;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for ToneForge app components.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    
    @Test
    public void testLatencyCalculations() {
        // Teste dos cálculos de latência baseados nas configurações do LatencyManager
        
        // Modo Baixa Latência: buffer 512, sample rate 48000
        float lowLatency = calculateLatency(512, 48000);
        assertEquals(21.33f, lowLatency, 0.1f);
        
        // Modo Equilibrado: buffer 1024, sample rate 48000
        float balancedLatency = calculateLatency(1024, 48000);
        assertEquals(42.67f, balancedLatency, 0.1f);
        
        // Modo Estabilidade: buffer 2048, sample rate 44100
        float stabilityLatency = calculateLatency(2048, 44100);
        assertEquals(92.88f, stabilityLatency, 0.1f);
    }
    
    @Test
    public void testLatencyProgression() {
        // Teste que verifica se a latência aumenta conforme o buffer aumenta
        
        float lowLatency = calculateLatency(512, 48000);
        float balancedLatency = calculateLatency(1024, 48000);
        float stabilityLatency = calculateLatency(2048, 44100);
        
        // Verificar progressão: baixa < equilibrada < estabilidade
        assertTrue("Latência de baixa latência deve ser menor que equilibrada", lowLatency < balancedLatency);
        assertTrue("Latência equilibrada deve ser menor que estabilidade", balancedLatency < stabilityLatency);
    }
    
    @Test
    public void testBufferSizeImpact() {
        // Teste do impacto do tamanho do buffer na latência
        
        float smallBuffer = calculateLatency(256, 48000);
        float mediumBuffer = calculateLatency(512, 48000);
        float largeBuffer = calculateLatency(1024, 48000);
        
        assertTrue("Buffer menor deve ter latência menor", smallBuffer < mediumBuffer);
        assertTrue("Buffer médio deve ter latência menor que grande", mediumBuffer < largeBuffer);
    }
    
    @Test
    public void testSampleRateImpact() {
        // Teste do impacto da taxa de amostragem na latência
        
        float highSampleRate = calculateLatency(512, 48000);
        float lowSampleRate = calculateLatency(512, 44100);
        
        assertTrue("Taxa de amostragem maior deve ter latência menor", highSampleRate < lowSampleRate);
    }
    
    @Test
    public void testLatencyFormula() {
        // Teste da fórmula de latência: (buffer / sampleRate) * 1000 * 2
        
        int bufferSize = 1024;
        int sampleRate = 48000;
        
        float expectedLatency = (float) bufferSize / sampleRate * 1000 * 2;
        float calculatedLatency = calculateLatency(bufferSize, sampleRate);
        
        assertEquals("Fórmula de latência deve estar correta", expectedLatency, calculatedLatency, 0.01f);
    }
    
    /**
     * Calcula a latência estimada em milissegundos
     * Fórmula: (tamanho do buffer / taxa de amostragem) * 1000ms * 2 (entrada + saída)
     */
    private float calculateLatency(int bufferSize, int sampleRate) {
        return (float) bufferSize / sampleRate * 1000 * 2;
    }
}