#include "jse_clib_Dlfcn.h"

#ifdef __unix__
#include <dlfcn.h>
#endif

JNIEXPORT void JNICALL Java_jse_clib_Dlfcn_dlopen(JNIEnv *aEnv, jclass aClazz, jstring aPath) {
#ifdef _DLFCN_H
    const char *tPath = (*aEnv)->GetStringUTFChars(aEnv, aPath, NULL);
    void* dlresult = dlopen(tPath, RTLD_LAZY | RTLD_NOLOAD | RTLD_GLOBAL);
    (*aEnv)->ReleaseStringUTFChars(aEnv, aPath, tPath);
    if (dlresult) {
        dlclose(dlresult);
    } else {
        dlerror();
    }
#endif
}
