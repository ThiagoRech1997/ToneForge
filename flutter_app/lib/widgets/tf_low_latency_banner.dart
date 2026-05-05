// Banner amarelo que aparece UMA vez por device quando o pipeline de áudio
// é degradado (devices Android budget que não expõem
// android.hardware.audio.low_latency no HAL → AAudio cai em shared/non-low-
// latency, latência fim-a-fim ~30-50ms). Ver TFR-14.
//
// Estratégia:
//  - Polling leve (1s) enquanto o widget está montado, lendo isRunning +
//    isLowLatencyPath direto do engine. Para de pollear assim que o banner
//    é dispensado ou o estado já foi avaliado e está OK.
//  - Persistência via SharedPreferences (`low_latency_warning_seen`). Como
//    SharedPreferences é por-device-por-install, basta uma única chave.
//  - Botão "Detalhes" abre um dialog com a tabela de diagnóstico (mesmas
//    informações da seção de Settings).

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../engine/engine.dart';
import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_typography.dart';

const String kPrefLowLatencyWarningSeen = 'low_latency_warning_seen';

class TfLowLatencyBanner extends StatefulWidget {
  const TfLowLatencyBanner({super.key, this.engine});

  /// Override para testes. Em produção usa o singleton ToneforgeEngine.
  final ToneforgeEngine? engine;

  @override
  State<TfLowLatencyBanner> createState() => _TfLowLatencyBannerState();
}

class _TfLowLatencyBannerState extends State<TfLowLatencyBanner> {
  late final ToneforgeEngine _engine;
  Timer? _pollTimer;
  bool _alreadySeen = true; // assume seen até carregar prefs
  bool _show = false;
  double _latencyMs = -1;
  int _framesPerBurst = 0;

  @override
  void initState() {
    super.initState();
    _engine = widget.engine ?? ToneforgeEngine();
    _bootstrap();
  }

  Future<void> _bootstrap() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      _alreadySeen = prefs.getBool(kPrefLowLatencyWarningSeen) ?? false;
    } catch (_) {
      // Sem prefs disponíveis cai no comportamento padrão (já visto = não
      // mostra). Não vale a pena ruidar a UI por falha de I/O em prefs.
      _alreadySeen = true;
    }
    if (!mounted) return;
    if (_alreadySeen) return;
    _startPolling();
  }

  void _startPolling() {
    _pollTimer?.cancel();
    _pollTimer = Timer.periodic(const Duration(milliseconds: 1000), (_) {
      _evaluate();
    });
    // Avaliação imediata pra não esperar 1s na primeira janela.
    _evaluate();
  }

  void _evaluate() {
    if (!mounted) return;
    final running = _engine.isRunning;
    if (!running) return; // ainda não inicializou
    final lowLatency = _engine.isLowLatencyPath;
    if (lowLatency) {
      // Caminho saudável — para de pollear, sem mostrar banner.
      _pollTimer?.cancel();
      _pollTimer = null;
      return;
    }
    // Degradado e ainda não visto: trava o snapshot e mostra.
    _pollTimer?.cancel();
    _pollTimer = null;
    setState(() {
      _show = true;
      _latencyMs = _engine.latencyMs;
      _framesPerBurst = _engine.framesPerBurst;
    });
  }

  Future<void> _dismiss() async {
    setState(() => _show = false);
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setBool(kPrefLowLatencyWarningSeen, true);
    } catch (_) {
      // Best effort. Se falhar, na próxima abertura o usuário verá de novo —
      // tolerável.
    }
  }

  void _showDetailsDialog() {
    final lat = _latencyMs >= 0 ? '${_latencyMs.toStringAsFixed(1)} ms' : '—';
    final burst = _framesPerBurst > 0 ? '$_framesPerBurst frames' : '—';
    final sr = _engine.sampleRate > 0 ? '${_engine.sampleRate} Hz' : '—';
    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Por que há delay?'),
        content: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text(
                'Este device não expõe o flag de áudio de baixa latência no '
                'HAL (Hardware Abstraction Layer). O Android cai automaticamente '
                'num caminho de áudio compartilhado, com burst maior e latência '
                'fim-a-fim na faixa de 30-50ms.',
              ),
              const SizedBox(height: 12),
              const Text(
                'Funciona normalmente:',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              const Text('  • Afinador'),
              const Text('  • Metrônomo'),
              const Text('  • Gravador'),
              const Text('  • Looper'),
              const SizedBox(height: 8),
              const Text(
                'Pode ter delay perceptível:',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              const Text('  • Monitoramento ao vivo com efeitos'),
              const SizedBox(height: 16),
              const Divider(),
              const SizedBox(height: 8),
              _detailRow('Latência atual', lat),
              _detailRow('Burst', burst),
              _detailRow('Sample rate', sr),
              _detailRow('Backend', 'Oboe'),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(),
            child: const Text('Fechar'),
          ),
        ],
      ),
    );
  }

  Widget _detailRow(String k, String v) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2),
      child: Row(
        children: [
          Expanded(child: Text(k, style: const TextStyle(fontSize: 13))),
          Text(
            v,
            style: const TextStyle(fontFamily: 'monospace', fontSize: 13),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _pollTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (!_show) return const SizedBox.shrink();
    final latText = _latencyMs >= 0
        ? 'Latência atual: ${_latencyMs.toStringAsFixed(1)} ms'
        : 'Latência atual indisponível';
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.lg),
      child: Container(
        padding: const EdgeInsets.all(AppSpacing.lg),
        decoration: BoxDecoration(
          color: AppColors.warning.withValues(alpha: 0.12),
          border: Border.all(color: AppColors.warning.withValues(alpha: 0.6)),
          borderRadius: BorderRadius.circular(AppRadius.lg),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(
                  Icons.warning_amber_rounded,
                  color: AppColors.warning,
                  size: 22,
                ),
                const SizedBox(width: AppSpacing.sm),
                Expanded(
                  child: Text(
                    'Áudio de baixa latência indisponível',
                    style: AppTypography.tabLabel.copyWith(
                      color: AppColors.warning,
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: AppSpacing.sm),
            const Text(
              'Este device não suporta o caminho de áudio de baixa latência. '
              'Afinador, metrônomo, looper e gravador funcionam normalmente. '
              'Tocar com efeitos em tempo real terá delay perceptível.',
              style: TextStyle(fontSize: 13, height: 1.35),
            ),
            const SizedBox(height: AppSpacing.sm),
            Text(
              latText,
              style: const TextStyle(
                fontFamily: 'monospace',
                fontSize: 12,
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: AppSpacing.sm),
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton(
                  onPressed: _showDetailsDialog,
                  child: const Text('Detalhes'),
                ),
                const SizedBox(width: AppSpacing.sm),
                FilledButton(
                  style: FilledButton.styleFrom(
                    backgroundColor: AppColors.warning,
                    foregroundColor: AppColors.bg,
                  ),
                  onPressed: _dismiss,
                  child: const Text('Entendi'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
