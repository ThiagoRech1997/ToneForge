// Landing da aba Effects: grid 2 colunas de categorias com accent color,
// signal chain strip derivado dos efeitos ativos, badge "N Active" e CTA
// de bypass do pipeline. O drilldown em cada categoria vai para
// CategoryDetailScreen, que reaproveita a mesma EffectsCubit via
// BlocProvider.value (para não instanciar outro engine binding).

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../theme/app_colors.dart';
import '../../theme/app_spacing.dart';
import '../../theme/app_typography.dart';
import '../../widgets/tf_accent_icon_tile.dart';
import '../../widgets/tf_card.dart';
import '../../widgets/tf_primary_button.dart';
import '../../widgets/tf_section_label.dart';
import '../../widgets/tf_signal_chain_strip.dart';
import 'category_detail_screen.dart';
import 'effects_categories.dart';
import 'effects_cubit.dart';
import 'preset_manager.dart';

class EffectsScreen extends StatelessWidget {
  const EffectsScreen({super.key});

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

  Future<void> _onLoadPreset(BuildContext context) async {
    final cubit = context.read<EffectsCubit>();
    final presets = await PresetManager.list();
    if (!context.mounted) return;
    if (presets.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Nenhum preset salvo ainda')),
      );
      return;
    }
    final picked = await showModalBottomSheet<PresetSummary>(
      context: context,
      backgroundColor: AppColors.surface,
      showDragHandle: true,
      builder: (ctx) => ListView(
        shrinkWrap: true,
        children: [
          const Padding(
            padding: EdgeInsets.symmetric(
              horizontal: AppSpacing.lg,
              vertical: AppSpacing.sm,
            ),
            child: TfSectionLabel('Carregar preset'),
          ),
          for (final p in presets)
            ListTile(
              leading: const Icon(Icons.tune, color: AppColors.primary),
              title: Text(p.name),
              subtitle: Text(
                '${p.modified.toLocal()}'.split('.').first,
                style: AppTypography.caption,
              ),
              trailing: IconButton(
                icon: const Icon(Icons.delete_outline),
                onPressed: () async {
                  await PresetManager.delete(p.file);
                  if (ctx.mounted) Navigator.pop(ctx);
                },
              ),
              onTap: () => Navigator.pop(ctx, p),
            ),
        ],
      ),
    );
    if (picked == null) return;
    try {
      final loaded = await PresetManager.load(picked.file);
      if (loaded == null) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Preset inválido')),
          );
        }
        return;
      }
      cubit.applySnapshot(loaded);
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Preset "${picked.name}" aplicado')),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Falha ao carregar: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Effects'),
        actions: [
          IconButton(
            icon: const Icon(Icons.folder_open),
            tooltip: 'Carregar preset',
            onPressed: () => _onLoadPreset(context),
          ),
        ],
      ),
      body: BlocBuilder<EffectsCubit, EffectsState>(
        builder: (context, state) {
          final activeCount = _countActive(state);
          final chain = _buildChain(state);
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
                  if (state.errorMessage != null)
                    Padding(
                      padding: const EdgeInsets.only(bottom: AppSpacing.md),
                      child: Text(
                        state.errorMessage!,
                        style: AppTypography.body.copyWith(
                          color: AppColors.error,
                        ),
                      ),
                    ),
                  _ActiveBadge(count: activeCount),
                  const SizedBox(height: AppSpacing.lg),
                  _CategoryGrid(),
                  const SizedBox(height: AppSpacing.xl),
                  const TfSectionLabel('Signal Chain'),
                  const SizedBox(height: AppSpacing.md),
                  TfCard(
                    padding: const EdgeInsets.symmetric(
                      horizontal: AppSpacing.md,
                      vertical: AppSpacing.lg,
                    ),
                    child: chain.isEmpty
                        ? Text(
                            'Todos os efeitos desligados.',
                            style: AppTypography.caption,
                          )
                        : TfSignalChainStrip(nodes: chain),
                  ),
                  const SizedBox(height: AppSpacing.xl),
                  TfPrimaryButton(
                    label: state.pipelineRunning
                        ? 'Parar áudio'
                        : 'Iniciar áudio',
                    icon: state.pipelineRunning
                        ? Icons.stop
                        : Icons.play_arrow,
                    onPressed: () => _togglePipeline(context, state),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  int _countActive(EffectsState s) {
    return [
      s.gain.enabled,
      s.distortion.enabled,
      s.delay.enabled,
      s.reverb.enabled,
      s.chorus.enabled,
      s.flanger.enabled,
      s.phaser.enabled,
      s.eq.enabled,
      s.compressor.enabled,
    ].where((e) => e).length;
  }

  List<SignalChainNode> _buildChain(EffectsState s) {
    final nodes = <SignalChainNode>[];
    void add(bool enabled, String label, Color c) {
      if (enabled) nodes.add(SignalChainNode(label: label, accent: c));
    }

    add(s.gain.enabled, 'Gain', AppColors.warning);
    add(s.distortion.enabled, 'Dist', AppColors.warning);
    add(s.compressor.enabled, 'Comp', AppColors.accentRecorder);
    add(s.eq.enabled, 'EQ', AppColors.accentMidi);
    add(s.chorus.enabled, 'Chorus', AppColors.primary);
    add(s.flanger.enabled, 'Flanger', AppColors.primary);
    add(s.phaser.enabled, 'Phaser', AppColors.primary);
    add(s.delay.enabled, 'Delay', AppColors.success);
    add(s.reverb.enabled, 'Reverb', AppColors.accentTuner);
    return nodes;
  }
}

class _ActiveBadge extends StatelessWidget {
  const _ActiveBadge({required this.count});
  final int count;

  @override
  Widget build(BuildContext context) {
    return Align(
      alignment: Alignment.centerRight,
      child: Container(
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.md,
          vertical: 6,
        ),
        decoration: BoxDecoration(
          color: AppColors.primary.withValues(alpha: 0.15),
          borderRadius: BorderRadius.circular(AppRadius.xxl),
          border: Border.all(
            color: AppColors.primary.withValues(alpha: 0.5),
          ),
        ),
        child: Text(
          '$count Active',
          style: AppTypography.caption.copyWith(
            color: AppColors.primary,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }
}

class _CategoryGrid extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final categories = EffectCategories.all;
    return LayoutBuilder(
      builder: (context, constraints) {
        const gap = AppSpacing.md;
        final tileWidth = (constraints.maxWidth - gap) / 2;
        return Wrap(
          spacing: gap,
          runSpacing: gap,
          children: [
            for (final cat in categories)
              SizedBox(
                width: tileWidth,
                height: 120,
                child: TfAccentIconTile(
                  icon: cat.icon,
                  label: cat.title,
                  subtitle: cat.subtitle,
                  accent: cat.accent,
                  onTap: () => Navigator.of(context).push(
                    CategoryDetailScreen.route(context, cat),
                  ),
                ),
              ),
          ],
        );
      },
    );
  }
}
