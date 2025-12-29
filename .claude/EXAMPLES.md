# Exemplos Práticos de Uso - ToneForge Skills & Agents

## Cenários do Mundo Real

### 1. Adicionar Efeito de Tremolo

**Solicitação:**
```
I want to add a tremolo effect with rate and depth controls
```

**Claude usará:**
- Skill: `cpp-effect`
- Agent: `audio-dsp-engineer` (implementação C++)
- Agent: `android-ui-designer` (UI controls)
- Agent: `android-qa-engineer` (testes)

**Resultado:**
- Algoritmo tremolo em C++
- JNI bindings
- UI com SeekBars para rate e depth
- Integração com preset system
- Unit tests

---

### 2. Refatorar Fragment Legado

**Solicitação:**
```
Refactor the old PresetFragment to MVP architecture
```

**Claude usará:**
- Skill: `refactor-legacy`
- Agent: `android-architecture-reviewer` (validação arquitetura)

**Resultado:**
- PresetContract interface
- PresetPresenter com lógica de negócio
- PresetFragmentRefactored seguindo MVP
- Unit tests para presenter
- Layout modernizado

---

### 3. Debug de Crash Nativo

**Solicitação:**
```
The app crashes when I enable the distortion effect.
Here's the logcat:
[crash log]
```

**Claude usará:**
- Skill: `debug-native`
- Agent: `audio-dsp-engineer`

**Processo:**
1. Analisa stack trace
2. Identifica buffer overflow em distortion processing
3. Sugere correção com bounds checking
4. Implementa fix
5. Adiciona testes para prevenir regressão

---

### 4. Preparar Release 2.0.0

**Solicitação:**
```
Prepare for release 2.0.0 with the new pedalboard UI
```

**Claude usará:**
- Skill: `audio-test` (validar áudio)
- Skill: `security-audit` (revisar segurança)
- Skill: `release-prep` (checklist completo)
- Agent: `android-qa-engineer` (executar testes)

**Checklist executado:**
- ✅ Atualizar versionCode e versionName
- ✅ Executar todos os testes
- ✅ Rodar lint e corrigir issues
- ✅ Auditoria de segurança
- ✅ Build release APK/AAB
- ✅ Criar tag Git
- ✅ Gerar release notes

---

### 5. Criar Tela de MIDI Settings

**Solicitação:**
```
Create a MIDI Settings screen where users can configure MIDI controllers
```

**Claude usará:**
- Skill: `mvp-scaffold`
- Agent: `toneforge-advanced-features` (MIDI expertise)
- Agent: `android-ui-designer` (UI)

**Resultado:**
```
ui/
├── contracts/
│   └── MidiSettingsContract.kt
├── presenters/
│   └── MidiSettingsPresenter.kt
└── fragments/
    └── MidiSettingsFragmentRefactored.kt

res/layout/
└── fragment_midi_settings_refactored.xml

test/
└── MidiSettingsPresenterTest.kt
```

---

### 6. Implementar MIDI Learn

**Solicitação:**
```
Add MIDI Learn functionality so users can map MIDI controller knobs to effect parameters
```

**Claude usará:**
- Agent: `toneforge-advanced-features` (especialista em MIDI)
- Agent: `android-ui-designer` (feedback visual)

**Implementação:**
- Sistema de MIDI Learn no ToneForgeMidiManager
- UI para indicar modo "Learning"
- Mapeamento persistente de CC → Parameter
- Visual feedback durante mapping

---

### 7. Otimizar Latência de Áudio

**Solicitação:**
```
The audio has too much latency on some devices. Can you optimize it?
```

**Claude usará:**
- Skill: `audio-test` (medir latência atual)
- Agent: `audio-dsp-engineer` (otimizar pipeline)

**Processo:**
1. Medir latência atual
2. Profile audio pipeline
3. Identificar gargalos
4. Otimizar buffer sizes
5. Reduzir overhead JNI
6. Testar em múltiplos devices
7. Documentar melhorias

---

### 8. Adicionar Testes para EffectsPresenter

**Solicitação:**
```
Add comprehensive unit tests for EffectsPresenter
```

**Claude usará:**
- Agent: `android-qa-engineer`

**Resultado:**
```kotlin
@Test
fun `when effect enabled, should update audio engine`() {
    // Arrange
    val effect = PedalEffect.DISTORTION

    // Act
    presenter.enableEffect(effect)

    // Assert
    verify(mockAudioRepository).enableEffect(effect)
    verify(mockView).showEffectEnabled(effect)
}

@Test
fun `when parameter changed, should update with debounce`() {
    // Test implementation
}

// Mais 20+ testes cobrindo todos os cenários
```

---

### 9. Implementar Preset Export/Import

**Solicitação:**
```
Users should be able to export their presets and share them with other ToneForge users
```

**Claude usará:**
- Agent: `toneforge-advanced-features` (PresetManager)
- Skill: `security-audit` (validar file handling)

**Implementação:**
- Serialização segura de presets
- File picker para import
- Share sheet para export
- Validação de formato
- Versioning de presets
- Testes de segurança

---

### 10. Criar Loop Library UI

**Solicitação:**
```
Create a UI for users to browse and manage their saved loops
```

**Claude usará:**
- Skill: `mvp-scaffold` (estrutura)
- Agent: `toneforge-utility-developer` (looper expertise)
- Agent: `android-ui-designer` (RecyclerView UI)

**Resultado:**
- LoopLibraryContract
- LoopLibraryPresenter com paginação
- RecyclerView com loops
- Swipe to delete
- Filtros e busca
- Play preview

---

## Combinações Avançadas

### Feature Completa: Sistema de Automation

**Solicitação:**
```
I want to add parameter automation - users should be able to record parameter changes over time and play them back
```

**Workflow completo:**

1. **Planejamento** (mvp-scaffold)
   - AutomationContract
   - AutomationPresenter
   - AutomationFragmentRefactored

2. **Implementação Backend** (toneforge-advanced-features)
   - AutomationRecorder
   - AutomationPlayer
   - Timeline data structure

3. **UI** (android-ui-designer)
   - Timeline view
   - Record/Play controls
   - Visual feedback

4. **Integração Audio** (audio-dsp-engineer)
   - Real-time parameter interpolation
   - Sync com audio pipeline

5. **Testes** (android-qa-engineer)
   - Unit tests
   - Integration tests
   - UI tests

6. **Validação** (audio-test)
   - Test automation recording
   - Test playback accuracy
   - Performance testing

---

## Dicas por Tipo de Tarefa

### Bug Fixes
```
Skill: debug-native (para crashes nativos)
Agent: audio-dsp-engineer ou android-architecture-reviewer
```

### Novas Features
```
Skill: mvp-scaffold
Agents: conforme a feature (UI, audio, advanced)
```

### Refatoração
```
Skill: refactor-legacy
Agent: android-architecture-reviewer
```

### Performance
```
Skill: audio-test
Agent: audio-dsp-engineer
```

### Segurança
```
Skill: security-audit
Use antes de cada release
```

---

## Perguntas Frequentes

### "Qual skill usar para X?"

- **Adicionar efeito?** → `cpp-effect`
- **Nova tela?** → `mvp-scaffold`
- **Crash?** → `debug-native`
- **Modernizar código?** → `refactor-legacy`
- **Release?** → `release-prep`
- **Segurança?** → `security-audit`
- **Testes?** → `audio-test`

### "Posso combinar skills?"

Sim! Exemplo:
```
Add a phaser effect and prepare for release
```
Claude usará `cpp-effect` + `audio-test` + `release-prep`

### "Como saber qual agent será usado?"

Claude escolhe automaticamente baseado no contexto:
- MIDI/Presets → `toneforge-advanced-features`
- Tuner/Looper → `toneforge-utility-developer`
- Native audio → `audio-dsp-engineer`
- UI → `android-ui-designer`
- Tests → `android-qa-engineer`
- Architecture → `android-architecture-reviewer`

---

## Próximos Passos

Experimente começar com tarefas simples:
1. "Run audio tests" (skill: audio-test)
2. "Add unit tests for TunerPresenter" (agent: qa-engineer)
3. "Review the architecture of EffectsFragment" (agent: architecture-reviewer)

Depois tente tarefas mais complexas:
1. "Add a tremolo effect"
2. "Refactor PresetFragment to MVP"
3. "Prepare for release 2.0.0"
