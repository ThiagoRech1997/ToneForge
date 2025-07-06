# 📋 Checklist de Validação Manual - ToneForge

## 🎯 **Objetivo**
Validar manualmente todas as funcionalidades após a refatoração MVP para garantir que nada foi quebrado.

## 📱 **Pré-requisitos**
- [ ] APK instalado no dispositivo/emulador
- [ ] Dispositivo com microfone funcionando
- [ ] Alto-falantes ou fones de ouvido
- [ ] Conexão com internet (para alguns recursos)

---

## 🏠 **1. HomeFragment**

### **Navegação**
- [ ] Botão "Efeitos" navega para EffectsFragment
- [ ] Botão "Looper" navega para LooperFragment
- [ ] Botão "Afinador" navega para TunerFragment
- [ ] Botão "Metrônomo" navega para MetronomeFragment
- [ ] Botão "Aprendizado" navega para LearningFragment
- [ ] Botão "Gravador" navega para RecorderFragment
- [ ] Botão "Configurações" navega para SettingsFragment
- [ ] Botão "Home" retorna ao HomeFragment

### **Status do Sistema**
- [ ] Status Wi-Fi é exibido corretamente
- [ ] Status da bateria é exibido corretamente
- [ ] Status do áudio é atualizado em tempo real
- [ ] Título do header é "ToneForge"

### **Diálogos**
- [ ] Clique no Wi-Fi abre diálogo de status
- [ ] Clique no Volume abre diálogo de controle
- [ ] Clique no Power abre diálogo de opções

---

## 🎛️ **2. EffectsFragment**

### **Controles de Efeitos**
- [ ] Switch de Ganho ativa/desativa o efeito
- [ ] SeekBar de Ganho ajusta o valor (0-100%)
- [ ] Switch de Distorção ativa/desativa o efeito
- [ ] SeekBar de Distorção ajusta o valor (0-100%)
- [ ] SeekBar de Mix da Distorção ajusta o valor (0-100%)
- [ ] Spinner de Tipo de Distorção funciona
- [ ] Switch de Delay ativa/desativa o efeito
- [ ] SeekBar de Tempo de Delay ajusta o valor
- [ ] SeekBar de Feedback do Delay ajusta o valor
- [ ] SeekBar de Mix do Delay ajusta o valor
- [ ] Switch de Sincronização BPM do Delay funciona
- [ ] SeekBar de BPM do Delay ajusta o valor

### **Efeitos Avançados**
- [ ] Switch de Reverb ativa/desativa o efeito
- [ ] SeekBar de Reverb ajusta o valor
- [ ] SeekBar de Room Size do Reverb ajusta o valor
- [ ] SeekBar de Damping do Reverb ajusta o valor
- [ ] SeekBar de Mix do Reverb ajusta o valor
- [ ] Spinner de Tipo de Reverb funciona
- [ ] Switch de Chorus ativa/desativa o efeito
- [ ] SeekBar de Depth do Chorus ajusta o valor
- [ ] SeekBar de Rate do Chorus ajusta o valor
- [ ] SeekBar de Feedback do Chorus ajusta o valor
- [ ] SeekBar de Mix do Chorus ajusta o valor

### **Efeitos Moduladores**
- [ ] Switch de Flanger ativa/desativa o efeito
- [ ] SeekBar de Depth do Flanger ajusta o valor
- [ ] SeekBar de Rate do Flanger ajusta o valor
- [ ] SeekBar de Feedback do Flanger ajusta o valor
- [ ] SeekBar de Mix do Flanger ajusta o valor
- [ ] Switch de Phaser ativa/desativa o efeito
- [ ] SeekBar de Depth do Phaser ajusta o valor
- [ ] SeekBar de Rate do Phaser ajusta o valor
- [ ] SeekBar de Feedback do Phaser ajusta o valor
- [ ] SeekBar de Mix do Phaser ajusta o valor

### **Efeitos de Processamento**
- [ ] Switch de EQ ativa/desativa o efeito
- [ ] SeekBar de EQ Low ajusta o valor
- [ ] SeekBar de EQ Mid ajusta o valor
- [ ] SeekBar de EQ High ajusta o valor
- [ ] SeekBar de Mix do EQ ajusta o valor
- [ ] Switch de Compressor ativa/desativa o efeito
- [ ] SeekBar de Threshold do Compressor ajusta o valor
- [ ] SeekBar de Ratio do Compressor ajusta o valor
- [ ] SeekBar de Attack do Compressor ajusta o valor
- [ ] SeekBar de Release do Compressor ajusta o valor
- [ ] SeekBar de Mix do Compressor ajusta o valor

### **Sistema de Presets**
- [ ] Spinner de Presets exibe lista de presets
- [ ] Botão "Salvar Preset" abre diálogo
- [ ] Botão "Deletar Preset" abre confirmação
- [ ] Botão "Favoritos" funciona
- [ ] Botão "Exportar Preset" funciona
- [ ] Botão "Importar Preset" funciona
- [ ] Preset é salvo corretamente
- [ ] Preset é carregado corretamente
- [ ] Preset é deletado corretamente

### **Sistema de Automação**
- [ ] Campo de nome da automação funciona
- [ ] Botão "Iniciar Gravação" inicia gravação
- [ ] Botão "Parar Gravação" para gravação
- [ ] Botão "Iniciar Reprodução" inicia reprodução
- [ ] Botão "Parar Reprodução" para reprodução
- [ ] Barra de progresso da automação funciona
- [ ] Texto de progresso da automação funciona
- [ ] Spinner de automações exibe lista
- [ ] Botão "Exportar Automação" funciona
- [ ] Botão "Importar Automação" funciona
- [ ] Botão "Deletar Automação" funciona

### **Ordem dos Efeitos**
- [ ] RecyclerView da ordem dos efeitos funciona
- [ ] Drag and drop para reordenar efeitos funciona
- [ ] Swipe para deletar efeitos funciona
- [ ] Ordem dos efeitos é salva

### **MIDI**
- [ ] Switch de MIDI ativa/desativa MIDI
- [ ] Status do MIDI é exibido corretamente
- [ ] MIDI Learn funciona para parâmetros

### **Reset**
- [ ] Botão "Reset All" reseta todos os efeitos

---

## 🔄 **3. LooperFragment**

### **Controles Principais**
- [ ] Botão de Gravação inicia/para gravação
- [ ] Botão de Reprodução inicia/para reprodução
- [ ] Botão de Limpar limpa o looper
- [ ] Botão de Undo desfaz última ação
- [ ] Switch de Sincronização funciona
- [ ] Campo de BPM aceita entrada
- [ ] Botão de Tap Tempo funciona

### **Status e Timer**
- [ ] Status do looper é exibido corretamente
- [ ] Timer mostra tempo atual
- [ ] Contador de batidas funciona
- [ ] Barra de progresso funciona

### **Controles de Arquivo**
- [ ] Botão "Salvar" salva o loop
- [ ] Botão "Carregar" carrega loop
- [ ] Botão "Biblioteca" abre biblioteca
- [ ] Botão "Exportar" exporta loop

### **Waveform**
- [ ] Visualização do waveform é exibida
- [ ] Switch de grade ativa/desativa grade
- [ ] Switch de grade de tempo ativa/desativa
- [ ] Switch de playhead ativa/desativa
- [ ] Clique no waveform navega para posição

### **Tracks**
- [ ] Lista de tracks é exibida
- [ ] Volume de cada track é ajustável
- [ ] Mute de cada track funciona
- [ ] Solo de cada track funciona
- [ ] Deletar track funciona

### **Efeitos de Loop**
- [ ] Switch de Speed ajusta velocidade
- [ ] Switch de Pitch ajusta pitch
- [ ] Switch de Reverse inverte loop
- [ ] Switch de Stutter aplica stutter
- [ ] Switch de Slicing ativa slicing
- [ ] Switch de Randomização randomiza slices

### **Filtros**
- [ ] Switch de Passa-baixa ativa filtro
- [ ] Switch de Passa-alta ativa filtro

### **Fade**
- [ ] Switch de Fade In aplica fade in
- [ ] Switch de Fade Out aplica fade out

### **Marcadores**
- [ ] Marcadores são criados corretamente
- [ ] Navegação entre marcadores funciona

---

## 🎵 **4. TunerFragment**

### **Detecção de Frequência**
- [ ] Frequência é detectada em tempo real
- [ ] Nota musical é exibida corretamente
- [ ] Oitava é exibida corretamente
- [ ] Desvio em cents é calculado
- [ ] Precisão da afinação é exibida

### **Controles de Calibração**
- [ ] SeekBar de calibração ajusta offset
- [ ] Texto de calibração é atualizado
- [ ] Botão de reset de calibração funciona

### **Controles de Sensibilidade**
- [ ] SeekBar de sensibilidade ajusta sensibilidade
- [ ] Texto de sensibilidade é atualizado
- [ ] SeekBar de threshold ajusta threshold
- [ ] Texto de threshold é atualizado

### **Temperamento**
- [ ] Spinner de temperamento funciona
- [ ] Botão de temperamento funciona

### **Notas de Referência**
- [ ] Botão E (Mi) define referência
- [ ] Botão A (Lá) define referência
- [ ] Botão D (Ré) define referência
- [ ] Botão G (Sol) define referência
- [ ] Botão B (Si) define referência
- [ ] Botão E2 (Mi agudo) define referência

### **Indicadores Visuais**
- [ ] Barra de progresso de afinação funciona
- [ ] Texto de precisão é atualizado
- [ ] Texto de nível de entrada é atualizado
- [ ] Cores dos indicadores mudam conforme precisão

### **Controles Gerais**
- [ ] Botão de toggle ativa/desativa afinador
- [ ] Status do afinador é exibido
- [ ] Botão de configurações funciona
- [ ] Botão de ajuda funciona

---

## ⏱️ **5. MetronomeFragment**

### **Controles de BPM**
- [ ] Botão "-" diminui BPM
- [ ] Botão "+" aumenta BPM
- [ ] Display de BPM mostra valor correto
- [ ] Presets de BPM funcionam (60, 80, 100, 120)

### **Controles de Compasso**
- [ ] Botão "-" diminui compasso
- [ ] Botão "+" aumenta compasso
- [ ] Display de compasso mostra valor correto
- [ ] Formato X/4 é exibido corretamente

### **Controles de Volume**
- [ ] SeekBar de volume ajusta volume (0-100%)
- [ ] Display de volume mostra percentual correto

### **Controle de Reprodução**
- [ ] Botão Play/Stop inicia/para metrônomo
- [ ] Ícone do botão muda conforme estado
- [ ] Metrônomo toca no BPM correto

### **Animação**
- [ ] Animação de batida é sincronizada com BPM
- [ ] Animação de pulso funciona
- [ ] Animação para quando metrônomo para

### **Validação**
- [ ] BPM não pode ser menor que 40
- [ ] BPM não pode ser maior que 200
- [ ] Compasso não pode ser menor que 1
- [ ] Compasso não pode ser maior que 16

---

## 🎙️ **6. RecorderFragment**

### **Controle de Gravação**
- [ ] Botão de gravação inicia/para gravação
- [ ] Estado do botão muda conforme gravação
- [ ] Cores do botão mudam conforme estado

### **Controle de Reprodução**
- [ ] Botão de reprodução inicia/para reprodução
- [ ] Estado do botão muda conforme reprodução
- [ ] Cores do botão mudam conforme estado

### **Timer**
- [ ] Timer mostra tempo de gravação em tempo real
- [ ] Timer mostra tempo de reprodução em tempo real
- [ ] Timer atualiza a cada 100ms

### **Lista de Gravações**
- [ ] Lista de gravações recentes é exibida
- [ ] Placeholder é mostrado quando não há gravações
- [ ] Gravações são listadas corretamente

### **Tratamento de Erros**
- [ ] Erros são capturados e exibidos
- [ ] Mensagens de erro são claras

### **Cleanup**
- [ ] Recursos são limpos adequadamente
- [ ] Não há memory leaks

---

## ⚙️ **7. SettingsFragment**

### **Configurações Gerais**
- [ ] Switch de tema escuro funciona
- [ ] Switch de vibração funciona
- [ ] Switch de auto-save funciona
- [ ] Switch de áudio em background funciona

### **Configurações de Latência**
- [ ] Radio button de baixa latência funciona
- [ ] Radio button de equilibrado funciona
- [ ] Radio button de estabilidade funciona
- [ ] Informações de latência são exibidas
- [ ] Detalhes de latência são exibidos
- [ ] Botão de informações de latência funciona

### **Configurações MIDI**
- [ ] Switch de MIDI funciona
- [ ] Status MIDI é exibido
- [ ] Informações do dispositivo MIDI são exibidas
- [ ] Botão de scan de dispositivos MIDI funciona
- [ ] Botão de mapeamentos MIDI funciona

### **Persistência**
- [ ] Configurações são salvas no SharedPreferences
- [ ] Configurações são carregadas ao abrir
- [ ] Mudanças são aplicadas imediatamente

### **Diálogos**
- [ ] Diálogo de informações de latência funciona
- [ ] Diálogo de mapeamentos MIDI funciona
- [ ] Diálogo de confirmações funciona
- [ ] Diálogo "Sobre o app" funciona

### **Integração**
- [ ] AudioBackgroundService é afetado pelas configurações
- [ ] LatencyManager é afetado pelas configurações
- [ ] ToneForgeMidiManager é afetado pelas configurações

---

## 📚 **8. LoopLibraryFragment**

### **Carregamento da Biblioteca**
- [ ] Biblioteca de loops é carregada assincronamente
- [ ] Estado vazio é exibido quando não há loops
- [ ] Estado com conteúdo é exibido quando há loops

### **Lista de Loops**
- [ ] RecyclerView exibe loops corretamente
- [ ] Informações de cada loop são exibidas
- [ ] Ações por item funcionam

### **Operações CRUD**
- [ ] Carregar loop funciona
- [ ] Compartilhar loop funciona
- [ ] Renomear loop funciona
- [ ] Deletar loop funciona

### **Interface Interativa**
- [ ] Clique em item abre opções
- [ ] Swipe em item abre ações
- [ ] Botões de ação funcionam

### **Diálogos**
- [ ] Diálogo de opções funciona
- [ ] Diálogo de confirmação funciona
- [ ] Diálogo de renomeação funciona

### **Integração**
- [ ] LoopLibraryManager é usado corretamente
- [ ] LoopLoadUtil é usado corretamente
- [ ] LoopShareUtil é usado corretamente

### **Navegação**
- [ ] Retorno automático após carregar loop
- [ ] Navegação de volta funciona

---

## 🔧 **9. Validação Geral**

### **Navegação**
- [ ] Navegação entre todos os fragments funciona
- [ ] Títulos dos headers são atualizados corretamente
- [ ] Botão voltar funciona em todos os fragments
- [ ] Navegação não causa crashes

### **Performance**
- [ ] Fragments carregam em menos de 500ms
- [ ] Não há lag na interface
- [ ] Uso de memória é razoável (< 100MB)
- [ ] Não há memory leaks

### **Estabilidade**
- [ ] App não crasha durante uso normal
- [ ] Rotação de tela funciona
- [ ] Mudança de configuração funciona
- [ ] App funciona em background

### **Integração de Áudio**
- [ ] Pipeline de áudio funciona
- [ ] Efeitos são aplicados corretamente
- [ ] Latência é aceitável (< 50ms)
- [ ] Não há clipping ou distorção

### **Compatibilidade**
- [ ] Funciona em diferentes tamanhos de tela
- [ ] Funciona em diferentes densidades
- [ ] Funciona em diferentes versões do Android
- [ ] Funciona em modo paisagem e retrato

---

## 📊 **10. Relatório Final**

### **Funcionalidades Testadas**
- [ ] **HomeFragment**: ___/___ testes passaram
- [ ] **EffectsFragment**: ___/___ testes passaram
- [ ] **LooperFragment**: ___/___ testes passaram
- [ ] **TunerFragment**: ___/___ testes passaram
- [ ] **MetronomeFragment**: ___/___ testes passaram
- [ ] **RecorderFragment**: ___/___ testes passaram
- [ ] **SettingsFragment**: ___/___ testes passaram
- [ ] **LoopLibraryFragment**: ___/___ testes passaram

### **Problemas Encontrados**
- [ ] Problema 1: ________________
- [ ] Problema 2: ________________
- [ ] Problema 3: ________________

### **Melhorias Identificadas**
- [ ] Melhoria 1: ________________
- [ ] Melhoria 2: ________________
- [ ] Melhoria 3: ________________

### **Conclusão**
- [ ] ✅ Todas as funcionalidades estão operacionais
- [ ] ✅ Refatoração MVP foi bem-sucedida
- [ ] ✅ Não há regressões funcionais
- [ ] ✅ App está pronto para produção

---

## 🎯 **Instruções de Uso**

1. **Instale o APK**: `adb install app/build/outputs/apk/debug/app-debug.apk`
2. **Execute o script de validação**: `./scripts/functional-validation.sh`
3. **Use este checklist** para validação manual
4. **Marque cada item** conforme testa
5. **Documente problemas** encontrados
6. **Gere relatório final** com métricas

**Tempo estimado para validação completa**: 2-3 horas 