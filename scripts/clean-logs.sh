#!/bin/bash

# Script para limpar logs excessivos e verificar anomalias no código
# ToneForge - Log Cleanup Script

echo "=== ToneForge Log Cleanup Script ==="
echo ""

# Verificar se estamos no diretório correto
if [ ! -f "app/build.gradle.kts" ]; then
    echo "Erro: Execute este script na raiz do projeto ToneForge"
    exit 1
fi

echo "1. Verificando logs excessivos no código..."
echo ""

# Contar logs por tipo
echo "=== Contagem de Logs por Tipo ==="
echo "Log.d (Debug):"
grep -r "Log\.d" app/src/main/java/ | wc -l

echo "Log.v (Verbose):"
grep -r "Log\.v" app/src/main/java/ | wc -l

echo "Log.i (Info):"
grep -r "Log\.i" app/src/main/java/ | wc -l

echo "Log.w (Warning):"
grep -r "Log\.w" app/src/main/java/ | wc -l

echo "Log.e (Error):"
grep -r "Log\.e" app/src/main/java/ | wc -l

echo ""

# Verificar arquivos com mais logs
echo "=== Top 10 Arquivos com Mais Logs ==="
grep -r "Log\." app/src/main/java/ | cut -d: -f1 | sort | uniq -c | sort -nr | head -10

echo ""

# Verificar logs em loops ou métodos chamados frequentemente
echo "=== Verificando Logs em Loops de Processamento ==="
echo "PipelineManager:"
grep -n "Log\." app/src/main/java/com/thiagofernendorech/toneforge/PipelineManager.java

echo ""
echo "LooperFragment:"
grep -n "Log\." app/src/main/java/com/thiagofernendorech/toneforge/LooperFragment.java | head -5

echo ""
echo "AutomationManager:"
grep -n "Log\." app/src/main/java/com/thiagofernendorech/toneforge/AutomationManager.java | head -5

echo ""

# Verificar se o LogManager foi implementado corretamente
echo "=== Verificando Implementação do LogManager ==="
if [ -f "app/src/main/java/com/thiagofernendorech/toneforge/LogManager.java" ]; then
    echo "✓ LogManager.java encontrado"
    echo "Métodos disponíveis:"
    grep -n "public static" app/src/main/java/com/thiagofernendorech/toneforge/LogManager.java
else
    echo "✗ LogManager.java não encontrado"
fi

echo ""

# Verificar configurações de log nas settings
echo "=== Verificando Configurações de Log ==="
if grep -q "LogManager" app/src/main/java/com/thiagofernendorech/toneforge/ui/fragments/settings/SettingsPresenter.java; then
    echo "✓ Configurações de log implementadas no SettingsPresenter"
else
    echo "✗ Configurações de log não implementadas"
fi

echo ""

# Sugestões de melhorias
echo "=== Sugestões de Melhorias ==="
echo "1. Substituir Log.d por LogManager.verbose() para logs de debug"
echo "2. Substituir Log.i por LogManager.i() para logs informativos"
echo "3. Manter Log.e para erros críticos"
echo "4. Usar LogManager.verbose() apenas quando verbose logging estiver ativo"
echo "5. Implementar filtros por tag no LogManager"

echo ""
echo "=== Script Concluído ==="
echo "Para aplicar as melhorias:"
echo "1. Configure o nível de log nas configurações do app"
echo "2. Use LogManager.verbose() para logs de debug"
echo "3. Teste o app com diferentes níveis de log" 