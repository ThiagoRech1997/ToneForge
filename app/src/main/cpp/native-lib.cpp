#include <jni.h>
#include <string>
#include <vector>
#include "audio_engine.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_thiagofernendorech_toneforge_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "ToneForge - Pedaleira Digital";
    return env->NewStringUTF(hello.c_str());
}

// Métodos JNI para controle dos efeitos
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_MainActivity_setGain(
        JNIEnv* env,
        jobject /* this */,
        jfloat gain) {
    setGain(gain);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_MainActivity_setDistortion(
        JNIEnv* env,
        jobject /* this */,
        jfloat amount) {
    setDistortion(amount);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_MainActivity_setDelay(
        JNIEnv* env,
        jobject /* this */,
        jfloat time,
        jfloat feedback) {
    setDelay(time, feedback);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_MainActivity_setReverb(
        JNIEnv* env,
        jobject /* this */,
        jfloat roomSize,
        jfloat damping) {
    setReverb(roomSize, damping);
}

// Métodos JNI para processamento de áudio
extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_MainActivity_processSample(
        JNIEnv* env,
        jobject /* this */,
        jfloat input) {
    return processSample(input);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_MainActivity_processBuffer(
        JNIEnv* env,
        jobject /* this */,
        jfloatArray input,
        jfloatArray output,
        jint numSamples) {
    
    jfloat* inputPtr = env->GetFloatArrayElements(input, nullptr);
    jfloat* outputPtr = env->GetFloatArrayElements(output, nullptr);
    
    processBuffer(inputPtr, outputPtr, numSamples);
    
    env->ReleaseFloatArrayElements(input, inputPtr, JNI_ABORT);
    env->ReleaseFloatArrayElements(output, outputPtr, 0);
}

// Métodos JNI para inicialização e limpeza
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_MainActivity_initAudioEngine(
        JNIEnv* env,
        jobject /* this */) {
    initAudioEngine();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_MainActivity_cleanupAudioEngine(
        JNIEnv* env,
        jobject /* this */) {
    cleanupAudioEngine();
}

// --------- NOVOS MÉTODOS JNI PARA AudioEngine.java ---------
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setGainEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setGainEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setGainLevel(JNIEnv* env, jclass clazz, jfloat level) {
    setGain(level);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDistortionEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setDistortionEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDistortionLevel(JNIEnv* env, jclass clazz, jfloat level) {
    setDistortion(level);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDelayEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setDelayEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDelayLevel(JNIEnv* env, jclass clazz, jfloat level) {
    setDelay(level, 0.5f); // Usar level como tempo, feedback fixo em 0.5
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setReverbEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setReverbEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setReverbLevel(JNIEnv* env, jclass clazz, jfloat level) {
    setReverb(level, 0.5f); // Usar level como roomSize, damping fixo em 0.5
}

// Processamento de áudio para AudioEngine
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_processBuffer(JNIEnv* env, jclass clazz, jfloatArray input, jfloatArray output, jint numSamples) {
    jfloat* inputPtr = env->GetFloatArrayElements(input, nullptr);
    jfloat* outputPtr = env->GetFloatArrayElements(output, nullptr);
    
    processBuffer(inputPtr, outputPtr, numSamples);
    
    env->ReleaseFloatArrayElements(input, inputPtr, JNI_ABORT);
    env->ReleaseFloatArrayElements(output, outputPtr, 0);
}

// Inicialização e limpeza para AudioEngine
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_initAudioEngine(JNIEnv* env, jclass clazz) {
    initAudioEngine();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_cleanupAudioEngine(JNIEnv* env, jclass clazz) {
    cleanupAudioEngine();
}

// Looper
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_startLooperRecording(JNIEnv* env, jclass clazz) {
    startLooperRecording();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_stopLooperRecording(JNIEnv* env, jclass clazz) {
    stopLooperRecording();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_startLooperPlayback(JNIEnv* env, jclass clazz) {
    startLooperPlayback();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_stopLooperPlayback(JNIEnv* env, jclass clazz) {
    stopLooperPlayback();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_clearLooper(JNIEnv* env, jclass clazz) {
    clearLooper();
}

// Gravador (placeholder - implementar futuramente)
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_startRecording(JNIEnv* env, jclass clazz) {
    // TODO: Implementar gravação
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_stopRecording(JNIEnv* env, jclass clazz) {
    // TODO: Implementar parada de gravação
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_playLastRecording(JNIEnv* env, jclass clazz) {
    // TODO: Implementar reprodução
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_stopPlayback(JNIEnv* env, jclass clazz) {
    // TODO: Implementar parada de reprodução
}

// Metrônomo
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_startMetronome(JNIEnv* env, jclass clazz, jint bpm) {
    startMetronome(bpm);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_stopMetronome(JNIEnv* env, jclass clazz) {
    stopMetronome();
}

// --------- JNI para Afinador ---------
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_startTuner(JNIEnv* env, jclass clazz) {
    startTuner();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_stopTuner(JNIEnv* env, jclass clazz) {
    stopTuner();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isTunerActive(JNIEnv* env, jclass clazz) {
    return isTunerActive();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getDetectedFrequency(JNIEnv* env, jclass clazz) {
    return getDetectedFrequency();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_processTunerBuffer(JNIEnv* env, jclass clazz, jfloatArray input, jint numSamples) {
    jfloat* inputPtr = env->GetFloatArrayElements(input, nullptr);
    processTunerBuffer(inputPtr, numSamples);
    env->ReleaseFloatArrayElements(input, inputPtr, JNI_ABORT);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDelayFeedback(JNIEnv* env, jclass clazz, jfloat feedback) {
    setDelay(getDelayTime(), feedback);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setReverbRoomSize(JNIEnv* env, jclass clazz, jfloat roomSize) {
    setReverb(roomSize, getReverbDamping());
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setReverbDamping(JNIEnv* env, jclass clazz, jfloat damping) {
    setReverb(getReverbRoomSize(), damping);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setEffectOrder(JNIEnv* env, jclass clazz, jobjectArray order) {
    int count = env->GetArrayLength(order);
    std::vector<const char*> orderVec(count);
    for (int i = 0; i < count; ++i) {
        jstring str = (jstring)env->GetObjectArrayElement(order, i);
        orderVec[i] = env->GetStringUTFChars(str, nullptr);
    }
    setEffectOrder(orderVec.data(), count);
    for (int i = 0; i < count; ++i) {
        jstring str = (jstring)env->GetObjectArrayElement(order, i);
        env->ReleaseStringUTFChars(str, orderVec[i]);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDistortionType(JNIEnv* env, jclass clazz, jint type) {
    setDistortionType(type);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDistortionMix(JNIEnv* env, jclass clazz, jfloat mix) {
    setDistortionMix(mix);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDelayMix(JNIEnv* env, jclass clazz, jfloat mix) {
    setDelayMix(mix);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDelayTime(JNIEnv* env, jclass clazz, jfloat timeMs) {
    setDelayTime(timeMs);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDelaySyncBPM(JNIEnv* env, jclass clazz, jboolean sync) {
    setDelaySyncBPM(sync);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDelayBPM(JNIEnv* env, jclass clazz, jint bpm) {
    setDelayBPM(bpm);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setReverbMix(JNIEnv* env, jclass clazz, jfloat mix) {
    setReverbMix(mix);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setChorusEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setChorusEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setChorusDepth(JNIEnv* env, jclass clazz, jfloat depth) {
    setChorusDepth(depth);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setChorusRate(JNIEnv* env, jclass clazz, jfloat rate) {
    setChorusRate(rate);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setChorusMix(JNIEnv* env, jclass clazz, jfloat mix) {
    setChorusMix(mix);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setFlangerEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setFlangerEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setFlangerDepth(JNIEnv* env, jclass clazz, jfloat depth) {
    setFlangerDepth(depth);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setFlangerRate(JNIEnv* env, jclass clazz, jfloat rate) {
    setFlangerRate(rate);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setFlangerFeedback(JNIEnv* env, jclass clazz, jfloat feedback) {
    setFlangerFeedback(feedback);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setFlangerMix(JNIEnv* env, jclass clazz, jfloat mix) {
    setFlangerMix(mix);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setPhaserEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setPhaserEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setPhaserDepth(JNIEnv* env, jclass clazz, jfloat depth) {
    setPhaserDepth(depth);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setPhaserRate(JNIEnv* env, jclass clazz, jfloat rate) {
    setPhaserRate(rate);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setPhaserFeedback(JNIEnv* env, jclass clazz, jfloat feedback) {
    setPhaserFeedback(feedback);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setPhaserMix(JNIEnv* env, jclass clazz, jfloat mix) {
    setPhaserMix(mix);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setEQEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setEQEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setEQLow(JNIEnv* env, jclass clazz, jfloat gain) {
    setEQLow(gain);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setEQMid(JNIEnv* env, jclass clazz, jfloat gain) {
    setEQMid(gain);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setEQHigh(JNIEnv* env, jclass clazz, jfloat gain) {
    setEQHigh(gain);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setEQMix(JNIEnv* env, jclass clazz, jfloat mix) {
    setEQMix(mix);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setCompressorEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setCompressorEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setCompressorThreshold(JNIEnv* env, jclass clazz, jfloat threshold) {
    setCompressorThreshold(threshold);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setCompressorRatio(JNIEnv* env, jclass clazz, jfloat ratio) {
    setCompressorRatio(ratio);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setCompressorAttack(JNIEnv* env, jclass clazz, jfloat attack) {
    setCompressorAttack(attack);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setCompressorRelease(JNIEnv* env, jclass clazz, jfloat release) {
    setCompressorRelease(release);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setCompressorMix(JNIEnv* env, jclass clazz, jfloat mix) {
    setCompressorMix(mix);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setReverbType(JNIEnv* env, jclass clazz, jint type) {
    setReverbType(type);
}

// Funções JNI para Oversampling
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setOversamplingEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setOversamplingEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setOversamplingFactor(JNIEnv* env, jclass clazz, jint factor) {
    setOversamplingFactor(factor);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isOversamplingEnabled(JNIEnv* env, jclass clazz) {
    return isOversamplingEnabled();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getOversamplingFactor(JNIEnv* env, jclass clazz) {
    return getOversamplingFactor();
}

// Novas funções JNI para looper avançado
extern "C" JNIEXPORT jint JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperLength(JNIEnv* env, jclass clazz) {
    return getLooperLength();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperPosition(JNIEnv* env, jclass clazz) {
    return getLooperPosition();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperPosition(JNIEnv* env, jclass clazz, jint position) {
    setLooperPosition(position);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperTrackVolume(JNIEnv* env, jclass clazz, jint trackIndex, jfloat volume) {
    setLooperTrackVolume(trackIndex, volume);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperTrackMuted(JNIEnv* env, jclass clazz, jint trackIndex, jboolean muted) {
    setLooperTrackMuted(trackIndex, muted);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperTrackSoloed(JNIEnv* env, jclass clazz, jint trackIndex, jboolean soloed) {
    setLooperTrackSoloed(trackIndex, soloed);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_removeLooperTrack(JNIEnv* env, jclass clazz, jint trackIndex) {
    removeLooperTrack(trackIndex);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperBPM(JNIEnv* env, jclass clazz, jint bpm) {
    setLooperBPM(bpm);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperSyncEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setLooperSyncEnabled(enabled);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperRecording(JNIEnv* env, jclass clazz) {
    return isLooperRecording();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperPlaying(JNIEnv* env, jclass clazz) {
    return isLooperPlaying();
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperMix(JNIEnv* env, jclass clazz) {
    int length = 0;
    float* mix = getLooperMix(&length);
    if (length == 0 || mix == nullptr) {
        return env->NewFloatArray(0);
    }
    jfloatArray result = env->NewFloatArray(length);
    env->SetFloatArrayRegion(result, 0, length, mix);
    delete[] mix;
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_loadLooperFromAudio(JNIEnv* env, jclass clazz, jfloatArray audioData) {
    jsize length = env->GetArrayLength(audioData);
    jfloat* data = env->GetFloatArrayElements(audioData, nullptr);
    
    loadLooperFromAudio(data, length);
    
    env->ReleaseFloatArrayElements(audioData, data, JNI_ABORT);
}

// Funcionalidades especiais do looper
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperReverse(JNIEnv* env, jclass clazz, jboolean enabled) {
    setLooperReverse(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperSpeed(JNIEnv* env, jclass clazz, jfloat speed) {
    setLooperSpeed(speed);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperPitchShift(JNIEnv* env, jclass clazz, jfloat semitones) {
    setLooperPitchShift(semitones);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperStutter(JNIEnv* env, jclass clazz, jboolean enabled, jfloat rate) {
    setLooperStutter(enabled, rate);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperReverseEnabled(JNIEnv* env, jclass clazz) {
    return isLooperReverseEnabled();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperSpeed(JNIEnv* env, jclass clazz) {
    return getLooperSpeed();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperPitchShift(JNIEnv* env, jclass clazz) {
    return getLooperPitchShift();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperStutterEnabled(JNIEnv* env, jclass clazz) {
    return isLooperStutterEnabled();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperStutterRate(JNIEnv* env, jclass clazz) {
    return getLooperStutterRate();
}

// Funcionalidade de Slicing
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperSlicingEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setLooperSlicingEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperSlicePoints(JNIEnv* env, jclass clazz, jintArray points) {
    jint* pointsPtr = env->GetIntArrayElements(points, nullptr);
    jsize length = env->GetArrayLength(points);
    setLooperSlicePoints(pointsPtr, length);
    env->ReleaseIntArrayElements(points, pointsPtr, JNI_ABORT);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperSliceLength(JNIEnv* env, jclass clazz, jint length) {
    setLooperSliceLength(length);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperSlicingEnabled(JNIEnv* env, jclass clazz) {
    return isLooperSlicingEnabled();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperSliceLength(JNIEnv* env, jclass clazz) {
    return getLooperSliceLength();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperNumSlices(JNIEnv* env, jclass clazz) {
    return getLooperNumSlices();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperSliceOrder(JNIEnv* env, jclass clazz, jintArray order) {
    jint* orderPtr = env->GetIntArrayElements(order, nullptr);
    jsize length = env->GetArrayLength(order);
    setLooperSliceOrder(orderPtr, length);
    env->ReleaseIntArrayElements(order, orderPtr, JNI_ABORT);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_randomizeLooperSlices(JNIEnv* env, jclass clazz) {
    randomizeLooperSlices();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_reverseLooperSlices(JNIEnv* env, jclass clazz) {
    reverseLooperSlices();
}

// === JNI PARA EFEITOS AVANÇADOS DO LOOPER (FASE 5) ===

// Compressão automática
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperAutoCompression(JNIEnv* env, jclass clazz, jboolean enabled) {
    setLooperAutoCompression(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperCompressionThreshold(JNIEnv* env, jclass clazz, jfloat threshold) {
    setLooperCompressionThreshold(threshold);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperCompressionRatio(JNIEnv* env, jclass clazz, jfloat ratio) {
    setLooperCompressionRatio(ratio);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperCompressionAttack(JNIEnv* env, jclass clazz, jfloat attack) {
    setLooperCompressionAttack(attack);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperCompressionRelease(JNIEnv* env, jclass clazz, jfloat release) {
    setLooperCompressionRelease(release);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperAutoCompressionEnabled(JNIEnv* env, jclass clazz) {
    return isLooperAutoCompressionEnabled();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperCompressionThreshold(JNIEnv* env, jclass clazz) {
    return getLooperCompressionThreshold();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperCompressionRatio(JNIEnv* env, jclass clazz) {
    return getLooperCompressionRatio();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperCompressionAttack(JNIEnv* env, jclass clazz) {
    return getLooperCompressionAttack();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperCompressionRelease(JNIEnv* env, jclass clazz) {
    return getLooperCompressionRelease();
}

// Normalização automática
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperAutoNormalization(JNIEnv* env, jclass clazz, jboolean enabled) {
    setLooperAutoNormalization(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperNormalizationTarget(JNIEnv* env, jclass clazz, jfloat target) {
    setLooperNormalizationTarget(target);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperAutoNormalizationEnabled(JNIEnv* env, jclass clazz) {
    return isLooperAutoNormalizationEnabled();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperNormalizationTarget(JNIEnv* env, jclass clazz) {
    return getLooperNormalizationTarget();
}

// Filtros
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperLowPassFilter(JNIEnv* env, jclass clazz, jboolean enabled) {
    setLooperLowPassFilter(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperLowPassFrequency(JNIEnv* env, jclass clazz, jfloat frequency) {
    setLooperLowPassFrequency(frequency);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperHighPassFilter(JNIEnv* env, jclass clazz, jboolean enabled) {
    setLooperHighPassFilter(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperHighPassFrequency(JNIEnv* env, jclass clazz, jfloat frequency) {
    setLooperHighPassFrequency(frequency);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperLowPassEnabled(JNIEnv* env, jclass clazz) {
    return isLooperLowPassEnabled();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperHighPassEnabled(JNIEnv* env, jclass clazz) {
    return isLooperHighPassEnabled();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperLowPassFrequency(JNIEnv* env, jclass clazz) {
    return getLooperLowPassFrequency();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperHighPassFrequency(JNIEnv* env, jclass clazz) {
    return getLooperHighPassFrequency();
}

// Reverb de cauda
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperReverbTail(JNIEnv* env, jclass clazz, jboolean enabled) {
    setLooperReverbTail(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperReverbTailDecay(JNIEnv* env, jclass clazz, jfloat decay) {
    setLooperReverbTailDecay(decay);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperReverbTailMix(JNIEnv* env, jclass clazz, jfloat mix) {
    setLooperReverbTailMix(mix);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperReverbTailEnabled(JNIEnv* env, jclass clazz) {
    return isLooperReverbTailEnabled();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperReverbTailDecay(JNIEnv* env, jclass clazz) {
    return getLooperReverbTailDecay();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperReverbTailMix(JNIEnv* env, jclass clazz) {
    return getLooperReverbTailMix();
}

// === JNI PARA INTEGRAÇÃO AVANÇADA (FASE 6) ===

// Quantização
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperQuantization(JNIEnv* env, jclass clazz, jboolean enabled) {
    setLooperQuantization(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperQuantizationGrid(JNIEnv* env, jclass clazz, jfloat gridSize) {
    setLooperQuantizationGrid(gridSize);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperQuantizationEnabled(JNIEnv* env, jclass clazz) {
    return isLooperQuantizationEnabled();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperQuantizationGrid(JNIEnv* env, jclass clazz) {
    return getLooperQuantizationGrid();
}

// Fade In/Out automático
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperAutoFadeIn(JNIEnv* env, jclass clazz, jboolean enabled) {
    setLooperAutoFadeIn(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperAutoFadeOut(JNIEnv* env, jclass clazz, jboolean enabled) {
    setLooperAutoFadeOut(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperFadeInDuration(JNIEnv* env, jclass clazz, jfloat duration) {
    setLooperFadeInDuration(duration);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperFadeOutDuration(JNIEnv* env, jclass clazz, jfloat duration) {
    setLooperFadeOutDuration(duration);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperAutoFadeInEnabled(JNIEnv* env, jclass clazz) {
    return isLooperAutoFadeInEnabled();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperAutoFadeOutEnabled(JNIEnv* env, jclass clazz) {
    return isLooperAutoFadeOutEnabled();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperFadeInDuration(JNIEnv* env, jclass clazz) {
    return getLooperFadeInDuration();
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperFadeOutDuration(JNIEnv* env, jclass clazz) {
    return getLooperFadeOutDuration();
}

// Integração MIDI
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperMidiEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setLooperMidiEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperMidiChannel(JNIEnv* env, jclass clazz, jint channel) {
    setLooperMidiChannel(channel);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperMidiCCMapping(JNIEnv* env, jclass clazz, jint ccNumber, jint function) {
    setLooperMidiCCMapping(ccNumber, function);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperMidiEnabled(JNIEnv* env, jclass clazz) {
    return isLooperMidiEnabled();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_getLooperMidiChannel(JNIEnv* env, jclass clazz) {
    return getLooperMidiChannel();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_processLooperMidiMessage(JNIEnv* env, jclass clazz, jint status, jint data1, jint data2) {
    processLooperMidiMessage(status, data1, data2);
}

// Notificações
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperNotificationEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {
    setLooperNotificationEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setLooperNotificationControls(JNIEnv* env, jclass clazz, jboolean showControls) {
    setLooperNotificationControls(showControls);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperNotificationEnabled(JNIEnv* env, jclass clazz) {
    return isLooperNotificationEnabled();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_isLooperNotificationControlsEnabled(JNIEnv* env, jclass clazz) {
    return isLooperNotificationControlsEnabled();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_updateLooperNotificationState(JNIEnv* env, jclass clazz) {
    updateLooperNotificationState();
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_cutLooperRegion(JNIEnv* env, jclass clazz, jfloat start, jfloat end) {
    cutLooperRegion(start, end);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_applyLooperFadeIn(JNIEnv* env, jclass clazz, jfloat start, jfloat end) {
    applyLooperFadeIn(start, end);
}

extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_applyLooperFadeOut(JNIEnv* env, jclass clazz, jfloat start, jfloat end) {
    applyLooperFadeOut(start, end);
}