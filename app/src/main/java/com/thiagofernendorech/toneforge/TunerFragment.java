package com.thiagofernendorech.toneforge;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

public class TunerFragment extends Fragment {
    private AudioRecord audioRecord;
    private Thread audioThread;
    private boolean isTuning = false;
    private Handler uiHandler;
    private TextView noteText;
    private TextView freqText;
    private TextView centsText;

    private static final int SAMPLE_RATE = 48000;
    private static final int BUFFER_SIZE = 2048;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tuner, container, false);
        
        noteText = view.findViewById(R.id.tunerNote);
        freqText = view.findViewById(R.id.tunerFreq);
        centsText = view.findViewById(R.id.tunerCents);
        Button toggleButton = view.findViewById(R.id.tunerToggleButton);
        
        uiHandler = new Handler(Looper.getMainLooper());

        // Configurar botões de notas
        setupNoteButtons(view);

        toggleButton.setOnClickListener(v -> {
            if (!isTuning) {
                startTuner(toggleButton);
            } else {
                stopTuner(toggleButton);
            }
        });
        
        return view;
    }
    
    private void setupNoteButtons(View view) {
        Button btnNoteE = view.findViewById(R.id.btnNoteE);
        Button btnNoteA = view.findViewById(R.id.btnNoteA);
        Button btnNoteD = view.findViewById(R.id.btnNoteD);
        Button btnNoteG = view.findViewById(R.id.btnNoteG);
        Button btnNoteB = view.findViewById(R.id.btnNoteB);
        Button btnNoteE2 = view.findViewById(R.id.btnNoteE2);
        
        btnNoteE.setOnClickListener(v -> noteText.setText("E"));
        btnNoteA.setOnClickListener(v -> noteText.setText("A"));
        btnNoteD.setOnClickListener(v -> noteText.setText("D"));
        btnNoteG.setOnClickListener(v -> noteText.setText("G"));
        btnNoteB.setOnClickListener(v -> noteText.setText("B"));
        btnNoteE2.setOnClickListener(v -> noteText.setText("E"));
    }

    private void startTuner(Button toggleButton) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }
        isTuning = true;
        toggleButton.setText("Parar");
        AudioEngine.startTuner();
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE * 2);
        audioRecord.startRecording();
        audioThread = new Thread(() -> {
            short[] buffer = new short[BUFFER_SIZE];
            float[] floatBuffer = new float[BUFFER_SIZE];
            while (isTuning && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                int read = audioRecord.read(buffer, 0, BUFFER_SIZE);
                for (int i = 0; i < read; i++) {
                    floatBuffer[i] = buffer[i] / 32768.0f;
                }
                AudioEngine.processTunerBuffer(floatBuffer, read);
                float freq = AudioEngine.getDetectedFrequency();
                uiHandler.post(() -> updateTunerUI(freq));
            }
        });
        audioThread.start();
    }

    private void stopTuner(Button toggleButton) {
        isTuning = false;
        toggleButton.setText("Iniciar");
        AudioEngine.stopTuner();
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        if (audioThread != null) {
            audioThread.interrupt();
            audioThread = null;
        }
    }

    private void updateTunerUI(float freq) {
        if (getView() == null || !isAdded()) return;
        
        if (freq < 40.0f || freq > 2000.0f) {
            noteText.setText("-");
            freqText.setText("");
            centsText.setText("");
            noteText.setTextColor(getResources().getColor(R.color.lava_text_primary));
            return;
        }
        
        String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        double A4 = 440.0;
        int noteNumber = (int) Math.round(12 * Math.log(freq / A4) / Math.log(2)) + 57;
        int noteIndex = (noteNumber + 1200) % 12;
        int octave = (noteNumber / 12) - 1;
        String note = noteNames[noteIndex] + octave;
        
        freqText.setText(String.format("Frequência: %.1f Hz", freq));
        
        // Calcular desvio em cents
        double noteFreq = A4 * Math.pow(2, (noteNumber - 57) / 12.0);
        int cents = (int) (1200 * Math.log(freq / noteFreq) / Math.log(2));
        
        centsText.setText(String.format("%s%d cents", (cents > 0 ? "+" : ""), cents));
        noteText.setText(noteNames[noteIndex]);
        
        // Feedback visual por cor
        int absCents = Math.abs(cents);
        if (absCents <= 5) {
            noteText.setTextColor(getResources().getColor(R.color.lava_green));
            centsText.setTextColor(getResources().getColor(R.color.lava_green));
        } else if (absCents <= 15) {
            noteText.setTextColor(getResources().getColor(R.color.lava_yellow));
            centsText.setTextColor(getResources().getColor(R.color.lava_text_secondary));
        } else {
            noteText.setTextColor(getResources().getColor(R.color.lava_red));
            centsText.setTextColor(getResources().getColor(R.color.lava_text_secondary));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isTuning = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        if (audioThread != null) {
            audioThread.interrupt();
            audioThread = null;
        }
        AudioEngine.stopTuner();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateHeaderTitle("Afinador");
        }
    }
} 