#include <jni.h>
#include <krisp-audio-sdk.hpp>
#include <krisp-audio-sdk-nc.hpp>
#include <krisp-audio-sdk-nc-stats.hpp>
#include <krisp-audio-sdk-vad.hpp>
#include <krisp-audio-sdk-noise-db.hpp>
#include <krisp-audio-sdk-rt.hpp>
#include <unistd.h>
#include <sched.h>
#include <logging_macros.h>
#include <sys/resource.h>
#include <thread>
#include <string>
#include <iostream>
#include <vector>
#include <cstdio>


unsigned const int BUFFER_SIZE = 320;

int mWavLength;
short mFrameOut[BUFFER_SIZE];
short *mWavDataOutput;
jshort *mWavDataInput;
KrispAudioSessionID mSession;
jobject mActivityInstance;
JavaVM *mJvm;
JNIEnv *mEnv;
jclass mJclass;
jmethodID mMethodID;
std::thread mNoiseCancellationThread;
std::thread mVADThread;
cpu_set_t mCpuSet;

#define STRING(num) #num

extern "C" {

wchar_t *charToWcharT(const char *ch) {
    const size_t cSize = strlen(ch) + 1;
    auto *wc = new wchar_t[cSize];
    mbstowcs(wc, ch, cSize);
    return wc;
}

void writeData() {
    mJvm->AttachCurrentThread(&mEnv, nullptr);
    mJclass = mEnv->GetObjectClass(mActivityInstance);

    if (mJclass == nullptr) {
        LOGE("Can't find Java class.");
    }

    mMethodID = mEnv->GetMethodID(mJclass, "writeWavData", "([B)V");
    jbyteArray array = mEnv->NewByteArray(mWavLength);
    mEnv->SetByteArrayRegion(array, 0, mWavLength, (jbyte *) mWavDataOutput);
    mEnv->CallVoidMethod(mActivityInstance, mMethodID, array);
    delete[] mWavDataOutput;
}

void NC_processing() {
    sched_setaffinity(gettid(), sizeof(cpu_set_t), &mCpuSet);

    // declare temporal input data for the noise cancelling
    short stepIn[BUFFER_SIZE];
    int lengthForShort = mWavLength / 2;


    for (int i = 0; i < lengthForShort - BUFFER_SIZE; i += BUFFER_SIZE) {
        // copy input from mWavDataInput into input array
        memcpy(stepIn, mWavDataInput + i, 2 * BUFFER_SIZE);
        krispAudioNcCleanAmbientNoiseInt16(mSession, stepIn, BUFFER_SIZE, mFrameOut, BUFFER_SIZE);
        // copy output to mWavDataOutput
        memcpy(mWavDataOutput + i, mFrameOut, 2 * BUFFER_SIZE);
    }
}

void NC_processingChunk() {
    sched_setaffinity(gettid(), sizeof(cpu_set_t), &mCpuSet);

    // declare temporal input data for the noise cancelling
    short stepIn[BUFFER_SIZE];
    int lengthForShort = mWavLength / 2;

    for (int i = 0; i < lengthForShort - BUFFER_SIZE; i += BUFFER_SIZE) {
        // copy input from mWavDataInput into input array
        memcpy(stepIn, mWavDataInput + i, 2 * BUFFER_SIZE);
        krispAudioNcCleanAmbientNoiseInt16(mSession, stepIn, BUFFER_SIZE, mFrameOut, BUFFER_SIZE);
        // copy output to mWavDataOutput
        memcpy(mWavDataOutput + i, mFrameOut, 2 * BUFFER_SIZE);
    }
}

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *jvm, void *reserved) {
    mJvm = jvm;
    int status = mJvm->GetEnv((void **) &mEnv, JNI_VERSION_1_6);

    if (status < 0) {
        LOGE("Failed to get JNI environment, assuming native thread");
        status = mJvm->AttachCurrentThread(&mEnv, nullptr);
        if (status < 0) {
            LOGE("Failed to attach current thread");
            return JNI_ERR;
        }
    }

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
Java_com_truvideo_sdk_video_noisecancel_TruvideoNoiseCancellation_processNC(JNIEnv *env, jobject activityInstance, jbyteArray jWavData) {
    mActivityInstance = activityInstance;

    // gets array length from WAV data provided from Android
    mWavLength = env->GetArrayLength(jWavData);
    mWavDataOutput = new short[mWavLength]; // output data array

    mWavDataInput = reinterpret_cast<short *>(env->GetByteArrayElements(jWavData, nullptr)); // copy input data and cast each element

    NC_processing();
    writeData();
}

JNIEXPORT jbyteArray JNICALL
Java_com_truvideo_sdk_video_noisecancel_NoiseCancelNative_processNCChunk(JNIEnv *env, jobject activityInstance, jbyteArray chunk, jint chunkLength) {
    mActivityInstance = activityInstance;

    mWavDataOutput = new short[chunkLength];

    mWavLength = chunkLength;
    mWavDataInput = reinterpret_cast<short *>(env->GetByteArrayElements(chunk, nullptr));

    NC_processingChunk();

    jbyteArray array = env->NewByteArray(chunkLength);
    env->SetByteArrayRegion(array, 0, chunkLength, (jbyte *) mWavDataOutput);

    return array;
}

JNIEXPORT void JNICALL
Java_com_truvideo_sdk_video_noisecancel_NoiseCancelNative_globalInit(JNIEnv *env, jobject clazz, jstring workPath) {
    const char *pWorkPath = env->GetStringUTFChars(workPath, nullptr);
    std::string str(pWorkPath);
    std::wstring wstr(str.begin(), str.end());
    const wchar_t *wc = wstr.c_str();
    if (krispAudioGlobalInit(wc) == 0) {
        LOGI("Global init is successful");
    } else {
        LOGE("Global init is not successful");
    }
    env->ReleaseStringUTFChars(workPath, pWorkPath);
}

JNIEXPORT void JNICALL
Java_com_truvideo_sdk_video_noisecancel_NoiseCancelNative_setModel(JNIEnv *env, jobject thiz, jstring configFilePath, jstring modelName) {
    const char *ch = env->GetStringUTFChars(configFilePath, nullptr);
    const char *ch_name = env->GetStringUTFChars(modelName, nullptr);
    LOGI("Setting model: %s , Model Name : %s", ch, ch_name);
    int st = krispAudioSetModel(charToWcharT(ch), ch_name);
    if (st == 0) {
        LOGI("Model is set successful");
    } else {
        LOGE("Model is set unsuccessful : %d", st);
    }
    env->ReleaseStringUTFChars(configFilePath, ch);
    env->ReleaseStringUTFChars(modelName, ch_name);
}

JNIEXPORT void JNICALL
Java_com_truvideo_sdk_video_noisecancel_NoiseCancelNative_globalDestroy(JNIEnv *env, jobject thiz) {
    if (krispAudioGlobalDestroy() == 0) {
        LOGI("Global destroy is successful");
    } else {
        LOGE("Global destroy is not successful");
    }
}

JNIEXPORT void JNICALL
Java_com_truvideo_sdk_video_noisecancel_NoiseCancelNative_createSession(JNIEnv *env, jobject thiz, jstring modelName) {
    const char *ch_name = env->GetStringUTFChars(modelName, nullptr);
    mSession = krispAudioNcCreateSession(
            KRISP_AUDIO_SAMPLING_RATE_32000HZ,
            KRISP_AUDIO_SAMPLING_RATE_32000HZ,
            KRISP_AUDIO_FRAME_DURATION_10MS,
            ch_name
    );
    if (mSession != nullptr) {
        LOGI("NC Session is created");
    } else {
        LOGE("NC Session is not created");
    }
    env->ReleaseStringUTFChars(modelName, ch_name);
}

JNIEXPORT void JNICALL
Java_com_truvideo_sdk_video_noisecancel_NoiseCancelNative_closeSession(JNIEnv *env, jobject thiz) {
    krispAudioNcCloseSession(mSession);
}

}
