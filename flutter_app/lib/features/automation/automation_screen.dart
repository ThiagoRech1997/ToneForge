// Tela de Automation — Fase 3. Auto-contida: 4 sliders dos parâmetros
// mais usados, transport (rec/play/stop/limpar), contador de eventos
// gravados e tempo decorrido. Não afeta o EffectsScreen.

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../theme/app_colors.dart';
import 'automation_cubit.dart';

class AutomationScreen extends StatelessWidget {
  const AutomationScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => AutomationCubit(),
      child: const _AutomationView(),
    );
  }
}

class _AutomationView extends StatelessWidget {
  const _AutomationView();

  Future<bool> _ensureMic(BuildContext context) async {
    final status = await Permission.microphone.request();
    if (!status.isGranted) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Permissão de microfone obrigatória')),
        );
      }
      return false;
    }
    return true;
  }

  String _formatMs(int ms) {
    final totalSec = ms / 1000;
    final s = totalSec.toInt();
    final mm = (s ~/ 60).toString().padLeft(2, '0');
    final ss = (s % 60).toString().padLeft(2, '0');
    final cs = ((totalSec - s) * 10).toInt();
    return '$mm:$ss.$cs';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Automation')),
      body: BlocBuilder<AutomationCubit, AutomationState>(
        builder: (context, state) {
          final cubit = context.read<AutomationCubit>();
          return Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                _Header(state: state, format: _formatMs),
                const SizedBox(height: 12),
                if (state.errorMessage != null)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: Text(
                      state.errorMessage!,
                      style: TextStyle(color: Theme.of(context).colorScheme.error),
                    ),
                  ),
                Expanded(
                  child: ListView(
                    children: [
                      for (final p in AutoParam.values)
                        _ParamSlider(
                          param: p,
                          value: state.values[p] ?? 0,
                          onChanged: (v) => cubit.setParam(p, v),
                        ),
                    ],
                  ),
                ),
                _Transport(
                  state: state,
                  onRecord: () async {
                    if (await _ensureMic(context)) {
                      await cubit.startRecording();
                    }
                  },
                  onStopRec: cubit.stopRecording,
                  onPlay: cubit.play,
                  onStopPlay: cubit.stopPlayback,
                  onClear: cubit.clear,
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header({required this.state, required this.format});
  final AutomationState state;
  final String Function(int) format;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color = state.mode == AutomationMode.recording
        ? AppColors.error
        : (state.mode == AutomationMode.playing ? AppColors.primary : AppColors.textTertiary);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Icon(_iconForMode(state.mode), color: color),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(_labelForMode(state.mode), style: theme.textTheme.titleMedium?.copyWith(color: color)),
                  Text(
                    '${state.events.length} eventos · total ${format(state.totalDurationMs)}',
                    style: theme.textTheme.bodySmall,
                  ),
                ],
              ),
            ),
            Text(
              format(state.elapsedMs),
              style: theme.textTheme.headlineSmall?.copyWith(fontFamily: 'monospace', color: color),
            ),
          ],
        ),
      ),
    );
  }

  IconData _iconForMode(AutomationMode m) {
    switch (m) {
      case AutomationMode.idle:
        return Icons.timeline;
      case AutomationMode.recording:
        return Icons.fiber_manual_record;
      case AutomationMode.playing:
        return Icons.play_arrow;
    }
  }

  String _labelForMode(AutomationMode m) {
    switch (m) {
      case AutomationMode.idle:
        return 'Idle';
      case AutomationMode.recording:
        return 'Gravando';
      case AutomationMode.playing:
        return 'Reproduzindo';
    }
  }
}

class _ParamSlider extends StatelessWidget {
  const _ParamSlider({required this.param, required this.value, required this.onChanged});
  final AutoParam param;
  final double value;
  final ValueChanged<double> onChanged;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            children: [
              Expanded(child: Text(param.label)),
              Text(
                value.toStringAsFixed(2),
                style: const TextStyle(fontFamily: 'monospace'),
              ),
            ],
          ),
          Slider(value: value, onChanged: onChanged),
        ],
      ),
    );
  }
}

class _Transport extends StatelessWidget {
  const _Transport({
    required this.state,
    required this.onRecord,
    required this.onStopRec,
    required this.onPlay,
    required this.onStopPlay,
    required this.onClear,
  });

  final AutomationState state;
  final VoidCallback onRecord;
  final VoidCallback onStopRec;
  final VoidCallback onPlay;
  final VoidCallback onStopPlay;
  final VoidCallback onClear;

  @override
  Widget build(BuildContext context) {
    final isRec = state.mode == AutomationMode.recording;
    final isPlay = state.mode == AutomationMode.playing;
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        FilledButton.tonalIcon(
          icon: Icon(isRec ? Icons.stop : Icons.fiber_manual_record, color: AppColors.error),
          label: Text(isRec ? 'Parar' : 'Gravar'),
          onPressed: isPlay ? null : (isRec ? onStopRec : onRecord),
        ),
        FilledButton.tonalIcon(
          icon: Icon(isPlay ? Icons.stop : Icons.play_arrow),
          label: Text(isPlay ? 'Parar' : 'Tocar'),
          onPressed: state.hasRecording && !isRec ? (isPlay ? onStopPlay : onPlay) : null,
        ),
        IconButton.outlined(
          icon: const Icon(Icons.delete_sweep),
          onPressed: state.hasRecording && !isRec && !isPlay ? onClear : null,
        ),
      ],
    );
  }
}
