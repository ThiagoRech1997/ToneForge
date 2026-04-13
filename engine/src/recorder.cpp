#include "toneforge/recorder.h"

#include <atomic>
#include <cstdint>
#include <cstdio>
#include <cstring>
#include <mutex>
#include <vector>

namespace {

// Buffer pré-alocado. write_pos é incrementado atomicamente pelo callback
// de áudio; o writer (recorder_stop_and_save) só lê depois de active=false.
std::vector<float> g_buffer;
std::atomic<size_t> g_write_pos{0};
std::atomic<bool> g_active{false};
std::atomic<int> g_sample_rate{48000};

// Lock para start/stop, fora do caminho quente.
std::mutex g_lifecycle_mutex;

// Escreve o cabeçalho WAV PCM 16-bit mono e os samples.
// Retorna 0 em sucesso ou um TF_RECORDER_ERR_*.
int write_wav(const char* path, const float* samples, size_t num_samples, int sample_rate) {
    FILE* f = std::fopen(path, "wb");
    if (!f) return TF_RECORDER_ERR_OPEN_FILE;

    const uint32_t data_bytes = static_cast<uint32_t>(num_samples * sizeof(int16_t));
    const uint32_t fmt_chunk_size = 16;
    const uint16_t audio_format = 1;     // PCM
    const uint16_t num_channels = 1;     // mono
    const uint16_t bits_per_sample = 16;
    const uint16_t block_align = num_channels * bits_per_sample / 8;
    const uint32_t byte_rate = static_cast<uint32_t>(sample_rate) * block_align;
    const uint32_t riff_size = 36 + data_bytes;

    auto write_u32 = [&](uint32_t v) { std::fwrite(&v, 4, 1, f); };
    auto write_u16 = [&](uint16_t v) { std::fwrite(&v, 2, 1, f); };

    if (std::fwrite("RIFF", 1, 4, f) != 4) { std::fclose(f); return TF_RECORDER_ERR_WRITE_FILE; }
    write_u32(riff_size);
    std::fwrite("WAVE", 1, 4, f);
    std::fwrite("fmt ", 1, 4, f);
    write_u32(fmt_chunk_size);
    write_u16(audio_format);
    write_u16(num_channels);
    write_u32(static_cast<uint32_t>(sample_rate));
    write_u32(byte_rate);
    write_u16(block_align);
    write_u16(bits_per_sample);
    std::fwrite("data", 1, 4, f);
    write_u32(data_bytes);

    // Conversão float[-1,1] → int16 com clamp e write em chunks de 4096
    // amostras pra evitar buffers gigantes na pilha.
    constexpr size_t kChunk = 4096;
    int16_t scratch[kChunk];
    for (size_t i = 0; i < num_samples; i += kChunk) {
        const size_t n = std::min(kChunk, num_samples - i);
        for (size_t j = 0; j < n; ++j) {
            float s = samples[i + j];
            if (s > 1.0f) s = 1.0f;
            if (s < -1.0f) s = -1.0f;
            scratch[j] = static_cast<int16_t>(s * 32767.0f);
        }
        if (std::fwrite(scratch, sizeof(int16_t), n, f) != n) {
            std::fclose(f);
            return TF_RECORDER_ERR_WRITE_FILE;
        }
    }

    std::fclose(f);
    return TF_RECORDER_OK;
}

} // namespace

extern "C" {

int recorder_start(int sample_rate, int max_seconds) {
    if (sample_rate <= 0 || max_seconds <= 0) return TF_RECORDER_ERR_INVALID_ARG;

    std::lock_guard<std::mutex> lock(g_lifecycle_mutex);
    if (g_active.load()) return TF_RECORDER_ERR_ALREADY_ACTIVE;

    const size_t total_frames =
        static_cast<size_t>(sample_rate) * static_cast<size_t>(max_seconds);
    try {
        g_buffer.assign(total_frames, 0.0f);
    } catch (const std::bad_alloc&) {
        return TF_RECORDER_ERR_OOM;
    }

    g_write_pos.store(0);
    g_sample_rate.store(sample_rate);
    g_active.store(true);
    return TF_RECORDER_OK;
}

int recorder_stop_and_save(const char* path) {
    std::lock_guard<std::mutex> lock(g_lifecycle_mutex);
    if (!g_active.load()) return TF_RECORDER_ERR_NOT_ACTIVE;
    if (path == nullptr) return TF_RECORDER_ERR_INVALID_ARG;

    g_active.store(false);
    const size_t recorded = g_write_pos.load();
    const int sr = g_sample_rate.load();

    int rc = write_wav(path, g_buffer.data(), recorded, sr);

    g_buffer.clear();
    g_buffer.shrink_to_fit();
    g_write_pos.store(0);
    return rc;
}

void recorder_discard() {
    std::lock_guard<std::mutex> lock(g_lifecycle_mutex);
    g_active.store(false);
    g_buffer.clear();
    g_buffer.shrink_to_fit();
    g_write_pos.store(0);
}

bool recorder_is_active() {
    return g_active.load();
}

int recorder_recorded_frames() {
    return static_cast<int>(g_write_pos.load());
}

double recorder_recorded_seconds() {
    const int sr = g_sample_rate.load();
    if (sr <= 0) return 0.0;
    return static_cast<double>(g_write_pos.load()) / static_cast<double>(sr);
}

void recorder_feed(const float* samples, int num_samples) {
    if (!g_active.load() || num_samples <= 0 || samples == nullptr) return;

    // Reserva slots atomicamente. Se ultrapassar a capacidade, descarta o
    // resto e desliga (gravação atinge o limite máximo).
    const size_t to_write = static_cast<size_t>(num_samples);
    const size_t start = g_write_pos.fetch_add(to_write);
    const size_t cap = g_buffer.size();
    if (start >= cap) {
        g_active.store(false);
        return;
    }
    const size_t writable = std::min(to_write, cap - start);
    std::memcpy(g_buffer.data() + start, samples, writable * sizeof(float));
    if (writable < to_write) {
        g_active.store(false);
    }
}

} // extern "C"
