package com.thiagofernendorech.toneforge.ui.fragments.tuner;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.AudioEngine;
import com.thiagofernendorech.toneforge.PermissionManager;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Presenter para o TunerFragment
 * Gerencia toda a lógica de negócio do afinador
 */
public class TunerPresenter extends BasePresenter<TunerContract.View> implements TunerContract.Presenter {
    
    private Context context;
    private AudioRepository audioRepository;
    private PermissionManager permissionManager;
    
    // Estado do afinador
    private boolean isTuning = false;
    private boolean isAudioInitialized = false;
    private AudioRecord audioRecord;
    private Thread audioThread;
    private AtomicBoolean shouldStop = new AtomicBoolean(false);
    
    // Configurações
    private String referenceNote = "A";
    private float referenceFrequency = 440.0f;
    private int calibrationOffset = 0;
    private String temperament = "Equal Temperament";
    private float sensitivity = 0.5f;
    private float detectionThreshold = -30.0f; // dB
    
    // Dados de áudio
    private static final int SAMPLE_RATE = 48000;
    private static final int BUFFER_SIZE = 2048;
    private static final int MIN_FREQUENCY = 40;
    private static final int MAX_FREQUENCY = 2000;
    
    // Histórico e análise
    private float[] frequencyHistory = new float[10];
    private int historyIndex = 0;
    private float currentFrequency = 0.0f;
    private String currentNote = "";
    private int currentOctave = 4;
    private int currentCents = 0;
    private float currentAccuracy = 0.0f;
    
    // Handler para atualizações da UI
    private Handler uiHandler;
    private Runnable uiUpdateRunnable;
    private static final int UI_UPDATE_INTERVAL_MS = 100; // 10 FPS
    
    // Constantes para notas
    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private static final double A4_FREQUENCY = 440.0;
    
    /**
     * Construtor do TunerPresenter
     * @param context contexto da aplicação
     * @param audioRepository repository de áudio
     */
    public TunerPresenter(Context context, AudioRepository audioRepository) {
        this.context = context.getApplicationContext();
        this.audioRepository = audioRepository;
        this.permissionManager = new PermissionManager();
        
        // Inicializar handler para atualizações da UI
        uiHandler = new Handler(Looper.getMainLooper());
        uiUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTuning && isViewAttached()) {
                    updateVisualIndicator();
                    updateFrequencyHistory();
                    updateInputLevel();
                    uiHandler.postDelayed(this, UI_UPDATE_INTERVAL_MS);
                }
            }
        };
    }
    
    // === Implementação da interface TunerContract.Presenter ===
    
    @Override
    public void onViewStarted() {
        // Verificar permissões
        checkAudioPermissions();
        
        // Carregar configurações
        loadTunerSettings();
        
        // Atualizar UI inicial
        if (isViewAttached()) {
            getView().updateReferenceNote(referenceNote);
            getView().updateReferenceFrequency(referenceFrequency);
            getView().updateCalibrationOffset(calibrationOffset);
            getView().updateTemperament(temperament);
            getView().updateSensitivity(sensitivity);
            getView().updateDetectionThreshold(detectionThreshold);
            getView().updateTunerStatus(false, "Pronto para afinar");
        }
    }
    
    @Override
    public void onViewResumed() {
        // Retomar atualizações se estiver afinando
        if (isTuning) {
            startUIUpdates();
        }
    }
    
    @Override
    public void onViewPaused() {
        // Pausar atualizações para economizar recursos
        stopUIUpdates();
    }
    
    @Override
    public void onViewDestroyed() {
        // Parar afinador e liberar recursos
        stopTuner();
        releaseAudio();
        
        // Salvar configurações
        saveTunerSettings();
        
        // Limpar handler
        if (uiHandler != null) {
            uiHandler.removeCallbacks(uiUpdateRunnable);
        }
    }
    
    // === CONTROLE DE AFINAÇÃO ===
    
    @Override
    public void startTuner() {
        if (!isTuning) {
            checkAudioPermissions();
            if (!permissionManager.hasRequiredPermissions(context)) {
                if (isViewAttached()) {
                    getView().requestAudioPermission();
                }
                return;
            }
            
            initializeAudio();
            if (isAudioInitialized) {
                isTuning = true;
                shouldStop.set(false);
                
                // Iniciar thread de áudio
                startAudioThread();
                
                // Iniciar atualizações da UI
                startUIUpdates();
                
                if (isViewAttached()) {
                    getView().updateTunerStatus(true, "Afinando...");
                    getView().updateMicrophoneStatus(true);
                }
            } else {
                if (isViewAttached()) {
                    getView().showMicrophoneError();
                }
            }
        }
    }
    
    @Override
    public void stopTuner() {
        if (isTuning) {
            isTuning = false;
            shouldStop.set(true);
            
            // Parar thread de áudio
            stopAudioThread();
            
            // Parar atualizações da UI
            stopUIUpdates();
            
            // Liberar recursos de áudio
            releaseAudio();
            
            if (isViewAttached()) {
                getView().updateTunerStatus(false, "Parado");
                getView().updateMicrophoneStatus(false);
                getView().updateDetectedNote("", 0);
                getView().updateDetectedFrequency(0.0f);
                getView().updateCentsDeviation(0);
                getView().updateTuningAccuracy(0.0f);
                getView().updateTuningIndicator(0.0f);
            }
        }
    }
    
    @Override
    public void toggleTuner() {
        if (isTuning) {
            stopTuner();
        } else {
            startTuner();
        }
    }
    
    @Override
    public void processAudioBuffer(float[] buffer, int size) {
        if (size > 0) {
            // Detectar frequência fundamental
            float frequency = detectFundamentalFrequency(buffer, size);
            
            if (frequency >= MIN_FREQUENCY && frequency <= MAX_FREQUENCY) {
                currentFrequency = frequency;
                
                // Calcular nota e desvio
                String note = calculateNearestNote(frequency);
                float noteFreq = getStandardFrequency(note);
                int cents = calculateCentsDeviation(frequency, noteFreq);
                float accuracy = calculateTuningAccuracy(cents);
                
                // Atualizar estado
                currentNote = note;
                currentCents = cents;
                currentAccuracy = accuracy;
                
                // Adicionar ao histórico
                addToFrequencyHistory(frequency);
                
                // Atualizar UI na thread principal
                if (isViewAttached()) {
                    uiHandler.post(() -> {
                        getView().updateDetectedFrequency(frequency);
                        getView().updateDetectedNote(note, currentOctave);
                        getView().updateCentsDeviation(cents);
                        getView().updateTuningAccuracy(accuracy);
                        // Calcular posição do indicador (-1.0 a 1.0)
                        float indicatorPosition = Math.max(-1.0f, Math.min(1.0f, cents / 50.0f));
                        getView().updateTuningIndicator(indicatorPosition);
                    });
                }
            } else {
                // Frequência fora do range válido
                if (isViewAttached()) {
                    uiHandler.post(() -> {
                        getView().updateDetectedNote("-", 0);
                        getView().updateDetectedFrequency(0.0f);
                        getView().updateCentsDeviation(0);
                        getView().updateTuningAccuracy(0.0f);
                        getView().updateTuningIndicator(0.0f);
                    });
                }
            }
        }
    }
    
    // === CONTROLE DE CALIBRAÇÃO ===
    
    @Override
    public void setCalibrationOffset(int offset) {
        calibrationOffset = offset;
        if (isViewAttached()) {
            getView().updateCalibrationOffset(offset);
        }
    }
    
    @Override
    public void resetCalibration() {
        calibrationOffset = 0;
        if (isViewAttached()) {
            getView().updateCalibrationOffset(0);
            getView().showSuccessMessage("Calibração resetada");
        }
    }
    
    @Override
    public void openCalibrationDialog() {
        if (isViewAttached()) {
            getView().showCalibrationDialog();
        }
    }
    
    // === CONTROLE DE TEMPERAMENTOS ===
    
    @Override
    public void setTemperament(String temperament) {
        this.temperament = temperament;
        if (isViewAttached()) {
            getView().updateTemperament(temperament);
        }
    }
    
    @Override
    public void openTemperamentSelector() {
        if (isViewAttached()) {
            getView().showTemperamentSelector();
        }
    }
    
    @Override
    public void loadTemperamentList() {
        String[] temperaments = {
            "Equal Temperament",
            "Just Intonation",
            "Pythagorean",
            "Meantone",
            "Well Temperament"
        };
        
        if (isViewAttached()) {
            getView().updateTemperamentList(temperaments);
        }
    }
    
    // === CONTROLE DE NOTAS DE REFERÊNCIA ===
    
    @Override
    public void setReferenceNote(String note) {
        referenceNote = note;
        referenceFrequency = getStandardFrequency(note);
        
        if (isViewAttached()) {
            getView().updateReferenceNote(note);
            getView().updateReferenceFrequency(referenceFrequency);
            getView().updateReferenceNoteButtons(note);
        }
    }
    
    @Override
    public void setReferenceFrequency(float frequency) {
        referenceFrequency = frequency;
        if (isViewAttached()) {
            getView().updateReferenceFrequency(frequency);
        }
    }
    
    @Override
    public float getStandardFrequency(String note) {
        // Implementação simplificada - apenas A4 = 440Hz
        if ("A".equals(note)) {
            return 440.0f;
        } else if ("E".equals(note)) {
            return 329.63f; // E4
        } else if ("D".equals(note)) {
            return 293.66f; // D4
        } else if ("G".equals(note)) {
            return 392.00f; // G4
        } else if ("B".equals(note)) {
            return 493.88f; // B4
        } else if ("C".equals(note)) {
            return 261.63f; // C4
        }
        return 440.0f; // Padrão
    }
    
    // === CONTROLE DE SENSIBILIDADE ===
    
    @Override
    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
        if (isViewAttached()) {
            getView().updateSensitivity(sensitivity);
        }
    }
    
    @Override
    public void setDetectionThreshold(float threshold) {
        detectionThreshold = threshold;
        if (isViewAttached()) {
            getView().updateDetectionThreshold(threshold);
        }
    }
    
    // === CONTROLE DE VISUALIZAÇÃO ===
    
    @Override
    public void updateVisualIndicator() {
        // Atualização periódica do indicador visual
        // Implementação específica seria feita na View
    }
    
    @Override
    public void updateFrequencyHistory() {
        if (isViewAttached()) {
            getView().updateFrequencyHistory(frequencyHistory);
        }
    }
    
    @Override
    public void updateFrequencySpectrum() {
        // Implementação futura para espectro de frequências
        float[] spectrum = new float[512]; // Placeholder
        if (isViewAttached()) {
            getView().updateFrequencySpectrum(spectrum);
        }
    }
    
    // === CONTROLE DE ÁUDIO ===
    
    @Override
    public void checkAudioPermissions() {
        if (!permissionManager.hasRequiredPermissions(context)) {
            if (isViewAttached()) {
                getView().showPermissionError();
            }
        }
    }
    
    @Override
    public void initializeAudio() {
        try {
            if (audioRecord == null) {
                audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    BUFFER_SIZE * 2
                );
                
                if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                    audioRecord.startRecording();
                    isAudioInitialized = true;
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        isAudioInitialized = false;
    }
    
    @Override
    public void releaseAudio() {
        if (audioRecord != null) {
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
            }
            audioRecord.release();
            audioRecord = null;
        }
        isAudioInitialized = false;
    }
    
    @Override
    public void updateInputLevel() {
        // Calcular nível de entrada baseado no histórico de frequências
        float avgLevel = 0.0f;
        int validSamples = 0;
        
        for (float freq : frequencyHistory) {
            if (freq > 0) {
                avgLevel += freq;
                validSamples++;
            }
        }
        
        if (validSamples > 0) {
            avgLevel /= validSamples;
            float levelDb = 20 * (float) Math.log10(avgLevel / 1000.0f); // Normalizado
            
            if (isViewAttached()) {
                getView().updateInputLevel(levelDb);
            }
        }
    }
    
    // === PROCESSAMENTO DE FREQUÊNCIA ===
    
    @Override
    public float detectFundamentalFrequency(float[] buffer, int size) {
        // Implementação simplificada usando AudioEngine
        AudioEngine.processTunerBuffer(buffer, size);
        return AudioEngine.getDetectedFrequency();
    }
    
    @Override
    public String calculateNearestNote(float frequency) {
        if (frequency < MIN_FREQUENCY || frequency > MAX_FREQUENCY) {
            return "";
        }
        
        // Calcular número da nota baseado em A4 = 440Hz
        double noteNumber = 12 * Math.log(frequency / A4_FREQUENCY) / Math.log(2) + 57;
        int noteIndex = (int) Math.round(noteNumber + 1200) % 12;
        currentOctave = (int) Math.round(noteNumber) / 12 - 1;
        
        return NOTE_NAMES[noteIndex];
    }
    
    @Override
    public int calculateCentsDeviation(float frequency, float noteFrequency) {
        if (noteFrequency <= 0) return 0;
        
        // Aplicar offset de calibração
        float adjustedFrequency = frequency * (float) Math.pow(2, calibrationOffset / 1200.0);
        
        // Calcular desvio em cents
        double cents = 1200 * Math.log(adjustedFrequency / noteFrequency) / Math.log(2);
        return (int) Math.round(cents);
    }
    
    @Override
    public float calculateTuningAccuracy(int cents) {
        int absCents = Math.abs(cents);
        
        if (absCents <= 5) {
            return 1.0f; // Perfeito
        } else if (absCents <= 15) {
            return 0.7f; // Bom
        } else if (absCents <= 30) {
            return 0.3f; // Regular
        } else {
            return 0.0f; // Ruim
        }
    }
    
    // === CONFIGURAÇÕES ===
    
    @Override
    public void saveTunerSettings() {
        // TODO: Implementar salvamento em SharedPreferences
        // Por enquanto apenas simular
    }
    
    @Override
    public void loadTunerSettings() {
        // TODO: Implementar carregamento de SharedPreferences
        // Por enquanto usar valores padrão
        referenceNote = "A";
        referenceFrequency = 440.0f;
        calibrationOffset = 0;
        temperament = "Equal Temperament";
        sensitivity = 0.5f;
        detectionThreshold = -30.0f;
    }
    
    @Override
    public void openSettings() {
        if (isViewAttached()) {
            getView().showSettingsDialog();
        }
    }
    
    @Override
    public void openHelp() {
        if (isViewAttached()) {
            getView().showHelpDialog();
        }
    }
    
    // === OUTROS ===
    
    @Override
    public void forceUpdateUI() {
        if (isViewAttached()) {
            getView().updateDetectedNote(currentNote, currentOctave);
            getView().updateDetectedFrequency(currentFrequency);
            getView().updateCentsDeviation(currentCents);
            getView().updateTuningAccuracy(currentAccuracy);
        }
    }
    
    @Override
    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) { // Código da requisição de áudio
            boolean granted = grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED;
            
            if (granted) {
                if (isViewAttached()) {
                    getView().showSuccessMessage("Permissão de áudio concedida");
                }
            } else {
                if (isViewAttached()) {
                    getView().showPermissionError();
                }
            }
        }
    }
    
    // === Métodos privados ===
    
    private void startAudioThread() {
        if (audioThread == null && audioRecord != null) {
            audioThread = new Thread(() -> {
                short[] buffer = new short[BUFFER_SIZE];
                float[] floatBuffer = new float[BUFFER_SIZE];
                
                while (!shouldStop.get() && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    int read = audioRecord.read(buffer, 0, BUFFER_SIZE);
                    
                    if (read > 0) {
                        // Converter para float
                        for (int i = 0; i < read; i++) {
                            floatBuffer[i] = buffer[i] / 32768.0f;
                        }
                        
                        // Processar buffer
                        processAudioBuffer(floatBuffer, read);
                    }
                }
            });
            audioThread.start();
        }
    }
    
    private void stopAudioThread() {
        shouldStop.set(true);
        if (audioThread != null) {
            audioThread.interrupt();
            audioThread = null;
        }
    }
    
    private void startUIUpdates() {
        if (uiHandler != null) {
            uiHandler.post(uiUpdateRunnable);
        }
    }
    
    private void stopUIUpdates() {
        if (uiHandler != null) {
            uiHandler.removeCallbacks(uiUpdateRunnable);
        }
    }
    
    private void addToFrequencyHistory(float frequency) {
        frequencyHistory[historyIndex] = frequency;
        historyIndex = (historyIndex + 1) % frequencyHistory.length;
    }
} 