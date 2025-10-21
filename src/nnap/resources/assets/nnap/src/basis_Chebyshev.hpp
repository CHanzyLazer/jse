#ifndef BASIS_CHEBYSHEV_H
#define BASIS_CHEBYSHEV_H

#include "basis_ChebyshevUtil.hpp"

namespace JSE_NNAP {

template <jint WTYPE, jboolean FULL_CACHE>
static void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                  jdouble *rFp, jdouble *rForwardCache,
                  jint aTypeNum, jdouble aRCut, jint aNMax, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    // const init
    jint tSizeFp;
    switch(WTYPE) {
    case WTYPE_EXFULL:  {tSizeFp = (aTypeNum+1)*(aNMax+1);  break;}
    case WTYPE_FULL:    {tSizeFp = aTypeNum*(aNMax+1);      break;}
    case WTYPE_NONE:    {tSizeFp = aNMax+1;                 break;}
    case WTYPE_DEFAULT: {tSizeFp = (aNMax+aNMax+2);         break;}
    case WTYPE_FUSE:    {tSizeFp = aFuseSize*(aNMax+1);     break;}
    case WTYPE_EXFUSE:  {tSizeFp = (aFuseSize+1)*(aNMax+1); break;}
    default:            {tSizeFp = 0;                       break;}
    }
    // init cache
    jdouble *rRn = NULL;
    jdouble *rNlRn = NULL, *rNlFc = NULL;
    if (FULL_CACHE) {
        rNlRn = rForwardCache;
        rNlFc = rNlRn + aNN*(aNMax+1);
    } else {
        rRn = rForwardCache;
    }
    // clear fp first
    for (jint i = 0; i < tSizeFp; ++i) {
        rFp[i] = 0.0;
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
        // cal fp
        if (WTYPE==WTYPE_FUSE) {
            mplusFpFuse(rFp, aFuseWeight, type, fc, rRn, aFuseSize, aNMax);
        } else
        if (WTYPE==WTYPE_EXFUSE) {
            mplusFpExFuse(rFp, aFuseWeight, type, fc, rRn, aFuseSize, aNMax);
        } else
        if (WTYPE==WTYPE_NONE) {
            mplus(rFp, fc, rRn, aNMax+1);
        } else
        if (WTYPE==WTYPE_FULL) {
            jdouble *tFp = rFp + (aNMax+1)*(type-1);
            mplus(tFp, fc, rRn, aNMax+1);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            jdouble *tFpWt = rFp + (aNMax+1)*type;
            mplus2(rFp, tFpWt, fc, fc, rRn, aNMax+1);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            jdouble wt = ((type&1)==1) ? type : -type;
            jdouble *tFpWt = rFp + (aNMax+1);
            mplus2(rFp, tFpWt, fc, fc*wt, rRn, aNMax+1);
        }
    }
}

template <jint WTYPE>
static void calBackward(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                        jdouble *aGradFp, jdouble *rGradPara, jdouble *aForwardCache,
                        jint aTypeNum, jdouble aRCut, jint aNMax, jint aFuseSize) noexcept {
    static_assert(WTYPE!=WTYPE_DEFAULT && WTYPE!=WTYPE_NONE && WTYPE!=WTYPE_FULL && WTYPE!=WTYPE_EXFULL, "WTYPE INVALID");
    // init cache
    jdouble *tNlRn = aForwardCache;
    jdouble *tNlFc = tNlRn + aNN*(aNMax+1);
    // loop for neighbor
    for (jint j = 0; j < aNN; ++j) {
        jint type = aNlType[j];
        jdouble dx = aNlDx[j], dy = aNlDy[j], dz = aNlDz[j];
        jdouble dis = sqrt((double)(dx*dx + dy*dy + dz*dz));
        // check rcut for merge
        if (dis >= aRCut) continue;
        // get fc Rn
        jdouble fc = tNlFc[j];
        jdouble *tRn = tNlRn + j*(aNMax+1);
        // plus to para
        if (WTYPE==WTYPE_FUSE) {
            mplusGradParaFuse(aGradFp, rGradPara, type, fc, tRn, aFuseSize, aNMax);
        } else
        if (WTYPE==WTYPE_EXFUSE) {
            jdouble *tGradFpWt = aGradFp + (aNMax+1);
            mplusGradParaFuse(tGradFpWt, rGradPara, type, fc, tRn, aFuseSize, aNMax);
        }
    }
}

template <jint WTYPE, jboolean FULL_CACHE>
static void calForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                     jdouble *aNNGrad, jdouble *rFx, jdouble *rFy, jdouble *rFz,
                     jdouble *aForwardCache, jdouble *rForwardForceCache,
                     jint aTypeNum, jdouble aRCut, jint aNMax, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    // init cache
    jdouble *tNlRn = aForwardCache;
    jdouble *tNlFc = tNlRn + aNN*(aNMax+1);
    jdouble *rRnPx = NULL, *rRnPy = NULL, *rRnPz = NULL, *rCheby2 = NULL;
    jdouble *rNlRnPx = NULL, *rNlRnPy = NULL, *rNlRnPz = NULL;
    jdouble *rNlFcPx = NULL, *rNlFcPy = NULL, *rNlFcPz = NULL;
    if (FULL_CACHE) {
        rNlRnPx = rForwardForceCache;
        rNlRnPy = rNlRnPx + aNN*(aNMax+1);
        rNlRnPz = rNlRnPy + aNN*(aNMax+1);
        rNlFcPx = rNlRnPz + aNN*(aNMax+1);
        rNlFcPy = rNlFcPx + aNN;
        rNlFcPz = rNlFcPy + aNN;
        rCheby2 = rNlFcPz + aNN;
    } else {
        rRnPx = rForwardForceCache;
        rRnPy = rRnPx + (aNMax+1);
        rRnPz = rRnPy + (aNMax+1);
        rCheby2 = rRnPz + (aNMax+1);
    }
    // loop for neighbor
    for (jint j = 0; j < aNN; ++j) {
        // init nl
        jint type = aNlType[j];
        jdouble dx = aNlDx[j], dy = aNlDy[j], dz = aNlDz[j];
        jdouble dis = sqrt((double)(dx*dx + dy*dy + dz*dz));
        // check rcut for merge
        if (dis >= aRCut) continue;
        // get fc Rn
        jdouble fc = tNlFc[j];
        jdouble *tRn = tNlRn + j*(aNMax+1);
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
        if (WTYPE==WTYPE_FUSE) {
            jdouble *tGradRn = rCheby2;
            calGradRnFuse(tGradRn, aNNGrad, aFuseWeight, type, aFuseSize, aNMax);
            gradRn2Fxyz(j, tGradRn, fc, tRn, aNMax, fcPx, fcPy, fcPz, rRnPx, rRnPy, rRnPz, rFx, rFy, rFz);
        } else
        if (WTYPE==WTYPE_EXFUSE) {
            jdouble *tGradRn = rCheby2;
            calGradRnExFuse(tGradRn, aNNGrad, aFuseWeight, type, aFuseSize, aNMax);
            gradRn2Fxyz(j, tGradRn, fc, tRn, aNMax, fcPx, fcPy, fcPz, rRnPx, rRnPy, rRnPz, rFx, rFy, rFz);
        } else
        if (WTYPE==WTYPE_NONE) {
            gradRn2Fxyz(j, aNNGrad, fc, tRn, aNMax, fcPx, fcPy, fcPz, rRnPx, rRnPy, rRnPz, rFx, rFy, rFz);
        } else
        if (WTYPE==WTYPE_FULL) {
            jdouble *tGradRn = aNNGrad + (aNMax+1)*(type-1);
            gradRn2Fxyz(j, tGradRn, fc, tRn, aNMax, fcPx, fcPy, fcPz, rRnPx, rRnPy, rRnPz, rFx, rFy, rFz);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            jdouble *tNNGradWt = aNNGrad + (aNMax+1)*type;
            gradRnWt2Fxyz(j, aNNGrad, tNNGradWt, fc, tRn, 1.0, aNMax, fcPx, fcPy, fcPz, rRnPx, rRnPy, rRnPz, rFx, rFy, rFz);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            jdouble wt = ((type&1)==1) ? type : -type;
            jdouble *tNNGradWt = aNNGrad + (aNMax+1);
            gradRnWt2Fxyz(j, aNNGrad, tNNGradWt, fc, tRn, wt, aNMax, fcPx, fcPy, fcPz, rRnPx, rRnPy, rRnPz, rFx, rFy, rFz);
        }
    }
}

template <jint WTYPE>
static void calBackwardForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                             jdouble *aNNGrad, jdouble *aGradFx, jdouble *aGradFy, jdouble *aGradFz,
                             jdouble *rGradNNGrad, jdouble *rGradPara,
                             jdouble *aForwardCache, jdouble *aForwardForceCache,
                             jdouble *rBackwardForceCache, jboolean aFixBasis,
                             jint aTypeNum, jdouble aRCut, jint aNMax, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    // init cache
    jdouble *tNlRn = aForwardCache;
    jdouble *tNlFc = tNlRn + aNN*(aNMax+1);
    jdouble *tNlRnPx = aForwardForceCache;
    jdouble *tNlRnPy = tNlRnPx + aNN*(aNMax+1);
    jdouble *tNlRnPz = tNlRnPy + aNN*(aNMax+1);
    jdouble *tNlFcPx = tNlRnPz + aNN*(aNMax+1);
    jdouble *tNlFcPy = tNlFcPx + aNN;
    jdouble *tNlFcPz = tNlFcPy + aNN;
    jdouble *rGradNNGradRn = NULL;
    if (WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
        rGradNNGradRn = rBackwardForceCache;
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
        // get fc Rn
        jdouble fc = tNlFc[j];
        jdouble *tRn = tNlRn + j*(aNMax+1);
        // get fcPxyz RnPxyz
        jdouble fcPx = tNlFcPx[j], fcPy = tNlFcPy[j], fcPz = tNlFcPz[j];
        jdouble *tRnPx = tNlRnPx + j*(aNMax+1);
        jdouble *tRnPy = tNlRnPy + j*(aNMax+1);
        jdouble *tRnPz = tNlRnPz + j*(aNMax+1);
        // get gradNNgradRn
        if (WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
            calGradNNGradRn(rGradNNGradRn, fc, tRn, aNMax, fcPx, fcPy, fcPz, tRnPx, tRnPy, tRnPz, tGradFx, tGradFy, tGradFz);
        }
        // mplus to gradNNgrad
        if (WTYPE==WTYPE_FUSE) {
            mplusGradNNGradFuse(rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, type, aFuseSize, aNMax, aFixBasis);
        } else
        if (WTYPE==WTYPE_EXFUSE) {
            mplusGradNNGradExFuse(rGradNNGrad, rGradNNGradRn, aNNGrad, aFuseWeight, rGradPara, type, aFuseSize, aNMax, aFixBasis);
        } else
        if (WTYPE==WTYPE_NONE) {
            mplusGradNNGrad(rGradNNGrad, fc, tRn, aNMax, fcPx, fcPy, fcPz, tRnPx, tRnPy, tRnPz, tGradFx, tGradFy, tGradFz);
        } else
        if (WTYPE==WTYPE_FULL) {
            jdouble *tGradNNGrad = rGradNNGrad + (aNMax+1)*(type-1);
            mplusGradNNGrad(tGradNNGrad, fc, tRn, aNMax, fcPx, fcPy, fcPz, tRnPx, tRnPy, tRnPz, tGradFx, tGradFy, tGradFz);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            jdouble *tGradNNGradWt = rGradNNGrad + (aNMax+1)*type;
            mplusGradNNGradWt(rGradNNGrad, tGradNNGradWt, fc, tRn, 1.0, aNMax, fcPx, fcPy, fcPz, tRnPx, tRnPy, tRnPz, tGradFx, tGradFy, tGradFz);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            jdouble wt = ((type&1)==1) ? type : -type;
            jdouble *tGradNNGradWt = rGradNNGrad + (aNMax+1);
            mplusGradNNGradWt(rGradNNGrad, tGradNNGradWt, fc, tRn, wt, aNMax, fcPx, fcPy, fcPz, tRnPx, tRnPy, tRnPz, tGradFx, tGradFy, tGradFz);
        }
    }
}


template <jint WTYPE>
static inline void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                         jdouble *rFp, jdouble *rForwardCache, jboolean aFullCache,
                         jint aTypeNum, jdouble aRCut, jint aNMax, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aFullCache) {
        calFp<WTYPE, JNI_TRUE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
    } else {
        calFp<WTYPE, JNI_FALSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
    }
}
static inline void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                         jdouble *rFp, jdouble *rForwardCache, jboolean aFullCache,
                         jint aTypeNum, jdouble aRCut, jint aNMax, jint aWType, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aTypeNum == 1) {
        switch(aWType) {
        case WTYPE_FUSE: {
            calFp<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_EXFUSE: {
            calFp<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_NONE:
        case WTYPE_FULL:
        case WTYPE_EXFULL:
        case WTYPE_DEFAULT: {
            calFp<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch(aWType) {
        case WTYPE_NONE: {
            calFp<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_EXFULL: {
            calFp<WTYPE_EXFULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_FULL: {
            calFp<WTYPE_FULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_DEFAULT: {
            calFp<WTYPE_DEFAULT>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_FUSE: {
            calFp<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_EXFUSE: {
            calFp<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    }
}


static inline void calBackward(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                               jdouble *aGradFp, jdouble *rGradPara, jdouble *aForwardCache,
                               jint aTypeNum, jdouble aRCut, jint aNMax, jint aWType, jint aFuseSize) noexcept {
    if (aWType==WTYPE_FUSE) {
        calBackward<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, aTypeNum, aRCut, aNMax, aFuseSize);
    } else
    if (aWType==WTYPE_EXFUSE) {
        calBackward<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, aTypeNum, aRCut, aNMax, aFuseSize);
    }
}


template <jint WTYPE>
static inline void calForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                            jdouble *aNNGrad, jdouble *rFx, jdouble *rFy, jdouble *rFz,
                            jdouble *aForwardCache, jdouble *rForwardForceCache, jboolean aFullCache,
                            jint aTypeNum, jdouble aRCut, jint aNMax, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aFullCache) {
        calForce<WTYPE, JNI_TRUE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
    } else {
        calForce<WTYPE, JNI_FALSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
    }
}
static inline void calForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                            jdouble *aNNGrad, jdouble *rFx, jdouble *rFy, jdouble *rFz,
                            jdouble *aForwardCache, jdouble *rForwardForceCache, jboolean aFullCache,
                            jint aTypeNum, jdouble aRCut, jint aNMax, jint aWType, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aTypeNum == 1) {
        switch(aWType) {
        case WTYPE_FUSE: {
            calForce<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_EXFUSE: {
            calForce<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_NONE:
        case WTYPE_FULL:
        case WTYPE_EXFULL:
        case WTYPE_DEFAULT: {
            calForce<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch(aWType) {
        case WTYPE_NONE: {
            calForce<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_EXFULL: {
            calForce<WTYPE_EXFULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_FULL: {
            calForce<WTYPE_FULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_DEFAULT: {
            calForce<WTYPE_DEFAULT>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_FUSE: {
            calForce<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_EXFUSE: {
            calForce<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
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
                             jdouble *rBackwardForceCache, jboolean aFixBasis,
                             jint aTypeNum, jdouble aRCut, jint aNMax, jint aWType, jdouble *aFuseWeight, jint aFuseSize) noexcept {
    if (aTypeNum == 1) {
        switch(aWType) {
        case WTYPE_FUSE: {
            calBackwardForce<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_EXFUSE: {
            calBackwardForce<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_NONE:
        case WTYPE_FULL:
        case WTYPE_EXFULL:
        case WTYPE_DEFAULT: {
            calBackwardForce<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch(aWType) {
        case WTYPE_EXFULL: {
            calBackwardForce<WTYPE_EXFULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_FULL: {
            calBackwardForce<WTYPE_FULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_NONE: {
            calBackwardForce<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_DEFAULT: {
            calBackwardForce<WTYPE_DEFAULT>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_FUSE: {
            calBackwardForce<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        case WTYPE_EXFUSE: {
            calBackwardForce<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aFuseWeight, aFuseSize);
            return;
        }
        default: {
            return;
        }}
    }
}

}

#endif //BASIS_CHEBYSHEV_H