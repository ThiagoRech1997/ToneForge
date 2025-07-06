# 🏗️ Plano de Refatoração da Arquitetura - ToneForge

## 📋 **Situação Atual vs. Proposta**

### **Problemas Identificados:**
1. **Arquitetura Monolítica** - MainActivity sobrecarregada
2. **Organização inadequada** - 36 classes na mesma pasta
3. **Acoplamento forte** - Fragments dependem diretamente da MainActivity
4. **Complexidade excessiva** - Fragments com 1000+ linhas
5. **Padrões inconsistentes** - Singletons mal implementados

## 🎯 **Nova Estrutura Proposta**

### **1. Reorganização de Pacotes**

```
com.thiagofernendorech.toneforge/
├── ui/
│   ├── activities/
│   │   ├── MainActivity.java
│   │   └── BaseActivity.java
│   ├── fragments/
│   │   ├── base/
│   │   │   ├── BaseFragment.java
│   │   │   └── BasePresenterFragment.java
│   │   ├── home/
│   │   │   ├── HomeFragment.java
│   │   │   └── HomePresenter.java
│   │   ├── effects/
│   │   │   ├── EffectsFragment.java
│   │   │   ├── EffectsPresenter.java
│   │   │   └── controls/
│   │   │       ├── GainControlView.java
│   │   │       ├── DistortionControlView.java
│   │   │       └── DelayControlView.java
│   │   ├── looper/
│   │   ├── tuner/
│   │   └── settings/
│   ├── adapters/
│   │   ├── PresetAdapter.java
│   │   ├── LooperTrackAdapter.java
│   │   └── EffectOrderAdapter.java
│   ├── views/
│   │   ├── WaveformView.java
│   │   └── custom/
│   └── dialogs/
│       ├── ExportDialog.java
│       └── VolumeControlDialog.java
├── audio/
│   ├── engine/
│   │   ├── AudioEngine.java
│   │   └── AudioEngineJNI.java
│   ├── effects/
│   │   ├── EffectProcessor.java
│   │   ├── DistortionEffect.java
│   │   └── DelayEffect.java
│   ├── analysis/
│   │   ├── AudioAnalyzer.java
│   │   └── SpectrumAnalyzer.java
│   └── pipeline/
│       ├── PipelineManager.java
│       └── AudioPipeline.java
├── data/
│   ├── repository/
│   │   ├── AudioRepository.java
│   │   ├── PresetRepository.java
│   │   └── AutomationRepository.java
│   ├── local/
│   │   ├── PresetDao.java
│   │   └── AutomationDao.java
│   └── preferences/
│       ├── AppPreferences.java
│       └── AudioPreferences.java
├── domain/
│   ├── models/
│   │   ├── Preset.java
│   │   ├── AutomationEvent.java
│   │   └── EffectParameter.java
│   ├── usecases/
│   │   ├── LoadPresetUseCase.java
│   │   ├── SavePresetUseCase.java
│   │   └── ProcessAudioUseCase.java
│   └── interfaces/
│       ├── AudioEngineInterface.java
│       └── PresetManagerInterface.java
├── managers/
│   ├── audio/
│   │   ├── AudioStateManager.java
│   │   └── LatencyManager.java
│   ├── preset/
│   │   ├── PresetManager.java
│   │   └── FavoritesManager.java
│   ├── automation/
│   │   └── AutomationManager.java
│   └── midi/
│       └── MidiManager.java
├── utils/
│   ├── permissions/
│   │   └── PermissionManager.java
│   ├── export/
│   │   ├── LoopExportManager.java
│   │   └── PresetExportManager.java
│   ├── recovery/
│   │   └── StateRecoveryManager.java
│   └── helpers/
│       ├── AudioHelper.java
│       └── FileHelper.java
└── services/
    ├── AudioBackgroundService.java
    └── NotificationService.java
```

### **2. Implementação do Padrão MVP**

#### **Base Classes**

```java
// BasePresenter.java
public abstract class BasePresenter<V extends BaseView> {
    protected V view;
    
    public void attachView(V view) {
        this.view = view;
    }
    
    public void detachView() {
        this.view = null;
    }
    
    protected boolean isViewAttached() {
        return view != null;
    }
}

// BaseView.java
public interface BaseView {
    void showLoading();
    void hideLoading();
    void showError(String message);
    void showSuccess(String message);
}

// BaseFragment.java
public abstract class BaseFragment<P extends BasePresenter> extends Fragment implements BaseView {
    protected P presenter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = createPresenter();
        presenter.attachView(this);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
    
    protected abstract P createPresenter();
}
```

#### **Exemplo: HomeFragment Refatorado**

```java
// HomeContract.java
public interface HomeContract {
    interface View extends BaseView {
        void navigateToEffects();
        void navigateToLooper();
        void navigateToTuner();
        void updateConnectionStatus(boolean connected);
    }
    
    interface Presenter extends BasePresenter<View> {
        void onEffectsClicked();
        void onLooperClicked();
        void onTunerClicked();
        void onWifiClicked();
        void onVolumeClicked();
    }
}

// HomePresenter.java
public class HomePresenter implements HomeContract.Presenter {
    private HomeContract.View view;
    private NavigationController navigationController;
    
    public HomePresenter(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
    
    @Override
    public void onEffectsClicked() {
        if (isViewAttached()) {
            navigationController.navigateToEffects();
        }
    }
    
    @Override
    public void onLooperClicked() {
        if (isViewAttached()) {
            navigationController.navigateToLooper();
        }
    }
    
    // ... outros métodos
}

// HomeFragment.java (Refatorado)
public class HomeFragment extends BaseFragment<HomeContract.Presenter> implements HomeContract.View {
    
    @Override
    protected HomeContract.Presenter createPresenter() {
        return new HomePresenter(NavigationController.getInstance());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        view.findViewById(R.id.btnEffects).setOnClickListener(v -> presenter.onEffectsClicked());
        view.findViewById(R.id.btnLooper).setOnClickListener(v -> presenter.onLooperClicked());
        view.findViewById(R.id.btnTuner).setOnClickListener(v -> presenter.onTunerClicked());
        
        return view;
    }
    
    @Override
    public void navigateToEffects() {
        // Navegação será feita pelo NavigationController
    }
    
    // ... outros métodos da interface
}
```

### **3. Sistema de Navegação Desacoplado**

```java
// NavigationController.java
public class NavigationController {
    private static NavigationController instance;
    private WeakReference<MainActivity> mainActivityRef;
    
    public static NavigationController getInstance() {
        if (instance == null) {
            instance = new NavigationController();
        }
        return instance;
    }
    
    public void init(MainActivity activity) {
        this.mainActivityRef = new WeakReference<>(activity);
    }
    
    public void navigateToEffects() {
        MainActivity activity = getActivity();
        if (activity != null) {
            activity.loadFragment(new EffectsFragment());
            activity.updateHeaderTitle("Efeitos");
        }
    }
    
    public void navigateToLooper() {
        MainActivity activity = getActivity();
        if (activity != null) {
            activity.loadFragment(new LooperFragment());
            activity.updateHeaderTitle("Looper");
        }
    }
    
    private MainActivity getActivity() {
        return mainActivityRef != null ? mainActivityRef.get() : null;
    }
}
```

### **4. Repository Pattern para Dados**

```java
// AudioRepository.java
public class AudioRepository {
    private static AudioRepository instance;
    private AudioEngine audioEngine;
    private PresetRepository presetRepository;
    private AutomationRepository automationRepository;
    
    public static AudioRepository getInstance() {
        if (instance == null) {
            instance = new AudioRepository();
        }
        return instance;
    }
    
    private AudioRepository() {
        audioEngine = AudioEngine.getInstance();
        presetRepository = PresetRepository.getInstance();
        automationRepository = AutomationRepository.getInstance();
    }
    
    public void applyPreset(Preset preset) {
        audioEngine.setGain(preset.getGain());
        audioEngine.setDistortion(preset.getDistortion());
        // ... aplicar outros parâmetros
    }
    
    public List<Preset> getAllPresets() {
        return presetRepository.getAllPresets();
    }
    
    public void savePreset(Preset preset) {
        presetRepository.savePreset(preset);
    }
}
```

### **5. Refatoração dos Fragments Grandes**

#### **EffectsFragment** (2590 linhas → múltiplos componentes)

```java
// EffectsFragment.java (Refatorado)
public class EffectsFragment extends BaseFragment<EffectsContract.Presenter> implements EffectsContract.View {
    private EffectControlsView effectControlsView;
    private PresetManagerView presetManagerView;
    private AutomationView automationView;
    private MidiControlView midiControlView;
    
    @Override
    protected EffectsContract.Presenter createPresenter() {
        return new EffectsPresenter(AudioRepository.getInstance());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_effects, container, false);
        
        // Inicializar sub-views
        effectControlsView = new EffectControlsView(view.findViewById(R.id.effectControlsContainer));
        presetManagerView = new PresetManagerView(view.findViewById(R.id.presetManagerContainer));
        automationView = new AutomationView(view.findViewById(R.id.automationContainer));
        midiControlView = new MidiControlView(view.findViewById(R.id.midiControlContainer));
        
        // Configurar listeners
        effectControlsView.setOnEffectChangedListener(presenter::onEffectChanged);
        presetManagerView.setOnPresetSelectedListener(presenter::onPresetSelected);
        
        return view;
    }
    
    @Override
    public void updateEffectParameters(EffectParameters parameters) {
        effectControlsView.updateParameters(parameters);
    }
    
    @Override
    public void showPresets(List<Preset> presets) {
        presetManagerView.showPresets(presets);
    }
    
    // ... outros métodos da interface
}

// EffectControlsView.java (Componente separado)
public class EffectControlsView {
    private View rootView;
    private GainControlView gainControl;
    private DistortionControlView distortionControl;
    private DelayControlView delayControl;
    private OnEffectChangedListener listener;
    
    public EffectControlsView(View rootView) {
        this.rootView = rootView;
        initializeControls();
    }
    
    private void initializeControls() {
        gainControl = new GainControlView(rootView.findViewById(R.id.gainControlContainer));
        distortionControl = new DistortionControlView(rootView.findViewById(R.id.distortionControlContainer));
        delayControl = new DelayControlView(rootView.findViewById(R.id.delayControlContainer));
        
        // Configurar listeners
        gainControl.setOnValueChangedListener(value -> {
            if (listener != null) {
                listener.onGainChanged(value);
            }
        });
        
        distortionControl.setOnValueChangedListener(value -> {
            if (listener != null) {
                listener.onDistortionChanged(value);
            }
        });
        
        delayControl.setOnValueChangedListener(value -> {
            if (listener != null) {
                listener.onDelayChanged(value);
            }
        });
    }
    
    public void updateParameters(EffectParameters parameters) {
        gainControl.setValue(parameters.getGain());
        distortionControl.setValue(parameters.getDistortion());
        delayControl.setValue(parameters.getDelay());
    }
    
    public void setOnEffectChangedListener(OnEffectChangedListener listener) {
        this.listener = listener;
    }
    
    public interface OnEffectChangedListener {
        void onGainChanged(float value);
        void onDistortionChanged(float value);
        void onDelayChanged(float value);
    }
}
```

## 📋 **Status da Implementação**

### **✅ Concluído**

1. **Estrutura Base MVP**
   - ✅ `BaseView` - Interface comum para todas as views
   - ✅ `BasePresenter<V>` - Classe base para presenters com gerenciamento de ciclo de vida
   - ✅ `BaseFragment<P>` - Fragment base com integração MVP

2. **Navegação Desacoplada**
   - ✅ `NavigationController` - Singleton para gerenciar navegação
   - ✅ Integração com `MainActivity`
   - ✅ Métodos para navegação entre fragments

3. **Camada de Dados**
   - ✅ `AudioRepository` - Interface unificada para operações de áudio
   - ✅ Integração com todos os managers existentes
   - ✅ Métodos para controle de pipeline, efeitos, presets, etc.

4. **Modelos de Domínio**
   - ✅ `AudioState` - Estado do sistema de áudio
   - ✅ `EffectParameters` - Parâmetros dos efeitos

5. **HomeFragment Refatorado**
   - ✅ `HomeContract` - Interface MVP para HomeFragment
   - ✅ `HomePresenter` - Lógica de negócio do HomeFragment
   - ✅ `HomeFragmentRefactored` - Fragment limpo usando MVP

6. **EffectsFragment Refatorado** ⭐ **COMPLETO**
   - ✅ `EffectsContract` - Interface MVP completa para efeitos
   - ✅ `EffectsPresenter` - Lógica de negócio com 9 efeitos completos
   - ✅ `EffectsFragmentRefactored` - Fragment com UI limpa
   - ✅ Sistema de presets, automação e MIDI integrado
   - ✅ Integração completa com MainActivity e NavigationController

7. **LooperFragment Refatorado** ⭐ **COMPLETO**
   - ✅ `LooperContract` - Interface MVP completa para looper
   - ✅ `LooperPresenter` - Lógica de negócio completa do looper
   - ✅ `LooperFragmentRefactored` - Fragment com UI modular
   - ✅ Sistema de tracks, efeitos, marcadores e waveform
   - ✅ Undo/Redo, fade, slicing e filtros implementados
   - ✅ Integração completa com MainActivity e NavigationController

8. **TunerFragment Refatorado** ⭐ **COMPLETO**
   - ✅ `TunerContract` - Interface MVP completa para afinador
   - ✅ `TunerPresenter` - Lógica de negócio completa do afinador
   - ✅ `TunerFragmentRefactored` - Fragment com UI limpa
   - ✅ Sistema de detecção de frequência, calibração e temperamentos
   - ✅ Controles de sensibilidade, threshold e indicadores visuais
   - ✅ Integração completa com MainActivity e NavigationController

## 🔧 **Como Usar a Nova Arquitetura**

*[Conteúdo existente permanece inalterado]*

## 📋 **Próximos Passos**

### **🎯 Fase 2: Refatorar Fragments Restantes**

1. **TunerFragment** (Prioridade Alta) ✅ **CONCLUÍDO**
   - ✅ Criar `TunerContract`
   - ✅ Implementar `TunerPresenter`
   - ✅ Refatorar `TunerFragment` para usar MVP
   - ✅ Funcionalidades: afinação, calibração, temperamentos

2. **MetronomeFragment** (Prioridade Média)
   - Criar contratos MVP
   - Implementar presenters
   - Refatorar fragment
   - Funcionalidades: padrões rítmicos, BPM, acentos

3. **RecorderFragment** ✅ **CONCLUÍDO** (Prioridade Média)
   - Aplicar padrão MVP
   - Desacoplar lógica de UI
   - Funcionalidades: gravação, reprodução, edição

4. **SettingsFragment** ✅ **CONCLUÍDO**
   - Aplicar padrão MVP
   - Organizar configurações por categoria
   - Funcionalidades: áudio, UI, permissões

### **🎯 Fase 3: Componentes Reutilizáveis**

*[Conteúdo existente permanece inalterado]*

### **🎯 Fase 4: Melhorias Avançadas**

*[Conteúdo existente permanece inalterado]*

## 🔄 **Migração Gradual**

### **Estratégia Segura**

1. **Manter Compatibilidade**
   - Fragments originais continuam funcionando
   - Novos fragments usam nova arquitetura
   - Migração gradual, um fragment por vez

2. **Fragments Refatorados (Concluídos)**
   - ✅ `HomeFragmentRefactored` - Navegação principal
   - ✅ `EffectsFragmentRefactored` - Sistema completo de efeitos
   - ✅ `LooperFragmentRefactored` - Sistema completo de looper

3. **Rollback Fácil**
   - Código original permanece intacto
   - Fácil voltar atrás se necessário

### **Status dos Fragments**

| Fragment | Status | Arquitetura | Funcionalidades |
|----------|--------|-------------|-----------------|
| HomeFragment | ✅ Refatorado | MVP | Navegação principal |
| EffectsFragment | ✅ Refatorado | MVP | 9 efeitos + presets + automação + MIDI |
| LooperFragment | ✅ Refatorado | MVP | Gravação + tracks + efeitos + marcadores |
| TunerFragment | ✅ Refatorado | MVP | Afinação + calibração + temperamentos |
| MetronomeFragment | ✅ Concluído | MVP | Metrônomo |
| RecorderFragment | ✅ Concluído | MVP | Gravação |
| SettingsFragment | ✅ Concluído | MVP | Configurações |

### **Exemplo de Migração**

*[Conteúdo existente permanece inalterado]*

## 📋 **Cronograma de Implementação**

### **Semana 1-2: Preparação**
- [ ] Criar nova estrutura de pastas
- [ ] Implementar classes base (BaseFragment, BasePresenter, BaseView)
- [ ] Criar NavigationController
- [ ] Implementar AudioRepository

### **Semana 3-4: Refatoração da MainActivity**
- [ ] Mover responsabilidades para managers apropriados
- [ ] Implementar sistema de navegação desacoplado
- [ ] Refatorar HomeFragment

### **Semana 5-6: Refatoração dos Fragments**
- [ ] Dividir EffectsFragment em componentes menores
- [ ] Refatorar LooperFragment
- [ ] Implementar presenters para cada fragment

### **Semana 7-8: Managers e Repositories**
- [ ] Refatorar managers existentes
- [ ] Implementar repositories para dados
- [ ] Criar use cases para lógica de negócio

### **Semana 9-10: Testes e Ajustes**
- [ ] Testes unitários para presenters
- [ ] Testes de integração
- [ ] Ajustes finais e documentação

## 📊 **Métricas de Sucesso**

### **Antes da Refatoração:**
- MainActivity: 510 linhas
- EffectsFragment: 2590 linhas
- LooperFragment: 1433 linhas
- Total de classes: 36 (todas na mesma pasta)

### **Após a Refatoração:**
- MainActivity: < 200 linhas
- EffectsFragment: < 300 linhas
- LooperFragment: < 300 linhas
- Total de classes: ~80 (organizadas em 8 pacotes)

### **Benefícios Esperados:**
1. **Manutenibilidade**: Código mais fácil de entender e modificar
2. **Testabilidade**: Componentes isolados e testáveis
3. **Reusabilidade**: Componentes reutilizáveis entre fragments
4. **Escalabilidade**: Estrutura preparada para crescimento
5. **Qualidade**: Redução de bugs e acoplamento
6. **Performance**: Melhor gestão de memória e recursos

## 🎯 **Próximos Passos**

1. **Criar branch para refatoração**
2. **Implementar estrutura base**
3. **Migrar funcionalidades gradualmente**
4. **Manter compatibilidade durante transição**
5. **Documentar mudanças**

---

*Este plano garante uma transição suave para uma arquitetura mais limpa e maintível, mantendo todas as funcionalidades existentes while improving code quality and developer experience.* 