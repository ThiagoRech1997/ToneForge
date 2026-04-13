// Tela de Settings — placeholder mínimo da Fase 3. Hoje só mostra
// informações estáticas + estado do engine. Configurações reais (latency
// mode, theme, audio backend) ficam para iterações futuras quando a
// engine ganhar getters de configuração equivalentes ao LatencyManager
// do app legado.

import 'package:flutter/material.dart';

import '../../engine/engine.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final engine = ToneforgeEngine();
    final theme = Theme.of(context);
    final running = engine.isRunning;

    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Engine', style: theme.textTheme.titleMedium),
                  const SizedBox(height: 8),
                  _Row(label: 'Pipeline', value: running ? 'Rodando' : 'Parado'),
                  _Row(label: 'Sample rate', value: running ? '${engine.sampleRate} Hz' : '—'),
                  _Row(
                    label: 'Latência (round-trip)',
                    value: running ? '${engine.latencyMs.toStringAsFixed(2)} ms' : '—',
                  ),
                  _Row(label: 'Backend', value: 'Oboe (C++ via FFI)'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Sobre', style: theme.textTheme.titleMedium),
                  const SizedBox(height: 8),
                  const Text(
                    'ToneForge Flutter — port em andamento. O DSP roda no '
                    'mesmo engine C++ do app Android legado, agora consumido '
                    'via Dart FFI.',
                  ),
                  const SizedBox(height: 12),
                  Text(
                    'Configurações como modo de latência, tema escuro/claro, '
                    'backend de áudio, persistência de estado e MIDI ainda '
                    'não foram implementados nesta Fase 3.',
                    style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.outline),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _Row extends StatelessWidget {
  const _Row({required this.label, required this.value});
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Expanded(child: Text(label, style: const TextStyle(fontSize: 13))),
          Text(
            value,
            style: const TextStyle(fontFamily: 'monospace', fontSize: 13),
          ),
        ],
      ),
    );
  }
}
