// LooperCubit — MVP + slicing (TFR-48). Gerencia gravação/reprodução de uma
// única loop com snapshot do waveform amostrado a 5Hz para visualização, mais
// overlay opcional de slicing (criar pontos tocando no waveform, disparar
// segmentos via pads). Multi-track, reverse, pitch shift, BPM sync etc. ficam
// para iterações futuras (o engine C++ já suporta — só falta o lado UI).
// Ver Fase 3.Looper.

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
    required this.slicingEnabled,
    required this.slicePoints,
    required this.activeSliceIndex,
    this.errorMessage,
  });

  static const initial = LooperState(
    mode: LooperMode.idle,
    lengthFrames: 0,
    positionFrames: 0,
    sampleRate: 0,
    waveform: [],
    slicingEnabled: false,
    slicePoints: [],
    activeSliceIndex: -1,
  );

  final LooperMode mode;
  final int lengthFrames;
  final int positionFrames;
  final int sampleRate;
  final List<double> waveform;
  final String? errorMessage;

  /// Slicing ligado/desligado pelo usuário (overlay opcional no waveform).
  final bool slicingEnabled;

  /// Pontos de slice em frames (ordenados crescentes). O frame 0 é implícito
  /// como "início do loop" para fins de disparo do slice 0.
  final List<int> slicePoints;

  /// Índice do slice atualmente disparado pelos pads (-1 = nenhum).
  final int activeSliceIndex;

  bool get hasContent => lengthFrames > 0;

  double get lengthSeconds =>
      sampleRate > 0 ? lengthFrames / sampleRate : 0.0;
  double get positionSeconds =>
      sampleRate > 0 ? positionFrames / sampleRate : 0.0;
  double get progress =>
      lengthFrames > 0 ? (positionFrames / lengthFrames).clamp(0.0, 1.0) : 0.0;

  /// Fronteiras efetivas dos segmentos, já incluindo o 0 implícito no início
  /// e lengthFrames no fim. Ex.: slicePoints=[2000,5000], lengthFrames=8000
  /// → [0,2000,5000,8000]. Sempre retorna ao menos [0, lengthFrames].
  List<int> get sliceBoundaries {
    if (lengthFrames <= 0) return const [];
    final sorted = [...slicePoints]..sort();
    final out = <int>[0];
    for (final p in sorted) {
      if (p > 0 && p < lengthFrames && p != out.last) {
        out.add(p);
      }
    }
    out.add(lengthFrames);
    return out;
  }

  /// Número de slices efetivos (boundaries.length - 1).
  int get numSlices {
    final b = sliceBoundaries;
    return b.length <= 1 ? 0 : b.length - 1;
  }

  LooperState copyWith({
    LooperMode? mode,
    int? lengthFrames,
    int? positionFrames,
    int? sampleRate,
    List<double>? waveform,
    bool? slicingEnabled,
    List<int>? slicePoints,
    int? activeSliceIndex,
    String? errorMessage,
    bool clearError = false,
  }) {
    return LooperState(
      mode: mode ?? this.mode,
      lengthFrames: lengthFrames ?? this.lengthFrames,
      positionFrames: positionFrames ?? this.positionFrames,
      sampleRate: sampleRate ?? this.sampleRate,
      waveform: waveform ?? this.waveform,
      slicingEnabled: slicingEnabled ?? this.slicingEnabled,
      slicePoints: slicePoints ?? this.slicePoints,
      activeSliceIndex: activeSliceIndex ?? this.activeSliceIndex,
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

  /// Mínima distância entre dois slice points, em proporção do loop total.
  /// Evita que taps muito próximos criem segmentos impossíveis de apertar.
  static const _minSliceSpacingRatio = 0.02;

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
    emit(state.copyWith(mode: LooperMode.idle, activeSliceIndex: -1));
  }

  Future<void> clear() async {
    _engine.clearLooper();
    _engine.setLooperSlicingEnabled(false);
    _stopPositionPolling();
    _stopWaveformPolling();
    if (_ownsEngine && _engine.isRunning) {
      _engine.stop();
      _ownsEngine = false;
    }
    emit(LooperState.initial);
  }

  // ----- slicing -----

  /// Liga ou desliga o overlay de slicing. Quando ligado pela primeira vez com
  /// a lista de pontos vazia, o engine cria 8 slices automáticos — refletimos
  /// esse comportamento na UI gerando a mesma distribuição.
  void setSlicingEnabled(bool enabled) {
    if (!state.hasContent) return;
    _engine.setLooperSlicingEnabled(enabled);
    List<int> points = state.slicePoints;
    if (enabled && points.isEmpty && state.lengthFrames > 0) {
      // 8 slices igualmente distribuídos, sem incluir o 0 (implícito).
      const n = 8;
      final step = state.lengthFrames / n;
      points = List<int>.generate(n - 1, (i) => ((i + 1) * step).round());
      _engine.setLooperSlicePoints(points);
    }
    emit(state.copyWith(
      slicingEnabled: enabled,
      slicePoints: points,
      activeSliceIndex: enabled ? state.activeSliceIndex : -1,
    ));
  }

  /// Adiciona um ponto de slice no frame [frame]. Ignora se a distância até
  /// o ponto mais próximo (ou até 0 / lengthFrames) for menor que o mínimo.
  void addSlicePoint(int frame) {
    if (!state.hasContent) return;
    final length = state.lengthFrames;
    if (frame <= 0 || frame >= length) return;

    final minGap = (length * _minSliceSpacingRatio).round().clamp(1, length);
    if (frame < minGap || frame > length - minGap) return;

    for (final p in state.slicePoints) {
      if ((p - frame).abs() < minGap) return;
    }

    final next = [...state.slicePoints, frame]..sort();
    _engine.setLooperSlicePoints(next);
    emit(state.copyWith(slicePoints: next));
  }

  /// Remove o ponto de slice mais próximo de [frame] dentro de uma janela de
  /// tolerância proporcional ao comprimento do loop. Usado pelo long-press.
  void removeSlicePointNear(int frame) {
    if (state.slicePoints.isEmpty) return;
    final tolerance = (state.lengthFrames * 0.03).round().clamp(1, 1 << 30);
    var bestIdx = -1;
    var bestDist = tolerance;
    for (var i = 0; i < state.slicePoints.length; i++) {
      final d = (state.slicePoints[i] - frame).abs();
      if (d <= bestDist) {
        bestDist = d;
        bestIdx = i;
      }
    }
    if (bestIdx < 0) return;
    final next = [...state.slicePoints]..removeAt(bestIdx);
    _engine.setLooperSlicePoints(next);
    emit(state.copyWith(slicePoints: next));
  }

  void clearSlicePoints() {
    if (state.slicePoints.isEmpty) return;
    _engine.setLooperSlicePoints(const []);
    emit(state.copyWith(slicePoints: const [], activeSliceIndex: -1));
  }

  /// Dispara o slice de índice [index]. O dispatch move o looper para o frame
  /// de início do slice e garante que a playback esteja tocando. O looper
  /// vai continuar tocando a partir dali — o usuário pode tocar outro pad
  /// para saltar, ou parar via transporte.
  Future<void> triggerSlice(int index) async {
    final boundaries = state.sliceBoundaries;
    if (index < 0 || index >= boundaries.length - 1) return;
    if (!state.hasContent) return;

    await _ensurePipeline();
    if (state.errorMessage != null) return;

    _engine.setLooperPosition(boundaries[index]);
    if (!_engine.isLooperPlaying) {
      _engine.startLooperPlayback();
      _startPositionPolling();
      _startWaveformPolling();
      emit(state.copyWith(
        mode: LooperMode.playing,
        activeSliceIndex: index,
        positionFrames: boundaries[index],
        clearError: true,
      ));
    } else {
      emit(state.copyWith(
        activeSliceIndex: index,
        positionFrames: boundaries[index],
      ));
    }
  }

  // ----- pollers -----

  void _startPositionPolling() {
    _positionTimer?.cancel();
    _positionTimer = Timer.periodic(_positionPollInterval, (_) {
      if (isClosed) return;
      // Em recording, length cresce a cada callback; em playback, length é
      // fixo e a position dá voltas. Refletimos os dois casos uniformemente.
      final pos = _engine.looperPosition;
      final len = _engine.looperLength;
      // Atualiza activeSliceIndex enquanto toca, para que o pad correspondente
      // fique destacado.
      var active = state.activeSliceIndex;
      if (state.mode == LooperMode.playing && state.slicingEnabled) {
        final boundaries = state.sliceBoundaries;
        if (boundaries.length >= 2) {
          for (var i = 0; i < boundaries.length - 1; i++) {
            if (pos >= boundaries[i] && pos < boundaries[i + 1]) {
              active = i;
              break;
            }
          }
        }
      }
      emit(state.copyWith(
        lengthFrames: len,
        positionFrames: pos,
        activeSliceIndex: active,
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
    _waveformTimer = Timer.periodic(_waveformRefreshInterval, (_) {
      if (isClosed) return;
      _refreshWaveform();
    });
  }

  void _stopWaveformPolling() {
    _waveformTimer?.cancel();
    _waveformTimer = null;
  }

  void _refreshWaveform() {
    final samples = _engine.snapshotLooperMix(targetPoints: 240);
    if (isClosed) return;
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
