#ifndef TONEFORGE_AUDIO_IO_H
#define TONEFORGE_AUDIO_IO_H

#ifdef __cplusplus
extern "C" {
#endif

// Fase 0 — camada de I/O de áudio em C++ usando Oboe.
// Encapsula input + output streams e injeta buffers em processBuffer() do audio_engine.
// Pipeline legado AudioRecord/AudioTrack continua intacto; alternância via feature flag
// no AudioRepository. Ver plano em .claude/plans/cuddly-jumping-wolf.md Fase 0.

// Abre streams Oboe e começa a processar. Retorna 0 em sucesso, código de erro Oboe < 0 caso falhe.
// sampleRate 0 deixa o Oboe escolher a taxa nativa do device.
// framesPerCallback 0 deixa o Oboe usar o burst nativo.
int audio_engine_start(int sampleRate, int framesPerCallback);

// Para e fecha os streams. Idempotente.
void audio_engine_stop();

// True se há streams abertas e rodando.
bool audio_engine_is_running();

// Latência de round-trip estimada em milissegundos (input + output). -1 se não disponível.
double audio_engine_get_latency_ms();

// Contador de underruns acumulados desde o último start. Para telemetria do benchmark.
int audio_engine_get_xrun_count();

// Sample rate efetivo escolhido pelo Oboe (pode diferir do solicitado). 0 se não rodando.
int audio_engine_get_sample_rate();

#ifdef __cplusplus
}
#endif

#endif // TONEFORGE_AUDIO_IO_H
