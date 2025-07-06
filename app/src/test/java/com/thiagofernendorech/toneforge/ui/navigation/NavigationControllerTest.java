package com.thiagofernendorech.toneforge.ui.navigation;

import androidx.fragment.app.Fragment;
import com.thiagofernendorech.toneforge.MainActivity;
import com.thiagofernendorech.toneforge.ui.fragments.effects.EffectsFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.home.HomeFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.looper.LooperFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.tuner.TunerFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.metronome.MetronomeFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.recorder.RecorderFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.settings.SettingsFragmentRefactored;
import com.thiagofernendorech.toneforge.ui.fragments.looplibrary.LoopLibraryFragmentRefactored;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.*;

/**
 * Testes unitários para o NavigationController
 * Valida a navegação entre fragments
 */
@RunWith(RobolectricTestRunner.class)
public class NavigationControllerTest {
    
    @Mock
    private MainActivity mockActivity;
    
    private NavigationController navigationController;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        navigationController = NavigationController.getInstance();
        navigationController.init(mockActivity);
    }
    
    @Test
    public void testNavigateToHome_shouldLoadHomeFragment() {
        // Act
        navigationController.navigateToHome();
        
        // Assert
        verify(mockActivity).loadFragment(any(HomeFragmentRefactored.class));
        verify(mockActivity).updateHeaderTitle("ToneForge");
    }
    
    @Test
    public void testNavigateToEffects_shouldLoadEffectsFragment() {
        // Act
        navigationController.navigateToEffects();
        
        // Assert
        verify(mockActivity).loadFragment(any(EffectsFragmentRefactored.class));
        verify(mockActivity).updateHeaderTitle("Efeitos");
    }
    
    @Test
    public void testNavigateToLooper_shouldLoadLooperFragment() {
        // Act
        navigationController.navigateToLooper();
        
        // Assert
        verify(mockActivity).loadFragment(any(LooperFragmentRefactored.class));
        verify(mockActivity).updateHeaderTitle("Looper");
    }
    
    @Test
    public void testNavigateToTuner_shouldLoadTunerFragment() {
        // Act
        navigationController.navigateToTuner();
        
        // Assert
        verify(mockActivity).loadFragment(any(TunerFragmentRefactored.class));
        verify(mockActivity).updateHeaderTitle("Afinador");
    }
    
    @Test
    public void testNavigateToMetronome_shouldLoadMetronomeFragment() {
        // Act
        navigationController.navigateToMetronome();
        
        // Assert
        verify(mockActivity).loadFragment(any(MetronomeFragmentRefactored.class));
        verify(mockActivity).updateHeaderTitle("Metrônomo");
    }
    
    @Test
    public void testNavigateToRecorder_shouldLoadRecorderFragment() {
        // Act
        navigationController.navigateToRecorder();
        
        // Assert
        verify(mockActivity).loadFragment(any(RecorderFragmentRefactored.class));
        verify(mockActivity).updateHeaderTitle("Gravador");
    }
    
    @Test
    public void testNavigateToSettings_shouldLoadSettingsFragment() {
        // Act
        navigationController.navigateToSettings();
        
        // Assert
        verify(mockActivity).loadFragment(any(SettingsFragmentRefactored.class));
        verify(mockActivity).updateHeaderTitle("Configurações");
    }
    
    @Test
    public void testNavigateToLoopLibrary_shouldLoadLoopLibraryFragment() {
        // Act
        navigationController.navigateToLoopLibrary();
        
        // Assert
        verify(mockActivity).loadFragment(any(LoopLibraryFragmentRefactored.class));
        verify(mockActivity).updateHeaderTitle("Biblioteca de Loops");
    }
    
    @Test
    public void testNavigateBack_shouldCallOnBackPressed() {
        // Act
        navigationController.navigateBack();
        
        // Assert
        verify(mockActivity).onBackPressed();
    }
    
    @Test
    public void testUpdateTitle_shouldUpdateHeaderTitle() {
        // Act
        navigationController.updateTitle("Novo Título");
        
        // Assert
        verify(mockActivity).updateHeaderTitle("Novo Título");
    }
    
    @Test
    public void testLoadFragment_shouldLoadFragmentWithTitle() {
        // Arrange
        Fragment testFragment = new HomeFragmentRefactored();
        String testTitle = "Test Fragment";
        
        // Act
        navigationController.loadFragment(testFragment, testTitle);
        
        // Assert
        verify(mockActivity).loadFragment(testFragment);
        verify(mockActivity).updateHeaderTitle(testTitle);
    }
    
    @Test
    public void testIsMainActivityAvailable_whenActivityExists_shouldReturnTrue() {
        // Act
        boolean result = navigationController.isMainActivityAvailable();
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testIsMainActivityAvailable_whenActivityNull_shouldReturnFalse() {
        // Arrange
        navigationController.clear();
        
        // Act
        boolean result = navigationController.isMainActivityAvailable();
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testClear_shouldClearActivityReference() {
        // Act
        navigationController.clear();
        
        // Act again - should not call activity methods
        navigationController.navigateToHome();
        
        // Assert
        verify(mockActivity, never()).loadFragment(any(Fragment.class));
        verify(mockActivity, never()).updateHeaderTitle(anyString());
    }
    
    @Test
    public void testSingletonInstance_shouldReturnSameInstance() {
        // Act
        NavigationController instance1 = NavigationController.getInstance();
        NavigationController instance2 = NavigationController.getInstance();
        
        // Assert
        assertSame(instance1, instance2);
    }
    
    @Test
    public void testNavigationWithNullActivity_shouldNotCrash() {
        // Arrange
        navigationController.clear();
        
        // Act & Assert - should not throw exception
        navigationController.navigateToHome();
        navigationController.navigateToEffects();
        navigationController.updateTitle("Test");
    }
} 