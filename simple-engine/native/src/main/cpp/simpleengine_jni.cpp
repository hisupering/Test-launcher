#include <jni.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <mutex>
#include <queue>
#include <cstdint>
#include <chrono>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "SimpleEngineJNI", __VA_ARGS__)

extern "C" void simple_engine_gl_start(ANativeWindow*);
extern "C" void simple_engine_gl_frame();
extern "C" void simple_engine_gl_stop();

struct TouchEvent {
    int action;
    int actionIndex;
    int pointerId;
    float x;
    float y;
    float pressure;
    int64_t timeMs;
};

static std::mutex g_windowMutex;
static ANativeWindow* g_window = nullptr;
static ANativeWindow* g_renderWindow = nullptr;
static std::mutex g_touchMutex;
static std::queue<TouchEvent> g_touchQueue;

extern "C" JNIEXPORT void JNICALL
Java_com_simpleengine_android_SimpleEngineNativeBridge_attachSurface(
        JNIEnv* env, jclass, jobject surface) {
    ANativeWindow* next = surface ? ANativeWindow_fromSurface(env, surface) : nullptr;
    std::lock_guard<std::mutex> lock(g_windowMutex);
    if (g_window) ANativeWindow_release(g_window);
    g_window = next;
}

extern "C" JNIEXPORT void JNICALL
Java_com_simpleengine_android_SimpleEngineNativeBridge_detachSurface(JNIEnv*, jclass) {
    // Called from the render thread so EGL teardown happens on the context-owning thread.
    simple_engine_gl_stop();
    if (g_renderWindow) {
        ANativeWindow_release(g_renderWindow);
        g_renderWindow = nullptr;
    }

    std::lock_guard<std::mutex> lock(g_windowMutex);
    if (g_window) {
        ANativeWindow_release(g_window);
        g_window = nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_simpleengine_android_SimpleEngineNativeBridge_renderFrame(JNIEnv*, jclass) {
    ANativeWindow* requested = nullptr;
    {
        std::lock_guard<std::mutex> lock(g_windowMutex);
        if (g_window) {
            requested = g_window;
            ANativeWindow_acquire(requested);
        }
    }

    if (requested != g_renderWindow) {
        simple_engine_gl_stop();
        if (g_renderWindow) ANativeWindow_release(g_renderWindow);
        g_renderWindow = requested;
        if (g_renderWindow) {
            simple_engine_gl_start(g_renderWindow);
        }
    } else if (requested) {
        ANativeWindow_release(requested);
    }

    simple_engine_gl_frame();
}

extern "C" JNIEXPORT void JNICALL
Java_com_simpleengine_android_SimpleEngineNativeBridge_sendTouch(
        JNIEnv*, jclass, jint action, jint actionIndex, jint pointerId,
        jfloat x, jfloat y, jfloat pressure) {
    std::lock_guard<std::mutex> lock(g_touchMutex);
    if (g_touchQueue.size() >= 512) g_touchQueue.pop();
    const auto now = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::steady_clock::now().time_since_epoch()).count();
    g_touchQueue.push({action, actionIndex, pointerId, x, y, pressure, now});
}

extern "C" JNIEXPORT jint JNICALL
Java_com_simpleengine_android_SimpleEngineNativeBridge_pollTouch(
        JNIEnv* env, jclass, jintArray meta, jfloatArray values) {
    // meta: [action, actionIndex, pointerId, timestampMs]
    // values: [xPx, yPx, pressure]
    if (!meta || env->GetArrayLength(meta) < 4 ||
        !values || env->GetArrayLength(values) < 3) return 0;

    TouchEvent event;
    {
        std::lock_guard<std::mutex> lock(g_touchMutex);
        if (g_touchQueue.empty()) return 0;
        event = g_touchQueue.front();
        g_touchQueue.pop();
    }

    jint m[4] = {
        static_cast<jint>(event.action),
        static_cast<jint>(event.actionIndex),
        static_cast<jint>(event.pointerId),
        static_cast<jint>(event.timeMs & 0x7fffffff)
    };
    jfloat v[3] = {event.x, event.y, event.pressure};
    env->SetIntArrayRegion(meta, 0, 4, m);
    env->SetFloatArrayRegion(values, 0, 3, v);
    return 1;
}
