// Cubit do Tuner. Encapsula o lifecycle do engine + tuner + polling de
// frequência, expõe um único `TunerState` para a tela. Equivalente ao
// TunerPresenter do app legado, mas com state imutável e Cubit pattern.
// Ver Fase 3.Tuner.

import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';

import '../../engine/engine.dart';
import 'note_math.dart';

class TunerState {
  const TunerState({
    required this.isRunning,
    required this.reading,
    this.errorMessage,
  });

  static const initial = TunerState(
    isRunning: false,
    reading: NoteReading.empty,
  );

  final bool isRunning;
  final NoteReading reading;
  final String? errorMessage;

  TunerState copyWith({
    bool? isRunning,
    NoteReading? reading,
    String? errorMessage,
    bool clearError = false,
  }) {
    return TunerState(
      isRunning: isRunning ?? this.isRunning,
      reading: reading ?? this.reading,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}

class TunerCubit extends Cubit<TunerState> {
  TunerCubit({ToneforgeEngine? engine})
      : _engine = engine ?? ToneforgeEngine(),
        super(TunerState.initial);

  static const _pollInterval = Duration(milliseconds: 100); // 10 Hz, igual ao legado

  final ToneforgeEngine _engine;
  Timer? _pollTimer;
  bool _ownsEngine = false;

  Future<void> start() async {
    if (state.isRunning) return;

    // Garante o pipeline de áudio rodando — Oboe vai alimentar
    // processTunerBuffer via audio_io_oboe.cpp enquanto isTunerActive=true.
    if (!_engine.isRunning) {
      final result = _engine.start();
      if (result != 0) {
        emit(state.copyWith(errorMessage: 'Falha ao iniciar pipeline (code=$result)'));
        return;
      }
      _ownsEngine = true;
    }

    _engine.startTuner();
    emit(state.copyWith(isRunning: true, clearError: true));

    _pollTimer = Timer.periodic(_pollInterval, (_) {
      final freq = _engine.detectedFrequency;
      emit(state.copyWith(reading: analyzeFrequency(freq)));
    });
  }

  Future<void> stop() async {
    _pollTimer?.cancel();
    _pollTimer = null;

    _engine.stopTuner();
    if (_ownsEngine && _engine.isRunning) {
      _engine.stop();
      _ownsEngine = false;
    }

    emit(const TunerState(isRunning: false, reading: NoteReading.empty));
  }

  @override
  Future<void> close() {
    _pollTimer?.cancel();
    if (_engine.isTunerActive) _engine.stopTuner();
    if (_ownsEngine && _engine.isRunning) _engine.stop();
    return super.close();
  }
}
