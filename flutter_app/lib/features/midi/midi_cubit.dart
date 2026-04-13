// MidiCubit — Fase 3.MIDI. Lista devices MIDI (USB / BLE), conecta,
// escuta a stream de pacotes, decodifica CCs e roteia para os mesmos
// 4 parâmetros automatizáveis usados pela Automation. O modo "Learn"
// captura o próximo CC recebido e o mapeia ao parâmetro alvo. Como o
// engine é singleton, qualquer outra tela compartilha a mesma audio.

import 'dart:async';
import 'dart:typed_data';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_midi_command/flutter_midi_command.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../engine/engine.dart';
import '../automation/automation_cubit.dart' show AutoParam, AutoParamX;

class CcMapping {
  const CcMapping({required this.param, required this.cc, required this.channel});
  final AutoParam param;
  final int cc;       // 0-127
  final int channel;  // 0-15
}

class MidiState {
  const MidiState({
    required this.devices,
    required this.connectedDeviceId,
    required this.mappings,
    required this.values,
    required this.learning,
    required this.lastCc,
    required this.lastValue,
    this.errorMessage,
  });

  static const initial = MidiState(
    devices: [],
    connectedDeviceId: null,
    mappings: [],
    values: {
      AutoParam.gainLevel: 0.5,
      AutoParam.distortionAmount: 0.0,
      AutoParam.delayMix: 0.0,
      AutoParam.reverbMix: 0.0,
    },
    learning: null,
    lastCc: null,
    lastValue: null,
  );

  final List<MidiDevice> devices;
  final String? connectedDeviceId;
  final List<CcMapping> mappings;
  final Map<AutoParam, double> values;
  final AutoParam? learning;
  final int? lastCc;
  final int? lastValue;
  final String? errorMessage;

  bool get isConnected => connectedDeviceId != null;

  CcMapping? mappingFor(AutoParam p) {
    for (final m in mappings) {
      if (m.param == p) return m;
    }
    return null;
  }

  MidiState copyWith({
    List<MidiDevice>? devices,
    String? connectedDeviceId,
    bool clearConnectedDeviceId = false,
    List<CcMapping>? mappings,
    Map<AutoParam, double>? values,
    AutoParam? learning,
    bool clearLearning = false,
    int? lastCc,
    int? lastValue,
    String? errorMessage,
    bool clearError = false,
  }) {
    return MidiState(
      devices: devices ?? this.devices,
      connectedDeviceId:
          clearConnectedDeviceId ? null : (connectedDeviceId ?? this.connectedDeviceId),
      mappings: mappings ?? this.mappings,
      values: values ?? this.values,
      learning: clearLearning ? null : (learning ?? this.learning),
      lastCc: lastCc ?? this.lastCc,
      lastValue: lastValue ?? this.lastValue,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}

class MidiCubit extends Cubit<MidiState> {
  MidiCubit({ToneforgeEngine? engine})
      : _engine = engine ?? ToneforgeEngine(),
        super(MidiState.initial) {
    _midi = MidiCommand();
    _setupSubscription = _midi.onMidiSetupChanged?.listen((_) => refreshDevices());
  }

  final ToneforgeEngine _engine;
  late final MidiCommand _midi;
  StreamSubscription<MidiPacket>? _packetSubscription;
  StreamSubscription<String>? _setupSubscription;
  bool _ownsEngine = false;

  Future<void> _ensurePipeline() async {
    if (_engine.isRunning) return;
    final result = _engine.start();
    if (result == 0) {
      _ownsEngine = true;
    } else {
      emit(state.copyWith(errorMessage: 'Falha ao iniciar pipeline (code=$result)'));
    }
  }

  // ----- discovery / connection -----

  Future<void> initialize() async {
    final btStatus = await Permission.bluetoothScan.request();
    final btConnect = await Permission.bluetoothConnect.request();
    if (!btStatus.isGranted || !btConnect.isGranted) {
      // Sem BLE não é fatal — USB MIDI ainda funciona. Só registramos o aviso.
      emit(state.copyWith(
        errorMessage: 'Sem permissão BLE; apenas devices USB serão listados',
      ));
    }
    try {
      await _midi.startBluetoothCentral();
      await _midi.waitUntilBluetoothIsInitialized();
      await _midi.startScanningForBluetoothDevices();
    } catch (e) {
      // Algumas plataformas (ex: emulador sem BT) lançam aqui — segue só com USB.
    }
    await refreshDevices();
  }

  Future<void> refreshDevices() async {
    final list = await _midi.devices ?? [];
    emit(state.copyWith(devices: list));
  }

  Future<void> connect(MidiDevice device) async {
    try {
      await _midi.connectToDevice(device);
      _packetSubscription?.cancel();
      _packetSubscription = _midi.onMidiDataReceived?.listen(_onPacket);
      await _ensurePipeline();
      emit(state.copyWith(
        connectedDeviceId: device.id,
        clearError: true,
      ));
      await refreshDevices();
    } catch (e) {
      emit(state.copyWith(errorMessage: 'Falha ao conectar: $e'));
    }
  }

  Future<void> disconnect() async {
    final id = state.connectedDeviceId;
    if (id == null) return;
    final device = state.devices.firstWhere(
      (d) => d.id == id,
      orElse: () => MidiDevice(id, '', '', false),
    );
    try {
      _midi.disconnectDevice(device);
    } catch (_) {}
    _packetSubscription?.cancel();
    _packetSubscription = null;
    emit(state.copyWith(clearConnectedDeviceId: true));
    await refreshDevices();
  }

  // ----- learn / mapping -----

  void startLearn(AutoParam param) {
    emit(state.copyWith(learning: param, clearError: true));
  }

  void cancelLearn() {
    emit(state.copyWith(clearLearning: true));
  }

  void clearMapping(AutoParam param) {
    final filtered = state.mappings.where((m) => m.param != param).toList();
    emit(state.copyWith(mappings: filtered));
  }

  // ----- packet handling -----

  void _onPacket(MidiPacket packet) {
    final data = packet.data;
    if (data.length < 3) return;
    final status = data[0];
    final isControlChange = (status & 0xF0) == 0xB0;
    if (!isControlChange) return;

    final channel = status & 0x0F;
    final cc = data[1];
    final value = data[2];
    emit(state.copyWith(lastCc: cc, lastValue: value));

    // Modo learn: amarra esse CC ao parâmetro alvo e sai do modo.
    if (state.learning != null) {
      final target = state.learning!;
      // Substitui qualquer mapping antigo do mesmo param OU do mesmo CC.
      final filtered = state.mappings
          .where((m) => m.param != target && !(m.cc == cc && m.channel == channel))
          .toList()
        ..add(CcMapping(param: target, cc: cc, channel: channel));
      emit(state.copyWith(mappings: filtered, clearLearning: true));
      return;
    }

    // Modo normal: roteia para param mapeado, se houver.
    for (final m in state.mappings) {
      if (m.cc == cc && m.channel == channel) {
        final normalized = (value / 127.0).clamp(0.0, 1.0);
        _applyEngine(m.param, normalized);
        final newValues = Map<AutoParam, double>.from(state.values);
        newValues[m.param] = normalized;
        emit(state.copyWith(values: newValues));
        break;
      }
    }
  }

  void _applyEngine(AutoParam param, double value) {
    switch (param) {
      case AutoParam.gainLevel:
        _engine.setGainLevel(value);
        _engine.gainEnabled = true;
        break;
      case AutoParam.distortionAmount:
        _engine.setDistortionAmount(value);
        _engine.setDistortionMix(value);
        _engine.distortionEnabled = value > 0;
        break;
      case AutoParam.delayMix:
        _engine.setDelayTimeMs(250);
        _engine.setDelayFeedback(0.4);
        _engine.setDelayMix(value);
        _engine.delayEnabled = value > 0;
        break;
      case AutoParam.reverbMix:
        _engine.setReverbRoomSize(0.6);
        _engine.setReverbDamping(0.5);
        _engine.setReverbMix(value);
        _engine.reverbEnabled = value > 0;
        break;
    }
  }

  // Helper público pra UI mostrar nomes de param.
  String paramLabel(AutoParam p) => p.label;

  // Mantido para Uint8List inferência; sem uso fora do cubit.
  // ignore: unused_element
  Uint8List _emptyUint8() => Uint8List(0);

  @override
  Future<void> close() {
    _packetSubscription?.cancel();
    _setupSubscription?.cancel();
    if (state.connectedDeviceId != null) disconnect();
    if (_ownsEngine && _engine.isRunning) _engine.stop();
    return super.close();
  }
}
