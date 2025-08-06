package com.thiagofernendorech.toneforge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.SeekBar;
import android.animation.ValueAnimator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.os.Handler;
import android.os.Looper;

public class MetronomeFragment extends Fragment {
    private boolean isPlaying = false;
    private int bpm = 120;
    private int timeSignature = 4; // Compasso 4/4 por padrão
    private float metronomeVolume = 0.6f; // Volume padrão 60%
    
    private TextView bpmValue;
    private Button playButton;
    private View beatIndicator;
    private Handler beatHandler;
    private Runnable beatRunnable;
    private AnimatorSet beatAnimator;
    
    // Novos controles
    private SeekBar volumeSeekBar;
    private TextView volumeValue;
    private TextView timeSignatureValue;
    private Button btnDecreaseTimeSignature;
    private Button btnIncreaseTimeSignature;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_metronome, container, false);

        bpmValue = view.findViewById(R.id.metronomeBpmValue);
        playButton = view.findViewById(R.id.metronomePlayButton);
        beatIndicator = view.findViewById(R.id.metronomeBeatIndicator);

        // Inicializar novos controles
        volumeSeekBar = view.findViewById(R.id.metronomeVolumeSeekBar);
        volumeValue = view.findViewById(R.id.metronomeVolumeValue);
        timeSignatureValue = view.findViewById(R.id.metronomeTimeSignatureValue);
        btnDecreaseTimeSignature = view.findViewById(R.id.btnDecreaseTimeSignature);
        btnIncreaseTimeSignature = view.findViewById(R.id.btnIncreaseTimeSignature);

        // Inicializar handler para animação de batida
        beatHandler = new Handler(Looper.getMainLooper());
        beatRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying) {
                    animateBeat();
                    // Agendar próxima batida
                    long beatInterval = (long) (60000.0 / bpm); // milissegundos
                    beatHandler.postDelayed(this, beatInterval);
                }
            }
        };

        // Configurar botões de incremento/decremento
        Button btnDecreaseBpm = view.findViewById(R.id.btnDecreaseBpm);
        Button btnIncreaseBpm = view.findViewById(R.id.btnIncreaseBpm);
        
        btnDecreaseBpm.setOnClickListener(v -> {
            bpm = Math.max(40, bpm - 5);
            updateBpmDisplay();
            if (isPlaying) {
                restartBeatAnimation();
            }
        });
        
        btnIncreaseBpm.setOnClickListener(v -> {
            bpm = Math.min(200, bpm + 5);
            updateBpmDisplay();
            if (isPlaying) {
                restartBeatAnimation();
            }
        });

        // Configurar botões de preset
        Button btnPreset60 = view.findViewById(R.id.btnPreset60);
        Button btnPreset80 = view.findViewById(R.id.btnPreset80);
        Button btnPreset100 = view.findViewById(R.id.btnPreset100);
        Button btnPreset120 = view.findViewById(R.id.btnPreset120);
        
        btnPreset60.setOnClickListener(v -> {
            bpm = 60;
            updateBpmDisplay();
            if (isPlaying) restartBeatAnimation();
        });
        
        btnPreset80.setOnClickListener(v -> {
            bpm = 80;
            updateBpmDisplay();
            if (isPlaying) restartBeatAnimation();
        });
        
        btnPreset100.setOnClickListener(v -> {
            bpm = 100;
            updateBpmDisplay();
            if (isPlaying) restartBeatAnimation();
        });
        
        btnPreset120.setOnClickListener(v -> {
            bpm = 120;
            updateBpmDisplay();
            if (isPlaying) restartBeatAnimation();
        });

        updateBpmDisplay();
        updateTimeSignatureDisplay();
        setupVolumeControl();

        playButton.setOnClickListener(v -> {
            isPlaying = !isPlaying;
            if (isPlaying) {
                AudioEngine.startMetronome(bpm);
                playButton.setBackground(getResources().getDrawable(R.drawable.round_button_background));
                playButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_stop), null, null);
                startBeatAnimation();
            } else {
                AudioEngine.stopMetronome();
                playButton.setBackground(getResources().getDrawable(R.drawable.round_button_background));
                playButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_play), null, null);
                stopBeatAnimation();
            }
        });

        return view;
    }
    
    private void setupVolumeControl() {
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    metronomeVolume = progress / 100.0f;
                    volumeValue.setText(progress + "%");
                    AudioEngine.setMetronomeVolume(metronomeVolume);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Configurar controles de compasso
        btnDecreaseTimeSignature.setOnClickListener(v -> {
            timeSignature = Math.max(1, timeSignature - 1);
            updateTimeSignatureDisplay();
            AudioEngine.setMetronomeTimeSignature(timeSignature);
        });
        
        btnIncreaseTimeSignature.setOnClickListener(v -> {
            timeSignature = Math.min(16, timeSignature + 1);
            updateTimeSignatureDisplay();
            AudioEngine.setMetronomeTimeSignature(timeSignature);
        });
    }
    
    private void updateTimeSignatureDisplay() {
        timeSignatureValue.setText(timeSignature + "/4");
    }
    
    private void updateBpmDisplay() {
        bpmValue.setText(String.valueOf(bpm));
        if (isPlaying) {
            AudioEngine.startMetronome(bpm); // Atualiza BPM em tempo real
        }
    }

    private void startBeatAnimation() {
        // Iniciar primeira batida imediatamente
        animateBeat();
        // Agendar próximas batidas
        long beatInterval = (long) (60000.0 / bpm); // milissegundos
        beatHandler.postDelayed(beatRunnable, beatInterval);
    }

    private void stopBeatAnimation() {
        beatHandler.removeCallbacks(beatRunnable);
        if (beatAnimator != null) {
            beatAnimator.cancel();
        }
        // Resetar indicador
        beatIndicator.setScaleX(1.0f);
        beatIndicator.setScaleY(1.0f);
        beatIndicator.setAlpha(1.0f);
    }

    private void restartBeatAnimation() {
        stopBeatAnimation();
        startBeatAnimation();
    }

    private void animateBeat() {
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
    public void onDestroyView() {
        super.onDestroyView();
        stopBeatAnimation();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateHeaderTitle("Metrônomo");
        }
    }
} 