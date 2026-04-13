# iOS Build Guide — ToneForge Flutter

Este documento descreve como fazer o primeiro build iOS do `flutter_app`.
Todo o código C++ do engine + backend CoreAudio já está versionado, mas
precisa ser compilado pela primeira vez num macOS com Xcode — esse passo
**nunca foi executado** no ambiente de desenvolvimento original (Linux).

## Pré-requisitos

- macOS com Xcode 15+ (iOS 13+ deployment target)
- Flutter stable (mesma versão usada no Android: ver `fvm list`)
- CocoaPods (`sudo gem install cocoapods` ou via Homebrew)
- iPhone físico para testar (simulador não tem input de áudio real)
- Interface de áudio USB-C ou cabo Lightning→TRS para plugar guitarra

## Estrutura relevante

```
engine/
├── include/toneforge/*.h      # headers públicos (audio_engine, audio_io, recorder, loop_io)
├── src/
│   ├── audio_engine.cpp       # DSP puro (portável)
│   ├── audio_io_oboe.cpp      # backend Android (ignorado no iOS)
│   ├── audio_io_coreaudio.mm  # backend iOS (AudioUnit/RemoteIO + AVAudioSession)
│   ├── recorder.cpp
│   ├── loop_io.cpp
│   ├── wav_writer.{h,cpp}
└── toneforge_engine.podspec   # define o pod local consumido pelo Flutter

flutter_app/ios/
├── Podfile                    # referencia engine/toneforge_engine.podspec via :path
├── Runner/Info.plist          # microfone + background audio + BLE MIDI
└── Runner.xcodeproj
```

## Primeiro build

```bash
cd flutter_app
fvm flutter pub get
cd ios
pod install
cd ..
fvm flutter build ios --debug --no-codesign
```

Se der erro de `toneforge_engine` não encontrado, verifique se o caminho
relativo `:path => '../../engine'` no Podfile resolve corretamente a
partir de `flutter_app/ios/`.

## Para rodar num device físico

1. Abrir `flutter_app/ios/Runner.xcworkspace` no Xcode (NUNCA o
   `.xcodeproj` — depois do `pod install`, use o workspace).
2. Em *Signing & Capabilities*, configurar seu Team ID Apple Developer.
3. Adicionar capability **Background Modes** → **Audio, AirPlay, and
   Picture in Picture** (o `Info.plist` já declara, mas Xcode precisa
   que a capability esteja visível no target).
4. Conectar o iPhone via cabo, selecionar como target, buildar.

## O que validar no primeiro run

Ordem de smoke tests, do mais crítico para o menos:

1. **Compila**. O arquivo `audio_io_coreaudio.mm` foi escrito cegamente
   no Linux; espere ajustes finos em headers, propriedades da
   `AudioUnit` e tipos. Se falhar no `AudioUnitSetProperty` ou similar,
   revise os códigos `err` retornados nas chamadas em `audio_engine_start`.
2. **Pipeline sobe sem crash**: abrir Benchmark Oboe (ainda é o nome do
   card mas no iOS ele usa CoreAudio) → START → tela mostra sample
   rate > 0 e latência estimada.
3. **Áudio passa**: tocar uma corda com interface conectada ao iPhone,
   som deve sair pelos fones com o Gain.
4. **Efeitos**: abrir Effects, ligar Distortion, voltar a tocar → deve
   distorcer.
5. **Background audio**: com o pipeline rodando, apertar home button →
   o som deve continuar.
6. **Recorder**: gravar 10s, parar, abrir a lista → verificar que o
   WAV aparece em `Documents/recordings/`.
7. **Loop Library**: gravar um loop, salvar, carregar → tocar.
8. **Tuner**: tocar uma corda solta → frequência e nota aparecem.
9. **MIDI**: se tiver um controller BLE/USB, conectar e fazer Learn.

## Problemas conhecidos / ajustes prováveis

### Latência de input muito alta

Por default, `AVAudioSession.preferredIOBufferDuration` ajusta para o
hardware, mas iPhones recentes conseguem 5-10ms com `128 frames` de
burst. Se medir >20ms:

- Confirme que o modo é `AVAudioSessionModeMeasurement`
- Reduza o `preferredBufFrames` em `audio_io_coreaudio.mm` (já está em 128)
- Verifique se `AVAudioSessionCategoryOptionMixWithOthers` não está
  degradando — remover se for o caso

### `kAudioUnitErr_InvalidProperty` no setup

Geralmente é ordem errada de chamadas. A ordem correta:
1. `AudioComponentInstanceNew`
2. `EnableIO` no input bus
3. `SetProperty(StreamFormat)` nas duas scopes
4. `SetProperty(SetRenderCallback)` no output bus
5. `AudioUnitInitialize`
6. `AudioOutputUnitStart`

### Cannot find C++ headers

Se `#include "toneforge/audio_engine.h"` falhar, adicione no pod
target_xcconfig:

```ruby
'HEADER_SEARCH_PATHS' => '$(PODS_TARGET_SRCROOT)/include $(inherited)'
```

### `Undefined symbol` ao linkar

Verifique que todas as `extern "C"` estão corretas nos headers e que o
`wav_writer.cpp` está no `source_files` do podspec.

## Paridade com Android

A API C pública é **idêntica** entre Oboe e CoreAudio: mesmo
`audio_io.h`, mesmas funções `audio_engine_start / stop / is_running /
get_latency_ms / get_xrun_count / get_sample_rate`. O Dart wrapper
(`flutter_app/lib/engine/engine.dart`) detecta a plataforma e usa
`DynamicLibrary.process()` no iOS em vez de
`DynamicLibrary.open('libtoneforge_engine.so')` do Android.

Se uma feature funciona no Android e quebra no iOS, o problema é no
backend de I/O, não no DSP nem no Dart.

## Quando a Fase 4 termina

Critério de saída:

- APK Android e IPA iOS buildam sem warnings bloqueantes
- Benchmark round-trip latency no iPhone ≤ latência do Pixel no
  mesmo hardware de input (interface USB-C)
- 10 minutos de stress com todos os efeitos ligados, sem xruns nem
  glitches audíveis em ambos
- Background audio funcional em ambos

Após isso, a Fase 5 (deprecação do projeto Java legado) pode começar.
