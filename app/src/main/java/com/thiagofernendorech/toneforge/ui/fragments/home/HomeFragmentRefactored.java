package com.thiagofernendorech.toneforge.ui.fragments.home;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.thiagofernendorech.toneforge.R;
import com.thiagofernendorech.toneforge.MainActivity;
import com.thiagofernendorech.toneforge.ui.base.BaseFragment;
import com.thiagofernendorech.toneforge.ui.navigation.NavigationController;
import com.thiagofernendorech.toneforge.ui.widgets.HilavaAppButton;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.domain.models.AudioState;

/**
 * HomeFragment refatorado usando arquitetura MVP
 * Nova interface HILAVA inspirada no Lava ME 3
 */
public class HomeFragmentRefactored extends BaseFragment<HomePresenter> implements HomeContract.View {

    private static final String TAG = "HomeFragmentRefactored";

    // Design flag - usar nova interface HILAVA
    public static final int DESIGN_LEGACY = 0;
    public static final int DESIGN_LAVA = 1;
    public static final int DESIGN_HILAVA = 2;

    private int currentDesign = DESIGN_HILAVA; // Nova interface HILAVA por padrão

    // Views - HILAVA Design
    private TextView titleText;
    private View fxIndicator;
    private View recordingIndicator;
    private View pipelineIndicator;
    private TextView audioStatusText;
    private ImageView batteryIcon;
    private TextView batteryText;
    private CardView audioStatusCard;

    // Botões HILAVA
    private HilavaAppButton hilavaButtonTuner, hilavaButtonEffects, hilavaButtonLooper;
    private HilavaAppButton hilavaButtonMetronome, hilavaButtonRecorder, hilavaButtonLearning;
    private HilavaAppButton hilavaButtonSettings, hilavaButtonLibrary, hilavaButtonPresets;

    // Views - Legacy Design (mantidas para compatibilidade)
    private View btnTuner, btnEffects, btnLooper, btnMetronome;
    private View btnLearning, btnRecorder, btnSettings;
    private View btnWifi, btnVolume, btnPower;
    private ImageView wifiStatusIcon, batteryStatusIcon;

    // Animação do recording indicator
    private ObjectAnimator recordingPulseAnimator;

    @Override
    protected HomePresenter createPresenter() {
        NavigationController navigationController = NavigationController.getInstance();
        AudioRepository audioRepository = AudioRepository.getInstance(requireContext());
        return new HomePresenter(requireContext(), navigationController, audioRepository);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = getLayoutForDesign();
        View view = inflater.inflate(layoutId, container, false);

        initializeViews(view);
        setupClickListeners();
        setupAnimations();

        return view;
    }

    private int getLayoutForDesign() {
        switch (currentDesign) {
            case DESIGN_HILAVA:
                return R.layout.fragment_home_hilava;
            case DESIGN_LAVA:
                return R.layout.fragment_home_lava;
            case DESIGN_LEGACY:
            default:
                return R.layout.fragment_home;
        }
    }

    @Override
    protected void onViewReady() {
        super.onViewReady();
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
        startRecordingAnimation();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (presenter != null) {
            presenter.onViewPaused();
        }
        stopRecordingAnimation();
    }

    /**
     * Inicializa as views do fragment baseado no design atual
     */
    private void initializeViews(View view) {
        if (currentDesign == DESIGN_HILAVA) {
            initializeHilavaViews(view);
        } else {
            initializeLegacyViews(view);
        }
    }

    private void initializeHilavaViews(View view) {
        // Header e status
        titleText = view.findViewById(R.id.titleText);
        fxIndicator = view.findViewById(R.id.fxIndicator);
        recordingIndicator = view.findViewById(R.id.recordingIndicator);

        // Audio status card
        audioStatusCard = view.findViewById(R.id.audioStatusCard);
        pipelineIndicator = view.findViewById(R.id.pipelineIndicator);
        audioStatusText = view.findViewById(R.id.audioStatusText);
        batteryIcon = view.findViewById(R.id.batteryIcon);
        batteryText = view.findViewById(R.id.batteryText);

        // Botões HILAVA
        hilavaButtonTuner = view.findViewById(R.id.btnTuner);
        hilavaButtonEffects = view.findViewById(R.id.btnEffects);
        hilavaButtonLooper = view.findViewById(R.id.btnLooper);
        hilavaButtonMetronome = view.findViewById(R.id.btnMetronome);
        hilavaButtonRecorder = view.findViewById(R.id.btnRecorder);
        hilavaButtonLearning = view.findViewById(R.id.btnLearning);
        hilavaButtonSettings = view.findViewById(R.id.btnSettings);
        hilavaButtonLibrary = view.findViewById(R.id.btnLibrary);
        hilavaButtonPresets = view.findViewById(R.id.btnPresets);
    }

    private void initializeLegacyViews(View view) {
        // Botões principais (design legado)
        btnTuner = view.findViewById(R.id.btnTuner);
        btnEffects = view.findViewById(R.id.btnEffects);
        btnLooper = view.findViewById(R.id.btnLooper);
        btnMetronome = view.findViewById(R.id.btnMetronome);
        btnLearning = view.findViewById(R.id.btnLearning);
        btnRecorder = view.findViewById(R.id.btnRecorder);
        btnSettings = view.findViewById(R.id.btnSettings);

        // Botões do header (apenas no layout Lava)
        if (currentDesign == DESIGN_LAVA) {
            btnWifi = view.findViewById(R.id.btnWifi);
            btnVolume = view.findViewById(R.id.btnVolume);
        }
    }

    /**
     * Configura os listeners dos botões
     */
    private void setupClickListeners() {
        if (currentDesign == DESIGN_HILAVA) {
            setupHilavaClickListeners();
        } else {
            setupLegacyClickListeners();
        }
    }

    private void setupHilavaClickListeners() {
        if (hilavaButtonTuner != null) {
            hilavaButtonTuner.setOnClickListener(v -> presenter.onTunerClicked());
        }
        if (hilavaButtonEffects != null) {
            hilavaButtonEffects.setOnClickListener(v -> presenter.onEffectsClicked());
        }
        if (hilavaButtonLooper != null) {
            hilavaButtonLooper.setOnClickListener(v -> presenter.onLooperClicked());
        }
        if (hilavaButtonMetronome != null) {
            hilavaButtonMetronome.setOnClickListener(v -> presenter.onMetronomeClicked());
        }
        if (hilavaButtonRecorder != null) {
            hilavaButtonRecorder.setOnClickListener(v -> presenter.onRecorderClicked());
        }
        if (hilavaButtonLearning != null) {
            hilavaButtonLearning.setOnClickListener(v -> presenter.onLearningClicked());
        }
        if (hilavaButtonSettings != null) {
            hilavaButtonSettings.setOnClickListener(v -> presenter.onSettingsClicked());
        }
        if (hilavaButtonLibrary != null) {
            hilavaButtonLibrary.setOnClickListener(v -> presenter.onLibraryClicked());
        }
        if (hilavaButtonPresets != null) {
            hilavaButtonPresets.setOnClickListener(v -> presenter.onPresetsClicked());
        }
    }

    private void setupLegacyClickListeners() {
        // Botões principais (com debounce para prevenir toques duplos)
        if (btnTuner != null) {
            btnTuner.setOnClickListener(debounced(v -> presenter.onTunerClicked()));
        }


        if (btnEffects != null) {
            btnEffects.setOnClickListener(debounced(v -> presenter.onEffectsClicked()));
        }


        if (btnLooper != null) {
            btnLooper.setOnClickListener(debounced(v -> presenter.onLooperClicked()));
        }


        if (btnMetronome != null) {
            btnMetronome.setOnClickListener(debounced(v -> presenter.onMetronomeClicked()));
        }


        if (btnLearning != null) {
            btnLearning.setOnClickListener(debounced(v -> presenter.onLearningClicked()));
        }


        if (btnRecorder != null) {
            btnRecorder.setOnClickListener(debounced(v -> presenter.onRecorderClicked()));
        }


        if (btnSettings != null) {
            btnSettings.setOnClickListener(debounced(v -> presenter.onSettingsClicked()));
        }
        if (btnWifi != null) {
            btnWifi.setOnClickListener(v -> presenter.onWifiClicked());
        }
        if (btnVolume != null) {
            btnVolume.setOnClickListener(v -> presenter.onVolumeClicked());
        }
    }

    private void setupAnimations() {
        // Configurar animação de pulse para o indicador de gravação
        if (recordingIndicator != null) {
            recordingPulseAnimator = ObjectAnimator.ofFloat(recordingIndicator, "alpha", 1f, 0.3f);
            recordingPulseAnimator.setDuration(500);
            recordingPulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            recordingPulseAnimator.setRepeatMode(ObjectAnimator.REVERSE);
            recordingPulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        }
    }

    private void startRecordingAnimation() {
        if (recordingPulseAnimator != null && recordingIndicator != null
                && recordingIndicator.getVisibility() == View.VISIBLE) {
            recordingPulseAnimator.start();
        }
    }

    private void stopRecordingAnimation() {
        if (recordingPulseAnimator != null) {
            recordingPulseAnimator.cancel();
        }
    }

    // === Implementação da interface HomeContract.View ===

    @Override
    public void updateWifiStatus(boolean connected) {
        // Na interface HILAVA, WiFi não é exibido diretamente
        if (wifiStatusIcon != null) {
            int tintColor = connected ?
                    getResources().getColor(R.color.hilava_practice) :
                    getResources().getColor(R.color.hilava_recorder);
            wifiStatusIcon.setColorFilter(tintColor);
        }

        if (btnWifi != null) {
            ImageView wifiIcon = btnWifi.findViewById(R.id.wifiIcon);
            if (wifiIcon != null) {
                int tintColor = connected ?
                    getResources().getColor(R.color.green) :
                    getResources().getColor(R.color.red);
                wifiIcon.setColorFilter(tintColor);
            }
        }
    }

    @Override
    public void updateBatteryStatus(int level, boolean isCharging) {
        if (currentDesign == DESIGN_HILAVA) {
            updateHilavaBatteryStatus(level, isCharging);
        } else {
            updateLegacyBatteryStatus(level, isCharging);
        }
    }

    private void updateHilavaBatteryStatus(int level, boolean isCharging) {
        if (batteryText != null) {
            batteryText.setText(level + "%");
        }

        if (batteryIcon != null) {
            int tintColor;
            if (isCharging) {
                tintColor = getResources().getColor(R.color.hilava_practice);
            } else if (level < 20) {
                tintColor = getResources().getColor(R.color.hilava_recorder);
            } else if (level < 50) {
                tintColor = getResources().getColor(R.color.hilava_tuner);
            } else {
                tintColor = getResources().getColor(R.color.hilava_practice);
            }
            batteryIcon.setColorFilter(tintColor);
        }
    }

    private void updateLegacyBatteryStatus(int level, boolean isCharging) {
        if (batteryStatusIcon != null) {
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
            batteryStatusIcon.setColorFilter(tintColor);
        }
    }

    @Override
    public void updateAudioState(AudioState audioState) {
        if (audioState == null) {
            Log.w(TAG, "AudioState é null - usando estado padrão");
            setButtonsEnabled(false);
            updateAudioStatusUI(false, "Inicializando...");
            return;
        }

        boolean isRunning = audioState.isPipelineRunning();
        setButtonsEnabled(true); // Botões sempre habilitados

        // Atualizar UI de status
        String statusText = audioState.getStatusDescription();
        updateAudioStatusUI(isRunning, statusText);
    }

    private void updateAudioStatusUI(boolean isRunning, String statusText) {
        if (currentDesign == DESIGN_HILAVA) {
            // Atualizar indicador de pipeline
            if (pipelineIndicator != null) {
                pipelineIndicator.setActivated(isRunning);
            }

            // Atualizar texto de status
            if (audioStatusText != null) {
                audioStatusText.setText(isRunning ? "Áudio ativo" : statusText);
            }
        }
    }

    @Override
    public void updateTitle(String title) {
        if (currentDesign == DESIGN_HILAVA && titleText != null) {
            titleText.setText(title);
        }
        updateHeaderTitle(title);
    }

    @Override
    public void showWifiDialog() {
        // Implementação delegada para SystemStatusController
    }

    @Override
    public void showVolumeDialog() {
        // Implementação delegada para SystemStatusController
    }

    @Override
    public void showPowerDialog() {
        // Implementação delegada para SystemStatusController
    }

    // === Métodos públicos para controle de indicadores ===

    /**
     * Mostra/esconde o indicador de FX ativo
     */
    public void setFxActive(boolean active) {
        if (fxIndicator != null) {
            fxIndicator.setVisibility(active ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Mostra/esconde o indicador de gravação
     */
    public void setRecordingActive(boolean recording) {
        if (recordingIndicator != null) {
            recordingIndicator.setVisibility(recording ? View.VISIBLE : View.GONE);
            if (recording) {
                startRecordingAnimation();
            } else {
                stopRecordingAnimation();
            }
        }
    }

    // === Métodos utilitários ===

    /**
     * Ativa/desativa os botões baseado no estado do áudio
     */
    private void setButtonsEnabled(boolean enabled) {
        float alpha = enabled ? 1.0f : 0.7f;

        if (currentDesign == DESIGN_HILAVA) {
            if (hilavaButtonTuner != null) hilavaButtonTuner.setAlpha(alpha);
            if (hilavaButtonEffects != null) hilavaButtonEffects.setAlpha(alpha);
            if (hilavaButtonLooper != null) hilavaButtonLooper.setAlpha(alpha);
            if (hilavaButtonMetronome != null) hilavaButtonMetronome.setAlpha(alpha);
            if (hilavaButtonRecorder != null) hilavaButtonRecorder.setAlpha(alpha);
            if (hilavaButtonLearning != null) hilavaButtonLearning.setAlpha(alpha);
            if (hilavaButtonSettings != null) hilavaButtonSettings.setAlpha(alpha);
            if (hilavaButtonLibrary != null) hilavaButtonLibrary.setAlpha(alpha);
            if (hilavaButtonPresets != null) hilavaButtonPresets.setAlpha(alpha);
        } else {
            if (btnTuner != null) btnTuner.setAlpha(alpha);
            if (btnEffects != null) btnEffects.setAlpha(alpha);
            if (btnLooper != null) btnLooper.setAlpha(alpha);
            if (btnMetronome != null) btnMetronome.setAlpha(alpha);
            if (btnLearning != null) btnLearning.setAlpha(alpha);
            if (btnRecorder != null) btnRecorder.setAlpha(alpha);
            if (btnSettings != null) btnSettings.setAlpha(alpha);
        }
    }

    /**
     * Altera o design da interface
     * @param design DESIGN_LEGACY, DESIGN_LAVA ou DESIGN_HILAVA
     */
    public void setDesign(int design) {
        if (design >= DESIGN_LEGACY && design <= DESIGN_HILAVA) {
            this.currentDesign = design;
        }
    }

    /**
     * Retorna o design atual
     */
    public int getCurrentDesign() {
        return currentDesign;
    }

    /**
     * Verifica se está usando o design HILAVA
     */
    public boolean isUsingHilavaDesign() {
        return currentDesign == DESIGN_HILAVA;
    }

    /**
     * Método para alternar entre os designs (mantido para compatibilidade)
     */
    public void setUseLavaDesign(boolean useLavaDesign) {
        this.currentDesign = useLavaDesign ? DESIGN_LAVA : DESIGN_LEGACY;
    }

    /**
     * Verifica se está usando o design Lava (mantido para compatibilidade)
     */
    public boolean isUsingLavaDesign() {
        return currentDesign == DESIGN_LAVA;
    }
}
