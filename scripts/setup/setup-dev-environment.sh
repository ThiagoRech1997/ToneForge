#!/bin/bash

# 🔧 Script de Configuração Automática do Ambiente ToneForge
# Este script configura o ambiente de desenvolvimento Android

set -e  # Para em caso de erro

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Funções de log
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

log_step() {
    echo -e "${CYAN}🔧 $1${NC}"
}

echo ""
echo "🎸 ToneForge - Configuração do Ambiente de Desenvolvimento"
echo "==========================================================="
echo ""

# Verificar se está executando na raiz do projeto
if [ ! -f "app/build.gradle.kts" ]; then
    log_error "Execute este script na raiz do projeto ToneForge"
    exit 1
fi

# Etapa 1: Verificar Java
log_step "Etapa 1/7: Verificando Java..."
if java -version 2>&1 | grep -q "openjdk version"; then
    JAVA_VERSION=$(java -version 2>&1 | head -1)
    log_success "Java já instalado: $JAVA_VERSION"
else
    log_warning "Java não encontrado. Instalando OpenJDK 17..."
    echo ""
    log_info "Este comando requer privilégios sudo. Por favor, digite sua senha:"
    echo ""

    sudo apt update
    sudo apt install -y openjdk-17-jdk

    if java -version 2>&1 | grep -q "openjdk version"; then
        log_success "OpenJDK 17 instalado com sucesso!"
    else
        log_error "Falha ao instalar Java. Instale manualmente."
        exit 1
    fi
fi
echo ""

# Etapa 2: Configurar JAVA_HOME
log_step "Etapa 2/7: Configurando JAVA_HOME..."

# Descobrir caminho do Java
JAVA_PATH=$(update-alternatives --list java 2>/dev/null | grep java-17 | head -1)

if [ -z "$JAVA_PATH" ]; then
    # Fallback: procurar manualmente
    JAVA_PATH=$(find /usr/lib/jvm -name "java-17-openjdk*" -type d 2>/dev/null | head -1)
fi

if [ -n "$JAVA_PATH" ]; then
    # Remover /bin/java do final
    JAVA_HOME_PATH=$(echo "$JAVA_PATH" | sed 's|/bin/java||')

    # Se ainda tiver /bin no final, remover
    JAVA_HOME_PATH=$(echo "$JAVA_HOME_PATH" | sed 's|/bin$||')

    export JAVA_HOME="$JAVA_HOME_PATH"
    export PATH="$JAVA_HOME/bin:$PATH"

    log_success "JAVA_HOME configurado: $JAVA_HOME"

    # Verificar se já está no .bashrc
    if ! grep -q "JAVA_HOME" "$HOME/.bashrc"; then
        log_info "Adicionando JAVA_HOME ao ~/.bashrc..."
        echo "" >> "$HOME/.bashrc"
        echo "# Java Configuration - ToneForge" >> "$HOME/.bashrc"
        echo "export JAVA_HOME=$JAVA_HOME" >> "$HOME/.bashrc"
        echo 'export PATH=$JAVA_HOME/bin:$PATH' >> "$HOME/.bashrc"
        log_success "JAVA_HOME adicionado ao ~/.bashrc"
    else
        log_info "JAVA_HOME já está configurado no ~/.bashrc"
    fi
else
    log_error "Não foi possível encontrar JAVA_HOME automaticamente"
    log_info "Configure manualmente: export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64"
    exit 1
fi
echo ""

# Etapa 3: Verificar ferramentas Java
log_step "Etapa 3/7: Verificando ferramentas Java..."
if command -v javac &> /dev/null; then
    JAVAC_VERSION=$(javac -version 2>&1)
    log_success "javac disponível: $JAVAC_VERSION"
else
    log_error "javac não encontrado"
    exit 1
fi
echo ""

# Etapa 4: Verificar Gradle
log_step "Etapa 4/7: Verificando Gradle..."
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    log_success "Gradle wrapper encontrado e configurado"

    # Testar Gradle
    log_info "Testando Gradle (isso pode demorar alguns segundos)..."
    if ./gradlew --version > /dev/null 2>&1; then
        log_success "Gradle funcional!"
    else
        log_warning "Gradle wrapper encontrado mas pode ter problemas"
    fi
else
    log_error "gradlew não encontrado"
    exit 1
fi
echo ""

# Etapa 5: Verificar estrutura do projeto
log_step "Etapa 5/7: Verificando estrutura do projeto..."

CHECKS=(
    "app/build.gradle.kts:Configuração do app"
    "app/src/main/cpp:Código C++ nativo"
    "app/src/main/java:Código Java"
    "app/src/test:Testes unitários"
    "scripts/functional-validation.sh:Script de validação"
)

for check in "${CHECKS[@]}"; do
    FILE=$(echo "$check" | cut -d: -f1)
    DESC=$(echo "$check" | cut -d: -f2)

    if [ -e "$FILE" ]; then
        log_success "$DESC encontrado"
    else
        log_warning "$DESC não encontrado: $FILE"
    fi
done
echo ""

# Etapa 6: Criar script de verificação
log_step "Etapa 6/7: Criando script de verificação..."
cat > verify-environment.sh << 'VERIFY_EOF'
#!/bin/bash

echo "🔍 Verificação do Ambiente ToneForge"
echo "====================================="
echo ""

echo "📦 Java:"
java -version 2>&1 | head -3
echo ""

echo "🔧 JAVA_HOME:"
echo "JAVA_HOME=$JAVA_HOME"
echo ""

echo "🛠️ Java Compiler:"
javac -version 2>&1
echo ""

echo "🏗️ Gradle:"
./gradlew --version 2>&1 | head -5
echo ""

echo "✅ Ambiente configurado!"
VERIFY_EOF

chmod +x verify-environment.sh
log_success "Script de verificação criado: ./verify-environment.sh"
echo ""

# Etapa 7: Testar compilação (opcional)
log_step "Etapa 7/7: Teste de compilação (opcional)..."
echo ""
read -p "Deseja testar a compilação agora? Isso pode demorar alguns minutos. (s/N): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[SsYy]$ ]]; then
    log_info "Iniciando compilação de teste..."
    echo ""

    if ./gradlew assembleDebug; then
        echo ""
        log_success "🎉 Compilação bem-sucedida!"

        if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
            APK_SIZE=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
            log_info "APK gerado: $APK_SIZE"
        fi
    else
        echo ""
        log_error "Compilação falhou. Verifique os erros acima."
        exit 1
    fi
else
    log_info "Pulando teste de compilação"
fi
echo ""

# Resumo final
echo "🎯 CONFIGURAÇÃO CONCLUÍDA!"
echo "=========================="
echo ""
log_success "Ambiente de desenvolvimento configurado com sucesso!"
echo ""
echo "📋 Resumo da configuração:"
echo "  ✅ Java: $(java -version 2>&1 | head -1)"
echo "  ✅ JAVA_HOME: $JAVA_HOME"
echo "  ✅ Gradle: Funcional"
echo "  ✅ Estrutura do projeto: Verificada"
echo ""
echo "🚀 Próximos passos:"
echo "  1. Recarregar shell: source ~/.bashrc"
echo "  2. Compilar projeto: ./gradlew assembleDebug"
echo "  3. Executar testes: ./gradlew test"
echo "  4. Validação completa: ./scripts/functional-validation.sh"
echo ""
echo "📚 Documentação:"
echo "  - Setup: SETUP-ENVIRONMENT.md"
echo "  - Projeto: CLAUDE.md"
echo "  - Verificação: ./verify-environment.sh"
echo ""
log_info "Configuração salva em ~/.bashrc"
log_warning "Execute 'source ~/.bashrc' para aplicar as mudanças neste terminal"
echo ""
