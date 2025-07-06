package com.thiagofernendorech.toneforge.ui.fragments.home;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;
import com.thiagofernendorech.toneforge.ui.navigation.NavigationController;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.domain.models.AudioState;

/**
 * Presenter para o HomeFragment
 * Gerencia a lógica de negócio e comunicação com a View
 */
public class HomePresenter extends BasePresenter<HomeContract.View> implements HomeContract.Presenter {
    
    private NavigationController navigationController;
    private AudioRepository audioRepository;
    private Context context;
    
    // Handler para atualizações periódicas
    private Handler updateHandler;
    private Runnable updateRunnable;
    private static final int UPDATE_INTERVAL_MS = 5000; // 5 segundos
    
    // Estado atual
    private boolean isUpdating = false;
    
    /**
     * Construtor do HomePresenter
     * @param context contexto da aplicação
     * @param navigationController controlador de navegação
     * @param audioRepository repository de áudio
     */
    public HomePresenter(Context context, NavigationController navigationController, AudioRepository audioRepository) {
        this.context = context.getApplicationContext();
        this.navigationController = navigationController;
        this.audioRepository = audioRepository;
        
        // Inicializar handler para atualizações
        this.updateHandler = new Handler(Looper.getMainLooper());
        this.updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isUpdating && isViewAttached()) {
                    updateSystemStatus();
                    updateHandler.postDelayed(this, UPDATE_INTERVAL_MS);
                }
            }
        };
    }
    
    // === Implementação da interface BasePresenter ===
    
    // Métodos herdados do BasePresenter - não precisam ser redefinidos
    
    // === Implementação da interface HomeContract.Presenter ===
    
    @Override
    public void onEffectsClicked() {
        if (navigationController.isMainActivityAvailable()) {
            navigationController.navigateToEffects();
        }
    }
    
    @Override
    public void onLooperClicked() {
        if (navigationController.isMainActivityAvailable()) {
            navigationController.navigateToLooper();
        }
    }
    
    @Override
    public void onTunerClicked() {
        if (navigationController.isMainActivityAvailable()) {
            navigationController.navigateToTuner();
        }
    }
    
    @Override
    public void onMetronomeClicked() {
        if (navigationController.isMainActivityAvailable()) {
            navigationController.navigateToMetronome();
        }
    }
    
    @Override
    public void onLearningClicked() {
        if (navigationController.isMainActivityAvailable()) {
            navigationController.navigateToLearning();
        }
    }
    
    @Override
    public void onRecorderClicked() {
        if (navigationController.isMainActivityAvailable()) {
            navigationController.navigateToRecorder();
        }
    }
    
    @Override
    public void onSettingsClicked() {
        if (navigationController.isMainActivityAvailable()) {
            navigationController.navigateToSettings();
        }
    }
    
    @Override
    public void onWifiClicked() {
        ifViewAttached(HomeContract.View::showWifiDialog);
    }
    
    @Override
    public void onVolumeClicked() {
        ifViewAttached(HomeContract.View::showVolumeDialog);
    }
    
    @Override
    public void onPowerClicked() {
        ifViewAttached(HomeContract.View::showPowerDialog);
    }
    
    @Override
    public void onViewStarted() {
        // Atualizar título
        ifViewAttached(view -> view.updateTitle("ToneForge"));
        
        // Fazer uma atualização inicial
        updateSystemStatus();
        
        // Iniciar atualizações periódicas
        startPeriodicUpdates();
    }
    
    @Override
    public void onViewPaused() {
        // Parar atualizações para economizar recursos
        stopPeriodicUpdates();
    }
    
    @Override
    public void onViewResumed() {
        // Retomar atualizações
        updateSystemStatus();
        startPeriodicUpdates();
    }
    
    @Override
    public void onViewDestroyed() {
        // Parar todas as atualizações
        stopPeriodicUpdates();
        
        // Limpar handler
        if (updateHandler != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }
    
    @Override
    public void updateSystemStatus() {
        if (!isViewAttached()) {
            return;
        }
        
        // Atualizar status Wi-Fi
        updateWifiStatus();
        
        // Atualizar status da bateria
        updateBatteryStatus();
        
        // Atualizar estado do áudio
        updateAudioStatus();
    }
    
    // === Métodos privados ===
    
    /**
     * Inicia atualizações periódicas
     */
    private void startPeriodicUpdates() {
        if (!isUpdating) {
            isUpdating = true;
            updateHandler.post(updateRunnable);
        }
    }
    
    /**
     * Para atualizações periódicas
     */
    private void stopPeriodicUpdates() {
        isUpdating = false;
        if (updateHandler != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }
    
    /**
     * Atualiza o status da conexão Wi-Fi
     */
    private void updateWifiStatus() {
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (connectivityManager != null) {
                NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                boolean isConnected = wifiInfo != null && wifiInfo.isConnected();
                
                ifViewAttached(view -> view.updateWifiStatus(isConnected));
            }
        } catch (Exception e) {
            // Erro ao obter status Wi-Fi - assumir desconectado
            ifViewAttached(view -> view.updateWifiStatus(false));
        }
    }
    
    /**
     * Atualiza o status da bateria
     */
    private void updateBatteryStatus() {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            
            if (batteryStatus != null) {
                // Nível da bateria
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int batteryPct = (int) ((level * 100.0f) / scale);
                
                // Status de carregamento
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                   status == BatteryManager.BATTERY_STATUS_FULL;
                
                ifViewAttached(view -> view.updateBatteryStatus(batteryPct, isCharging));
            }
        } catch (Exception e) {
            // Erro ao obter status da bateria - usar valores padrão
            ifViewAttached(view -> view.updateBatteryStatus(50, false));
        }
    }
    
    /**
     * Atualiza o estado do áudio
     */
    private void updateAudioStatus() {
        try {
            AudioState audioState = audioRepository.getCurrentAudioState();
            ifViewAttached(view -> view.updateAudioState(audioState));
        } catch (Exception e) {
            // Erro ao obter estado do áudio - usar estado padrão
            AudioState defaultState = new AudioState();
            ifViewAttached(view -> view.updateAudioState(defaultState));
        }
    }
    
    /**
     * Verifica se o Wi-Fi está ativo
     * @return true se Wi-Fi está ativo
     */
    public boolean isWifiEnabled() {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            return wifiManager != null && wifiManager.isWifiEnabled();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifica se há conexão com a internet
     * @return true se há conexão
     */
    public boolean hasInternetConnection() {
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }
        } catch (Exception e) {
            // Erro ao verificar conexão
        }
        return false;
    }
    
    /**
     * Obtém informações resumidas do sistema
     * @return string com informações do sistema
     */
    public String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        
        // Status Wi-Fi
        if (isWifiEnabled()) {
            info.append("Wi-Fi: Ativo");
        } else {
            info.append("Wi-Fi: Inativo");
        }
        
        // Status Internet
        if (hasInternetConnection()) {
            info.append(" | Internet: Conectado");
        } else {
            info.append(" | Internet: Desconectado");
        }
        
        // Status do pipeline de áudio
        if (audioRepository.isAudioPipelineRunning()) {
            info.append(" | Áudio: Ativo");
        } else {
            info.append(" | Áudio: Inativo");
        }
        
        return info.toString();
    }
} 