import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_typography.dart';

class TfParamSlider extends StatelessWidget {
  const TfParamSlider({
    super.key,
    required this.label,
    required this.value,
    required this.min,
    required this.max,
    required this.onChanged,
    this.valueFormatter,
    this.accent,
    this.enabled = true,
    this.divisions,
  });

  final String label;
  final double value;
  final double min;
  final double max;
  final ValueChanged<double> onChanged;
  final String Function(double)? valueFormatter;
  final Color? accent;
  final bool enabled;
  final int? divisions;

  @override
  Widget build(BuildContext context) {
    final color = accent ?? AppColors.primary;
    final formatted = valueFormatter?.call(value) ??
        value.toStringAsFixed(2);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              label,
              style: AppTypography.caption.copyWith(
                color: AppColors.textSecondary,
                fontWeight: FontWeight.w500,
              ),
            ),
            Text(
              formatted,
              style: AppTypography.caption.copyWith(
                color: enabled ? color : AppColors.textTertiary,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.xs),
        SliderTheme(
          data: SliderTheme.of(context).copyWith(
            trackHeight: 4,
            activeTrackColor: enabled ? color : AppColors.muted,
            inactiveTrackColor: AppColors.muted,
            thumbColor: enabled ? color : AppColors.textTertiary,
            thumbShape: const RoundSliderThumbShape(
              enabledThumbRadius: 8,
              disabledThumbRadius: 6,
            ),
            overlayColor: color.withValues(alpha: 0.18),
            overlayShape: const RoundSliderOverlayShape(overlayRadius: 16),
            valueIndicatorColor: AppColors.elevated,
          ),
          child: Slider(
            value: value.clamp(min, max),
            min: min,
            max: max,
            divisions: divisions,
            onChanged: enabled ? onChanged : null,
          ),
        ),
      ],
    );
  }
}
