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
import android.widget.ProgressBar;
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

    private static final int SAMPLE_RATE = 48000;
    private static final int BUFFER_SIZE = 2048;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tuner, container, false);
        TextView noteText = view.findViewById(R.id.tunerNote);
        ProgressBar tunerBar = view.findViewById(R.id.tunerBar);
        Button toggleButton = view.findViewById(R.id.tunerToggleButton);
        TextView freqText = view.findViewById(R.id.tunerFreq);
        TextView centsText = view.findViewById(R.id.tunerCents);
        uiHandler = new Handler(Looper.getMainLooper());

        toggleButton.setOnClickListener(v -> {
            if (!isTuning) {
                startTuner(noteText, tunerBar, toggleButton);
            } else {
                stopTuner(toggleButton);
            }
        });
        return view;
    }

    private void startTuner(TextView noteText, ProgressBar tunerBar, Button toggleButton) {
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
                uiHandler.post(() -> updateTunerUI(freq, noteText, tunerBar));
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

    private void updateTunerUI(float freq, TextView noteText, ProgressBar tunerBar) {
        if (getView() == null || !isAdded()) return;
        TextView freqText = getView().findViewById(R.id.tunerFreq);
        TextView centsText = getView().findViewById(R.id.tunerCents);
        if (freqText == null || centsText == null || noteText == null || tunerBar == null) return;
        if (freq < 40.0f || freq > 2000.0f) {
            noteText.setText("-");
            tunerBar.setProgress(50);
            freqText.setText("");
            centsText.setText("");
            noteText.setTextColor(getResources().getColor(R.color.white));
            tunerBar.setProgressTintList(getResources().getColorStateList(R.color.light_gray));
            return;
        }
        String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        double A4 = 440.0;
        int noteNumber = (int) Math.round(12 * Math.log(freq / A4) / Math.log(2)) + 57;
        int noteIndex = (noteNumber + 1200) % 12;
        String note = noteNames[noteIndex];
        noteText.setText(note);
        freqText.setText(String.format("%.1f Hz", freq));
        // Calcular desvio em cents
        double noteFreq = A4 * Math.pow(2, (noteNumber - 57) / 12.0);
        int cents = (int) (1200 * Math.log(freq / noteFreq) / Math.log(2));
        centsText.setText((cents > 0 ? "+" : "") + cents + " cents");
        int progress = 50 + cents / 2; // -50 a +50 cents -> 0 a 100
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        tunerBar.setProgress(progress);
        // Feedback visual por cor
        int absCents = Math.abs(cents);
        if (absCents <= 5) {
            noteText.setTextColor(getResources().getColor(R.color.green));
            tunerBar.setProgressTintList(getResources().getColorStateList(R.color.green));
        } else if (absCents <= 15) {
            noteText.setTextColor(getResources().getColor(R.color.yellow));
            tunerBar.setProgressTintList(getResources().getColorStateList(R.color.yellow));
        } else {
            noteText.setTextColor(getResources().getColor(R.color.red));
            tunerBar.setProgressTintList(getResources().getColorStateList(R.color.red));
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
} 