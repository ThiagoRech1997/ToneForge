package com.thiagofernendorech.toneforge.ui.presenters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.thiagofernendorech.toneforge.data.repository.AudioRepository;
import com.thiagofernendorech.toneforge.ui.fragments.home.HomeContract;
import com.thiagofernendorech.toneforge.ui.fragments.home.HomePresenter;
import com.thiagofernendorech.toneforge.ui.navigation.NavigationController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowNetworkInfo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para HomePresenter
 * Testa a logica de navegacao e atualizacao de status do sistema
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class HomePresenterTest {

    @Mock
    private NavigationController mockNavigationController;

    @Mock
    private AudioRepository mockAudioRepository;

    @Mock
    private HomeContract.View mockView;

    private HomePresenter presenter;
    private Context context;
    private ShadowConnectivityManager shadowConnectivityManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Usar contexto real do Robolectric
        context = RuntimeEnvironment.getApplication();

        // Obter shadow do ConnectivityManager para simular estados de rede
        ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        shadowConnectivityManager = Shadows.shadowOf(connectivityManager);

        presenter = new HomePresenter(context, mockNavigationController, mockAudioRepository);

        // Configurar view como ativa
        when(mockView.isViewActive()).thenReturn(true);
        presenter.attachView(mockView);
    }

    // === TESTES DE CICLO DE VIDA ===

    @Test
    public void attachView_shouldSetView() {
        // Arrange
        HomePresenter newPresenter = new HomePresenter(context, mockNavigationController, mockAudioRepository);

        // Act
        newPresenter.attachView(mockView);
        newPresenter.onViewStarted();

        // Assert
        verify(mockView).updateTitle("ToneForge");
    }

    @Test
    public void detachView_shouldClearView() {
        // Act
        presenter.detachView();
        presenter.updateSystemStatus();

        // Assert - nao deve chamar view apos detach
        verify(mockView, never()).updateWifiStatus(anyBoolean());
    }

    @Test
    public void onViewStarted_shouldUpdateTitle() {
        // Act
        presenter.onViewStarted();

        // Assert
        verify(mockView).updateTitle("ToneForge");
    }

    @Test
    public void onViewStarted_shouldUpdateSystemStatus() {
        // Act
        presenter.onViewStarted();

        // Assert
        verify(mockView).updateWifiStatus(anyBoolean());
    }

    @Test
    public void onViewResumed_shouldUpdateSystemStatus() {
        // Act
        presenter.onViewResumed();

        // Assert
        verify(mockView).updateWifiStatus(anyBoolean());
    }

    @Test
    public void onViewPaused_shouldNotCrash() {
        // Act & Assert - nao deve lancar excecao
        presenter.onViewPaused();
    }

    @Test
    public void onViewDestroyed_shouldNotCrash() {
        // Act & Assert - nao deve lancar excecao
        presenter.onViewDestroyed();
    }

    // === TESTES DE NAVEGACAO ===

    @Test
    public void onEffectsClicked_whenActivityAvailable_shouldNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(true);

        // Act
        presenter.onEffectsClicked();

        // Assert
        verify(mockNavigationController).navigateToEffects();
    }

    @Test
    public void onEffectsClicked_whenActivityNotAvailable_shouldNotNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(false);

        // Act
        presenter.onEffectsClicked();

        // Assert
        verify(mockNavigationController, never()).navigateToEffects();
    }

    @Test
    public void onLooperClicked_whenActivityAvailable_shouldNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(true);

        // Act
        presenter.onLooperClicked();

        // Assert
        verify(mockNavigationController).navigateToLooper();
    }

    @Test
    public void onLooperClicked_whenActivityNotAvailable_shouldNotNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(false);

        // Act
        presenter.onLooperClicked();

        // Assert
        verify(mockNavigationController, never()).navigateToLooper();
    }

    @Test
    public void onTunerClicked_whenActivityAvailable_shouldNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(true);

        // Act
        presenter.onTunerClicked();

        // Assert
        verify(mockNavigationController).navigateToTuner();
    }

    @Test
    public void onTunerClicked_whenActivityNotAvailable_shouldNotNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(false);

        // Act
        presenter.onTunerClicked();

        // Assert
        verify(mockNavigationController, never()).navigateToTuner();
    }

    @Test
    public void onMetronomeClicked_whenActivityAvailable_shouldNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(true);

        // Act
        presenter.onMetronomeClicked();

        // Assert
        verify(mockNavigationController).navigateToMetronome();
    }

    @Test
    public void onMetronomeClicked_whenActivityNotAvailable_shouldNotNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(false);

        // Act
        presenter.onMetronomeClicked();

        // Assert
        verify(mockNavigationController, never()).navigateToMetronome();
    }

    @Test
    public void onLearningClicked_whenActivityAvailable_shouldNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(true);

        // Act
        presenter.onLearningClicked();

        // Assert
        verify(mockNavigationController).navigateToLearning();
    }

    @Test
    public void onLearningClicked_whenActivityNotAvailable_shouldNotNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(false);

        // Act
        presenter.onLearningClicked();

        // Assert
        verify(mockNavigationController, never()).navigateToLearning();
    }

    @Test
    public void onRecorderClicked_whenActivityAvailable_shouldNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(true);

        // Act
        presenter.onRecorderClicked();

        // Assert
        verify(mockNavigationController).navigateToRecorder();
    }

    @Test
    public void onRecorderClicked_whenActivityNotAvailable_shouldNotNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(false);

        // Act
        presenter.onRecorderClicked();

        // Assert
        verify(mockNavigationController, never()).navigateToRecorder();
    }

    @Test
    public void onSettingsClicked_whenActivityAvailable_shouldNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(true);

        // Act
        presenter.onSettingsClicked();

        // Assert
        verify(mockNavigationController).navigateToSettings();
    }

    @Test
    public void onSettingsClicked_whenActivityNotAvailable_shouldNotNavigate() {
        // Arrange
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(false);

        // Act
        presenter.onSettingsClicked();

        // Assert
        verify(mockNavigationController, never()).navigateToSettings();
    }

    // === TESTES DE DIALOGOS ===

    @Test
    public void onWifiClicked_shouldShowWifiDialog() {
        // Act
        presenter.onWifiClicked();

        // Assert
        verify(mockView).showWifiDialog();
    }

    @Test
    public void onWifiClicked_shouldUpdateWifiStatus() {
        // Act
        presenter.onWifiClicked();

        // Assert
        verify(mockView).updateWifiStatus(anyBoolean());
    }

    @Test
    public void onVolumeClicked_shouldShowVolumeDialog() {
        // Act
        presenter.onVolumeClicked();

        // Assert
        verify(mockView).showVolumeDialog();
    }

    @Test
    public void onPowerClicked_shouldShowPowerDialog() {
        // Act
        presenter.onPowerClicked();

        // Assert
        verify(mockView).showPowerDialog();
    }

    // === TESTES DE STATUS WIFI ===

    @Test
    public void updateSystemStatus_whenWifiConnected_shouldUpdateTrue() {
        // Arrange - simular Wi-Fi conectado
        NetworkInfo wifiInfo = ShadowNetworkInfo.newInstance(
            null, ConnectivityManager.TYPE_WIFI, 0, true, NetworkInfo.State.CONNECTED);
        shadowConnectivityManager.setNetworkInfo(ConnectivityManager.TYPE_WIFI, wifiInfo);

        // Act
        presenter.updateSystemStatus();

        // Assert
        verify(mockView).updateWifiStatus(true);
    }

    @Test
    public void updateSystemStatus_whenWifiDisconnected_shouldUpdateFalse() {
        // Arrange - simular Wi-Fi desconectado
        NetworkInfo wifiInfo = ShadowNetworkInfo.newInstance(
            null, ConnectivityManager.TYPE_WIFI, 0, false, NetworkInfo.State.DISCONNECTED);
        shadowConnectivityManager.setNetworkInfo(ConnectivityManager.TYPE_WIFI, wifiInfo);

        // Act
        presenter.updateSystemStatus();

        // Assert
        verify(mockView).updateWifiStatus(false);
    }

    // === TESTES DE VIEW INATIVA ===

    @Test
    public void updateSystemStatus_withInactiveView_shouldNotUpdate() {
        // Arrange
        when(mockView.isViewActive()).thenReturn(false);

        // Act
        presenter.updateSystemStatus();

        // Assert
        verify(mockView, never()).updateWifiStatus(anyBoolean());
        verify(mockView, never()).updateBatteryStatus(anyInt(), anyBoolean());
    }

    // === TESTES DE UTILIDADES ===

    @Test
    public void isWifiEnabled_whenConnected_shouldReturnTrue() {
        // Arrange - simular Wi-Fi conectado
        NetworkInfo wifiInfo = ShadowNetworkInfo.newInstance(
            null, ConnectivityManager.TYPE_WIFI, 0, true, NetworkInfo.State.CONNECTED);
        shadowConnectivityManager.setNetworkInfo(ConnectivityManager.TYPE_WIFI, wifiInfo);

        // Act
        boolean result = presenter.isWifiEnabled();

        // Assert
        assertTrue(result);
    }

    @Test
    public void isWifiEnabled_whenDisconnected_shouldReturnFalse() {
        // Arrange - simular Wi-Fi desconectado
        NetworkInfo wifiInfo = ShadowNetworkInfo.newInstance(
            null, ConnectivityManager.TYPE_WIFI, 0, false, NetworkInfo.State.DISCONNECTED);
        shadowConnectivityManager.setNetworkInfo(ConnectivityManager.TYPE_WIFI, wifiInfo);

        // Act
        boolean result = presenter.isWifiEnabled();

        // Assert
        assertFalse(result);
    }

    @Test
    public void hasInternetConnection_whenConnected_shouldReturnTrue() {
        // Arrange - simular conexao ativa
        NetworkInfo activeInfo = ShadowNetworkInfo.newInstance(
            null, ConnectivityManager.TYPE_MOBILE, 0, true, NetworkInfo.State.CONNECTED);
        shadowConnectivityManager.setActiveNetworkInfo(activeInfo);

        // Act
        boolean result = presenter.hasInternetConnection();

        // Assert
        assertTrue(result);
    }

    @Test
    public void hasInternetConnection_whenDisconnected_shouldReturnFalse() {
        // Arrange - simular sem conexao
        shadowConnectivityManager.setActiveNetworkInfo(null);

        // Act
        boolean result = presenter.hasInternetConnection();

        // Assert
        assertFalse(result);
    }

    @Test
    public void getSystemInfo_shouldReturnFormattedInfo() {
        // Act
        String info = presenter.getSystemInfo();

        // Assert
        assertNotNull(info);
        assertTrue(info.contains("Android:"));
        assertTrue(info.contains("API Level:"));
        assertTrue(info.contains("Modelo:"));
        assertTrue(info.contains("Fabricante:"));
        assertTrue(info.contains("Wi-Fi:"));
        assertTrue(info.contains("Internet:"));
    }

    @Test
    public void getSystemInfo_whenWifiDisabled_shouldShowDisabled() {
        // Arrange - simular Wi-Fi desconectado
        NetworkInfo wifiInfo = ShadowNetworkInfo.newInstance(
            null, ConnectivityManager.TYPE_WIFI, 0, false, NetworkInfo.State.DISCONNECTED);
        shadowConnectivityManager.setNetworkInfo(ConnectivityManager.TYPE_WIFI, wifiInfo);

        // Act
        String info = presenter.getSystemInfo();

        // Assert
        assertTrue(info.contains("Wi-Fi: Desabilitado"));
    }

    @Test
    public void getSystemInfo_whenInternetDisconnected_shouldShowDisconnected() {
        // Arrange - simular sem conexao
        shadowConnectivityManager.setActiveNetworkInfo(null);

        // Act
        String info = presenter.getSystemInfo();

        // Assert
        assertTrue(info.contains("Internet: Desconectado"));
    }

    // === TESTES DE DETACH MULTIPLO ===

    @Test
    public void detachView_calledTwice_shouldNotCrash() {
        // Act & Assert - nao deve lancar excecao
        presenter.detachView();
        presenter.detachView();
    }

    // === TESTES DE NAVEGACAO COM VIEW DETACHED ===

    @Test
    public void onEffectsClicked_withDetachedView_shouldStillNavigate() {
        // Arrange
        presenter.detachView();
        when(mockNavigationController.isMainActivityAvailable()).thenReturn(true);

        // Act
        presenter.onEffectsClicked();

        // Assert - navegacao deve funcionar mesmo sem view
        verify(mockNavigationController).navigateToEffects();
    }
}
