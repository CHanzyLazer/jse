#ifndef BASIS_MULTI_LAYER_SPHERICAL_CHEBYSHEV_H
#define BASIS_MULTI_LAYER_SPHERICAL_CHEBYSHEV_H

#include "basis_SphericalUtil.hpp"
#include "basis_MultiUtil.hpp"

namespace JSE_NNAP {

template <jboolean FULL_CACHE>
static void calCnlm(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                    jdouble *rCnlm, jdouble *rForwardCache,
                    jdouble aRCutMax, jdouble *aRCutsL, jdouble *aRCutsR, jint aRCutsSize,
                    jint aNMax, jint aLMax, jdouble *aRFuncScale) noexcept {
    const jint tLMAll = (aLMax+1)*(aLMax+1);
    const jint tSizeBnlm = (aNMax+1)*(aLMax+1)*(aLMax+1);
    // init cache
    jdouble *rRn = NULL, *rFc = NULL, *rY = NULL;
    jdouble *rNlRn = NULL, *rNlFc = NULL, *rNlY = NULL;
    if (FULL_CACHE) {
        rNlRn = rForwardCache;
        rNlFc = rNlRn + aNN*(aNMax+1);
        rNlY = rNlFc + aNN*aRCutsSize;
    } else {
        rRn = rForwardCache;
        rY = rRn + (aNMax+1);
    }
    // loop for neighbor
    for (jint j = 0; j < aNN; ++j) {
        jint type = aNlType[j];
        jdouble dx = aNlDx[j], dy = aNlDy[j], dz = aNlDz[j];
        jdouble dis = sqrt((double)(dx*dx + dy*dy + dz*dz));
        // check rcut for merge
        if (dis >= aRCutMax) continue;
        // cal Rn
        if (FULL_CACHE) rRn = rNlRn + j*(aNMax+1);
        calRn(rRn, aNMax, dis, aRCutMax);
        // cal Y
        if (FULL_CACHE) rY = rNlY + j*tLMAll;
        realSphericalHarmonicsFull4(aLMax, dx, dy, dz, dis, rY);
        // cal fc
        if (FULL_CACHE) rFc = rNlFc + j*aRCutsSize;
        jdouble *tCnlm = rCnlm;
        jdouble *tRFuncScale = aRFuncScale;
        for (jint k = 0; k < aRCutsSize; ++k, tCnlm+=tSizeBnlm, tRFuncScale+=(aNMax+1)) {
            const jdouble tRCutL = aRCutsL[k];
            const jdouble tRCutR = aRCutsR[k];
            if (dis<=tRCutL || dis>=tRCutR) continue;
            jdouble fc = calFc(dis, tRCutL, tRCutR);
            if (FULL_CACHE) rFc[k] = fc;
            mplusCnlmMulti(tCnlm, rY, fc, rRn, tRFuncScale, aNMax, aLMax);
        }
    }
}
static void calCnlm(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                    jdouble *rCnlm, jdouble *rForwardCache, jboolean aFullCache,
                    jdouble aRCutMax, jdouble *aRCutsL, jdouble *aRCutsR, jint aRCutsSize,
                    jint aNMax, jint aLMax, jdouble *aRFuncScale) noexcept {
    if (aFullCache) {
        calCnlm<JNI_TRUE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rCnlm, rForwardCache, aRCutMax, aRCutsL, aRCutsR, aRCutsSize, aNMax, aLMax, aRFuncScale);
    } else {
        calCnlm<JNI_FALSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rCnlm, rForwardCache, aRCutMax, aRCutsL, aRCutsR, aRCutsSize, aNMax, aLMax, aRFuncScale);
    }
}
static void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                  jdouble *rFp, jdouble *rForwardCache, jboolean aFullCache,
                  jint aTypeNum, jdouble aRCutMax, jdouble *aRCutsL, jdouble *aRCutsR, jint aRCutsSize,
                  jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                  jdouble *aEquFuseWeight, jint aEquFuseOutDim, jdouble aEquFuseScale,
                  jdouble *aRFuncScale) noexcept {
    // const init
    const jint tSizeL = aLMax+1+ L3NCOLS[aL3Max] + L4NCOLS[aL4Max];
    const jint tLMAll = (aLMax+1)*(aLMax+1);
    const jint tEquFuseInDim = aRCutsSize*(aNMax+1);
    const jint tSizeCnlm = tEquFuseInDim*tLMAll;
    const jint tSizeAnlm = aEquFuseOutDim*tLMAll;
    // init cache
    jdouble *rCnlm = rForwardCache;
    jdouble *rAnlm = rCnlm + tSizeCnlm;
    jdouble *rForwardCacheElse = rAnlm + tSizeAnlm;
    // clear cnlm, anlm first
    fill(rCnlm, 0.0, tSizeCnlm);
    fill(rAnlm, 0.0, tSizeAnlm);
    // do cal
    calCnlm(aNlDx, aNlDy, aNlDz, aNlType, aNN, rCnlm, rForwardCacheElse, aFullCache, aRCutMax, aRCutsL, aRCutsR, aRCutsSize, aNMax, aLMax, aRFuncScale);
    // cnlm -> anlm
    mplusAnlm(rAnlm, rCnlm, aEquFuseWeight, aEquFuseOutDim, tEquFuseInDim, aLMax);
    // scale anlm here
    multiply(rAnlm, aEquFuseScale, tSizeAnlm);
    // cal L2 L3 L4
    const jint tSizeL2 = aLMax+1;
    const jint tSizeL3 = L3NCOLS[aL3Max];
    for (jint np=0, tShift=0, tShiftFp=0; np<aEquFuseOutDim; ++np, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calL2_(rAnlm+tShift, rFp+tShiftFp, aLMax, JNI_FALSE);
        calL3_(rAnlm+tShift, rFp+tShiftFp+tSizeL2, aL3Max);
        calL4_(rAnlm+tShift, rFp+tShiftFp+tSizeL2+tSizeL3, aL4Max);
    }
}

static void calBackwardMainLoop(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                                jdouble *rGradPara, jdouble *aGradCnlm, jdouble *aForwardCache, jdouble *rBackwardCache,
                                jdouble aRCutMax, jdouble *aRCutsL, jdouble *aRCutsR, jint aRCutsSize,
                                jint aNMax, jint aLMax) {
    const jint tLMAll = (aLMax+1)*(aLMax+1);
    const jint tSizeBnlm = (aNMax+1)*(aLMax+1)*(aLMax+1);
    // init cache
    jdouble *tNlRn = aForwardCache;
    jdouble *tNlFc = tNlRn + aNN*(aNMax+1);
    jdouble *tNlY = tNlFc + aNN*aRCutsSize;
    for (jint j = 0; j < aNN; ++j) {
        jint type = aNlType[j];
        jdouble dx = aNlDx[j], dy = aNlDy[j], dz = aNlDz[j];
        jdouble dis = sqrt((double)(dx*dx + dy*dy + dz*dz));
        // check rcut for merge
        if (dis >= aRCutMax) continue;
        // get Rn
        jdouble *tRn = tNlRn + j*(aNMax+1);
        // get Y
        jdouble *tY = tNlY + j*tLMAll;
        // get fc
        jdouble *tFc = tNlFc + j*aRCutsSize;
        jdouble *tGradCnlm = aGradCnlm;
        for (jint k = 0; k < aRCutsSize; ++k, tGradCnlm+=tSizeBnlm) {
            const jdouble tRCutL = aRCutsL[k];
            const jdouble tRCutR = aRCutsR[k];
            if (dis<=tRCutL || dis>=tRCutR) continue;
            jdouble fc = tFc[k];
            // mplusGradParaFuse(aGradCnlm, tBnlm, rGradPara, type, aFuseSize, aNMax, aLMaxMax);
        }
    }
}
static void calBackward(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                        jdouble *aGradFp, jdouble *rGradPara, jdouble *aForwardCache, jdouble *rBackwardCache,
                        jint aTypeNum, jdouble aRCutMax, jdouble *aRCutsL, jdouble *aRCutsR, jint aRCutsSize,
                        jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                        jdouble *aEquFuseWeight, jint aEquFuseOutDim, jdouble aEquFuseScale) noexcept {
    // const init
    const jint tSizeL = aLMax+1 + L3NCOLS[aL3Max] + L4NCOLS[aL4Max];
    const jint tLMAll = (aLMax+1)*(aLMax+1);
    const jint tEquFuseInDim = aRCutsSize*(aNMax+1);
    const jint tSizeCnlm = tEquFuseInDim*tLMAll;
    const jint tSizeAnlm = aEquFuseOutDim*tLMAll;
    // init cache
    jdouble *tCnlm = aForwardCache;
    jdouble *tAnlm = tCnlm + tSizeCnlm;
    jdouble *tForwardCacheElse = tAnlm + tSizeAnlm;
    jdouble *rGradCnlm = rBackwardCache;
    jdouble *rGradAnlm = rGradCnlm + tSizeCnlm;
    jdouble *rBackwardCacheElse = rGradAnlm + tSizeAnlm;
    // cal grad cnlm
    const jint tSizeL2 = aLMax+1;
    const jint tSizeL3 = L3NCOLS[aL3Max];
    for (jint np=0, tShift=0, tShiftFp=0; np<aEquFuseOutDim; ++np, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calGradL2_(tAnlm+tShift, rGradAnlm+tShift, aGradFp+tShiftFp, aLMax, JNI_FALSE);
        calGradL3_(tAnlm+tShift, rGradAnlm+tShift, aGradFp+tShiftFp+tSizeL2, aL3Max);
        calGradL4_(tAnlm+tShift, rGradAnlm+tShift, aGradFp+tShiftFp+tSizeL2+tSizeL3, aL4Max);
    }
    // anlm stuffs
    jdouble *tGradPara = rGradPara;
    // scale anlm here
    multiply(rGradAnlm, aEquFuseScale, tSizeAnlm);
    mplusGradParaPostFuse(rGradAnlm, tCnlm, tGradPara, aEquFuseOutDim, tEquFuseInDim, aLMax);
    // anlm -> cnlm
    mplusGradAnlm(rGradAnlm, rGradCnlm, aEquFuseWeight, aEquFuseOutDim, tEquFuseInDim, aLMax);
    // plus to para
    calBackwardMainLoop(aNlDx, aNlDy, aNlDz, aNlType, aNN, rGradPara, rGradCnlm, tForwardCacheElse, rBackwardCacheElse, aRCutMax, aRCutsL, aRCutsR, aRCutsSize, aNMax, aLMax);
}

}

#endif //BASIS_MULTI_LAYER_SPHERICAL_CHEBYSHEV_H