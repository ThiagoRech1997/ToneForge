#include <jni.h>
#include <string>
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
Java_com_thiagofernendorech_toneforge_AudioEngine_setGainEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setGainLevel(JNIEnv* env, jclass clazz, jfloat level) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDistortionEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDistortionLevel(JNIEnv* env, jclass clazz, jfloat level) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDelayEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setDelayLevel(JNIEnv* env, jclass clazz, jfloat level) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setReverbEnabled(JNIEnv* env, jclass clazz, jboolean enabled) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_setReverbLevel(JNIEnv* env, jclass clazz, jfloat level) {}

// Looper
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_startLooperRecording(JNIEnv* env, jclass clazz) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_stopLooperRecording(JNIEnv* env, jclass clazz) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_startLooperPlayback(JNIEnv* env, jclass clazz) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_stopLooperPlayback(JNIEnv* env, jclass clazz) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_clearLooper(JNIEnv* env, jclass clazz) {}

// Gravador
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_startRecording(JNIEnv* env, jclass clazz) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_stopRecording(JNIEnv* env, jclass clazz) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_playLastRecording(JNIEnv* env, jclass clazz) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_stopPlayback(JNIEnv* env, jclass clazz) {}

// Metrônomo
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_startMetronome(JNIEnv* env, jclass clazz, jint bpm) {}
extern "C" JNIEXPORT void JNICALL
Java_com_thiagofernendorech_toneforge_AudioEngine_stopMetronome(JNIEnv* env, jclass clazz) {}

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