package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gerenciador de automação para o ToneForge
 * Responsável por armazenar, gerenciar e reproduzir automações de parâmetros
 */
public class AutomationManager {
    private static final String TAG = "AutomationManager";
    private static final String AUTOMATION_PREFS_KEY = "automation_data";
    private static final String AUTOMATION_VERSION = "1.0";
    
    /**
     * Representa um evento de automação individual
     */
    public static class AutomationEvent {
        public long timestamp;      // Timestamp em milissegundos
        public String parameter;    // Nome do parâmetro (ex: "gain", "distortion", "delay_time")
        public float value;         // Valor do parâmetro
        public String source;       // Fonte da mudança ("ui", "midi", "automation")
        
        public AutomationEvent(long timestamp, String parameter, float value, String source) {
            this.timestamp = timestamp;
            this.parameter = parameter;
            this.value = value;
            this.source = source;
        }
        
        public JSONObject toJson() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("timestamp", timestamp);
            obj.put("parameter", parameter);
            obj.put("value", value);
            obj.put("source", source);
            return obj;
        }
        
        public static AutomationEvent fromJson(JSONObject obj) throws JSONException {
            return new AutomationEvent(
                obj.getLong("timestamp"),
                obj.getString("parameter"),
                (float) obj.getDouble("value"),
                obj.getString("source")
            );
        }
    }
    
    /**
     * Representa uma automação completa para um preset
     */
    public static class AutomationData {
        public String presetName;                           // Nome do preset associado
        public String automationName;                       // Nome da automação
        public long duration;                               // Duração total em ms
        public List<AutomationEvent> events;                // Lista de eventos
        public Map<String, List<AutomationEvent>> eventsByParameter; // Eventos organizados por parâmetro
        public boolean isLooping;                           // Se deve repetir
        public long loopStart;                              // Início do loop em ms
        public long loopEnd;                                // Fim do loop em ms
        
        public AutomationData(String presetName, String automationName) {
            this.presetName = presetName;
            this.automationName = automationName;
            this.events = new ArrayList<>();
            this.eventsByParameter = new HashMap<>();
            this.isLooping = false;
            this.loopStart = 0;
            this.loopEnd = 0;
        }
        
        /**
         * Adiciona um evento de automação
         */
        public void addEvent(AutomationEvent event) {
            events.add(event);
            
            // Organizar por parâmetro para busca rápida
            if (!eventsByParameter.containsKey(event.parameter)) {
                eventsByParameter.put(event.parameter, new ArrayList<>());
            }
            eventsByParameter.get(event.parameter).add(event);
            
            // Atualizar duração se necessário
            if (event.timestamp > duration) {
                duration = event.timestamp;
            }
        }
        
        /**
         * Obtém o valor de um parâmetro em um timestamp específico
         */
        public float getParameterValueAtTime(String parameter, long timestamp) {
            List<AutomationEvent> parameterEvents = eventsByParameter.get(parameter);
            if (parameterEvents == null || parameterEvents.isEmpty()) {
                return Float.NaN; // Sem automação para este parâmetro
            }
            
            // Encontrar os dois eventos mais próximos do timestamp
            AutomationEvent before = null;
            AutomationEvent after = null;
            
            for (AutomationEvent event : parameterEvents) {
                if (event.timestamp <= timestamp) {
                    if (before == null || event.timestamp > before.timestamp) {
                        before = event;
                    }
                } else {
                    if (after == null || event.timestamp < after.timestamp) {
                        after = event;
                    }
                }
            }
            
            // Se não há evento antes, usar o primeiro
            if (before == null && !parameterEvents.isEmpty()) {
                before = parameterEvents.get(0);
            }
            
            // Se não há evento depois, usar o último
            if (after == null && !parameterEvents.isEmpty()) {
                after = parameterEvents.get(parameterEvents.size() - 1);
            }
            
            // Se só há um evento, retornar seu valor
            if (before != null && after == null) {
                return before.value;
            }
            
            if (before == null && after != null) {
                return after.value;
            }
            
            // Interpolar entre os dois eventos
            if (before != null && after != null) {
                if (before.timestamp == after.timestamp) {
                    return before.value;
                }
                
                float t = (float) (timestamp - before.timestamp) / (after.timestamp - before.timestamp);
                return before.value + (after.value - before.value) * t;
            }
            
            return Float.NaN;
        }
        
        /**
         * Aplica loop se configurado
         */
        public long applyLoop(long timestamp) {
            if (!isLooping || loopEnd <= loopStart) {
                return timestamp;
            }
            
            long loopDuration = loopEnd - loopStart;
            if (loopDuration <= 0) {
                return timestamp;
            }
            
            if (timestamp > loopEnd) {
                long cycles = (timestamp - loopStart) / loopDuration;
                return loopStart + (timestamp - loopStart) % loopDuration;
            }
            
            return timestamp;
        }
        
        public JSONObject toJson() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("presetName", presetName);
            obj.put("automationName", automationName);
            obj.put("duration", duration);
            obj.put("isLooping", isLooping);
            obj.put("loopStart", loopStart);
            obj.put("loopEnd", loopEnd);
            
            JSONArray eventsArray = new JSONArray();
            for (AutomationEvent event : events) {
                eventsArray.put(event.toJson());
            }
            obj.put("events", eventsArray);
            
            return obj;
        }
        
        public static AutomationData fromJson(JSONObject obj) throws JSONException {
            String presetName = obj.getString("presetName");
            String automationName = obj.getString("automationName");
            
            AutomationData data = new AutomationData(presetName, automationName);
            data.duration = obj.getLong("duration");
            data.isLooping = obj.getBoolean("isLooping");
            data.loopStart = obj.getLong("loopStart");
            data.loopEnd = obj.getLong("loopEnd");
            
            JSONArray eventsArray = obj.getJSONArray("events");
            for (int i = 0; i < eventsArray.length(); i++) {
                AutomationEvent event = AutomationEvent.fromJson(eventsArray.getJSONObject(i));
                data.addEvent(event);
            }
            
            return data;
        }
    }
    
    // Instância singleton
    private static AutomationManager instance;
    private Context context;
    private Map<String, AutomationData> automations; // presetName -> AutomationData
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private long recordingStartTime = 0;
    private long playbackStartTime = 0;
    private String currentPresetName = "";
    private String currentAutomationName = "";
    
    private AutomationManager(Context context) {
        this.context = context.getApplicationContext();
        this.automations = new HashMap<>();
        loadAutomations();
    }
    
    public static synchronized AutomationManager getInstance(Context context) {
        if (instance == null) {
            instance = new AutomationManager(context);
        }
        return instance;
    }
    
    /**
     * Inicia a gravação de automação
     */
    public void startRecording(String presetName, String automationName) {
        if (isRecording) {
            Log.w(TAG, "Gravação já está ativa");
            return;
        }
        
        this.currentPresetName = presetName;
        this.currentAutomationName = automationName;
        this.recordingStartTime = System.currentTimeMillis();
        this.isRecording = true;
        
        // Criar nova automação ou limpar existente
        String key = getAutomationKey(presetName, automationName);
        automations.put(key, new AutomationData(presetName, automationName));
        
        Log.d(TAG, "Iniciada gravação de automação: " + automationName + " para preset: " + presetName);
    }
    
    /**
     * Para a gravação de automação
     */
    public void stopRecording() {
        if (!isRecording) {
            Log.w(TAG, "Nenhuma gravação ativa");
            return;
        }
        
        this.isRecording = false;
        saveAutomations();
        
        Log.d(TAG, "Parada gravação de automação: " + currentAutomationName);
    }
    
    /**
     * Registra um evento de automação durante a gravação
     */
    public void recordEvent(String parameter, float value, String source) {
        if (!isRecording) {
            return;
        }
        
        long timestamp = System.currentTimeMillis() - recordingStartTime;
        AutomationEvent event = new AutomationEvent(timestamp, parameter, value, source);
        
        String key = getAutomationKey(currentPresetName, currentAutomationName);
        AutomationData automation = automations.get(key);
        if (automation != null) {
            automation.addEvent(event);
        }
        
        Log.d(TAG, "Evento registrado: " + parameter + " = " + value + " em " + timestamp + "ms");
    }
    
    /**
     * Inicia a reprodução de automação
     */
    public void startPlayback(String presetName, String automationName) {
        if (isPlaying) {
            Log.w(TAG, "Reprodução já está ativa");
            return;
        }
        
        String key = getAutomationKey(presetName, automationName);
        AutomationData automation = automations.get(key);
        if (automation == null) {
            Log.w(TAG, "Automação não encontrada: " + automationName);
            return;
        }
        
        this.currentPresetName = presetName;
        this.currentAutomationName = automationName;
        this.playbackStartTime = System.currentTimeMillis();
        this.isPlaying = true;
        
        Log.d(TAG, "Iniciada reprodução de automação: " + automationName);
    }
    
    /**
     * Para a reprodução de automação
     */
    public void stopPlayback() {
        if (!isPlaying) {
            Log.w(TAG, "Nenhuma reprodução ativa");
            return;
        }
        
        this.isPlaying = false;
        Log.d(TAG, "Parada reprodução de automação: " + currentAutomationName);
    }
    
    /**
     * Obtém o valor atual de um parâmetro durante a reprodução
     */
    public float getCurrentParameterValue(String parameter) {
        if (!isPlaying) {
            return Float.NaN;
        }
        
        String key = getAutomationKey(currentPresetName, currentAutomationName);
        AutomationData automation = automations.get(key);
        if (automation == null) {
            return Float.NaN;
        }
        
        long currentTime = System.currentTimeMillis() - playbackStartTime;
        currentTime = automation.applyLoop(currentTime);
        
        return automation.getParameterValueAtTime(parameter, currentTime);
    }
    
    /**
     * Salva uma automação
     */
    public void saveAutomation(AutomationData automation) {
        String key = getAutomationKey(automation.presetName, automation.automationName);
        automations.put(key, automation);
        saveAutomations();
    }
    
    /**
     * Remove uma automação
     */
    public void deleteAutomation(String presetName, String automationName) {
        String key = getAutomationKey(presetName, automationName);
        automations.remove(key);
        saveAutomations();
    }
    
    /**
     * Obtém todas as automações de um preset
     */
    public List<AutomationData> getAutomationsForPreset(String presetName) {
        List<AutomationData> result = new ArrayList<>();
        for (AutomationData automation : automations.values()) {
            if (automation.presetName.equals(presetName)) {
                result.add(automation);
            }
        }
        return result;
    }
    
    /**
     * Obtém todas as automações
     */
    public List<AutomationData> getAllAutomations() {
        return new ArrayList<>(automations.values());
    }
    
    /**
     * Verifica se está gravando
     */
    public boolean isRecording() {
        return isRecording;
    }
    
    /**
     * Verifica se está reproduzindo
     */
    public boolean isPlaying() {
        return isPlaying;
    }
    
    /**
     * Obtém o nome da automação atual
     */
    public String getCurrentAutomationName() {
        return currentAutomationName;
    }
    
    /**
     * Obtém o progresso atual da reprodução (0.0 a 1.0)
     */
    public float getPlaybackProgress() {
        if (!isPlaying || currentAutomationName.isEmpty()) {
            return 0.0f;
        }
        
        AutomationData automation = automations.get(getAutomationKey(currentPresetName, currentAutomationName));
        if (automation == null || automation.duration == 0) {
            return 0.0f;
        }
        
        long currentTime = System.currentTimeMillis() - playbackStartTime;
        currentTime = automation.applyLoop(currentTime);
        
        return Math.min(1.0f, (float) currentTime / automation.duration);
    }
    
    /**
     * Obtém a duração total da automação atual em segundos
     */
    public float getAutomationDuration() {
        if (currentAutomationName.isEmpty()) {
            return 0.0f;
        }
        
        AutomationData automation = automations.get(getAutomationKey(currentPresetName, currentAutomationName));
        if (automation == null) {
            return 0.0f;
        }
        
        return automation.duration / 1000.0f; // Converter de ms para segundos
    }
    
    /**
     * Obtém o tempo atual de gravação em segundos
     */
    public float getRecordingTime() {
        if (!isRecording) {
            return 0.0f;
        }
        
        return (System.currentTimeMillis() - recordingStartTime) / 1000.0f; // Converter de ms para segundos
    }
    
    /**
     * Gera chave única para automação
     */
    private String getAutomationKey(String presetName, String automationName) {
        return presetName + "::" + automationName;
    }
    
    /**
     * Carrega automações do SharedPreferences
     */
    private void loadAutomations() {
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String automationJson = prefs.getString(AUTOMATION_PREFS_KEY, "{}");
        
        try {
            JSONObject root = new JSONObject(automationJson);
            JSONArray automationsArray = root.getJSONArray("automations");
            
            for (int i = 0; i < automationsArray.length(); i++) {
                JSONObject automationObj = automationsArray.getJSONObject(i);
                AutomationData automation = AutomationData.fromJson(automationObj);
                String key = getAutomationKey(automation.presetName, automation.automationName);
                automations.put(key, automation);
            }
            
            Log.d(TAG, "Carregadas " + automations.size() + " automações");
            
        } catch (JSONException e) {
            Log.e(TAG, "Erro ao carregar automações: " + e.getMessage());
        }
    }
    
    /**
     * Salva automações no SharedPreferences
     */
    private void saveAutomations() {
        try {
            JSONObject root = new JSONObject();
            root.put("version", AUTOMATION_VERSION);
            
            JSONArray automationsArray = new JSONArray();
            for (AutomationData automation : automations.values()) {
                automationsArray.put(automation.toJson());
            }
            root.put("automations", automationsArray);
            
            SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putString(AUTOMATION_PREFS_KEY, root.toString()).apply();
            
            Log.d(TAG, "Salvas " + automations.size() + " automações");
            
        } catch (JSONException e) {
            Log.e(TAG, "Erro ao salvar automações: " + e.getMessage());
        }
    }
} 