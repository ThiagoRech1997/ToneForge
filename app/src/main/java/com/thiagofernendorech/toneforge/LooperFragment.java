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

public class LooperFragment extends Fragment {
    private boolean isRecording = false;
    private boolean isPlaying = false;
    // TODO: Adicionar lógica de timer e integração com áudio/looper

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_looper, container, false);

        Button recordButton = view.findViewById(R.id.looperRecordButton);
        Button playButton = view.findViewById(R.id.looperPlayButton);
        Button clearButton = view.findViewById(R.id.looperClearButton);
        TextView timerText = view.findViewById(R.id.looperTimer);

        recordButton.setOnClickListener(v -> {
            isRecording = !isRecording;
            if (isRecording) {
                AudioEngine.startLooperRecording();
                recordButton.setBackgroundTintList(getResources().getColorStateList(R.color.green));
                // Atualizar UI, timer, etc
            } else {
                AudioEngine.stopLooperRecording();
                recordButton.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                // Atualizar UI, timer, etc
            }
        });

        playButton.setOnClickListener(v -> {
            isPlaying = !isPlaying;
            if (isPlaying) {
                AudioEngine.startLooperPlayback();
                playButton.setBackgroundTintList(getResources().getColorStateList(R.color.accent_blue));
                // Atualizar UI, timer, etc
            } else {
                AudioEngine.stopLooperPlayback();
                playButton.setBackgroundTintList(getResources().getColorStateList(R.color.green));
                // Atualizar UI, timer, etc
            }
        });

        clearButton.setOnClickListener(v -> {
            AudioEngine.clearLooper();
            isRecording = false;
            isPlaying = false;
            recordButton.setBackgroundTintList(getResources().getColorStateList(R.color.red));
            playButton.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            timerText.setText("00:00");
            // Atualizar UI, timer, etc
        });

        return view;
    }
} 