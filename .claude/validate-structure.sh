#!/bin/bash
# Valida a estrutura de skills e agents do ToneForge

echo "🔍 Validando estrutura .claude/"
echo ""

# Cores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

errors=0
warnings=0

# Verificar documentação principal
echo "📄 Verificando documentação principal..."
docs=("README.md" "EXAMPLES.md" "QUICK-REFERENCE.md" "skills-index.md")
for doc in "${docs[@]}"; do
    if [ -f ".claude/$doc" ]; then
        echo -e "${GREEN}✓${NC} $doc encontrado"
    else
        echo -e "${RED}✗${NC} $doc não encontrado"
        ((errors++))
    fi
done

echo ""
echo "🤖 Verificando agents..."
agents=("android-architecture-reviewer" "android-qa-engineer" "android-ui-designer"
        "audio-dsp-engineer" "toneforge-advanced-features" "toneforge-utility-developer")
for agent in "${agents[@]}"; do
    if [ -f ".claude/agents/$agent.md" ]; then
        echo -e "${GREEN}✓${NC} $agent.md"
    else
        echo -e "${RED}✗${NC} $agent.md não encontrado"
        ((errors++))
    fi
done

echo ""
echo "⚡ Verificando skills (padrão Anthropic)..."
skills=("audio-test" "cpp-effect" "debug-native" "mvp-scaffold"
        "refactor-legacy" "release-prep" "security-audit")
for skill in "${skills[@]}"; do
    if [ -d ".claude/skills/$skill" ]; then
        if [ -f ".claude/skills/$skill/SKILL.md" ]; then
            echo -e "${GREEN}✓${NC} $skill/SKILL.md"
        else
            echo -e "${RED}✗${NC} $skill/SKILL.md não encontrado (diretório existe)"
            ((errors++))
        fi
    else
        echo -e "${RED}✗${NC} $skill/ diretório não encontrado"
        ((errors++))
    fi
done

echo ""
echo "📚 Verificando documentação de skills..."
if [ -f ".claude/skills/README.md" ]; then
    echo -e "${GREEN}✓${NC} skills/README.md"
else
    echo -e "${RED}✗${NC} skills/README.md não encontrado"
    ((errors++))
fi

if [ -f ".claude/skills/USAGE.md" ]; then
    echo -e "${GREEN}✓${NC} skills/USAGE.md"
else
    echo -e "${RED}✗${NC} skills/USAGE.md não encontrado"
    ((errors++))
fi

echo ""
echo "═══════════════════════════════════════"
if [ $errors -eq 0 ]; then
    echo -e "${GREEN}✅ Estrutura válida!${NC}"
    echo ""
    echo "Estatísticas:"
    echo "  • $(ls -1 .claude/agents/*.md 2>/dev/null | wc -l) agents"
    echo "  • $(find .claude/skills -name 'SKILL.md' 2>/dev/null | wc -l) skills"
    echo "  • $(ls -1 .claude/*.md 2>/dev/null | wc -l) documentos principais"
    echo ""
    echo "Padrão Anthropic: ✅"
    echo "  Estrutura: .claude/skills/[skill-name]/SKILL.md"
    exit 0
else
    echo -e "${RED}❌ Encontrados $errors erro(s)${NC}"
    exit 1
fi
