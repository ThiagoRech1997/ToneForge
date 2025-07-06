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
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.widget.ImageView;
import android.app.AlertDialog;
import android.widget.SeekBar;
import android.view.WindowManager;
import android.graphics.Color;

// Nova arquitetura
import com.thiagofernendorech.toneforge.ui.navigation.NavigationController;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.ui.fragments.home.HomeFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.effects.EffectsFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.looper.LooperFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.tuner.TunerFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.metronome.MetronomeFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.recorder.RecorderFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.looplibrary.LoopLibraryFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.learning.LearningFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.settings.SettingsFragmentRefactored;

public class MainActivity extends AppCompatActivity implements PermissionManager.PermissionCallback {
    
    private StateRecoveryManager stateRecoveryManager;
    private LatencyManager latencyManager;
    private TextView headerTitle;
    private Timer timeUpdateTimer;
    private ImageView btnWifi;
    private ImageView btnVolume;
    private ImageView btnPower;
    
    // Nova arquitetura
    private NavigationController navigationController;
    private AudioRepository audioRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Remover ActionBar se existir
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // Configurar status bar para integração orgânica
        getWindow().setStatusBarColor(getResources().getColor(R.color.lava_status_bg));
        // Permitir que o conteúdo se estenda sob a status bar
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        setTitle("");
        setContentView(R.layout.activity_main);
        
        // Inicializar views
        headerTitle = findViewById(R.id.headerTitle);
        btnWifi = findViewById(R.id.btnWifi);
        btnVolume = findViewById(R.id.btnVolume);
        btnPower = findViewById(R.id.btnPower);
        
        // Inicializar gerenciadores
        stateRecoveryManager = StateRecoveryManager.getInstance(this);
        latencyManager = LatencyManager.getInstance(this);
        
        // Inicializar nova arquitetura
        navigationController = NavigationController.getInstance();
        navigationController.init(this);
        audioRepository = AudioRepository.getInstance(this);
        
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
        
        // Iniciar pipeline de áudio
        AudioEngine.startAudioPipeline();
        
        // Recuperar estado salvo
        stateRecoveryManager.restoreState();
        
        // Carregar fragment inicial (nova arquitetura)
        loadFragment(new HomeFragmentRefactored());
        
        // Iniciar timer para atualização de status
        startStatusUpdateTimer();
        
        // Ativar modo fullscreen novamente
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        // Forçar conteúdo sob a status bar
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
    }
    
    private void setupNavigation() {
        // Botão Home (nova arquitetura)
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            loadFragment(new HomeFragmentRefactored());
            updateHeaderTitle("ToneForge");
        });
        
        // Botão Pedaleira (nova interface)
        findViewById(R.id.btnPedalboard).setOnClickListener(v -> {
            loadFragment(new PedalboardFragment());
            updateHeaderTitle("🎸 Pedaleira");
        });
        
        // Botão Power - Sistema de Power Management
        btnPower.setOnClickListener(v -> {
            showPowerOptionsDialog();
        });
        
        // Botão Wi-Fi - Status de conectividade
        btnWifi.setOnClickListener(v -> {
            showWifiStatusDialog();
        });
        
        // Botão Volume - Controle de volume do sistema
        btnVolume.setOnClickListener(v -> {
            showVolumeControlDialog();
        });
    }
    
    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        
        // Adicionar à pilha de navegação apenas se não for o HomeFragment
        if (!(fragment instanceof HomeFragment || fragment instanceof HomeFragmentRefactored)) {
            transaction.addToBackStack(null);
        }
        
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
        
        // Limpar nova arquitetura
        if (navigationController != null) {
            navigationController.clear();
        }
        if (audioRepository != null) {
            audioRepository.cleanup();
        }
    }
    
    private void showPowerOptionsDialog() {
        String[] options = {
            "Modo Performance (Baixa Latência)",
            "Modo Economia (Baixo Consumo)",
            "Modo Normal (Equilibrado)",
            "Configurações de Energia"
        };
        
        new AlertDialog.Builder(this)
            .setTitle("Gerenciamento de Energia")
            .setItems(options, (dialog, which) -> {
                switch (which) {
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
            })
            .show();
    }
    
    private void setPowerMode(String mode) {
        // Implementar mudança de modo de energia
        switch (mode) {
            case "performance":
                latencyManager.setLatencyMode(LatencyManager.MODE_LOW_LATENCY);
                AudioEngine.setOversamplingEnabled(true);
                AudioEngine.setOversamplingFactor(4);
                Toast.makeText(this, "Modo Performance ativado", Toast.LENGTH_SHORT).show();
                break;
            case "economy":
                latencyManager.setLatencyMode(LatencyManager.MODE_STABILITY);
                AudioEngine.setOversamplingEnabled(false);
                Toast.makeText(this, "Modo Economia ativado", Toast.LENGTH_SHORT).show();
                break;
            case "normal":
                latencyManager.setLatencyMode(LatencyManager.MODE_BALANCED);
                AudioEngine.setOversamplingEnabled(true);
                AudioEngine.setOversamplingFactor(2);
                Toast.makeText(this, "Modo Normal ativado", Toast.LENGTH_SHORT).show();
                break;
        }
        updatePowerButtonIcon();
    }
    
    public void showWifiStatusDialog() {
        try {
            // Verificar se temos permissão para acessar o Wi-Fi
            if (checkSelfPermission(android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                boolean isWifiEnabled = wifiManager.isWifiEnabled();
                
                String status = isWifiEnabled ? "Wi-Fi Conectado" : "Wi-Fi Desconectado";
                String action = isWifiEnabled ? "Desconectar" : "Conectar";
                
                new AlertDialog.Builder(this)
                    .setTitle("Status Wi-Fi")
                    .setMessage(status)
                    .setPositiveButton(action, (dialog, which) -> {
                        try {
                            if (checkSelfPermission(android.Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                                if (isWifiEnabled) {
                                    wifiManager.setWifiEnabled(false);
                                } else {
                                    wifiManager.setWifiEnabled(true);
                                }
                                updateWifiButtonIcon();
                            } else {
                                Toast.makeText(this, "Permissão necessária para alterar Wi-Fi", Toast.LENGTH_SHORT).show();
                            }
                        } catch (SecurityException e) {
                            Toast.makeText(this, "Erro ao alterar Wi-Fi", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            } else {
                // Se não temos permissão, mostrar mensagem
                new AlertDialog.Builder(this)
                    .setTitle("Status Wi-Fi")
                    .setMessage("Permissão necessária para acessar Wi-Fi")
                    .setPositiveButton("OK", null)
                    .show();
            }
        } catch (SecurityException e) {
            // Em caso de erro de segurança, mostrar mensagem
            new AlertDialog.Builder(this)
                .setTitle("Status Wi-Fi")
                .setMessage("Erro ao acessar Wi-Fi")
                .setPositiveButton("OK", null)
                .show();
        }
    }
    
    public void showVolumeControlDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_volume_control, null);
        SeekBar volumeSeekBar = dialogView.findViewById(R.id.volumeSeekBar);
        TextView volumeText = dialogView.findViewById(R.id.volumeText);
        
        // Configurar SeekBar para volume do sistema
        volumeSeekBar.setMax(100);
        volumeSeekBar.setProgress(50); // Valor padrão
        
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumeText.setText("Volume: " + progress + "%");
                // Aqui você implementaria o controle de volume do sistema
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        new AlertDialog.Builder(this)
            .setTitle("Controle de Volume")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void startStatusUpdateTimer() {
        timeUpdateTimer = new Timer();
        timeUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    updateStatusIcons();
                });
            }
        }, 0, 5000); // Atualizar a cada 5 segundos
    }
    
    private void updateStatusIcons() {
        updateBatteryIcon();
        updateWifiButtonIcon();
        updatePowerButtonIcon();
    }
    
    private void updateBatteryIcon() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level * 100 / (float) scale;
            
            // Atualizar ícone de bateria baseado no nível
            if (batteryPct < 20) {
                // Ícone de bateria baixa
            } else if (batteryPct < 50) {
                // Ícone de bateria média
            } else {
                // Ícone de bateria alta
            }
        }
    }
    
    private void updateWifiButtonIcon() {
        try {
            // Verificar se temos permissão para acessar o Wi-Fi
            if (checkSelfPermission(android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                boolean isWifiEnabled = wifiManager.isWifiEnabled();
                
                if (isWifiEnabled) {
                    btnWifi.setImageResource(R.drawable.ic_wifi);
                    btnWifi.setColorFilter(getResources().getColor(R.color.lava_green));
                } else {
                    btnWifi.setImageResource(R.drawable.ic_wifi);
                    btnWifi.setColorFilter(getResources().getColor(R.color.lava_text_secondary));
                }
            } else {
                // Se não temos permissão, mostrar ícone neutro
                btnWifi.setImageResource(R.drawable.ic_wifi);
                btnWifi.setColorFilter(getResources().getColor(R.color.lava_text_secondary));
            }
        } catch (SecurityException e) {
            // Em caso de erro de segurança, mostrar ícone neutro
            btnWifi.setImageResource(R.drawable.ic_wifi);
            btnWifi.setColorFilter(getResources().getColor(R.color.lava_text_secondary));
        }
    }
    
    private void updatePowerButtonIcon() {
        // Atualizar ícone baseado no modo de energia atual
        btnPower.setImageResource(R.drawable.ic_power);
        btnPower.setColorFilter(getResources().getColor(R.color.lava_blue));
    }
    
    private void openPowerSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS);
        startActivity(intent);
    }
    
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
                    updateHeaderTitleByFragment(currentFragment);
                    fragmentManager.removeOnBackStackChangedListener(this);
                }
            });
        } else {
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
            if (currentFragment instanceof HomeFragment || currentFragment instanceof HomeFragmentRefactored) {
                updateHeaderTitle("ToneForge");
                showExitConfirmationDialog();
            } else {
                loadFragment(new HomeFragmentRefactored());
                updateHeaderTitle("ToneForge");
            }
        }
    }
    
    private void updateHeaderTitleByFragment(Fragment fragment) {
        if (fragment instanceof HomeFragment || fragment instanceof HomeFragmentRefactored) {
            updateHeaderTitle("ToneForge");
        } else if (fragment instanceof EffectsFragment || fragment instanceof EffectsFragmentRefactored) {
            updateHeaderTitle("Efeitos");
        } else if (fragment instanceof PedalboardFragment) {
            updateHeaderTitle("🎸 Pedaleira");
        } else if (fragment instanceof TunerFragment || fragment instanceof TunerFragmentRefactored) {
            updateHeaderTitle("Afinador");
        } else if (fragment instanceof LooperFragment || fragment instanceof LooperFragmentRefactored) {
            updateHeaderTitle("Looper");
        } else if (fragment instanceof MetronomeFragment) {
            updateHeaderTitle("Metrônomo");
        } else if (fragment instanceof LearningFragment) {
            updateHeaderTitle("Aprendizado");
        } else if (fragment instanceof RecorderFragment) {
            updateHeaderTitle("Gravador");
        } else if (fragment instanceof SettingsFragmentRefactored) {
            updateHeaderTitle("Configurações");
        } else if (fragment instanceof LoopLibraryFragmentRefactored) {
            updateHeaderTitle("Biblioteca de Loops");
        }
    }
    
    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Sair do ToneForge")
            .setMessage("Deseja realmente sair do aplicativo?")
            .setPositiveButton("Sair", (dialog, which) -> {
                // Salvar estado antes de sair
                if (stateRecoveryManager != null) {
                    stateRecoveryManager.saveCurrentState();
                }
                finish();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
}