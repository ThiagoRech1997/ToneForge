#include "toneforge/recorder.h"
#include "wav_writer.h"

#include <atomic>
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

// Wrapper que mapeia os códigos do wav_writer interno para TF_RECORDER_ERR_*.
int write_wav(const char* path, const float* samples, size_t num_samples, int sample_rate) {
    const int rc = toneforge::internal::write_wav_mono16(path, samples, num_samples, sample_rate);
    if (rc == -1) return TF_RECORDER_ERR_OPEN_FILE;
    if (rc == -2) return TF_RECORDER_ERR_WRITE_FILE;
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
