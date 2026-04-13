#include "wav_writer.h"

#include <algorithm>
#include <cstdint>
#include <cstdio>

namespace toneforge::internal {

int write_wav_mono16(const char* path, const float* samples, size_t num_samples, int sample_rate) {
    FILE* f = std::fopen(path, "wb");
    if (!f) return -1;

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

    if (std::fwrite("RIFF", 1, 4, f) != 4) { std::fclose(f); return -2; }
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
            return -2;
        }
    }

    std::fclose(f);
    return 0;
}

} // namespace toneforge::internal
