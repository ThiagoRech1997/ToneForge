package com.thiagofernendorech.toneforge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.thiagofernendorech.toneforge.ui.fragments.effects.EffectsFragmentRefactored;

public class HomeFragment extends Fragment {
    
    private boolean useLavaDesign = true; // Flag para controlar qual layout usar
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Escolher layout baseado na flag
        int layoutId = useLavaDesign ? R.layout.fragment_home_lava : R.layout.fragment_home;
        View view = inflater.inflate(layoutId, container, false);

        // Navegação para cada função
        view.findViewById(R.id.btnTuner).setOnClickListener(v -> {
            ((MainActivity) getActivity()).loadFragment(new TunerFragment());
            ((MainActivity) getActivity()).updateHeaderTitle("Afinador");
        });
        
        view.findViewById(R.id.btnEffects).setOnClickListener(v -> {
            ((MainActivity) getActivity()).loadFragment(new EffectsFragmentRefactored());
            ((MainActivity) getActivity()).updateHeaderTitle("Efeitos");
        });
        
        view.findViewById(R.id.btnLooper).setOnClickListener(v -> {
            ((MainActivity) getActivity()).loadFragment(new com.thiagofernendorech.toneforge.ui.fragments.looper.LooperFragmentRefactored());
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
            ((MainActivity) getActivity()).loadFragment(new com.thiagofernendorech.toneforge.ui.fragments.settings.SettingsFragmentRefactored());
            ((MainActivity) getActivity()).updateHeaderTitle("Configurações");
        });
        
        // Botões específicos do layout Lava
        if (useLavaDesign) {
            view.findViewById(R.id.btnWifi).setOnClickListener(v -> {
                // Implementar funcionalidade Wi-Fi
                ((MainActivity) getActivity()).showWifiStatusDialog();
            });
            
            view.findViewById(R.id.btnVolume).setOnClickListener(v -> {
                // Implementar controle de volume
                ((MainActivity) getActivity()).showVolumeControlDialog();
            });
        }

        return view;
    }
    
    // Método para alternar entre os designs
    public void setUseLavaDesign(boolean useLavaDesign) {
        this.useLavaDesign = useLavaDesign;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateHeaderTitle("ToneForge");
        }
    }
} 