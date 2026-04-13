// Tela do Tuner. Material 3, single column: nota grande, frequência,
// indicador de cents com cor por accuracy, botão start/stop. Ver Fase
// 3.Tuner. Equivalente ao TunerFragmentRefactored mas em Flutter.

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:permission_handler/permission_handler.dart';

import 'note_math.dart';
import 'tuner_cubit.dart';

class TunerScreen extends StatelessWidget {
  const TunerScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => TunerCubit(),
      child: const _TunerView(),
    );
  }
}

class _TunerView extends StatelessWidget {
  const _TunerView();

  Future<void> _onToggle(BuildContext context) async {
    final cubit = context.read<TunerCubit>();
    if (cubit.state.isRunning) {
      await cubit.stop();
      return;
    }
    final status = await Permission.microphone.request();
    if (!status.isGranted) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Permissão de microfone obrigatória')),
        );
      }
      return;
    }
    await cubit.start();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Tuner')),
      body: BlocBuilder<TunerCubit, TunerState>(
        builder: (context, state) {
          return Padding(
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
            child: Column(
              children: [
                Expanded(child: _NoteDisplay(reading: state.reading, isRunning: state.isRunning)),
                if (state.errorMessage != null)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: Text(
                      state.errorMessage!,
                      style: TextStyle(color: Theme.of(context).colorScheme.error),
                    ),
                  ),
                FilledButton.tonalIcon(
                  onPressed: () => _onToggle(context),
                  icon: Icon(state.isRunning ? Icons.stop : Icons.mic),
                  label: Text(state.isRunning ? 'Parar afinador' : 'Iniciar afinador'),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _NoteDisplay extends StatelessWidget {
  const _NoteDisplay({required this.reading, required this.isRunning});

  final NoteReading reading;
  final bool isRunning;

  Color _accuracyColor(BuildContext context) {
    switch (reading.accuracy) {
      case TunerAccuracy.perfect:
        return Colors.greenAccent;
      case TunerAccuracy.good:
        return Colors.lightGreen;
      case TunerAccuracy.fair:
        return Colors.amber;
      case TunerAccuracy.poor:
        return Colors.redAccent;
      case TunerAccuracy.none:
        return Theme.of(context).colorScheme.outline;
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color = _accuracyColor(context);

    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          reading.noteWithOctave,
          style: theme.textTheme.displayLarge?.copyWith(
            fontSize: 144,
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
        const SizedBox(height: 12),
        Text(
          reading.hasSignal
              ? '${reading.frequency.toStringAsFixed(2)} Hz'
              : (isRunning ? 'Toque uma corda...' : 'Pressione iniciar'),
          style: theme.textTheme.titleMedium,
        ),
        const SizedBox(height: 32),
        _CentsIndicator(cents: reading.cents, color: color, hasSignal: reading.hasSignal),
      ],
    );
  }
}

class _CentsIndicator extends StatelessWidget {
  const _CentsIndicator({required this.cents, required this.color, required this.hasSignal});

  final double cents;
  final Color color;
  final bool hasSignal;

  static const double _range = 50.0;

  @override
  Widget build(BuildContext context) {
    final clamped = cents.clamp(-_range, _range);
    final normalized = (clamped + _range) / (2 * _range); // 0..1

    return Column(
      children: [
        SizedBox(
          height: 36,
          child: Stack(
            alignment: Alignment.center,
            children: [
              Container(
                height: 4,
                color: Theme.of(context).colorScheme.outlineVariant,
              ),
              // Marcador central (afinação perfeita)
              Container(width: 2, height: 24, color: Theme.of(context).colorScheme.onSurface),
              // Indicador da nota atual
              if (hasSignal)
                Align(
                  alignment: Alignment(normalized * 2 - 1, 0),
                  child: Container(
                    width: 6,
                    height: 32,
                    decoration: BoxDecoration(
                      color: color,
                      borderRadius: BorderRadius.circular(3),
                    ),
                  ),
                ),
            ],
          ),
        ),
        const SizedBox(height: 8),
        Text(
          hasSignal ? '${cents >= 0 ? '+' : ''}${cents.toStringAsFixed(0)} cents' : '',
          style: TextStyle(color: color, fontFamily: 'monospace'),
        ),
      ],
    );
  }
}
