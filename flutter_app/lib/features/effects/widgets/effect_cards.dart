import 'package:flutter/material.dart';

import '../../../theme/app_colors.dart';
import '../../../theme/app_spacing.dart';
import '../../../theme/app_typography.dart';
import '../../../widgets/tf_card.dart';
import '../../../widgets/tf_param_slider.dart';
import '../effects_categories.dart';
import '../effects_cubit.dart';
import '../models.dart';

class AccentEffectCard extends StatelessWidget {
  const AccentEffectCard({
    super.key,
    required this.title,
    required this.accent,
    required this.enabled,
    required this.onEnabled,
    required this.children,
  });

  final String title;
  final Color accent;
  final bool enabled;
  final ValueChanged<bool> onEnabled;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.md),
      child: TfCard(
        accent: accent,
        padding: EdgeInsets.zero,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            _Header(
              title: title,
              accent: accent,
              enabled: enabled,
              onEnabled: onEnabled,
            ),
            AnimatedOpacity(
              duration: const Duration(milliseconds: 140),
              opacity: enabled ? 1.0 : 0.45,
              child: Padding(
                padding: const EdgeInsets.fromLTRB(
                  AppSpacing.lg,
                  AppSpacing.sm,
                  AppSpacing.lg,
                  AppSpacing.lg,
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: children,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header({
    required this.title,
    required this.accent,
    required this.enabled,
    required this.onEnabled,
  });

  final String title;
  final Color accent;
  final bool enabled;
  final ValueChanged<bool> onEnabled;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.fromLTRB(
        AppSpacing.lg,
        AppSpacing.md,
        AppSpacing.md,
        AppSpacing.md,
      ),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            accent.withValues(alpha: 0.20),
            accent.withValues(alpha: 0.04),
          ],
          begin: Alignment.centerLeft,
          end: Alignment.centerRight,
        ),
        borderRadius: const BorderRadius.only(
          topLeft: Radius.circular(AppRadius.lg),
          topRight: Radius.circular(AppRadius.lg),
        ),
      ),
      child: Row(
        children: [
          Container(
            width: 8,
            height: 8,
            decoration: BoxDecoration(
              color: accent,
              shape: BoxShape.circle,
            ),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Text(
              title,
              style: AppTypography.cardTitle.copyWith(color: accent),
            ),
          ),
          Switch(
            value: enabled,
            onChanged: onEnabled,
            activeTrackColor: accent,
          ),
        ],
      ),
    );
  }
}

class _DropdownRow<T> extends StatelessWidget {
  const _DropdownRow({
    required this.label,
    required this.value,
    required this.options,
    required this.onChanged,
    required this.accent,
  });

  final String label;
  final T value;
  final Map<T, String> options;
  final ValueChanged<T> onChanged;
  final Color accent;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.sm),
      child: Row(
        children: [
          Expanded(
            child: Text(
              label,
              style: AppTypography.caption.copyWith(
                color: AppColors.textSecondary,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(
              horizontal: AppSpacing.md,
              vertical: 2,
            ),
            decoration: BoxDecoration(
              color: AppColors.elevated,
              borderRadius: BorderRadius.circular(AppRadius.md),
              border: Border.all(color: accent.withValues(alpha: 0.3)),
            ),
            child: DropdownButton<T>(
              value: value,
              dropdownColor: AppColors.elevated,
              underline: const SizedBox.shrink(),
              iconEnabledColor: accent,
              style: AppTypography.caption.copyWith(
                color: AppColors.textPrimary,
                fontWeight: FontWeight.w600,
              ),
              items: [
                for (final entry in options.entries)
                  DropdownMenuItem(value: entry.key, child: Text(entry.value)),
              ],
              onChanged: (v) {
                if (v != null) onChanged(v);
              },
            ),
          ),
        ],
      ),
    );
  }
}

/// Public factory: builds the effect card widget for a given [EffectKind]
/// using the provided [state] and [cubit], with the category [accent] color.
Widget buildEffectCard({
  required EffectKind kind,
  required EffectsState state,
  required EffectsCubit cubit,
  required Color accent,
}) {
  switch (kind) {
    case EffectKind.gain:
      return _GainCard(state: state.gain, cubit: cubit, accent: accent);
    case EffectKind.distortion:
      return _DistortionCard(
          state: state.distortion, cubit: cubit, accent: accent);
    case EffectKind.delay:
      return _DelayCard(state: state.delay, cubit: cubit, accent: accent);
    case EffectKind.reverb:
      return _ReverbCard(state: state.reverb, cubit: cubit, accent: accent);
    case EffectKind.chorus:
      return _ChorusCard(state: state.chorus, cubit: cubit, accent: accent);
    case EffectKind.flanger:
      return _FlangerCard(state: state.flanger, cubit: cubit, accent: accent);
    case EffectKind.phaser:
      return _PhaserCard(state: state.phaser, cubit: cubit, accent: accent);
    case EffectKind.eq:
      return _EqCard(state: state.eq, cubit: cubit, accent: accent);
    case EffectKind.compressor:
      return _CompressorCard(
          state: state.compressor, cubit: cubit, accent: accent);
    case EffectKind.pitchShift:
      return _PitchShiftCard(
          state: state.pitchShift, cubit: cubit, accent: accent);
  }
}

String _fmtPct(double v) => '${(v * 100).toStringAsFixed(0)}%';
String _fmtMs(double v) => '${v.toStringAsFixed(0)} ms';
String _fmtHz(double v) => '${v.toStringAsFixed(2)} Hz';
String _fmtDb(double v) => '${v >= 0 ? '+' : ''}${v.toStringAsFixed(1)} dB';
String _fmtRatio(double v) => '${v.toStringAsFixed(1)}:1';
String _fmtSemis(double v) {
  final rounded = v.round();
  final sign = rounded > 0 ? '+' : '';
  if (rounded == 0) return '0 st';
  return '$sign$rounded st';
}

class _GainCard extends StatelessWidget {
  const _GainCard({required this.state, required this.cubit, required this.accent});
  final GainConfig state;
  final EffectsCubit cubit;
  final Color accent;
  @override
  Widget build(BuildContext context) {
    return AccentEffectCard(
      title: 'Gain',
      accent: accent,
      enabled: state.enabled,
      onEnabled: cubit.setGainEnabled,
      children: [
        TfParamSlider(
          label: 'Level',
          value: state.level,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setGainLevel,
        ),
      ],
    );
  }
}

class _DistortionCard extends StatelessWidget {
  const _DistortionCard({required this.state, required this.cubit, required this.accent});
  final DistortionConfig state;
  final EffectsCubit cubit;
  final Color accent;
  @override
  Widget build(BuildContext context) {
    return AccentEffectCard(
      title: 'Distortion',
      accent: accent,
      enabled: state.enabled,
      onEnabled: cubit.setDistortionEnabled,
      children: [
        _DropdownRow<int>(
          label: 'Type',
          value: state.type,
          accent: accent,
          options: {
            for (var i = 0; i < DistortionConfig.types.length; i++)
              i: DistortionConfig.types[i],
          },
          onChanged: cubit.setDistortionType,
        ),
        TfParamSlider(
          label: 'Amount',
          value: state.amount,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setDistortionAmount,
        ),
        TfParamSlider(
          label: 'Mix',
          value: state.mix,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setDistortionMix,
        ),
      ],
    );
  }
}

class _DelayCard extends StatelessWidget {
  const _DelayCard({required this.state, required this.cubit, required this.accent});
  final DelayConfig state;
  final EffectsCubit cubit;
  final Color accent;
  @override
  Widget build(BuildContext context) {
    return AccentEffectCard(
      title: 'Delay',
      accent: accent,
      enabled: state.enabled,
      onEnabled: cubit.setDelayEnabled,
      children: [
        TfParamSlider(
          label: 'Time',
          value: state.timeMs,
          min: 0,
          max: 1000,
          accent: accent,
          valueFormatter: _fmtMs,
          onChanged: cubit.setDelayTimeMs,
        ),
        TfParamSlider(
          label: 'Feedback',
          value: state.feedback,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setDelayFeedback,
        ),
        TfParamSlider(
          label: 'Mix',
          value: state.mix,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setDelayMix,
        ),
      ],
    );
  }
}

class _ReverbCard extends StatelessWidget {
  const _ReverbCard({required this.state, required this.cubit, required this.accent});
  final ReverbConfig state;
  final EffectsCubit cubit;
  final Color accent;
  @override
  Widget build(BuildContext context) {
    return AccentEffectCard(
      title: 'Reverb',
      accent: accent,
      enabled: state.enabled,
      onEnabled: cubit.setReverbEnabled,
      children: [
        _DropdownRow<int>(
          label: 'Type',
          value: state.type,
          accent: accent,
          options: {
            for (var i = 0; i < ReverbConfig.types.length; i++)
              i: ReverbConfig.types[i],
          },
          onChanged: cubit.setReverbType,
        ),
        TfParamSlider(
          label: 'Room',
          value: state.roomSize,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setReverbRoomSize,
        ),
        TfParamSlider(
          label: 'Damping',
          value: state.damping,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setReverbDamping,
        ),
        TfParamSlider(
          label: 'Mix',
          value: state.mix,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setReverbMix,
        ),
      ],
    );
  }
}

class _ChorusCard extends StatelessWidget {
  const _ChorusCard({required this.state, required this.cubit, required this.accent});
  final ModConfig state;
  final EffectsCubit cubit;
  final Color accent;
  @override
  Widget build(BuildContext context) {
    return AccentEffectCard(
      title: 'Chorus',
      accent: accent,
      enabled: state.enabled,
      onEnabled: cubit.setChorusEnabled,
      children: [
        TfParamSlider(
          label: 'Depth',
          value: state.depth,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setChorusDepth,
        ),
        TfParamSlider(
          label: 'Rate',
          value: state.rate,
          min: 0,
          max: 20,
          accent: accent,
          valueFormatter: _fmtHz,
          onChanged: cubit.setChorusRate,
        ),
        TfParamSlider(
          label: 'Mix',
          value: state.mix,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setChorusMix,
        ),
      ],
    );
  }
}

class _FlangerCard extends StatelessWidget {
  const _FlangerCard({required this.state, required this.cubit, required this.accent});
  final ModConfig state;
  final EffectsCubit cubit;
  final Color accent;
  @override
  Widget build(BuildContext context) {
    return AccentEffectCard(
      title: 'Flanger',
      accent: accent,
      enabled: state.enabled,
      onEnabled: cubit.setFlangerEnabled,
      children: [
        TfParamSlider(
          label: 'Depth',
          value: state.depth,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setFlangerDepth,
        ),
        TfParamSlider(
          label: 'Rate',
          value: state.rate,
          min: 0,
          max: 20,
          accent: accent,
          valueFormatter: _fmtHz,
          onChanged: cubit.setFlangerRate,
        ),
        TfParamSlider(
          label: 'Feedback',
          value: state.feedback,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setFlangerFeedback,
        ),
        TfParamSlider(
          label: 'Mix',
          value: state.mix,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setFlangerMix,
        ),
      ],
    );
  }
}

class _PhaserCard extends StatelessWidget {
  const _PhaserCard({required this.state, required this.cubit, required this.accent});
  final ModConfig state;
  final EffectsCubit cubit;
  final Color accent;
  @override
  Widget build(BuildContext context) {
    return AccentEffectCard(
      title: 'Phaser',
      accent: accent,
      enabled: state.enabled,
      onEnabled: cubit.setPhaserEnabled,
      children: [
        TfParamSlider(
          label: 'Depth',
          value: state.depth,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setPhaserDepth,
        ),
        TfParamSlider(
          label: 'Rate',
          value: state.rate,
          min: 0,
          max: 20,
          accent: accent,
          valueFormatter: _fmtHz,
          onChanged: cubit.setPhaserRate,
        ),
        TfParamSlider(
          label: 'Feedback',
          value: state.feedback,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setPhaserFeedback,
        ),
        TfParamSlider(
          label: 'Mix',
          value: state.mix,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setPhaserMix,
        ),
      ],
    );
  }
}

class _EqCard extends StatelessWidget {
  const _EqCard({required this.state, required this.cubit, required this.accent});
  final EqConfig state;
  final EffectsCubit cubit;
  final Color accent;
  @override
  Widget build(BuildContext context) {
    return AccentEffectCard(
      title: 'EQ (3-band)',
      accent: accent,
      enabled: state.enabled,
      onEnabled: cubit.setEqEnabled,
      children: [
        TfParamSlider(
          label: 'Low',
          value: state.low,
          min: -12,
          max: 12,
          accent: accent,
          valueFormatter: _fmtDb,
          onChanged: cubit.setEqLow,
        ),
        TfParamSlider(
          label: 'Mid',
          value: state.mid,
          min: -12,
          max: 12,
          accent: accent,
          valueFormatter: _fmtDb,
          onChanged: cubit.setEqMid,
        ),
        TfParamSlider(
          label: 'High',
          value: state.high,
          min: -12,
          max: 12,
          accent: accent,
          valueFormatter: _fmtDb,
          onChanged: cubit.setEqHigh,
        ),
        TfParamSlider(
          label: 'Mix',
          value: state.mix,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setEqMix,
        ),
      ],
    );
  }
}

class _CompressorCard extends StatelessWidget {
  const _CompressorCard({required this.state, required this.cubit, required this.accent});
  final CompressorConfig state;
  final EffectsCubit cubit;
  final Color accent;
  @override
  Widget build(BuildContext context) {
    return AccentEffectCard(
      title: 'Compressor',
      accent: accent,
      enabled: state.enabled,
      onEnabled: cubit.setCompressorEnabled,
      children: [
        TfParamSlider(
          label: 'Threshold',
          value: state.thresholdDb,
          min: -60,
          max: 0,
          accent: accent,
          valueFormatter: (v) => '${v.toStringAsFixed(1)} dB',
          onChanged: cubit.setCompressorThreshold,
        ),
        TfParamSlider(
          label: 'Ratio',
          value: state.ratio,
          min: 1,
          max: 20,
          accent: accent,
          valueFormatter: _fmtRatio,
          onChanged: cubit.setCompressorRatio,
        ),
        TfParamSlider(
          label: 'Attack',
          value: state.attackMs,
          min: 1,
          max: 100,
          accent: accent,
          valueFormatter: _fmtMs,
          onChanged: cubit.setCompressorAttack,
        ),
        TfParamSlider(
          label: 'Release',
          value: state.releaseMs,
          min: 10,
          max: 1000,
          accent: accent,
          valueFormatter: _fmtMs,
          onChanged: cubit.setCompressorRelease,
        ),
        TfParamSlider(
          label: 'Mix',
          value: state.mix,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setCompressorMix,
        ),
      ],
    );
  }
}

class _PitchShiftCard extends StatelessWidget {
  const _PitchShiftCard({required this.state, required this.cubit, required this.accent});
  final PitchShiftConfig state;
  final EffectsCubit cubit;
  final Color accent;
  @override
  Widget build(BuildContext context) {
    return AccentEffectCard(
      title: 'Pitch Shift',
      accent: accent,
      enabled: state.enabled,
      onEnabled: cubit.setPitchShiftEnabled,
      children: [
        // Slider de semitons em incrementos de 1; 25 divisões cobrem
        // -12..+12 inclusive. Range fixo no native (clampa em ±12).
        TfParamSlider(
          label: 'Semitones',
          value: state.semitones,
          min: -12,
          max: 12,
          divisions: 24,
          accent: accent,
          valueFormatter: _fmtSemis,
          onChanged: cubit.setPitchShiftSemitones,
        ),
        TfParamSlider(
          label: 'Mix',
          value: state.mix,
          min: 0,
          max: 1,
          accent: accent,
          valueFormatter: _fmtPct,
          onChanged: cubit.setPitchShiftMix,
        ),
      ],
    );
  }
}
