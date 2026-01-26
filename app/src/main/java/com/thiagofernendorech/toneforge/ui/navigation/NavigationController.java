package com.thiagofernendorech.toneforge.ui.navigation;

import androidx.fragment.app.Fragment;
import com.thiagofernendorech.toneforge.MainActivity;
import com.thiagofernendorech.toneforge.ui.fragments.effects.EffectsFragmentRefactored;
import com.thiagofernendorech.toneforge.HomeFragment;
import com.thiagofernendorech.toneforge.LooperFragment;
import com.thiagofernendorech.toneforge.TunerFragment;
import com.thiagofernendorech.toneforge.ui.fragments.tuner.TunerFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.metronome.MetronomeFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.recorder.RecorderFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.learning.LearningFragmentRefactored;
import com.thiagofernendorech.toneforge.RecorderFragment;
import com.thiagofernendorech.toneforge.ui.fragments.settings.SettingsFragmentRefactored;
import com.thiagofernendorech.toneforge.LoopLibraryFragment;
import com.thiagofernendorech.toneforge.ui.activities.BaseActivity.TransitionType;
import java.lang.ref.WeakReference;

/**
 * Controlador de navegação centralizado
 * Gerencia a navegação entre fragments de forma desacoplada
 */
public class NavigationController {
    
    private static NavigationController instance;
    private WeakReference<MainActivity> mainActivityRef;
    
    /**
     * Obtém a instância singleton do NavigationController
     * @return instância do NavigationController
     */
    public static NavigationController getInstance() {
        if (instance == null) {
            instance = new NavigationController();
        }
        return instance;
    }
    
    /**
     * Inicializa o controller com a MainActivity
     * @param activity MainActivity
     */
    public void init(MainActivity activity) {
        this.mainActivityRef = new WeakReference<>(activity);
    }
    
    /**
     * Navega para o fragment Home (com slide da esquerda - voltar)
     */
    public void navigateToHome() {
        MainActivity activity = getActivity();
        if (activity != null) {
            activity.hideHeader(); // Home tem seu próprio status card
            activity.loadFragment(new HomeFragment(), TransitionType.SLIDE_LEFT);
            activity.updateHeaderTitle("ToneForge");
        }
    }

    /**
     * Navega para o fragment de Efeitos
     */
    public void navigateToEffects() {
        navigateWithHeader(new EffectsFragmentRefactored(), "Efeitos");
    }

    /**
     * Navega para o fragment de looper
     */
    public void navigateToLooper() {
        MainActivity activity = getActivity();
        if (activity != null) {
            activity.showHeader();
            com.thiagofernendorech.toneforge.ui.fragments.looper.LooperFragmentRefactored looperFragment =
                new com.thiagofernendorech.toneforge.ui.fragments.looper.LooperFragmentRefactored();
            activity.loadFragment(looperFragment, TransitionType.SLIDE_RIGHT);
            activity.updateHeaderTitle("Looper");
        }
    }

    /**
     * Navega para o fragment do Afinador
     */
    public void navigateToTuner() {
        MainActivity activity = getActivity();
        if (activity != null) {
            activity.showHeader();
            TunerFragmentRefactored tunerFragment = new TunerFragmentRefactored();
            activity.loadFragment(tunerFragment, TransitionType.SLIDE_RIGHT);
            activity.updateHeaderTitle("Afinador");
        }
    }

    /**
     * Navega para o fragment do Metrônomo
     */
    public void navigateToMetronome() {
        MainActivity activity = getActivity();
        if (activity != null) {
            activity.showHeader();
            MetronomeFragmentRefactored metronomeFragment = new MetronomeFragmentRefactored();
            activity.loadFragment(metronomeFragment, TransitionType.SLIDE_RIGHT);
            activity.updateHeaderTitle("Metrônomo");
        }
    }

    /**
     * Navega para o fragment de Aprendizado
     */
    public void navigateToLearning() {
        navigateWithHeader(new LearningFragmentRefactored(), "Aprendizado");
    }

    /**
     * Navega para o fragment do Gravador
     */
    public void navigateToRecorder() {
        MainActivity activity = getActivity();
        if (activity != null) {
            activity.showHeader();
            RecorderFragmentRefactored recorderFragment = new RecorderFragmentRefactored();
            activity.loadFragment(recorderFragment, TransitionType.SLIDE_RIGHT);
            activity.updateHeaderTitle("Gravador");
        }
    }

    /**
     * Navega para o fragment de Configurações
     */
    public void navigateToSettings() {
        navigateWithHeader(new SettingsFragmentRefactored(), "Configurações");
    }

    /**
     * Navega para o fragment da Biblioteca de Loops
     */
    public void navigateToLoopLibrary() {
        navigateWithHeader(new LoopLibraryFragment(), "Biblioteca de Loops");
    }

    /**
     * Navega para um fragment mostrando o header
     * @param fragment fragment a ser carregado
     * @param title título do header
     */
    private void navigateWithHeader(Fragment fragment, String title) {
        MainActivity activity = getActivity();
        if (activity != null) {
            activity.showHeader();
            activity.loadFragment(fragment, TransitionType.SLIDE_RIGHT);
            activity.updateHeaderTitle(title);
        }
    }
    
    /**
     * Navega para trás na pilha de fragments
     */
    public void navigateBack() {
        MainActivity activity = getActivity();
        if (activity != null) {
            activity.onBackPressed();
        }
    }
    
    /**
     * Carrega um fragment com título (usa transição padrão fade)
     * @param fragment fragment a ser carregado
     * @param title título do header
     */
    public void loadFragment(Fragment fragment, String title) {
        MainActivity activity = getActivity();
        if (activity != null) {
            activity.loadFragment(fragment);
            activity.updateHeaderTitle(title);
        }
    }

    /**
     * Carrega um fragment com título e tipo de transição específico
     * @param fragment fragment a ser carregado
     * @param title título do header
     * @param transitionType tipo de transição
     */
    public void loadFragmentWithTransition(Fragment fragment, String title, TransitionType transitionType) {
        MainActivity activity = getActivity();
        if (activity != null) {
            activity.loadFragment(fragment, transitionType);
            activity.updateHeaderTitle(title);
        }
    }
    
    /**
     * Atualiza apenas o título do header
     * @param title novo título
     */
    public void updateTitle(String title) {
        MainActivity activity = getActivity();
        if (activity != null) {
            activity.updateHeaderTitle(title);
        }
    }
    
    /**
     * Verifica se a MainActivity está disponível
     * @return true se a MainActivity está disponível
     */
    public boolean isMainActivityAvailable() {
        return getActivity() != null;
    }
    
    /**
     * Obtém a MainActivity atual
     * @return MainActivity ou null se não estiver disponível
     */
    private MainActivity getActivity() {
        return mainActivityRef != null ? mainActivityRef.get() : null;
    }
    
    /**
     * Limpa a referência da MainActivity
     */
    public void clear() {
        if (mainActivityRef != null) {
            mainActivityRef.clear();
            mainActivityRef = null;
        }
    }
} 