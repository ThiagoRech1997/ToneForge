# 🎸 ToneForge - Pedaleira Digital

Uma pedaleira digital de efeitos sonoros para Android com processamento de áudio em tempo real usando código nativo C++.

## ✨ Características

- **Processamento em tempo real**: Baixa latência usando código C++ nativo via JNI
- **Múltiplos efeitos**: Ganho, distorção, delay, reverb, chorus, flanger, phaser, EQ e compressor
- **Sistema de presets**: Salvar, carregar, exportar/importar e favoritar configurações
- **Ordem customizável**: Reordenar efeitos via drag-and-drop
- **Interface moderna**: Design escuro com controles intuitivos e tooltips informativos
- **Afinador em tempo real**: Pitch detection robusto, visual moderno, feedback instantâneo
- **Metrônomo**: Controle de BPM com integração ao motor de áudio
- **Looper**: Gravação e reprodução de loops (funcionalidade básica)
- **Processamento em background**: Áudio contínuo com tela desligada
- **Recuperação de estado**: Restaura configurações ao retornar do background
- **Oversampling**: Melhoria de qualidade para distorção e delay
- **MIDI Learn**: Controle de parâmetros via MIDI externo
- **Sistema de automação**: Gravar e reproduzir mudanças de parâmetros ao longo do tempo
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

### 🎛️ Compressor
- **Controle de dinâmica**: Nivela o volume do sinal para maior consistência
- **Threshold**: Ponto onde a compressão começa (-60dB a 0dB)
- **Ratio**: Intensidade da compressão (1:1 a 20:1)
- **Attack**: Velocidade de resposta (0.1ms a 100ms)
- **Release**: Velocidade de recuperação (10ms a 1000ms)
- **Mix**: Controle dry/wet (0-100%) para misturar sinal original e comprimido

## 🔄 Processamento em Background

### 🎵 Áudio Contínuo
- **ForegroundService**: Mantém o processamento de áudio ativo mesmo com a tela desligada
- **Notificação persistente**: Mostra status do áudio e controles rápidos
- **Controle de ativação**: Switch nas configurações para ativar/desativar
- **Gerenciamento de pipeline**: Recuperação automática em caso de erros

### 🔧 Recuperação de Estado
- **Salvamento automático**: Estado salvo quando o app vai para background
- **Restauração inteligente**: Configurações restauradas ao retornar
- **Recuperação de**: Pipeline, presets, oversampling, efeitos ativos
- **Sincronização**: Interface atualizada com estado real

### 🛡️ Sistema de Permissões
- **Permissões necessárias**: Microfone, notificações, armazenamento
- **Permissões opcionais**: Overlay, otimização de bateria
- **Verificação automática**: Solicitação de permissões ao iniciar
- **Compatibilidade**: Suporte a diferentes versões do Android

## 🎛️ Sistema de Presets

- **Salvar presets**: Guarde suas configurações favoritas com nomes personalizados
- **Carregar presets**: Acesse rapidamente suas configurações salvas
- **Excluir presets**: Remova presets que não usa mais
- **Exportar/Importar**: Compartilhe presets via arquivos JSON
- **Favoritos**: Marque presets como favoritos para acesso rápido
- **Filtro**: Visualize apenas favoritos ou todos os presets
- **Persistência**: Presets são salvos automaticamente no dispositivo
- **Ordem dos efeitos**: Presets também salvam a ordem customizada dos efeitos

## 🔄 Ordem Customizável de Efeitos

- **Drag-and-drop**: Reordene efeitos arrastando e soltando
- **Ordem padrão**: Ganho → Distorção → Chorus → Flanger → Phaser → Delay → Reverb
- **Persistência**: A ordem é salva automaticamente
- **Tempo real**: Mudanças aplicadas instantaneamente


## 🎛️ **Efeitos Implementados**

### **Efeitos de Modulação**
- **Chorus**: Efeito de modulação que adiciona espessura e movimento ao som
  - **Depth**: Intensidade da modulação (0-100%)
  - **Rate**: Velocidade da modulação (0.1-10 Hz)
  - **Mix**: Controle dry/wet (0-100%)

- **Flanger**: Efeito de modulação com delay variável
  - **Depth**: Intensidade da modulação (0-100%)
  - **Rate**: Velocidade da modulação (0.1-10 Hz)
  - **Feedback**: Realimentação do sinal (0-100%)
  - **Mix**: Controle dry/wet (0-100%)

- **Phaser**: Efeito de modulação com filtros passa-tudo
  - **Depth**: Intensidade da modulação (0-100%)
  - **Rate**: Velocidade da modulação (0.1-10 Hz)
  - **Feedback**: Realimentação do sinal (0-100%)
  - **Mix**: Controle dry/wet (0-100%)

### **Efeitos de Filtragem**
- **Equalizador (EQ)**: Controle de frequências em 3 bandas
  - **Graves**: Ganho para frequências baixas (-12dB a +12dB)
  - **Médios**: Ganho para frequências médias (-12dB a +12dB)
  - **Agudos**: Ganho para frequências altas (-12dB a +12dB)
  - **Mix**: Controle dry/wet (0-100%)

### **Efeitos de Dinâmica**
- **Compressor**: Controle de dinâmica do sinal
  - **Threshold**: Ponto onde a compressão começa (-60dB a 0dB)
  - **Ratio**: Intensidade da compressão (1:1 a 20:1)
  - **Attack**: Velocidade de resposta (0.1ms a 100ms)
  - **Release**: Velocidade de recuperação (10ms a 1000ms)
  - **Mix**: Controle dry/wet (0-100%)

## 🎵 **Funcionalidades Principais**

### **Efeitos Básicos**
- **Ganho**: Controle de volume geral
- **Distorção**: 4 tipos (Soft Clip, Hard Clip, Fuzz, Overdrive) com mix dry/wet
- **Delay**: Efeito de eco com feedback e mix
- **Reverb**: Reverb com decay e mix
- **Chorus**: Modulação com depth, rate e mix
- **Flanger**: Modulação com delay variável, feedback e mix
- **Phaser**: Modulação com filtros passa-tudo, feedback e mix
- **Equalizador**: 3 bandas (graves, médios, agudos) com mix
- **Compressor**: Controle de dinâmica com threshold, ratio, attack, release e mix 

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
│       ├── MainActivity.java              # Interface principal e navegação
│       ├── AudioEngine.java               # Pipeline de áudio em tempo real
│       ├── EffectsFragment.java           # Interface de efeitos e presets
│       ├── EffectOrderAdapter.java        # Adapter para ordem de efeitos
│       ├── HomeFragment.java              # Tela inicial
│       ├── TunerFragment.java             # Afinador em tempo real
│       ├── MetronomeFragment.java         # Metrônomo
│       ├── LooperFragment.java            # Looper de gravação
│       ├── RecorderFragment.java          # Gravador
│       ├── LearningFragment.java          # Tela de aprendizado
│       ├── SettingsFragment.java          # Configurações
│       ├── AudioBackgroundService.java    # Serviço de áudio em background
│       ├── AudioStateManager.java         # Gerenciamento de estado dos efeitos
│       ├── PipelineManager.java           # Gerenciamento do pipeline de áudio
│       ├── PermissionManager.java         # Gerenciamento de permissões
│       ├── StateRecoveryManager.java      # Recuperação de estado
│       ├── PresetManager.java             # Gerenciamento de presets
│       ├── FavoritesManager.java          # Gerenciamento de favoritos
│       ├── TooltipManager.java            # Gerenciamento de tooltips
│       └── FavoritePresetAdapter.java     # Adapter para presets favoritos
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

## 🎹 MIDI Learn

ToneForge now supports **MIDI Learn** for real-time control of effect parameters using external MIDI controllers.

### How to use

1. Go to **Settings > MIDI Learn** and enable MIDI support.
2. Connect your MIDI controller (USB OTG or Bluetooth, if supported).
3. Long-press any effect parameter (slider/knob) to enter MIDI Learn mode.
4. Move a control on your MIDI device to map it to the selected parameter.
5. The mapping is saved and will persist for future sessions.

You can manage and clear mappings in the MIDI section of the settings.

> **Note:** Requires a compatible MIDI device.

## 🎛️ Sistema de Automação

O ToneForge agora inclui um **sistema completo de automação** que permite gravar e reproduzir mudanças de parâmetros ao longo do tempo, criando performances dinâmicas e expressivas.

### 🎬 Como Funciona

O sistema de automação registra todas as mudanças de parâmetros (via interface ou MIDI) durante a gravação e as reproduz automaticamente, criando movimentos suaves e precisos nos efeitos.

### 🎯 Fluxo de Automação

#### 1. **Preparação**
- Acesse a aba **"Efeitos"** no ToneForge
- Configure os parâmetros iniciais dos efeitos desejados
- Escolha um preset ou crie uma nova configuração

#### 2. **Gravação**
- Digite um nome para sua automação no campo **"Nome da Automação"**
- Clique no botão **"🎙️ Gravar"** para iniciar a gravação
- **Manipule os controles** em tempo real:
  - Mova os sliders dos efeitos
  - Ative/desative switches
  - Use controles MIDI (se configurado)
- Todas as mudanças são registradas com timestamp preciso
- Clique em **"⏹️ Parar"** para finalizar a gravação

#### 3. **Reprodução**
- Clique no botão **"▶️ Reproduzir"** para ativar a reprodução
- A automação aplica automaticamente os valores gravados
- Os controles da interface se movem em tempo real
- Clique em **"⏸️ Parar Reprodução"** para interromper

#### 4. **Persistência**
- As automações são salvas automaticamente com os presets
- Cada preset pode ter múltiplas automações
- As automações persistem entre sessões do app

### 🎨 Características Técnicas

#### **Estrutura de Dados**
- **Eventos de automação**: Timestamp, parâmetro, valor
- **Suporte a múltiplos parâmetros**: Todos os controles dos efeitos
- **Interpolação suave**: Transições fluidas entre valores
- **Alta precisão**: Atualização a ~60 FPS durante reprodução

#### **Integração Completa**
- **Interface**: Controles visuais para gravação/reprodução
- **MIDI**: Automação via controles MIDI externos
- **Presets**: Automações salvas junto com configurações
- **Tempo real**: Aplicação instantânea durante reprodução

#### **Controles Suportados**
- **SeekBars**: Todos os parâmetros numéricos (depth, rate, mix, etc.)
- **Switches**: Ativação/desativação de efeitos
- **Spinners**: Seleção de tipos (distorção, reverb, etc.)
- **MIDI**: Qualquer controle mapeado via MIDI Learn

### 🎵 Casos de Uso

#### **Performance Dinâmica**
- Grave mudanças de intensidade de distorção durante um solo
- Automatize variações de rate no chorus para criar movimento
- Crie crescendos com mudanças graduais de volume

#### **Transições Suaves**
- Automatize mudanças de preset durante uma música
- Crie transições de ambiente com reverb
- Varie o feedback do delay para efeitos dramáticos

#### **Expressão Musical**
- Use automação para simular pedal wah
- Crie variações de modulação em tempo real
- Automatize mudanças de EQ para diferentes seções

### 🔧 Configuração Avançada

#### **Sincronização**
- A automação funciona independentemente do tempo
- Futuras versões incluirão sincronização com metrônomo
- Suporte a múltiplas automações simultâneas

#### **Edição**
- Interface para visualizar e editar automações gravadas
- Possibilidade de ajustar timestamps e valores
- Exportação/importação de automações

#### **Integração com Áudio**
- Sincronização com looper e metrônomo
- Automação baseada em triggers de áudio
- Suporte a automação por envelope

### 💡 Dicas de Uso

1. **Planeje sua automação**: Teste os valores antes de gravar
2. **Use movimentos suaves**: Evite mudanças bruscas para melhor resultado
3. **Combine com MIDI**: Use controles físicos para maior expressão
4. **Experimente**: Teste diferentes combinações de parâmetros
5. **Salve variações**: Crie múltiplas automações para o mesmo preset

### 🚀 Próximas Funcionalidades

- [ ] **Editor visual**: Interface gráfica para editar automações
- [ ] **Sincronização com tempo**: Integração com metrônomo e BPM
- [ ] **Curvas de interpolação**: Diferentes tipos de transição
- [ ] **Automação por envelope**: Baseada no nível de áudio
- [ ] **Exportação**: Compartilhar automações entre usuários

> **Nota**: O sistema de automação está em desenvolvimento ativo. Novas funcionalidades serão adicionadas regularmente.

## 🚀 Como Usar

1. **Instalar**: Compile e instale o APK no dispositivo Android
2. **Permissões**: Conceda permissões de microfone, notificações e armazenamento
3. **Navegar**: Use a navegação inferior para acessar diferentes funcionalidades
4. **Efeitos**: Na aba "Efeitos", ajuste os parâmetros em tempo real
5. **Presets**: Salve suas configurações favoritas e marque como favoritos
6. **Exportar/Importar**: Compartilhe presets via arquivos JSON
7. **Ordem**: Reordene os efeitos arrastando e soltando
8. **Background**: Ative o processamento em background nas configurações
9. **Tooltips**: Toque longo nos controles para ver explicações
10. **Reset**: Use os botões de reset para restaurar valores padrão
11. **Testar**: Fale ou toque um instrumento no microfone
12. **MIDI Learn**: Ative o MIDI nas configurações e mapeie controles externos para parâmetros dos efeitos
13. **Automação**: Na aba "Efeitos", use os controles de automação para gravar e reproduzir mudanças de parâmetros

### 🎛️ Configurações Avançadas

- **Oversampling**: Ative nas configurações para melhor qualidade (mais CPU)
- **Áudio em Background**: Mantém o processamento ativo com tela desligada
- **Recuperação de Estado**: Configurações são restauradas automaticamente
- **Permissões**: Verifique se todas as permissões necessárias estão concedidas

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
- [x] Equalizador (EQ) de 3 bandas
- [x] Compressor com controles avançados
- [x] Sistema de tooltips informativos
- [x] Reset rápido de parâmetros
- [x] Exportar/importar presets
- [x] Sistema de favoritos
- [x] Visualização gráfica da cadeia de efeitos
- [x] Oversampling para melhor qualidade
- [x] Processamento em background com ForegroundService
- [x] Notificação persistente com controles rápidos
- [x] Gerenciamento robusto de permissões
- [x] Recuperação automática de estado
- [x] Sistema de pipeline com recuperação automática
- [x] Ajuste de latência: Permitir escolha entre menor latência ou maior estabilidade
- [x] MIDI Learn para controle externo
- [x] Sistema de automação: Gravar e reproduzir mudanças de parâmetros

### 🚧 Parcialmente Implementado
- [x] Metrônomo (UI + integração básica com C++)
- [x] Looper (UI + integração básica com C++)
- [x] Gravador (UI pronta, funcionalidade básica)

### 🔄 Em Desenvolvimento
- [ ] Melhorias no metrônomo (animações, visualização)
- [ ] Melhorias no looper (timer, visualização de duração)
- [ ] Funcionalidade completa do gravador

### 📋 Próximas Funcionalidades
- [ ] Persistência completa de automações (salvar/carregar)
- [ ] Interface de edição de automações
- [ ] Sincronização de automação com metrônomo
- [ ] Curvas de resposta customizáveis
- [ ] Melhorias de interface (animações, transições)
- [ ] Suporte a diferentes taxas de amostragem
- [ ] Integração com DAWs externos

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

## Ajuste de Latência

O ToneForge agora permite ao usuário escolher entre diferentes modos de latência, adaptando o processamento de áudio para melhor desempenho ou maior estabilidade, conforme a necessidade:

- **Baixa Latência:** Prioriza resposta rápida, ideal para tocar ao vivo.
- **Equilibrado:** Compromisso entre latência e estabilidade, recomendado para uso geral.
- **Estabilidade:** Prioriza qualidade e robustez, ideal para gravação.

A seleção pode ser feita nas configurações do app, com informações detalhadas, estimativa de latência e recomendações exibidas na interface.

O modo selecionado ajusta automaticamente o tamanho do buffer, taxa de amostragem e oversampling, otimizando o áudio para o perfil escolhido. A escolha é salva e restaurada automaticamente ao abrir o app, inclusive após uso em background.

> Observação: O modo de baixa latência depende do suporte do dispositivo Android. Em aparelhos antigos, pode não estar disponível ou apresentar limitações.

---

## Roadmap (trecho relevante)

- [x] Ajuste de latência: Permitir escolha entre menor latência ou maior estabilidade. 