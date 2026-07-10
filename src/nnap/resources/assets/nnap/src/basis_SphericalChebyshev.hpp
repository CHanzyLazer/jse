#ifndef BASIS_SPHERICAL_CHEBYSHEV_H
#define BASIS_SPHERICAL_CHEBYSHEV_H

#include "basis_SphericalUtil.hpp"

namespace JSE_NNAP {

template <int WTYPE, int MTYPE, int NMAX, int LMAXMAX, int SIZE_NP, int TWO_PASS>
static NNAP_DEVICE void calAnlmGpu(int nb, int bi, int np,
    int aNlSize, int *aBufNl,
    flt_t *bY, flt_t *rAnlm1, flt_t *rAnlm2,
    flt_t *posx, flt_t *posy, flt_t *posz, int *type,
    flt_t aRCut, flt_t *aParams) noexcept {
    // const init
    constexpr int tLMAll = (LMAXMAX+1)*(LMAXMAX+1);
    // init cache
    flt_t bRn[NMAX+1];
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
        // cal Y
        calY<LMAXMAX>(bY, dx, dy, dz, dis);
        // cal Rn, fc
        calRn<NMAX>(bRn, dis, aRCut);
        const flt_t fc = calFc(dis, aRCut);
        // cal anlm
        const int tParamShift = (typej-1)*(SIZE_NP*(NMAX+1)) + np*(NMAX+1);
        if (TWO_PASS) {
            const flt_t *tWeight1 = aParams+tParamShift, *tWeight2 = aParams+tParamShift+(NMAX+1);
            flt_t tRnp1 = ZERO, tRnp2 = ZERO;
            for (int k = 0; k < (NMAX+1); ++k) {
                const flt_t subRn = bRn[k];
                tRnp1 += subRn*tWeight1[k];
                tRnp2 += subRn*tWeight2[k];
            }
            tRnp1 *= fc; tRnp2 *= fc;
            for (int k = 0; k < tLMAll; ++k) {
                const flt_t subY = bY[k];
                rAnlm1[k] += tRnp1*subY;
                rAnlm2[k] += tRnp2*subY;
            }
        } else {
            const flt_t tRnp1 = dot<NMAX+1>(bRn, aParams+tParamShift);
            mplus<tLMAll>(rAnlm1, fc*tRnp1, bY);
        }
    }
}
template <int WTYPE, int MTYPE, int NMAX, int LMAX, int L3MAX, int L4MAX, int SIZE_NP>
static NNAP_DEVICE void sphForwardGpu(int nb, int bi,
    int aNlSize, int *aBufNl, flt_t *rFp,
    flt_t *posx, flt_t *posy, flt_t *posz, int *type,
    flt_t aRCut, flt_t *aParams) noexcept {
    // const init
    constexpr int tSizeL = (LMAX+1) + L3NCOLS[L3MAX] + L4NCOLS[L4MAX];
    constexpr int tLMaxMax = LMAX>L3MAX ? (LMAX>L4MAX?LMAX:L4MAX) : (L3MAX>L4MAX?L3MAX:L4MAX);
    constexpr int tLMAll = (tLMaxMax+1)*(tLMaxMax+1);
    // init cache
    flt_t bY[tLMAll];
    flt_t bAnlm1[tLMAll], bAnlm2[tLMAll];
    // change loop order for less cache
    constexpr int tSizeL2 = LMAX+1;
    constexpr int tSizeL3 = L3NCOLS[L3MAX];
    int np = 0, tShiftFp = 0;
    for (; np < (SIZE_NP-1); np += 2) {
        fill<tLMAll>(bAnlm1, ZERO);
        fill<tLMAll>(bAnlm2, ZERO);
        calAnlmGpu<WTYPE, MTYPE, NMAX, tLMaxMax, SIZE_NP, TRUE>(nb, bi, np,
            aNlSize, aBufNl,
            bY, bAnlm1, bAnlm2,
            posx, posy, posz, type,
            aRCut, aParams
        );
        // anlm -> fp
        calSphL2<LMAX >(bAnlm1, rFp+tShiftFp);
        calSphL3<L3MAX>(bAnlm1, rFp+tShiftFp+tSizeL2);
        calSphL4<L4MAX>(bAnlm1, rFp+tShiftFp+tSizeL2+tSizeL3);
        tShiftFp += tSizeL;
        calSphL2<LMAX >(bAnlm2, rFp+tShiftFp);
        calSphL3<L3MAX>(bAnlm2, rFp+tShiftFp+tSizeL2);
        calSphL4<L4MAX>(bAnlm2, rFp+tShiftFp+tSizeL2+tSizeL3);
        tShiftFp += tSizeL;
    }
    // rest
    if (SIZE_NP%2 == 1) {
        fill<tLMAll>(bAnlm1, ZERO);
        calAnlmGpu<WTYPE, MTYPE, NMAX, tLMaxMax, SIZE_NP, FALSE>(nb, bi, np,
            aNlSize, aBufNl,
            bY, bAnlm1, NULL,
            posx, posy, posz, type,
            aRCut, aParams
        );
        // anlm -> fp
        calSphL2<LMAX >(bAnlm1, rFp+tShiftFp);
        calSphL3<L3MAX>(bAnlm1, rFp+tShiftFp+tSizeL2);
        calSphL4<L4MAX>(bAnlm1, rFp+tShiftFp+tSizeL2+tSizeL3);
    }
}

template <int WTYPE, int MTYPE, int NMAX, int LMAXMAX, int SIZE_NP, int TWO_PASS>
static NNAP_DEVICE void backwardAnlmGpu(int nb, int bi, int np,
    int aNlSize, int *aBufNl,
    flt_t *bY, flt_t *bYPtheta, flt_t *aAGradAnlm1, flt_t *aAGradAnlm2,
    flt_t *posx, flt_t *posy, flt_t *posz, int *type,
    flt_t *f0, flt_t *v0, flt_t *nlFx, flt_t *nlFy, flt_t *nlFz,
    flt_t aRCut, flt_t *aParams) {
    
    // const init
    constexpr int tLMAll = (LMAXMAX+1)*(LMAXMAX+1);
    // init cache
    flt_t bRn[NMAX+1];
    // loop for neighbor
    flt_t f0xi = ZERO, f0yi = ZERO, f0zi = ZERO;
    flt_t v0xxi = ZERO, v0yyi = ZERO, v0zzi = ZERO;
    flt_t v0xyi = ZERO, v0xzi = ZERO, v0yzi = ZERO;
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
        // cal Y
        calY<LMAXMAX>(bY, dx, dy, dz, dis);
        // cal Rn, fc
        calRn<NMAX>(bRn, dis, aRCut);
        const flt_t fc = calFc(dis, aRCut);
        
        // grad anlm -> grad xyz
        flt_t thetaPx, thetaPy, thetaPz, phiPx, phiPy;
        flt_t rAGradj = ZERO, rAGradThetaj = ZERO, rAGradPhij = ZERO;
        const int tParamShift = (typej-1)*(SIZE_NP*(NMAX+1)) + np*(NMAX+1);
        if (TWO_PASS) {
            const flt_t *tWeight1 = aParams+tParamShift, *tWeight2 = aParams+tParamShift+(NMAX+1);
            flt_t tRnp1 = ZERO, tRnp2 = ZERO;
            for (int k = 0; k < (NMAX+1); ++k) {
                const flt_t subRn = bRn[k];
                tRnp1 += subRn*tWeight1[k];
                tRnp2 += subRn*tWeight2[k];
            }
            // cal grad fc & Rnp here, for no use Y later
            flt_t tAGradFcRnp1 = ZERO, tAGradFcRnp2 = ZERO;
            for (int k = 0; k < tLMAll; ++k) {
                const flt_t subY = bY[k];
                tAGradFcRnp1 += aAGradAnlm1[k]*subY;
                tAGradFcRnp2 += aAGradAnlm2[k]*subY;
            }
            // cal RnGrad, fcGrag
            flt_t *tRnGrad = bRn;
            calRnGrad<NMAX>(tRnGrad, dis, aRCut);
            const flt_t fcGrad = calFcGrad(dis, aRCut);
            // gradRnp to gradRn (cache optim)
            flt_t tRnp1Grad = ZERO, tRnp2Grad = ZERO;
            for (int k = 0; k < (NMAX+1); ++k) {
                const flt_t subRnGrad = tRnGrad[k];
                tRnp1Grad += subRnGrad*tWeight1[k];
                tRnp2Grad += subRnGrad*tWeight2[k];
            }
            rAGradj += (tAGradFcRnp1*tRnp1Grad + tAGradFcRnp2*tRnp2Grad) * fc;
            rAGradj += (tAGradFcRnp1*tRnp1 + tAGradFcRnp2*tRnp2) * fcGrad;
            
            // cal YlmPthetaPphi
            calYPthetaPphiGpu<LMAXMAX>(
                bYPtheta, bY, dx, dy, dz, dis,
                thetaPx, thetaPy, thetaPz, phiPx, phiPy
            );
            // backward in single loop for save cache
            tRnp1 *= fc; tRnp2 *= fc;
            for (int k = 0; k < tLMAll; ++k) {
                const flt_t subAGradY = aAGradAnlm1[k]*tRnp1 + aAGradAnlm2[k]*tRnp2;
                rAGradThetaj += subAGradY*bYPtheta[k];
                rAGradPhij += subAGradY*bY[k]; // bY <-> bYPphi
            }
        } else {
            const flt_t tRnp = dot<NMAX+1>(bRn, aParams+tParamShift);
            // cal grad fc & Rnp here, for no use Y later
            const flt_t tAGradFcRnp = dot<tLMAll>(aAGradAnlm1, bY);
            // cal RnGrad, fcGrag
            flt_t *tRnGrad = bRn;
            calRnGrad<NMAX>(tRnGrad, dis, aRCut);
            const flt_t fcGrad = calFcGrad(dis, aRCut);
            // gradRnp to gradRn (cache optim)
            rAGradj += tAGradFcRnp*fc * dot<NMAX+1>(aParams+tParamShift, tRnGrad);
            rAGradj += tAGradFcRnp*tRnp * fcGrad;
        
            // cal YlmPthetaPphi
            calYPthetaPphiGpu<LMAXMAX>(
                bYPtheta, bY, dx, dy, dz, dis,
                thetaPx, thetaPy, thetaPz, phiPx, phiPy
            );
            // backward in single loop for save cache
            const flt_t subFcRnp = fc*tRnp;
            for (int k = 0; k < tLMAll; ++k) {
                const flt_t subAGradY = aAGradAnlm1[k]*subFcRnp;
                rAGradThetaj += subAGradY*bYPtheta[k];
                rAGradPhij += subAGradY*bY[k]; // bY <-> bYPphi
            }
        }
        // to grad xyz
        const flt_t fxj = rAGradj*dx + rAGradThetaj*thetaPx + rAGradPhij*phiPx;
        const flt_t fyj = rAGradj*dy + rAGradThetaj*thetaPy + rAGradPhij*phiPy;
        const flt_t fzj = rAGradj*dz + rAGradThetaj*thetaPz;
        f0xi -= fxj; f0yi -= fyj; f0zi -= fzj;
        v0xxi += dx*fxj; v0yyi += dy*fyj; v0zzi += dz*fzj;
        v0xyi += dx*fyj; v0xzi += dx*fzj; v0yzi += dy*fzj;
        nlFx[jj*nb + bi] += fxj;
        nlFy[jj*nb + bi] += fyj;
        nlFz[jj*nb + bi] += fzj;
    }
    f0[0] += f0xi; f0[1] += f0yi; f0[2] += f0zi;
    v0[0] += v0xxi; v0[1] += v0yyi; v0[2] += v0zzi;
    v0[3] += v0xyi; v0[4] += v0xzi; v0[5] += v0yzi;
}
template <int WTYPE, int MTYPE, int NMAX, int LMAX, int L3MAX, int L4MAX, int SIZE_NP>
static NNAP_DEVICE void sphBackwardGpu(int nb, int bi,
    int aNlSize, int *aBufNl, flt_t *aAGradFp,
    flt_t *posx, flt_t *posy, flt_t *posz, int *type,
    flt_t *f0, flt_t *v0, flt_t *nlFx, flt_t *nlFy, flt_t *nlFz,
    flt_t aRCut, flt_t *aParams) noexcept {
    
    // const init
    constexpr int tSizeL = (LMAX+1) + L3NCOLS[L3MAX] + L4NCOLS[L4MAX];
    constexpr int tLMaxMax = LMAX>L3MAX ? (LMAX>L4MAX?LMAX:L4MAX) : (L3MAX>L4MAX?L3MAX:L4MAX);
    constexpr int tLMAll = (tLMaxMax+1)*(tLMaxMax+1);
    // init cache
    flt_t bAnlm1[tLMAll], bAnlm2[tLMAll];
    flt_t bAGradAnlm1[tLMAll], bAGradAnlm2[tLMAll];
    // change loop order for less cache
    constexpr int tSizeL2 = LMAX+1;
    constexpr int tSizeL3 = L3NCOLS[L3MAX];
    int np = 0, tShiftFp = 0;
    for (; np < (SIZE_NP-1); np += 2) {
        // recalculated for save cache
        fill<tLMAll>(bAnlm1, ZERO);
        fill<tLMAll>(bAnlm2, ZERO);
        calAnlmGpu<WTYPE, MTYPE, NMAX, tLMaxMax, SIZE_NP, TRUE>(nb, bi, np,
            aNlSize, aBufNl,
            bAGradAnlm1, bAnlm1, bAnlm2,
            posx, posy, posz, type,
            aRCut, aParams
        );
        fill<tLMAll>(bAGradAnlm1, ZERO);
        fill<tLMAll>(bAGradAnlm2, ZERO);
        calGradSphL2<LMAX >(bAnlm1, bAGradAnlm1, aAGradFp+tShiftFp);
        calGradSphL3<L3MAX>(bAnlm1, bAGradAnlm1, aAGradFp+tShiftFp+tSizeL2);
        calGradSphL4<L4MAX>(bAnlm1, bAGradAnlm1, aAGradFp+tShiftFp+tSizeL2+tSizeL3);
        tShiftFp += tSizeL;
        calGradSphL2<LMAX >(bAnlm2, bAGradAnlm2, aAGradFp+tShiftFp);
        calGradSphL3<L3MAX>(bAnlm2, bAGradAnlm2, aAGradFp+tShiftFp+tSizeL2);
        calGradSphL4<L4MAX>(bAnlm2, bAGradAnlm2, aAGradFp+tShiftFp+tSizeL2+tSizeL3);
        tShiftFp += tSizeL;
        backwardAnlmGpu<WTYPE, MTYPE, NMAX,
                        tLMaxMax, SIZE_NP, TRUE>(nb, bi, np,
            aNlSize, aBufNl,
            bAnlm1, bAnlm2, bAGradAnlm1, bAGradAnlm2,
            posx, posy, posz, type,
            f0, v0, nlFx, nlFy, nlFz,
            aRCut, aParams
        );
    }
    // rest
    if (SIZE_NP%2 == 1) {
        fill<tLMAll>(bAnlm1, ZERO);
        calAnlmGpu<WTYPE, MTYPE, NMAX, tLMaxMax, SIZE_NP, FALSE>(nb, bi, np,
            aNlSize, aBufNl,
            bAGradAnlm1, bAnlm1, NULL,
            posx, posy, posz, type,
            aRCut, aParams
        );
        fill<tLMAll>(bAGradAnlm1, ZERO);
        calGradSphL2<LMAX >(bAnlm1, bAGradAnlm1, aAGradFp+tShiftFp);
        calGradSphL3<L3MAX>(bAnlm1, bAGradAnlm1, aAGradFp+tShiftFp+tSizeL2);
        calGradSphL4<L4MAX>(bAnlm1, bAGradAnlm1, aAGradFp+tShiftFp+tSizeL2+tSizeL3);
        backwardAnlmGpu<WTYPE, MTYPE, NMAX,
                        tLMaxMax, SIZE_NP, FALSE>(nb, bi, np,
            aNlSize, aBufNl,
            bAnlm1, bAnlm2, bAGradAnlm1, NULL,
            posx, posy, posz, type,
            f0, v0, nlFx, nlFy, nlFz,
            aRCut, aParams
        );
    }
}



template <int WTYPE, int MTYPE, int NMAX, int LMAXMAX, int SIZE_NP, int REQUIRE_CACHE>
static void calAnlm(int bi,
    int aNlSize, int *aNl, flt_t *rAnlm,
    flt_t *pos, int *type,
    flt_t **rForwardCache, flt_t aRCut, flt_t *aParams) noexcept {
    
    constexpr int tLMAll = (LMAXMAX+1)*(LMAXMAX+1);
    // init cache
    flt_t bRn[REQUIRE_CACHE ? 1 : (NMAX+1)]; flt_t *rRn = REQUIRE_CACHE ? NULL : bRn;
    flt_t bRnp[REQUIRE_CACHE ? 1 : SIZE_NP]; flt_t *rRnp = REQUIRE_CACHE ? NULL : bRnp;
    flt_t bY[REQUIRE_CACHE ? 1 : tLMAll]; flt_t *rY = REQUIRE_CACHE ? NULL : bY;
    flt_t *rNlFc = NULL, *rNlRn = NULL, *rNlRnp = NULL, *rNlY = NULL;
    if (REQUIRE_CACHE) {
        rNlFc = *rForwardCache; *rForwardCache += aNlSize;
        rNlRn = *rForwardCache; *rForwardCache += aNlSize*(NMAX+1);
        rNlRnp = *rForwardCache; *rForwardCache += aNlSize*SIZE_NP;
        rNlY = *rForwardCache; *rForwardCache += aNlSize*tLMAll;
    }
    // loop for neighbor
    const flt_t xi = pos[3*bi+0];
    const flt_t yi = pos[3*bi+1];
    const flt_t zi = pos[3*bi+2];
    const int typei = type[bi];
    for (int jj = 0; jj < aNlSize; ++jj) {
        const int j = aNl[jj];
        const flt_t dx = pos[3*j+0] - xi;
        const flt_t dy = pos[3*j+1] - yi;
        const flt_t dz = pos[3*j+2] - zi;
        const flt_t dis = nnap_sqrt(dx*dx + dy*dy + dz*dz);
        // check rcut for merge
        if (dis >= aRCut) continue;
        // mirror stuff
        int typej = type[j];
        if (MTYPE > 0) {
            if (typej==MTYPE) typej = typei;
            else if (typej==typei) typej = MTYPE;
        }
        // cal Y
        if (REQUIRE_CACHE) rY = rNlY + jj*tLMAll;
        calY<LMAXMAX>(rY, dx, dy, dz, dis);
        // cal Rn, fc
        if (REQUIRE_CACHE) rRn = rNlRn + jj*(NMAX+1);
        calRn<NMAX>(rRn, dis, aRCut);
        flt_t fc = calFc(dis, aRCut);
        if (REQUIRE_CACHE) rNlFc[jj] = fc;
        // to anlm
        if (WTYPE==WTYPE_RFUSE || WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
            const int tParamShift = (typej-1)*(SIZE_NP*(NMAX+1));
            // cal Rnp
            if (REQUIRE_CACHE) rRnp = rNlRnp + jj*SIZE_NP;
            calRnp<NMAX, SIZE_NP>(rRnp, rRn, aParams+tParamShift);
            mplusAnlm<SIZE_NP, LMAXMAX>(rAnlm, rY, fc, rRnp);
        } else
        if (WTYPE==WTYPE_NONE) {
            mplusAnlm<NMAX+1, LMAXMAX>(rAnlm, rY, fc, rRn);
        } else
        if (WTYPE==WTYPE_FULL) {
            flt_t *tAnlm = rAnlm + (typej-1)*(NMAX+1)*tLMAll;
            mplusAnlm<NMAX+1, LMAXMAX>(tAnlm, rY, fc, rRn);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            flt_t *tAnlmWt = rAnlm + typej*(NMAX+1)*tLMAll;
            mplusAnlmWt<NMAX+1, LMAXMAX>(rAnlm, tAnlmWt, ONE, rY, fc, rRn);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            double wt = ((typej&1)==1) ? typej : (-typej);
            flt_t *tAnlmWt = rAnlm + (NMAX+1)*tLMAll;
            mplusAnlmWt<NMAX+1, LMAXMAX>(rAnlm, tAnlmWt, wt, rY, fc, rRn);
        }
    }
}

template <int WTYPE, int MTYPE, int NMAX, int LMAX, int L3MAX, int L4MAX, int SIZE_NP, int REQUIRE_CACHE>
static void sphForward(int bi,
    int aNlSize, int *aNl, flt_t *rFp,
    flt_t *pos, int *type,
    flt_t **rForwardCache, flt_t aRCut, flt_t *aParams) noexcept {
    // const init
    constexpr int tSizeL = (LMAX+1) + L3NCOLS[L3MAX] + L4NCOLS[L4MAX];
    constexpr int tLMaxMax = LMAX>L3MAX ? (LMAX>L4MAX?LMAX:L4MAX) : (L3MAX>L4MAX?L3MAX:L4MAX);
    constexpr int tLMAll = (tLMaxMax+1)*(tLMaxMax+1);
    constexpr int tSizeAnlm = SIZE_NP*tLMAll;
    // init cache
    flt_t bAnlm[REQUIRE_CACHE ? 1 : tSizeAnlm] = {0};
    flt_t *rAnlm = NULL;
    if (REQUIRE_CACHE) {
        rAnlm = *rForwardCache; *rForwardCache += tSizeAnlm;
        fill<tSizeAnlm>(rAnlm, ZERO);
    } else {
        rAnlm = bAnlm;
    }
    // do cal
    calAnlm<WTYPE, MTYPE, NMAX, tLMaxMax, SIZE_NP, REQUIRE_CACHE>(bi,
        aNlSize, aNl, rAnlm,
        pos, type,
        rForwardCache, aRCut, aParams
    );
    // anlm -> fp
    constexpr int tSizeL2 = LMAX+1;
    constexpr int tSizeL3 = L3NCOLS[L3MAX];
    for (int np=0, tShift=0, tShiftFp=0; np<SIZE_NP; ++np, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calSphL2<LMAX >(rAnlm+tShift, rFp+tShiftFp);
        calSphL3<L3MAX>(rAnlm+tShift, rFp+tShiftFp+tSizeL2);
        calSphL4<L4MAX>(rAnlm+tShift, rFp+tShiftFp+tSizeL2+tSizeL3);
    }
}

template <int WTYPE, int MTYPE, int NMAX, int LMAXMAX, int SIZE_NP, int GRAD_PARAM, int USE_BB, int REQUIRE_CACHE>
static void backwardAnlm(int bi,
    int aNlSize, int *aNl, flt_t *aAGradAnlm,
    flt_t *pos, int *type,
    flt_t *f, flt_t *v0, flt_t *v1,
    flt_t **aForwardCache, flt_t **rBackwardCache, flt_t **rBackwardBackwardCache,
    flt_t aRCut, flt_t *aParams, flt_t *rAGradParams) {
    
    static_assert(!(GRAD_PARAM && REQUIRE_CACHE), "INVALID STATE");
    static_assert(!(USE_BB && REQUIRE_CACHE), "INVALID STATE");
    static_assert(!(!GRAD_PARAM && USE_BB), "INVALID STATE");
    if (GRAD_PARAM) {
        // no param
        if (WTYPE!=WTYPE_RFUSE && WTYPE!=WTYPE_FUSE && WTYPE!=WTYPE_EXFUSE) return;
    }
    // const init
    constexpr int tLMAll = (LMAXMAX+1)*(LMAXMAX+1);
    // init cache
    flt_t *tNlFc = *aForwardCache; *aForwardCache += aNlSize;
    flt_t *tNlRn = *aForwardCache; *aForwardCache += aNlSize*(NMAX+1);
    flt_t *tNlRnp = *aForwardCache; *aForwardCache += aNlSize*SIZE_NP;
    flt_t *tNlY = *aForwardCache; *aForwardCache += aNlSize*tLMAll;
    flt_t bRnGrad[REQUIRE_CACHE ? 1 : (NMAX+1)]; flt_t *rRnGrad = REQUIRE_CACHE ? NULL : bRnGrad;
    flt_t bAGradRnp[REQUIRE_CACHE ? 1 : SIZE_NP]; flt_t *rAGradRnp = REQUIRE_CACHE ? NULL : bAGradRnp;
    flt_t bYPtheta[REQUIRE_CACHE ? 1 : tLMAll]; flt_t *rYPtheta = REQUIRE_CACHE ? NULL : bYPtheta;
    flt_t bYPphi[REQUIRE_CACHE ? 1 : tLMAll]; flt_t *rYPphi = REQUIRE_CACHE ? NULL : bYPphi;
    flt_t *rNlFcGrad = NULL, *rNlRnGrad = NULL, *rNlAGradRnp = NULL;
    flt_t *rNlYPtheta = NULL, *rNlYPphi = NULL;
    if (REQUIRE_CACHE) {
        rNlFcGrad = *rBackwardCache; *rBackwardCache += aNlSize;
        rNlRnGrad = *rBackwardCache; *rBackwardCache += aNlSize*(NMAX+1);
        rNlAGradRnp = *rBackwardCache; *rBackwardCache += aNlSize*SIZE_NP;
        rNlYPtheta = *rBackwardCache; *rBackwardCache += aNlSize*tLMAll;
        rNlYPphi = *rBackwardCache; *rBackwardCache += aNlSize*tLMAll;
    }
    if (USE_BB) {
        rNlAGradRnp = *rBackwardBackwardCache; *rBackwardBackwardCache += aNlSize*SIZE_NP;
    }
    flt_t rAGradRn[NMAX+1];
    flt_t rAGradY[tLMAll];
    // loop for neighbor
    flt_t f0xi = ZERO, f0yi = ZERO, f0zi = ZERO;
    flt_t v0xxi = ZERO, v0yyi = ZERO, v0zzi = ZERO;
    flt_t v0xyi = ZERO, v0xzi = ZERO, v0yzi = ZERO;
    const flt_t xi = pos[3*bi + 0];
    const flt_t yi = pos[3*bi + 1];
    const flt_t zi = pos[3*bi + 2];
    const int typei = type[bi];
    for (int jj = 0; jj < aNlSize; ++jj) {
        const int j = aNl[jj];
        const flt_t dx = pos[3*j + 0] - xi;
        const flt_t dy = pos[3*j + 1] - yi;
        const flt_t dz = pos[3*j + 2] - zi;
        const flt_t dis = nnap_sqrt(dx*dx + dy*dy + dz*dz);
        // check rcut for merge
        if (dis >= aRCut) continue;
        // mirror stuff
        int typej = type[j];
        if (MTYPE > 0) {
            if (typej==MTYPE) typej = typei;
            else if (typej==typei) typej = MTYPE;
        }
        // get Y
        flt_t *tY = tNlY + jj*tLMAll;
        // get Rn, fc
        flt_t *tRn = tNlRn + jj*(NMAX+1);
        flt_t fc = tNlFc[jj];
        // grad anlm to grad Rn fc & Y
        flt_t rAGradFc = ZERO;
        fill<NMAX+1>(rAGradRn, ZERO);
        fill<tLMAll>(rAGradY, ZERO);
        if (WTYPE==WTYPE_RFUSE || WTYPE==WTYPE_FUSE || WTYPE==WTYPE_EXFUSE) {
            const int tParamShift = (typej-1)*(SIZE_NP*(NMAX+1));
            // get Rnp
            flt_t *tRnp = tNlRnp + jj*SIZE_NP;
            // cache grad Rnp here
            if (REQUIRE_CACHE || USE_BB) rAGradRnp = rNlAGradRnp + jj*SIZE_NP;
            if (!USE_BB) fill<SIZE_NP>(rAGradRnp, ZERO);
            backwardMplusAnlm<SIZE_NP, LMAXMAX>(aAGradAnlm, tY, rAGradY, fc, rAGradFc, tRnp, rAGradRnp);
            backwardRnp<NMAX, SIZE_NP, GRAD_PARAM, !GRAD_PARAM>(
                rAGradRnp, tRn, rAGradRn,
                aParams+tParamShift,
                GRAD_PARAM ? (rAGradParams+tParamShift) : NULL
            );
        } else
        if (WTYPE==WTYPE_NONE) {
            backwardMplusAnlm<NMAX+1, LMAXMAX>(aAGradAnlm, tY, rAGradY, fc, rAGradFc, tRn, rAGradRn);
        } else
        if (WTYPE==WTYPE_FULL) {
            flt_t *tAGradAnlm = aAGradAnlm + (typej-1)*(NMAX+1)*tLMAll;
            backwardMplusAnlm<NMAX+1, LMAXMAX>(tAGradAnlm, tY, rAGradY, fc, rAGradFc, tRn, rAGradRn);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            flt_t *tAGradAnlmWt = aAGradAnlm + typej*(NMAX+1)*tLMAll;
            backwardMplusAnlmWt<NMAX+1, LMAXMAX>(aAGradAnlm, tAGradAnlmWt, ONE, tY, rAGradY, fc, rAGradFc, tRn, rAGradRn);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            double wt = ((typej&1)==1) ? typej : (-typej);
            flt_t *tAGradAnlmWt = aAGradAnlm + (NMAX+1)*tLMAll;
            backwardMplusAnlmWt<NMAX+1, LMAXMAX>(aAGradAnlm, tAGradAnlmWt, wt, tY, rAGradY, fc, rAGradFc, tRn, rAGradRn);
        }
        if (!GRAD_PARAM) {
            // cal RnGrad, fcGrag
            if (REQUIRE_CACHE) rRnGrad = rNlRnGrad + jj*(NMAX+1);
            calRnGrad<NMAX>(rRnGrad, dis, aRCut);
            flt_t fcGrad = calFcGrad(dis, aRCut);
            if (REQUIRE_CACHE) rNlFcGrad[jj] = fcGrad;
            // cal YlmPthetaPphi
            if (REQUIRE_CACHE) {
                rYPtheta = rNlYPtheta + jj*tLMAll;
                rYPphi = rNlYPphi + jj*tLMAll;
            }
            flt_t thetaPx, thetaPy, thetaPz, phiPx, phiPy;
            calYPthetaPphi<LMAXMAX>(
                rYPtheta, rYPphi, tY, dx, dy, dz, dis,
                thetaPx, thetaPy, thetaPz, phiPx, phiPy
            );
            flt_t rAGradj = dot<NMAX+1>(rAGradRn, rRnGrad);
            rAGradj += rAGradFc*fcGrad;
            flt_t rAGradThetaj = ZERO, rAGradPhij = ZERO;
            for (int k = 0; k < tLMAll; ++k) {
                const flt_t subAGradY = rAGradY[k];
                rAGradThetaj += subAGradY*rYPtheta[k];
                rAGradPhij += subAGradY*rYPphi[k];
            }
            const flt_t fxj = rAGradj*dx + rAGradThetaj*thetaPx + rAGradPhij*phiPx;
            const flt_t fyj = rAGradj*dy + rAGradThetaj*thetaPy + rAGradPhij*phiPy;
            const flt_t fzj = rAGradj*dz + rAGradThetaj*thetaPz;
            f0xi -= fxj; f0yi -= fyj; f0zi -= fzj;
            f[3*j + 0] += fxj; f[3*j + 1] += fyj; f[3*j + 2] += fzj;
            const flt_t vxxj = dx*fxj, vyyj = dy*fyj, vzzj = dz*fzj;
            const flt_t vxyj = dx*fyj, vxzj = dx*fzj, vyzj = dy*fzj;
            v0xxi += vxxj; v0yyi += vyyj; v0zzi += vzzj;
            v0xyi += vxyj; v0xzi += vxzj; v0yzi += vyzj;
            v1[9*j + 0] += vxxj; v1[9*j + 1] += vyyj; v1[9*j + 2] += vzzj;
            v1[9*j + 3] += vxyj; v1[9*j + 4] += vxzj; v1[9*j + 5] += vyzj;
            v1[9*j + 6] += dy*fxj;
            v1[9*j + 7] += dz*fxj;
            v1[9*j + 8] += dz*fyj;
        }
    }
    if (!GRAD_PARAM) {
        f[3*bi + 0] += f0xi; f[3*bi + 1] += f0yi; f[3*bi + 2] += f0zi;
        v0[0] += v0xxi; v0[1] += v0yyi; v0[2] += v0zzi;
        v0[3] += v0xyi; v0[4] += v0xzi; v0[5] += v0yzi;
    }
}
template <int WTYPE, int MTYPE, int NMAX, int LMAX, int L3MAX, int L4MAX, int SIZE_NP, int GRAD_PARAM, int USE_BB, int REQUIRE_CACHE>
static void sphBackward(int bi,
    int aNlSize, int *aNl, flt_t *aAGradFp,
    flt_t *pos, int *type,
    flt_t *f, flt_t *v0, flt_t *v1,
    flt_t **aForwardCache, flt_t **rBackwardCache, flt_t **rBackwardBackwardCache,
    flt_t aRCut, flt_t *aParams, flt_t *rAGradParams) noexcept {
    
    static_assert(!(GRAD_PARAM && REQUIRE_CACHE), "INVALID STATE");
    static_assert(!(USE_BB && REQUIRE_CACHE), "INVALID STATE");
    static_assert(!(!GRAD_PARAM && USE_BB), "INVALID STATE");
    // const init
    constexpr int tSizeL = (LMAX+1) + L3NCOLS[L3MAX] + L4NCOLS[L4MAX];
    constexpr int tLMaxMax = LMAX>L3MAX ? (LMAX>L4MAX?LMAX:L4MAX) : (L3MAX>L4MAX?L3MAX:L4MAX);
    constexpr int tLMAll = (tLMaxMax+1)*(tLMaxMax+1);
    constexpr int tSizeAnlm = SIZE_NP*tLMAll;
    if (GRAD_PARAM) {
        // no param
        if (WTYPE!=WTYPE_RFUSE && WTYPE!=WTYPE_FUSE && WTYPE!=WTYPE_EXFUSE) {
            // aForwardCache shift required
            *aForwardCache += tSizeAnlm;
            *aForwardCache += aNlSize;
            *aForwardCache += aNlSize*(NMAX+1);
            *aForwardCache += aNlSize*SIZE_NP;
            *aForwardCache += aNlSize*tLMAll;
            if (USE_BB) {
                // rBackwardBackwardCache shift required
                *rBackwardBackwardCache += tSizeAnlm;
                *rBackwardBackwardCache += aNlSize*SIZE_NP;
            }
            return;
        }
    }
    // init cache
    flt_t *tAnlm = *aForwardCache; *aForwardCache += tSizeAnlm;
    flt_t bAGradAnlm[(USE_BB || REQUIRE_CACHE) ? 1 : tSizeAnlm] = {0};
    flt_t *rAGradAnlm = NULL;
    if (REQUIRE_CACHE) {
        // use cache
        rAGradAnlm = *rBackwardCache; *rBackwardCache += tSizeAnlm;
        fill<tSizeAnlm>(rAGradAnlm, ZERO);
    } else
    if (USE_BB) {
        // use bb values
        rAGradAnlm = *rBackwardBackwardCache; *rBackwardBackwardCache += tSizeAnlm;
    } else {
        rAGradAnlm = bAGradAnlm;
    }
    // fp -> anlm
    constexpr int tSizeL2 = LMAX+1;
    constexpr int tSizeL3 = L3NCOLS[L3MAX];
    for (int np=0, tShift=0, tShiftFp=0; np<SIZE_NP; ++np, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calGradSphL2<LMAX >(tAnlm+tShift, rAGradAnlm+tShift, aAGradFp+tShiftFp);
        calGradSphL3<L3MAX>(tAnlm+tShift, rAGradAnlm+tShift, aAGradFp+tShiftFp+tSizeL2);
        calGradSphL4<L4MAX>(tAnlm+tShift, rAGradAnlm+tShift, aAGradFp+tShiftFp+tSizeL2+tSizeL3);
    }
    backwardAnlm<WTYPE, MTYPE, NMAX, tLMaxMax, SIZE_NP, GRAD_PARAM, USE_BB, REQUIRE_CACHE>(bi,
        aNlSize, aNl, rAGradAnlm,
        pos, type,
        f, v0, v1,
        aForwardCache, rBackwardCache, rBackwardBackwardCache,
        aRCut, aParams, rAGradParams
    );
}

template <int WTYPE, int MTYPE, int NMAX, int LMAXMAX, int SIZE_NP>
static void backwardBackwardAnlm(int bi,
    int aNlSize, int *aNl, flt_t *aAGradAnlm, flt_t *rBGradAGradAnlm,
    flt_t *pos, int *type,
    flt_t *aBGradF, flt_t *aBGradV0,
    flt_t **aForwardCache, flt_t **aBackwardCache, flt_t **rBackwardBackwardCache,
    flt_t aRCut, flt_t *aParams, flt_t *rBGradParams) {
    
    constexpr int tLMAll = (LMAXMAX+1)*(LMAXMAX+1);
    // init cache
    flt_t *tNlFc = *aForwardCache; *aForwardCache += aNlSize;
    flt_t *tNlRn = *aForwardCache; *aForwardCache += aNlSize*(NMAX+1);
    flt_t *tNlRnp = *aForwardCache; *aForwardCache += aNlSize*SIZE_NP;
    flt_t *tNlY = *aForwardCache; *aForwardCache += aNlSize*tLMAll;
    flt_t *tNlFcGrad = *aBackwardCache; *aBackwardCache += aNlSize;
    flt_t *tNlRnGrad = *aBackwardCache; *aBackwardCache += aNlSize*(NMAX+1);
    flt_t *tNlAGradRnp = *aBackwardCache; *aBackwardCache += aNlSize*SIZE_NP;
    flt_t *tNlYPtheta = *aBackwardCache; *aBackwardCache += aNlSize*tLMAll;
    flt_t *tNlYPphi = *aBackwardCache; *aBackwardCache += aNlSize*tLMAll;
    flt_t *rNlBGradRnp = *rBackwardBackwardCache; *rBackwardBackwardCache += aNlSize*SIZE_NP;
    flt_t rBGradAGradRn[NMAX+1], rBGradAGradRnp[SIZE_NP];
    flt_t rBGradAGradY[tLMAll];
    // loop for neighbor
    const flt_t tBGradFxi = aBGradF[3*bi + 0];
    const flt_t tBGradFyi = aBGradF[3*bi + 1];
    const flt_t tBGradFzi = aBGradF[3*bi + 2];
    const flt_t tBGradVxx = aBGradV0[0], tBGradVyy = aBGradV0[1], tBGradVzz = aBGradV0[2];
    const flt_t tBGradVxy = aBGradV0[3], tBGradVxz = aBGradV0[4], tBGradVyz = aBGradV0[5];
    const flt_t xi = pos[3*bi + 0];
    const flt_t yi = pos[3*bi + 1];
    const flt_t zi = pos[3*bi + 2];
    const int typei = type[bi];
    for (int jj = 0; jj < aNlSize; ++jj) {
        const int j = aNl[jj];
        const flt_t dx = pos[3*j + 0] - xi;
        const flt_t dy = pos[3*j + 1] - yi;
        const flt_t dz = pos[3*j + 2] - zi;
        const flt_t dis = nnap_sqrt(dx*dx + dy*dy + dz*dz);
        // check rcut for merge
        if (dis >= aRCut) continue;
        // mirror stuff
        int typej = type[j];
        if (MTYPE > 0) {
            if (typej==MTYPE) typej = typei;
            else if (typej==typei) typej = MTYPE;
        }
        // backward f v
        flt_t rBGradFxj = ZERO, rBGradFyj = ZERO, rBGradFzj = ZERO;
        rBGradFxj += aBGradF[3*j + 0] - tBGradFxi;
        rBGradFyj += aBGradF[3*j + 1] - tBGradFyi;
        rBGradFzj += aBGradF[3*j + 2] - tBGradFzi;
        rBGradFxj += dx*tBGradVxx;
        rBGradFyj += dy*tBGradVyy + dx*tBGradVxy;
        rBGradFzj += dz*tBGradVzz + dx*tBGradVxz + dy*tBGradVyz;
        // get Rn, fc
        flt_t *tRn = tNlRn + jj*(NMAX+1);
        flt_t fc = tNlFc[jj];
        // get RnGrad, fcGrad
        flt_t *tRnGrad = tNlRnGrad + jj*(NMAX+1);
        flt_t fcGrad = tNlFcGrad[jj];
        // get Y YGrad
        flt_t *tY = tNlY + jj*tLMAll;
        // get YGrad
        flt_t *tYPtheta = tNlYPtheta + jj*tLMAll;
        flt_t *tYPphi = tNlYPphi + jj*tLMAll;
        // cal theta phi pxyz
        flt_t thetaPx, thetaPy, thetaPz, phiPx, phiPy;
        calthetaPhiPxyz(
            dx, dy, dz, dis,
            thetaPx, thetaPy, thetaPz,
            phiPx, phiPy
        );
        // grad grad xyz to grad grad fc, Rn & Y
        const flt_t tBGradAGradj = rBGradFxj*dx + rBGradFyj*dy + rBGradFzj*dz;
        const flt_t tBGradAGradThetaj = rBGradFxj*thetaPx + rBGradFyj*thetaPy + rBGradFzj*thetaPz;
        const flt_t tBGradAGradPhij = rBGradFxj*phiPx + rBGradFyj*phiPy;
        fill<NMAX+1>(rBGradAGradRn, ZERO);
        fill<tLMAll>(rBGradAGradY, ZERO);
        flt_t tBGradAGradFc = tBGradAGradj*fcGrad;
        mplus<NMAX+1>(rBGradAGradRn, tBGradAGradj, tRnGrad);
        for (int k = 0; k < tLMAll; ++k) {
            rBGradAGradY[k] += tBGradAGradThetaj*tYPtheta[k] + tBGradAGradPhij*tYPphi[k];
        }
        // grad grad fc, Rn & Y to grad grad anlm
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
            backwardBackwardMplusAnlm<SIZE_NP, LMAXMAX, TRUE>(aAGradAnlm, rBGradAGradAnlm, tY, rBGradAGradY, fc, tBGradAGradFc, tRnp, rBGradRnp, rBGradAGradRnp);
        } else
        if (WTYPE==WTYPE_NONE) {
            backwardBackwardMplusAnlm<NMAX+1, LMAXMAX>(rBGradAGradAnlm, tY, rBGradAGradY, fc, tBGradAGradFc, tRn, rBGradAGradRn);
        } else
        if (WTYPE==WTYPE_FULL) {
            flt_t *tBGradAGradAnlm = rBGradAGradAnlm + (typej-1)*(NMAX+1)*tLMAll;
            backwardBackwardMplusAnlm<NMAX+1, LMAXMAX>(tBGradAGradAnlm, tY, rBGradAGradY, fc, tBGradAGradFc, tRn, rBGradAGradRn);
        } else
        if (WTYPE==WTYPE_EXFULL) {
            flt_t *rBGradAGradAnlmWt = rBGradAGradAnlm + typej*(NMAX+1)*tLMAll;
            backwardBackwardMplusAnlmWt<NMAX+1, LMAXMAX>(rBGradAGradAnlm, rBGradAGradAnlmWt, ONE, tY, rBGradAGradY, fc, tBGradAGradFc, tRn, rBGradAGradRn);
        } else
        if (WTYPE==WTYPE_DEFAULT) {
            double wt = ((typej&1)==1) ? typej : (-typej);
            flt_t *rBGradAGradAnlmWt = rBGradAGradAnlm + (NMAX+1)*tLMAll;
            backwardBackwardMplusAnlmWt<NMAX+1, LMAXMAX>(rBGradAGradAnlm, rBGradAGradAnlmWt, wt, tY, rBGradAGradY, fc, tBGradAGradFc, tRn, rBGradAGradRn);
        }
    }
}
template <int WTYPE, int MTYPE, int NMAX, int LMAX, int L3MAX, int L4MAX, int SIZE_NP>
static void sphBackwardBackward(int bi,
    int aNlSize, int *aNl, flt_t *aAGradFp, flt_t *rBGradAGradFp,
    flt_t *pos, int *type,
    flt_t *aBGradF, flt_t *aBGradV0,
    flt_t **aForwardCache, flt_t **aBackwardCache, flt_t **rBackwardBackwardCache,
    flt_t aRCut, flt_t *aParams, flt_t *rBGradParams) noexcept {
    // const init
    constexpr int tSizeL = (LMAX+1) + L3NCOLS[L3MAX] + L4NCOLS[L4MAX];
    constexpr int tLMaxMax = LMAX>L3MAX ? (LMAX>L4MAX?LMAX:L4MAX) : (L3MAX>L4MAX?L3MAX:L4MAX);
    constexpr int tLMAll = (tLMaxMax+1)*(tLMaxMax+1);
    constexpr int tSizeAnlm = SIZE_NP*tLMAll;
    // init cache
    flt_t *tAnlm = *aForwardCache; *aForwardCache += tSizeAnlm;
    flt_t *tAGradAnlm = *aBackwardCache; *aBackwardCache += tSizeAnlm;
    flt_t *rBGradAnlm = *rBackwardBackwardCache; *rBackwardBackwardCache += tSizeAnlm;
    flt_t rBGradAGradAnlm[tSizeAnlm] = {0};
    // clear bb cache required
    fill<tSizeAnlm>(rBGradAnlm, ZERO);
    // xyz -> anlm
    backwardBackwardAnlm<WTYPE, MTYPE, NMAX, tLMaxMax, SIZE_NP>(bi,
        aNlSize, aNl, tAGradAnlm, rBGradAGradAnlm,
        pos, type,
        aBGradF, aBGradV0,
        aForwardCache, aBackwardCache, rBackwardBackwardCache,
        aRCut, aParams, rBGradParams
    );
    // anlm -> fp
    constexpr int tSizeL2 = LMAX+1;
    constexpr int tSizeL3 = L3NCOLS[L3MAX];
    for (int np=0, tShift=0, tShiftFp=0; np<SIZE_NP; ++np, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calGradGradSphL2<LMAX>(tAnlm+tShift, rBGradAGradAnlm+tShift, rBGradAGradFp+tShiftFp);
        calGradGradSphL3<L3MAX>(tAnlm+tShift, rBGradAGradAnlm+tShift, rBGradAGradFp+tShiftFp+tSizeL2);
        calGradGradSphL4<L4MAX>(tAnlm+tShift, rBGradAGradAnlm+tShift, rBGradAGradFp+tShiftFp+tSizeL2+tSizeL3);
    }
    // anlm -> anlm
    for (int np=0, tShift=0, tShiftFp=0; np<SIZE_NP; ++np, tShift+=tLMAll, tShiftFp+=tSizeL) {
        calBGradSphL2<LMAX>(tAnlm+tShift, rBGradAnlm+tShift, rBGradAGradAnlm+tShift, aAGradFp+tShiftFp);
        calBGradSphL3<L3MAX>(tAnlm+tShift, rBGradAnlm+tShift, rBGradAGradAnlm+tShift, aAGradFp+tShiftFp+tSizeL2);
        calBGradSphL4<L4MAX>(tAnlm+tShift, rBGradAnlm+tShift, rBGradAGradAnlm+tShift, aAGradFp+tShiftFp+tSizeL2+tSizeL3);
    }
}

}

#endif //BASIS_SPHERICAL_CHEBYSHEV_H