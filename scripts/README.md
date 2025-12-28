# ToneForge - Scripts

Este diretório contém scripts utilitários para desenvolvimento e testes do ToneForge.

## 📁 Estrutura de Scripts

```
scripts/
├── README.md                      # Este arquivo
├── setup/                         # Scripts de configuração de ambiente
│   ├── install-android-sdk.sh   # Instalação do Android SDK
│   └── setup-dev-environment.sh  # Setup do ambiente de desenvolvimento
├── test-app-device.sh            # Script de teste em dispositivo
└── verify-environment.sh         # Verificação do ambiente
```

## 🛠️ Scripts Disponíveis

### Setup e Configuração

#### `setup/install-android-sdk.sh`
Instala e configura o Android SDK necessário para desenvolvimento.

**Uso:**
```bash
./scripts/setup/install-android-sdk.sh
```

#### `setup/setup-dev-environment.sh`
Configura completamente o ambiente de desenvolvimento incluindo:
- Android SDK
- Build tools
- Platform tools
- Emuladores (opcional)

**Uso:**
```bash
./scripts/setup/setup-dev-environment.sh
```

### Verificação

#### `verify-environment.sh`
Verifica se o ambiente de desenvolvimento está configurado corretamente.

**Uso:**
```bash
./scripts/verify-environment.sh
```

**Saída esperada:**
- ✅ Android SDK instalado
- ✅ Gradle disponível
- ✅ ADB funcionando
- ✅ Variáveis de ambiente configuradas

### Testes

#### `test-app-device.sh`
Executa testes completos do app em dispositivo conectado via ADB.

**Uso:**
```bash
./scripts/test-app-device.sh
```

**Funcionalidades:**
- Verifica dispositivo conectado
- Compila o app
- Instala no dispositivo
- Executa testes automatizados
- Coleta logs

## 🔧 Outros Scripts Úteis

### Build e Deploy
```bash
# Build debug
./gradlew assembleDebug

# Build e instalar
./gradlew installDebug

# Build release
./gradlew assembleRelease
```

### Testes
```bash
# Testes unitários
./gradlew test

# Testes instrumentados
./gradlew connectedAndroidTest

# Coverage report
./gradlew jacocoTestReport
```

### Validação
```bash
# Validação funcional completa
./scripts/functional-validation.sh

# Criar release
./scripts/create-release.sh 1.0.0 "Release message"
```

## 📝 Convenções

- Scripts executáveis devem ter permissão de execução (`chmod +x`)
- Scripts de setup estão em `setup/`
- Scripts de teste começam com `test-`
- Scripts de validação começam com `verify-` ou `validate-`

## 🔍 Logs

Logs gerados pelos scripts são salvos em `/logs/` na raiz do projeto.

## ⚠️ Requisitos

Certifique-se de ter:
- Bash 4.0+
- Android SDK instalado (ou use `setup-dev-environment.sh`)
- Gradle 7.0+
- ADB acessível no PATH

## 🔄 Atualizações

**Última atualização:** 2025-12-28
