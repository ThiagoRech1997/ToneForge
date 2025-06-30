package com.thiagofernendorech.toneforge;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.app.ActivityCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements PermissionManager.PermissionCallback {
    
    private StateRecoveryManager stateRecoveryManager;
    private LatencyManager latencyManager;
    private TextView headerTitle;
    private Timer timeUpdateTimer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.activity_main);
        
        // Inicializar views
        headerTitle = findViewById(R.id.headerTitle);
        
        // Inicializar gerenciadores
        stateRecoveryManager = StateRecoveryManager.getInstance(this);
        latencyManager = LatencyManager.getInstance(this);
        
        // Setup navegação
        setupNavigation();
        
        // Verificar e solicitar permissões necessárias
        checkAndRequestPermissions();
        
        // Log do status das permissões para debug
        PermissionManager.logPermissionStatus(this);
        
        // Inicializar pipeline com configurações de latência
        PipelineManager pipelineManager = PipelineManager.getInstance();
        pipelineManager.initialize(this);
        
        // Inicializar AudioEngine com configurações de latência
        AudioEngine audioEngine = AudioEngine.getInstance();
        audioEngine.initialize(this);
        
        // Recuperar estado salvo
        stateRecoveryManager.restoreState();
        
        // Carregar fragment inicial
        loadFragment(new HomeFragment());
    }
    
    private void setupNavigation() {
        // Botão Home
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            updateHeaderTitle("ToneForge");
        });
        
        // Botão Power
        findViewById(R.id.btnPower).setOnClickListener(v -> {
            // Implementar funcionalidade de power
            Toast.makeText(this, "Power", Toast.LENGTH_SHORT).show();
        });
    }
    
    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
    
    public void updateHeaderTitle(String title) {
        headerTitle.setText(title);
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
        
        // Atualizar configurações de latência se necessário
        if (latencyManager != null) {
            // As configurações são aplicadas automaticamente pelo LatencyManager
        }
        
        // Recuperar estado se necessário
        stateRecoveryManager.restoreState();
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Parar timer de atualização do tempo (caso ainda exista)
        if (timeUpdateTimer != null) {
            timeUpdateTimer.cancel();
            timeUpdateTimer = null;
        }
    }
}