# 📱 Configurar ADB no WSL2 para Android

Guia completo para conectar seu smartphone Android ao WSL2 via ADB.

---

## 🎯 Métodos Disponíveis

Existem **3 métodos** principais para conectar Android ao WSL2:

1. **ADB via WiFi** (Recomendado - Mais fácil) ⭐
2. **USB/IP + usbipd-win** (Mais estável)
3. **ADB Windows + Port Forwarding** (Método alternativo)

---

## ⭐ Método 1: ADB via WiFi (RECOMENDADO)

**Vantagens:** Sem fio, fácil de configurar, funciona perfeitamente
**Desvantagens:** Celular e PC devem estar na mesma rede WiFi

### Passo 1: Habilitar Depuração USB no Android

1. Abra **Configurações** no Android
2. Vá em **Sobre o telefone**
3. Toque **7 vezes** em "Número da versão" ou "Número da compilação"
4. Volte para **Configurações**
5. Entre em **Opções do desenvolvedor** (ou **Sistema → Avançado → Opções do desenvolvedor**)
6. Ative **Depuração USB**
7. Ative **Depuração sem fio** (ou **Depuração por WiFi**)

### Passo 2: Instalar ADB no WSL2

```bash
# Verificar se ADB já está instalado
adb --version

# Se não estiver, instalar
sudo apt update
sudo apt install -y android-tools-adb android-tools-fastboot

# Verificar instalação
adb --version
```

### Passo 3: Conectar via WiFi

#### Opção A: Android 11+ (Pareamento por QR Code)

1. No Android, vá em **Opções do desenvolvedor → Depuração sem fio**
2. Toque em **Parear dispositivo com código de pareamento**
3. Anote o **IP e Porta** mostrados (ex: 192.168.1.100:12345)

4. No WSL2:
```bash
# Usar IP e porta mostrados no celular
adb pair 192.168.1.100:12345

# Digitar o código de pareamento mostrado no celular
# (6 dígitos)
```

5. Após parear, conectar:
```bash
# Pegar o IP permanente do celular (geralmente porta 5555)
# Visível em "Opções do desenvolvedor → Depuração sem fio"
adb connect 192.168.1.100:5555
```

#### Opção B: Android 10 ou inferior (Conexão USB primeiro)

**Problema:** WSL2 não acessa USB diretamente. **Solução:** Usar ADB do Windows temporariamente.

1. **No Windows**, baixe [Platform Tools](https://developer.android.com/tools/releases/platform-tools):
   - Baixar: https://dl.google.com/android/repository/platform-tools-latest-windows.zip
   - Extrair para: `C:\platform-tools\`

2. Conectar celular via USB ao PC

3. **No Windows PowerShell** (como Administrador):
```powershell
cd C:\platform-tools

# Verificar dispositivo conectado
.\adb.exe devices

# Habilitar ADB via TCP/IP na porta 5555
.\adb.exe tcpip 5555

# Pegar o IP do celular
.\adb.exe shell ip -f inet addr show wlan0
# Ou verificar em: Configurações → Sobre o telefone → Status → IP
```

4. Desconectar USB do celular

5. **No WSL2**:
```bash
# Conectar ao IP do celular
adb connect 192.168.1.100:5555

# Verificar conexão
adb devices
```

### Passo 4: Testar Conexão

```bash
# Listar dispositivos conectados
adb devices

# Saída esperada:
# List of devices attached
# 192.168.1.100:5555    device

# Testar comando
adb shell getprop ro.product.model

# Instalar APK do ToneForge
adb install app/build/outputs/apk/debug/app-debug.apk

# Ver logs
adb logcat | grep ToneForge
```

### Passo 5: Desconectar (quando terminar)

```bash
# Desconectar dispositivo
adb disconnect 192.168.1.100:5555

# Ou desconectar todos
adb disconnect
```

---

## 🔌 Método 2: USB/IP com usbipd-win (Mais Estável)

**Vantagens:** Conexão USB nativa, mais estável
**Desvantagens:** Configuração mais complexa, requer Windows 11 ou Windows 10 (2004+)

### Passo 1: Instalar usbipd-win no Windows

1. Baixe e instale [usbipd-win](https://github.com/dorssel/usbipd-win/releases)
   - Arquivo: `usbipd-win_x.x.x.msi`
   - Instalar e reiniciar o PC

### Passo 2: Instalar cliente USB/IP no WSL2

```bash
# Atualizar sistema
sudo apt update

# Instalar ferramentas USB/IP
sudo apt install -y linux-tools-generic hwdata

# Atualizar alternativas do kernel
sudo update-alternatives --install /usr/local/bin/usbip usbip /usr/lib/linux-tools/*-generic/usbip 20
```

### Passo 3: Conectar Dispositivo Android

1. Conectar celular via USB ao PC

2. **No Windows PowerShell** (como Administrador):
```powershell
# Listar dispositivos USB
usbipd list

# Encontrar o dispositivo Android (busque algo como "Android" ou nome do fabricante)
# Exemplo de saída:
# BUSID  VID:PID    DEVICE                  STATE
# 1-4    18d1:4ee7  Android Device          Not attached

# Compartilhar o dispositivo (substituir 1-4 pelo BUSID correto)
usbipd bind --busid 1-4

# Anexar ao WSL2
usbipd attach --wsl --busid 1-4
```

3. **No WSL2**:
```bash
# Verificar se dispositivo está visível
lsusb

# Verificar no ADB
adb devices

# Instalar APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Passo 4: Desconectar (quando terminar)

**No Windows PowerShell:**
```powershell
# Desanexar do WSL2
usbipd detach --busid 1-4
```

---

## 🔄 Método 3: Port Forwarding do ADB Windows

**Vantagens:** Usa ADB do Windows, sem instalar no WSL2
**Desvantagens:** Menos direto, requer ADB em ambos os lados

### Passo 1: Configurar ADB no Windows

1. Baixar [Platform Tools](https://developer.android.com/tools/releases/platform-tools)
2. Extrair para `C:\platform-tools\`
3. Adicionar ao PATH do Windows

### Passo 2: Conectar Celular via USB no Windows

```powershell
# No PowerShell
cd C:\platform-tools
.\adb.exe devices
```

### Passo 3: Fazer Port Forwarding para WSL2

```powershell
# Iniciar servidor ADB no Windows
.\adb.exe -a -P 5037 nodaemon server

# Em outro terminal PowerShell
# Pegar IP do WSL2
wsl hostname -I
# Exemplo: 172.28.160.1

# Fazer port forwarding
netsh interface portproxy add v4tov4 listenport=5037 listenaddress=0.0.0.0 connectport=5037 connectaddress=172.28.160.1
```

### Passo 4: Conectar do WSL2

```bash
# No WSL2, configurar ADB_SERVER_SOCKET
export ADB_SERVER_SOCKET=tcp:$(cat /etc/resolv.conf | grep nameserver | awk '{print $2}'):5037

# Verificar conexão
adb devices
```

---

## 🎯 Qual Método Usar?

### Use **Método 1 (WiFi)** se:
- ✅ Tem Android 11+
- ✅ Celular e PC estão na mesma rede WiFi
- ✅ Quer praticidade (recomendado para ToneForge)

### Use **Método 2 (USB/IP)** se:
- ✅ Tem Windows 11 ou Windows 10 (2004+)
- ✅ Precisa de conexão mais estável
- ✅ Vai fazer debug intenso ou transferências grandes

### Use **Método 3 (Port Forwarding)** se:
- ✅ Os outros métodos não funcionaram
- ✅ Já tem ADB configurado no Windows

---

## 🛠️ Troubleshooting

### Problema: `adb: command not found`

```bash
# Instalar ADB
sudo apt install -y android-tools-adb
```

### Problema: `device unauthorized`

No celular, aceite a solicitação "Permitir depuração USB" que aparece na tela.

```bash
# No WSL2
adb kill-server
adb start-server
adb devices
```

### Problema: `no devices/emulators found`

```bash
# Verificar se servidor ADB está rodando
adb kill-server
adb start-server

# Reconectar via WiFi
adb connect 192.168.1.100:5555
```

### Problema: `offline` no `adb devices`

```bash
# Desconectar e reconectar
adb disconnect
adb connect 192.168.1.100:5555
```

### Problema: WiFi não conecta

1. Verificar se celular e PC estão na mesma rede
2. Verificar firewall do Windows (permitir porta 5555)
3. Reiniciar servidor ADB:
   ```bash
   adb kill-server
   adb start-server
   ```

### Problema: USB/IP não funciona

```bash
# No WSL2, verificar se módulo está carregado
lsmod | grep vhci

# Se não estiver, carregar
sudo modprobe vhci-hcd
```

---

## ✅ Checklist Rápido (Método WiFi)

Para ToneForge, o método mais prático é WiFi:

- [ ] Habilitar "Opções do desenvolvedor" no Android (tocar 7x em "Número da versão")
- [ ] Ativar "Depuração USB" e "Depuração sem fio"
- [ ] Instalar ADB no WSL2: `sudo apt install android-tools-adb`
- [ ] Parear dispositivo: `adb pair IP:PORTA` (Android 11+)
- [ ] Conectar: `adb connect IP:5555`
- [ ] Verificar: `adb devices`
- [ ] Instalar ToneForge: `adb install app/build/outputs/apk/debug/app-debug.apk`

---

## 🎸 Testando ToneForge

Após conectar o celular:

```bash
cd /home/thiago/thiago-code/07-entretenimento/ToneForge

# Verificar dispositivo
adb devices

# Instalar APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Ver logs em tempo real
adb logcat | grep ToneForge

# Limpar logs antes de testar
adb logcat -c

# Abrir app no celular e ver logs
adb logcat | grep -E "ToneForge|AndroidRuntime"
```

---

## 📚 Recursos Adicionais

- [Android Developer - ADB](https://developer.android.com/tools/adb)
- [usbipd-win GitHub](https://github.com/dorssel/usbipd-win)
- [WSL USB Support](https://learn.microsoft.com/en-us/windows/wsl/connect-usb)

---

**🎯 Recomendação:** Use o **Método 1 (WiFi)** para testar o ToneForge rapidamente!
