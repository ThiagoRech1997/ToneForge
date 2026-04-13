#include "toneforge/loop_io.h"
#include "toneforge/audio_engine.h"
#include "wav_writer.h"

#include <cstddef>

extern "C" {

int looper_save_wav(const char* path, int sample_rate) {
    if (path == nullptr || sample_rate <= 0) return TF_LOOP_IO_ERR_INVALID_ARG;

    int length = 0;
    const float* mix = getLooperMix(&length);
    if (mix == nullptr || length <= 0) return TF_LOOP_IO_ERR_EMPTY;

    const int rc = toneforge::internal::write_wav_mono16(
        path, mix, static_cast<std::size_t>(length), sample_rate);
    if (rc == -1) return TF_LOOP_IO_ERR_OPEN_FILE;
    if (rc == -2) return TF_LOOP_IO_ERR_WRITE_FILE;
    return TF_LOOP_IO_OK;
}

} // extern "C"
