// Cubit do metrônomo. O áudio é gerado pelo DSP nativo (mesmo
// pipeline do Oboe); aqui só controlamos parâmetros e mantemos um
// scheduler local para o pulso visual da UI — análogo ao Handler do
// MetronomePresenter legado. Ver Fase 3.Metronome.

import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';

import '../../engine/engine.dart';

class MetronomeState {
  const MetronomeState({
    required this.bpm,
    required this.timeSignature,
    required this.volume,
    required this.isPlaying,
    required this.currentBeat,
    this.errorMessage,
  });

  static const initial = MetronomeState(
    bpm: 120,
    timeSignature: 4,
    volume: 0.6,
    isPlaying: false,
    currentBeat: -1,
  );

  final int bpm;
  final int timeSignature;
  final double volume;
  final bool isPlaying;
  final int currentBeat;
  final String? errorMessage;

  MetronomeState copyWith({
    int? bpm,
    int? timeSignature,
    double? volume,
    bool? isPlaying,
    int? currentBeat,
    String? errorMessage,
    bool clearError = false,
  }) {
    return MetronomeState(
      bpm: bpm ?? this.bpm,
      timeSignature: timeSignature ?? this.timeSignature,
      volume: volume ?? this.volume,
      isPlaying: isPlaying ?? this.isPlaying,
      currentBeat: currentBeat ?? this.currentBeat,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}

class MetronomeCubit extends Cubit<MetronomeState> {
  MetronomeCubit({ToneforgeEngine? engine})
      : _engine = engine ?? ToneforgeEngine(),
        super(MetronomeState.initial) {
    // Aplica o volume default no engine assim que o cubit nasce, para
    // evitar que o primeiro start use o volume residual de outra sessão.
    _engine.metronomeVolume = state.volume;
    _engine.metronomeTimeSignature = state.timeSignature;
  }

  static const int minBpm = 40;
  static const int maxBpm = 240;
  static const int minTs = 1;
  static const int maxTs = 16;

  final ToneforgeEngine _engine;
  Timer? _beatTimer;
  bool _ownsEngine = false;

  // ----- controles -----

  Future<void> start() async {
    if (state.isPlaying) return;

    if (!_engine.isRunning) {
      final result = _engine.start();
      if (result != 0) {
        emit(state.copyWith(errorMessage: 'Falha ao iniciar pipeline (code=$result)'));
        return;
      }
      _ownsEngine = true;
    }

    _engine.metronomeVolume = state.volume;
    _engine.metronomeTimeSignature = state.timeSignature;
    _engine.startMetronome(state.bpm);

    emit(state.copyWith(isPlaying: true, currentBeat: 0, clearError: true));
    _scheduleBeats();
  }

  Future<void> stop() async {
    _beatTimer?.cancel();
    _beatTimer = null;

    _engine.stopMetronome();
    if (_ownsEngine && _engine.isRunning) {
      _engine.stop();
      _ownsEngine = false;
    }

    emit(state.copyWith(isPlaying: false, currentBeat: -1));
  }

  void setBpm(int bpm) {
    final clamped = bpm.clamp(minBpm, maxBpm);
    if (clamped == state.bpm) return;
    emit(state.copyWith(bpm: clamped));
    if (state.isPlaying) {
      // O native recalcula samples/beat; reiniciar evita drift no scheduler.
      _engine.startMetronome(clamped);
      _scheduleBeats();
    }
  }

  void increaseBpm() => setBpm(state.bpm + 5);
  void decreaseBpm() => setBpm(state.bpm - 5);

  void setTimeSignature(int beats) {
    final clamped = beats.clamp(minTs, maxTs);
    if (clamped == state.timeSignature) return;
    emit(state.copyWith(timeSignature: clamped, currentBeat: state.isPlaying ? 0 : -1));
    _engine.metronomeTimeSignature = clamped;
  }

  void increaseTimeSignature() => setTimeSignature(state.timeSignature + 1);
  void decreaseTimeSignature() => setTimeSignature(state.timeSignature - 1);

  void setVolume(double volume) {
    final clamped = volume.clamp(0.0, 1.0);
    emit(state.copyWith(volume: clamped));
    _engine.metronomeVolume = clamped;
  }

  // ----- scheduling do pulso visual -----

  void _scheduleBeats() {
    _beatTimer?.cancel();
    final intervalMs = (60000 / state.bpm).round();
    _beatTimer = Timer.periodic(Duration(milliseconds: intervalMs), (_) {
      final next = (state.currentBeat + 1) % state.timeSignature;
      emit(state.copyWith(currentBeat: next));
    });
  }

  @override
  Future<void> close() {
    _beatTimer?.cancel();
    if (_engine.isMetronomeActive) _engine.stopMetronome();
    if (_ownsEngine && _engine.isRunning) _engine.stop();
    return super.close();
  }
}
