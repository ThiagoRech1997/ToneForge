// Tela do Recorder. Botão grande de rec/stop, timer MM:SS e lista das
// gravações da pasta do app. Sem playback ainda — o foco aqui é provar
// que o engine C++ grava WAV via FFI fim-a-fim. Ver Fase 3.Recorder.

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:permission_handler/permission_handler.dart';

import 'recorder_cubit.dart';

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
    final mm = (s ~/ 60).toString().padLeft(2, '0');
    final ss = (s % 60).toString().padLeft(2, '0');
    final cs = ((seconds - s) * 10).toInt();
    return '$mm:$ss.$cs';
  }

  String _formatBytes(int bytes) {
    if (bytes < 1024) return '$bytes B';
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
    return '${(bytes / (1024 * 1024)).toStringAsFixed(1)} MB';
  }

  Future<void> _toggleRecording(BuildContext context, RecorderState state) async {
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
      appBar: AppBar(title: const Text('Recorder')),
      body: BlocBuilder<RecorderCubit, RecorderState>(
        builder: (context, state) {
          final cubit = context.read<RecorderCubit>();
          return Column(
            children: [
              const SizedBox(height: 32),
              Text(
                _formatTime(state.elapsedSeconds),
                style: Theme.of(context).textTheme.displayLarge?.copyWith(
                      fontFamily: 'monospace',
                      fontSize: 64,
                      color: state.isRecording ? Colors.redAccent : null,
                    ),
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: 120,
                height: 120,
                child: FloatingActionButton(
                  shape: const CircleBorder(),
                  backgroundColor: state.isRecording ? Colors.redAccent : Theme.of(context).colorScheme.primary,
                  onPressed: () => _toggleRecording(context, state),
                  child: Icon(
                    state.isRecording ? Icons.stop : Icons.fiber_manual_record,
                    size: 64,
                  ),
                ),
              ),
              if (state.errorMessage != null)
                Padding(
                  padding: const EdgeInsets.all(16),
                  child: Text(
                    state.errorMessage!,
                    style: TextStyle(color: Theme.of(context).colorScheme.error),
                    textAlign: TextAlign.center,
                  ),
                ),
              const SizedBox(height: 16),
              const Divider(),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: Row(
                  children: [
                    Text('Gravações (${state.recordings.length})',
                        style: Theme.of(context).textTheme.titleMedium),
                    const Spacer(),
                    IconButton(
                      icon: const Icon(Icons.refresh),
                      onPressed: cubit.refreshRecordings,
                    ),
                  ],
                ),
              ),
              Expanded(
                child: state.recordings.isEmpty
                    ? const Center(child: Text('Nenhuma gravação ainda'))
                    : ListView.builder(
                        itemCount: state.recordings.length,
                        itemBuilder: (context, i) {
                          final rec = state.recordings[i];
                          return ListTile(
                            leading: const Icon(Icons.audiotrack),
                            title: Text(rec.name, style: const TextStyle(fontFamily: 'monospace', fontSize: 13)),
                            subtitle: Text(_formatBytes(rec.sizeBytes)),
                            trailing: IconButton(
                              icon: const Icon(Icons.delete_outline),
                              onPressed: () => cubit.deleteRecording(rec),
                            ),
                          );
                        },
                      ),
              ),
            ],
          );
        },
      ),
    );
  }
}
