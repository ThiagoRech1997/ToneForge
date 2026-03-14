package com.thiagofernendorech.toneforge.ui.fragments.effects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thiagofernendorech.toneforge.R;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.ui.base.BaseFragment;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fragment para exibir efeitos de uma categoria especifica.
 * Mostra toggle + sliders para cada efeito da categoria.
 */
public class EffectCategoryFragment extends BaseFragment<EffectCategoryPresenter>
        implements EffectCategoryContract.View {

    private static final String ARG_CATEGORY = "category";

    private String category;
    private LinearLayout effectsContainer;

    public static EffectCategoryFragment newInstance(String category) {
        EffectCategoryFragment fragment = new EffectCategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected EffectCategoryPresenter createPresenter() {
        AudioRepository audioRepository = AudioRepository.getInstance(requireContext());
        return new EffectCategoryPresenter(audioRepository);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY, "distortion");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Use a simple scrollable linear layout
        View view = inflater.inflate(R.layout.fragment_effect_detail, container, false);

        effectsContainer = view.findViewById(R.id.effectsContainer);

        // Populate effects for this category
        buildEffectsUI();

        if (presenter != null) {
            presenter.onCategoryLoaded(category);
        }

        return view;
    }

    private void buildEffectsUI() {
        if (effectsContainer == null) return;
        effectsContainer.removeAllViews();

        Map<String, String[]> effectsByCategory = getEffectsForCategory();

        for (Map.Entry<String, String[]> entry : effectsByCategory.entrySet()) {
            String effectName = entry.getKey();
            String[] params = entry.getValue();
            addEffectCard(effectName, params);
        }
    }

    private Map<String, String[]> getEffectsForCategory() {
        Map<String, String[]> effects = new LinkedHashMap<>();

        switch (category) {
            case "distortion":
                effects.put("Ganho", new String[]{"level"});
                effects.put("Distortion", new String[]{"amount", "mix"});
                break;
            case "modulation":
                effects.put("Chorus", new String[]{"depth", "rate", "mix"});
                effects.put("Flanger", new String[]{"depth", "rate", "feedback", "mix"});
                effects.put("Phaser", new String[]{"depth", "rate", "feedback", "mix"});
                break;
            case "time":
                effects.put("Delay", new String[]{"time", "feedback", "mix"});
                effects.put("Reverb", new String[]{"roomSize", "damping", "mix"});
                break;
            case "filter":
                effects.put("EQ", new String[]{"low", "mid", "high"});
                break;
            case "dynamics":
                effects.put("Compressor", new String[]{"threshold", "ratio", "attack", "release"});
                break;
            case "ambient":
                effects.put("Reverb", new String[]{"roomSize", "damping", "mix"});
                break;
            default:
                break;
        }

        return effects;
    }

    private void addEffectCard(String effectName, String[] params) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.modern_card_background);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        // Header: effect name + toggle
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView nameView = new TextView(requireContext());
        nameView.setText(effectName);
        nameView.setTextColor(getResources().getColor(R.color.lava_text_primary));
        nameView.setTextSize(18);
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        nameView.setLayoutParams(nameParams);
        header.addView(nameView);

        Switch toggle = new Switch(requireContext());
        toggle.setChecked(false);
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (presenter != null) {
                presenter.onEffectToggled(effectName, isChecked);
            }
        });
        header.addView(toggle);

        card.addView(header);

        // Parameter sliders
        for (String param : params) {
            addParameterSlider(card, effectName, param);
        }

        effectsContainer.addView(card);
    }

    private void addParameterSlider(LinearLayout parent, String effectName, String paramName) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0, dp(8), 0, 0);

        // Label row
        LinearLayout labelRow = new LinearLayout(requireContext());
        labelRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView label = new TextView(requireContext());
        label.setText(capitalize(paramName));
        label.setTextColor(getResources().getColor(R.color.lava_text_secondary));
        label.setTextSize(14);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        label.setLayoutParams(labelParams);
        labelRow.addView(label);

        TextView valueText = new TextView(requireContext());
        valueText.setText("50");
        valueText.setTextColor(getResources().getColor(R.color.lava_text_primary));
        valueText.setTextSize(14);
        valueText.setTypeface(null, android.graphics.Typeface.BOLD);
        labelRow.addView(valueText);

        container.addView(labelRow);

        // SeekBar
        SeekBar seekBar = new SeekBar(requireContext());
        seekBar.setMax(100);
        seekBar.setProgress(50);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valueText.setText(String.valueOf(progress));
                if (fromUser && presenter != null) {
                    presenter.onParameterChanged(effectName, paramName, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        container.addView(seekBar);
        parent.addView(container);
    }

    @Override
    public void updateEffectEnabled(String effectName, boolean enabled) {
        // TODO: update toggle state in UI
    }

    @Override
    public void updateEffectParameter(String effectName, String paramName, int value) {
        // TODO: update seekbar value in UI
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
