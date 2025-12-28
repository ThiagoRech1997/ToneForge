package com.thiagofernendorech.toneforge.domain.usecases;

import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;
import com.thiagofernendorech.toneforge.domain.models.EffectParameters;

/**
 * Use Case para aplicar parametros de efeitos
 * Segue os principios de Clean Architecture
 */
public class ApplyEffectParametersUseCase {

    private final AudioEngineInterface audioEngine;

    public ApplyEffectParametersUseCase(AudioEngineInterface audioEngine) {
        this.audioEngine = audioEngine;
    }

    /**
     * Executa a aplicacao de parametros de efeitos
     * @param parameters parametros a serem aplicados
     * @return resultado da operacao
     */
    public Result execute(EffectParameters parameters) {
        // 1. Validar parametros
        if (parameters == null) {
            return Result.failure("Parametros nao podem ser nulos");
        }

        // 2. Verificar se biblioteca nativa esta carregada
        if (!audioEngine.isNativeLibraryLoaded()) {
            return Result.failure("Biblioteca nativa nao carregada");
        }

        // 3. Verificar se pipeline esta rodando
        if (!audioEngine.isPipelineRunning()) {
            return Result.failure("Pipeline de audio nao esta ativo");
        }

        // 4. Tentar aplicar parametros
        try {
            audioEngine.applyEffectParameters(parameters);

            // 5. Verificar se parametros foram aplicados
            EffectParameters appliedParams = audioEngine.getCurrentEffectParameters();
            if (appliedParams != null) {
                return Result.success("Parametros aplicados com sucesso", appliedParams);
            } else {
                return Result.failure("Erro ao verificar parametros aplicados");
            }
        } catch (Exception e) {
            return Result.failure("Erro ao aplicar parametros: " + e.getMessage());
        }
    }

    /**
     * Aplica apenas um efeito especifico
     * @param effectName nome do efeito
     * @param enabled se o efeito deve ser ativado
     * @return resultado da operacao
     */
    public Result executeForEffect(String effectName, boolean enabled) {
        // 1. Validar nome do efeito
        if (effectName == null || effectName.isEmpty()) {
            return Result.failure("Nome do efeito nao pode ser vazio");
        }

        // 2. Verificar se biblioteca nativa esta carregada
        if (!audioEngine.isNativeLibraryLoaded()) {
            return Result.failure("Biblioteca nativa nao carregada");
        }

        // 3. Obter parametros atuais e modificar o efeito especifico
        EffectParameters currentParams = audioEngine.getCurrentEffectParameters();
        if (currentParams == null) {
            currentParams = new EffectParameters();
        }

        // 4. Aplicar mudanca no efeito especifico
        try {
            applyEffectChange(currentParams, effectName, enabled);
            audioEngine.applyEffectParameters(currentParams);
            return Result.success("Efeito '" + effectName + "' " + (enabled ? "ativado" : "desativado"), currentParams);
        } catch (IllegalArgumentException e) {
            return Result.failure(e.getMessage());
        } catch (Exception e) {
            return Result.failure("Erro ao aplicar efeito: " + e.getMessage());
        }
    }

    /**
     * Aplica mudanca em um efeito especifico
     */
    private void applyEffectChange(EffectParameters params, String effectName, boolean enabled) {
        switch (effectName.toLowerCase()) {
            case "gain":
            case "ganho":
                params.setGainEnabled(enabled);
                break;
            case "distortion":
            case "distorcao":
                params.setDistortionEnabled(enabled);
                break;
            case "delay":
                params.setDelayEnabled(enabled);
                break;
            case "reverb":
                params.setReverbEnabled(enabled);
                break;
            case "chorus":
                params.setChorusEnabled(enabled);
                break;
            case "flanger":
                params.setFlangerEnabled(enabled);
                break;
            case "phaser":
                params.setPhaserEnabled(enabled);
                break;
            case "eq":
            case "equalizer":
                params.setEqEnabled(enabled);
                break;
            case "compressor":
                params.setCompressorEnabled(enabled);
                break;
            default:
                throw new IllegalArgumentException("Efeito desconhecido: " + effectName);
        }
    }

    /**
     * Classe que encapsula o resultado da operacao
     */
    public static class Result {
        private final boolean success;
        private final String message;
        private final EffectParameters appliedParameters;

        private Result(boolean success, String message, EffectParameters appliedParameters) {
            this.success = success;
            this.message = message;
            this.appliedParameters = appliedParameters;
        }

        public static Result success(String message, EffectParameters appliedParameters) {
            return new Result(true, message, appliedParameters);
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

        public EffectParameters getAppliedParameters() {
            return appliedParameters;
        }
    }
}
