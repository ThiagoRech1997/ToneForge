// Salvar o conteúdo atual do looper como WAV. Carregar é feito chamando
// loadLooperFromAudio (já em audio_engine.h) com um Pointer<Float> vindo
// do Dart — a leitura WAV acontece do lado Flutter usando dart:io. Ver
// Fase 3.LoopLib.
#ifndef TONEFORGE_LOOP_IO_H
#define TONEFORGE_LOOP_IO_H

#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

enum {
    TF_LOOP_IO_OK = 0,
    TF_LOOP_IO_ERR_EMPTY = -1,
    TF_LOOP_IO_ERR_OPEN_FILE = -2,
    TF_LOOP_IO_ERR_WRITE_FILE = -3,
    TF_LOOP_IO_ERR_INVALID_ARG = -4,
};

// Serializa o mix atual do looper em path como WAV mono PCM 16-bit.
// Retorna 0 em sucesso ou um TF_LOOP_IO_ERR_* negativo.
int looper_save_wav(const char* path, int sample_rate);

// Serializa um track específico do looper em path como WAV mono PCM 16-bit.
// Retorna 0 em sucesso, TF_LOOP_IO_ERR_INVALID_ARG se trackIndex estiver
// fora de [0, max_tracks), TF_LOOP_IO_ERR_EMPTY se o track estiver vazio.
int looper_save_track_wav(int track_index, const char* path, int sample_rate);

#ifdef __cplusplus
}
#endif

#endif // TONEFORGE_LOOP_IO_H
