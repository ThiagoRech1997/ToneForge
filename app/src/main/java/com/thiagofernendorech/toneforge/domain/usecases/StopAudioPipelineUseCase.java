package com.thiagofernendorech.toneforge.domain.usecases;

import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;

/**
 * Use Case para parar o pipeline de audio
 * Segue os principios de Clean Architecture
 */
public class StopAudioPipelineUseCase {

    private final AudioEngineInterface audioEngine;

    public StopAudioPipelineUseCase(AudioEngineInterface audioEngine) {
        this.audioEngine = audioEngine;
    }

    /**
     * Executa a parada do pipeline de audio
     * @return resultado da operacao
     */
    public Result execute() {
        // 1. Verificar se pipeline esta rodando
        if (!audioEngine.isPipelineRunning()) {
            return Result.success("Pipeline ja estava parado");
        }

        // 2. Tentar parar o pipeline
        try {
            audioEngine.stopPipeline();

            // 3. Verificar se realmente parou
            if (!audioEngine.isPipelineRunning()) {
                return Result.success("Pipeline parado com sucesso");
            } else {
                return Result.failure("Pipeline nao parou corretamente");
            }
        } catch (Exception e) {
            return Result.failure("Erro ao parar pipeline: " + e.getMessage());
        }
    }

    /**
     * Classe que encapsula o resultado da operacao
     */
    public static class Result {
        private final boolean success;
        private final String message;

        private Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static Result success(String message) {
            return new Result(true, message);
        }

        public static Result failure(String message) {
            return new Result(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
