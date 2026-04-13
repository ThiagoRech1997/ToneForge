// Cálculos musicais para o tuner. Reproduzem a lógica do TunerPresenter
// legado em Java (12 * log2(f/440) + 57). Pure Dart, testável sem engine.
// Ver Fase 3.Tuner.

import 'dart:math' as math;

const List<String> _noteNames = [
  'C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B',
];

/// Resultado da análise de uma frequência.
class NoteReading {
  const NoteReading({
    required this.frequency,
    required this.noteName,
    required this.octave,
    required this.cents,
    required this.accuracy,
  });

  /// Reading vazio (sem sinal).
  static const empty = NoteReading(
    frequency: 0,
    noteName: '—',
    octave: 0,
    cents: 0,
    accuracy: TunerAccuracy.none,
  );

  final double frequency;
  final String noteName;
  final int octave;
  final double cents;
  final TunerAccuracy accuracy;

  bool get hasSignal => frequency > 0;
  String get noteWithOctave => hasSignal ? '$noteName$octave' : '—';
}

enum TunerAccuracy { none, perfect, good, fair, poor }

/// Converte uma frequência em Hz para uma nota musical + cents de desafinação.
/// A=440 hardcoded — adicionar calibração depois.
NoteReading analyzeFrequency(double frequency) {
  if (frequency <= 0) return NoteReading.empty;

  // MIDI note number (A4 = 69), depois mapeia para C-relativo.
  final midi = 69 + 12 * (math.log(frequency / 440) / math.ln2);
  final midiRounded = midi.round();
  final noteIndex = ((midiRounded % 12) + 12) % 12;
  final octave = (midiRounded ~/ 12) - 1;
  final noteName = _noteNames[noteIndex];

  // Cents = 100 * (midi - midiRounded), no intervalo [-50, +50].
  final cents = (midi - midiRounded) * 100;

  final absCents = cents.abs();
  TunerAccuracy accuracy;
  if (absCents <= 5) {
    accuracy = TunerAccuracy.perfect;
  } else if (absCents <= 15) {
    accuracy = TunerAccuracy.good;
  } else if (absCents <= 30) {
    accuracy = TunerAccuracy.fair;
  } else {
    accuracy = TunerAccuracy.poor;
  }

  return NoteReading(
    frequency: frequency,
    noteName: noteName,
    octave: octave,
    cents: cents,
    accuracy: accuracy,
  );
}
