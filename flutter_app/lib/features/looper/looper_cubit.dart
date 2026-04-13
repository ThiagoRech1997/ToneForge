// LooperCubit — MVP. Gerencia gravação/reprodução de uma única loop com
// snapshot do waveform amostrado a 5Hz para visualização. Multi-track,
// reverse, pitch shift, slicing, BPM sync etc. ficam para iterações
// futuras (o engine C++ já suporta — só falta o lado UI). Ver Fase 3.Looper.

import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';

import '../../engine/engine.dart';

enum LooperMode { idle, recording, playing }

class LooperState {
  const LooperState({
    required this.mode,
    required this.lengthFrames,
    required this.positionFrames,
    required this.sampleRate,
    required this.waveform,
    this.errorMessage,
  });

  static const initial = LooperState(
    mode: LooperMode.idle,
    lengthFrames: 0,
    positionFrames: 0,
    sampleRate: 0,
    waveform: [],
  );

  final LooperMode mode;
  final int lengthFrames;
  final int positionFrames;
  final int sampleRate;
  final List<double> waveform;
  final String? errorMessage;

  bool get hasContent => lengthFrames > 0;

  double get lengthSeconds =>
      sampleRate > 0 ? lengthFrames / sampleRate : 0.0;
  double get positionSeconds =>
      sampleRate > 0 ? positionFrames / sampleRate : 0.0;
  double get progress =>
      lengthFrames > 0 ? (positionFrames / lengthFrames).clamp(0.0, 1.0) : 0.0;

  LooperState copyWith({
    LooperMode? mode,
    int? lengthFrames,
    int? positionFrames,
    int? sampleRate,
    List<double>? waveform,
    String? errorMessage,
    bool clearError = false,
  }) {
    return LooperState(
      mode: mode ?? this.mode,
      lengthFrames: lengthFrames ?? this.lengthFrames,
      positionFrames: positionFrames ?? this.positionFrames,
      sampleRate: sampleRate ?? this.sampleRate,
      waveform: waveform ?? this.waveform,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}

class LooperCubit extends Cubit<LooperState> {
  LooperCubit({ToneforgeEngine? engine})
      : _engine = engine ?? ToneforgeEngine(),
        super(LooperState.initial);

  static const _positionPollInterval = Duration(milliseconds: 50);
  static const _waveformRefreshInterval = Duration(milliseconds: 200);

  final ToneforgeEngine _engine;
  Timer? _positionTimer;
  Timer? _waveformTimer;
  bool _ownsEngine = false;

  Future<void> _ensurePipeline() async {
    if (_engine.isRunning) return;
    final result = _engine.start();
    if (result != 0) {
      emit(state.copyWith(errorMessage: 'Falha ao iniciar pipeline (code=$result)'));
      return;
    }
    _ownsEngine = true;
  }

  Future<void> startRecording() async {
    await _ensurePipeline();
    if (state.errorMessage != null) return;

    _engine.startLooperRecording();
    emit(state.copyWith(
      mode: LooperMode.recording,
      sampleRate: _engine.sampleRate > 0 ? _engine.sampleRate : 48000,
      clearError: true,
    ));
    _startPositionPolling();
  }

  Future<void> stopRecording() async {
    if (state.mode != LooperMode.recording) return;
    _engine.stopLooperRecording();
    final length = _engine.looperLength;
    emit(state.copyWith(
      mode: LooperMode.idle,
      lengthFrames: length,
      positionFrames: 0,
    ));
    _refreshWaveform();
  }

  Future<void> startPlayback() async {
    if (!state.hasContent) return;
    await _ensurePipeline();
    if (state.errorMessage != null) return;

    _engine.startLooperPlayback();
    emit(state.copyWith(mode: LooperMode.playing, clearError: true));
    _startPositionPolling();
    _startWaveformPolling();
  }

  Future<void> stopPlayback() async {
    if (state.mode != LooperMode.playing) return;
    _engine.stopLooperPlayback();
    _stopWaveformPolling();
    emit(state.copyWith(mode: LooperMode.idle));
  }

  Future<void> clear() async {
    _engine.clearLooper();
    _stopPositionPolling();
    _stopWaveformPolling();
    if (_ownsEngine && _engine.isRunning) {
      _engine.stop();
      _ownsEngine = false;
    }
    emit(LooperState.initial);
  }

  // ----- pollers -----

  void _startPositionPolling() {
    _positionTimer?.cancel();
    _positionTimer = Timer.periodic(_positionPollInterval, (_) {
      // Em recording, length cresce a cada callback; em playback, length é
      // fixo e a position dá voltas. Refletimos os dois casos uniformemente.
      emit(state.copyWith(
        lengthFrames: _engine.looperLength,
        positionFrames: _engine.looperPosition,
      ));
      // Auto-stop quando o native terminou recording por outro motivo.
      if (state.mode == LooperMode.recording && !_engine.isLooperRecording) {
        emit(state.copyWith(mode: LooperMode.idle));
        _stopPositionPolling();
        _refreshWaveform();
      }
    });
  }

  void _stopPositionPolling() {
    _positionTimer?.cancel();
    _positionTimer = null;
  }

  void _startWaveformPolling() {
    _waveformTimer?.cancel();
    _waveformTimer = Timer.periodic(_waveformRefreshInterval, (_) => _refreshWaveform());
  }

  void _stopWaveformPolling() {
    _waveformTimer?.cancel();
    _waveformTimer = null;
  }

  void _refreshWaveform() {
    final samples = _engine.snapshotLooperMix(targetPoints: 240);
    if (samples != null) {
      emit(state.copyWith(waveform: samples));
    }
  }

  @override
  Future<void> close() {
    _positionTimer?.cancel();
    _waveformTimer?.cancel();
    if (_engine.isLooperRecording) _engine.stopLooperRecording();
    if (_engine.isLooperPlaying) _engine.stopLooperPlayback();
    if (_ownsEngine && _engine.isRunning) _engine.stop();
    return super.close();
  }
}
