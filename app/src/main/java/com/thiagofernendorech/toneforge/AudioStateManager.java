package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class AudioStateManager {
    private static final String TAG = "AudioStateManager";
    private static final String PREFS_NAME = "AudioStatePrefs";
    private static final String KEY_CURRENT_PRESET = "current_preset";
    private static final String KEY_AUDIO_LEVEL = "audio_level";
    private static final String KEY_OVERSAMPLING_ENABLED = "oversampling_enabled";
    private static final String KEY_OVERSAMPLING_FACTOR = "oversampling_factor";
    
    private static AudioStateManager instance;
    private SharedPreferences prefs;
    
    // Contadores de efeitos ativos (serão atualizados dinamicamente)
    private static int activeEffectsCount = 0;
    private static boolean gainEnabled = false;
    private static boolean distortionEnabled = false;
    private static boolean delayEnabled = false;
    private static boolean reverbEnabled = false;
    private static boolean chorusEnabled = false;
    private static boolean flangerEnabled = false;
    private static boolean phaserEnabled = false;
    private static boolean eqEnabled = false;
    private static boolean compressorEnabled = false;
    
    private AudioStateManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static AudioStateManager getInstance(Context context) {
        if (instance == null) {
            instance = new AudioStateManager(context.getApplicationContext());
        }
        return instance;
    }
    
    // Métodos para atualizar estado dos efeitos
    public static void updateEffectState(String effectName, boolean enabled) {
        switch (effectName) {
            case "gain":
                gainEnabled = enabled;
                break;
            case "distortion":
                distortionEnabled = enabled;
                break;
            case "delay":
                delayEnabled = enabled;
                break;
            case "reverb":
                reverbEnabled = enabled;
                break;
            case "chorus":
                chorusEnabled = enabled;
                break;
            case "flanger":
                flangerEnabled = enabled;
                break;
            case "phaser":
                phaserEnabled = enabled;
                break;
            case "eq":
                eqEnabled = enabled;
                break;
            case "compressor":
                compressorEnabled = enabled;
                break;
        }
        updateActiveEffectsCount();
    }
    
    private static void updateActiveEffectsCount() {
        activeEffectsCount = 0;
        if (gainEnabled) activeEffectsCount++;
        if (distortionEnabled) activeEffectsCount++;
        if (delayEnabled) activeEffectsCount++;
        if (reverbEnabled) activeEffectsCount++;
        if (chorusEnabled) activeEffectsCount++;
        if (flangerEnabled) activeEffectsCount++;
        if (phaserEnabled) activeEffectsCount++;
        if (eqEnabled) activeEffectsCount++;
        if (compressorEnabled) activeEffectsCount++;
    }
    
    // Métodos para obter informações do estado
    public int getActiveEffectsCount() {
        return activeEffectsCount;
    }
    
    public String getCurrentPreset() {
        return prefs.getString(KEY_CURRENT_PRESET, null);
    }
    
    public void setCurrentPreset(String presetName) {
        prefs.edit().putString(KEY_CURRENT_PRESET, presetName).apply();
    }
    
    public int getAudioLevel() {
        return prefs.getInt(KEY_AUDIO_LEVEL, 50);
    }
    
    public void setAudioLevel(int level) {
        prefs.edit().putInt(KEY_AUDIO_LEVEL, level).apply();
    }
    
    public boolean isOversamplingEnabled() {
        return prefs.getBoolean(KEY_OVERSAMPLING_ENABLED, false);
    }
    
    public void setOversamplingEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_OVERSAMPLING_ENABLED, enabled).apply();
    }
    
    public int getOversamplingFactor() {
        return prefs.getInt(KEY_OVERSAMPLING_FACTOR, 1);
    }
    
    public void setOversamplingFactor(int factor) {
        prefs.edit().putInt(KEY_OVERSAMPLING_FACTOR, factor).apply();
    }
    
    // Métodos para obter informações formatadas para notificação
    public String getEffectsStatusText(Context context) {
        if (activeEffectsCount == 0) {
            return context.getString(R.string.notification_effects_none);
        } else {
            return context.getString(R.string.notification_effects_active, activeEffectsCount);
        }
    }
    
    public String getPresetStatusText(Context context) {
        String currentPreset = getCurrentPreset();
        if (currentPreset == null || currentPreset.isEmpty()) {
            return context.getString(R.string.notification_preset_none);
        } else {
            return context.getString(R.string.notification_preset_current, currentPreset);
        }
    }
    
    public String getAudioLevelText(Context context) {
        return context.getString(R.string.notification_audio_level, getAudioLevel());
    }
    
    public String getOversamplingText(Context context) {
        if (isOversamplingEnabled()) {
            return context.getString(R.string.notification_oversampling_on);
        } else {
            return context.getString(R.string.notification_oversampling_off);
        }
    }
    
    public String getAudioQualityText(Context context) {
        if (isOversamplingEnabled()) {
            int factor = getOversamplingFactor();
            return context.getString(R.string.notification_audio_quality, factor + "x");
        } else {
            return context.getString(R.string.notification_audio_quality, "1x");
        }
    }
    
    public String getStatusText(Context context) {
        if (!AudioEngine.isAudioPipelineRunning()) {
            return context.getString(R.string.notification_status_standby);
        } else {
            return context.getString(R.string.notification_status_processing);
        }
    }
    
    // Métodos para verificar efeitos específicos
    public boolean isGainEnabled() { return gainEnabled; }
    public boolean isDistortionEnabled() { return distortionEnabled; }
    public boolean isDelayEnabled() { return delayEnabled; }
    public boolean isReverbEnabled() { return reverbEnabled; }
    public boolean isChorusEnabled() { return chorusEnabled; }
    public boolean isFlangerEnabled() { return flangerEnabled; }
    public boolean isPhaserEnabled() { return phaserEnabled; }
    public boolean isEQEnabled() { return eqEnabled; }
    public boolean isCompressorEnabled() { return compressorEnabled; }
    
    /**
     * Serializa o estado dos efeitos para JSON
     */
    public JSONObject getEffectsStateAsJson() {
        try {
            JSONObject state = new JSONObject();
            
            // Estado dos efeitos
            JSONObject effects = new JSONObject();
            effects.put("gain", gainEnabled);
            effects.put("distortion", distortionEnabled);
            effects.put("delay", delayEnabled);
            effects.put("reverb", reverbEnabled);
            effects.put("chorus", chorusEnabled);
            effects.put("flanger", flangerEnabled);
            effects.put("phaser", phaserEnabled);
            effects.put("eq", eqEnabled);
            effects.put("compressor", compressorEnabled);
            effects.put("activeCount", activeEffectsCount);
            
            state.put("effects", effects);
            
            // Configurações gerais
            state.put("currentPreset", getCurrentPreset());
            state.put("audioLevel", getAudioLevel());
            state.put("oversamplingEnabled", isOversamplingEnabled());
            state.put("oversamplingFactor", getOversamplingFactor());
            
            return state;
            
        } catch (JSONException e) {
            Log.e(TAG, "Erro ao serializar estado dos efeitos: " + e.getMessage());
            return new JSONObject();
        }
    }
    
    /**
     * Restaura o estado dos efeitos a partir de JSON
     */
    public void restoreEffectsStateFromJson(String jsonString) {
        try {
            JSONObject state = new JSONObject(jsonString);
            
            // Restaurar estado dos efeitos
            if (state.has("effects")) {
                JSONObject effects = state.getJSONObject("effects");
                gainEnabled = effects.optBoolean("gain", false);
                distortionEnabled = effects.optBoolean("distortion", false);
                delayEnabled = effects.optBoolean("delay", false);
                reverbEnabled = effects.optBoolean("reverb", false);
                chorusEnabled = effects.optBoolean("chorus", false);
                flangerEnabled = effects.optBoolean("flanger", false);
                phaserEnabled = effects.optBoolean("phaser", false);
                eqEnabled = effects.optBoolean("eq", false);
                compressorEnabled = effects.optBoolean("compressor", false);
                activeEffectsCount = effects.optInt("activeCount", 0);
            }
            
            // Restaurar configurações gerais
            if (state.has("currentPreset")) {
                setCurrentPreset(state.optString("currentPreset", null));
            }
            if (state.has("audioLevel")) {
                setAudioLevel(state.optInt("audioLevel", 50));
            }
            if (state.has("oversamplingEnabled")) {
                setOversamplingEnabled(state.optBoolean("oversamplingEnabled", false));
            }
            if (state.has("oversamplingFactor")) {
                setOversamplingFactor(state.optInt("oversamplingFactor", 1));
            }
            
            Log.d(TAG, "Estado dos efeitos restaurado com sucesso");
            
        } catch (JSONException e) {
            Log.e(TAG, "Erro ao restaurar estado dos efeitos: " + e.getMessage());
        }
    }
} 