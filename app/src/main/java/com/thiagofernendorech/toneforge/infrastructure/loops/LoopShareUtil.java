package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;

public class LoopShareUtil {
    
    // SECURITY: Interface para capturar a escolha real do usuário
    public interface ShareCallback {
        void onAppChosen(String packageName);
        void onShareCancelled();
    }
    
    public static void shareLoop(Context context, String fileName) {
        shareLoop(context, fileName, null);
    }
    
    public static void shareLoop(Context context, String fileName, ShareCallback callback) {
        try {
            File loopsDir = new File(context.getFilesDir(), "loops");
            File file = new File(loopsDir, fileName);
            if (!file.exists()) {
                android.widget.Toast.makeText(context, "Arquivo não encontrado", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Criar URI usando FileProvider para compartilhamento seguro
            // SECURITY: Usando FileProvider restrito com caminhos específicos
            Uri fileUri = FileProvider.getUriForFile(
                context, 
                "com.thiagofernendorech.toneforge.fileprovider", 
                file
            );
            
            // Criar intent de compartilhamento
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("audio/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Loop exportado do ToneForge");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Loop criado com ToneForge - " + fileName);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // Verificar se há apps disponíveis para compartilhamento
            if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
                // Mostrar seletor de apps
                Intent chooser = Intent.createChooser(shareIntent, "Compartilhar Loop");
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                // SECURITY: Usar ActivityResultLauncher ou callback para capturar escolha real
                // Por enquanto, conceder permissão temporária e revogar após uso
                context.startActivity(chooser);
                
                // SECURITY: Revogar permissão após um delay para permitir o compartilhamento
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        context.revokeUriPermission(fileUri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        android.util.Log.d("LoopShareUtil", "Permissão de URI revogada por segurança");
                    } catch (Exception e) {
                        android.util.Log.e("LoopShareUtil", "Erro ao revogar permissão: " + e.getMessage());
                    }
                }, 5000); // 5 segundos de delay
                
            } else {
                android.widget.Toast.makeText(context, "Nenhum app disponível para compartilhamento", android.widget.Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            android.util.Log.e("LoopShareUtil", "Erro ao compartilhar loop: " + e.getMessage());
            android.widget.Toast.makeText(context, "Erro ao compartilhar: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    public static void shareLoopFromLibrary(Context context, String fileName) {
        shareLoopFromLibrary(context, fileName, null);
    }
    
    public static void shareLoopFromLibrary(Context context, String fileName, ShareCallback callback) {
        try {
            // Tentar diferentes localizações do arquivo
            File loopsDir = new File(context.getFilesDir(), "loops");
            File file = new File(loopsDir, fileName);
            if (!file.exists()) {
                file = new File(new File(context.getCacheDir(), "loops"), fileName);
            }
            if (!file.exists()) {
                file = new File(new File(context.getExternalFilesDir(null), "loops"), fileName);
            }
            
            if (!file.exists()) {
                android.widget.Toast.makeText(context, "Arquivo não encontrado: " + fileName, android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Criar URI usando FileProvider
            // SECURITY: Usando FileProvider restrito com caminhos específicos
            Uri fileUri = FileProvider.getUriForFile(
                context, 
                "com.thiagofernendorech.toneforge.fileprovider", 
                file
            );
            
            // Criar intent de compartilhamento
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("audio/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Loop do ToneForge");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Loop criado com ToneForge - " + fileName);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Verificar se há apps disponíveis
            if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
                Intent chooser = Intent.createChooser(shareIntent, "Compartilhar Loop");
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                context.startActivity(chooser);
                
                // SECURITY: Revogar permissão após um delay para permitir o compartilhamento
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        context.revokeUriPermission(fileUri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        android.util.Log.d("LoopShareUtil", "Permissão de URI revogada por segurança");
                    } catch (Exception e) {
                        android.util.Log.e("LoopShareUtil", "Erro ao revogar permissão: " + e.getMessage());
                    }
                }, 5000); // 5 segundos de delay
                
            } else {
                android.widget.Toast.makeText(context, "Nenhum app disponível para compartilhamento", android.widget.Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            android.util.Log.e("LoopShareUtil", "Erro ao compartilhar loop da biblioteca: " + e.getMessage());
            android.widget.Toast.makeText(context, "Erro ao compartilhar: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    // SECURITY: Método para revogar todas as permissões de URI pendentes
    public static void revokeAllUriPermissions(Context context) {
        try {
            // Revogar permissões para todos os URIs do FileProvider
            context.revokeUriPermission(
                Uri.parse("content://com.thiagofernendorech.toneforge.fileprovider/"), 
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
            android.util.Log.d("LoopShareUtil", "Todas as permissões de URI revogadas");
        } catch (Exception e) {
            android.util.Log.e("LoopShareUtil", "Erro ao revogar permissões: " + e.getMessage());
        }
    }
} 