// Looper: timer circular com progress ring em accentLooper, waveform card
// abaixo, transport Rec/Play/Clear e save to library. TFR-48 acrescenta
// slicing overlay — toggle de modo, tap no waveform para criar pontos,
// long-press para remover, e pads numerados para disparar segmentos.

import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../theme/app_colors.dart';
import '../../theme/app_spacing.dart';
import '../../theme/app_typography.dart';
import '../../widgets/tf_card.dart';
import '../../widgets/tf_pill.dart';
import '../../widgets/tf_section_label.dart';
import '../loop_library/loop_library_cubit.dart';
import 'looper_cubit.dart';

const Color _accent = AppColors.accentLooper;

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
    final total = seconds.toInt();
    final mm = (total ~/ 60).toString().padLeft(1, '0');
    final ss = (total % 60).toString().padLeft(2, '0');
    final tenths = ((seconds - total) * 10).clamp(0, 9).toInt();
    return '$mm:$ss.$tenths';
  }

  String _formatLen(double seconds) => '${seconds.toStringAsFixed(1)}s loop';

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

  Future<void> _onSaveLoop(BuildContext context, LooperState state) async {
    if (!state.hasContent) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Grave um loop primeiro')),
      );
      return;
    }
    final controller = TextEditingController(
      text: 'loop_${DateTime.now().millisecondsSinceEpoch}',
    );
    final name = await showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.surface,
        title: const Text('Salvar loop'),
        content: TextField(
          controller: controller,
          autofocus: true,
          decoration: const InputDecoration(labelText: 'Nome'),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(ctx, controller.text.trim()),
            child: const Text('Salvar'),
          ),
        ],
      ),
    );
    if (name == null || name.isEmpty) return;
    final lib = LoopLibraryCubit();
    try {
      final filename = await lib.saveCurrentLoop(name);
      if (context.mounted) {
        final msg = filename != null
            ? 'Loop salvo como "$filename"'
            : (lib.state.errorMessage ?? 'Falha ao salvar');
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(msg)));
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
            builder: (context, state) => Padding(
              padding: const EdgeInsets.only(right: AppSpacing.lg),
              child: Center(
                child: TfPill(
                  label: state.hasContent ? 'Ready' : 'Empty',
                  accent: _accent,
                  selected: state.hasContent,
                ),
              ),
            ),
          ),
          BlocBuilder<LooperCubit, LooperState>(
            builder: (context, state) => IconButton(
              icon: const Icon(Icons.save_outlined),
              tooltip: 'Salvar loop',
              onPressed:
                  state.hasContent ? () => _onSaveLoop(context, state) : null,
            ),
          ),
        ],
      ),
      body: BlocBuilder<LooperCubit, LooperState>(
        builder: (context, state) {
          final cubit = context.read<LooperCubit>();
          return SafeArea(
            child: SingleChildScrollView(
              padding: const EdgeInsets.fromLTRB(
                AppSpacing.xl,
                AppSpacing.xl,
                AppSpacing.xl,
                AppSpacing.xl,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Center(
                    child: _CircularTimer(
                      position: _formatTime(state.positionSeconds),
                      length: _formatLen(state.lengthSeconds),
                      progress: state.progress,
                    ),
                  ),
                  const SizedBox(height: AppSpacing.xl),
                  _Transport(
                    state: state,
                    onRecord: () async {
                      if (await _ensureMic(context)) {
                        await cubit.startRecording();
                      }
                    },
                    onStopRec: cubit.stopRecording,
                    onPlay: cubit.startPlayback,
                    onStopPlay: cubit.stopPlayback,
                    onClear: cubit.clear,
                  ),
                  const SizedBox(height: AppSpacing.xl),
                  if (state.errorMessage != null)
                    Padding(
                      padding: const EdgeInsets.only(bottom: AppSpacing.md),
                      child: Text(
                        state.errorMessage!,
                        style: AppTypography.caption
                            .copyWith(color: AppColors.error),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  TfCard(
                    accent: _accent,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            const Expanded(child: TfSectionLabel('Waveform')),
                            TfPill(
                              label: state.slicingEnabled ? 'Slicing on' : 'Slicing',
                              accent: _accent,
                              selected: state.slicingEnabled,
                              onTap: state.hasContent
                                  ? () => cubit
                                      .setSlicingEnabled(!state.slicingEnabled)
                                  : null,
                            ),
                          ],
                        ),
                        const SizedBox(height: AppSpacing.sm),
                        if (state.slicingEnabled)
                          Padding(
                            padding:
                                const EdgeInsets.only(bottom: AppSpacing.sm),
                            child: Text(
                              'Toque para marcar · segure para remover',
                              style: AppTypography.caption,
                            ),
                          ),
                        _WaveformArea(state: state, cubit: cubit),
                        if (state.slicingEnabled && state.hasContent) ...[
                          const SizedBox(height: AppSpacing.md),
                          _SlicePads(state: state, cubit: cubit),
                          const SizedBox(height: AppSpacing.sm),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.end,
                            children: [
                              TextButton.icon(
                                onPressed: state.slicePoints.isEmpty
                                    ? null
                                    : cubit.clearSlicePoints,
                                icon: const Icon(Icons.close, size: 16),
                                label: const Text('Limpar pontos'),
                              ),
                            ],
                          ),
                        ],
                      ],
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}

class _WaveformArea extends StatelessWidget {
  const _WaveformArea({required this.state, required this.cubit});

  final LooperState state;
  final LooperCubit cubit;

  int _tapFrame(double dx, double width) {
    if (width <= 0 || state.lengthFrames <= 0) return -1;
    final clamped = dx.clamp(0.0, width);
    return ((clamped / width) * state.lengthFrames).round();
  }

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 120,
      child: LayoutBuilder(
        builder: (context, constraints) {
          if (state.waveform.isEmpty) {
            return Center(
              child: Text(
                state.mode == LooperMode.recording
                    ? 'Gravando…'
                    : 'Pressione gravar para começar',
                style: AppTypography.caption,
              ),
            );
          }
          final painter = _WaveformPainter(
            samples: state.waveform,
            progress: state.progress,
            waveColor: _accent,
            playheadColor: AppColors.textPrimary,
            lengthFrames: state.lengthFrames,
            slicePoints: state.slicePoints,
            showSlices: state.slicingEnabled,
            activeBoundaries:
                state.slicingEnabled ? state.sliceBoundaries : const [],
            activeSliceIndex: state.activeSliceIndex,
          );
          final paint = CustomPaint(
            painter: painter,
            size: Size.infinite,
          );
          if (!state.slicingEnabled) return paint;
          return GestureDetector(
            behavior: HitTestBehavior.opaque,
            onTapUp: (details) {
              final frame = _tapFrame(
                details.localPosition.dx,
                constraints.maxWidth,
              );
              if (frame >= 0) cubit.addSlicePoint(frame);
            },
            onLongPressStart: (details) {
              final frame = _tapFrame(
                details.localPosition.dx,
                constraints.maxWidth,
              );
              if (frame >= 0) cubit.removeSlicePointNear(frame);
            },
            child: paint,
          );
        },
      ),
    );
  }
}

class _SlicePads extends StatelessWidget {
  const _SlicePads({required this.state, required this.cubit});

  final LooperState state;
  final LooperCubit cubit;

  @override
  Widget build(BuildContext context) {
    final n = state.numSlices;
    if (n <= 0) {
      return Text(
        'Nenhum slice definido ainda',
        style: AppTypography.caption,
      );
    }
    return Wrap(
      spacing: AppSpacing.sm,
      runSpacing: AppSpacing.sm,
      children: List.generate(n, (i) {
        final active = state.activeSliceIndex == i;
        return _SlicePadButton(
          index: i + 1,
          active: active,
          onTap: () => cubit.triggerSlice(i),
        );
      }),
    );
  }
}

class _SlicePadButton extends StatelessWidget {
  const _SlicePadButton({
    required this.index,
    required this.active,
    required this.onTap,
  });

  final int index;
  final bool active;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 120),
        width: 52,
        height: 52,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(10),
          color: active ? _accent.withValues(alpha: 0.22) : AppColors.elevated,
          border: Border.all(
            color: active ? _accent : AppColors.muted,
            width: active ? 2 : 1,
          ),
        ),
        alignment: Alignment.center,
        child: Text(
          '$index',
          style: AppTypography.monoLarge.copyWith(
            fontSize: 20,
            color: active ? _accent : AppColors.textPrimary,
          ),
        ),
      ),
    );
  }
}

class _CircularTimer extends StatelessWidget {
  const _CircularTimer({
    required this.position,
    required this.length,
    required this.progress,
  });
  final String position;
  final String length;
  final double progress;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 240,
      height: 240,
      child: CustomPaint(
        painter: _ProgressRingPainter(
          progress: progress,
          color: _accent,
          trackColor: AppColors.muted,
        ),
        child: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                position,
                style: AppTypography.monoLarge.copyWith(
                  fontSize: 56,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: AppSpacing.sm),
              Text(
                '/ $length',
                style: AppTypography.caption.copyWith(color: _accent),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ProgressRingPainter extends CustomPainter {
  _ProgressRingPainter({
    required this.progress,
    required this.color,
    required this.trackColor,
  });

  final double progress;
  final Color color;
  final Color trackColor;
  static const double _stroke = 8;

  @override
  void paint(Canvas canvas, Size size) {
    final center = size.center(Offset.zero);
    final radius = (size.shortestSide - _stroke) / 2;
    final track = Paint()
      ..color = trackColor
      ..strokeWidth = _stroke
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;
    canvas.drawCircle(center, radius, track);

    if (progress > 0) {
      final arc = Paint()
        ..color = color
        ..strokeWidth = _stroke
        ..style = PaintingStyle.stroke
        ..strokeCap = StrokeCap.round;
      canvas.drawArc(
        Rect.fromCircle(center: center, radius: radius),
        -math.pi / 2,
        math.pi * 2 * progress.clamp(0.0, 1.0),
        false,
        arc,
      );
    }
  }

  @override
  bool shouldRepaint(covariant _ProgressRingPainter old) =>
      old.progress != progress || old.color != color;
}

class _WaveformPainter extends CustomPainter {
  _WaveformPainter({
    required this.samples,
    required this.progress,
    required this.waveColor,
    required this.playheadColor,
    required this.lengthFrames,
    required this.slicePoints,
    required this.showSlices,
    required this.activeBoundaries,
    required this.activeSliceIndex,
  });

  final List<double> samples;
  final double progress;
  final Color waveColor;
  final Color playheadColor;
  final int lengthFrames;
  final List<int> slicePoints;
  final bool showSlices;
  final List<int> activeBoundaries;
  final int activeSliceIndex;

  @override
  void paint(Canvas canvas, Size size) {
    if (samples.isEmpty) return;
    final mid = size.height / 2;

    // Highlight do slice ativo, se houver.
    if (showSlices &&
        activeSliceIndex >= 0 &&
        activeSliceIndex < activeBoundaries.length - 1 &&
        lengthFrames > 0) {
      final startF = activeBoundaries[activeSliceIndex];
      final endF = activeBoundaries[activeSliceIndex + 1];
      final x0 = (startF / lengthFrames) * size.width;
      final x1 = (endF / lengthFrames) * size.width;
      final fill = Paint()..color = waveColor.withValues(alpha: 0.14);
      canvas.drawRect(Rect.fromLTRB(x0, 0, x1, size.height), fill);
    }

    // Waveform peaks.
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

    // Slice markers (linha vertical tracejada + chapéu triangular no topo).
    if (showSlices && lengthFrames > 0) {
      final markerPaint = Paint()
        ..color = waveColor.withValues(alpha: 0.85)
        ..strokeWidth = 2;
      final headPaint = Paint()..color = waveColor;
      for (final p in slicePoints) {
        final x = (p / lengthFrames) * size.width;
        // Linha tracejada manual — 4px on / 3px off.
        double y = 0;
        while (y < size.height) {
          canvas.drawLine(
            Offset(x, y),
            Offset(x, math.min(y + 4, size.height)),
            markerPaint,
          );
          y += 7;
        }
        final path = Path()
          ..moveTo(x - 4, 0)
          ..lineTo(x + 4, 0)
          ..lineTo(x, 6)
          ..close();
        canvas.drawPath(path, headPaint);
      }
    }

    if (progress > 0) {
      final playheadX = size.width * progress;
      final playPaint = Paint()
        ..color = playheadColor
        ..strokeWidth = 2;
      canvas.drawLine(
        Offset(playheadX, 0),
        Offset(playheadX, size.height),
        playPaint,
      );
    }
  }

  @override
  bool shouldRepaint(covariant _WaveformPainter old) {
    return old.samples != samples ||
        old.progress != progress ||
        old.slicePoints != slicePoints ||
        old.showSlices != showSlices ||
        old.activeSliceIndex != activeSliceIndex ||
        old.lengthFrames != lengthFrames;
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
          icon: isRecording ? Icons.stop_rounded : Icons.fiber_manual_record,
          label: isRecording ? 'Parar' : 'Gravar',
          color: AppColors.error,
          hero: true,
          onPressed: isPlaying ? null : (isRecording ? onStopRec : onRecord),
        ),
        _TransportButton(
          icon: isPlaying ? Icons.pause_rounded : Icons.play_arrow_rounded,
          label: isPlaying ? 'Pause' : 'Tocar',
          color: _accent,
          onPressed: canPlay ? (isPlaying ? onStopPlay : onPlay) : null,
        ),
        _TransportButton(
          icon: Icons.delete_sweep_outlined,
          label: 'Limpar',
          color: AppColors.textSecondary,
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
    this.hero = false,
  });

  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback? onPressed;
  final bool hero;

  @override
  Widget build(BuildContext context) {
    final enabled = onPressed != null;
    final size = hero ? 72.0 : 56.0;
    return Column(
      children: [
        GestureDetector(
          onTap: onPressed,
          child: Container(
            width: size,
            height: size,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: enabled
                  ? (hero ? color : AppColors.elevated)
                  : AppColors.elevated,
              border: Border.all(
                color: enabled ? color : AppColors.muted,
                width: 1.5,
              ),
            ),
            child: Icon(
              icon,
              size: hero ? 34 : 26,
              color: enabled
                  ? (hero ? AppColors.textPrimary : color)
                  : AppColors.textTertiary,
            ),
          ),
        ),
        const SizedBox(height: AppSpacing.sm),
        Text(
          label,
          style: AppTypography.caption.copyWith(
            color: enabled ? AppColors.textSecondary : AppColors.textTertiary,
          ),
        ),
      ],
    );
  }
}
