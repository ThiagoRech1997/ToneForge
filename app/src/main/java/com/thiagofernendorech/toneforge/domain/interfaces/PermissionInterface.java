package com.thiagofernendorech.toneforge.domain.interfaces;

import java.util.List;

/**
 * Interface para gerenciamento de permissões
 * Abstrai as operações de permissão da plataforma Android
 */
public interface PermissionInterface {
    
    /**
     * Verifica se tem permissão de áudio
     * @return true se tem permissão
     */
    boolean hasAudioPermission();
    
    /**
     * Verifica se tem todas as permissões necessárias
     * @return true se tem todas
     */
    boolean hasAllRequiredPermissions();
    
    /**
     * Verifica se tem permissões opcionais
     * @return true se tem opcionais
     */
    boolean hasOptionalPermissions();
    
    /**
     * Verifica se tem permissão de overlay
     * @return true se tem permissão
     */
    boolean hasOverlayPermission();
    
    /**
     * Verifica se otimização de bateria está desabilitada
     * @return true se desabilitada
     */
    boolean isBatteryOptimizationDisabled();
    
    /**
     * Obtém lista de permissões negadas
     * @return lista de permissões negadas
     */
    List<String> getDeniedPermissions();
    
    /**
     * Interface para callbacks de permissão
     */
    interface PermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied(List<String> deniedPermissions);
        void onPermissionExplanationNeeded(List<String> permissions);
    }
} 