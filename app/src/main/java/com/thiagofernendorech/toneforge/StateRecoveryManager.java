package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.HashMap;
import java.util.Map;
import android.widget.Toast;

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
    private static final String KEY_AUDIO_STATE = "audio_state";
    private static final String KEY_LATENCY_STATE = "latency_state";
    
    private static StateRecoveryManager instance;
    private Context context;
    private SharedPreferences prefs;
    private boolean isRecovering = false;
    private LatencyManager latencyManager;
    
    private StateRecoveryManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.latencyManager = LatencyManager.getInstance(context);
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
            JSONObject pipelineState = new JSONObject();
            pipelineState.put("isRunning", pipelineManager.isRunning());
            pipelineState.put("isPaused", pipelineManager.isPaused());
            editor.putString(KEY_PIPELINE_STATE, pipelineState.toString());
            
            // Salvar preset atual (usar AudioStateManager)
            AudioStateManager stateManager = AudioStateManager.getInstance(context);
            String currentPreset = stateManager.getCurrentPreset();
            editor.putString(KEY_CURRENT_PRESET, currentPreset);
            
            // Salvar configurações de oversampling
            boolean oversamplingEnabled = AudioEngine.isOversamplingEnabled();
            int oversamplingFactor = AudioEngine.getOversamplingFactor();
            editor.putBoolean(KEY_OVERSAMPLING_ENABLED, oversamplingEnabled);
            editor.putInt(KEY_OVERSAMPLING_FACTOR, oversamplingFactor);
            
            // Salvar estado dos efeitos
            JSONObject effectsState = stateManager.getEffectsStateAsJson();
            editor.putString(KEY_EFFECTS_STATE, effectsState.toString());
            
            // Salvar estado do áudio
            JSONObject audioState = stateManager.serializeState();
            editor.putString(KEY_AUDIO_STATE, audioState.toString());
            
            // Salvar estado de latência
            JSONObject latencyState = new JSONObject();
            latencyState.put("mode", latencyManager.getCurrentMode());
            editor.putString(KEY_LATENCY_STATE, latencyState.toString());
            
            // Salvar estado do serviço de background
            editor.putBoolean(KEY_AUDIO_BACKGROUND_ENABLED, AudioBackgroundService.isServiceRunning(context));
            
            // Salvar timestamp da última atividade
            editor.putLong(KEY_LAST_ACTIVE_TIME, System.currentTimeMillis());
            
            editor.apply();
            Log.d(TAG, "Estado salvo com sucesso");
            Toast.makeText(context, context.getString(R.string.state_saved), Toast.LENGTH_SHORT).show();
            
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
            String pipelineStateStr = prefs.getString(KEY_PIPELINE_STATE, null);
            if (pipelineStateStr != null) {
                JSONObject pipelineState = new JSONObject(pipelineStateStr);
                PipelineManager pipelineManager = PipelineManager.getInstance();
                
                boolean wasRunning = pipelineState.optBoolean("isRunning", false);
                boolean wasPaused = pipelineState.optBoolean("isPaused", false);
                
                if (wasRunning && !pipelineManager.isRunning()) {
                    pipelineManager.startPipeline();
                    if (wasPaused) {
                        pipelineManager.pausePipeline();
                    }
                }
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
            
            // Restaurar estado do áudio
            String audioStateStr = prefs.getString(KEY_AUDIO_STATE, null);
            if (audioStateStr != null) {
                JSONObject audioState = new JSONObject(audioStateStr);
                AudioStateManager audioStateManager = AudioStateManager.getInstance(context);
                audioStateManager.deserializeState(audioState);
            }
            
            // Restaurar estado de latência
            String latencyStateStr = prefs.getString(KEY_LATENCY_STATE, null);
            if (latencyStateStr != null) {
                JSONObject latencyState = new JSONObject(latencyStateStr);
                int savedMode = latencyState.optInt("mode", LatencyManager.MODE_BALANCED);
                latencyManager.setLatencyMode(savedMode);
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
            Toast.makeText(context, context.getString(R.string.state_restored), Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao restaurar estado: " + e.getMessage());
            Toast.makeText(context, context.getString(R.string.state_recovery_failed), Toast.LENGTH_SHORT).show();
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
            info.append("- Pipeline: ").append(prefs.getString(KEY_PIPELINE_STATE, "null")).append("\n");
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