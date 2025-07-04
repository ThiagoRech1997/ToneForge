# 🔒 Guia de Relatórios de Segurança - ToneForge

Este guia explica como acessar, baixar e interpretar os relatórios das ferramentas de análise de segurança do projeto ToneForge.

## 📊 Ferramentas de Análise

### 1. 🔍 **Semgrep** - Análise de Vulnerabilidades
- **O que faz**: Detecta vulnerabilidades de segurança, bugs e problemas de qualidade
- **Onde encontrar**: GitHub Security tab → Code scanning alerts
- **Formato**: SARIF (Structured Analysis Results Interchange Format)
- **Frequência**: A cada push/PR

### 2. 🧠 **CodeQL** - Análise Semântica
- **O que faz**: Análise semântica profunda do código Java
- **Onde encontrar**: GitHub Security tab → Code scanning alerts
- **Formato**: SARIF
- **Frequência**: A cada push/PR

### 3. 🐛 **SpotBugs** - Análise Java/Android
- **O que faz**: Detecta bugs específicos em código Java/Android
- **Onde encontrar**: GitHub Actions → Artifacts → `spotbugs-report/`
- **Formato**: HTML
- **Frequência**: A cada push/PR

### 4. ⚙️ **Clang Static Analyzer** - Análise C/C++
- **O que faz**: Analisa código nativo C/C++ do Android NDK
- **Onde encontrar**: GitHub Actions → Artifacts → `clang-report/`
- **Formato**: Markdown
- **Frequência**: A cada push/PR

### 5. 📦 **OWASP Dependency Check** - Dependências
- **O que faz**: Verifica vulnerabilidades em dependências
- **Onde encontrar**: GitHub Actions → Artifacts → `owasp-security-report/`
- **Formato**: HTML
- **Frequência**: A cada push/PR

### 6. 📊 **SonarQube** - Qualidade Geral
- **O que faz**: Análise completa de qualidade e segurança
- **Onde encontrar**: Servidor SonarQube (se configurado)
- **Formato**: Web interface
- **Frequência**: A cada push/PR

## 🚀 Como Acessar os Relatórios

### Método 1: Script Automático (Recomendado)

```bash
# Instalar GitHub CLI (se não tiver)
sudo apt install gh
gh auth login

# Baixar última execução
./scripts/download-security-reports.sh

# Listar execuções recentes
./scripts/download-security-reports.sh -l

# Baixar execução específica
./scripts/download-security-reports.sh 1234567890

# Ver ajuda
./scripts/download-security-reports.sh -h
```

### Método 2: Interface Web do GitHub

1. **GitHub Actions**:
   - Vá para: `https://github.com/ThiagoRech1997/ToneForge/actions`
   - Clique em "Security Analysis Suite"
   - Clique na execução desejada
   - Role para baixo e clique nos **Artifacts**

2. **GitHub Security**:
   - Vá para: `https://github.com/ThiagoRech1997/ToneForge/security`
   - Veja "Code scanning alerts"
   - Filtre por ferramenta (Semgrep/CodeQL)

### Método 3: GitHub CLI Manual

```bash
# Listar execuções
gh run list --repo ThiagoRech1997/ToneForge --workflow security-suite.yml

# Ver detalhes de uma execução
gh run view <RUN_ID> --repo ThiagoRech1997/ToneForge

# Baixar artifacts
gh run download <RUN_ID> --repo ThiagoRech1997/ToneForge
```

## 📋 Interpretando os Relatórios

### 🔴 **Prioridade Alta** (Crítico)
- Vulnerabilidades de segurança críticas
- Bugs que podem causar crashes
- Problemas de autenticação/autorização

### 🟡 **Prioridade Média** (Importante)
- Problemas de performance
- Código deprecated
- Possíveis memory leaks

### 🟢 **Prioridade Baixa** (Sugestão)
- Melhorias de código
- Sugestões de refatoração
- Problemas de estilo

## 🛠️ Configuração Adicional

### SonarQube (Opcional)

Para habilitar análise completa do SonarQube:

1. **Configure os secrets no GitHub**:
   - `SONAR_TOKEN`: Token de acesso do SonarQube
   - `SONAR_HOST_URL`: URL do seu servidor SonarQube

2. **Ou use SonarCloud gratuito**:
   - Vá para: https://sonarcloud.io
   - Conecte seu repositório
   - Configure automaticamente

### Notificações

O workflow envia comentários automáticos em Pull Requests com:
- Resumo dos resultados
- Links para relatórios detalhados
- Status de cada ferramenta

## 📈 Monitoramento Contínuo

### Alertas Automáticos
- **GitHub Security**: Alertas em tempo real
- **Email**: Notificações de vulnerabilidades críticas
- **Slack/Discord**: Integração via webhooks (configurável)

### Métricas
- **Cobertura de código**: JaCoCo reports
- **Qualidade**: SonarQube metrics
- **Vulnerabilidades**: Contagem por severidade

## 🔧 Troubleshooting

### Problemas Comuns

1. **Pipeline falha**:
   - Verifique os logs no GitHub Actions
   - Confirme se Java 17 está configurado
   - Verifique permissões de arquivos

2. **Relatórios não aparecem**:
   - Aguarde a conclusão da pipeline
   - Verifique se os artifacts foram gerados
   - Confirme se não há erros nos jobs

3. **SonarQube não funciona**:
   - Verifique se os secrets estão configurados
   - Confirme se o servidor está acessível
   - Verifique as credenciais

### Logs Úteis

```bash
# Ver logs de uma execução
gh run view <RUN_ID> --repo ThiagoRech1997/ToneForge --log

# Ver logs de um job específico
gh run view <RUN_ID> --repo ThiagoRech1997/ToneForge --log --job <JOB_NAME>
```

## 📞 Suporte

Se encontrar problemas:
1. Verifique este guia
2. Consulte os logs do GitHub Actions
3. Abra uma issue no repositório
4. Consulte a documentação das ferramentas

---

**🎯 Dica**: Execute o script `./scripts/download-security-reports.sh` regularmente para manter-se atualizado sobre a segurança do projeto! 