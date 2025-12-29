package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Gerenciador centralizado de logs para controlar verbosidade
 * e evitar spam no logcat
 */
public class LogManager {
    private static final String TAG = "LogManager";
    private static final String PREFS_NAME = "LogPrefs";
    private static final String KEY_LOG_LEVEL = "log_level";
    private static final String KEY_VERBOSE_LOGGING = "verbose_logging";
    
    // Níveis de log
    public static final int LEVEL_ERROR = 0;
    public static final int LEVEL_WARN = 1;
    public static final int LEVEL_INFO = 2;
    public static final int LEVEL_DEBUG = 3;
    public static final int LEVEL_VERBOSE = 4;
    
    private static LogManager instance;
    private SharedPreferences prefs;
    private int currentLogLevel = LEVEL_INFO; // Padrão: apenas INFO e acima
    private boolean verboseLogging = false;
    private boolean isInitialized = false;
    
    private LogManager(Context context) {
        try {
            if (context != null) {
                prefs = context.getApplicationContext()
                              .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                loadSettings();
                isInitialized = true;
            } else {
                Log.w(TAG, "Contexto nulo - usando configurações padrão");
                isInitialized = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar LogManager: " + e.getMessage());
            isInitialized = false;
        }
    }
    
    public static synchronized LogManager getInstance(Context context) {
        if (instance == null) {
            instance = new LogManager(context);
        }
        return instance;
    }
    
    private void loadSettings() {
        try {
            if (prefs != null) {
                currentLogLevel = prefs.getInt(KEY_LOG_LEVEL, LEVEL_INFO);
                verboseLogging = prefs.getBoolean(KEY_VERBOSE_LOGGING, false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar configurações: " + e.getMessage());
            // Usar valores padrão em caso de erro
            currentLogLevel = LEVEL_INFO;
            verboseLogging = false;
        }
    }
    
    public void setLogLevel(int level) {
        this.currentLogLevel = level;
        try {
            if (prefs != null) {
                prefs.edit().putInt(KEY_LOG_LEVEL, level).apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar nível de log: " + e.getMessage());
        }
    }
    
    public void setVerboseLogging(boolean enabled) {
        this.verboseLogging = enabled;
        try {
            if (prefs != null) {
                prefs.edit().putBoolean(KEY_VERBOSE_LOGGING, enabled).apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar logging verboso: " + e.getMessage());
        }
    }
    
    public int getLogLevel() {
        return currentLogLevel;
    }
    
    public boolean isVerboseLogging() {
        return verboseLogging;
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    // Métodos de log condicionais
    public static void v(String tag, String message) {
        if (instance != null && instance.currentLogLevel >= LEVEL_VERBOSE) {
            Log.v(tag, message);
        }
    }
    
    public static void d(String tag, String message) {
        if (instance != null && instance.currentLogLevel >= LEVEL_DEBUG) {
            Log.d(tag, message);
        }
    }
    
    public static void i(String tag, String message) {
        if (instance != null && instance.currentLogLevel >= LEVEL_INFO) {
            Log.i(tag, message);
        }
    }
    
    public static void w(String tag, String message) {
        if (instance != null && instance.currentLogLevel >= LEVEL_WARN) {
            Log.w(tag, message);
        }
    }
    
    public static void e(String tag, String message) {
        if (instance != null && instance.currentLogLevel >= LEVEL_ERROR) {
            Log.e(tag, message);
        }
    }
    
    public static void e(String tag, String message, Throwable tr) {
        if (instance != null && instance.currentLogLevel >= LEVEL_ERROR) {
            Log.e(tag, message, tr);
        }
    }
    
    // Logs verbosos (só aparecem se verboseLogging = true)
    public static void verbose(String tag, String message) {
        if (instance != null && instance.verboseLogging) {
            Log.d(tag, "[VERBOSE] " + message);
        }
    }
    
    // Logs de performance (sempre aparecem)
    public static void perf(String tag, String message) {
        Log.i(tag, "[PERF] " + message);
    }
    
    // Logs de erro crítico (sempre aparecem)
    public static void critical(String tag, String message) {
        Log.e(tag, "[CRITICAL] " + message);
    }
    
    public static void critical(String tag, String message, Throwable tr) {
        Log.e(tag, "[CRITICAL] " + message, tr);
    }
} 