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