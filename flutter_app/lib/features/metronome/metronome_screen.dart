// Tela do metrônomo. Material 3 com BPM grande, presets, time signature,
// volume e pulso visual sincronizado com o scheduler do Cubit.
// Ver Fase 3.Metronome.

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'metronome_cubit.dart';

class MetronomeScreen extends StatelessWidget {
  const MetronomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => MetronomeCubit(),
      child: const _MetronomeView(),
    );
  }
}

class _MetronomeView extends StatelessWidget {
  const _MetronomeView();

  static const _bpmPresets = [60, 80, 100, 120];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Metronome')),
      body: BlocBuilder<MetronomeCubit, MetronomeState>(
        builder: (context, state) {
          final cubit = context.read<MetronomeCubit>();
          return SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                _BpmDisplay(state: state, cubit: cubit),
                const SizedBox(height: 16),
                _PresetsRow(presets: _bpmPresets, current: state.bpm, onTap: cubit.setBpm),
                const SizedBox(height: 24),
                _BeatIndicator(state: state),
                const SizedBox(height: 24),
                _TimeSignatureRow(state: state, cubit: cubit),
                const SizedBox(height: 24),
                _VolumeRow(state: state, cubit: cubit),
                const SizedBox(height: 32),
                if (state.errorMessage != null)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: Text(
                      state.errorMessage!,
                      style: TextStyle(color: Theme.of(context).colorScheme.error),
                    ),
                  ),
                FilledButton.tonalIcon(
                  onPressed: () => state.isPlaying ? cubit.stop() : cubit.start(),
                  icon: Icon(state.isPlaying ? Icons.stop : Icons.play_arrow),
                  label: Text(state.isPlaying ? 'Parar' : 'Tocar'),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _BpmDisplay extends StatelessWidget {
  const _BpmDisplay({required this.state, required this.cubit});

  final MetronomeState state;
  final MetronomeCubit cubit;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        IconButton.filledTonal(
          onPressed: cubit.decreaseBpm,
          iconSize: 32,
          icon: const Icon(Icons.remove),
        ),
        const SizedBox(width: 24),
        Column(
          children: [
            Text(
              '${state.bpm}',
              style: theme.textTheme.displayLarge?.copyWith(
                fontSize: 96,
                fontWeight: FontWeight.bold,
                color: theme.colorScheme.primary,
              ),
            ),
            Text('BPM', style: theme.textTheme.titleMedium),
          ],
        ),
        const SizedBox(width: 24),
        IconButton.filledTonal(
          onPressed: cubit.increaseBpm,
          iconSize: 32,
          icon: const Icon(Icons.add),
        ),
      ],
    );
  }
}

class _PresetsRow extends StatelessWidget {
  const _PresetsRow({required this.presets, required this.current, required this.onTap});

  final List<int> presets;
  final int current;
  final ValueChanged<int> onTap;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        for (final preset in presets) ...[
          ChoiceChip(
            label: Text('$preset'),
            selected: preset == current,
            onSelected: (_) => onTap(preset),
          ),
          if (preset != presets.last) const SizedBox(width: 8),
        ],
      ],
    );
  }
}

class _BeatIndicator extends StatelessWidget {
  const _BeatIndicator({required this.state});

  final MetronomeState state;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Wrap(
      alignment: WrapAlignment.center,
      spacing: 12,
      children: List.generate(state.timeSignature, (i) {
        final isCurrent = state.isPlaying && i == state.currentBeat;
        final isDownbeat = i == 0;
        final base = isDownbeat ? theme.colorScheme.tertiary : theme.colorScheme.primary;
        return AnimatedContainer(
          duration: const Duration(milliseconds: 120),
          width: isCurrent ? 32 : 20,
          height: isCurrent ? 32 : 20,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: isCurrent ? base : base.withValues(alpha: 0.25),
          ),
        );
      }),
    );
  }
}

class _TimeSignatureRow extends StatelessWidget {
  const _TimeSignatureRow({required this.state, required this.cubit});

  final MetronomeState state;
  final MetronomeCubit cubit;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        const Text('Compasso:'),
        const SizedBox(width: 16),
        IconButton.outlined(
          onPressed: cubit.decreaseTimeSignature,
          icon: const Icon(Icons.remove),
        ),
        SizedBox(
          width: 64,
          child: Text(
            '${state.timeSignature}/4',
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.titleLarge,
          ),
        ),
        IconButton.outlined(
          onPressed: cubit.increaseTimeSignature,
          icon: const Icon(Icons.add),
        ),
      ],
    );
  }
}

class _VolumeRow extends StatelessWidget {
  const _VolumeRow({required this.state, required this.cubit});

  final MetronomeState state;
  final MetronomeCubit cubit;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        const Icon(Icons.volume_down),
        Expanded(
          child: Slider(
            value: state.volume,
            onChanged: cubit.setVolume,
          ),
        ),
        const Icon(Icons.volume_up),
      ],
    );
  }
}
