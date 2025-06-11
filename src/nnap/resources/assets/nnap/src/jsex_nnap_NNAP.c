#include "jsex_nnap_NNAP.h"
#include "jniutil.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_jsex_nnap_NNAP_forceDot1(JNIEnv *aEnv, jclass aClazz,
        jdoubleArray aXGrad, jint aShift, jint aLength, jdoubleArray aFpPx, jdoubleArray aFpPy, jdoubleArray aFpPz,
        jdoubleArray rFx, jdoubleArray rFy, jdoubleArray rFz, jint aNN) {
    // java array init
    double *tXGrad = (double *)getJArrayBuf(aEnv, aXGrad);
    double *tFpPx = (double *)getJArrayBuf(aEnv, aFpPx);
    double *tFpPy = (double *)getJArrayBuf(aEnv, aFpPy);
    double *tFpPz = (double *)getJArrayBuf(aEnv, aFpPz);
    double *tFx = (double *)getJArrayBuf(aEnv, rFx);
    double *tFy = (double *)getJArrayBuf(aEnv, rFy);
    double *tFz = (double *)getJArrayBuf(aEnv, rFz);
    
    double *tXGrad_ = tXGrad + aShift;
    double *tFpPx_ = tFpPx;
    double *tFpPy_ = tFpPy;
    double *tFpPz_ = tFpPz;
    for (jint j = 0; j < aNN; ++j) {
        double rDotX = 0.0, rDotY = 0.0, rDotZ = 0.0;
        for (jint i = 0; i < aLength; ++i) {
            double subXGrad = tXGrad_[i];
            rDotX += subXGrad * tFpPx_[i];
            rDotY += subXGrad * tFpPy_[i];
            rDotZ += subXGrad * tFpPz_[i];
        }
        tFx[j] = rDotX; tFy[j] = rDotY; tFz[j] = rDotZ;
        tFpPx_ += aLength; tFpPy_ += aLength; tFpPz_ += aLength;
    }
    
    // release java array
    releaseJArrayBuf(aEnv, aXGrad, tXGrad, JNI_ABORT);
    releaseJArrayBuf(aEnv, aFpPx, tFpPx, JNI_ABORT);
    releaseJArrayBuf(aEnv, aFpPy, tFpPy, JNI_ABORT);
    releaseJArrayBuf(aEnv, aFpPz, tFpPz, JNI_ABORT);
    releaseJArrayBuf(aEnv, rFx, tFx, 0);
    releaseJArrayBuf(aEnv, rFy, tFy, 0);
    releaseJArrayBuf(aEnv, rFz, tFz, 0);
}

#ifdef __cplusplus
}
#endif
