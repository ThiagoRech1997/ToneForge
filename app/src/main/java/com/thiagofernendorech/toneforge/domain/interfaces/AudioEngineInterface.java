package com.thiagofernendorech.toneforge.domain.interfaces;

import com.thiagofernendorech.toneforge.domain.models.AudioState;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;

/**
 * Interface para o motor de áudio
 * Abstrai a implementação específica seguindo o DIP
 */
public interface AudioEngineInterface {
    
    // === CONTROLE DE PIPELINE ===
    
    /**
     * Inicia o pipeline de áudio
     * @return true se iniciado com sucesso
     */
    boolean startPipeline();
    
    /**
     * Para o pipeline de áudio
     */
    void stopPipeline();
    
    /**
     * Pausa o pipeline de áudio
     */
    void pausePipeline();
    
    /**
     * Resume o pipeline de áudio
     */
    void resumePipeline();
    
    /**
     * Verifica se o pipeline está rodando
     * @return true se está ativo
     */
    boolean isPipelineRunning();
    
    /**
     * Verifica se a biblioteca nativa está carregada
     * @return true se carregada
     */
    boolean isNativeLibraryLoaded();
    
    // === CONTROLE DE EFEITOS ===
    
    /**
     * Aplica parâmetros de efeitos
     * @param parameters parâmetros a aplicar
     */
    void applyEffectParameters(EffectParameters parameters);
    
    /**
     * Obtém parâmetros atuais dos efeitos
     * @return parâmetros atuais
     */
    EffectParameters getCurrentEffectParameters();
    
    /**
     * Define ganho principal
     * @param gain valor de 0.0 a 1.0
     */
    void setGain(float gain);
    
    /**
     * Define distorção
     * @param distortion valor de 0.0 a 1.0
     */
    void setDistortion(float distortion);
    
    /**
     * Define delay
     * @param delayTime tempo em segundos
     * @param feedback feedback de 0.0 a 1.0
     */
    void setDelay(float delayTime, float feedback);
    
    /**
     * Define reverb
     * @param roomSize tamanho da sala de 0.0 a 1.0
     * @param damping amortecimento de 0.0 a 1.0
     */
    void setReverb(float roomSize, float damping);
    
    // === CONTROLE DE ESTADO ===
    
    /**
     * Obtém estado atual do áudio
     * @return estado atual
     */
    AudioState getCurrentState();
    
    /**
     * Define configurações de latência
     * @param bufferSize tamanho do buffer
     * @param sampleRate taxa de amostragem
     */
    void setLatencySettings(int bufferSize, int sampleRate);
    
    // === LOOPER ===
    
    /**
     * Inicia gravação do looper
     */
    void startLooperRecording();
    
    /**
     * Para gravação do looper
     */
    void stopLooperRecording();
    
    /**
     * Inicia reprodução do looper
     */
    void startLooperPlayback();
    
    /**
     * Para reprodução do looper
     */
    void stopLooperPlayback();
    
    /**
     * Limpa o looper
     */
    void clearLooper();
    
    // === AFINADOR ===
    
    /**
     * Inicia afinador
     */
    void startTuner();
    
    /**
     * Para afinador
     */
    void stopTuner();
    
    /**
     * Obtém frequência detectada
     * @return frequência em Hz
     */
    float getDetectedFrequency();
    
    // === METRÔNOMO ===
    
    /**
     * Inicia metrônomo
     * @param bpm batidas por minuto
     */
    void startMetronome(int bpm);
    
    /**
     * Para metrônomo
     */
    void stopMetronome();
    
    /**
     * Verifica se metrônomo está ativo
     * @return true se ativo
     */
    boolean isMetronomeActive();
    
    // === LIMPEZA ===
    
    /**
     * Limpa recursos do motor de áudio
     */
    void cleanup();
} 