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
import 'dart:typed_data';

import 'package:ffi/ffi.dart';

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

  // ========================================================================
  // Tuner — modo passivo. Quando ativo, audio_io_oboe.cpp encadeia os samples
  // de entrada para processTunerBuffer automaticamente (Fase 3.Tuner). Basta
  // ligar a flag e ler getDetectedFrequency periodicamente.
  // ========================================================================

  void startTuner() => _bindings.startTuner();
  void stopTuner() => _bindings.stopTuner();
  bool get isTunerActive => _bindings.isTunerActive();

  /// Última frequência fundamental detectada em Hz, ou 0 se sem sinal.
  double get detectedFrequency => _bindings.getDetectedFrequency();

  // ========================================================================
  // Metronome — gera samples no DSP e mistura na saída do pipeline. Como o
  // tuner, requer o pipeline de áudio rodando. Ver Fase 3.Metronome.
  // ========================================================================

  void startMetronome(int bpm) => _bindings.startMetronome(bpm);
  void stopMetronome() => _bindings.stopMetronome();
  bool get isMetronomeActive => _bindings.isMetronomeActive();
  set metronomeVolume(double v) => _bindings.setMetronomeVolume(v);
  set metronomeTimeSignature(int beats) => _bindings.setMetronomeTimeSignature(beats);

  // ========================================================================
  // Effects — parâmetros completos dos 9 efeitos. Faixas e defaults são
  // mantidos no Cubit / models.dart; aqui só forwardamos para o native.
  // Ver Fase 3.Effects.
  // ========================================================================

  // Gain
  void setGainLevel(double v) => _bindings.setGain(v);

  // Distortion
  void setDistortionAmount(double v) => _bindings.setDistortion(v);
  void setDistortionType(int type) => _bindings.setDistortionType(type);
  void setDistortionMix(double v) => _bindings.setDistortionMix(v);

  // Delay
  void setDelayTimeMs(double ms) => _bindings.setDelayTime(ms);
  void setDelayFeedback(double v) {
    // setDelay(time, feedback) é a única forma de setar feedback no native;
    // re-aplicamos o tempo atual lido pelo getter para não perdê-lo.
    final currentTime = _bindings.getDelayTime();
    _bindings.setDelay(currentTime, v);
  }
  void setDelayMix(double v) => _bindings.setDelayMix(v);

  // Reverb
  void setReverbRoomSize(double v) {
    final currentDamping = _bindings.getReverbDamping();
    _bindings.setReverb(v, currentDamping);
  }
  void setReverbDamping(double v) {
    final currentRoom = _bindings.getReverbRoomSize();
    _bindings.setReverb(currentRoom, v);
  }
  void setReverbType(int type) => _bindings.setReverbType(type);
  void setReverbMix(double v) => _bindings.setReverbMix(v);

  // Chorus
  void setChorusDepth(double v) => _bindings.setChorusDepth(v);
  void setChorusRate(double v) => _bindings.setChorusRate(v);
  void setChorusMix(double v) => _bindings.setChorusMix(v);

  // Flanger
  void setFlangerDepth(double v) => _bindings.setFlangerDepth(v);
  void setFlangerRate(double v) => _bindings.setFlangerRate(v);
  void setFlangerFeedback(double v) => _bindings.setFlangerFeedback(v);
  void setFlangerMix(double v) => _bindings.setFlangerMix(v);

  // Phaser
  void setPhaserDepth(double v) => _bindings.setPhaserDepth(v);
  void setPhaserRate(double v) => _bindings.setPhaserRate(v);
  void setPhaserFeedback(double v) => _bindings.setPhaserFeedback(v);
  void setPhaserMix(double v) => _bindings.setPhaserMix(v);

  // EQ (3-band)
  void setEqLow(double db) => _bindings.setEQLow(db);
  void setEqMid(double db) => _bindings.setEQMid(db);
  void setEqHigh(double db) => _bindings.setEQHigh(db);
  void setEqMix(double v) => _bindings.setEQMix(v);

  // Compressor
  void setCompressorThreshold(double db) => _bindings.setCompressorThreshold(db);
  void setCompressorRatio(double r) => _bindings.setCompressorRatio(r);
  void setCompressorAttack(double ms) => _bindings.setCompressorAttack(ms);
  void setCompressorRelease(double ms) => _bindings.setCompressorRelease(ms);
  void setCompressorMix(double v) => _bindings.setCompressorMix(v);

  // ========================================================================
  // Recorder — captura mono pós-FX no buffer interno do engine. O callback
  // Oboe alimenta automaticamente quando ativo. Ver Fase 3.Recorder.
  // ========================================================================

  /// Inicia a captura. Retorna 0 em sucesso ou um TF_RECORDER_ERR_* negativo.
  int startRecording({required int sampleRate, int maxSeconds = 600}) {
    return _bindings.recorder_start(sampleRate, maxSeconds);
  }

  /// Encerra a captura e escreve um WAV mono PCM 16-bit em [path].
  /// Retorna 0 em sucesso ou um TF_RECORDER_ERR_* negativo.
  int stopRecordingAndSave(String path) {
    final pathPtr = path.toNativeUtf8();
    try {
      return _bindings.recorder_stop_and_save(pathPtr.cast());
    } finally {
      malloc.free(pathPtr);
    }
  }

  void discardRecording() => _bindings.recorder_discard();
  bool get isRecording => _bindings.recorder_is_active();
  int get recordedFrames => _bindings.recorder_recorded_frames();
  double get recordedSeconds => _bindings.recorder_recorded_seconds();

  // ========================================================================
  // Looper — MVP + slicing. Fluxo de record/play/stop/clear/length/position/
  // waveform, mais a API de slicing (TFR-48). Multi-track, pitch shift,
  // reverse, quantização, MIDI, etc. ainda ficam para iterações futuras.
  // Ver Fase 3.Looper.
  // ========================================================================

  void startLooperRecording() => _bindings.startLooperRecording();
  void stopLooperRecording() => _bindings.stopLooperRecording();
  void startLooperPlayback() => _bindings.startLooperPlayback();
  void stopLooperPlayback() => _bindings.stopLooperPlayback();
  void clearLooper() => _bindings.clearLooper();
  bool get isLooperRecording => _bindings.isLooperRecording();
  bool get isLooperPlaying => _bindings.isLooperPlaying();
  int get looperLength => _bindings.getLooperLength();
  int get looperPosition => _bindings.getLooperPosition();
  void setLooperPosition(int frame) => _bindings.setLooperPosition(frame);

  // --- Slicing (TFR-48) -----------------------------------------------------

  /// Liga/desliga o modo slicing. Se ligado sem pontos definidos, o engine
  /// cria 8 slices automáticos baseados no comprimento atual do loop.
  void setLooperSlicingEnabled(bool enabled) =>
      _bindings.setLooperSlicingEnabled(enabled);

  bool get isLooperSlicingEnabled => _bindings.isLooperSlicingEnabled();

  int get looperNumSlices => _bindings.getLooperNumSlices();

  int get looperSliceLength => _bindings.getLooperSliceLength();

  void setLooperSliceLength(int length) =>
      _bindings.setLooperSliceLength(length);

  /// Define a lista de pontos de slice (em frames). O engine copia internamente
  /// — a memória alocada aqui é liberada no `finally`.
  void setLooperSlicePoints(List<int> points) {
    if (points.isEmpty) {
      // Passa um buffer de 1 elemento como stub — o engine só olha numPoints.
      final ptr = malloc<ffi.Int>(1);
      try {
        _bindings.setLooperSlicePoints(ptr, 0);
      } finally {
        malloc.free(ptr);
      }
      return;
    }
    final ptr = malloc<ffi.Int>(points.length);
    try {
      for (var i = 0; i < points.length; i++) {
        ptr[i] = points[i];
      }
      _bindings.setLooperSlicePoints(ptr, points.length);
    } finally {
      malloc.free(ptr);
    }
  }

  /// Define a ordem de reprodução dos slices (permutação de índices).
  void setLooperSliceOrder(List<int> order) {
    if (order.isEmpty) return;
    final ptr = malloc<ffi.Int>(order.length);
    try {
      for (var i = 0; i < order.length; i++) {
        ptr[i] = order[i];
      }
      _bindings.setLooperSliceOrder(ptr, order.length);
    } finally {
      malloc.free(ptr);
    }
  }

  void randomizeLooperSlices() => _bindings.randomizeLooperSlices();
  void reverseLooperSlices() => _bindings.reverseLooperSlices();

  /// Salva o mix atual do looper em [path] como WAV mono PCM 16-bit.
  /// Retorna 0 em sucesso ou um TF_LOOP_IO_ERR_* negativo.
  int saveLooperToWav(String path, {int? sampleRate}) {
    final sr = sampleRate ?? (this.sampleRate > 0 ? this.sampleRate : 48000);
    final pathPtr = path.toNativeUtf8();
    try {
      return _bindings.looper_save_wav(pathPtr.cast(), sr);
    } finally {
      malloc.free(pathPtr);
    }
  }

  /// Carrega samples float[-1,1] no buffer do looper. Aloca um
  /// `Pointer<Float>` temporário, copia os samples e libera após a chamada
  /// nativa retornar.
  void loadLooperFromFloats(Float32List samples) {
    if (samples.isEmpty) return;
    final ptr = malloc<ffi.Float>(samples.length);
    try {
      ptr.asTypedList(samples.length).setAll(0, samples);
      _bindings.loadLooperFromAudio(ptr, samples.length);
    } finally {
      malloc.free(ptr);
    }
  }

  /// Snapshot do mix do looper para visualização. O buffer nativo é alocado
  /// pelo engine (`new float[]`) e precisa ser liberado via
  /// `releaseLooperMix` — sem essa liberação o leak é ~maxLength*4 bytes por
  /// chamada, e o poller do LooperCubit chama isso a cada 200ms.
  List<double>? snapshotLooperMix({int targetPoints = 200}) {
    final outLen = malloc<ffi.Int>();
    ffi.Pointer<ffi.Float> ptr = ffi.nullptr;
    try {
      ptr = _bindings.getLooperMix(outLen);
      final len = outLen.value;
      if (ptr == ffi.nullptr || len <= 0) return null;
      final samples = ptr.asTypedList(len);
      // Downsampling peak-based: divide em targetPoints buckets, tira o
      // peak absoluto de cada. Simples, sem aliasing visual e barato.
      final pts = targetPoints.clamp(1, len);
      final out = List<double>.filled(pts, 0.0);
      final bucket = len / pts;
      for (var i = 0; i < pts; i++) {
        final start = (i * bucket).floor();
        final end = ((i + 1) * bucket).floor().clamp(start + 1, len);
        var peak = 0.0;
        for (var j = start; j < end; j++) {
          final a = samples[j].abs();
          if (a > peak) peak = a;
        }
        out[i] = peak;
      }
      return out;
    } finally {
      if (ptr != ffi.nullptr) _bindings.releaseLooperMix(ptr);
      malloc.free(outLen);
    }
  }
}

