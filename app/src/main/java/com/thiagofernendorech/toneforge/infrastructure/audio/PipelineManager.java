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
    private static final int STATE_IDLE = 5; // Novo estado para quando não há atividade
    
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
    
    // Sistema de ativação sob demanda
    private volatile boolean hasActiveEffects = false;
    private volatile boolean isRecording = false;
    private volatile boolean isLooping = false;
    private volatile boolean isPlaying = false;
    private volatile boolean hasUserActivity = false;
    private long lastActivityTime = 0;
    private static final long ACTIVITY_TIMEOUT = 30000; // 30 segundos sem atividade
    
    // Estatísticas e monitoramento (só quando necessário)
    private long startTime;
    private long totalSamplesProcessed;
    private long errorCount;
    private long lastErrorTime;
    private String lastErrorMessage;
    
    // Controle de throttling de logs
    private long lastLogTime = 0;
    private static final long LOG_THROTTLE_INTERVAL = 5000; // 5 segundos
    private long lastHealthCheckTime = 0;
    private static final long HEALTH_CHECK_INTERVAL = 1000; // 1 segundo
    
    // Controle de recuperação mais inteligente
    private int recoveryAttempts = 0;
    private static final int MAX_RECOVERY_ATTEMPTS = 2; // Reduzido de 3 para 2
    private static final long RECOVERY_COOLDOWN = 15000; // Aumentado para 15 segundos
    private long lastRecoveryTime = 0;
    
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
        
        // Configurar listener mais inteligente para mudanças de latência
        latencyManager.setLatencyChangeListener(new LatencyManager.LatencyChangeListener() {
            @Override
            public void onLatencyModeChanged(int newMode) {
                // Só reiniciar se o pipeline estiver realmente ativo e necessário
                if (isRunning && isActivityRequired()) {
                    Log.d(TAG, "Aplicando nova configuração de latência");
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
    
    /**
     * Verifica se há atividade que justifica manter o pipeline ativo
     */
    private boolean isActivityRequired() {
        return hasActiveEffects || isRecording || isLooping || isPlaying || hasUserActivity;
    }
    
    /**
     * Notifica que há efeitos ativos
     */
    public void setEffectsActive(boolean active) {
        boolean wasRequired = isActivityRequired();
        hasActiveEffects = active;
        updateActivityStatus();
        
        if (!wasRequired && isActivityRequired()) {
            Log.d(TAG, "Efeitos ativados - iniciando pipeline");
            startPipelineIfNeeded();
        } else if (wasRequired && !isActivityRequired()) {
            Log.d(TAG, "Nenhuma atividade detectada - parando pipeline");
            stopPipelineIfNotNeeded();
        }
    }
    
    /**
     * Notifica que há gravação ativa
     */
    public void setRecordingActive(boolean active) {
        boolean wasRequired = isActivityRequired();
        isRecording = active;
        updateActivityStatus();
        
        if (!wasRequired && isActivityRequired()) {
            Log.d(TAG, "Gravação iniciada - iniciando pipeline");
            startPipelineIfNeeded();
        } else if (wasRequired && !isActivityRequired()) {
            stopPipelineIfNotNeeded();
        }
    }
    
    /**
     * Notifica que há looping ativo
     */
    public void setLoopingActive(boolean active) {
        boolean wasRequired = isActivityRequired();
        isLooping = active;
        updateActivityStatus();
        
        if (!wasRequired && isActivityRequired()) {
            Log.d(TAG, "Looping iniciado - iniciando pipeline");
            startPipelineIfNeeded();
        } else if (wasRequired && !isActivityRequired()) {
            stopPipelineIfNotNeeded();
        }
    }
    
    /**
     * Notifica que há reprodução ativa
     */
    public void setPlayingActive(boolean active) {
        boolean wasRequired = isActivityRequired();
        isPlaying = active;
        updateActivityStatus();
        
        if (!wasRequired && isActivityRequired()) {
            Log.d(TAG, "Reprodução iniciada - iniciando pipeline");
            startPipelineIfNeeded();
        } else if (wasRequired && !isActivityRequired()) {
            stopPipelineIfNotNeeded();
        }
    }
    
    /**
     * Notifica atividade do usuário (interação com controles)
     */
    public void notifyUserActivity() {
        hasUserActivity = true;
        updateActivityStatus();
        
        if (!isActivityRequired()) {
            startPipelineIfNeeded();
        }
        
        // Agendar verificação de timeout
        uiHandler.removeCallbacks(activityTimeoutRunnable);
        uiHandler.postDelayed(activityTimeoutRunnable, ACTIVITY_TIMEOUT);
    }
    
    private final Runnable activityTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            // Verificar se ainda há atividade do usuário
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastActivityTime > ACTIVITY_TIMEOUT) {
                hasUserActivity = false;
                Log.d(TAG, "Timeout de atividade do usuário");
                if (!isActivityRequired()) {
                    stopPipelineIfNotNeeded();
                }
            }
        }
    };
    
    private void updateActivityStatus() {
        lastActivityTime = System.currentTimeMillis();
    }
    
    private void startPipelineIfNeeded() {
        if (!isRunning && isActivityRequired()) {
            startPipeline();
        }
    }
    
    private void stopPipelineIfNotNeeded() {
        if (isRunning && !isActivityRequired()) {
            Log.d(TAG, "Parando pipeline - nenhuma atividade necessária");
            stopPipeline();
        }
    }
    
    public synchronized boolean startPipeline() {
        if (currentState == STATE_RUNNING || currentState == STATE_STARTING) {
            return true;
        }
        
        Log.d(TAG, "Iniciando pipeline de áudio sob demanda");
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
            recoveryAttempts = 0;
            
            Log.d(TAG, "Pipeline iniciado com sucesso");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar pipeline", e);
            setState(STATE_ERROR);
            lastErrorMessage = e.getMessage();
            lastErrorTime = System.currentTimeMillis();
            errorCount++;
            
            // Tentar recuperação apenas se ainda há atividade necessária
            if (isActivityRequired() && recoveryAttempts < MAX_RECOVERY_ATTEMPTS) {
                scheduleRecovery();
            } else {
                Log.e(TAG, "Pipeline não será recuperado - sem atividade necessária ou muitas tentativas");
            }
            return false;
        }
    }
    
    public synchronized void stopPipeline() {
        Log.d(TAG, "Parando pipeline de áudio");
        
        shouldRun.set(false);
        setState(STATE_STOPPED);
        isRunning = false;
        isPaused = false;
        
        // Cancelar timeout de atividade
        uiHandler.removeCallbacks(activityTimeoutRunnable);
        
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
        if (isRunning && isActivityRequired()) {
            Log.d(TAG, "Reiniciando pipeline com novas configurações");
            
            // Parar pipeline atual
            stopPipeline();
            
            // Aguardar um pouco para garantir que parou completamente
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Reiniciar apenas se ainda necessário
            if (isActivityRequired()) {
                startPipeline();
            }
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
            LogManager.i(TAG, "Thread de áudio iniciada");
            
            while (shouldRun.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    // Verificar se ainda há atividade necessária
                    if (!isActivityRequired()) {
                        Log.d(TAG, "Nenhuma atividade necessária - finalizando thread");
                        break;
                    }
                    
                    // Verificar saúde dos componentes apenas ocasionalmente
                    if (Math.random() < 0.001 && !isHealthy()) { // 0.1% chance de verificar
                        LogManager.w(TAG, "Componentes de áudio em estado inválido");
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
                    LogManager.e(TAG, "Erro no loop de áudio", e);
                    handleAudioError();
                }
            }
            
            LogManager.i(TAG, "Thread de áudio finalizada");
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
            // Só tentar recuperar se ainda há atividade necessária
            if (isActivityRequired()) {
                scheduleRecovery();
            } else {
                Log.d(TAG, "Erro no pipeline mas nenhuma atividade necessária - não recuperando");
                stopPipeline();
            }
        }
    }
    
    private void scheduleRecovery() {
        if (isRecovering.get()) {
            return; // Já está tentando recuperar
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Verificar se não excedeu o limite de tentativas
        if (recoveryAttempts >= MAX_RECOVERY_ATTEMPTS) {
            Log.e(TAG, "Máximo de tentativas de recuperação excedido. Pipeline permanecerá parado.");
            return;
        }
        
        // Verificar se ainda há atividade necessária
        if (!isActivityRequired()) {
            Log.d(TAG, "Nenhuma atividade necessária - cancelando recuperação");
            return;
        }
        
        // Verificar cooldown entre tentativas
        if (currentTime - lastRecoveryTime < RECOVERY_COOLDOWN) {
            Log.d(TAG, "Aguardando cooldown antes da próxima tentativa de recuperação");
            return;
        }
        
        isRecovering.set(true);
        recoveryAttempts++;
        lastRecoveryTime = currentTime;
        
        uiHandler.postDelayed(() -> {
            Log.d(TAG, "Tentativa de recuperação " + recoveryAttempts + "/" + MAX_RECOVERY_ATTEMPTS);
            setState(STATE_RECOVERING);
            
            try {
                if (isActivityRequired()) {
                    restartPipeline();
                    Log.d(TAG, "Recuperação bem-sucedida");
                    setState(STATE_RUNNING);
                    recoveryAttempts = 0; // Reset contador em caso de sucesso
                    if (callback != null) {
                        callback.onPipelineRecovered();
                    }
                } else {
                    Log.d(TAG, "Recuperação cancelada - sem atividade necessária");
                    setState(STATE_STOPPED);
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
        
        // Só logar mudanças significativas de estado
        if (oldState != newState) {
            LogManager.verbose(TAG, "Estado do pipeline: " + getStateName(oldState) + " -> " + getStateName(newState));
            
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
    }
    
    private String getStateName(int state) {
        switch (state) {
            case STATE_STOPPED: return "STOPPED";
            case STATE_STARTING: return "STARTING";
            case STATE_RUNNING: return "RUNNING";
            case STATE_ERROR: return "ERROR";
            case STATE_RECOVERING: return "RECOVERING";
            case STATE_IDLE: return "IDLE";
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