package com.thiagofernendorech.toneforge.ui.fragments.looper;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.thiagofernendorech.toneforge.R;
import com.thiagofernendorech.toneforge.ui.base.BaseFragment;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.LooperTrackAdapter;
import com.thiagofernendorech.toneforge.WaveformView;
import com.thiagofernendorech.toneforge.ExportDialog;
import com.thiagofernendorech.toneforge.LoopLibraryFragment;
import com.thiagofernendorech.toneforge.MainActivity;
import java.util.List;

/**
 * LooperFragment refatorado usando arquitetura MVP
 * Versão limpa e modular do fragment original
 */
public class LooperFragmentRefactored extends BaseFragment<LooperPresenter> 
    implements LooperContract.View, LooperTrackAdapter.OnTrackActionListener, ExportDialog.ExportDialogListener {
    
    // === UI COMPONENTS ===
    
    // Status e controles principais
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
    
    // Controles de arquivo
    private Button looperSaveButton;
    private Button looperLoadButton;
    private Button looperLibraryButton;
    private Button looperExportButton;
    
    // Waveform
    private WaveformView looperWaveformView;
    private Switch looperWaveformGridSwitch;
    private Switch looperWaveformTimeGridSwitch;
    private Switch looperWaveformPlayheadSwitch;
    
    // Tracks
    private RecyclerView looperTracksList;
    private LooperTrackAdapter trackAdapter;
    
    // Efeitos especiais
    private Switch looperReverseSwitch;
    private SeekBar looperSpeedSeekBar;
    private TextView looperSpeedText;
    private SeekBar looperPitchSeekBar;
    private TextView looperPitchText;
    private Switch looperStutterSwitch;
    private SeekBar looperStutterRateSeekBar;
    private TextView looperStutterRateText;
    
    // Slicing
    private Switch looperSlicingSwitch;
    private Button looperRandomizeSlicesButton;
    private Button looperReverseSlicesButton;
    private TextView looperSlicesInfoText;
    
    // Filtros
    private Switch looperLowPassSwitch;
    private SeekBar looperLowPassFreqSeekBar;
    private TextView looperLowPassFreqText;
    private Switch looperHighPassSwitch;
    private SeekBar looperHighPassFreqSeekBar;
    private TextView looperHighPassFreqText;
    
    // Fade
    private Switch looperFadeInSwitch;
    private SeekBar looperFadeInDurationSeekBar;
    private TextView looperFadeInDurationText;
    private Switch looperFadeOutSwitch;
    private SeekBar looperFadeOutDurationSeekBar;
    private TextView looperFadeOutDurationText;
    
    // Marcadores
    private Button looperAddMarkerButton;
    private Button looperRemoveMarkerButton;
    private Button looperPrevMarkerButton;
    private Button looperNextMarkerButton;
    private TextView looperMarkerInfoText;
    
    // Quantização
    private Switch looperQuantizationSwitch;
    private Spinner looperQuantizationGridSpinner;
    
    // MIDI e notificações
    private Switch looperMidiSwitch;
    private Switch looperNotificationSwitch;
    
    @Override
    protected LooperPresenter createPresenter() {
        AudioRepository audioRepository = AudioRepository.getInstance(requireContext());
        return new LooperPresenter(requireContext(), audioRepository);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_looper, container, false);
        
        initializeViews(view);
        setupClickListeners();
        setupSeekBarListeners();
        setupSwitchListeners();
        setupRecyclerView();
        setupWaveformView();
        
        return view;
    }
    
    @Override
    protected void onViewReady() {
        super.onViewReady();
        if (presenter != null) {
            presenter.onViewStarted();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.onViewResumed();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (presenter != null) {
            presenter.onViewPaused();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.onViewDestroyed();
        }
    }
    
    // === INICIALIZAÇÃO ===
    
    private void initializeViews(View view) {
        // Status e controles principais
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
        
        // Controles de arquivo
        looperSaveButton = view.findViewById(R.id.looperSaveButton);
        looperLoadButton = view.findViewById(R.id.looperLoadButton);
        looperLibraryButton = view.findViewById(R.id.looperLibraryButton);
        looperExportButton = view.findViewById(R.id.looperExportButton);
        
        // Waveform
        looperWaveformView = view.findViewById(R.id.looperWaveformView);
        looperWaveformGridSwitch = view.findViewById(R.id.looperWaveformGridSwitch);
        looperWaveformTimeGridSwitch = view.findViewById(R.id.looperWaveformTimeGridSwitch);
        looperWaveformPlayheadSwitch = view.findViewById(R.id.looperWaveformPlayheadSwitch);
        
        // Tracks
        looperTracksList = view.findViewById(R.id.looperTracksList);
        
        // Efeitos especiais
        looperReverseSwitch = view.findViewById(R.id.looperReverseSwitch);
        looperSpeedSeekBar = view.findViewById(R.id.looperSpeedSeekBar);
        looperSpeedText = view.findViewById(R.id.looperSpeedText);
        looperPitchSeekBar = view.findViewById(R.id.looperPitchSeekBar);
        looperPitchText = view.findViewById(R.id.looperPitchText);
        looperStutterSwitch = view.findViewById(R.id.looperStutterSwitch);
        looperStutterRateSeekBar = view.findViewById(R.id.looperStutterRateSeekBar);
        looperStutterRateText = view.findViewById(R.id.looperStutterRateText);
        
        // Slicing
        looperSlicingSwitch = view.findViewById(R.id.looperSlicingSwitch);
        looperRandomizeSlicesButton = view.findViewById(R.id.looperRandomizeSlicesButton);
        looperReverseSlicesButton = view.findViewById(R.id.looperReverseSlicesButton);
        looperSlicesInfoText = view.findViewById(R.id.looperSlicesInfoText);
        
        // Filtros
        looperLowPassSwitch = view.findViewById(R.id.looperLowPassSwitch);
        looperLowPassFreqSeekBar = view.findViewById(R.id.looperLowPassFreqSeekBar);
        looperLowPassFreqText = view.findViewById(R.id.looperLowPassFreqText);
        looperHighPassSwitch = view.findViewById(R.id.looperHighPassSwitch);
        looperHighPassFreqSeekBar = view.findViewById(R.id.looperHighPassFreqSeekBar);
        looperHighPassFreqText = view.findViewById(R.id.looperHighPassFreqText);
        
        // Fade
        looperFadeInSwitch = view.findViewById(R.id.looperFadeInSwitch);
        looperFadeInDurationSeekBar = view.findViewById(R.id.looperFadeInDurationSeekBar);
        looperFadeInDurationText = view.findViewById(R.id.looperFadeInDurationText);
        looperFadeOutSwitch = view.findViewById(R.id.looperFadeOutSwitch);
        looperFadeOutDurationSeekBar = view.findViewById(R.id.looperFadeOutDurationSeekBar);
        looperFadeOutDurationText = view.findViewById(R.id.looperFadeOutDurationText);
        
        // Marcadores
        looperAddMarkerButton = view.findViewById(R.id.looperAddMarkerButton);
        looperRemoveMarkerButton = view.findViewById(R.id.looperRemoveMarkerButton);
        looperPrevMarkerButton = view.findViewById(R.id.looperPrevMarkerButton);
        looperNextMarkerButton = view.findViewById(R.id.looperNextMarkerButton);
        looperMarkerInfoText = view.findViewById(R.id.looperMarkerInfoText);
        
        // Quantização
        looperQuantizationSwitch = view.findViewById(R.id.looperQuantizationSwitch);
        looperQuantizationGridSpinner = view.findViewById(R.id.looperQuantizationGridSpinner);
        
        // MIDI e notificações
        looperMidiSwitch = view.findViewById(R.id.looperMidiSwitch);
        looperNotificationSwitch = view.findViewById(R.id.looperNotificationSwitch);
    }
    
    private void setupClickListeners() {
        // Controles principais
        looperRecordButton.setOnClickListener(v -> {
            if (presenter != null) presenter.toggleRecording();
        });
        
        looperPlayButton.setOnClickListener(v -> {
            if (presenter != null) presenter.togglePlayback();
        });
        
        looperClearButton.setOnClickListener(v -> {
            if (presenter != null) presenter.clearLoop();
        });
        
        looperUndoButton.setOnClickListener(v -> {
            if (presenter != null) presenter.undo();
        });
        
        looperTapTempoButton.setOnClickListener(v -> {
            if (presenter != null) presenter.tapTempo();
        });
        
        // Controles de arquivo
        looperSaveButton.setOnClickListener(v -> showSaveLoopDialog());
        looperLoadButton.setOnClickListener(v -> showLoadLoopDialog());
        looperLibraryButton.setOnClickListener(v -> openLoopLibrary());
        looperExportButton.setOnClickListener(v -> showExportDialog());
        
        // Slicing
        looperRandomizeSlicesButton.setOnClickListener(v -> {
            if (presenter != null) presenter.randomizeSlices();
        });
        
        looperReverseSlicesButton.setOnClickListener(v -> {
            if (presenter != null) presenter.reverseSlices();
        });
        
        // Marcadores
        looperAddMarkerButton.setOnClickListener(v -> {
            if (presenter != null) presenter.addMarker();
        });
        
        looperRemoveMarkerButton.setOnClickListener(v -> {
            if (presenter != null) presenter.removeMarker();
        });
        
        looperPrevMarkerButton.setOnClickListener(v -> {
            if (presenter != null) presenter.goToPreviousMarker();
        });
        
        looperNextMarkerButton.setOnClickListener(v -> {
            if (presenter != null) presenter.goToNextMarker();
        });
    }
    
    private void setupSeekBarListeners() {
        // Speed
        looperSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && presenter != null) {
                    float speed = 0.25f + (progress / 100.0f) * 3.75f; // 0.25x a 4.0x
                    presenter.setSpeed(speed);
                }
            }
            
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Pitch
        looperPitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && presenter != null) {
                    float pitch = (progress - 120) / 10.0f; // -12 a +12 semitons
                    presenter.setPitch(pitch);
                }
            }
            
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Stutter rate
        looperStutterRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && presenter != null) {
                    float rate = 0.1f + (progress / 10.0f); // 0.1 Hz a 20 Hz
                    presenter.setStutter(looperStutterSwitch.isChecked(), rate);
                }
            }
            
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Filtros
        looperLowPassFreqSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && presenter != null) {
                    float frequency = 100.0f + (progress / 100.0f) * 19900.0f; // 100Hz a 20kHz
                    presenter.setLowPassFrequency(frequency);
                }
            }
            
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        looperHighPassFreqSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && presenter != null) {
                    float frequency = 20.0f + (progress / 100.0f) * 1980.0f; // 20Hz a 2kHz
                    presenter.setHighPassFrequency(frequency);
                }
            }
            
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Fade durations
        looperFadeInDurationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && presenter != null) {
                    float duration = 0.01f + (progress / 100.0f) * 0.99f; // 0.01s a 1.0s
                    presenter.setFadeInDuration(duration);
                }
            }
            
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        looperFadeOutDurationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && presenter != null) {
                    float duration = 0.01f + (progress / 100.0f) * 0.99f; // 0.01s a 1.0s
                    presenter.setFadeOutDuration(duration);
                }
            }
            
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private void setupSwitchListeners() {
        // Sync
        looperSyncSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.toggleSync();
        });
        
        // Reverse
        looperReverseSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setReverse(isChecked);
        });
        
        // Stutter
        looperStutterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) {
                float rate = 0.1f + (looperStutterRateSeekBar.getProgress() / 10.0f);
                presenter.setStutter(isChecked, rate);
            }
        });
        
        // Waveform
        looperWaveformGridSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setWaveformGrid(isChecked);
        });
        
        looperWaveformTimeGridSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setWaveformTimeGrid(isChecked);
        });
        
        looperWaveformPlayheadSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setWaveformPlayhead(isChecked);
        });
        
        // Slicing
        looperSlicingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setSlicing(isChecked);
        });
        
        // Filtros
        looperLowPassSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setLowPassFilter(isChecked);
        });
        
        looperHighPassSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setHighPassFilter(isChecked);
        });
        
        // Fade
        looperFadeInSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setAutoFadeIn(isChecked);
        });
        
        looperFadeOutSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) presenter.setAutoFadeOut(isChecked);
        });
        
        // MIDI
        looperMidiSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Implementar MIDI quando necessário
        });
        
        // Notificações
        looperNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Implementar notificações quando necessário
        });
    }
    
    private void setupRecyclerView() {
        trackAdapter = new LooperTrackAdapter(this);
        looperTracksList.setLayoutManager(new LinearLayoutManager(getContext()));
        looperTracksList.setAdapter(trackAdapter);
    }
    
    private void setupWaveformView() {
        if (looperWaveformView != null) {
            looperWaveformView.setOnWaveformClickListener(position -> {
                if (presenter != null) presenter.onWaveformClicked(position);
            });
        }
    }
    
    // === Implementação da interface LooperContract.View ===
    
    @Override
    public void updateLooperStatus(String status, boolean isRecording, boolean isPlaying) {
        if (looperStatus != null) {
            looperStatus.setText(status);
        }
        updateRecordButton(isRecording);
        updatePlayButton(isPlaying);
    }
    
    @Override
    public void updateLooperTimer(int minutes, int seconds) {
        if (looperTimer != null) {
            looperTimer.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }
    
    @Override
    public void updateBeatCount(int beatCount) {
        if (looperBeatCount != null) {
            looperBeatCount.setText(beatCount + " batidas");
        }
    }
    
    @Override
    public void updateProgress(int progress) {
        if (looperProgress != null) {
            looperProgress.setProgress(progress);
        }
    }
    
    @Override
    public void updateBPM(int bpm) {
        if (looperBPMInput != null) {
            looperBPMInput.setText(String.valueOf(bpm));
        }
    }
    
    @Override
    public void updateRecordButton(boolean isRecording) {
        if (looperRecordButton != null) {
            int colorRes = isRecording ? R.color.red : R.color.gray;
            looperRecordButton.setBackgroundTintList(getResources().getColorStateList(colorRes));
        }
    }
    
    @Override
    public void updatePlayButton(boolean isPlaying) {
        if (looperPlayButton != null) {
            int colorRes = isPlaying ? R.color.green : R.color.gray;
            looperPlayButton.setBackgroundTintList(getResources().getColorStateList(colorRes));
            
            int iconRes = isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
            looperPlayButton.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
        }
    }
    
    @Override
    public void updateSyncButton(boolean isSynced) {
        if (looperSyncSwitch != null) {
            looperSyncSwitch.setChecked(isSynced);
        }
    }
    
    @Override
    public void updateTracksList(List<LooperTrackAdapter.LooperTrack> tracks) {
        if (trackAdapter != null) {
            trackAdapter.clearTracks();
            for (LooperTrackAdapter.LooperTrack track : tracks) {
                trackAdapter.addTrack(track);
            }
        }
    }
    
    @Override
    public void addTrack(LooperTrackAdapter.LooperTrack track) {
        if (trackAdapter != null) {
            trackAdapter.addTrack(track);
        }
    }
    
    @Override
    public void removeTrack(int trackIndex) {
        if (trackAdapter != null) {
            trackAdapter.removeTrack(trackIndex);
        }
    }
    
    @Override
    public void updateTrack(int trackIndex, LooperTrackAdapter.LooperTrack track) {
        if (trackAdapter != null) {
            trackAdapter.updateTrack(trackIndex, track);
        }
    }
    
    @Override
    public void updateSpeed(float speed) {
        if (looperSpeedSeekBar != null && looperSpeedText != null) {
            int progress = (int) ((speed - 0.25f) / 3.75f * 100);
            looperSpeedSeekBar.setProgress(progress);
            looperSpeedText.setText(String.format("%.1fx", speed));
        }
    }
    
    @Override
    public void updatePitch(float pitch) {
        if (looperPitchSeekBar != null && looperPitchText != null) {
            int progress = (int) (pitch * 10 + 120);
            looperPitchSeekBar.setProgress(progress);
            looperPitchText.setText(String.format("%.1f", pitch));
        }
    }
    
    @Override
    public void updateStutter(boolean enabled, float rate) {
        if (looperStutterSwitch != null) {
            looperStutterSwitch.setChecked(enabled);
        }
        if (looperStutterRateSeekBar != null) {
            looperStutterRateSeekBar.setEnabled(enabled);
            int progress = (int) ((rate - 0.1f) * 10);
            looperStutterRateSeekBar.setProgress(progress);
        }
        if (looperStutterRateText != null) {
            looperStutterRateText.setText(String.format("%.1fHz", rate));
            int colorRes = enabled ? R.color.white : R.color.gray;
            looperStutterRateText.setTextColor(getResources().getColor(colorRes));
        }
    }
    
    @Override
    public void updateReverse(boolean enabled) {
        if (looperReverseSwitch != null) {
            looperReverseSwitch.setChecked(enabled);
        }
    }
    
    @Override
    public void updateWaveformData(float[] waveformData) {
        if (looperWaveformView != null) {
            looperWaveformView.setWaveformData(waveformData);
        }
    }
    
    @Override
    public void updatePlayheadPosition(float position) {
        if (looperWaveformView != null) {
            looperWaveformView.setPlayheadPosition(position);
        }
    }
    
    @Override
    public void updateWaveformSettings(boolean showGrid, boolean showTimeGrid, boolean showPlayhead) {
        if (looperWaveformView != null) {
            looperWaveformView.setShowGrid(showGrid);
            looperWaveformView.setShowTimeGrid(showTimeGrid);
            looperWaveformView.setShowPlayhead(showPlayhead);
        }
        if (looperWaveformGridSwitch != null) {
            looperWaveformGridSwitch.setChecked(showGrid);
        }
        if (looperWaveformTimeGridSwitch != null) {
            looperWaveformTimeGridSwitch.setChecked(showTimeGrid);
        }
        if (looperWaveformPlayheadSwitch != null) {
            looperWaveformPlayheadSwitch.setChecked(showPlayhead);
        }
    }
    
    @Override
    public void updateSlicing(boolean enabled, int numSlices, int sliceLength) {
        if (looperSlicingSwitch != null) {
            looperSlicingSwitch.setChecked(enabled);
        }
        if (looperSlicesInfoText != null) {
            if (enabled) {
                looperSlicesInfoText.setText(numSlices + " slices");
            } else {
                looperSlicesInfoText.setText("0 slices");
            }
        }
        if (looperRandomizeSlicesButton != null) {
            looperRandomizeSlicesButton.setEnabled(enabled);
        }
        if (looperReverseSlicesButton != null) {
            looperReverseSlicesButton.setEnabled(enabled);
        }
    }
    
    @Override
    public void updateFilters(boolean lowPassEnabled, float lowPassFreq, boolean highPassEnabled, float highPassFreq) {
        // Low pass
        if (looperLowPassSwitch != null) {
            looperLowPassSwitch.setChecked(lowPassEnabled);
        }
        if (looperLowPassFreqSeekBar != null) {
            looperLowPassFreqSeekBar.setEnabled(lowPassEnabled);
            int progress = (int) ((lowPassFreq - 100.0f) / 19900.0f * 100);
            looperLowPassFreqSeekBar.setProgress(progress);
        }
        if (looperLowPassFreqText != null) {
            if (lowPassFreq >= 1000) {
                looperLowPassFreqText.setText(String.format("%.1fkHz", lowPassFreq / 1000));
            } else {
                looperLowPassFreqText.setText(String.format("%.0fHz", lowPassFreq));
            }
        }
        
        // High pass
        if (looperHighPassSwitch != null) {
            looperHighPassSwitch.setChecked(highPassEnabled);
        }
        if (looperHighPassFreqSeekBar != null) {
            looperHighPassFreqSeekBar.setEnabled(highPassEnabled);
            int progress = (int) ((highPassFreq - 20.0f) / 1980.0f * 100);
            looperHighPassFreqSeekBar.setProgress(progress);
        }
        if (looperHighPassFreqText != null) {
            looperHighPassFreqText.setText(String.format("%.0fHz", highPassFreq));
        }
    }
    
    @Override
    public void updateMarkersInfo(int totalMarkers, int currentMarker, float markerTime) {
        if (looperMarkerInfoText != null) {
            if (totalMarkers == 0) {
                looperMarkerInfoText.setText("0 marcadores");
            } else if (currentMarker >= 0) {
                int minutes = (int) (markerTime / 60);
                int seconds = (int) (markerTime % 60);
                String info = String.format("Marcador %d/%d (%02d:%02d)", 
                                          currentMarker + 1, totalMarkers, minutes, seconds);
                looperMarkerInfoText.setText(info);
            } else {
                looperMarkerInfoText.setText(totalMarkers + " marcadores");
            }
        }
    }
    
    @Override
    public void updateMarkerButtons(boolean canAdd, boolean canRemove, boolean canNavigate) {
        if (looperAddMarkerButton != null) {
            looperAddMarkerButton.setEnabled(canAdd);
        }
        if (looperRemoveMarkerButton != null) {
            looperRemoveMarkerButton.setEnabled(canRemove);
        }
        if (looperPrevMarkerButton != null) {
            looperPrevMarkerButton.setEnabled(canNavigate);
        }
        if (looperNextMarkerButton != null) {
            looperNextMarkerButton.setEnabled(canNavigate);
        }
    }
    
    @Override
    public void updateFadeSettings(boolean fadeInEnabled, float fadeInDuration, boolean fadeOutEnabled, float fadeOutDuration) {
        // Fade in
        if (looperFadeInSwitch != null) {
            looperFadeInSwitch.setChecked(fadeInEnabled);
        }
        if (looperFadeInDurationSeekBar != null) {
            looperFadeInDurationSeekBar.setEnabled(fadeInEnabled);
            int progress = (int) ((fadeInDuration - 0.01f) / 0.99f * 100);
            looperFadeInDurationSeekBar.setProgress(progress);
        }
        if (looperFadeInDurationText != null) {
            looperFadeInDurationText.setText(String.format("%.2fs", fadeInDuration));
            int colorRes = fadeInEnabled ? R.color.white : R.color.gray;
            looperFadeInDurationText.setTextColor(getResources().getColor(colorRes));
        }
        
        // Fade out
        if (looperFadeOutSwitch != null) {
            looperFadeOutSwitch.setChecked(fadeOutEnabled);
        }
        if (looperFadeOutDurationSeekBar != null) {
            looperFadeOutDurationSeekBar.setEnabled(fadeOutEnabled);
            int progress = (int) ((fadeOutDuration - 0.01f) / 0.99f * 100);
            looperFadeOutDurationSeekBar.setProgress(progress);
        }
        if (looperFadeOutDurationText != null) {
            looperFadeOutDurationText.setText(String.format("%.2fs", fadeOutDuration));
            int colorRes = fadeOutEnabled ? R.color.white : R.color.gray;
            looperFadeOutDurationText.setTextColor(getResources().getColor(colorRes));
        }
    }
    
    @Override
    public void showSaveLoopDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Salvar Loop");
        
        EditText input = new EditText(getContext());
        input.setHint("Nome do loop");
        builder.setView(input);
        
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String fileName = input.getText().toString().trim();
            if (!fileName.isEmpty() && presenter != null) {
                presenter.saveLoop(fileName);
            }
        });
        
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
    
    @Override
    public void showLoadLoopDialog() {
        if (presenter != null) {
            openFilePicker(LooperPresenter.LOAD_LOOP_REQUEST);
        }
    }
    
    @Override
    public void showExportDialog() {
        ExportDialog dialog = ExportDialog.newInstance();
        dialog.show(getChildFragmentManager(), "export_dialog");
    }
    
    @Override
    public void showClearConfirmation() {
        new AlertDialog.Builder(getContext())
            .setTitle("Limpar Loop")
            .setMessage("Tem certeza que deseja limpar o loop atual? Esta ação não pode ser desfeita.")
            .setPositiveButton("Sim", (dialog, which) -> {
                // TODO: Implementar limpeza do loop
            })
            .setNegativeButton("Não", null)
            .show();
    }
    
    @Override
    public void showSuccessMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void openLoopLibrary() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadFragment(new LoopLibraryFragment());
            ((MainActivity) getActivity()).updateHeaderTitle("Biblioteca de Loops");
        }
    }
    
    @Override
    public void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, requestCode);
    }
    
    @Override
    public void openFileCreator(String fileName, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, requestCode);
    }
    
    // === Implementação da interface LooperTrackAdapter.OnTrackActionListener ===
    
    @Override
    public void onTrackVolumeChanged(int trackIndex, float volume) {
        if (presenter != null) {
            presenter.setTrackVolume(trackIndex, volume);
        }
    }
    
    @Override
    public void onTrackMuteToggled(int trackIndex, boolean muted) {
        if (presenter != null) {
            presenter.setTrackMuted(trackIndex, muted);
        }
    }
    
    @Override
    public void onTrackSoloToggled(int trackIndex, boolean soloed) {
        if (presenter != null) {
            presenter.setTrackSoloed(trackIndex, soloed);
        }
    }
    
    @Override
    public void onTrackDeleted(int trackIndex) {
        if (presenter != null) {
            presenter.deleteTrack(trackIndex);
        }
    }
    
    // === Implementação da interface ExportDialog.ExportDialogListener ===
    
    @Override
    public void onExportSelected(com.thiagofernendorech.toneforge.LoopExportManager.ExportFormat format, String fileName) {
        if (presenter != null) {
            presenter.exportLoop(fileName, format.name());
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (presenter != null) {
            String filePath = null;
            if (data != null && data.getData() != null) {
                filePath = data.getData().getPath();
            }
            presenter.handleFileResult(requestCode, resultCode, filePath);
        }
    }
} 