// RecorderCubit. Orquestra o lifecycle do engine + a captura via
// recorder_start/stop_and_save. As gravações vão para a pasta de
// documentos do app (path_provider) com nome timestamped. A lista de
// gravações é lida do disco em refresh(), não persistida em memória.
// Ver Fase 3.Recorder.

import 'dart:async';
import 'dart:io';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:path_provider/path_provider.dart';

import '../../engine/engine.dart';

class Recording {
  const Recording({required this.file, required this.sizeBytes, required this.modified});
  final File file;
  final int sizeBytes;
  final DateTime modified;
  String get name => file.uri.pathSegments.last;
}

class RecorderState {
  const RecorderState({
    required this.pipelineRunning,
    required this.isRecording,
    required this.elapsedSeconds,
    required this.recordings,
    this.errorMessage,
  });

  static const initial = RecorderState(
    pipelineRunning: false,
    isRecording: false,
    elapsedSeconds: 0,
    recordings: [],
  );

  final bool pipelineRunning;
  final bool isRecording;
  final double elapsedSeconds;
  final List<Recording> recordings;
  final String? errorMessage;

  RecorderState copyWith({
    bool? pipelineRunning,
    bool? isRecording,
    double? elapsedSeconds,
    List<Recording>? recordings,
    String? errorMessage,
    bool clearError = false,
  }) {
    return RecorderState(
      pipelineRunning: pipelineRunning ?? this.pipelineRunning,
      isRecording: isRecording ?? this.isRecording,
      elapsedSeconds: elapsedSeconds ?? this.elapsedSeconds,
      recordings: recordings ?? this.recordings,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}

class RecorderCubit extends Cubit<RecorderState> {
  RecorderCubit({ToneforgeEngine? engine})
      : _engine = engine ?? ToneforgeEngine(),
        super(RecorderState.initial) {
    refreshRecordings();
  }

  static const int _maxSeconds = 600; // 10 min, ~115 MB float

  final ToneforgeEngine _engine;
  Timer? _pollTimer;
  bool _ownsEngine = false;

  Future<Directory> _recordingsDir() async {
    final docs = await getApplicationDocumentsDirectory();
    final dir = Directory('${docs.path}/recordings');
    if (!dir.existsSync()) dir.createSync(recursive: true);
    return dir;
  }

  Future<void> refreshRecordings() async {
    try {
      final dir = await _recordingsDir();
      final files = dir
          .listSync()
          .whereType<File>()
          .where((f) => f.path.toLowerCase().endsWith('.wav'))
          .toList();
      final recordings = files.map((f) {
        final stat = f.statSync();
        return Recording(file: f, sizeBytes: stat.size, modified: stat.modified);
      }).toList()
        ..sort((a, b) => b.modified.compareTo(a.modified));
      emit(state.copyWith(recordings: recordings));
    } catch (e) {
      emit(state.copyWith(errorMessage: 'Falha ao listar gravações: $e'));
    }
  }

  Future<void> startRecording() async {
    if (state.isRecording) return;

    if (!_engine.isRunning) {
      final result = _engine.start();
      if (result != 0) {
        emit(state.copyWith(errorMessage: 'Falha ao iniciar pipeline (code=$result)'));
        return;
      }
      _ownsEngine = true;
    }

    final sr = _engine.sampleRate > 0 ? _engine.sampleRate : 48000;
    final rc = _engine.startRecording(sampleRate: sr, maxSeconds: _maxSeconds);
    if (rc != 0) {
      emit(state.copyWith(errorMessage: 'recorder_start falhou (code=$rc)'));
      return;
    }

    emit(state.copyWith(
      pipelineRunning: true,
      isRecording: true,
      elapsedSeconds: 0,
      clearError: true,
    ));

    _pollTimer = Timer.periodic(const Duration(milliseconds: 100), (_) {
      emit(state.copyWith(elapsedSeconds: _engine.recordedSeconds));
      // Buffer encheu: o native desativou sozinho. Refletimos na UI.
      if (!_engine.isRecording && state.isRecording) {
        _stopAndSave();
      }
    });
  }

  Future<void> stopRecording() async {
    if (!state.isRecording) return;
    await _stopAndSave();
  }

  Future<void> _stopAndSave() async {
    _pollTimer?.cancel();
    _pollTimer = null;

    final dir = await _recordingsDir();
    final filename = 'recording_${DateTime.now().millisecondsSinceEpoch}.wav';
    final path = '${dir.path}/$filename';

    final rc = _engine.stopRecordingAndSave(path);

    if (_ownsEngine && _engine.isRunning) {
      _engine.stop();
      _ownsEngine = false;
    }

    if (rc != 0) {
      emit(state.copyWith(
        isRecording: false,
        pipelineRunning: false,
        errorMessage: 'recorder_stop_and_save falhou (code=$rc)',
      ));
      return;
    }

    emit(state.copyWith(isRecording: false, pipelineRunning: false, clearError: true));
    await refreshRecordings();
  }

  Future<void> deleteRecording(Recording rec) async {
    try {
      if (rec.file.existsSync()) rec.file.deleteSync();
      await refreshRecordings();
    } catch (e) {
      emit(state.copyWith(errorMessage: 'Falha ao deletar: $e'));
    }
  }

  @override
  Future<void> close() {
    _pollTimer?.cancel();
    if (_engine.isRecording) _engine.discardRecording();
    if (_ownsEngine && _engine.isRunning) _engine.stop();
    return super.close();
  }
}
