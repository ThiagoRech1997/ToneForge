import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_typography.dart';

class TfPrimaryButton extends StatelessWidget {
  const TfPrimaryButton({
    super.key,
    required this.label,
    this.icon,
    this.onPressed,
    this.accent,
    this.gradient = true,
    this.fullWidth = true,
  });

  final String label;
  final IconData? icon;
  final VoidCallback? onPressed;
  final Color? accent;
  final bool gradient;
  final bool fullWidth;

  @override
  Widget build(BuildContext context) {
    final base = accent ?? AppColors.primary;
    final enabled = onPressed != null;
    final decoration = BoxDecoration(
      borderRadius: BorderRadius.circular(AppRadius.xxl),
      gradient: gradient
          ? LinearGradient(
              colors: enabled
                  ? [base.withValues(alpha: 0.9), AppColors.primary]
                  : [AppColors.muted, AppColors.elevated],
              begin: Alignment.centerLeft,
              end: Alignment.centerRight,
            )
          : null,
      color: gradient ? null : (enabled ? base : AppColors.muted),
    );
    final content = Container(
      decoration: decoration,
      height: 56,
      width: fullWidth ? double.infinity : null,
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.xl),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        mainAxisSize: MainAxisSize.min,
        children: [
          if (icon != null) ...[
            Icon(icon, color: AppColors.textPrimary, size: 20),
            const SizedBox(width: AppSpacing.md),
          ],
          Text(
            label,
            style: AppTypography.cardTitle.copyWith(
              color: AppColors.textPrimary,
            ),
          ),
        ],
      ),
    );
    return Material(
      color: Colors.transparent,
      borderRadius: BorderRadius.circular(AppRadius.xxl),
      child: InkWell(
        borderRadius: BorderRadius.circular(AppRadius.xxl),
        onTap: onPressed,
        child: content,
      ),
    );
  }
}
