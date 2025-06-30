package com.thiagofernendorech.toneforge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Navegação para cada função
        view.findViewById(R.id.btnTuner).setOnClickListener(v -> {
            ((MainActivity) getActivity()).loadFragment(new TunerFragment());
            ((MainActivity) getActivity()).updateHeaderTitle("Afinador");
        });
        
        view.findViewById(R.id.btnEffects).setOnClickListener(v -> {
            ((MainActivity) getActivity()).loadFragment(new EffectsFragment());
            ((MainActivity) getActivity()).updateHeaderTitle("Efeitos");
        });
        
        view.findViewById(R.id.btnLooper).setOnClickListener(v -> {
            ((MainActivity) getActivity()).loadFragment(new LooperFragment());
            ((MainActivity) getActivity()).updateHeaderTitle("Looper");
        });
        
        view.findViewById(R.id.btnMetronome).setOnClickListener(v -> {
            ((MainActivity) getActivity()).loadFragment(new MetronomeFragment());
            ((MainActivity) getActivity()).updateHeaderTitle("Metrônomo");
        });
        
        view.findViewById(R.id.btnLearning).setOnClickListener(v -> {
            ((MainActivity) getActivity()).loadFragment(new LearningFragment());
            ((MainActivity) getActivity()).updateHeaderTitle("Aprendizado");
        });
        
        view.findViewById(R.id.btnRecorder).setOnClickListener(v -> {
            ((MainActivity) getActivity()).loadFragment(new RecorderFragment());
            ((MainActivity) getActivity()).updateHeaderTitle("Gravador");
        });
        
        view.findViewById(R.id.btnSettings).setOnClickListener(v -> {
            ((MainActivity) getActivity()).loadFragment(new SettingsFragment());
            ((MainActivity) getActivity()).updateHeaderTitle("Configurações");
        });
        
        view.findViewById(R.id.btnWifi).setOnClickListener(v -> {
            // Implementar funcionalidade Wi-Fi
        });
        
        view.findViewById(R.id.btnVolume).setOnClickListener(v -> {
            // Implementar controle de volume
        });

        return view;
    }
} 