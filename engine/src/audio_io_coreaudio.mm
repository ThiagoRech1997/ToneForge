// Fase 4 — Backend CoreAudio para iOS. Implementa a mesma API C de
// audio_io.h usando AudioUnit RemoteIO + AVAudioSession, espelhando o
// comportamento de audio_io_oboe.cpp. O callback de render puxa a
// entrada via AudioUnitRender, alimenta o tuner/recorder se ativos,
// chama processBuffer (DSP existente) e devolve a saída.
//
// NOTA: este arquivo NUNCA foi compilado porque o ambiente de
// desenvolvimento é Linux sem Xcode. Precisa de uma primeira
// verificação num macOS antes do push. Ver docs/ios-build.md.
//
// Obj-C++ é necessário para AVAudioSession (Obj-C); o resto é C++
// puro. Compatível com C++17.

#import <AVFoundation/AVFoundation.h>
#import <AudioToolbox/AudioToolbox.h>

#include "toneforge/audio_io.h"
#include "toneforge/audio_engine.h"
#include "toneforge/recorder.h"

#include <atomic>
#include <cstring>
#include <mutex>
#include <vector>

namespace {

// ====== estado global ======
AudioUnit g_remote_io = nullptr;
std::atomic<bool> g_running{false};
std::atomic<int> g_sample_rate{48000};
std::atomic<int> g_xrun_count{0};

// Scratch para o input buffer que puxamos do AudioUnitRender. Reused
// entre callbacks, redimensionado só se numFrames crescer — evita
// mallocs no caminho quente.
std::vector<float> g_input_scratch;

// Lock protege start/stop; callback de áudio não pega o lock.
std::mutex g_lifecycle_mutex;

// ====== render callback ======
OSStatus renderCallback(void* /*inRefCon*/,
                        AudioUnitRenderActionFlags* /*ioActionFlags*/,
                        const AudioTimeStamp* inTimeStamp,
                        UInt32 /*inBusNumber*/,
                        UInt32 inNumberFrames,
                        AudioBufferList* ioData) {
    if (ioData == nullptr || ioData->mNumberBuffers == 0) {
        return noErr;
    }

    float* out = static_cast<float*>(ioData->mBuffers[0].mData);

    // Garante scratch do tamanho do buffer corrente.
    if (static_cast<UInt32>(g_input_scratch.size()) < inNumberFrames) {
        g_input_scratch.resize(inNumberFrames);
    }
    float* in = g_input_scratch.data();

    // Puxa o input do bus 1. Passamos um AudioBufferList on-stack
    // apontando para o scratch — o AudioUnitRender escreve nele.
    AudioBufferList inputList;
    inputList.mNumberBuffers = 1;
    inputList.mBuffers[0].mNumberChannels = 1;
    inputList.mBuffers[0].mDataByteSize = inNumberFrames * sizeof(float);
    inputList.mBuffers[0].mData = in;

    const UInt32 kInputBus = 1;
    OSStatus err = AudioUnitRender(g_remote_io,
                                   nullptr, // no action flags out
                                   inTimeStamp,
                                   kInputBus,
                                   inNumberFrames,
                                   &inputList);
    if (err != noErr) {
        // No startup ou em underflow o input pode falhar; zera o
        // scratch para não injetar lixo no DSP e conta como xrun.
        std::memset(in, 0, inNumberFrames * sizeof(float));
        g_xrun_count.fetch_add(1);
    }

    // Encadeia tuner (mesmo padrão do Oboe).
    if (isTunerActive()) {
        processTunerBuffer(in, static_cast<int>(inNumberFrames));
    }

    // DSP — chamada idêntica ao backend Android.
    processBuffer(in, out, static_cast<int>(inNumberFrames),
                  static_cast<int>(inNumberFrames),
                  static_cast<int>(inNumberFrames));

    // Recorder pós-FX (mesmo padrão do Oboe).
    if (recorder_is_active()) {
        recorder_feed(out, static_cast<int>(inNumberFrames));
    }

    return noErr;
}

// Fecha e libera o RemoteIO. Chamado com lock segurado.
void teardown_locked() {
    if (g_remote_io != nullptr) {
        AudioOutputUnitStop(g_remote_io);
        AudioUnitUninitialize(g_remote_io);
        AudioComponentInstanceDispose(g_remote_io);
        g_remote_io = nullptr;
    }
    g_running.store(false);
    NSError* sessionError = nil;
    [[AVAudioSession sharedInstance] setActive:NO
                                   withOptions:AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
                                         error:&sessionError];
    (void)sessionError;
}

} // namespace

extern "C" {

int audio_engine_start(int sampleRate, int framesPerCallback) {
    std::lock_guard<std::mutex> lock(g_lifecycle_mutex);
    if (g_running.load()) return 0;

    // --- AVAudioSession ---
    NSError* sessionError = nil;
    AVAudioSession* session = [AVAudioSession sharedInstance];

    [session setCategory:AVAudioSessionCategoryPlayAndRecord
             withOptions:(AVAudioSessionCategoryOptionDefaultToSpeaker |
                          AVAudioSessionCategoryOptionAllowBluetoothA2DP |
                          AVAudioSessionCategoryOptionMixWithOthers)
                   error:&sessionError];
    if (sessionError != nil) return -10;

    // Modo measurement = menor latência, sem processamento de voz.
    [session setMode:AVAudioSessionModeMeasurement error:&sessionError];
    if (sessionError != nil) return -11;

    const double preferredSR = sampleRate > 0 ? static_cast<double>(sampleRate) : 48000.0;
    [session setPreferredSampleRate:preferredSR error:&sessionError];

    const double preferredBufFrames =
        framesPerCallback > 0 ? static_cast<double>(framesPerCallback) : 128.0;
    [session setPreferredIOBufferDuration:(preferredBufFrames / preferredSR)
                                    error:&sessionError];

    [session setActive:YES error:&sessionError];
    if (sessionError != nil) return -12;

    const double actualSR = session.sampleRate;

    // --- RemoteIO ---
    AudioComponentDescription desc = {0};
    desc.componentType = kAudioUnitType_Output;
    desc.componentSubType = kAudioUnitSubType_RemoteIO;
    desc.componentManufacturer = kAudioUnitManufacturer_Apple;

    AudioComponent component = AudioComponentFindNext(nullptr, &desc);
    if (component == nullptr) return -20;

    OSStatus err = AudioComponentInstanceNew(component, &g_remote_io);
    if (err != noErr) return -21;

    UInt32 flag = 1;
    const UInt32 kInputBus = 1;
    const UInt32 kOutputBus = 0;

    // Habilita I/O no bus de input. Bus de output já é enabled by default.
    err = AudioUnitSetProperty(g_remote_io,
                               kAudioOutputUnitProperty_EnableIO,
                               kAudioUnitScope_Input,
                               kInputBus,
                               &flag,
                               sizeof(flag));
    if (err != noErr) { teardown_locked(); return -22; }

    // Stream format: mono float32 na taxa efetiva da session.
    AudioStreamBasicDescription fmt = {0};
    fmt.mSampleRate = actualSR;
    fmt.mFormatID = kAudioFormatLinearPCM;
    fmt.mFormatFlags = kAudioFormatFlagIsFloat | kAudioFormatFlagIsPacked;
    fmt.mChannelsPerFrame = 1;
    fmt.mBitsPerChannel = 32;
    fmt.mBytesPerFrame = sizeof(float);
    fmt.mFramesPerPacket = 1;
    fmt.mBytesPerPacket = sizeof(float);

    // Output scope do bus de input = o que o render callback lê.
    err = AudioUnitSetProperty(g_remote_io,
                               kAudioUnitProperty_StreamFormat,
                               kAudioUnitScope_Output,
                               kInputBus,
                               &fmt,
                               sizeof(fmt));
    if (err != noErr) { teardown_locked(); return -23; }

    // Input scope do bus de output = o que o render callback escreve.
    err = AudioUnitSetProperty(g_remote_io,
                               kAudioUnitProperty_StreamFormat,
                               kAudioUnitScope_Input,
                               kOutputBus,
                               &fmt,
                               sizeof(fmt));
    if (err != noErr) { teardown_locked(); return -24; }

    // Render callback no bus de output.
    AURenderCallbackStruct callback = {0};
    callback.inputProc = &renderCallback;
    callback.inputProcRefCon = nullptr;
    err = AudioUnitSetProperty(g_remote_io,
                               kAudioUnitProperty_SetRenderCallback,
                               kAudioUnitScope_Input,
                               kOutputBus,
                               &callback,
                               sizeof(callback));
    if (err != noErr) { teardown_locked(); return -25; }

    err = AudioUnitInitialize(g_remote_io);
    if (err != noErr) { teardown_locked(); return -26; }

    err = AudioOutputUnitStart(g_remote_io);
    if (err != noErr) { teardown_locked(); return -27; }

    g_xrun_count.store(0);
    g_sample_rate.store(static_cast<int>(actualSR));
    g_running.store(true);

    // Propaga a taxa efetiva para o DSP existente (mesma chamada do
    // backend Oboe).
    setSampleRate(static_cast<int>(actualSR));

    return 0;
}

void audio_engine_stop() {
    std::lock_guard<std::mutex> lock(g_lifecycle_mutex);
    teardown_locked();
}

bool audio_engine_is_running() {
    return g_running.load();
}

double audio_engine_get_latency_ms() {
    if (!g_running.load()) return -1.0;
    AVAudioSession* s = [AVAudioSession sharedInstance];
    const double latency = s.inputLatency + s.outputLatency + s.IOBufferDuration;
    return latency * 1000.0;
}

int audio_engine_get_xrun_count() {
    return g_xrun_count.load();
}

int audio_engine_get_sample_rate() {
    return g_sample_rate.load();
}

} // extern "C"
