// Helper interno para serialização WAV PCM 16-bit mono. Compartilhado entre
// recorder.cpp e loop_io.cpp para evitar duplicação. Não exposto via header
// público — só é usado dentro de engine/src/. Ver Fase 3.LoopLib.
#ifndef TONEFORGE_INTERNAL_WAV_WRITER_H
#define TONEFORGE_INTERNAL_WAV_WRITER_H

#include <cstddef>

namespace toneforge::internal {

// Escreve samples float[-1,1] como WAV PCM 16-bit mono em path.
// Retorna 0 em sucesso, -1 ao abrir o arquivo, -2 ao escrever.
int write_wav_mono16(const char* path, const float* samples, size_t num_samples, int sample_rate);

} // namespace toneforge::internal

#endif // TONEFORGE_INTERNAL_WAV_WRITER_H
