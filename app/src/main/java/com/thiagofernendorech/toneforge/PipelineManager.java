package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class PipelineManager {
    private static final String TAG = "PipelineManager";
    
    // Configurações do pipeline
    private static final int SAMPLE_RATE = 48000;
    private static final int BUFFER_SIZE = 2048;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
    
    // Estados do pipeline
    private static final int STATE_STOPPED = 0;
    private static final int STATE_STARTING = 1;
    private static final int STATE_RUNNING = 2;
    private static final int STATE_ERROR = 3;
    private static final int STATE_RECOVERING = 4;
    
    // Componentes do pipeline
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private Thread audioThread;
    private Handler uiHandler;
    
    // Buffers de áudio
    private float[] inputBuffer;
    private float[] outputBuffer;
    
    // Controle de estado
    private volatile int currentState = STATE_STOPPED;
    private final AtomicBoolean shouldRun = new AtomicBoolean(false);
    private final AtomicBoolean isRecovering = new AtomicBoolean(false);
    
    // Estatísticas e monitoramento
    private long startTime;
    private long totalSamplesProcessed;
    private long errorCount;
    private long lastErrorTime;
    private String lastErrorMessage;
    
    // Callbacks
    private PipelineCallback callback;
    
    // Singleton
    private static PipelineManager instance;
    
    private boolean isRunning = false;
    private boolean isPaused = false;
    private LatencyManager latencyManager;
    
    public interface PipelineCallback {
        void onPipelineStarted();
        void onPipelineStopped();
        void onPipelineError(String error);
        void onPipelineRecovered();
        void onPipelineStateChanged(int oldState, int newState);
    }
    
    private PipelineManager() {
        uiHandler = new Handler(Looper.getMainLooper());
        inputBuffer = new float[BUFFER_SIZE];
        outputBuffer = new float[BUFFER_SIZE];
    }
    
    public static synchronized PipelineManager getInstance() {
        if (instance == null) {
            instance = new PipelineManager();
        }
        return instance;
    }
    
    public void initialize(Context context) {
        latencyManager = LatencyManager.getInstance(context);
        
        // Configurar listener para mudanças de latência
        latencyManager.setLatencyChangeListener(new LatencyManager.LatencyChangeListener() {
            @Override
            public void onLatencyModeChanged(int newMode) {
                if (isRunning) {
                    Log.d("PipelineManager", "Reiniciando pipeline devido a mudança de latência");
                    restartPipeline();
                }
            }
            
            @Override
            public void onBufferSizeChanged(int newBufferSize) {
                // Buffer size é aplicado automaticamente pelo AudioEngine
            }
            
            @Override
            public void onSampleRateChanged(int newSampleRate) {
                // Sample rate é aplicado automaticamente pelo AudioEngine
            }
        });
    }
    
    public void setCallback(PipelineCallback callback) {
        this.callback = callback;
    }
    
    public synchronized boolean startPipeline() {
        if (currentState == STATE_RUNNING || currentState == STATE_STARTING) {
            Log.d(TAG, "Pipeline já está rodando ou iniciando");
            return true;
        }
        
        Log.d(TAG, "Iniciando pipeline de áudio");
        setState(STATE_STARTING);
        
        try {
            // Inicializar engine de áudio
            AudioEngine.initAudioEngine();
            
            // Configurar AudioRecord
            setupAudioRecord();
            
            // Configurar AudioTrack
            setupAudioTrack();
            
            // Iniciar thread de processamento
            startAudioThread();
            
            setState(STATE_RUNNING);
            isRunning = true;
            startTime = System.currentTimeMillis();
            totalSamplesProcessed = 0;
            errorCount = 0;
            
            Log.d(TAG, "Pipeline iniciado com sucesso");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar pipeline", e);
            setState(STATE_ERROR);
            lastErrorMessage = e.getMessage();
            lastErrorTime = System.currentTimeMillis();
            errorCount++;
            
            // Tentar recuperação automática
            scheduleRecovery();
            return false;
        }
    }
    
    public synchronized void stopPipeline() {
        Log.d(TAG, "Parando pipeline de áudio");
        
        shouldRun.set(false);
        setState(STATE_STOPPED);
        isRunning = false;
        isPaused = false;
        
        // Parar thread
        if (audioThread != null) {
            audioThread.interrupt();
            try {
                audioThread.join(1000); // Aguardar até 1 segundo
            } catch (InterruptedException e) {
                Log.w(TAG, "Interrompido ao aguardar thread");
            }
            audioThread = null;
        }
        
        // Liberar recursos de áudio
        releaseAudioResources();
        
        // Limpar engine
        try {
            AudioEngine.cleanupAudioEngine();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao limpar engine", e);
        }
        
        Log.d(TAG, "Pipeline parado");
    }
    
    public void restartPipeline() {
        if (isRunning) {
            Log.d("PipelineManager", "Reiniciando pipeline com novas configurações de latência");
            
            // Parar pipeline atual
            stopPipeline();
            
            // Aguardar um pouco para garantir que parou completamente
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Reiniciar com novas configurações
            startPipeline();
        }
    }
    
    public void pausePipeline() {
        if (isRunning && !isPaused) {
            isPaused = true;
            Log.d(TAG, "Pipeline pausado");
        }
    }
    
    public void resumePipeline() {
        if (isRunning && isPaused) {
            isPaused = false;
            Log.d(TAG, "Pipeline resumido");
        }
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    private void setupAudioRecord() throws Exception {
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, 
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT);
        
        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            throw new Exception("Configuração de áudio inválida para AudioRecord");
        }
        
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT, 
            Math.max(minBufferSize, BUFFER_SIZE * 4));
        
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new Exception("Falha ao inicializar AudioRecord");
        }
        
        audioRecord.startRecording();
        Log.d(TAG, "AudioRecord configurado e iniciado");
    }
    
    private void setupAudioTrack() throws Exception {
        int minTrackBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, 
            CHANNEL_CONFIG, AUDIO_FORMAT);
        
        if (minTrackBufferSize == AudioTrack.ERROR_BAD_VALUE) {
            throw new Exception("Configuração de áudio inválida para AudioTrack");
        }
        
        audioTrack = new AudioTrack.Builder()
            .setAudioAttributes(new android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
            .setAudioFormat(new android.media.AudioFormat.Builder()
                .setEncoding(AUDIO_FORMAT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(CHANNEL_CONFIG)
                .build())
            .setBufferSizeInBytes(Math.max(minTrackBufferSize, BUFFER_SIZE * 4))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build();
        
        if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            throw new Exception("Falha ao inicializar AudioTrack");
        }
        
        audioTrack.play();
        Log.d(TAG, "AudioTrack configurado e iniciado");
    }
    
    private void startAudioThread() {
        shouldRun.set(true);
        
        audioThread = new Thread(() -> {
            Log.d(TAG, "Thread de áudio iniciada");
            
            while (shouldRun.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    // Verificar se os componentes estão funcionando
                    if (audioRecord == null || audioTrack == null || 
                        audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING ||
                        audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                        
                        Log.w(TAG, "Componentes de áudio em estado inválido, tentando recuperar");
                        handleAudioError();
                        continue;
                    }
                    
                    // Capturar áudio
                    int read = audioRecord.read(inputBuffer, 0, BUFFER_SIZE, AudioRecord.READ_BLOCKING);
                    
                    if (read > 0) {
                        // Processar áudio com efeitos
                        AudioEngine.processBuffer(inputBuffer, outputBuffer, read);
                        
                        // Reproduzir áudio processado
                        int written = audioTrack.write(outputBuffer, 0, read, AudioTrack.WRITE_BLOCKING);
                        
                        if (written > 0) {
                            totalSamplesProcessed += read;
                        }
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Erro no loop de áudio", e);
                    handleAudioError();
                }
            }
            
            Log.d(TAG, "Thread de áudio finalizada");
        });
        
        audioThread.setPriority(Thread.MAX_PRIORITY);
        audioThread.start();
    }
    
    private void handleAudioError() {
        errorCount++;
        lastErrorTime = System.currentTimeMillis();
        lastErrorMessage = "Erro no processamento de áudio";
        
        if (currentState == STATE_RUNNING) {
            setState(STATE_ERROR);
            scheduleRecovery();
        }
    }
    
    private void scheduleRecovery() {
        if (isRecovering.get()) {
            return; // Já está tentando recuperar
        }
        
        isRecovering.set(true);
        uiHandler.postDelayed(() -> {
            Log.d(TAG, "Tentando recuperação automática do pipeline");
            setState(STATE_RECOVERING);
            
            try {
                restartPipeline();
                Log.d(TAG, "Recuperação bem-sucedida");
                setState(STATE_RUNNING);
                if (callback != null) {
                    callback.onPipelineRecovered();
                }
            } catch (Exception e) {
                Log.e(TAG, "Falha na recuperação: " + e.getMessage());
                setState(STATE_ERROR);
            }
            
            isRecovering.set(false);
        }, 1000); // Aguardar 1 segundo antes de tentar recuperar
    }
    
    private void releaseAudioResources() {
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao liberar AudioRecord", e);
            }
            audioRecord = null;
        }
        
        if (audioTrack != null) {
            try {
                audioTrack.stop();
                audioTrack.release();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao liberar AudioTrack", e);
            }
            audioTrack = null;
        }
    }
    
    private void setState(int newState) {
        int oldState = currentState;
        currentState = newState;
        
        Log.d(TAG, "Estado do pipeline: " + getStateName(oldState) + " -> " + getStateName(newState));
        
        if (callback != null) {
            callback.onPipelineStateChanged(oldState, newState);
            
            switch (newState) {
                case STATE_RUNNING:
                    callback.onPipelineStarted();
                    break;
                case STATE_STOPPED:
                    callback.onPipelineStopped();
                    break;
                case STATE_ERROR:
                    callback.onPipelineError(lastErrorMessage);
                    break;
            }
        }
    }
    
    private String getStateName(int state) {
        switch (state) {
            case STATE_STOPPED: return "STOPPED";
            case STATE_STARTING: return "STARTING";
            case STATE_RUNNING: return "RUNNING";
            case STATE_ERROR: return "ERROR";
            case STATE_RECOVERING: return "RECOVERING";
            default: return "UNKNOWN";
        }
    }
    
    // Métodos públicos para consulta de estado
    public boolean isRunning() {
        return currentState == STATE_RUNNING;
    }
    
    public boolean isError() {
        return currentState == STATE_ERROR;
    }
    
    public boolean isRecovering() {
        return currentState == STATE_RECOVERING;
    }
    
    public int getState() {
        return currentState;
    }
    
    public String getStateName() {
        return getStateName(currentState);
    }
    
    public long getUptime() {
        if (startTime == 0) return 0;
        return System.currentTimeMillis() - startTime;
    }
    
    public long getTotalSamplesProcessed() {
        return totalSamplesProcessed;
    }
    
    public long getErrorCount() {
        return errorCount;
    }
    
    public String getLastError() {
        return lastErrorMessage;
    }
    
    public long getLastErrorTime() {
        return lastErrorTime;
    }
    
    // Método para verificar saúde do pipeline
    public boolean isHealthy() {
        return currentState == STATE_RUNNING && 
               audioRecord != null && 
               audioTrack != null &&
               audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING &&
               audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
    }
} 