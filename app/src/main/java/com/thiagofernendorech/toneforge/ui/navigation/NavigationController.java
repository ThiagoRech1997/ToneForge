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
     * Navega para o fragment Home
     */
    public void navigateToHome() {
        loadFragment(new HomeFragment(), "ToneForge");
    }
    
    /**
     * Navega para o fragment de Efeitos
     */
    public void navigateToEffects() {
        loadFragment(new EffectsFragmentRefactored(), "Efeitos");
    }
    
    /**
     * Navega para o fragment de looper
     */
    public void navigateToLooper() {
        MainActivity activity = getActivity();
        if (activity != null) {
            com.thiagofernendorech.toneforge.ui.fragments.looper.LooperFragmentRefactored looperFragment = 
                new com.thiagofernendorech.toneforge.ui.fragments.looper.LooperFragmentRefactored();
            activity.loadFragment(looperFragment);
            activity.updateHeaderTitle("Looper");
        }
    }
    
    /**
     * Navega para o fragment do Afinador
     */
    public void navigateToTuner() {
        MainActivity activity = getActivity();
        if (activity != null) {
            TunerFragmentRefactored tunerFragment = new TunerFragmentRefactored();
            activity.loadFragment(tunerFragment);
            activity.updateHeaderTitle("Afinador");
        }
    }
    
    /**
     * Navega para o fragment do Metrônomo
     */
    public void navigateToMetronome() {
        MainActivity activity = getActivity();
        if (activity != null) {
            MetronomeFragmentRefactored metronomeFragment = new MetronomeFragmentRefactored();
            activity.loadFragment(metronomeFragment);
            activity.updateHeaderTitle("Metrônomo");
        }
    }
    
    /**
     * Navega para o fragment de Aprendizado
     */
    public void navigateToLearning() {
        loadFragment(new LearningFragmentRefactored(), "Aprendizado");
    }
    
    /**
     * Navega para o fragment do Gravador
     */
    public void navigateToRecorder() {
        MainActivity activity = getActivity();
        if (activity != null) {
            RecorderFragmentRefactored recorderFragment = new RecorderFragmentRefactored();
            activity.loadFragment(recorderFragment);
            activity.updateHeaderTitle("Gravador");
        }
    }
    
    /**
     * Navega para o fragment de Configurações
     */
    public void navigateToSettings() {
        loadFragment(new SettingsFragmentRefactored(), "Configurações");
    }
    
    /**
     * Navega para o fragment da Biblioteca de Loops
     */
    public void navigateToLoopLibrary() {
        loadFragment(new LoopLibraryFragment(), "Biblioteca de Loops");
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
     * Carrega um fragment com título
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