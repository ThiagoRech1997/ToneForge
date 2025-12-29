# ToneForge - Índice de Skills e Agents

## Skills (Workflows)

### Audio & DSP
| Skill | Descrição | Quando Usar |
|-------|-----------|-------------|
| [audio-test](skills/audio-test.md) | Testes de áudio | Validar processamento de áudio |
| [cpp-effect](skills/cpp-effect.md) | Adicionar efeito | Criar novo efeito de áudio |
| [debug-native](skills/debug-native.md) | Debug C++ | Debugar código nativo |

### Arquitetura
| Skill | Descrição | Quando Usar |
|-------|-----------|-------------|
| [mvp-scaffold](skills/mvp-scaffold.md) | Scaffold MVP | Criar novo fragment |
| [refactor-legacy](skills/refactor-legacy.md) | Refatorar | Modernizar código legado |

### Qualidade
| Skill | Descrição | Quando Usar |
|-------|-----------|-------------|
| [security-audit](skills/security-audit.md) | Auditoria | Antes de releases |
| [release-prep](skills/release-prep.md) | Release | Preparar versão |

## Agents (Especialistas)

### Android
| Agent | Especialidade |
|-------|---------------|
| [android-architecture-reviewer](agents/android-architecture-reviewer.md) | Review de arquitetura MVP |
| [android-ui-designer](agents/android-ui-designer.md) | Design de UI/UX |
| [android-qa-engineer](agents/android-qa-engineer.md) | Testes e QA |

### Audio
| Agent | Especialidade |
|-------|---------------|
| [audio-dsp-engineer](agents/audio-dsp-engineer.md) | DSP e C++ audio |

### Features
| Agent | Especialidade |
|-------|---------------|
| [toneforge-advanced-features](agents/toneforge-advanced-features.md) | MIDI, Presets, Automação |
| [toneforge-utility-developer](agents/toneforge-utility-developer.md) | Tuner, Looper, Metronome |

## Workflows por Tarefa

### Adicionar Novo Efeito
```
Skill: cpp-effect
Agents: audio-dsp-engineer, android-ui-designer, android-qa-engineer
```

### Criar Nova Tela
```
Skill: mvp-scaffold
Agents: android-architecture-reviewer, android-ui-designer
```

### Refatorar Fragment
```
Skill: refactor-legacy
Agents: android-architecture-reviewer, android-qa-engineer
```

### Preparar Release
```
Skills: audio-test, security-audit, release-prep
Agents: android-qa-engineer
```

### Debugar Crash Nativo
```
Skill: debug-native
Agent: audio-dsp-engineer
```

## Comandos Rápidos

### Desenvolvimento
```bash
# Build
./gradlew assembleDebug

# Testes
./gradlew test
./gradlew connectedAndroidTest

# Validação
./scripts/functional-validation.sh
```

### Release
```bash
# Preparar
./scripts/create-release.sh 1.0.0 "Message"

# Build release
./gradlew assembleRelease
./gradlew bundleRelease
```

## Recursos

- **Documentação**: [CLAUDE.md](../CLAUDE.md)
- **Skills**: [skills/](skills/)
- **Agents**: [agents/](agents/)
- **Guia de Uso**: [skills/USAGE.md](skills/USAGE.md)
