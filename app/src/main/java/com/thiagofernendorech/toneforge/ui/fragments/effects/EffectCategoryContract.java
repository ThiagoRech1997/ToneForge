package com.thiagofernendorech.toneforge.ui.fragments.effects;

import com.thiagofernendorech.toneforge.ui.base.BaseView;

/**
 * Contrato MVP para o EffectCategoryFragment
 * Tela de detalhe de uma categoria de efeitos
 */
public interface EffectCategoryContract {

    interface View extends BaseView {
        void updateEffectEnabled(String effectName, boolean enabled);
        void updateEffectParameter(String effectName, String paramName, int value);
    }

    interface Presenter {
        void onCategoryLoaded(String category);
        void onEffectToggled(String effectName, boolean enabled);
        void onParameterChanged(String effectName, String paramName, int value);
        void onSavePreset();
        void onReset();
    }
}
