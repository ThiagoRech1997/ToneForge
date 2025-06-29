package com.thiagofernendorech.toneforge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MetronomeFragment extends Fragment {
    private boolean isPlaying = false;
    private int bpm = 120;
    // TODO: Adicionar lógica de animação e integração com áudio/metronome

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_metronome, container, false);

        TextView bpmValue = view.findViewById(R.id.metronomeBpmValue);
        SeekBar seekBar = view.findViewById(R.id.metronomeSeekBar);
        Button playButton = view.findViewById(R.id.metronomePlayButton);
        View beatIndicator = view.findViewById(R.id.metronomeBeatIndicator);

        bpmValue.setText(String.valueOf(bpm));
        seekBar.setProgress(bpm);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bpm = Math.max(30, progress); // Limite mínimo de 30 BPM
                bpmValue.setText(String.valueOf(bpm));
                // TODO: Atualizar BPM do metrônomo via JNI
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        playButton.setOnClickListener(v -> {
            isPlaying = !isPlaying;
            if (isPlaying) {
                // TODO: Iniciar metrônomo via JNI
                playButton.setBackgroundTintList(getResources().getColorStateList(R.color.accent_blue));
                // TODO: Iniciar animação do indicador de batida
            } else {
                // TODO: Parar metrônomo via JNI
                playButton.setBackgroundTintList(getResources().getColorStateList(R.color.green));
                // TODO: Parar animação do indicador de batida
            }
        });

        // TODO: Animação do indicador de batida (beatIndicator)

        return view;
    }
} 