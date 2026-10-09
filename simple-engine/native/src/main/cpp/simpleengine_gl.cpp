#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <android/native_window.h>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "SimpleEngineGL", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "SimpleEngineGL", __VA_ARGS__)

static EGLDisplay g_display = EGL_NO_DISPLAY;
static EGLSurface g_surface = EGL_NO_SURFACE;
static EGLContext g_context = EGL_NO_CONTEXT;

extern "C" void simple_engine_gl_start(ANativeWindow* window) {
    if (!window) return;

    g_display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (g_display == EGL_NO_DISPLAY || !eglInitialize(g_display, nullptr, nullptr)) {
        LOGE("eglInitialize failed: 0x%x", eglGetError());
        g_display = EGL_NO_DISPLAY;
        return;
    }

    const EGLint configAttributes[] = {
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
        EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
        EGL_RED_SIZE, 8, EGL_GREEN_SIZE, 8,
        EGL_BLUE_SIZE, 8, EGL_ALPHA_SIZE, 8,
        EGL_NONE
    };
    EGLConfig config = nullptr;
    EGLint configCount = 0;
    if (!eglChooseConfig(g_display, configAttributes, &config, 1, &configCount) ||
        configCount < 1) {
        LOGE("eglChooseConfig failed: 0x%x", eglGetError());
        eglTerminate(g_display);
        g_display = EGL_NO_DISPLAY;
        return;
    }

    const EGLint contextAttributes[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
    g_context = eglCreateContext(g_display, config, EGL_NO_CONTEXT, contextAttributes);
    if (g_context == EGL_NO_CONTEXT) {
        LOGE("eglCreateContext failed: 0x%x", eglGetError());
        eglTerminate(g_display);
        g_display = EGL_NO_DISPLAY;
        return;
    }

    g_surface = eglCreateWindowSurface(g_display, config, window, nullptr);
    if (g_surface == EGL_NO_SURFACE) {
        LOGE("eglCreateWindowSurface failed: 0x%x", eglGetError());
        eglDestroyContext(g_display, g_context);
        g_context = EGL_NO_CONTEXT;
        eglTerminate(g_display);
        g_display = EGL_NO_DISPLAY;
        return;
    }

    if (!eglMakeCurrent(g_display, g_surface, g_surface, g_context)) {
        LOGE("eglMakeCurrent failed: 0x%x", eglGetError());
        eglDestroySurface(g_display, g_surface);
        eglDestroyContext(g_display, g_context);
        g_surface = EGL_NO_SURFACE;
        g_context = EGL_NO_CONTEXT;
        eglTerminate(g_display);
        g_display = EGL_NO_DISPLAY;
        return;
    }

    glViewport(0, 0, ANativeWindow_getWidth(window), ANativeWindow_getHeight(window));
    glClearColor(0.02f, 0.05f, 0.02f, 1.0f);
    LOGI("EGL ES2 surface initialized on render thread");
}

extern "C" void simple_engine_gl_frame() {
    if (g_display == EGL_NO_DISPLAY || g_surface == EGL_NO_SURFACE ||
        g_context == EGL_NO_CONTEXT) return;

    EGLint width = 0, height = 0;
    eglQuerySurface(g_display, g_surface, EGL_WIDTH, &width);
    eglQuerySurface(g_display, g_surface, EGL_HEIGHT, &height);
    if (width > 0 && height > 0) glViewport(0, 0, width, height);

    glClear(GL_COLOR_BUFFER_BIT);
    if (!eglSwapBuffers(g_display, g_surface)) {
        LOGE("eglSwapBuffers failed: 0x%x", eglGetError());
    }
}

extern "C" void simple_engine_gl_stop() {
    if (g_display != EGL_NO_DISPLAY) {
        eglMakeCurrent(g_display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        if (g_surface != EGL_NO_SURFACE) eglDestroySurface(g_display, g_surface);
        if (g_context != EGL_NO_CONTEXT) eglDestroyContext(g_display, g_context);
        eglTerminate(g_display);
    }
    g_display = EGL_NO_DISPLAY;
    g_surface = EGL_NO_SURFACE;
    g_context = EGL_NO_CONTEXT;
}
