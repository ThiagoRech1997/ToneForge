package com.thiagofernendorech.toneforge.ui.fragments.effects;

import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;

/**
 * Presenter para categoria de efeitos.
 * Delega operacoes de enable/disable ao AudioRepository.
 * Parametros sao aplicados via EffectParameters model.
 */
public class EffectCategoryPresenter extends BasePresenter<EffectCategoryContract.View>
        implements EffectCategoryContract.Presenter {

    private final AudioRepository audioRepository;
    private String currentCategory;
    private EffectParameters parameters;

    public EffectCategoryPresenter(AudioRepository audioRepository) {
        this.audioRepository = audioRepository;
        this.parameters = new EffectParameters();
    }

    @Override
    public void onCategoryLoaded(String category) {
        this.currentCategory = category;
        // Parameters start with defaults; applied incrementally
    }

    @Override
    public void onEffectToggled(String effectName, boolean enabled) {
        if (audioRepository == null) return;

        switch (effectName) {
            case "Ganho":
                audioRepository.setGainEnabled(enabled);
                break;
            case "Distortion":
                audioRepository.setDistortionEnabled(enabled);
                break;
            case "Delay":
                audioRepository.setDelayEnabled(enabled);
                break;
            case "Reverb":
                audioRepository.setReverbEnabled(enabled);
                break;
            case "Chorus":
                audioRepository.setChorusEnabled(enabled);
                break;
            case "Flanger":
                audioRepository.setFlangerEnabled(enabled);
                break;
            case "Phaser":
                audioRepository.setPhaserEnabled(enabled);
                break;
            case "EQ":
                audioRepository.setEQEnabled(enabled);
                break;
            case "Compressor":
                audioRepository.setCompressorEnabled(enabled);
                break;
        }
    }

    @Override
    public void onParameterChanged(String effectName, String paramName, int value) {
        if (audioRepository == null) return;

        float normalized = value / 100f;

        // Update local parameters model and apply
        updateParameter(effectName, paramName, normalized);
        audioRepository.applyEffectParameters(parameters);
    }

    private void updateParameter(String effectName, String paramName, float value) {
        switch (effectName) {
            case "Ganho":
                parameters.setGain(value);
                break;
            case "Distortion":
                if ("amount".equals(paramName)) parameters.setDistortion(value);
                break;
            case "Delay":
                if ("time".equals(paramName)) parameters.setDelayTime(value);
                else if ("feedback".equals(paramName)) parameters.setDelayFeedback(value);
                break;
            case "Reverb":
                if ("roomSize".equals(paramName)) parameters.setReverbRoomSize(value);
                else if ("damping".equals(paramName)) parameters.setReverbDamping(value);
                break;
            case "Chorus":
                if ("depth".equals(paramName)) parameters.setChorusDepth(value);
                else if ("rate".equals(paramName)) parameters.setChorusRate(value);
                break;
            case "Flanger":
                if ("depth".equals(paramName)) parameters.setFlangerDepth(value);
                else if ("rate".equals(paramName)) parameters.setFlangerRate(value);
                else if ("feedback".equals(paramName)) parameters.setFlangerMix(value);
                break;
            case "Phaser":
                if ("depth".equals(paramName)) parameters.setPhaserDepth(value);
                else if ("rate".equals(paramName)) parameters.setPhaserRate(value);
                else if ("feedback".equals(paramName)) parameters.setPhaserMix(value);
                break;
            case "EQ":
                if ("low".equals(paramName)) parameters.setEqLow(value);
                else if ("mid".equals(paramName)) parameters.setEqMid(value);
                else if ("high".equals(paramName)) parameters.setEqHigh(value);
                break;
            case "Compressor":
                if ("threshold".equals(paramName)) parameters.setCompressorThreshold(value);
                else if ("ratio".equals(paramName)) parameters.setCompressorRatio(value);
                else if ("attack".equals(paramName)) parameters.setCompressorAttack(value);
                else if ("release".equals(paramName)) parameters.setCompressorRelease(value);
                break;
        }
    }

    @Override
    public void onSavePreset() {
        // TODO: Delegate to PresetManager
    }

    @Override
    public void onReset() {
        // TODO: Reset effects in this category to defaults
    }
}
