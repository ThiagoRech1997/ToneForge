# Como Usar as Skills do ToneForge

## Visão Geral

Skills são workflows especializados que o Claude Code pode usar para executar tarefas complexas de forma consistente e completa. Elas complementam os agents fornecendo processos estruturados passo a passo.

## Skills Disponíveis

### 🎵 Audio & DSP

#### audio-test
**Uso:** Quando precisar validar o processamento de áudio

**Exemplos:**
- "Run audio tests before release"
- "Validate the audio pipeline"
- "Check if the new effect is working correctly"

**O que faz:**
- Executa testes nativos C++
- Valida interface JNI
- Verifica pipeline de áudio
- Gera relatório de cobertura
- Mede latência e uso de CPU

---

#### cpp-effect
**Uso:** Para adicionar um novo efeito de áudio

**Exemplos:**
- "Add a tremolo effect"
- "Implement a wah-wah pedal"
- "Create a ring modulator effect"

**O que faz:**
- Implementa algoritmo DSP em C++
- Cria interface JNI
- Integra com AudioEngine.kt
- Adiciona UI controls
- Atualiza preset system
- Cria testes

---

#### debug-native
**Uso:** Para debugar problemas no código nativo

**Exemplos:**
- "The app crashes when I enable reverb"
- "Debug this native segfault"
- "Find memory leak in audio processing"

**O que faz:**
- Analisa stack traces
- Usa ferramentas NDK
- Identifica memory leaks
- Detecta buffer overflows
- Sugere correções

---

### 🏗️ Arquitetura & Desenvolvimento

#### mvp-scaffold
**Uso:** Para criar novos fragments seguindo MVP

**Exemplos:**
- "Create a new Equalizer fragment"
- "Scaffold a Preset Browser screen"
- "I need a new MIDI Settings fragment"

**O que faz:**
- Cria Contract interface
- Implementa Presenter
- Cria Fragment refactored
- Gera layout XML
- Adiciona navegação
- Cria unit tests

---

#### refactor-legacy
**Uso:** Para modernizar fragments antigos

**Exemplos:**
- "Refactor the old SettingsFragment to MVP"
- "Modernize the legacy TunerFragment"
- "Convert MetronomeFragment to the new architecture"

**O que faz:**
- Analisa código legado
- Extrai lógica de negócio
- Cria camadas Domain/Infrastructure
- Implementa MVP pattern
- Migra funcionalidades
- Mantém feature parity

---

### 🔒 Segurança & Qualidade

#### security-audit
**Uso:** Antes de releases ou após mudanças críticas

**Exemplos:**
- "Perform a security audit before release"
- "Check for security issues in file handling"
- "Review JNI security"

**O que faz:**
- Audita código JNI
- Verifica file system security
- Valida input validation
- Checa permissions
- Revisa dependências
- Gera relatório OWASP

---

#### release-prep
**Uso:** Antes de criar uma release

**Exemplos:**
- "Prepare for version 2.0.0 release"
- "Get ready for beta release"
- "Pre-release checklist for hotfix"

**O que faz:**
- Atualiza versões
- Executa todos os testes
- Roda quality checks
- Verifica segurança
- Gera builds
- Cria release notes
- Prepara Git tags

---

## Workflows Comuns

### Adicionar Nova Feature

1. **Planejamento**
   ```
   I want to add a new 10-band equalizer feature
   ```
   Claude usará `mvp-scaffold` para criar estrutura

2. **Implementação de Efeito (se necessário)**
   ```
   Add the equalizer DSP processing
   ```
   Claude usará `cpp-effect` para código nativo

3. **Testes**
   ```
   Test the equalizer implementation
   ```
   Claude usará `audio-test`

### Refatorar Código Legado

1. **Análise**
   ```
   Analyze the legacy PresetFragment
   ```

2. **Refatoração**
   ```
   Refactor PresetFragment to MVP
   ```
   Claude usará `refactor-legacy`

3. **Validação**
   ```
   Verify the refactored fragment works correctly
   ```

### Preparar Release

1. **Testes**
   ```
   Run comprehensive tests
   ```
   Claude usará `audio-test`

2. **Segurança**
   ```
   Security audit for release
   ```
   Claude usará `security-audit`

3. **Release**
   ```
   Prepare release 2.1.0
   ```
   Claude usará `release-prep`

## Combinando Skills e Agents

Skills fornecem o processo, agents executam o trabalho:

### Exemplo: Adicionar Tremolo Effect

**Skill:** `cpp-effect` (fornece o processo)

**Agents envolvidos:**
- `audio-dsp-engineer` - Implementa DSP
- `android-ui-designer` - Cria UI
- `android-qa-engineer` - Cria testes

**Workflow:**
```
User: Add a tremolo effect to ToneForge

Claude: I'll use the cpp-effect skill to guide this implementation.
        Let me launch the audio-dsp-engineer agent to implement
        the native DSP code...

        [Implementa C++]

        Now I'll use the android-ui-designer agent to create
        the UI controls...

        [Cria UI]

        Finally, I'll use the android-qa-engineer to create tests...

        [Cria testes]
```

## Dicas de Uso

### Seja Específico
❌ "Add an effect"
✅ "Add a tremolo effect with rate and depth controls"

### Use Contexto
❌ "Test it"
✅ "Test the audio pipeline after adding tremolo"

### Combine Quando Apropriado
```
Add a flanger effect and prepare for release
```
Claude usará `cpp-effect` + `release-prep`

### Peça Explicações
```
Explain how to use the mvp-scaffold skill
```

## Criando Suas Próprias Skills

### Template Básico

```markdown
# Nome da Skill

## Description
O que a skill faz em 1-2 frases

## When to Use
- Cenário 1
- Cenário 2
- Cenário 3

## Instructions

1. **Passo 1**
   - Detalhe
   - Detalhe

2. **Passo 2**
   - Detalhe
   - Detalhe

[etc...]

## Expected Deliverables
- Item 1
- Item 2
- Item 3
```

### Boas Práticas

1. **Seja Claro**: Instruções devem ser inequívocas
2. **Seja Completo**: Cubra todo o processo
3. **Seja Específico**: Use exemplos do projeto
4. **Seja Prático**: Inclua comandos e código
5. **Seja Útil**: Adicione checklists e templates

## Troubleshooting

### Skill Não Encontrada
```
Available skills: audio-test, cpp-effect, debug-native,
mvp-scaffold, refactor-legacy, release-prep, security-audit
```

### Skill Não Funciona Como Esperado
1. Verifique se o contexto está claro
2. Seja mais específico na solicitação
3. Forneça informações adicionais
4. Peça para Claude explicar o plano primeiro

### Skill Está Desatualizada
Atualize o arquivo `.claude/skills/[nome].md` com novos passos ou informações

## Recursos

- **Skills**: `.claude/skills/*.md`
- **Agents**: `.claude/agents/*.md`
- **Docs**: `CLAUDE.md`
- **Scripts**: `./scripts/`

## Feedback

Se uma skill não está funcionando bem ou você tem ideias para novas skills, atualize os arquivos em `.claude/skills/` ou adicione ao backlog do projeto.
