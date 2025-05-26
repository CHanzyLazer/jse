#include "jsex_nnap_basis_Mirror.h"
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_jsex_nnap_basis_Mirror_buildNlType1(JNIEnv *aEnv, jclass aClazz, jlong aNlType, jlong rMirrorNlType, jint aNN, jint aMirrorType, jint aThisType) {
    int *tNlType = (int *)(intptr_t)aNlType;
    int *tMirrorNlType = (int *)(intptr_t)rMirrorNlType;
    for (jint j = 0; j < aNN; ++j) {
        jint tType = tNlType[j];
        if (tType == aThisType) tMirrorNlType[j] = (int)aMirrorType;
        else if (tType == aMirrorType) tMirrorNlType[j] = (int)aThisType;
        else tMirrorNlType[j] = (int)tType;
    }
}

#ifdef __cplusplus
}
#endif
