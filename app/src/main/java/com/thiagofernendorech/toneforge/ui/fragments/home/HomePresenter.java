package com.thiagofernendorech.toneforge.ui.fragments.home;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.ui.base.BasePresenter;
import com.thiagofernendorech.toneforge.ui.navigation.NavigationController;
import com.thiagofernendorech.toneforge.PipelineManager;
import com.thiagofernendorech.toneforge.domain.models.AudioState;

/**
 * Presenter para o HomeFragment seguindo o padrão MVP
 * Versão otimizada sem atualizações automáticas
 */
public class HomePresenter extends BasePresenter<HomeContract.View> implements HomeContract.Presenter {

    private NavigationController navigationController;
    private AudioRepository audioRepository;
    private Context context;
    
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
    public void onLibraryClicked() {
        if (navigationController.isMainActivityAvailable()) {
            navigationController.navigateToLoopLibrary();
        }
    }

    @Override
    public void onPresetsClicked() {
        // Navegar para a tela de efeitos com foco em presets
        if (navigationController.isMainActivityAvailable()) {
            navigationController.navigateToEffects();
        }
    }

    @Override
    public void onWifiClicked() {
        // Atualizar status Wi-Fi apenas quando solicitado
        updateWifiStatus();
        ifViewAttached(HomeContract.View::showWifiDialog);
    }
    
    @Override
    public void onVolumeClicked() {
        ifViewAttached(HomeContract.View::showVolumeDialog);
    }
    
    @Override
    public void onPowerClicked() {
        // Atualizar status da bateria apenas quando solicitado
        updateBatteryStatus();
        ifViewAttached(HomeContract.View::showPowerDialog);
    }
    
    @Override
    public void onViewStarted() {
        // Atualizar título
        ifViewAttached(view -> view.updateTitle("ToneForge"));
        
        // Fazer uma atualização inicial (apenas uma vez)
        updateSystemStatus();
    }
    
    @Override
    public void onViewPaused() {
        // REMOVIDO: Parar atualizações (não há mais atualizações automáticas)
    }
    
    @Override
    public void onViewResumed() {
        // Atualizar status apenas uma vez ao resumir
        updateSystemStatus();
    }
    
    @Override
    public void onViewDestroyed() {
        // REMOVIDO: Parar atualizações e limpar handler (não há mais)
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
     * Atualiza o status da conexão Wi-Fi (apenas quando solicitado)
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
     * Atualiza o status da bateria (apenas quando solicitado)
     */
    private void updateBatteryStatus() {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                
                if (scale > 0) {
                    int batteryPct = (int)(level * 100f / scale);
                    boolean isCharging = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == 
                                       BatteryManager.BATTERY_STATUS_CHARGING;
                    
                    ifViewAttached(view -> view.updateBatteryStatus(batteryPct, isCharging));
                }
            }
        } catch (Exception e) {
            // Erro ao obter status da bateria
            ifViewAttached(view -> view.updateBatteryStatus(50, false)); // Valor padrão
        }
    }
    
    /**
     * Atualiza o estado do áudio (apenas quando solicitado)
     */
    private void updateAudioStatus() {
        try {
            PipelineManager pipelineManager = PipelineManager.getInstance();
            boolean isRunning = pipelineManager.isRunning();
            boolean hasError = pipelineManager.isError();
            
            // Criar um AudioState adequado baseado no status atual
            AudioState audioState = new AudioState();
            audioState.setPipelineRunning(isRunning);
            audioState.setPipelinePaused(pipelineManager.isPaused());
            
            // Não modificar a descrição diretamente, o método getStatusDescription() 
            // já calcula automaticamente baseado no estado
            
            ifViewAttached(view -> view.updateAudioState(audioState));
            
        } catch (Exception e) {
            // Erro ao obter status do áudio - criar um estado padrão (pipeline parado)
            AudioState errorState = new AudioState();
            errorState.setPipelineRunning(false);
            errorState.setPipelinePaused(false);
            
            ifViewAttached(view -> view.updateAudioState(errorState));
        }
    }
    
    // === Métodos de utilidade (mantidos para compatibilidade) ===
    
    public boolean isWifiEnabled() {
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                
            if (connectivityManager != null) {
                NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                return wifiInfo != null && wifiInfo.isConnected();
            }
        } catch (Exception e) {
            // Erro ao verificar Wi-Fi
        }
        return false;
    }
    
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
    
    public String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        
        info.append("Android: ").append(Build.VERSION.RELEASE).append("\n");
        info.append("API Level: ").append(Build.VERSION.SDK_INT).append("\n");
        info.append("Modelo: ").append(Build.MODEL).append("\n");
        info.append("Fabricante: ").append(Build.MANUFACTURER).append("\n");
        
        // Informações de conectividade
        info.append("Wi-Fi: ").append(isWifiEnabled() ? "Habilitado" : "Desabilitado").append("\n");
        info.append("Internet: ").append(hasInternetConnection() ? "Conectado" : "Desconectado");
        
        return info.toString();
    }
} 