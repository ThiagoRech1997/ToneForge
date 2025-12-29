# ToneForge Claude Configuration

Este diretório contém configurações especializadas para o Claude Code trabalhar de forma eficiente no projeto ToneForge.

## Estrutura

```
.claude/
├── README.md                  # Este arquivo
├── EXAMPLES.md               # Exemplos práticos de uso
├── QUICK-REFERENCE.md        # Referência rápida
├── TROUBLESHOOTING.md        # Guia de resolução de problemas
├── skills-index.md           # Índice visual de skills e agents
├── claude_settings.json      # Configurações e hooks
├── git-commit-template.txt   # Template para mensagens de commit
├── agents/                   # Agents especializados
│   ├── android-architecture-reviewer.md
│   ├── android-qa-engineer.md
│   ├── android-ui-designer.md
│   ├── audio-dsp-engineer.md
│   ├── toneforge-advanced-features.md
│   └── toneforge-utility-developer.md
├── skills/                   # Skills (workflows)
│   ├── README.md
│   ├── USAGE.md
│   ├── audio-test/
│   │   └── SKILL.md
│   ├── code-review/          # ✨ NEW
│   │   └── SKILL.md
│   ├── cpp-effect/
│   │   └── SKILL.md
│   ├── debug-native/
│   │   └── SKILL.md
│   ├── mvp-scaffold/
│   │   └── SKILL.md
│   ├── refactor-legacy/
│   │   └── SKILL.md
│   ├── release-prep/
│   │   └── SKILL.md
│   └── security-audit/
│       └── SKILL.md
├── commands/                 # ✨ NEW - Slash commands
│   ├── validate.md           # Validação completa
│   ├── test-coverage.md      # Análise de cobertura
│   ├── debug-audio.md        # Debug de audio pipeline
│   └── review-pr.md          # Review de pull request
├── checklists/               # ✨ NEW - Checklists
│   ├── pr-review.md          # Checklist de code review
│   ├── new-feature.md        # Checklist de nova feature
│   └── security-audit.md     # Checklist de segurança
├── templates/                # ✨ NEW - Templates de código
│   ├── README.md
│   ├── mvp-contract-template.kt
│   ├── presenter-template.kt
│   ├── fragment-refactored-template.kt
│   ├── test-presenter-template.kt
│   └── layout-fragment-template.xml
└── adr/                      # ✨ NEW - Architecture Decision Records
    ├── README.md
    ├── template.md
    ├── 001-mvp-pattern-adoption.md
    ├── 002-clean-architecture-layers.md
    └── 003-jni-security-practices.md
```

## Conceitos

### Agents
**Especialistas autônomos** que executam tarefas específicas:
- Têm conhecimento profundo de uma área
- Executam implementações complexas
- Tomam decisões técnicas
- Trabalham de forma independente

### Skills
**Workflows estruturados** para processos repetíveis:
- Fornecem guia passo a passo
- Garantem consistência
- Documentam best practices
- Podem usar múltiplos agents

## Quick Start

### Ver todas as skills disponíveis
```
Consulte skills/README.md ou skills-index.md
```

### Usar uma skill
```
Add a tremolo effect
```
Claude automaticamente usará a skill `cpp-effect`

### Solicitar um agent específico
```
Use the audio-dsp-engineer agent to optimize the reverb algorithm
```

### Combinar skills e agents
```
Add a flanger effect and prepare for release
```
Claude usará `cpp-effect` + `release-prep` com os agents apropriados

## Documentação

### Referências Principais
- **[skills-index.md](skills-index.md)** - Índice visual de todos os recursos
- **[EXAMPLES.md](EXAMPLES.md)** - Exemplos práticos detalhados
- **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** - Guia completo de resolução de problemas
- **[skills/README.md](skills/README.md)** - Documentação de skills
- **[skills/USAGE.md](skills/USAGE.md)** - Guia de uso em português

### Recursos de Desenvolvimento
- **[commands/](commands/)** - Slash commands para tarefas comuns
- **[checklists/](checklists/)** - Checklists de qualidade e segurança
- **[templates/](templates/)** - Templates de código MVP
- **[adr/](adr/)** - Decisões arquiteturais documentadas

## Manutenção

### Adicionar nova skill
1. Crie diretório em `skills/nova-skill/`
2. Crie arquivo `skills/nova-skill/SKILL.md`
3. Use o template de skills/README.md
4. Adicione ao índice em skills-index.md
5. Adicione exemplos em EXAMPLES.md

### Adicionar novo agent
1. Crie arquivo em `agents/novo-agent.md`
2. Defina especialização e ferramentas
3. Adicione ao índice em skills-index.md
4. Documente casos de uso

### Atualizar documentação
1. Mantenha EXAMPLES.md atualizado
2. Revise skills quando arquitetura mudar
3. Atualize agents se ferramentas mudarem

## Padrão Anthropic

Esta estrutura segue as recomendações da Anthropic para:
- ✅ Skills como workflows declarativos em `.claude/skills/[skill-name]/SKILL.md`
- ✅ Agents como especialistas autônomos
- ✅ Separação clara de responsabilidades
- ✅ Documentação rica com exemplos
- ✅ Nomenclatura clara e descritiva

## Recursos Externos

- [Claude Code Docs](https://docs.claude.com/claude-code)
- [Anthropic Skills Guide](https://docs.anthropic.com/claude/docs/skills)
- [ToneForge CLAUDE.md](../CLAUDE.md)

## Novos Recursos (v2.0)

### 🎯 Slash Commands
Comandos rápidos para tarefas comuns:

- `/validate` - Validação completa (testes, lint, build)
- `/test-coverage` - Análise detalhada de cobertura de testes
- `/debug-audio` - Debug de problemas no audio pipeline
- `/review-pr` - Review abrangente de pull requests

### ✅ Checklists
Checklists completas para garantir qualidade:

- `checklists/pr-review.md` - Review de código (arquitetura, segurança, qualidade)
- `checklists/new-feature.md` - Implementação de novas features
- `checklists/security-audit.md` - Auditoria de segurança completa

### 📝 Templates de Código
Templates prontos para desenvolvimento MVP:

- `mvp-contract-template.kt` - Definição de contratos
- `presenter-template.kt` - Presenters com coroutines
- `fragment-refactored-template.kt` - Fragments MVP
- `test-presenter-template.kt` - Testes unitários
- `layout-fragment-template.xml` - Layouts Material Design

### 📚 Architecture Decision Records (ADRs)
Decisões arquiteturais documentadas:

- **ADR-001**: Adoção do padrão MVP
- **ADR-002**: Estrutura de camadas Clean Architecture
- **ADR-003**: Práticas de segurança JNI

### 🔧 Configurações Avançadas
- `claude_settings.json` - Hooks, prioridades de contexto, configurações
- `git-commit-template.txt` - Template padronizado para commits
- `TROUBLESHOOTING.md` - Guia completo de resolução de problemas

## Troubleshooting

### Skill não é reconhecida
- Verifique se o diretório existe em `skills/skill-name/`
- Confirme que existe arquivo `SKILL.md` no diretório
- Use nome exato (lowercase-with-hyphens)

### Agent não é acionado
- Seja mais específico na solicitação
- Mencione explicitamente: "Use the X agent"
- Verifique se o contexto está claro

### Resultado não esperado
- Leia EXAMPLES.md para ver uso correto
- Seja mais específico nos requisitos
- Peça para Claude explicar o plano primeiro

### Problemas de desenvolvimento
- Consulte [TROUBLESHOOTING.md](TROUBLESHOOTING.md) para guia completo
- Use `/debug-audio` para problemas de áudio
- Use `/validate` para verificar projeto completo

## Contribuindo

Para melhorar esta configuração:
1. Identifique padrões repetitivos no trabalho
2. Crie skills para workflows comuns
3. Documente com exemplos práticos
4. Teste com casos reais
5. Itere baseado em feedback

## Como Usar os Novos Recursos

### Slash Commands
Simplesmente digite o comando no chat:
```
/validate
/test-coverage
/debug-audio
/review-pr
```

### Checklists
Referenciadas automaticamente durante code reviews ou use manualmente:
```
Review this code using the PR review checklist
```

### Templates
Copie templates para criar novos componentes MVP:
```
Create a new Profile feature using MVP templates
```

### ADRs
Consulte quando tiver dúvidas sobre decisões arquiteturais:
```
Why did we choose MVP over MVVM?
```

### Troubleshooting
Quando encontrar problemas:
```
Audio pipeline is not starting, help me debug
The build is failing with CMake errors
```

## Boas Práticas

### Antes de Commitar
```
/validate
```

### Antes de Pull Request
```
/review-pr
/test-coverage
```

### Ao Criar Nova Feature
1. Consulte checklist: `checklists/new-feature.md`
2. Use templates: `templates/`
3. Siga ADRs existentes
4. Execute `/validate` antes do commit

### Ao Encontrar Problemas
1. Consulte `TROUBLESHOOTING.md`
2. Use comandos de debug (`/debug-audio`)
3. Verifique ADRs relacionados
4. Revise checklists relevantes

---

**Versão:** 2.0.0
**Última atualização:** 2025-11-07
**Projeto:** ToneForge Android Multi-Effects

**Novidades v2.0:**
- ✨ 4 Slash commands para tarefas comuns
- ✅ 3 Checklists completas
- 📝 5 Templates de código MVP
- 📚 3 ADRs documentando decisões arquiteturais
- 🔧 Configurações avançadas com hooks
- 📖 Guia de troubleshooting expandido
- 🎯 Skill de code review
