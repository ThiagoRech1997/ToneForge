import 'package:flutter/material.dart';

import '../../theme/app_colors.dart';
import '../../widgets/tf_coming_soon.dart';

class LearningScreen extends StatelessWidget {
  const LearningScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return const TfComingSoon(
      icon: Icons.menu_book_rounded,
      accent: AppColors.accentLearning,
      title: 'Learning',
      subtitle:
          'Exercícios, escalas e acordes guiados com feedback em tempo real. Em desenvolvimento.',
    );
  }
}
