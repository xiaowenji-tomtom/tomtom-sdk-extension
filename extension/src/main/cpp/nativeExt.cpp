#include <jni.h>
#include <string>
#include <android/log.h>
#include "shadowhook.h"

#define LOG_TAG "NativeExt"
#define LOG(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_tomtom_sdk_extension_library_NavSdkExtension_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

/* ---------------------------------------------------------------------------------------------- */

typedef jlong (*setStyle11_t)(JNIEnv *, jclass, jlong, jobject, jlong, jobject);
setStyle11_t orig_setStyle11 = nullptr;
jlong hooked_setStyle11(JNIEnv *jenv, jclass jcls, jlong jarg1, jobject jarg1_, jlong jarg2, jobject jarg2_) {
    LOG("before setStyle11");
    // cast arg1 to MapImpl
    // change private member mMapStyler to custom instance
    return orig_setStyle11(jenv, jcls, jarg1, jarg1_, jarg2, jarg2_);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tomtom_sdk_extension_library_NavSdkExtension_hookForSmoothTransition(
        JNIEnv *env,
        jobject /* this */) {
    LOG("hook");
    shadowhook_hook_sym_name(
            "libtomtom-navsdk.so",
            "Java_com_tomtom_sdk_maps_display_engine_TomTomNavKitMapJNI_Map_1setStyle_1_1SWIG_11",
            (void *)hooked_setStyle11,
            (void **)&orig_setStyle11);
}
