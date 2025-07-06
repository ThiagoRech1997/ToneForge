package com.thiagofernendorech.toneforge.ui.fragments.tuner;

import com.thiagofernendorech.toneforge.ui.base.BaseView;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;

/**
 * Contrato MVP para o TunerFragment
 * Define as interfaces de comunicação entre View e Presenter
 */
public interface TunerContract {
    
    /**
     * Interface da View (TunerFragment)
     */
    interface View extends BaseView {
        
        // === CONTROLE DE ESTADO ===
        
        /**
         * Atualiza o status do afinador
         * @param isActive true se está ativo
         * @param statusText texto de status
         */
        void updateTunerStatus(boolean isActive, String statusText);
        
        /**
         * Atualiza a nota detectada
         * @param note nota (ex: "A", "C#", "E")
         * @param octave oitava (ex: 4, 5)
         */
        void updateDetectedNote(String note, int octave);
        
        /**
         * Atualiza a frequência detectada
         * @param frequency frequência em Hz
         */
        void updateDetectedFrequency(float frequency);
        
        /**
         * Atualiza o desvio em cents
         * @param cents desvio em cents (-50 a +50)
         */
        void updateCentsDeviation(int cents);
        
        /**
         * Atualiza a precisão da afinação
         * @param accuracy precisão (0.0 = muito baixa, 1.0 = perfeita)
         */
        void updateTuningAccuracy(float accuracy);
        
        /**
         * Atualiza a nota de referência
         * @param note nota de referência
         */
        void updateReferenceNote(String note);
        
        /**
         * Atualiza a frequência de referência
         * @param frequency frequência de referência em Hz
         */
        void updateReferenceFrequency(float frequency);
        
        // === CONTROLE DE CALIBRAÇÃO ===
        
        /**
         * Atualiza o offset de calibração
         * @param offset offset em cents
         */
        void updateCalibrationOffset(int offset);
        
        /**
         * Mostra diálogo de calibração
         */
        void showCalibrationDialog();
        
        /**
         * Atualiza o temperamento musical
         * @param temperament nome do temperamento
         */
        void updateTemperament(String temperament);
        
        // === CONTROLE DE TEMPERAMENTOS ===
        
        /**
         * Mostra seletor de temperamento
         */
        void showTemperamentSelector();
        
        /**
         * Atualiza lista de temperamentos disponíveis
         * @param temperaments lista de temperamentos
         */
        void updateTemperamentList(String[] temperaments);
        
        // === CONTROLE DE NOTAS DE REFERÊNCIA ===
        
        /**
         * Atualiza botões de notas de referência
         * @param selectedNote nota selecionada
         */
        void updateReferenceNoteButtons(String selectedNote);
        
        /**
         * Atualiza a nota de referência selecionada
         * @param note nota selecionada
         */
        void selectReferenceNote(String note);
        
        // === CONTROLE DE SENSIBILIDADE ===
        
        /**
         * Atualiza a sensibilidade do detector
         * @param sensitivity sensibilidade (0.0 - 1.0)
         */
        void updateSensitivity(float sensitivity);
        
        /**
         * Atualiza o threshold de detecção
         * @param threshold threshold em dB
         */
        void updateDetectionThreshold(float threshold);
        
        // === CONTROLE DE VISUALIZAÇÃO ===
        
        /**
         * Atualiza o indicador visual de afinação
         * @param position posição (-1.0 = muito baixo, 0.0 = afinado, 1.0 = muito alto)
         */
        void updateTuningIndicator(float position);
        
        /**
         * Atualiza o histórico de frequências
         * @param frequencies array de frequências recentes
         */
        void updateFrequencyHistory(float[] frequencies);
        
        /**
         * Atualiza o espectro de frequências
         * @param spectrum array com magnitudes do espectro
         */
        void updateFrequencySpectrum(float[] spectrum);
        
        // === CONTROLE DE ÁUDIO ===
        
        /**
         * Atualiza o nível de entrada de áudio
         * @param level nível em dB
         */
        void updateInputLevel(float level);
        
        /**
         * Atualiza o status do microfone
         * @param isActive true se microfone está ativo
         */
        void updateMicrophoneStatus(boolean isActive);
        
        // === DIÁLOGOS ===
        
        /**
         * Solicita permissão de áudio
         */
        void requestAudioPermission();
        
        /**
         * Mostra erro de permissão
         */
        void showPermissionError();
        
        /**
         * Mostra erro de microfone
         */
        void showMicrophoneError();
        
        /**
         * Mostra mensagem de sucesso
         * @param message mensagem
         */
        void showSuccessMessage(String message);
        
        /**
         * Mostra mensagem de erro
         * @param message mensagem
         */
        void showErrorMessage(String message);
        
        /**
         * Mostra diálogo de configurações
         */
        void showSettingsDialog();
        
        /**
         * Mostra diálogo de ajuda
         */
        void showHelpDialog();
    }
    
    /**
     * Interface do Presenter (TunerPresenter)
     */
    interface Presenter {
        
        // === CICLO DE VIDA ===
        
        /**
         * Chamado quando a view é iniciada
         */
        void onViewStarted();
        
        /**
         * Chamado quando a view é resumida
         */
        void onViewResumed();
        
        /**
         * Chamado quando a view é pausada
         */
        void onViewPaused();
        
        /**
         * Chamado quando a view é destruída
         */
        void onViewDestroyed();
        
        // === CONTROLE DE AFINAÇÃO ===
        
        /**
         * Inicia o afinador
         */
        void startTuner();
        
        /**
         * Para o afinador
         */
        void stopTuner();
        
        /**
         * Alterna o estado do afinador
         */
        void toggleTuner();
        
        /**
         * Processa buffer de áudio
         * @param buffer buffer de áudio
         * @param size tamanho do buffer
         */
        void processAudioBuffer(float[] buffer, int size);
        
        // === CONTROLE DE CALIBRAÇÃO ===
        
        /**
         * Define o offset de calibração
         * @param offset offset em cents
         */
        void setCalibrationOffset(int offset);
        
        /**
         * Reseta a calibração para padrão
         */
        void resetCalibration();
        
        /**
         * Abre diálogo de calibração
         */
        void openCalibrationDialog();
        
        // === CONTROLE DE TEMPERAMENTOS ===
        
        /**
         * Define o temperamento musical
         * @param temperament nome do temperamento
         */
        void setTemperament(String temperament);
        
        /**
         * Abre seletor de temperamento
         */
        void openTemperamentSelector();
        
        /**
         * Obtém lista de temperamentos disponíveis
         */
        void loadTemperamentList();
        
        // === CONTROLE DE NOTAS DE REFERÊNCIA ===
        
        /**
         * Define a nota de referência
         * @param note nota (ex: "A", "E", "D")
         */
        void setReferenceNote(String note);
        
        /**
         * Define a frequência de referência
         * @param frequency frequência em Hz
         */
        void setReferenceFrequency(float frequency);
        
        /**
         * Obtém a frequência padrão para uma nota
         * @param note nota
         * @return frequência em Hz
         */
        float getStandardFrequency(String note);
        
        // === CONTROLE DE SENSIBILIDADE ===
        
        /**
         * Define a sensibilidade do detector
         * @param sensitivity sensibilidade (0.0 - 1.0)
         */
        void setSensitivity(float sensitivity);
        
        /**
         * Define o threshold de detecção
         * @param threshold threshold em dB
         */
        void setDetectionThreshold(float threshold);
        
        // === CONTROLE DE VISUALIZAÇÃO ===
        
        /**
         * Atualiza o indicador visual
         */
        void updateVisualIndicator();
        
        /**
         * Atualiza o histórico de frequências
         */
        void updateFrequencyHistory();
        
        /**
         * Atualiza o espectro de frequências
         */
        void updateFrequencySpectrum();
        
        // === CONTROLE DE ÁUDIO ===
        
        /**
         * Verifica permissões de áudio
         */
        void checkAudioPermissions();
        
        /**
         * Inicializa o sistema de áudio
         */
        void initializeAudio();
        
        /**
         * Libera recursos de áudio
         */
        void releaseAudio();
        
        /**
         * Atualiza o nível de entrada
         */
        void updateInputLevel();
        
        // === PROCESSAMENTO DE FREQUÊNCIA ===
        
        /**
         * Detecta a frequência fundamental
         * @param buffer buffer de áudio
         * @param size tamanho do buffer
         * @return frequência detectada em Hz
         */
        float detectFundamentalFrequency(float[] buffer, int size);
        
        /**
         * Calcula a nota mais próxima
         * @param frequency frequência em Hz
         * @return nota calculada
         */
        String calculateNearestNote(float frequency);
        
        /**
         * Calcula o desvio em cents
         * @param frequency frequência detectada
         * @param noteFrequency frequência da nota
         * @return desvio em cents
         */
        int calculateCentsDeviation(float frequency, float noteFrequency);
        
        /**
         * Calcula a precisão da afinação
         * @param cents desvio em cents
         * @return precisão (0.0 - 1.0)
         */
        float calculateTuningAccuracy(int cents);
        
        // === CONFIGURAÇÕES ===
        
        /**
         * Salva configurações do afinador
         */
        void saveTunerSettings();
        
        /**
         * Carrega configurações do afinador
         */
        void loadTunerSettings();
        
        /**
         * Abre configurações
         */
        void openSettings();
        
        /**
         * Abre ajuda
         */
        void openHelp();
        
        // === OUTROS ===
        
        /**
         * Força atualização da UI
         */
        void forceUpdateUI();
        
        /**
         * Processa resultado de permissão
         * @param requestCode código da requisição
         * @param permissions permissões
         * @param grantResults resultados
         */
        void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults);
    }
} 