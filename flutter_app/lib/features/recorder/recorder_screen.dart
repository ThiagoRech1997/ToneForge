// Recorder: timer gigante em accentRecorder, botão record circular com
// glow, card de detalhes e lista das gravações do diretório do app.

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../theme/app_colors.dart';
import '../../theme/app_spacing.dart';
import '../../theme/app_typography.dart';
import '../../widgets/tf_card.dart';
import '../../widgets/tf_pill.dart';
import '../../widgets/tf_section_label.dart';
import 'recorder_cubit.dart';

const Color _accent = AppColors.accentRecorder;

class RecorderScreen extends StatelessWidget {
  const RecorderScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => RecorderCubit(),
      child: const _RecorderView(),
    );
  }
}

class _RecorderView extends StatelessWidget {
  const _RecorderView();

  String _formatTime(double seconds) {
    final s = seconds.toInt();
    final mm = (s ~/ 60).toString().padLeft(1, '0');
    final ss = (s % 60).toString().padLeft(2, '0');
    final cs = ((seconds - s) * 10).clamp(0, 9).toInt();
    return '$mm:$ss.$cs';
  }

  String _formatBytes(int bytes) {
    if (bytes < 1024) return '$bytes B';
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
    return '${(bytes / (1024 * 1024)).toStringAsFixed(1)} MB';
  }

  Future<void> _toggleRecording(
      BuildContext context, RecorderState state) async {
    final cubit = context.read<RecorderCubit>();
    if (state.isRecording) {
      await cubit.stopRecording();
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
    await cubit.startRecording();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Recorder'),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: AppSpacing.lg),
            child: Center(
              child: TfPill(
                label: 'WAV',
                accent: _accent,
                selected: true,
              ),
            ),
          ),
        ],
      ),
      body: BlocBuilder<RecorderCubit, RecorderState>(
        builder: (context, state) {
          final cubit = context.read<RecorderCubit>();
          return SafeArea(
            child: ListView(
              padding: const EdgeInsets.fromLTRB(
                AppSpacing.xl,
                AppSpacing.xl,
                AppSpacing.xl,
                AppSpacing.xxl,
              ),
              children: [
                Center(
                  child: Text(
                    _formatTime(state.elapsedSeconds),
                    style: AppTypography.monoLarge.copyWith(
                      fontSize: 72,
                      color: state.isRecording
                          ? _accent
                          : AppColors.textPrimary,
                    ),
                  ),
                ),
                const SizedBox(height: AppSpacing.sm),
                Center(
                  child: Text(
                    state.isRecording ? 'Recording…' : 'Ready',
                    style: AppTypography.caption.copyWith(
                      color: state.isRecording
                          ? _accent
                          : AppColors.textTertiary,
                    ),
                  ),
                ),
                const SizedBox(height: AppSpacing.xl),
                Center(
                  child: GestureDetector(
                    onTap: () => _toggleRecording(context, state),
                    child: Container(
                      width: 120,
                      height: 120,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        gradient: RadialGradient(
                          colors: [
                            _accent,
                            _accent.withValues(alpha: 0.7),
                          ],
                        ),
                        boxShadow: state.isRecording
                            ? [
                                BoxShadow(
                                  color: _accent.withValues(alpha: 0.45),
                                  blurRadius: 32,
                                  spreadRadius: 4,
                                ),
                              ]
                            : [],
                      ),
                      child: Icon(
                        state.isRecording
                            ? Icons.stop_rounded
                            : Icons.fiber_manual_record,
                        size: 56,
                        color: AppColors.textPrimary,
                      ),
                    ),
                  ),
                ),
                if (state.errorMessage != null)
                  Padding(
                    padding: const EdgeInsets.only(top: AppSpacing.lg),
                    child: Text(
                      state.errorMessage!,
                      textAlign: TextAlign.center,
                      style: AppTypography.caption
                          .copyWith(color: AppColors.error),
                    ),
                  ),
                const SizedBox(height: AppSpacing.xl),
                TfCard(
                  accent: _accent,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          const TfSectionLabel('Gravações'),
                          const Spacer(),
                          Text(
                            '${state.recordings.length}',
                            style: AppTypography.caption
                                .copyWith(color: AppColors.textSecondary),
                          ),
                          IconButton(
                            visualDensity: VisualDensity.compact,
                            icon: const Icon(
                              Icons.refresh,
                              color: AppColors.textSecondary,
                              size: 20,
                            ),
                            onPressed: cubit.refreshRecordings,
                          ),
                        ],
                      ),
                      const SizedBox(height: AppSpacing.sm),
                      if (state.recordings.isEmpty)
                        Padding(
                          padding: const EdgeInsets.symmetric(
                              vertical: AppSpacing.md),
                          child: Text(
                            'Nenhuma gravação ainda. Toque no botão para começar.',
                            style: AppTypography.caption,
                          ),
                        )
                      else
                        for (final rec in state.recordings)
                          _RecordingRow(
                            rec: rec,
                            sizeLabel: _formatBytes(rec.sizeBytes),
                            onDelete: () => cubit.deleteRecording(rec),
                          ),
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _RecordingRow extends StatelessWidget {
  const _RecordingRow({
    required this.rec,
    required this.sizeLabel,
    required this.onDelete,
  });
  final Recording rec;
  final String sizeLabel;
  final VoidCallback onDelete;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.sm),
      child: Row(
        children: [
          Container(
            width: 32,
            height: 32,
            decoration: BoxDecoration(
              color: _accent.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(AppRadius.md),
            ),
            child: const Icon(Icons.audiotrack, color: _accent, size: 18),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  rec.name,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: AppTypography.body.copyWith(
                    fontWeight: FontWeight.w500,
                  ),
                ),
                Text(sizeLabel, style: AppTypography.caption),
              ],
            ),
          ),
          IconButton(
            icon: const Icon(
              Icons.delete_outline,
              color: AppColors.textTertiary,
              size: 20,
            ),
            onPressed: onDelete,
          ),
        ],
      ),
    );
  }
}
