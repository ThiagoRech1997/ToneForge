#!/bin/bash

# 🧪 Script de Teste Automatizado do ToneForge em Dispositivo Real
# Testa funcionalidades básicas do app via ADB

set -e

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

log_success() { echo -e "${GREEN}✅ $1${NC}"; }
log_error() { echo -e "${RED}❌ $1${NC}"; }
log_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
log_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
log_step() { echo -e "${CYAN}🔧 $1${NC}"; }

PACKAGE_NAME="com.thiagofernendorech.toneforge"
MAIN_ACTIVITY="${PACKAGE_NAME}/.MainActivity"

echo ""
echo "🎸 ToneForge - Teste Automatizado em Dispositivo"
echo "=================================================="
echo ""

# Verificar dispositivo conectado
log_step "Etapa 1/10: Verificando dispositivo conectado..."
DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device$" | wc -l)

if [ "$DEVICE_COUNT" -eq 0 ]; then
    log_error "Nenhum dispositivo Android conectado"
    echo ""
    echo "Conecte seu dispositivo via WiFi:"
    echo "  adb connect IP:PORTA"
    exit 1
elif [ "$DEVICE_COUNT" -gt 1 ]; then
    log_warning "Múltiplos dispositivos conectados. Usando o primeiro."
fi

DEVICE_ID=$(adb devices | grep -v "List" | grep "device$" | head -1 | awk '{print $1}')
log_success "Dispositivo conectado: $DEVICE_ID"
echo ""

# Verificar se app está instalado
log_step "Etapa 2/10: Verificando se ToneForge está instalado..."
if adb shell pm list packages | grep -q "$PACKAGE_NAME"; then
    VERSION=$(adb shell dumpsys package "$PACKAGE_NAME" | grep versionName | head -1 | awk '{print $1}')
    log_success "ToneForge instalado ($VERSION)"
else
    log_error "ToneForge não está instalado"
    echo ""
    echo "Instale o app primeiro:"
    echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
    exit 1
fi
echo ""

# Verificar permissões
log_step "Etapa 3/10: Verificando permissões do app..."

PERMISSIONS=(
    "android.permission.RECORD_AUDIO"
    "android.permission.WRITE_EXTERNAL_STORAGE"
    "android.permission.READ_EXTERNAL_STORAGE"
)

for PERM in "${PERMISSIONS[@]}"; do
    GRANTED=$(adb shell dumpsys package "$PACKAGE_NAME" | grep "$PERM" | grep "granted=true" || echo "")

    if [ -n "$GRANTED" ]; then
        log_success "Permissão concedida: $PERM"
    else
        log_warning "Permissão não concedida: $PERM"

        # Tentar conceder permissão automaticamente (funciona apenas em alguns casos)
        adb shell pm grant "$PACKAGE_NAME" "$PERM" 2>/dev/null || true
    fi
done
echo ""

# Limpar logs
log_step "Etapa 4/10: Limpando logs antigos..."
adb logcat -c
log_success "Logs limpos"
echo ""

# Parar app se estiver rodando
log_step "Etapa 5/10: Parando app (se estiver rodando)..."
adb shell am force-stop "$PACKAGE_NAME" 2>/dev/null || true
sleep 1
log_success "App parado"
echo ""

# Iniciar app
log_step "Etapa 6/10: Iniciando ToneForge..."
adb shell am start -n "$MAIN_ACTIVITY" > /dev/null 2>&1

if [ $? -eq 0 ]; then
    log_success "App iniciado com sucesso"
else
    log_error "Falha ao iniciar app"
    exit 1
fi
echo ""

# Aguardar app carregar
log_step "Etapa 7/10: Aguardando app carregar (5 segundos)..."
sleep 5
log_success "App carregado"
echo ""

# Verificar se app está em execução
log_step "Etapa 8/10: Verificando se app está em execução..."
RUNNING=$(adb shell pidof "$PACKAGE_NAME")

if [ -n "$RUNNING" ]; then
    log_success "App está rodando (PID: $RUNNING)"
else
    log_error "App não está em execução"

    echo ""
    log_info "Capturando logs de crash..."
    adb logcat -d | grep -A 20 "AndroidRuntime" | tail -30
    exit 1
fi
echo ""

# Capturar logs do app
log_step "Etapa 9/10: Capturando logs do app (últimos 10 segundos)..."
sleep 10

LOG_FILE="toneforge-test-logs.txt"
adb logcat -d | grep -i toneforge > "$LOG_FILE" 2>/dev/null || true

if [ -s "$LOG_FILE" ]; then
    LINE_COUNT=$(wc -l < "$LOG_FILE")
    log_success "Logs capturados: $LINE_COUNT linhas em $LOG_FILE"

    # Verificar erros nos logs
    ERROR_COUNT=$(grep -i "error\|exception\|crash" "$LOG_FILE" | wc -l)
    WARNING_COUNT=$(grep -i "warning" "$LOG_FILE" | wc -l)

    if [ "$ERROR_COUNT" -gt 0 ]; then
        log_warning "Encontrados $ERROR_COUNT erros nos logs"
    fi

    if [ "$WARNING_COUNT" -gt 0 ]; then
        log_info "Encontrados $WARNING_COUNT avisos nos logs"
    fi
else
    log_warning "Nenhum log do ToneForge capturado"
fi
echo ""

# Capturar screenshot
log_step "Etapa 10/10: Capturando screenshot..."
SCREENSHOT_FILE="toneforge-screenshot.png"
adb exec-out screencap -p > "$SCREENSHOT_FILE" 2>/dev/null

if [ -s "$SCREENSHOT_FILE" ]; then
    FILE_SIZE=$(du -h "$SCREENSHOT_FILE" | cut -f1)
    log_success "Screenshot salvo: $SCREENSHOT_FILE ($FILE_SIZE)"
else
    log_warning "Falha ao capturar screenshot"
fi
echo ""

# Verificar memória e CPU
log_info "Estatísticas do app:"
echo ""
adb shell dumpsys meminfo "$PACKAGE_NAME" | grep -A 1 "TOTAL" | head -2
echo ""

# Resumo final
echo "🎯 RESUMO DO TESTE"
echo "==================="
echo ""
log_success "✅ Dispositivo: $DEVICE_ID"
log_success "✅ App instalado e rodando"
log_success "✅ Logs capturados: $LOG_FILE"
[ -s "$SCREENSHOT_FILE" ] && log_success "✅ Screenshot: $SCREENSHOT_FILE"
echo ""

echo "📋 Próximos passos:"
echo "  1. Ver logs: cat $LOG_FILE"
echo "  2. Ver screenshot: xdg-open $SCREENSHOT_FILE (ou abrir manualmente)"
echo "  3. Testar funcionalidades manualmente no dispositivo"
echo "  4. Monitorar logs em tempo real: adb logcat | grep -i toneforge"
echo ""

echo "🧪 Testes Manuais Sugeridos:"
echo "  [ ] Efeitos de áudio funcionam?"
echo "  [ ] Looper grava e reproduz?"
echo "  [ ] Afinador detecta pitch?"
echo "  [ ] Metrônomo sincroniza?"
echo "  [ ] Presets salvam e carregam?"
echo "  [ ] App não trava ou congela?"
echo ""

log_info "Teste automatizado concluído!"
echo ""

# Oferecer monitoramento em tempo real
read -p "Deseja monitorar logs em tempo real? (s/N): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[SsYy]$ ]]; then
    log_info "Iniciando monitoramento de logs (Ctrl+C para parar)..."
    echo ""
    adb logcat | grep --color=always -i "toneforge\|androidruntime"
fi
