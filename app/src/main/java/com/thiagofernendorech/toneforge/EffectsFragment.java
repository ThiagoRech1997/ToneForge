package com.thiagofernendorech.toneforge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.SeekBar;
import android.widget.Switch;

public class EffectsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_effects, container, false);

        // Ganho
        Switch switchGain = view.findViewById(R.id.switchGain);
        SeekBar seekGain = view.findViewById(R.id.seekGain);
        switchGain.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Ativar/desativar efeito de ganho via JNI
        });
        seekGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO: Atualizar parâmetro de ganho via JNI
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Distorção
        Switch switchDistortion = view.findViewById(R.id.switchDistortion);
        SeekBar seekDistortion = view.findViewById(R.id.seekDistortion);
        switchDistortion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Ativar/desativar distorção via JNI
        });
        seekDistortion.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO: Atualizar parâmetro de distorção via JNI
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Delay
        Switch switchDelay = view.findViewById(R.id.switchDelay);
        SeekBar seekDelay = view.findViewById(R.id.seekDelay);
        switchDelay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Ativar/desativar delay via JNI
        });
        seekDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO: Atualizar parâmetro de delay via JNI
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Reverb
        Switch switchReverb = view.findViewById(R.id.switchReverb);
        SeekBar seekReverb = view.findViewById(R.id.seekReverb);
        switchReverb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Ativar/desativar reverb via JNI
        });
        seekReverb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO: Atualizar parâmetro de reverb via JNI
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return view;
    }
} 