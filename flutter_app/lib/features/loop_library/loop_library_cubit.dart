// Cubit da biblioteca de loops. Gerencia a lista de WAVs em
// $documents/loops/, salva o mix atual do looper como WAV via
// looper_save_wav (engine), e carrega WAVs de volta para o buffer do
// looper via loadLooperFromAudio. Fase 3.LoopLib.

import 'dart:io';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:path_provider/path_provider.dart';

import '../../engine/engine.dart';
import 'wav_reader.dart';

class LoopFile {
  const LoopFile({required this.file, required this.sizeBytes, required this.modified});
  final File file;
  final int sizeBytes;
  final DateTime modified;
  String get name => file.uri.pathSegments.last;
}

class LoopLibraryState {
  const LoopLibraryState({required this.loops, this.errorMessage});
  static const initial = LoopLibraryState(loops: []);

  final List<LoopFile> loops;
  final String? errorMessage;

  LoopLibraryState copyWith({List<LoopFile>? loops, String? errorMessage, bool clearError = false}) {
    return LoopLibraryState(
      loops: loops ?? this.loops,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}

class LoopLibraryCubit extends Cubit<LoopLibraryState> {
  LoopLibraryCubit({ToneforgeEngine? engine})
      : _engine = engine ?? ToneforgeEngine(),
        super(LoopLibraryState.initial) {
    refresh();
  }

  final ToneforgeEngine _engine;

  static Future<Directory> _dir() async {
    final docs = await getApplicationDocumentsDirectory();
    final dir = Directory('${docs.path}/loops');
    if (!dir.existsSync()) dir.createSync(recursive: true);
    return dir;
  }

  Future<void> refresh() async {
    try {
      final dir = await _dir();
      final files = dir
          .listSync()
          .whereType<File>()
          .where((f) => f.path.toLowerCase().endsWith('.wav'))
          .toList();
      final loops = files.map((f) {
        final stat = f.statSync();
        return LoopFile(file: f, sizeBytes: stat.size, modified: stat.modified);
      }).toList()
        ..sort((a, b) => b.modified.compareTo(a.modified));
      emit(state.copyWith(loops: loops, clearError: true));
    } catch (e) {
      emit(state.copyWith(errorMessage: 'Falha ao listar loops: $e'));
    }
  }

  /// Salva o conteúdo atual do looper como WAV. Retorna o nome do arquivo
  /// criado, ou null em erro (mensagem em state.errorMessage).
  Future<String?> saveCurrentLoop(String name) async {
    final dir = await _dir();
    final cleaned = name.trim().replaceAll(RegExp(r'[^A-Za-z0-9._\- ]'), '_');
    final filename = '${cleaned.isEmpty ? "loop_${DateTime.now().millisecondsSinceEpoch}" : cleaned}.wav';
    final path = '${dir.path}/$filename';

    final rc = _engine.saveLooperToWav(path);
    if (rc != 0) {
      emit(state.copyWith(errorMessage: 'looper_save_wav falhou (code=$rc)'));
      return null;
    }
    await refresh();
    return filename;
  }

  /// Carrega um WAV mono PCM 16-bit no buffer do looper. Não inicia
  /// reprodução — quem chama decide.
  Future<bool> loadLoop(LoopFile loop) async {
    try {
      final wav = readWavMono16(loop.file);
      if (wav == null) {
        emit(state.copyWith(errorMessage: 'WAV inválido (precisa ser mono PCM 16-bit)'));
        return false;
      }
      _engine.loadLooperFromFloats(wav.samples);
      emit(state.copyWith(clearError: true));
      return true;
    } catch (e) {
      emit(state.copyWith(errorMessage: 'Falha ao carregar: $e'));
      return false;
    }
  }

  Future<void> deleteLoop(LoopFile loop) async {
    try {
      if (loop.file.existsSync()) loop.file.deleteSync();
      await refresh();
    } catch (e) {
      emit(state.copyWith(errorMessage: 'Falha ao deletar: $e'));
    }
  }
}
