package com.thiagofernendorech.toneforge.infrastructure.adapters;

import android.content.Context;
import com.thiagofernendorech.toneforge.PermissionManager;
import com.thiagofernendorech.toneforge.domain.interfaces.PermissionInterface;
import java.util.List;

/**
 * Adapter para integrar PermissionManager existente com a interface do domínio
 * Implementa o padrão Adapter para manter compatibilidade
 */
public class PermissionManagerAdapter implements PermissionInterface {
    
    private final Context context;
    
    public PermissionManagerAdapter(Context context) {
        this.context = context.getApplicationContext();
    }
    
    @Override
    public boolean hasAudioPermission() {
        return context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) 
            == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }
    
    @Override
    public boolean hasAllRequiredPermissions() {
        return PermissionManager.hasAllRequiredPermissions(context);
    }
    
    @Override
    public boolean hasOptionalPermissions() {
        return PermissionManager.hasOptionalPermissions(context);
    }
    
    @Override
    public boolean hasOverlayPermission() {
        return PermissionManager.hasOverlayPermission(context);
    }
    
    @Override
    public boolean isBatteryOptimizationDisabled() {
        return PermissionManager.isBatteryOptimizationDisabled(context);
    }
    
    @Override
    public List<String> getDeniedPermissions() {
        // Implementação simplificada - retornar lista vazia por enquanto
        return new java.util.ArrayList<>();
    }
} 