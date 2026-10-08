#include <jni.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
static SLObjectItf engineObj=nullptr; static SLEngineItf engineItf=nullptr;
extern "C" JNIEXPORT jboolean JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_initAudio(JNIEnv*,jclass){
 if(engineObj)return JNI_TRUE;
 if(slCreateEngine(&engineObj,0,nullptr,0,nullptr,nullptr)!=SL_RESULT_SUCCESS)return JNI_FALSE;
 if((*engineObj)->Realize(engineObj,SL_BOOLEAN_FALSE)!=SL_RESULT_SUCCESS)return JNI_FALSE;
 if((*engineObj)->GetInterface(engineObj,SL_IID_ENGINE,&engineItf)!=SL_RESULT_SUCCESS)return JNI_FALSE;
 return JNI_TRUE;
}
extern "C" JNIEXPORT void JNICALL Java_com_simplelauncher_engine_android_SimpleEngineNativeBridge_shutdownAudio(JNIEnv*,jclass){if(engineObj){(*engineObj)->Destroy(engineObj);engineObj=nullptr;engineItf=nullptr;}}
