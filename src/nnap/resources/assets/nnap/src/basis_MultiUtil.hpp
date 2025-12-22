#ifndef NNAP_BASIS_MULTILAYERUTIL_H
#define NNAP_BASIS_MULTILAYERUTIL_H

#include "nnap_util.hpp"

namespace JSE_NNAP {

template <jint NMAX, jint LMAX>
static void mplusCnlmMl(jdouble *rCnlm, jdouble *aY, jdouble aFc, jdouble *aRn) noexcept {
    constexpr jint tLMAll = (LMAX+1)*(LMAX+1);
    jdouble *tCnlm = rCnlm;
    for (jint n = 0; n <= NMAX; ++n) {
        const jdouble tMul = aFc*aRn[n];
        for (jint k = 0; k < tLMAll; ++k) {
            const jdouble tValue = tMul*aY[k];
            tCnlm[k] += tValue;
        }
        tCnlm += tLMAll;
    }
}
template <jint LMAX>
static void mplusCnlmMl(jdouble *rCnlm, jdouble *aY, jdouble aFc, jdouble *aRn, jint aNMax) noexcept {
    switch (aNMax) {
    case 0: {mplusCnlmMl<0, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 1: {mplusCnlmMl<1, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 2: {mplusCnlmMl<2, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 3: {mplusCnlmMl<3, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 4: {mplusCnlmMl<4, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 5: {mplusCnlmMl<5, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 6: {mplusCnlmMl<6, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 7: {mplusCnlmMl<7, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 8: {mplusCnlmMl<8, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 9: {mplusCnlmMl<9, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 10: {mplusCnlmMl<10, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 11: {mplusCnlmMl<11, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 12: {mplusCnlmMl<12, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 13: {mplusCnlmMl<13, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 14: {mplusCnlmMl<14, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 15: {mplusCnlmMl<15, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 16: {mplusCnlmMl<16, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 17: {mplusCnlmMl<17, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 18: {mplusCnlmMl<18, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 19: {mplusCnlmMl<19, LMAX>(rCnlm, aY, aFc, aRn); return;}
    case 20: {mplusCnlmMl<20, LMAX>(rCnlm, aY, aFc, aRn); return;}
    default: {return;}
    }
}
static void mplusCnlmMl(jdouble *rCnlm, jdouble *aY, jdouble aFc, jdouble *aRn, jint aNMax, jint aLMax) noexcept {
    switch (aLMax) {
    case 0: {mplusCnlmMl<0>(rCnlm, aY, aFc, aRn, aNMax); return;}
    case 1: {mplusCnlmMl<1>(rCnlm, aY, aFc, aRn, aNMax); return;}
    case 2: {mplusCnlmMl<2>(rCnlm, aY, aFc, aRn, aNMax); return;}
    case 3: {mplusCnlmMl<3>(rCnlm, aY, aFc, aRn, aNMax); return;}
    case 4: {mplusCnlmMl<4>(rCnlm, aY, aFc, aRn, aNMax); return;}
    case 5: {mplusCnlmMl<5>(rCnlm, aY, aFc, aRn, aNMax); return;}
    case 6: {mplusCnlmMl<6>(rCnlm, aY, aFc, aRn, aNMax); return;}
    case 7: {mplusCnlmMl<7>(rCnlm, aY, aFc, aRn, aNMax); return;}
    case 8: {mplusCnlmMl<8>(rCnlm, aY, aFc, aRn, aNMax); return;}
    case 9: {mplusCnlmMl<9>(rCnlm, aY, aFc, aRn, aNMax); return;}
    case 10: {mplusCnlmMl<10>(rCnlm, aY, aFc, aRn, aNMax); return;}
    case 11: {mplusCnlmMl<11>(rCnlm, aY, aFc, aRn, aNMax); return;}
    case 12: {mplusCnlmMl<12>(rCnlm, aY, aFc, aRn, aNMax); return;}
    default: {return;}
    }
}


static inline jdouble silu(jdouble aX) noexcept {
    return aX / (1.0 + exp(-aX));
}
static inline jdouble siluGrad(jdouble aX, jdouble *rGrad) noexcept {
    jdouble tSigmoid = 1.0 / (1.0 + exp(-aX));
    *rGrad = tSigmoid * (1 + aX * (1 - tSigmoid));
    return aX * tSigmoid;
}

template <jint NMAX>
static void calRFuse(jdouble *rRFn, jdouble *aRn, jdouble *aRFuseWeight, jint aRFuseSize) {
    jdouble *tRFuseWeight = aRFuseWeight;
    for (jint np = 0; np < aRFuseSize; ++np) {
        rRFn[np] = dot<NMAX+1>(aRn, tRFuseWeight);
        tRFuseWeight += NMAX+1;
    }
}
static void calEmbRn(jdouble *aInputX, jint aInputDim, jdouble *aEmbWeights, jdouble *aEmbBiases, jint *aEmbDims, jint aEmbNumber, jdouble *rEmbCache, jdouble *rEmbCache2, jdouble *rEmbRn) {
    jdouble *tInput = aInputX;
    jdouble *rOutput = rEmbCache;
    jdouble *rGrad = rEmbCache2;
    jdouble *tWeights = aEmbWeights, *tBiases = aEmbBiases;
    jint tInSize = aInputDim;
    const jint tEnd = aEmbNumber - 1;
    for (jint i = 0; i < tEnd; ++i) {
        const jint tOutSize = aEmbDims[i];
        for (jint j = 0; j < tOutSize; ++j) {
            const jdouble tDot = dot(tInput, tWeights, tInSize) + tBiases[j];
            tWeights += tInSize;
            if (rEmbCache2==NULL) {
                rOutput[j] = silu(tDot);
            } else {
                rOutput[j] = siluGrad(tDot, rGrad+j);
            }
        }
        tBiases += tOutSize;
        tInput = rOutput;
        rOutput += tOutSize;
        if (rEmbCache2!=NULL) rGrad += tOutSize;
        tInSize = tOutSize;
    }
    // no activate in last layer
    rOutput = rEmbRn;
    const jint tOutSize = aEmbDims[tEnd];
    for (jint j = 0; j < tOutSize; ++j) {
        rOutput[j] = dot(tInput, tWeights, tInSize) + tBiases[j];
        tWeights += tInSize;
    }
}

template <jint LMAX>
static void mplusCnlmEmb(jdouble *rCnlm, jdouble *aY, jdouble aFc, jdouble *aEmbRn, jint aSizeN) noexcept {
    constexpr jint tLMAll = (LMAX+1)*(LMAX+1);
    jdouble *tCnlm = rCnlm;
    for (jint np = 0; np < aSizeN; ++np) {
        const jdouble tMul = aFc*aEmbRn[np];
        for (jint k = 0; k < tLMAll; ++k) {
            tCnlm[k] += tMul*aY[k];
        }
        tCnlm += tLMAll;
    }
}
static void mplusCnlmEmb(jdouble *rCnlm, jdouble *aY, jdouble aFc, jdouble *aEmbRn, jint aSizeN, jint aLMax) noexcept {
    switch (aLMax) {
    case 0: {mplusCnlmEmb<0>(rCnlm, aY, aFc, aEmbRn, aSizeN); return;}
    case 1: {mplusCnlmEmb<1>(rCnlm, aY, aFc, aEmbRn, aSizeN); return;}
    case 2: {mplusCnlmEmb<2>(rCnlm, aY, aFc, aEmbRn, aSizeN); return;}
    case 3: {mplusCnlmEmb<3>(rCnlm, aY, aFc, aEmbRn, aSizeN); return;}
    case 4: {mplusCnlmEmb<4>(rCnlm, aY, aFc, aEmbRn, aSizeN); return;}
    case 5: {mplusCnlmEmb<5>(rCnlm, aY, aFc, aEmbRn, aSizeN); return;}
    case 6: {mplusCnlmEmb<6>(rCnlm, aY, aFc, aEmbRn, aSizeN); return;}
    case 7: {mplusCnlmEmb<7>(rCnlm, aY, aFc, aEmbRn, aSizeN); return;}
    case 8: {mplusCnlmEmb<8>(rCnlm, aY, aFc, aEmbRn, aSizeN); return;}
    case 9: {mplusCnlmEmb<9>(rCnlm, aY, aFc, aEmbRn, aSizeN); return;}
    case 10: {mplusCnlmEmb<10>(rCnlm, aY, aFc, aEmbRn, aSizeN); return;}
    case 11: {mplusCnlmEmb<11>(rCnlm, aY, aFc, aEmbRn, aSizeN); return;}
    case 12: {mplusCnlmEmb<12>(rCnlm, aY, aFc, aEmbRn, aSizeN); return;}
    default: {return;}
    }
}


static void backwardEmbRn(jdouble *aInputX, jint aInputDim, jdouble *aEmbWeights, jint *aEmbDims, jint aEmbNumber, jdouble *aEmbCache, jdouble *aEmbCache2, jdouble *aGradEmbRn, jdouble *rGradEmbCache, jdouble *rGradPara) {
    /// switch to last layer
    const jint tEnd = aEmbNumber - 1;
    jdouble *tGradInput = NULL;
    jdouble *tGradOutput = NULL;
    jdouble *tCacheInput = aInputX;
    jdouble *tCacheOutput2 = aEmbCache2;
    jdouble *tWeights = aEmbWeights;
    jdouble *rGradWeights = rGradPara, *rGradBiases = NULL;
    jint tInSize = aInputDim;
    if (tEnd > 0) {
        tGradInput = rGradEmbCache;
        tCacheInput = aEmbCache;
        const jint tEndMM = tEnd - 1;
        for (jint i = 0; i < tEndMM; ++i) {
            const jint tOutSize = aEmbDims[i];
            tGradInput += tOutSize;
            tCacheInput += tOutSize;
        }
    }
    for (jint i = 0; i < tEnd; ++i) {
        const jint tOutSize = aEmbDims[i];
        tWeights += tInSize*tOutSize;
        rGradWeights += tInSize*tOutSize;
        tCacheOutput2 += tOutSize;
        tInSize = tOutSize;
    }
    const jint tEmbOutputDim = aEmbDims[tEnd];
    rGradBiases = rGradWeights + tInSize*tEmbOutputDim;
    for (jint i = 0; i < tEnd; ++i) {
        rGradBiases += aEmbDims[i];
    }
    /// begin backward
    // last layer
    jint tOutSize = tEmbOutputDim;
    for (jint j = 0; j < tOutSize; ++j) {
        const jdouble tSubGradEmbRn = aGradEmbRn[j];
        rGradBiases[j] += tSubGradEmbRn;
        mplus(rGradWeights, tSubGradEmbRn, tCacheInput, tInSize);
        if (tGradInput != NULL) {
            mplus(tGradInput, tSubGradEmbRn, tWeights, tInSize);
        }
        tWeights += tInSize;
        rGradWeights += tInSize;
    }
    if (tEnd == 0) return; // on hidden layer
    tWeights -= tInSize*tOutSize;
    rGradWeights -= tInSize*tOutSize;
    // else layers
    tOutSize = tInSize;
    tGradOutput = tGradInput;
    for (jint i = tEnd-2; i >= 0; --i) {
        tInSize = aEmbDims[i];
        tGradInput -= tInSize;
        tCacheInput -= tInSize;
        tWeights -= tInSize*tOutSize;
        rGradWeights -= tInSize*tOutSize;
        rGradBiases -= tOutSize;
        tCacheOutput2 -= tOutSize;
        for (jint j = 0; j < tOutSize; ++j) {
            const jdouble tSubGrad = tGradOutput[j] * tCacheOutput2[j];
            rGradBiases[j] += tSubGrad;
            mplus(rGradWeights, tSubGrad, tCacheInput, tInSize);
            mplus(tGradInput, tSubGrad, tWeights, tInSize);
            tWeights += tInSize;
            rGradWeights += tInSize;
        }
        tWeights -= tInSize*tOutSize;
        rGradWeights -= tInSize*tOutSize;
        tOutSize = tInSize;
        tGradOutput = tGradInput;
    }
    // to input layer
    tInSize = aInputDim;
    tCacheInput = aInputX;
    rGradWeights -= tInSize*tOutSize;
    rGradBiases -= tOutSize;
    tCacheOutput2 -= tOutSize;
    for (jint j = 0; j < tOutSize; ++j) {
        const jdouble tSubGrad = tGradOutput[j] * tCacheOutput2[j];
        rGradBiases[j] += tSubGrad;
        mplus(rGradWeights, tSubGrad, tCacheInput, tInSize);
        rGradWeights += tInSize;
    }
}

template <jint LMAX>
static void mplusGradCnlmEmb(jdouble *aGradCnlm, jdouble *aY, jdouble aFc, jdouble *rGradEmbRn, jint aSizeN) noexcept {
    constexpr jint tLMAll = (LMAX+1)*(LMAX+1);
    jdouble *tGradCnlm = aGradCnlm;
    for (jint np = 0; np < aSizeN; ++np) {
        const jdouble tDot = dot<tLMAll>(tGradCnlm, aY);
        rGradEmbRn[np] += aFc*tDot;
        tGradCnlm += tLMAll;
    }
}
static void mplusGradCnlmEmb(jdouble *aGradCnlm, jdouble *aY, jdouble aFc, jdouble *rGradEmbRn, jint aSizeN, jint aLMax) noexcept {
    switch (aLMax) {
    case 0: {mplusGradCnlmEmb<0>(aGradCnlm, aY, aFc, rGradEmbRn, aSizeN); return;}
    case 1: {mplusGradCnlmEmb<1>(aGradCnlm, aY, aFc, rGradEmbRn, aSizeN); return;}
    case 2: {mplusGradCnlmEmb<2>(aGradCnlm, aY, aFc, rGradEmbRn, aSizeN); return;}
    case 3: {mplusGradCnlmEmb<3>(aGradCnlm, aY, aFc, rGradEmbRn, aSizeN); return;}
    case 4: {mplusGradCnlmEmb<4>(aGradCnlm, aY, aFc, rGradEmbRn, aSizeN); return;}
    case 5: {mplusGradCnlmEmb<5>(aGradCnlm, aY, aFc, rGradEmbRn, aSizeN); return;}
    case 6: {mplusGradCnlmEmb<6>(aGradCnlm, aY, aFc, rGradEmbRn, aSizeN); return;}
    case 7: {mplusGradCnlmEmb<7>(aGradCnlm, aY, aFc, rGradEmbRn, aSizeN); return;}
    case 8: {mplusGradCnlmEmb<8>(aGradCnlm, aY, aFc, rGradEmbRn, aSizeN); return;}
    case 9: {mplusGradCnlmEmb<9>(aGradCnlm, aY, aFc, rGradEmbRn, aSizeN); return;}
    case 10: {mplusGradCnlmEmb<10>(aGradCnlm, aY, aFc, rGradEmbRn, aSizeN); return;}
    case 11: {mplusGradCnlmEmb<11>(aGradCnlm, aY, aFc, rGradEmbRn, aSizeN); return;}
    case 12: {mplusGradCnlmEmb<12>(aGradCnlm, aY, aFc, rGradEmbRn, aSizeN); return;}
    default: {return;}
    }
}

}


#endif //NNAP_BASIS_MULTILAYERUTIL_H