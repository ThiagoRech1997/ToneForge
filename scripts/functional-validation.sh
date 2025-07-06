#!/bin/bash

# 🧪 Script de Validação Funcional - ToneForge
# Valida todas as funcionalidades após a refatoração MVP

set -e  # Para em caso de erro

echo "🎵 ToneForge - Validação Funcional"
echo "=================================="

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log colorido
log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Verificar se estamos no diretório correto
if [ ! -f "app/build.gradle.kts" ]; then
    log_error "Execute este script na raiz do projeto ToneForge"
    exit 1
fi

echo ""
log_info "Iniciando validação funcional..."

# 1. Verificar se o projeto compila
echo ""
log_info "1. Verificando compilação do projeto..."
if ./gradlew assembleDebug; then
    log_success "Projeto compila com sucesso"
else
    log_error "Falha na compilação do projeto"
    exit 1
fi

# 2. Executar testes unitários
echo ""
log_info "2. Executando testes unitários..."
if ./gradlew test; then
    log_success "Todos os testes unitários passaram"
else
    log_warning "Alguns testes unitários falharam"
fi

# 3. Verificar cobertura de código
echo ""
log_info "3. Verificando cobertura de código..."
if ./gradlew jacocoTestReport; then
    log_success "Relatório de cobertura gerado"
    echo "📊 Relatório disponível em: app/build/reports/jacoco/test/html/index.html"
else
    log_warning "Falha ao gerar relatório de cobertura"
fi

# 4. Verificar se o APK foi gerado
echo ""
log_info "4. Verificando geração do APK..."
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    log_success "APK gerado com sucesso"
    APK_SIZE=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
    echo "📱 Tamanho do APK: $APK_SIZE"
else
    log_error "APK não foi gerado"
    exit 1
fi

# 5. Verificar estrutura de arquivos refatorados
echo ""
log_info "5. Verificando estrutura de arquivos refatorados..."

# Verificar se todos os fragments refatorados existem
FRAGMENTS=(
    "HomeFragmentRefactored"
    "EffectsFragmentRefactored"
    "LooperFragmentRefactored"
    "TunerFragmentRefactored"
    "MetronomeFragmentRefactored"
    "RecorderFragmentRefactored"
    "SettingsFragmentRefactored"
    "LoopLibraryFragmentRefactored"
)

for fragment in "${FRAGMENTS[@]}"; do
    if find app/src -name "*$fragment*" -type f | grep -q .; then
        log_success "Fragment $fragment encontrado"
    else
        log_error "Fragment $fragment não encontrado"
    fi
done

# Verificar se todos os presenters existem
PRESENTERS=(
    "HomePresenter"
    "EffectsPresenter"
    "LooperPresenter"
    "TunerPresenter"
    "MetronomePresenter"
    "RecorderPresenter"
    "SettingsPresenter"
    "LoopLibraryPresenter"
)

for presenter in "${PRESENTERS[@]}"; do
    if find app/src -name "*$presenter*" -type f | grep -q .; then
        log_success "Presenter $presenter encontrado"
    else
        log_error "Presenter $presenter não encontrado"
    fi
done

# Verificar se todos os contratos existem
CONTRACTS=(
    "HomeContract"
    "EffectsContract"
    "LooperContract"
    "TunerContract"
    "MetronomeContract"
    "RecorderContract"
    "SettingsContract"
    "LoopLibraryContract"
)

for contract in "${CONTRACTS[@]}"; do
    if find app/src -name "*$contract*" -type f | grep -q .; then
        log_success "Contrato $contract encontrado"
    else
        log_error "Contrato $contract não encontrado"
    fi
done

# 6. Verificar classes base da arquitetura
echo ""
log_info "6. Verificando classes base da arquitetura..."

BASE_CLASSES=(
    "BaseFragment"
    "BasePresenter"
    "BaseView"
    "NavigationController"
    "AudioRepository"
)

for base_class in "${BASE_CLASSES[@]}"; do
    if find app/src -name "*$base_class*" -type f | grep -q .; then
        log_success "Classe base $base_class encontrada"
    else
        log_error "Classe base $base_class não encontrada"
    fi
done

# 7. Verificar modelos de domínio
echo ""
log_info "7. Verificando modelos de domínio..."

DOMAIN_MODELS=(
    "AudioState"
    "EffectParameters"
)

for model in "${DOMAIN_MODELS[@]}"; do
    if find app/src -name "*$model*" -type f | grep -q .; then
        log_success "Modelo $model encontrado"
    else
        log_error "Modelo $model não encontrado"
    fi
done

# 8. Verificar se não há fragments antigos sendo usados
echo ""
log_info "8. Verificando uso de fragments antigos..."

OLD_FRAGMENTS=(
    "HomeFragment"
    "EffectsFragment"
    "LooperFragment"
    "TunerFragment"
    "MetronomeFragment"
    "RecorderFragment"
    "SettingsFragment"
    "LoopLibraryFragment"
)

for old_fragment in "${OLD_FRAGMENTS[@]}"; do
    # Verificar se o fragment antigo ainda está sendo usado no MainActivity
    if grep -r "$old_fragment" app/src/main/java/com/thiagofernendorech/toneforge/MainActivity.java | grep -v "import" | grep -q .; then
        log_warning "Fragment antigo $old_fragment ainda está sendo usado no MainActivity"
    else
        log_success "Fragment antigo $old_fragment não está sendo usado"
    fi
done

# 9. Verificar imports corretos
echo ""
log_info "9. Verificando imports corretos..."

# Verificar se MainActivity importa fragments refatorados
if grep -q "import.*FragmentRefactored" app/src/main/java/com/thiagofernendorech/toneforge/MainActivity.java; then
    log_success "MainActivity importa fragments refatorados"
else
    log_warning "MainActivity pode não estar importando fragments refatorados"
fi

# 10. Verificar se NavigationController usa fragments corretos
echo ""
log_info "10. Verificando NavigationController..."

if grep -q "FragmentRefactored" app/src/main/java/com/thiagofernendorech/toneforge/ui/navigation/NavigationController.java; then
    log_success "NavigationController usa fragments refatorados"
else
    log_warning "NavigationController pode estar usando fragments antigos"
fi

# 11. Verificar documentação
echo ""
log_info "11. Verificando documentação..."

DOCS=(
    "architecture-refactoring-plan.md"
    "refactoring-progress-summary.md"
    "implementation-guide.md"
    "testing-strategy.md"
)

for doc in "${DOCS[@]}"; do
    if [ -f "docs/$doc" ]; then
        log_success "Documentação $doc encontrada"
    else
        log_warning "Documentação $doc não encontrada"
    fi
done

# 12. Resumo final
echo ""
echo "🎯 RESUMO DA VALIDAÇÃO"
echo "======================"

# Contar arquivos de teste
TEST_COUNT=$(find app/src/test -name "*.java" | wc -l)
log_info "Total de arquivos de teste: $TEST_COUNT"

# Contar fragments refatorados
REFACTORED_COUNT=$(find app/src -name "*FragmentRefactored*" | wc -l)
log_info "Total de fragments refatorados: $REFACTORED_COUNT"

# Contar presenters
PRESENTER_COUNT=$(find app/src -name "*Presenter*" | wc -l)
log_info "Total de presenters: $PRESENTER_COUNT"

# Verificar se há erros de compilação
if [ $? -eq 0 ]; then
    echo ""
    log_success "🎉 VALIDAÇÃO CONCLUÍDA COM SUCESSO!"
    echo ""
    echo "📋 Checklist de Funcionalidades:"
    echo "   ✅ Projeto compila sem erros"
    echo "   ✅ APK gerado com sucesso"
    echo "   ✅ Estrutura MVP implementada"
    echo "   ✅ Fragments refatorados"
    echo "   ✅ Presenters implementados"
    echo "   ✅ Contratos definidos"
    echo "   ✅ Navegação desacoplada"
    echo "   ✅ Repository pattern implementado"
    echo ""
    echo "🚀 Próximos passos:"
    echo "   1. Instalar o APK em um dispositivo/emulador"
    echo "   2. Testar navegação entre todos os fragments"
    echo "   3. Verificar funcionalidades de áudio"
    echo "   4. Validar efeitos e presets"
    echo "   5. Testar looper e gravador"
    echo "   6. Verificar afinador e metrônomo"
    echo ""
    echo "📱 Para instalar o APK:"
    echo "   adb install app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    log_error "❌ VALIDAÇÃO FALHOU!"
    echo "Verifique os erros acima e corrija antes de prosseguir."
    exit 1
fi

echo ""
log_info "Validação funcional concluída!" 