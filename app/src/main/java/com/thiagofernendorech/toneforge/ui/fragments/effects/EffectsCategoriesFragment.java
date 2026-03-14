package com.thiagofernendorech.toneforge.ui.fragments.effects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.thiagofernendorech.toneforge.R;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.ui.navigation.NavigationController;

/**
 * Fragment que mostra o grid de categorias de efeitos.
 * Ponto de entrada para a aba Efeitos na bottom navigation.
 * Navega para EffectCategoryFragment ao selecionar uma categoria.
 */
public class EffectsCategoriesFragment extends Fragment {

    private NavigationController navigationController;
    private LinearLayout effectChainChips;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_effects_categories, container, false);

        navigationController = NavigationController.getInstance();
        effectChainChips = view.findViewById(R.id.effectChainChips);

        setupCategoryClickListeners(view);
        populateEffectChain();

        return view;
    }

    private void setupCategoryClickListeners(View view) {
        view.findViewById(R.id.catDistortion).setOnClickListener(v ->
                navigationController.navigateToEffectCategory("distortion"));
        view.findViewById(R.id.catModulation).setOnClickListener(v ->
                navigationController.navigateToEffectCategory("modulation"));
        view.findViewById(R.id.catTime).setOnClickListener(v ->
                navigationController.navigateToEffectCategory("time"));
        view.findViewById(R.id.catFilter).setOnClickListener(v ->
                navigationController.navigateToEffectCategory("filter"));
        view.findViewById(R.id.catDynamics).setOnClickListener(v ->
                navigationController.navigateToEffectCategory("dynamics"));
        view.findViewById(R.id.catAmbient).setOnClickListener(v ->
                navigationController.navigateToEffectCategory("ambient"));

        View btnEditChain = view.findViewById(R.id.btnEditChain);
        if (btnEditChain != null) {
            btnEditChain.setOnClickListener(v -> {
                // Navigate to the full effects view for chain editing
                navigationController.navigateToEffectsFull();
            });
        }
    }

    /**
     * Populates the effect chain strip with chips for active effects.
     */
    private void populateEffectChain() {
        if (effectChainChips == null || getContext() == null) return;
        effectChainChips.removeAllViews();

        try {
            AudioRepository repo = AudioRepository.getInstance(getContext());

            String[][] chainEffects = {
                    {"Ganho", "#FF6B7280"},
                    {"Distortion", "#FFDC2626"},
                    {"Chorus", "#FF2563EB"},
                    {"Flanger", "#FF2563EB"},
                    {"Phaser", "#FF2563EB"},
                    {"EQ", "#FFCA8A04"},
                    {"Compressor", "#FF7C3AED"},
                    {"Delay", "#FF059669"},
                    {"Reverb", "#FF059669"}
            };

            for (String[] effect : chainEffects) {
                // Show all effects as chips (active ones highlighted)
                addChip(effect[0], effect[1]);
            }
        } catch (Exception e) {
            // Fallback: show default chain
            addChip("Overdrive", "#FFDC2626");
            addChip("Chorus", "#FF2563EB");
            addChip("Delay", "#FF059669");
        }
    }

    private void addChip(String name, String colorHex) {
        if (getContext() == null) return;

        TextView chip = new TextView(getContext());
        chip.setText(name);
        chip.setTextColor(0xFFFFFFFF);
        chip.setTextSize(12);
        chip.setPadding(dp(12), dp(6), dp(12), dp(6));
        chip.setBackgroundResource(R.drawable.bg_effect_chain_chip);

        try {
            chip.getBackground().setTint(android.graphics.Color.parseColor(colorHex));
        } catch (Exception ignored) {
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(dp(8));
        chip.setLayoutParams(params);

        effectChainChips.addView(chip);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
