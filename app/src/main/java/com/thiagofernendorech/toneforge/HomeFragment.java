package com.thiagofernendorech.toneforge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Navegação para cada função
        view.findViewById(R.id.btnTuner).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.tunerFragment));
        view.findViewById(R.id.btnEffects).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.effectsFragment));
        view.findViewById(R.id.btnLooper).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.looperFragment));
        view.findViewById(R.id.btnMetronome).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.metronomeFragment));
        view.findViewById(R.id.btnLearning).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.learningFragment));
        view.findViewById(R.id.btnRecorder).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.recorderFragment));
        view.findViewById(R.id.btnSettings).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.settingsFragment));

        return view;
    }
} 