// Metronome: BPM gigante em accentMetronome, FAB central de play, preset
// pills, beat dots animados e card "Configurações" com volume + compasso.

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../theme/app_colors.dart';
import '../../theme/app_spacing.dart';
import '../../theme/app_typography.dart';
import '../../widgets/tf_card.dart';
import '../../widgets/tf_pill.dart';
import '../../widgets/tf_section_label.dart';
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
  static const Color _accent = AppColors.accentMetronome;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Metronome'),
        actions: [
          BlocBuilder<MetronomeCubit, MetronomeState>(
            builder: (context, state) => Padding(
              padding: const EdgeInsets.only(right: AppSpacing.lg),
              child: Center(
                child: TfPill(
                  label: '${state.timeSignature}/4',
                  accent: _accent,
                  selected: true,
                ),
              ),
            ),
          ),
        ],
      ),
      body: BlocBuilder<MetronomeCubit, MetronomeState>(
        builder: (context, state) {
          final cubit = context.read<MetronomeCubit>();
          return SafeArea(
            child: SingleChildScrollView(
              padding: const EdgeInsets.fromLTRB(
                AppSpacing.xl,
                AppSpacing.xxl,
                AppSpacing.xl,
                AppSpacing.xl,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  _BpmDisplay(bpm: state.bpm),
                  const SizedBox(height: AppSpacing.xl),
                  _TransportRow(
                    isPlaying: state.isPlaying,
                    onMinus: cubit.decreaseBpm,
                    onPlus: cubit.increaseBpm,
                    onToggle: () =>
                        state.isPlaying ? cubit.stop() : cubit.start(),
                  ),
                  const SizedBox(height: AppSpacing.xl),
                  _PresetsRow(
                    presets: _bpmPresets,
                    current: state.bpm,
                    onTap: cubit.setBpm,
                  ),
                  const SizedBox(height: AppSpacing.xl),
                  _BeatIndicator(state: state),
                  const SizedBox(height: AppSpacing.xl),
                  if (state.errorMessage != null)
                    Padding(
                      padding: const EdgeInsets.only(bottom: AppSpacing.md),
                      child: Text(
                        state.errorMessage!,
                        style: AppTypography.caption
                            .copyWith(color: AppColors.error),
                      ),
                    ),
                  TfCard(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const TfSectionLabel('Configurações'),
                        const SizedBox(height: AppSpacing.lg),
                        _VolumeRow(value: state.volume, onChanged: cubit.setVolume),
                        const SizedBox(height: AppSpacing.lg),
                        _SignatureRow(
                          value: state.timeSignature,
                          onMinus: cubit.decreaseTimeSignature,
                          onPlus: cubit.increaseTimeSignature,
                        ),
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

class _BpmDisplay extends StatelessWidget {
  const _BpmDisplay({required this.bpm});
  final int bpm;
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(
          '$bpm',
          style: AppTypography.monoLarge.copyWith(
            color: _MetronomeView._accent,
            fontSize: 132,
          ),
        ),
        const SizedBox(height: AppSpacing.xs),
        Text('BPM', style: AppTypography.sectionLabel),
      ],
    );
  }
}

class _TransportRow extends StatelessWidget {
  const _TransportRow({
    required this.isPlaying,
    required this.onMinus,
    required this.onPlus,
    required this.onToggle,
  });
  final bool isPlaying;
  final VoidCallback onMinus;
  final VoidCallback onPlus;
  final VoidCallback onToggle;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        _RoundButton(icon: Icons.remove, onTap: onMinus),
        const SizedBox(width: AppSpacing.xl),
        GestureDetector(
          onTap: onToggle,
          child: Container(
            width: 88,
            height: 88,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  _MetronomeView._accent.withValues(alpha: 0.9),
                  _MetronomeView._accent,
                ],
              ),
              boxShadow: [
                BoxShadow(
                  color: _MetronomeView._accent.withValues(alpha: 0.3),
                  blurRadius: 24,
                  spreadRadius: 2,
                ),
              ],
            ),
            child: Icon(
              isPlaying ? Icons.pause_rounded : Icons.play_arrow_rounded,
              color: AppColors.textPrimary,
              size: 44,
            ),
          ),
        ),
        const SizedBox(width: AppSpacing.xl),
        _RoundButton(icon: Icons.add, onTap: onPlus),
      ],
    );
  }
}

class _RoundButton extends StatelessWidget {
  const _RoundButton({required this.icon, required this.onTap});
  final IconData icon;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) {
    return Material(
      color: AppColors.elevated,
      shape: const CircleBorder(),
      child: InkWell(
        customBorder: const CircleBorder(),
        onTap: onTap,
        child: Container(
          width: 48,
          height: 48,
          alignment: Alignment.center,
          child: Icon(icon, color: AppColors.textSecondary, size: 22),
        ),
      ),
    );
  }
}

class _PresetsRow extends StatelessWidget {
  const _PresetsRow({
    required this.presets,
    required this.current,
    required this.onTap,
  });
  final List<int> presets;
  final int current;
  final ValueChanged<int> onTap;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        for (final p in presets)
          TfPill(
            label: '$p',
            accent: _MetronomeView._accent,
            selected: p == current,
            onTap: () => onTap(p),
          ),
      ],
    );
  }
}

class _BeatIndicator extends StatelessWidget {
  const _BeatIndicator({required this.state});
  final MetronomeState state;
  @override
  Widget build(BuildContext context) {
    return Wrap(
      alignment: WrapAlignment.center,
      spacing: AppSpacing.md,
      runSpacing: AppSpacing.sm,
      children: List.generate(state.timeSignature, (i) {
        final isCurrent = state.isPlaying && i == state.currentBeat;
        final isDownbeat = i == 0;
        final base = isDownbeat
            ? _MetronomeView._accent
            : AppColors.textSecondary;
        return AnimatedContainer(
          duration: const Duration(milliseconds: 120),
          width: isCurrent ? 24 : 14,
          height: isCurrent ? 24 : 14,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: isCurrent ? base : base.withValues(alpha: 0.18),
          ),
        );
      }),
    );
  }
}

class _VolumeRow extends StatelessWidget {
  const _VolumeRow({required this.value, required this.onChanged});
  final double value;
  final ValueChanged<double> onChanged;
  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        const Icon(Icons.volume_up_outlined,
            color: AppColors.textSecondary, size: 20),
        const SizedBox(width: AppSpacing.md),
        Text('Volume', style: AppTypography.caption),
        Expanded(
          child: SliderTheme(
            data: SliderTheme.of(context).copyWith(
              activeTrackColor: _MetronomeView._accent,
              thumbColor: _MetronomeView._accent,
              overlayColor:
                  _MetronomeView._accent.withValues(alpha: 0.2),
            ),
            child: Slider(value: value, onChanged: onChanged),
          ),
        ),
        Text(
          '${(value * 100).toStringAsFixed(0)}%',
          style: AppTypography.caption
              .copyWith(color: AppColors.textPrimary),
        ),
      ],
    );
  }
}

class _SignatureRow extends StatelessWidget {
  const _SignatureRow({
    required this.value,
    required this.onMinus,
    required this.onPlus,
  });
  final int value;
  final VoidCallback onMinus;
  final VoidCallback onPlus;
  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Text('Compasso', style: AppTypography.caption),
        const Spacer(),
        _RoundButton(icon: Icons.remove, onTap: onMinus),
        SizedBox(
          width: 64,
          child: Text(
            '$value/4',
            textAlign: TextAlign.center,
            style: AppTypography.cardTitle
                .copyWith(color: _MetronomeView._accent),
          ),
        ),
        _RoundButton(icon: Icons.add, onTap: onPlus),
      ],
    );
  }
}
