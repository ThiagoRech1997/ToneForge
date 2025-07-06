package com.thiagofernendorech.toneforge.ui.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thiagofernendorech.toneforge.R;
import com.thiagofernendorech.toneforge.MainActivity;
import com.thiagofernendorech.toneforge.ui.base.BaseFragment;
import com.thiagofernendorech.toneforge.ui.navigation.NavigationController;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.domain.models.AudioState;

/**
 * HomeFragment refatorado usando arquitetura MVP
 * Versão limpa e desacoplada do fragment original
 */
public class HomeFragmentRefactored extends BaseFragment<HomePresenter> implements HomeContract.View {
    
    // Views
    private View btnTuner, btnEffects, btnLooper, btnMetronome;
    private View btnLearning, btnRecorder, btnSettings;
    private View btnWifi, btnVolume, btnPower;
    private ImageView wifiStatusIcon, batteryStatusIcon;
    
    // Flag para controlar qual layout usar
    private boolean useLavaDesign = true;
    
    @Override
    protected HomePresenter createPresenter() {
        NavigationController navigationController = NavigationController.getInstance();
        AudioRepository audioRepository = AudioRepository.getInstance(requireContext());
        return new HomePresenter(requireContext(), navigationController, audioRepository);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Escolher layout baseado na flag
        int layoutId = useLavaDesign ? R.layout.fragment_home_lava : R.layout.fragment_home;
        View view = inflater.inflate(layoutId, container, false);
        
        initializeViews(view);
        setupClickListeners();
        
        return view;
    }
    
    @Override
    protected void onViewReady() {
        super.onViewReady();
        // Notificar presenter que a view está pronta
        if (presenter != null) {
            presenter.onViewStarted();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.onViewResumed();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (presenter != null) {
            presenter.onViewPaused();
        }
    }
    
    /**
     * Inicializa as views do fragment
     */
    private void initializeViews(View view) {
        // Botões principais
        btnTuner = view.findViewById(R.id.btnTuner);
        btnEffects = view.findViewById(R.id.btnEffects);
        btnLooper = view.findViewById(R.id.btnLooper);
        btnMetronome = view.findViewById(R.id.btnMetronome);
        btnLearning = view.findViewById(R.id.btnLearning);
        btnRecorder = view.findViewById(R.id.btnRecorder);
        btnSettings = view.findViewById(R.id.btnSettings);
        
        // Botões do header (apenas no layout Lava)
        if (useLavaDesign) {
            btnWifi = view.findViewById(R.id.btnWifi);
            btnVolume = view.findViewById(R.id.btnVolume);
            // btnPower não existe no layout fragment_home_lava.xml
            // btnPower = view.findViewById(R.id.btnPower);
            
            // Ícones de status (se existirem no layout)
            // wifiStatusIcon = view.findViewById(R.id.wifiStatusIcon); // ID não existe
            // batteryStatusIcon = view.findViewById(R.id.batteryStatusIcon); // ID não existe
        }
    }
    
    /**
     * Configura os listeners dos botões
     */
    private void setupClickListeners() {
        // Botões principais
        if (btnTuner != null) {
            btnTuner.setOnClickListener(v -> presenter.onTunerClicked());
        }
        
        if (btnEffects != null) {
            btnEffects.setOnClickListener(v -> presenter.onEffectsClicked());
        }
        
        if (btnLooper != null) {
            btnLooper.setOnClickListener(v -> presenter.onLooperClicked());
        }
        
        if (btnMetronome != null) {
            btnMetronome.setOnClickListener(v -> presenter.onMetronomeClicked());
        }
        
        if (btnLearning != null) {
            btnLearning.setOnClickListener(v -> presenter.onLearningClicked());
        }
        
        if (btnRecorder != null) {
            btnRecorder.setOnClickListener(v -> presenter.onRecorderClicked());
        }
        
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> presenter.onSettingsClicked());
        }
        
        // Botões do header (apenas no layout Lava)
        if (useLavaDesign) {
            if (btnWifi != null) {
                btnWifi.setOnClickListener(v -> presenter.onWifiClicked());
            }
            
            if (btnVolume != null) {
                btnVolume.setOnClickListener(v -> presenter.onVolumeClicked());
            }
            
            // btnPower não existe no layout fragment_home_lava.xml
            // if (btnPower != null) {
            //     btnPower.setOnClickListener(v -> presenter.onPowerClicked());
            // }
        }
    }
    
    // === Implementação da interface HomeContract.View ===
    
    @Override
    public void updateWifiStatus(boolean connected) {
        if (wifiStatusIcon != null) {
            int iconResource = connected ? R.drawable.ic_wifi : R.drawable.ic_wifi;
            int tintColor = connected ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red);
            
            wifiStatusIcon.setImageResource(iconResource);
            wifiStatusIcon.setColorFilter(tintColor);
        }
        
        if (btnWifi != null) {
            // Encontrar o ImageView dentro do LinearLayout
            ImageView wifiIcon = btnWifi.findViewById(R.id.wifiIcon);
            if (wifiIcon != null) {
                int tintColor = connected ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red);
                wifiIcon.setColorFilter(tintColor);
            }
        }
    }
    
    @Override
    public void updateBatteryStatus(int level, boolean isCharging) {
        if (batteryStatusIcon != null) {
            int iconResource = isCharging ? R.drawable.ic_battery : R.drawable.ic_battery;
            int tintColor;
            
            if (isCharging) {
                tintColor = getResources().getColor(R.color.green);
            } else if (level < 20) {
                tintColor = getResources().getColor(R.color.red);
            } else if (level < 50) {
                tintColor = getResources().getColor(android.R.color.holo_orange_light);
            } else {
                tintColor = getResources().getColor(R.color.white);
            }
            
            batteryStatusIcon.setImageResource(iconResource);
            batteryStatusIcon.setColorFilter(tintColor);
        }
        
        // btnPower não existe no layout fragment_home_lava.xml
        // if (btnPower != null) {
        //     // Atualizar ícone do botão power se necessário
        // }
    }
    
    @Override
    public void updateAudioState(AudioState audioState) {
        // Atualizar UI baseado no estado do áudio
        // Por exemplo, mudar cores dos botões baseado no estado
        
        if (audioState.isPipelineRunning()) {
            // Pipeline ativo - botões podem ficar com cor normal
            setButtonsEnabled(true);
        } else {
            // Pipeline inativo - botões podem ficar com cor mais fraca
            setButtonsEnabled(false);
        }
        
        // Mostrar informações de status se necessário
        String statusDescription = audioState.getStatusDescription();
        if (!statusDescription.equals("Pronto")) {
            showMessage(statusDescription);
        }
    }
    
    @Override
    public void updateTitle(String title) {
        updateHeaderTitle(title);
    }
    
    @Override
    public void showWifiDialog() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showWifiStatusDialog();
        }
    }
    
    @Override
    public void showVolumeDialog() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showVolumeControlDialog();
        }
    }
    
    @Override
    public void showPowerDialog() {
        if (getActivity() instanceof MainActivity) {
            // ((MainActivity) getActivity()).showPowerOptionsDialog(); // Método privado
        }
    }
    
    // === Métodos utilitários ===
    
    /**
     * Ativa/desativa os botões baseado no estado do áudio
     * @param enabled true para ativar botões
     */
    private void setButtonsEnabled(boolean enabled) {
        float alpha = enabled ? 1.0f : 0.6f;
        
        if (btnTuner != null) btnTuner.setAlpha(alpha);
        if (btnEffects != null) btnEffects.setAlpha(alpha);
        if (btnLooper != null) btnLooper.setAlpha(alpha);
        if (btnMetronome != null) btnMetronome.setAlpha(alpha);
        if (btnLearning != null) btnLearning.setAlpha(alpha);
        if (btnRecorder != null) btnRecorder.setAlpha(alpha);
        if (btnSettings != null) btnSettings.setAlpha(alpha);
    }
    
    /**
     * Método para alternar entre os designs
     * @param useLavaDesign true para usar layout Lava
     */
    public void setUseLavaDesign(boolean useLavaDesign) {
        this.useLavaDesign = useLavaDesign;
    }
    
    /**
     * Verifica se está usando o design Lava
     * @return true se está usando design Lava
     */
    public boolean isUsingLavaDesign() {
        return useLavaDesign;
    }
} 