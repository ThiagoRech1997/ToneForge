package com.thiagofernendorech.toneforge.ui.fragments.home;

import com.thiagofernendorech.toneforge.ui.base.BaseView;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;
import com.thiagofernendorech.toneforge.domain.models.AudioState;

/**
 * Contrato MVP para o HomeFragment
 * Define as interfaces de comunicação entre View e Presenter
 */
public interface HomeContract {
    
    /**
     * Interface da View (HomeFragment)
     */
    interface View extends BaseView {
        
        /**
         * Atualiza o status de conectividade Wi-Fi
         * @param connected true se conectado
         */
        void updateWifiStatus(boolean connected);
        
        /**
         * Atualiza o status da bateria
         * @param level nível da bateria (0-100)
         * @param isCharging true se está carregando
         */
        void updateBatteryStatus(int level, boolean isCharging);
        
        /**
         * Atualiza o estado do áudio
         * @param audioState estado atual do áudio
         */
        void updateAudioState(AudioState audioState);
        
        /**
         * Atualiza o título do header
         * @param title novo título
         */
        void updateTitle(String title);
        
        /**
         * Mostra diálogo de status Wi-Fi
         */
        void showWifiDialog();
        
        /**
         * Mostra diálogo de controle de volume
         */
        void showVolumeDialog();
        
        /**
         * Mostra diálogo de opções de energia
         */
        void showPowerDialog();
    }
    
    /**
     * Interface do Presenter (HomePresenter)
     */
    interface Presenter {
        
        /**
         * Chamado quando o usuário clica no botão Efeitos
         */
        void onEffectsClicked();
        
        /**
         * Chamado quando o usuário clica no botão Looper
         */
        void onLooperClicked();
        
        /**
         * Chamado quando o usuário clica no botão Afinador
         */
        void onTunerClicked();
        
        /**
         * Chamado quando o usuário clica no botão Metrônomo
         */
        void onMetronomeClicked();
        
        /**
         * Chamado quando o usuário clica no botão Aprendizado
         */
        void onLearningClicked();
        
        /**
         * Chamado quando o usuário clica no botão Gravador
         */
        void onRecorderClicked();
        
        /**
         * Chamado quando o usuário clica no botão Configurações
         */
        void onSettingsClicked();
        
        /**
         * Chamado quando o usuário clica no botão Wi-Fi
         */
        void onWifiClicked();
        
        /**
         * Chamado quando o usuário clica no botão Volume
         */
        void onVolumeClicked();
        
        /**
         * Chamado quando o usuário clica no botão Power
         */
        void onPowerClicked();
        
        /**
         * Chamado quando a view é iniciada
         * Deve inicializar dados e começar atualizações
         */
        void onViewStarted();
        
        /**
         * Chamado quando a view é pausada
         * Deve parar atualizações para economizar recursos
         */
        void onViewPaused();
        
        /**
         * Chamado quando a view é resumida
         * Deve retomar atualizações
         */
        void onViewResumed();
        
        /**
         * Chamado quando a view é destruída
         * Deve limpar recursos e parar atualizações
         */
        void onViewDestroyed();
        
        /**
         * Atualiza o status do sistema
         * Chamado periodicamente para atualizar informações
         */
        void updateSystemStatus();
    }
} 