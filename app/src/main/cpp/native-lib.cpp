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