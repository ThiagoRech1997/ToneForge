package com.thiagofernendorech.toneforge;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LooperFragment extends Fragment implements LooperTrackAdapter.OnTrackActionListener, ExportDialog.ExportDialogListener {
    
    // UI Elements
    private TextView looperStatus;
    private TextView looperTimer;
    private TextView looperBeatCount;
    private ProgressBar looperProgress;
    private Button looperRecordButton;
    private Button looperPlayButton;
    private Button looperClearButton;
    private Button looperUndoButton;
    private Switch looperSyncSwitch;
    private EditText looperBPMInput;
    private Button looperTapTempoButton;
    private Button looperSaveButton;
    private Button looperLoadButton;
    private Button looperLibraryButton;
    private Button looperExportButton;
    private RecyclerView looperTracksList;
    
    // Controles especiais do looper
    private Switch looperReverseSwitch;
    private SeekBar looperSpeedSeekBar;
    private TextView looperSpeedText;
    private SeekBar looperPitchSeekBar;
    private TextView looperPitchText;
    private Switch looperStutterSwitch;
    private SeekBar looperStutterRateSeekBar;
    private TextView looperStutterRateText;
    
    // Adapter para faixas
    private LooperTrackAdapter trackAdapter;
    
    // Estado do looper
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean isSynced = false;
    private int currentBPM = 120;
    private int loopLength = 0; // em samples
    private int currentPosition = 0; // em samples
    private int beatCount = 0;
    
    // Timer para atualização da UI
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Runnable uiUpdateRunnable;
    
    // Tap tempo
    private List<Long> tapTimes = new ArrayList<>();
    private static final int MAX_TAP_TIMES = 4;
    
    // Undo/Redo
    private Stack<LooperState> undoStack = new Stack<>();
    private Stack<LooperState> redoStack = new Stack<>();
    
    // Estado para undo/redo
    private static class LooperState {
        List<LooperTrackAdapter.LooperTrack> tracks;
        int loopLength;
        int currentPosition;
        int beatCount;
        
        LooperState(List<LooperTrackAdapter.LooperTrack> tracks, int loopLength, 
                   int currentPosition, int beatCount) {
            this.tracks = new ArrayList<>();
            for (LooperTrackAdapter.LooperTrack track : tracks) {
                this.tracks.add(new LooperTrackAdapter.LooperTrack(track.id, track.name));
                this.tracks.get(this.tracks.size() - 1).volume = track.volume;
                this.tracks.get(this.tracks.size() - 1).muted = track.muted;
                this.tracks.get(this.tracks.size() - 1).soloed = track.soloed;
                this.tracks.get(this.tracks.size() - 1).length = track.length;
            }
            this.loopLength = loopLength;
            this.currentPosition = currentPosition;
            this.beatCount = beatCount;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_looper, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        setupUIUpdateRunnable();
        
        return view;
    }
    
    private void initializeViews(View view) {
        looperStatus = view.findViewById(R.id.looperStatus);
        looperTimer = view.findViewById(R.id.looperTimer);
        looperBeatCount = view.findViewById(R.id.looperBeatCount);
        looperProgress = view.findViewById(R.id.looperProgress);
        looperRecordButton = view.findViewById(R.id.looperRecordButton);
        looperPlayButton = view.findViewById(R.id.looperPlayButton);
        looperClearButton = view.findViewById(R.id.looperClearButton);
        looperUndoButton = view.findViewById(R.id.looperUndoButton);
        looperSyncSwitch = view.findViewById(R.id.looperSyncSwitch);
        looperBPMInput = view.findViewById(R.id.looperBPMInput);
        looperTapTempoButton = view.findViewById(R.id.looperTapTempoButton);
        looperSaveButton = view.findViewById(R.id.looperSaveButton);
        looperLoadButton = view.findViewById(R.id.looperLoadButton);
        looperLibraryButton = view.findViewById(R.id.looperLibraryButton);
        looperExportButton = view.findViewById(R.id.looperExportButton);
        looperTracksList = view.findViewById(R.id.looperTracksList);
        
        // Controles especiais do looper
        looperReverseSwitch = view.findViewById(R.id.looperReverseSwitch);
        looperSpeedSeekBar = view.findViewById(R.id.looperSpeedSeekBar);
        looperSpeedText = view.findViewById(R.id.looperSpeedText);
        looperPitchSeekBar = view.findViewById(R.id.looperPitchSeekBar);
        looperPitchText = view.findViewById(R.id.looperPitchText);
        looperStutterSwitch = view.findViewById(R.id.looperStutterSwitch);
        looperStutterRateSeekBar = view.findViewById(R.id.looperStutterRateSeekBar);
        looperStutterRateText = view.findViewById(R.id.looperStutterRateText);
    }
    
    private void setupRecyclerView() {
        trackAdapter = new LooperTrackAdapter(this);
        looperTracksList.setLayoutManager(new LinearLayoutManager(getContext()));
        looperTracksList.setAdapter(trackAdapter);
    }
    
    private void setupClickListeners() {
        // Botão Gravar
        looperRecordButton.setOnClickListener(v -> toggleRecording());
        
        // Botão Play/Stop
        looperPlayButton.setOnClickListener(v -> togglePlayback());
        
        // Botão Clear
        looperClearButton.setOnClickListener(v -> clearLooper());
        
        // Botão Undo
        looperUndoButton.setOnClickListener(v -> undo());
        
        // Switch de sincronização
        looperSyncSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isSynced = isChecked;
            if (isSynced) {
                // Ativar sincronização com metrônomo
                AudioEngine.startMetronome(currentBPM);
            } else {
                // Desativar sincronização
                AudioEngine.stopMetronome();
            }
        });
        
        // Botão Tap Tempo
        looperTapTempoButton.setOnClickListener(v -> tapTempo());
        
        // Input BPM
        looperBPMInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    int newBPM = Integer.parseInt(looperBPMInput.getText().toString());
                    if (newBPM >= 60 && newBPM <= 200) {
                        currentBPM = newBPM;
                        if (isSynced) {
                            AudioEngine.startMetronome(currentBPM);
                        }
                    }
                } catch (NumberFormatException e) {
                    looperBPMInput.setText(String.valueOf(currentBPM));
                }
            }
        });
        
        // Botão Salvar
        looperSaveButton.setOnClickListener(v -> {
            android.util.Log.d("LooperFragment", "Botão Salvar clicado!");
            saveLoopToWav();
        });
        
        // Botão Carregar
        looperLoadButton.setOnClickListener(v -> showLoadDialog());
        
        // Botão Biblioteca
        looperLibraryButton.setOnClickListener(v -> openLibrary());
        
        // Botão Exportar
        looperExportButton.setOnClickListener(v -> showExportDialog());
        
        // Controles especiais do looper
        setupSpecialControls();
    }
    
    private void setupSpecialControls() {
        // Reverse Switch
        looperReverseSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setLooperReverse(isChecked);
            android.util.Log.d("LooperFragment", "Reverse: " + (isChecked ? "ON" : "OFF"));
        });
        
        // Speed SeekBar
        looperSpeedSeekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float speed = 0.25f + (progress / 100.0f) * 3.75f; // 0.25x a 4.0x
                    AudioEngine.setLooperSpeed(speed);
                    looperSpeedText.setText(String.format("%.1fx", speed));
                }
            }
            
            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
        
        // Pitch SeekBar
        looperPitchSeekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float semitones = (progress - 120) / 10.0f; // -12 a +12 semitons
                    AudioEngine.setLooperPitchShift(semitones);
                    looperPitchText.setText(String.format("%.1f", semitones));
                }
            }
            
            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
        
        // Stutter Switch
        looperStutterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setLooperStutter(isChecked, getStutterRateFromSeekBar());
            looperStutterRateSeekBar.setEnabled(isChecked);
            looperStutterRateText.setTextColor(getResources().getColor(isChecked ? R.color.white : R.color.gray));
            android.util.Log.d("LooperFragment", "Stutter: " + (isChecked ? "ON" : "OFF"));
        });
        
        // Stutter Rate SeekBar
        looperStutterRateSeekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && looperStutterSwitch.isChecked()) {
                    float rate = getStutterRateFromSeekBar();
                    AudioEngine.setLooperStutter(true, rate);
                    looperStutterRateText.setText(String.format("%.1fHz", rate));
                }
            }
            
            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
    }
    
    private float getStutterRateFromSeekBar() {
        int progress = looperStutterRateSeekBar.getProgress();
        return 0.1f + (progress / 10.0f); // 0.1 Hz a 20 Hz
    }
    
    private void setupUIUpdateRunnable() {
        uiUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updateUI();
                uiHandler.postDelayed(this, 50); // Atualizar a cada 50ms
            }
        };
    }
    
    private void toggleRecording() {
        if (!isRecording) {
            // Iniciar gravação
            saveStateForUndo();
            isRecording = true;
            isPlaying = false;
            
            // Criar nova faixa
            int trackId = trackAdapter.getTracks().size() + 1;
            LooperTrackAdapter.LooperTrack newTrack = new LooperTrackAdapter.LooperTrack(trackId, "Faixa " + trackId);
            trackAdapter.addTrack(newTrack);
            
            // Iniciar gravação no motor de áudio
            AudioEngine.startLooperRecording();
            android.util.Log.d("LooperFragment", "Iniciando gravação do looper");
            Toast.makeText(getContext(), "Iniciando gravação do looper", Toast.LENGTH_SHORT).show();
            
            // Atualizar UI
            looperRecordButton.setBackgroundTintList(getResources().getColorStateList(R.color.accent_red));
            looperStatus.setText("Gravando...");
            
            // Iniciar timer
            startUITimer();
            
        } else {
            // Parar gravação
            isRecording = false;
            
            // Parar gravação no motor de áudio
            AudioEngine.stopLooperRecording();
            int samplesGravadas = AudioEngine.getLooperLength();
            android.util.Log.d("LooperFragment", "Parando gravação do looper. Amostras gravadas: " + samplesGravadas);
            Toast.makeText(getContext(), "Parando gravação. Amostras: " + samplesGravadas, Toast.LENGTH_SHORT).show();
            
            // Atualizar comprimento do loop
            loopLength = samplesGravadas;
            
            // Atualizar UI
            looperRecordButton.setBackgroundTintList(getResources().getColorStateList(R.color.red));
            looperStatus.setText("Pronto para reproduzir");
            
            // Calcular batidas se sincronizado
            if (isSynced && currentBPM > 0) {
                float loopDurationSeconds = (float) loopLength / 48000.0f; // Assumindo 48kHz
                beatCount = Math.round(loopDurationSeconds * currentBPM / 60.0f);
                looperBeatCount.setText(beatCount + " batidas");
            }
        }
    }
    
    private void togglePlayback() {
        if (!isPlaying && loopLength > 0) {
            // Iniciar reprodução
            isPlaying = true;
            AudioEngine.startLooperPlayback();
            
            looperPlayButton.setBackgroundTintList(getResources().getColorStateList(R.color.accent_red));
            looperPlayButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_pause, 0, 0, 0);
            looperStatus.setText("Reproduzindo");
            
            startUITimer();
            
        } else if (isPlaying) {
            // Parar reprodução
            isPlaying = false;
            AudioEngine.stopLooperPlayback();
            
            looperPlayButton.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            looperPlayButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);
            looperStatus.setText("Pausado");
            
            stopUITimer();
        }
    }
    
    private void clearLooper() {
        saveStateForUndo();
        
        isRecording = false;
        isPlaying = false;
        loopLength = 0;
        currentPosition = 0;
        beatCount = 0;
        
        AudioEngine.clearLooper();
        trackAdapter.clearTracks();
        
        // Resetar UI
        looperRecordButton.setBackgroundTintList(getResources().getColorStateList(R.color.red));
        looperPlayButton.setBackgroundTintList(getResources().getColorStateList(R.color.green));
        looperPlayButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);
        looperStatus.setText("Pronto");
        looperTimer.setText("00:00");
        looperBeatCount.setText("0 batidas");
        looperProgress.setProgress(0);
        
        stopUITimer();
    }
    
    private void undo() {
        if (!undoStack.isEmpty()) {
            // Salvar estado atual para redo
            saveStateForRedo();
            
            // Restaurar estado anterior
            LooperState previousState = undoStack.pop();
            restoreState(previousState);
        }
    }
    
    private void redo() {
        if (!redoStack.isEmpty()) {
            // Salvar estado atual para undo
            saveStateForUndo();
            
            // Restaurar estado do redo
            LooperState redoState = redoStack.pop();
            restoreState(redoState);
        }
    }
    
    private void saveStateForUndo() {
        LooperState currentState = new LooperState(
            trackAdapter.getTracks(), loopLength, currentPosition, beatCount
        );
        undoStack.push(currentState);
        
        // Limitar tamanho da pilha
        if (undoStack.size() > 10) {
            undoStack.remove(0);
        }
    }
    
    private void saveStateForRedo() {
        LooperState currentState = new LooperState(
            trackAdapter.getTracks(), loopLength, currentPosition, beatCount
        );
        redoStack.push(currentState);
        
        // Limitar tamanho da pilha
        if (redoStack.size() > 10) {
            redoStack.remove(0);
        }
    }
    
    private void restoreState(LooperState state) {
        trackAdapter.clearTracks();
        for (LooperTrackAdapter.LooperTrack track : state.tracks) {
            trackAdapter.addTrack(track);
        }
        
        loopLength = state.loopLength;
        currentPosition = state.currentPosition;
        beatCount = state.beatCount;
        
        updateUI();
    }
    
    private void tapTempo() {
        long currentTime = System.currentTimeMillis();
        tapTimes.add(currentTime);
        
        // Manter apenas os últimos taps
        if (tapTimes.size() > MAX_TAP_TIMES) {
            tapTimes.remove(0);
        }
        
        // Calcular BPM se tivermos pelo menos 2 taps
        if (tapTimes.size() >= 2) {
            long totalTime = tapTimes.get(tapTimes.size() - 1) - tapTimes.get(0);
            long intervals = tapTimes.size() - 1;
            double averageInterval = (double) totalTime / intervals;
            int newBPM = (int) Math.round(60000.0 / averageInterval);
            
            // Limitar BPM a valores razoáveis
            if (newBPM >= 60 && newBPM <= 200) {
                currentBPM = newBPM;
                looperBPMInput.setText(String.valueOf(currentBPM));
                
                if (isSynced) {
                    AudioEngine.startMetronome(currentBPM);
                }
            }
        }
        
        // Limpar taps antigos após 3 segundos
        uiHandler.postDelayed(() -> {
            if (tapTimes.size() > 0) {
                long timeSinceLastTap = System.currentTimeMillis() - tapTimes.get(tapTimes.size() - 1);
                if (timeSinceLastTap > 3000) {
                    tapTimes.clear();
                }
            }
        }, 3000);
    }
    
    private void startUITimer() {
        uiHandler.post(uiUpdateRunnable);
    }
    
    private void stopUITimer() {
        uiHandler.removeCallbacks(uiUpdateRunnable);
    }
    
    private void updateUI() {
        if (isRecording || isPlaying) {
            // Atualizar posição atual
            currentPosition = AudioEngine.getLooperPosition();
            
            // Atualizar timer
            float seconds = (float) currentPosition / 48000.0f; // Assumindo 48kHz
            int minutes = (int) (seconds / 60);
            int secs = (int) (seconds % 60);
            looperTimer.setText(String.format("%02d:%02d", minutes, secs));
            
            // Atualizar barra de progresso
            if (loopLength > 0) {
                int progress = (int) ((float) currentPosition / loopLength * 100);
                looperProgress.setProgress(progress);
            }
        }
    }
    
    private void saveLoopToWav() {
        android.util.Log.d("LooperFragment", "saveLoopToWav() chamado");
        
        // Verificar se há um loop para salvar
        int loopLength = AudioEngine.getLooperLength();
        android.util.Log.d("LooperFragment", "Comprimento do loop: " + loopLength);
        
        if (loopLength == 0) {
            Toast.makeText(getContext(), "Nenhum loop para salvar!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(getContext(), "Iniciando exportação do loop", Toast.LENGTH_SHORT).show();
        
        // Chamar utilitário para exportar o loop como WAV
        float[] mix = AudioEngine.getLooperMix();
        int mixLen = (mix != null) ? mix.length : 0;
        android.util.Log.d("LooperFragment", "Exportando loop. Tamanho do mix: " + mixLen);
        
        if (mixLen == 0) {
            Toast.makeText(getContext(), "Erro: Mix vazio!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(getContext(), "Exportando loop. Mix: " + mixLen + " amostras", Toast.LENGTH_SHORT).show();
        LoopExportUtil.saveCurrentLoopAsWav(getContext(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Loop salvo com sucesso!", Toast.LENGTH_SHORT).show();
                android.util.Log.d("LooperFragment", "Loop salvo com sucesso!");
            } else {
                Toast.makeText(getContext(), "Erro ao salvar loop!", Toast.LENGTH_SHORT).show();
                android.util.Log.e("LooperFragment", "Erro ao salvar loop!");
            }
        });
    }
    
    private void showLoadDialog() {
        // Listar loops salvos
        LoopLoadUtil.listSavedLoops(getContext(), fileNames -> {
            if (fileNames.isEmpty()) {
                Toast.makeText(getContext(), "Nenhum loop salvo encontrado!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Criar array de nomes para o diálogo
            String[] names = fileNames.toArray(new String[0]);
            
            // Mostrar diálogo de seleção
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Carregar Loop")
                .setItems(names, (dialog, which) -> {
                    String selectedFile = names[which];
                    loadLoopFromFile(selectedFile);
                })
                .setNegativeButton("Cancelar", null)
                .show();
        });
    }
    
    private void loadLoopFromFile(String fileName) {
        // Carregar loop do arquivo
        LoopLoadUtil.loadLoopFromFile(getContext(), fileName, (success, loadedFileName) -> {
            if (success) {
                // Atualizar UI após carregar
                updateUIAfterLoad();
                Toast.makeText(getContext(), "Loop carregado: " + loadedFileName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Erro ao carregar loop!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUIAfterLoad() {
        // Atualizar comprimento do loop
        loopLength = AudioEngine.getLooperLength();
        currentPosition = 0;
        
        // Atualizar contador de batidas (aproximado)
        if (loopLength > 0) {
            float seconds = (float) loopLength / 48000.0f;
            beatCount = (int) (seconds * currentBPM / 60.0f);
        }
        
        // Atualizar faixas na UI
        trackAdapter.clearTracks();
        LooperTrackAdapter.LooperTrack loadedTrack = new LooperTrackAdapter.LooperTrack(1, "Loop Carregado");
        loadedTrack.volume = 1.0f;
        loadedTrack.muted = false;
        loadedTrack.soloed = false;
        loadedTrack.length = loopLength;
        trackAdapter.addTrack(loadedTrack);
        
        // Atualizar status
        looperStatus.setText("Loop Carregado");
        looperTimer.setText("00:00");
        looperBeatCount.setText(beatCount + " batidas");
        looperProgress.setProgress(0);
        
        // Atualizar botões
        looperRecordButton.setBackgroundTintList(getResources().getColorStateList(R.color.red));
        looperPlayButton.setBackgroundTintList(getResources().getColorStateList(R.color.green));
        looperPlayButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);
        
        // Parar gravação e reprodução se estiverem ativas
        isRecording = false;
        isPlaying = false;
        stopUITimer();
    }
    
    private void openLibrary() {
        // Navegar para o fragment da biblioteca
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new LoopLibraryFragment())
                .addToBackStack(null)
                .commit();
        }
    }
    
    private void showExportDialog() {
        // Verificar se há um loop para exportar
        if (AudioEngine.getLooperLength() == 0) {
            Toast.makeText(getContext(), "Nenhum loop para exportar!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Mostrar diálogo de exportação
        ExportDialog dialog = ExportDialog.newInstance();
        dialog.show(getChildFragmentManager(), "export_dialog");
    }
    
    // Implementar interface do diálogo de exportação
    public void onExportSelected(LoopExportManager.ExportFormat format, String fileName) {
        // Exportar loop no formato selecionado
        LoopExportManager.exportLoop(getContext(), format, fileName, (success, exportedFileName, formatName) -> {
            if (success) {
                showExportSuccessDialog(exportedFileName, formatName);
            } else {
                Toast.makeText(getContext(), "Erro ao exportar loop!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showExportSuccessDialog(String fileName, String formatName) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Exportação Concluída!")
            .setMessage("Loop exportado com sucesso!\n\n" +
                       "Formato: " + formatName + "\n" +
                       "Arquivo: " + fileName + "\n\n" +
                       "O arquivo foi salvo na pasta interna do app.")
            .setPositiveButton("Compartilhar", (dialog, which) -> {
                shareExportedFile(fileName);
            })
            .setNegativeButton("OK", null)
            .setNeutralButton("Ver na Biblioteca", (dialog, which) -> {
                openLibrary();
            })
            .show();
    }
    
    private void shareExportedFile(String fileName) {
        // Compartilhar o arquivo exportado
        LoopShareUtil.shareLoop(getContext(), fileName);
    }
    
    // Implementação da interface OnTrackActionListener
    @Override
    public void onTrackVolumeChanged(int trackIndex, float volume) {
        // Atualizar volume da faixa no motor de áudio
        AudioEngine.setLooperTrackVolume(trackIndex, volume);
    }
    
    @Override
    public void onTrackMuteToggled(int trackIndex, boolean muted) {
        // Atualizar mute da faixa no motor de áudio
        AudioEngine.setLooperTrackMuted(trackIndex, muted);
    }
    
    @Override
    public void onTrackSoloToggled(int trackIndex, boolean soloed) {
        // Atualizar solo da faixa no motor de áudio
        AudioEngine.setLooperTrackSoloed(trackIndex, soloed);
    }
    
    @Override
    public void onTrackDeleted(int trackIndex) {
        saveStateForUndo();
        
        // Remover faixa do motor de áudio
        AudioEngine.removeLooperTrack(trackIndex);
        
        // Remover faixa da lista
        trackAdapter.removeTrack(trackIndex);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Garantir que o pipeline de áudio está ativo
        AudioEngine.startPipelineIfNeeded();
        // Verificar se há um loop carregado e atualizar UI
        int currentLength = AudioEngine.getLooperLength();
        if (currentLength > 0 && loopLength == 0) {
            // Loop foi carregado enquanto estávamos fora
            updateUIAfterLoad();
        }
        if (isRecording || isPlaying) {
            startUITimer();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        stopUITimer();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUITimer();
    }
} 