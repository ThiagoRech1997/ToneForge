import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../theme/app_colors.dart';
import '../../theme/app_spacing.dart';
import '../../theme/app_typography.dart';
import '../../widgets/tf_primary_button.dart';
import '../../widgets/tf_section_label.dart';
import 'effects_categories.dart';
import 'effects_cubit.dart';
import 'preset_manager.dart';
import 'widgets/effect_cards.dart';

class CategoryDetailScreen extends StatelessWidget {
  const CategoryDetailScreen({super.key, required this.category});

  final EffectCategoryInfo category;

  /// The category detail must reuse the shell-level EffectsCubit.
  /// Navigator.push does not propagate BlocProvider ancestors, so we read
  /// the cubit from [parentContext] and re-provide it via `.value` below.
  static Route<void> route(
    BuildContext parentContext,
    EffectCategoryInfo category,
  ) {
    final cubit = parentContext.read<EffectsCubit>();
    return MaterialPageRoute(
      builder: (_) => BlocProvider<EffectsCubit>.value(
        value: cubit,
        child: CategoryDetailScreen(category: category),
      ),
    );
  }

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

  Future<void> _onSavePreset(BuildContext context) async {
    final cubit = context.read<EffectsCubit>();
    final controller = TextEditingController();
    final name = await showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.surface,
        title: const Text('Salvar preset'),
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
    try {
      await PresetManager.save(name, cubit.state);
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Preset "$name" salvo')),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Falha ao salvar: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(category.title),
        actions: [
          IconButton(
            icon: const Icon(Icons.save_outlined),
            tooltip: 'Salvar preset',
            onPressed: () => _onSavePreset(context),
          ),
        ],
      ),
      body: BlocBuilder<EffectsCubit, EffectsState>(
        builder: (context, state) {
          final cubit = context.read<EffectsCubit>();
          return ListView(
            padding: const EdgeInsets.fromLTRB(
              AppSpacing.xl,
              AppSpacing.lg,
              AppSpacing.xl,
              AppSpacing.xxl * 2,
            ),
            children: [
              if (state.errorMessage != null)
                Padding(
                  padding: const EdgeInsets.only(bottom: AppSpacing.md),
                  child: Text(
                    state.errorMessage!,
                    style: AppTypography.body.copyWith(color: AppColors.error),
                  ),
                ),
              Text(
                category.subtitle,
                style: AppTypography.bodySecondary,
              ),
              const SizedBox(height: AppSpacing.lg),
              const TfSectionLabel('Parâmetros'),
              const SizedBox(height: AppSpacing.md),
              for (final kind in category.effects)
                buildEffectCard(
                  kind: kind,
                  state: state,
                  cubit: cubit,
                  accent: category.accent,
                ),
              const SizedBox(height: AppSpacing.xl),
              TfPrimaryButton(
                label: state.pipelineRunning ? 'Parar áudio' : 'Iniciar áudio',
                icon: state.pipelineRunning ? Icons.stop : Icons.play_arrow,
                accent: category.accent,
                onPressed: () => _togglePipeline(context, state),
              ),
            ],
          );
        },
      ),
    );
  }
}
