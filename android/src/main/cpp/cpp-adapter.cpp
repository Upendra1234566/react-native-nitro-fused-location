#include <jni.h>
#include "NitroFusedLocationOnLoad.hpp"

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
    return margelo::nitro::nitrofusedlocation::initialize(vm);
}