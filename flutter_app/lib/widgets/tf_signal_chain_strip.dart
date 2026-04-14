import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_typography.dart';

class TfSignalChainStrip extends StatelessWidget {
  const TfSignalChainStrip({
    super.key,
    required this.nodes,
  });

  final List<SignalChainNode> nodes;

  @override
  Widget build(BuildContext context) {
    final children = <Widget>[];
    children.add(_buildChip('INPUT', AppColors.textTertiary, outlined: true));
    for (final node in nodes) {
      children.add(_arrow());
      children.add(_buildChip(node.label, node.accent));
    }
    children.add(_arrow());
    children.add(_buildChip('OUT', AppColors.textTertiary, outlined: true));

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: Row(children: children),
    );
  }

  Widget _buildChip(String label, Color color, {bool outlined = false}) {
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: 6,
      ),
      decoration: BoxDecoration(
        color: outlined
            ? Colors.transparent
            : color.withValues(alpha: 0.18),
        borderRadius: BorderRadius.circular(AppRadius.md),
        border: Border.all(
          color: outlined ? AppColors.muted : color.withValues(alpha: 0.5),
          width: 1,
        ),
      ),
      child: Text(
        label,
        style: AppTypography.caption.copyWith(
          color: outlined ? AppColors.textSecondary : color,
          fontWeight: FontWeight.w600,
          letterSpacing: 0.4,
        ),
      ),
    );
  }

  Widget _arrow() {
    return const Padding(
      padding: EdgeInsets.symmetric(horizontal: AppSpacing.sm),
      child: Icon(
        Icons.arrow_forward_rounded,
        color: AppColors.textTertiary,
        size: 16,
      ),
    );
  }
}

class SignalChainNode {
  const SignalChainNode({required this.label, required this.accent});

  final String label;
  final Color accent;
}
