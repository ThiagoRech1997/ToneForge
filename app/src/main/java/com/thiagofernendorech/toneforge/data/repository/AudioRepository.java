package com.thiagofernendorech.toneforge.data.repository;

import android.content.Context;
import android.util.Log;
import com.thiagofernendorech.toneforge.AudioEngine;
import com.thiagofernendorech.toneforge.PipelineManager;
import com.thiagofernendorech.toneforge.AudioStateManager;
import com.thiagofernendorech.toneforge.LatencyManager;
import com.thiagofernendorech.toneforge.AudioAnalyzer;
import com.thiagofernendorech.toneforge.PresetManager;
import com.thiagofernendorech.toneforge.AutomationManager;
import com.thiagofernendorech.toneforge.ToneForgeMidiManager;
import com.thiagofernendorech.toneforge.domain.models.AudioState;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Repository centralizado para operações de áudio
 * Abstrai a complexidade dos managers de áudio e fornece interface unificada
 */
public class AudioRepository {
    
    private static final String TAG = "AudioRepository";
    
    private static AudioRepository instance;
    private Context context;
    
    // Managers de áudio
    private AudioEngine audioEngine;
    private PipelineManager pipelineManager;
    private AudioStateManager stateManager;
    private LatencyManager latencyManager;
    private AudioAnalyzer audioAnalyzer;
    private PresetManager presetManager;
    private AutomationManager automationManager;
    private ToneForgeMidiManager midiManager;
    
    private AudioRepository(Context context) {
        this.context = context.getApplicationContext();
        initializeManagers();
    }
    
    /**
     * Obtém a instância singleton do AudioRepository
     * @param context contexto da aplicação
     * @return instância do AudioRepository
     */
    public static synchronized AudioRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AudioRepository(context);
        }
        return instance;
    }
    
    /**
     * Inicializa todos os managers de áudio
     */
    private void initializeManagers() {
        try {
            audioEngine = AudioEngine.getInstance();
            pipelineManager = PipelineManager.getInstance();
            stateManager = AudioStateManager.getInstance(context);
            latencyManager = LatencyManager.getInstance(context);
            audioAnalyzer = new AudioAnalyzer();
            presetManager = new PresetManager();
            automationManager = AutomationManager.getInstance(context);
            midiManager = ToneForgeMidiManager.getInstance(context);
            
            // Inicializar pipeline manager
            pipelineManager.initialize(context);
            
            // Configurar callback para mudanças de estado
            pipelineManager.setCallback(new PipelineManager.PipelineCallback() {
                @Override
                public void onPipelineStarted() {
                    Log.d(TAG, "Pipeline de áudio iniciado");
                    updateAudioState();
                }
                
                @Override
                public void onPipelineStopped() {
                    Log.d(TAG, "Pipeline de áudio parado");
                    updateAudioState();
                }
                
                @Override
                public void onPipelineError(String error) {
                    Log.e(TAG, "Erro no pipeline de áudio: " + error);
                    updateAudioState();
                }
                
                @Override
                public void onPipelineRecovered() {
                    Log.d(TAG, "Pipeline de áudio recuperado");
                    updateAudioState();
                }
                
                @Override
                public void onPipelineStateChanged(int oldState, int newState) {
                    Log.d(TAG, "Estado do pipeline alterado: " + oldState + " -> " + newState);
                    updateAudioState();
                }
                
                @Override
                public void onSampleRateChanged(int newSampleRate) {
                    Log.d(TAG, "Taxa de amostragem alterada: " + newSampleRate + " Hz");
                    // Atualizar engine nativo com nova taxa de amostragem
                    if (AudioEngine.isNativeLibraryLoaded()) {
                        audioEngine.setSampleRate(newSampleRate);
                    }
                    updateAudioState();
                }
            });
            
            Log.d(TAG, "Managers de áudio inicializados com sucesso");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar managers de áudio: " + e.getMessage(), e);
        }
    }
    
    /**
     * Atualiza o estado do áudio
     */
    private void updateAudioState() {
        try {
            AudioState state = getCurrentAudioState();
            stateManager.saveCurrentState();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar estado do áudio: " + e.getMessage(), e);
        }
    }
    
    // === OPERAÇÕES DE ÁUDIO ===
    
    /**
     * Inicia o pipeline de áudio
     * @return true se iniciado com sucesso
     */
    public boolean startAudioPipeline() {
        try {
            Log.d(TAG, "Iniciando pipeline de áudio...");
            
            // Verificar se a biblioteca nativa está carregada
            if (!AudioEngine.isNativeLibraryLoaded()) {
                Log.e(TAG, "Biblioteca nativa não está carregada");
                return false;
            }
            
            // Verificar se o pipeline já está rodando
            if (pipelineManager.isRunning()) {
                Log.d(TAG, "Pipeline já está rodando");
                return true;
            }
            
            // Inicializar engine de áudio
            try {
                audioEngine.initAudioEngine();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao inicializar engine de áudio: " + e.getMessage(), e);
                return false;
            }
            
            // Iniciar pipeline com retry
            boolean success = false;
            int retryCount = 0;
            final int MAX_RETRIES = 3;
            
            while (!success && retryCount < MAX_RETRIES) {
                try {
                    success = pipelineManager.startPipeline();
                    if (!success) {
                        retryCount++;
                        Log.w(TAG, "Tentativa " + retryCount + " de iniciar pipeline falhou");
                        if (retryCount < MAX_RETRIES) {
                            Thread.sleep(100 * retryCount); // Backoff exponencial
                        }
                    }
                } catch (Exception e) {
                    retryCount++;
                    Log.e(TAG, "Erro na tentativa " + retryCount + ": " + e.getMessage(), e);
                    if (retryCount < MAX_RETRIES) {
                        try {
                            Thread.sleep(100 * retryCount);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            
            if (success) {
                Log.d(TAG, "Pipeline de áudio iniciado com sucesso");
                updateAudioState();
            } else {
                Log.e(TAG, "Falha ao iniciar pipeline após " + MAX_RETRIES + " tentativas");
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar pipeline de áudio: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Para o pipeline de áudio
     */
    public void stopAudioPipeline() {
        try {
            Log.d(TAG, "Parando pipeline de áudio...");
            
            // Verificar se o pipeline está rodando antes de parar
            if (!pipelineManager.isRunning()) {
                Log.d(TAG, "Pipeline já estava parado");
                return;
            }
            
            // Parar pipeline
            try {
                pipelineManager.stopPipeline();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao parar pipeline: " + e.getMessage(), e);
                // Continuar com a limpeza mesmo se houver erro
            }
            
            // Limpar engine de áudio
            if (AudioEngine.isNativeLibraryLoaded()) {
                try {
                    audioEngine.cleanupAudioEngine();
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao limpar engine de áudio: " + e.getMessage(), e);
                }
            }
            
            Log.d(TAG, "Pipeline de áudio parado");
            updateAudioState();
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao parar pipeline de áudio: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica se o pipeline está rodando
     * @return true se o pipeline está ativo
     */
    public boolean isAudioPipelineRunning() {
        try {
            // Verificar se a biblioteca nativa está carregada
            if (!AudioEngine.isNativeLibraryLoaded()) {
                Log.d(TAG, "Biblioteca nativa não carregada, pipeline não pode estar rodando");
                return false;
            }
            
            // Verificar estado do pipeline
            boolean isRunning = pipelineManager.isRunning();
            Log.d(TAG, "Estado do pipeline: " + (isRunning ? "rodando" : "parado"));
            return isRunning;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar estado do pipeline: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Pausa o pipeline de áudio
     */
    public void pauseAudioPipeline() {
        try {
            Log.d(TAG, "Pausando pipeline de áudio...");
            pipelineManager.pausePipeline();
            updateAudioState();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao pausar pipeline de áudio: " + e.getMessage(), e);
        }
    }
    
    /**
     * Resume o pipeline de áudio
     */
    public void resumeAudioPipeline() {
        try {
            Log.d(TAG, "Resumindo pipeline de áudio...");
            pipelineManager.resumePipeline();
            updateAudioState();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao resumir pipeline de áudio: " + e.getMessage(), e);
        }
    }
    
    // === PARÂMETROS DE EFEITOS ===
    
    /**
     * Aplica parâmetros de efeitos
     * @param parameters parâmetros a serem aplicados
     */
    public void applyEffectParameters(EffectParameters parameters) {
        try {
            if (!AudioEngine.isNativeLibraryLoaded()) {
                Log.w(TAG, "Biblioteca nativa não carregada - não é possível aplicar parâmetros");
                return;
            }
            
            if (parameters.getGain() != null) {
                audioEngine.setGain(parameters.getGain());
                Log.d(TAG, "Ganho aplicado: " + parameters.getGain());
            }
            
            if (parameters.getDistortion() != null) {
                audioEngine.setDistortion(parameters.getDistortion());
                Log.d(TAG, "Distorção aplicada: " + parameters.getDistortion());
            }
            
            if (parameters.getDelayTime() != null && parameters.getDelayFeedback() != null) {
                audioEngine.setDelay(parameters.getDelayTime(), parameters.getDelayFeedback());
                Log.d(TAG, "Delay aplicado: tempo=" + parameters.getDelayTime() + 
                          ", feedback=" + parameters.getDelayFeedback());
            }
            
            if (parameters.getReverbRoomSize() != null && parameters.getReverbDamping() != null) {
                audioEngine.setReverb(parameters.getReverbRoomSize(), parameters.getReverbDamping());
                Log.d(TAG, "Reverb aplicado: roomSize=" + parameters.getReverbRoomSize() + 
                          ", damping=" + parameters.getReverbDamping());
            }
            
            // Atualizar estado
            updateAudioState();
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao aplicar parâmetros de efeitos: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtém os parâmetros atuais dos efeitos
     * @return parâmetros atuais
     */
    public EffectParameters getCurrentEffectParameters() {
        EffectParameters parameters = new EffectParameters();
        
        try {
            if (AudioEngine.isNativeLibraryLoaded()) {
                // Obter valores do engine nativo
                parameters.setGain(audioEngine.getGain());
                parameters.setDistortion(audioEngine.getDistortion());
                parameters.setDelayTime(audioEngine.getDelayTime());
                parameters.setDelayFeedback(audioEngine.getDelayFeedback());
                parameters.setReverbRoomSize(audioEngine.getReverbRoomSize());
                parameters.setReverbDamping(audioEngine.getReverbDamping());
            } else {
                // Valores padrão se a biblioteca não estiver carregada
                parameters.setGain(0.5f);
                parameters.setDistortion(0.0f);
                parameters.setDelayTime(0.0f);
                parameters.setDelayFeedback(0.0f);
                parameters.setReverbRoomSize(0.0f);
                parameters.setReverbDamping(0.0f);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter parâmetros atuais: " + e.getMessage(), e);
            // Usar valores padrão em caso de erro
            parameters.setGain(0.5f);
            parameters.setDistortion(0.0f);
            parameters.setDelayTime(0.0f);
            parameters.setDelayFeedback(0.0f);
            parameters.setReverbRoomSize(0.0f);
            parameters.setReverbDamping(0.0f);
        }
        
        return parameters;
    }
    
    // === CONTROLE DE EFEITOS ===
    
    /**
     * Ativa/desativa efeito de ganho
     * @param enabled true para ativar
     */
    public void setGainEnabled(boolean enabled) {
        try {
            if (AudioEngine.isNativeLibraryLoaded()) {
                audioEngine.setGainEnabled(enabled);
                Log.d(TAG, "Ganho " + (enabled ? "ativado" : "desativado"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar ganho: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ativa/desativa efeito de distorção
     * @param enabled true para ativar
     */
    public void setDistortionEnabled(boolean enabled) {
        try {
            if (AudioEngine.isNativeLibraryLoaded()) {
                audioEngine.setDistortionEnabled(enabled);
                Log.d(TAG, "Distorção " + (enabled ? "ativada" : "desativada"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar distorção: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ativa/desativa efeito de delay
     * @param enabled true para ativar
     */
    public void setDelayEnabled(boolean enabled) {
        try {
            if (AudioEngine.isNativeLibraryLoaded()) {
                audioEngine.setDelayEnabled(enabled);
                Log.d(TAG, "Delay " + (enabled ? "ativado" : "desativado"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar delay: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ativa/desativa efeito de reverb
     * @param enabled true para ativar
     */
    public void setReverbEnabled(boolean enabled) {
        try {
            if (AudioEngine.isNativeLibraryLoaded()) {
                audioEngine.setReverbEnabled(enabled);
                Log.d(TAG, "Reverb " + (enabled ? "ativado" : "desativado"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar reverb: " + e.getMessage(), e);
        }
    }
    
    // === PRESETS ===
    
    /**
     * Carrega um preset
     * @param presetName nome do preset
     * @return true se carregado com sucesso
     */
    public boolean loadPreset(String presetName) {
        try {
            Log.d(TAG, "Carregando preset: " + presetName);
            
            // Carregar preset do manager
            boolean success = presetManager.loadPreset(presetName);
            
            if (success) {
                // Aplicar parâmetros do preset
                EffectParameters parameters = presetManager.getCurrentPresetParameters();
                applyEffectParameters(parameters);
                
                Log.d(TAG, "Preset carregado com sucesso: " + presetName);
            } else {
                Log.e(TAG, "Falha ao carregar preset: " + presetName);
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar preset: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Salva um preset
     * @param presetName nome do preset
     * @return true se salvo com sucesso
     */
    public boolean savePreset(String presetName) {
        try {
            Log.d(TAG, "Salvando preset: " + presetName);
            
            // Obter parâmetros atuais
            EffectParameters parameters = getCurrentEffectParameters();
            
            // Salvar preset
            boolean success = presetManager.savePreset(presetName, parameters);
            
            if (success) {
                Log.d(TAG, "Preset salvo com sucesso: " + presetName);
            } else {
                Log.e(TAG, "Falha ao salvar preset: " + presetName);
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar preset: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Obtém lista de presets
     * @return lista de nomes de presets
     */
    public List<String> getPresetNames() {
        try {
            return presetManager.getPresetNames();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter lista de presets: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Remove um preset
     * @param presetName nome do preset
     * @return true se removido com sucesso
     */
    public boolean deletePreset(String presetName) {
        try {
            Log.d(TAG, "Removendo preset: " + presetName);
            
            boolean success = presetManager.deletePreset(presetName);
            
            if (success) {
                Log.d(TAG, "Preset removido com sucesso: " + presetName);
            } else {
                Log.e(TAG, "Falha ao remover preset: " + presetName);
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao remover preset: " + e.getMessage(), e);
            return false;
        }
    }
    
    // === AUTOMAÇÃO ===
    
    /**
     * Inicia gravação de automação
     * @param presetName nome do preset
     * @param automationName nome da automação
     * @return true se iniciado com sucesso
     */
    public boolean startAutomationRecording(String presetName, String automationName) {
        try {
            Log.d(TAG, "Iniciando gravação de automação: " + automationName + " para preset: " + presetName);
            
            automationManager.startRecording(presetName, automationName);
            
            Log.d(TAG, "Gravação de automação iniciada com sucesso");
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar gravação de automação: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Para gravação de automação
     * @return true se parado com sucesso
     */
    public boolean stopAutomationRecording() {
        try {
            Log.d(TAG, "Parando gravação de automação...");
            
            automationManager.stopRecording();
            
            Log.d(TAG, "Gravação de automação parada com sucesso");
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao parar gravação de automação: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Inicia reprodução de automação
     * @param presetName nome do preset
     * @param automationName nome da automação
     * @return true se iniciado com sucesso
     */
    public boolean startAutomationPlayback(String presetName, String automationName) {
        try {
            Log.d(TAG, "Iniciando reprodução de automação: " + automationName + " para preset: " + presetName);
            
            automationManager.startPlayback(presetName, automationName);
            
            Log.d(TAG, "Reprodução de automação iniciada com sucesso");
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar reprodução de automação: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Para reprodução de automação
     * @return true se parado com sucesso
     */
    public boolean stopAutomationPlayback() {
        try {
            Log.d(TAG, "Parando reprodução de automação...");
            
            automationManager.stopPlayback();
            
            Log.d(TAG, "Reprodução de automação parada com sucesso");
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao parar reprodução de automação: " + e.getMessage(), e);
            return false;
        }
    }
    
    // === ESTADO DO ÁUDIO ===
    
    /**
     * Obtém o estado atual do áudio
     * @return estado atual
     */
    public AudioState getCurrentAudioState() {
        AudioState state = new AudioState();
        
        try {
            state.setPipelineRunning(pipelineManager.isRunning());
            state.setPipelinePaused(pipelineManager.isPaused());
            state.setCurrentLatencyMode(latencyManager.getCurrentMode());
            state.setOversamplingEnabled(audioEngine.isOversamplingEnabled());
            state.setSampleRate(pipelineManager.getCurrentSampleRate());
            state.setBufferSize(pipelineManager.getCurrentBufferSize());
            state.setErrorCount(pipelineManager.getErrorCount());
            state.setLastError(pipelineManager.getLastError());
            state.setUptime(pipelineManager.getUptime());
            state.setTotalSamplesProcessed(pipelineManager.getTotalSamplesProcessed());
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter estado do áudio: " + e.getMessage(), e);
            // Estado padrão em caso de erro
            state.setPipelineRunning(false);
            state.setPipelinePaused(false);
            state.setCurrentLatencyMode(1);
            state.setOversamplingEnabled(false);
        }
        
        return state;
    }
    
    /**
     * Salva o estado atual do áudio
     */
    public void saveCurrentState() {
        try {
            AudioState state = getCurrentAudioState();
            stateManager.saveCurrentState();
            Log.d(TAG, "Estado do áudio salvo");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar estado do áudio: " + e.getMessage(), e);
        }
    }
    
    /**
     * Restaura o estado salvo do áudio
     */
    public void restoreState() {
        try {
            stateManager.restoreState();
            Log.d(TAG, "Estado do áudio restaurado");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao restaurar estado do áudio: " + e.getMessage(), e);
        }
    }
    
    // === CONFIGURAÇÕES DE LATÊNCIA ===
    
    /**
     * Define o modo de latência
     * @param mode modo (0=baixa, 1=equilibrado, 2=estabilidade)
     */
    public void setLatencyMode(int mode) {
        try {
            latencyManager.setLatencyMode(mode);
            Log.d(TAG, "Modo de latência alterado para: " + mode);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao alterar modo de latência: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtém o modo de latência atual
     * @return modo atual
     */
    public int getCurrentLatencyMode() {
        try {
            return latencyManager.getCurrentMode();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter modo de latência: " + e.getMessage(), e);
            return 1; // Modo equilibrado como fallback
        }
    }
    
    /**
     * Obtém a latência estimada em millisegundos
     * @return latência estimada
     */
    public float getEstimatedLatency() {
        try {
            return latencyManager.getEstimatedLatency();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter latência estimada: " + e.getMessage(), e);
            return 10.0f; // Latência padrão como fallback
        }
    }
    
    // === MIDI ===
    
    /**
     * Ativa/desativa MIDI
     * @param enabled true para ativar
     */
    public void setMidiEnabled(boolean enabled) {
        try {
            midiManager.setMidiEnabled(enabled);
            Log.d(TAG, "MIDI " + (enabled ? "ativado" : "desativado"));
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar MIDI: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica se MIDI está ativo
     * @return true se MIDI está ativo
     */
    public boolean isMidiEnabled() {
        try {
            return midiManager.isMidiEnabled();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar estado do MIDI: " + e.getMessage(), e);
            return false;
        }
    }
    
    // === ANÁLISE DE ÁUDIO ===
    
    /**
     * Inicia análise de áudio
     * @param callback callback para receber dados
     */
    public void startAudioAnalysis(AudioAnalyzer.AudioAnalyzerCallback callback) {
        try {
            audioAnalyzer.setCallback(callback);
            audioAnalyzer.startAnalysis();
            Log.d(TAG, "Análise de áudio iniciada");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar análise de áudio: " + e.getMessage(), e);
        }
    }
    
    /**
     * Para análise de áudio
     */
    public void stopAudioAnalysis() {
        try {
            audioAnalyzer.stopAnalysis();
            Log.d(TAG, "Análise de áudio parada");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao parar análise de áudio: " + e.getMessage(), e);
        }
    }
    
    // === LOOPER ===
    
    /**
     * Inicia gravação do looper
     */
    public void startLooperRecording() {
        try {
            audioEngine.startLooperRecording();
            Log.d(TAG, "Gravação do looper iniciada");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar gravação do looper: " + e.getMessage(), e);
        }
    }
    
    /**
     * Para gravação do looper
     */
    public void stopLooperRecording() {
        try {
            audioEngine.stopLooperRecording();
            Log.d(TAG, "Gravação do looper parada");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao parar gravação do looper: " + e.getMessage(), e);
        }
    }
    
    /**
     * Inicia reprodução do looper
     */
    public void startLooperPlayback() {
        try {
            audioEngine.startLooperPlayback();
            Log.d(TAG, "Reprodução do looper iniciada");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar reprodução do looper: " + e.getMessage(), e);
        }
    }
    
    /**
     * Para reprodução do looper
     */
    public void stopLooperPlayback() {
        try {
            audioEngine.stopLooperPlayback();
            Log.d(TAG, "Reprodução do looper parada");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao parar reprodução do looper: " + e.getMessage(), e);
        }
    }
    
    /**
     * Limpa o looper
     */
    public void clearLooper() {
        try {
            audioEngine.clearLooper();
            Log.d(TAG, "Looper limpo");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao limpar looper: " + e.getMessage(), e);
        }
    }
    
    // === AFINADOR ===
    
    /**
     * Inicia afinador
     */
    public void startTuner() {
        try {
            audioEngine.startTuner();
            Log.d(TAG, "Afinador iniciado");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar afinador: " + e.getMessage(), e);
        }
    }
    
    /**
     * Para afinador
     */
    public void stopTuner() {
        try {
            audioEngine.stopTuner();
            Log.d(TAG, "Afinador parado");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao parar afinador: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtém frequência detectada pelo afinador
     * @return frequência em Hz
     */
    public float getDetectedFrequency() {
        try {
            return audioEngine.getDetectedFrequency();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter frequência detectada: " + e.getMessage(), e);
            return 0.0f;
        }
    }
    
    // === METRÔNOMO ===
    
    /**
     * Inicia metrônomo
     * @param bpm batidas por minuto
     */
    public void startMetronome(int bpm) {
        try {
            audioEngine.startMetronome(bpm);
            Log.d(TAG, "Metrônomo iniciado com " + bpm + " BPM");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar metrônomo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Para metrônomo
     */
    public void stopMetronome() {
        try {
            audioEngine.stopMetronome();
            Log.d(TAG, "Metrônomo parado");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao parar metrônomo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica se metrônomo está ativo
     * @return true se ativo
     */
    public boolean isMetronomeActive() {
        try {
            return audioEngine.isMetronomeActive();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar estado do metrônomo: " + e.getMessage(), e);
            return false;
        }
    }
    
    // === LIMPEZA ===
    
    /**
     * Limpa recursos do repository
     */
    public void cleanup() {
        try {
            Log.d(TAG, "Limpando recursos do AudioRepository...");
            
            // Parar pipeline se estiver rodando
            if (isAudioPipelineRunning()) {
                stopAudioPipeline();
            }
            
            // Limpar managers
            if (audioAnalyzer != null) {
                audioAnalyzer.cleanup();
            }
            
            if (midiManager != null) {
                midiManager.cleanup();
            }
            
            if (automationManager != null) {
                automationManager.cleanup();
            }
            
            Log.d(TAG, "Recursos do AudioRepository limpos");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao limpar recursos: " + e.getMessage(), e);
        }
    }
} 