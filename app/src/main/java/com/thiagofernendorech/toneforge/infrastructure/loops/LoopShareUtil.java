package com.thiagofernendorech.toneforge;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;

public class LoopShareUtil {
    
    public static void shareLoop(Context context, String fileName) {
        try {
            File file = new File(context.getFilesDir(), fileName);
            if (!file.exists()) {
                android.widget.Toast.makeText(context, "Arquivo não encontrado", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Criar URI usando FileProvider para compartilhamento seguro
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
                
                // Conceder permissão apenas ao pacote escolhido
                android.content.pm.ResolveInfo chosenApp =
                    context.getPackageManager().resolveActivity(shareIntent, 0);
                if (chosenApp != null) {
                    String packageName = chosenApp.activityInfo.packageName;
                    context.grantUriPermission(packageName, fileUri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                context.startActivity(chooser);

                // Revogar permissão após o compartilhamento
                context.revokeUriPermission(fileUri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                android.widget.Toast.makeText(context, "Nenhum app disponível para compartilhamento", android.widget.Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            android.util.Log.e("LoopShareUtil", "Erro ao compartilhar loop: " + e.getMessage());
            android.widget.Toast.makeText(context, "Erro ao compartilhar: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    public static void shareLoopFromLibrary(Context context, String fileName) {
        try {
            // Tentar diferentes localizações do arquivo
            File file = new File(context.getFilesDir(), fileName);
            if (!file.exists()) {
                file = new File(context.getCacheDir(), fileName);
            }
            if (!file.exists()) {
                file = new File(context.getExternalFilesDir(null), fileName);
            }
            
            if (!file.exists()) {
                android.widget.Toast.makeText(context, "Arquivo não encontrado: " + fileName, android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Criar URI usando FileProvider
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
                
                // Conceder permissão apenas ao pacote escolhido
                android.content.pm.ResolveInfo chosenApp =
                    context.getPackageManager().resolveActivity(shareIntent, 0);
                if (chosenApp != null) {
                    String packageName = chosenApp.activityInfo.packageName;
                    context.grantUriPermission(packageName, fileUri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                context.startActivity(chooser);

                // Revogar permissão após o compartilhamento
                context.revokeUriPermission(fileUri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                android.widget.Toast.makeText(context, "Nenhum app disponível para compartilhamento", android.widget.Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            android.util.Log.e("LoopShareUtil", "Erro ao compartilhar loop da biblioteca: " + e.getMessage());
            android.widget.Toast.makeText(context, "Erro ao compartilhar: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
} 