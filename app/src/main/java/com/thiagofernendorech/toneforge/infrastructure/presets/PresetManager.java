package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PresetManager {
    private static final String TAG = "PresetManager";
    private static final String PRESET_VERSION = "1.1";
    
    public static class PresetData {
        public String name;
        public String createdBy;
        public String createdDate;
        public String version;
        public JSONObject effectData;
        public ArrayList<String> effectOrder;
        public List<AutomationManager.AutomationData> automations;
        
        public PresetData(String name, JSONObject effectData, ArrayList<String> effectOrder) {
            this.name = name;
            this.createdBy = "ToneForge User";
            this.createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            this.version = PRESET_VERSION;
            this.effectData = effectData;
            this.effectOrder = effectOrder;
            this.automations = new ArrayList<>();
        }
        
        public PresetData(String name, JSONObject effectData, ArrayList<String> effectOrder, List<AutomationManager.AutomationData> automations) {
            this.name = name;
            this.createdBy = "ToneForge User";
            this.createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            this.version = PRESET_VERSION;
            this.effectData = effectData;
            this.effectOrder = effectOrder;
            this.automations = automations != null ? automations : new ArrayList<>();
        }
    }
    
    public static String exportPreset(Context context, String presetName) {
        try {
            // Obter dados do preset atual
            SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> presetSet = prefs.getStringSet("effect_presets", new HashSet<>());
            
            // Encontrar o preset pelo nome
            JSONObject presetJson = null;
            for (String jsonStr : presetSet) {
                try {
                    JSONObject obj = new JSONObject(jsonStr);
                    if (obj.getString("name").equals(presetName)) {
                        presetJson = obj;
                        break;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing preset JSON", e);
                }
            }
            
            if (presetJson == null) {
                return null;
            }
            
            // Criar dados do preset para exportação
            ArrayList<String> effectOrder = new ArrayList<>();
            if (presetJson.has("order")) {
                JSONArray orderArray = presetJson.getJSONArray("order");
                for (int i = 0; i < orderArray.length(); i++) {
                    effectOrder.add(orderArray.getString(i));
                }
            }
            
            // Obter automações do preset
            List<AutomationManager.AutomationData> automations = new ArrayList<>();
            AutomationManager automationManager = AutomationManager.getInstance(context);
            List<AutomationManager.AutomationData> presetAutomations = automationManager.getAutomationsForPreset(presetName);
            if (presetAutomations != null) {
                automations.addAll(presetAutomations);
            }
            
            PresetData presetData = new PresetData(presetName, presetJson, effectOrder, automations);
            
            // Converter para JSON de exportação
            JSONObject exportJson = new JSONObject();
            exportJson.put("name", presetData.name);
            exportJson.put("createdBy", presetData.createdBy);
            exportJson.put("createdDate", presetData.createdDate);
            exportJson.put("version", presetData.version);
            exportJson.put("effectData", presetData.effectData);
            exportJson.put("effectOrder", new JSONArray(presetData.effectOrder));
            
            // Adicionar automações ao JSON de exportação
            JSONArray automationsArray = new JSONArray();
            for (AutomationManager.AutomationData automation : presetData.automations) {
                automationsArray.put(automation.toJson());
            }
            exportJson.put("automations", automationsArray);
            
            return exportJson.toString(2); // Pretty print com indentação
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating export JSON", e);
            return null;
        }
    }
    
    public static boolean importPreset(Context context, Uri fileUri) {
        try {
            // Ler arquivo JSON
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                return false;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            inputStream.close();
            
            // Parse do JSON
            JSONObject importJson = new JSONObject(stringBuilder.toString());
            
            // Validar formato
            if (!importJson.has("name") || !importJson.has("effectData")) {
                return false;
            }
            
            String presetName = importJson.getString("name");
            JSONObject effectData = importJson.getJSONObject("effectData");
            
            // Verificar se preset já existe
            SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> presetSet = new HashSet<>(prefs.getStringSet("effect_presets", new HashSet<>()));
            
            // Remover preset existente com mesmo nome
            Set<String> newPresetSet = new HashSet<>();
            for (String jsonStr : presetSet) {
                try {
                    JSONObject obj = new JSONObject(jsonStr);
                    if (!obj.getString("name").equals(presetName)) {
                        newPresetSet.add(jsonStr);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing existing preset", e);
                }
            }
            
            // Adicionar novo preset
            newPresetSet.add(effectData.toString());
            prefs.edit().putStringSet("effect_presets", newPresetSet).apply();
            
            // Salvar ordem dos efeitos se existir
            if (importJson.has("effectOrder")) {
                JSONArray orderArray = importJson.getJSONArray("effectOrder");
                ArrayList<String> effectOrder = new ArrayList<>();
                for (int i = 0; i < orderArray.length(); i++) {
                    effectOrder.add(orderArray.getString(i));
                }
                
                // Salvar ordem no preset
                effectData.put("order", orderArray);
                newPresetSet.remove(effectData.toString());
                newPresetSet.add(effectData.toString());
                prefs.edit().putStringSet("effect_presets", newPresetSet).apply();
            }
            
            // Importar automações se existirem
            if (importJson.has("automations")) {
                JSONArray automationsArray = importJson.getJSONArray("automations");
                AutomationManager automationManager = AutomationManager.getInstance(context);
                
                for (int i = 0; i < automationsArray.length(); i++) {
                    try {
                        JSONObject automationJson = automationsArray.getJSONObject(i);
                        AutomationManager.AutomationData automation = AutomationManager.AutomationData.fromJson(automationJson);
                        
                        // Garantir que o nome do preset está correto
                        automation.presetName = presetName;
                        
                        // Salvar a automação
                        automationManager.saveAutomation(automation);
                        
                    } catch (JSONException e) {
                        Log.e(TAG, "Error importing automation " + i, e);
                    }
                }
            }
            
            return true;
            
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error importing preset", e);
            return false;
        }
    }
    
    public static boolean savePresetToFile(Context context, String presetName, Uri fileUri) {
        try {
            String presetJson = exportPreset(context, presetName);
            if (presetJson == null) {
                return false;
            }
            
            OutputStream outputStream = context.getContentResolver().openOutputStream(fileUri);
            if (outputStream == null) {
                return false;
            }
            
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(presetJson);
            writer.close();
            outputStream.close();
            
            return true;
            
        } catch (IOException e) {
            Log.e(TAG, "Error saving preset to file", e);
            return false;
        }
    }
    
    public static ArrayList<String> getPresetNames(Context context) {
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> presetSet = prefs.getStringSet("effect_presets", new HashSet<>());
        ArrayList<String> names = new ArrayList<>();
        
        for (String jsonStr : presetSet) {
            try {
                JSONObject obj = new JSONObject(jsonStr);
                names.add(obj.getString("name"));
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing preset name", e);
            }
        }
        
        return names;
    }
    
    // Novos métodos para gerenciar automações
    
    /**
     * Exporta uma automação específica para JSON
     */
    public static String exportAutomation(Context context, String presetName, String automationName) {
        try {
            AutomationManager automationManager = AutomationManager.getInstance(context);
            List<AutomationManager.AutomationData> automations = automationManager.getAutomationsForPreset(presetName);
            
            if (automations != null) {
                for (AutomationManager.AutomationData automation : automations) {
                    if (automation.automationName.equals(automationName)) {
                        JSONObject exportJson = new JSONObject();
                        exportJson.put("presetName", automation.presetName);
                        exportJson.put("automationName", automation.automationName);
                        exportJson.put("automationData", automation.toJson());
                        exportJson.put("exportDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
                        exportJson.put("version", "1.0");
                        
                        return exportJson.toString(2);
                    }
                }
            }
            
            return null;
            
        } catch (JSONException e) {
            Log.e(TAG, "Error exporting automation", e);
            return null;
        }
    }
    
    /**
     * Importa uma automação de um arquivo JSON
     */
    public static boolean importAutomation(Context context, Uri fileUri) {
        try {
            // Ler arquivo JSON
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                return false;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            inputStream.close();
            
            // Parse do JSON
            JSONObject importJson = new JSONObject(stringBuilder.toString());
            
            // Validar formato
            if (!importJson.has("automationData")) {
                return false;
            }
            
            JSONObject automationDataJson = importJson.getJSONObject("automationData");
            AutomationManager.AutomationData automation = AutomationManager.AutomationData.fromJson(automationDataJson);
            
            // Salvar a automação
            AutomationManager automationManager = AutomationManager.getInstance(context);
            automationManager.saveAutomation(automation);
            
            return true;
            
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error importing automation", e);
            return false;
        }
    }
    
    /**
     * Salva uma automação em arquivo
     */
    public static boolean saveAutomationToFile(Context context, String presetName, String automationName, Uri fileUri) {
        try {
            String automationJson = exportAutomation(context, presetName, automationName);
            if (automationJson == null) {
                return false;
            }
            
            OutputStream outputStream = context.getContentResolver().openOutputStream(fileUri);
            if (outputStream == null) {
                return false;
            }
            
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(automationJson);
            writer.close();
            outputStream.close();
            
            return true;
            
        } catch (IOException e) {
            Log.e(TAG, "Error saving automation to file", e);
            return false;
        }
    }
    
    /**
     * Obtém lista de automações para um preset
     */
    public static ArrayList<String> getAutomationNames(Context context, String presetName) {
        AutomationManager automationManager = AutomationManager.getInstance(context);
        List<AutomationManager.AutomationData> automations = automationManager.getAutomationsForPreset(presetName);
        
        ArrayList<String> names = new ArrayList<>();
        if (automations != null) {
            for (AutomationManager.AutomationData automation : automations) {
                names.add(automation.automationName);
            }
        }
        
        return names;
    }
} 