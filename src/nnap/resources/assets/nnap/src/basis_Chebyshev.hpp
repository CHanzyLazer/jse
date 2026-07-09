#ifndef BASIS_CHEBYSHEV_H
#define BASIS_CHEBYSHEV_H

#include "basis_ChebyshevUtil.hpp"

namespace JSE_NNAP {

template <int WTYPE, int MTYPE, int NMAX, int SIZE_NP>
static NNAP_DEVICE void chebyForwardGpu(int nb, int bi,
    int aNlSize, int *aBufNl, flt_t *rFp,
    flt_t *posx, flt_t *posy, flt_t *posz, int *type,
    flt_t aRCut, flt_t *aParams) noexcept {
    // init cache
    flt_t bRn[NMAX+1];
    flt_t bRnp[SIZE_NP];
    // clear fp first
    fill<SIZE_NP>(rFp, ZERO);
    // loop for neighbor
    const flt_t xi = posx[bi];
    const flt_t yi = posy[bi];
    const flt_t zi = posz[bi];
    const int typei = type[bi];
    for (int jj = 0; jj < aNlSize; ++jj) {
        const int j = aBufNl[jj*nb + bi];
        const flt_t dx = posx[j] - xi;
        const flt_t dy = posy[j] - yi;
        const flt_t dz = posz[j] - zi;
        const flt_t dis = nnap_sqrt(dx*dx + dy*dy + dz*dz);
        // check rcut for merge
        if (dis >= aRCut) continue;
        // mirror stuff
        int typej = type[j];
        if (MTYPE > 0) {
            if (typej==MTYPE) typej = typei;
            else if (typej==typei) typej = MTYPE;
        }
        // cal Rn, fc
        calRn<NMAX>(bRn, dis, aRCut);
        flt_t fc = calFc(dis, aRCut);
        // cal Rnp
        const int tParamShift = (typej-1)*(SIZE_NP*(NMAX+1));
        calRnp<NMAX, SIZE_NP>(bRnp, bRn, aParams+tParamShift);
        // Rn to fp
        mplusFp<SIZE_NP>(rFp, fc, bRnp);
    }
}
template <int VEITHER, int VATOM, int CVATOM, int WTYPE, int MTYPE, int NMAX, int SIZE_NP>
static NNAP_DEVICE void chebyBackwardGpu(int nb, int bi,
    int aNlSize, int *aBufNl, flt_t *aAGradFp,
    flt_t *posx, flt_t *posy, flt_t *posz, int *type,
    flt_t *fx, flt_t *fy, flt_t *fz,
    flt_t *v0xx, flt_t *v0yy, flt_t *v0zz, flt_t *v0xy, flt_t *v0xz, flt_t *v0yz,
    flt_t *v1xx, flt_t *v1yy, flt_t *v1zz, flt_t *v1xy, flt_t *v1xz, flt_t *v1yz, flt_t *v1yx, flt_t *v1zx, flt_t *v1zy,
    flt_t aRCut, flt_t *aParams) noexcept {
    // init cache
    flt_t bRn[NMAX+1], bAGradRn[NMAX+1];
    flt_t bRnp[SIZE_NP];
    // loop for neighbor
    flt_t f0xi = ZERO;
    flt_t f0yi = ZERO;
    flt_t f0zi = ZERO;
    const flt_t xi = posx[bi];
    const flt_t yi = posy[bi];
    const flt_t zi = posz[bi];
    const int typei = type[bi];
    for (int jj = 0; jj < aNlSize; ++jj) {
        const int j = aBufNl[jj*nb + bi];
        const flt_t dx = posx[j] - xi;
        const flt_t dy = posy[j] - yi;
        const flt_t dz = posz[j] - zi;
        const flt_t dis = nnap_sqrt(dx*dx + dy*dy + dz*dz);
        // check rcut for merge
        if (dis >= aRCut) continue;
        // mirror stuff
        int typej = type[j];
        if (MTYPE > 0) {
            if (typej==MTYPE) typej = typei;
            else if (typej==typei) typej = MTYPE;
        }
        // cal Rn, fc
        calRn<NMAX>(bRn, dis, aRCut);
        flt_t fc = calFc(dis, aRCut);
        // cal Rnp
        const int tParamShift = (typej-1)*(SIZE_NP*(NMAX+1));
        calRnp<NMAX, SIZE_NP>(bRnp, bRn, aParams+tParamShift);
        
        // backward in single loop for save cache
        flt_t rAGradFc = ZERO;
        fill<NMAX+1>(bAGradRn, ZERO);
        flt_t *tWeight = aParams+tParamShift;
        for (int np = 0; np < SIZE_NP; ++np) {
            const flt_t subAGradFp = aAGradFp[np];
            rAGradFc += subAGradFp*bRnp[np];
            mplus<NMAX+1>(bAGradRn, subAGradFp*fc, tWeight);
            tWeight += (NMAX+1);
        }
        // cal RnGrad, fcGrag
        flt_t *tRnGrad = bRn;
        calRnGrad<NMAX>(tRnGrad, dis, aRCut);
        flt_t fcGrad = calFcGrad(dis, aRCut);
        flt_t rAGradj = dot<NMAX+1>(bAGradRn, tRnGrad);
        rAGradj += rAGradFc*fcGrad;
        // to grad xyz
        const flt_t fxj = rAGradj*dx;
        const flt_t fyj = rAGradj*dy;
        const flt_t fzj = rAGradj*dz;
        f0xi -= fxj; f0yi -= fyj; f0zi -= fzj;
        atomicAdd(fx + j, fxj);
        atomicAdd(fy + j, fyj);
        atomicAdd(fz + j, fzj);
        if (VEITHER) {
            const flt_t vxxj = dx*fxj, vyyj = dy*fyj, vzzj = dz*fzj;
            const flt_t vxyj = dx*fyj, vxzj = dx*fzj, vyzj = dy*fzj;
            v0xx[bi] += vxxj; v0yy[bi] += vyyj; v0zz[bi] += vzzj;
            v0xy[bi] += vxyj; v0xz[bi] += vxzj; v0yz[bi] += vyzj;
            if (CVATOM) {
                atomicAdd(v1xx + j, vxxj);
                atomicAdd(v1yy + j, vyyj);
                atomicAdd(v1zz + j, vzzj);
                atomicAdd(v1xy + j, vxyj);
                atomicAdd(v1xz + j, vxzj);
                atomicAdd(v1yz + j, vyzj);
                atomicAdd(v1yx + j, dy*fxj);
                atomicAdd(v1zx + j, dz*fxj);
                atomicAdd(v1zy + j, dz*fyj);
            } else if (VATOM) {
                atomicAdd(v1xx + j, vxxj);
                atomicAdd(v1yy + j, vyyj);
                atomicAdd(v1zz + j, vzzj);
                atomicAdd(v1xy + j, vxyj);
                atomicAdd(v1xz + j, vxzj);
                atomicAdd(v1yz + j, vyzj);
            }
        }
    }
    atomicAdd(fx + bi, f0xi);
    atomicAdd(fy + bi, f0yi);
    atomicAdd(fz + bi, f0zi);
}



template <int WTYPE, int MTYPE, int NMAX, int SIZE_NP, int REQUIRE_CACHE>
static void chebyForward(int bi,
    flt4_t *aPosType, int aNlSize, int *aNl, flt_t *rFp,
    flt_t **rForwardCache, flt_t aRCut, flt_t *aParams) noexcept {
    // init cache
    flt_t bRn[REQUIRE_CACHE ? 1 : (NMAX+1)]; flt_t *rRn = REQUIRE_CACHE ? NULL : bRn;
    flt_t bRnp[REQUIRE_CACHE ? 1 : SIZE_NP]; flt_t *rRnp = REQUIRE_CACHE ? NULL : bRnp;
    flt_t *rNlFc = NULL, *rNlRn = NULL, *rNlRnp = NULL;
    if (REQUIRE_CACHE) {
        rNlFc = *rForwardCache; *rForwardCache += aNlSize;
        rNlRn = *rForwardCache; *rForwardCache += aNlSize*(NMAX+1);
        rNlRnp = *rForwardCache; *rForwardCache += aNlSize*SIZE_NP;
    }
    // clear fp first
    fill<SIZE_NP>(rFp, ZERO);
    // loop for neighbor
    const flt4_t cinfo = aPosType[bi];
    for (int jj = 0; jj < aNlSize; ++jj) {
        const int j = aNl[jj];
        const flt4_t jinfo = aPosType[j];
        const flt_t dx = jinfo.x - cinfo.x;
        const flt_t dy = jinfo.y - cinfo.y;
        const flt_t dz = jinfo.z - cinfo.z;
        const flt_t dis = nnap_sqrt(dx*dx + dy*dy + dz*dz);
        // check rcut for merge
        if (dis >= aRCut) continue;
        // mirror stuff
        int type = (int)jinfo.w;
        if (MTYPE > 0) {
            const int ctype = (int)cinfo.w;
            if (type==MTYPE) type = ctype;
            else if (type==ctype) type = MTYPE;
        }
        // cal Rn, fc
        if (REQUIRE_CACHE) rRn = rNlRn + jj*(NMAX+1);
        calRn<NMAX>(rRn, dis, aRCut);
        flt_t fc = calFc(dis, aRCut);
        if (REQUIRE_CACHE) rNlFc[jj] = fc;
        // Rn to fp
        if (WTYPE==WTYPE_RFUSE || WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
            const int tParamShift = (type-1)*(SIZE_NP*(NMAX+1));
            // cal Rnp
            if (REQUIRE_CACHE) rRnp = rNlRnp + jj*SIZE_NP;
            calRnp<NMAX, SIZE_NP>(rRnp, rRn, aParams+tParamShift);
            mplusFp<SIZE_NP>(rFp, fc, rRnp);
        } else
        if (WTYPE==WTYPE_NONE) {
            mplusFp<NMAX+1>(rFp, fc, rRn);
        } else
        if (WTYPE==WTYPE_FULL) {
            flt_t *tFp = rFp + (type-1)*(NMAX+1);
            mplusFp<NMAX+1>(tFp, fc, rRn);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            flt_t *tFpWt = rFp + type*(NMAX+1);
            mplusFpWt<NMAX+1>(rFp, tFpWt, ONE, fc, rRn);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            double wt = ((type&1)==1) ? type : (-type);
            flt_t *tFpWt = rFp + (NMAX+1);
            mplusFpWt<NMAX+1>(rFp, tFpWt, wt, fc, rRn);
        }
    }
}

template <int WTYPE, int MTYPE, int NMAX, int SIZE_NP, int GRAD_PARAM, int USE_BB, int REQUIRE_CACHE>
static void chebyBackward(int bi,
    flt4_t *aPosType, int aNlSize, int *aNl,flt_t *aAGradFp,
    flt_t *rAGradNlDx, flt_t *rAGradNlDy, flt_t *rAGradNlDz,
    flt_t **aForwardCache, flt_t **rBackwardCache, flt_t **rBackwardBackwardCache,
    flt_t aRCut, flt_t *aParams, flt_t *rAGradParams) noexcept {
    static_assert(!(GRAD_PARAM && REQUIRE_CACHE), "INVALID STATE");
    static_assert(!(USE_BB && REQUIRE_CACHE), "INVALID STATE");
    static_assert(!(!GRAD_PARAM && USE_BB), "INVALID STATE");
    if (GRAD_PARAM) {
        // no param
        if (WTYPE!=WTYPE_RFUSE && WTYPE!=WTYPE_FUSE && WTYPE!=WTYPE_EXFUSE) {
            // aForwardCache shift required
            *aForwardCache += aNlSize;
            *aForwardCache += aNlSize*(NMAX+1);
            *aForwardCache += aNlSize*SIZE_NP;
            if (USE_BB) {
                // rBackwardBackwardCache shift required
                *rBackwardBackwardCache += aNlSize*SIZE_NP;
            }
            return;
        }
    }
    // init cache
    flt_t *tNlFc = *aForwardCache; *aForwardCache += aNlSize;
    flt_t *tNlRn = *aForwardCache; *aForwardCache += aNlSize*(NMAX+1);
    flt_t *tNlRnp = *aForwardCache; *aForwardCache += aNlSize*SIZE_NP;
    flt_t bRnGrad[REQUIRE_CACHE ? 1 : (NMAX+1)]; flt_t *rRnGrad = REQUIRE_CACHE ? NULL : bRnGrad;
    flt_t bAGradRnp[REQUIRE_CACHE ? 1 : SIZE_NP]; flt_t *rAGradRnp = REQUIRE_CACHE ? NULL : bAGradRnp;
    flt_t *rNlFcGrad = NULL, *rNlRnGrad = NULL, *rNlAGradRnp = NULL;
    if (REQUIRE_CACHE) {
        rNlFcGrad = *rBackwardCache; *rBackwardCache += aNlSize;
        rNlRnGrad = *rBackwardCache; *rBackwardCache += aNlSize*(NMAX+1);
        rNlAGradRnp = *rBackwardCache; *rBackwardCache += aNlSize*SIZE_NP;
    }
    if (USE_BB) {
        rNlAGradRnp = *rBackwardBackwardCache; *rBackwardBackwardCache += aNlSize*SIZE_NP;
    }
    flt_t rAGradRn[NMAX+1];
    // loop for neighbor
    const flt4_t cinfo = aPosType[bi];
    for (int jj = 0; jj < aNlSize; ++jj) {
        const int j = aNl[jj];
        const flt4_t jinfo = aPosType[j];
        const flt_t dx = jinfo.x - cinfo.x;
        const flt_t dy = jinfo.y - cinfo.y;
        const flt_t dz = jinfo.z - cinfo.z;
        const flt_t dis = nnap_sqrt(dx*dx + dy*dy + dz*dz);
        // check rcut for merge
        if (dis >= aRCut) continue;
        // mirror stuff
        int type = (int)jinfo.w;
        if (MTYPE > 0) {
            const int ctype = (int)cinfo.w;
            if (type==MTYPE) type = ctype;
            else if (type==ctype) type = MTYPE;
        }
        // get Rn, fc
        flt_t *tRn = tNlRn + jj*(NMAX+1);
        flt_t fc = tNlFc[jj];
        // gradFp to gradRn & gradFc
        flt_t rAGradFc = ZERO;
        fill<NMAX+1>(rAGradRn, ZERO);
        if (WTYPE==WTYPE_RFUSE || WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
            const int tParamShift = (type-1)*(SIZE_NP*(NMAX+1));
            // get Rnp
            flt_t *tRnp = tNlRnp + jj*SIZE_NP;
            // cache grad Rnp
            if (REQUIRE_CACHE || USE_BB) rAGradRnp = rNlAGradRnp + jj*SIZE_NP;
            if (!USE_BB) fill<SIZE_NP>(rAGradRnp, ZERO);
            backwardMplusFp<SIZE_NP>(aAGradFp, fc, rAGradFc, tRnp, rAGradRnp);
            backwardRnp<NMAX, SIZE_NP, GRAD_PARAM, !GRAD_PARAM>(
                rAGradRnp, tRn, rAGradRn,
                aParams+tParamShift,
                GRAD_PARAM ? (rAGradParams+tParamShift) : NULL
            );
        } else
        if (WTYPE==WTYPE_NONE) {
            backwardMplusFp<NMAX+1>(aAGradFp, fc, rAGradFc, tRn, rAGradRn);
        } else
        if (WTYPE==WTYPE_FULL) {
            flt_t *tAGradFp = aAGradFp + (type-1)*(NMAX+1);
            backwardMplusFp<NMAX+1>(tAGradFp, fc, rAGradFc, tRn, rAGradRn);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            flt_t *tAGradFpWt = aAGradFp + type*(NMAX+1);
            backwardMplusFpWt<NMAX+1>(aAGradFp, tAGradFpWt, ONE, fc, rAGradFc, tRn, rAGradRn);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            double wt = ((type&1)==1) ? type : (-type);
            flt_t *tAGradFpWt = aAGradFp + (NMAX+1);
            backwardMplusFpWt<NMAX+1>(aAGradFp, tAGradFpWt, wt, fc, rAGradFc, tRn, rAGradRn);
        }
        // gradRn, gradFc to grad xyz
        if (!GRAD_PARAM) {
            // cal RnGrad, fcGrag
            if (REQUIRE_CACHE) rRnGrad = rNlRnGrad + jj*(NMAX+1);
            calRnGrad<NMAX>(rRnGrad, dis, aRCut);
            flt_t fcGrad = calFcGrad(dis, aRCut);
            if (REQUIRE_CACHE) rNlFcGrad[jj] = fcGrad;
            flt_t rAGradj = dot<NMAX+1>(rAGradRn, rRnGrad);
            rAGradj += rAGradFc*fcGrad;
            rAGradNlDx[jj] += rAGradj*dx;
            rAGradNlDy[jj] += rAGradj*dy;
            rAGradNlDz[jj] += rAGradj*dz;
        }
    }
}

template <int WTYPE, int MTYPE, int NMAX, int SIZE_NP>
static void chebyBackwardBackward(int bi,
    flt4_t *aPosType, int aNlSize, int *aNl, flt_t *aAGradFp, flt_t *rBGradAGradFp,
    flt_t *aBGradAGradNlDx, flt_t *aBGradAGradNlDy, flt_t *aBGradAGradNlDz,
    flt_t **aForwardCache, flt_t **aBackwardCache, flt_t **rBackwardBackwardCache,
    flt_t aRCut, flt_t *aParams, flt_t *rBGradParams) noexcept {
    // init cache
    flt_t *tNlFc = *aForwardCache; *aForwardCache += aNlSize;
    flt_t *tNlRn = *aForwardCache; *aForwardCache += aNlSize*(NMAX+1);
    flt_t *tNlRnp = *aForwardCache; *aForwardCache += aNlSize*SIZE_NP;
    flt_t *tNlFcGrad = *aBackwardCache; *aBackwardCache += aNlSize;
    flt_t *tNlRnGrad = *aBackwardCache; *aBackwardCache += aNlSize*(NMAX+1);
    flt_t *tNlAGradRnp = *aBackwardCache; *aBackwardCache += aNlSize*SIZE_NP;
    flt_t *rNlBGradRnp = *rBackwardBackwardCache; *rBackwardBackwardCache += aNlSize*SIZE_NP;
    flt_t rBGradAGradRn[NMAX+1], rBGradAGradRnp[SIZE_NP];
    // loop for neighbor
    const flt4_t cinfo = aPosType[bi];
    for (int jj = 0; jj < aNlSize; ++jj) {
        const int j = aNl[jj];
        const flt4_t jinfo = aPosType[j];
        const flt_t dx = jinfo.x - cinfo.x;
        const flt_t dy = jinfo.y - cinfo.y;
        const flt_t dz = jinfo.z - cinfo.z;
        const flt_t dis = nnap_sqrt(dx*dx + dy*dy + dz*dz);
        // check rcut for merge
        if (dis >= aRCut) continue;
        // mirror stuff
        int type = (int)jinfo.w;
        if (MTYPE > 0) {
            const int ctype = (int)cinfo.w;
            if (type==MTYPE) type = ctype;
            else if (type==ctype) type = MTYPE;
        }
        // get Rn, fc
        flt_t *tRn = tNlRn + jj*(NMAX+1);
        flt_t fc = tNlFc[jj];
        // get RnGrad, fcGrad
        flt_t *tRnGrad = tNlRnGrad + jj*(NMAX+1);
        flt_t fcGrad = tNlFcGrad[jj];
        // grad grad xyz to grad grad fc & Rn
        const flt_t tBGradAGradj = aBGradAGradNlDx[jj]*dx + aBGradAGradNlDy[jj]*dy + aBGradAGradNlDz[jj]*dz;
        fill<NMAX+1>(rBGradAGradRn, ZERO);
        flt_t tBGradAGradFc = tBGradAGradj*fcGrad;
        mplus<NMAX+1>(rBGradAGradRn, tBGradAGradj, tRnGrad);
        // grad grad fc & Rn to grad grad fp
        if (WTYPE==WTYPE_RFUSE || WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
            const int tParamShift = (type-1)*(SIZE_NP*(NMAX+1));
            // get gradRnp
            flt_t *tAGradRnp = tNlAGradRnp + jj*SIZE_NP;
            fill<SIZE_NP>(rBGradAGradRnp, ZERO);
            backwardBackwardRnp<NMAX, SIZE_NP>(
                tAGradRnp, rBGradAGradRnp, rBGradAGradRn,
                aParams+tParamShift, rBGradParams+tParamShift
            );
            // get Rnp
            flt_t *tRnp = tNlRnp + jj*SIZE_NP;
            flt_t *rBGradRnp = rNlBGradRnp + jj*SIZE_NP;
            fill<SIZE_NP>(rBGradRnp, ZERO);
            backwardBackwardMplusFp<SIZE_NP, TRUE>(aAGradFp, rBGradAGradFp, fc, tBGradAGradFc, tRnp, rBGradRnp, rBGradAGradRnp);
        } else
        if (WTYPE==WTYPE_NONE) {
            backwardBackwardMplusFp<NMAX+1>(rBGradAGradFp, fc, tBGradAGradFc, tRn, rBGradAGradRn);
        } else
        if (WTYPE==WTYPE_FULL) {
            flt_t *tBGradAGradFp = rBGradAGradFp + (type-1)*(NMAX+1);
            backwardBackwardMplusFp<NMAX+1>(tBGradAGradFp, fc, tBGradAGradFc, tRn, rBGradAGradRn);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            flt_t *tBGradAGradFpWt = rBGradAGradFp + type*(NMAX+1);
            backwardBackwardMplusFpWt<NMAX+1>(rBGradAGradFp, tBGradAGradFpWt, ONE, fc, tBGradAGradFc, tRn, rBGradAGradRn);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            double wt = ((type&1)==1) ? type : (-type);
            flt_t *tBGradAGradFpWt = rBGradAGradFp + (NMAX+1);
            backwardBackwardMplusFpWt<NMAX+1>(rBGradAGradFp, tBGradAGradFpWt, wt, fc, tBGradAGradFc, tRn, rBGradAGradRn);
        }
    }
}

}

#endif //BASIS_CHEBYSHEV_H