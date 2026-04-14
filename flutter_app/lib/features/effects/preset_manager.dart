// PresetManager — service stateless para serializar EffectsState como
// JSON em arquivos sob $documents/presets/. Os arquivos são planos
// (UTF-8) e portáveis para o app legado caso ele um dia adote o mesmo
// schema. Ver Fase 3.Presets.

import 'dart:convert';
import 'dart:io';

import 'package:path_provider/path_provider.dart';

import 'effects_categories.dart';
import 'effects_cubit.dart';
import 'models.dart';

class PresetSummary {
  const PresetSummary({required this.name, required this.file, required this.modified});
  final String name;
  final File file;
  final DateTime modified;
}

class PresetManager {
  static Future<Directory> _dir() async {
    final docs = await getApplicationDocumentsDirectory();
    final dir = Directory('${docs.path}/presets');
    if (!dir.existsSync()) dir.createSync(recursive: true);
    return dir;
  }

  static String _sanitize(String name) {
    final cleaned = name.trim().replaceAll(RegExp(r'[^A-Za-z0-9._\- ]'), '_');
    return cleaned.isEmpty ? 'preset' : cleaned;
  }

  static Future<List<PresetSummary>> list() async {
    final dir = await _dir();
    final files = dir.listSync().whereType<File>().where((f) => f.path.endsWith('.json'));
    final out = files.map((f) {
      final name = f.uri.pathSegments.last.replaceAll(RegExp(r'\.json$'), '');
      return PresetSummary(name: name, file: f, modified: f.statSync().modified);
    }).toList();
    out.sort((a, b) => b.modified.compareTo(a.modified));
    return out;
  }

  static Future<File> save(String name, EffectsState state) async {
    final dir = await _dir();
    final file = File('${dir.path}/${_sanitize(name)}.json');
    // Schema mantido em v1: o campo `effectOrder` é opcional e retrocompa-
    // tível — presets antigos sem o campo reaproveitam a ordem default do
    // engine (via EffectsState.initial.order) ao carregar. Isso evita um
    // bump de versão e migração só pelo reorder.
    final payload = <String, dynamic>{
      'schema': 1,
      'name': name,
      'createdAt': DateTime.now().toIso8601String(),
      'effects': {
        'gain': state.gain.toJson(),
        'distortion': state.distortion.toJson(),
        'delay': state.delay.toJson(),
        'reverb': state.reverb.toJson(),
        'chorus': state.chorus.toJson(),
        'flanger': state.flanger.toJson(),
        'phaser': state.phaser.toJson(),
        'eq': state.eq.toJson(),
        'compressor': state.compressor.toJson(),
      },
      'effectOrder': state.order.map((e) => e.name).toList(),
    };
    await file.writeAsString(const JsonEncoder.withIndent('  ').convert(payload));
    return file;
  }

  static Future<EffectsState?> load(File file) async {
    final raw = await file.readAsString();
    final json = jsonDecode(raw);
    if (json is! Map<String, dynamic>) return null;
    final effects = json['effects'];
    if (effects is! Map<String, dynamic>) return null;
    final order = _parseOrder(json['effectOrder']);
    return EffectsState.initial.copyWith(
      gain: GainConfig.fromJson(_obj(effects['gain'])),
      distortion: DistortionConfig.fromJson(_obj(effects['distortion'])),
      delay: DelayConfig.fromJson(_obj(effects['delay'])),
      reverb: ReverbConfig.fromJson(_obj(effects['reverb'])),
      chorus: ModConfig.fromJson(_obj(effects['chorus'])),
      flanger: ModConfig.fromJson(_obj(effects['flanger'])),
      phaser: ModConfig.fromJson(_obj(effects['phaser'])),
      eq: EqConfig.fromJson(_obj(effects['eq'])),
      compressor: CompressorConfig.fromJson(_obj(effects['compressor'])),
      order: order,
    );
  }

  /// Converte a lista de strings de `effectOrder` (se presente) em
  /// `List<EffectKind>`. Retorna a ordem default se o campo estiver
  /// ausente, inválido, duplicado ou incompleto — presets antigos não
  /// carregavam o campo e devem abrir sem erro.
  static List<EffectKind> _parseOrder(Object? raw) {
    if (raw is! List) return kDefaultEffectOrder;
    final byName = <String, EffectKind>{
      for (final k in EffectKind.values) k.name: k,
    };
    final parsed = <EffectKind>[];
    final seen = <EffectKind>{};
    for (final entry in raw) {
      if (entry is! String) return kDefaultEffectOrder;
      final kind = byName[entry];
      if (kind == null || !seen.add(kind)) return kDefaultEffectOrder;
      parsed.add(kind);
    }
    if (parsed.length != EffectKind.values.length) {
      return kDefaultEffectOrder;
    }
    return parsed;
  }

  static Future<void> delete(File file) async {
    if (file.existsSync()) await file.delete();
  }

  static Map<String, dynamic> _obj(Object? v) =>
      v is Map<String, dynamic> ? v : <String, dynamic>{};
}
