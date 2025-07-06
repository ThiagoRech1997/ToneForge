package com.thiagofernendorech.toneforge.ui.fragments.looper;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.AudioEngine;
import com.thiagofernendorech.toneforge.LooperTrackAdapter;
import com.thiagofernendorech.toneforge.LoopLibraryManager;
import com.thiagofernendorech.toneforge.LoopExportManager;
import com.thiagofernendorech.toneforge.LoopLoadUtil;
import com.thiagofernendorech.toneforge.PermissionManager;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Presenter para o LooperFragment
 * Gerencia toda a lógica de negócio do looper
 */
public class LooperPresenter extends BasePresenter<LooperContract.View> implements LooperContract.Presenter {
    
    private Context context;
    private AudioRepository audioRepository;
    
    // Managers específicos
    private PermissionManager permissionManager;
    
    // Estado interno do looper
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean isSynced = false;
    private int currentBPM = 120;
    private int loopLength = 0; // em samples
    private int currentPosition = 0; // em samples
    private int beatCount = 0;
    
    // Efeitos
    private boolean reverseEnabled = false;
    private float currentSpeed = 1.0f;
    private float currentPitch = 0.0f;
    private boolean stutterEnabled = false;
    private float stutterRate = 1.0f;
    
    // Filtros
    private boolean lowPassEnabled = false;
    private float lowPassFreq = 20000.0f;
    private boolean highPassEnabled = false;
    private float highPassFreq = 20.0f;
    
    // Fade
    private boolean fadeInEnabled = false;
    private float fadeInDuration = 0.1f;
    private boolean fadeOutEnabled = false;
    private float fadeOutDuration = 0.1f;
    
    // Waveform
    private boolean showGrid = true;
    private boolean showTimeGrid = true;
    private boolean showPlayhead = true;
    
    // Slicing
    private boolean slicingEnabled = false;
    private int numSlices = 8;
    private int sliceLength = 0;
    
    // Marcadores
    private List<Float> markers = new ArrayList<>();
    private int currentMarkerIndex = -1;
    
    // Tracks
    private List<LooperTrackAdapter.LooperTrack> tracks = new ArrayList<>();
    
    // Undo/Redo
    private Stack<LooperState> undoStack = new Stack<>();
    private Stack<LooperState> redoStack = new Stack<>();
    
    // Tap tempo
    private List<Long> tapTimes = new ArrayList<>();
    private static final int MAX_TAP_TIMES = 4;
    
    // Handler para atualizações periódicas
    private Handler updateHandler;
    private Runnable updateRunnable;
    private static final int UPDATE_INTERVAL_MS = 50; // 50ms para suavidade
    private boolean isUpdating = false;
    
    // Constantes para códigos de arquivo
    public static final int SAVE_LOOP_REQUEST = 2001;
    public static final int LOAD_LOOP_REQUEST = 2002;
    public static final int EXPORT_LOOP_REQUEST = 2003;
    
    /**
     * Construtor do LooperPresenter
     * @param context contexto da aplicação
     * @param audioRepository repository de áudio
     */
    public LooperPresenter(Context context, AudioRepository audioRepository) {
        this.context = context.getApplicationContext();
        this.audioRepository = audioRepository;
        
        // Inicializar managers
        initializeManagers();
        
        // Configurar atualizações periódicas
        setupPeriodicUpdates();
    }
    
    /**
     * Inicializa todos os managers necessários
     */
    private void initializeManagers() {
        // LoopLibraryManager e LoopExportManager são classes estáticas, não precisam de instância
        permissionManager = new PermissionManager();
    }
    
    /**
     * Configura atualizações periódicas
     */
    private void setupPeriodicUpdates() {
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isUpdating && isViewAttached()) {
                    updateCurrentState();
                    updateHandler.postDelayed(this, UPDATE_INTERVAL_MS);
                }
            }
        };
    }
    
    // === Estado interno ===
    
    /**
     * Estado do looper para undo/redo
     */
    private static class LooperState {
        List<LooperTrackAdapter.LooperTrack> tracks;
        int loopLength;
        int currentPosition;
        int beatCount;
        List<Float> markers;
        
        LooperState(List<LooperTrackAdapter.LooperTrack> tracks, int loopLength, 
                   int currentPosition, int beatCount, List<Float> markers) {
            this.tracks = new ArrayList<>();
            for (LooperTrackAdapter.LooperTrack track : tracks) {
                LooperTrackAdapter.LooperTrack trackCopy = new LooperTrackAdapter.LooperTrack(track.id, track.name);
                trackCopy.volume = track.volume;
                trackCopy.muted = track.muted;
                trackCopy.soloed = track.soloed;
                trackCopy.length = track.length;
                this.tracks.add(trackCopy);
            }
            this.loopLength = loopLength;
            this.currentPosition = currentPosition;
            this.beatCount = beatCount;
            this.markers = new ArrayList<>(markers);
        }
    }
    
    // === Implementação da interface LooperContract.Presenter ===
    
    @Override
    public void onViewStarted() {
        // Verificar permissões
        checkAudioPermissions();
        
        // Carregar estado atual
        loadCurrentState();
        
        // Iniciar atualizações
        startPeriodicUpdates();
    }
    
    @Override
    public void onViewResumed() {
        // Retomar atualizações
        startPeriodicUpdates();
        
        // Verificar se há loop carregado
        int currentLength = AudioEngine.getLooperLength();
        if (currentLength > 0 && loopLength == 0) {
            updateAfterLoopLoad();
        }
    }
    
    @Override
    public void onViewPaused() {
        // Pausar atualizações
        stopPeriodicUpdates();
    }
    
    @Override
    public void onViewDestroyed() {
        // Parar todas as atualizações
        stopPeriodicUpdates();
        
        // Limpar handler
        if (updateHandler != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }
    
    // === CONTROLE DE GRAVAÇÃO/REPRODUÇÃO ===
    
    @Override
    public void toggleRecording() {
        if (!isRecording) {
            startRecording();
        } else {
            stopRecording();
        }
    }
    
    @Override
    public void togglePlayback() {
        if (!isPlaying) {
            startPlayback();
        } else {
            stopPlayback();
        }
    }
    
    @Override
    public void clearLoop() {
        if (isViewAttached()) {
            getView().showClearConfirmation();
        }
    }
    
    @Override
    public void undo() {
        if (!undoStack.isEmpty()) {
            // Salvar estado atual para redo
            saveCurrentStateForRedo();
            
            // Restaurar estado anterior
            LooperState previousState = undoStack.pop();
            restoreState(previousState);
            
            if (isViewAttached()) {
                getView().showSuccessMessage("Undo realizado");
            }
        }
    }
    
    @Override
    public void redo() {
        if (!redoStack.isEmpty()) {
            // Salvar estado atual para undo
            saveCurrentStateForUndo();
            
            // Restaurar estado seguinte
            LooperState nextState = redoStack.pop();
            restoreState(nextState);
            
            if (isViewAttached()) {
                getView().showSuccessMessage("Redo realizado");
            }
        }
    }
    
    @Override
    public void toggleSync() {
        isSynced = !isSynced;
        AudioEngine.setLooperSyncEnabled(isSynced);
        
        if (isViewAttached()) {
            getView().updateSyncButton(isSynced);
        }
    }
    
    @Override
    public void setBPM(int bpm) {
        if (bpm >= 60 && bpm <= 200) {
            currentBPM = bpm;
            AudioEngine.setLooperBPM(bpm);
            
            if (isViewAttached()) {
                getView().updateBPM(bpm);
            }
        }
    }
    
    @Override
    public void tapTempo() {
        long currentTime = System.currentTimeMillis();
        
        // Limpar tempos antigos (mais de 3 segundos)
        tapTimes.removeIf(time -> currentTime - time > 3000);
        
        // Adicionar tempo atual
        tapTimes.add(currentTime);
        
        // Manter apenas os últimos tempos
        if (tapTimes.size() > MAX_TAP_TIMES) {
            tapTimes.remove(0);
        }
        
        // Calcular BPM se temos pelo menos 2 tempos
        if (tapTimes.size() >= 2) {
            long totalTime = tapTimes.get(tapTimes.size() - 1) - tapTimes.get(0);
            int intervals = tapTimes.size() - 1;
            double averageInterval = (double) totalTime / intervals;
            int calculatedBPM = (int) Math.round(60000.0 / averageInterval);
            
            // Validar BPM
            if (calculatedBPM >= 60 && calculatedBPM <= 200) {
                setBPM(calculatedBPM);
            }
        }
    }
    
    // === CONTROLE DE TRACKS ===
    
    @Override
    public void setTrackVolume(int trackIndex, float volume) {
        if (trackIndex >= 0 && trackIndex < tracks.size()) {
            tracks.get(trackIndex).volume = volume;
            AudioEngine.setLooperTrackVolume(trackIndex, volume);
            
            if (isViewAttached()) {
                getView().updateTrack(trackIndex, tracks.get(trackIndex));
            }
        }
    }
    
    @Override
    public void setTrackMuted(int trackIndex, boolean muted) {
        if (trackIndex >= 0 && trackIndex < tracks.size()) {
            tracks.get(trackIndex).muted = muted;
            AudioEngine.setLooperTrackMuted(trackIndex, muted);
            
            if (isViewAttached()) {
                getView().updateTrack(trackIndex, tracks.get(trackIndex));
            }
        }
    }
    
    @Override
    public void setTrackSoloed(int trackIndex, boolean soloed) {
        if (trackIndex >= 0 && trackIndex < tracks.size()) {
            tracks.get(trackIndex).soloed = soloed;
            AudioEngine.setLooperTrackSoloed(trackIndex, soloed);
            
            if (isViewAttached()) {
                getView().updateTrack(trackIndex, tracks.get(trackIndex));
            }
        }
    }
    
    @Override
    public void deleteTrack(int trackIndex) {
        if (trackIndex >= 0 && trackIndex < tracks.size()) {
            // Salvar estado para undo
            saveCurrentStateForUndo();
            
            // Remover track
            tracks.remove(trackIndex);
            AudioEngine.removeLooperTrack(trackIndex);
            
            if (isViewAttached()) {
                getView().removeTrack(trackIndex);
                getView().showSuccessMessage("Track removida");
            }
        }
    }
    
    // === CONTROLE DE EFEITOS ===
    
    @Override
    public void setSpeed(float speed) {
        if (speed >= 0.25f && speed <= 4.0f) {
            currentSpeed = speed;
            AudioEngine.setLooperSpeed(speed);
            
            if (isViewAttached()) {
                getView().updateSpeed(speed);
            }
        }
    }
    
    @Override
    public void setPitch(float pitch) {
        if (pitch >= -12.0f && pitch <= 12.0f) {
            currentPitch = pitch;
            AudioEngine.setLooperPitchShift(pitch);
            
            if (isViewAttached()) {
                getView().updatePitch(pitch);
            }
        }
    }
    
    @Override
    public void setReverse(boolean enabled) {
        reverseEnabled = enabled;
        AudioEngine.setLooperReverse(enabled);
        
        if (isViewAttached()) {
            getView().updateReverse(enabled);
        }
    }
    
    @Override
    public void setStutter(boolean enabled, float rate) {
        stutterEnabled = enabled;
        stutterRate = rate;
        AudioEngine.setLooperStutter(enabled, rate);
        
        if (isViewAttached()) {
            getView().updateStutter(enabled, rate);
        }
    }
    
    // === CONTROLE DE WAVEFORM ===
    
    @Override
    public void setWaveformGrid(boolean enabled) {
        showGrid = enabled;
        if (isViewAttached()) {
            getView().updateWaveformSettings(showGrid, showTimeGrid, showPlayhead);
        }
    }
    
    @Override
    public void setWaveformTimeGrid(boolean enabled) {
        showTimeGrid = enabled;
        if (isViewAttached()) {
            getView().updateWaveformSettings(showGrid, showTimeGrid, showPlayhead);
        }
    }
    
    @Override
    public void setWaveformPlayhead(boolean enabled) {
        showPlayhead = enabled;
        if (isViewAttached()) {
            getView().updateWaveformSettings(showGrid, showTimeGrid, showPlayhead);
        }
    }
    
    @Override
    public void onWaveformClicked(float position) {
        if (loopLength > 0) {
            int targetPosition = (int) (position * loopLength);
            AudioEngine.setLooperPosition(targetPosition);
            currentPosition = targetPosition;
        }
    }
    
    // === CONTROLE DE SLICING ===
    
    @Override
    public void setSlicing(boolean enabled) {
        slicingEnabled = enabled;
        AudioEngine.setLooperSlicingEnabled(enabled);
        
        if (enabled) {
            updateSlicingInfo();
        }
        
        if (isViewAttached()) {
            getView().updateSlicing(enabled, numSlices, sliceLength);
        }
    }
    
    @Override
    public void randomizeSlices() {
        if (slicingEnabled) {
            AudioEngine.randomizeLooperSlices();
            updateSlicingInfo();
        }
    }
    
    @Override
    public void reverseSlices() {
        if (slicingEnabled) {
            AudioEngine.reverseLooperSlices();
            updateSlicingInfo();
        }
    }
    
    // === CONTROLE DE FILTROS ===
    
    @Override
    public void setLowPassFilter(boolean enabled) {
        lowPassEnabled = enabled;
        AudioEngine.setLooperLowPassFilter(enabled);
        updateFiltersView();
    }
    
    @Override
    public void setLowPassFrequency(float frequency) {
        lowPassFreq = frequency;
        AudioEngine.setLooperLowPassFrequency(frequency);
        updateFiltersView();
    }
    
    @Override
    public void setHighPassFilter(boolean enabled) {
        highPassEnabled = enabled;
        AudioEngine.setLooperHighPassFilter(enabled);
        updateFiltersView();
    }
    
    @Override
    public void setHighPassFrequency(float frequency) {
        highPassFreq = frequency;
        AudioEngine.setLooperHighPassFrequency(frequency);
        updateFiltersView();
    }
    
    // === CONTROLE DE MARCADORES ===
    
    @Override
    public void addMarker() {
        if (loopLength > 0) {
            float position = (float) currentPosition / loopLength;
            markers.add(position);
            currentMarkerIndex = markers.size() - 1;
            
            updateMarkersView();
            
            if (isViewAttached()) {
                getView().showSuccessMessage("Marcador adicionado");
            }
        }
    }
    
    @Override
    public void removeMarker() {
        if (currentMarkerIndex >= 0 && currentMarkerIndex < markers.size()) {
            markers.remove(currentMarkerIndex);
            if (currentMarkerIndex >= markers.size()) {
                currentMarkerIndex = markers.size() - 1;
            }
            
            updateMarkersView();
            
            if (isViewAttached()) {
                getView().showSuccessMessage("Marcador removido");
            }
        }
    }
    
    @Override
    public void goToPreviousMarker() {
        if (!markers.isEmpty()) {
            if (currentMarkerIndex <= 0) {
                currentMarkerIndex = markers.size() - 1;
            } else {
                currentMarkerIndex--;
            }
            
            jumpToMarker(currentMarkerIndex);
        }
    }
    
    @Override
    public void goToNextMarker() {
        if (!markers.isEmpty()) {
            if (currentMarkerIndex >= markers.size() - 1) {
                currentMarkerIndex = 0;
            } else {
                currentMarkerIndex++;
            }
            
            jumpToMarker(currentMarkerIndex);
        }
    }
    
    // === CONTROLE DE FADE ===
    
    @Override
    public void setAutoFadeIn(boolean enabled) {
        fadeInEnabled = enabled;
        AudioEngine.setLooperAutoFadeIn(enabled);
        updateFadeView();
    }
    
    @Override
    public void setFadeInDuration(float duration) {
        fadeInDuration = duration;
        AudioEngine.setLooperFadeInDuration(duration);
        updateFadeView();
    }
    
    @Override
    public void setAutoFadeOut(boolean enabled) {
        fadeOutEnabled = enabled;
        AudioEngine.setLooperAutoFadeOut(enabled);
        updateFadeView();
    }
    
    @Override
    public void setFadeOutDuration(float duration) {
        fadeOutDuration = duration;
        AudioEngine.setLooperFadeOutDuration(duration);
        updateFadeView();
    }
    
    // === CONTROLE DE ARQUIVOS ===
    
    @Override
    public void saveLoop(String fileName) {
        if (loopLength > 0) {
            // Implementação simplificada - por enquanto apenas simular sucesso
            boolean success = true; // TODO: Implementar salvamento real
            
            if (isViewAttached()) {
                if (success) {
                    getView().showSuccessMessage("Loop salvo: " + fileName);
                } else {
                    getView().showError("Erro ao salvar loop");
                }
            }
        }
    }
    
    @Override
    public void loadLoop(String fileName) {
        // Implementação simplificada - por enquanto apenas simular sucesso
        boolean success = true; // TODO: Implementar carregamento real
        
        if (success) {
            updateAfterLoopLoad();
            if (isViewAttached()) {
                getView().showSuccessMessage("Loop carregado: " + fileName);
            }
        } else {
            if (isViewAttached()) {
                getView().showError("Erro ao carregar loop");
            }
        }
    }
    
    @Override
    public void exportLoop(String fileName, String format) {
        if (loopLength > 0) {
            // Usar LoopExportManager
            LoopExportManager.ExportFormat exportFormat = LoopExportManager.ExportFormat.valueOf(format);
            LoopExportManager.exportLoop(context, exportFormat, fileName, new LoopExportManager.ExportCallback() {
                @Override
                public void onExported(boolean success, String fileName, String format) {
                    if (isViewAttached()) {
                        if (success) {
                            getView().showSuccessMessage("Loop exportado: " + fileName);
                        } else {
                            getView().showError("Erro ao exportar loop");
                        }
                    }
                }
            });
        }
    }
    
    @Override
    public void openLibrary() {
        if (isViewAttached()) {
            getView().openLoopLibrary();
        }
    }
    
    @Override
    public void handleFileResult(int requestCode, int resultCode, String filePath) {
        if (resultCode == android.app.Activity.RESULT_OK && filePath != null) {
            switch (requestCode) {
                case LOAD_LOOP_REQUEST:
                    loadLoop(filePath);
                    break;
                case EXPORT_LOOP_REQUEST:
                    exportLoop(filePath, "WAV");
                    break;
            }
        }
    }
    
    // === OUTROS ===
    
    @Override
    public void forceUpdateUI() {
        updateCurrentState();
    }
    
    @Override
    public void checkAudioPermissions() {
        // Implementação simplificada
        if (isViewAttached()) {
            getView().showLoading();
            getView().hideLoading();
        }
    }
    
    // === Métodos privados ===
    
    private void startRecording() {
        if (!isRecording) {
            // Salvar estado para undo
            saveCurrentStateForUndo();
            
            isRecording = true;
            AudioEngine.startLooperRecording();
            
            if (isViewAttached()) {
                getView().updateRecordButton(true);
                getView().updateLooperStatus("Gravando...", true, isPlaying);
            }
        }
    }
    
    private void stopRecording() {
        if (isRecording) {
            isRecording = false;
            AudioEngine.stopLooperRecording();
            
            // Atualizar comprimento do loop
            loopLength = AudioEngine.getLooperLength();
            
            // Criar nova track
            if (loopLength > 0) {
                LooperTrackAdapter.LooperTrack newTrack = new LooperTrackAdapter.LooperTrack(
                    tracks.size() + 1, "Track " + (tracks.size() + 1));
                newTrack.length = loopLength;
                tracks.add(newTrack);
                
                if (isViewAttached()) {
                    getView().addTrack(newTrack);
                }
            }
            
            if (isViewAttached()) {
                getView().updateRecordButton(false);
                getView().updateLooperStatus("Parado", false, isPlaying);
            }
        }
    }
    
    private void startPlayback() {
        if (!isPlaying && loopLength > 0) {
            isPlaying = true;
            AudioEngine.startLooperPlayback();
            
            if (isViewAttached()) {
                getView().updatePlayButton(true);
                getView().updateLooperStatus("Reproduzindo", isRecording, true);
            }
        }
    }
    
    private void stopPlayback() {
        if (isPlaying) {
            isPlaying = false;
            AudioEngine.stopLooperPlayback();
            
            if (isViewAttached()) {
                getView().updatePlayButton(false);
                getView().updateLooperStatus("Parado", isRecording, false);
            }
        }
    }
    
    private void loadCurrentState() {
        // Carregar estado do AudioEngine
        loopLength = AudioEngine.getLooperLength();
        currentPosition = AudioEngine.getLooperPosition();
        
        // Atualizar view
        updateAllViews();
    }
    
    private void updateCurrentState() {
        if (isRecording || isPlaying) {
            // Atualizar posição
            currentPosition = AudioEngine.getLooperPosition();
            
            // Calcular tempo
            float seconds = (float) currentPosition / 48000.0f;
            int minutes = (int) (seconds / 60);
            int secs = (int) (seconds % 60);
            
            // Calcular progresso
            int progress = 0;
            if (loopLength > 0) {
                progress = (int) ((float) currentPosition / loopLength * 100);
            }
            
            // Atualizar view
            if (isViewAttached()) {
                getView().updateLooperTimer(minutes, secs);
                getView().updateProgress(progress);
                getView().updatePlayheadPosition((float) currentPosition / loopLength);
                
                // Atualizar waveform
                float[] waveformData = AudioEngine.getLooperMix();
                if (waveformData != null) {
                    getView().updateWaveformData(waveformData);
                }
            }
        }
    }
    
    private void updateAfterLoopLoad() {
        loopLength = AudioEngine.getLooperLength();
        currentPosition = 0;
        
        // Calcular batidas
        if (loopLength > 0) {
            float seconds = (float) loopLength / 48000.0f;
            beatCount = (int) (seconds * currentBPM / 60.0f);
        }
        
        // Criar track carregada
        tracks.clear();
        LooperTrackAdapter.LooperTrack loadedTrack = new LooperTrackAdapter.LooperTrack(1, "Loop Carregado");
        loadedTrack.length = loopLength;
        tracks.add(loadedTrack);
        
        updateAllViews();
    }
    
    private void updateAllViews() {
        if (isViewAttached()) {
            getView().updateLooperStatus("Pronto", isRecording, isPlaying);
            getView().updateBeatCount(beatCount);
            getView().updateBPM(currentBPM);
            getView().updateSyncButton(isSynced);
            getView().updateTracksList(tracks);
            getView().updateSpeed(currentSpeed);
            getView().updatePitch(currentPitch);
            getView().updateReverse(reverseEnabled);
            getView().updateStutter(stutterEnabled, stutterRate);
            getView().updateWaveformSettings(showGrid, showTimeGrid, showPlayhead);
            updateFiltersView();
            updateFadeView();
            updateMarkersView();
            updateSlicingInfo();
        }
    }
    
    private void updateFiltersView() {
        if (isViewAttached()) {
            getView().updateFilters(lowPassEnabled, lowPassFreq, highPassEnabled, highPassFreq);
        }
    }
    
    private void updateFadeView() {
        if (isViewAttached()) {
            getView().updateFadeSettings(fadeInEnabled, fadeInDuration, fadeOutEnabled, fadeOutDuration);
        }
    }
    
    private void updateMarkersView() {
        if (isViewAttached()) {
            float markerTime = 0.0f;
            if (currentMarkerIndex >= 0 && currentMarkerIndex < markers.size()) {
                markerTime = markers.get(currentMarkerIndex) * (loopLength / 48000.0f);
            }
            
            getView().updateMarkersInfo(markers.size(), currentMarkerIndex, markerTime);
            getView().updateMarkerButtons(loopLength > 0, currentMarkerIndex >= 0, !markers.isEmpty());
        }
    }
    
    private void updateSlicingInfo() {
        if (slicingEnabled && loopLength > 0) {
            numSlices = AudioEngine.getLooperNumSlices();
            sliceLength = AudioEngine.getLooperSliceLength();
        } else {
            numSlices = 0;
            sliceLength = 0;
        }
        
        if (isViewAttached()) {
            getView().updateSlicing(slicingEnabled, numSlices, sliceLength);
        }
    }
    
    private void jumpToMarker(int markerIndex) {
        if (markerIndex >= 0 && markerIndex < markers.size() && loopLength > 0) {
            float markerPosition = markers.get(markerIndex);
            int targetSample = (int) (markerPosition * loopLength);
            
            AudioEngine.setLooperPosition(targetSample);
            currentPosition = targetSample;
            
            updateMarkersView();
        }
    }
    
    private void saveCurrentStateForUndo() {
        LooperState state = new LooperState(tracks, loopLength, currentPosition, beatCount, markers);
        undoStack.push(state);
        
        // Limitar tamanho da pilha
        if (undoStack.size() > 10) {
            undoStack.remove(0);
        }
        
        // Limpar redo stack
        redoStack.clear();
    }
    
    private void saveCurrentStateForRedo() {
        LooperState state = new LooperState(tracks, loopLength, currentPosition, beatCount, markers);
        redoStack.push(state);
        
        // Limitar tamanho da pilha
        if (redoStack.size() > 10) {
            redoStack.remove(0);
        }
    }
    
    private void restoreState(LooperState state) {
        tracks = state.tracks;
        loopLength = state.loopLength;
        currentPosition = state.currentPosition;
        beatCount = state.beatCount;
        markers = state.markers;
        
        // Atualizar AudioEngine
        // AudioEngine.setLooperLength(loopLength); // Método não existe
        AudioEngine.setLooperPosition(currentPosition);
        
        // Atualizar view
        updateAllViews();
    }
    
    private void startPeriodicUpdates() {
        if (!isUpdating) {
            isUpdating = true;
            updateHandler.post(updateRunnable);
        }
    }
    
    private void stopPeriodicUpdates() {
        isUpdating = false;
        if (updateHandler != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }
} 