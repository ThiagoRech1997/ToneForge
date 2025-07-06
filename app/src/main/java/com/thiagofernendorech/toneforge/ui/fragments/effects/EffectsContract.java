package com.thiagofernendorech.toneforge.ui.fragments.effects;

import com.thiagofernendorech.toneforge.ui.base.BaseView;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;
import com.thiagofernendorech.toneforge.domain.models.AudioState;
import java.util.List;

/**
 * Contrato MVP para o EffectsFragment
 * Define as interfaces de comunicação entre View e Presenter
 */
public interface EffectsContract {
    
    /**
     * Interface da View (EffectsFragment)
     */
    interface View extends BaseView {
        
        // === CONTROLE DE ESTADO ===
        
        /**
         * Atualiza o status geral dos efeitos
         * @param status mensagem de status
         * @param isActive true se pipeline está ativo
         */
        void updateEffectsStatus(String status, boolean isActive);
        
        /**
         * Atualiza o estado do áudio
         * @param audioState estado atual do áudio
         */
        void updateAudioState(AudioState audioState);
        
        // === CONTROLE DE EFEITOS ===
        
        /**
         * Atualiza os parâmetros de todos os efeitos
         * @param parameters parâmetros atuais
         */
        void updateEffectParameters(EffectParameters parameters);
        
        /**
         * Atualiza o estado de um efeito específico
         * @param effectName nome do efeito
         * @param enabled true se está ativo
         */
        void updateEffectState(String effectName, boolean enabled);
        
        /**
         * Atualiza indicadores visuais de bypass
         */
        void updateBypassIndicators();
        
        /**
         * Atualiza a ordem dos efeitos
         * @param effectOrder nova ordem dos efeitos
         */
        void updateEffectOrder(List<String> effectOrder);
        
        // === PRESETS ===
        
        /**
         * Atualiza a lista de presets
         * @param presets lista de nomes de presets
         */
        void updatePresetList(List<String> presets);
        
        /**
         * Seleciona um preset na interface
         * @param presetName nome do preset
         */
        void selectPreset(String presetName);
        
        /**
         * Mostra diálogo para salvar preset
         */
        void showSavePresetDialog();
        
        /**
         * Mostra diálogo para deletar preset
         * @param presetName nome do preset
         */
        void showDeletePresetConfirmation(String presetName);
        
        /**
         * Mostra diálogo de export de preset
         */
        void showExportPresetDialog();
        
        /**
         * Atualiza o botão de favoritos
         * @param isFavorite true se o preset atual é favorito
         */
        void updateFavoriteButton(boolean isFavorite);
        
        /**
         * Atualiza filtro de favoritos
         * @param showFavoritesOnly true se mostrando apenas favoritos
         */
        void updateFavoritesFilter(boolean showFavoritesOnly);
        
        // === AUTOMAÇÃO ===
        
        /**
         * Atualiza status da automação
         * @param isRecording true se está gravando
         * @param isPlaying true se está reproduzindo
         * @param statusText texto de status
         */
        void updateAutomationStatus(boolean isRecording, boolean isPlaying, String statusText);
        
        /**
         * Atualiza progresso da automação
         * @param progress progresso (0-100)
         * @param timeText texto do tempo atual
         */
        void updateAutomationProgress(int progress, String timeText);
        
        /**
         * Atualiza lista de automações
         * @param automations lista de nomes de automações
         */
        void updateAutomationList(List<String> automations);
        
        /**
         * Mostra diálogo para salvar automação
         */
        void showSaveAutomationDialog();
        
        /**
         * Mostra diálogo de export de automação
         */
        void showExportAutomationDialog();
        
        // === MIDI ===
        
        /**
         * Atualiza status MIDI
         * @param enabled true se MIDI está ativo
         * @param deviceCount número de dispositivos conectados
         */
        void updateMidiStatus(boolean enabled, int deviceCount);
        
        /**
         * Mostra feedback de MIDI learn
         * @param parameterName parâmetro sendo mapeado
         * @param isActive true se learn está ativo
         */
        void showMidiLearnFeedback(String parameterName, boolean isActive);
        
        // === OUTROS ===
        
        /**
         * Mostra tooltip
         * @param message mensagem do tooltip
         */
        void showTooltip(String message);
        
        /**
         * Solicita permissão de áudio
         */
        void requestAudioPermission();
        
        /**
         * Abre seletor de arquivo para import
         * @param requestCode código da requisição
         */
        void openFilePicker(int requestCode);
        
        /**
         * Abre seletor de diretório para export
         * @param fileName nome do arquivo
         * @param requestCode código da requisição
         */
        void openFileCreator(String fileName, int requestCode);
    }
    
    /**
     * Interface do Presenter (EffectsPresenter)
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
        
        // === CONTROLE DE EFEITOS ===
        
        /**
         * Ativa/desativa um efeito
         * @param effectName nome do efeito
         * @param enabled true para ativar
         */
        void setEffectEnabled(String effectName, boolean enabled);
        
        /**
         * Altera parâmetro de um efeito
         * @param effectName nome do efeito
         * @param parameterName nome do parâmetro
         * @param value valor (0.0-1.0)
         */
        void setEffectParameter(String effectName, String parameterName, float value);
        
        /**
         * Reseta um efeito específico
         * @param effectName nome do efeito
         */
        void resetEffect(String effectName);
        
        /**
         * Reseta todos os efeitos
         */
        void resetAllEffects();
        
        /**
         * Altera a ordem dos efeitos
         * @param fromPosition posição original
         * @param toPosition nova posição
         */
        void moveEffect(int fromPosition, int toPosition);
        
        // === PRESETS ===
        
        /**
         * Carrega um preset
         * @param presetName nome do preset
         */
        void loadPreset(String presetName);
        
        /**
         * Salva preset atual
         * @param presetName nome do preset
         */
        void savePreset(String presetName);
        
        /**
         * Deleta um preset
         * @param presetName nome do preset
         */
        void deletePreset(String presetName);
        
        /**
         * Exporta um preset
         * @param presetName nome do preset
         * @param filePath caminho do arquivo
         */
        void exportPreset(String presetName, String filePath);
        
        /**
         * Importa preset de arquivo
         * @param filePath caminho do arquivo
         */
        void importPreset(String filePath);
        
        /**
         * Marca/desmarca preset como favorito
         * @param presetName nome do preset
         */
        void togglePresetFavorite(String presetName);
        
        /**
         * Ativa/desativa filtro de favoritos
         */
        void toggleFavoritesFilter();
        
        // === AUTOMAÇÃO ===
        
        /**
         * Inicia gravação de automação
         * @param automationName nome da automação
         */
        void startAutomationRecording(String automationName);
        
        /**
         * Para gravação de automação
         */
        void stopAutomationRecording();
        
        /**
         * Inicia reprodução de automação
         * @param automationName nome da automação
         */
        void startAutomationPlayback(String automationName);
        
        /**
         * Para reprodução de automação
         */
        void stopAutomationPlayback();
        
        /**
         * Exporta automação
         * @param automationName nome da automação
         * @param filePath caminho do arquivo
         */
        void exportAutomation(String automationName, String filePath);
        
        /**
         * Importa automação de arquivo
         * @param filePath caminho do arquivo
         */
        void importAutomation(String filePath);
        
        /**
         * Deleta uma automação
         * @param automationName nome da automação
         */
        void deleteAutomation(String automationName);
        
        // === MIDI ===
        
        /**
         * Ativa/desativa MIDI
         * @param enabled true para ativar
         */
        void setMidiEnabled(boolean enabled);
        
        /**
         * Inicia MIDI learn para um parâmetro
         * @param parameterName nome do parâmetro
         */
        void startMidiLearn(String parameterName);
        
        /**
         * Para MIDI learn
         */
        void stopMidiLearn();
        
        /**
         * Aplica parâmetro vindo do MIDI
         * @param parameterName nome do parâmetro
         * @param value valor (0.0-1.0)
         */
        void applyMidiParameter(String parameterName, float value);
        
        // === OUTROS ===
        
        /**
         * Verifica permissões de áudio
         */
        void checkAudioPermissions();
        
        /**
         * Atualiza status dos efeitos
         */
        void updateStatus();
        
        /**
         * Processa resultado de seleção de arquivo
         * @param requestCode código da requisição
         * @param resultCode código do resultado
         * @param filePath caminho do arquivo selecionado
         */
        void handleFileResult(int requestCode, int resultCode, String filePath);
    }
} 