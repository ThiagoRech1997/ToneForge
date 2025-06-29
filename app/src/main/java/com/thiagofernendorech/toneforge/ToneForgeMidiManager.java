package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToneForgeMidiManager {
    private static final String TAG = "ToneForgeMidiManager";
    private static final String PREFS_NAME = "midi_prefs";
    private static final String KEY_MIDI_ENABLED = "midi_enabled";
    private static final String KEY_MAPPINGS = "midi_mappings";
    private static final String KEY_LEARN_MODE = "learn_mode";
    
    // Tipos de mensagens MIDI
    public static final int MIDI_CC = 0xB0;      // Control Change
    public static final int MIDI_NOTE = 0x90;    // Note On/Off
    public static final int MIDI_PITCH_BEND = 0xE0; // Pitch Bend
    
    // Estados do MIDI Learn
    public static final int LEARN_MODE_OFF = 0;
    public static final int LEARN_MODE_WAITING = 1;
    public static final int LEARN_MODE_ACTIVE = 2;
    
    private static ToneForgeMidiManager instance;
    private Context context;
    private android.media.midi.MidiManager systemMidiManager;
    private SharedPreferences prefs;
    private Handler mainHandler;
    
    // Dispositivos MIDI
    private List<MidiDeviceInfo> availableDevices;
    private MidiDevice currentDevice;
    private MidiInputPort inputPort;
    
    // Mapeamentos MIDI
    private Map<String, MidiMapping> midiMappings;
    private int learnMode = LEARN_MODE_OFF;
    private String pendingParameter = null;
    
    // Callbacks
    private MidiLearnListener learnListener;
    private MidiControlListener controlListener;
    
    public interface MidiLearnListener {
        void onLearnModeChanged(int mode);
        void onMidiMessageReceived(int type, int channel, int data1, int data2);
        void onMappingCreated(String parameter, MidiMapping mapping);
        void onMappingRemoved(String parameter);
    }
    
    public interface MidiControlListener {
        void onParameterChanged(String parameter, float value);
    }
    
    public static class MidiMapping {
        public int messageType;
        public int channel;
        public int controller;
        public float minValue;
        public float maxValue;
        public String curve; // linear, exponential, logarithmic
        public boolean inverted;
        
        public MidiMapping(int messageType, int channel, int controller, 
                          float minValue, float maxValue, String curve, boolean inverted) {
            this.messageType = messageType;
            this.channel = channel;
            this.controller = controller;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.curve = curve;
            this.inverted = inverted;
        }
        
        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("messageType", messageType);
            json.put("channel", channel);
            json.put("controller", controller);
            json.put("minValue", minValue);
            json.put("maxValue", maxValue);
            json.put("curve", curve);
            json.put("inverted", inverted);
            return json;
        }
        
        public static MidiMapping fromJson(JSONObject json) throws JSONException {
            return new MidiMapping(
                json.getInt("messageType"),
                json.getInt("channel"),
                json.getInt("controller"),
                (float) json.getDouble("minValue"),
                (float) json.getDouble("maxValue"),
                json.getString("curve"),
                json.getBoolean("inverted")
            );
        }
    }
    
    private ToneForgeMidiManager(Context context) {
        this.context = context.getApplicationContext();
        this.systemMidiManager = (android.media.midi.MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.midiMappings = new HashMap<>();
        
        loadMappings();
        scanMidiDevices();
    }
    
    public static synchronized ToneForgeMidiManager getInstance(Context context) {
        if (instance == null) {
            instance = new ToneForgeMidiManager(context);
        }
        return instance;
    }
    
    /**
     * Escaneia dispositivos MIDI disponíveis
     */
    public void scanMidiDevices() {
        if (systemMidiManager != null) {
            availableDevices = new ArrayList<>();
            MidiDeviceInfo[] devices = systemMidiManager.getDevices();
            
            for (MidiDeviceInfo device : devices) {
                if (device.getInputPortCount() > 0) {
                    availableDevices.add(device);
                    Log.d(TAG, "Dispositivo MIDI encontrado: " + device.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME));
                }
            }
            
            Log.d(TAG, "Encontrados " + availableDevices.size() + " dispositivos MIDI");
        }
    }
    
    /**
     * Conecta ao primeiro dispositivo MIDI disponível
     */
    public boolean connectToFirstDevice() {
        if (availableDevices == null || availableDevices.isEmpty()) {
            Log.w(TAG, "Nenhum dispositivo MIDI disponível");
            return false;
        }
        
        return connectToDevice(availableDevices.get(0));
    }
    
    /**
     * Conecta a um dispositivo MIDI específico
     */
    public boolean connectToDevice(MidiDeviceInfo deviceInfo) {
        try {
            systemMidiManager.openDevice(deviceInfo, new android.media.midi.MidiManager.OnDeviceOpenedListener() {
                @Override
                public void onDeviceOpened(MidiDevice device) {
                    if (device != null) {
                        currentDevice = device;
                        setupMidiInput(device);
                        Log.d(TAG, "Conectado ao dispositivo MIDI: " + deviceInfo.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME));
                    } else {
                        Log.e(TAG, "Falha ao abrir dispositivo MIDI");
                    }
                }
            }, mainHandler);
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao conectar dispositivo MIDI: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Configura a entrada MIDI
     */
    private void setupMidiInput(MidiDevice device) {
        inputPort = device.openInputPort(0);
        Log.d(TAG, "Entrada MIDI configurada");
        // Para receber mensagens MIDI, precisamos implementar um MidiReceiver
        // que será chamado quando mensagens chegarem
        // Por enquanto, vamos apenas logar que a porta foi aberta
    }
    
    /**
     * Processa mensagens MIDI recebidas
     */
    private void processMidiMessage(byte[] data, int offset, int count) {
        if (count < 2) return;
        
        int messageType = data[offset] & 0xF0;
        int channel = data[offset] & 0x0F;
        int data1 = data[offset + 1] & 0xFF;
        int data2 = count > 2 ? data[offset + 2] & 0xFF : 0;
        
        Log.d(TAG, String.format("MIDI: %02X %02X %02X", messageType, data1, data2));
        
        // Notificar listener de aprendizado
        if (learnListener != null) {
            mainHandler.post(() -> learnListener.onMidiMessageReceived(messageType, channel, data1, data2));
        }
        
        // Processar mapeamentos ativos
        processMidiMapping(messageType, channel, data1, data2);
    }
    
    /**
     * Processa mapeamentos MIDI ativos
     */
    private void processMidiMapping(int messageType, int channel, int data1, int data2) {
        for (Map.Entry<String, MidiMapping> entry : midiMappings.entrySet()) {
            MidiMapping mapping = entry.getValue();
            
            if (mapping.messageType == messageType && 
                mapping.channel == channel && 
                mapping.controller == data1) {
                
                float normalizedValue = data2 / 127.0f;
                float mappedValue = mapValue(normalizedValue, mapping);
                
                // Notificar listener de controle
                if (controlListener != null) {
                    mainHandler.post(() -> controlListener.onParameterChanged(entry.getKey(), mappedValue));
                }
                
                Log.d(TAG, "Parâmetro " + entry.getKey() + " alterado para " + mappedValue);
            }
        }
    }
    
    /**
     * Mapeia valor MIDI (0-1) para range do parâmetro
     */
    private float mapValue(float midiValue, MidiMapping mapping) {
        float value = midiValue;
        
        // Aplicar inversão
        if (mapping.inverted) {
            value = 1.0f - value;
        }
        
        // Aplicar curva
        switch (mapping.curve) {
            case "exponential":
                value = value * value;
                break;
            case "logarithmic":
                value = (float) Math.log(1 + value * 9) / (float) Math.log(10);
                break;
            default: // linear
                break;
        }
        
        // Mapear para range do parâmetro
        return mapping.minValue + (mapping.maxValue - mapping.minValue) * value;
    }
    
    /**
     * Ativa modo de aprendizado para um parâmetro
     */
    public void startLearnMode(String parameter) {
        learnMode = LEARN_MODE_WAITING;
        pendingParameter = parameter;
        
        if (learnListener != null) {
            learnListener.onLearnModeChanged(learnMode);
        }
        
        Log.d(TAG, "Modo de aprendizado ativado para: " + parameter);
    }
    
    /**
     * Cria mapeamento MIDI
     */
    public void createMapping(String parameter, int messageType, int channel, int controller) {
        MidiMapping mapping = new MidiMapping(messageType, channel, controller, 0.0f, 1.0f, "linear", false);
        midiMappings.put(parameter, mapping);
        
        learnMode = LEARN_MODE_OFF;
        pendingParameter = null;
        
        saveMappings();
        
        if (learnListener != null) {
            learnListener.onMappingCreated(parameter, mapping);
            learnListener.onLearnModeChanged(learnMode);
        }
        
        Log.d(TAG, "Mapeamento criado: " + parameter + " -> CC" + controller);
    }
    
    /**
     * Remove mapeamento MIDI
     */
    public void removeMapping(String parameter) {
        midiMappings.remove(parameter);
        saveMappings();
        
        if (learnListener != null) {
            learnListener.onMappingRemoved(parameter);
        }
        
        Log.d(TAG, "Mapeamento removido: " + parameter);
    }
    
    /**
     * Salva mapeamentos no SharedPreferences
     */
    private void saveMappings() {
        try {
            JSONObject mappingsJson = new JSONObject();
            for (Map.Entry<String, MidiMapping> entry : midiMappings.entrySet()) {
                mappingsJson.put(entry.getKey(), entry.getValue().toJson());
            }
            
            prefs.edit()
                .putString(KEY_MAPPINGS, mappingsJson.toString())
                .apply();
                
            Log.d(TAG, "Mapeamentos MIDI salvos");
        } catch (JSONException e) {
            Log.e(TAG, "Erro ao salvar mapeamentos MIDI: " + e.getMessage());
        }
    }
    
    /**
     * Carrega mapeamentos do SharedPreferences
     */
    private void loadMappings() {
        try {
            String mappingsStr = prefs.getString(KEY_MAPPINGS, "{}");
            JSONObject mappingsJson = new JSONObject(mappingsStr);
            
            midiMappings.clear();
            JSONArray keys = mappingsJson.names();
            if (keys != null) {
                for (int i = 0; i < keys.length(); i++) {
                    String key = keys.getString(i);
                    JSONObject mappingJson = mappingsJson.getJSONObject(key);
                    midiMappings.put(key, MidiMapping.fromJson(mappingJson));
                }
            }
            
            Log.d(TAG, "Mapeamentos MIDI carregados: " + midiMappings.size());
        } catch (JSONException e) {
            Log.e(TAG, "Erro ao carregar mapeamentos MIDI: " + e.getMessage());
        }
    }
    
    /**
     * Define listeners
     */
    public void setLearnListener(MidiLearnListener listener) {
        this.learnListener = listener;
    }
    
    public void setControlListener(MidiControlListener listener) {
        this.controlListener = listener;
    }
    
    /**
     * Getters
     */
    public List<MidiDeviceInfo> getAvailableDevices() {
        return availableDevices != null ? availableDevices : new ArrayList<>();
    }
    
    public MidiMapping getMapping(String parameter) {
        return midiMappings.get(parameter);
    }
    
    public Map<String, MidiMapping> getAllMappings() {
        return new HashMap<>(midiMappings);
    }
    
    public int getLearnMode() {
        return learnMode;
    }
    
    public String getPendingParameter() {
        return pendingParameter;
    }
    
    public boolean isMidiEnabled() {
        return prefs.getBoolean(KEY_MIDI_ENABLED, false);
    }
    
    public void setMidiEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MIDI_ENABLED, enabled).apply();
        
        if (enabled) {
            connectToFirstDevice();
        } else {
            disconnect();
        }
    }
    
    /**
     * Desconecta do dispositivo MIDI
     */
    public void disconnect() {
        if (inputPort != null) {
            try {
                inputPort.close();
            } catch (IOException e) {
                Log.e(TAG, "Erro ao fechar porta MIDI: " + e.getMessage());
            }
            inputPort = null;
        }
        
        if (currentDevice != null) {
            try {
                currentDevice.close();
            } catch (IOException e) {
                Log.e(TAG, "Erro ao fechar dispositivo MIDI: " + e.getMessage());
            }
            currentDevice = null;
        }
        
        Log.d(TAG, "Dispositivo MIDI desconectado");
    }
    
    /**
     * Limpa recursos
     */
    public void cleanup() {
        disconnect();
        if (instance != null) {
            instance = null;
        }
    }
} 