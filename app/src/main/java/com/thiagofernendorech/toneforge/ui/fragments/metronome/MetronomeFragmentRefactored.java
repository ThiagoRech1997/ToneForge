package com.thiagofernendorech.toneforge.ui.fragments.metronome;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.thiagofernendorech.toneforge.MainActivity;
import com.thiagofernendorech.toneforge.R;
import com.thiagofernendorech.toneforge.ui.base.BaseFragment;

/**
 * Fragment refatorado do Metrônomo com arquitetura MVP
 * Interface limpa e modular, separada da lógica de negócio
 */
public class MetronomeFragmentRefactored extends BaseFragment<MetronomePresenter> 
        implements MetronomeContract.View {

    // Views principais
    private TextView bpmValue;
    private Button playButton;
    private View beatIndicator;
    
    // Controles de volume
    private SeekBar volumeSeekBar;
    private TextView volumeValue;
    
    // Controles de compasso
    private TextView timeSignatureValue;
    private Button btnDecreaseTimeSignature;
    private Button btnIncreaseTimeSignature;
    
    // Controles de BPM
    private Button btnDecreaseBpm;
    private Button btnIncreaseBpm;
    
    // Botões de preset
    private Button btnPreset60;
    private Button btnPreset80;
    private Button btnPreset100;
    private Button btnPreset120;
    
    // Animação
    private AnimatorSet beatAnimator;

    @Override
    protected MetronomePresenter createPresenter() {
        return new MetronomePresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_metronome, container, false);
        
        initializeViews(view);
        setupControlListeners();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.initialize();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHeaderTitle("Metrônomo");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.cleanup();
        }
    }

    @Override
    public void updateBpmDisplay(int bpm) {
        if (bpmValue != null) {
            bpmValue.setText(String.valueOf(bpm));
        }
    }

    @Override
    public void updateTimeSignatureDisplay(int timeSignature) {
        if (timeSignatureValue != null) {
            timeSignatureValue.setText(timeSignature + "/4");
        }
    }

    @Override
    public void updateVolumeDisplay(int volumePercent) {
        if (volumeValue != null) {
            volumeValue.setText(volumePercent + "%");
        }
    }

    @Override
    public void updatePlayButtonState(boolean isPlaying) {
        if (playButton != null) {
            if (isPlaying) {
                playButton.setCompoundDrawablesWithIntrinsicBounds(null, 
                    getResources().getDrawable(R.drawable.ic_stop), null, null);
            } else {
                playButton.setCompoundDrawablesWithIntrinsicBounds(null, 
                    getResources().getDrawable(R.drawable.ic_play), null, null);
            }
        }
    }

    @Override
    public void startBeatAnimation() {
        animateBeat();
    }

    @Override
    public void stopBeatAnimation() {
        if (beatAnimator != null) {
            beatAnimator.cancel();
        }
        // Resetar indicador
        if (beatIndicator != null) {
            beatIndicator.setScaleX(1.0f);
            beatIndicator.setScaleY(1.0f);
            beatIndicator.setAlpha(1.0f);
        }
    }

    @Override
    public void restartBeatAnimation() {
        stopBeatAnimation();
        startBeatAnimation();
    }

    @Override
    public void animateBeat() {
        if (beatIndicator == null) return;
        
        // Cancelar animação anterior se estiver rodando
        if (beatAnimator != null) {
            beatAnimator.cancel();
        }

        // Criar animação de pulso
        AnimatorSet animatorSet = new AnimatorSet();
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(beatIndicator, "scaleX", 1.0f, 1.5f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(beatIndicator, "scaleY", 1.0f, 1.5f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(beatIndicator, "alpha", 1.0f, 0.8f, 1.0f);
        
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        
        beatAnimator = animatorSet;
        animatorSet.start();
    }

    @Override
    public void updateHeaderTitle(String title) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateHeaderTitle(title);
        }
    }

    @Override
    public void setupControlListeners() {
        setupBpmControlButtons();
        setupPresetButtons();
        setupVolumeSeekBar();
        setupTimeSignatureButtons();
        setupPlayButton();
    }

    @Override
    public void setupVolumeSeekBar() {
        if (volumeSeekBar != null) {
            volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && presenter != null) {
                        presenter.setVolume(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }

    @Override
    public void setupPresetButtons() {
        if (btnPreset60 != null) {
            btnPreset60.setOnClickListener(v -> {
                if (presenter != null) presenter.setBpm(60);
            });
        }
        
        if (btnPreset80 != null) {
            btnPreset80.setOnClickListener(v -> {
                if (presenter != null) presenter.setBpm(80);
            });
        }
        
        if (btnPreset100 != null) {
            btnPreset100.setOnClickListener(v -> {
                if (presenter != null) presenter.setBpm(100);
            });
        }
        
        if (btnPreset120 != null) {
            btnPreset120.setOnClickListener(v -> {
                if (presenter != null) presenter.setBpm(120);
            });
        }
    }

    @Override
    public void setupBpmControlButtons() {
        if (btnDecreaseBpm != null) {
            btnDecreaseBpm.setOnClickListener(v -> {
                if (presenter != null) presenter.decreaseBpm();
            });
        }
        
        if (btnIncreaseBpm != null) {
            btnIncreaseBpm.setOnClickListener(v -> {
                if (presenter != null) presenter.increaseBpm();
            });
        }
    }

    @Override
    public void setupTimeSignatureButtons() {
        if (btnDecreaseTimeSignature != null) {
            btnDecreaseTimeSignature.setOnClickListener(v -> {
                if (presenter != null) presenter.decreaseTimeSignature();
            });
        }
        
        if (btnIncreaseTimeSignature != null) {
            btnIncreaseTimeSignature.setOnClickListener(v -> {
                if (presenter != null) presenter.increaseTimeSignature();
            });
        }
    }

    /**
     * Inicializa todas as views do fragment
     */
    private void initializeViews(View view) {
        // Views principais
        bpmValue = view.findViewById(R.id.metronomeBpmValue);
        playButton = view.findViewById(R.id.metronomePlayButton);
        beatIndicator = view.findViewById(R.id.metronomeBeatIndicator);
        
        // Controles de volume
        volumeSeekBar = view.findViewById(R.id.metronomeVolumeSeekBar);
        volumeValue = view.findViewById(R.id.metronomeVolumeValue);
        
        // Controles de compasso
        timeSignatureValue = view.findViewById(R.id.metronomeTimeSignatureValue);
        btnDecreaseTimeSignature = view.findViewById(R.id.btnDecreaseTimeSignature);
        btnIncreaseTimeSignature = view.findViewById(R.id.btnIncreaseTimeSignature);
        
        // Controles de BPM
        btnDecreaseBpm = view.findViewById(R.id.btnDecreaseBpm);
        btnIncreaseBpm = view.findViewById(R.id.btnIncreaseBpm);
        
        // Botões de preset
        btnPreset60 = view.findViewById(R.id.btnPreset60);
        btnPreset80 = view.findViewById(R.id.btnPreset80);
        btnPreset100 = view.findViewById(R.id.btnPreset100);
        btnPreset120 = view.findViewById(R.id.btnPreset120);
    }

    /**
     * Configura o botão de play/stop
     */
    private void setupPlayButton() {
        if (playButton != null) {
            playButton.setOnClickListener(v -> {
                if (presenter != null) {
                    presenter.togglePlayStop();
                }
            });
        }
    }
} 