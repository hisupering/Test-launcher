#include <jni.h>
#include <android/native_window_jni.h>
extern "C" void simple_engine_gl_start(ANativeWindow*);
extern "C" void simple_engine_gl_frame();
extern "C" void simple_engine_gl_stop();
static ANativeWindow* g_window=nullptr;
extern "C" JNIEXPORT void JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_attachSurface(JNIEnv* env,jclass,jobject surface){if(g_window){ANativeWindow_release(g_window);g_window=nullptr;}if(surface){g_window=ANativeWindow_fromSurface(env,surface);simple_engine_gl_start(g_window);}}
extern "C" JNIEXPORT void JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_detachSurface(JNIEnv*,jclass){simple_engine_gl_stop();if(g_window){ANativeWindow_release(g_window);g_window=nullptr;}}
extern "C" JNIEXPORT void JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_renderFrame(JNIEnv*,jclass){simple_engine_gl_frame();}
extern "C" JNIEXPORT void JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_sendTouch(JNIEnv*,jclass,jint,jfloat,jfloat,jfloat){}
