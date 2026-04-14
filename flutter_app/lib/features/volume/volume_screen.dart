import 'package:flutter/material.dart';

import '../../theme/app_colors.dart';
import '../../widgets/tf_coming_soon.dart';

class VolumeScreen extends StatelessWidget {
  const VolumeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return const TfComingSoon(
      icon: Icons.volume_up_rounded,
      accent: AppColors.accentVolume,
      title: 'Volume',
      subtitle:
          'Mixer mestre com controle de input/output e limiter dedicado. Em breve.',
    );
  }
}
