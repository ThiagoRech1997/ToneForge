#!/bin/bash

# Script para executar análise de segurança no ToneForge
# Uso: ./scripts/run-security-scan.sh [sonarqube|semgrep|codeql|spotbugs|clang|all]

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERRO]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCESSO]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[AVISO]${NC} $1"
}

# Verificar se estamos no diretório raiz do projeto
if [ ! -f "app/build.gradle.kts" ]; then
    error "Execute este script no diretório raiz do projeto ToneForge"
    exit 1
fi

# Função para executar SonarQube
run_sonarqube() {
    log "Iniciando análise SonarQube..."
    
    # Verificar se as variáveis de ambiente estão configuradas
    if [ -z "$SONAR_TOKEN" ] || [ -z "$SONAR_HOST_URL" ]; then
        error "Configure as variáveis de ambiente SONAR_TOKEN e SONAR_HOST_URL"
        echo "Exemplo:"
        echo "export SONAR_TOKEN='seu_token_aqui'"
        echo "export SONAR_HOST_URL='https://sonarcloud.io'"
        exit 1
    fi
    
    # Limpar e construir o projeto
    log "Limpando e construindo o projeto..."
    ./gradlew clean build
    
    # Executar testes
    log "Executando testes..."
    ./gradlew test
    
    # Gerar relatório de cobertura
    log "Gerando relatório de cobertura..."
    ./gradlew jacocoTestReport
    
    # Executar lint
    log "Executando Android Lint..."
    ./gradlew lint
    
    # Executar análise SonarQube
    log "Executando análise SonarQube..."
    ./gradlew sonarqube \
        -Dsonar.projectKey=thiagofernendorech_toneforge \
        -Dsonar.host.url="$SONAR_HOST_URL" \
        -Dsonar.login="$SONAR_TOKEN"
    
    success "Análise SonarQube concluída!"
}

# Função para executar Semgrep
run_semgrep() {
    log "Iniciando análise Semgrep..."
    
    # Verificar se o Semgrep está instalado
    if ! command -v semgrep &> /dev/null; then
        log "Instalando Semgrep..."
        pip install semgrep
    fi
    
    # Criar diretório para relatórios
    mkdir -p semgrep-reports
    
    # Executar análise
    log "Executando análise Semgrep..."
    semgrep scan \
        --config auto \
        --config p/security-audit \
        --config p/secrets \
        --config p/owasp-top-ten \
        --config p/android \
        --json > semgrep-reports/results.json \
        --html > semgrep-reports/results.html \
        --sarif > semgrep-reports/results.sarif
    
    success "Análise Semgrep concluída! Relatórios em: semgrep-reports/"
}

# Função para executar SpotBugs
run_spotbugs() {
    log "Iniciando análise SpotBugs..."
    
    # Verificar se o SpotBugs está disponível
    if ! command -v spotbugs &> /dev/null; then
        log "Baixando SpotBugs..."
        wget https://github.com/spotbugs/spotbugs/releases/download/4.7.3/spotbugs-4.7.3.tgz
        tar -xzf spotbugs-4.7.3.tgz
        export PATH=$PATH:./spotbugs-4.7.3/bin
    fi
    
    # Construir projeto
    log "Construindo projeto..."
    ./gradlew build
    
    # Criar diretório para relatórios
    mkdir -p spotbugs-reports
    
    # Executar análise
    log "Executando análise SpotBugs..."
    spotbugs -textui -html -outputDir spotbugs-reports \
        -include spotbugs-include.xml \
        app/build/intermediates/javac/debug/
    
    success "Análise SpotBugs concluída! Relatórios em: spotbugs-reports/"
}

# Função para executar Clang Static Analyzer
run_clang_analyzer() {
    log "Iniciando análise Clang Static Analyzer..."
    
    # Verificar se o Clang está instalado
    if ! command -v clang &> /dev/null; then
        error "Clang não encontrado. Instale com: sudo apt-get install clang"
        exit 1
    fi
    
    # Criar diretório para relatórios
    mkdir -p clang-reports
    
    # Configurar CMake com Clang
    log "Configurando CMake..."
    cd app/src/main/cpp
    mkdir -p build
    cd build
    cmake -DCMAKE_C_COMPILER=clang -DCMAKE_CXX_COMPILER=clang++ \
          -DCMAKE_BUILD_TYPE=Debug \
          -DCMAKE_EXPORT_COMPILE_COMMANDS=ON \
          ../
    
    # Executar análise estática
    log "Executando análise estática..."
    scan-build -o ../../../../../clang-reports make
    
    cd ../../../../../
    
    success "Análise Clang Static Analyzer concluída! Relatórios em: clang-reports/"
}

# Função para executar OWASP Dependency Check
run_owasp_check() {
    log "Iniciando OWASP Dependency Check..."
    
    # Verificar se o OWASP Dependency Check está disponível
    if ! command -v dependency-check.sh &> /dev/null; then
        warning "OWASP Dependency Check não encontrado. Pulando..."
        return 0
    fi
    
    # Criar diretório para relatórios
    mkdir -p owasp-reports
    
    # Executar análise
    dependency-check.sh \
        --project "ToneForge" \
        --scan "." \
        --format "HTML" \
        --out "owasp-reports" \
        --failOnCVSS 7 \
        --enableRetired \
        --enableExperimental
    
    success "OWASP Dependency Check concluído! Relatório em: owasp-reports/"
}

# Função para mostrar ajuda
show_help() {
    echo "Uso: $0 [sonarqube|semgrep|spotbugs|clang|owasp|all]"
    echo ""
    echo "Opções:"
    echo "  sonarqube  - Executar análise SonarQube (qualidade geral)"
    echo "  semgrep    - Executar análise Semgrep (vulnerabilidades)"
    echo "  spotbugs   - Executar análise SpotBugs (Java/Android)"
    echo "  clang      - Executar análise Clang (C/C++ nativo)"
    echo "  owasp      - Executar OWASP Dependency Check"
    echo "  all        - Executar todas as análises"
    echo "  help       - Mostrar esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  $0 sonarqube"
    echo "  $0 semgrep"
    echo "  $0 all"
    echo ""
    echo "Todas as ferramentas são gratuitas e focadas em segurança!"
}

# Main
case "${1:-help}" in
    "sonarqube")
        run_sonarqube
        ;;
    "semgrep")
        run_semgrep
        ;;
    "spotbugs")
        run_spotbugs
        ;;
    "clang")
        run_clang_analyzer
        ;;
    "owasp")
        run_owasp_check
        ;;
    "all")
        log "Executando todas as análises de segurança..."
        run_sonarqube
        echo ""
        run_semgrep
        echo ""
        run_spotbugs
        echo ""
        run_clang_analyzer
        echo ""
        run_owasp_check
        success "Todas as análises foram concluídas!"
        ;;
    "help"|*)
        show_help
        ;;
esac 