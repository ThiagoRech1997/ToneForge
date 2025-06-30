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

public class MetronomeFragment extends Fragment {
    private boolean isPlaying = false;
    private int bpm = 120;
    private TextView bpmValue;
    private Button playButton;
    private View beatIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_metronome, container, false);

        bpmValue = view.findViewById(R.id.metronomeBpmValue);
        playButton = view.findViewById(R.id.metronomePlayButton);
        beatIndicator = view.findViewById(R.id.metronomeBeatIndicator);

        // Configurar botões de incremento/decremento
        Button btnDecreaseBpm = view.findViewById(R.id.btnDecreaseBpm);
        Button btnIncreaseBpm = view.findViewById(R.id.btnIncreaseBpm);
        
        btnDecreaseBpm.setOnClickListener(v -> {
            bpm = Math.max(40, bpm - 5);
            updateBpmDisplay();
        });
        
        btnIncreaseBpm.setOnClickListener(v -> {
            bpm = Math.min(200, bpm + 5);
            updateBpmDisplay();
        });

        // Configurar botões de preset
        Button btnPreset60 = view.findViewById(R.id.btnPreset60);
        Button btnPreset80 = view.findViewById(R.id.btnPreset80);
        Button btnPreset100 = view.findViewById(R.id.btnPreset100);
        Button btnPreset120 = view.findViewById(R.id.btnPreset120);
        
        btnPreset60.setOnClickListener(v -> {
            bpm = 60;
            updateBpmDisplay();
        });
        
        btnPreset80.setOnClickListener(v -> {
            bpm = 80;
            updateBpmDisplay();
        });
        
        btnPreset100.setOnClickListener(v -> {
            bpm = 100;
            updateBpmDisplay();
        });
        
        btnPreset120.setOnClickListener(v -> {
            bpm = 120;
            updateBpmDisplay();
        });

        updateBpmDisplay();

        playButton.setOnClickListener(v -> {
            isPlaying = !isPlaying;
            if (isPlaying) {
                AudioEngine.startMetronome(bpm);
                playButton.setBackground(getResources().getDrawable(R.drawable.round_button_background));
                // TODO: Iniciar animação do indicador de batida
            } else {
                AudioEngine.stopMetronome();
                playButton.setBackground(getResources().getDrawable(R.drawable.round_button_background));
                // TODO: Parar animação do indicador de batida
            }
        });

        // TODO: Animação do indicador de batida (beatIndicator)

        return view;
    }
    
    private void updateBpmDisplay() {
        bpmValue.setText(String.valueOf(bpm));
        if (isPlaying) {
            AudioEngine.startMetronome(bpm); // Atualiza BPM em tempo real
        }
    }
} 