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

public class RecorderFragment extends Fragment {
    private boolean isRecording = false;
    private boolean isPlaying = false;
    // TODO: Adicionar lógica de timer, gravação e reprodução de áudio

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recorder, container, false);

        Button recordButton = view.findViewById(R.id.recorderRecordButton);
        Button playButton = view.findViewById(R.id.recorderPlayButton);
        TextView timerText = view.findViewById(R.id.recorderTimer);
        TextView listPlaceholder = view.findViewById(R.id.recorderListPlaceholder);

        recordButton.setOnClickListener(v -> {
            isRecording = !isRecording;
            if (isRecording) {
                AudioEngine.startRecording();
                recordButton.setBackgroundTintList(getResources().getColorStateList(R.color.green));
                timerText.setText("00:00");
                // Atualizar UI, timer, etc
            } else {
                AudioEngine.stopRecording();
                recordButton.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                // Atualizar UI, timer, etc
                // TODO: Atualizar lista de gravações
            }
        });

        playButton.setOnClickListener(v -> {
            isPlaying = !isPlaying;
            if (isPlaying) {
                AudioEngine.playLastRecording();
                playButton.setBackgroundTintList(getResources().getColorStateList(R.color.accent_blue));
                // Atualizar UI, timer, etc
            } else {
                AudioEngine.stopPlayback();
                playButton.setBackgroundTintList(getResources().getColorStateList(R.color.green));
                // Atualizar UI, timer, etc
            }
        });

        // TODO: Atualizar listPlaceholder com as gravações recentes

        return view;
    }
} 