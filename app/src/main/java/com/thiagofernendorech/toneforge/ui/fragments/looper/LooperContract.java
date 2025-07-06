package com.thiagofernendorech.toneforge.ui.fragments.looper;

import com.thiagofernendorech.toneforge.ui.base.BaseView;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;
import com.thiagofernendorech.toneforge.LooperTrackAdapter;
import java.util.List;

/**
 * Contrato MVP para o LooperFragment
 * Define as interfaces de comunicação entre View e Presenter
 */
public interface LooperContract {
    
    /**
     * Interface da View (LooperFragment)
     */
    interface View extends BaseView {
        
        // === CONTROLE DE ESTADO ===
        
        /**
         * Atualiza o status do looper
         * @param status mensagem de status
         * @param isRecording true se está gravando
         * @param isPlaying true se está reproduzindo
         */
        void updateLooperStatus(String status, boolean isRecording, boolean isPlaying);
        
        /**
         * Atualiza o timer do looper
         * @param minutes minutos
         * @param seconds segundos
         */
        void updateLooperTimer(int minutes, int seconds);
        
        /**
         * Atualiza o contador de batidas
         * @param beatCount número de batidas
         */
        void updateBeatCount(int beatCount);
        
        /**
         * Atualiza a barra de progresso
         * @param progress progresso (0-100)
         */
        void updateProgress(int progress);
        
        /**
         * Atualiza o BPM exibido
         * @param bpm valor do BPM
         */
        void updateBPM(int bpm);
        
        // === CONTROLE DE GRAVAÇÃO/REPRODUÇÃO ===
        
        /**
         * Atualiza o botão de gravação
         * @param isRecording true se está gravando
         */
        void updateRecordButton(boolean isRecording);
        
        /**
         * Atualiza o botão de reprodução
         * @param isPlaying true se está reproduzindo
         */
        void updatePlayButton(boolean isPlaying);
        
        /**
         * Atualiza o estado do botão de sincronização
         * @param isSynced true se está sincronizado
         */
        void updateSyncButton(boolean isSynced);
        
        // === CONTROLE DE TRACKS ===
        
        /**
         * Atualiza a lista de tracks
         * @param tracks lista de tracks
         */
        void updateTracksList(List<LooperTrackAdapter.LooperTrack> tracks);
        
        /**
         * Adiciona uma nova track
         * @param track track a ser adicionada
         */
        void addTrack(LooperTrackAdapter.LooperTrack track);
        
        /**
         * Remove uma track
         * @param trackIndex índice da track
         */
        void removeTrack(int trackIndex);
        
        /**
         * Atualiza uma track específica
         * @param trackIndex índice da track
         * @param track dados atualizados da track
         */
        void updateTrack(int trackIndex, LooperTrackAdapter.LooperTrack track);
        
        // === CONTROLE DE EFEITOS ===
        
        /**
         * Atualiza o valor de speed
         * @param speed valor do speed (0.25x - 4.0x)
         */
        void updateSpeed(float speed);
        
        /**
         * Atualiza o valor de pitch
         * @param pitch valor do pitch em semitons (-12 a +12)
         */
        void updatePitch(float pitch);
        
        /**
         * Atualiza o estado do stutter
         * @param enabled true se stutter está ativo
         * @param rate taxa do stutter
         */
        void updateStutter(boolean enabled, float rate);
        
        /**
         * Atualiza o estado do reverse
         * @param enabled true se reverse está ativo
         */
        void updateReverse(boolean enabled);
        
        // === CONTROLE DE WAVEFORM ===
        
        /**
         * Atualiza os dados da waveform
         * @param waveformData dados da waveform
         */
        void updateWaveformData(float[] waveformData);
        
        /**
         * Atualiza a posição do playhead
         * @param position posição (0.0-1.0)
         */
        void updatePlayheadPosition(float position);
        
        /**
         * Atualiza as configurações da waveform
         * @param showGrid mostrar grade
         * @param showTimeGrid mostrar grade de tempo
         * @param showPlayhead mostrar playhead
         */
        void updateWaveformSettings(boolean showGrid, boolean showTimeGrid, boolean showPlayhead);
        
        // === CONTROLE DE SLICING ===
        
        /**
         * Atualiza o estado do slicing
         * @param enabled true se slicing está ativo
         * @param numSlices número de slices
         * @param sliceLength tamanho do slice
         */
        void updateSlicing(boolean enabled, int numSlices, int sliceLength);
        
        // === CONTROLE DE FILTROS ===
        
        /**
         * Atualiza filtros de áudio
         * @param lowPassEnabled filtro passa-baixa ativo
         * @param lowPassFreq frequência do passa-baixa
         * @param highPassEnabled filtro passa-alta ativo
         * @param highPassFreq frequência do passa-alta
         */
        void updateFilters(boolean lowPassEnabled, float lowPassFreq, boolean highPassEnabled, float highPassFreq);
        
        // === CONTROLE DE MARCADORES ===
        
        /**
         * Atualiza informações dos marcadores
         * @param totalMarkers total de marcadores
         * @param currentMarker marcador atual (0-based, -1 se nenhum)
         * @param markerTime tempo do marcador atual
         */
        void updateMarkersInfo(int totalMarkers, int currentMarker, float markerTime);
        
        /**
         * Atualiza estado dos botões de marcadores
         * @param canAdd pode adicionar marcador
         * @param canRemove pode remover marcador
         * @param canNavigate pode navegar entre marcadores
         */
        void updateMarkerButtons(boolean canAdd, boolean canRemove, boolean canNavigate);
        
        // === CONTROLE DE FADE ===
        
        /**
         * Atualiza configurações de fade
         * @param fadeInEnabled fade in ativo
         * @param fadeInDuration duração do fade in
         * @param fadeOutEnabled fade out ativo
         * @param fadeOutDuration duração do fade out
         */
        void updateFadeSettings(boolean fadeInEnabled, float fadeInDuration, boolean fadeOutEnabled, float fadeOutDuration);
        
        // === DIÁLOGOS ===
        
        /**
         * Mostra diálogo para salvar loop
         */
        void showSaveLoopDialog();
        
        /**
         * Mostra diálogo para carregar loop
         */
        void showLoadLoopDialog();
        
        /**
         * Mostra diálogo de export
         */
        void showExportDialog();
        
        /**
         * Mostra confirmação de limpeza
         */
        void showClearConfirmation();
        
        /**
         * Mostra mensagem de sucesso
         * @param message mensagem
         */
        void showSuccessMessage(String message);
        
        /**
         * Abre biblioteca de loops
         */
        void openLoopLibrary();
        
        /**
         * Abre seletor de arquivo
         * @param requestCode código da requisição
         */
        void openFilePicker(int requestCode);
        
        /**
         * Abre criador de arquivo
         * @param fileName nome do arquivo
         * @param requestCode código da requisição
         */
        void openFileCreator(String fileName, int requestCode);
    }
    
    /**
     * Interface do Presenter (LooperPresenter)
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
        
        // === CONTROLE DE GRAVAÇÃO/REPRODUÇÃO ===
        
        /**
         * Alterna o estado de gravação
         */
        void toggleRecording();
        
        /**
         * Alterna o estado de reprodução
         */
        void togglePlayback();
        
        /**
         * Limpa o loop atual
         */
        void clearLoop();
        
        /**
         * Executa undo
         */
        void undo();
        
        /**
         * Executa redo
         */
        void redo();
        
        /**
         * Alterna sincronização
         */
        void toggleSync();
        
        /**
         * Atualiza BPM
         * @param bpm novo valor de BPM
         */
        void setBPM(int bpm);
        
        /**
         * Executa tap tempo
         */
        void tapTempo();
        
        // === CONTROLE DE TRACKS ===
        
        /**
         * Altera volume de uma track
         * @param trackIndex índice da track
         * @param volume novo volume (0.0-1.0)
         */
        void setTrackVolume(int trackIndex, float volume);
        
        /**
         * Alterna mute de uma track
         * @param trackIndex índice da track
         * @param muted estado do mute
         */
        void setTrackMuted(int trackIndex, boolean muted);
        
        /**
         * Alterna solo de uma track
         * @param trackIndex índice da track
         * @param soloed estado do solo
         */
        void setTrackSoloed(int trackIndex, boolean soloed);
        
        /**
         * Deleta uma track
         * @param trackIndex índice da track
         */
        void deleteTrack(int trackIndex);
        
        // === CONTROLE DE EFEITOS ===
        
        /**
         * Altera a velocidade de reprodução
         * @param speed nova velocidade (0.25x - 4.0x)
         */
        void setSpeed(float speed);
        
        /**
         * Altera o pitch
         * @param pitch novo pitch em semitons (-12 a +12)
         */
        void setPitch(float pitch);
        
        /**
         * Ativa/desativa reverse
         * @param enabled true para ativar
         */
        void setReverse(boolean enabled);
        
        /**
         * Ativa/desativa stutter
         * @param enabled true para ativar
         * @param rate taxa do stutter
         */
        void setStutter(boolean enabled, float rate);
        
        // === CONTROLE DE WAVEFORM ===
        
        /**
         * Ativa/desativa grade na waveform
         * @param enabled true para ativar
         */
        void setWaveformGrid(boolean enabled);
        
        /**
         * Ativa/desativa grade de tempo na waveform
         * @param enabled true para ativar
         */
        void setWaveformTimeGrid(boolean enabled);
        
        /**
         * Ativa/desativa playhead na waveform
         * @param enabled true para ativar
         */
        void setWaveformPlayhead(boolean enabled);
        
        /**
         * Clique na waveform
         * @param position posição clicada (0.0-1.0)
         */
        void onWaveformClicked(float position);
        
        // === CONTROLE DE SLICING ===
        
        /**
         * Ativa/desativa slicing
         * @param enabled true para ativar
         */
        void setSlicing(boolean enabled);
        
        /**
         * Randomiza slices
         */
        void randomizeSlices();
        
        /**
         * Inverte slices
         */
        void reverseSlices();
        
        // === CONTROLE DE FILTROS ===
        
        /**
         * Ativa/desativa filtro passa-baixa
         * @param enabled true para ativar
         */
        void setLowPassFilter(boolean enabled);
        
        /**
         * Altera frequência do filtro passa-baixa
         * @param frequency nova frequência
         */
        void setLowPassFrequency(float frequency);
        
        /**
         * Ativa/desativa filtro passa-alta
         * @param enabled true para ativar
         */
        void setHighPassFilter(boolean enabled);
        
        /**
         * Altera frequência do filtro passa-alta
         * @param frequency nova frequência
         */
        void setHighPassFrequency(float frequency);
        
        // === CONTROLE DE MARCADORES ===
        
        /**
         * Adiciona marcador na posição atual
         */
        void addMarker();
        
        /**
         * Remove marcador atual
         */
        void removeMarker();
        
        /**
         * Vai para marcador anterior
         */
        void goToPreviousMarker();
        
        /**
         * Vai para próximo marcador
         */
        void goToNextMarker();
        
        // === CONTROLE DE FADE ===
        
        /**
         * Ativa/desativa fade in automático
         * @param enabled true para ativar
         */
        void setAutoFadeIn(boolean enabled);
        
        /**
         * Altera duração do fade in
         * @param duration nova duração em segundos
         */
        void setFadeInDuration(float duration);
        
        /**
         * Ativa/desativa fade out automático
         * @param enabled true para ativar
         */
        void setAutoFadeOut(boolean enabled);
        
        /**
         * Altera duração do fade out
         * @param duration nova duração em segundos
         */
        void setFadeOutDuration(float duration);
        
        // === CONTROLE DE ARQUIVOS ===
        
        /**
         * Salva loop atual
         * @param fileName nome do arquivo
         */
        void saveLoop(String fileName);
        
        /**
         * Carrega loop de arquivo
         * @param fileName nome do arquivo
         */
        void loadLoop(String fileName);
        
        /**
         * Exporta loop
         * @param fileName nome do arquivo
         * @param format formato de export
         */
        void exportLoop(String fileName, String format);
        
        /**
         * Abre biblioteca de loops
         */
        void openLibrary();
        
        /**
         * Processa resultado de seleção de arquivo
         * @param requestCode código da requisição
         * @param resultCode código do resultado
         * @param filePath caminho do arquivo selecionado
         */
        void handleFileResult(int requestCode, int resultCode, String filePath);
        
        // === OUTROS ===
        
        /**
         * Força atualização da UI
         */
        void forceUpdateUI();
        
        /**
         * Verifica permissões de áudio
         */
        void checkAudioPermissions();
    }
} 