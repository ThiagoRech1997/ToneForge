package com.thiagofernendorech.toneforge.ui.fragments.metronome;

import com.thiagofernendorech.toneforge.ui.base.BasePresenter;
import com.thiagofernendorech.toneforge.ui.base.BaseView;

/**
 * Contrato MVP para o MetronomeFragment
 * Define as interfaces View e Presenter para o metrônomo
 */
public interface MetronomeContract {

    /**
     * Interface View para o MetronomeFragment
     * Define os métodos que a View deve implementar
     */
    interface View extends BaseView {
        
        /**
         * Atualiza o display do BPM
         * @param bpm valor do BPM a ser exibido
         */
        void updateBpmDisplay(int bpm);
        
        /**
         * Atualiza o display do compasso
         * @param timeSignature valor do compasso a ser exibido
         */
        void updateTimeSignatureDisplay(int timeSignature);
        
        /**
         * Atualiza o display do volume
         * @param volumePercent percentual do volume (0-100)
         */
        void updateVolumeDisplay(int volumePercent);
        
        /**
         * Atualiza o estado do botão de play/stop
         * @param isPlaying true se está tocando, false se parado
         */
        void updatePlayButtonState(boolean isPlaying);
        
        /**
         * Inicia a animação de batida
         */
        void startBeatAnimation();
        
        /**
         * Para a animação de batida
         */
        void stopBeatAnimation();
        
        /**
         * Reinicia a animação de batida
         */
        void restartBeatAnimation();
        
        /**
         * Executa uma animação de batida única
         */
        void animateBeat();
        
        /**
         * Atualiza o título do header
         * @param title título a ser exibido
         */
        void updateHeaderTitle(String title);
        
        /**
         * Configura os listeners dos controles
         */
        void setupControlListeners();
        
        /**
         * Configura o SeekBar de volume
         */
        void setupVolumeSeekBar();
        
        /**
         * Configura os botões de preset de BPM
         */
        void setupPresetButtons();
        
        /**
         * Configura os botões de incremento/decremento de BPM
         */
        void setupBpmControlButtons();
        
        /**
         * Configura os botões de controle de compasso
         */
        void setupTimeSignatureButtons();
    }

    /**
     * Interface Presenter para o MetronomeFragment
     * Define os métodos que o Presenter deve implementar
     */
    interface Presenter {
        
        /**
         * Inicializa o presenter
         */
        void initialize();
        
        /**
         * Alterna o estado de play/stop do metrônomo
         */
        void togglePlayStop();
        
        /**
         * Aumenta o BPM
         */
        void increaseBpm();
        
        /**
         * Diminui o BPM
         */
        void decreaseBpm();
        
        /**
         * Define o BPM para um valor específico
         * @param bpm valor do BPM
         */
        void setBpm(int bpm);
        
        /**
         * Aumenta o compasso
         */
        void increaseTimeSignature();
        
        /**
         * Diminui o compasso
         */
        void decreaseTimeSignature();
        
        /**
         * Define o volume do metrônomo
         * @param volumePercent percentual do volume (0-100)
         */
        void setVolume(int volumePercent);
        
        /**
         * Obtém o BPM atual
         * @return valor do BPM
         */
        int getCurrentBpm();
        
        /**
         * Obtém o compasso atual
         * @return valor do compasso
         */
        int getCurrentTimeSignature();
        
        /**
         * Obtém o volume atual
         * @return percentual do volume (0-100)
         */
        int getCurrentVolume();
        
        /**
         * Verifica se o metrônomo está tocando
         * @return true se está tocando, false caso contrário
         */
        boolean isPlaying();
        
        /**
         * Limpa recursos quando o fragment é destruído
         */
        void cleanup();
    }
} 