package com.thiagofernendorech.toneforge;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class AudioBackgroundService extends Service {
    private static final String TAG = "AudioBackgroundService";
    private static final String CHANNEL_ID = "ToneForge_Audio_Channel";
    private static final int NOTIFICATION_ID = 1001;
    
    // Actions para controle do serviço
    public static final String ACTION_STOP_SERVICE = "com.thiagofernendorech.toneforge.STOP_SERVICE";
    public static final String ACTION_TOGGLE_AUDIO = "com.thiagofernendorech.toneforge.TOGGLE_AUDIO";
    public static final String ACTION_UPDATE_NOTIFICATION = "com.thiagofernendorech.toneforge.UPDATE_NOTIFICATION";
    
    // REMOVIDO: Controle de throttling - sem atualizações automáticas
    private static String lastNotificationContent = "";
    private static volatile boolean isCreatingNotification = false; // Apenas para thread safety

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AudioBackgroundService criado");
        createNotificationChannel();
        
        // REMOVIDO: Callbacks automáticos do pipeline
        // Agora o serviço é apenas um container, sem atualizações automáticas
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AudioBackgroundService iniciado");
        
        // Criar notificação inicial APENAS UMA VEZ
        if (isCreatingNotification) {
            return START_STICKY;
        }
        
        isCreatingNotification = true;
        try {
            Notification notification = createNotification();
            startForeground(NOTIFICATION_ID, notification);
        } finally {
            isCreatingNotification = false;
        }
        
        if (intent != null && intent.getAction() != null) {
            handleAction(intent.getAction());
        }
        
        return START_STICKY; // Serviço será reiniciado se for morto pelo sistema
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "AudioBackgroundService destruído");
        super.onDestroy();
    }

    private void handleAction(String action) {
        switch (action) {
            case ACTION_STOP_SERVICE:
                stopSelf();
                break;
            case ACTION_TOGGLE_AUDIO:
                toggleAudioPipeline();
                break;
            case ACTION_UPDATE_NOTIFICATION:
                // REMOVIDO: Atualizações automáticas
                // Apenas manter uma notificação estática
                break;
        }
    }

    private void toggleAudioPipeline() {
        PipelineManager pipelineManager = PipelineManager.getInstance();
        
        if (pipelineManager.isRunning()) {
            pipelineManager.stopPipeline();
        } else {
            pipelineManager.startPipeline();
        }
        
        // REMOVIDO: Atualização forçada da notificação
        // A notificação permanece estática
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "ToneForge Audio Service",
                NotificationManager.IMPORTANCE_LOW // Baixa importância para evitar interferências
            );
            
            channel.setDescription("Serviço de processamento de áudio em background");
            channel.setShowBadge(false); // Não mostrar badge
            channel.enableVibration(false); // Sem vibração
            channel.setSound(null, null); // Sem som
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        // Notificação SIMPLES e ESTÁTICA - sem atualizações
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent para parar o serviço
        Intent stopIntent = new Intent(this, AudioBackgroundService.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ToneForge")
            .setContentText("Serviço de áudio ativo")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Baixa prioridade
            .setShowWhen(false) // Não mostrar timestamp
            .setSilent(true) // Silencioso
            .addAction(R.drawable.ic_stop, "Parar", stopPendingIntent)
            .build();
    }

    // === MÉTODOS ESTÁTICOS SIMPLIFICADOS ===
    
    public static void startService(Context context) {
        // Verificar permissões antes de iniciar o serviço
        if (!PermissionManager.hasAllRequiredPermissions(context)) {
            Log.e(TAG, "Permissões necessárias não concedidas");
            return;
        }
        
        Intent intent = new Intent(context, AudioBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, AudioBackgroundService.class);
        context.stopService(intent);
    }

    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AudioBackgroundService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isPipelineRunning() {
        return PipelineManager.getInstance().isRunning();
    }

    // REMOVIDO: updateNotification - sem atualizações automáticas
    // REMOVIDO: scheduleNotificationUpdate - sem throttling
    // REMOVIDO: buildNotificationContent - notificação estática
} 