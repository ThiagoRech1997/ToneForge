package com.thiagofernendorech.toneforge.domain.usecases;

import com.thiagofernendorech.toneforge.domain.models.AudioState;
import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;
import com.thiagofernendorech.toneforge.domain.interfaces.PermissionInterface;

/**
 * Use Case para inicializar o pipeline de áudio
 * Segue os princípios de Clean Architecture
 */
public class StartAudioPipelineUseCase {
    
    private final AudioEngineInterface audioEngine;
    private final PermissionInterface permissionManager;
    
    public StartAudioPipelineUseCase(
        AudioEngineInterface audioEngine,
        PermissionInterface permissionManager
    ) {
        this.audioEngine = audioEngine;
        this.permissionManager = permissionManager;
    }
    
    /**
     * Executa a inicialização do pipeline de áudio
     * @return resultado da operação
     */
    public Result execute() {
        // 1. Verificar permissões necessárias
        if (!permissionManager.hasAudioPermission()) {
            return Result.failure("Permissão de áudio necessária");
        }
        
        // 2. Verificar se biblioteca nativa está carregada
        if (!audioEngine.isNativeLibraryLoaded()) {
            return Result.failure("Biblioteca nativa não carregada");
        }
        
        // 3. Tentar inicializar o pipeline
        try {
            boolean success = audioEngine.startPipeline();
            if (success) {
                AudioState state = audioEngine.getCurrentState();
                return Result.success("Pipeline iniciado com sucesso", state);
            } else {
                return Result.failure("Falha ao iniciar pipeline");
            }
        } catch (Exception e) {
            return Result.failure("Erro ao iniciar pipeline: " + e.getMessage());
        }
    }
    
    /**
     * Classe que encapsula o resultado da operação
     */
    public static class Result {
        private final boolean success;
        private final String message;
        private final AudioState audioState;
        
        private Result(boolean success, String message, AudioState audioState) {
            this.success = success;
            this.message = message;
            this.audioState = audioState;
        }
        
        public static Result success(String message, AudioState audioState) {
            return new Result(true, message, audioState);
        }
        
        public static Result failure(String message) {
            return new Result(false, message, null);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public AudioState getAudioState() {
            return audioState;
        }
    }
} 