#ifndef BASIS_EQUIVARIANT_UTIL_H
#define BASIS_EQUIVARIANT_UTIL_H

#include "basis_EquivariantUtil0.hpp"

namespace JSE_NNAP {

template <jint LMAX, jint CGIDX, jint SUBIDX>
static inline jdouble mplusMnlmSubSub_(jdouble *aAnlm) noexcept {
    constexpr jint i1 = CG_INDEX1[LMAX][CGIDX][SUBIDX];
    constexpr jint i2 = CG_INDEX2[LMAX][CGIDX][SUBIDX];
    constexpr jdouble coeff = CG_COEFF[LMAX][CGIDX][SUBIDX];
    return coeff * aAnlm[i1] * aAnlm[i2];
}
template <jint LMAX, jint CGIDX>
static jdouble mplusMnlmSub_(jdouble *aAnlm) noexcept {
    constexpr jint tSize = CG_SIZE[LMAX][CGIDX];
    jdouble rSubMnlm = 0.0;
    if (tSize==0) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 0>(aAnlm);
    if (tSize==1) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 1>(aAnlm);
    if (tSize==2) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 2>(aAnlm);
    if (tSize==3) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 3>(aAnlm);
    if (tSize==4) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 4>(aAnlm);
    if (tSize==5) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 5>(aAnlm);
    if (tSize==6) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 6>(aAnlm);
    if (tSize==7) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 7>(aAnlm);
    if (tSize==8) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 8>(aAnlm);
    if (tSize==9) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 9>(aAnlm);
    if (tSize==10) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 10>(aAnlm);
    if (tSize==11) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 11>(aAnlm);
    if (tSize==12) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 12>(aAnlm);
    if (tSize==13) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 13>(aAnlm);
    if (tSize==14) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 14>(aAnlm);
    if (tSize==15) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 15>(aAnlm);
    if (tSize==16) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 16>(aAnlm);
    if (tSize==17) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 17>(aAnlm);
    if (tSize==18) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 18>(aAnlm);
    if (tSize==19) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 19>(aAnlm);
    if (tSize==20) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 20>(aAnlm);
    if (tSize==21) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 21>(aAnlm);
    if (tSize==22) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 22>(aAnlm);
    if (tSize==23) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 23>(aAnlm);
    if (tSize==24) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 24>(aAnlm);
    if (tSize==25) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 25>(aAnlm);
    if (tSize==26) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 26>(aAnlm);
    if (tSize==27) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 27>(aAnlm);
    if (tSize==28) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 28>(aAnlm);
    if (tSize==29) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 29>(aAnlm);
    if (tSize==30) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 30>(aAnlm);
    if (tSize==31) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 31>(aAnlm);
    if (tSize==32) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 32>(aAnlm);
    if (tSize==33) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 33>(aAnlm);
    if (tSize==34) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 34>(aAnlm);
    if (tSize==35) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 35>(aAnlm);
    if (tSize==36) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 36>(aAnlm);
    if (tSize==37) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 37>(aAnlm);
    if (tSize==38) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 38>(aAnlm);
    if (tSize==39) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 39>(aAnlm);
    if (tSize==40) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 40>(aAnlm);
    if (tSize==41) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 41>(aAnlm);
    if (tSize==42) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 42>(aAnlm);
    if (tSize==43) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 43>(aAnlm);
    if (tSize==44) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 44>(aAnlm);
    if (tSize==45) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 45>(aAnlm);
    if (tSize==46) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 46>(aAnlm);
    if (tSize==47) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 47>(aAnlm);
    if (tSize==48) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 48>(aAnlm);
    if (tSize==49) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 49>(aAnlm);
    if (tSize==50) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 50>(aAnlm);
    if (tSize==51) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 51>(aAnlm);
    if (tSize==52) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 52>(aAnlm);
    if (tSize==53) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 53>(aAnlm);
    if (tSize==54) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 54>(aAnlm);
    if (tSize==55) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 55>(aAnlm);
    if (tSize==56) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 56>(aAnlm);
    if (tSize==57) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 57>(aAnlm);
    if (tSize==58) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 58>(aAnlm);
    if (tSize==59) {return rSubMnlm;} rSubMnlm += mplusMnlmSubSub_<LMAX, CGIDX, 59>(aAnlm);
    return rSubMnlm;
}
template <jint LMAX>
static void mplusMnlm_(jdouble *rMnlm, jdouble *aAnlm) noexcept {
    rMnlm[0] += mplusMnlmSub_<LMAX, 0>(aAnlm);
    if (LMAX==0) return;
    rMnlm[1] += mplusMnlmSub_<LMAX, 1>(aAnlm);
    rMnlm[2] += mplusMnlmSub_<LMAX, 2>(aAnlm);
    rMnlm[3] += mplusMnlmSub_<LMAX, 3>(aAnlm);
    if (LMAX==1) return;
    rMnlm[4] += mplusMnlmSub_<LMAX, 4>(aAnlm);
    rMnlm[5] += mplusMnlmSub_<LMAX, 5>(aAnlm);
    rMnlm[6] += mplusMnlmSub_<LMAX, 6>(aAnlm);
    rMnlm[7] += mplusMnlmSub_<LMAX, 7>(aAnlm);
    rMnlm[8] += mplusMnlmSub_<LMAX, 8>(aAnlm);
    if (LMAX==2) return;
    rMnlm[9] += mplusMnlmSub_<LMAX, 9>(aAnlm);
    rMnlm[10] += mplusMnlmSub_<LMAX, 10>(aAnlm);
    rMnlm[11] += mplusMnlmSub_<LMAX, 11>(aAnlm);
    rMnlm[12] += mplusMnlmSub_<LMAX, 12>(aAnlm);
    rMnlm[13] += mplusMnlmSub_<LMAX, 13>(aAnlm);
    rMnlm[14] += mplusMnlmSub_<LMAX, 14>(aAnlm);
    rMnlm[15] += mplusMnlmSub_<LMAX, 15>(aAnlm);
    if (LMAX==3) return;
    rMnlm[16] += mplusMnlmSub_<LMAX, 16>(aAnlm);
    rMnlm[17] += mplusMnlmSub_<LMAX, 17>(aAnlm);
    rMnlm[18] += mplusMnlmSub_<LMAX, 18>(aAnlm);
    rMnlm[19] += mplusMnlmSub_<LMAX, 19>(aAnlm);
    rMnlm[20] += mplusMnlmSub_<LMAX, 20>(aAnlm);
    rMnlm[21] += mplusMnlmSub_<LMAX, 21>(aAnlm);
    rMnlm[22] += mplusMnlmSub_<LMAX, 22>(aAnlm);
    rMnlm[23] += mplusMnlmSub_<LMAX, 23>(aAnlm);
    rMnlm[24] += mplusMnlmSub_<LMAX, 24>(aAnlm);
    if (LMAX==4) return;
    rMnlm[25] += mplusMnlmSub_<LMAX, 25>(aAnlm);
    rMnlm[26] += mplusMnlmSub_<LMAX, 26>(aAnlm);
    rMnlm[27] += mplusMnlmSub_<LMAX, 27>(aAnlm);
    rMnlm[28] += mplusMnlmSub_<LMAX, 28>(aAnlm);
    rMnlm[29] += mplusMnlmSub_<LMAX, 29>(aAnlm);
    rMnlm[30] += mplusMnlmSub_<LMAX, 30>(aAnlm);
    rMnlm[31] += mplusMnlmSub_<LMAX, 31>(aAnlm);
    rMnlm[32] += mplusMnlmSub_<LMAX, 32>(aAnlm);
    rMnlm[33] += mplusMnlmSub_<LMAX, 33>(aAnlm);
    rMnlm[34] += mplusMnlmSub_<LMAX, 34>(aAnlm);
    rMnlm[35] += mplusMnlmSub_<LMAX, 35>(aAnlm);
}
template <jint LMAX>
static void mplusMnlm(jdouble *rMnlm, jdouble *aAnlm, jint aEquSize) noexcept {
    constexpr jint tLMAll = (LMAX+1)*(LMAX+1);
    jdouble *tMnlm = rMnlm;
    jdouble *tAnlm = aAnlm;
    for (jint np = 0; np < aEquSize; ++np) {
        mplusMnlm_<LMAX>(tMnlm, tAnlm);
        tMnlm += tLMAll;
        tAnlm += tLMAll;
    }
}
static void mplusMnlm(jdouble *rMnlm, jdouble *aAnlm, jint aEquSize, jint aLMax) noexcept {
    switch (aLMax) {
    case 0: {mplusMnlm<0>(rMnlm, aAnlm, aEquSize); return;}
    case 1: {mplusMnlm<1>(rMnlm, aAnlm, aEquSize); return;}
    case 2: {mplusMnlm<2>(rMnlm, aAnlm, aEquSize); return;}
    case 3: {mplusMnlm<3>(rMnlm, aAnlm, aEquSize); return;}
    case 4: {mplusMnlm<4>(rMnlm, aAnlm, aEquSize); return;}
    case 5: {mplusMnlm<5>(rMnlm, aAnlm, aEquSize); return;}
    default: {return;}
    }
}

template <jint LMAX, jint CGIDX, jint SUBIDX>
static inline void mplusGradMnlmSubSub_(jdouble aSubGradMnlm, jdouble *aAnlm, jdouble *rGradAnlm) noexcept {
    constexpr jint i1 = CG_INDEX1[LMAX][CGIDX][SUBIDX];
    constexpr jint i2 = CG_INDEX2[LMAX][CGIDX][SUBIDX];
    constexpr jdouble coeff = CG_COEFF[LMAX][CGIDX][SUBIDX];
    const jdouble tMul = coeff * aSubGradMnlm;
    rGradAnlm[i1] += tMul * aAnlm[i2];
    rGradAnlm[i2] += tMul * aAnlm[i1];
}
template <jint LMAX, jint CGIDX>
static void mplusGradMnlmSub_(jdouble aSubGradMnlm, jdouble *aAnlm, jdouble *rGradAnlm) noexcept {
    constexpr jint tSize = CG_SIZE[LMAX][CGIDX];
    if (tSize==0) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 0>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==1) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 1>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==2) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 2>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==3) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 3>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==4) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 4>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==5) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 5>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==6) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 6>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==7) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 7>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==8) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 8>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==9) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 9>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==10) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 10>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==11) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 11>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==12) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 12>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==13) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 13>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==14) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 14>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==15) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 15>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==16) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 16>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==17) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 17>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==18) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 18>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==19) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 19>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==20) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 20>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==21) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 21>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==22) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 22>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==23) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 23>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==24) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 24>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==25) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 25>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==26) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 26>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==27) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 27>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==28) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 28>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==29) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 29>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==30) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 30>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==31) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 31>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==32) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 32>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==33) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 33>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==34) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 34>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==35) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 35>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==36) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 36>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==37) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 37>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==38) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 38>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==39) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 39>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==40) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 40>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==41) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 41>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==42) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 42>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==43) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 43>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==44) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 44>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==45) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 45>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==46) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 46>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==47) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 47>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==48) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 48>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==49) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 49>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==50) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 50>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==51) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 51>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==52) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 52>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==53) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 53>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==54) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 54>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==55) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 55>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==56) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 56>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==57) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 57>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==58) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 58>(aSubGradMnlm, aAnlm, rGradAnlm);
    if (tSize==59) {return;} mplusGradMnlmSubSub_<LMAX, CGIDX, 59>(aSubGradMnlm, aAnlm, rGradAnlm);
}
template <jint LMAX>
static void mplusGradMnlm_(jdouble *aGradMnlm, jdouble *aAnlm, jdouble *rGradAnlm) noexcept {
    mplusGradMnlmSub_<LMAX, 0>(aGradMnlm[0], aAnlm, rGradAnlm);
    if (LMAX==0) return;
    mplusGradMnlmSub_<LMAX, 1>(aGradMnlm[1], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 2>(aGradMnlm[2], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 3>(aGradMnlm[3], aAnlm, rGradAnlm);
    if (LMAX==1) return;
    mplusGradMnlmSub_<LMAX, 4>(aGradMnlm[4], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 5>(aGradMnlm[5], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 6>(aGradMnlm[6], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 7>(aGradMnlm[7], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 8>(aGradMnlm[8], aAnlm, rGradAnlm);
    if (LMAX==2) return;
    mplusGradMnlmSub_<LMAX, 9>(aGradMnlm[9], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 10>(aGradMnlm[10], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 11>(aGradMnlm[11], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 12>(aGradMnlm[12], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 13>(aGradMnlm[13], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 14>(aGradMnlm[14], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 15>(aGradMnlm[15], aAnlm, rGradAnlm);
    if (LMAX==3) return;
    mplusGradMnlmSub_<LMAX, 16>(aGradMnlm[16], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 17>(aGradMnlm[17], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 18>(aGradMnlm[18], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 19>(aGradMnlm[19], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 20>(aGradMnlm[20], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 21>(aGradMnlm[21], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 22>(aGradMnlm[22], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 23>(aGradMnlm[23], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 24>(aGradMnlm[24], aAnlm, rGradAnlm);
    if (LMAX==4) return;
    mplusGradMnlmSub_<LMAX, 25>(aGradMnlm[25], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 26>(aGradMnlm[26], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 27>(aGradMnlm[27], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 28>(aGradMnlm[28], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 29>(aGradMnlm[29], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 30>(aGradMnlm[30], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 31>(aGradMnlm[31], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 32>(aGradMnlm[32], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 33>(aGradMnlm[33], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 34>(aGradMnlm[34], aAnlm, rGradAnlm);
    mplusGradMnlmSub_<LMAX, 35>(aGradMnlm[35], aAnlm, rGradAnlm);
}
template <jint LMAX>
static void mplusGradMnlm(jdouble *aGradMnlm, jdouble *aAnlm, jdouble *rGradAnlm, jint aEquSize) noexcept {
    constexpr jint tLMAll = (LMAX+1)*(LMAX+1);
    jdouble *tGradMnlm = aGradMnlm;
    jdouble *tGradAnlm = rGradAnlm;
    jdouble *tAnlm = aAnlm;
    for (jint np = 0; np < aEquSize; ++np) {
        mplusGradMnlm_<LMAX>(tGradMnlm, tAnlm, tGradAnlm);
        tGradMnlm += tLMAll;
        tGradAnlm += tLMAll;
        tAnlm += tLMAll;
    }
}
static void mplusGradMnlm(jdouble *aGradMnlm, jdouble *aAnlm, jdouble *rGradAnlm, jint aEquSize, jint aLMax) noexcept {
    switch (aLMax) {
    case 0: {mplusGradMnlm<0>(aGradMnlm, aAnlm, rGradAnlm, aEquSize); return;}
    case 1: {mplusGradMnlm<1>(aGradMnlm, aAnlm, rGradAnlm, aEquSize); return;}
    case 2: {mplusGradMnlm<2>(aGradMnlm, aAnlm, rGradAnlm, aEquSize); return;}
    case 3: {mplusGradMnlm<3>(aGradMnlm, aAnlm, rGradAnlm, aEquSize); return;}
    case 4: {mplusGradMnlm<4>(aGradMnlm, aAnlm, rGradAnlm, aEquSize); return;}
    case 5: {mplusGradMnlm<5>(aGradMnlm, aAnlm, rGradAnlm, aEquSize); return;}
    default: {return;}
    }
}

template <jint LMAX, jint CGIDX, jint SUBIDX>
static inline jdouble mplusGradNNGradMnlmSubSub_(jdouble *aAnlm, jdouble *aGradNNGradAnlm) noexcept {
    constexpr jint i1 = CG_INDEX1[LMAX][CGIDX][SUBIDX];
    constexpr jint i2 = CG_INDEX2[LMAX][CGIDX][SUBIDX];
    constexpr jdouble coeff = CG_COEFF[LMAX][CGIDX][SUBIDX];
    return coeff * (aAnlm[i2]*aGradNNGradAnlm[i1] + aAnlm[i1]*aGradNNGradAnlm[i2]);
}
template <jint LMAX, jint CGIDX>
static jdouble mplusGradNNGradMnlmSub_(jdouble *aAnlm, jdouble *aGradNNGradAnlm) noexcept {
    constexpr jint tSize = CG_SIZE[LMAX][CGIDX];
    jdouble rSubGGMnlm = 0.0;
    if (tSize==0) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 0>(aAnlm, aGradNNGradAnlm);
    if (tSize==1) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 1>(aAnlm, aGradNNGradAnlm);
    if (tSize==2) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 2>(aAnlm, aGradNNGradAnlm);
    if (tSize==3) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 3>(aAnlm, aGradNNGradAnlm);
    if (tSize==4) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 4>(aAnlm, aGradNNGradAnlm);
    if (tSize==5) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 5>(aAnlm, aGradNNGradAnlm);
    if (tSize==6) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 6>(aAnlm, aGradNNGradAnlm);
    if (tSize==7) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 7>(aAnlm, aGradNNGradAnlm);
    if (tSize==8) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 8>(aAnlm, aGradNNGradAnlm);
    if (tSize==9) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 9>(aAnlm, aGradNNGradAnlm);
    if (tSize==10) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 10>(aAnlm, aGradNNGradAnlm);
    if (tSize==11) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 11>(aAnlm, aGradNNGradAnlm);
    if (tSize==12) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 12>(aAnlm, aGradNNGradAnlm);
    if (tSize==13) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 13>(aAnlm, aGradNNGradAnlm);
    if (tSize==14) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 14>(aAnlm, aGradNNGradAnlm);
    if (tSize==15) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 15>(aAnlm, aGradNNGradAnlm);
    if (tSize==16) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 16>(aAnlm, aGradNNGradAnlm);
    if (tSize==17) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 17>(aAnlm, aGradNNGradAnlm);
    if (tSize==18) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 18>(aAnlm, aGradNNGradAnlm);
    if (tSize==19) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 19>(aAnlm, aGradNNGradAnlm);
    if (tSize==20) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 20>(aAnlm, aGradNNGradAnlm);
    if (tSize==21) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 21>(aAnlm, aGradNNGradAnlm);
    if (tSize==22) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 22>(aAnlm, aGradNNGradAnlm);
    if (tSize==23) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 23>(aAnlm, aGradNNGradAnlm);
    if (tSize==24) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 24>(aAnlm, aGradNNGradAnlm);
    if (tSize==25) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 25>(aAnlm, aGradNNGradAnlm);
    if (tSize==26) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 26>(aAnlm, aGradNNGradAnlm);
    if (tSize==27) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 27>(aAnlm, aGradNNGradAnlm);
    if (tSize==28) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 28>(aAnlm, aGradNNGradAnlm);
    if (tSize==29) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 29>(aAnlm, aGradNNGradAnlm);
    if (tSize==30) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 30>(aAnlm, aGradNNGradAnlm);
    if (tSize==31) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 31>(aAnlm, aGradNNGradAnlm);
    if (tSize==32) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 32>(aAnlm, aGradNNGradAnlm);
    if (tSize==33) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 33>(aAnlm, aGradNNGradAnlm);
    if (tSize==34) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 34>(aAnlm, aGradNNGradAnlm);
    if (tSize==35) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 35>(aAnlm, aGradNNGradAnlm);
    if (tSize==36) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 36>(aAnlm, aGradNNGradAnlm);
    if (tSize==37) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 37>(aAnlm, aGradNNGradAnlm);
    if (tSize==38) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 38>(aAnlm, aGradNNGradAnlm);
    if (tSize==39) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 39>(aAnlm, aGradNNGradAnlm);
    if (tSize==40) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 40>(aAnlm, aGradNNGradAnlm);
    if (tSize==41) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 41>(aAnlm, aGradNNGradAnlm);
    if (tSize==42) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 42>(aAnlm, aGradNNGradAnlm);
    if (tSize==43) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 43>(aAnlm, aGradNNGradAnlm);
    if (tSize==44) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 44>(aAnlm, aGradNNGradAnlm);
    if (tSize==45) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 45>(aAnlm, aGradNNGradAnlm);
    if (tSize==46) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 46>(aAnlm, aGradNNGradAnlm);
    if (tSize==47) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 47>(aAnlm, aGradNNGradAnlm);
    if (tSize==48) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 48>(aAnlm, aGradNNGradAnlm);
    if (tSize==49) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 49>(aAnlm, aGradNNGradAnlm);
    if (tSize==50) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 50>(aAnlm, aGradNNGradAnlm);
    if (tSize==51) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 51>(aAnlm, aGradNNGradAnlm);
    if (tSize==52) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 52>(aAnlm, aGradNNGradAnlm);
    if (tSize==53) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 53>(aAnlm, aGradNNGradAnlm);
    if (tSize==54) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 54>(aAnlm, aGradNNGradAnlm);
    if (tSize==55) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 55>(aAnlm, aGradNNGradAnlm);
    if (tSize==56) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 56>(aAnlm, aGradNNGradAnlm);
    if (tSize==57) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 57>(aAnlm, aGradNNGradAnlm);
    if (tSize==58) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 58>(aAnlm, aGradNNGradAnlm);
    if (tSize==59) {return rSubGGMnlm;} rSubGGMnlm += mplusGradNNGradMnlmSubSub_<LMAX, CGIDX, 59>(aAnlm, aGradNNGradAnlm);
    return rSubGGMnlm;
}
template <jint LMAX>
static void mplusGradNNGradMnlm_(jdouble *rGradNNGradMnlm, jdouble *aAnlm, jdouble *aGradNNGradAnlm) noexcept {
    rGradNNGradMnlm[0] += mplusGradNNGradMnlmSub_<LMAX, 0>(aAnlm, aGradNNGradAnlm);
    if (LMAX==0) return;
    rGradNNGradMnlm[1] += mplusGradNNGradMnlmSub_<LMAX, 1>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[2] += mplusGradNNGradMnlmSub_<LMAX, 2>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[3] += mplusGradNNGradMnlmSub_<LMAX, 3>(aAnlm, aGradNNGradAnlm);
    if (LMAX==1) return;
    rGradNNGradMnlm[4] += mplusGradNNGradMnlmSub_<LMAX, 4>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[5] += mplusGradNNGradMnlmSub_<LMAX, 5>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[6] += mplusGradNNGradMnlmSub_<LMAX, 6>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[7] += mplusGradNNGradMnlmSub_<LMAX, 7>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[8] += mplusGradNNGradMnlmSub_<LMAX, 8>(aAnlm, aGradNNGradAnlm);
    if (LMAX==2) return;
    rGradNNGradMnlm[9] += mplusGradNNGradMnlmSub_<LMAX, 9>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[10] += mplusGradNNGradMnlmSub_<LMAX, 10>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[11] += mplusGradNNGradMnlmSub_<LMAX, 11>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[12] += mplusGradNNGradMnlmSub_<LMAX, 12>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[13] += mplusGradNNGradMnlmSub_<LMAX, 13>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[14] += mplusGradNNGradMnlmSub_<LMAX, 14>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[15] += mplusGradNNGradMnlmSub_<LMAX, 15>(aAnlm, aGradNNGradAnlm);
    if (LMAX==3) return;
    rGradNNGradMnlm[16] += mplusGradNNGradMnlmSub_<LMAX, 16>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[17] += mplusGradNNGradMnlmSub_<LMAX, 17>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[18] += mplusGradNNGradMnlmSub_<LMAX, 18>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[19] += mplusGradNNGradMnlmSub_<LMAX, 19>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[20] += mplusGradNNGradMnlmSub_<LMAX, 20>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[21] += mplusGradNNGradMnlmSub_<LMAX, 21>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[22] += mplusGradNNGradMnlmSub_<LMAX, 22>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[23] += mplusGradNNGradMnlmSub_<LMAX, 23>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[24] += mplusGradNNGradMnlmSub_<LMAX, 24>(aAnlm, aGradNNGradAnlm);
    if (LMAX==4) return;
    rGradNNGradMnlm[25] += mplusGradNNGradMnlmSub_<LMAX, 25>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[26] += mplusGradNNGradMnlmSub_<LMAX, 26>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[27] += mplusGradNNGradMnlmSub_<LMAX, 27>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[28] += mplusGradNNGradMnlmSub_<LMAX, 28>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[29] += mplusGradNNGradMnlmSub_<LMAX, 29>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[30] += mplusGradNNGradMnlmSub_<LMAX, 30>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[31] += mplusGradNNGradMnlmSub_<LMAX, 31>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[32] += mplusGradNNGradMnlmSub_<LMAX, 32>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[33] += mplusGradNNGradMnlmSub_<LMAX, 33>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[34] += mplusGradNNGradMnlmSub_<LMAX, 34>(aAnlm, aGradNNGradAnlm);
    rGradNNGradMnlm[35] += mplusGradNNGradMnlmSub_<LMAX, 35>(aAnlm, aGradNNGradAnlm);
}
template <jint LMAX>
static void mplusGradNNGradMnlm(jdouble *rGradNNGradMnlm, jdouble *aAnlm, jdouble *aGradNNGradAnlm, jint aEquSize) noexcept {
    constexpr jint tLMAll = (LMAX+1)*(LMAX+1);
    jdouble *tGradNNGradMnlm = rGradNNGradMnlm;
    jdouble *tGradNNGradAnlm = aGradNNGradAnlm;
    jdouble *tAnlm = aAnlm;
    for (jint np = 0; np < aEquSize; ++np) {
        mplusGradNNGradMnlm_<LMAX>(tGradNNGradMnlm, tAnlm, tGradNNGradAnlm);
        tGradNNGradMnlm += tLMAll;
        tGradNNGradAnlm += tLMAll;
        tAnlm += tLMAll;
    }
}
static void mplusGradNNGradMnlm(jdouble *rGradNNGradMnlm, jdouble *aAnlm, jdouble *aGradNNGradAnlm, jint aEquSize, jint aLMax) noexcept {
    switch (aLMax) {
    case 0: {mplusGradNNGradMnlm<0>(rGradNNGradMnlm, aAnlm, aGradNNGradAnlm, aEquSize); return;}
    case 1: {mplusGradNNGradMnlm<1>(rGradNNGradMnlm, aAnlm, aGradNNGradAnlm, aEquSize); return;}
    case 2: {mplusGradNNGradMnlm<2>(rGradNNGradMnlm, aAnlm, aGradNNGradAnlm, aEquSize); return;}
    case 3: {mplusGradNNGradMnlm<3>(rGradNNGradMnlm, aAnlm, aGradNNGradAnlm, aEquSize); return;}
    case 4: {mplusGradNNGradMnlm<4>(rGradNNGradMnlm, aAnlm, aGradNNGradAnlm, aEquSize); return;}
    case 5: {mplusGradNNGradMnlm<5>(rGradNNGradMnlm, aAnlm, aGradNNGradAnlm, aEquSize); return;}
    default: {return;}
    }
}

template <jint LMAX>
static void mplusGradMnlmAnlm(jdouble *aNNGradMnlm, jdouble *rGradAnlm, jdouble *aGradNNGradAnlm, jint aEquSize) noexcept {
    constexpr jint tLMAll = (LMAX+1)*(LMAX+1);
    jdouble *tNNGradMnlm = aNNGradMnlm;
    jdouble *tGradNNGradAnlm = aGradNNGradAnlm;
    jdouble *tGradAnlm = rGradAnlm;
    for (jint np = 0; np < aEquSize; ++np) {
        mplusGradMnlm_<LMAX>(tNNGradMnlm, tGradNNGradAnlm, tGradAnlm);
        tNNGradMnlm += tLMAll;
        tGradNNGradAnlm += tLMAll;
        tGradAnlm += tLMAll;
    }
}
static void mplusGradMnlmAnlm(jdouble *aNNGradMnlm, jdouble *rGradAnlm, jdouble *aGradNNGradAnlm, jint aEquSize, jint aLMax) noexcept {
    switch (aLMax) {
    case 0: {mplusGradMnlmAnlm<0>(aNNGradMnlm, rGradAnlm, aGradNNGradAnlm, aEquSize); return;}
    case 1: {mplusGradMnlmAnlm<1>(aNNGradMnlm, rGradAnlm, aGradNNGradAnlm, aEquSize); return;}
    case 2: {mplusGradMnlmAnlm<2>(aNNGradMnlm, rGradAnlm, aGradNNGradAnlm, aEquSize); return;}
    case 3: {mplusGradMnlmAnlm<3>(aNNGradMnlm, rGradAnlm, aGradNNGradAnlm, aEquSize); return;}
    case 4: {mplusGradMnlmAnlm<4>(aNNGradMnlm, rGradAnlm, aGradNNGradAnlm, aEquSize); return;}
    case 5: {mplusGradMnlmAnlm<5>(aNNGradMnlm, rGradAnlm, aGradNNGradAnlm, aEquSize); return;}
    default: {return;}
    }
}

}

#endif //BASIS_EQUIVARIANT_UTIL_H