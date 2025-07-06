package com.thiagofernendorech.toneforge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EffectsLavaFragment extends Fragment {
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_effects_lava, container, false);

        // Configurar navegação para categorias de efeitos
        view.findViewById(R.id.btnDistortion).setOnClickListener(v -> {
            navigateToEffectCategory("Distortion", "distortion");
        });
        
        view.findViewById(R.id.btnModulation).setOnClickListener(v -> {
            navigateToEffectCategory("Modulation", "modulation");
        });
        
        view.findViewById(R.id.btnTime).setOnClickListener(v -> {
            navigateToEffectCategory("Time Effects", "time");
        });
        
        view.findViewById(R.id.btnFilter).setOnClickListener(v -> {
            navigateToEffectCategory("Filter", "filter");
        });
        
        view.findViewById(R.id.btnDynamics).setOnClickListener(v -> {
            navigateToEffectCategory("Dynamics", "dynamics");
        });
        
        view.findViewById(R.id.btnAmbient).setOnClickListener(v -> {
            navigateToEffectCategory("Ambient", "ambient");
        });
        
        // Botão para editar chain
        view.findViewById(R.id.btnEditChain).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Abrindo editor de chain...", Toast.LENGTH_SHORT).show();
            // TODO: Implementar navegação para editor de chain
        });

        return view;
    }
    
    private void navigateToEffectCategory(String categoryName, String categoryType) {
        Toast.makeText(getContext(), "Navegando para " + categoryName, Toast.LENGTH_SHORT).show();
        
        // Criar bundle com informações da categoria
        Bundle args = new Bundle();
        args.putString("categoryName", categoryName);
        args.putString("categoryType", categoryType);
        
        // Navegar para fragment de categoria específica ou para EffectsFragment normal
        EffectsFragment effectsFragment = new EffectsFragment();
        effectsFragment.setArguments(args);
        
        ((MainActivity) getActivity()).loadFragment(effectsFragment);
        ((MainActivity) getActivity()).updateHeaderTitle(categoryName);
    }
} 