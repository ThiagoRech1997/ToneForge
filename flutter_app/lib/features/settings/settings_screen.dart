// Tela de Settings real — TFR-49.
//
// Substitui o placeholder informativo pela primeira versão funcional:
//  - Seção "Sobre" (versão fixa do pubspec + link textual para os docs).
//  - Seção "Áudio" (telemetria read-only auto-refreshada).
//  - Seção "Preferências" (toggle persistido de telemetria do benchmark).
//  - Seção "Armazenamento" (limpar gravações e loops com confirmação).
//
// Escopo mais extenso (sample rate picker, routing, tema, etc.) ficou
// documentado como follow-up no ticket; ver settings_cubit.dart.

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../theme/app_colors.dart';
import 'settings_cubit.dart';

/// Placeholder da versão do app. TODO(TFR-49-followup): expor a versão real
/// via `package_info_plus` quando o pacote for adicionado ao pubspec.
const String _kAppVersion = '1.0.0+1';

/// Placeholder do commit hash do engine. TODO: injetar via
/// `--dart-define=ENGINE_COMMIT=xxx` no build (String.fromEnvironment).
const String _kEngineCommit =
    String.fromEnvironment('ENGINE_COMMIT', defaultValue: '—');

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => SettingsCubit(),
      child: const _SettingsView(),
    );
  }
}

class _SettingsView extends StatelessWidget {
  const _SettingsView();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: BlocConsumer<SettingsCubit, SettingsState>(
        listenWhen: (prev, next) =>
            prev.errorMessage != next.errorMessage ||
            prev.infoMessage != next.infoMessage,
        listener: (context, state) {
          final messenger = ScaffoldMessenger.of(context);
          if (state.errorMessage != null) {
            messenger.showSnackBar(
              SnackBar(
                content: Text(state.errorMessage!),
                backgroundColor: AppColors.error,
              ),
            );
            context.read<SettingsCubit>().clearTransientMessages();
          } else if (state.infoMessage != null) {
            messenger.showSnackBar(
              SnackBar(
                content: Text(state.infoMessage!),
                backgroundColor: AppColors.success,
              ),
            );
            context.read<SettingsCubit>().clearTransientMessages();
          }
        },
        builder: (context, state) {
          if (!state.ready) {
            return const Center(child: CircularProgressIndicator());
          }
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              _AboutSection(),
              const SizedBox(height: 12),
              _AudioSection(state: state),
              const SizedBox(height: 12),
              _PreferencesSection(state: state),
              const SizedBox(height: 12),
              _StorageSection(),
            ],
          );
        },
      ),
    );
  }
}

class _SectionCard extends StatelessWidget {
  const _SectionCard({required this.title, required this.children});
  final String title;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: theme.textTheme.titleMedium),
            const SizedBox(height: 12),
            ...children,
          ],
        ),
      ),
    );
  }
}

class _KeyValueRow extends StatelessWidget {
  const _KeyValueRow({required this.label, required this.value});
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Expanded(child: Text(label, style: const TextStyle(fontSize: 13))),
          Text(
            value,
            style: const TextStyle(fontFamily: 'monospace', fontSize: 13),
          ),
        ],
      ),
    );
  }
}

class _AboutSection extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return _SectionCard(
      title: 'Sobre',
      children: [
        _KeyValueRow(label: 'Versão do app', value: _kAppVersion),
        _KeyValueRow(label: 'Engine commit', value: _kEngineCommit),
        const SizedBox(height: 8),
        Text(
          'ToneForge — pedaleira de efeitos em tempo real.\n'
          'DSP portátil em C++17, UI em Flutter consumindo via Dart FFI.',
          style: theme.textTheme.bodySmall?.copyWith(
            color: theme.colorScheme.outline,
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'Status da migração: docs/migration-status.md',
          style: theme.textTheme.bodySmall?.copyWith(
            color: AppColors.textSecondary,
            fontFamily: 'monospace',
          ),
        ),
      ],
    );
  }
}

class _AudioSection extends StatelessWidget {
  const _AudioSection({required this.state});
  final SettingsState state;

  @override
  Widget build(BuildContext context) {
    final running = state.pipelineRunning;
    return _SectionCard(
      title: 'Áudio',
      children: [
        _KeyValueRow(
          label: 'Pipeline',
          value: running ? 'Rodando' : 'Parado',
        ),
        _KeyValueRow(
          label: 'Sample rate',
          value: state.sampleRate > 0 ? '${state.sampleRate} Hz' : '—',
        ),
        _KeyValueRow(
          label: 'Latência (round-trip)',
          value: running && state.latencyMs >= 0
              ? '${state.latencyMs.toStringAsFixed(2)} ms'
              : '—',
        ),
        _KeyValueRow(
          label: 'Xruns',
          value: state.xrunCount.toString(),
        ),
        const _KeyValueRow(label: 'Backend', value: 'Oboe / CoreAudio'),
        const SizedBox(height: 6),
        Text(
          'Atualiza automaticamente a cada ~1.5s enquanto a tela está aberta.',
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: AppColors.textTertiary,
              ),
        ),
      ],
    );
  }
}

class _PreferencesSection extends StatelessWidget {
  const _PreferencesSection({required this.state});
  final SettingsState state;

  @override
  Widget build(BuildContext context) {
    return _SectionCard(
      title: 'Preferências',
      children: [
        SwitchListTile(
          contentPadding: EdgeInsets.zero,
          dense: true,
          title: const Text('Mostrar telemetria no Benchmark'),
          subtitle: const Text(
            'Exibe o painel monoespaçado de latência/xruns na tela de Benchmark.',
          ),
          value: state.showBenchmarkTelemetry,
          onChanged: (v) =>
              context.read<SettingsCubit>().setShowBenchmarkTelemetry(v),
        ),
      ],
    );
  }
}

class _StorageSection extends StatelessWidget {
  Future<bool> _confirm(
    BuildContext context, {
    required String title,
    required String message,
  }) async {
    final result = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(title),
        content: Text(message),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(backgroundColor: AppColors.error),
            onPressed: () => Navigator.of(ctx).pop(true),
            child: const Text('Apagar'),
          ),
        ],
      ),
    );
    return result ?? false;
  }

  @override
  Widget build(BuildContext context) {
    return _SectionCard(
      title: 'Armazenamento',
      children: [
        ListTile(
          contentPadding: EdgeInsets.zero,
          leading: const Icon(Icons.delete_outline, color: AppColors.error),
          title: const Text('Limpar gravações'),
          subtitle: const Text('Remove todos os WAVs em recordings/'),
          onTap: () async {
            final cubit = context.read<SettingsCubit>();
            final ok = await _confirm(
              context,
              title: 'Limpar gravações?',
              message:
                  'Isto apaga permanentemente todos os arquivos .wav em '
                  'recordings/. Não pode ser desfeito.',
            );
            if (ok) await cubit.clearRecordings();
          },
        ),
        ListTile(
          contentPadding: EdgeInsets.zero,
          leading: const Icon(Icons.delete_outline, color: AppColors.error),
          title: const Text('Limpar loops salvos'),
          subtitle: const Text('Remove todos os WAVs em loops/'),
          onTap: () async {
            final cubit = context.read<SettingsCubit>();
            final ok = await _confirm(
              context,
              title: 'Limpar loops salvos?',
              message:
                  'Isto apaga permanentemente todos os arquivos .wav em '
                  'loops/. Não pode ser desfeito.',
            );
            if (ok) await cubit.clearLoops();
          },
        ),
      ],
    );
  }
}
