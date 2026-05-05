import 'package:flutter/material.dart';

import '../../theme/app_colors.dart';

enum EffectKind {
  gain,
  distortion,
  delay,
  reverb,
  chorus,
  flanger,
  phaser,
  eq,
  compressor,
  pitchShift,
}

extension EffectKindLabel on EffectKind {
  String get label {
    switch (this) {
      case EffectKind.gain:
        return 'Gain';
      case EffectKind.distortion:
        return 'Distortion';
      case EffectKind.delay:
        return 'Delay';
      case EffectKind.reverb:
        return 'Reverb';
      case EffectKind.chorus:
        return 'Chorus';
      case EffectKind.flanger:
        return 'Flanger';
      case EffectKind.phaser:
        return 'Phaser';
      case EffectKind.eq:
        return 'EQ';
      case EffectKind.compressor:
        return 'Compressor';
      case EffectKind.pitchShift:
        return 'Pitch Shift';
    }
  }

  String get shortLabel {
    switch (this) {
      case EffectKind.distortion:
        return 'Dist';
      case EffectKind.compressor:
        return 'Comp';
      case EffectKind.pitchShift:
        return 'Pitch';
      default:
        return label;
    }
  }

  /// Chave esperada pelo native `setEffectOrder` (veja o vetor
  /// `effectOrder` em `engine/src/audio_engine.cpp`). Strings têm que
  /// bater byte-a-byte, incluindo acentos — divergências são ignoradas
  /// silenciosamente pelo C++.
  String get engineKey {
    switch (this) {
      case EffectKind.gain:
        return 'Ganho';
      case EffectKind.distortion:
        return 'Distorção';
      case EffectKind.delay:
        return 'Delay';
      case EffectKind.reverb:
        return 'Reverb';
      case EffectKind.chorus:
        return 'Chorus';
      case EffectKind.flanger:
        return 'Flanger';
      case EffectKind.phaser:
        return 'Phaser';
      case EffectKind.eq:
        return 'EQ';
      case EffectKind.compressor:
        return 'Compressor';
      case EffectKind.pitchShift:
        return 'Pitch Shift';
    }
  }
}

/// Ordem default da cadeia de efeitos. Precisa espelhar a inicialização
/// estática de `effectOrder` em `engine/src/audio_engine.cpp` (kDefaultOrder
/// dentro de initAudioEngine) para que o estado inicial do Cubit coincida
/// com o que o native reporta.
const List<EffectKind> kDefaultEffectOrder = [
  EffectKind.gain,
  EffectKind.distortion,
  EffectKind.chorus,
  EffectKind.flanger,
  EffectKind.phaser,
  EffectKind.pitchShift,
  EffectKind.eq,
  EffectKind.compressor,
  EffectKind.delay,
  EffectKind.reverb,
];

class EffectCategoryInfo {
  const EffectCategoryInfo({
    required this.id,
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.accent,
    required this.effects,
  });

  final String id;
  final String title;
  final String subtitle;
  final IconData icon;
  final Color accent;
  final List<EffectKind> effects;
}

class EffectCategories {
  EffectCategories._();

  static const distortion = EffectCategoryInfo(
    id: 'distortion',
    title: 'Distortion',
    subtitle: 'Gain, Overdrive, Fuzz',
    icon: Icons.bolt_rounded,
    accent: AppColors.warning,
    effects: [EffectKind.gain, EffectKind.distortion],
  );

  static const modulation = EffectCategoryInfo(
    id: 'modulation',
    title: 'Modulation',
    subtitle: 'Chorus, Flanger, Phaser',
    icon: Icons.graphic_eq_rounded,
    accent: AppColors.primary,
    effects: [EffectKind.chorus, EffectKind.flanger, EffectKind.phaser],
  );

  static const time = EffectCategoryInfo(
    id: 'time',
    title: 'Time',
    subtitle: 'Delay, Echo',
    icon: Icons.access_time_filled_rounded,
    accent: AppColors.success,
    effects: [EffectKind.delay],
  );

  static const filter = EffectCategoryInfo(
    id: 'filter',
    title: 'Filter',
    subtitle: 'EQ 3-Band',
    icon: Icons.filter_alt_rounded,
    accent: AppColors.accentMidi,
    effects: [EffectKind.eq],
  );

  static const dynamics = EffectCategoryInfo(
    id: 'dynamics',
    title: 'Dynamics',
    subtitle: 'Compressor',
    icon: Icons.equalizer_rounded,
    accent: AppColors.accentRecorder,
    effects: [EffectKind.compressor],
  );

  static const ambient = EffectCategoryInfo(
    id: 'ambient',
    title: 'Ambient',
    subtitle: 'Reverb, Space',
    icon: Icons.blur_on_rounded,
    accent: AppColors.accentTuner,
    effects: [EffectKind.reverb],
  );

  static const pitch = EffectCategoryInfo(
    id: 'pitch',
    title: 'Pitch',
    subtitle: 'Pitch Shift ±12 semitones',
    icon: Icons.unfold_more_rounded,
    accent: AppColors.accentMetronome,
    effects: [EffectKind.pitchShift],
  );

  static const all = <EffectCategoryInfo>[
    distortion,
    modulation,
    time,
    filter,
    dynamics,
    ambient,
    pitch,
  ];
}
