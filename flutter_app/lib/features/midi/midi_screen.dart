// Tela MIDI Learn — Fase 3.MIDI. Topo: lista de devices com botão
// connect/disconnect. Meio: telemetria do último CC recebido. Fundo:
// uma linha por parâmetro automatizável com o mapping atual e botão
// Learn. Quando Learn está ativo, o próximo CC vincula.

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_midi_command/flutter_midi_command.dart';

import '../../theme/app_colors.dart';
import '../automation/automation_cubit.dart' show AutoParam, AutoParamX;
import 'midi_cubit.dart';

class MidiScreen extends StatelessWidget {
  const MidiScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => MidiCubit()..initialize(),
      child: const _MidiView(),
    );
  }
}

class _MidiView extends StatelessWidget {
  const _MidiView();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('MIDI Learn'),
        actions: [
          BlocBuilder<MidiCubit, MidiState>(
            builder: (context, state) => IconButton(
              icon: const Icon(Icons.refresh),
              tooltip: 'Re-scan devices',
              onPressed: context.read<MidiCubit>().refreshDevices,
            ),
          ),
        ],
      ),
      body: BlocBuilder<MidiCubit, MidiState>(
        builder: (context, state) {
          final cubit = context.read<MidiCubit>();
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              if (state.errorMessage != null)
                Padding(
                  padding: const EdgeInsets.only(bottom: 12),
                  child: Text(
                    state.errorMessage!,
                    style: TextStyle(color: Theme.of(context).colorScheme.error),
                  ),
                ),
              _DevicesSection(state: state, cubit: cubit),
              const SizedBox(height: 16),
              _TelemetrySection(state: state),
              const SizedBox(height: 16),
              _MappingsSection(state: state, cubit: cubit),
            ],
          );
        },
      ),
    );
  }
}

class _DevicesSection extends StatelessWidget {
  const _DevicesSection({required this.state, required this.cubit});
  final MidiState state;
  final MidiCubit cubit;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Devices', style: theme.textTheme.titleMedium),
            const SizedBox(height: 8),
            if (state.devices.isEmpty)
              const Padding(
                padding: EdgeInsets.symmetric(vertical: 8),
                child: Text('Nenhum device MIDI encontrado. Conecte via USB ou BLE.'),
              )
            else
              for (final device in state.devices) _DeviceTile(device: device, state: state, cubit: cubit),
          ],
        ),
      ),
    );
  }
}

class _DeviceTile extends StatelessWidget {
  const _DeviceTile({required this.device, required this.state, required this.cubit});
  final MidiDevice device;
  final MidiState state;
  final MidiCubit cubit;

  @override
  Widget build(BuildContext context) {
    final isConnected = state.connectedDeviceId == device.id;
    return ListTile(
      contentPadding: EdgeInsets.zero,
      leading: Icon(
        device.type == 'BLE' ? Icons.bluetooth : Icons.usb,
        color: isConnected ? AppColors.success : null,
      ),
      title: Text(device.name.isEmpty ? '(sem nome)' : device.name),
      subtitle: Text('${device.type} · ${device.id}', style: const TextStyle(fontSize: 11)),
      trailing: isConnected
          ? FilledButton.tonal(onPressed: cubit.disconnect, child: const Text('Desconectar'))
          : FilledButton.tonal(onPressed: () => cubit.connect(device), child: const Text('Conectar')),
    );
  }
}

class _TelemetrySection extends StatelessWidget {
  const _TelemetrySection({required this.state});
  final MidiState state;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final lastDesc = state.lastCc != null
        ? 'CC #${state.lastCc} = ${state.lastValue}'
        : 'Aguardando mensagens...';
    return Card(
      color: AppColors.elevated,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            const Icon(Icons.input),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Último CC recebido', style: theme.textTheme.bodySmall),
                  Text(
                    lastDesc,
                    style: theme.textTheme.titleMedium?.copyWith(fontFamily: 'monospace'),
                  ),
                ],
              ),
            ),
            Text(state.isConnected ? 'Conectado' : 'Offline',
                style: TextStyle(
                  color: state.isConnected ? AppColors.success : theme.colorScheme.outline,
                )),
          ],
        ),
      ),
    );
  }
}

class _MappingsSection extends StatelessWidget {
  const _MappingsSection({required this.state, required this.cubit});
  final MidiState state;
  final MidiCubit cubit;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Mapeamentos', style: theme.textTheme.titleMedium),
            const SizedBox(height: 8),
            for (final p in AutoParam.values) _MappingRow(param: p, state: state, cubit: cubit),
          ],
        ),
      ),
    );
  }
}

class _MappingRow extends StatelessWidget {
  const _MappingRow({required this.param, required this.state, required this.cubit});
  final AutoParam param;
  final MidiState state;
  final MidiCubit cubit;

  @override
  Widget build(BuildContext context) {
    final mapping = state.mappingFor(param);
    final isLearning = state.learning == param;
    final value = state.values[param] ?? 0;

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(param.label, style: const TextStyle(fontSize: 14)),
                Text(
                  mapping == null
                      ? '(sem mapping)'
                      : 'CC #${mapping.cc} ch ${mapping.channel + 1} · ${value.toStringAsFixed(2)}',
                  style: TextStyle(
                    fontSize: 12,
                    fontFamily: 'monospace',
                    color: Theme.of(context).colorScheme.outline,
                  ),
                ),
              ],
            ),
          ),
          if (isLearning)
            FilledButton.tonal(
              onPressed: cubit.cancelLearn,
              child: const Text('Aguardando…'),
            )
          else
            OutlinedButton(
              onPressed: () => cubit.startLearn(param),
              child: const Text('Learn'),
            ),
          IconButton(
            icon: const Icon(Icons.close, size: 18),
            onPressed: mapping == null ? null : () => cubit.clearMapping(param),
          ),
        ],
      ),
    );
  }
}
