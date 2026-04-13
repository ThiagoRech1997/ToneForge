// LooperScreen — MVP. Transport básico (rec/play/stop/clear) + tempo
// total/atual + WaveformPainter custom (CustomPainter usando o snapshot
// do mix vindo do engine via FFI). Ver Fase 3.Looper.

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:permission_handler/permission_handler.dart';

import '../loop_library/loop_library_cubit.dart';
import 'looper_cubit.dart';

class LooperScreen extends StatelessWidget {
  const LooperScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => LooperCubit(),
      child: const _LooperView(),
    );
  }
}

class _LooperView extends StatelessWidget {
  const _LooperView();

  String _formatTime(double seconds) {
    final s = seconds.toInt();
    final mm = (s ~/ 60).toString().padLeft(2, '0');
    final ss = (s % 60).toString().padLeft(2, '0');
    return '$mm:$ss';
  }

  Future<bool> _ensureMicPermission(BuildContext context) async {
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

  Future<void> _onSaveLoop(BuildContext context, LooperState state) async {
    if (!state.hasContent) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Grave um loop primeiro')),
      );
      return;
    }
    final controller = TextEditingController(text: 'loop_${DateTime.now().millisecondsSinceEpoch}');
    final name = await showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Salvar loop'),
        content: TextField(
          controller: controller,
          autofocus: true,
          decoration: const InputDecoration(labelText: 'Nome'),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),
          FilledButton(
            onPressed: () => Navigator.pop(ctx, controller.text.trim()),
            child: const Text('Salvar'),
          ),
        ],
      ),
    );
    if (name == null || name.isEmpty) return;

    // Cria um Cubit transiente só para o save — não instalamos na árvore.
    final lib = LoopLibraryCubit();
    try {
      final filename = await lib.saveCurrentLoop(name);
      if (context.mounted) {
        final msg = filename != null
            ? 'Loop salvo como "$filename"'
            : (lib.state.errorMessage ?? 'Falha ao salvar');
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
      }
    } finally {
      await lib.close();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Looper'),
        actions: [
          BlocBuilder<LooperCubit, LooperState>(
            builder: (context, state) => IconButton(
              icon: const Icon(Icons.save_outlined),
              tooltip: 'Salvar loop como WAV',
              onPressed: state.hasContent ? () => _onSaveLoop(context, state) : null,
            ),
          ),
        ],
      ),
      body: BlocBuilder<LooperCubit, LooperState>(
        builder: (context, state) {
          final cubit = context.read<LooperCubit>();
          return Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                _WaveformPanel(state: state),
                const SizedBox(height: 16),
                _TimeReadout(state: state, format: _formatTime),
                const Spacer(),
                if (state.errorMessage != null)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: Text(
                      state.errorMessage!,
                      style: TextStyle(color: Theme.of(context).colorScheme.error),
                      textAlign: TextAlign.center,
                    ),
                  ),
                _Transport(
                  state: state,
                  onRecord: () async {
                    if (await _ensureMicPermission(context)) {
                      await cubit.startRecording();
                    }
                  },
                  onStopRec: cubit.stopRecording,
                  onPlay: cubit.startPlayback,
                  onStopPlay: cubit.stopPlayback,
                  onClear: cubit.clear,
                ),
                const SizedBox(height: 24),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _WaveformPanel extends StatelessWidget {
  const _WaveformPanel({required this.state});
  final LooperState state;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      height: 180,
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(12),
      ),
      padding: const EdgeInsets.all(12),
      child: state.waveform.isEmpty
          ? Center(
              child: Text(
                state.mode == LooperMode.recording ? 'Gravando…' : 'Pressione gravar',
                style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.outline),
              ),
            )
          : CustomPaint(
              painter: _WaveformPainter(
                samples: state.waveform,
                progress: state.progress,
                waveColor: theme.colorScheme.primary,
                playheadColor: theme.colorScheme.tertiary,
              ),
              size: Size.infinite,
            ),
    );
  }
}

class _WaveformPainter extends CustomPainter {
  _WaveformPainter({
    required this.samples,
    required this.progress,
    required this.waveColor,
    required this.playheadColor,
  });

  final List<double> samples;
  final double progress;
  final Color waveColor;
  final Color playheadColor;

  @override
  void paint(Canvas canvas, Size size) {
    if (samples.isEmpty) return;
    final mid = size.height / 2;
    final stepX = size.width / samples.length;
    final paint = Paint()
      ..color = waveColor
      ..strokeWidth = 1.5
      ..strokeCap = StrokeCap.round;

    for (var i = 0; i < samples.length; i++) {
      final amp = samples[i].clamp(0.0, 1.0) * mid;
      final x = i * stepX + stepX / 2;
      canvas.drawLine(Offset(x, mid - amp), Offset(x, mid + amp), paint);
    }

    if (progress > 0) {
      final playheadX = size.width * progress;
      final playPaint = Paint()
        ..color = playheadColor
        ..strokeWidth = 2;
      canvas.drawLine(Offset(playheadX, 0), Offset(playheadX, size.height), playPaint);
    }
  }

  @override
  bool shouldRepaint(covariant _WaveformPainter old) {
    return old.samples != samples || old.progress != progress;
  }
}

class _TimeReadout extends StatelessWidget {
  const _TimeReadout({required this.state, required this.format});

  final LooperState state;
  final String Function(double) format;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final positionStyle = theme.textTheme.headlineMedium?.copyWith(
      fontFamily: 'monospace',
      color: theme.colorScheme.primary,
    );
    final lengthStyle = theme.textTheme.bodyMedium?.copyWith(
      fontFamily: 'monospace',
      color: theme.colorScheme.outline,
    );
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.baseline,
      textBaseline: TextBaseline.alphabetic,
      children: [
        Text(format(state.positionSeconds), style: positionStyle),
        const SizedBox(width: 8),
        Text('/ ${format(state.lengthSeconds)}', style: lengthStyle),
      ],
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

  final LooperState state;
  final VoidCallback onRecord;
  final VoidCallback onStopRec;
  final VoidCallback onPlay;
  final VoidCallback onStopPlay;
  final VoidCallback onClear;

  @override
  Widget build(BuildContext context) {
    final isRecording = state.mode == LooperMode.recording;
    final isPlaying = state.mode == LooperMode.playing;
    final canPlay = state.hasContent && !isRecording;
    final canClear = state.hasContent && !isRecording && !isPlaying;

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        _TransportButton(
          icon: isRecording ? Icons.stop : Icons.fiber_manual_record,
          label: isRecording ? 'Parar' : 'Gravar',
          color: Colors.redAccent,
          onPressed: isPlaying ? null : (isRecording ? onStopRec : onRecord),
        ),
        _TransportButton(
          icon: isPlaying ? Icons.stop : Icons.play_arrow,
          label: isPlaying ? 'Parar' : 'Tocar',
          color: Theme.of(context).colorScheme.primary,
          onPressed: canPlay ? (isPlaying ? onStopPlay : onPlay) : null,
        ),
        _TransportButton(
          icon: Icons.delete_sweep,
          label: 'Limpar',
          color: Theme.of(context).colorScheme.outline,
          onPressed: canClear ? onClear : null,
        ),
      ],
    );
  }
}

class _TransportButton extends StatelessWidget {
  const _TransportButton({
    required this.icon,
    required this.label,
    required this.color,
    required this.onPressed,
  });

  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) {
    final enabled = onPressed != null;
    return Column(
      children: [
        SizedBox(
          width: 80,
          height: 80,
          child: FloatingActionButton(
            heroTag: label,
            backgroundColor: enabled ? color : color.withValues(alpha: 0.3),
            onPressed: onPressed,
            child: Icon(icon, size: 36),
          ),
        ),
        const SizedBox(height: 6),
        Text(label, style: TextStyle(color: enabled ? null : Theme.of(context).colorScheme.outline)),
      ],
    );
  }
}
