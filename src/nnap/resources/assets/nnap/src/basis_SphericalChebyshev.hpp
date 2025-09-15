#ifndef BASIS_SPHERICAL_CHEBYSHEV_H
#define BASIS_SPHERICAL_CHEBYSHEV_H

#include "basis_SphericalUtil.hpp"

namespace JSE_NNAP {

template <jint LMAXMAX, jint LMALL, jint WTYPE, jboolean FULL_CACHE>
static void calCnlm(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                    jdouble *rCnlm, jdouble *rForwardCache,
                    jint aTypeNum, jdouble aRCut, jint aNMax, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    const jint tSizeBnlm = (aNMax+1)*LMALL;
    // init cache
    jdouble *rRn = NULL, *rY = NULL;
    jdouble *rBnlm = NULL;
    jdouble *rNlRn = NULL, *rNlFc = NULL, *rNlY = NULL;
    jdouble *rNlBnlm = NULL;
    if (FULL_CACHE) {
        rNlRn = rForwardCache;
        rNlFc = rNlRn + aNN*(aNMax+1);
        rNlY = rNlFc + aNN;
    } else {
        rRn = rForwardCache;
        rY = rRn + (aNMax+1);
    }
    if (WTYPE==WTYPE_FUSE) {
        if (FULL_CACHE) {
            rNlBnlm = rNlY + aNN*LMALL;
        } else {
            rBnlm = rY + LMALL;
        }
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
        calRn(rRn, aNMax, dis, aRCut);
        // cal Y
        if (FULL_CACHE) rY = rNlY + j*LMALL;
        realSphericalHarmonicsFull4<LMAXMAX>(dx, dy, dz, dis, rY);
        // cal cnlm
        if (WTYPE==WTYPE_FUSE) {
            // cal bnlm
            if (FULL_CACHE) rBnlm = rNlBnlm + j*tSizeBnlm;
            calBnlm<LMALL>(rBnlm, rY, fc, rRn, aNMax);
            // mplus2cnlm
            jdouble *tFuseWeight = aFuseWeight;
            jdouble *tCnlm = rCnlm;
            for (jint k = 0; k < aFuseSize; ++k) {
                jdouble wt = tFuseWeight[type-1];
                mplusBnlm2Cnlm<LMALL>(tCnlm, rBnlm, wt, aNMax);
                tFuseWeight += aTypeNum;
                tCnlm += tSizeBnlm;
            }
        } else
        if (WTYPE==WTYPE_NONE) {
            mplusCnlm<LMALL>(rCnlm, rY, fc, rRn, aNMax);
        } else
        if (WTYPE==WTYPE_FULL) {
            jdouble *tCnlm = rCnlm + tSizeBnlm*(type-1);
            mplusCnlm<LMALL>(tCnlm, rY, fc, rRn, aNMax);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            jdouble *tCnlmWt = rCnlm + tSizeBnlm*type;
            mplusCnlmWt<LMALL>(rCnlm, tCnlmWt, rY, fc, rRn, 1.0, aNMax);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            jdouble wt = ((type&1)==1) ? type : -type;
            jdouble *tCnlmWt = rCnlm + tSizeBnlm;
            mplusCnlmWt<LMALL>(rCnlm, tCnlmWt, rY, fc, rRn, wt, aNMax);
        }
    }
}

template <jint LMAX, jboolean NO_RADIAL, jint L3MAX, jboolean L3CROSS, jint L4MAX, jboolean L4CROSS, jint WTYPE>
static inline void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN, jdouble *rFp,
                         jdouble *rForwardCache, jboolean aFullCache,
                         jint aTypeNum, jdouble aRCut, jint aNMax, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    // const init
    jint tSizeN;
    switch(WTYPE) {
    case WTYPE_EXFULL: {
        tSizeN = (aTypeNum+1)*(aNMax+1);
        break;
    }
    case WTYPE_FULL: {
        tSizeN = aTypeNum*(aNMax+1);
        break;
    }
    case WTYPE_NONE: {
        tSizeN = aNMax+1;
        break;
    }
    case WTYPE_DEFAULT: {
        tSizeN = (aNMax+aNMax+2);
        break;
    }
    case WTYPE_FUSE: {
        tSizeN = aFuseSize*(aNMax+1);
        break;
    }
    default: {
        tSizeN = 0;
        break;
    }}
    constexpr jint tSizeL = (NO_RADIAL?LMAX:(LMAX+1)) + (L3CROSS?L3NCOLS:L3NCOLS_NOCROSS)[L3MAX] + (L4CROSS?L4NCOLS:L4NCOLS_NOCROSS)[L4MAX];
    constexpr jint tLMaxMax = LMAX>L3MAX ? (LMAX>L4MAX?LMAX:L4MAX) : (L3MAX>L4MAX?L3MAX:L4MAX);
    constexpr jint tLMAll = (tLMaxMax+1)*(tLMaxMax+1);
    const jint tSizeCnlm = tSizeN*tLMAll;
    // init cache
    jdouble *rCnlm = rForwardCache;
    jdouble *rForwardCacheElse = rCnlm + tSizeCnlm;
    // clear cnlm first
    for (jint i = 0; i < tSizeCnlm; ++i) {
        rCnlm[i] = 0.0;
    }
    // do cal
    if (aFullCache) {
        calCnlm<tLMaxMax, tLMAll, WTYPE, JNI_TRUE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rCnlm, rForwardCacheElse, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
    } else {
        calCnlm<tLMaxMax, tLMAll, WTYPE, JNI_FALSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rCnlm, rForwardCacheElse, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
    }
    constexpr jint tShiftL3 = NO_RADIAL?LMAX:(LMAX+1);
    constexpr jint tShiftL4 = tShiftL3 + (L3CROSS?L3NCOLS:L3NCOLS_NOCROSS)[L3MAX];
    for (jint n=0, tShift=0, tShiftFp=0; n<tSizeN; ++n, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calL2_<LMAX, NO_RADIAL>(rCnlm+tShift, rFp+tShiftFp);
        calL3_<L3MAX, L3CROSS>(rCnlm+tShift, rFp+tShiftFp+tShiftL3);
        calL4_<L4MAX, L4CROSS>(rCnlm+tShift, rFp+tShiftFp+tShiftL4);
    }
}

template <jint LMALL, jint WTYPE>
static void calBackwardMainLoop(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                                jdouble *rGradPara, jdouble *aNlBnlm,  jdouble *aGradCnlm,
                                jint aTypeNum, jdouble aRCut, jint aNMax, jint aFuseSize) {
    const jint tSizeBnlm = (aNMax+1)*LMALL;
    for (jint j = 0; j < aNN; ++j) {
        jint type = aNlType[j];
        jdouble dx = aNlDx[j], dy = aNlDy[j], dz = aNlDz[j];
        jdouble dis = sqrt((double)(dx*dx + dy*dy + dz*dz));
        // check rcut for merge
        if (dis >= aRCut) continue;
        jdouble *tBnlm = aNlBnlm + j*tSizeBnlm;
        if (WTYPE==WTYPE_FUSE) {
            jdouble *tGradPara = rGradPara;
            jdouble *tGradCnlm = aGradCnlm;
            for (jint fi = 0; fi < aFuseSize; ++fi) {
                tGradPara[type-1] += dotBnlmGradCnlm<LMALL>(tBnlm, tGradCnlm, aNMax);
                tGradPara += aTypeNum;
                tGradCnlm += tSizeBnlm;
            }
            continue;
        }
    }
}
template <jint LMAX, jboolean NO_RADIAL, jint L3MAX, jboolean L3CROSS, jint L4MAX, jboolean L4CROSS, jint WTYPE>
static void calBackward(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                        jdouble *aGradFp, jdouble *rGradPara, jdouble *aForwardCache, jdouble *rBackwardCache,
                        jint aTypeNum, jdouble aRCut, jint aNMax, jint aFuseSize) noexcept {
    static_assert(WTYPE!=WTYPE_DEFAULT &&
                  WTYPE!=WTYPE_NONE &&
                  WTYPE!=WTYPE_FULL &&
                  WTYPE!=WTYPE_EXFULL, "WTYPE INVALID");
    // const init
    jint tSizeN;
    if (WTYPE==WTYPE_FUSE) {
        tSizeN = aFuseSize*(aNMax+1);
    } else {
        tSizeN = 0;
    }
    constexpr jint tSizeL = (NO_RADIAL?LMAX:(LMAX+1)) + (L3CROSS?L3NCOLS:L3NCOLS_NOCROSS)[L3MAX] + (L4CROSS?L4NCOLS:L4NCOLS_NOCROSS)[L4MAX];
    constexpr jint tLMaxMax = LMAX>L3MAX ? (LMAX>L4MAX?LMAX:L4MAX) : (L3MAX>L4MAX?L3MAX:L4MAX);
    constexpr jint tLMAll = (tLMaxMax+1)*(tLMaxMax+1);
    const jint tSizeCnlm = tSizeN*tLMAll;
    // init cache
    jdouble *tCnlm = aForwardCache;
    jdouble *tNlBnlm = tCnlm + tSizeCnlm + aNN*(aNMax+1 + 1 + tLMAll);
    jdouble *rGradCnlm = rBackwardCache;
    // cal grad cnlm
    constexpr jint tShiftL3 = NO_RADIAL?LMAX:(LMAX+1);
    constexpr jint tShiftL4 = tShiftL3 + (L3CROSS?L3NCOLS:L3NCOLS_NOCROSS)[L3MAX];
    for (jint n=0, tShift=0, tShiftFp=0; n<tSizeN; ++n, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calGradL2_<LMAX, NO_RADIAL>(tCnlm+tShift, rGradCnlm+tShift, aGradFp+tShiftFp);
        calGradL3_<L3MAX, L3CROSS>(tCnlm+tShift, rGradCnlm+tShift, aGradFp+tShiftFp+tShiftL3);
        calGradL4_<L4MAX, L4CROSS>(tCnlm+tShift, rGradCnlm+tShift, aGradFp+tShiftFp+tShiftL4);
    }
    // plus to para
    calBackwardMainLoop<tLMAll, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rGradPara, tNlBnlm, rGradCnlm, aTypeNum, aRCut, aNMax, aFuseSize);
}

template <jint LMAXMAX, jint LMALL, jint WTYPE, jboolean FULL_CACHE>
static void calForceMainLoop(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                             jdouble *aGradCnlm, jdouble *rFx, jdouble *rFy, jdouble *rFz,
                             jdouble *aForwardCache, jdouble *rForwardForceCache,
                             jint aTypeNum, jdouble aRCut, jint aNMax, jdouble *aFuseWeight, jint aFuseSize) {
    const jint tSizeBnlm = (aNMax+1)*LMALL;
    // init cache
    jdouble *tNlRn = aForwardCache;
    jdouble *tNlFc = tNlRn + aNN*(aNMax+1);
    jdouble *tNlY = tNlFc + aNN;
    jdouble *rRnPx = NULL, *rRnPy = NULL, *rRnPz = NULL, *rCheby2 = NULL;
    jdouble *rYPx = NULL, *rYPy = NULL, *rYPz = NULL, *rYPtheta = NULL, *rYPphi = NULL;
    jdouble *rGradBnlm = NULL;
    jdouble *rNlRnPx = NULL, *rNlRnPy = NULL, *rNlRnPz = NULL;
    jdouble *rNlFcPx = NULL, *rNlFcPy = NULL, *rNlFcPz = NULL;
    jdouble *rNlYPx = NULL, *rNlYPy = NULL, *rNlYPz = NULL;
    jdouble *rNlGradBnlm = NULL;
    if (FULL_CACHE) {
        rNlRnPx = rForwardForceCache;
        rNlRnPy = rNlRnPx + aNN*(aNMax+1);
        rNlRnPz = rNlRnPy + aNN*(aNMax+1);
        rNlFcPx = rNlRnPz + aNN*(aNMax+1);
        rNlFcPy = rNlFcPx + aNN;
        rNlFcPz = rNlFcPy + aNN;
        rNlYPx = rNlFcPz + aNN;
        rNlYPy = rNlYPx + aNN*LMALL;
        rNlYPz = rNlYPy + aNN*LMALL;
        rYPtheta = rNlYPz + aNN*LMALL;
        rYPphi = rYPtheta + LMALL;
        rCheby2 = rYPphi + LMALL;
    } else {
        rRnPx = rForwardForceCache;
        rRnPy = rRnPx + (aNMax+1);
        rRnPz = rRnPy + (aNMax+1);
        rYPx = rRnPz + (aNMax+1);
        rYPy = rYPx + LMALL;
        rYPz = rYPy + LMALL;
        rYPtheta = rYPz + LMALL;
        rYPphi = rYPtheta + LMALL;
        rCheby2 = rYPphi + LMALL;
    }
    if (WTYPE==WTYPE_FUSE) {
        if (FULL_CACHE) {
            rNlGradBnlm = rCheby2 + (aNMax+1);
        } else {
            rGradBnlm = rCheby2 + (aNMax+1);
        }
    }
    // loop for neighbor
    for (jint j = 0; j < aNN; ++j) {
        // init nl
        jint type = aNlType[j];
        jdouble dx = aNlDx[j], dy = aNlDy[j], dz = aNlDz[j];
        jdouble dis = sqrt((double)(dx*dx + dy*dy + dz*dz));
        // check rcut for merge
        if (dis >= aRCut) continue;
        // get fc Rn Y
        jdouble fc = tNlFc[j];
        jdouble *tRn = tNlRn + j*(aNMax+1);
        jdouble *tY = tNlY + j*LMALL;
        // cal fcPxyz
        jdouble fcPx, fcPy, fcPz;
        calFcPxyz(&fcPx, &fcPy, &fcPz, dis, aRCut, dx, dy, dz);
        if (FULL_CACHE) {
            rNlFcPx[j] = fcPx;
            rNlFcPy[j] = fcPy;
            rNlFcPz[j] = fcPz;
        }
        // cal RnPxyz
        if (FULL_CACHE) {
            rRnPx = rNlRnPx + j*(aNMax+1);
            rRnPy = rNlRnPy + j*(aNMax+1);
            rRnPz = rNlRnPz + j*(aNMax+1);
        }
        calRnPxyz(rRnPx, rRnPy, rRnPz, rCheby2, aNMax, dis, aRCut, dx, dy, dz);
        // cal Ylm
        if (FULL_CACHE) {
            rYPx = rNlYPx + j*LMALL;
            rYPy = rNlYPy + j*LMALL;
            rYPz = rNlYPz + j*LMALL;
        }
        calYPxyz<LMAXMAX, LMALL>(tY, dx, dy, dz, dis, rYPx, rYPy, rYPz, rYPtheta, rYPphi);
        // cal fxyz
        if (WTYPE==WTYPE_FUSE) {
            if (FULL_CACHE) {
                rGradBnlm = rNlGradBnlm + j*tSizeBnlm;
            }
            for (jint k = 0; k < tSizeBnlm; ++k) {
                rGradBnlm[k] = 0.0;
            }
            jdouble *tFuseWeight = aFuseWeight;
            jdouble *tGradCnlm = aGradCnlm;
            for (jint k = 0; k < aFuseSize; ++k) {
                jdouble wt = tFuseWeight[type-1];
                mplusBnlm2Cnlm<LMALL>(rGradBnlm, tGradCnlm, wt, aNMax);
                tFuseWeight += aTypeNum;
                tGradCnlm += tSizeBnlm;
            }
            gradBnlm2Fxyz<LMALL>(j, rGradBnlm, rYPtheta, tY, fc, tRn, aNMax, fcPx, fcPy, fcPz, rRnPx, rRnPy, rRnPz, rYPx, rYPy, rYPz, rFx, rFy, rFz);
        } else
        if (WTYPE==WTYPE_NONE) {
            gradBnlm2Fxyz<LMALL>(j, aGradCnlm, rYPtheta, tY, fc, tRn, aNMax, fcPx, fcPy, fcPz, rRnPx, rRnPy, rRnPz, rYPx, rYPy, rYPz, rFx, rFy, rFz);
        } else
        if (WTYPE==WTYPE_FULL) {
            jdouble *tGradBnlm = aGradCnlm + tSizeBnlm*(type-1);
            gradBnlm2Fxyz<LMALL>(j, tGradBnlm, rYPtheta, tY, fc, tRn, aNMax, fcPx, fcPy, fcPz, rRnPx, rRnPy, rRnPz, rYPx, rYPy, rYPz, rFx, rFy, rFz);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            jdouble *tGradCnlmWt = aGradCnlm + tSizeBnlm*type;
            gradCnlmWt2Fxyz<LMALL>(j, aGradCnlm, tGradCnlmWt, rYPtheta, tY, fc, tRn, 1.0, aNMax, fcPx, fcPy, fcPz, rRnPx, rRnPy, rRnPz, rYPx, rYPy, rYPz, rFx, rFy, rFz);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            jdouble wt = ((type&1)==1) ? type : -type;
            jdouble *tGradCnlmWt = aGradCnlm + tSizeBnlm;
            gradCnlmWt2Fxyz<LMALL>(j, aGradCnlm, tGradCnlmWt, rYPtheta, tY, fc, tRn, wt, aNMax, fcPx, fcPy, fcPz, rRnPx, rRnPy, rRnPz, rYPx, rYPy, rYPz, rFx, rFy, rFz);
        }
    }
}
template <jint LMAX, jboolean NO_RADIAL, jint L3MAX, jboolean L3CROSS, jint L4MAX, jboolean L4CROSS, jint WTYPE>
static void calForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                     jdouble *aNNGrad, jdouble *rFx, jdouble *rFy, jdouble *rFz,
                     jdouble *aForwardCache, jdouble *rForwardForceCache, jboolean aFullCache,
                     jint aTypeNum, jdouble aRCut, jint aNMax, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    // const init
    jint tSizeN;
    switch(WTYPE) {
    case WTYPE_EXFULL: {
        tSizeN = (aTypeNum+1)*(aNMax+1);
        break;
    }
    case WTYPE_FULL: {
        tSizeN = aTypeNum*(aNMax+1);
        break;
    }
    case WTYPE_NONE: {
        tSizeN = aNMax+1;
        break;
    }
    case WTYPE_DEFAULT: {
        tSizeN = (aNMax+aNMax+2);
        break;
    }
    case WTYPE_FUSE: {
        tSizeN = aFuseSize*(aNMax+1);
        break;
    }
    default: {
        tSizeN = 0;
        break;
    }}
    constexpr jint tSizeL = (NO_RADIAL?LMAX:(LMAX+1)) + (L3CROSS?L3NCOLS:L3NCOLS_NOCROSS)[L3MAX] + (L4CROSS?L4NCOLS:L4NCOLS_NOCROSS)[L4MAX];
    constexpr jint tLMaxMax = LMAX>L3MAX ? (LMAX>L4MAX?LMAX:L4MAX) : (L3MAX>L4MAX?L3MAX:L4MAX);
    constexpr jint tLMAll = (tLMaxMax+1)*(tLMaxMax+1);
    const jint tSizeCnlm = tSizeN*tLMAll;
    // init cache
    jdouble *tCnlm = aForwardCache;
    jdouble *tForwardCacheElse = tCnlm + tSizeCnlm;
    jdouble *rGradCnlm = rForwardForceCache;
    jdouble *rForwardForceCacheElse = rGradCnlm + tSizeCnlm;
    // forward need init gradCnlm here
    for (jint i = 0; i < tSizeCnlm; ++i) {
        rGradCnlm[i] = 0.0;
    }
    constexpr jint tShiftL3 = NO_RADIAL?LMAX:(LMAX+1);
    constexpr jint tShiftL4 = tShiftL3 + (L3CROSS?L3NCOLS:L3NCOLS_NOCROSS)[L3MAX];
    for (jint n=0, tShift=0, tShiftFp=0; n<tSizeN; ++n, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calGradL2_<LMAX, NO_RADIAL>(tCnlm+tShift, rGradCnlm+tShift, aNNGrad+tShiftFp);
        calGradL3_<L3MAX, L3CROSS>(tCnlm+tShift, rGradCnlm+tShift, aNNGrad+tShiftFp+tShiftL3);
        calGradL4_<L4MAX, L4CROSS>(tCnlm+tShift, rGradCnlm+tShift, aNNGrad+tShiftFp+tShiftL4);
    }
    if (aFullCache) {
        calForceMainLoop<tLMaxMax, tLMAll, WTYPE, JNI_TRUE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rGradCnlm, rFx, rFy, rFz, tForwardCacheElse, rForwardForceCacheElse, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
    } else {
        calForceMainLoop<tLMaxMax, tLMAll, WTYPE, JNI_FALSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rGradCnlm, rFx, rFy, rFz, tForwardCacheElse, rForwardForceCacheElse, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
    }
}

template <jint LMALL, jint WTYPE>
static void calBackwardForceMainLoop(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                                     jdouble *rGradNNGradCnlm, jdouble *aGradFx, jdouble *aGradFy, jdouble *aGradFz,
                                     jdouble *aNNGradCnlm, jdouble *rGradPara,
                                     jdouble *aForwardCache, jdouble *aForwardForceCache,
                                     jdouble *rBackwardForceCache, jboolean aFixBasis,
                                     jint aTypeNum, jdouble aRCut, jint aNMax, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    const jint tSizeBnlm = (aNMax+1)*LMALL;
    // init cache
    jdouble *tNlRn = aForwardCache;
    jdouble *tNlFc = tNlRn + aNN*(aNMax+1);
    jdouble *tNlY = tNlFc + aNN;
    jdouble *tNlRnPx = aForwardForceCache;
    jdouble *tNlRnPy = tNlRnPx + aNN*(aNMax+1);
    jdouble *tNlRnPz = tNlRnPy + aNN*(aNMax+1);
    jdouble *tNlFcPx = tNlRnPz + aNN*(aNMax+1);
    jdouble *tNlFcPy = tNlFcPx + aNN;
    jdouble *tNlFcPz = tNlFcPy + aNN;
    jdouble *tNlYPx = tNlFcPz + aNN;
    jdouble *tNlYPy = tNlYPx + aNN*LMALL;
    jdouble *tNlYPz = tNlYPy + aNN*LMALL;
    jdouble *rGradNNGradY = rBackwardForceCache;
    jdouble *rGradNNGradBnlm = NULL;
    if (WTYPE==WTYPE_FUSE) {
        rGradNNGradBnlm = rGradNNGradY + LMALL;
    }
    // loop for neighbor
    for (jint j = 0; j < aNN; ++j) {
        // init nl
        jint type = aNlType[j];
        jdouble dx = aNlDx[j], dy = aNlDy[j], dz = aNlDz[j];
        jdouble dis = sqrt((double)(dx*dx + dy*dy + dz*dz));
        // check rcut for merge
        if (dis >= aRCut) continue;
        // get gradFxyz
        jdouble tGradFx = aGradFx[j], tGradFy = aGradFy[j], tGradFz = aGradFz[j];
        // get fc Rn Y
        jdouble fc = tNlFc[j];
        jdouble *tRn = tNlRn + j*(aNMax+1);
        jdouble *tY = tNlY + j*LMALL;
        // get fcPxyz RnPxyz YPxyz
        jdouble fcPx = tNlFcPx[j], fcPy = tNlFcPy[j], fcPz = tNlFcPz[j];
        jdouble *tRnPx = tNlRnPx + j*(aNMax+1);
        jdouble *tRnPy = tNlRnPy + j*(aNMax+1);
        jdouble *tRnPz = tNlRnPz + j*(aNMax+1);
        jdouble *tYPx = tNlYPx + j*LMALL;
        jdouble *tYPy = tNlYPy + j*LMALL;
        jdouble *tYPz = tNlYPz + j*LMALL;
        // mplus to gradNNgrad
        if (WTYPE==WTYPE_FUSE) {
            calGradNNGradBnlm<LMALL>(rGradNNGradBnlm, tY, fc, tRn, aNMax, tYPx, tYPy, tYPz, rGradNNGradY, fcPx, fcPy, fcPz, tRnPx, tRnPy, tRnPz, tGradFx, tGradFy, tGradFz);
            jdouble *tFuseWeight = aFuseWeight;
            jdouble *tGradNNGradCnlm = rGradNNGradCnlm;
            for (jint k = 0; k < aFuseSize; ++k) {
                jdouble wt = tFuseWeight[type-1];
                mplusBnlm2Cnlm<LMALL>(tGradNNGradCnlm, rGradNNGradBnlm, wt, aNMax);
                tGradNNGradCnlm += tSizeBnlm;
                tFuseWeight += aTypeNum;
            }
            if (!aFixBasis) {
                jdouble *tGradPara = rGradPara;
                jdouble *tNNGradCnlm = aNNGradCnlm;
                for (jint k = 0; k < aFuseSize; ++k) {
                    tGradPara[type-1] += dotBnlmGradCnlm<LMALL>(tNNGradCnlm, rGradNNGradBnlm, aNMax);
                    tNNGradCnlm += tSizeBnlm;
                    tGradPara += aTypeNum;
                }
            }
        } else
        if (WTYPE==WTYPE_NONE) {
            mplusGradNNGradCnlm<LMALL>(rGradNNGradCnlm, tY, fc, tRn, aNMax, tYPx, tYPy, tYPz, rGradNNGradY, fcPx, fcPy, fcPz, tRnPx, tRnPy, tRnPz, tGradFx, tGradFy, tGradFz);
        } else
        if (WTYPE==WTYPE_FULL) {
            jdouble *tGradNNGradCnlm = rGradNNGradCnlm + tSizeBnlm*(type-1);
            mplusGradNNGradCnlm<LMALL>(tGradNNGradCnlm, tY, fc, tRn, aNMax, tYPx, tYPy, tYPz, rGradNNGradY, fcPx, fcPy, fcPz, tRnPx, tRnPy, tRnPz, tGradFx, tGradFy, tGradFz);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            jdouble *rGradNNGradCnlmWt = rGradNNGradCnlm + tSizeBnlm*type;
            mplusGradNNGradCnlmWt<LMALL>(rGradNNGradCnlm, rGradNNGradCnlmWt, tY, fc, tRn, 1.0, aNMax, tYPx, tYPy, tYPz, rGradNNGradY, fcPx, fcPy, fcPz, tRnPx, tRnPy, tRnPz, tGradFx, tGradFy, tGradFz);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            jdouble wt = ((type&1)==1) ? type : -type;
            jdouble *rGradNNGradCnlmWt = rGradNNGradCnlm + tSizeBnlm;
            mplusGradNNGradCnlmWt<LMALL>(rGradNNGradCnlm, rGradNNGradCnlmWt, tY, fc, tRn, wt, aNMax, tYPx, tYPy, tYPz, rGradNNGradY, fcPx, fcPy, fcPz, tRnPx, tRnPy, tRnPz, tGradFx, tGradFy, tGradFz);
        }
    }
}
template <jint LMAX, jboolean NO_RADIAL, jint L3MAX, jboolean L3CROSS, jint L4MAX, jboolean L4CROSS, jint WTYPE>
static void calBackwardForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                             jdouble *aNNGrad, jdouble *aGradFx, jdouble *aGradFy, jdouble *aGradFz,
                             jdouble *rGradNNGrad, jdouble *rGradPara,
                             jdouble *aForwardCache, jdouble *aForwardForceCache,
                             jdouble *rBackwardCache, jdouble *rBackwardForceCache, jboolean aFixBasis,
                             jint aTypeNum, jdouble aRCut, jint aNMax, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    // const init
    jint tSizeN;
    switch(WTYPE) {
    case WTYPE_EXFULL: {
        tSizeN = (aTypeNum+1)*(aNMax+1);
        break;
    }
    case WTYPE_FULL: {
        tSizeN = aTypeNum*(aNMax+1);
        break;
    }
    case WTYPE_NONE: {
        tSizeN = aNMax+1;
        break;
    }
    case WTYPE_DEFAULT: {
        tSizeN = (aNMax+aNMax+2);
        break;
    }
    case WTYPE_FUSE: {
        tSizeN = aFuseSize*(aNMax+1);
        break;
    }
    default: {
        tSizeN = 0;
        break;
    }}
    constexpr jint tSizeL = (NO_RADIAL?LMAX:(LMAX+1)) + (L3CROSS?L3NCOLS:L3NCOLS_NOCROSS)[L3MAX] + (L4CROSS?L4NCOLS:L4NCOLS_NOCROSS)[L4MAX];
    constexpr jint tLMaxMax = LMAX>L3MAX ? (LMAX>L4MAX?LMAX:L4MAX) : (L3MAX>L4MAX?L3MAX:L4MAX);
    constexpr jint tLMAll = (tLMaxMax+1)*(tLMaxMax+1);
    const jint tSizeCnlm = tSizeN*tLMAll;
    // init cache
    jdouble *tCnlm = aForwardCache;
    jdouble *tForwardCacheElse = tCnlm + tSizeCnlm;
    jdouble *tNNGradCnlm = aForwardForceCache;
    jdouble *tForwardForceCacheElse = tNNGradCnlm + tSizeCnlm;
    jdouble *rGradCnlm = rBackwardCache;
    jdouble *rGradNNGradCnlm = rBackwardForceCache;
    jdouble *rBackwardForceCacheElse = rGradNNGradCnlm + tSizeCnlm;
    
    // cal rGradNNGradCnlm
    calBackwardForceMainLoop<tLMAll, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rGradNNGradCnlm, aGradFx, aGradFy, aGradFz, tNNGradCnlm, rGradPara, tForwardCacheElse, tForwardForceCacheElse, rBackwardForceCacheElse, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
    
    // grad grad cnlm to grad grad fp
    constexpr jint tShiftL3 = NO_RADIAL?LMAX:(LMAX+1);
    constexpr jint tShiftL4 = tShiftL3 + (L3CROSS?L3NCOLS:L3NCOLS_NOCROSS)[L3MAX];
    for (jint n=0, tShift=0, tShiftFp=0; n<tSizeN; ++n, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calGradNNGradL2_<LMAX, NO_RADIAL>(tCnlm+tShift, rGradNNGradCnlm+tShift, rGradNNGrad+tShiftFp);
        calGradNNGradL3_<L3MAX, L3CROSS>(tCnlm+tShift, rGradNNGradCnlm+tShift, rGradNNGrad+tShiftFp+tShiftL3);
        calGradNNGradL4_<L4MAX, L4CROSS>(tCnlm+tShift, rGradNNGradCnlm+tShift, rGradNNGrad+tShiftFp+tShiftL4);
    }
    if (WTYPE==WTYPE_FUSE) if (!aFixBasis) {
        for (jint n=0, tShift=0, tShiftFp=0; n<tSizeN; ++n, tShift+=tLMAll, tShiftFp+=tSizeL) {
            calGradCnlmL2_<LMAX, NO_RADIAL>(rGradCnlm+tShift, rGradNNGradCnlm+tShift, aNNGrad+tShiftFp);
            calGradCnlmL3_<L3MAX, L3CROSS>(tCnlm+tShift, rGradCnlm+tShift, rGradNNGradCnlm+tShift, aNNGrad+tShiftFp+tShiftL3);
            calGradCnlmL4_<L4MAX, L4CROSS>(tCnlm+tShift, rGradCnlm+tShift, rGradNNGradCnlm+tShift, aNNGrad+tShiftFp+tShiftL4);
        }
    }
}


template <jboolean NO_RADIAL, jint L3MAX, jboolean L3CROSS, jint L4MAX, jboolean L4CROSS, jint WTYPE>
static inline void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                         jdouble *rFp, jdouble *rForwardCache, jboolean aFullCache,
                         jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    switch (aLMax) {
    case 0: {calFp<0, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 1: {calFp<1, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 2: {calFp<2, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 3: {calFp<3, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 4: {calFp<4, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 5: {calFp<5, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 6: {calFp<6, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 7: {calFp<7, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 8: {calFp<8, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 9: {calFp<9, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 10: {calFp<10, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 11: {calFp<11, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 12: {calFp<12, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    default: {return;}
    }
}
template <jint L3MAX, jboolean L3CROSS, jint L4MAX, jboolean L4CROSS, jint WTYPE>
static inline void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                         jdouble *rFp, jdouble *rForwardCache, jboolean aFullCache,
                         jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aNoRadial) {
        calFp<JNI_TRUE, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aFuseWeight, aFuseSize);
    } else {
        calFp<JNI_FALSE, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aFuseWeight, aFuseSize);
    }
}
template <jint L4MAX, jboolean L4CROSS, jint WTYPE>
static inline void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                         jdouble *rFp, jdouble *rForwardCache, jboolean aFullCache,
                         jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial,
                         jint aL3Max, jboolean aL3Cross, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aL3Cross) {
        switch (aL3Max) {
        case 0: case 1: {
            calFp<0, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 2: {
            calFp<2, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 3: {
            calFp<3, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 4: {
            calFp<4, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 5: {
            calFp<5, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 6: {
            calFp<6, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch (aL3Max) {
        case 0: case 1: {
            calFp<0, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 2: case 3: {
            calFp<2, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 4: case 5: {
            calFp<4, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 6: {
            calFp<6, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    }
}
template <jint WTYPE>
static inline void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                         jdouble *rFp, jdouble *rForwardCache, jboolean aFullCache,
                         jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial,
                         jint aL3Max, jboolean aL3Cross, jint aL4Max, jboolean aL4Cross,
                         jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aL4Cross) {
        switch (aL4Max) {
        case 0: {
            calFp<0, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 1: {
            calFp<1, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 2: {
            calFp<2, JNI_TRUE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 3: {
            calFp<3, JNI_TRUE,  WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch (aL4Max) {
        case 0: {
            calFp<0, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 1: {
            calFp<1, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 2: {
            calFp<2, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 3: {
            calFp<3, JNI_FALSE,  WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    }
}
static inline void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                         jdouble *rFp, jdouble *rForwardCache, jboolean aFullCache,
                         jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial,
                         jint aL3Max, jboolean aL3Cross, jint aL4Max, jboolean aL4Cross,
                         jint aWType, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aTypeNum == 1) {
        switch(aWType) {
        case WTYPE_FUSE: {
            calFp<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_NONE:
        case WTYPE_FULL:
        case WTYPE_EXFULL:
        case WTYPE_DEFAULT: {
            calFp<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch(aWType) {
        case WTYPE_EXFULL: {
            calFp<WTYPE_EXFULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_FULL: {
            calFp<WTYPE_FULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_NONE: {
            calFp<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_DEFAULT: {
            calFp<WTYPE_DEFAULT>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_FUSE: {
            calFp<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    }
}


template <jboolean NO_RADIAL, jint L3MAX, jboolean L3CROSS, jint L4MAX, jboolean L4CROSS, jint WTYPE>
static void calBackward(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                        jdouble *aGradFp, jdouble *rGradPara, jdouble *aForwardCache, jdouble *rBackwardCache,
                        jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aFuseSize) noexcept {
    switch (aLMax) {
    case 0: {calBackward<0, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aFuseSize); return;}
    case 1: {calBackward<1, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aFuseSize); return;}
    case 2: {calBackward<2, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aFuseSize); return;}
    case 3: {calBackward<3, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aFuseSize); return;}
    case 4: {calBackward<4, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aFuseSize); return;}
    case 5: {calBackward<5, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aFuseSize); return;}
    case 6: {calBackward<6, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aFuseSize); return;}
    case 7: {calBackward<7, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aFuseSize); return;}
    case 8: {calBackward<8, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aFuseSize); return;}
    case 9: {calBackward<9, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aFuseSize); return;}
    case 10: {calBackward<10, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aFuseSize); return;}
    case 11: {calBackward<11, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aFuseSize); return;}
    case 12: {calBackward<12, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aFuseSize); return;}
    default: {return;}
    }
}
template <jint L3MAX, jboolean L3CROSS, jint L4MAX, jboolean L4CROSS, jint WTYPE>
static void calBackward(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                        jdouble *aGradFp, jdouble *rGradPara, jdouble *aForwardCache, jdouble *rBackwardCache,
                        jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial, jint aFuseSize) noexcept {
    if (aNoRadial) {
        calBackward<JNI_TRUE, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aFuseSize);
    } else {
        calBackward<JNI_FALSE, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aFuseSize);
    }
}
template <jint L4MAX, jboolean L4CROSS, jint WTYPE>
static void calBackward(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                        jdouble *aGradFp, jdouble *rGradPara, jdouble *aForwardCache, jdouble *rBackwardCache,
                        jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial,
                        jint aL3Max, jboolean aL3Cross, jint aFuseSize) noexcept {
    if (aL3Cross) {
        switch (aL3Max) {
        case 0: case 1: {
            calBackward<0, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseSize);
            return;
        }
        case 2: {
            calBackward<2, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseSize);
            return;
        }
        case 3: {
            calBackward<3, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseSize);
            return;
        }
        case 4: {
            calBackward<4, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseSize);
            return;
        }
        case 5: {
            calBackward<5, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseSize);
            return;
        }
        case 6: {
            calBackward<6, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch (aL3Max) {
        case 0: case 1: {
            calBackward<0, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseSize);
            return;
        }
        case 2: case 3: {
            calBackward<2, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseSize);
            return;
        }
        case 4: case 5: {
            calBackward<4, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseSize);
            return;
        }
        case 6: {
            calBackward<6, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    }
}
template <jint WTYPE>
static void calBackward(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                        jdouble *aGradFp, jdouble *rGradPara, jdouble *aForwardCache, jdouble *rBackwardCache,
                        jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial,
                        jint aL3Max, jboolean aL3Cross, jint aL4Max, jboolean aL4Cross, jint aFuseSize) noexcept {
    if (aL4Cross) {
        switch (aL4Max) {
        case 0: {
            calBackward<0, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseSize);
            return;
        }
        case 1: {
            calBackward<1, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseSize);
            return;
        }
        case 2: {
            calBackward<2, JNI_TRUE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseSize);
            return;
        }
        case 3: {
            calBackward<3, JNI_TRUE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch (aL4Max) {
        case 0: {
            calBackward<0, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseSize);
            return;
        }
        case 1: {
            calBackward<1, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseSize);
            return;
        }
        case 2: {
            calBackward<2, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseSize);
            return;
        }
        case 3: {
            calBackward<3, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    }
}
static void calBackward(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                        jdouble *aGradFp, jdouble *rGradPara, jdouble *aForwardCache, jdouble *rBackwardCache,
                        jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial,
                        jint aL3Max, jboolean aL3Cross, jint aL4Max, jboolean aL4Cross, jint aWType, jint aFuseSize) noexcept {
    if (aWType==WTYPE_FUSE) {
        calBackward<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseSize);
    }
}


template <jboolean NO_RADIAL, jint L3MAX, jboolean L3CROSS, jint L4MAX, jboolean L4CROSS, jint WTYPE>
static inline void calForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                            jdouble *aNNGrad, jdouble *rFx, jdouble *rFy, jdouble *rFz,
                            jdouble *aForwardCache, jdouble *rForwardForceCache, jboolean aFullCache,
                            jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    switch (aLMax) {
    case 0: {calForce<0, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 1: {calForce<1, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 2: {calForce<2, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 3: {calForce<3, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 4: {calForce<4, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 5: {calForce<5, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 6: {calForce<6, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 7: {calForce<7, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 8: {calForce<8, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 9: {calForce<9, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 10: {calForce<10, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 11: {calForce<11, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 12: {calForce<12, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    default: {return;}
    }
}
template <jint L3MAX, jboolean L3CROSS, jint L4MAX, jboolean L4CROSS, jint WTYPE>
static inline void calForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                            jdouble *aNNGrad, jdouble *rFx, jdouble *rFy, jdouble *rFz,
                            jdouble *aForwardCache, jdouble *rForwardForceCache, jboolean aFullCache,
                            jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aNoRadial) {
        calForce<JNI_TRUE, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aFuseWeight, aFuseSize);
    } else {
        calForce<JNI_FALSE, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aFuseWeight, aFuseSize);
    }
}
template <jint L4MAX, jboolean L4CROSS, jint WTYPE>
static inline void calForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                            jdouble *aNNGrad, jdouble *rFx, jdouble *rFy, jdouble *rFz,
                            jdouble *aForwardCache, jdouble *rForwardForceCache, jboolean aFullCache,
                            jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial,
                            jint aL3Max, jboolean aL3Cross, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aL3Cross) {
        switch (aL3Max) {
        case 0: case 1: {
            calForce<0, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 2: {
            calForce<2, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 3: {
            calForce<3, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 4: {
            calForce<4, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 5: {
            calForce<5, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 6: {
            calForce<6, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch (aL3Max) {
        case 0: case 1: {
            calForce<0, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 2: case 3: {
            calForce<2, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 4: case 5: {
            calForce<4, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 6: {
            calForce<6, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    }
}
template <jint WTYPE>
static inline void calForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                            jdouble *aNNGrad, jdouble *rFx, jdouble *rFy, jdouble *rFz,
                            jdouble *aForwardCache, jdouble *rForwardForceCache, jboolean aFullCache,
                            jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial,
                            jint aL3Max, jboolean aL3Cross, jint aL4Max, jboolean aL4Cross,
                            jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aL4Cross) {
        switch (aL4Max) {
        case 0: {
            calForce<0, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 1: {
            calForce<1, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 2: {
            calForce<2, JNI_TRUE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 3: {
            calForce<3, JNI_TRUE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch (aL4Max) {
        case 0: {
            calForce<0, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 1: {
            calForce<1, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 2: {
            calForce<2, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 3: {
            calForce<3, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    }
}
static inline void calForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                            jdouble *aNNGrad, jdouble *rFx, jdouble *rFy, jdouble *rFz,
                            jdouble *aForwardCache, jdouble *rForwardForceCache, jboolean aFullCache,
                            jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial,
                            jint aL3Max, jboolean aL3Cross, jint aL4Max, jboolean aL4Cross, jint aWType, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aTypeNum == 1) {
        switch(aWType) {
        case WTYPE_FUSE: {
            calForce<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_NONE:
        case WTYPE_FULL:
        case WTYPE_EXFULL:
        case WTYPE_DEFAULT: {
            calForce<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch(aWType) {
        case WTYPE_EXFULL: {
            calForce<WTYPE_EXFULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_FULL: {
            calForce<WTYPE_FULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_NONE: {
            calForce<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_DEFAULT: {
            calForce<WTYPE_DEFAULT>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_FUSE: {
            calForce<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    }
}


template <jboolean NO_RADIAL, jint L3MAX, jboolean L3CROSS, jint L4MAX, jboolean L4CROSS, jint WTYPE>
static void calBackwardForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                             jdouble *aNNGrad, jdouble *aGradFx, jdouble *aGradFy, jdouble *aGradFz,
                             jdouble *rGradNNGrad, jdouble *rGradPara,
                             jdouble *aForwardCache, jdouble *aForwardForceCache,
                             jdouble *rBackwardCache, jdouble *rBackwardForceCache, jboolean aFixBasis,
                             jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    switch (aLMax) {
    case 0: {calBackwardForce<0, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 1: {calBackwardForce<1, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 2: {calBackwardForce<2, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 3: {calBackwardForce<3, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 4: {calBackwardForce<4, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 5: {calBackwardForce<5, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 6: {calBackwardForce<6, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 7: {calBackwardForce<7, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 8: {calBackwardForce<8, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 9: {calBackwardForce<9, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 10: {calBackwardForce<10, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 11: {calBackwardForce<11, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    case 12: {calBackwardForce<12, NO_RADIAL, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize); return;}
    default: {return;}
    }
}
template <jint L3MAX, jboolean L3CROSS, jint L4MAX, jboolean L4CROSS, jint WTYPE>
static void calBackwardForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                             jdouble *aNNGrad, jdouble *aGradFx, jdouble *aGradFy, jdouble *aGradFz,
                             jdouble *rGradNNGrad, jdouble *rGradPara,
                             jdouble *aForwardCache, jdouble *aForwardForceCache,
                             jdouble *rBackwardCache, jdouble *rBackwardForceCache, jboolean aFixBasis,
                             jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aNoRadial) {
        calBackwardForce<JNI_TRUE, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aFuseWeight, aFuseSize);
    } else {
        calBackwardForce<JNI_FALSE, L3MAX, L3CROSS, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aFuseWeight, aFuseSize);
    }
}
template <jint L4MAX, jboolean L4CROSS, jint WTYPE>
static void calBackwardForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                             jdouble *aNNGrad, jdouble *aGradFx, jdouble *aGradFy, jdouble *aGradFz,
                             jdouble *rGradNNGrad, jdouble *rGradPara,
                             jdouble *aForwardCache, jdouble *aForwardForceCache,
                             jdouble *rBackwardCache, jdouble *rBackwardForceCache, jboolean aFixBasis,
                             jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial,
                             jint aL3Max, jboolean aL3Cross, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aL3Cross) {
        switch (aL3Max) {
        case 0: case 1: {
            calBackwardForce<0, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 2: {
            calBackwardForce<2, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 3: {
            calBackwardForce<3, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 4: {
            calBackwardForce<4, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 5: {
            calBackwardForce<5, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 6: {
            calBackwardForce<6, JNI_TRUE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch (aL3Max) {
        case 0: case 1: {
            calBackwardForce<0, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 2: case 3: {
            calBackwardForce<2, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 4: case 5: {
            calBackwardForce<4, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        case 6: {
            calBackwardForce<6, JNI_FALSE, L4MAX, L4CROSS, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    }
}
template <jint WTYPE>
static void calBackwardForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                             jdouble *aNNGrad, jdouble *aGradFx, jdouble *aGradFy, jdouble *aGradFz,
                             jdouble *rGradNNGrad, jdouble *rGradPara,
                             jdouble *aForwardCache, jdouble *aForwardForceCache,
                             jdouble *rBackwardCache, jdouble *rBackwardForceCache, jboolean aFixBasis,
                             jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial,
                             jint aL3Max, jboolean aL3Cross, jint aL4Max, jboolean aL4Cross,
                             jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aL4Cross) {
        switch (aL4Max) {
        case 0: {
            calBackwardForce<0, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 1: {
            calBackwardForce<1, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 2: {
            calBackwardForce<2, JNI_TRUE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 3: {
            calBackwardForce<3, JNI_TRUE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch (aL4Max) {
        case 0: {
            calBackwardForce<0, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 1: {
            calBackwardForce<1, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 2: {
            calBackwardForce<2, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        case 3: {
            calBackwardForce<3, JNI_FALSE, WTYPE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    }
}
static void calBackwardForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                             jdouble *aNNGrad, jdouble *aGradFx, jdouble *aGradFy, jdouble *aGradFz,
                             jdouble *rGradNNGrad, jdouble *rGradPara,
                             jdouble *aForwardCache, jdouble *aForwardForceCache,
                             jdouble *rBackwardCache, jdouble *rBackwardForceCache, jboolean aFixBasis,
                             jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jboolean aNoRadial,
                             jint aL3Max, jboolean aL3Cross, jint aL4Max, jboolean aL4Cross,
                             jint aWType, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aTypeNum == 1) {
        switch(aWType) {
        case WTYPE_FUSE: {
            calBackwardForce<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_NONE:
        case WTYPE_FULL:
        case WTYPE_EXFULL:
        case WTYPE_DEFAULT: {
            calBackwardForce<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch(aWType) {
        case WTYPE_EXFULL: {
            calBackwardForce<WTYPE_EXFULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_FULL: {
            calBackwardForce<WTYPE_FULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_NONE: {
            calBackwardForce<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_DEFAULT: {
            calBackwardForce<WTYPE_DEFAULT>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_FUSE: {
            calBackwardForce<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aNoRadial, aL3Max, aL3Cross, aL4Max, aL4Cross, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    }
}

}

#endif //BASIS_SPHERICAL_CHEBYSHEV_H