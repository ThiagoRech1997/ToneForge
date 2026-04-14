// Entry point. A navegação ficou a cargo do AppShell, que hospeda um
// IndexedStack com 4 tabs (Home / Effects / Pedals / Tuner) e a
// EffectsCubit compartilhada. Telas secundárias continuam acessíveis
// via Navigator.push a partir da grid da Home.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'shell/app_shell.dart';
import 'theme/app_theme.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  SystemChrome.setSystemUIOverlayStyle(AppTheme.systemOverlay);
  runApp(const ToneforgeFlutterApp());
}

class ToneforgeFlutterApp extends StatelessWidget {
  const ToneforgeFlutterApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'ToneForge',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.dark(),
      home: const AppShell(),
    );
  }
}
