#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <android/native_window.h>
#include <android/log.h>
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"SimpleEngineGL",__VA_ARGS__)
static EGLDisplay d=EGL_NO_DISPLAY; static EGLSurface s=EGL_NO_SURFACE; static EGLContext c=EGL_NO_CONTEXT;
extern "C" void simple_engine_gl_start(ANativeWindow* w){
 d=eglGetDisplay(EGL_DEFAULT_DISPLAY); eglInitialize(d,nullptr,nullptr);
 const EGLint cfg[]={EGL_RENDERABLE_TYPE,EGL_OPENGL_ES2_BIT,EGL_SURFACE_TYPE,EGL_WINDOW_BIT,EGL_RED_SIZE,8,EGL_GREEN_SIZE,8,EGL_BLUE_SIZE,8,EGL_ALPHA_SIZE,8,EGL_NONE};
 EGLConfig config; EGLint n=0; eglChooseConfig(d,cfg,&config,1,&n);
 const EGLint ctx[]={EGL_CONTEXT_CLIENT_VERSION,2,EGL_NONE}; c=eglCreateContext(d,config,EGL_NO_CONTEXT,ctx); s=eglCreateWindowSurface(d,config,w,nullptr); eglMakeCurrent(d,s,s,c); glClearColor(0.02f,0.05f,0.02f,1.f); LOGI("EGL renderer started");
}
extern "C" void simple_engine_gl_frame(){if(d!=EGL_NO_DISPLAY&&s!=EGL_NO_SURFACE){glClear(GL_COLOR_BUFFER_BIT);eglSwapBuffers(d,s);}}
extern "C" void simple_engine_gl_stop(){if(d!=EGL_NO_DISPLAY){eglMakeCurrent(d,EGL_NO_SURFACE,EGL_NO_SURFACE,EGL_NO_CONTEXT);if(s!=EGL_NO_SURFACE)eglDestroySurface(d,s);if(c!=EGL_NO_CONTEXT)eglDestroyContext(d,c);eglTerminate(d);}d=EGL_NO_DISPLAY;s=EGL_NO_SURFACE;c=EGL_NO_CONTEXT;}
