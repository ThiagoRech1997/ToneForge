# 🚀 Workflow de Releases Otimizado

Este documento explica como o sistema de releases foi otimizado para reduzir custos do GitHub Actions.

## 📊 Economia de Custos

### Antes (Custoso)
- ✅ Workflows executavam em **todos os pushes** para main
- ✅ Workflows executavam em **todos os pull requests**
- ✅ Análise de segurança completa em cada commit
- ✅ Múltiplos jobs paralelos consumindo minutos

### Agora (Econômico)
- ✅ Workflow executa **apenas em releases** (tags)
- ✅ Apenas build e release do Android
- ✅ Execução manual disponível quando necessário
- ✅ Máxima economia de custos

## 🎯 Como Criar uma Release

### Método 1: Script Automatizado (Recomendado)
```bash
# Criar release v1.2.0
./scripts/create-release.sh 1.2.0 "Nova versão com correções de bugs"

# Criar release simples
./scripts/create-release.sh 1.1.5
```

### Método 2: Manual
```bash
# 1. Criar tag
git tag -a v1.2.0 -m "Release version 1.2.0"

# 2. Fazer push da tag
git push origin v1.2.0
```

## 🔄 Workflow Disponível

### Android CI/CD (`android.yml`)
**Trigger:** Tags `v*` ou execução manual
**Jobs:**
- `build`: Compila APK Debug e Release
- `release`: Cria release no GitHub com APK anexada

**Economia:** Apenas este workflow executa, maximizando a economia de custos

## 📈 Monitoramento

### Verificar Status dos Workflows
```bash
# Listar últimas execuções
gh run list --limit 10

# Monitorar em tempo real
watch -n 5 'gh run list --limit 5'

# Ver detalhes de uma execução específica
gh run view <run-id>
```

### URLs Úteis
- **Actions:** `https://github.com/[user]/ToneForge/actions`
- **Releases:** `https://github.com/[user]/ToneForge/releases`
- **Security:** `https://github.com/[user]/ToneForge/security`

## ⚡ Execução Manual

Se precisar executar um workflow sem criar uma tag:

1. Vá para a aba **Actions** no GitHub
2. Selecione o workflow desejado
3. Clique em **"Run workflow"**
4. Escolha a branch e clique em **"Run workflow"**

## 💡 Dicas de Economia

### 1. Agrupe Mudanças
- Evite criar releases para cada pequena mudança
- Agrupe correções e melhorias em releases maiores

### 2. Use Branches para Desenvolvimento
- Desenvolva features em branches separadas
- Faça merge apenas quando estiver pronto para release

### 3. Teste Localmente
- Use `./gradlew assembleDebug` para testes locais
- Só use Actions para builds oficiais

### 4. Monitore Uso
- Verifique o uso de minutos em: `https://github.com/settings/billing`
- Configure alertas de limite de uso

## 🛠️ Troubleshooting

### Workflow Falhou
```bash
# Ver logs detalhados
gh run view <run-id> --log

# Re-executar workflow
gh run rerun <run-id>
```

### APK Não Gerada
- Verifique se o build foi bem-sucedido
- Confirme que o arquivo existe em `app/build/outputs/apk/`
- Verifique logs do job `build`

### Release Não Criada
- Verifique se a tag foi criada corretamente
- Confirme que o job `release` executou
- Verifique permissões do repositório

## 📋 Checklist de Release

Antes de criar uma release:

- [ ] Todos os testes passam localmente
- [ ] Código está limpo e documentado
- [ ] Version code atualizado no `build.gradle.kts`
- [ ] Changelog atualizado (se aplicável)
- [ ] Branch main está estável

## 🔧 Configuração Avançada

### Personalizar Build
Edite o arquivo `android.yml` para adicionar mais jobs se necessário:
```yaml
jobs:
  novo-job:
    runs-on: ubuntu-latest
    needs: build
    steps:
      - name: Meu novo passo
        run: echo "Novo job"
```

### Adicionar Mais Jobs
Para adicionar novos jobs ao workflow:
```yaml
jobs:
  novo-job:
    runs-on: ubuntu-latest
    needs: build  # Depende do job build
    steps:
      - name: Meu novo passo
        run: echo "Novo job"
```

---

**💡 Lembre-se:** O objetivo é manter a qualidade enquanto reduzimos custos. Use os Actions apenas quando realmente necessário! 