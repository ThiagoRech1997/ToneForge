package com.thiagofernendorech.toneforge.ui.components;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.widget.ImageView;
import android.app.AlertDialog;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.thiagofernendorech.toneforge.LogManager;
import com.thiagofernendorech.toneforge.R;
import com.thiagofernendorech.toneforge.LatencyManager;
import com.thiagofernendorech.toneforge.AudioEngine;

/**
 * Controlador responsável pelo status do sistema
 * Extrai responsabilidades de UI da MainActivity
 */
public class SystemStatusController {
    
    private static final String TAG = "SystemStatusController";
    
    private final Context context;
    private final ImageView btnWifi;
    private final ImageView btnVolume;
    private final ImageView btnPower;
    private final LatencyManager latencyManager;
    
    public SystemStatusController(Context context, ImageView btnWifi, 
                                ImageView btnVolume, ImageView btnPower) {
        this.context = context;
        this.btnWifi = btnWifi;
        this.btnVolume = btnVolume;
        this.btnPower = btnPower;
        this.latencyManager = LatencyManager.getInstance(context);
        
        setupClickListeners();
        updateStatusIcons();
    }
    
    /**
     * Configura os listeners dos botões
     */
    private void setupClickListeners() {
        if (btnWifi != null) {
            btnWifi.setOnClickListener(v -> showWifiDialog());
        }
        
        if (btnVolume != null) {
            btnVolume.setOnClickListener(v -> showVolumeDialog());
        }
        
        if (btnPower != null) {
            btnPower.setOnClickListener(v -> showPowerDialog());
        }
    }
    
    /**
     * Atualiza todos os ícones de status uma vez
     */
    public void updateStatusIcons() {
        updateBatteryIcon();
        updateWifiIcon();
        updatePowerIcon();
    }
    
    /**
     * Atualiza ícone de bateria
     */
    private void updateBatteryIcon() {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float) scale;
                
                // Implementar atualização visual baseada no nível
                LogManager.d(TAG, "Nível de bateria: " + batteryPct + "%");
            }
        } catch (Exception e) {
            LogManager.e(TAG, "Erro ao atualizar ícone de bateria", e);
        }
    }
    
    /**
     * Atualiza ícone de Wi-Fi
     */
    private void updateWifiIcon() {
        try {
            if (hasWifiPermission()) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
                boolean isWifiEnabled = wifiManager.isWifiEnabled();
                
                updateWifiIconAppearance(isWifiEnabled);
            } else {
                updateWifiIconAppearance(false);
            }
        } catch (Exception e) {
            LogManager.e(TAG, "Erro ao atualizar ícone Wi-Fi", e);
            updateWifiIconAppearance(false);
        }
    }
    
    /**
     * Atualiza aparência do ícone Wi-Fi
     */
    private void updateWifiIconAppearance(boolean isConnected) {
        if (btnWifi != null) {
            btnWifi.setImageResource(R.drawable.ic_wifi);
            int colorRes = isConnected ? R.color.lava_green : R.color.lava_text_secondary;
            btnWifi.setColorFilter(context.getResources().getColor(colorRes));
        }
    }
    
    /**
     * Atualiza ícone de energia
     */
    private void updatePowerIcon() {
        if (btnPower != null) {
            btnPower.setImageResource(R.drawable.ic_power);
            btnPower.setColorFilter(context.getResources().getColor(R.color.lava_blue));
        }
    }
    
    /**
     * Mostra diálogo de Wi-Fi
     */
    private void showWifiDialog() {
        try {
            if (hasWifiPermission()) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
                boolean isWifiEnabled = wifiManager.isWifiEnabled();
                
                String status = isWifiEnabled ? "Wi-Fi Conectado" : "Wi-Fi Desconectado";
                String action = isWifiEnabled ? "Desconectar" : "Conectar";
                
                new AlertDialog.Builder(context)
                    .setTitle("Status Wi-Fi")
                    .setMessage(status)
                    .setPositiveButton(action, (dialog, which) -> toggleWifi(wifiManager, isWifiEnabled))
                    .setNegativeButton("Cancelar", null)
                    .show();
            } else {
                showPermissionDialog("Wi-Fi", "Permissão necessária para acessar Wi-Fi");
            }
        } catch (Exception e) {
            LogManager.e(TAG, "Erro ao mostrar diálogo Wi-Fi", e);
            showPermissionDialog("Wi-Fi", "Erro ao acessar Wi-Fi");
        }
    }
    
    /**
     * Mostra diálogo de volume
     */
    private void showVolumeDialog() {
        View dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_volume_control, null);
        SeekBar volumeSeekBar = dialogView.findViewById(R.id.volumeSeekBar);
        TextView volumeText = dialogView.findViewById(R.id.volumeText);
        
        setupVolumeSeekBar(volumeSeekBar, volumeText);
        
        new AlertDialog.Builder(context)
            .setTitle("Controle de Volume")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show();
    }
    
    /**
     * Mostra diálogo de energia
     */
    private void showPowerDialog() {
        String[] options = {
            "Modo Performance (Baixa Latência)",
            "Modo Economia (Baixo Consumo)",
            "Modo Normal (Equilibrado)",
            "Configurações de Energia"
        };
        
        new AlertDialog.Builder(context)
            .setTitle("Gerenciamento de Energia")
            .setItems(options, (dialog, which) -> handlePowerModeSelection(which))
            .show();
    }
    
    /**
     * Configura SeekBar de volume
     */
    private void setupVolumeSeekBar(SeekBar volumeSeekBar, TextView volumeText) {
        volumeSeekBar.setMax(100);
        volumeSeekBar.setProgress(50);
        
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumeText.setText("Volume: " + progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    /**
     * Trata seleção de modo de energia
     */
    private void handlePowerModeSelection(int selection) {
        switch (selection) {
            case 0:
                setPowerMode("performance");
                break;
            case 1:
                setPowerMode("economy");
                break;
            case 2:
                setPowerMode("normal");
                break;
            case 3:
                openPowerSettings();
                break;
        }
    }
    
    /**
     * Define modo de energia
     */
    private void setPowerMode(String mode) {
        switch (mode) {
            case "performance":
                latencyManager.setLatencyMode(LatencyManager.MODE_LOW_LATENCY);
                AudioEngine.setOversamplingEnabled(true);
                AudioEngine.setOversamplingFactor(4);
                showToast("Modo Performance ativado");
                break;
            case "economy":
                latencyManager.setLatencyMode(LatencyManager.MODE_STABILITY);
                AudioEngine.setOversamplingEnabled(false);
                showToast("Modo Economia ativado");
                break;
            case "normal":
                latencyManager.setLatencyMode(LatencyManager.MODE_BALANCED);
                AudioEngine.setOversamplingEnabled(true);
                AudioEngine.setOversamplingFactor(2);
                showToast("Modo Normal ativado");
                break;
        }
        updatePowerIcon();
    }
    
    // Métodos auxiliares
    
    private boolean hasWifiPermission() {
        return context.checkSelfPermission(android.Manifest.permission.ACCESS_WIFI_STATE) 
            == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }
    
    private void toggleWifi(WifiManager wifiManager, boolean isCurrentlyEnabled) {
        try {
            if (context.checkSelfPermission(android.Manifest.permission.CHANGE_WIFI_STATE) 
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                wifiManager.setWifiEnabled(!isCurrentlyEnabled);
                updateWifiIcon();
            } else {
                showToast("Permissão necessária para alterar Wi-Fi");
            }
        } catch (Exception e) {
            LogManager.e(TAG, "Erro ao alterar Wi-Fi", e);
            showToast("Erro ao alterar Wi-Fi");
        }
    }
    
    private void showPermissionDialog(String title, String message) {
        new AlertDialog.Builder(context)
            .setTitle("Status " + title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void openPowerSettings() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS);
            context.startActivity(intent);
        } catch (Exception e) {
            LogManager.e(TAG, "Erro ao abrir configurações de energia", e);
            showToast("Erro ao abrir configurações");
        }
    }
    
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
} 