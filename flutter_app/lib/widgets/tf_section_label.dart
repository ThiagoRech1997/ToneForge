import 'package:flutter/material.dart';

import '../theme/app_typography.dart';

class TfSectionLabel extends StatelessWidget {
  const TfSectionLabel(this.text, {super.key, this.color});

  final String text;
  final Color? color;

  @override
  Widget build(BuildContext context) {
    final style = AppTypography.sectionLabel;
    return Text(
      text.toUpperCase(),
      style: color == null ? style : style.copyWith(color: color),
    );
  }
}
