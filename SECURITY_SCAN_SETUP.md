# Configuração de Análise de Segurança - ToneForge

Este documento explica como configurar análise de segurança usando ferramentas gratuitas no projeto ToneForge.

## 🎯 SonarQube (Recomendado)

### Pré-requisitos
1. Servidor SonarQube configurado (pode ser SonarCloud para projetos públicos)
2. Token de acesso do SonarQube
3. URL do servidor SonarQube

### Configuração Local

1. **Instalar SonarQube Scanner**:
```bash
# Linux/macOS
wget https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-4.8.0.2856-linux.zip
unzip sonar-scanner-cli-4.8.0.2856-linux.zip
export PATH=$PATH:./sonar-scanner-4.8.0.2856-linux/bin
```

2. **Configurar variáveis de ambiente**:
```bash
export SONAR_TOKEN="seu_token_aqui"
export SONAR_HOST_URL="https://sonarcloud.io"  # ou URL do seu servidor
```

3. **Executar análise**:
```bash
./gradlew sonarqube
```

### Configuração no GitHub

1. **Adicionar Secrets no repositório**:
   - `SONAR_TOKEN`: Token de acesso do SonarQube
   - `SONAR_HOST_URL`: URL do servidor SonarQube

2. **O workflow já está configurado** em `.github/workflows/ci-cd.yml`

## 🔍 Outras Ferramentas Gratuitas

### Semgrep
- Detecção de vulnerabilidades OWASP Top 10
- Regras específicas para Android
- Análise de secrets e credenciais
- Integração com GitHub Security tab

### CodeQL
- Análise semântica profunda (GitHub)
- Detecção de vulnerabilidades zero-day
- Integração nativa com GitHub

### SpotBugs
- Análise específica Java/Android
- Detecção de bugs de segurança

### Clang Static Analyzer
- Análise código nativo C/C++
- Detecção de memory leaks e buffer overflow

### OWASP Dependency Check
- Análise de dependências vulneráveis
- Base de dados CVE atualizada

## 🚀 Execução Automática

### GitHub Actions
- **Push/Pull Request**: Análise automática
- **Artefatos**: Relatórios disponíveis para download

### Comandos Úteis

```bash
# Executar todas as análises
./scripts/run-security-scan.sh all

# Ou individualmente
./scripts/run-security-scan.sh sonarqube
./scripts/run-security-scan.sh semgrep
./scripts/run-security-scan.sh spotbugs
./scripts/run-security-scan.sh clang
```

## 📈 Resultados Esperados

Com esta stack gratuita, você terá:
- **Análise automática** em cada PR
- **Detecção de vulnerabilidades** em tempo real
- **Relatórios detalhados** para cada área
- **Integração com GitHub Security tab**
- **Cobertura completa** do código Java, Kotlin e C/C++
- **Zero custo** de licenciamento

## 📊 Relatórios e Métricas

### SonarQube
- **Qualidade do código**: Bugs, vulnerabilidades, code smells
- **Cobertura de testes**: Relatórios JaCoCo integrados
- **Duplicação**: Código duplicado identificado
- **Complexidade**: Complexidade ciclomática
- **Manutenibilidade**: Índice de manutenibilidade

## 🔧 Configurações Avançadas

### SonarQube
- Ajustar regras em `sonar-project.properties`
- Configurar quality gates
- Personalizar exclusões de arquivos

## 📈 Monitoramento

### Dashboards
- **SonarQube**: Dashboard web com métricas em tempo real

### Alertas
- **GitHub**: Comentários automáticos em PRs
- **Email**: Notificações configuráveis
- **Slack/Teams**: Integração opcional

## 🛠️ Troubleshooting

### Problemas Comuns

1. **Erro de autenticação**:
   - Verificar tokens e credenciais
   - Confirmar permissões no servidor

2. **Timeout na análise**:
   - Aumentar timeout no workflow
   - Otimizar configurações de exclusão

3. **Falsos positivos**:
   - Configurar supressões em `suppression.xml`
   - Ajustar regras no SonarQube

### Logs e Debug
```bash
# SonarQube com debug
./gradlew sonarqube --debug
```

## 📚 Recursos Adicionais

- [Documentação SonarQube](https://docs.sonarqube.org/)
- [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/) 