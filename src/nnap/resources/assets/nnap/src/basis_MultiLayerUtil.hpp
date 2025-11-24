#ifndef NNAP_BASIS_MULTILAYERUTIL_H
#define NNAP_BASIS_MULTILAYERUTIL_H

#include "nnap_util.hpp"

namespace JSE_NNAP {

template <jint NMAX>
static void calRFuse(jdouble *rRFn, jdouble *aRn, jdouble *aRFuseWeight, jint aRFuseSize) {
    jdouble *tRFuseWeight = aRFuseWeight;
    for (jint np = 0; np < aRFuseSize; ++np) {
        rRFn[np] = dot<NMAX+1>(aRn, tRFuseWeight);
        tRFuseWeight += NMAX+1;
    }
}
static void calRFuse(jdouble *rRFn, jdouble *aRn, jdouble *aRFuseWeight, jint aRFuseSize, jint aNMax) {
    switch (aNMax) {
    case 0: {calRFuse<0>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 1: {calRFuse<1>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 2: {calRFuse<2>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 3: {calRFuse<3>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 4: {calRFuse<4>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 5: {calRFuse<5>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 6: {calRFuse<6>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 7: {calRFuse<7>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 8: {calRFuse<8>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 9: {calRFuse<9>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 10: {calRFuse<10>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 11: {calRFuse<11>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 12: {calRFuse<12>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 13: {calRFuse<13>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 14: {calRFuse<14>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 15: {calRFuse<15>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 16: {calRFuse<16>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 17: {calRFuse<17>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 18: {calRFuse<18>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 19: {calRFuse<19>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    case 20: {calRFuse<20>(rRFn, aRn, aRFuseWeight, aRFuseSize); return;}
    default: {return;}
    }
}

template <jint LMAX>
static void mplusCnlmRFuse(jdouble *rCnlm, jdouble *aY, jdouble aFc, jdouble *aRFn, jint aRFuseSize) noexcept {
    constexpr jint tLMAll = (LMAX+1)*(LMAX+1);
    jdouble *tCnlm = rCnlm;
    for (jint np = 0; np < aRFuseSize; ++np) {
        const jdouble tMul = aFc*aRFn[np];
        for (jint k = 0; k < tLMAll; ++k) {
            tCnlm[k] += tMul*aY[k];
        }
        tCnlm += tLMAll;
    }
}
static void mplusCnlmRFuse(jdouble *rCnlm, jdouble *aY, jdouble aFc, jdouble *aRFn, jint aRFuseSize, jint aLMax) noexcept {
    switch (aLMax) {
    case 0: {mplusCnlmRFuse<0>(rCnlm, aY, aFc, aRFn, aRFuseSize); return;}
    case 1: {mplusCnlmRFuse<1>(rCnlm, aY, aFc, aRFn, aRFuseSize); return;}
    case 2: {mplusCnlmRFuse<2>(rCnlm, aY, aFc, aRFn, aRFuseSize); return;}
    case 3: {mplusCnlmRFuse<3>(rCnlm, aY, aFc, aRFn, aRFuseSize); return;}
    case 4: {mplusCnlmRFuse<4>(rCnlm, aY, aFc, aRFn, aRFuseSize); return;}
    case 5: {mplusCnlmRFuse<5>(rCnlm, aY, aFc, aRFn, aRFuseSize); return;}
    case 6: {mplusCnlmRFuse<6>(rCnlm, aY, aFc, aRFn, aRFuseSize); return;}
    case 7: {mplusCnlmRFuse<7>(rCnlm, aY, aFc, aRFn, aRFuseSize); return;}
    case 8: {mplusCnlmRFuse<8>(rCnlm, aY, aFc, aRFn, aRFuseSize); return;}
    case 9: {mplusCnlmRFuse<9>(rCnlm, aY, aFc, aRFn, aRFuseSize); return;}
    case 10: {mplusCnlmRFuse<10>(rCnlm, aY, aFc, aRFn, aRFuseSize); return;}
    case 11: {mplusCnlmRFuse<11>(rCnlm, aY, aFc, aRFn, aRFuseSize); return;}
    case 12: {mplusCnlmRFuse<12>(rCnlm, aY, aFc, aRFn, aRFuseSize); return;}
    default: {return;}
    }
}

template <jint NMAX>
static void mplusGradParaRFuse(jdouble *aGradRFn, jdouble *aRn, jdouble *rGradPara, jint aRFuseSize) {
    jdouble *tGradPara = rGradPara;
    for (jint np = 0; np < aRFuseSize; ++np) {
        mplus<NMAX+1>(tGradPara, aGradRFn[np], aRn);
        tGradPara += NMAX+1;
    }
}
static void mplusGradParaRFuse(jdouble *aGradRFn, jdouble *aRn, jdouble *rGradPara, jint aRFuseSize, jint aNMax) {
    switch (aNMax) {
    case 0: {mplusGradParaRFuse<0>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 1: {mplusGradParaRFuse<1>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 2: {mplusGradParaRFuse<2>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 3: {mplusGradParaRFuse<3>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 4: {mplusGradParaRFuse<4>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 5: {mplusGradParaRFuse<5>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 6: {mplusGradParaRFuse<6>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 7: {mplusGradParaRFuse<7>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 8: {mplusGradParaRFuse<8>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 9: {mplusGradParaRFuse<9>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 10: {mplusGradParaRFuse<10>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 11: {mplusGradParaRFuse<11>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 12: {mplusGradParaRFuse<12>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 13: {mplusGradParaRFuse<13>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 14: {mplusGradParaRFuse<14>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 15: {mplusGradParaRFuse<15>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 16: {mplusGradParaRFuse<16>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 17: {mplusGradParaRFuse<17>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 18: {mplusGradParaRFuse<18>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 19: {mplusGradParaRFuse<19>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    case 20: {mplusGradParaRFuse<20>(aGradRFn, aRn, rGradPara, aRFuseSize); return;}
    default: {return;}
    }
}

template <jint LMAX>
static void mplusGradCnlmRFuse(jdouble *aGradCnlm, jdouble *aY, jdouble aFc, jdouble *rGradRFn, jint aRFuseSize) noexcept {
    constexpr jint tLMAll = (LMAX+1)*(LMAX+1);
    jdouble *tGradCnlm = aGradCnlm;
    for (jint np = 0; np < aRFuseSize; ++np) {
        const jdouble tDot = dot<tLMAll>(tGradCnlm, aY);
        rGradRFn[np] += aFc*tDot;
        tGradCnlm += tLMAll;
    }
}
static void mplusGradCnlmRFuse(jdouble *aGradCnlm, jdouble *aY, jdouble aFc, jdouble *rGradRFn, jint aRFuseSize, jint aLMax) noexcept {
    switch (aLMax) {
    case 0: {mplusGradCnlmRFuse<0>(aGradCnlm, aY, aFc, rGradRFn, aRFuseSize); return;}
    case 1: {mplusGradCnlmRFuse<1>(aGradCnlm, aY, aFc, rGradRFn, aRFuseSize); return;}
    case 2: {mplusGradCnlmRFuse<2>(aGradCnlm, aY, aFc, rGradRFn, aRFuseSize); return;}
    case 3: {mplusGradCnlmRFuse<3>(aGradCnlm, aY, aFc, rGradRFn, aRFuseSize); return;}
    case 4: {mplusGradCnlmRFuse<4>(aGradCnlm, aY, aFc, rGradRFn, aRFuseSize); return;}
    case 5: {mplusGradCnlmRFuse<5>(aGradCnlm, aY, aFc, rGradRFn, aRFuseSize); return;}
    case 6: {mplusGradCnlmRFuse<6>(aGradCnlm, aY, aFc, rGradRFn, aRFuseSize); return;}
    case 7: {mplusGradCnlmRFuse<7>(aGradCnlm, aY, aFc, rGradRFn, aRFuseSize); return;}
    case 8: {mplusGradCnlmRFuse<8>(aGradCnlm, aY, aFc, rGradRFn, aRFuseSize); return;}
    case 9: {mplusGradCnlmRFuse<9>(aGradCnlm, aY, aFc, rGradRFn, aRFuseSize); return;}
    case 10: {mplusGradCnlmRFuse<10>(aGradCnlm, aY, aFc, rGradRFn, aRFuseSize); return;}
    case 11: {mplusGradCnlmRFuse<11>(aGradCnlm, aY, aFc, rGradRFn, aRFuseSize); return;}
    case 12: {mplusGradCnlmRFuse<12>(aGradCnlm, aY, aFc, rGradRFn, aRFuseSize); return;}
    default: {return;}
    }
}

}


#endif //NNAP_BASIS_MULTILAYERUTIL_H