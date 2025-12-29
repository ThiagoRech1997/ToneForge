package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

public class LatencyManager {
    private static final String TAG = "LatencyManager";
    private static final String PREFS_NAME = "latency_prefs";
    private static final String KEY_LATENCY_MODE = "latency_mode";
    
    // Modos de latência disponíveis
    public static final int MODE_LOW_LATENCY = 0;
    public static final int MODE_BALANCED = 1;
    public static final int MODE_STABILITY = 2;
    
    // Configurações por modo
    private static final int[] BUFFER_SIZES = {
        512,   // Baixa latência: buffer pequeno
        1024,  // Equilibrado: buffer médio
        2048   // Estabilidade: buffer grande
    };
    
    private static final int[] SAMPLE_RATES = {
        48000, // Baixa latência: taxa padrão
        48000, // Equilibrado: taxa padrão
        44100  // Estabilidade: taxa menor para estabilidade
    };
    
    private static final boolean[] AUTO_OVERSAMPLING = {
        false, // Baixa latência: sem oversampling para menor CPU
        true,  // Equilibrado: oversampling moderado
        true   // Estabilidade: oversampling para melhor qualidade
    };
    
    private static final int[] OVERSAMPLING_FACTORS = {
        1,     // Baixa latência: sem oversampling
        2,     // Equilibrado: 2x oversampling
        4      // Estabilidade: 4x oversampling
    };
    
    private static LatencyManager instance;
    private Context context;
    private SharedPreferences prefs;
    private int currentMode;
    private LatencyChangeListener listener;
    
    public interface LatencyChangeListener {
        void onLatencyModeChanged(int newMode);
        void onBufferSizeChanged(int newBufferSize);
        void onSampleRateChanged(int newSampleRate);
    }
    
    private LatencyManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.currentMode = prefs.getInt(KEY_LATENCY_MODE, MODE_BALANCED);
    }
    
    public static synchronized LatencyManager getInstance(Context context) {
        if (instance == null) {
            instance = new LatencyManager(context);
        }
        return instance;
    }
    
    /**
     * Define o modo de latência
     */
    public void setLatencyMode(int mode) {
        if (mode < 0 || mode > 2) {
            Log.w(TAG, "Modo de latência inválido: " + mode);
            return;
        }
        
        if (currentMode != mode) {
            currentMode = mode;
            prefs.edit().putInt(KEY_LATENCY_MODE, mode).apply();
            
            // Aplicar configurações do novo modo
            applyLatencySettings();
            
            // Notificar listener
            if (listener != null) {
                listener.onLatencyModeChanged(mode);
            }
            
            Log.d(TAG, "Modo de latência alterado para: " + getModeName(mode));
        }
    }
    
    /**
     * Obtém o modo atual de latência
     */
    public int getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Obtém o nome do modo
     */
    public String getModeName(int mode) {
        switch (mode) {
            case MODE_LOW_LATENCY:
                return "Baixa Latência";
            case MODE_BALANCED:
                return "Equilibrado";
            case MODE_STABILITY:
                return "Estabilidade";
            default:
                return "Desconhecido";
        }
    }
    
    /**
     * Obtém a descrição do modo
     */
    public String getModeDescription(int mode) {
        switch (mode) {
            case MODE_LOW_LATENCY:
                return "Prioriza resposta rápida. Ideal para performance ao vivo.";
            case MODE_BALANCED:
                return "Equilibra latência e estabilidade. Modo recomendado.";
            case MODE_STABILITY:
                return "Prioriza estabilidade e qualidade. Ideal para gravação.";
            default:
                return "";
        }
    }
    
    /**
     * Obtém o tamanho do buffer para o modo atual
     */
    public int getBufferSize() {
        return BUFFER_SIZES[currentMode];
    }
    
    /**
     * Obtém a taxa de amostragem para o modo atual
     */
    public int getSampleRate() {
        return SAMPLE_RATES[currentMode];
    }
    
    /**
     * Verifica se o oversampling automático está ativo
     */
    public boolean isAutoOversamplingEnabled() {
        return AUTO_OVERSAMPLING[currentMode];
    }
    
    /**
     * Obtém o fator de oversampling para o modo atual
     */
    public int getOversamplingFactor() {
        return OVERSAMPLING_FACTORS[currentMode];
    }
    
    /**
     * Calcula a latência estimada em milissegundos
     */
    public float getEstimatedLatency() {
        int bufferSize = getBufferSize();
        int sampleRate = getSampleRate();
        
        // Latência = (tamanho do buffer / taxa de amostragem) * 1000ms
        // Multiplicado por 2 para considerar entrada + saída
        return (float) bufferSize / sampleRate * 1000 * 2;
    }
    
    /**
     * Aplica as configurações de latência atuais
     */
    private void applyLatencySettings() {
        try {
            // Aplicar oversampling se necessário
            if (isAutoOversamplingEnabled()) {
                AudioEngine.setOversamplingEnabled(true);
                AudioEngine.setOversamplingFactor(getOversamplingFactor());
            } else {
                AudioEngine.setOversamplingEnabled(false);
            }
            
            // Reiniciar pipeline se estiver rodando
            PipelineManager pipelineManager = PipelineManager.getInstance();
            if (pipelineManager.isRunning()) {
                Log.d(TAG, "Reiniciando pipeline com novas configurações de latência");
                pipelineManager.restartPipeline();
            }
            
            Log.d(TAG, "Configurações de latência aplicadas - Modo: " + getModeName(currentMode) + 
                      ", Buffer: " + getBufferSize() + ", Sample Rate: " + getSampleRate() + 
                      ", Latência estimada: " + String.format("%.1f", getEstimatedLatency()) + "ms");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao aplicar configurações de latência: " + e.getMessage());
        }
    }
    
    /**
     * Define o listener para mudanças de latência
     */
    public void setLatencyChangeListener(LatencyChangeListener listener) {
        this.listener = listener;
    }
    
    /**
     * Obtém informações detalhadas do modo atual
     */
    public String getCurrentModeInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Modo: ").append(getModeName(currentMode)).append("\n");
        info.append("Buffer: ").append(getBufferSize()).append(" samples\n");
        info.append("Sample Rate: ").append(getSampleRate()).append(" Hz\n");
        info.append("Oversampling: ").append(isAutoOversamplingEnabled() ? "Sim" : "Não");
        if (isAutoOversamplingEnabled()) {
            info.append(" (").append(getOversamplingFactor()).append("x)");
        }
        info.append("\n");
        info.append("Latência estimada: ").append(String.format("%.1f", getEstimatedLatency())).append(" ms");
        
        return info.toString();
    }
    
    /**
     * Verifica se o dispositivo suporta baixa latência
     */
    public boolean isLowLatencySupported() {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            return audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE) != null &&
                   audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER) != null;
        } catch (Exception e) {
            Log.w(TAG, "Erro ao verificar suporte a baixa latência: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtém a latência mínima possível no dispositivo
     */
    public float getMinimumLatency() {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            String framesPerBuffer = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
            String sampleRate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
            
            if (framesPerBuffer != null && sampleRate != null) {
                int frames = Integer.parseInt(framesPerBuffer);
                int rate = Integer.parseInt(sampleRate);
                return (float) frames / rate * 1000 * 2; // entrada + saída
            }
        } catch (Exception e) {
            Log.w(TAG, "Erro ao obter latência mínima: " + e.getMessage());
        }
        
        // Fallback para valores padrão
        return 10.0f; // 10ms como estimativa conservadora
    }
    
    /**
     * Obtém recomendações de uso para cada modo
     */
    public String getUsageRecommendations() {
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("🎸 Baixa Latência: Performance ao vivo, prática\n");
        recommendations.append("⚖️ Equilibrado: Uso geral, recomendado\n");
        recommendations.append("🎧 Estabilidade: Gravação, qualidade máxima\n");
        return recommendations.toString();
    }
} 