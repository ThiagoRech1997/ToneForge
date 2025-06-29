# 🎸 ToneForge - Pedaleira Digital

Uma pedaleira digital de efeitos sonoros para Android com processamento de áudio em tempo real usando código nativo C++.

## ✨ Características

- **Processamento em tempo real**: Baixa latência usando código C++ nativo via JNI
- **Múltiplos efeitos**: Ganho, distorção, delay, reverb, chorus, flanger e phaser
- **Sistema de presets**: Salvar, carregar e excluir configurações de efeitos
- **Ordem customizável**: Reordenar efeitos via drag-and-drop
- **Interface moderna**: Design escuro com controles intuitivos
- **Afinador em tempo real**: Pitch detection robusto, visual moderno, feedback instantâneo
- **Metrônomo**: Controle de BPM com integração ao motor de áudio
- **Looper**: Gravação e reprodução de loops (funcionalidade básica)
- **Compatibilidade**: Android 8.1+ (API 27) para suporte futuro ao AAudio

## 🎛️ Efeitos Disponíveis

### 🔊 Ganho
- Controle de volume de 0.0x a 2.0x
- Aplicado conforme ordem configurada na cadeia de efeitos

### 🎸 Distorção
- **4 tipos de distorção**: Soft Clip, Hard Clip, Fuzz e Overdrive
- Intensidade de 0% a 100%
- Controle de mix dry/wet (0-100%)
- Simula diferentes tipos de overdrive e distorção

### 🎵 Chorus
- **Depth**: 0-40ms de modulação
- **Rate**: 0-5Hz de velocidade de modulação
- **Mix**: Controle dry/wet (0-100%)
- Efeito de modulação suave para enriquecer o som

### 🌊 Flanger
- **Depth**: 0-10ms de modulação
- **Rate**: 0-5Hz de velocidade
- **Feedback**: 0-100% de realimentação
- **Mix**: Controle dry/wet (0-100%)
- Efeito de modulação mais intenso que o chorus

### 🔄 Phaser
- **Depth**: 0-100% de profundidade da modulação
- **Rate**: 0-5Hz de velocidade
- **Feedback**: 0-100% de realimentação
- **Mix**: Controle dry/wet (0-100%)
- Implementação com filtros passa-tudo em série (4 estágios)

### ⏱️ Delay
- Tempo de delay configurável
- Feedback de 0% a 100%
- Controle de mix dry/wet (0-100%)
- Buffer circular para eficiência

### 🏛️ Reverb
- Reverb com tamanho de sala configurável
- Amortecimento ajustável
- Controle de mix dry/wet (0-100%)
- Simula acústica de ambientes

### 🎵 Afinador (Tuner)
- Detecção de frequência em tempo real (pitch detection via autocorrelação em C++)
- Exibe nota, frequência (Hz), desvio em cents e barra de afinação com cor dinâmica (verde, amarelo, vermelho)
- Feedback visual instantâneo para facilitar a afinação precisa
- Robusto contra rotação de tela e uso intenso (thread-safe, mutex, checagens de null)
- Baixa latência e processamento eficiente

### ⏰ Metrônomo
- Controle de BPM de 30 a 200
- Integração com motor de áudio C++
- Interface visual com indicador de batida
- Funcionalidade básica implementada

### 🔄 Looper
- Gravação de loops de áudio
- Reprodução e limpeza de loops
- Integração com motor de áudio C++
- Funcionalidade básica implementada

### 🎙️ Gravador
- Interface para gravação de áudio
- Reprodução da última gravação
- Funcionalidade básica implementada (UI pronta)

### 🎚️ Equalizador (EQ)
- **Equalizador de 3 bandas**: Permite esculpir o timbre do áudio em tempo real, ajustando graves, médios e agudos de forma independente.
- **Low (Graves)**: Atua em 60Hz, ideal para reforçar ou suavizar o peso do som (-12dB a +12dB).
- **Mid (Médios)**: Atua em 1kHz, essencial para dar presença, clareza ou remover frequências incômodas (-12dB a +12dB).
- **High (Agudos)**: Atua em 8kHz, usado para adicionar brilho ou suavizar o som (-12dB a +12dB).
- **Mix**: Controle dry/wet (0-100%) para dosar o quanto do sinal equalizado é misturado ao original.
- **Aplicação**: Útil para adaptar o som ao instrumento, estilo musical ou ambiente, diretamente na cadeia de efeitos do app.

## 🎚️ Sistema de Presets

- **Salvar presets**: Guarde suas configurações favoritas com nomes personalizados
- **Carregar presets**: Acesse rapidamente suas configurações salvas
- **Excluir presets**: Remova presets que não usa mais
- **Persistência**: Presets são salvos automaticamente no dispositivo
- **Ordem dos efeitos**: Presets também salvam a ordem customizada dos efeitos

## 🔄 Ordem Customizável de Efeitos

- **Drag-and-drop**: Reordene efeitos arrastando e soltando
- **Ordem padrão**: Ganho → Distorção → Chorus → Flanger → Phaser → Delay → Reverb
- **Persistência**: A ordem é salva automaticamente
- **Tempo real**: Mudanças aplicadas instantaneamente

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
3. **Efeitos**: Aplicação sequencial conforme ordem configurada
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
│       ├── MainActivity.java      # Interface principal e navegação
│       ├── AudioEngine.java       # Pipeline de áudio em tempo real
│       ├── EffectsFragment.java   # Interface de efeitos e presets
│       ├── EffectOrderAdapter.java # Adapter para ordem de efeitos
│       ├── HomeFragment.java      # Tela inicial
│       ├── TunerFragment.java     # Afinador em tempo real
│       ├── MetronomeFragment.java # Metrônomo
│       ├── LooperFragment.java    # Looper de gravação
│       ├── RecorderFragment.java  # Gravador
│       ├── LearningFragment.java  # Tela de aprendizado
│       └── SettingsFragment.java  # Configurações
├── res/
│   ├── layout/
│   │   ├── activity_main.xml      # Layout principal
│   │   ├── fragment_effects.xml   # Interface de efeitos
│   │   ├── fragment_home.xml      # Tela inicial
│   │   ├── fragment_tuner.xml     # Afinador
│   │   ├── fragment_metronome.xml # Metrônomo
│   │   ├── fragment_looper.xml    # Looper
│   │   ├── fragment_recorder.xml  # Gravador
│   │   ├── fragment_learning.xml  # Aprendizado
│   │   └── fragment_settings.xml  # Configurações
│   ├── drawable/
│   │   ├── ic_*.xml               # Ícones da interface
│   │   ├── bg_gradient.xml        # Gradiente de fundo
│   │   ├── button_background.xml  # Estilo de botões
│   │   └── round_button.xml       # Botões arredondados
│   ├── navigation/
│   │   └── nav_graph.xml          # Navegação entre telas
│   ├── anim/
│   │   ├── slide_in_*.xml         # Animações de entrada
│   │   └── slide_out_*.xml        # Animações de saída
│   ├── values/
│   │   ├── colors.xml             # Cores do tema
│   │   ├── strings.xml            # Strings localizadas
│   │   ├── arrays.xml             # Arrays (tipos de distorção)
│   │   └── themes.xml             # Temas da aplicação
│   ├── values-night/
│   │   └── themes.xml             # Tema escuro
│   ├── mipmap-*/                  # Ícones do app (diferentes densidades)
│   └── xml/
│       ├── backup_rules.xml       # Regras de backup
│       └── data_extraction_rules.xml # Regras de extração
└── AndroidManifest.xml            # Configuração do app
```

## 🚀 Como Usar

1. **Instalar**: Compile e instale o APK no dispositivo Android
2. **Permissões**: Conceda permissão de gravação de áudio
3. **Navegar**: Use a navegação inferior para acessar diferentes funcionalidades
4. **Efeitos**: Na aba "Efeitos", ajuste os parâmetros em tempo real
5. **Presets**: Salve suas configurações favoritas
6. **Ordem**: Reordene os efeitos arrastando e soltando
7. **Testar**: Fale ou toque um instrumento no microfone

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
- **Fragments**: Navegação multi-tela
- **RecyclerView**: Interface de drag-and-drop
- **SharedPreferences**: Persistência de dados

## 🎯 Roadmap de Evolução

### ✅ Concluído
- [x] Pipeline de áudio em tempo real
- [x] Efeitos básicos (Ganho, Distorção, Delay, Reverb)
- [x] Afinador em tempo real
- [x] Sistema de presets
- [x] Ordem customizável de efeitos
- [x] Efeitos de modulação (Chorus, Flanger, Phaser)
- [x] Controles avançados (mix dry/wet, tipos de distorção)
- [x] Interface multi-tela com navegação

### 🚧 Parcialmente Implementado
- [x] Metrônomo (UI + integração básica com C++)
- [x] Looper (UI + integração básica com C++)
- [x] Gravador (UI pronta, funcionalidade básica)

### 🔄 Em Desenvolvimento
- [ ] Melhorias no metrônomo (animações, visualização)
- [ ] Melhorias no looper (timer, visualização de duração)
- [ ] Funcionalidade completa do gravador
- [ ] Equalizador (EQ) com controles de graves, médios e agudos
- [ ] Compressor para nivelamento de volume
- [ ] Melhorias de interface (tooltips, reset rápido)

### 📋 Próximas Funcionalidades
- [ ] Exportar/importar presets
- [ ] Favoritos e categorização
- [ ] Visualização gráfica da cadeia de efeitos
- [ ] Oversampling para melhor qualidade
- [ ] Processamento em background
- [ ] MIDI Learn para controle externo
- [ ] Automação de parâmetros
- [ ] Sincronização com metrônomo
- [ ] Curvas de resposta customizáveis

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
5. **Presets não salvam**: Verifique espaço em disco
6. **Ordem não persiste**: Reinicie o app

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