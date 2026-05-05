// Modelos imutáveis dos parâmetros dos 9 efeitos. Faixas e defaults
// reproduzem o EffectParameters.java + arrays.xml do app legado.
// toJson/fromJson dão suporte ao PresetManager (Fase 3.Presets) sem
// dependências externas — usa apenas dart:convert.

double _d(Object? v, double fallback) => v is num ? v.toDouble() : fallback;
int _i(Object? v, int fallback) => v is num ? v.toInt() : fallback;
bool _b(Object? v, bool fallback) => v is bool ? v : fallback;

class GainConfig {
  const GainConfig({this.enabled = true, this.level = 0.5});
  final bool enabled;
  final double level;
  GainConfig copyWith({bool? enabled, double? level}) =>
      GainConfig(enabled: enabled ?? this.enabled, level: level ?? this.level);
  Map<String, dynamic> toJson() => {'enabled': enabled, 'level': level};
  factory GainConfig.fromJson(Map<String, dynamic> j) =>
      GainConfig(enabled: _b(j['enabled'], true), level: _d(j['level'], 0.5));
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
  Map<String, dynamic> toJson() =>
      {'enabled': enabled, 'amount': amount, 'type': type, 'mix': mix};
  factory DistortionConfig.fromJson(Map<String, dynamic> j) => DistortionConfig(
        enabled: _b(j['enabled'], false),
        amount: _d(j['amount'], 0),
        type: _i(j['type'], 0),
        mix: _d(j['mix'], 0),
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
  Map<String, dynamic> toJson() =>
      {'enabled': enabled, 'timeMs': timeMs, 'feedback': feedback, 'mix': mix};
  factory DelayConfig.fromJson(Map<String, dynamic> j) => DelayConfig(
        enabled: _b(j['enabled'], false),
        timeMs: _d(j['timeMs'], 0),
        feedback: _d(j['feedback'], 0),
        mix: _d(j['mix'], 0),
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
  Map<String, dynamic> toJson() => {
        'enabled': enabled,
        'roomSize': roomSize,
        'damping': damping,
        'type': type,
        'mix': mix,
      };
  factory ReverbConfig.fromJson(Map<String, dynamic> j) => ReverbConfig(
        enabled: _b(j['enabled'], false),
        roomSize: _d(j['roomSize'], 0),
        damping: _d(j['damping'], 0),
        type: _i(j['type'], 0),
        mix: _d(j['mix'], 0),
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
  Map<String, dynamic> toJson() => {
        'enabled': enabled,
        'depth': depth,
        'rate': rate,
        'feedback': feedback,
        'mix': mix,
      };
  factory ModConfig.fromJson(Map<String, dynamic> j) => ModConfig(
        enabled: _b(j['enabled'], false),
        depth: _d(j['depth'], 0),
        rate: _d(j['rate'], 0),
        feedback: _d(j['feedback'], 0),
        mix: _d(j['mix'], 0),
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
  Map<String, dynamic> toJson() =>
      {'enabled': enabled, 'low': low, 'mid': mid, 'high': high, 'mix': mix};
  factory EqConfig.fromJson(Map<String, dynamic> j) => EqConfig(
        enabled: _b(j['enabled'], false),
        low: _d(j['low'], 0),
        mid: _d(j['mid'], 0),
        high: _d(j['high'], 0),
        mix: _d(j['mix'], 0),
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
  Map<String, dynamic> toJson() => {
        'enabled': enabled,
        'thresholdDb': thresholdDb,
        'ratio': ratio,
        'attackMs': attackMs,
        'releaseMs': releaseMs,
        'mix': mix,
      };
  factory CompressorConfig.fromJson(Map<String, dynamic> j) => CompressorConfig(
        enabled: _b(j['enabled'], false),
        thresholdDb: _d(j['thresholdDb'], -20),
        ratio: _d(j['ratio'], 2),
        attackMs: _d(j['attackMs'], 10),
        releaseMs: _d(j['releaseMs'], 100),
        mix: _d(j['mix'], 0),
      );
}

class PitchShiftConfig {
  // Pitch shift granular real-time (TFR-10). semitones em ±12; o native
  // já clampa, mas mantemos os defaults dentro do range pra evitar
  // surpresa de UI. mix=1.0 default (assim que o usuário liga, ouve o
  // efeito imediatamente; se quiser parallel-blend é só baixar).
  const PitchShiftConfig({
    this.enabled = false,
    this.semitones = 0.0,
    this.mix = 1.0,
  });
  final bool enabled;
  final double semitones; // -12..+12
  final double mix;
  PitchShiftConfig copyWith({bool? enabled, double? semitones, double? mix}) =>
      PitchShiftConfig(
        enabled: enabled ?? this.enabled,
        semitones: semitones ?? this.semitones,
        mix: mix ?? this.mix,
      );
  Map<String, dynamic> toJson() =>
      {'enabled': enabled, 'semitones': semitones, 'mix': mix};
  factory PitchShiftConfig.fromJson(Map<String, dynamic> j) => PitchShiftConfig(
        enabled: _b(j['enabled'], false),
        semitones: _d(j['semitones'], 0),
        mix: _d(j['mix'], 1),
      );
}
