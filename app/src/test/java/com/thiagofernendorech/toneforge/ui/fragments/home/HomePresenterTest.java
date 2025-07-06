package com.thiagofernendorech.toneforge.ui.fragments.home;

import android.content.Context;
import com.thiagofernendorech.toneforge.ui.navigation.NavigationController;
import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.domain.models.AudioState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Testes unitários para o HomePresenter
 * Valida a lógica de negócio e navegação do HomeFragment
 */
@RunWith(RobolectricTestRunner.class)
public class HomePresenterTest {
    
    @Mock
    private HomeContract.View mockView;
    
    @Mock
    private NavigationController mockNavigation;
    
    @Mock
    private AudioRepository mockRepository;
    
    private Context context;
    private HomePresenter presenter;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        presenter = new HomePresenter(context, mockNavigation, mockRepository);
        presenter.attachView(mockView);
    }
    
    @Test
    public void testOnEffectsClicked_shouldNavigateToEffects() {
        // Act
        presenter.onEffectsClicked();
        
        // Assert
        verify(mockNavigation).navigateToEffects();
    }
    
    @Test
    public void testOnLooperClicked_shouldNavigateToLooper() {
        // Act
        presenter.onLooperClicked();
        
        // Assert
        verify(mockNavigation).navigateToLooper();
    }
    
    @Test
    public void testOnTunerClicked_shouldNavigateToTuner() {
        // Act
        presenter.onTunerClicked();
        
        // Assert
        verify(mockNavigation).navigateToTuner();
    }
    
    @Test
    public void testOnMetronomeClicked_shouldNavigateToMetronome() {
        // Act
        presenter.onMetronomeClicked();
        
        // Assert
        verify(mockNavigation).navigateToMetronome();
    }
    
    @Test
    public void testOnRecorderClicked_shouldNavigateToRecorder() {
        // Act
        presenter.onRecorderClicked();
        
        // Assert
        verify(mockNavigation).navigateToRecorder();
    }
    
    @Test
    public void testOnSettingsClicked_shouldNavigateToSettings() {
        // Act
        presenter.onSettingsClicked();
        
        // Assert
        verify(mockNavigation).navigateToSettings();
    }
    
    @Test
    public void testOnLearningClicked_shouldNavigateToLearning() {
        // Act
        presenter.onLearningClicked();
        
        // Assert
        verify(mockNavigation).navigateToLearning();
    }
    
    @Test
    public void testOnViewStarted_shouldLoadAudioState() {
        // Arrange
        AudioState mockAudioState = new AudioState();
        when(mockRepository.getCurrentAudioState()).thenReturn(mockAudioState);
        
        // Act
        presenter.onViewStarted();
        
        // Assert
        verify(mockRepository).getCurrentAudioState();
        verify(mockView).updateAudioState(mockAudioState);
    }
    
    @Test
    public void testOnViewStarted_shouldUpdateTitle() {
        // Act
        presenter.onViewStarted();
        
        // Assert
        verify(mockView).updateTitle("ToneForge");
    }
    
    @Test
    public void testOnWifiClicked_shouldShowWifiDialog() {
        // Act
        presenter.onWifiClicked();
        
        // Assert
        verify(mockView).showWifiDialog();
    }
    
    @Test
    public void testOnVolumeClicked_shouldShowVolumeDialog() {
        // Act
        presenter.onVolumeClicked();
        
        // Assert
        verify(mockView).showVolumeDialog();
    }
    
    @Test
    public void testOnPowerClicked_shouldShowPowerDialog() {
        // Act
        presenter.onPowerClicked();
        
        // Assert
        verify(mockView).showPowerDialog();
    }
    
    @Test
    public void testUpdateSystemStatus_shouldUpdateWifiStatus() {
        // Arrange
        when(mockRepository.isWifiConnected()).thenReturn(true);
        
        // Act
        presenter.updateSystemStatus();
        
        // Assert
        verify(mockView).updateWifiStatus(true);
    }
    
    @Test
    public void testUpdateSystemStatus_shouldUpdateBatteryStatus() {
        // Arrange
        when(mockRepository.getBatteryLevel()).thenReturn(75);
        when(mockRepository.isBatteryCharging()).thenReturn(false);
        
        // Act
        presenter.updateSystemStatus();
        
        // Assert
        verify(mockView).updateBatteryStatus(75, false);
    }
    
    @Test
    public void testDetachView_shouldNotCallViewMethods() {
        // Arrange
        presenter.detachView();
        
        // Act
        presenter.onEffectsClicked();
        
        // Assert
        verify(mockNavigation).navigateToEffects(); // Navigation ainda funciona
        verify(mockView, never()).updateTitle(anyString()); // View não é chamada
    }
    
    @Test
    public void testOnViewPaused_shouldStopUpdates() {
        // Act
        presenter.onViewPaused();
        
        // Act again - should not trigger updates
        presenter.updateSystemStatus();
        
        // Assert
        verify(mockView, never()).updateWifiStatus(anyBoolean());
        verify(mockView, never()).updateBatteryStatus(anyInt(), anyBoolean());
    }
    
    @Test
    public void testOnViewResumed_shouldResumeUpdates() {
        // Arrange
        presenter.onViewPaused();
        when(mockRepository.isWifiConnected()).thenReturn(true);
        
        // Act
        presenter.onViewResumed();
        presenter.updateSystemStatus();
        
        // Assert
        verify(mockView).updateWifiStatus(true);
    }
} 