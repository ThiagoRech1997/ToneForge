// Wrapper Dart idiomático sobre os bindings FFI gerados em bindings.dart.
// Encapsula o carregamento da libtoneforge_engine.so e expõe uma API
// type-safe para o resto do app Flutter. Ver plano Fase 2.
//
// O nome da biblioteca nativa depende da plataforma:
//   Android: libtoneforge_engine.so (carregado pelo próprio sistema)
//   iOS:     toneforge_engine (framework estático, process().lookup)
//
// Chamadas DSP são non-blocking por natureza (só mexem em atomics).
// Se alguma chamada passar a bloquear (arquivos, I/O), envolver em
// `Isolate.run` — nunca rodar da main isolate.

import 'dart:ffi' as ffi;
import 'dart:io' show Platform;

import 'bindings.dart';

class ToneforgeEngine {
  ToneforgeEngine._(this._bindings);

  static ToneforgeEngine? _instance;

  factory ToneforgeEngine() {
    _instance ??= ToneforgeEngine._(ToneforgeEngineBindings(_openLibrary()));
    return _instance!;
  }

  final ToneforgeEngineBindings _bindings;

  static ffi.DynamicLibrary _openLibrary() {
    if (Platform.isAndroid) {
      return ffi.DynamicLibrary.open('libtoneforge_engine.so');
    }
    if (Platform.isIOS || Platform.isMacOS) {
      // Fase 4 — iOS empacota como static framework, símbolos no process.
      return ffi.DynamicLibrary.process();
    }
    if (Platform.isLinux) {
      return ffi.DynamicLibrary.open('libtoneforge_engine.so');
    }
    throw UnsupportedError('Plataforma não suportada pelo toneforge_engine');
  }

  // ========================================================================
  // Lifecycle do pipeline de áudio (Oboe no Android, AVAudioEngine no iOS)
  // ========================================================================

  /// Inicia o pipeline. Retorna 0 em sucesso, código de erro Oboe caso falhe.
  /// sampleRate=0 e framesPerCallback=0 deixam o Oboe escolher valores nativos.
  int start({int sampleRate = 0, int framesPerCallback = 0}) {
    _bindings.initAudioEngine();
    return _bindings.audio_engine_start(sampleRate, framesPerCallback);
  }

  void stop() {
    _bindings.audio_engine_stop();
    _bindings.cleanupAudioEngine();
  }

  bool get isRunning => _bindings.audio_engine_is_running();

  double get latencyMs => _bindings.audio_engine_get_latency_ms();

  int get xrunCount => _bindings.audio_engine_get_xrun_count();

  int get sampleRate => _bindings.audio_engine_get_sample_rate();

  // ========================================================================
  // Efeitos — enable/disable. Parâmetros dos efeitos ainda não expostos
  // neste wrapper (cobertura se expande feature-a-feature em Fase 3).
  // ========================================================================

  set gainEnabled(bool v) => _bindings.setGainEnabled(v);
  set distortionEnabled(bool v) => _bindings.setDistortionEnabled(v);
  set delayEnabled(bool v) => _bindings.setDelayEnabled(v);
  set reverbEnabled(bool v) => _bindings.setReverbEnabled(v);
  set chorusEnabled(bool v) => _bindings.setChorusEnabled(v);
  set flangerEnabled(bool v) => _bindings.setFlangerEnabled(v);
  set phaserEnabled(bool v) => _bindings.setPhaserEnabled(v);
  set eqEnabled(bool v) => _bindings.setEQEnabled(v);
  set compressorEnabled(bool v) => _bindings.setCompressorEnabled(v);

  void setGain(double gain) => _bindings.setGain(gain);
  void setDistortionAmount(double amount) => _bindings.setDistortion(amount);
}
