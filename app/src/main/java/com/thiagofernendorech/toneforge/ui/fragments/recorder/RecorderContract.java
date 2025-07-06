package com.thiagofernendorech.toneforge.ui.fragments.recorder;

import com.thiagofernendorech.toneforge.ui.base.BaseView;

/**
 * Contrato MVP para o RecorderFragment
 * Define as interfaces View e Presenter para o gravador de áudio
 */
public interface RecorderContract {

    /**
     * Define os métodos que a View deve implementar
     */
    interface View extends BaseView {
        
        /**
         * Atualiza o estado do botão de gravação
         * @param isRecording true se estiver gravando, false caso contrário
         */
        void updateRecordButtonState(boolean isRecording);
        
        /**
         * Atualiza o estado do botão de reprodução
         * @param isPlaying true se estiver reproduzindo, false caso contrário
         */
        void updatePlayButtonState(boolean isPlaying);
        
        /**
         * Atualiza o timer de gravação/reprodução
         * @param timeString string formatada do tempo (ex: "00:00")
         */
        void updateTimer(String timeString);
        
        /**
         * Atualiza a lista de gravações recentes
         * @param recordings lista de gravações ou null se não houver gravações
         */
        void updateRecordingsList(String[] recordings);
        
        /**
         * Mostra mensagem de placeholder quando não há gravações
         */
        void showNoRecordingsPlaceholder();
        
        /**
         * Mostra mensagem de erro
         * @param message mensagem de erro
         */
        void showError(String message);
        
        /**
         * Mostra mensagem de sucesso
         * @param message mensagem de sucesso
         */
        void showSuccess(String message);
    }

    /**
     * Define os métodos que o Presenter deve implementar
     */
    interface Presenter {
        
        /**
         * Inicia ou para a gravação de áudio
         */
        void toggleRecording();
        
        /**
         * Inicia ou para a reprodução da última gravação
         */
        void togglePlayback();
        
        /**
         * Carrega a lista de gravações recentes
         */
        void loadRecordings();
        
        /**
         * Atualiza o timer de gravação/reprodução
         */
        void updateTimer();
        
        /**
         * Para a gravação atual
         */
        void stopRecording();
        
        /**
         * Para a reprodução atual
         */
        void stopPlayback();
        
        /**
         * Verifica se está gravando
         * @return true se estiver gravando
         */
        boolean isRecording();
        
        /**
         * Verifica se está reproduzindo
         * @return true se estiver reproduzindo
         */
        boolean isPlaying();
        
        /**
         * Obtém o tempo atual de gravação/reprodução em segundos
         * @return tempo em segundos
         */
        int getCurrentTime();
        
        /**
         * Formata o tempo em segundos para string MM:SS
         * @param seconds tempo em segundos
         * @return string formatada
         */
        String formatTime(int seconds);
        
        /**
         * Inicializa o presenter
         */
        void initialize();
        
        /**
         * Limpa recursos quando o presenter é destruído
         */
        void cleanup();
    }
} 