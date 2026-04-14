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
    }
  }

  String get shortLabel {
    switch (this) {
      case EffectKind.distortion:
        return 'Dist';
      case EffectKind.compressor:
        return 'Comp';
      default:
        return label;
    }
  }
}

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

  static const all = <EffectCategoryInfo>[
    distortion,
    modulation,
    time,
    filter,
    dynamics,
    ambient,
  ];
}
