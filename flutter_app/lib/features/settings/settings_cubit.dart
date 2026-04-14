// SettingsCubit — TFR-49.
//
// Centraliza as preferências do usuário (persistidas em SharedPreferences)
// e a leitura de telemetria do engine para a tela de Settings.
//
// Escopo mínimo aprovado:
//  - Toggle `showBenchmarkTelemetry` persistido (valida o pipeline
//    shared_preferences end-to-end).
//  - Snapshot de áudio (sampleRate, latencyMs, xrunCount, isRunning)
//    atualizado a cada ~1.5s por um Timer.periodic.
//  - Operações de limpeza de diretórios (`recordings/` e `loops/`) expostas
//    via clearRecordings() / clearLoops() para a UI chamar após a
//    confirmação do AlertDialog.
//
// Settings fora de escopo (deixados como follow-up neste ticket):
//  sample rate / buffer size, device routing, warning low-latency,
//  tema escuro/claro, etc. Ver CLAUDE.md e docs/migration-status.md.

import 'dart:async';
import 'dart:io';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:path_provider/path_provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../engine/engine.dart';

/// Chave compartilhada com outras telas (ex.: BenchmarkScreen) que
/// queiram ler o toggle direto do SharedPreferences sem depender do Cubit.
const String kPrefShowBenchmarkTelemetry = 'settings.show_benchmark_telemetry';

class SettingsState {
  const SettingsState({
    required this.ready,
    required this.showBenchmarkTelemetry,
    required this.pipelineRunning,
    required this.sampleRate,
    required this.latencyMs,
    required this.xrunCount,
    this.errorMessage,
    this.infoMessage,
  });

  static const initial = SettingsState(
    ready: false,
    showBenchmarkTelemetry: true,
    pipelineRunning: false,
    sampleRate: 0,
    latencyMs: -1,
    xrunCount: 0,
  );

  /// true depois que o SharedPreferences foi carregado pelo menos uma vez.
  final bool ready;

  /// Toggle persistido — consumido pelo BenchmarkScreen para esconder o
  /// painel monoespaçado de telemetria quando o usuário não quiser vê-lo.
  final bool showBenchmarkTelemetry;

  final bool pipelineRunning;
  final int sampleRate;
  final double latencyMs;
  final int xrunCount;

  /// Erro transitório (ex.: falha em limpar diretório).
  final String? errorMessage;

  /// Mensagem informativa transitória (ex.: "3 arquivos removidos").
  final String? infoMessage;

  SettingsState copyWith({
    bool? ready,
    bool? showBenchmarkTelemetry,
    bool? pipelineRunning,
    int? sampleRate,
    double? latencyMs,
    int? xrunCount,
    String? errorMessage,
    String? infoMessage,
    bool clearError = false,
    bool clearInfo = false,
  }) {
    return SettingsState(
      ready: ready ?? this.ready,
      showBenchmarkTelemetry: showBenchmarkTelemetry ?? this.showBenchmarkTelemetry,
      pipelineRunning: pipelineRunning ?? this.pipelineRunning,
      sampleRate: sampleRate ?? this.sampleRate,
      latencyMs: latencyMs ?? this.latencyMs,
      xrunCount: xrunCount ?? this.xrunCount,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
      infoMessage: clearInfo ? null : (infoMessage ?? this.infoMessage),
    );
  }
}

class SettingsCubit extends Cubit<SettingsState> {
  SettingsCubit({ToneforgeEngine? engine})
      : _engine = engine ?? ToneforgeEngine(),
        super(SettingsState.initial) {
    _bootstrap();
  }

  final ToneforgeEngine _engine;
  SharedPreferences? _prefs;
  Timer? _pollTimer;

  Future<void> _bootstrap() async {
    try {
      _prefs = await SharedPreferences.getInstance();
    } catch (e) {
      if (isClosed) return;
      emit(state.copyWith(
        ready: true,
        errorMessage: 'Falha ao carregar preferências: $e',
      ));
      _startPolling();
      return;
    }
    if (isClosed) return;

    final show = _prefs?.getBool(kPrefShowBenchmarkTelemetry) ?? true;
    emit(state.copyWith(
      ready: true,
      showBenchmarkTelemetry: show,
      pipelineRunning: _engine.isRunning,
      sampleRate: _engine.sampleRate,
      latencyMs: _engine.isRunning ? _engine.latencyMs : -1,
      xrunCount: _engine.xrunCount,
    ));

    _startPolling();
  }

  void _startPolling() {
    _pollTimer?.cancel();
    _pollTimer = Timer.periodic(const Duration(milliseconds: 1500), (_) {
      if (isClosed) return;
      final running = _engine.isRunning;
      emit(state.copyWith(
        pipelineRunning: running,
        sampleRate: _engine.sampleRate,
        latencyMs: running ? _engine.latencyMs : -1,
        xrunCount: _engine.xrunCount,
      ));
    });
  }

  /// Force a single refresh of audio telemetry (usado por pull-to-refresh
  /// eventual e nos testes).
  void refreshAudioSnapshot() {
    if (isClosed) return;
    final running = _engine.isRunning;
    emit(state.copyWith(
      pipelineRunning: running,
      sampleRate: _engine.sampleRate,
      latencyMs: running ? _engine.latencyMs : -1,
      xrunCount: _engine.xrunCount,
    ));
  }

  Future<void> setShowBenchmarkTelemetry(bool value) async {
    if (state.showBenchmarkTelemetry == value) return;
    emit(state.copyWith(showBenchmarkTelemetry: value, clearError: true));
    try {
      await _prefs?.setBool(kPrefShowBenchmarkTelemetry, value);
    } catch (e) {
      if (isClosed) return;
      emit(state.copyWith(errorMessage: 'Falha ao salvar preferência: $e'));
    }
  }

  Future<Directory> _recordingsDir() async {
    final docs = await getApplicationDocumentsDirectory();
    return Directory('${docs.path}/recordings');
  }

  Future<Directory> _loopsDir() async {
    final docs = await getApplicationDocumentsDirectory();
    return Directory('${docs.path}/loops');
  }

  Future<int> _deleteWavsIn(Directory dir) async {
    if (!dir.existsSync()) return 0;
    var removed = 0;
    for (final entity in dir.listSync()) {
      if (entity is File && entity.path.toLowerCase().endsWith('.wav')) {
        try {
          entity.deleteSync();
          removed++;
        } catch (_) {
          // Ignora: contabilizamos apenas o que conseguimos apagar; o
          // chamador vê o total e pode reportar caso difira do esperado.
        }
      }
    }
    return removed;
  }

  /// Deleta todos os .wav em `$documents/recordings/`.
  Future<void> clearRecordings() async {
    try {
      final dir = await _recordingsDir();
      final count = await _deleteWavsIn(dir);
      if (isClosed) return;
      emit(state.copyWith(
        infoMessage: count == 0
            ? 'Nenhuma gravação para remover'
            : '$count gravação(ões) removida(s)',
        clearError: true,
      ));
    } catch (e) {
      if (isClosed) return;
      emit(state.copyWith(errorMessage: 'Falha ao limpar gravações: $e'));
    }
  }

  /// Deleta todos os .wav em `$documents/loops/`.
  Future<void> clearLoops() async {
    try {
      final dir = await _loopsDir();
      final count = await _deleteWavsIn(dir);
      if (isClosed) return;
      emit(state.copyWith(
        infoMessage: count == 0
            ? 'Nenhum loop para remover'
            : '$count loop(s) removido(s)',
        clearError: true,
      ));
    } catch (e) {
      if (isClosed) return;
      emit(state.copyWith(errorMessage: 'Falha ao limpar loops: $e'));
    }
  }

  void clearTransientMessages() {
    if (isClosed) return;
    emit(state.copyWith(clearError: true, clearInfo: true));
  }

  @override
  Future<void> close() {
    _pollTimer?.cancel();
    _pollTimer = null;
    return super.close();
  }
}
