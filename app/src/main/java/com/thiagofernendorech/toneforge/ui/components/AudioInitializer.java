package com.thiagofernendorech.toneforge.ui.components;

import android.content.Context;
import android.widget.Toast;
import com.thiagofernendorech.toneforge.LogManager;
import com.thiagofernendorech.toneforge.domain.usecases.StartAudioPipelineUseCase;
import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;
import com.thiagofernendorech.toneforge.domain.interfaces.PermissionInterface;
import com.thiagofernendorech.toneforge.infrastructure.adapters.AudioEngineAdapter;
import com.thiagofernendorech.toneforge.infrastructure.adapters.PermissionManagerAdapter;

/**
 * Inicializador de componentes de áudio seguindo Clean Architecture
 * Usa Use Cases para operações de domínio
 */
public class AudioInitializer {
    
    private static final String TAG = "AudioInitializer";
    
    private final Context context;
    private final AudioEngineInterface audioEngine;
    private final PermissionInterface permissionManager;
    private final StartAudioPipelineUseCase startPipelineUseCase;
    
    public AudioInitializer(Context context) {
        this.context = context;
        this.audioEngine = new AudioEngineAdapter();
        this.permissionManager = new PermissionManagerAdapter(context);
        this.startPipelineUseCase = new StartAudioPipelineUseCase(audioEngine, permissionManager);
    }
    
    /**
     * Inicializa componentes de áudio de forma segura
     */
    public void initializeAudioComponents() {
        LogManager.i(TAG, "Iniciando inicialização de componentes de áudio");
        
        try {
            // Verificar se biblioteca nativa está carregada
            checkNativeLibrary();
            
            // Não iniciar pipeline automaticamente - apenas sob demanda
            LogManager.i(TAG, "Componentes de áudio prontos (pipeline não iniciado)");
            
        } catch (Exception e) {
            LogManager.e(TAG, "Erro na inicialização de áudio", e);
            showErrorToUser("Erro na inicialização de áudio");
        }
    }
    
    /**
     * Inicia pipeline de áudio sob demanda
     */
    public void startAudioPipeline() {
        LogManager.i(TAG, "Solicitação para iniciar pipeline de áudio");
        
        StartAudioPipelineUseCase.Result result = startPipelineUseCase.execute();
        
        if (result.isSuccess()) {
            LogManager.i(TAG, "Pipeline iniciado: " + result.getMessage());
            showSuccessToUser("Pipeline de áudio iniciado!");
        } else {
            LogManager.w(TAG, "Falha ao iniciar pipeline: " + result.getMessage());
            showErrorToUser(result.getMessage());
        }
    }
    
    /**
     * Para pipeline de áudio
     */
    public void stopAudioPipeline() {
        try {
            audioEngine.stopPipeline();
            LogManager.i(TAG, "Pipeline de áudio parado");
        } catch (Exception e) {
            LogManager.e(TAG, "Erro ao parar pipeline", e);
        }
    }
    
    /**
     * Verifica se pipeline está rodando
     */
    public boolean isPipelineRunning() {
        return audioEngine.isPipelineRunning();
    }
    
    /**
     * Obtém interface do motor de áudio
     */
    public AudioEngineInterface getAudioEngine() {
        return audioEngine;
    }
    
    /**
     * Verifica biblioteca nativa
     */
    private void checkNativeLibrary() {
        if (!audioEngine.isNativeLibraryLoaded()) {
            LogManager.w(TAG, "Biblioteca nativa não carregada - funcionalidade limitada");
            showWarningToUser("Modo limitado: alguns recursos podem não funcionar");
        } else {
            LogManager.d(TAG, "Biblioteca nativa carregada com sucesso");
        }
    }
    
    /**
     * Limpa recursos de áudio
     */
    public void cleanup() {
        try {
            if (isPipelineRunning()) {
                stopAudioPipeline();
            }
            audioEngine.cleanup();
            LogManager.i(TAG, "Recursos de áudio limpos");
        } catch (Exception e) {
            LogManager.e(TAG, "Erro na limpeza de recursos", e);
        }
    }
    
    // Métodos auxiliares para feedback ao usuário
    
    private void showSuccessToUser(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    private void showErrorToUser(String message) {
        Toast.makeText(context, "Erro: " + message, Toast.LENGTH_LONG).show();
    }
    
    private void showWarningToUser(String message) {
        Toast.makeText(context, "Aviso: " + message, Toast.LENGTH_LONG).show();
    }
} 