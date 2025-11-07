# ToneForge - Quick Reference

## 🎯 Comandos Mais Usados

### Desenvolvimento
| Quero... | Diga ao Claude... | Skill/Agent |
|----------|-------------------|-------------|
| Adicionar efeito de áudio | "Add a [effect name] effect" | cpp-effect |
| Criar nova tela/fragment | "Create a [feature] screen" | mvp-scaffold |
| Refatorar código antigo | "Refactor [Fragment] to MVP" | refactor-legacy |
| Adicionar testes | "Add tests for [Class]" | android-qa-engineer |
| Revisar arquitetura | "Review architecture of [Component]" | android-architecture-reviewer |

### Qualidade & Release
| Quero... | Diga ao Claude... | Skill/Agent |
|----------|-------------------|-------------|
| Testar áudio | "Run audio tests" | audio-test |
| Auditoria de segurança | "Security audit" | security-audit |
| Preparar release | "Prepare release [version]" | release-prep |
| Debug crash nativo | "Debug crash: [logcat]" | debug-native |

### Features Avançadas
| Quero... | Diga ao Claude... | Agent |
|----------|-------------------|-------|
| MIDI functionality | "Implement MIDI [feature]" | toneforge-advanced-features |
| Preset system | "Add preset [feature]" | toneforge-advanced-features |
| Tuner/Looper/Metronome | "Improve [utility]" | toneforge-utility-developer |

## 📋 Checklists Rápidos

### Antes de Commitar
```
- [ ] Código compila sem erros
- [ ] Testes unitários passando
- [ ] Lint sem erros críticos
- [ ] Sem TODOs críticos pendentes
```

### Antes de Release
```
- [ ] Todos os testes passando
- [ ] Security audit executado
- [ ] Performance testada
- [ ] Documentação atualizada
- [ ] CHANGELOG atualizado
- [ ] Version numbers atualizados
```

### Ao Adicionar Efeito
```
- [ ] Algoritmo DSP implementado
- [ ] JNI bindings criados
- [ ] UI controls adicionados
- [ ] Preset integration
- [ ] Testes criados
- [ ] Documentação
```

### Ao Criar Fragment
```
- [ ] Contract interface
- [ ] Presenter implementado
- [ ] Fragment refactored
- [ ] Layout XML
- [ ] Navigation configurada
- [ ] Testes unitários
```

## 🚀 Atalhos de Build

```bash
# Build completo
./gradlew clean assembleDebug

# Apenas testes
./gradlew test

# Testes + Coverage
./gradlew jacocoTestReport

# Validação funcional
./scripts/functional-validation.sh

# Release
./scripts/create-release.sh X.Y.Z "Message"
```

## 🔍 Debug Rápido

### Logcat
```bash
# Filtrar por ToneForge
adb logcat | grep ToneForge

# Ver apenas erros
adb logcat *:E

# Salvar em arquivo
adb logcat -d > crash.log
```

### Native Debug
```bash
# Symbolicate stack trace
ndk-stack -sym app/build/intermediates/cmake/debug/obj < crash.log

# Enable CheckJNI
adb shell setprop debug.checkjni 1
```

## 📁 Estrutura de Arquivos

### Criar novo MVP Fragment
```
ui/contracts/YourFeatureContract.kt
ui/presenters/YourFeaturePresenter.kt
ui/fragments/YourFeatureFragmentRefactored.kt
res/layout/fragment_your_feature_refactored.xml
test/ui/presenters/YourFeaturePresenterTest.kt
```

### Adicionar efeito C++
```
cpp/audio_engine.cpp           # Implementação
cpp/native-lib.cpp             # JNI bindings
infrastructure/audio/AudioEngine.kt  # Kotlin wrapper
ui/fragments/EffectsFragmentRefactored.kt  # UI
```

## 💡 Templates Rápidos

### JNI Method
```cpp
extern "C" JNIEXPORT void JNICALL
Java_..._setEffectParameter(JNIEnv* env, jobject, jfloat value) {
    if (value < 0.0f || value > 1.0f) return;
    effectParam = value;
}
```

### MVP Contract
```kotlin
interface YourFeatureContract {
    interface View {
        fun showData(data: Data)
        fun showError(message: String)
    }

    interface Presenter {
        fun loadData()
        fun onUserAction()
    }
}
```

### Presenter Test
```kotlin
@Test
fun `when action performed, should update view`() {
    // Arrange
    val testData = TestData()

    // Act
    presenter.performAction()

    // Assert
    verify(mockView).showData(testData)
}
```

## 🎓 Recursos de Aprendizado

### Documentação Principal
- [.claude/README.md](.claude/README.md) - Visão geral
- [.claude/EXAMPLES.md](.claude/EXAMPLES.md) - 10 exemplos
- [.claude/skills/USAGE.md](.claude/skills/USAGE.md) - Guia completo
- [CLAUDE.md](../CLAUDE.md) - Projeto overview

### Skills
- [skills/](skills/) - 7 workflows especializados
- [skills-index.md](skills-index.md) - Índice visual

### Agents
- [agents/](agents/) - 6 especialistas

## ⚡ Pro Tips

1. **Combine skills**: "Add effect AND prepare release"
2. **Seja específico**: Mencione parâmetros, nomes, detalhes
3. **Use contexto**: Referencie código existente
4. **Peça validação**: "Review before committing"
5. **Itere**: Peça melhorias após implementação

## 🐛 Troubleshooting Comum

### Build falha
```bash
./gradlew clean
./gradlew assembleDebug --info
```

### Testes falham
```bash
./gradlew test --info --stacktrace
```

### Native crash
```bash
# Habilite ASAN no build.gradle
# Capture logcat
# Use ndk-stack
```

### UI não atualiza
```
- Verifique lifecycle do Fragment
- Confirme view binding está correto
- Valide presenter attachment
```

## 📞 Quando Pedir Ajuda

### Use Skills Para:
- Processos repetíveis
- Workflows estabelecidos
- Tarefas multi-passo

### Use Agents Para:
- Implementações específicas
- Expertise especializada
- Tarefas técnicas complexas

### Peça Explicação:
- "Explain how [feature] works"
- "What's the best way to [task]"
- "Review my approach to [problem]"

---

**Pro Tip**: Mantenha este arquivo aberto enquanto trabalha no ToneForge!
