#include <jni.h>
#include <android/native_window_jni.h>
#include <mutex>
#include <queue>
#include <cstdint>
#include <chrono>

extern "C" void simple_engine_gl_start(ANativeWindow*);
extern "C" void simple_engine_gl_frame();
extern "C" void simple_engine_gl_stop();

struct TouchEvent { int action; int pointerId; float x; float y; float pressure; int64_t timeNs; };
static ANativeWindow* g_window = nullptr;
static std::mutex g_touchMutex;
static std::queue<TouchEvent> g_touchQueue;

extern "C" JNIEXPORT void JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_attachSurface(JNIEnv* env,jclass,jobject surface) {
    if (g_window) { ANativeWindow_release(g_window); g_window=nullptr; }
    if (surface) { g_window=ANativeWindow_fromSurface(env,surface); simple_engine_gl_start(g_window); }
}
extern "C" JNIEXPORT void JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_detachSurface(JNIEnv*,jclass) {
    simple_engine_gl_stop();
    if (g_window) { ANativeWindow_release(g_window); g_window=nullptr; }
}
extern "C" JNIEXPORT void JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_renderFrame(JNIEnv*,jclass) { simple_engine_gl_frame(); }

extern "C" JNIEXPORT void JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_sendTouch(JNIEnv*,jclass,jint action,jint pointerId,jfloat x,jfloat y,jfloat pressure) {
    std::lock_guard<std::mutex> lock(g_touchMutex);
    if (g_touchQueue.size() >= 256) g_touchQueue.pop();
    g_touchQueue.push({action,pointerId,x,y,pressure,(int64_t)std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::steady_clock::now().time_since_epoch()).count()});
}

extern "C" JNIEXPORT jint JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_pollTouch(JNIEnv* env,jclass,jintArray out) {
    if (!out || env->GetArrayLength(out) < 6) return 0;
    TouchEvent e;
    { std::lock_guard<std::mutex> lock(g_touchMutex); if (g_touchQueue.empty()) return 0; e=g_touchQueue.front(); g_touchQueue.pop(); }
    jint values[6]={(jint)e.action,(jint)e.pointerId,(jint)e.x,(jint)e.y,(jint)e.pressure,(jint)(e.timeNs/1000000)};
    env->SetIntArrayRegion(out,0,6,values);
    return 1;
}
