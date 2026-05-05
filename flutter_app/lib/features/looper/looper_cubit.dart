// LooperCubit — multi-track + slicing. TFR-9 expandiu pra suportar até
// `looperMaxTracks` slots (8 no engine atual). Cada track tem estado
// independente (active/length/position/volume/mute/solo/waveform). A
// gravação cai no `armedTrack` (ou no primeiro slot livre via auto-pick),
// e a playback global (`mode=playing`) toca todas as tracks ativas mixando
// no audio callback nativo. O slicing continua operando sobre o track
// armado (último gravado por padrão), pra não quebrar a UI da TFR-48.

import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';

import '../../engine/engine.dart';

enum LooperMode { idle, recording, playing }

class TrackState {
  const TrackState({
    required this.index,
    required this.active,
    required this.lengthFrames,
    required this.positionFrames,
    required this.volume,
    required this.muted,
    required this.soloed,
    required this.waveform,
  });

  final int index;
  final bool active;
  final int lengthFrames;
  final int positionFrames;
  final double volume;
  final bool muted;
  final bool soloed;
  final List<double> waveform;

  bool get hasContent => active && lengthFrames > 0;

  double progress(int sampleRate) =>
      lengthFrames > 0 ? (positionFrames / lengthFrames).clamp(0.0, 1.0) : 0.0;

  TrackState copyWith({
    bool? active,
    int? lengthFrames,
    int? positionFrames,
    double? volume,
    bool? muted,
    bool? soloed,
    List<double>? waveform,
  }) {
    return TrackState(
      index: index,
      active: active ?? this.active,
      lengthFrames: lengthFrames ?? this.lengthFrames,
      positionFrames: positionFrames ?? this.positionFrames,
      volume: volume ?? this.volume,
      muted: muted ?? this.muted,
      soloed: soloed ?? this.soloed,
      waveform: waveform ?? this.waveform,
    );
  }

  static TrackState empty(int index) => TrackState(
        index: index,
        active: false,
        lengthFrames: 0,
        positionFrames: 0,
        volume: 1.0,
        muted: false,
        soloed: false,
        waveform: const [],
      );
}

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
    required this.tracks,
    required this.armedTrack,
    required this.maxTracks,
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
    tracks: [],
    armedTrack: -1,
    maxTracks: 0,
  );

  final LooperMode mode;
  final int lengthFrames;
  final int positionFrames;
  final int sampleRate;
  final List<double> waveform;
  final String? errorMessage;

  /// Slicing ligado/desligado pelo usuário (overlay opcional no waveform).
  /// Opera sobre o track armado / último gravado.
  final bool slicingEnabled;
  final List<int> slicePoints;
  final int activeSliceIndex;

  /// Slots de track. Sempre tem [maxTracks] entradas após [LooperCubit] ser
  /// inicializado (lazy: a primeira chamada que fala com o engine descobre).
  final List<TrackState> tracks;

  /// Slot armado para a próxima gravação. -1 = engine auto-pick (primeiro
  /// slot inactive, ou 0 se todos cheios).
  final int armedTrack;

  /// Total de slots suportados pelo engine. 0 antes da inicialização.
  final int maxTracks;

  bool get hasContent => lengthFrames > 0 || tracks.any((t) => t.hasContent);

  /// True se algum track estiver com solo ligado — usado pela UI pra
  /// aplicar visualmente o "outros mutados".
  bool get anySoloed => tracks.any((t) => t.active && t.soloed);

  double get lengthSeconds =>
      sampleRate > 0 ? lengthFrames / sampleRate : 0.0;
  double get positionSeconds =>
      sampleRate > 0 ? positionFrames / sampleRate : 0.0;
  double get progress =>
      lengthFrames > 0 ? (positionFrames / lengthFrames).clamp(0.0, 1.0) : 0.0;

  /// Fronteiras efetivas dos segmentos, já incluindo o 0 implícito no início
  /// e lengthFrames no fim. Sempre retorna ao menos [0, lengthFrames].
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

  int get numSlices {
    final b = sliceBoundaries;
    return b.length <= 1 ? 0 : b.length - 1;
  }

  /// Próximo slot vazio na grade (para indicar onde a próxima gravação cai
  /// quando nada está armado). Retorna -1 se todos cheios.
  int get nextEmptyTrack {
    for (final t in tracks) {
      if (!t.active) return t.index;
    }
    return -1;
  }

  /// Track destino efetivo da próxima gravação: armado, ou auto-pick.
  int get effectiveTargetTrack {
    if (armedTrack >= 0 && armedTrack < tracks.length) return armedTrack;
    final next = nextEmptyTrack;
    return next >= 0 ? next : 0;
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
    List<TrackState>? tracks,
    int? armedTrack,
    int? maxTracks,
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
      tracks: tracks ?? this.tracks,
      armedTrack: armedTrack ?? this.armedTrack,
      maxTracks: maxTracks ?? this.maxTracks,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}

class LooperCubit extends Cubit<LooperState> {
  LooperCubit({ToneforgeEngine? engine})
      : _engine = engine ?? ToneforgeEngine(),
        super(LooperState.initial) {
    _initTrackSlots();
  }

  static const _positionPollInterval = Duration(milliseconds: 50);
  static const _waveformRefreshInterval = Duration(milliseconds: 200);

  /// Mínima distância entre dois slice points, em proporção do loop total.
  static const _minSliceSpacingRatio = 0.02;

  final ToneforgeEngine _engine;
  Timer? _positionTimer;
  Timer? _waveformTimer;
  bool _ownsEngine = false;

  void _initTrackSlots() {
    final n = _engine.looperMaxTracks;
    if (n <= 0) return;
    emit(state.copyWith(
      maxTracks: n,
      tracks: List.generate(n, TrackState.empty),
    ));
  }

  Future<void> _ensurePipeline() async {
    if (_engine.isRunning) return;
    final result = _engine.start();
    if (result != 0) {
      emit(state.copyWith(errorMessage: 'Falha ao iniciar pipeline (code=$result)'));
      return;
    }
    _ownsEngine = true;
  }

  // ----- transport -----

  Future<void> startRecording() async {
    await _ensurePipeline();
    if (state.errorMessage != null) return;

    // Empurra o armed atual para o engine (o getter `armedTrack` no engine
    // continua sendo a fonte da verdade, mas em caso de race a UI também
    // pode forçar isso explícitamente aqui).
    if (state.armedTrack >= 0) {
      _engine.looperArmedTrack = state.armedTrack;
    }

    _engine.startLooperRecording();
    final target = _engine.looperCurrentTrack;
    emit(state.copyWith(
      mode: LooperMode.recording,
      sampleRate: _engine.sampleRate > 0 ? _engine.sampleRate : 48000,
      clearError: true,
    ));
    _startPositionPolling();
    // Também garante que o slot de destino aparece como ativo na UI durante a
    // gravação (mesmo antes do primeiro poll).
    _patchTrack(target, (t) => t.copyWith(active: true, lengthFrames: 0));
  }

  Future<void> stopRecording() async {
    if (state.mode != LooperMode.recording) return;
    _engine.stopLooperRecording();
    final length = _engine.looperLength;
    final recordedTrack = _engine.looperCurrentTrack;
    emit(state.copyWith(
      mode: LooperMode.idle,
      lengthFrames: length,
      positionFrames: 0,
      armedTrack: -1, // engine resetou armed; reflete na UI.
    ));
    _refreshTrackWaveform(recordedTrack);
    _refreshWaveform();
  }

  Future<void> startPlayback() async {
    if (!state.hasContent) return;
    await _ensurePipeline();
    if (state.errorMessage != null) return;

    _engine.startLooperPlayback();
    if (!_engine.isLooperPlaying) {
      // Engine recusou — provavelmente nenhum track ativo.
      emit(state.copyWith(errorMessage: 'Nada para tocar'));
      return;
    }
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
    final n = state.maxTracks;
    emit(LooperState.initial.copyWith(
      maxTracks: n,
      tracks: List.generate(n, TrackState.empty),
    ));
  }

  // ----- multi-track -----

  void armTrack(int trackIndex) {
    if (trackIndex < -1 || trackIndex >= state.maxTracks) return;
    _engine.looperArmedTrack = trackIndex;
    emit(state.copyWith(armedTrack: trackIndex));
  }

  void setTrackVolume(int trackIndex, double volume) {
    if (trackIndex < 0 || trackIndex >= state.maxTracks) return;
    final v = volume.clamp(0.0, 2.0);
    _engine.setLooperTrackVolume(trackIndex, v);
    _patchTrack(trackIndex, (t) => t.copyWith(volume: v));
  }

  void setTrackMuted(int trackIndex, bool muted) {
    if (trackIndex < 0 || trackIndex >= state.maxTracks) return;
    _engine.setLooperTrackMuted(trackIndex, muted);
    _patchTrack(trackIndex, (t) => t.copyWith(muted: muted));
  }

  void setTrackSoloed(int trackIndex, bool soloed) {
    if (trackIndex < 0 || trackIndex >= state.maxTracks) return;
    _engine.setLooperTrackSoloed(trackIndex, soloed);
    _patchTrack(trackIndex, (t) => t.copyWith(soloed: soloed));
  }

  Future<void> clearTrack(int trackIndex) async {
    if (trackIndex < 0 || trackIndex >= state.maxTracks) return;
    _engine.removeLooperTrack(trackIndex);
    _patchTrack(trackIndex, (_) => TrackState.empty(trackIndex));
    // Se o engine parou a playback porque sobrou nada, sincroniza a UI.
    if (state.mode == LooperMode.playing && !_engine.isLooperPlaying) {
      _stopWaveformPolling();
      emit(state.copyWith(mode: LooperMode.idle, activeSliceIndex: -1));
    }
    _refreshWaveform();
  }

  // ----- slicing -----

  void setSlicingEnabled(bool enabled) {
    if (!state.hasContent) return;
    _engine.setLooperSlicingEnabled(enabled);
    List<int> points = state.slicePoints;
    if (enabled && points.isEmpty && state.lengthFrames > 0) {
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

      final pos = _engine.looperPosition;
      final len = _engine.looperLength;

      // Atualiza estado de cada track. Em recording, a track destino tem
      // length crescendo; nas outras, position avança em playback.
      var changed = false;
      final updated = <TrackState>[];
      for (final t in state.tracks) {
        final i = t.index;
        final active = _engine.isLooperTrackActive(i);
        final tLen = _engine.looperTrackLength(i);
        final tPos = _engine.looperTrackPosition(i);
        final tVol = _engine.looperTrackVolume(i);
        final tMuted = _engine.isLooperTrackMuted(i);
        final tSoloed = _engine.isLooperTrackSoloed(i);
        if (t.active != active ||
            t.lengthFrames != tLen ||
            t.positionFrames != tPos ||
            t.volume != tVol ||
            t.muted != tMuted ||
            t.soloed != tSoloed) {
          changed = true;
          updated.add(t.copyWith(
            active: active,
            lengthFrames: tLen,
            positionFrames: tPos,
            volume: tVol,
            muted: tMuted,
            soloed: tSoloed,
          ));
        } else {
          updated.add(t);
        }
      }

      // Slicing active index tracking (apenas em playback global).
      var activeSlice = state.activeSliceIndex;
      if (state.mode == LooperMode.playing && state.slicingEnabled) {
        final boundaries = state.sliceBoundaries;
        if (boundaries.length >= 2) {
          for (var i = 0; i < boundaries.length - 1; i++) {
            if (pos >= boundaries[i] && pos < boundaries[i + 1]) {
              activeSlice = i;
              break;
            }
          }
        }
      }

      emit(state.copyWith(
        lengthFrames: len,
        positionFrames: pos,
        activeSliceIndex: activeSlice,
        tracks: changed ? updated : null,
      ));

      // Auto-stop quando o native terminou recording por outro motivo.
      if (state.mode == LooperMode.recording && !_engine.isLooperRecording) {
        emit(state.copyWith(mode: LooperMode.idle));
        _stopPositionPolling();
        final recorded = _engine.looperCurrentTrack;
        _refreshTrackWaveform(recorded);
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

  /// Snapshot do waveform de um track. Chamado em transição (parar gravação,
  /// load de WAV) — não em loop, porque alocar 30s × 4 bytes a 48 kHz é caro
  /// (~5,7 MB por chamada). Mantém o thumbnail no estado pra a UI repintar.
  void _refreshTrackWaveform(int trackIndex) {
    if (trackIndex < 0 || trackIndex >= state.maxTracks) return;
    final samples = _engine.snapshotLooperTrack(trackIndex, targetPoints: 120);
    if (isClosed) return;
    _patchTrack(trackIndex, (t) => t.copyWith(waveform: samples ?? const []));
  }

  void _patchTrack(int trackIndex, TrackState Function(TrackState) update) {
    if (trackIndex < 0 || trackIndex >= state.tracks.length) return;
    final next = [...state.tracks];
    next[trackIndex] = update(next[trackIndex]);
    emit(state.copyWith(tracks: next));
  }

  /// Carrega samples Float32 num slot específico SEM apagar os outros.
  /// Usado pelo loop_library na opção "carregar em slot N".
  void loadIntoTrack(int trackIndex, dynamic /* Float32List */ samples) {
    if (trackIndex < 0 || trackIndex >= state.maxTracks) return;
    _engine.loadLooperTrackFromFloats(trackIndex, samples);
    _patchTrack(
      trackIndex,
      (t) => t.copyWith(
        active: true,
        lengthFrames: _engine.looperTrackLength(trackIndex),
        positionFrames: 0,
      ),
    );
    _refreshTrackWaveform(trackIndex);
    _refreshWaveform();
    emit(state.copyWith(
      sampleRate: _engine.sampleRate > 0 ? _engine.sampleRate : state.sampleRate,
      lengthFrames: _engine.looperLength,
    ));
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
