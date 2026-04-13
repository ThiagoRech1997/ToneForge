// AutomationCubit — MVP da Fase 3. Captura mudanças de parâmetro via
// uma palette fixa de 4 controles dentro da própria tela e reproduz a
// sequência aplicando os mesmos setters do engine. Para evitar refactor
// global, a tela é auto-contida: não escuta nem afeta o EffectsCubit.
// Quando MIDI Learn / EffectsCubit global chegarem, dá pra unificar.

import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';

import '../../engine/engine.dart';

/// Identificador estável de cada parâmetro automatizável. O cubit
/// mantém um mapa de current values + setters; a UI consome via this enum.
enum AutoParam { gainLevel, distortionAmount, delayMix, reverbMix }

extension AutoParamX on AutoParam {
  String get label {
    switch (this) {
      case AutoParam.gainLevel:
        return 'Gain';
      case AutoParam.distortionAmount:
        return 'Distortion';
      case AutoParam.delayMix:
        return 'Delay mix';
      case AutoParam.reverbMix:
        return 'Reverb mix';
    }
  }
}

class AutomationEvent {
  const AutomationEvent({required this.tMs, required this.param, required this.value});
  final int tMs;
  final AutoParam param;
  final double value;
}

enum AutomationMode { idle, recording, playing }

class AutomationState {
  const AutomationState({
    required this.mode,
    required this.events,
    required this.values,
    required this.elapsedMs,
    this.errorMessage,
  });

  static const initial = AutomationState(
    mode: AutomationMode.idle,
    events: [],
    values: {
      AutoParam.gainLevel: 0.5,
      AutoParam.distortionAmount: 0.0,
      AutoParam.delayMix: 0.0,
      AutoParam.reverbMix: 0.0,
    },
    elapsedMs: 0,
  );

  final AutomationMode mode;
  final List<AutomationEvent> events;
  final Map<AutoParam, double> values;
  final int elapsedMs;
  final String? errorMessage;

  bool get hasRecording => events.isNotEmpty;
  int get totalDurationMs => events.isEmpty ? 0 : events.last.tMs;

  AutomationState copyWith({
    AutomationMode? mode,
    List<AutomationEvent>? events,
    Map<AutoParam, double>? values,
    int? elapsedMs,
    String? errorMessage,
    bool clearError = false,
  }) {
    return AutomationState(
      mode: mode ?? this.mode,
      events: events ?? this.events,
      values: values ?? this.values,
      elapsedMs: elapsedMs ?? this.elapsedMs,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}

class AutomationCubit extends Cubit<AutomationState> {
  AutomationCubit({ToneforgeEngine? engine})
      : _engine = engine ?? ToneforgeEngine(),
        super(AutomationState.initial) {
    // Garante que a engine recebe os defaults assim que o cubit nasce —
    // se o usuário só mexer no automation, queremos defaults consistentes.
    _applyAll(state.values);
  }

  final ToneforgeEngine _engine;
  Stopwatch? _stopwatch;
  Timer? _playTimer;
  Timer? _tickTimer;
  int _playCursor = 0;
  bool _ownsEngine = false;

  // ----- pipeline lifecycle -----

  Future<void> _ensurePipeline() async {
    if (_engine.isRunning) return;
    final result = _engine.start();
    if (result != 0) {
      emit(state.copyWith(errorMessage: 'Falha ao iniciar pipeline (code=$result)'));
      return;
    }
    _ownsEngine = true;
    _applyAll(state.values);
  }

  // ----- parâmetros -----

  void setParam(AutoParam param, double value) {
    final clamped = value.clamp(0.0, 1.0);
    final newValues = Map<AutoParam, double>.from(state.values);
    newValues[param] = clamped;
    _apply(param, clamped);

    if (state.mode == AutomationMode.recording && _stopwatch != null) {
      final t = _stopwatch!.elapsedMilliseconds;
      final newEvents = List<AutomationEvent>.from(state.events)
        ..add(AutomationEvent(tMs: t, param: param, value: clamped));
      emit(state.copyWith(values: newValues, events: newEvents));
    } else {
      emit(state.copyWith(values: newValues));
    }
  }

  void _apply(AutoParam param, double value) {
    switch (param) {
      case AutoParam.gainLevel:
        _engine.setGainLevel(value);
        _engine.gainEnabled = true;
        break;
      case AutoParam.distortionAmount:
        _engine.setDistortionAmount(value);
        _engine.setDistortionMix(value);
        _engine.distortionEnabled = value > 0;
        break;
      case AutoParam.delayMix:
        _engine.setDelayTimeMs(250); // tempo fixo razoável para demo
        _engine.setDelayFeedback(0.4);
        _engine.setDelayMix(value);
        _engine.delayEnabled = value > 0;
        break;
      case AutoParam.reverbMix:
        _engine.setReverbRoomSize(0.6);
        _engine.setReverbDamping(0.5);
        _engine.setReverbMix(value);
        _engine.reverbEnabled = value > 0;
        break;
    }
  }

  void _applyAll(Map<AutoParam, double> values) {
    for (final entry in values.entries) {
      _apply(entry.key, entry.value);
    }
  }

  // ----- transport -----

  Future<void> startRecording() async {
    await _ensurePipeline();
    if (state.errorMessage != null) return;

    _stopwatch = Stopwatch()..start();
    emit(state.copyWith(
      mode: AutomationMode.recording,
      events: const [],
      elapsedMs: 0,
      clearError: true,
    ));
    _startTicker();
  }

  Future<void> stopRecording() async {
    if (state.mode != AutomationMode.recording) return;
    _stopwatch?.stop();
    _stopTicker();
    emit(state.copyWith(mode: AutomationMode.idle));
  }

  Future<void> play() async {
    if (state.events.isEmpty) return;
    await _ensurePipeline();
    if (state.errorMessage != null) return;

    _playCursor = 0;
    _stopwatch = Stopwatch()..start();
    emit(state.copyWith(mode: AutomationMode.playing, elapsedMs: 0, clearError: true));
    _startTicker();
    _playTimer = Timer.periodic(const Duration(milliseconds: 16), (_) {
      final t = _stopwatch?.elapsedMilliseconds ?? 0;
      while (_playCursor < state.events.length && state.events[_playCursor].tMs <= t) {
        final ev = state.events[_playCursor++];
        final newValues = Map<AutoParam, double>.from(state.values);
        newValues[ev.param] = ev.value;
        _apply(ev.param, ev.value);
        emit(state.copyWith(values: newValues));
      }
      if (_playCursor >= state.events.length && t >= state.totalDurationMs) {
        stopPlayback();
      }
    });
  }

  void stopPlayback() {
    _playTimer?.cancel();
    _playTimer = null;
    _stopwatch?.stop();
    _stopTicker();
    if (state.mode == AutomationMode.playing) {
      emit(state.copyWith(mode: AutomationMode.idle));
    }
  }

  void clear() {
    stopPlayback();
    emit(state.copyWith(events: const [], elapsedMs: 0, mode: AutomationMode.idle));
  }

  void _startTicker() {
    _tickTimer?.cancel();
    _tickTimer = Timer.periodic(const Duration(milliseconds: 100), (_) {
      emit(state.copyWith(elapsedMs: _stopwatch?.elapsedMilliseconds ?? 0));
    });
  }

  void _stopTicker() {
    _tickTimer?.cancel();
    _tickTimer = null;
  }

  @override
  Future<void> close() {
    _playTimer?.cancel();
    _tickTimer?.cancel();
    _stopwatch?.stop();
    if (_ownsEngine && _engine.isRunning) _engine.stop();
    return super.close();
  }
}
