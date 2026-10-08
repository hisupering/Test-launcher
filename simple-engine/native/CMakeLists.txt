cmake_minimum_required(VERSION 3.22.1)
project(simpleengine)

add_library(simpleengine SHARED
    src/main/cpp/simpleengine_jni.cpp
    src/main/cpp/simpleengine_gl.cpp
    src/main/cpp/simpleengine_audio.cpp
)

find_library(log_lib log)
find_library(android_lib android)
find_library(egl_lib EGL)
find_library(gles_lib GLESv2)
find_library(openSLES_lib OpenSLES)

target_link_libraries(simpleengine
    ${log_lib}
    ${android_lib}
    ${egl_lib}
    ${gles_lib}
    ${openSLES_lib}
)
