package com.thiagofernendorech.toneforge.ui.fragments.metronome;

import android.os.Handler;
import android.os.Looper;

import com.thiagofernendorech.toneforge.AudioEngine;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;

/**
 * Presenter para o MetronomeFragment
 * Gerencia a lógica de negócio do metrônomo
 */
public class MetronomePresenter extends BasePresenter<MetronomeContract.View> implements MetronomeContract.Presenter {
    
    // Estado do metrônomo
    private boolean isPlaying = false;
    private int bpm = 120;
    private int timeSignature = 4; // Compasso 4/4 por padrão
    private float metronomeVolume = 0.6f; // Volume padrão 60%
    
    // Handler para animações de batida
    private Handler beatHandler;
    private Runnable beatRunnable;
    
    // Constantes
    private static final int MIN_BPM = 40;
    private static final int MAX_BPM = 200;
    private static final int BPM_STEP = 5;
    private static final int MIN_TIME_SIGNATURE = 1;
    private static final int MAX_TIME_SIGNATURE = 16;
    private static final int MIN_VOLUME = 0;
    private static final int MAX_VOLUME = 100;

    public MetronomePresenter() {
        beatHandler = new Handler(Looper.getMainLooper());
        initializeBeatRunnable();
    }

    @Override
    public void initialize() {
        if (isViewAttached()) {
            getView().updateBpmDisplay(bpm);
            getView().updateTimeSignatureDisplay(timeSignature);
            getView().updateVolumeDisplay((int) (metronomeVolume * 100));
            getView().updatePlayButtonState(isPlaying);
            getView().updateHeaderTitle("Metrônomo");
        }
    }

    @Override
    public void togglePlayStop() {
        isPlaying = !isPlaying;
        
        if (isPlaying) {
            startMetronome();
        } else {
            stopMetronome();
        }
        
        if (isViewAttached()) {
            getView().updatePlayButtonState(isPlaying);
        }
    }

    @Override
    public void increaseBpm() {
        int newBpm = Math.min(MAX_BPM, bpm + BPM_STEP);
        if (newBpm != bpm) {
            setBpm(newBpm);
        }
    }

    @Override
    public void decreaseBpm() {
        int newBpm = Math.max(MIN_BPM, bpm - BPM_STEP);
        if (newBpm != bpm) {
            setBpm(newBpm);
        }
    }

    @Override
    public void setBpm(int bpm) {
        if (bpm >= MIN_BPM && bpm <= MAX_BPM) {
            this.bpm = bpm;
            
            if (isViewAttached()) {
                getView().updateBpmDisplay(bpm);
            }
            
            // Se estiver tocando, reinicia com o novo BPM
            if (isPlaying) {
                restartMetronome();
            }
        }
    }

    @Override
    public void increaseTimeSignature() {
        int newTimeSignature = Math.min(MAX_TIME_SIGNATURE, timeSignature + 1);
        if (newTimeSignature != timeSignature) {
            timeSignature = newTimeSignature;
            
            if (isViewAttached()) {
                getView().updateTimeSignatureDisplay(timeSignature);
            }
            
            AudioEngine.setMetronomeTimeSignature(timeSignature);
        }
    }

    @Override
    public void decreaseTimeSignature() {
        int newTimeSignature = Math.max(MIN_TIME_SIGNATURE, timeSignature - 1);
        if (newTimeSignature != timeSignature) {
            timeSignature = newTimeSignature;
            
            if (isViewAttached()) {
                getView().updateTimeSignatureDisplay(timeSignature);
            }
            
            AudioEngine.setMetronomeTimeSignature(timeSignature);
        }
    }

    @Override
    public void setVolume(int volumePercent) {
        if (volumePercent >= MIN_VOLUME && volumePercent <= MAX_VOLUME) {
            this.metronomeVolume = volumePercent / 100.0f;
            
            if (isViewAttached()) {
                getView().updateVolumeDisplay(volumePercent);
            }
            
            AudioEngine.setMetronomeVolume(metronomeVolume);
        }
    }

    @Override
    public int getCurrentBpm() {
        return bpm;
    }

    @Override
    public int getCurrentTimeSignature() {
        return timeSignature;
    }

    @Override
    public int getCurrentVolume() {
        return (int) (metronomeVolume * 100);
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void cleanup() {
        stopMetronome();
        if (beatHandler != null) {
            beatHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Inicializa o Runnable para a animação de batida
     */
    private void initializeBeatRunnable() {
        beatRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying && isViewAttached()) {
                    getView().animateBeat();
                    // Agendar próxima batida
                    long beatInterval = (long) (60000.0 / bpm); // milissegundos
                    beatHandler.postDelayed(this, beatInterval);
                }
            }
        };
    }

    /**
     * Inicia o metrônomo
     */
    private void startMetronome() {
        AudioEngine.startMetronome(bpm);
        if (isViewAttached()) {
            getView().startBeatAnimation();
        }
    }

    /**
     * Para o metrônomo
     */
    private void stopMetronome() {
        AudioEngine.stopMetronome();
        if (beatHandler != null) {
            beatHandler.removeCallbacks(beatRunnable);
        }
        if (isViewAttached()) {
            getView().stopBeatAnimation();
        }
    }

    /**
     * Reinicia o metrônomo com o BPM atual
     */
    private void restartMetronome() {
        stopMetronome();
        startMetronome();
    }
} 