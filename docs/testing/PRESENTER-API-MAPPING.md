# Mapeamento de APIs de Presenters - ToneForge

**Data:** 2025-12-28
**Sprint:** 2

Este documento mapeia os métodos reais dos Presenters do ToneForge para facilitar a criação de testes unitários que correspondam à API real.

## EffectsPresenter

### Construtor
```java
public EffectsPresenter(Context context, AudioRepository audioRepository)
```

### Métodos de Ciclo de Vida (Interface EffectsContract.Presenter)

| Método | Descrição | Chamadas na View |
|--------|-----------|------------------|
| `onViewStarted()` | Inicializa o presenter | `updateEffectsStatus()`, carrega presets/automações |
| `onViewResumed()` | Retoma atualizações | `updateStatus()` |
| `onViewPaused()` | Pausa atualizações | Nenhuma |
| `onViewDestroyed()` | Limpa recursos | Nenhuma |

### Métodos de Controle de Efeitos

| Método | Parâmetros | Chamadas na View |
|--------|------------|------------------|
| `setEffectEnabled(String, boolean)` | effectName, enabled | `updateEffectState()`, `updateBypassIndicators()` |
| `setEffectParameter(String, String, float)` | effectName, parameterName, value | Nenhuma direta |
| `resetEffect(String)` | effectName | `updateEffectParameters()`, `updateBypassIndicators()`, `showMessage()` |
| `resetAllEffects()` | - | `updateEffectParameters()`, `updateEffectOrder()`, `updateBypassIndicators()`, `showMessage()` |
| `moveEffect(int, int)` | fromPosition, toPosition | `updateEffectOrder()` |

### Métodos de Presets

| Método | Parâmetros | Chamadas na View |
|--------|------------|------------------|
| `loadPreset(String)` | presetName | `updateEffectParameters()`, `selectPreset()`, `updateBypassIndicators()`, `showMessage()` |
| `savePreset(String)` | presetName | `selectPreset()`, `showSuccess()` |
| `deletePreset(String)` | presetName | `showSuccess()` |
| `exportPreset(String, String)` | presetName, filePath | `showMessage()` (em implementação) |
| `importPreset(String)` | filePath | `showMessage()` (em implementação) |
| `togglePresetFavorite(String)` | presetName | `showMessage()` (em implementação) |
| `toggleFavoritesFilter()` | - | `updateFavoritesFilter()` |

### Métodos de Automação

| Método | Parâmetros | Chamadas na View |
|--------|------------|------------------|
| `startAutomationRecording(String)` | automationName | `updateAutomationStatus()`, `showMessage()` |
| `stopAutomationRecording()` | - | `updateAutomationStatus()`, `showSuccess()` |
| `startAutomationPlayback(String)` | automationName | `updateAutomationStatus()`, `showMessage()` |
| `stopAutomationPlayback()` | - | `updateAutomationStatus()`, `showMessage()` |
| `exportAutomation(String, String)` | automationName, filePath | `showMessage()` |
| `importAutomation(String)` | filePath | `showMessage()` |
| `deleteAutomation(String)` | automationName | `showMessage()` |

### Métodos MIDI

| Método | Parâmetros | Chamadas na View |
|--------|------------|------------------|
| `setMidiEnabled(boolean)` | enabled | `updateMidiStatus()` |
| `startMidiLearn(String)` | parameterName | `showMidiLearnFeedback()` |
| `stopMidiLearn()` | - | `showMidiLearnFeedback()` |
| `applyMidiParameter(String, float)` | parameterName, value | Via `setEffectParameter()` |

### Métodos Outros

| Método | Parâmetros | Chamadas na View |
|--------|------------|------------------|
| `checkAudioPermissions()` | - | `updateEffectsStatus()` ou `requestAudioPermission()` |
| `updateStatus()` | - | `updateAudioState()`, `updateEffectsStatus()`, `updateMidiStatus()` |
| `handleFileResult(int, int, String)` | requestCode, resultCode, filePath | Depende do requestCode |

---

## EffectsContract.View - Métodos Requeridos

Métodos que devem existir na View para os testes:

```java
// === CONTROLE DE ESTADO ===
void updateEffectsStatus(String status, boolean isActive);
void updateAudioState(AudioState audioState);

// === CONTROLE DE EFEITOS ===
void updateEffectParameters(EffectParameters parameters);
void updateEffectState(String effectName, boolean enabled);
void updateBypassIndicators();
void updateEffectOrder(List<String> effectOrder);

// === PRESETS ===
void updatePresetList(List<String> presets);
void selectPreset(String presetName);
void showSavePresetDialog();
void showDeletePresetConfirmation(String presetName);
void showExportPresetDialog();
void updateFavoriteButton(boolean isFavorite);
void updateFavoritesFilter(boolean showFavoritesOnly);

// === AUTOMAÇÃO ===
void updateAutomationStatus(boolean isRecording, boolean isPlaying, String statusText);
void updateAutomationProgress(int progress, String timeText);
void updateAutomationList(List<String> automations);
void showSaveAutomationDialog();
void showExportAutomationDialog();

// === MIDI ===
void updateMidiStatus(boolean enabled, int deviceCount);
void showMidiLearnFeedback(String parameterName, boolean isActive);

// === OUTROS ===
void showTooltip(String message);
void requestAudioPermission();
void openFilePicker(int requestCode);
void openFileCreator(String fileName, int requestCode);

// === HERDADOS DE BaseView ===
void showLoading();
void hideLoading();
void showError(String message);
void showSuccess(String message);
void showMessage(String message);
```

---

## Padrões de Teste Recomendados

### Teste Básico de Presenter

```java
@RunWith(MockitoJUnitRunner.class)
public class EffectsPresenterTest {

    @Mock
    private EffectsContract.View mockView;

    @Mock
    private AudioRepository mockAudioRepository;

    private EffectsPresenter presenter;

    @Before
    public void setUp() {
        // Para testes que não precisam de contexto Android real
        Context mockContext = mock(Context.class);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);

        presenter = new EffectsPresenter(mockContext, mockAudioRepository);
        presenter.attachView(mockView);
    }

    @Test
    public void setEffectEnabled_shouldCallUpdateEffectState() {
        // Act
        presenter.setEffectEnabled("gain", true);

        // Assert
        verify(mockView).updateEffectState("gain", true);
        verify(mockView).updateBypassIndicators();
    }
}
```

### Teste com ifViewAttached Pattern

O EffectsPresenter usa o padrão `ifViewAttached(v -> ...)`. Para testar:

```java
@Test
public void loadPreset_shouldUpdateViewComponents() {
    // Arrange
    String presetName = "Rock";

    // Act
    presenter.loadPreset(presetName);

    // Assert - verificar todas as chamadas esperadas
    verify(mockView).updateEffectParameters(any(EffectParameters.class));
    verify(mockView).selectPreset(presetName);
    verify(mockView).updateBypassIndicators();
    verify(mockView).showMessage(contains(presetName));
}
```

### Teste de Ciclo de Vida

```java
@Test
public void onViewDestroyed_shouldStopUpdates() {
    // Arrange
    presenter.onViewStarted(); // Iniciar updates

    // Act
    presenter.onViewDestroyed();

    // Assert - verificar que não há mais interações após destroy
    // (pode precisar verificar internamente se updates pararam)
}
```

---

## Dependências de Mock Necessárias

Para testar corretamente, é necessário mockar:

1. **AudioRepository** - Operações de áudio
2. **PresetManager** - Gerenciamento de presets (interno, pode ser difícil mockar)
3. **AutomationManager** - Automação (singleton, usar `MockedStatic`)
4. **ToneForgeMidiManager** - MIDI (singleton, usar `MockedStatic`)
5. **PermissionManager** - Permissões (interno)

### Exemplo com MockedStatic

```java
@Test
public void test_withMockedSingleton() {
    try (MockedStatic<AutomationManager> mockedStatic = mockStatic(AutomationManager.class)) {
        AutomationManager mockManager = mock(AutomationManager.class);
        mockedStatic.when(() -> AutomationManager.getInstance(any())).thenReturn(mockManager);

        // Seu teste aqui
    }
}
```

---

## Notas Importantes

1. **Handler/Looper**: O presenter usa `Handler` com `Looper.getMainLooper()`. Para testes, usar Robolectric ou shadows.

2. **Métodos privados**: Vários métodos são privados e não podem ser testados diretamente. Testar através dos métodos públicos.

3. **Estado interno**: O presenter mantém estado interno (`currentParameters`, `effectOrder`, etc.). Testar comportamento, não estado.

4. **Versões simplificadas**: Vários métodos têm comentários "versão simplificada - implementação futura". Estes podem não funcionar como esperado.

---

**Próximos passos:**
- Implementar EffectsPresenterTest básico seguindo este mapeamento
- Adicionar mapeamento para outros Presenters (Looper, Tuner, etc.)
