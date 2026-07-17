#include "jse_jit_SimpleJIT.h"
#include "jse_jit_JITLibHandle.h"

#include "jniutil.h"

typedef int (*plugin_method_t)(void *);

JNIEXPORT jlong JNICALL Java_jse_jit_SimpleJIT_loadLibrary0(JNIEnv *aEnv, jclass aClazz, jstring aLibPath) {
    return (jlong)(intptr_t)jloadLibraryS(aEnv, aLibPath);
}

JNIEXPORT void JNICALL Java_jse_jit_JITLibHandle_freeLibrary0(JNIEnv *aEnv, jclass aClazz, jlong aLibHandle) {
    jfreeLibrary(aEnv, (void *)(intptr_t)aLibHandle);
}

JNIEXPORT jlong JNICALL Java_jse_jit_SimpleJIT_findMethod0(JNIEnv *aEnv, jclass aClazz, jlong aLibHandle, jstring aMethodName) {
    return (jlong)(intptr_t)jfindMethodS(aEnv, (void *)(intptr_t)aLibHandle, aMethodName);
}

JNIEXPORT jint JNICALL Java_jse_jit_SimpleJIT_invokeMethod0(JNIEnv *aEnv, jclass aClazz, jlong aMethodPtr, jlong aPtr) {
    plugin_method_t tMethod = (plugin_method_t)(intptr_t)aMethodPtr;
    return (jint)tMethod((void *)(intptr_t)aPtr);
}

