# 🔧 ToneForge - Guia de Configuração do Ambiente de Desenvolvimento

Este guia ajudará você a configurar o ambiente de desenvolvimento Android para o ToneForge.

## ✅ Pré-requisitos

- Ubuntu/Debian ou WSL2
- Acesso sudo
- Conexão com internet

---

## 📦 Passo 1: Instalar OpenJDK 17

O ToneForge requer Java 17 para compilação. Execute os comandos abaixo:

```bash
# Atualizar lista de pacotes
sudo apt update

# Instalar OpenJDK 17
sudo apt install -y openjdk-17-jdk

# Verificar instalação
java -version
```

**Saída esperada:**
```
openjdk version "17.0.x" ...
```

---

## 🔧 Passo 2: Configurar JAVA_HOME

Após instalar o Java, configure a variável de ambiente `JAVA_HOME`:

```bash
# Descobrir o caminho do Java
update-alternatives --list java

# Normalmente será algo como:
# /usr/lib/jvm/java-17-openjdk-amd64/bin/java

# Configurar JAVA_HOME (remova o /bin/java do final)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Verificar
echo $JAVA_HOME
javac -version
```

---

## 💾 Passo 3: Tornar Configuração Permanente

Adicione ao seu `~/.bashrc` ou `~/.zshrc`:

```bash
# Adicionar ao final do arquivo
echo '# Java Configuration' >> ~/.bashrc
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc

# Recarregar configuração
source ~/.bashrc
```

---

## 📱 Passo 4: Instalar Android SDK (Opcional)

O Gradle pode baixar automaticamente o Android SDK, mas você pode instalá-lo manualmente:

### Opção A: Via SDK Manager (Recomendado)

```bash
# O Gradle baixará automaticamente quando você executar:
cd /home/thiago/thiago-code/07-entretenimento/ToneForge
./gradlew assembleDebug
```

### Opção B: Android Studio (Mais Completo)

1. Baixe Android Studio: https://developer.android.com/studio
2. Instale e configure o SDK
3. Configure `ANDROID_HOME`:

```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

---

## 🛠️ Passo 5: Instalar NDK (Native Development Kit)

O ToneForge usa código C++ nativo. O NDK será baixado automaticamente pelo Gradle, mas você pode verificar:

```bash
cd /home/thiago/thiago-code/07-entretenimento/ToneForge

# Verificar configuração do NDK no gradle.properties
cat gradle.properties | grep ndk

# O Gradle baixará automaticamente na primeira compilação
```

---

## ✅ Passo 6: Verificar Instalação Completa

Execute este script para validar a configuração:

```bash
cd /home/thiago/thiago-code/07-entretenimento/ToneForge

# Criar script de verificação
cat > verify-setup.sh << 'EOF'
#!/bin/bash

echo "🔍 Verificando Ambiente de Desenvolvimento ToneForge"
echo "===================================================="
echo ""

# Verificar Java
echo "📦 Java:"
if java -version 2>&1 | grep -q "openjdk version"; then
    java -version 2>&1 | head -1
    echo "✅ Java instalado"
else
    echo "❌ Java não encontrado"
fi
echo ""

# Verificar JAVA_HOME
echo "🔧 JAVA_HOME:"
if [ -n "$JAVA_HOME" ]; then
    echo "JAVA_HOME=$JAVA_HOME"
    echo "✅ JAVA_HOME configurado"
else
    echo "❌ JAVA_HOME não configurado"
fi
echo ""

# Verificar javac
echo "🛠️ Java Compiler:"
if command -v javac &> /dev/null; then
    javac -version 2>&1
    echo "✅ javac disponível"
else
    echo "❌ javac não encontrado"
fi
echo ""

# Verificar Gradle
echo "🏗️ Gradle:"
if [ -f "./gradlew" ]; then
    echo "✅ gradlew encontrado"
    ./gradlew --version | head -5
else
    echo "❌ gradlew não encontrado"
fi
echo ""

# Verificar estrutura do projeto
echo "📂 Estrutura do Projeto:"
if [ -f "app/build.gradle.kts" ]; then
    echo "✅ app/build.gradle.kts encontrado"
else
    echo "❌ app/build.gradle.kts não encontrado"
fi

if [ -d "app/src/main/cpp" ]; then
    echo "✅ Código C++ nativo encontrado"
else
    echo "❌ Código C++ nativo não encontrado"
fi
echo ""

echo "🎯 Resumo:"
echo "=========="
if java -version 2>&1 | grep -q "openjdk version" && [ -n "$JAVA_HOME" ]; then
    echo "✅ Ambiente pronto para desenvolvimento!"
    echo ""
    echo "Próximos passos:"
    echo "  1. ./gradlew assembleDebug    # Compilar o projeto"
    echo "  2. ./gradlew test              # Executar testes"
    echo "  3. ./scripts/functional-validation.sh  # Validação completa"
else
    echo "⚠️ Configuração incompleta. Siga os passos acima."
fi
EOF

chmod +x verify-setup.sh
./verify-setup.sh
```

---

## 🚀 Passo 7: Compilar o Projeto

Após configurar o Java, compile o ToneForge:

```bash
cd /home/thiago/thiago-code/07-entretenimento/ToneForge

# Limpar builds anteriores
./gradlew clean

# Compilar versão debug
./gradlew assembleDebug

# Executar testes unitários
./gradlew test

# Gerar relatório de cobertura
./gradlew jacocoTestReport

# Validação funcional completa
./scripts/functional-validation.sh
```

---

## 📱 Passo 8: Instalar em Dispositivo Android

Se você tiver um dispositivo Android conectado via USB:

```bash
# Habilitar USB Debugging no dispositivo:
# Configurações → Sobre o telefone → Tocar 7x em "Número da versão"
# Configurações → Opções do desenvolvedor → Ativar "Depuração USB"

# Verificar dispositivo conectado
adb devices

# Instalar APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Ver logs em tempo real
adb logcat | grep ToneForge
```

---

## 🐛 Troubleshooting

### Erro: "JAVA_HOME is not set"

```bash
# Verificar se Java está instalado
java -version

# Configurar JAVA_HOME temporariamente
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))

# Adicionar ao .bashrc permanentemente
echo "export JAVA_HOME=$JAVA_HOME" >> ~/.bashrc
source ~/.bashrc
```

### Erro: "SDK location not found"

```bash
# Criar local.properties
echo "sdk.dir=$HOME/Android/Sdk" > local.properties

# Ou deixar o Gradle baixar automaticamente
./gradlew assembleDebug
```

### Erro: "NDK not found"

```bash
# O NDK será baixado automaticamente
# Ou configurar manualmente no local.properties:
echo "ndk.dir=$HOME/Android/Sdk/ndk/25.1.8937393" >> local.properties
```

### Erro de Compilação C++

```bash
# Verificar CMake
cmake --version

# Instalar se necessário
sudo apt install cmake

# Verificar configuração no app/build.gradle.kts
grep -A 5 "externalNativeBuild" app/build.gradle.kts
```

---

## 📚 Recursos Adicionais

- [Documentação ToneForge](CLAUDE.md)
- [Android Developer Guide](https://developer.android.com/guide)
- [Gradle Build Tool](https://gradle.org/guides/)
- [Android NDK Guide](https://developer.android.com/ndk/guides)

---

## ✅ Checklist de Configuração

- [ ] OpenJDK 17 instalado
- [ ] JAVA_HOME configurado
- [ ] JAVA_HOME adicionado ao .bashrc
- [ ] Gradle wrapper funcional (./gradlew --version)
- [ ] Projeto compila (./gradlew assembleDebug)
- [ ] Testes passam (./gradlew test)
- [ ] APK gerado (app/build/outputs/apk/debug/app-debug.apk)

---

**🎸 Pronto! Seu ambiente está configurado para desenvolver no ToneForge!**
