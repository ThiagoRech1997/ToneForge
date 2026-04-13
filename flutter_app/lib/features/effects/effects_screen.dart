// Tela dos 9 efeitos. Lista vertical de cards, sem expand/collapse para
// se manter próxima ao EffectsFragmentRefactored legado. Cada card tem
// um Switch + sliders/dropdown para os parâmetros do efeito.
// Reordenamento de cadeia (drag-and-drop) ainda não implementado.
// Ver Fase 3.Effects.

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:permission_handler/permission_handler.dart';

import 'effects_cubit.dart';
import 'models.dart';

class EffectsScreen extends StatelessWidget {
  const EffectsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => EffectsCubit(),
      child: const _EffectsView(),
    );
  }
}

class _EffectsView extends StatelessWidget {
  const _EffectsView();

  Future<void> _togglePipeline(BuildContext context, EffectsState state) async {
    final cubit = context.read<EffectsCubit>();
    if (state.pipelineRunning) {
      await cubit.stopPipeline();
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
    await cubit.startPipeline();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Effects')),
      floatingActionButton: BlocBuilder<EffectsCubit, EffectsState>(
        builder: (context, state) => FloatingActionButton.extended(
          onPressed: () => _togglePipeline(context, state),
          icon: Icon(state.pipelineRunning ? Icons.stop : Icons.play_arrow),
          label: Text(state.pipelineRunning ? 'Parar' : 'Iniciar'),
        ),
      ),
      body: BlocBuilder<EffectsCubit, EffectsState>(
        builder: (context, state) {
          final cubit = context.read<EffectsCubit>();
          return ListView(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 96),
            children: [
              if (state.errorMessage != null)
                Padding(
                  padding: const EdgeInsets.only(bottom: 12),
                  child: Text(
                    state.errorMessage!,
                    style: TextStyle(color: Theme.of(context).colorScheme.error),
                  ),
                ),
              _GainCard(state: state.gain, cubit: cubit),
              _DistortionCard(state: state.distortion, cubit: cubit),
              _DelayCard(state: state.delay, cubit: cubit),
              _ReverbCard(state: state.reverb, cubit: cubit),
              _ChorusCard(state: state.chorus, cubit: cubit),
              _FlangerCard(state: state.flanger, cubit: cubit),
              _PhaserCard(state: state.phaser, cubit: cubit),
              _EqCard(state: state.eq, cubit: cubit),
              _CompressorCard(state: state.compressor, cubit: cubit),
            ],
          );
        },
      ),
    );
  }
}

// ============================================================================
// Card base — header com Switch e bloco de filhos. Mantido fora dos cards
// específicos para que cada feito só descreva seus controles, não a moldura.
// ============================================================================

class _EffectCard extends StatelessWidget {
  const _EffectCard({
    required this.title,
    required this.enabled,
    required this.onEnabled,
    required this.children,
  });

  final String title;
  final bool enabled;
  final ValueChanged<bool> onEnabled;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    title,
                    style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
                  ),
                ),
                Switch(value: enabled, onChanged: onEnabled),
              ],
            ),
            const SizedBox(height: 4),
            AnimatedOpacity(
              duration: const Duration(milliseconds: 120),
              opacity: enabled ? 1.0 : 0.45,
              child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: children),
            ),
          ],
        ),
      ),
    );
  }
}

class _ParamSlider extends StatelessWidget {
  const _ParamSlider({
    required this.label,
    required this.value,
    required this.min,
    required this.max,
    required this.onChanged,
    this.format,
  });

  final String label;
  final double value;
  final double min;
  final double max;
  final ValueChanged<double> onChanged;
  final String Function(double)? format;

  @override
  Widget build(BuildContext context) {
    final formatter = format ?? (v) => v.toStringAsFixed(2);
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            children: [
              Expanded(child: Text(label, style: const TextStyle(fontSize: 13))),
              Text(
                formatter(value),
                style: const TextStyle(fontFamily: 'monospace', fontSize: 13),
              ),
            ],
          ),
          Slider(
            value: value.clamp(min, max),
            min: min,
            max: max,
            onChanged: onChanged,
          ),
        ],
      ),
    );
  }
}

class _TypeDropdown<T> extends StatelessWidget {
  const _TypeDropdown({
    required this.label,
    required this.value,
    required this.options,
    required this.onChanged,
  });

  final String label;
  final T value;
  final Map<T, String> options;
  final ValueChanged<T> onChanged;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Expanded(child: Text(label, style: const TextStyle(fontSize: 13))),
          DropdownButton<T>(
            value: value,
            items: [
              for (final entry in options.entries)
                DropdownMenuItem(value: entry.key, child: Text(entry.value)),
            ],
            onChanged: (v) {
              if (v != null) onChanged(v);
            },
          ),
        ],
      ),
    );
  }
}

// ============================================================================
// Cards específicos por efeito
// ============================================================================

class _GainCard extends StatelessWidget {
  const _GainCard({required this.state, required this.cubit});
  final GainConfig state;
  final EffectsCubit cubit;

  @override
  Widget build(BuildContext context) {
    return _EffectCard(
      title: 'Gain',
      enabled: state.enabled,
      onEnabled: cubit.setGainEnabled,
      children: [
        _ParamSlider(label: 'Level', value: state.level, min: 0, max: 1, onChanged: cubit.setGainLevel),
      ],
    );
  }
}

class _DistortionCard extends StatelessWidget {
  const _DistortionCard({required this.state, required this.cubit});
  final DistortionConfig state;
  final EffectsCubit cubit;

  @override
  Widget build(BuildContext context) {
    return _EffectCard(
      title: 'Distortion',
      enabled: state.enabled,
      onEnabled: cubit.setDistortionEnabled,
      children: [
        _TypeDropdown<int>(
          label: 'Type',
          value: state.type,
          options: {for (var i = 0; i < DistortionConfig.types.length; i++) i: DistortionConfig.types[i]},
          onChanged: cubit.setDistortionType,
        ),
        _ParamSlider(label: 'Amount', value: state.amount, min: 0, max: 1, onChanged: cubit.setDistortionAmount),
        _ParamSlider(label: 'Mix', value: state.mix, min: 0, max: 1, onChanged: cubit.setDistortionMix),
      ],
    );
  }
}

class _DelayCard extends StatelessWidget {
  const _DelayCard({required this.state, required this.cubit});
  final DelayConfig state;
  final EffectsCubit cubit;

  @override
  Widget build(BuildContext context) {
    return _EffectCard(
      title: 'Delay',
      enabled: state.enabled,
      onEnabled: cubit.setDelayEnabled,
      children: [
        _ParamSlider(
          label: 'Time',
          value: state.timeMs,
          min: 0,
          max: 1000,
          format: (v) => '${v.toStringAsFixed(0)} ms',
          onChanged: cubit.setDelayTimeMs,
        ),
        _ParamSlider(label: 'Feedback', value: state.feedback, min: 0, max: 1, onChanged: cubit.setDelayFeedback),
        _ParamSlider(label: 'Mix', value: state.mix, min: 0, max: 1, onChanged: cubit.setDelayMix),
      ],
    );
  }
}

class _ReverbCard extends StatelessWidget {
  const _ReverbCard({required this.state, required this.cubit});
  final ReverbConfig state;
  final EffectsCubit cubit;

  @override
  Widget build(BuildContext context) {
    return _EffectCard(
      title: 'Reverb',
      enabled: state.enabled,
      onEnabled: cubit.setReverbEnabled,
      children: [
        _TypeDropdown<int>(
          label: 'Type',
          value: state.type,
          options: {for (var i = 0; i < ReverbConfig.types.length; i++) i: ReverbConfig.types[i]},
          onChanged: cubit.setReverbType,
        ),
        _ParamSlider(label: 'Room', value: state.roomSize, min: 0, max: 1, onChanged: cubit.setReverbRoomSize),
        _ParamSlider(label: 'Damping', value: state.damping, min: 0, max: 1, onChanged: cubit.setReverbDamping),
        _ParamSlider(label: 'Mix', value: state.mix, min: 0, max: 1, onChanged: cubit.setReverbMix),
      ],
    );
  }
}

class _ChorusCard extends StatelessWidget {
  const _ChorusCard({required this.state, required this.cubit});
  final ModConfig state;
  final EffectsCubit cubit;

  @override
  Widget build(BuildContext context) {
    return _EffectCard(
      title: 'Chorus',
      enabled: state.enabled,
      onEnabled: cubit.setChorusEnabled,
      children: [
        _ParamSlider(label: 'Depth', value: state.depth, min: 0, max: 1, onChanged: cubit.setChorusDepth),
        _ParamSlider(
          label: 'Rate',
          value: state.rate,
          min: 0,
          max: 20,
          format: (v) => '${v.toStringAsFixed(2)} Hz',
          onChanged: cubit.setChorusRate,
        ),
        _ParamSlider(label: 'Mix', value: state.mix, min: 0, max: 1, onChanged: cubit.setChorusMix),
      ],
    );
  }
}

class _FlangerCard extends StatelessWidget {
  const _FlangerCard({required this.state, required this.cubit});
  final ModConfig state;
  final EffectsCubit cubit;

  @override
  Widget build(BuildContext context) {
    return _EffectCard(
      title: 'Flanger',
      enabled: state.enabled,
      onEnabled: cubit.setFlangerEnabled,
      children: [
        _ParamSlider(label: 'Depth', value: state.depth, min: 0, max: 1, onChanged: cubit.setFlangerDepth),
        _ParamSlider(
          label: 'Rate',
          value: state.rate,
          min: 0,
          max: 20,
          format: (v) => '${v.toStringAsFixed(2)} Hz',
          onChanged: cubit.setFlangerRate,
        ),
        _ParamSlider(label: 'Feedback', value: state.feedback, min: 0, max: 1, onChanged: cubit.setFlangerFeedback),
        _ParamSlider(label: 'Mix', value: state.mix, min: 0, max: 1, onChanged: cubit.setFlangerMix),
      ],
    );
  }
}

class _PhaserCard extends StatelessWidget {
  const _PhaserCard({required this.state, required this.cubit});
  final ModConfig state;
  final EffectsCubit cubit;

  @override
  Widget build(BuildContext context) {
    return _EffectCard(
      title: 'Phaser',
      enabled: state.enabled,
      onEnabled: cubit.setPhaserEnabled,
      children: [
        _ParamSlider(label: 'Depth', value: state.depth, min: 0, max: 1, onChanged: cubit.setPhaserDepth),
        _ParamSlider(
          label: 'Rate',
          value: state.rate,
          min: 0,
          max: 20,
          format: (v) => '${v.toStringAsFixed(2)} Hz',
          onChanged: cubit.setPhaserRate,
        ),
        _ParamSlider(label: 'Feedback', value: state.feedback, min: 0, max: 1, onChanged: cubit.setPhaserFeedback),
        _ParamSlider(label: 'Mix', value: state.mix, min: 0, max: 1, onChanged: cubit.setPhaserMix),
      ],
    );
  }
}

class _EqCard extends StatelessWidget {
  const _EqCard({required this.state, required this.cubit});
  final EqConfig state;
  final EffectsCubit cubit;

  @override
  Widget build(BuildContext context) {
    return _EffectCard(
      title: 'EQ (3-band)',
      enabled: state.enabled,
      onEnabled: cubit.setEqEnabled,
      children: [
        _ParamSlider(
          label: 'Low',
          value: state.low,
          min: -12,
          max: 12,
          format: (v) => '${v >= 0 ? '+' : ''}${v.toStringAsFixed(1)} dB',
          onChanged: cubit.setEqLow,
        ),
        _ParamSlider(
          label: 'Mid',
          value: state.mid,
          min: -12,
          max: 12,
          format: (v) => '${v >= 0 ? '+' : ''}${v.toStringAsFixed(1)} dB',
          onChanged: cubit.setEqMid,
        ),
        _ParamSlider(
          label: 'High',
          value: state.high,
          min: -12,
          max: 12,
          format: (v) => '${v >= 0 ? '+' : ''}${v.toStringAsFixed(1)} dB',
          onChanged: cubit.setEqHigh,
        ),
        _ParamSlider(label: 'Mix', value: state.mix, min: 0, max: 1, onChanged: cubit.setEqMix),
      ],
    );
  }
}

class _CompressorCard extends StatelessWidget {
  const _CompressorCard({required this.state, required this.cubit});
  final CompressorConfig state;
  final EffectsCubit cubit;

  @override
  Widget build(BuildContext context) {
    return _EffectCard(
      title: 'Compressor',
      enabled: state.enabled,
      onEnabled: cubit.setCompressorEnabled,
      children: [
        _ParamSlider(
          label: 'Threshold',
          value: state.thresholdDb,
          min: -60,
          max: 0,
          format: (v) => '${v.toStringAsFixed(1)} dB',
          onChanged: cubit.setCompressorThreshold,
        ),
        _ParamSlider(
          label: 'Ratio',
          value: state.ratio,
          min: 1,
          max: 20,
          format: (v) => '${v.toStringAsFixed(1)}:1',
          onChanged: cubit.setCompressorRatio,
        ),
        _ParamSlider(
          label: 'Attack',
          value: state.attackMs,
          min: 1,
          max: 100,
          format: (v) => '${v.toStringAsFixed(0)} ms',
          onChanged: cubit.setCompressorAttack,
        ),
        _ParamSlider(
          label: 'Release',
          value: state.releaseMs,
          min: 10,
          max: 1000,
          format: (v) => '${v.toStringAsFixed(0)} ms',
          onChanged: cubit.setCompressorRelease,
        ),
        _ParamSlider(label: 'Mix', value: state.mix, min: 0, max: 1, onChanged: cubit.setCompressorMix),
      ],
    );
  }
}
