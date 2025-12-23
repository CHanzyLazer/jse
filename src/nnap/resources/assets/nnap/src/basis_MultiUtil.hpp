#ifndef NNAP_BASIS_MULTILAYERUTIL_H
#define NNAP_BASIS_MULTILAYERUTIL_H

#include "nnap_util.hpp"

namespace JSE_NNAP {

template <jint NMAX, jint LMAX>
static void mplusCnlmMulti(jdouble *rCnlm, jdouble *aY, jdouble *rFc, jdouble *aRn, jdouble *aRFuncScale, jdouble aDis, jdouble *aRCutsL, jdouble *aRCutsR, jint aRCutsSize) noexcept {
    constexpr jint tLMAll = (LMAX+1)*(LMAX+1);
    constexpr jint tSizeBnlm = (NMAX+1)*tLMAll;
    jdouble *tCnlm = rCnlm;
    jdouble *tRFuncScale = aRFuncScale;
    for (jint k = 0; k < aRCutsSize; ++k, tCnlm+=tSizeBnlm, tRFuncScale+=(NMAX+1)) {
        const jdouble tRCutL = aRCutsL[k];
        const jdouble tRCutR = aRCutsR[k];
        if (aDis<=tRCutL || aDis>=tRCutR) continue;
        const jdouble fc = calFc(aDis, tRCutL, tRCutR);
        rFc[k] = fc;
        jdouble *tSubCnlm = tCnlm;
        for (jint n = 0; n <= NMAX; ++n) {
            const jdouble tMul = fc*aRn[n]*tRFuncScale[n];
            mplus<tLMAll>(tSubCnlm, tMul, aY);
            tSubCnlm += tLMAll;
        }
    }
}
template <jint LMAX>
static void mplusCnlmMulti(jdouble *rCnlm, jdouble *aY, jdouble *rFc, jdouble *aRn, jdouble *aRFuncScale, jdouble aDis, jdouble *aRCutsL, jdouble *aRCutsR, jint aRCutsSize, jint aNMax) noexcept {
    switch (aNMax) {
    case 0: {mplusCnlmMulti<0, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 1: {mplusCnlmMulti<1, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 2: {mplusCnlmMulti<2, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 3: {mplusCnlmMulti<3, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 4: {mplusCnlmMulti<4, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 5: {mplusCnlmMulti<5, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 6: {mplusCnlmMulti<6, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 7: {mplusCnlmMulti<7, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 8: {mplusCnlmMulti<8, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 9: {mplusCnlmMulti<9, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 10: {mplusCnlmMulti<10, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 11: {mplusCnlmMulti<11, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 12: {mplusCnlmMulti<12, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 13: {mplusCnlmMulti<13, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 14: {mplusCnlmMulti<14, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 15: {mplusCnlmMulti<15, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 16: {mplusCnlmMulti<16, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 17: {mplusCnlmMulti<17, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 18: {mplusCnlmMulti<18, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 19: {mplusCnlmMulti<19, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    case 20: {mplusCnlmMulti<20, LMAX>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize); return;}
    default: {return;}
    }
}
static void mplusCnlmMulti(jdouble *rCnlm, jdouble *aY, jdouble *rFc, jdouble *aRn, jdouble *aRFuncScale, jdouble aDis, jdouble *aRCutsL, jdouble *aRCutsR, jint aRCutsSize, jint aNMax, jint aLMax) noexcept {
    switch (aLMax) {
    case 0: {mplusCnlmMulti<0>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 1: {mplusCnlmMulti<1>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 2: {mplusCnlmMulti<2>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 3: {mplusCnlmMulti<3>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 4: {mplusCnlmMulti<4>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 5: {mplusCnlmMulti<5>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 6: {mplusCnlmMulti<6>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 7: {mplusCnlmMulti<7>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 8: {mplusCnlmMulti<8>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 9: {mplusCnlmMulti<9>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 10: {mplusCnlmMulti<10>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 11: {mplusCnlmMulti<11>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 12: {mplusCnlmMulti<12>(rCnlm, aY, rFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    default: {return;}
    }
}

template <jint LMAX>
static void gradCnlm2GradYRFcMulti(jdouble *aGradCnlm, jdouble *rGradY, jdouble *rGradRn, jdouble *rGradFc,
                                   jdouble *aY, jdouble *aFc, jdouble *aRn, jdouble *aRFuncScale, jdouble aDis,
                                   jdouble *aRCutsL, jdouble *aRCutsR, jint aRCutsSize, jint aNMax) noexcept {
    constexpr jint tLMAll = (LMAX+1)*(LMAX+1);
    const jint tSizeBnlm = (aNMax+1)*tLMAll;
    jdouble *tGradCnlm = aGradCnlm;
    jdouble *tRFuncScale = aRFuncScale;
    for (jint k = 0; k < aRCutsSize; ++k, tGradCnlm+=tSizeBnlm, tRFuncScale+=(aNMax+1)) {
        const jdouble tRCutL = aRCutsL[k];
        const jdouble tRCutR = aRCutsR[k];
        if (aDis<=tRCutL || aDis>=tRCutR) continue;
        const jdouble fc = aFc[k];
        jdouble rSubGradFc = 0.0;
        jdouble *tSubGradCnlm = tGradCnlm;
        for (jint n = 0; n <= aNMax; ++n) {
            const jdouble tScale = tRFuncScale[n];
            const jdouble tRnn = aRn[n];
            const jdouble tFcRnn = fc*tRnn*tScale;
            
            jdouble tGradFcRn = 0.0;
            for (jint i = 0; i < tLMAll; ++i) {
                const jdouble tSubGradCnlm_i = tSubGradCnlm[i];
                rGradY[i] += tFcRnn * tSubGradCnlm_i;
                tGradFcRn += aY[i] * tSubGradCnlm_i;
            }
            tSubGradCnlm += tLMAll;
            
            rSubGradFc += tScale*tRnn * tGradFcRn;
            rGradRn[n] += tScale*fc * tGradFcRn;
        }
        rGradFc[k] += rSubGradFc;
    }
}
static void gradCnlm2GradYRFcMulti(jdouble *aGradCnlm, jdouble *rGradY, jdouble *rGradRn, jdouble *rGradFc,
                                   jdouble *aY, jdouble *aFc, jdouble *aRn, jdouble *aRFuncScale, jdouble aDis,
                                   jdouble *aRCutsL, jdouble *aRCutsR, jint aRCutsSize, jint aNMax, jint aLMax) noexcept {
    switch (aLMax) {
    case 0: {gradCnlm2GradYRFcMulti<0>(aGradCnlm, rGradY, rGradRn, rGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 1: {gradCnlm2GradYRFcMulti<1>(aGradCnlm, rGradY, rGradRn, rGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 2: {gradCnlm2GradYRFcMulti<2>(aGradCnlm, rGradY, rGradRn, rGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 3: {gradCnlm2GradYRFcMulti<3>(aGradCnlm, rGradY, rGradRn, rGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 4: {gradCnlm2GradYRFcMulti<4>(aGradCnlm, rGradY, rGradRn, rGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 5: {gradCnlm2GradYRFcMulti<5>(aGradCnlm, rGradY, rGradRn, rGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 6: {gradCnlm2GradYRFcMulti<6>(aGradCnlm, rGradY, rGradRn, rGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 7: {gradCnlm2GradYRFcMulti<7>(aGradCnlm, rGradY, rGradRn, rGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 8: {gradCnlm2GradYRFcMulti<8>(aGradCnlm, rGradY, rGradRn, rGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 9: {gradCnlm2GradYRFcMulti<9>(aGradCnlm, rGradY, rGradRn, rGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 10: {gradCnlm2GradYRFcMulti<10>(aGradCnlm, rGradY, rGradRn, rGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 11: {gradCnlm2GradYRFcMulti<11>(aGradCnlm, rGradY, rGradRn, rGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 12: {gradCnlm2GradYRFcMulti<12>(aGradCnlm, rGradY, rGradRn, rGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    default: {return;}
    }
}

template <jint N>
static void gradAny2Fxyz(jdouble *aGradAny, jdouble *aAnyPx, jdouble *aAnyPy, jdouble *aAnyPz,
                        jdouble &rFx, jdouble &rFy, jdouble &rFz) noexcept {
    for (jint i = 0; i < N; ++i) {
        const jdouble tSubGradAny = aGradAny[i];
        rFx += tSubGradAny*aAnyPx[i];
        rFy += tSubGradAny*aAnyPy[i];
        rFz += tSubGradAny*aAnyPz[i];
    }
}
static void gradRn2Fxyz(jdouble *aGradRn, jint aNMax, jdouble *aRnPx, jdouble *aRnPy, jdouble *aRnPz,
                        jdouble &rFx, jdouble &rFy, jdouble &rFz) noexcept {
    switch (aNMax) {
    case 0: {gradAny2Fxyz<0+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 1: {gradAny2Fxyz<1+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 2: {gradAny2Fxyz<2+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 3: {gradAny2Fxyz<3+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 4: {gradAny2Fxyz<4+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 5: {gradAny2Fxyz<5+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 6: {gradAny2Fxyz<6+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 7: {gradAny2Fxyz<7+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 8: {gradAny2Fxyz<8+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 9: {gradAny2Fxyz<9+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 10: {gradAny2Fxyz<10+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 11: {gradAny2Fxyz<11+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 12: {gradAny2Fxyz<12+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 13: {gradAny2Fxyz<13+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 14: {gradAny2Fxyz<14+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 15: {gradAny2Fxyz<15+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 16: {gradAny2Fxyz<16+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 17: {gradAny2Fxyz<17+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 18: {gradAny2Fxyz<18+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 19: {gradAny2Fxyz<19+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 20: {gradAny2Fxyz<20+1>(aGradRn, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    default: {return;}
    }
}
static void gradY2Fxyz(jdouble *aGradY, jint aLMax, jdouble *aYPx, jdouble *aYPy, jdouble *aYPz,
                        jdouble &rFx, jdouble &rFy, jdouble &rFz) noexcept {
    switch (aLMax) {
    case 0: {gradAny2Fxyz<(0+1)*(0+1)>(aGradY, aYPx, aYPy, aYPz, rFx, rFy, rFz); return;}
    case 1: {gradAny2Fxyz<(1+1)*(1+1)>(aGradY, aYPx, aYPy, aYPz, rFx, rFy, rFz); return;}
    case 2: {gradAny2Fxyz<(2+1)*(2+1)>(aGradY, aYPx, aYPy, aYPz, rFx, rFy, rFz); return;}
    case 3: {gradAny2Fxyz<(3+1)*(3+1)>(aGradY, aYPx, aYPy, aYPz, rFx, rFy, rFz); return;}
    case 4: {gradAny2Fxyz<(4+1)*(4+1)>(aGradY, aYPx, aYPy, aYPz, rFx, rFy, rFz); return;}
    case 5: {gradAny2Fxyz<(5+1)*(5+1)>(aGradY, aYPx, aYPy, aYPz, rFx, rFy, rFz); return;}
    case 6: {gradAny2Fxyz<(6+1)*(6+1)>(aGradY, aYPx, aYPy, aYPz, rFx, rFy, rFz); return;}
    case 7: {gradAny2Fxyz<(7+1)*(7+1)>(aGradY, aYPx, aYPy, aYPz, rFx, rFy, rFz); return;}
    case 8: {gradAny2Fxyz<(8+1)*(8+1)>(aGradY, aYPx, aYPy, aYPz, rFx, rFy, rFz); return;}
    case 9: {gradAny2Fxyz<(9+1)*(9+1)>(aGradY, aYPx, aYPy, aYPz, rFx, rFy, rFz); return;}
    case 10: {gradAny2Fxyz<(10+1)*(10+1)>(aGradY, aYPx, aYPy, aYPz, rFx, rFy, rFz); return;}
    case 11: {gradAny2Fxyz<(11+1)*(11+1)>(aGradY, aYPx, aYPy, aYPz, rFx, rFy, rFz); return;}
    case 12: {gradAny2Fxyz<(12+1)*(12+1)>(aGradY, aYPx, aYPy, aYPz, rFx, rFy, rFz); return;}
    default: {return;}
    }
}

template <jint N>
static void gradFxyz2GradNNGradAny(jdouble *rGradNNGradAny, jdouble *aAnyPx, jdouble *aAnyPy, jdouble *aAnyPz,
                                   jdouble aGradFx, jdouble aGradFy, jdouble aGradFz) noexcept {
    for (jint i = 0; i < N; ++i) {
        rGradNNGradAny[i] += aAnyPx[i]*aGradFx + aAnyPy[i]*aGradFy + aAnyPz[i]*aGradFz;
    }
}
static void gradFxyz2GradNNGradRn(jdouble *rGradNNGradRn, jint aNMax, jdouble *aRnPx, jdouble *aRnPy, jdouble *aRnPz,
                                  jdouble aGradFx, jdouble aGradFy, jdouble aGradFz) noexcept {
    switch (aNMax) {
    case 0: {gradFxyz2GradNNGradAny<0+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 1: {gradFxyz2GradNNGradAny<1+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 2: {gradFxyz2GradNNGradAny<2+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 3: {gradFxyz2GradNNGradAny<3+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 4: {gradFxyz2GradNNGradAny<4+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 5: {gradFxyz2GradNNGradAny<5+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 6: {gradFxyz2GradNNGradAny<6+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 7: {gradFxyz2GradNNGradAny<7+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 8: {gradFxyz2GradNNGradAny<8+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 9: {gradFxyz2GradNNGradAny<9+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 10: {gradFxyz2GradNNGradAny<10+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 11: {gradFxyz2GradNNGradAny<11+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 12: {gradFxyz2GradNNGradAny<12+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 13: {gradFxyz2GradNNGradAny<13+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 14: {gradFxyz2GradNNGradAny<14+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 15: {gradFxyz2GradNNGradAny<15+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 16: {gradFxyz2GradNNGradAny<16+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 17: {gradFxyz2GradNNGradAny<17+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 18: {gradFxyz2GradNNGradAny<18+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 19: {gradFxyz2GradNNGradAny<19+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 20: {gradFxyz2GradNNGradAny<20+1>(rGradNNGradRn, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    default: {return;}
    }
}
static void gradFxyz2GradNNGradY(jdouble *rGradNNGradY, jint aLMax, jdouble *aYPx, jdouble *aYPy, jdouble *aYPz,
                                 jdouble aGradFx, jdouble aGradFy, jdouble aGradFz) noexcept {
    switch (aLMax) {
    case 0: {gradFxyz2GradNNGradAny<(0+1)*(0+1)>(rGradNNGradY, aYPx, aYPy, aYPz, aGradFx, aGradFy, aGradFz); return;}
    case 1: {gradFxyz2GradNNGradAny<(1+1)*(1+1)>(rGradNNGradY, aYPx, aYPy, aYPz, aGradFx, aGradFy, aGradFz); return;}
    case 2: {gradFxyz2GradNNGradAny<(2+1)*(2+1)>(rGradNNGradY, aYPx, aYPy, aYPz, aGradFx, aGradFy, aGradFz); return;}
    case 3: {gradFxyz2GradNNGradAny<(3+1)*(3+1)>(rGradNNGradY, aYPx, aYPy, aYPz, aGradFx, aGradFy, aGradFz); return;}
    case 4: {gradFxyz2GradNNGradAny<(4+1)*(4+1)>(rGradNNGradY, aYPx, aYPy, aYPz, aGradFx, aGradFy, aGradFz); return;}
    case 5: {gradFxyz2GradNNGradAny<(5+1)*(5+1)>(rGradNNGradY, aYPx, aYPy, aYPz, aGradFx, aGradFy, aGradFz); return;}
    case 6: {gradFxyz2GradNNGradAny<(6+1)*(6+1)>(rGradNNGradY, aYPx, aYPy, aYPz, aGradFx, aGradFy, aGradFz); return;}
    case 7: {gradFxyz2GradNNGradAny<(7+1)*(7+1)>(rGradNNGradY, aYPx, aYPy, aYPz, aGradFx, aGradFy, aGradFz); return;}
    case 8: {gradFxyz2GradNNGradAny<(8+1)*(8+1)>(rGradNNGradY, aYPx, aYPy, aYPz, aGradFx, aGradFy, aGradFz); return;}
    case 9: {gradFxyz2GradNNGradAny<(9+1)*(9+1)>(rGradNNGradY, aYPx, aYPy, aYPz, aGradFx, aGradFy, aGradFz); return;}
    case 10: {gradFxyz2GradNNGradAny<(10+1)*(10+1)>(rGradNNGradY, aYPx, aYPy, aYPz, aGradFx, aGradFy, aGradFz); return;}
    case 11: {gradFxyz2GradNNGradAny<(11+1)*(11+1)>(rGradNNGradY, aYPx, aYPy, aYPz, aGradFx, aGradFy, aGradFz); return;}
    case 12: {gradFxyz2GradNNGradAny<(12+1)*(12+1)>(rGradNNGradY, aYPx, aYPy, aYPz, aGradFx, aGradFy, aGradFz); return;}
    default: {return;}
    }
}

template <jint LMAX>
static void mplusGradNNGradCnlmMulti(jdouble *rGradNNGradCnlm, jdouble *aGradNNGradY, jdouble *aGradNNGradRn, jdouble *aGradNNGradFc,
                                     jdouble *aY, jdouble *aFc, jdouble *aRn, jdouble *aRFuncScale, jdouble aDis,
                                     jdouble *aRCutsL, jdouble *aRCutsR, jint aRCutsSize, jint aNMax) noexcept {
    constexpr jint tLMAll = (LMAX+1)*(LMAX+1);
    const jint tSizeBnlm = (aNMax+1)*tLMAll;
    jdouble *tGradNNGradCnlm = rGradNNGradCnlm;
    jdouble *tRFuncScale = aRFuncScale;
    for (jint k = 0; k < aRCutsSize; ++k, tGradNNGradCnlm+=tSizeBnlm, tRFuncScale+=(aNMax+1)) {
        const jdouble tRCutL = aRCutsL[k];
        const jdouble tRCutR = aRCutsR[k];
        if (aDis<=tRCutL || aDis>=tRCutR) continue;
        const jdouble fc = aFc[k];
        const jdouble tGradNNGradFc = aGradNNGradFc[k];
        jdouble *tSubGradNNGradCnlm = tGradNNGradCnlm;
        for (jint n = 0; n <= aNMax; ++n) {
            const jdouble tScale = tRFuncScale[n];
            const jdouble tRnn = aRn[n];
            const jdouble tFcRnn = tScale*fc*tRnn;
            const jdouble tGradNNGradFcRn = tScale * (tRnn*tGradNNGradFc + fc*aGradNNGradRn[n]);
            for (jint i = 0; i < tLMAll; ++i) {
                tSubGradNNGradCnlm[i] += aY[i]*tGradNNGradFcRn + tFcRnn*aGradNNGradY[i];
            }
            tSubGradNNGradCnlm += tLMAll;
        }
    }
}
static void mplusGradNNGradCnlmMulti(jdouble *rGradNNGradCnlm, jdouble *aGradNNGradY, jdouble *aGradNNGradRn, jdouble *aGradNNGradFc,
                                     jdouble *aY, jdouble *aFc, jdouble *aRn, jdouble *aRFuncScale, jdouble aDis,
                                     jdouble *aRCutsL, jdouble *aRCutsR, jint aRCutsSize, jint aNMax, jint aLMax) noexcept {
    switch (aLMax) {
    case 0: {mplusGradNNGradCnlmMulti<0>(rGradNNGradCnlm, aGradNNGradY, aGradNNGradRn, aGradNNGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 1: {mplusGradNNGradCnlmMulti<1>(rGradNNGradCnlm, aGradNNGradY, aGradNNGradRn, aGradNNGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 2: {mplusGradNNGradCnlmMulti<2>(rGradNNGradCnlm, aGradNNGradY, aGradNNGradRn, aGradNNGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 3: {mplusGradNNGradCnlmMulti<3>(rGradNNGradCnlm, aGradNNGradY, aGradNNGradRn, aGradNNGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 4: {mplusGradNNGradCnlmMulti<4>(rGradNNGradCnlm, aGradNNGradY, aGradNNGradRn, aGradNNGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 5: {mplusGradNNGradCnlmMulti<5>(rGradNNGradCnlm, aGradNNGradY, aGradNNGradRn, aGradNNGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 6: {mplusGradNNGradCnlmMulti<6>(rGradNNGradCnlm, aGradNNGradY, aGradNNGradRn, aGradNNGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 7: {mplusGradNNGradCnlmMulti<7>(rGradNNGradCnlm, aGradNNGradY, aGradNNGradRn, aGradNNGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 8: {mplusGradNNGradCnlmMulti<8>(rGradNNGradCnlm, aGradNNGradY, aGradNNGradRn, aGradNNGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 9: {mplusGradNNGradCnlmMulti<9>(rGradNNGradCnlm, aGradNNGradY, aGradNNGradRn, aGradNNGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 10: {mplusGradNNGradCnlmMulti<10>(rGradNNGradCnlm, aGradNNGradY, aGradNNGradRn, aGradNNGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 11: {mplusGradNNGradCnlmMulti<11>(rGradNNGradCnlm, aGradNNGradY, aGradNNGradRn, aGradNNGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
    case 12: {mplusGradNNGradCnlmMulti<12>(rGradNNGradCnlm, aGradNNGradY, aGradNNGradRn, aGradNNGradFc, aY, aFc, aRn, aRFuncScale, aDis, aRCutsL, aRCutsR, aRCutsSize, aNMax); return;}
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