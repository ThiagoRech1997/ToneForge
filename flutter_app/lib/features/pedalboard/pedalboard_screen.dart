import 'package:flutter/material.dart';

import '../../theme/app_colors.dart';
import '../../widgets/tf_coming_soon.dart';

class PedalboardScreen extends StatelessWidget {
  const PedalboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return const TfComingSoon(
      icon: Icons.dashboard_customize_rounded,
      accent: AppColors.accentPedalboard,
      title: 'Pedalboard',
      subtitle:
          'Arraste e reordene seus pedais em uma cadeia visual. O engine C++ já suporta — só falta a UI.',
    );
  }
}
