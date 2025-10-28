#ifndef BASIS_EQUIVARIANT_SPHERICAL_CHEBYSHEV_H
#define BASIS_EQUIVARIANT_SPHERICAL_CHEBYSHEV_H

#include "basis_EquivariantUtil.hpp"
#include "basis_SphericalChebyshev.hpp"

namespace JSE_NNAP {

template <jint WTYPE, jint FSTYLE>
static void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                  jdouble *rFp, jdouble *rForwardCache, jboolean aFullCache,
                  jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                  jdouble *aFuseWeight, jint aFuseSize,
                  jdouble *aEquWeight, jint *aEquSize, jdouble *aEquScale) noexcept {
    // const init
    jint tSizeN;
    switch(WTYPE) {
    case WTYPE_EXFULL:  {tSizeN = (aTypeNum+1)*(aNMax+1);  break;}
    case WTYPE_FULL:    {tSizeN = aTypeNum*(aNMax+1);      break;}
    case WTYPE_NONE:    {tSizeN = aNMax+1;                 break;}
    case WTYPE_DEFAULT: {tSizeN = (aNMax+aNMax+2);         break;}
    case WTYPE_FUSE:    {tSizeN = aFuseSize*(aNMax+1);     break;}
    case WTYPE_EXFUSE:  {tSizeN = (aFuseSize+1)*(aNMax+1); break;}
    default:            {tSizeN = 0;                       break;}
    }
    const jint tSizeL = (aLMax+1) + L3NCOLS[aL3Max] + L4NCOLS[aL4Max];
    const jint tLMAll = (aLMax+1)*(aLMax+1);
    const jint tSizeCnlm = tSizeN*tLMAll;
    const jint tSizeAnlm = aEquSize[0]*tLMAll;
    const jint tSizeMnlm = aEquSize[0]*tLMAll;
    const jint tSizeHnlm = aEquSize[1]*tLMAll;
    // init cache
    jdouble *rCnlm = rForwardCache;
    jdouble *rAnlm = rCnlm + tSizeCnlm;
    jdouble *rMnlm = rAnlm + tSizeAnlm;
    jdouble *rHnlm = rMnlm + tSizeMnlm;
    jdouble *rForwardCacheElse = rHnlm + tSizeHnlm;
    // clear cnlm first
    fill(rCnlm, 0.0, tSizeCnlm);
    // do cal
    calCnlm<WTYPE, FSTYLE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rCnlm, rForwardCacheElse, aFullCache, aRCut, aNMax, aLMax, aFuseWeight, aFuseSize);
    // clear anlm first
    fill(rAnlm, 0.0, tSizeAnlm);
    // cnlm -> anlm
    mplusAnlm<FSTYLE>(rAnlm, rCnlm, aEquWeight, aEquSize[0], tSizeN, aLMax);
    // scale anlm here
    multiply(rAnlm, aEquScale[0], tSizeAnlm);
    // clear mnlm first
    fill(rMnlm, 0.0, tSizeMnlm);
    // anlm -> mnlm
    mplusMnlm(rMnlm, rAnlm, aEquSize[0], aLMax);
    // clear hnlm first
    fill(rHnlm, 0.0, tSizeHnlm);
    // mnlm -> hnlm
    jdouble *tEquWeight = aEquWeight;
    if (FSTYLE==FUSE_STYLE_LIMITED) {
        tEquWeight += aEquSize[0]*tSizeN;
    } else {
        tEquWeight += aEquSize[0]*tSizeN*(aLMax+1);
    }
    mplusAnlm<FSTYLE>(rHnlm, rAnlm, tEquWeight, aEquSize[1], aEquSize[0], aLMax);
    if (FSTYLE==FUSE_STYLE_LIMITED) {
        tEquWeight += aEquSize[1]*aEquSize[0];
    } else {
        tEquWeight += aEquSize[1]*aEquSize[0]*(aLMax+1);
    }
    mplusAnlm<FSTYLE>(rHnlm, rMnlm, tEquWeight, aEquSize[1], aEquSize[0], aLMax);
    // scale hnlm here
    multiply(rHnlm, aEquScale[1], tSizeHnlm);
    // hnlm -> Pnl
    const jint tShiftL3 = (aLMax+1);
    const jint tShiftL4 = tShiftL3 + L3NCOLS[aL3Max];
    const jint tSizeNp = aEquSize[1];
    for (jint np=0, tShift=0, tShiftFp=0; np<tSizeNp; ++np, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calL2_(rHnlm+tShift, rFp+tShiftFp, aLMax, JNI_FALSE);
        calL3_(rHnlm+tShift, rFp+tShiftFp+tShiftL3, aL3Max, JNI_TRUE);
        calL4_(rHnlm+tShift, rFp+tShiftFp+tShiftL4, aL4Max, JNI_TRUE);
    }
}
template <jint WTYPE, jint FSTYLE>
static void calBackward(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                        jdouble *aGradFp, jdouble *rGradPara, jdouble *aForwardCache, jdouble *rBackwardCache,
                        jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                        jint aFuseSize, jdouble *aEquWeight, jint *aEquSize, jdouble *aEquScale) noexcept {
    // const init
    jint tSizeN;
    switch(WTYPE) {
    case WTYPE_EXFULL:  {tSizeN = (aTypeNum+1)*(aNMax+1);  break;}
    case WTYPE_FULL:    {tSizeN = aTypeNum*(aNMax+1);      break;}
    case WTYPE_NONE:    {tSizeN = aNMax+1;                 break;}
    case WTYPE_DEFAULT: {tSizeN = (aNMax+aNMax+2);         break;}
    case WTYPE_FUSE:    {tSizeN = aFuseSize*(aNMax+1);     break;}
    case WTYPE_EXFUSE:  {tSizeN = (aFuseSize+1)*(aNMax+1); break;}
    default:            {tSizeN = 0;                       break;}
    }
    const jint tSizeL = (aLMax+1) + L3NCOLS[aL3Max] + L4NCOLS[aL4Max];
    const jint tLMAll = (aLMax+1)*(aLMax+1);
    const jint tSizeCnlm = tSizeN*tLMAll;
    const jint tSizeAnlm = aEquSize[0]*tLMAll;
    const jint tSizeMnlm = aEquSize[0]*tLMAll;
    const jint tSizeHnlm = aEquSize[1]*tLMAll;
    // init cache
    jdouble *tCnlm = aForwardCache;
    jdouble *tAnlm = tCnlm + tSizeCnlm;
    jdouble *tMnlm = tAnlm + tSizeAnlm;
    jdouble *tHnlm = tMnlm + tSizeMnlm;
    jdouble *tForwardCacheElse = tHnlm + tSizeHnlm;
    jdouble *rGradCnlm = NULL;
    jdouble *rGradAnlm = NULL;
    if (WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
        rGradCnlm = rBackwardCache;
        rGradAnlm = rGradCnlm + tSizeCnlm;
    } else {
        rGradAnlm = rBackwardCache;
    }
    jdouble *rGradMnlm = rGradAnlm + tSizeAnlm;
    jdouble *rGradHnlm = rGradMnlm + tSizeMnlm;
    jdouble *rBackwardCacheElse = rGradHnlm + tSizeHnlm;
    // cal grad hnlm
    const jint tShiftL3 = (aLMax+1);
    const jint tShiftL4 = tShiftL3 + L3NCOLS[aL3Max];
    const jint tSizeNp = aEquSize[1];
    for (jint np=0, tShift=0, tShiftFp=0; np<tSizeNp; ++np, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calGradL2_(tHnlm+tShift, rGradHnlm+tShift, aGradFp+tShiftFp, aLMax, JNI_FALSE);
        calGradL3_(tHnlm+tShift, rGradHnlm+tShift, aGradFp+tShiftFp+tShiftL3, aL3Max, JNI_TRUE);
        calGradL4_(tHnlm+tShift, rGradHnlm+tShift, aGradFp+tShiftFp+tShiftL4, aL4Max, JNI_TRUE);
    }
    jdouble *tGradParaEqu = rGradPara;
    if (WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
        if (FSTYLE==FUSE_STYLE_LIMITED) {
            tGradParaEqu += aTypeNum*aFuseSize;
        } else {
            tGradParaEqu += aTypeNum*(aNMax+1)*(aLMax+1)*aFuseSize;
        }
    }
    // scale hnlm here
    multiply(rGradHnlm, aEquScale[1], tSizeHnlm);
    // hnlm -> mnlm
    jdouble *tEquWeight = aEquWeight;
    jdouble *tGradParaEqu2 = tGradParaEqu;
    if (FSTYLE==FUSE_STYLE_LIMITED) {
        tEquWeight += aEquSize[0]*tSizeN;
        tGradParaEqu2 += aEquSize[0]*tSizeN;
    } else {
        tEquWeight += aEquSize[0]*tSizeN*(aLMax+1);
        tGradParaEqu2 += aEquSize[0]*tSizeN*(aLMax+1);
    }
    mplusGradParaPostFuse<FSTYLE>(rGradHnlm, tAnlm, tGradParaEqu2, aEquSize[1], aEquSize[0], aLMax);
    mplusGradAnlm<FSTYLE>(rGradHnlm, rGradAnlm, tEquWeight, aEquSize[1], aEquSize[0], aLMax);
    if (FSTYLE==FUSE_STYLE_LIMITED) {
        tEquWeight += aEquSize[1]*aEquSize[0];
        tGradParaEqu2 += aEquSize[1]*aEquSize[0];
    } else {
        tEquWeight += aEquSize[1]*aEquSize[0]*(aLMax+1);
        tGradParaEqu2 += aEquSize[1]*aEquSize[0]*(aLMax+1);
    }
    mplusGradParaPostFuse<FSTYLE>(rGradHnlm, tMnlm, tGradParaEqu2, aEquSize[1], aEquSize[0], aLMax);
    mplusGradAnlm<FSTYLE>(rGradHnlm, rGradMnlm, tEquWeight, aEquSize[1], aEquSize[0], aLMax);
    // mnlm -> anlm
    mplusGradMnlm(rGradMnlm, tAnlm, rGradAnlm, aEquSize[0], aLMax);
    // scale anlm here
    multiply(rGradAnlm, aEquScale[0], tSizeAnlm);
    // anlm -> cnlm
    mplusGradParaPostFuse<FSTYLE>(rGradAnlm, tCnlm, tGradParaEqu, aEquSize[0], tSizeN, aLMax);
    if (WTYPE!=WTYPE_FUSE && WTYPE!=WTYPE_EXFUSE) return;
    mplusGradAnlm<FSTYLE>(rGradAnlm, rGradCnlm, aEquWeight, aEquSize[0], tSizeN, aLMax);
    // plus to para
    calBackwardMainLoop<WTYPE, FSTYLE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rGradPara, rGradCnlm, tForwardCacheElse, rBackwardCacheElse, aRCut, aNMax, aLMax, aFuseSize);
}
template <jint WTYPE, jint FSTYLE>
static void calForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                     jdouble *aNNGrad, jdouble *rFx, jdouble *rFy, jdouble *rFz,
                     jdouble *aForwardCache, jdouble *rForwardForceCache, jboolean aFullCache,
                     jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                     jdouble *aFuseWeight, jint aFuseSize,
                     jdouble *aEquWeight, jint *aEquSize, jdouble *aEquScale) noexcept {
    // const init
    jint tSizeN;
    switch(WTYPE) {
    case WTYPE_EXFULL:  {tSizeN = (aTypeNum+1)*(aNMax+1);  break;}
    case WTYPE_FULL:    {tSizeN = aTypeNum*(aNMax+1);      break;}
    case WTYPE_NONE:    {tSizeN = aNMax+1;                 break;}
    case WTYPE_DEFAULT: {tSizeN = (aNMax+aNMax+2);         break;}
    case WTYPE_FUSE:    {tSizeN = aFuseSize*(aNMax+1);     break;}
    case WTYPE_EXFUSE:  {tSizeN = (aFuseSize+1)*(aNMax+1); break;}
    default:            {tSizeN = 0;                       break;}
    }
    const jint tSizeL = (aLMax+1) + L3NCOLS[aL3Max] + L4NCOLS[aL4Max];
    const jint tLMAll = (aLMax+1)*(aLMax+1);
    const jint tSizeCnlm = tSizeN*tLMAll;
    const jint tSizeAnlm = aEquSize[0]*tLMAll;
    const jint tSizeMnlm = aEquSize[0]*tLMAll;
    const jint tSizeHnlm = aEquSize[1]*tLMAll;
    // init cache
    jdouble *tCnlm = aForwardCache;
    jdouble *tAnlm = tCnlm + tSizeCnlm;
    jdouble *tMnlm = tAnlm + tSizeAnlm;
    jdouble *tHnlm = tMnlm + tSizeMnlm;
    jdouble *tForwardCacheElse = tHnlm + tSizeHnlm;
    jdouble *rGradCnlm = rForwardForceCache;
    jdouble *rGradAnlm = rGradCnlm + tSizeCnlm;
    jdouble *rGradMnlm = rGradAnlm + tSizeAnlm;
    jdouble *rGradHnlm = rGradMnlm + tSizeMnlm;
    jdouble *rForwardForceCacheElse = rGradHnlm + tSizeHnlm;
    // forward need init gradHnlm gradMnlm gradAnlm gradCnlm here
    fill(rGradHnlm, 0.0, tSizeHnlm);
    fill(rGradMnlm, 0.0, tSizeMnlm);
    fill(rGradAnlm, 0.0, tSizeAnlm);
    fill(rGradCnlm, 0.0, tSizeCnlm);
    const jint tShiftL3 = (aLMax+1);
    const jint tShiftL4 = tShiftL3 + L3NCOLS[aL3Max];
    const jint tSizeNp = aEquSize[1];
    for (jint np=0, tShift=0, tShiftFp=0; np<tSizeNp; ++np, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calGradL2_(tHnlm+tShift, rGradHnlm+tShift, aNNGrad+tShiftFp, aLMax, JNI_FALSE);
        calGradL3_(tHnlm+tShift, rGradHnlm+tShift, aNNGrad+tShiftFp+tShiftL3, aL3Max, JNI_TRUE);
        calGradL4_(tHnlm+tShift, rGradHnlm+tShift, aNNGrad+tShiftFp+tShiftL4, aL4Max, JNI_TRUE);
    }
    // scale hnlm here
    multiply(rGradHnlm, aEquScale[1], tSizeHnlm);
    // hnlm -> mnlm
    jdouble *tEquWeight = aEquWeight;
    if (FSTYLE==FUSE_STYLE_LIMITED) {
        tEquWeight += aEquSize[0]*tSizeN;
    } else {
        tEquWeight += aEquSize[0]*tSizeN*(aLMax+1);
    }
    mplusGradAnlm<FSTYLE>(rGradHnlm, rGradAnlm, tEquWeight, aEquSize[1], aEquSize[0], aLMax);
    if (FSTYLE==FUSE_STYLE_LIMITED) {
        tEquWeight += aEquSize[1]*aEquSize[0];
    } else {
        tEquWeight += aEquSize[1]*aEquSize[0]*(aLMax+1);
    }
    mplusGradAnlm<FSTYLE>(rGradHnlm, rGradMnlm, tEquWeight, aEquSize[1], aEquSize[0], aLMax);
    // mnlm -> anlm
    mplusGradMnlm(rGradMnlm, tAnlm, rGradAnlm, aEquSize[0], aLMax);
    // scale anlm here
    multiply(rGradAnlm, aEquScale[0], tSizeAnlm);
    // anlm -> cnlm
    mplusGradAnlm<FSTYLE>(rGradAnlm, rGradCnlm, aEquWeight, aEquSize[0], tSizeN, aLMax);
    calForceMainLoop<WTYPE, FSTYLE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rGradCnlm, rFx, rFy, rFz, tForwardCacheElse, rForwardForceCacheElse, aFullCache, aRCut, aNMax, aLMax, aFuseWeight, aFuseSize);
}
template <jint WTYPE, jint FSTYLE>
static void calBackwardForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                             jdouble *aNNGrad, jdouble *aGradFx, jdouble *aGradFy, jdouble *aGradFz,
                             jdouble *rGradNNGrad, jdouble *rGradPara,
                             jdouble *aForwardCache, jdouble *aForwardForceCache,
                             jdouble *rBackwardCache, jdouble *rBackwardForceCache, jboolean aFixBasis,
                             jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                             jdouble *aFuseWeight, jint aFuseSize,
                             jdouble *aEquWeight, jint *aEquSize, jdouble *aEquScale) noexcept {
    // const init
    jint tSizeN;
    switch(WTYPE) {
    case WTYPE_EXFULL:  {tSizeN = (aTypeNum+1)*(aNMax+1);  break;}
    case WTYPE_FULL:    {tSizeN = aTypeNum*(aNMax+1);      break;}
    case WTYPE_NONE:    {tSizeN = aNMax+1;                 break;}
    case WTYPE_DEFAULT: {tSizeN = (aNMax+aNMax+2);         break;}
    case WTYPE_FUSE:    {tSizeN = aFuseSize*(aNMax+1);     break;}
    case WTYPE_EXFUSE:  {tSizeN = (aFuseSize+1)*(aNMax+1); break;}
    default:            {tSizeN = 0;                       break;}
    }
    const jint tSizeL = (aLMax+1) + L3NCOLS[aL3Max] + L4NCOLS[aL4Max];
    const jint tLMAll = (aLMax+1)*(aLMax+1);
    const jint tSizeCnlm = tSizeN*tLMAll;
    const jint tSizeAnlm = aEquSize[0]*tLMAll;
    const jint tSizeMnlm = aEquSize[0]*tLMAll;
    const jint tSizeHnlm = aEquSize[1]*tLMAll;
    // init cache
    jdouble *tCnlm = aForwardCache;
    jdouble *tAnlm = tCnlm + tSizeCnlm;
    jdouble *tMnlm = tAnlm + tSizeAnlm;
    jdouble *tHnlm = tMnlm + tSizeMnlm;
    jdouble *tForwardCacheElse = tHnlm + tSizeHnlm;
    jdouble *tNNGradCnlm = aForwardForceCache;
    jdouble *tNNGradAnlm = tNNGradCnlm + tSizeCnlm;
    jdouble *tNNGradMnlm = tNNGradAnlm + tSizeAnlm;
    jdouble *tNNGradHnlm = tNNGradMnlm + tSizeMnlm;
    jdouble *tForwardForceCacheElse = tNNGradHnlm + tSizeHnlm;
    jdouble *rGradAnlm = NULL;
    if (WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
        rGradAnlm = rBackwardCache + tSizeCnlm;
    } else {
        rGradAnlm = rBackwardCache;
    }
    jdouble *rGradHnlm = rGradAnlm + tSizeAnlm + tSizeMnlm;
    jdouble *rBackwardCacheElse = rGradHnlm + tSizeHnlm;
    jdouble *rGradNNGradCnlm = rBackwardForceCache;
    jdouble *rGradNNGradAnlm = rGradNNGradCnlm + tSizeCnlm;
    jdouble *rGradNNGradMnlm = rGradNNGradAnlm + tSizeAnlm;
    jdouble *rGradNNGradHnlm = rGradNNGradMnlm + tSizeMnlm;
    jdouble *rBackwardForceCacheElse = rGradNNGradHnlm + tSizeHnlm;
    
    // cal rGradNNGradCnlm
    calBackwardForceMainLoop<WTYPE, FSTYLE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rGradNNGradCnlm, aGradFx, aGradFy, aGradFz, tNNGradCnlm, rGradPara, tForwardCacheElse, tForwardForceCacheElse, rBackwardCacheElse, rBackwardForceCacheElse, aFixBasis, aRCut, aNMax, aLMax, aFuseWeight, aFuseSize);
    
    jdouble *tGradParaEqu = rGradPara;
    if (!aFixBasis) if (WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
        if (FSTYLE==FUSE_STYLE_LIMITED) {
            tGradParaEqu += aTypeNum*aFuseSize;
        } else {
            tGradParaEqu += aTypeNum*(aNMax+1)*(aLMax+1)*aFuseSize;
        }
    }
    // cnlm -> anlm
    if (!aFixBasis) {
        mplusGradParaPostFuse<FSTYLE>(tNNGradAnlm, rGradNNGradCnlm, tGradParaEqu, aEquSize[0], tSizeN, aLMax);
    }
    mplusAnlm<FSTYLE>(rGradNNGradAnlm, rGradNNGradCnlm, aEquWeight, aEquSize[0], tSizeN, aLMax);
    // scale anlm here
    multiply(rGradNNGradAnlm, aEquScale[0], tSizeAnlm);
    // anlm -> mnlm
    if (!aFixBasis) {
        mplusGradMnlmAnlm(tNNGradMnlm, rGradAnlm, rGradNNGradAnlm, aEquSize[0], aLMax);
    }
    mplusGradNNGradMnlm(rGradNNGradMnlm, tAnlm, rGradNNGradAnlm, aEquSize[0], aLMax);
    // mnlm -> hnlm
    if (!aFixBasis) {
        jdouble *tGradParaEqu2 = tGradParaEqu;
        if (FSTYLE==FUSE_STYLE_LIMITED) {
            tGradParaEqu2 += aEquSize[0]*tSizeN;
        } else {
            tGradParaEqu2 += aEquSize[0]*tSizeN*(aLMax+1);
        }
        mplusGradParaPostFuse<FSTYLE>(tNNGradHnlm, rGradNNGradAnlm, tGradParaEqu2, aEquSize[1], aEquSize[0], aLMax);
        if (FSTYLE==FUSE_STYLE_LIMITED) {
            tGradParaEqu2 += aEquSize[1]*aEquSize[0];
        } else {
            tGradParaEqu2 += aEquSize[1]*aEquSize[0]*(aLMax+1);
        }
        mplusGradParaPostFuse<FSTYLE>(tNNGradHnlm, rGradNNGradMnlm, tGradParaEqu2, aEquSize[1], aEquSize[0], aLMax);
    }
    jdouble *tEquWeight = aEquWeight;
    if (FSTYLE==FUSE_STYLE_LIMITED) {
        tEquWeight += aEquSize[0]*tSizeN;
    } else {
        tEquWeight += aEquSize[0]*tSizeN*(aLMax+1);
    }
    mplusAnlm<FSTYLE>(rGradNNGradHnlm, rGradNNGradAnlm, tEquWeight, aEquSize[1], aEquSize[0], aLMax);
    if (FSTYLE==FUSE_STYLE_LIMITED) {
        tEquWeight += aEquSize[1]*aEquSize[0];
    } else {
        tEquWeight += aEquSize[1]*aEquSize[0]*(aLMax+1);
    }
    mplusAnlm<FSTYLE>(rGradNNGradHnlm, rGradNNGradMnlm, tEquWeight, aEquSize[1], aEquSize[0], aLMax);
    // scale hnlm here
    multiply(rGradNNGradHnlm, aEquScale[1], tSizeHnlm);
    // grad grad hnlm to grad grad fp
    const jint tShiftL3 = (aLMax+1);
    const jint tShiftL4 = tShiftL3 + L3NCOLS[aL3Max];
    const jint tSizeNp = aEquSize[1];
    for (jint np=0, tShift=0, tShiftFp=0; np<tSizeNp; ++np, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calGradNNGradL2_(tHnlm+tShift, rGradNNGradHnlm+tShift, rGradNNGrad+tShiftFp, aLMax, JNI_FALSE);
        calGradNNGradL3_(tHnlm+tShift, rGradNNGradHnlm+tShift, rGradNNGrad+tShiftFp+tShiftL3, aL3Max, JNI_TRUE);
        calGradNNGradL4_(tHnlm+tShift, rGradNNGradHnlm+tShift, rGradNNGrad+tShiftFp+tShiftL4, aL4Max, JNI_TRUE);
    }
    if (!aFixBasis) for (jint np=0, tShift=0, tShiftFp=0; np<tSizeNp; ++np, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calGradCnlmL2_(rGradHnlm+tShift, rGradNNGradHnlm+tShift, aNNGrad+tShiftFp, aLMax, JNI_FALSE);
        calGradCnlmL3_(tHnlm+tShift, rGradHnlm+tShift, rGradNNGradHnlm+tShift, aNNGrad+tShiftFp+tShiftL3, aL3Max, JNI_TRUE);
        calGradCnlmL4_(tHnlm+tShift, rGradHnlm+tShift, rGradNNGradHnlm+tShift, aNNGrad+tShiftFp+tShiftL4, aL4Max, JNI_TRUE);
    }
}


template <jint WTYPE>
static void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                  jdouble *rFp, jdouble *rForwardCache, jboolean aFullCache,
                  jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                  jint aFuseStyle, jdouble *aFuseWeight, jint aFuseSize,
                  jdouble *aEquWeight, jint *aEquSize, jdouble *aEquScale) noexcept {
    if (aFuseStyle==FUSE_STYLE_LIMITED) {
        calFp<WTYPE, FUSE_STYLE_LIMITED>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
    } else {
        calFp<WTYPE, FUSE_STYLE_EXTENSIVE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
    }
}
static void calFp(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                  jdouble *rFp, jdouble *rForwardCache, jboolean aFullCache,
                  jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                  jint aWType, jint aFuseStyle, jdouble *aFuseWeight, jint aFuseSize,
                  jdouble *aEquWeight, jint *aEquSize, jdouble *aEquScale) noexcept {
    if (aTypeNum == 1) {
        switch(aWType) {
        case WTYPE_FUSE: {
            calFp<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_EXFUSE: {
            calFp<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_NONE:
        case WTYPE_FULL:
        case WTYPE_EXFULL:
        case WTYPE_DEFAULT: {
            calFp<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch(aWType) {
        case WTYPE_EXFULL: {
            calFp<WTYPE_EXFULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_FULL: {
            calFp<WTYPE_FULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_NONE: {
            calFp<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_DEFAULT: {
            calFp<WTYPE_DEFAULT>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_FUSE: {
            calFp<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_EXFUSE: {
            calFp<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, rFp, rForwardCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
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
                        jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                        jint aFuseStyle, jint aFuseSize,
                        jdouble *aEquWeight, jint *aEquSize, jdouble *aEquScale) noexcept {
    if (aFuseStyle==FUSE_STYLE_LIMITED) {
        calBackward<WTYPE, FUSE_STYLE_LIMITED>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseSize, aEquWeight, aEquSize, aEquScale);
    } else {
        calBackward<WTYPE, FUSE_STYLE_EXTENSIVE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseSize, aEquWeight, aEquSize, aEquScale);
    }
}
static void calBackward(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                        jdouble *aGradFp, jdouble *rGradPara, jdouble *aForwardCache, jdouble *rBackwardCache,
                        jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                        jint aWType, jint aFuseStyle, jint aFuseSize,
                        jdouble *aEquWeight, jint *aEquSize, jdouble *aEquScale) noexcept {
    if (aTypeNum == 1) {
        switch(aWType) {
        case WTYPE_FUSE: {
            calBackward<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_EXFUSE: {
            calBackward<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_NONE:
        case WTYPE_FULL:
        case WTYPE_EXFULL:
        case WTYPE_DEFAULT: {
            calBackward<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch(aWType) {
        case WTYPE_EXFULL: {
            calBackward<WTYPE_EXFULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_FULL: {
            calBackward<WTYPE_FULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_NONE: {
            calBackward<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_DEFAULT: {
            calBackward<WTYPE_DEFAULT>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_FUSE: {
            calBackward<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_EXFUSE: {
            calBackward<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aGradFp, rGradPara, aForwardCache, rBackwardCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        default: {
            return;
        }}
    }
}
template <jint WTYPE>
static void calForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                     jdouble *aNNGrad, jdouble *rFx, jdouble *rFy, jdouble *rFz,
                     jdouble *aForwardCache, jdouble *rForwardForceCache, jboolean aFullCache,
                     jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                     jint aFuseStyle, jdouble *aFuseWeight, jint aFuseSize,
                     jdouble *aEquWeight, jint *aEquSize, jdouble *aEquScale) noexcept {
    if (aFuseStyle==FUSE_STYLE_LIMITED) {
        calForce<WTYPE, FUSE_STYLE_LIMITED>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
    } else {
        calForce<WTYPE, FUSE_STYLE_EXTENSIVE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
    }
}
static void calForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                     jdouble *aNNGrad, jdouble *rFx, jdouble *rFy, jdouble *rFz,
                     jdouble *aForwardCache, jdouble *rForwardForceCache, jboolean aFullCache,
                     jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                     jint aWType, jint aFuseStyle, jdouble *aFuseWeight, jint aFuseSize,
                     jdouble *aEquWeight, jint *aEquSize, jdouble *aEquScale) noexcept {
    if (aTypeNum == 1) {
        switch(aWType) {
        case WTYPE_FUSE: {
            calForce<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_EXFUSE: {
            calForce<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_NONE:
        case WTYPE_FULL:
        case WTYPE_EXFULL:
        case WTYPE_DEFAULT: {
            calForce<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch(aWType) {
        case WTYPE_EXFULL: {
            calForce<WTYPE_EXFULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_FULL: {
            calForce<WTYPE_FULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_NONE: {
            calForce<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_DEFAULT: {
            calForce<WTYPE_DEFAULT>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_FUSE: {
            calForce<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_EXFUSE: {
            calForce<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, rFx, rFy, rFz, aForwardCache, rForwardForceCache, aFullCache, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
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
                             jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                             jint aFuseStyle, jdouble *aFuseWeight, jint aFuseSize,
                             jdouble *aEquWeight, jint *aEquSize, jdouble *aEquScale) noexcept {
    if (aFuseStyle==FUSE_STYLE_LIMITED) {
        calBackwardForce<WTYPE, FUSE_STYLE_LIMITED>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
    } else {
        calBackwardForce<WTYPE, FUSE_STYLE_EXTENSIVE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
    }
}
static void calBackwardForce(jdouble *aNlDx, jdouble *aNlDy, jdouble *aNlDz, jint *aNlType, jint aNN,
                             jdouble *aNNGrad, jdouble *aGradFx, jdouble *aGradFy, jdouble *aGradFz,
                             jdouble *rGradNNGrad, jdouble *rGradPara,
                             jdouble *aForwardCache, jdouble *aForwardForceCache,
                             jdouble *rBackwardCache, jdouble *rBackwardForceCache, jboolean aFixBasis,
                             jint aTypeNum, jdouble aRCut, jint aNMax, jint aLMax, jint aL3Max, jint aL4Max,
                             jint aWType, jint aFuseStyle, jdouble *aFuseWeight, jint aFuseSize,
                             jdouble *aEquWeight, jint *aEquSize, jdouble *aEquScale) noexcept {
    if (aTypeNum == 1) {
        switch(aWType) {
        case WTYPE_FUSE: {
            calBackwardForce<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_EXFUSE: {
            calBackwardForce<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_NONE:
        case WTYPE_FULL:
        case WTYPE_EXFULL:
        case WTYPE_DEFAULT: {
            calBackwardForce<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        default: {
            return;
        }}
    } else {
        switch(aWType) {
        case WTYPE_EXFULL: {
            calBackwardForce<WTYPE_EXFULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_FULL: {
            calBackwardForce<WTYPE_FULL>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_NONE: {
            calBackwardForce<WTYPE_NONE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_DEFAULT: {
            calBackwardForce<WTYPE_DEFAULT>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_FUSE: {
            calBackwardForce<WTYPE_FUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        case WTYPE_EXFUSE: {
            calBackwardForce<WTYPE_EXFUSE>(aNlDx, aNlDy, aNlDz, aNlType, aNN, aNNGrad, aGradFx, aGradFy, aGradFz, rGradNNGrad, rGradPara, aForwardCache, aForwardForceCache, rBackwardCache, rBackwardForceCache, aFixBasis, aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL4Max, aFuseStyle, aFuseWeight, aFuseSize, aEquWeight, aEquSize, aEquScale);
            return;
        }
        default: {
            return;
        }}
    }
}

}

#endif //BASIS_EQUIVARIANT_SPHERICAL_CHEBYSHEV_H