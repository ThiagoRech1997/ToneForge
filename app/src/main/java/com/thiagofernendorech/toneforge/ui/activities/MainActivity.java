package com.thiagofernendorech.toneforge;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import java.util.List;
import com.thiagofernendorech.toneforge.infrastructure.ui.DebounceClickListener;

// Clean Architecture imports
import com.thiagofernendorech.toneforge.ui.activities.BaseActivity;
import com.thiagofernendorech.toneforge.ui.navigation.NavigationController;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.domain.usecases.StartAudioPipelineUseCase;
import com.thiagofernendorech.toneforge.domain.interfaces.AudioEngineInterface;
import com.thiagofernendorech.toneforge.domain.interfaces.PermissionInterface;
import com.thiagofernendorech.toneforge.infrastructure.adapters.AudioEngineAdapter;
import com.thiagofernendorech.toneforge.infrastructure.adapters.PermissionManagerAdapter;

// UI Components
import com.thiagofernendorech.toneforge.ui.components.SystemStatusController;
import com.thiagofernendorech.toneforge.ui.components.AudioInitializer;

// Refactored Fragments
import com.thiagofernendorech.toneforge.ui.fragments.home.HomeFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.effects.EffectsFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.looper.LooperFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.tuner.TunerFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.metronome.MetronomeFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.recorder.RecorderFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.looplibrary.LoopLibraryFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.learning.LearningFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.settings.SettingsFragmentRefactored;

// Legacy Fragments (still needed)
import com.thiagofernendorech.toneforge.PedalboardFragment;

/**
 * MainActivity refatorada seguindo princípios de Clean Code e Clean Architecture
 * Responsabilidades reduzidas: apenas coordenação de alto nível
 */
public class MainActivity extends BaseActivity {
    
    private static final String TAG = "MainActivity";
    
    private StateRecoveryManager stateRecoveryManager;
    private LatencyManager latencyManager;
    private ViewGroup headerContainer;
    private TextView headerTitle;
    private ImageView btnWifi;
    private ImageView btnVolume;
    private ImageView btnPower;

    // Clean Architecture components
    private NavigationController navigationController;
    private AudioRepository audioRepository;
    private SystemStatusController systemStatusController;
    private AudioInitializer audioInitializer;

    // Header animation duration
    private static final int HEADER_ANIMATION_DURATION = 200;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            LogManager.d(TAG, "Iniciando onCreate");
            setContentView(R.layout.activity_main);
            
            // Inicializar views primeiro
            initializeViews();
            
            // Inicializar managers
            initializeManagers();
            
            // Inicializar componentes de áudio
            initializeAudioComponents();
            
            // Configurar UI
            setupUI();
            
            LogManager.d(TAG, "onCreate concluído com sucesso");
            
        } catch (Exception e) {
            LogManager.e(TAG, "Erro crítico no onCreate: " + e.getMessage(), e);
            handleInitializationError(e);
        }
    }
    
    @Override
    protected int getFragmentContainerId() {
        return R.id.fragmentContainer;
    }
    
    @Override
    protected void requestPermissions() {
        PermissionManager.requestRequiredPermissions(this, new PermissionManager.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                MainActivity.this.onPermissionsGranted();
            }
            
            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                MainActivity.this.onPermissionsDenied(deniedPermissions);
            }
            
            @Override
            public void onPermissionExplanationNeeded(List<String> permissions) {
                MainActivity.this.onPermissionExplanationNeeded(permissions);
            }
        });
    }
    
    @Override
    protected boolean shouldAddToBackStack(Fragment fragment) {
        // Não adicionar HomeFragment à pilha
        return !(fragment instanceof HomeFragmentRefactored);
    }
    
    private void initializeViews() {
        try {
            LogManager.d(TAG, "Inicializando views");
            headerContainer = findViewById(R.id.headerContainer);
            headerTitle = findViewById(R.id.headerTitle);
            btnWifi = findViewById(R.id.btnWifi);
            btnVolume = findViewById(R.id.btnVolume);
            btnPower = findViewById(R.id.btnPower);

            LogManager.d(TAG, "Views inicializadas com sucesso");
        } catch (Exception e) {
            LogManager.e(TAG, "Erro ao inicializar views: " + e.getMessage(), e);
        }
    }
    
    private void initializeManagers() {
        try {
            LogManager.d(TAG, "Inicializando managers");
            
            // Inicializar managers
            latencyManager = LatencyManager.getInstance(this);
            stateRecoveryManager = StateRecoveryManager.getInstance(this);
            
            // Inicializar nova arquitetura
            navigationController = NavigationController.getInstance();
            navigationController.init(this);
            audioRepository = AudioRepository.getInstance(this);
            
            LogManager.d(TAG, "Managers inicializados com sucesso");
        } catch (Exception e) {
            LogManager.e(TAG, "Erro ao inicializar managers: " + e.getMessage(), e);
        }
    }
    
    private void initializeAudioComponents() {
        try {
            LogManager.i(TAG, "Inicializando componentes de áudio");
            
            // Usar AudioInitializer com Clean Architecture
            audioInitializer = new AudioInitializer(this);
            audioInitializer.initializeAudioComponents();
            
            // Verificar permissões e inicializar se necessário
            checkAndRequestPermissions();
            
            // Recuperar estado salvo
            if (stateRecoveryManager != null) {
                stateRecoveryManager.restoreState();
            }
            
            LogManager.d(TAG, "Componentes de áudio inicializados");
        } catch (Exception e) {
            LogManager.e(TAG, "Erro ao inicializar componentes de áudio: " + e.getMessage(), e);
        }
    }
    
    private void setupUI() {
        try {
            LogManager.d(TAG, "Configurando UI");

            // Configurar controladores UI
            systemStatusController = new SystemStatusController(this, btnWifi, btnVolume, btnPower);

            // Configurar navegação
            setupNavigation();

            // Esconder header na Home (já tem status card próprio)
            if (headerContainer != null) {
                headerContainer.setVisibility(View.GONE);
            }

            // Carregar fragment inicial (Home)
            loadFragment(new HomeFragmentRefactored(), TransitionType.NONE);

            LogManager.d(TAG, "UI configurada com sucesso");
        } catch (Exception e) {
            LogManager.e(TAG, "Erro ao configurar UI: " + e.getMessage(), e);
        }
    }
    
    private void handleInitializationError(Exception e) {
        try {
            Toast.makeText(this, "Erro na inicialização do app. Verifique os logs.", Toast.LENGTH_LONG).show();
            
            if (findViewById(R.id.fragmentContainer) == null) {
                LogManager.e(TAG, "Layout não carregado corretamente");
            }
        } catch (Exception recoveryError) {
            LogManager.e(TAG, "Erro na recuperação: " + recoveryError.getMessage(), recoveryError);
        }
    }
    
    private void setupNavigation() {
        // Botão Home
        findViewById(R.id.btnHome).setOnClickListener(new DebounceClickListener(v -> {
            hideHeader();
            loadFragment(new HomeFragmentRefactored(), TransitionType.SLIDE_LEFT);
            updateHeaderTitle("ToneForge");
        }));

        // Botão Pedaleira
        findViewById(R.id.btnPedalboard).setOnClickListener(new DebounceClickListener(v -> {
            showHeader();
            loadFragment(new PedalboardFragment(), TransitionType.SLIDE_RIGHT);
            updateHeaderTitle("Pedaleira");
        }));
    }
    
    public void updateHeaderTitle(String title) {
        if (headerTitle != null) {
            headerTitle.setText(title);
        }
    }

    /**
     * Mostra o header com animação
     */
    public void showHeader() {
        if (headerContainer != null && headerContainer.getVisibility() != View.VISIBLE) {
            headerContainer.setAlpha(0f);
            headerContainer.setVisibility(View.VISIBLE);
            headerContainer.animate()
                .alpha(1f)
                .setDuration(HEADER_ANIMATION_DURATION)
                .setListener(null)
                .start();
        }
    }

    /**
     * Esconde o header com animação
     */
    public void hideHeader() {
        if (headerContainer != null && headerContainer.getVisibility() == View.VISIBLE) {
            headerContainer.animate()
                .alpha(0f)
                .setDuration(HEADER_ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        headerContainer.setVisibility(View.GONE);
                    }
                })
                .start();
        }
    }

    /**
     * Define a visibilidade do header (com animação)
     * @param visible true para mostrar, false para esconder
     */
    public void setHeaderVisible(boolean visible) {
        if (visible) {
            showHeader();
        } else {
            hideHeader();
        }
    }

    /**
     * Verifica se o header está visível
     * @return true se visível
     */
    public boolean isHeaderVisible() {
        return headerContainer != null && headerContainer.getVisibility() == View.VISIBLE;
    }

    // Callbacks de permissão sobrescrevem os da BaseActivity
    @Override
    public void onPermissionsGranted() {
        super.onPermissionsGranted();
        Toast.makeText(this, "Permissões concedidas", Toast.LENGTH_SHORT).show();
        
        // Inicializar pipeline se permissões foram concedidas
        if (audioInitializer != null) {
            audioInitializer.startAudioPipeline();
        }
    }
    
    @Override
    public void onPermissionsDenied(List<String> deniedPermissions) {
        super.onPermissionsDenied(deniedPermissions);
        Toast.makeText(this, "Algumas permissões foram negadas", Toast.LENGTH_LONG).show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Recuperar estado se necessário
        if (stateRecoveryManager != null) {
            stateRecoveryManager.restoreState();
        }
        
        // Atualizar status icons
        if (systemStatusController != null) {
            systemStatusController.updateStatusIcons();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Salvar estado atual
        if (stateRecoveryManager != null) {
            stateRecoveryManager.saveCurrentState();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Limpar recursos
        if (navigationController != null) {
            navigationController.clear();
        }
        if (audioRepository != null) {
            audioRepository.cleanup();
        }
        if (audioInitializer != null) {
            audioInitializer.cleanup();
        }
    }
    

}