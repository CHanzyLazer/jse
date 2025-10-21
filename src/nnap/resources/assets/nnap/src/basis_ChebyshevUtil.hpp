#ifndef BASIS_CHEBYSHEV_UTIL_H
#define BASIS_CHEBYSHEV_UTIL_H

#include "nnap_util.hpp"

namespace JSE_NNAP {

template <jint NMAX>
static void mplusFpFuse_(jboolean aExFlag, jdouble *rFp, jdouble *aFuseWeight, jint aType, jdouble aFc, jdouble *aRn, jint aFuseSize) {
    jdouble *tFuseWeight = aFuseWeight + aFuseSize*(aType-1);
    jdouble *tFp = rFp;
    if (aExFlag) {
        mplus<NMAX+1>(tFp, aFc, aRn);
        tFp += (NMAX+1);
    }
    for (jint k = 0; k < aFuseSize; ++k) {
        mplus<NMAX+1>(tFp, aFc*tFuseWeight[k], aRn);
        tFp += (NMAX+1);
    }
}
static void mplusFpFuse_(jboolean aExFlag, jdouble *rFp, jdouble *aFuseWeight, jint aType, jdouble aFc, jdouble *aRn, jint aFuseSize, jint aNMax) {
    switch (aNMax) {
    case 0: {mplusFpFuse_<0>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 1: {mplusFpFuse_<1>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 2: {mplusFpFuse_<2>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 3: {mplusFpFuse_<3>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 4: {mplusFpFuse_<4>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 5: {mplusFpFuse_<5>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 6: {mplusFpFuse_<6>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 7: {mplusFpFuse_<7>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 8: {mplusFpFuse_<8>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 9: {mplusFpFuse_<9>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 10: {mplusFpFuse_<10>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 11: {mplusFpFuse_<11>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 12: {mplusFpFuse_<12>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 13: {mplusFpFuse_<13>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 14: {mplusFpFuse_<14>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 15: {mplusFpFuse_<15>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 16: {mplusFpFuse_<16>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 17: {mplusFpFuse_<17>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 18: {mplusFpFuse_<18>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 19: {mplusFpFuse_<19>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    case 20: {mplusFpFuse_<20>(aExFlag, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize); return;}
    default: {return;}
    }
}
static inline void mplusFpFuse(jdouble *rFp, jdouble *aFuseWeight, jint aType, jdouble aFc, jdouble *aRn, jint aFuseSize, jint aNMax) {
    mplusFpFuse_(JNI_FALSE, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize, aNMax);
}
static inline void mplusFpExFuse(jdouble *rFp, jdouble *aFuseWeight, jint aType, jdouble aFc, jdouble *aRn, jint aFuseSize, jint aNMax) {
    mplusFpFuse_(JNI_TRUE, rFp, aFuseWeight, aType, aFc, aRn, aFuseSize, aNMax);
}


template <jint NMAX>
static void mplusGradParaFuse(jdouble *aGradFp, jdouble *rGradPara, jint aType, jdouble aFc, jdouble *aRn, jint aFuseSize) {
    jdouble *tGradFp = aGradFp;
    jdouble *tGradPara = rGradPara + aFuseSize*(aType-1);
    for (jint k = 0; k < aFuseSize; ++k) {
        tGradPara[k] += aFc*dot<NMAX+1>(aRn, tGradFp);
        tGradFp += (NMAX+1);
    }
}
static void mplusGradParaFuse(jdouble *aGradFp, jdouble *rGradPara, jint aType, jdouble aFc, jdouble *aRn, jint aFuseSize, jint aNMax) {
    switch (aNMax) {
    case 0: {mplusGradParaFuse<0>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 1: {mplusGradParaFuse<1>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 2: {mplusGradParaFuse<2>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 3: {mplusGradParaFuse<3>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 4: {mplusGradParaFuse<4>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 5: {mplusGradParaFuse<5>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 6: {mplusGradParaFuse<6>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 7: {mplusGradParaFuse<7>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 8: {mplusGradParaFuse<8>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 9: {mplusGradParaFuse<9>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 10: {mplusGradParaFuse<10>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 11: {mplusGradParaFuse<11>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 12: {mplusGradParaFuse<12>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 13: {mplusGradParaFuse<13>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 14: {mplusGradParaFuse<14>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 15: {mplusGradParaFuse<15>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 16: {mplusGradParaFuse<16>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 17: {mplusGradParaFuse<17>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 18: {mplusGradParaFuse<18>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 19: {mplusGradParaFuse<19>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    case 20: {mplusGradParaFuse<20>(aGradFp, rGradPara, aType, aFc, aRn, aFuseSize); return;}
    default: {return;}
    }
}


template <jint NMAX>
static void calGradRnFuse_(jboolean aExFlag, jdouble *rGradRn, jdouble *aNNGrad, jdouble *aFuseWeight, jint aType, jint aFuseSize) {
    for (jint n = 0; n <= NMAX; ++n) {
        rGradRn[n] = 0.0;
    }
    jdouble *tFuseWeight = aFuseWeight + aFuseSize*(aType-1);
    jdouble *tNNGrad = aNNGrad;
    if (aExFlag) {
        mplus<NMAX+1>(rGradRn, 1.0, tNNGrad);
        tNNGrad += (NMAX+1);
    }
    for (jint k = 0; k < aFuseSize; ++k) {
        mplus<NMAX+1>(rGradRn, tFuseWeight[k], tNNGrad);
        tNNGrad += (NMAX+1);
    }
}
static void calGradRnFuse_(jboolean aExFlag, jdouble *rGradRn, jdouble *aNNGrad, jdouble *aFuseWeight, jint aType, jint aFuseSize, jint aNMax) {
    switch (aNMax) {
    case 0: {calGradRnFuse_<0>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 1: {calGradRnFuse_<1>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 2: {calGradRnFuse_<2>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 3: {calGradRnFuse_<3>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 4: {calGradRnFuse_<4>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 5: {calGradRnFuse_<5>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 6: {calGradRnFuse_<6>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 7: {calGradRnFuse_<7>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 8: {calGradRnFuse_<8>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 9: {calGradRnFuse_<9>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 10: {calGradRnFuse_<10>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 11: {calGradRnFuse_<11>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 12: {calGradRnFuse_<12>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 13: {calGradRnFuse_<13>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 14: {calGradRnFuse_<14>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 15: {calGradRnFuse_<15>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 16: {calGradRnFuse_<16>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 17: {calGradRnFuse_<17>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 18: {calGradRnFuse_<18>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 19: {calGradRnFuse_<19>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    case 20: {calGradRnFuse_<20>(aExFlag, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize); return;}
    default: {return;}
    }
}
static inline void calGradRnFuse(jdouble *rGradRn, jdouble *aNNGrad, jdouble *aFuseWeight, jint aType, jint aFuseSize, jint aNMax) {
    calGradRnFuse_(JNI_FALSE, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize, aNMax);
}
static inline void calGradRnExFuse(jdouble *rGradRn, jdouble *aNNGrad, jdouble *aFuseWeight, jint aType, jint aFuseSize, jint aNMax) {
    calGradRnFuse_(JNI_TRUE, rGradRn, aNNGrad, aFuseWeight, aType, aFuseSize, aNMax);
}

template <jint NMAX, jboolean WT>
static void gradRn2Fxyz_(jint j, jdouble *aGradRn, jdouble *aGradRnWt, jdouble aFc, jdouble *aRn, jdouble aWt,
                         jdouble aFcPx, jdouble aFcPy, jdouble aFcPz, jdouble *aRnPx, jdouble *aRnPy, jdouble *aRnPz,
                         jdouble *rFx, jdouble *rFy, jdouble *rFz) noexcept {
    jdouble tGradFc = 0.0;
    jdouble rFxj = 0.0, rFyj = 0.0, rFzj = 0.0;
    for (jint n = 0; n <= NMAX; ++n) {
        jdouble tGradRnn = aGradRn[n];
        if (WT) tGradRnn += aWt*aGradRnWt[n];
        const jdouble tRnn = aRn[n];
        tGradFc += tRnn * tGradRnn;
        tGradRnn *= aFc;
        rFxj += tGradRnn*aRnPx[n];
        rFyj += tGradRnn*aRnPy[n];
        rFzj += tGradRnn*aRnPz[n];
    }
    rFxj += aFcPx*tGradFc;
    rFyj += aFcPy*tGradFc;
    rFzj += aFcPz*tGradFc;
    rFx[j] += rFxj; rFy[j] += rFyj; rFz[j] += rFzj;
}
template <jboolean WT>
static void gradRn2Fxyz_(jint j, jdouble *aGradRn, jdouble *aGradRnWt, jdouble aFc, jdouble *aRn, jdouble aWt, jint aNMax,
                         jdouble aFcPx, jdouble aFcPy, jdouble aFcPz, jdouble *aRnPx, jdouble *aRnPy, jdouble *aRnPz,
                         jdouble *rFx, jdouble *rFy, jdouble *rFz) noexcept {
    switch (aNMax) {
    case 0: {gradRn2Fxyz_<0, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 1: {gradRn2Fxyz_<1, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 2: {gradRn2Fxyz_<2, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 3: {gradRn2Fxyz_<3, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 4: {gradRn2Fxyz_<4, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 5: {gradRn2Fxyz_<5, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 6: {gradRn2Fxyz_<6, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 7: {gradRn2Fxyz_<7, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 8: {gradRn2Fxyz_<8, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 9: {gradRn2Fxyz_<9, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 10: {gradRn2Fxyz_<10, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 11: {gradRn2Fxyz_<11, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 12: {gradRn2Fxyz_<12, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 13: {gradRn2Fxyz_<13, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 14: {gradRn2Fxyz_<14, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 15: {gradRn2Fxyz_<15, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 16: {gradRn2Fxyz_<16, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 17: {gradRn2Fxyz_<17, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 18: {gradRn2Fxyz_<18, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 19: {gradRn2Fxyz_<19, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    case 20: {gradRn2Fxyz_<20, WT>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz); return;}
    default: {return;}
    }
}
static inline void gradRn2Fxyz(jint j, jdouble *aGradRn, jdouble aFc, jdouble *aRn, jint aNMax, jdouble aFcPx, jdouble aFcPy, jdouble aFcPz, jdouble *aRnPx, jdouble *aRnPy, jdouble *aRnPz, jdouble *rFx, jdouble *rFy, jdouble *rFz) noexcept {
    gradRn2Fxyz_<JNI_FALSE>(j, aGradRn, NULL, aFc, aRn, 0, aNMax, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz);
}
static inline void gradRnWt2Fxyz(jint j, jdouble *aGradRn, jdouble *aGradRnWt, jdouble aFc, jdouble *aRn, jdouble aWt, jint aNMax, jdouble aFcPx, jdouble aFcPy, jdouble aFcPz, jdouble *aRnPx, jdouble *aRnPy, jdouble *aRnPz, jdouble *rFx, jdouble *rFy, jdouble *rFz) noexcept {
    gradRn2Fxyz_<JNI_TRUE>(j, aGradRn, aGradRnWt, aFc, aRn, aWt, aNMax, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, rFx, rFy, rFz);
}


template <jint NMAX, jboolean MPLUS, jboolean WT>
static void calormplusGradNNGrad_(jdouble *rGradNNGrad, jdouble *rGradNNGradWt, jdouble aFc, jdouble *aRn, jdouble aWt,
                                  jdouble aFcPx, jdouble aFcPy, jdouble aFcPz, jdouble *aRnPx, jdouble *aRnPy, jdouble *aRnPz,
                                  jdouble aGradFx, jdouble aGradFy, jdouble aGradFz) {
    // cal gradNNgradFc
    const jdouble tGradNNGradFc = aFcPx*aGradFx + aFcPy*aGradFy + aFcPz*aGradFz;
    // cal gradNNgrad
    for (jint n = 0; n <= NMAX; ++n) {
        jdouble tGradNNGradRn = aRnPx[n]*aGradFx + aRnPy[n]*aGradFy + aRnPz[n]*aGradFz;
        tGradNNGradRn = aRn[n]*tGradNNGradFc + aFc*tGradNNGradRn;
        if (MPLUS) {
            rGradNNGrad[n] += tGradNNGradRn;
            if (WT) rGradNNGradWt[n] += aWt*tGradNNGradRn;
        } else {
            rGradNNGrad[n] = tGradNNGradRn;
            if (WT) rGradNNGradWt[n] = aWt*tGradNNGradRn;
        }
    }
}
template <jboolean MPLUS, jboolean WT>
static void calormplusGradNNGrad_(jdouble *rGradNNGrad, jdouble *rGradNNGradWt, jdouble aFc, jdouble *aRn, jdouble aWt, jint aNMax,
                                  jdouble aFcPx, jdouble aFcPy, jdouble aFcPz, jdouble *aRnPx, jdouble *aRnPy, jdouble *aRnPz,
                                  jdouble aGradFx, jdouble aGradFy, jdouble aGradFz) {
    switch (aNMax) {
    case 0: {calormplusGradNNGrad_<0, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 1: {calormplusGradNNGrad_<1, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 2: {calormplusGradNNGrad_<2, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 3: {calormplusGradNNGrad_<3, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 4: {calormplusGradNNGrad_<4, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 5: {calormplusGradNNGrad_<5, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 6: {calormplusGradNNGrad_<6, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 7: {calormplusGradNNGrad_<7, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 8: {calormplusGradNNGrad_<8, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 9: {calormplusGradNNGrad_<9, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 10: {calormplusGradNNGrad_<10, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 11: {calormplusGradNNGrad_<11, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 12: {calormplusGradNNGrad_<12, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 13: {calormplusGradNNGrad_<13, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 14: {calormplusGradNNGrad_<14, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 15: {calormplusGradNNGrad_<15, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 16: {calormplusGradNNGrad_<16, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 17: {calormplusGradNNGrad_<17, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 18: {calormplusGradNNGrad_<18, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 19: {calormplusGradNNGrad_<19, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    case 20: {calormplusGradNNGrad_<20, MPLUS, WT>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz); return;}
    default: {return;}
    }
}
static inline void calGradNNGradRn(jdouble *rGradNNGradRn, jdouble aFc, jdouble *aRn, jint aNMax, jdouble aFcPx, jdouble aFcPy, jdouble aFcPz, jdouble *aRnPx, jdouble *aRnPy, jdouble *aRnPz, jdouble aGradFx, jdouble aGradFy, jdouble aGradFz) {
    calormplusGradNNGrad_<JNI_FALSE, JNI_FALSE>(rGradNNGradRn, NULL, aFc, aRn, 0, aNMax, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz);
}
static inline void mplusGradNNGrad(jdouble *rGradNNGrad, jdouble aFc, jdouble *aRn, jint aNMax, jdouble aFcPx, jdouble aFcPy, jdouble aFcPz, jdouble *aRnPx, jdouble *aRnPy, jdouble *aRnPz, jdouble aGradFx, jdouble aGradFy, jdouble aGradFz) {
    calormplusGradNNGrad_<JNI_TRUE, JNI_FALSE>(rGradNNGrad, NULL, aFc, aRn, 0, aNMax, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz);
}
static inline void mplusGradNNGradWt(jdouble *rGradNNGrad, jdouble *rGradNNGradWt, jdouble aFc, jdouble *aRn, jdouble aWt, jint aNMax, jdouble aFcPx, jdouble aFcPy, jdouble aFcPz, jdouble *aRnPx, jdouble *aRnPy, jdouble *aRnPz, jdouble aGradFx, jdouble aGradFy, jdouble aGradFz) {
    calormplusGradNNGrad_<JNI_TRUE, JNI_TRUE>(rGradNNGrad, rGradNNGradWt, aFc, aRn, aWt, aNMax, aFcPx, aFcPy, aFcPz, aRnPx, aRnPy, aRnPz, aGradFx, aGradFy, aGradFz);
}

template <jint NMAX>
static void mplusGradNNGradFuse_(jboolean aExFlag, jdouble *rGradNNGrad, jdouble *rGradNNGradRn, jdouble *aNNGrad, jdouble *aFuseWeight, jdouble *rGradPara, jint aType, jint aFuseSize, jboolean aFixBasis) {
    jdouble *tFuseWeight = aFuseWeight + aFuseSize*(aType-1);
    jdouble *tGradNNGrad = rGradNNGrad;
    if (aExFlag) {
        mplus<NMAX+1>(tGradNNGrad, 1.0, rGradNNGradRn);
        tGradNNGrad += (NMAX+1);
    }
    for (jint k = 0; k < aFuseSize; ++k) {
        mplus<NMAX+1>(tGradNNGrad, tFuseWeight[k], rGradNNGradRn);
        tGradNNGrad += (NMAX+1);
    }
    if (!aFixBasis) {
        jdouble *tGradPara = rGradPara + aFuseSize*(aType-1);
        jdouble *tNNGrad = aNNGrad;
        if (aExFlag) {
            tNNGrad += (NMAX+1);
        }
        for (jint k = 0; k < aFuseSize; ++k) {
            tGradPara[k] += dot<NMAX+1>(tNNGrad, rGradNNGradRn);
            tNNGrad += (NMAX+1);
        }
    }
}
static void mplusGradNNGradFuse_(jboolean aExFlag, jdouble *rGradNNGrad, jdouble *rGradNNGradRn, jdouble *aNNGrad, jdouble *aFuseWeight, jdouble *rGradPara, jint aType, jint aFuseSize, jint aNMax, jboolean aFixBasis) {
    switch (aNMax) {
    case 0: {mplusGradNNGradFuse_<0>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 1: {mplusGradNNGradFuse_<1>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 2: {mplusGradNNGradFuse_<2>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 3: {mplusGradNNGradFuse_<3>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 4: {mplusGradNNGradFuse_<4>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 5: {mplusGradNNGradFuse_<5>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 6: {mplusGradNNGradFuse_<6>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 7: {mplusGradNNGradFuse_<7>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 8: {mplusGradNNGradFuse_<8>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 9: {mplusGradNNGradFuse_<9>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 10: {mplusGradNNGradFuse_<10>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 11: {mplusGradNNGradFuse_<11>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 12: {mplusGradNNGradFuse_<12>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 13: {mplusGradNNGradFuse_<13>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 14: {mplusGradNNGradFuse_<14>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 15: {mplusGradNNGradFuse_<15>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 16: {mplusGradNNGradFuse_<16>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 17: {mplusGradNNGradFuse_<17>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 18: {mplusGradNNGradFuse_<18>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 19: {mplusGradNNGradFuse_<19>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    case 20: {mplusGradNNGradFuse_<20>(aExFlag, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aFixBasis); return;}
    default: {return;}
    }
}
static inline void mplusGradNNGradFuse(jdouble *rGradNNGrad, jdouble *rGradNNGradRn, jdouble *aNNGrad, jdouble *aFuseWeight, jdouble *rGradPara, jint aType, jint aFuseSize, jint aNMax, jboolean aFixBasis) {
    mplusGradNNGradFuse_(JNI_FALSE, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aNMax, aFixBasis);
}
static inline void mplusGradNNGradExFuse(jdouble *rGradNNGrad, jdouble *rGradNNGradRn, jdouble *aNNGrad, jdouble *aFuseWeight, jdouble *rGradPara, jint aType, jint aFuseSize, jint aNMax, jboolean aFixBasis) {
    mplusGradNNGradFuse_(JNI_TRUE, rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, aType, aFuseSize, aNMax, aFixBasis);
}

}

#endif //BASIS_CHEBYSHEV_UTIL_H