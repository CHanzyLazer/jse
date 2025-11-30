#ifndef BASIS_MULTI_LAYER_SPHERICAL_CHEBYSHEV_H
#define BASIS_MULTI_LAYER_SPHERICAL_CHEBYSHEV_H

#include "basis_SphericalUtil.hpp"
#include "basis_MultiLayerUtil.hpp"

namespace JSE_NNAP {

template <jboolean FULL_CACHE>
static void calCnlm(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                    jdouble *rCnlm, jdouble *rForwardCache,
                    jdouble aRCut, jint aNMax, jint aLMax,
                    jdouble *aEmbWeights, jdouble *aEmbBiases, jint *aEmbDims, jint aEmbNumber,
                    jdouble *aRFuncScale) noexcept {
    const jint tLMAll = (aLMax+1)*(aLMax+1);
    const jint tEmbOutputDim = aEmbDims[aEmbNumber-1];
    jint tEmbCacheSize = 0;
    for (jint l = 0; l < aEmbNumber-1; ++l) {
        tEmbCacheSize += aEmbDims[l];
    }
    // init cache
    jdouble *rRn = NULL, *rY = NULL;
    jdouble *rEmbRn = NULL, *rEmbCache = NULL, *rEmbCache2 = NULL;
    jdouble *rNlRn = NULL, *rNlFc = NULL, *rNlY = NULL;
    jdouble *rNlEmbCache = NULL, *rNlEmbCache2 = NULL;
    if (FULL_CACHE) {
        rNlRn = rForwardCache;
        rNlFc = rNlRn + aNN*(aNMax+1);
        rNlY = rNlFc + aNN;
        rEmbRn = rNlY + aNN*tLMAll;
        rNlEmbCache = rEmbRn + tEmbOutputDim;
        rNlEmbCache2 = rNlEmbCache + aNN*tEmbCacheSize;
    } else {
        rRn = rForwardCache;
        rY = rRn + (aNMax+1);
        rEmbRn = rY + tLMAll;
        rEmbCache = rEmbRn + tEmbOutputDim;
    }
    // loop for neighbor
    for (jint j = 0; j < aNN; ++j) {
        jint type = aNlType[j];
        jdouble dx = aNlDx[j], dy = aNlDy[j], dz = aNlDz[j];
        jdouble dis = sqrt((double)(dx*dx + dy*dy + dz*dz));
        // check rcut for merge
        if (dis >= aRCut) continue;
        // cal fc
        jdouble fc = calFc(dis, aRCut);
        if (FULL_CACHE) rNlFc[j] = fc;
        // cal Rn
        if (FULL_CACHE) rRn = rNlRn + j*(aNMax+1);
        calRn(rRn, aNMax, dis, aRCut, aRFuncScale);
        // cal Y
        if (FULL_CACHE) rY = rNlY + j*tLMAll;
        realSphericalHarmonicsFull4(aLMax, dx, dy, dz, dis, rY);
        // scale Y here
        multiply(rY, SQRT_PI4, tLMAll);
        // cal embedding
        if (FULL_CACHE) {
            rEmbCache = rNlEmbCache + j*tEmbCacheSize;
            rEmbCache2 = rNlEmbCache2 + j*tEmbCacheSize;
        }
        calEmbRn(rRn+1, aNMax, aEmbWeights, aEmbBiases, aEmbDims, aEmbNumber, rEmbCache, rEmbCache2, rEmbRn);
        // mplus2cnlm
        mplusCnlmEmb(rCnlm, rY, fc, rEmbRn, tEmbOutputDim, aLMax);
    }
}
static void calCnlm(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                    jdouble *rCnlm, jdouble *rForwardCache, jboolean aFullCache,
                    jdouble aRCut, jint aNMax, jint aLMax,
                    jdouble *aEmbWeights, jdouble *aEmbBiases, jint *aEmbDims, jint aEmbNumber,
                    jdouble *aRFuncScale) noexcept {
    if (aFullCache) {
        calCnlm<JNI_TRUE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rCnlm, rForwardCache, aRCut, aNMax, aLMax, aEmbWeights, aEmbBiases, aEmbDims, aEmbNumber, aRFuncScale);
    } else {
        calCnlm<JNI_FALSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rCnlm, rForwardCache, aRCut, aNMax, aLMax, aEmbWeights, aEmbBiases, aEmbDims, aEmbNumber, aRFuncScale);
    }
}
static void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                  jdouble *rFp, jdouble *rForwardCache, jboolean aFullCache,
                  jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                  jdouble *aEmbWeights, jdouble *aEmbBiases, jint *aEmbDims, jint aEmbNumber,
                  jdouble *aEquFuseWeight, jint aEquFuseSize, jdouble aEquFuseScale,
                  jdouble *aRFuncScale, jdouble *aSystemScale) noexcept {
    // const init
    const jint tSizeL = aLMax+1+ L3NCOLS[aL3Max] + L4NCOLS[aL4Max];
    const jint tLMAll = (aLMax+1)*(aLMax+1);
    const jint tEmbOutputDim = aEmbDims[aEmbNumber-1];
    const jint tSizeCnlm = tEmbOutputDim*tLMAll;
    const jint tSizeAnlm = aEquFuseSize*tLMAll;
    // init cache
    jdouble *rCnlm = rForwardCache;
    jdouble *rAnlm = rCnlm + tSizeCnlm;
    jdouble *rForwardCacheElse = rAnlm + tSizeAnlm;
    // clear cnlm, anlm first
    fill(rCnlm, 0.0, tSizeCnlm);
    fill(rAnlm, 0.0, tSizeAnlm);
    // do cal
    calCnlm(aNlDx, aNlDy, aNlDz, aNlType, aNN, rCnlm, rForwardCacheElse, aFullCache, aRCut, aNMax, aLMax, aEmbWeights, aEmbBiases, aEmbDims, aEmbNumber, aRFuncScale);
    // system scale here
    for (jint n=0, tShift=0, tShiftS=0; n<tEmbOutputDim; ++n, tShift+=tLMAll, tShiftS+=aLMax+1) {
        multiplyLM(rCnlm+tShift, aSystemScale+tShiftS, aLMax);
    }
    // cnlm -> anlm
    mplusAnlm<FUSE_STYLE_LIMITED>(rAnlm, rCnlm, aEquFuseWeight, aEquFuseSize, tEmbOutputDim, aLMax);
    // scale anlm here
    multiply(rAnlm, aEquFuseScale, tSizeAnlm);
    // cal L2 L3 L4
    const jint tSizeL2 = aLMax+1;
    const jint tSizeL3 = L3NCOLS[aL3Max];
    for (jint np=0, tShift=0, tShiftFp=0; np<aEquFuseSize; ++np, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calL2_(rAnlm+tShift, rFp+tShiftFp, aLMax, JNI_FALSE, JNI_TRUE);
        calL3_(rAnlm+tShift, rFp+tShiftFp+tSizeL2, aL3Max);
        calL4_(rAnlm+tShift, rFp+tShiftFp+tSizeL2+tSizeL3, aL4Max);
    }
}

static void calBackwardMainLoop(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                                jdouble *rGradPara, jdouble *aGradCnlm, jdouble *aForwardCache, jdouble *rBackwardCache,
                                jdouble aRCut, jint aNMax, jint aLMax,
                                jdouble *aEmbWeights, jint *aEmbDims, jint aEmbNumber) {
    const jint tLMAll = (aLMax+1)*(aLMax+1);
    const jint tEmbOutputDim = aEmbDims[aEmbNumber-1];
    jint tEmbCacheSize = 0;
    for (jint l = 0; l < aEmbNumber-1; ++l) {
        tEmbCacheSize += aEmbDims[l];
    }
    jdouble *tNlRn = aForwardCache;
    jdouble *tNlFc = tNlRn + aNN*(aNMax+1);
    jdouble *tNlY = tNlFc + aNN;
    jdouble *tNlEmbCache = tNlY + aNN*tLMAll + tEmbOutputDim;
    jdouble *tNlEmbCache2 = tNlEmbCache + aNN*tEmbCacheSize;
    jdouble *rNlGradEmbRn = rBackwardCache;
    jdouble *rNlGradEmbCache = rNlGradEmbRn + aNN*tEmbOutputDim;
    for (jint j = 0; j < aNN; ++j) {
        jint type = aNlType[j];
        jdouble dx = aNlDx[j], dy = aNlDy[j], dz = aNlDz[j];
        jdouble dis = sqrt((double)(dx*dx + dy*dy + dz*dz));
        // check rcut for merge
        if (dis >= aRCut) continue;
        // get fc
        jdouble fc = tNlFc[j];
        // get Rn
        jdouble *tRn = tNlRn + j*(aNMax+1);
        // get Y
        jdouble *tY = tNlY + j*tLMAll;
        // cnlm -> embedding Rn
        jdouble *rGradEmbRn = rNlGradEmbRn + j*tEmbOutputDim;
        mplusGradCnlmEmb(aGradCnlm, tY, fc, rGradEmbRn, tEmbOutputDim, aLMax);
        // backward embedding
        jdouble *tEmbCache = tNlEmbCache + j*tEmbCacheSize;
        jdouble *tEmbCache2 = tNlEmbCache2 + j*tEmbCacheSize;
        jdouble *rGradEmbCache = rNlGradEmbCache + j*tEmbCacheSize;
        backwardEmbRn(tRn+1, aNMax, aEmbWeights, aEmbDims, aEmbNumber, tEmbCache, tEmbCache2, rGradEmbRn, rGradEmbCache, rGradPara);
    }
}
static void calBackward(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                        jdouble *aGradFp, jdouble *rGradPara, jdouble *aForwardCache, jdouble *rBackwardCache,
                        jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                        jdouble *aEmbWeights, jint *aEmbDims, jint aEmbNumber,
                        jdouble *aEquFuseWeight, jint aEquFuseSize, jdouble aEquFuseScale,
                        jdouble *aSystemScale) noexcept {
    // const init
    const jint tSizeL = aLMax+1 + L3NCOLS[aL3Max] + L4NCOLS[aL4Max];
    const jint tLMAll = (aLMax+1)*(aLMax+1);
    const jint tEmbOutputDim = aEmbDims[aEmbNumber-1];
    jint tEmbWeightsSize = 0;
    jint tEmbBiasesSize = 0;
    jint tColNum = aNMax; // TODO
    for (jint l = 0; l < aEmbNumber; ++l) {
        const jint tEmbDim = aEmbDims[l];
        tEmbWeightsSize += tColNum * tEmbDim;
        tEmbBiasesSize += tEmbDim;
        tColNum = tEmbDim;
    }
    const jint tSizeCnlm = tEmbOutputDim*tLMAll;
    const jint tSizeAnlm = aEquFuseSize*tLMAll;
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
    for (jint np=0, tShift=0, tShiftFp=0; np<aEquFuseSize; ++np, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calGradL2_(tAnlm+tShift, rGradAnlm+tShift, aGradFp+tShiftFp, aLMax, JNI_FALSE, JNI_TRUE);
        calGradL3_(tAnlm+tShift, rGradAnlm+tShift, aGradFp+tShiftFp+tSizeL2, aL3Max);
        calGradL4_(tAnlm+tShift, rGradAnlm+tShift, aGradFp+tShiftFp+tSizeL2+tSizeL3, aL4Max);
    }
    // anlm stuffs
    jdouble *tGradPara = rGradPara + tEmbWeightsSize+tEmbBiasesSize;
    // scale anlm here
    multiply(rGradAnlm, aEquFuseScale, tSizeAnlm);
    mplusGradParaPostFuse<FUSE_STYLE_LIMITED>(rGradAnlm, tCnlm, tGradPara, aEquFuseSize, tEmbOutputDim, aLMax);
    // anlm -> cnlm
    mplusGradAnlm<FUSE_STYLE_LIMITED>(rGradAnlm, rGradCnlm, aEquFuseWeight, aEquFuseSize, tEmbOutputDim, aLMax);
    // system scale here
    for (jint n=0, tShift=0, tShiftS=0; n<tEmbOutputDim; ++n, tShift+=tLMAll, tShiftS+=aLMax+1) {
        multiplyLM(rGradCnlm+tShift, aSystemScale+tShiftS, aLMax);
    }
    // plus to para
    calBackwardMainLoop(aNlDx, aNlDy, aNlDz, aNlType, aNN, rGradPara, rGradCnlm, tForwardCacheElse, rBackwardCacheElse, aRCut, aNMax, aLMax, aEmbWeights, aEmbDims, aEmbNumber);
}

}

#endif //BASIS_MULTI_LAYER_SPHERICAL_CHEBYSHEV_H