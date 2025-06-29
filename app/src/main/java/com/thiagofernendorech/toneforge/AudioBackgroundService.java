package com.thiagofernendorech.toneforge;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AudioBackgroundService extends Service {
    private static final String TAG = "AudioBackgroundService";
    private static final String CHANNEL_ID = "ToneForge_Audio_Channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private static final String ACTION_STOP_SERVICE = "com.thiagofernendorech.toneforge.STOP_SERVICE";
    private static final String ACTION_TOGGLE_AUDIO = "com.thiagofernendorech.toneforge.TOGGLE_AUDIO";
    private static final String ACTION_UPDATE_NOTIFICATION = "UPDATE_NOTIFICATION";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AudioBackgroundService criado");
        createNotificationChannel();
        
        // Configurar callback do PipelineManager
        PipelineManager.getInstance().setCallback(new PipelineManager.PipelineCallback() {
            @Override
            public void onPipelineStarted() {
                Log.d(TAG, "Pipeline iniciado");
                updateNotification();
            }
            
            @Override
            public void onPipelineStopped() {
                Log.d(TAG, "Pipeline parado");
                updateNotification();
            }
            
            @Override
            public void onPipelineError(String error) {
                Log.e(TAG, "Erro no pipeline: " + error);
                updateNotification();
            }
            
            @Override
            public void onPipelineRecovered() {
                Log.d(TAG, "Pipeline recuperado");
                updateNotification();
            }
            
            @Override
            public void onPipelineStateChanged(int oldState, int newState) {
                Log.d(TAG, "Estado do pipeline mudou: " + oldState + " -> " + newState);
                updateNotification();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AudioBackgroundService iniciado");
        
        if (intent != null && intent.getAction() != null) {
            handleAction(intent.getAction());
        }
        
        // Iniciar o pipeline de áudio se não estiver rodando
        if (!AudioEngine.isAudioPipelineRunning()) {
            AudioEngine.startAudioPipeline();
        }
        
        // Criar e mostrar a notificação
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);
        
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
                updateNotification();
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
        
        // A notificação será atualizada automaticamente pelo callback
    }

    private void updateNotification() {
        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, createNotification());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.audio_background_channel_name),
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(getString(R.string.audio_background_channel_description));
            channel.setShowBadge(false);
            channel.setSound(null, null);
            
            NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        AudioStateManager stateManager = AudioStateManager.getInstance(this);
        PipelineManager pipelineManager = PipelineManager.getInstance();
        
        // Intent para abrir o app
        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent para parar o serviço
        Intent stopServiceIntent = new Intent(this, AudioBackgroundService.class);
        stopServiceIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopServicePendingIntent = PendingIntent.getService(
            this, 0, stopServiceIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent para alternar áudio
        Intent toggleAudioIntent = new Intent(this, AudioBackgroundService.class);
        toggleAudioIntent.setAction(ACTION_TOGGLE_AUDIO);
        PendingIntent toggleAudioPendingIntent = PendingIntent.getService(
            this, 0, toggleAudioIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent para abrir efeitos
        Intent openEffectsIntent = new Intent(this, MainActivity.class);
        openEffectsIntent.putExtra("fragment", "effects");
        openEffectsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openEffectsPendingIntent = PendingIntent.getActivity(
            this, 1, openEffectsIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent para abrir presets
        Intent openPresetsIntent = new Intent(this, MainActivity.class);
        openPresetsIntent.putExtra("fragment", "home");
        openPresetsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPresetsPendingIntent = PendingIntent.getActivity(
            this, 2, openPresetsIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Status do pipeline
        String pipelineStatus;
        if (pipelineManager.isError()) {
            pipelineStatus = getString(R.string.pipeline_error);
        } else if (pipelineManager.isRecovering()) {
            pipelineStatus = getString(R.string.pipeline_recovering);
        } else if (pipelineManager.isRunning()) {
            pipelineStatus = getString(R.string.audio_background_active);
        } else {
            pipelineStatus = getString(R.string.audio_background_paused);
        }
        
        String title = "ToneForge - " + pipelineStatus;

        // Conteúdo detalhado da notificação
        StringBuilder contentText = new StringBuilder();
        contentText.append(stateManager.getEffectsStatusText(this)).append(" • ");
        contentText.append(stateManager.getPresetStatusText(this)).append(" • ");
        contentText.append(stateManager.getAudioQualityText(this));

        // Texto expandido com mais detalhes
        StringBuilder bigText = new StringBuilder();
        bigText.append("Pipeline: ").append(pipelineManager.getStateName()).append("\n");
        bigText.append(stateManager.getEffectsStatusText(this)).append("\n");
        bigText.append(stateManager.getPresetStatusText(this)).append("\n");
        bigText.append(stateManager.getAudioLevelText(this)).append("\n");
        bigText.append(stateManager.getOversamplingText(this));
        
        // Adicionar estatísticas do pipeline se estiver rodando
        if (pipelineManager.isRunning()) {
            long uptime = pipelineManager.getUptime() / 1000; // em segundos
            bigText.append(getString(R.string.pipeline_uptime, uptime)).append("\n");
            bigText.append(getString(R.string.pipeline_samples, pipelineManager.getTotalSamplesProcessed())).append("\n");
        }
        
        // Adicionar informações de erro se houver
        if (pipelineManager.isError()) {
            bigText.append(getString(R.string.pipeline_errors, pipelineManager.getErrorCount())).append("\n");
            bigText.append(getString(R.string.pipeline_last_error, pipelineManager.getLastError())).append("\n");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText.toString())
            .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText.toString()))
            .setSmallIcon(R.drawable.ic_volume_up)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_volume_up, getString(R.string.audio_background_toggle), toggleAudioPendingIntent)
            .addAction(R.drawable.ic_graphic_eq, getString(R.string.notification_open_effects), openEffectsPendingIntent)
            .addAction(R.drawable.ic_book, getString(R.string.notification_open_presets), openPresetsPendingIntent)
            .addAction(R.drawable.ic_settings, getString(R.string.audio_background_stop), stopServicePendingIntent);

        return builder.build();
    }

    // Métodos estáticos para controle externo
    public static void startService(Context context) {
        // Verificar permissões antes de iniciar o serviço
        if (!PermissionManager.hasAllRequiredPermissions(context)) {
            Log.e(TAG, "Permissões necessárias não concedidas");
            Toast.makeText(context, context.getString(R.string.permission_audio_required), Toast.LENGTH_LONG).show();
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

    // Método para atualizar a notificação quando o estado dos efeitos mudar
    public static void updateNotification(Context context) {
        if (isServiceRunning(context)) {
            Intent intent = new Intent(context, AudioBackgroundService.class);
            intent.setAction("UPDATE_NOTIFICATION");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }
} 