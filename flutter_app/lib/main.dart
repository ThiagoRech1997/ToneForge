// Fase 2 — UI mínima de prova. Um botão start/stop que invoca o engine C++
// via FFI, engaja todos os efeitos e mostra latência Oboe em tempo real.
// Mesma lógica da DebugBenchmarkActivity Android, mas rodando em Flutter.
// Ver plano .claude/plans/cuddly-jumping-wolf.md Fase 2.

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

import 'engine/engine.dart';

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
      home: const BenchmarkScreen(),
    );
  }
}

class BenchmarkScreen extends StatefulWidget {
  const BenchmarkScreen({super.key});

  @override
  State<BenchmarkScreen> createState() => _BenchmarkScreenState();
}

class _BenchmarkScreenState extends State<BenchmarkScreen> {
  final _engine = ToneforgeEngine();
  Timer? _pollTimer;
  bool _running = false;
  bool _effectsOn = false;
  double _latencyMs = -1;
  int _xruns = 0;
  int _sampleRate = 0;

  @override
  void dispose() {
    _pollTimer?.cancel();
    if (_engine.isRunning) _engine.stop();
    super.dispose();
  }

  Future<void> _toggleStart() async {
    if (_running) {
      _engine.stop();
      _pollTimer?.cancel();
      setState(() {
        _running = false;
        _latencyMs = -1;
      });
      return;
    }

    final status = await Permission.microphone.request();
    if (!status.isGranted) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Permissão de microfone obrigatória')),
        );
      }
      return;
    }

    final result = _engine.start();
    if (result != 0) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Falha ao iniciar engine (code=$result)')),
        );
      }
      return;
    }
    setState(() => _running = true);
    _pollTimer = Timer.periodic(const Duration(milliseconds: 250), (_) {
      if (!mounted) return;
      setState(() {
        _latencyMs = _engine.latencyMs;
        _xruns = _engine.xrunCount;
        _sampleRate = _engine.sampleRate;
      });
    });
  }

  void _toggleEffects() {
    setState(() => _effectsOn = !_effectsOn);
    _engine
      ..gainEnabled = _effectsOn
      ..distortionEnabled = _effectsOn
      ..delayEnabled = _effectsOn
      ..reverbEnabled = _effectsOn
      ..chorusEnabled = _effectsOn
      ..flangerEnabled = _effectsOn
      ..phaserEnabled = _effectsOn
      ..eqEnabled = _effectsOn
      ..compressorEnabled = _effectsOn;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('ToneForge (Flutter) — Fase 2')),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text(
              'Prova de conceito: Flutter → FFI → Oboe → DSP C++.\n'
              'Mesma engine que o app legado, mas carregada via DynamicLibrary.',
              style: TextStyle(fontSize: 12),
            ),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: _toggleStart,
              child: Text(_running ? 'STOP' : 'START'),
            ),
            const SizedBox(height: 12),
            OutlinedButton(
              onPressed: _running ? _toggleEffects : null,
              child: Text(_effectsOn ? 'Desengajar efeitos' : 'Engajar TODOS os efeitos'),
            ),
            const SizedBox(height: 24),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.black54,
                borderRadius: BorderRadius.circular(8),
              ),
              child: DefaultTextStyle(
                style: const TextStyle(
                  fontFamily: 'monospace',
                  color: Colors.greenAccent,
                  fontSize: 13,
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('running      : $_running'),
                    Text('sample rate  : $_sampleRate Hz'),
                    Text('latency rt   : ${_latencyMs.toStringAsFixed(2)} ms'),
                    Text('xruns        : $_xruns'),
                    Text('effects on   : $_effectsOn'),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
