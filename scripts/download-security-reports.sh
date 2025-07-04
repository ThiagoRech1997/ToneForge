#!/bin/bash

# Script para baixar relatórios de segurança das GitHub Actions
# Uso: ./scripts/download-security-reports.sh [run_id]

set -e

# Configurações
REPO="ThiagoRech1997/ToneForge"
WORKFLOW="security-suite.yml"
OUTPUT_DIR="security-reports"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔒 Security Reports Downloader${NC}"
echo "=================================="

# Verificar se gh CLI está instalado
if ! command -v gh &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI (gh) não está instalado.${NC}"
    echo "Instale com: sudo apt install gh"
    echo "Ou configure: gh auth login"
    exit 1
fi

# Verificar se está autenticado
if ! gh auth status &> /dev/null; then
    echo -e "${RED}❌ Não autenticado no GitHub CLI.${NC}"
    echo "Execute: gh auth login"
    exit 1
fi

# Função para listar runs
list_runs() {
    echo -e "${YELLOW}📋 Execuções recentes do Security Suite:${NC}"
    gh run list --repo $REPO --workflow $WORKFLOW --limit 10
}

# Função para baixar artifacts
download_artifacts() {
    local run_id=$1
    
    echo -e "${GREEN}📥 Baixando artifacts da execução $run_id...${NC}"
    
    # Criar diretório de saída
    mkdir -p "$OUTPUT_DIR/$run_id"
    cd "$OUTPUT_DIR/$run_id"
    
    # Listar artifacts disponíveis
    echo -e "${BLUE}📦 Artifacts disponíveis:${NC}"
    gh run view $run_id --repo $REPO --json artifacts --jq '.artifacts[].name'
    
    # Baixar todos os artifacts
    gh run download $run_id --repo $REPO
    
    echo -e "${GREEN}✅ Artifacts baixados em: $OUTPUT_DIR/$run_id${NC}"
    cd - > /dev/null
}

# Função para mostrar resumo
show_summary() {
    local run_id=$1
    
    echo -e "${BLUE}📊 Resumo da execução $run_id:${NC}"
    gh run view $run_id --repo $REPO --json conclusion,status,createdAt,updatedAt,url
    
    echo -e "\n${YELLOW}🔍 Jobs executados:${NC}"
    gh run view $run_id --repo $REPO --json jobs --jq '.jobs[] | "\(.name): \(.conclusion // .status)"'
}

# Função para abrir relatórios no navegador
open_reports() {
    local run_id=$1
    
    echo -e "${BLUE}🌐 Abrindo relatórios no navegador...${NC}"
    
    # Abrir GitHub Actions
    gh run view $run_id --repo $REPO --web
    
    # Abrir Security tab
    echo -e "${YELLOW}🔒 Abrindo Security tab...${NC}"
    xdg-open "https://github.com/$REPO/security" 2>/dev/null || \
    open "https://github.com/$REPO/security" 2>/dev/null || \
    echo "Abra manualmente: https://github.com/$REPO/security"
}

# Função para mostrar ajuda
show_help() {
    echo "Uso: $0 [OPÇÕES] [RUN_ID]"
    echo ""
    echo "OPÇÕES:"
    echo "  -l, --list     Listar execuções recentes"
    echo "  -h, --help     Mostrar esta ajuda"
    echo ""
    echo "EXEMPLOS:"
    echo "  $0                    # Baixar última execução"
    echo "  $0 1234567890         # Baixar execução específica"
    echo "  $0 -l                 # Listar execuções"
    echo ""
    echo "RELATÓRIOS DISPONÍVEIS:"
    echo "  📊 SpotBugs: Análise de bugs Java/Android"
    echo "  ⚙️  Clang: Análise de código C/C++"
    echo "  📦 OWASP: Vulnerabilidades de dependências"
    echo "  🧪 Testes: Resultados de testes"
    echo "  📈 JaCoCo: Cobertura de código"
    echo "  🔍 Semgrep: Vulnerabilidades de segurança"
    echo "  🧠 CodeQL: Análise semântica"
}

# Processar argumentos
if [[ $# -eq 0 ]]; then
    # Sem argumentos: baixar última execução
    echo -e "${YELLOW}🔍 Obtendo última execução...${NC}"
    RUN_ID=$(gh run list --repo $REPO --workflow $WORKFLOW --limit 1 --json databaseId --jq '.[0].databaseId')
    
    if [[ "$RUN_ID" == "null" ]]; then
        echo -e "${RED}❌ Nenhuma execução encontrada.${NC}"
        list_runs
        exit 1
    fi
    
    show_summary $RUN_ID
    download_artifacts $RUN_ID
    open_reports $RUN_ID
    
elif [[ "$1" == "-l" || "$1" == "--list" ]]; then
    list_runs
    
elif [[ "$1" == "-h" || "$1" == "--help" ]]; then
    show_help
    
elif [[ "$1" =~ ^[0-9]+$ ]]; then
    # Número: baixar execução específica
    RUN_ID=$1
    show_summary $RUN_ID
    download_artifacts $RUN_ID
    open_reports $RUN_ID
    
else
    echo -e "${RED}❌ Argumento inválido: $1${NC}"
    show_help
    exit 1
fi

echo -e "\n${GREEN}🎉 Processo concluído!${NC}"
echo -e "${BLUE}📁 Relatórios salvos em: $OUTPUT_DIR${NC}"
echo -e "${YELLOW}💡 Dica: Abra os arquivos HTML no navegador para visualizar os relatórios${NC}" 