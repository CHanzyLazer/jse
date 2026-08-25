#ifndef BASIS_CHEBYSHEV_H
#define BASIS_CHEBYSHEV_H

#include "basis_ChebyshevUtil.hpp"

#include <cstdint>

namespace JSE_NNAP {

template <int WTYPE, int MTYPE, int NMAX, int SIZE_NP>
static NNAP_DEVICE void chebyForwardGpu(int nb, int bi,
    int aNlSize, int *aNlIdx, flt_t *rFp,
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
        const int j = aNlIdx[(size_t)jj*nb + bi];
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
template <int WTYPE, int MTYPE, int NMAX, int SIZE_NP>
static NNAP_DEVICE void chebyBackwardGpu(int nb, int bi,
    int aNlSize, int *aNlIdx, flt_t *aAGradFp,
    flt_t *posx, flt_t *posy, flt_t *posz, int *type,
    flt_t *rGradNlDx, flt_t *rGradNlDy, flt_t *rGradNlDz,
    flt_t aRCut, flt_t *aParams) noexcept {
    
    // init cache
    flt_t bRn[NMAX+1], bAGradRn[NMAX+1];
    flt_t bRnp[SIZE_NP];
    // loop for neighbor
    const flt_t xi = posx[bi];
    const flt_t yi = posy[bi];
    const flt_t zi = posz[bi];
    const int typei = type[bi];
    for (int jj = 0; jj < aNlSize; ++jj) {
        const int j = aNlIdx[(size_t)jj*nb + bi];
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
        rGradNlDx[(size_t)jj*nb + bi] += fxj;
        rGradNlDy[(size_t)jj*nb + bi] += fyj;
        rGradNlDz[(size_t)jj*nb + bi] += fzj;
    }
}



template <int WTYPE, int MTYPE, int NMAX, int SIZE_NP, int REQUIRE_CACHE>
static void chebyForward(int ctype,
    int aNlSize, flt_t *aNlDx, flt_t *aNlDy, flt_t *aNlDz, int *aNlType,
    flt_t *rFp, flt_t aRCut, flt_t *aParams,
    flt_t **rForwardCache) noexcept {
    
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
    for (int jj = 0; jj < aNlSize; ++jj) {
        const flt_t dx = aNlDx[jj];
        const flt_t dy = aNlDy[jj];
        const flt_t dz = aNlDz[jj];
        const flt_t dis = nnap_sqrt(dx*dx + dy*dy + dz*dz);
        // check rcut for merge
        if (dis >= aRCut) continue;
        // mirror stuff
        int typej = aNlType[jj];
        if (MTYPE > 0) {
            if (typej==MTYPE) typej = ctype;
            else if (typej==ctype) typej = MTYPE;
        }
        // cal Rn, fc
        if (REQUIRE_CACHE) rRn = rNlRn + jj*(NMAX+1);
        calRn<NMAX>(rRn, dis, aRCut);
        flt_t fc = calFc(dis, aRCut);
        if (REQUIRE_CACHE) rNlFc[jj] = fc;
        // Rn to fp
        if (WTYPE==WTYPE_RFUSE || WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
            const int tParamShift = (typej-1)*(SIZE_NP*(NMAX+1));
            // cal Rnp
            if (REQUIRE_CACHE) rRnp = rNlRnp + jj*SIZE_NP;
            calRnp<NMAX, SIZE_NP>(rRnp, rRn, aParams+tParamShift);
            mplusFp<SIZE_NP>(rFp, fc, rRnp);
        } else
        if (WTYPE==WTYPE_NONE) {
            mplusFp<NMAX+1>(rFp, fc, rRn);
        } else
        if (WTYPE==WTYPE_FULL) {
            flt_t *tFp = rFp + (typej-1)*(NMAX+1);
            mplusFp<NMAX+1>(tFp, fc, rRn);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            flt_t *tFpWt = rFp + typej*(NMAX+1);
            mplusFpWt<NMAX+1>(rFp, tFpWt, ONE, fc, rRn);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            double wt = ((typej&1)==1) ? typej : (-typej);
            flt_t *tFpWt = rFp + (NMAX+1);
            mplusFpWt<NMAX+1>(rFp, tFpWt, wt, fc, rRn);
        }
    }
}

template <int WTYPE, int MTYPE, int NMAX, int SIZE_NP, int GRAD_PARAM, int USE_BB, int REQUIRE_CACHE>
static void chebyBackward(int ctype,
    int aNlSize, flt_t *aNlDx, flt_t *aNlDy, flt_t *aNlDz, int *aNlType,
    flt_t *rAGradNlDx, flt_t *rAGradNlDy, flt_t *rAGradNlDz, flt_t *aAGradFp,
    flt_t aRCut, flt_t *aParams, flt_t *rAGradParams,
    flt_t **aForwardCache, flt_t **rBackwardCache, flt_t **rBackwardBackwardCache) noexcept {
    
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
    for (int jj = 0; jj < aNlSize; ++jj) {
        const flt_t dx = aNlDx[jj];
        const flt_t dy = aNlDy[jj];
        const flt_t dz = aNlDz[jj];
        const flt_t dis = nnap_sqrt(dx*dx + dy*dy + dz*dz);
        // check rcut for merge
        if (dis >= aRCut) continue;
        // mirror stuff
        int typej = aNlType[jj];
        if (MTYPE > 0) {
            if (typej==MTYPE) typej = ctype;
            else if (typej==ctype) typej = MTYPE;
        }
        // get Rn, fc
        flt_t *tRn = tNlRn + jj*(NMAX+1);
        flt_t fc = tNlFc[jj];
        // gradFp to gradRn & gradFc
        flt_t rAGradFc = ZERO;
        fill<NMAX+1>(rAGradRn, ZERO);
        if (WTYPE==WTYPE_RFUSE || WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
            const int tParamShift = (typej-1)*(SIZE_NP*(NMAX+1));
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
            flt_t *tAGradFp = aAGradFp + (typej-1)*(NMAX+1);
            backwardMplusFp<NMAX+1>(tAGradFp, fc, rAGradFc, tRn, rAGradRn);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            flt_t *tAGradFpWt = aAGradFp + typej*(NMAX+1);
            backwardMplusFpWt<NMAX+1>(aAGradFp, tAGradFpWt, ONE, fc, rAGradFc, tRn, rAGradRn);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            double wt = ((typej&1)==1) ? typej : (-typej);
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
static void chebyBackwardBackward(int ctype,
    int aNlSize, flt_t *aNlDx, flt_t *aNlDy, flt_t *aNlDz, int *aNlType,
    flt_t *aBGradNlDx, flt_t *aBGradNlDy, flt_t *aBGradNlDz,
    flt_t *aAGradFp, flt_t *rBGradAGradFp,
    flt_t aRCut, flt_t *aParams, flt_t *rBGradParams,
    flt_t **aForwardCache, flt_t **aBackwardCache, flt_t **rBackwardBackwardCache) noexcept {
    
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
    for (int jj = 0; jj < aNlSize; ++jj) {
        const flt_t dx = aNlDx[jj];
        const flt_t dy = aNlDy[jj];
        const flt_t dz = aNlDz[jj];
        const flt_t dis = nnap_sqrt(dx*dx + dy*dy + dz*dz);
        // check rcut for merge
        if (dis >= aRCut) continue;
        // mirror stuff
        int typej = aNlType[jj];
        if (MTYPE > 0) {
            if (typej==MTYPE) typej = ctype;
            else if (typej==ctype) typej = MTYPE;
        }
        // get Rn, fc
        flt_t *tRn = tNlRn + jj*(NMAX+1);
        flt_t fc = tNlFc[jj];
        // get RnGrad, fcGrad
        flt_t *tRnGrad = tNlRnGrad + jj*(NMAX+1);
        flt_t fcGrad = tNlFcGrad[jj];
        // grad grad xyz to grad grad fc & Rn
        const flt_t tBGradAGradj = aBGradNlDx[jj]*dx + aBGradNlDy[jj]*dy + aBGradNlDz[jj]*dz;
        fill<NMAX+1>(rBGradAGradRn, ZERO);
        flt_t tBGradAGradFc = tBGradAGradj*fcGrad;
        mplus<NMAX+1>(rBGradAGradRn, tBGradAGradj, tRnGrad);
        // grad grad fc & Rn to grad grad fp
        if (WTYPE==WTYPE_RFUSE || WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
            const int tParamShift = (typej-1)*(SIZE_NP*(NMAX+1));
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
            flt_t *tBGradAGradFp = rBGradAGradFp + (typej-1)*(NMAX+1);
            backwardBackwardMplusFp<NMAX+1>(tBGradAGradFp, fc, tBGradAGradFc, tRn, rBGradAGradRn);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            flt_t *tBGradAGradFpWt = rBGradAGradFp + typej*(NMAX+1);
            backwardBackwardMplusFpWt<NMAX+1>(rBGradAGradFp, tBGradAGradFpWt, ONE, fc, tBGradAGradFc, tRn, rBGradAGradRn);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            double wt = ((typej&1)==1) ? typej : (-typej);
            flt_t *tBGradAGradFpWt = rBGradAGradFp + (NMAX+1);
            backwardBackwardMplusFpWt<NMAX+1>(rBGradAGradFp, tBGradAGradFpWt, wt, fc, tBGradAGradFc, tRn, rBGradAGradRn);
        }
    }
}

}

#endif //BASIS_CHEBYSHEV_H