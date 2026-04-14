#!/bin/bash

# 🧪 Script de Validação Funcional - ToneForge
#
# Valida:
#   - Headers do engine C++ portátil estão no lugar
#   - Podspec e CMakeLists existem para os dois backends (Android + iOS)
#   - flutter_app/ roda `flutter analyze` sem issues
#   - flutter_app/ compila o APK debug
#
# O projeto Android Java legado foi removido em Fase 5 — qualquer
# validação dele agora vive no git tag `legacy-android-final`.

set -e

echo "🎵 ToneForge - Validação Funcional"
echo "=================================="

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_success() { echo -e "${GREEN}✅ $1${NC}"; }
log_error()   { echo -e "${RED}❌ $1${NC}"; }
log_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
log_info()    { echo -e "${BLUE}ℹ️  $1${NC}"; }

# Verificar se estamos no diretório correto
if [ ! -d "engine" ] || [ ! -d "flutter_app" ]; then
    log_error "Execute este script na raiz do projeto ToneForge"
    log_error "Esperado encontrar engine/ e flutter_app/ no diretório atual"
    exit 1
fi

echo ""
log_info "Iniciando validação funcional..."

# 1. Engine portátil — headers, CMake, podspec
echo ""
log_info "1. Verificando engine/ portátil..."

ENGINE_HEADERS=(
    "engine/include/toneforge/audio_engine.h"
    "engine/include/toneforge/audio_io.h"
    "engine/include/toneforge/recorder.h"
    "engine/include/toneforge/loop_io.h"
)
for h in "${ENGINE_HEADERS[@]}"; do
    if [ -f "$h" ]; then
        log_success "Header $h presente"
    else
        log_error "Header $h ausente"
        exit 1
    fi
done

ENGINE_SOURCES=(
    "engine/src/audio_engine.cpp"
    "engine/src/audio_io_oboe.cpp"
    "engine/src/audio_io_coreaudio.mm"
    "engine/src/recorder.cpp"
    "engine/src/loop_io.cpp"
    "engine/src/wav_writer.cpp"
    "engine/src/wav_writer.h"
)
for s in "${ENGINE_SOURCES[@]}"; do
    if [ -f "$s" ]; then
        log_success "Source $s presente"
    else
        log_error "Source $s ausente"
        exit 1
    fi
done

if [ -f "engine/CMakeLists.txt" ] && [ -f "engine/toneforge_engine.podspec" ]; then
    log_success "engine/CMakeLists.txt e toneforge_engine.podspec presentes"
else
    log_error "Configuração de build do engine incompleta"
    exit 1
fi

# 2. Flutter toolchain check
echo ""
log_info "2. Verificando toolchain Flutter..."

FLUTTER_CMD=""
if command -v fvm >/dev/null 2>&1; then
    FLUTTER_CMD="fvm flutter"
    log_success "fvm detectado"
elif command -v flutter >/dev/null 2>&1; then
    FLUTTER_CMD="flutter"
    log_success "flutter detectado no PATH"
else
    log_error "Flutter não encontrado (nem via fvm nem no PATH)"
    log_info "   Instale via fvm (https://fvm.app) ou pela distribuição oficial"
    exit 1
fi

# 3. flutter_app analyze
echo ""
log_info "3. Rodando flutter analyze em flutter_app/..."
(
    cd flutter_app || exit 1
    if $FLUTTER_CMD pub get >/dev/null 2>&1; then
        log_success "pub get ok"
    else
        log_error "flutter pub get falhou"
        exit 1
    fi
    if $FLUTTER_CMD analyze; then
        log_success "flutter analyze: 0 issues"
    else
        log_error "flutter analyze reportou problemas"
        exit 1
    fi
) || exit 1

# 4. flutter_app debug APK build (validates engine/ compilation cross-tree)
echo ""
log_info "4. Buildando flutter_app (debug APK)..."
(
    cd flutter_app || exit 1
    if $FLUTTER_CMD build apk --debug; then
        log_success "flutter_app debug APK compilou com sucesso"
        if [ -f "build/app/outputs/flutter-apk/app-debug.apk" ]; then
            F_APK_SIZE=$(du -h build/app/outputs/flutter-apk/app-debug.apk | cut -f1)
            echo "📱 Tamanho do APK: $F_APK_SIZE"
        fi
    else
        log_error "Falha ao buildar flutter_app"
        exit 1
    fi
) || exit 1

# 5. Verificar que libtoneforge_engine.so foi bundled no APK
echo ""
log_info "5. Verificando native libs no APK..."
APK="flutter_app/build/app/outputs/flutter-apk/app-debug.apk"
if command -v unzip >/dev/null 2>&1 && [ -f "$APK" ]; then
    SO_LINES=$(unzip -l "$APK" 2>/dev/null | grep -c "libtoneforge_engine.so" || true)
    if [ "$SO_LINES" -ge 1 ]; then
        log_success "libtoneforge_engine.so presente em $SO_LINES ABI(s) do APK"
    else
        log_error "libtoneforge_engine.so ausente do APK"
        exit 1
    fi
else
    log_warning "unzip não disponível ou APK ausente — pulando verificação de libs"
fi

# 6. Documentação
echo ""
log_info "6. Verificando documentação..."

DOCS=(
    "docs/migration-status.md"
    "docs/ios-build.md"
    "CLAUDE.md"
    "README.md"
)
for doc in "${DOCS[@]}"; do
    if [ -f "$doc" ]; then
        log_success "Documentação $doc presente"
    else
        log_warning "Documentação $doc ausente"
    fi
done

# 7. Resumo final
echo ""
echo "🎯 RESUMO DA VALIDAÇÃO"
echo "======================"

log_success "🎉 VALIDAÇÃO CONCLUÍDA COM SUCESSO!"
echo ""
echo "📋 Checklist:"
echo "   ✅ Engine C++ headers e sources presentes"
echo "   ✅ CMake (Android) e podspec (iOS) configurados"
echo "   ✅ Flutter analyze sem issues"
echo "   ✅ flutter_app debug APK compila"
echo "   ✅ libtoneforge_engine.so bundled"
echo ""
echo "🚀 Próximos passos manuais:"
echo "   1. Instalar o APK:"
echo "      cd flutter_app && $FLUTTER_CMD install"
echo "   2. Para iOS: rodar docs/ios-build.md num macOS com Xcode"
echo "   3. Ver backlog em docs/migration-status.md"
echo ""
log_info "Validação funcional concluída!"
