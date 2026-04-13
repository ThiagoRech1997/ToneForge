// Modelos imutáveis dos parâmetros dos 9 efeitos. Faixas e defaults
// reproduzem o EffectParameters.java + arrays.xml do app legado.
// Ver Fase 3.Effects.

class GainConfig {
  const GainConfig({this.enabled = true, this.level = 0.5});
  final bool enabled;
  final double level;
  GainConfig copyWith({bool? enabled, double? level}) =>
      GainConfig(enabled: enabled ?? this.enabled, level: level ?? this.level);
}

class DistortionConfig {
  const DistortionConfig({
    this.enabled = false,
    this.amount = 0.0,
    this.type = 0,
    this.mix = 0.0,
  });
  final bool enabled;
  final double amount;
  final int type; // 0=Soft Clip, 1=Hard Clip, 2=Fuzz, 3=Overdrive
  final double mix;
  DistortionConfig copyWith({bool? enabled, double? amount, int? type, double? mix}) =>
      DistortionConfig(
        enabled: enabled ?? this.enabled,
        amount: amount ?? this.amount,
        type: type ?? this.type,
        mix: mix ?? this.mix,
      );
  static const types = ['Soft Clip', 'Hard Clip', 'Fuzz', 'Overdrive'];
}

class DelayConfig {
  const DelayConfig({
    this.enabled = false,
    this.timeMs = 0.0,
    this.feedback = 0.0,
    this.mix = 0.0,
  });
  final bool enabled;
  final double timeMs; // 0..1000ms
  final double feedback;
  final double mix;
  DelayConfig copyWith({bool? enabled, double? timeMs, double? feedback, double? mix}) =>
      DelayConfig(
        enabled: enabled ?? this.enabled,
        timeMs: timeMs ?? this.timeMs,
        feedback: feedback ?? this.feedback,
        mix: mix ?? this.mix,
      );
}

class ReverbConfig {
  const ReverbConfig({
    this.enabled = false,
    this.roomSize = 0.0,
    this.damping = 0.0,
    this.type = 0,
    this.mix = 0.0,
  });
  final bool enabled;
  final double roomSize;
  final double damping;
  final int type; // 0=Hall, 1=Plate, 2=Spring
  final double mix;
  ReverbConfig copyWith({
    bool? enabled,
    double? roomSize,
    double? damping,
    int? type,
    double? mix,
  }) =>
      ReverbConfig(
        enabled: enabled ?? this.enabled,
        roomSize: roomSize ?? this.roomSize,
        damping: damping ?? this.damping,
        type: type ?? this.type,
        mix: mix ?? this.mix,
      );
  static const types = ['Hall', 'Plate', 'Spring'];
}

class ModConfig {
  // Estrutura compartilhada por Chorus, Flanger e Phaser (modulação simples).
  // Flanger e Phaser têm feedback; Chorus não.
  const ModConfig({
    this.enabled = false,
    this.depth = 0.0,
    this.rate = 0.0,
    this.feedback = 0.0,
    this.mix = 0.0,
  });
  final bool enabled;
  final double depth;
  final double rate; // Hz, 0..20
  final double feedback;
  final double mix;
  ModConfig copyWith({
    bool? enabled,
    double? depth,
    double? rate,
    double? feedback,
    double? mix,
  }) =>
      ModConfig(
        enabled: enabled ?? this.enabled,
        depth: depth ?? this.depth,
        rate: rate ?? this.rate,
        feedback: feedback ?? this.feedback,
        mix: mix ?? this.mix,
      );
}

class EqConfig {
  const EqConfig({
    this.enabled = false,
    this.low = 0.0,
    this.mid = 0.0,
    this.high = 0.0,
    this.mix = 0.0,
  });
  final bool enabled;
  final double low; // -12..+12 dB
  final double mid;
  final double high;
  final double mix;
  EqConfig copyWith({bool? enabled, double? low, double? mid, double? high, double? mix}) =>
      EqConfig(
        enabled: enabled ?? this.enabled,
        low: low ?? this.low,
        mid: mid ?? this.mid,
        high: high ?? this.high,
        mix: mix ?? this.mix,
      );
}

class CompressorConfig {
  const CompressorConfig({
    this.enabled = false,
    this.thresholdDb = -20.0,
    this.ratio = 2.0,
    this.attackMs = 10.0,
    this.releaseMs = 100.0,
    this.mix = 0.0,
  });
  final bool enabled;
  final double thresholdDb; // -60..0
  final double ratio; // 1..20
  final double attackMs; // 1..100
  final double releaseMs; // 10..1000
  final double mix;
  CompressorConfig copyWith({
    bool? enabled,
    double? thresholdDb,
    double? ratio,
    double? attackMs,
    double? releaseMs,
    double? mix,
  }) =>
      CompressorConfig(
        enabled: enabled ?? this.enabled,
        thresholdDb: thresholdDb ?? this.thresholdDb,
        ratio: ratio ?? this.ratio,
        attackMs: attackMs ?? this.attackMs,
        releaseMs: releaseMs ?? this.releaseMs,
        mix: mix ?? this.mix,
      );
}
