// Leitor mínimo de WAV PCM 16-bit mono em Dart puro. Aceita o formato que
// o engine gera (single fmt + data chunks); se o arquivo tiver chunks
// extras (LIST, INFO, etc.) avança até encontrar o data chunk. Devolve
// um Float32List em [-1,1] ou null se o cabeçalho for inválido. Ver
// Fase 3.LoopLib.

import 'dart:io';
import 'dart:typed_data';

class WavData {
  const WavData({required this.samples, required this.sampleRate});
  final Float32List samples;
  final int sampleRate;
}

WavData? readWavMono16(File file) {
  final bytes = file.readAsBytesSync();
  if (bytes.length < 44) return null;
  final bd = ByteData.sublistView(bytes);

  if (_str(bytes, 0, 4) != 'RIFF' || _str(bytes, 8, 4) != 'WAVE') return null;

  // Procura fmt chunk + data chunk a partir do offset 12.
  var offset = 12;
  int? sampleRate;
  int? bitsPerSample;
  int? numChannels;
  int? audioFormat;
  int? dataOffset;
  int? dataSize;

  while (offset + 8 <= bytes.length) {
    final tag = _str(bytes, offset, 4);
    final size = bd.getUint32(offset + 4, Endian.little);
    final payload = offset + 8;
    if (tag == 'fmt ') {
      audioFormat = bd.getUint16(payload, Endian.little);
      numChannels = bd.getUint16(payload + 2, Endian.little);
      sampleRate = bd.getUint32(payload + 4, Endian.little);
      bitsPerSample = bd.getUint16(payload + 14, Endian.little);
    } else if (tag == 'data') {
      dataOffset = payload;
      dataSize = size;
      break;
    }
    offset = payload + size + (size.isOdd ? 1 : 0); // padding
  }

  if (audioFormat != 1 ||
      numChannels != 1 ||
      bitsPerSample != 16 ||
      sampleRate == null ||
      dataOffset == null ||
      dataSize == null) {
    return null;
  }

  final numSamples = dataSize ~/ 2;
  final out = Float32List(numSamples);
  for (var i = 0; i < numSamples; i++) {
    final s = bd.getInt16(dataOffset + i * 2, Endian.little);
    out[i] = s / 32768.0;
  }
  return WavData(samples: out, sampleRate: sampleRate);
}

String _str(Uint8List bytes, int offset, int length) {
  return String.fromCharCodes(bytes.sublist(offset, offset + length));
}
