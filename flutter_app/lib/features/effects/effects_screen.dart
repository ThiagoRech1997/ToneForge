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
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const TfSectionLabel('Chain Order'),
                      TextButton.icon(
                        onPressed: () =>
                            context.read<EffectsCubit>().resetOrder(),
                        icon: const Icon(
                          Icons.restart_alt_rounded,
                          size: 16,
                        ),
                        label: Text(
                          'Reset',
                          style: AppTypography.caption.copyWith(
                            color: AppColors.textSecondary,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppSpacing.xs),
                  Padding(
                    padding: const EdgeInsets.only(bottom: AppSpacing.md),
                    child: Text(
                      'Arraste para reordenar a cadeia.',
                      style: AppTypography.caption,
                    ),
                  ),
                  _ChainReorderList(state: state),
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

  /// Renderiza o signal chain strip na mesma ordem que o usuário definiu
  /// via drag-and-drop (state.order). Só inclui os efeitos habilitados.
  /// Ver TFR-45: antes, o strip tinha ordem hardcoded e não refletia
  /// as reordenações feitas no _ChainReorderList abaixo.
  List<SignalChainNode> _buildChain(EffectsState s) {
    final nodes = <SignalChainNode>[];
    for (final kind in s.order) {
      if (!_ChainReorderList._enabledFor(s, kind)) continue;
      nodes.add(SignalChainNode(
        label: kind.shortLabel,
        accent: _ChainReorderList._accentFor(kind),
      ));
    }
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

class _ChainReorderList extends StatelessWidget {
  const _ChainReorderList({required this.state});

  final EffectsState state;

  @override
  Widget build(BuildContext context) {
    final cubit = context.read<EffectsCubit>();
    // ReorderableListView precisa de altura finita dentro de um
    // SingleChildScrollView. shrinkWrap + NeverScrollableScrollPhysics
    // deixa o pai cuidar do scroll e dimensiona pela soma dos itens.
    return ReorderableListView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      buildDefaultDragHandles: false,
      itemCount: state.order.length,
      onReorder: cubit.reorderEffects,
      proxyDecorator: (child, index, animation) {
        return Material(
          color: Colors.transparent,
          elevation: 4,
          borderRadius: BorderRadius.circular(AppRadius.md),
          child: child,
        );
      },
      itemBuilder: (context, index) {
        final kind = state.order[index];
        final enabled = _enabledFor(state, kind);
        final accent = _accentFor(kind);
        return _ChainTile(
          key: ValueKey('chain-${kind.name}'),
          index: index,
          kind: kind,
          accent: accent,
          enabled: enabled,
        );
      },
    );
  }

  static bool _enabledFor(EffectsState s, EffectKind k) {
    switch (k) {
      case EffectKind.gain:
        return s.gain.enabled;
      case EffectKind.distortion:
        return s.distortion.enabled;
      case EffectKind.delay:
        return s.delay.enabled;
      case EffectKind.reverb:
        return s.reverb.enabled;
      case EffectKind.chorus:
        return s.chorus.enabled;
      case EffectKind.flanger:
        return s.flanger.enabled;
      case EffectKind.phaser:
        return s.phaser.enabled;
      case EffectKind.eq:
        return s.eq.enabled;
      case EffectKind.compressor:
        return s.compressor.enabled;
    }
  }

  static Color _accentFor(EffectKind k) {
    switch (k) {
      case EffectKind.gain:
      case EffectKind.distortion:
        return AppColors.warning;
      case EffectKind.compressor:
        return AppColors.accentRecorder;
      case EffectKind.eq:
        return AppColors.accentMidi;
      case EffectKind.chorus:
      case EffectKind.flanger:
      case EffectKind.phaser:
        return AppColors.primary;
      case EffectKind.delay:
        return AppColors.success;
      case EffectKind.reverb:
        return AppColors.accentTuner;
    }
  }
}

class _ChainTile extends StatelessWidget {
  const _ChainTile({
    super.key,
    required this.index,
    required this.kind,
    required this.accent,
    required this.enabled,
  });

  final int index;
  final EffectKind kind;
  final Color accent;
  final bool enabled;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.sm),
      child: TfCard(
        accent: enabled ? accent : null,
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.md,
          vertical: AppSpacing.sm,
        ),
        child: Row(
          children: [
            ReorderableDragStartListener(
              index: index,
              child: const Padding(
                padding: EdgeInsets.symmetric(horizontal: 4),
                child: Icon(
                  Icons.drag_indicator_rounded,
                  color: AppColors.textTertiary,
                ),
              ),
            ),
            const SizedBox(width: AppSpacing.sm),
            Container(
              width: 28,
              alignment: Alignment.center,
              child: Text(
                '${index + 1}',
                style: AppTypography.caption.copyWith(
                  color: AppColors.textTertiary,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            const SizedBox(width: AppSpacing.sm),
            Expanded(
              child: Text(
                kind.label,
                style: AppTypography.body.copyWith(
                  color: enabled
                      ? AppColors.textPrimary
                      : AppColors.textSecondary,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            Container(
              width: 8,
              height: 8,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: enabled ? accent : AppColors.muted,
              ),
            ),
          ],
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
