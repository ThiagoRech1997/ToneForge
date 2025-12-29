package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SmartPresetManager {
    private static final String TAG = "SmartPresetManager";
    private static final String PREFS_NAME = "smart_presets";
    private static final String KEY_LEARNING_DATA = "learning_data";
    private static final String KEY_STYLE_PATTERNS = "style_patterns";
    
    private Context context;
    private SharedPreferences prefs;
    private AudioAnalyzer audioAnalyzer;
    
    // Dados de aprendizado
    private Map<String, StylePattern> stylePatterns;
    private List<AudioAnalysis> learningData;
    
    // Configurações de análise
    private static final int ANALYSIS_WINDOW_MS = 5000; // 5 segundos
    private static final float CONFIDENCE_THRESHOLD = 0.7f;
    
    public static class StylePattern {
        public String style;
        public float avgLowFreq;
        public float avgMidFreq;
        public float avgHighFreq;
        public float avgRMS;
        public float avgPeak;
        public float tempoRange;
        public Map<String, Float> effectPreferences;
        
        public StylePattern(String style) {
            this.style = style;
            this.effectPreferences = new HashMap<>();
        }
        
        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("style", style);
            json.put("avgLowFreq", avgLowFreq);
            json.put("avgMidFreq", avgMidFreq);
            json.put("avgHighFreq", avgHighFreq);
            json.put("avgRMS", avgRMS);
            json.put("avgPeak", avgPeak);
            json.put("tempoRange", tempoRange);
            
            JSONObject effects = new JSONObject();
            for (Map.Entry<String, Float> entry : effectPreferences.entrySet()) {
                effects.put(entry.getKey(), entry.getValue());
            }
            json.put("effectPreferences", effects);
            
            return json;
        }
        
        public static StylePattern fromJson(JSONObject json) throws JSONException {
            String style = json.getString("style");
            StylePattern pattern = new StylePattern(style);
            
            pattern.avgLowFreq = (float) json.getDouble("avgLowFreq");
            pattern.avgMidFreq = (float) json.getDouble("avgMidFreq");
            pattern.avgHighFreq = (float) json.getDouble("avgHighFreq");
            pattern.avgRMS = (float) json.getDouble("avgRMS");
            pattern.avgPeak = (float) json.getDouble("avgPeak");
            pattern.tempoRange = (float) json.getDouble("tempoRange");
            
            JSONObject effects = json.getJSONObject("effectPreferences");
            Iterator<String> keys = effects.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                pattern.effectPreferences.put(key, (float) effects.getDouble(key));
            }
            
            return pattern;
        }
    }
    
    public static class AudioAnalysis {
        public float[] frequencyBands;
        public float rms;
        public float peak;
        public float dominantFreq;
        public String style;
        public long timestamp;
        
        public AudioAnalysis(float[] bands, float rms, float peak, float freq) {
            this.frequencyBands = bands.clone();
            this.rms = rms;
            this.peak = peak;
            this.dominantFreq = freq;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    public static class PresetSuggestion {
        public String style;
        public float confidence;
        public Map<String, Float> recommendedSettings;
        public String description;
        
        public PresetSuggestion(String style, float confidence) {
            this.style = style;
            this.confidence = confidence;
            this.recommendedSettings = new HashMap<>();
        }
    }
    
    public interface SmartPresetCallback {
        void onStyleDetected(String style, float confidence);
        void onPresetSuggested(PresetSuggestion suggestion);
        void onLearningProgress(float progress);
    }
    
    public SmartPresetManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.stylePatterns = new HashMap<>();
        this.learningData = new ArrayList<>();
        
        loadLearningData();
        initializeDefaultPatterns();
    }
    
    private void initializeDefaultPatterns() {
        // Padrões padrão para diferentes estilos musicais
        
        // Rock
        StylePattern rock = new StylePattern("Rock");
        rock.avgLowFreq = 0.6f;
        rock.avgMidFreq = 0.8f;
        rock.avgHighFreq = 0.4f;
        rock.avgRMS = 0.7f;
        rock.avgPeak = 0.9f;
        rock.tempoRange = 120.0f;
        rock.effectPreferences.put("distortion", 0.7f);
        rock.effectPreferences.put("gain", 0.8f);
        rock.effectPreferences.put("reverb", 0.3f);
        rock.effectPreferences.put("delay", 0.4f);
        stylePatterns.put("Rock", rock);
        
        // Jazz
        StylePattern jazz = new StylePattern("Jazz");
        jazz.avgLowFreq = 0.4f;
        jazz.avgMidFreq = 0.6f;
        jazz.avgHighFreq = 0.5f;
        jazz.avgRMS = 0.5f;
        jazz.avgPeak = 0.7f;
        jazz.tempoRange = 90.0f;
        jazz.effectPreferences.put("reverb", 0.6f);
        jazz.effectPreferences.put("chorus", 0.4f);
        jazz.effectPreferences.put("compressor", 0.5f);
        jazz.effectPreferences.put("gain", 0.6f);
        stylePatterns.put("Jazz", jazz);
        
        // Blues
        StylePattern blues = new StylePattern("Blues");
        blues.avgLowFreq = 0.5f;
        blues.avgMidFreq = 0.7f;
        blues.avgHighFreq = 0.3f;
        blues.avgRMS = 0.6f;
        blues.avgPeak = 0.8f;
        blues.tempoRange = 80.0f;
        blues.effectPreferences.put("distortion", 0.5f);
        blues.effectPreferences.put("reverb", 0.4f);
        blues.effectPreferences.put("delay", 0.3f);
        blues.effectPreferences.put("gain", 0.7f);
        stylePatterns.put("Blues", blues);
        
        // Metal
        StylePattern metal = new StylePattern("Metal");
        metal.avgLowFreq = 0.8f;
        metal.avgMidFreq = 0.6f;
        metal.avgHighFreq = 0.7f;
        metal.avgRMS = 0.8f;
        metal.avgPeak = 0.95f;
        metal.tempoRange = 140.0f;
        metal.effectPreferences.put("distortion", 0.9f);
        metal.effectPreferences.put("gain", 0.9f);
        metal.effectPreferences.put("compressor", 0.7f);
        metal.effectPreferences.put("reverb", 0.2f);
        stylePatterns.put("Metal", metal);
        
        // Acoustic
        StylePattern acoustic = new StylePattern("Acoustic");
        acoustic.avgLowFreq = 0.3f;
        acoustic.avgMidFreq = 0.5f;
        acoustic.avgHighFreq = 0.6f;
        acoustic.avgRMS = 0.4f;
        acoustic.avgPeak = 0.6f;
        acoustic.tempoRange = 100.0f;
        acoustic.effectPreferences.put("reverb", 0.4f);
        acoustic.effectPreferences.put("compressor", 0.3f);
        acoustic.effectPreferences.put("gain", 0.5f);
        acoustic.effectPreferences.put("eq", 0.4f);
        stylePatterns.put("Acoustic", acoustic);
    }
    
    public void startLearning(AudioAnalyzer analyzer) {
        this.audioAnalyzer = analyzer;
        audioAnalyzer.setCallback(new AudioAnalyzer.AudioAnalyzerCallback() {
            @Override
            public void onSpectrumData(float[] spectrum) {}
            
            @Override
            public void onWaveformData(float[] waveform) {}
            
            @Override
            public void onVUMeterData(float rms, float peak, float average) {}
            
            @Override
            public void onClippingDetected(boolean clipping) {}
            
            @Override
            public void onFrequencyAnalysis(float dominantFreq, float[] bands) {
                collectLearningData(bands, audioAnalyzer.getCurrentRMS(), 
                                  audioAnalyzer.getPeakLevel(), dominantFreq);
            }
        });
    }
    
    private void collectLearningData(float[] bands, float rms, float peak, float freq) {
        AudioAnalysis analysis = new AudioAnalysis(bands, rms, peak, freq);
        learningData.add(analysis);
        
        // Limitar dados de aprendizado
        if (learningData.size() > 1000) {
            learningData.remove(0);
        }
        
        // Salvar periodicamente
        if (learningData.size() % 100 == 0) {
            saveLearningData();
        }
    }
    
    public void teachStyle(String style, long startTime, long endTime) {
        List<AudioAnalysis> relevantData = new ArrayList<>();
        
        for (AudioAnalysis analysis : learningData) {
            if (analysis.timestamp >= startTime && analysis.timestamp <= endTime) {
                analysis.style = style;
                relevantData.add(analysis);
            }
        }
        
        if (relevantData.size() > 10) {
            updateStylePattern(style, relevantData);
            saveLearningData();
            Log.d(TAG, "Estilo '" + style + "' aprendido com " + relevantData.size() + " amostras");
        }
    }
    
    private void updateStylePattern(String style, List<AudioAnalysis> data) {
        StylePattern pattern = stylePatterns.get(style);
        if (pattern == null) {
            pattern = new StylePattern(style);
            stylePatterns.put(style, pattern);
        }
        
        // Calcular médias
        float sumLow = 0, sumMid = 0, sumHigh = 0, sumRMS = 0, sumPeak = 0;
        
        for (AudioAnalysis analysis : data) {
            sumLow += analysis.frequencyBands[0] + analysis.frequencyBands[1];
            sumMid += analysis.frequencyBands[2] + analysis.frequencyBands[3] + analysis.frequencyBands[4];
            sumHigh += analysis.frequencyBands[5] + analysis.frequencyBands[6] + analysis.frequencyBands[7];
            sumRMS += analysis.rms;
            sumPeak += analysis.peak;
        }
        
        int count = data.size();
        pattern.avgLowFreq = sumLow / (count * 2);
        pattern.avgMidFreq = sumMid / (count * 3);
        pattern.avgHighFreq = sumHigh / (count * 3);
        pattern.avgRMS = sumRMS / count;
        pattern.avgPeak = sumPeak / count;
    }
    
    public PresetSuggestion analyzeAndSuggest(float[] currentBands, float currentRMS, 
                                            float currentPeak, float currentFreq) {
        String bestStyle = null;
        float bestConfidence = 0.0f;
        
        for (Map.Entry<String, StylePattern> entry : stylePatterns.entrySet()) {
            StylePattern pattern = entry.getValue();
            float confidence = calculateConfidence(pattern, currentBands, currentRMS, currentPeak, currentFreq);
            
            if (confidence > bestConfidence) {
                bestConfidence = confidence;
                bestStyle = entry.getKey();
            }
        }
        
        if (bestConfidence > CONFIDENCE_THRESHOLD && bestStyle != null) {
            return createPresetSuggestion(bestStyle, bestConfidence);
        }
        
        return null;
    }
    
    private float calculateConfidence(StylePattern pattern, float[] bands, float rms, float peak, float freq) {
        // Calcular similaridade para cada característica
        float lowFreqSimilarity = 1.0f - Math.abs(pattern.avgLowFreq - (bands[0] + bands[1]) / 2.0f);
        float midFreqSimilarity = 1.0f - Math.abs(pattern.avgMidFreq - (bands[2] + bands[3] + bands[4]) / 3.0f);
        float highFreqSimilarity = 1.0f - Math.abs(pattern.avgHighFreq - (bands[5] + bands[6] + bands[7]) / 3.0f);
        float rmsSimilarity = 1.0f - Math.abs(pattern.avgRMS - rms);
        float peakSimilarity = 1.0f - Math.abs(pattern.avgPeak - peak);
        
        // Média ponderada
        return (lowFreqSimilarity * 0.2f + midFreqSimilarity * 0.3f + 
                highFreqSimilarity * 0.2f + rmsSimilarity * 0.15f + peakSimilarity * 0.15f);
    }
    
    private PresetSuggestion createPresetSuggestion(String style, float confidence) {
        StylePattern pattern = stylePatterns.get(style);
        PresetSuggestion suggestion = new PresetSuggestion(style, confidence);
        
        // Copiar configurações recomendadas
        suggestion.recommendedSettings.putAll(pattern.effectPreferences);
        
        // Gerar descrição
        suggestion.description = String.format("Estilo %s detectado (%.1f%% confiança)", 
                                             style, confidence * 100);
        
        return suggestion;
    }
    
    public void applyPresetSuggestion(PresetSuggestion suggestion) {
        if (suggestion == null || suggestion.recommendedSettings == null) {
            return;
        }
        
        // Aplicar configurações recomendadas
        for (Map.Entry<String, Float> entry : suggestion.recommendedSettings.entrySet()) {
            String effect = entry.getKey();
            float value = entry.getValue();
            
            switch (effect) {
                case "gain":
                    AudioEngine.setGainEnabled(true);
                    AudioEngine.setGainLevel(value);
                    break;
                case "distortion":
                    AudioEngine.setDistortionEnabled(true);
                    AudioEngine.setDistortionLevel(value);
                    break;
                case "reverb":
                    AudioEngine.setReverbEnabled(true);
                    AudioEngine.setReverbLevel(value);
                    break;
                case "delay":
                    AudioEngine.setDelayEnabled(true);
                    AudioEngine.setDelayLevel(value);
                    break;
                case "chorus":
                    AudioEngine.setChorusEnabled(true);
                    AudioEngine.setChorusMix(value);
                    break;
                case "compressor":
                    AudioEngine.setCompressorEnabled(true);
                    AudioEngine.setCompressorMix(value);
                    break;
                case "eq":
                    AudioEngine.setEQEnabled(true);
                    AudioEngine.setEQLow(value * 0.8f);
                    AudioEngine.setEQMid(value);
                    AudioEngine.setEQHigh(value * 1.2f);
                    break;
            }
        }
        
        Log.d(TAG, "Preset aplicado: " + suggestion.style);
    }
    
    private void saveLearningData() {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            
            // Salvar dados de aprendizado
            JSONArray learningArray = new JSONArray();
            for (AudioAnalysis analysis : learningData) {
                JSONObject obj = new JSONObject();
                obj.put("rms", analysis.rms);
                obj.put("peak", analysis.peak);
                obj.put("dominantFreq", analysis.dominantFreq);
                obj.put("timestamp", analysis.timestamp);
                if (analysis.style != null) {
                    obj.put("style", analysis.style);
                }
                
                JSONArray bandsArray = new JSONArray();
                for (float band : analysis.frequencyBands) {
                    bandsArray.put(band);
                }
                obj.put("frequencyBands", bandsArray);
                
                learningArray.put(obj);
            }
            editor.putString(KEY_LEARNING_DATA, learningArray.toString());
            
            // Salvar padrões de estilo
            JSONObject patternsObject = new JSONObject();
            for (Map.Entry<String, StylePattern> entry : stylePatterns.entrySet()) {
                patternsObject.put(entry.getKey(), entry.getValue().toJson());
            }
            editor.putString(KEY_STYLE_PATTERNS, patternsObject.toString());
            
            editor.apply();
            Log.d(TAG, "Dados de aprendizado salvos");
            
        } catch (JSONException e) {
            Log.e(TAG, "Erro ao salvar dados de aprendizado: " + e.getMessage());
        }
    }
    
    private void loadLearningData() {
        try {
            // Carregar dados de aprendizado
            String learningDataStr = prefs.getString(KEY_LEARNING_DATA, "[]");
            JSONArray learningArray = new JSONArray(learningDataStr);
            
            learningData.clear();
            for (int i = 0; i < learningArray.length(); i++) {
                JSONObject obj = learningArray.getJSONObject(i);
                AudioAnalysis analysis = new AudioAnalysis(
                    jsonArrayToFloatArray(obj.getJSONArray("frequencyBands")),
                    (float) obj.getDouble("rms"),
                    (float) obj.getDouble("peak"),
                    (float) obj.getDouble("dominantFreq")
                );
                analysis.timestamp = obj.getLong("timestamp");
                if (obj.has("style")) {
                    analysis.style = obj.getString("style");
                }
                learningData.add(analysis);
            }
            
            // Carregar padrões de estilo
            String patternsStr = prefs.getString(KEY_STYLE_PATTERNS, "{}");
            JSONObject patternsObject = new JSONObject(patternsStr);
            
            stylePatterns.clear();
            Iterator<String> keys = patternsObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                StylePattern pattern = StylePattern.fromJson(patternsObject.getJSONObject(key));
                stylePatterns.put(key, pattern);
            }
            
            Log.d(TAG, "Dados de aprendizado carregados: " + learningData.size() + " amostras, " + 
                      stylePatterns.size() + " estilos");
            
        } catch (JSONException e) {
            Log.e(TAG, "Erro ao carregar dados de aprendizado: " + e.getMessage());
        }
    }
    
    private float[] jsonArrayToFloatArray(JSONArray array) throws JSONException {
        float[] result = new float[array.length()];
        for (int i = 0; i < array.length(); i++) {
            result[i] = (float) array.getDouble(i);
        }
        return result;
    }
    
    public List<String> getAvailableStyles() {
        return new ArrayList<>(stylePatterns.keySet());
    }
    
    public void clearLearningData() {
        learningData.clear();
        stylePatterns.clear();
        initializeDefaultPatterns();
        saveLearningData();
        Log.d(TAG, "Dados de aprendizado limpos");
    }
} 