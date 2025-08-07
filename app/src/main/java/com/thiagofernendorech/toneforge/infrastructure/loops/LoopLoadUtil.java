package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.os.AsyncTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class LoopLoadUtil {
    public interface LoadCallback {
        void onLoaded(boolean success, String fileName);
    }

    public interface FileListCallback {
        void onFilesListed(List<String> fileNames);
    }

    public static void listSavedLoops(Context context, FileListCallback callback) {
        new ListFilesTask(context, callback).execute();
    }

    public static void loadLoopFromFile(Context context, String fileName, LoadCallback callback) {
        new LoadTask(context, fileName, callback).execute();
    }

    private static class ListFilesTask extends AsyncTask<Void, Void, List<String>> {
        private final Context context;
        private final FileListCallback callback;

        ListFilesTask(Context context, FileListCallback callback) {
            this.context = context.getApplicationContext();
            this.callback = callback;
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> fileNames = new ArrayList<>();
            File filesDir = new File(context.getFilesDir(), "loops");
            if (!filesDir.exists()) {
                filesDir.mkdirs();
            }
            LogManager.verbose("LoopLoadUtil", "Procurando arquivos em: " + filesDir.getAbsolutePath());

            File[] files = filesDir.listFiles();
            LogManager.verbose("LoopLoadUtil", "Total de arquivos encontrados: " + (files != null ? files.length : 0));
            
            if (files != null) {
                for (File file : files) {
                    LogManager.verbose("LoopLoadUtil", "Arquivo: " + file.getName() + " (tamanho: " + file.length() + " bytes)");
                    if (file.getName().endsWith(".wav") && file.getName().startsWith("loop_")) {
                        fileNames.add(file.getName());
                        LogManager.verbose("LoopLoadUtil", "Loop encontrado: " + file.getName());
                    }
                }
            }
            
            LogManager.i("LoopLoadUtil", "Total de loops encontrados: " + fileNames.size());
            return fileNames;
        }

        @Override
        protected void onPostExecute(List<String> fileNames) {
            if (callback != null) callback.onFilesListed(fileNames);
        }
    }

    private static class LoadTask extends AsyncTask<Void, Void, Boolean> {
        private final Context context;
        private final String fileName;
        private final LoadCallback callback;

        LoadTask(Context context, String fileName, LoadCallback callback) {
            this.context = context.getApplicationContext();
            this.fileName = fileName;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                File file = new File(new File(context.getFilesDir(), "loops"), fileName);
                if (!file.exists()) {
                    return false;
                }

                // Ler arquivo WAV
                float[] audioData = readWavFile(file);
                
                // Carregar no motor de áudio via JNI
                AudioEngine.loadLooperFromAudio(audioData);
                
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (callback != null) callback.onLoaded(success, fileName);
        }
    }

    private static final int MAX_WAV_DATA_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final int MIN_WAV_DATA_SIZE = 1024; // 1 KB mínimo
    private static final int MAX_SAMPLE_RATE = 192000; // 192 kHz máximo
    private static final int MIN_SAMPLE_RATE = 8000; // 8 kHz mínimo
    private static final int MAX_CHANNELS = 2; // Estéreo máximo
    private static final int MIN_CHANNELS = 1; // Mono mínimo
    private static final int MAX_BITS_PER_SAMPLE = 32; // 32 bits máximo
    private static final int MIN_BITS_PER_SAMPLE = 8; // 8 bits mínimo

    // Lê um arquivo WAV e retorna os dados de áudio como array de float
    // SECURITY: Implementa validações robustas para prevenir crashes e ataques
    private static float[] readWavFile(File file) throws IOException {
        // SECURITY: Validar arquivo antes de processar
        if (file == null) {
            throw new IOException("Arquivo WAV é null");
        }
        
        if (!file.exists()) {
            throw new IOException("Arquivo WAV não existe: " + file.getPath());
        }
        
        if (!file.canRead()) {
            throw new IOException("Arquivo WAV não pode ser lido: " + file.getPath());
        }
        
        long fileSize = file.length();
        if (fileSize <= 44) { // Header WAV mínimo é 44 bytes
            throw new IOException("Arquivo WAV muito pequeno: " + fileSize + " bytes");
        }
        
        if (fileSize > MAX_WAV_DATA_SIZE + 44) { // 44 bytes para header
            throw new IOException("Arquivo WAV muito grande: " + fileSize + " bytes (máximo: " + (MAX_WAV_DATA_SIZE + 44) + ")");
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            // Ler header WAV
            byte[] header = new byte[44];
            int headerRead = fis.read(header);
            if (headerRead != header.length) {
                throw new IOException("Falha ao ler cabeçalho WAV: " + headerRead + " de " + header.length + " bytes");
            }

            // Verificar se é um arquivo WAV válido
            ByteBuffer buffer = ByteBuffer.wrap(header);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            String riff = new String(header, 0, 4);
            String wave = new String(header, 8, 4);

            if (!riff.equals("RIFF") || !wave.equals("WAVE")) {
                throw new IOException("Arquivo não é um WAV válido (RIFF: " + riff + ", WAVE: " + wave + ")");
            }

            // SECURITY: Extrair e validar informações do header
            int sampleRate = buffer.getInt(24);
            int numChannels = buffer.getShort(22) & 0xFFFF; // Converter para unsigned
            int bitsPerSample = buffer.getShort(34) & 0xFFFF; // Converter para unsigned
            int dataSize = buffer.getInt(40);

            // SECURITY: Validações robustas dos parâmetros WAV
            if (sampleRate < MIN_SAMPLE_RATE || sampleRate > MAX_SAMPLE_RATE) {
                throw new IOException("Taxa de amostragem inválida: " + sampleRate + " Hz (deve estar entre " + MIN_SAMPLE_RATE + " e " + MAX_SAMPLE_RATE + ")");
            }
            
            if (numChannels < MIN_CHANNELS || numChannels > MAX_CHANNELS) {
                throw new IOException("Número de canais inválido: " + numChannels + " (deve estar entre " + MIN_CHANNELS + " e " + MAX_CHANNELS + ")");
            }
            
            if (bitsPerSample < MIN_BITS_PER_SAMPLE || bitsPerSample > MAX_BITS_PER_SAMPLE) {
                throw new IOException("Bits por amostra inválidos: " + bitsPerSample + " (deve estar entre " + MIN_BITS_PER_SAMPLE + " e " + MAX_BITS_PER_SAMPLE + ")");
            }
            
            if (dataSize <= 0) {
                throw new IOException("Tamanho de dados WAV inválido: " + dataSize + " bytes");
            }
            
            if (dataSize > MAX_WAV_DATA_SIZE) {
                throw new IOException("Tamanho de dados WAV muito grande: " + dataSize + " bytes (máximo: " + MAX_WAV_DATA_SIZE + ")");
            }
            
            if (dataSize > fileSize - header.length) {
                throw new IOException("Tamanho de dados WAV excede o tamanho do arquivo: " + dataSize + " > " + (fileSize - header.length));
            }
            
            if (dataSize < MIN_WAV_DATA_SIZE) {
                throw new IOException("Tamanho de dados WAV muito pequeno: " + dataSize + " bytes (mínimo: " + MIN_WAV_DATA_SIZE + ")");
            }

            // SECURITY: Calcular tamanho esperado e validar
            int bytesPerSample = bitsPerSample / 8;
            int expectedDataSize = (int) (fileSize - header.length);
            
            if (dataSize > expectedDataSize) {
                throw new IOException("Tamanho de dados WAV inconsistente: " + dataSize + " > " + expectedDataSize);
            }

            // Ler dados de áudio
            byte[] audioBytes = new byte[dataSize];
            int totalRead = 0;
            int attempts = 0;
            final int maxAttempts = 1000; // Prevenir loop infinito
            
            while (totalRead < dataSize && attempts < maxAttempts) {
                int bytesRead = fis.read(audioBytes, totalRead, dataSize - totalRead);
                if (bytesRead == -1) {
                    break; // EOF
                }
                if (bytesRead == 0) {
                    attempts++; // Prevenir loop infinito
                    continue;
                }
                totalRead += bytesRead;
            }

            if (totalRead != dataSize) {
                throw new IOException("Leitura de dados de áudio incompleta: " + totalRead + " de " + dataSize + " bytes");
            }

            // SECURITY: Validar que todos os bytes foram lidos corretamente
            if (totalRead <= 0) {
                throw new IOException("Nenhum dado de áudio foi lido");
            }

            // Converter para float
            int numSamples = dataSize / bytesPerSample;
            
            // SECURITY: Validar número de amostras
            if (numSamples <= 0) {
                throw new IOException("Número de amostras inválido: " + numSamples);
            }
            
            if (numSamples > MAX_WAV_DATA_SIZE / bytesPerSample) {
                throw new IOException("Número de amostras muito grande: " + numSamples);
            }
            
            float[] audioData = new float[numSamples];

            ByteBuffer audioBuffer = ByteBuffer.wrap(audioBytes);
            audioBuffer.order(ByteOrder.LITTLE_ENDIAN);

            // SECURITY: Converter amostras com validação
            for (int i = 0; i < numSamples; i++) {
                if (audioBuffer.remaining() < bytesPerSample) {
                    throw new IOException("Buffer insuficiente para ler amostra " + i);
                }
                
                short sample;
                if (bitsPerSample == 16) {
                    sample = audioBuffer.getShort();
                } else if (bitsPerSample == 8) {
                    sample = (short) ((audioBuffer.get() & 0xFF) << 8);
                } else if (bitsPerSample == 24) {
                    // Ler 24 bits como 3 bytes
                    byte b1 = audioBuffer.get();
                    byte b2 = audioBuffer.get();
                    byte b3 = audioBuffer.get();
                    sample = (short) ((b3 << 16) | ((b2 & 0xFF) << 8) | (b1 & 0xFF));
                } else {
                    // Para outros formatos, usar 16 bits
                    sample = audioBuffer.getShort();
                }
                
                // Normalizar para [-1, 1]
                audioData[i] = sample / 32767.0f;
                
                // SECURITY: Validar se o valor está dentro dos limites esperados
                if (Float.isNaN(audioData[i]) || Float.isInfinite(audioData[i])) {
                    throw new IOException("Valor de áudio inválido na amostra " + i + ": " + audioData[i]);
                }
            }

            // SECURITY: Log de sucesso para auditoria
            android.util.Log.d("LoopLoadUtil", "WAV carregado com sucesso: " + 
                numSamples + " amostras, " + sampleRate + " Hz, " + 
                numChannels + " canais, " + bitsPerSample + " bits");

            return audioData;
        } catch (OutOfMemoryError e) {
            // SECURITY: Capturar erro de memória e fornecer mensagem clara
            throw new IOException("Memória insuficiente para carregar arquivo WAV: " + e.getMessage());
        } catch (Exception e) {
            // SECURITY: Capturar outros erros e fornecer contexto
            throw new IOException("Erro ao processar arquivo WAV: " + e.getMessage(), e);
        }
    }
} 