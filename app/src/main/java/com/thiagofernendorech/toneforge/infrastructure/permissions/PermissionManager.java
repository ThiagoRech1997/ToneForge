package com.thiagofernendorech.toneforge;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    private static final String TAG = "PermissionManager";
    
    // Códigos de requisição de permissão
    public static final int PERMISSION_REQUEST_CODE = 1001;
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 1002;
    public static final int OVERLAY_PERMISSION_REQUEST_CODE = 1003;
    public static final int BATTERY_OPTIMIZATION_REQUEST_CODE = 1004;
    
    // Lista de permissões necessárias
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.POST_NOTIFICATIONS
    };
    
    // Permissões opcionais para melhor funcionalidade
    private static final String[] OPTIONAL_PERMISSIONS = {
        Manifest.permission.VIBRATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.SYSTEM_ALERT_WINDOW
    };
    
    // Permissões de mídia para Android 13+
    private static final String[] MEDIA_PERMISSIONS = {
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO
    };
    
    // Permissões de armazenamento para Android < 13
    private static final String[] STORAGE_PERMISSIONS = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    public interface PermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied(List<String> deniedPermissions);
        void onPermissionExplanationNeeded(List<String> permissions);
    }
    
    /**
     * Verifica se todas as permissões necessárias estão concedidas
     */
    public static boolean hasRequiredPermissions(Context context) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Verifica se as permissões de mídia estão concedidas (Android 13+)
     */
    public static boolean hasMediaPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (String permission : MEDIA_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Verifica se as permissões de armazenamento estão concedidas (Android < 13)
     */
    public static boolean hasStoragePermissions(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            for (String permission : STORAGE_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Verifica se a permissão de overlay está concedida
     */
    public static boolean hasOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }
    
    /**
     * Verifica se a otimização de bateria está desabilitada
     */
    public static boolean isBatteryOptimizationDisabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            String packageName = context.getPackageName();
            return powerManager.isIgnoringBatteryOptimizations(packageName);
        }
        return true;
    }
    
    /**
     * Solicita todas as permissões necessárias
     */
    public static void requestRequiredPermissions(Activity activity, PermissionCallback callback) {
        List<String> permissionsToRequest = new ArrayList<>();
        
        // Verificar permissões necessárias
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        // Verificar permissões de mídia (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (String permission : MEDIA_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
        }
        
        // Permissões de armazenamento são opcionais e serão solicitadas quando necessário
        // (por exemplo, quando o usuário tentar exportar/importar presets)
        
        if (permissionsToRequest.isEmpty()) {
            callback.onPermissionsGranted();
        } else {
            String[] permissions = permissionsToRequest.toArray(new String[0]);
            ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
        }
    }
    
    /**
     * Solicita permissão de overlay
     */
    public static void requestOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }
    
    /**
     * Solicita desabilitar otimização de bateria
     */
    public static void requestBatteryOptimizationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            String packageName = activity.getPackageName();
            
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                activity.startActivityForResult(intent, BATTERY_OPTIMIZATION_REQUEST_CODE);
            }
        }
    }
    
    /**
     * Processa o resultado da requisição de permissões
     */
    public static void handlePermissionResult(Activity activity, int requestCode, String[] permissions, int[] grantResults, PermissionCallback callback) {
        if (requestCode == PERMISSION_REQUEST_CODE || requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            List<String> deniedPermissions = new ArrayList<>();
            List<String> explanationNeeded = new ArrayList<>();
            
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                    
                    // Verificar se deve mostrar explicação
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])) {
                        explanationNeeded.add(permissions[i]);
                    }
                }
            }
            
            if (deniedPermissions.isEmpty()) {
                callback.onPermissionsGranted();
            } else if (!explanationNeeded.isEmpty()) {
                callback.onPermissionExplanationNeeded(explanationNeeded);
            } else {
                callback.onPermissionsDenied(deniedPermissions);
            }
        }
    }
    
    /**
     * Verifica se o app tem todas as permissões necessárias para funcionar
     */
    public static boolean hasAllRequiredPermissions(Context context) {
        // Permissões básicas são obrigatórias
        if (!hasRequiredPermissions(context)) {
            return false;
        }
        
        // Permissões de mídia são obrigatórias no Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasMediaPermissions(context)) {
            return false;
        }
        
        // Permissões de armazenamento são opcionais no Android < 13
        // (podem ser necessárias para exportar/importar presets, mas não para funcionamento básico)
        
        return true;
    }
    
    /**
     * Verifica se o app tem permissões opcionais para melhor funcionalidade
     */
    public static boolean hasOptionalPermissions(Context context) {
        for (String permission : OPTIONAL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return hasOverlayPermission(context) && isBatteryOptimizationDisabled(context);
    }
    
    /**
     * Obtém uma lista de permissões que ainda precisam ser concedidas
     */
    public static List<String> getMissingPermissions(Context context) {
        List<String> missingPermissions = new ArrayList<>();
        
        // Verificar permissões necessárias
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        
        // Verificar permissões de mídia (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (String permission : MEDIA_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(permission);
                }
            }
        }
        
        // Verificar permissões de armazenamento (Android < 13)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            for (String permission : STORAGE_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(permission);
                }
            }
        }
        
        return missingPermissions;
    }
    
    /**
     * Loga o status das permissões para debug
     */
    public static void logPermissionStatus(Context context) {
        Log.d(TAG, "=== Status das Permissões ===");
        Log.d(TAG, "Permissões necessárias: " + hasRequiredPermissions(context));
        Log.d(TAG, "Permissões de mídia: " + hasMediaPermissions(context));
        Log.d(TAG, "Permissões de armazenamento: " + hasStoragePermissions(context));
        Log.d(TAG, "Permissão de overlay: " + hasOverlayPermission(context));
        Log.d(TAG, "Otimização de bateria desabilitada: " + isBatteryOptimizationDisabled(context));
        Log.d(TAG, "Permissões opcionais: " + hasOptionalPermissions(context));
        
        List<String> missing = getMissingPermissions(context);
        if (!missing.isEmpty()) {
            Log.d(TAG, "Permissões faltando: " + missing);
        }
    }
    
    /**
     * Solicita permissões de armazenamento quando necessário (para exportar/importar presets)
     */
    public static void requestStoragePermissions(Activity activity, PermissionCallback callback) {
        List<String> permissionsToRequest = new ArrayList<>();
        
        // Verificar permissões de armazenamento (Android < 13)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            for (String permission : STORAGE_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
        }
        
        // Verificar permissões de mídia (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (String permission : MEDIA_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
        }
        
        if (permissionsToRequest.isEmpty()) {
            callback.onPermissionsGranted();
        } else {
            String[] permissions = permissionsToRequest.toArray(new String[0]);
            ActivityCompat.requestPermissions(activity, permissions, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }
    
    /**
     * Verifica se tem permissões de armazenamento para exportar/importar presets
     */
    public static boolean hasStoragePermissionsForPresets(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return hasMediaPermissions(context);
        } else {
            return hasStoragePermissions(context);
        }
    }
} 