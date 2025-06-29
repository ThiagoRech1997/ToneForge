package com.thiagofernendorech.toneforge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Switch;
import android.widget.Button;
import android.widget.Toast;

public class SettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Switch switchDarkTheme = view.findViewById(R.id.switchDarkTheme);
        Switch switchVibration = view.findViewById(R.id.switchVibration);
        Switch switchAutoSave = view.findViewById(R.id.switchAutoSave);
        Button aboutButton = view.findViewById(R.id.settingsAboutButton);

        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Ativar/desativar tema escuro
            Toast.makeText(getContext(), isChecked ? "Tema escuro ativado" : "Tema claro ativado", Toast.LENGTH_SHORT).show();
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Ativar/desativar vibração
            Toast.makeText(getContext(), isChecked ? "Vibração ativada" : "Vibração desativada", Toast.LENGTH_SHORT).show();
        });

        switchAutoSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Ativar/desativar salvamento automático
            Toast.makeText(getContext(), isChecked ? "Salvar gravações automaticamente" : "Salvar manualmente", Toast.LENGTH_SHORT).show();
        });

        aboutButton.setOnClickListener(v -> {
            // Exibir versão e informações do app
            Toast.makeText(getContext(), "ToneForge v1.0\nDesenvolvido por Thiago F. Rech", Toast.LENGTH_LONG).show();
        });

        return view;
    }
} 