package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.os.AsyncTask;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

public class LoopExportUtil {
    public interface ExportCallback {
        void onExported(boolean success);
    }

    public static void saveCurrentLoopAsWav(Context context, ExportCallback callback) {
        new ExportTask(context, callback).execute();
    }

    private static class ExportTask extends AsyncTask<Void, Void, Boolean> {
        private final Context context;
        private final ExportCallback callback;

        ExportTask(Context context, ExportCallback callback) {
            this.context = context.getApplicationContext();
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Obter mix do loop do nativo (JNI)
                float[] mix = AudioEngine.getLooperMix();
                android.util.Log.d("LoopExportUtil", "Mix obtido. Tamanho: " + (mix != null ? mix.length : 0));
                
                if (mix == null || mix.length == 0) {
                    android.util.Log.e("LoopExportUtil", "Mix é null ou vazio!");
                    return false;
                }
                
                int sampleRate = 48000; // fixo por enquanto
                String fileName = "loop_" + new Date().getTime() + ".wav";
                File outFile = new File(context.getFilesDir(), fileName);
                android.util.Log.d("LoopExportUtil", "Salvando WAV em: " + outFile.getAbsolutePath());
                
                writeWavFile(mix, sampleRate, outFile);
                
                // Verificar se o arquivo foi criado
                if (outFile.exists()) {
                    android.util.Log.d("LoopExportUtil", "Arquivo criado com sucesso. Tamanho: " + outFile.length() + " bytes");
                } else {
                    android.util.Log.e("LoopExportUtil", "Arquivo não foi criado!");
                    return false;
                }
                
                return true;
            } catch (Exception e) {
                android.util.Log.e("LoopExportUtil", "Erro ao salvar WAV: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (callback != null) callback.onExported(success);
            if (success) {
                android.widget.Toast.makeText(context, "Loop salvo na pasta interna!", android.widget.Toast.LENGTH_LONG).show();
            } else {
                android.widget.Toast.makeText(context, "Erro ao salvar WAV! Veja o log.", android.widget.Toast.LENGTH_LONG).show();
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
} 