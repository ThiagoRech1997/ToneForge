// Recorder em C++. O legado Java tinha apenas stubs JNI vazios — esta é
// a primeira implementação real. Captura mono pós-FX a partir do buffer
// de saída do callback Oboe e serializa como WAV PCM 16-bit no stop.
// Buffer pré-alocado para evitar mallocs no audio thread; usa CAS pra
// reservar slots concorrentemente. Ver Fase 3.Recorder.
#ifndef TONEFORGE_RECORDER_H
#define TONEFORGE_RECORDER_H

#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

// Códigos de erro retornados por recorder_start / recorder_stop_and_save.
// 0 = sucesso, negativos = erro.
enum {
    TF_RECORDER_OK = 0,
    TF_RECORDER_ERR_ALREADY_ACTIVE = -1,
    TF_RECORDER_ERR_NOT_ACTIVE = -2,
    TF_RECORDER_ERR_OOM = -3,
    TF_RECORDER_ERR_OPEN_FILE = -4,
    TF_RECORDER_ERR_WRITE_FILE = -5,
    TF_RECORDER_ERR_INVALID_ARG = -6,
};

// Pré-aloca buffer para até max_seconds de áudio mono no sample_rate dado
// e ativa a captura. Chamadas subsequentes ao recorder_feed do callback
// Oboe vão acumulando samples no buffer.
int recorder_start(int sample_rate, int max_seconds);

// Encerra a captura e serializa o conteúdo gravado em path como WAV.
// Libera o buffer interno depois de escrever.
int recorder_stop_and_save(const char* path);

// Encerra e descarta o buffer sem escrever em disco.
void recorder_discard();

bool recorder_is_active();
int recorder_recorded_frames();
double recorder_recorded_seconds();

// Chamada pelo callback de áudio (audio_io_oboe.cpp). NÃO é parte da API
// pública para o Dart — só existe para o engine pegar samples. Lock-free.
void recorder_feed(const float* samples, int num_samples);

#ifdef __cplusplus
}
#endif

#endif // TONEFORGE_RECORDER_H
