package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.os.AsyncTask;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

public class LoopExportManager {
    
    public enum ExportFormat {
        WAV("WAV", ".wav"),
        MP3("MP3", ".mp3"),
        FLAC("FLAC", ".flac");
        
        private final String displayName;
        private final String extension;
        
        ExportFormat(String displayName, String extension) {
            this.displayName = displayName;
            this.extension = extension;
        }
        
        public String getDisplayName() { return displayName; }
        public String getExtension() { return extension; }
    }
    
    public interface ExportCallback {
        void onExported(boolean success, String fileName, String format);
    }
    
    public static void exportLoop(Context context, ExportFormat format, String customName, ExportCallback callback) {
        new ExportTask(context, format, customName, callback).execute();
    }
    
    private static class ExportTask extends AsyncTask<Void, Void, Boolean> {
        private final Context context;
        private final ExportFormat format;
        private final String customName;
        private final ExportCallback callback;
        private String exportedFileName;

        ExportTask(Context context, ExportFormat format, String customName, ExportCallback callback) {
            this.context = context.getApplicationContext();
            this.format = format;
            this.customName = customName;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Obter mix do loop do nativo (JNI)
                float[] mix = AudioEngine.getLooperMix();
                if (mix == null || mix.length == 0) {
                    return false;
                }
                
                int sampleRate = 48000; // fixo por enquanto
                
                // Gerar nome do arquivo
                String baseName = customName != null && !customName.trim().isEmpty() 
                    ? customName.trim() 
                    : "loop_" + new Date().getTime();
                exportedFileName = baseName + format.getExtension();
                
                File outFile = new File(context.getFilesDir(), exportedFileName);
                
                // Exportar baseado no formato
                switch (format) {
                    case WAV:
                        writeWavFile(mix, sampleRate, outFile);
                        break;
                    case MP3:
                        writeMp3File(mix, sampleRate, outFile);
                        break;
                    case FLAC:
                        writeFlacFile(mix, sampleRate, outFile);
                        break;
                }
                
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (callback != null) {
                callback.onExported(success, exportedFileName, format.getDisplayName());
            }
        }
    }

    // Escreve um array de float PCM [-1,1] como WAV 16-bit
    private static void writeWavFile(float[] audio, int sampleRate, File outFile) throws IOException {
        int numSamples = audio.length;
        int numChannels = 1;
        int bitsPerSample = 16;
        int byteRate = sampleRate * numChannels * bitsPerSample / 8;
        int dataSize = numSamples * numChannels * bitsPerSample / 8;
        int chunkSize = 36 + dataSize;

        FileOutputStream fos = new FileOutputStream(outFile);
        ByteBuffer buffer = ByteBuffer.allocate(44 + dataSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // RIFF header
        buffer.put("RIFF".getBytes());
        buffer.putInt(chunkSize);
        buffer.put("WAVE".getBytes());
        buffer.put("fmt ".getBytes());
        buffer.putInt(16); // Subchunk1Size
        buffer.putShort((short) 1); // AudioFormat PCM
        buffer.putShort((short) numChannels);
        buffer.putInt(sampleRate);
        buffer.putInt(byteRate);
        buffer.putShort((short) (numChannels * bitsPerSample / 8));
        buffer.putShort((short) bitsPerSample);
        buffer.put("data".getBytes());
        buffer.putInt(dataSize);

        // PCM data
        for (float v : audio) {
            short s = (short) Math.max(Math.min(v * 32767f, 32767f), -32768f);
            buffer.putShort(s);
        }

        fos.write(buffer.array());
        fos.close();
    }
    
    // Escreve como MP3 (implementação simplificada - na prática seria necessário um encoder MP3)
    private static void writeMp3File(float[] audio, int sampleRate, File outFile) throws IOException {
        // Por enquanto, vamos salvar como WAV mas com extensão .mp3
        // Em uma implementação real, seria necessário integrar um encoder MP3 como LAME
        writeWavFile(audio, sampleRate, outFile);
        
        // Renomear para .mp3
        File wavFile = outFile;
        File mp3File = new File(outFile.getParent(), outFile.getName().replace(".wav", ".mp3"));
        wavFile.renameTo(mp3File);
    }
    
    // Escreve como FLAC (implementação simplificada - na prática seria necessário um encoder FLAC)
    private static void writeFlacFile(float[] audio, int sampleRate, File outFile) throws IOException {
        // Por enquanto, vamos salvar como WAV mas com extensão .flac
        // Em uma implementação real, seria necessário integrar um encoder FLAC
        writeWavFile(audio, sampleRate, outFile);
        
        // Renomear para .flac
        File wavFile = outFile;
        File flacFile = new File(outFile.getParent(), outFile.getName().replace(".wav", ".flac"));
        wavFile.renameTo(flacFile);
    }
} 