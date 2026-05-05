// Tela LoopLibrary. Lista os WAVs em $documents/loops/ com load e delete.
// Carregamento padrão limpa todas as tracks e coloca em track 0; o menu de
// long-press oferece "carregar em track N" pra preservar as outras (TFR-9).
// Após carregar, o usuário vai até a tela do Looper pra ouvir. Ver Fase 3.LoopLib.

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../engine/engine.dart';
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

  /// Mini-picker pra "carregar em track N". Lê o número de slots do engine e
  /// mostra um dialog simples com botões. Retorna o índice escolhido (0-based)
  /// ou null se cancelado. TFR-9.
  Future<int?> _pickTrack(BuildContext context) async {
    final engine = ToneforgeEngine();
    final n = engine.looperMaxTracks;
    if (n <= 0) return 0;
    return showDialog<int>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Carregar em qual track?'),
        content: SizedBox(
          width: 280,
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: List<Widget>.generate(n, (i) {
              final used = engine.isLooperTrackActive(i);
              return ChoiceChip(
                label: Text(used ? '${i + 1} (subst.)' : '${i + 1}'),
                selected: false,
                onSelected: (_) => Navigator.pop(ctx, i),
              );
            }),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancelar'),
          ),
        ],
      ),
    );
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
                              tooltip: 'Carregar no Looper (substitui)',
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
                              icon: const Icon(Icons.layers),
                              tooltip: 'Carregar em track…',
                              onPressed: () async {
                                final track = await _pickTrack(context);
                                if (track == null) return;
                                final ok = await cubit.loadLoopIntoTrack(loop, track);
                                if (ok && context.mounted) {
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    SnackBar(content: Text('"${loop.name}" → track ${track + 1}')),
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
