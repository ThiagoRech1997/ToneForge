package com.thiagofernendorech.toneforge.domain.usecases;

import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;

/**
 * Use Case para o afinador (tuner)
 * Segue os principios de Clean Architecture
 */
public class TunerUseCase {

    private final AudioEngineInterface audioEngine;

    // Notas musicais e suas frequencias (A4 = 440Hz como referencia)
    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private static final float A4_FREQUENCY = 440.0f;
    private static final int A4_MIDI_NOTE = 69;

    public TunerUseCase(AudioEngineInterface audioEngine) {
        this.audioEngine = audioEngine;
    }

    /**
     * Inicia o afinador
     * @return resultado da operacao
     */
    public Result startTuner() {
        // 1. Verificar se biblioteca nativa esta carregada
        if (!audioEngine.isNativeLibraryLoaded()) {
            return Result.failure("Biblioteca nativa nao carregada");
        }

        // 2. Tentar iniciar o afinador
        try {
            audioEngine.startTuner();
            return Result.success("Afinador iniciado");
        } catch (Exception e) {
            return Result.failure("Erro ao iniciar afinador: " + e.getMessage());
        }
    }

    /**
     * Para o afinador
     * @return resultado da operacao
     */
    public Result stopTuner() {
        try {
            audioEngine.stopTuner();
            return Result.success("Afinador parado");
        } catch (Exception e) {
            return Result.failure("Erro ao parar afinador: " + e.getMessage());
        }
    }

    /**
     * Obtem a leitura atual do afinador
     * @return resultado com os dados da leitura
     */
    public TunerReading getReading() {
        // 1. Verificar se biblioteca nativa esta carregada
        if (!audioEngine.isNativeLibraryLoaded()) {
            return TunerReading.invalid("Biblioteca nativa nao carregada");
        }

        // 2. Obter frequencia detectada
        try {
            float frequency = audioEngine.getDetectedFrequency();

            // 3. Se frequencia invalida ou muito baixa
            if (frequency <= 0 || frequency < 20.0f) {
                return TunerReading.noSignal();
            }

            // 4. Calcular nota e cents
            NoteInfo noteInfo = calculateNoteFromFrequency(frequency);
            return TunerReading.valid(frequency, noteInfo.noteName, noteInfo.octave, noteInfo.cents);
        } catch (Exception e) {
            return TunerReading.invalid("Erro ao obter frequencia: " + e.getMessage());
        }
    }

    /**
     * Calcula a nota musical a partir da frequencia
     */
    private NoteInfo calculateNoteFromFrequency(float frequency) {
        // Calcular numero de semitons a partir de A4
        double semitones = 12.0 * Math.log(frequency / A4_FREQUENCY) / Math.log(2.0);
        int roundedSemitones = (int) Math.round(semitones);

        // Calcular cents (diferenca em centesimos de semitom)
        double exactSemitones = 12.0 * Math.log(frequency / A4_FREQUENCY) / Math.log(2.0);
        int cents = (int) Math.round((exactSemitones - roundedSemitones) * 100);

        // Calcular nota MIDI
        int midiNote = A4_MIDI_NOTE + roundedSemitones;

        // Calcular nome da nota e oitava
        int noteIndex = ((midiNote % 12) + 12) % 12; // Garantir indice positivo
        int octave = (midiNote / 12) - 1;

        String noteName = NOTE_NAMES[noteIndex];

        return new NoteInfo(noteName, octave, cents);
    }

    /**
     * Classe auxiliar para informacoes da nota
     */
    private static class NoteInfo {
        final String noteName;
        final int octave;
        final int cents;

        NoteInfo(String noteName, int octave, int cents) {
            this.noteName = noteName;
            this.octave = octave;
            this.cents = cents;
        }
    }

    /**
     * Classe que encapsula o resultado de operacoes simples
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

    /**
     * Classe que encapsula a leitura do afinador
     */
    public static class TunerReading {
        private final boolean valid;
        private final boolean hasSignal;
        private final float frequency;
        private final String noteName;
        private final int octave;
        private final int cents; // -50 a +50
        private final String errorMessage;

        private TunerReading(boolean valid, boolean hasSignal, float frequency,
                            String noteName, int octave, int cents, String errorMessage) {
            this.valid = valid;
            this.hasSignal = hasSignal;
            this.frequency = frequency;
            this.noteName = noteName;
            this.octave = octave;
            this.cents = cents;
            this.errorMessage = errorMessage;
        }

        public static TunerReading valid(float frequency, String noteName, int octave, int cents) {
            return new TunerReading(true, true, frequency, noteName, octave, cents, null);
        }

        public static TunerReading noSignal() {
            return new TunerReading(true, false, 0f, null, 0, 0, null);
        }

        public static TunerReading invalid(String errorMessage) {
            return new TunerReading(false, false, 0f, null, 0, 0, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public boolean hasSignal() {
            return hasSignal;
        }

        public float getFrequency() {
            return frequency;
        }

        public String getNoteName() {
            return noteName;
        }

        public int getOctave() {
            return octave;
        }

        public int getCents() {
            return cents;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * Retorna a nota completa (ex: "A4", "C#5")
         */
        public String getFullNoteName() {
            if (!hasSignal || noteName == null) {
                return "--";
            }
            return noteName + octave;
        }

        /**
         * Verifica se esta afinado (cents entre -5 e +5)
         */
        public boolean isInTune() {
            return hasSignal && Math.abs(cents) <= 5;
        }

        /**
         * Retorna direcao para afinar (-1 = baixar, 0 = ok, 1 = subir)
         */
        public int getTuningDirection() {
            if (!hasSignal) return 0;
            if (cents < -5) return 1;  // Nota baixa, precisa subir
            if (cents > 5) return -1;   // Nota alta, precisa baixar
            return 0;                    // Afinado
        }
    }
}
