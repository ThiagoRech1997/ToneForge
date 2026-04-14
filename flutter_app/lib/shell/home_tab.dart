import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../features/automation/automation_screen.dart';
import '../features/benchmark/benchmark_screen.dart';
import '../features/effects/effects_cubit.dart';
import '../features/learning/learning_screen.dart';
import '../features/loop_library/loop_library_screen.dart';
import '../features/looper/looper_screen.dart';
import '../features/metronome/metronome_screen.dart';
import '../features/midi/midi_screen.dart';
import '../features/recorder/recorder_screen.dart';
import '../features/settings/settings_screen.dart';
import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_typography.dart';
import '../widgets/tf_accent_icon_tile.dart';
import '../widgets/tf_pill.dart';
import '../widgets/tf_section_label.dart';
import '../widgets/tf_stat_cell.dart';
import 'app_shell.dart';

class HomeTab extends StatelessWidget {
  const HomeTab({super.key, required this.onSwitchTab});

  final ValueChanged<ShellTab> onSwitchTab;

  void _push(BuildContext context, Widget screen) {
    Navigator.of(context).push(MaterialPageRoute(builder: (_) => screen));
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      bottom: false,
      child: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(
          AppSpacing.xl,
          AppSpacing.lg,
          AppSpacing.xl,
          AppSpacing.xxl,
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            _Header(),
            const SizedBox(height: AppSpacing.xl),
            _FeatureGrid(onSwitchTab: onSwitchTab, onPush: _push),
            const SizedBox(height: AppSpacing.xl),
            const _ActiveChainSection(),
            const SizedBox(height: AppSpacing.xl),
            const _QuickStatsRow(),
          ],
        ),
      ),
    );
  }
}

class _Header extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Container(
          width: 56,
          height: 56,
          decoration: BoxDecoration(
            gradient: LinearGradient(
              colors: [
                AppColors.primary.withValues(alpha: 0.32),
                AppColors.primary.withValues(alpha: 0.08),
              ],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
            borderRadius: BorderRadius.circular(AppRadius.lg),
            border: Border.all(
              color: AppColors.primary.withValues(alpha: 0.45),
            ),
          ),
          child: const Icon(
            Icons.music_note_rounded,
            color: AppColors.primary,
            size: 28,
          ),
        ),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: const [
              Text('ToneForge', style: AppTypography.screenTitle),
              SizedBox(height: 2),
              Text(
                'Pedaleira Digital Profissional',
                style: AppTypography.caption,
              ),
            ],
          ),
        ),
        IconButton(
          icon: const Icon(Icons.settings_outlined, color: AppColors.textSecondary),
          onPressed: () => Navigator.of(context).push(
            MaterialPageRoute(builder: (_) => const SettingsScreen()),
          ),
        ),
      ],
    );
  }
}

class _FeatureGrid extends StatelessWidget {
  const _FeatureGrid({required this.onSwitchTab, required this.onPush});

  final ValueChanged<ShellTab> onSwitchTab;
  final void Function(BuildContext, Widget) onPush;

  @override
  Widget build(BuildContext context) {
    final tiles = <_TileDef>[
      _TileDef(
        icon: Icons.graphic_eq_rounded,
        label: 'Effects',
        accent: AppColors.accentEffects,
        onTap: () => onSwitchTab(ShellTab.effects),
      ),
      _TileDef(
        icon: Icons.music_note_rounded,
        label: 'Tuner',
        accent: AppColors.accentTuner,
        onTap: () => onSwitchTab(ShellTab.tuner),
      ),
      _TileDef(
        icon: Icons.loop_rounded,
        label: 'Looper',
        accent: AppColors.accentLooper,
        onTap: () => onPush(context, const LooperScreen()),
      ),
      _TileDef(
        icon: Icons.timer_rounded,
        label: 'Metronome',
        accent: AppColors.accentMetronome,
        onTap: () => onPush(context, const MetronomeScreen()),
      ),
      _TileDef(
        icon: Icons.menu_book_rounded,
        label: 'Learning',
        accent: AppColors.accentLearning,
        onTap: () => onPush(context, const LearningScreen()),
      ),
      _TileDef(
        icon: Icons.mic_rounded,
        label: 'Recorder',
        accent: AppColors.accentRecorder,
        onTap: () => onPush(context, const RecorderScreen()),
      ),
      _TileDef(
        icon: Icons.library_music_rounded,
        label: 'Loops',
        accent: AppColors.accentLoopLibrary,
        onTap: () => onPush(context, const LoopLibraryScreen()),
      ),
      _TileDef(
        icon: Icons.piano_rounded,
        label: 'MIDI',
        accent: AppColors.accentMidi,
        onTap: () => onPush(context, const MidiScreen()),
      ),
      _TileDef(
        icon: Icons.timeline_rounded,
        label: 'Automation',
        accent: AppColors.accentAutomation,
        onTap: () => onPush(context, const AutomationScreen()),
      ),
    ];

    return LayoutBuilder(
      builder: (context, constraints) {
        const crossAxisCount = 3;
        const gap = AppSpacing.md;
        final tileWidth = (constraints.maxWidth - gap * (crossAxisCount - 1)) /
            crossAxisCount;
        return Wrap(
          spacing: gap,
          runSpacing: gap,
          children: [
            for (final t in tiles)
              SizedBox(
                width: tileWidth,
                height: 100,
                child: TfAccentIconTile(
                  icon: t.icon,
                  label: t.label,
                  accent: t.accent,
                  onTap: t.onTap,
                ),
              ),
          ],
        );
      },
    );
  }
}

class _TileDef {
  const _TileDef({
    required this.icon,
    required this.label,
    required this.accent,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final Color accent;
  final VoidCallback onTap;
}

class _ActiveChainSection extends StatelessWidget {
  const _ActiveChainSection();

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<EffectsCubit, EffectsState>(
      builder: (context, state) {
        final active = _collectActive(state);
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const TfSectionLabel('Active Chain'),
            const SizedBox(height: AppSpacing.md),
            if (active.isEmpty)
              Text(
                'Nenhum efeito ativo — toque em Effects para começar.',
                style: AppTypography.caption,
              )
            else
              Wrap(
                spacing: AppSpacing.sm,
                runSpacing: AppSpacing.sm,
                children: [
                  for (final item in active)
                    TfPill(label: item.label, accent: item.color),
                ],
              ),
          ],
        );
      },
    );
  }

  List<_ActiveItem> _collectActive(EffectsState state) {
    final items = <_ActiveItem>[];
    if (state.gain.enabled) {
      items.add(const _ActiveItem('Gain', AppColors.warning));
    }
    if (state.distortion.enabled) {
      items.add(const _ActiveItem('Distortion', AppColors.warning));
    }
    if (state.delay.enabled) {
      items.add(const _ActiveItem('Delay', AppColors.success));
    }
    if (state.reverb.enabled) {
      items.add(const _ActiveItem('Reverb', AppColors.accentTuner));
    }
    if (state.chorus.enabled) {
      items.add(const _ActiveItem('Chorus', AppColors.primary));
    }
    if (state.flanger.enabled) {
      items.add(const _ActiveItem('Flanger', AppColors.primary));
    }
    if (state.phaser.enabled) {
      items.add(const _ActiveItem('Phaser', AppColors.primary));
    }
    if (state.eq.enabled) {
      items.add(const _ActiveItem('EQ', AppColors.accentMidi));
    }
    if (state.compressor.enabled) {
      items.add(const _ActiveItem('Comp', AppColors.accentRecorder));
    }
    return items;
  }
}

class _ActiveItem {
  const _ActiveItem(this.label, this.color);
  final String label;
  final Color color;
}

class _QuickStatsRow extends StatelessWidget {
  const _QuickStatsRow();

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<EffectsCubit, EffectsState>(
      builder: (context, state) {
        final activeCount = [
          state.gain.enabled,
          state.distortion.enabled,
          state.delay.enabled,
          state.reverb.enabled,
          state.chorus.enabled,
          state.flanger.enabled,
          state.phaser.enabled,
          state.eq.enabled,
          state.compressor.enabled,
        ].where((e) => e).length;
        return Row(
          children: [
            Expanded(
              child: TfStatCell(
                value: '$activeCount',
                label: 'Active',
                accent: AppColors.primary,
              ),
            ),
            const SizedBox(width: AppSpacing.md),
            const Expanded(
              child: TfStatCell(
                value: '<3ms',
                label: 'Latency',
                accent: AppColors.error,
              ),
            ),
            const SizedBox(width: AppSpacing.md),
            Expanded(
              child: GestureDetector(
                onTap: () => Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const BenchmarkScreen()),
                ),
                child: const TfStatCell(
                  value: 'C++',
                  label: 'Engine',
                  accent: AppColors.success,
                ),
              ),
            ),
          ],
        );
      },
    );
  }
}
