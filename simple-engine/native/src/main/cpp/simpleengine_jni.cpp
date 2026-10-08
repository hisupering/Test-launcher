#include <jni.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"SimpleEngine",__VA_ARGS__)
static ANativeWindow* g_window=nullptr;
extern "C" JNIEXPORT void JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_attachSurface(JNIEnv* env,jclass,jobject surface){if(g_window){ANativeWindow_release(g_window);g_window=nullptr;} if(surface) g_window=ANativeWindow_fromSurface(env,surface); LOGI("surface attached");}
extern "C" JNIEXPORT void JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_detachSurface(JNIEnv*,jclass){if(g_window){ANativeWindow_release(g_window);g_window=nullptr;} LOGI("surface detached");}
extern "C" JNIEXPORT void JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_sendTouch(JNIEnv*,jclass,jint action,jfloat x,jfloat y,jfloat pressure){LOGI("touch action=%d x=%f y=%f p=%f",action,x,y,pressure);}
