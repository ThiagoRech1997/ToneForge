// Tela do Tuner seguindo o Design System Paper:
// nota gigante em accentTuner, barra FLAT→SHARP com marker, readout de
// frequência + referência 440 Hz, pill row das 6 cordas padrão de guitarra
// e CTA gradiente start/stop.

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../theme/app_colors.dart';
import '../../theme/app_spacing.dart';
import '../../theme/app_typography.dart';
import '../../widgets/tf_pill.dart';
import '../../widgets/tf_primary_button.dart';
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

  Color _accuracyColor(NoteReading reading) {
    switch (reading.accuracy) {
      case TunerAccuracy.perfect:
        return AppColors.success;
      case TunerAccuracy.good:
        return AppColors.success;
      case TunerAccuracy.fair:
        return AppColors.warning;
      case TunerAccuracy.poor:
        return AppColors.error;
      case TunerAccuracy.none:
        return AppColors.accentTuner;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Tuner'),
        actions: [
          BlocBuilder<TunerCubit, TunerState>(
            builder: (context, state) => Padding(
              padding: const EdgeInsets.only(right: AppSpacing.lg),
              child: Center(
                child: TfPill(
                  label: state.isRunning ? 'Listening…' : 'Idle',
                  accent: state.isRunning
                      ? AppColors.success
                      : AppColors.textTertiary,
                  selected: state.isRunning,
                ),
              ),
            ),
          ),
        ],
      ),
      body: BlocBuilder<TunerCubit, TunerState>(
        builder: (context, state) {
          final color = _accuracyColor(state.reading);
          return SafeArea(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(
                AppSpacing.xl,
                AppSpacing.xxl,
                AppSpacing.xl,
                AppSpacing.xl,
              ),
              child: Column(
                children: [
                  Expanded(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        _NoteDisplay(reading: state.reading, color: color),
                        const SizedBox(height: AppSpacing.xxl),
                        _CentsBar(
                          cents: state.reading.cents,
                          color: color,
                          hasSignal: state.reading.hasSignal,
                        ),
                        const SizedBox(height: AppSpacing.lg),
                        Text(
                          state.reading.hasSignal
                              ? '${state.reading.frequency.toStringAsFixed(1)} Hz'
                              : (state.isRunning
                                  ? 'Toque uma corda…'
                                  : 'Pressione iniciar'),
                          style: AppTypography.cardTitle,
                        ),
                        const SizedBox(height: 2),
                        Text(
                          'Reference: 440 Hz',
                          style: AppTypography.caption,
                        ),
                      ],
                    ),
                  ),
                  if (state.errorMessage != null)
                    Padding(
                      padding: const EdgeInsets.only(bottom: AppSpacing.md),
                      child: Text(
                        state.errorMessage!,
                        style: AppTypography.caption
                            .copyWith(color: AppColors.error),
                      ),
                    ),
                  _StringRow(
                    currentNote: state.reading.hasSignal
                        ? state.reading.noteName
                        : '',
                  ),
                  const SizedBox(height: AppSpacing.lg),
                  TfPrimaryButton(
                    label: state.isRunning ? 'Parar afinador' : 'Start Tuning',
                    icon: state.isRunning ? Icons.stop : Icons.mic_rounded,
                    accent: AppColors.accentTuner,
                    onPressed: () => _onToggle(context),
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

class _NoteDisplay extends StatelessWidget {
  const _NoteDisplay({required this.reading, required this.color});

  final NoteReading reading;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(
          reading.hasSignal ? reading.noteName : '—',
          style: TextStyle(
            fontFamily: 'SpaceGrotesk',
            fontSize: 160,
            height: 1,
            fontWeight: FontWeight.w700,
            color: reading.hasSignal ? color : AppColors.textTertiary,
            letterSpacing: -4,
          ),
        ),
        const SizedBox(height: AppSpacing.sm),
        Text(
          reading.hasSignal ? reading.noteWithOctave : '---',
          style: AppTypography.bodySecondary,
        ),
      ],
    );
  }
}

class _CentsBar extends StatelessWidget {
  const _CentsBar({
    required this.cents,
    required this.color,
    required this.hasSignal,
  });

  final double cents;
  final Color color;
  final bool hasSignal;

  static const double _range = 50;

  @override
  Widget build(BuildContext context) {
    final clamped = cents.clamp(-_range, _range);
    final normalized = (clamped + _range) / (2 * _range);

    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'FLAT',
              style: AppTypography.sectionLabel.copyWith(
                color: AppColors.error.withValues(alpha: 0.7),
              ),
            ),
            Text(
              'SHARP',
              style: AppTypography.sectionLabel.copyWith(
                color: AppColors.error.withValues(alpha: 0.7),
              ),
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.sm),
        SizedBox(
          height: 20,
          child: LayoutBuilder(
            builder: (context, constraints) {
              return Stack(
                alignment: Alignment.center,
                children: [
                  Container(
                    height: 6,
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        colors: [
                          AppColors.error.withValues(alpha: 0.4),
                          AppColors.muted,
                          AppColors.error.withValues(alpha: 0.4),
                        ],
                        stops: const [0, 0.5, 1],
                      ),
                      borderRadius: BorderRadius.circular(3),
                    ),
                  ),
                  Container(
                    width: 2,
                    height: 16,
                    color: AppColors.textPrimary,
                  ),
                  if (hasSignal)
                    Positioned(
                      left: normalized * constraints.maxWidth - 8,
                      child: Container(
                        width: 16,
                        height: 16,
                        decoration: BoxDecoration(
                          color: color,
                          shape: BoxShape.circle,
                          border: Border.all(
                            color: AppColors.textPrimary,
                            width: 2,
                          ),
                        ),
                      ),
                    ),
                ],
              );
            },
          ),
        ),
        const SizedBox(height: AppSpacing.md),
        Text(
          hasSignal
              ? '${cents >= 0 ? '+' : ''}${cents.toStringAsFixed(0)} cents'
              : '',
          style: AppTypography.cardTitle.copyWith(color: color),
        ),
      ],
    );
  }
}

class _StringRow extends StatelessWidget {
  const _StringRow({required this.currentNote});

  final String currentNote;

  static const _strings = ['E', 'A', 'D', 'G', 'B', 'E'];

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        for (var i = 0; i < _strings.length; i++)
          _StringPill(
            label: _strings[i],
            highlighted: currentNote.isNotEmpty && currentNote == _strings[i],
          ),
      ],
    );
  }
}

class _StringPill extends StatelessWidget {
  const _StringPill({required this.label, required this.highlighted});

  final String label;
  final bool highlighted;

  @override
  Widget build(BuildContext context) {
    final accent = AppColors.accentTuner;
    return Container(
      width: 44,
      height: 44,
      decoration: BoxDecoration(
        color: highlighted
            ? accent.withValues(alpha: 0.25)
            : AppColors.elevated,
        borderRadius: BorderRadius.circular(AppRadius.md),
        border: Border.all(
          color: highlighted ? accent : Colors.transparent,
          width: 1.5,
        ),
      ),
      alignment: Alignment.center,
      child: Text(
        label,
        style: AppTypography.cardTitle.copyWith(
          color: highlighted ? accent : AppColors.textSecondary,
        ),
      ),
    );
  }
}
