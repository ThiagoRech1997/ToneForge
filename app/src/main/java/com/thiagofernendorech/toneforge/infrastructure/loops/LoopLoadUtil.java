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

    // Lê um arquivo WAV e retorna os dados de áudio como array de float
    private static float[] readWavFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            // Ler header WAV
            byte[] header = new byte[44];
            int headerRead = fis.read(header);
            if (headerRead != header.length) {
                throw new IOException("Falha ao ler cabeçalho WAV");
            }

            // Verificar se é um arquivo WAV válido
            ByteBuffer buffer = ByteBuffer.wrap(header);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            String riff = new String(header, 0, 4);
            String wave = new String(header, 8, 4);

            if (!riff.equals("RIFF") || !wave.equals("WAVE")) {
                throw new IOException("Arquivo não é um WAV válido");
            }

            // Extrair informações do header
            int sampleRate = buffer.getInt(24);
            int numChannels = buffer.getShort(22);
            int bitsPerSample = buffer.getShort(34);
            int dataSize = buffer.getInt(40);

            if (dataSize <= 0 || dataSize > MAX_WAV_DATA_SIZE || dataSize > file.length() - header.length) {
                throw new IOException("Tamanho de dados WAV inválido: " + dataSize);
            }

            // Ler dados de áudio
            byte[] audioBytes = new byte[dataSize];
            int totalRead = 0;
            while (totalRead < dataSize) {
                int bytesRead = fis.read(audioBytes, totalRead, dataSize - totalRead);
                if (bytesRead == -1) break;
                totalRead += bytesRead;
            }

            if (totalRead != dataSize) {
                throw new IOException("Leitura de dados de áudio incompleta: " + totalRead + " de " + dataSize);
            }

            // Converter para float
            int numSamples = dataSize / (bitsPerSample / 8);
            float[] audioData = new float[numSamples];

            ByteBuffer audioBuffer = ByteBuffer.wrap(audioBytes);
            audioBuffer.order(ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < numSamples; i++) {
                short sample = audioBuffer.getShort();
                audioData[i] = sample / 32767.0f; // Normalizar para [-1, 1]
            }

            return audioData;
        }
    }
} 