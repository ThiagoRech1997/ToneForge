// Entry point + navegação. Fase 3 introduziu múltiplas features
// (Tuner, Benchmark, …); a Home aqui é só um menu enquanto a estrutura
// final de navegação não é desenhada. Conforme features são portadas, vão
// virando cards aqui.

import 'package:flutter/material.dart';

import 'features/benchmark/benchmark_screen.dart';
import 'features/effects/effects_screen.dart';
import 'features/looper/looper_screen.dart';
import 'features/metronome/metronome_screen.dart';
import 'features/recorder/recorder_screen.dart';
import 'features/tuner/tuner_screen.dart';

void main() {
  runApp(const ToneforgeFlutterApp());
}

class ToneforgeFlutterApp extends StatelessWidget {
  const ToneforgeFlutterApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'ToneForge (Flutter)',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.deepPurple,
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      home: const HomeScreen(),
    );
  }
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('ToneForge (Flutter)')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _FeatureCard(
            icon: Icons.tune,
            title: 'Effects',
            subtitle: '9 efeitos: Gain, Dist, Delay, Reverb, Chorus, Flanger, Phaser, EQ, Comp',
            destination: const EffectsScreen(),
          ),
          const SizedBox(height: 12),
          _FeatureCard(
            icon: Icons.graphic_eq,
            title: 'Tuner',
            subtitle: 'Afinador cromático via FFI → Oboe → DSP C++',
            destination: const TunerScreen(),
          ),
          const SizedBox(height: 12),
          _FeatureCard(
            icon: Icons.timer,
            title: 'Metronome',
            subtitle: 'BPM, compasso e volume com pulso visual',
            destination: const MetronomeScreen(),
          ),
          const SizedBox(height: 12),
          _FeatureCard(
            icon: Icons.fiber_manual_record,
            title: 'Recorder',
            subtitle: 'Captura WAV pós-FX no engine C++',
            destination: const RecorderScreen(),
          ),
          const SizedBox(height: 12),
          _FeatureCard(
            icon: Icons.loop,
            title: 'Looper',
            subtitle: 'MVP: gravar / tocar / limpar com waveform',
            destination: const LooperScreen(),
          ),
          const SizedBox(height: 12),
          _FeatureCard(
            icon: Icons.speed,
            title: 'Benchmark Oboe',
            subtitle: 'Latência round-trip + toggle de efeitos',
            destination: const BenchmarkScreen(),
          ),
        ],
      ),
    );
  }
}

class _FeatureCard extends StatelessWidget {
  const _FeatureCard({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.destination,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final Widget destination;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        leading: Icon(icon, size: 32),
        title: Text(title),
        subtitle: Text(subtitle),
        trailing: const Icon(Icons.chevron_right),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        onTap: () {
          Navigator.of(context).push(
            MaterialPageRoute(builder: (_) => destination),
          );
        },
      ),
    );
  }
}
