// EffectsCubit — orquestra o pipeline de áudio + estado dos 9 efeitos.
// Reuso intencional: o engine é singleton, então o mesmo Cubit pode rodar
// junto com Tuner/Metronome (eles não param o pipeline se outro feature
// estiver usando). Ver Fase 3.Effects.

import 'package:flutter_bloc/flutter_bloc.dart';

import '../../engine/engine.dart';
import 'effects_categories.dart';
import 'models.dart';

class EffectsState {
  const EffectsState({
    required this.pipelineRunning,
    required this.gain,
    required this.distortion,
    required this.delay,
    required this.reverb,
    required this.chorus,
    required this.flanger,
    required this.phaser,
    required this.eq,
    required this.compressor,
    required this.pitchShift,
    required this.order,
    this.errorMessage,
  });

  static const initial = EffectsState(
    pipelineRunning: false,
    gain: GainConfig(),
    distortion: DistortionConfig(),
    delay: DelayConfig(),
    reverb: ReverbConfig(),
    chorus: ModConfig(),
    flanger: ModConfig(),
    phaser: ModConfig(),
    eq: EqConfig(),
    compressor: CompressorConfig(),
    pitchShift: PitchShiftConfig(),
    order: kDefaultEffectOrder,
  );

  final bool pipelineRunning;
  final GainConfig gain;
  final DistortionConfig distortion;
  final DelayConfig delay;
  final ReverbConfig reverb;
  final ModConfig chorus;
  final ModConfig flanger;
  final ModConfig phaser;
  final EqConfig eq;
  final CompressorConfig compressor;
  final PitchShiftConfig pitchShift;
  final List<EffectKind> order;
  final String? errorMessage;

  EffectsState copyWith({
    bool? pipelineRunning,
    GainConfig? gain,
    DistortionConfig? distortion,
    DelayConfig? delay,
    ReverbConfig? reverb,
    ModConfig? chorus,
    ModConfig? flanger,
    ModConfig? phaser,
    EqConfig? eq,
    CompressorConfig? compressor,
    PitchShiftConfig? pitchShift,
    List<EffectKind>? order,
    String? errorMessage,
    bool clearError = false,
  }) {
    return EffectsState(
      pipelineRunning: pipelineRunning ?? this.pipelineRunning,
      gain: gain ?? this.gain,
      distortion: distortion ?? this.distortion,
      delay: delay ?? this.delay,
      reverb: reverb ?? this.reverb,
      chorus: chorus ?? this.chorus,
      flanger: flanger ?? this.flanger,
      phaser: phaser ?? this.phaser,
      eq: eq ?? this.eq,
      compressor: compressor ?? this.compressor,
      pitchShift: pitchShift ?? this.pitchShift,
      order: order ?? this.order,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}

class EffectsCubit extends Cubit<EffectsState> {
  EffectsCubit({ToneforgeEngine? engine})
      : _engine = engine ?? ToneforgeEngine(),
        super(EffectsState.initial) {
    _pushAll();
  }

  final ToneforgeEngine _engine;
  bool _ownsEngine = false;

  // ----- pipeline lifecycle -----

  Future<void> startPipeline() async {
    if (_engine.isRunning) {
      emit(state.copyWith(pipelineRunning: true, clearError: true));
      return;
    }
    final result = _engine.start();
    if (result != 0) {
      emit(state.copyWith(errorMessage: 'Falha ao iniciar pipeline (code=$result)'));
      return;
    }
    _ownsEngine = true;
    _pushAll();
    emit(state.copyWith(pipelineRunning: true, clearError: true));
  }

  Future<void> stopPipeline() async {
    if (_ownsEngine && _engine.isRunning) {
      _engine.stop();
      _ownsEngine = false;
    }
    emit(state.copyWith(pipelineRunning: false));
  }

  /// Aplica um snapshot completo (vindo de um preset, por exemplo) e
  /// sincroniza tudo com o engine. Usado pelo PresetManager.load().
  void applySnapshot(EffectsState snapshot) {
    emit(snapshot.copyWith(
      pipelineRunning: state.pipelineRunning,
      clearError: true,
    ));
    _pushAll();
  }

  /// Empurra todos os parâmetros + flags pra o native. Usado no init e
  /// quando o pipeline reconecta — o engine pode ter perdido estado.
  void _pushAll() {
    final s = state;
    _engine
      ..gainEnabled = s.gain.enabled
      ..setGainLevel(s.gain.level)
      ..distortionEnabled = s.distortion.enabled
      ..setDistortionAmount(s.distortion.amount)
      ..setDistortionType(s.distortion.type)
      ..setDistortionMix(s.distortion.mix)
      ..delayEnabled = s.delay.enabled
      ..setDelayTimeMs(s.delay.timeMs)
      ..setDelayFeedback(s.delay.feedback)
      ..setDelayMix(s.delay.mix)
      ..reverbEnabled = s.reverb.enabled
      ..setReverbRoomSize(s.reverb.roomSize)
      ..setReverbDamping(s.reverb.damping)
      ..setReverbType(s.reverb.type)
      ..setReverbMix(s.reverb.mix)
      ..chorusEnabled = s.chorus.enabled
      ..setChorusDepth(s.chorus.depth)
      ..setChorusRate(s.chorus.rate)
      ..setChorusMix(s.chorus.mix)
      ..flangerEnabled = s.flanger.enabled
      ..setFlangerDepth(s.flanger.depth)
      ..setFlangerRate(s.flanger.rate)
      ..setFlangerFeedback(s.flanger.feedback)
      ..setFlangerMix(s.flanger.mix)
      ..phaserEnabled = s.phaser.enabled
      ..setPhaserDepth(s.phaser.depth)
      ..setPhaserRate(s.phaser.rate)
      ..setPhaserFeedback(s.phaser.feedback)
      ..setPhaserMix(s.phaser.mix)
      ..eqEnabled = s.eq.enabled
      ..setEqLow(s.eq.low)
      ..setEqMid(s.eq.mid)
      ..setEqHigh(s.eq.high)
      ..setEqMix(s.eq.mix)
      ..compressorEnabled = s.compressor.enabled
      ..setCompressorThreshold(s.compressor.thresholdDb)
      ..setCompressorRatio(s.compressor.ratio)
      ..setCompressorAttack(s.compressor.attackMs)
      ..setCompressorRelease(s.compressor.releaseMs)
      ..setCompressorMix(s.compressor.mix)
      ..pitchShiftEnabled = s.pitchShift.enabled
      ..setPitchShiftSemitones(s.pitchShift.semitones)
      ..setPitchShiftMix(s.pitchShift.mix);
    _pushOrder(s.order);
  }

  void _pushOrder(List<EffectKind> order) {
    _engine.setEffectOrder(order.map((e) => e.engineKey).toList(growable: false));
  }

  /// Move o efeito em [oldIndex] para [newIndex] (semântica idêntica à do
  /// `ReorderableListView.onReorder`: quando oldIndex < newIndex, a lista
  /// removeu o item antes de reinserir, então decrementamos newIndex).
  /// Em seguida empurra a ordem completa pra engine.
  void reorderEffects(int oldIndex, int newIndex) {
    final current = state.order;
    if (oldIndex < 0 || oldIndex >= current.length) return;
    var target = newIndex;
    if (oldIndex < target) target -= 1;
    if (target < 0) target = 0;
    if (target > current.length - 1) target = current.length - 1;
    if (target == oldIndex) return;
    final next = List<EffectKind>.of(current);
    final moved = next.removeAt(oldIndex);
    next.insert(target, moved);
    _pushOrder(next);
    emit(state.copyWith(order: next));
  }

  /// Restaura a ordem canônica que o engine tem hardcoded.
  void resetOrder() {
    _pushOrder(kDefaultEffectOrder);
    emit(state.copyWith(order: kDefaultEffectOrder));
  }

  // ----- Gain -----
  void setGainEnabled(bool v) {
    _engine.gainEnabled = v;
    emit(state.copyWith(gain: state.gain.copyWith(enabled: v)));
  }

  void setGainLevel(double v) {
    _engine.setGainLevel(v);
    emit(state.copyWith(gain: state.gain.copyWith(level: v)));
  }

  // ----- Distortion -----
  void setDistortionEnabled(bool v) {
    _engine.distortionEnabled = v;
    emit(state.copyWith(distortion: state.distortion.copyWith(enabled: v)));
  }

  void setDistortionAmount(double v) {
    _engine.setDistortionAmount(v);
    emit(state.copyWith(distortion: state.distortion.copyWith(amount: v)));
  }

  void setDistortionType(int t) {
    _engine.setDistortionType(t);
    emit(state.copyWith(distortion: state.distortion.copyWith(type: t)));
  }

  void setDistortionMix(double v) {
    _engine.setDistortionMix(v);
    emit(state.copyWith(distortion: state.distortion.copyWith(mix: v)));
  }

  // ----- Delay -----
  void setDelayEnabled(bool v) {
    _engine.delayEnabled = v;
    emit(state.copyWith(delay: state.delay.copyWith(enabled: v)));
  }

  void setDelayTimeMs(double v) {
    _engine.setDelayTimeMs(v);
    emit(state.copyWith(delay: state.delay.copyWith(timeMs: v)));
  }

  void setDelayFeedback(double v) {
    _engine.setDelayFeedback(v);
    emit(state.copyWith(delay: state.delay.copyWith(feedback: v)));
  }

  void setDelayMix(double v) {
    _engine.setDelayMix(v);
    emit(state.copyWith(delay: state.delay.copyWith(mix: v)));
  }

  // ----- Reverb -----
  void setReverbEnabled(bool v) {
    _engine.reverbEnabled = v;
    emit(state.copyWith(reverb: state.reverb.copyWith(enabled: v)));
  }

  void setReverbRoomSize(double v) {
    _engine.setReverbRoomSize(v);
    emit(state.copyWith(reverb: state.reverb.copyWith(roomSize: v)));
  }

  void setReverbDamping(double v) {
    _engine.setReverbDamping(v);
    emit(state.copyWith(reverb: state.reverb.copyWith(damping: v)));
  }

  void setReverbType(int t) {
    _engine.setReverbType(t);
    emit(state.copyWith(reverb: state.reverb.copyWith(type: t)));
  }

  void setReverbMix(double v) {
    _engine.setReverbMix(v);
    emit(state.copyWith(reverb: state.reverb.copyWith(mix: v)));
  }

  // ----- Chorus -----
  void setChorusEnabled(bool v) {
    _engine.chorusEnabled = v;
    emit(state.copyWith(chorus: state.chorus.copyWith(enabled: v)));
  }

  void setChorusDepth(double v) {
    _engine.setChorusDepth(v);
    emit(state.copyWith(chorus: state.chorus.copyWith(depth: v)));
  }

  void setChorusRate(double v) {
    _engine.setChorusRate(v);
    emit(state.copyWith(chorus: state.chorus.copyWith(rate: v)));
  }

  void setChorusMix(double v) {
    _engine.setChorusMix(v);
    emit(state.copyWith(chorus: state.chorus.copyWith(mix: v)));
  }

  // ----- Flanger -----
  void setFlangerEnabled(bool v) {
    _engine.flangerEnabled = v;
    emit(state.copyWith(flanger: state.flanger.copyWith(enabled: v)));
  }

  void setFlangerDepth(double v) {
    _engine.setFlangerDepth(v);
    emit(state.copyWith(flanger: state.flanger.copyWith(depth: v)));
  }

  void setFlangerRate(double v) {
    _engine.setFlangerRate(v);
    emit(state.copyWith(flanger: state.flanger.copyWith(rate: v)));
  }

  void setFlangerFeedback(double v) {
    _engine.setFlangerFeedback(v);
    emit(state.copyWith(flanger: state.flanger.copyWith(feedback: v)));
  }

  void setFlangerMix(double v) {
    _engine.setFlangerMix(v);
    emit(state.copyWith(flanger: state.flanger.copyWith(mix: v)));
  }

  // ----- Phaser -----
  void setPhaserEnabled(bool v) {
    _engine.phaserEnabled = v;
    emit(state.copyWith(phaser: state.phaser.copyWith(enabled: v)));
  }

  void setPhaserDepth(double v) {
    _engine.setPhaserDepth(v);
    emit(state.copyWith(phaser: state.phaser.copyWith(depth: v)));
  }

  void setPhaserRate(double v) {
    _engine.setPhaserRate(v);
    emit(state.copyWith(phaser: state.phaser.copyWith(rate: v)));
  }

  void setPhaserFeedback(double v) {
    _engine.setPhaserFeedback(v);
    emit(state.copyWith(phaser: state.phaser.copyWith(feedback: v)));
  }

  void setPhaserMix(double v) {
    _engine.setPhaserMix(v);
    emit(state.copyWith(phaser: state.phaser.copyWith(mix: v)));
  }

  // ----- EQ -----
  void setEqEnabled(bool v) {
    _engine.eqEnabled = v;
    emit(state.copyWith(eq: state.eq.copyWith(enabled: v)));
  }

  void setEqLow(double v) {
    _engine.setEqLow(v);
    emit(state.copyWith(eq: state.eq.copyWith(low: v)));
  }

  void setEqMid(double v) {
    _engine.setEqMid(v);
    emit(state.copyWith(eq: state.eq.copyWith(mid: v)));
  }

  void setEqHigh(double v) {
    _engine.setEqHigh(v);
    emit(state.copyWith(eq: state.eq.copyWith(high: v)));
  }

  void setEqMix(double v) {
    _engine.setEqMix(v);
    emit(state.copyWith(eq: state.eq.copyWith(mix: v)));
  }

  // ----- Compressor -----
  void setCompressorEnabled(bool v) {
    _engine.compressorEnabled = v;
    emit(state.copyWith(compressor: state.compressor.copyWith(enabled: v)));
  }

  void setCompressorThreshold(double v) {
    _engine.setCompressorThreshold(v);
    emit(state.copyWith(compressor: state.compressor.copyWith(thresholdDb: v)));
  }

  void setCompressorRatio(double v) {
    _engine.setCompressorRatio(v);
    emit(state.copyWith(compressor: state.compressor.copyWith(ratio: v)));
  }

  void setCompressorAttack(double v) {
    _engine.setCompressorAttack(v);
    emit(state.copyWith(compressor: state.compressor.copyWith(attackMs: v)));
  }

  void setCompressorRelease(double v) {
    _engine.setCompressorRelease(v);
    emit(state.copyWith(compressor: state.compressor.copyWith(releaseMs: v)));
  }

  void setCompressorMix(double v) {
    _engine.setCompressorMix(v);
    emit(state.copyWith(compressor: state.compressor.copyWith(mix: v)));
  }

  // ----- Pitch Shift (TFR-10) -----
  void setPitchShiftEnabled(bool v) {
    _engine.pitchShiftEnabled = v;
    emit(state.copyWith(pitchShift: state.pitchShift.copyWith(enabled: v)));
  }

  void setPitchShiftSemitones(double v) {
    _engine.setPitchShiftSemitones(v);
    emit(state.copyWith(pitchShift: state.pitchShift.copyWith(semitones: v)));
  }

  void setPitchShiftMix(double v) {
    _engine.setPitchShiftMix(v);
    emit(state.copyWith(pitchShift: state.pitchShift.copyWith(mix: v)));
  }

  @override
  Future<void> close() {
    if (_ownsEngine && _engine.isRunning) _engine.stop();
    return super.close();
  }
}
