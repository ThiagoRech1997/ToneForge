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
    private WaveformView looperWaveformView;
    private Switch looperWaveformGridSwitch;
    private Switch looperWaveformTimeGridSwitch;
    private Switch looperWaveformPlayheadSwitch;
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
    
    // Controles de slicing
    private Switch looperSlicingSwitch;
    private Button looperRandomizeSlicesButton;
    private Button looperReverseSlicesButton;
    private TextView looperSlicesInfoText;
    
    // Controles de Efeitos Avançados (Fase 5)
    private Switch looperCompressionSwitch;
    private Switch looperNormalizationSwitch;
    private SeekBar looperCompressionThresholdSeekBar;
    private TextView looperCompressionThresholdText;
    private SeekBar looperCompressionRatioSeekBar;
    private TextView looperCompressionRatioText;
    private Switch looperLowPassSwitch;
    private SeekBar looperLowPassFreqSeekBar;
    private TextView looperLowPassFreqText;
    private Switch looperHighPassSwitch;
    private SeekBar looperHighPassFreqSeekBar;
    private TextView looperHighPassFreqText;
    private Switch looperReverbTailSwitch;
    private SeekBar looperReverbTailDecaySeekBar;
    private TextView looperReverbTailDecayText;
    
    // Controles de Edição
    private Switch looperEditModeSwitch;
    private Button looperCutButton;
    private Button looperZoomInButton;
    private Button looperZoomOutButton;
    private TextView looperSelectionInfoText;
    
    // Controles de Marcadores
    private Button looperAddMarkerButton;
    private Button looperRemoveMarkerButton;
    private Button looperPrevMarkerButton;
    private Button looperNextMarkerButton;
    private TextView looperMarkerInfoText;
    
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
    
    // Sistema de Marcadores
    private List<Float> markers = new ArrayList<>();
    private int currentMarkerIndex = -1; // -1 = nenhum marcador selecionado
    
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
        looperWaveformView = view.findViewById(R.id.looperWaveformView);
        looperWaveformGridSwitch = view.findViewById(R.id.looperWaveformGridSwitch);
        looperWaveformTimeGridSwitch = view.findViewById(R.id.looperWaveformTimeGridSwitch);
        looperWaveformPlayheadSwitch = view.findViewById(R.id.looperWaveformPlayheadSwitch);
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
        
        // Controles de slicing
        looperSlicingSwitch = view.findViewById(R.id.looperSlicingSwitch);
        looperRandomizeSlicesButton = view.findViewById(R.id.looperRandomizeSlicesButton);
        looperReverseSlicesButton = view.findViewById(R.id.looperReverseSlicesButton);
        looperSlicesInfoText = view.findViewById(R.id.looperSlicesInfoText);
        
        // Controles de Efeitos Avançados (Fase 5)
        looperCompressionSwitch = view.findViewById(R.id.looperCompressionSwitch);
        looperNormalizationSwitch = view.findViewById(R.id.looperNormalizationSwitch);
        looperCompressionThresholdSeekBar = view.findViewById(R.id.looperCompressionThresholdSeekBar);
        looperCompressionThresholdText = view.findViewById(R.id.looperCompressionThresholdText);
        looperCompressionRatioSeekBar = view.findViewById(R.id.looperCompressionRatioSeekBar);
        looperCompressionRatioText = view.findViewById(R.id.looperCompressionRatioText);
        looperLowPassSwitch = view.findViewById(R.id.looperLowPassSwitch);
        looperLowPassFreqSeekBar = view.findViewById(R.id.looperLowPassFreqSeekBar);
        looperLowPassFreqText = view.findViewById(R.id.looperLowPassFreqText);
        looperHighPassSwitch = view.findViewById(R.id.looperHighPassSwitch);
        looperHighPassFreqSeekBar = view.findViewById(R.id.looperHighPassFreqSeekBar);
        looperHighPassFreqText = view.findViewById(R.id.looperHighPassFreqText);
        looperReverbTailSwitch = view.findViewById(R.id.looperReverbTailSwitch);
        looperReverbTailDecaySeekBar = view.findViewById(R.id.looperReverbTailDecaySeekBar);
        looperReverbTailDecayText = view.findViewById(R.id.looperReverbTailDecayText);
        
        // Controles de Edição
        looperEditModeSwitch = view.findViewById(R.id.looperEditModeSwitch);
        looperCutButton = view.findViewById(R.id.looperCutButton);
        looperZoomInButton = view.findViewById(R.id.looperZoomInButton);
        looperZoomOutButton = view.findViewById(R.id.looperZoomOutButton);
        looperSelectionInfoText = view.findViewById(R.id.looperSelectionInfoText);
        
        // Controles de Marcadores
        looperAddMarkerButton = view.findViewById(R.id.looperAddMarkerButton);
        looperRemoveMarkerButton = view.findViewById(R.id.looperRemoveMarkerButton);
        looperPrevMarkerButton = view.findViewById(R.id.looperPrevMarkerButton);
        looperNextMarkerButton = view.findViewById(R.id.looperNextMarkerButton);
        looperMarkerInfoText = view.findViewById(R.id.looperMarkerInfoText);
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
                        // Atualizar BPM da grade de tempo
                        looperWaveformView.setGridBPM(currentBPM);
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
        
        // Controles de edição
        setupEditControls();
        
        // Controles de marcadores
        setupMarkerControls();
        
        // Controles da visualização de onda
        setupWaveformControls();
        
        // Controles de slicing
        setupSlicingControls();
        
        // Controles de Efeitos Avançados (Fase 5)
        setupAdvancedEffectsControls();
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
    
    private void setupWaveformControls() {
        // Switch para mostrar/ocultar grade
        looperWaveformGridSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            looperWaveformView.setShowGrid(isChecked);
        });
        
        // Switch para mostrar/ocultar grade de tempo
        looperWaveformTimeGridSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            looperWaveformView.setShowTimeGrid(isChecked);
            if (isChecked) {
                // Atualizar BPM da grade quando ativada
                looperWaveformView.setGridBPM(currentBPM);
            }
        });
        
        // Switch para mostrar/ocultar playhead
        looperWaveformPlayheadSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            looperWaveformView.setShowPlayhead(isChecked);
        });
        
        // Listener para cliques na forma de onda
        looperWaveformView.setOnWaveformClickListener(position -> {
            // Aqui podemos implementar funcionalidades como:
            // - Pular para uma posição específica no loop
            // - Marcar pontos de interesse
            // - Editar trechos específicos
            android.util.Log.d("LooperFragment", "Waveform clicada na posição: " + position);
        });
    }
    
    private void setupSlicingControls() {
        // Switch para ativar/desativar slicing
        looperSlicingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setLooperSlicingEnabled(isChecked);
            looperRandomizeSlicesButton.setEnabled(isChecked);
            looperReverseSlicesButton.setEnabled(isChecked);
            updateSlicesInfo();
            android.util.Log.d("LooperFragment", "Slicing: " + (isChecked ? "ON" : "OFF"));
        });
        
        // Botão para randomizar slices
        looperRandomizeSlicesButton.setOnClickListener(v -> {
            AudioEngine.randomizeLooperSlices();
            updateSlicesInfo();
            android.util.Log.d("LooperFragment", "Slices randomizados");
        });
        
        // Botão para reverter ordem dos slices
        looperReverseSlicesButton.setOnClickListener(v -> {
            AudioEngine.reverseLooperSlices();
            updateSlicesInfo();
            android.util.Log.d("LooperFragment", "Ordem dos slices revertida");
        });
    }
    
    private void setupAdvancedEffectsControls() {
        // Compressão automática
        looperCompressionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setLooperAutoCompression(isChecked);
            looperCompressionThresholdSeekBar.setEnabled(isChecked);
            looperCompressionRatioSeekBar.setEnabled(isChecked);
            android.util.Log.d("LooperFragment", "Compressão automática: " + (isChecked ? "ON" : "OFF"));
        });
        
        // Normalização automática
        looperNormalizationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setLooperAutoNormalization(isChecked);
            android.util.Log.d("LooperFragment", "Normalização automática: " + (isChecked ? "ON" : "OFF"));
        });
        
        // Threshold da compressão
        looperCompressionThresholdSeekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float threshold = (progress / 100.0f) * 60.0f - 60.0f; // -60 a 0 dB
                    AudioEngine.setLooperCompressionThreshold(threshold);
                    looperCompressionThresholdText.setText(String.format("%.0fdB", threshold));
                }
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
        
        // Ratio da compressão
        looperCompressionRatioSeekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float ratio = 1.0f + (progress / 100.0f) * 19.0f; // 1.0 a 20.0
                    AudioEngine.setLooperCompressionRatio(ratio);
                    looperCompressionRatioText.setText(String.format("%.1f:1", ratio));
                }
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
        
        // Filtro passa-baixa
        looperLowPassSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setLooperLowPassFilter(isChecked);
            looperLowPassFreqSeekBar.setEnabled(isChecked);
            android.util.Log.d("LooperFragment", "Filtro passa-baixa: " + (isChecked ? "ON" : "OFF"));
        });
        
        // Frequência do filtro passa-baixa
        looperLowPassFreqSeekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float frequency = 100.0f + (progress / 100.0f) * 19900.0f; // 100Hz a 20kHz
                    AudioEngine.setLooperLowPassFrequency(frequency);
                    if (frequency >= 1000) {
                        looperLowPassFreqText.setText(String.format("%.0fkHz", frequency / 1000));
                    } else {
                        looperLowPassFreqText.setText(String.format("%.0fHz", frequency));
                    }
                }
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
        
        // Filtro passa-alta
        looperHighPassSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setLooperHighPassFilter(isChecked);
            looperHighPassFreqSeekBar.setEnabled(isChecked);
            android.util.Log.d("LooperFragment", "Filtro passa-alta: " + (isChecked ? "ON" : "OFF"));
        });
        
        // Frequência do filtro passa-alta
        looperHighPassFreqSeekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float frequency = 20.0f + (progress / 100.0f) * 1980.0f; // 20Hz a 2kHz
                    AudioEngine.setLooperHighPassFrequency(frequency);
                    if (frequency >= 1000) {
                        looperHighPassFreqText.setText(String.format("%.1fkHz", frequency / 1000));
                    } else {
                        looperHighPassFreqText.setText(String.format("%.0fHz", frequency));
                    }
                }
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
        
        // Reverb de cauda
        looperReverbTailSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AudioEngine.setLooperReverbTail(isChecked);
            looperReverbTailDecaySeekBar.setEnabled(isChecked);
            android.util.Log.d("LooperFragment", "Reverb de cauda: " + (isChecked ? "ON" : "OFF"));
        });
        
        // Decay do reverb de cauda
        looperReverbTailDecaySeekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float decay = 0.1f + (progress / 100.0f) * 9.9f; // 0.1 a 10 segundos
                    AudioEngine.setLooperReverbTailDecay(decay);
                    looperReverbTailDecayText.setText(String.format("%.1fs", decay));
                }
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
        
        // Configurar estados iniciais
        looperCompressionThresholdSeekBar.setEnabled(false);
        looperCompressionRatioSeekBar.setEnabled(false);
        looperLowPassFreqSeekBar.setEnabled(false);
        looperHighPassFreqSeekBar.setEnabled(false);
        looperReverbTailDecaySeekBar.setEnabled(false);
    }
    
    private void setupEditControls() {
        // Switch do modo de edição
        looperEditModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            looperWaveformView.setEditMode(isChecked);
            updateEditButtonsState();
        });
        
        // Botão Cut
        looperCutButton.setOnClickListener(v -> {
            if (looperWaveformView.hasSelection()) {
                cutSelectedRegion();
            }
        });
        
        // Botão Zoom In
        looperZoomInButton.setOnClickListener(v -> {
            looperWaveformView.zoomIn();
        });
        
        // Botão Zoom Out
        looperZoomOutButton.setOnClickListener(v -> {
            looperWaveformView.zoomOut();
        });
        
        // Listener para mudanças na seleção
        looperWaveformView.setOnWaveformSelectionListener((start, end) -> {
            updateSelectionInfo(start, end);
            updateEditButtonsState();
        });
    }
    
    private void setupMarkerControls() {
        // Botão Adicionar Marcador
        looperAddMarkerButton.setOnClickListener(v -> {
            addMarkerAtCurrentPosition();
        });
        
        // Botão Remover Marcador
        looperRemoveMarkerButton.setOnClickListener(v -> {
            removeCurrentMarker();
        });
        
        // Botão Marcador Anterior
        looperPrevMarkerButton.setOnClickListener(v -> {
            goToPreviousMarker();
        });
        
        // Botão Próximo Marcador
        looperNextMarkerButton.setOnClickListener(v -> {
            goToNextMarker();
        });
        
        updateMarkerButtonsState();
    }
    
    private void updateEditButtonsState() {
        boolean hasSelection = looperWaveformView.hasSelection();
        boolean editMode = looperEditModeSwitch.isChecked();
        
        looperCutButton.setEnabled(editMode && hasSelection);
        looperZoomInButton.setEnabled(editMode);
        looperZoomOutButton.setEnabled(editMode);
    }
    
    private void updateSelectionInfo(float start, float end) {
        if (start > end) {
            float temp = start;
            start = end;
            end = temp;
        }
        
        int startSample = (int) (start * loopLength);
        int endSample = (int) (end * loopLength);
        int durationMs = (int) ((endSample - startSample) / (AudioEngine.SAMPLE_RATE / 1000.0));
        
        String info = String.format("Seleção: %.1fs - %.1fs (%dms)", 
                                  start * (loopLength / (float)AudioEngine.SAMPLE_RATE),
                                  end * (loopLength / (float)AudioEngine.SAMPLE_RATE),
                                  durationMs);
        looperSelectionInfoText.setText(info);
    }
    
    private void cutSelectedRegion() {
        float start = looperWaveformView.getSelectionStart();
        float end = looperWaveformView.getSelectionEnd();
        
        if (start > end) {
            float temp = start;
            start = end;
            end = temp;
        }
        
        // Implementar corte no código nativo
        AudioEngine.cutLooperRegion(start, end);
        
        // Limpar seleção
        looperWaveformView.clearSelection();
        updateSelectionInfo(0, 0);
        updateEditButtonsState();
        
        // Atualizar visualização
        updateWaveformData();
    }
    

    
    // Métodos dos Marcadores
    private void addMarkerAtCurrentPosition() {
        if (loopLength == 0) {
            Toast.makeText(getContext(), "Nenhum loop para marcar!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        float position = (float) currentPosition / loopLength;
        position = Math.max(0.0f, Math.min(1.0f, position));
        
        // Verificar se já existe um marcador próximo
        for (int i = 0; i < markers.size(); i++) {
            if (Math.abs(markers.get(i) - position) < 0.01f) {
                Toast.makeText(getContext(), "Marcador já existe nesta posição!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // Adicionar marcador
        markers.add(position);
        markers.sort(Float::compareTo); // Ordenar por posição
        
        // Atualizar índice do marcador atual
        currentMarkerIndex = markers.indexOf(position);
        
        // Atualizar UI
        updateMarkerButtonsState();
        updateMarkerInfo();
        looperWaveformView.setMarkers(markers);
        
        Toast.makeText(getContext(), "Marcador adicionado!", Toast.LENGTH_SHORT).show();
    }
    
    private void removeCurrentMarker() {
        if (currentMarkerIndex >= 0 && currentMarkerIndex < markers.size()) {
            markers.remove(currentMarkerIndex);
            currentMarkerIndex = -1;
            
            updateMarkerButtonsState();
            updateMarkerInfo();
            looperWaveformView.setMarkers(markers);
            
            Toast.makeText(getContext(), "Marcador removido!", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void goToPreviousMarker() {
        if (markers.isEmpty()) return;
        
        if (currentMarkerIndex <= 0) {
            currentMarkerIndex = markers.size() - 1;
        } else {
            currentMarkerIndex--;
        }
        
        float markerPosition = markers.get(currentMarkerIndex);
        int targetSample = (int) (markerPosition * loopLength);
        
        // Pular para a posição do marcador
        AudioEngine.setLooperPosition(targetSample);
        currentPosition = targetSample;
        
        updateMarkerButtonsState();
        updateMarkerInfo();
        
        Toast.makeText(getContext(), "Marcador " + (currentMarkerIndex + 1), Toast.LENGTH_SHORT).show();
    }
    
    private void goToNextMarker() {
        if (markers.isEmpty()) return;
        
        if (currentMarkerIndex >= markers.size() - 1) {
            currentMarkerIndex = 0;
        } else {
            currentMarkerIndex++;
        }
        
        float markerPosition = markers.get(currentMarkerIndex);
        int targetSample = (int) (markerPosition * loopLength);
        
        // Pular para a posição do marcador
        AudioEngine.setLooperPosition(targetSample);
        currentPosition = targetSample;
        
        updateMarkerButtonsState();
        updateMarkerInfo();
        
        Toast.makeText(getContext(), "Marcador " + (currentMarkerIndex + 1), Toast.LENGTH_SHORT).show();
    }
    
    private void updateMarkerButtonsState() {
        boolean hasLoop = loopLength > 0;
        boolean hasMarkers = !markers.isEmpty();
        boolean hasSelectedMarker = currentMarkerIndex >= 0 && currentMarkerIndex < markers.size();
        
        looperAddMarkerButton.setEnabled(hasLoop);
        looperRemoveMarkerButton.setEnabled(hasSelectedMarker);
        looperPrevMarkerButton.setEnabled(hasMarkers);
        looperNextMarkerButton.setEnabled(hasMarkers);
    }
    
    private void updateMarkerInfo() {
        if (markers.isEmpty()) {
            looperMarkerInfoText.setText("0 marcadores");
        } else if (currentMarkerIndex >= 0 && currentMarkerIndex < markers.size()) {
            float markerPosition = markers.get(currentMarkerIndex);
            float seconds = markerPosition * (loopLength / 48000.0f);
            int minutes = (int) (seconds / 60);
            int secs = (int) (seconds % 60);
            
            String info = String.format("Marcador %d/%d (%.2d:%.2d)", 
                                      currentMarkerIndex + 1, markers.size(), minutes, secs);
            looperMarkerInfoText.setText(info);
        } else {
            looperMarkerInfoText.setText(markers.size() + " marcadores");
        }
    }
    
    private void updateSlicesInfo() {
        if (AudioEngine.isLooperSlicingEnabled()) {
            int numSlices = AudioEngine.getLooperNumSlices();
            int sliceLength = AudioEngine.getLooperSliceLength();
            looperSlicesInfoText.setText(numSlices + " slices");
        } else {
            looperSlicesInfoText.setText("0 slices");
        }
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
        
        // Limpar visualização de onda
        looperWaveformView.setWaveformData(new float[0]);
        looperWaveformView.setPlayheadPosition(0.0f);
        looperWaveformView.setPlaying(false);
        looperWaveformView.setMarkers(new ArrayList<>());
        
        // Limpar marcadores
        markers.clear();
        currentMarkerIndex = -1;
        
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
                
                // Atualizar posição do playhead na visualização
                float playheadPosition = (float) currentPosition / loopLength;
                looperWaveformView.setPlayheadPosition(playheadPosition);
            }
        }
        
        // Atualizar estado de reprodução na visualização
        looperWaveformView.setPlaying(isPlaying);
        
        // Atualizar dados da forma de onda periodicamente (a cada 500ms)
        if (System.currentTimeMillis() % 500 < 50) {
            updateWaveformData();
        }
        
        // Atualizar informações de slicing periodicamente
        if (System.currentTimeMillis() % 1000 < 50) {
            updateSlicesInfo();
        }
        
        // Atualizar controles de marcadores periodicamente
        if (System.currentTimeMillis() % 500 < 50) {
            updateMarkerButtonsState();
            updateMarkerInfo();
        }
    }
    
    private void updateWaveformData() {
        // Obter dados do mix do looper
        float[] mix = AudioEngine.getLooperMix();
        if (mix != null && mix.length > 0) {
            looperWaveformView.setWaveformData(mix);
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
        
        // Atualizar visualização de onda
        updateWaveformData();
        looperWaveformView.setPlayheadPosition(0.0f);
        looperWaveformView.setPlaying(false);
        
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