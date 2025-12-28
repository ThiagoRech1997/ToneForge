# 🚀 ToneForge - Guia de Início Rápido

## ⚡ Setup Rápido (15 minutos)

### 1️⃣ Configurar Java (5 minutos)

```bash
cd /home/thiago/thiago-code/07-entretenimento/ToneForge

# Executar script de configuração automática
./setup-dev-environment.sh

# Seguir as instruções na tela
# O script irá:
#   - Verificar/instalar Java 17
#   - Configurar JAVA_HOME
#   - Verificar Gradle
#   - (Opcional) Testar compilação
```

### 2️⃣ Instalar Android SDK (10 minutos)

```bash
# Executar script de instalação do Android SDK
./install-android-sdk.sh

# O script irá:
#   - Baixar Android Command Line Tools (~150MB)
#   - Instalar Android SDK Platform 36
#   - Instalar Build Tools 36.0.0
#   - Instalar NDK 27.2 (para código C++ nativo)
#   - Instalar CMake 3.22.1
#   - Criar arquivo local.properties
#   - Configurar ANDROID_HOME

# Aplicar mudanças
source ~/.bashrc
```

### 3️⃣ Compilar o Projeto

```bash
# Limpar builds anteriores
./gradlew clean

# Compilar versão debug
./gradlew assembleDebug

# ✅ APK gerado em: app/build/outputs/apk/debug/app-debug.apk
```

### 4️⃣ Executar Testes

```bash
# Testes unitários
./gradlew test

# Testes com cobertura
./gradlew jacocoTestReport

# Relatório em: app/build/reports/jacoco/test/html/index.html
```

### 5️⃣ Validação Funcional

```bash
# Executar validação completa do projeto
./scripts/functional-validation.sh

# Verifica:
#   ✅ Compilação
#   ✅ Testes
#   ✅ Estrutura MVP
#   ✅ Arquitetura
```

---

## 📱 Instalação em Dispositivo

### Via USB (Recomendado)

```bash
# 1. Habilitar "Depuração USB" no Android:
#    Configurações → Sobre → Tocar 7x em "Número da versão"
#    Configurações → Opções do desenvolvedor → Depuração USB

# 2. Conectar dispositivo e verificar
adb devices

# 3. Instalar APK
adb install app/build/outputs/apk/debug/app-debug.apk

# 4. Ver logs
adb logcat | grep ToneForge
```

### Via Android Studio

```bash
# Abrir projeto no Android Studio
studio /home/thiago/thiago-code/07-entretenimento/ToneForge

# Usar botão "Run" (Shift+F10)
```

---

## 🛠️ Comandos Úteis

### Desenvolvimento

```bash
# Build
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build
./gradlew clean                  # Limpar builds

# Testes
./gradlew test                   # Testes unitários
./gradlew connectedAndroidTest   # Testes UI (requer dispositivo)
./gradlew jacocoTestReport       # Relatório de cobertura

# Análise
./gradlew lint                   # Linter
./gradlew dependencies           # Ver dependências
```

### Logs

```bash
# Ver logs do app
adb logcat | grep ToneForge

# Limpar logs
./scripts/clean-logs.sh

# Salvar logs em arquivo
adb logcat | grep ToneForge > toneforge.log
```

### APK

```bash
# Gerar APK assinado (release)
./gradlew assembleRelease

# Ver informações do APK
aapt dump badging app/build/outputs/apk/debug/app-debug.apk

# Instalar versão específica
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📂 Estrutura do Projeto

```
ToneForge/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Código Java (MVP, Clean Architecture)
│   │   │   ├── cpp/           # Código C++ nativo (processamento de áudio)
│   │   │   ├── res/           # Recursos (layouts, strings, drawables)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/              # Testes unitários
│   │   └── androidTest/       # Testes instrumentados
│   └── build.gradle.kts       # Configuração do módulo app
├── docs/                      # Documentação técnica (29 arquivos)
├── scripts/                   # Scripts utilitários
│   ├── functional-validation.sh
│   └── create-release.sh
├── CLAUDE.md                  # Guia do projeto
├── SETUP-ENVIRONMENT.md       # Guia de configuração detalhado
├── QUICKSTART.md             # Este arquivo
└── setup-dev-environment.sh   # Script de setup automático
```

---

## 🎯 Workflows Comuns

### Workflow 1: Adicionar Novo Efeito de Áudio

```bash
# 1. Modificar C++
vim app/src/main/cpp/audio_engine.cpp

# 2. Adicionar UI
vim app/src/main/java/.../fragments/effects/EffectsFragmentRefactored.java

# 3. Testar
./gradlew test
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 4. Validar
./scripts/functional-validation.sh
```

### Workflow 2: Refatorar Fragment

```bash
# 1. Criar Contract
vim app/src/main/java/.../fragments/novo/NovoContract.java

# 2. Criar Presenter
vim app/src/main/java/.../fragments/novo/NovoPresenter.java

# 3. Criar Fragment
vim app/src/main/java/.../fragments/novo/NovoFragmentRefactored.java

# 4. Adicionar testes
vim app/src/test/java/.../fragments/novo/NovoPresenterTest.java

# 5. Executar testes
./gradlew test
```

### Workflow 3: Criar Release

```bash
# Usar script de release
./scripts/create-release.sh 1.0.0 "Initial release"

# Verificar release
git tag -l
git log --oneline
```

---

## 🐛 Troubleshooting Rápido

### Problema: "JAVA_HOME is not set"

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
source ~/.bashrc
```

### Problema: Compilação falha

```bash
# Limpar e reconstruir
./gradlew clean
./gradlew assembleDebug --stacktrace
```

### Problema: Testes falhando

```bash
# Executar com mais detalhes
./gradlew test --info

# Ver relatório
xdg-open app/build/reports/tests/testDebugUnitTest/index.html
```

### Problema: APK não instala

```bash
# Desinstalar versão anterior
adb uninstall com.thiagofernendorech.toneforge

# Reinstalar
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📚 Documentação Adicional

- **[SETUP-ENVIRONMENT.md](SETUP-ENVIRONMENT.md)** - Configuração detalhada
- **[CLAUDE.md](CLAUDE.md)** - Arquitetura, features, comandos
- **[docs/](docs/)** - 29 documentos técnicos:
  - architecture-refactoring-plan.md
  - testing-strategy.md
  - implementation-guide.md
  - manual-validation-checklist.md
  - E muito mais...

---

## 🎸 Features Principais

- ✅ **9 Efeitos de Áudio**: Gain, Distortion, Delay, Reverb, Chorus, Flanger, Phaser, EQ, Compressor
- ✅ **Looper Avançado**: Multi-track, reverse, pitch shift, slicing
- ✅ **Afinador**: Detecção de pitch em tempo real
- ✅ **Metrônomo**: BPM configurável
- ✅ **Presets**: Sistema completo com favoritos
- ✅ **MIDI Learn**: Controle externo
- ✅ **Background Processing**: Continua com tela desligada
- ✅ **Clean Architecture**: MVP + Domain + Infrastructure

---

## ✅ Checklist de Início Rápido

- [ ] Executar `./setup-dev-environment.sh`
- [ ] Configurar JAVA_HOME
- [ ] Compilar com `./gradlew assembleDebug`
- [ ] Executar testes `./gradlew test`
- [ ] Validação funcional `./scripts/functional-validation.sh`
- [ ] Instalar em dispositivo `adb install ...`
- [ ] Testar features principais
- [ ] Ler documentação em `CLAUDE.md`

---

**🎸 Pronto para começar a desenvolver no ToneForge!**

Para ajuda: `cat CLAUDE.md` ou `cat SETUP-ENVIRONMENT.md`
