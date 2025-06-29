package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.SharedPreferences;
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
    private static final String PREFS_NAME = "toneforge_prefs";
    private static final String KEY_AUDIO_BACKGROUND = "audio_background_enabled";
    private Switch switchAudioBackground;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Switch switchDarkTheme = view.findViewById(R.id.switchDarkTheme);
        Switch switchVibration = view.findViewById(R.id.switchVibration);
        Switch switchAutoSave = view.findViewById(R.id.switchAutoSave);
        switchAudioBackground = view.findViewById(R.id.switchAudioBackground);
        Button aboutButton = view.findViewById(R.id.settingsAboutButton);

        // Carregar preferência salva
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean audioBackgroundEnabled = prefs.getBoolean(KEY_AUDIO_BACKGROUND, false);
        // Atualizar switch conforme preferência OU estado real do serviço
        boolean isServiceRunning = AudioBackgroundService.isServiceRunning(requireContext());
        switchAudioBackground.setChecked(audioBackgroundEnabled || isServiceRunning);

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

        switchAudioBackground.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_AUDIO_BACKGROUND, isChecked);
            editor.apply();
            if (isChecked) {
                // Iniciar serviço de áudio em background
                AudioBackgroundService.startService(requireContext());
                Toast.makeText(getContext(), getString(R.string.audio_background_enabled), Toast.LENGTH_SHORT).show();
            } else {
                // Parar serviço de áudio em background
                AudioBackgroundService.stopService(requireContext());
                Toast.makeText(getContext(), getString(R.string.audio_background_disabled), Toast.LENGTH_SHORT).show();
            }
        });

        aboutButton.setOnClickListener(v -> {
            // Exibir versão e informações do app
            Toast.makeText(getContext(), "ToneForge v1.0\nDesenvolvido por Thiago F. Rech", Toast.LENGTH_LONG).show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Atualizar switch conforme estado real do serviço ao voltar para a tela
        if (switchAudioBackground != null) {
            boolean isServiceRunning = AudioBackgroundService.isServiceRunning(requireContext());
            switchAudioBackground.setChecked(isServiceRunning);
        }
    }
} 