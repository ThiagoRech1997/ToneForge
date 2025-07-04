#!/bin/bash

# Script para criar uma nova release do ToneForge
# Uso: ./scripts/create-release.sh [version] [message]

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para mostrar ajuda
show_help() {
    echo -e "${BLUE}ToneForge Release Script${NC}"
    echo ""
    echo "Uso: $0 [version] [message]"
    echo ""
    echo "Exemplos:"
    echo "  $0 1.2.0 \"Nova versão com correções de bugs\""
    echo "  $0 1.1.5"
    echo ""
    echo "O script irá:"
    echo "  1. Criar uma tag git com a versão"
    echo "  2. Fazer push da tag para o GitHub"
    echo "  3. Trigger do workflow de build e release"
    echo ""
}

# Verificar se está no diretório correto
if [ ! -f "app/build.gradle.kts" ]; then
    echo -e "${RED}Erro: Execute este script na raiz do projeto ToneForge${NC}"
    exit 1
fi

# Verificar se git está configurado
if ! git config --get user.name > /dev/null 2>&1; then
    echo -e "${RED}Erro: Git não está configurado. Configure seu nome e email:${NC}"
    echo "git config --global user.name \"Seu Nome\""
    echo "git config --global user.email \"seu@email.com\""
    exit 1
fi

# Verificar argumentos
if [ $# -eq 0 ] || [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    show_help
    exit 0
fi

VERSION=$1
MESSAGE=${2:-"Release version $VERSION"}

# Validar formato da versão
if [[ ! $VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo -e "${RED}Erro: Versão deve estar no formato X.Y.Z (ex: 1.2.0)${NC}"
    exit 1
fi

echo -e "${BLUE}🎸 Criando release v$VERSION do ToneForge...${NC}"
echo ""

# Verificar se há mudanças não commitadas
if [ -n "$(git status --porcelain)" ]; then
    echo -e "${YELLOW}⚠️  Há mudanças não commitadas no repositório.${NC}"
    echo "Deseja continuar mesmo assim? (y/N)"
    read -r response
    if [[ ! "$response" =~ ^[Yy]$ ]]; then
        echo "Release cancelada."
        exit 1
    fi
fi

# Verificar se a tag já existe
if git tag -l | grep -q "v$VERSION"; then
    echo -e "${RED}Erro: A tag v$VERSION já existe!${NC}"
    exit 1
fi

# Criar tag
echo -e "${BLUE}📝 Criando tag v$VERSION...${NC}"
git tag -a "v$VERSION" -m "$MESSAGE"

# Fazer push da tag
echo -e "${BLUE}🚀 Fazendo push da tag para o GitHub...${NC}"
git push origin "v$VERSION"

echo ""
echo -e "${GREEN}✅ Release v$VERSION criada com sucesso!${NC}"
echo ""
echo -e "${BLUE}📋 Próximos passos:${NC}"
echo "  1. O workflow de build será executado automaticamente"
echo "  2. A APK será gerada e anexada à release"
echo "  3. Verifique o progresso em: https://github.com/$(git config --get remote.origin.url | sed 's/.*github.com[:/]\([^/]*\/[^/]*\).*/\1/')/actions"
echo ""
echo -e "${YELLOW}💡 Dica: Você pode monitorar o progresso com:${NC}"
echo "  watch -n 5 'gh run list --limit 5'"
echo "" 