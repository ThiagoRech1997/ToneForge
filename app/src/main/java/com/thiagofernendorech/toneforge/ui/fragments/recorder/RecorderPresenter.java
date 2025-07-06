package com.thiagofernendorech.toneforge.ui.fragments.recorder;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.thiagofernendorech.toneforge.AudioEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter para o RecorderFragment
 * Gerencia toda a lógica de negócio do gravador de áudio
 */
public class RecorderPresenter implements RecorderContract.Presenter {

    private static final String TAG = "RecorderPresenter";
    private static final int TIMER_UPDATE_INTERVAL = 100; // 100ms

    private RecorderContract.View view;
    private Handler timerHandler;
    private Runnable timerRunnable;
    
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private int currentTimeSeconds = 0;
    private long recordingStartTime = 0;
    private long playbackStartTime = 0;
    
    private List<String> recordingsList = new ArrayList<>();

    public RecorderPresenter(RecorderContract.View view) {
        this.view = view;
        this.timerHandler = new Handler(Looper.getMainLooper());
        initializeTimerRunnable();
    }

    @Override
    public void initialize() {
        Log.d(TAG, "Inicializando RecorderPresenter");
        loadRecordings();
        updateTimer();
    }

    @Override
    public void cleanup() {
        Log.d(TAG, "Limpando RecorderPresenter");
        stopRecording();
        stopPlayback();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    @Override
    public void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    @Override
    public void togglePlayback() {
        if (isPlaying) {
            stopPlayback();
        } else {
            startPlayback();
        }
    }

    @Override
    public void loadRecordings() {
        Log.d(TAG, "Carregando lista de gravações");
        // TODO: Implementar carregamento real de gravações do armazenamento
        // Por enquanto, usamos dados simulados
        if (recordingsList.isEmpty()) {
            view.showNoRecordingsPlaceholder();
        } else {
            String[] recordingsArray = recordingsList.toArray(new String[0]);
            view.updateRecordingsList(recordingsArray);
        }
    }

    @Override
    public void updateTimer() {
        if (isRecording || isPlaying) {
            timerHandler.postDelayed(timerRunnable, TIMER_UPDATE_INTERVAL);
        }
    }

    @Override
    public void stopRecording() {
        if (isRecording) {
            Log.d(TAG, "Parando gravação");
            isRecording = false;
            AudioEngine.stopRecording();
            view.updateRecordButtonState(false);
            view.showSuccess("Gravação finalizada");
            
            // Adicionar à lista de gravações (simulado)
            String recordingName = "Gravação " + (recordingsList.size() + 1);
            recordingsList.add(recordingName);
            loadRecordings();
            
            stopTimer();
        }
    }

    @Override
    public void stopPlayback() {
        if (isPlaying) {
            Log.d(TAG, "Parando reprodução");
            isPlaying = false;
            AudioEngine.stopPlayback();
            view.updatePlayButtonState(false);
            stopTimer();
        }
    }

    @Override
    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public int getCurrentTime() {
        return currentTimeSeconds;
    }

    @Override
    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    /**
     * Inicia a gravação de áudio
     */
    private void startRecording() {
        if (isPlaying) {
            stopPlayback();
        }
        
        Log.d(TAG, "Iniciando gravação");
        try {
            AudioEngine.startRecording();
            isRecording = true;
            recordingStartTime = System.currentTimeMillis();
            currentTimeSeconds = 0;
            
            view.updateRecordButtonState(true);
            view.updateTimer(formatTime(0));
            view.showSuccess("Gravação iniciada");
            
            startTimer();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar gravação", e);
            view.showError("Erro ao iniciar gravação: " + e.getMessage());
        }
    }

    /**
     * Inicia a reprodução da última gravação
     */
    private void startPlayback() {
        if (isRecording) {
            stopRecording();
        }
        
        Log.d(TAG, "Iniciando reprodução");
        try {
            AudioEngine.playLastRecording();
            isPlaying = true;
            playbackStartTime = System.currentTimeMillis();
            currentTimeSeconds = 0;
            
            view.updatePlayButtonState(true);
            view.updateTimer(formatTime(0));
            view.showSuccess("Reprodução iniciada");
            
            startTimer();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar reprodução", e);
            view.showError("Erro ao iniciar reprodução: " + e.getMessage());
        }
    }

    /**
     * Inicializa o runnable do timer
     */
    private void initializeTimerRunnable() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording || isPlaying) {
                    updateCurrentTime();
                    view.updateTimer(formatTime(currentTimeSeconds));
                    updateTimer();
                }
            }
        };
    }

    /**
     * Inicia o timer
     */
    private void startTimer() {
        timerHandler.post(timerRunnable);
    }

    /**
     * Para o timer
     */
    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
        currentTimeSeconds = 0;
        view.updateTimer(formatTime(0));
    }

    /**
     * Atualiza o tempo atual baseado no tempo decorrido
     */
    private void updateCurrentTime() {
        long currentTime = System.currentTimeMillis();
        long startTime = isRecording ? recordingStartTime : playbackStartTime;
        currentTimeSeconds = (int) ((currentTime - startTime) / 1000);
    }
} 