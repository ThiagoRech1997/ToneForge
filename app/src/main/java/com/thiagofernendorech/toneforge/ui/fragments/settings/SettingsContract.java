package com.thiagofernendorech.toneforge.ui.fragments.settings;

import com.thiagofernendorech.toneforge.ui.base.BaseView;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;

/**
 * Contrato MVP para o SettingsFragment
 * Define as interfaces View e Presenter para gerenciar as configurações do app
 */
public interface SettingsContract {

    /**
     * Interface View para o SettingsFragment
     * Define os métodos que a View deve implementar
     */
    interface View extends BaseView {
        
        /**
         * Atualiza o estado do switch de tema escuro
         */
        void setDarkThemeEnabled(boolean enabled);
        
        /**
         * Atualiza o estado do switch de vibração
         */
        void setVibrationEnabled(boolean enabled);
        
        /**
         * Atualiza o estado do switch de salvamento automático
         */
        void setAutoSaveEnabled(boolean enabled);
        
        /**
         * Atualiza o estado do switch de áudio em background
         */
        void setAudioBackgroundEnabled(boolean enabled);
        
        /**
         * Atualiza o modo de latência selecionado
         */
        void setLatencyMode(int mode);
        
        /**
         * Atualiza as informações de latência exibidas
         */
        void updateLatencyInfo(String modeName, String details);
        
        /**
         * Atualiza o estado do switch MIDI
         */
        void setMidiEnabled(boolean enabled);
        
        /**
         * Atualiza o status MIDI exibido
         */
        void updateMidiStatus(String status, String deviceInfo);
        
        /**
         * Mostra diálogo de informações sobre latência
         */
        void showLatencyInfoDialog();
        
        /**
         * Mostra diálogo de mapeamentos MIDI
         */
        void showMidiMappingsDialog();
        
        /**
         * Mostra diálogo de confirmação para limpar mapeamentos
         */
        void showClearMappingsConfirmDialog();
        
        /**
         * Mostra informações sobre o app
         */
        void showAboutInfo(String version, String developer);
        
        /**
         * Mostra mensagem de feedback
         */
        void showMessage(String message);
    }

    /**
     * Interface Presenter para o SettingsFragment
     * Define os métodos que o Presenter deve implementar
     */
    interface Presenter {
        
        /**
         * Carrega as configurações salvas
         */
        void loadSettings();
        
        /**
         * Salva a configuração de tema escuro
         */
        void setDarkTheme(boolean enabled);
        
        /**
         * Salva a configuração de vibração
         */
        void setVibration(boolean enabled);
        
        /**
         * Salva a configuração de salvamento automático
         */
        void setAutoSave(boolean enabled);
        
        /**
         * Gerencia o serviço de áudio em background
         */
        void setAudioBackground(boolean enabled);
        
        /**
         * Aplica novo modo de latência
         */
        void setLatencyMode(int mode);
        
        /**
         * Ativa/desativa o modo de aprendizado MIDI
         */
        void setMidiLearnMode(boolean enabled);
        
        /**
         * Escaneia dispositivos MIDI disponíveis
         */
        void scanMidiDevices();
        
        /**
         * Abre diálogo de informações de latência
         */
        void onLatencyInfoRequested();
        
        /**
         * Abre diálogo de mapeamentos MIDI
         */
        void onMidiMappingsRequested();
        
        /**
         * Limpa todos os mapeamentos MIDI
         */
        void clearMidiMappings();
        
        /**
         * Mostra informações sobre o app
         */
        void onAboutRequested();
        
        /**
         * Atualiza a UI quando o fragment é retomado
         */
        void onResume();
    }
} 