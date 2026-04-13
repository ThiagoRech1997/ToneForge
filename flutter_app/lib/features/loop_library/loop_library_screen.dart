// Tela LoopLibrary. Lista os WAVs em $documents/loops/ com load e delete.
// O carregamento empurra os samples para o buffer do looper via
// loadLooperFromAudio — após carregar, o usuário precisa ir até a tela
// do Looper para ouvir. Ver Fase 3.LoopLib.

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'loop_library_cubit.dart';

class LoopLibraryScreen extends StatelessWidget {
  const LoopLibraryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => LoopLibraryCubit(),
      child: const _LoopLibraryView(),
    );
  }
}

class _LoopLibraryView extends StatelessWidget {
  const _LoopLibraryView();

  String _formatBytes(int b) {
    if (b < 1024) return '$b B';
    if (b < 1024 * 1024) return '${(b / 1024).toStringAsFixed(1)} KB';
    return '${(b / (1024 * 1024)).toStringAsFixed(1)} MB';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Loop Library'),
        actions: [
          BlocBuilder<LoopLibraryCubit, LoopLibraryState>(
            builder: (context, state) => IconButton(
              icon: const Icon(Icons.refresh),
              onPressed: context.read<LoopLibraryCubit>().refresh,
            ),
          ),
        ],
      ),
      body: BlocBuilder<LoopLibraryCubit, LoopLibraryState>(
        builder: (context, state) {
          final cubit = context.read<LoopLibraryCubit>();
          return Column(
            children: [
              if (state.errorMessage != null)
                Padding(
                  padding: const EdgeInsets.all(16),
                  child: Text(
                    state.errorMessage!,
                    style: TextStyle(color: Theme.of(context).colorScheme.error),
                  ),
                ),
              if (state.loops.isEmpty)
                const Padding(
                  padding: EdgeInsets.all(32),
                  child: Center(
                    child: Text('Nenhum loop salvo. Grave um no Looper e use "Salvar".'),
                  ),
                )
              else
                Expanded(
                  child: ListView.builder(
                    itemCount: state.loops.length,
                    itemBuilder: (context, i) {
                      final loop = state.loops[i];
                      return ListTile(
                        leading: const Icon(Icons.loop),
                        title: Text(loop.name, style: const TextStyle(fontFamily: 'monospace', fontSize: 13)),
                        subtitle: Text(_formatBytes(loop.sizeBytes)),
                        trailing: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            IconButton(
                              icon: const Icon(Icons.download),
                              tooltip: 'Carregar no Looper',
                              onPressed: () async {
                                final ok = await cubit.loadLoop(loop);
                                if (ok && context.mounted) {
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    SnackBar(content: Text('"${loop.name}" carregado. Abra o Looper.')),
                                  );
                                }
                              },
                            ),
                            IconButton(
                              icon: const Icon(Icons.delete_outline),
                              onPressed: () => cubit.deleteLoop(loop),
                            ),
                          ],
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
