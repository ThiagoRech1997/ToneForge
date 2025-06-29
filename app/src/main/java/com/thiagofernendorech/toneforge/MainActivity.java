package com.thiagofernendorech.toneforge;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.core.app.ActivityCompat;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionManager.PermissionCallback {
    
    private StateRecoveryManager stateRecoveryManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Inicializar StateRecoveryManager
        stateRecoveryManager = StateRecoveryManager.getInstance(this);
        
        // Setup Navigation Component
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        
        // Verificar e solicitar permissões necessárias
        checkAndRequestPermissions();
        
        // Log do status das permissões para debug
        PermissionManager.logPermissionStatus(this);
    }
    
    private void checkAndRequestPermissions() {
        if (!PermissionManager.hasAllRequiredPermissions(this)) {
            PermissionManager.requestRequiredPermissions(this, this);
        } else {
            // Todas as permissões necessárias já estão concedidas
            onPermissionsGranted();
        }
    }
    
    @Override
    public void onPermissionsGranted() {
        Toast.makeText(this, getString(R.string.permission_all_granted), Toast.LENGTH_SHORT).show();
        
        // Verificar permissões opcionais
        if (!PermissionManager.hasOptionalPermissions(this)) {
            // Solicitar permissões opcionais se necessário
            requestOptionalPermissions();
        }
    }
    
    @Override
    public void onPermissionsDenied(List<String> deniedPermissions) {
        Toast.makeText(this, getString(R.string.permission_some_denied), Toast.LENGTH_LONG).show();
        
        // Tentar solicitar permissões opcionais mesmo assim
        requestOptionalPermissions();
    }
    
    @Override
    public void onPermissionExplanationNeeded(List<String> permissions) {
        Toast.makeText(this, getString(R.string.permission_explanation_needed), Toast.LENGTH_LONG).show();
        
        // Tentar solicitar permissões opcionais mesmo assim
        requestOptionalPermissions();
    }
    
    private void requestOptionalPermissions() {
        // Solicitar permissão de overlay se necessário
        if (!PermissionManager.hasOverlayPermission(this)) {
            PermissionManager.requestOverlayPermission(this);
        }
        
        // Solicitar desabilitar otimização de bateria se necessário
        if (!PermissionManager.isBatteryOptimizationDisabled(this)) {
            PermissionManager.requestBatteryOptimizationPermission(this);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handlePermissionResult(this, requestCode, permissions, grantResults, this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Verificar se permissões foram concedidas quando o usuário retorna das configurações
        if (PermissionManager.hasAllRequiredPermissions(this)) {
            // Verificar se o usuário concedeu permissões opcionais
            if (PermissionManager.hasOptionalPermissions(this)) {
                // Todas as permissões concedidas
            }
        }
        
        // Verificar se precisa restaurar estado
        if (stateRecoveryManager != null && stateRecoveryManager.needsStateRecovery()) {
            stateRecoveryManager.restoreState();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Salvar estado atual quando o app vai para background
        if (stateRecoveryManager != null) {
            stateRecoveryManager.saveCurrentState();
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        // Salvar estado também quando o app para completamente
        if (stateRecoveryManager != null) {
            stateRecoveryManager.saveCurrentState();
        }
    }
}