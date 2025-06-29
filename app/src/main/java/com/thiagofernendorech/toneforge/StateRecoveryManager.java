package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.HashMap;
import java.util.Map;

public class StateRecoveryManager {
    private static final String TAG = "StateRecoveryManager";
    private static final String PREFS_NAME = "toneforge_state";
    
    // Chaves para salvar estado
    private static final String KEY_PIPELINE_STATE = "pipeline_state";
    private static final String KEY_CURRENT_PRESET = "current_preset";
    private static final String KEY_OVERSAMPLING_ENABLED = "oversampling_enabled";
    private static final String KEY_OVERSAMPLING_FACTOR = "oversampling_factor";
    private static final String KEY_AUDIO_BACKGROUND_ENABLED = "audio_background_enabled";
    private static final String KEY_EFFECTS_STATE = "effects_state";
    private static final String KEY_LAST_ACTIVE_TIME = "last_active_time";
    
    private static StateRecoveryManager instance;
    private Context context;
    private SharedPreferences prefs;
    private boolean isRecovering = false;
    
    private StateRecoveryManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized StateRecoveryManager getInstance(Context context) {
        if (instance == null) {
            instance = new StateRecoveryManager(context);
        }
        return instance;
    }
    
    /**
     * Salva o estado atual do app
     */
    public void saveCurrentState() {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            
            // Salvar estado do pipeline
            PipelineManager pipelineManager = PipelineManager.getInstance();
            editor.putInt(KEY_PIPELINE_STATE, pipelineManager.getState());
            
            // Salvar preset atual (usar AudioStateManager)
            AudioStateManager stateManager = AudioStateManager.getInstance(context);
            String currentPreset = stateManager.getCurrentPreset();
            if (currentPreset != null) {
                editor.putString(KEY_CURRENT_PRESET, currentPreset);
            }
            
            // Salvar configurações de oversampling
            editor.putBoolean(KEY_OVERSAMPLING_ENABLED, AudioEngine.isOversamplingEnabled());
            editor.putInt(KEY_OVERSAMPLING_FACTOR, AudioEngine.getOversamplingFactor());
            
            // Salvar estado do serviço de background
            editor.putBoolean(KEY_AUDIO_BACKGROUND_ENABLED, AudioBackgroundService.isServiceRunning(context));
            
            // Salvar estado dos efeitos
            JSONObject effectsState = stateManager.getEffectsStateAsJson();
            editor.putString(KEY_EFFECTS_STATE, effectsState.toString());
            
            // Salvar timestamp da última atividade
            editor.putLong(KEY_LAST_ACTIVE_TIME, System.currentTimeMillis());
            
            editor.apply();
            Log.d(TAG, "Estado salvo com sucesso");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar estado: " + e.getMessage());
        }
    }
    
    /**
     * Restaura o estado salvo do app
     */
    public void restoreState() {
        if (isRecovering) {
            Log.d(TAG, "Recuperação já em andamento, ignorando");
            return;
        }
        
        isRecovering = true;
        
        try {
            Log.d(TAG, "Iniciando recuperação de estado");
            
            // Verificar se há estado salvo
            long lastActiveTime = prefs.getLong(KEY_LAST_ACTIVE_TIME, 0);
            if (lastActiveTime == 0) {
                Log.d(TAG, "Nenhum estado salvo encontrado");
                isRecovering = false;
                return;
            }
            
            // Restaurar estado do pipeline
            int pipelineState = prefs.getInt(KEY_PIPELINE_STATE, 0); // 0 = STATE_STOPPED
            PipelineManager pipelineManager = PipelineManager.getInstance();
            
            if (pipelineState == 2 && !pipelineManager.isRunning()) { // 2 = STATE_RUNNING
                Log.d(TAG, "Restaurando pipeline para estado ativo");
                pipelineManager.startPipeline();
            }
            
            // Restaurar preset atual (usar AudioStateManager)
            String currentPreset = prefs.getString(KEY_CURRENT_PRESET, null);
            if (currentPreset != null) {
                AudioStateManager stateManager = AudioStateManager.getInstance(context);
                stateManager.setCurrentPreset(currentPreset);
                Log.d(TAG, "Preset restaurado: " + currentPreset);
            }
            
            // Restaurar configurações de oversampling
            boolean oversamplingEnabled = prefs.getBoolean(KEY_OVERSAMPLING_ENABLED, false);
            int oversamplingFactor = prefs.getInt(KEY_OVERSAMPLING_FACTOR, 1);
            
            if (AudioEngine.isOversamplingEnabled() != oversamplingEnabled) {
                AudioEngine.setOversamplingEnabled(oversamplingEnabled);
            }
            if (AudioEngine.getOversamplingFactor() != oversamplingFactor) {
                AudioEngine.setOversamplingFactor(oversamplingFactor);
            }
            
            // Restaurar estado dos efeitos
            String effectsStateJson = prefs.getString(KEY_EFFECTS_STATE, null);
            if (effectsStateJson != null) {
                AudioStateManager stateManager = AudioStateManager.getInstance(context);
                stateManager.restoreEffectsStateFromJson(effectsStateJson);
                Log.d(TAG, "Estado dos efeitos restaurado");
            }
            
            // Restaurar serviço de background se necessário
            boolean audioBackgroundEnabled = prefs.getBoolean(KEY_AUDIO_BACKGROUND_ENABLED, false);
            if (audioBackgroundEnabled && !AudioBackgroundService.isServiceRunning(context)) {
                Log.d(TAG, "Restaurando serviço de background");
                AudioBackgroundService.startService(context);
            }
            
            // Atualizar notificação se o serviço estiver rodando
            if (AudioBackgroundService.isServiceRunning(context)) {
                AudioBackgroundService.updateNotification(context);
            }
            
            Log.d(TAG, "Recuperação de estado concluída com sucesso");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao restaurar estado: " + e.getMessage());
        } finally {
            isRecovering = false;
        }
    }
    
    /**
     * Verifica se é necessário restaurar o estado
     */
    public boolean needsStateRecovery() {
        long lastActiveTime = prefs.getLong(KEY_LAST_ACTIVE_TIME, 0);
        if (lastActiveTime == 0) {
            return false;
        }
        
        // Se passou mais de 5 minutos desde a última atividade, considerar que precisa recuperar
        long timeSinceLastActive = System.currentTimeMillis() - lastActiveTime;
        return timeSinceLastActive > 5 * 60 * 1000; // 5 minutos
    }
    
    /**
     * Limpa o estado salvo
     */
    public void clearSavedState() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        Log.d(TAG, "Estado salvo limpo");
    }
    
    /**
     * Força a recuperação de estado (ignora verificações de tempo)
     */
    public void forceStateRecovery() {
        Log.d(TAG, "Forçando recuperação de estado");
        restoreState();
    }
    
    /**
     * Obtém informações sobre o estado salvo para debug
     */
    public String getSavedStateInfo() {
        try {
            StringBuilder info = new StringBuilder();
            info.append("Estado salvo:\n");
            info.append("- Pipeline: ").append(prefs.getInt(KEY_PIPELINE_STATE, -1)).append("\n");
            info.append("- Preset: ").append(prefs.getString(KEY_CURRENT_PRESET, "null")).append("\n");
            info.append("- Oversampling: ").append(prefs.getBoolean(KEY_OVERSAMPLING_ENABLED, false)).append("\n");
            info.append("- Fator: ").append(prefs.getInt(KEY_OVERSAMPLING_FACTOR, 1)).append("\n");
            info.append("- Background: ").append(prefs.getBoolean(KEY_AUDIO_BACKGROUND_ENABLED, false)).append("\n");
            info.append("- Última atividade: ").append(prefs.getLong(KEY_LAST_ACTIVE_TIME, 0)).append("\n");
            
            return info.toString();
        } catch (Exception e) {
            return "Erro ao obter informações do estado: " + e.getMessage();
        }
    }
    
    /**
     * Verifica se a recuperação está em andamento
     */
    public boolean isRecovering() {
        return isRecovering;
    }
} 