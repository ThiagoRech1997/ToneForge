package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.os.AsyncTask;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoopLibraryManager {
    
    public static class LoopInfo {
        public String fileName;
        public String displayName;
        public long fileSize;
        public long creationTime;
        public int durationSeconds;
        public String durationFormatted;
        
        public LoopInfo(String fileName, String displayName, long fileSize, long creationTime, int durationSeconds) {
            this.fileName = fileName;
            this.displayName = displayName;
            this.fileSize = fileSize;
            this.creationTime = creationTime;
            this.durationSeconds = durationSeconds;
            this.durationFormatted = formatDuration(durationSeconds);
        }
        
        private String formatDuration(int seconds) {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
        }
    }
    
    public interface LibraryCallback {
        void onLibraryUpdated(List<LoopInfo> loops);
    }
    
    public interface DeleteCallback {
        void onDeleted(boolean success, String fileName);
    }
    
    public interface RenameCallback {
        void onRenamed(boolean success, String oldName, String newName);
    }
    
    public static void loadLibrary(Context context, LibraryCallback callback) {
        new LoadLibraryTask(context, callback).execute();
    }
    
    public static void deleteLoop(Context context, String fileName, DeleteCallback callback) {
        new DeleteLoopTask(context, fileName, callback).execute();
    }
    
    public static void renameLoop(Context context, String oldFileName, String newDisplayName, RenameCallback callback) {
        new RenameLoopTask(context, oldFileName, newDisplayName, callback).execute();
    }
    
    private static class LoadLibraryTask extends AsyncTask<Void, Void, List<LoopInfo>> {
        private final Context context;
        private final LibraryCallback callback;
        
        LoadLibraryTask(Context context, LibraryCallback callback) {
            this.context = context.getApplicationContext();
            this.callback = callback;
        }
        
        @Override
        protected List<LoopInfo> doInBackground(Void... voids) {
            List<LoopInfo> loops = new ArrayList<>();
            File filesDir = context.getFilesDir();
            android.util.Log.d("LoopLibraryManager", "Procurando arquivos em: " + filesDir.getAbsolutePath());
            
            File[] files = filesDir.listFiles();
            android.util.Log.d("LoopLibraryManager", "Total de arquivos encontrados: " + (files != null ? files.length : 0));
            
            if (files != null) {
                for (File file : files) {
                    android.util.Log.d("LoopLibraryManager", "Verificando arquivo: " + file.getName() + " (tamanho: " + file.length() + " bytes)");
                    if (file.getName().endsWith(".wav") && file.getName().startsWith("loop_")) {
                        String fileName = file.getName();
                        String displayName = getDisplayName(fileName);
                        long fileSize = file.length();
                        long creationTime = file.lastModified();
                        
                        // Calcular duração aproximada baseada no tamanho do arquivo
                        // WAV 16-bit mono a 48kHz: 2 bytes por sample
                        int durationSeconds = (int) (fileSize / (48000.0 * 2.0));
                        
                        LoopInfo loopInfo = new LoopInfo(fileName, displayName, fileSize, creationTime, durationSeconds);
                        loops.add(loopInfo);
                        android.util.Log.d("LoopLibraryManager", "Loop adicionado: " + fileName + " (" + displayName + ")");
                    }
                }
                
                // Ordenar por data de criação (mais recente primeiro)
                Collections.sort(loops, (a, b) -> Long.compare(b.creationTime, a.creationTime));
            }
            
            android.util.Log.d("LoopLibraryManager", "Total de loops encontrados: " + loops.size());
            return loops;
        }
        
        @Override
        protected void onPostExecute(List<LoopInfo> loops) {
            if (callback != null) callback.onLibraryUpdated(loops);
        }
        
        private String getDisplayName(String fileName) {
            // Remover extensão .wav
            String name = fileName.replace(".wav", "");
            
            // Se começa com "loop_", tentar extrair timestamp
            if (name.startsWith("loop_")) {
                String timestamp = name.substring(5); // Remove "loop_"
                try {
                    long time = Long.parseLong(timestamp);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    return "Loop " + sdf.format(new Date(time));
                } catch (NumberFormatException e) {
                    // Se não conseguir parsear, usar nome original
                    return name;
                }
            }
            
            return name;
        }
    }
    
    private static class DeleteLoopTask extends AsyncTask<Void, Void, Boolean> {
        private final Context context;
        private final String fileName;
        private final DeleteCallback callback;
        
        DeleteLoopTask(Context context, String fileName, DeleteCallback callback) {
            this.context = context.getApplicationContext();
            this.fileName = fileName;
            this.callback = callback;
        }
        
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                File file = new File(context.getFilesDir(), fileName);
                return file.delete();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (callback != null) callback.onDeleted(success, fileName);
        }
    }
    
    private static class RenameLoopTask extends AsyncTask<Void, Void, Boolean> {
        private final Context context;
        private final String oldFileName;
        private final String newDisplayName;
        private final RenameCallback callback;
        
        RenameLoopTask(Context context, String oldFileName, String newDisplayName, RenameCallback callback) {
            this.context = context.getApplicationContext();
            this.oldFileName = oldFileName;
            this.newDisplayName = newDisplayName;
            this.callback = callback;
        }
        
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                File oldFile = new File(context.getFilesDir(), oldFileName);
                if (!oldFile.exists()) {
                    return false;
                }
                
                // Criar novo nome de arquivo baseado no display name
                String newFileName = createFileNameFromDisplayName(newDisplayName);
                File newFile = new File(context.getFilesDir(), newFileName);
                
                // Se o arquivo já existe, adicionar sufixo
                int counter = 1;
                while (newFile.exists()) {
                    newFileName = createFileNameFromDisplayName(newDisplayName + " (" + counter + ")");
                    newFile = new File(context.getFilesDir(), newFileName);
                    counter++;
                }
                
                return oldFile.renameTo(newFile);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (callback != null) {
                String newFileName = success ? createFileNameFromDisplayName(newDisplayName) : oldFileName;
                callback.onRenamed(success, oldFileName, newFileName);
            }
        }
        
        private String createFileNameFromDisplayName(String displayName) {
            // Remover caracteres especiais e espaços
            String sanitizedName = displayName.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
            sanitizedName = sanitizedName.replaceAll("\\s+", "_");
            
            // Se ficou vazio, usar timestamp
            if (sanitizedName.isEmpty()) {
                sanitizedName = "loop_" + System.currentTimeMillis();
            } else {
                sanitizedName = "loop_" + sanitizedName + "_" + System.currentTimeMillis();
            }
            
            return sanitizedName + ".wav";
        }
    }
} 