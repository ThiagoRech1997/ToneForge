# ToneForge Claude Configuration

Este diretório contém configurações especializadas para o Claude Code trabalhar de forma eficiente no projeto ToneForge.

## Estrutura

```
.claude/
├── README.md                  # Este arquivo
├── EXAMPLES.md               # Exemplos práticos de uso
├── QUICK-REFERENCE.md        # Referência rápida
├── skills-index.md           # Índice visual de skills e agents
├── agents/                   # Agents especializados
│   ├── android-architecture-reviewer.md
│   ├── android-qa-engineer.md
│   ├── android-ui-designer.md
│   ├── audio-dsp-engineer.md
│   ├── toneforge-advanced-features.md
│   └── toneforge-utility-developer.md
└── skills/                   # Skills (workflows)
    ├── README.md
    ├── USAGE.md
    ├── audio-test/
    │   └── SKILL.md
    ├── cpp-effect/
    │   └── SKILL.md
    ├── debug-native/
    │   └── SKILL.md
    ├── mvp-scaffold/
    │   └── SKILL.md
    ├── refactor-legacy/
    │   └── SKILL.md
    ├── release-prep/
    │   └── SKILL.md
    └── security-audit/
        └── SKILL.md
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

- **[skills-index.md](skills-index.md)** - Índice visual de todos os recursos
- **[EXAMPLES.md](EXAMPLES.md)** - Exemplos práticos detalhados
- **[skills/README.md](skills/README.md)** - Documentação de skills
- **[skills/USAGE.md](skills/USAGE.md)** - Guia de uso em português

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

## Contribuindo

Para melhorar esta configuração:
1. Identifique padrões repetitivos no trabalho
2. Crie skills para workflows comuns
3. Documente com exemplos práticos
4. Teste com casos reais
5. Itere baseado em feedback

---

**Versão:** 1.0.0
**Última atualização:** 2025-11-07
**Projeto:** ToneForge Android Multi-Effects
