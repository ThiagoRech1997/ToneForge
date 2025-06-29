# 🎸 ToneForge - Pedaleira Digital

Uma pedaleira digital de efeitos sonoros para Android com processamento de áudio em tempo real usando código nativo C++.

## ✨ Características

- **Processamento em tempo real**: Baixa latência usando código C++ nativo via JNI
- **Múltiplos efeitos**: Ganho, distorção, delay e reverb
- **Interface moderna**: Design escuro com controles intuitivos
- **Compatibilidade**: Android 8.1+ (API 27) para suporte futuro ao AAudio

## 🎛️ Efeitos Disponíveis

### 🔊 Ganho
- Controle de volume de 0.0x a 2.0x
- Aplicado como último estágio do processamento

### 🎸 Distorção
- Distorção baseada em tanh() com drive variável
- Intensidade de 0% a 100%
- Simula overdrive de amplificadores

### ⏱️ Delay
- Tempo de delay de 0ms a 1000ms
- Feedback de 0% a 100%
- Buffer circular para eficiência

### 🏛️ Reverb
- Reverb simples com tamanho de sala configurável
- Amortecimento ajustável
- Simula acústica de ambientes

## 🏗️ Arquitetura

### Componentes Principais

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   AudioRecord   │───▶│  Audio Engine   │───▶│   AudioTrack    │
│   (Entrada)     │    │   (C++/JNI)     │    │   (Saída)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Pipeline de Processamento

1. **Captura**: `AudioRecord` captura áudio do microfone
2. **Processamento**: Buffer é enviado para `audio_engine.cpp` via JNI
3. **Efeitos**: Aplicação sequencial de ganho, distorção, delay e reverb
4. **Reprodução**: `AudioTrack` reproduz o áudio processado

### Estrutura de Arquivos

```
app/src/main/
├── cpp/
│   ├── audio_engine.h      # Header do motor de áudio
│   ├── audio_engine.cpp    # Implementação dos efeitos
│   ├── native-lib.cpp      # Métodos JNI
│   └── CMakeLists.txt      # Configuração do build
├── java/
│   └── com/thiagofernendorech/toneforge/
│       └── MainActivity.java  # Interface e lógica de áudio
└── res/
    ├── layout/
    │   └── activity_main.xml  # Interface de usuário
    ├── drawable/
    │   └── button_background.xml
    └── values/
        └── colors.xml
```

## 🚀 Como Usar

1. **Instalar**: Compile e instale o APK no dispositivo Android
2. **Permissões**: Conceda permissão de gravação de áudio
3. **Iniciar**: Toque em "▶️ Iniciar Áudio"
4. **Ajustar**: Use os controles deslizantes para configurar os efeitos
5. **Testar**: Fale ou toque um instrumento no microfone

## 🔧 Configuração de Desenvolvimento

### Pré-requisitos

- Android Studio Hedgehog ou superior
- Android SDK API 27+
- NDK (Native Development Kit)
- Dispositivo Android com microfone

### Build

```bash
# Clone o repositório
git clone <repository-url>
cd ToneForge

# Abra no Android Studio ou compile via linha de comando
./gradlew assembleDebug
```

### Estrutura do Projeto

O projeto usa:
- **Gradle**: Build system
- **CMake**: Build do código nativo
- **JNI**: Interface Java-C++
- **AudioRecord/AudioTrack**: API de áudio Android

## 🎯 Próximas Melhorias

- [ ] Suporte ao AAudio para menor latência
- [ ] Interface USB/OTG para instrumentos
- [ ] Mais efeitos (chorus, flanger, compressor)
- [ ] Presets salvos
- [ ] Visualização de espectro
- [ ] MIDI control
- [ ] Loop de gravação

## 📱 Compatibilidade

- **Android mínimo**: 8.1 (API 27)
- **Arquiteturas**: ARM64, ARM32, x86, x86_64
- **Taxa de amostragem**: 48kHz
- **Formato**: PCM Float 32-bit
- **Canais**: Mono (entrada e saída)

## 🔍 Troubleshooting

### Problemas Comuns

1. **Áudio não funciona**: Verifique permissões de microfone
2. **Latência alta**: Use dispositivo com baixa latência de áudio
3. **Crash no start**: Verifique se NDK está instalado
4. **Efeitos não aplicam**: Reinicie o áudio

### Logs

Use `adb logcat` para ver logs detalhados:
```bash
adb logcat | grep ToneForge
```

## 📄 Licença

Este projeto é open source. Sinta-se livre para contribuir!

## 👨‍💻 Autor

Thiago Fernando Rech - Desenvolvedor Android e entusiasta de áudio

---

**ToneForge** - Transformando seu Android em uma pedaleira profissional! 🎸 