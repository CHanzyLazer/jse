#include "nnap_main.hpp"

// >>> NNAPGEN REMOVE
#define __NNAPGENS_typeiNNAP__ 1
// <<< NNAPGEN REMOVE

#define __jsefunc__

extern "C" {

__jsefunc__ int jse_nnap_calFp(int ctype,
    int aNlSize, JSE_NNAP::flt_t *aNlDx, JSE_NNAP::flt_t *aNlDy, JSE_NNAP::flt_t *aNlDz, int *aNlType,
    JSE_NNAP::flt_t *rFp, JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam) {
    
    int code;
// >>> NNAPGEN SWITCH
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::FALSE>(ctype,
        aNlSize, aNlDx, aNlDy, aNlDz, aNlType,
        rFp, aFpHyperParam, aFpParam,
        NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP TYPE]
    return 0;
}

__jsefunc__ int jse_nnap_calEnergy(int ctype,
    int aNlSize, JSE_NNAP::flt_t *aNlDx, JSE_NNAP::flt_t *aNlDy, JSE_NNAP::flt_t *aNlDz, int *aNlType,
    JSE_NNAP::flt_t *eng,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t **aNormParam) {
    
    int code;
// >>> NNAPGEN SWITCH
    JSE_NNAP::flt_t rLayers[__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__];
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::FALSE>(ctype,
        aNlSize, aNlDx, aNlDy, aNlDz, aNlType,
        rLayers, aFpHyperParam, aFpParam,
        NULL
    );
    if (code!=0) return code;
    code = JSE_NNAP::normedNnForward<__NNAPGENS_ctype__, JSE_NNAP::FALSE, JSE_NNAP::FALSE>(ctype,
        eng, rLayers,
        aNormParam, aNnParam, NULL, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}

__jsefunc__ int jse_nnap_calEnergyForce(int ctype,
    int aNlSize, JSE_NNAP::flt_t *aNlDx, JSE_NNAP::flt_t *aNlDy, JSE_NNAP::flt_t *aNlDz, int *aNlType,
    JSE_NNAP::flt_t *rAGradNlDx, JSE_NNAP::flt_t *rAGradNlDy, JSE_NNAP::flt_t *rAGradNlDz, JSE_NNAP::flt_t *eng,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t **aNormParam,
    JSE_NNAP::flt_t *rFpForwardCache) {
    
    // manual clear required for backward in force
    for (int jj = 0; jj < aNlSize; ++jj) {
        rAGradNlDx[jj] = JSE_NNAP::ZERO;
        rAGradNlDy[jj] = JSE_NNAP::ZERO;
        rAGradNlDz[jj] = JSE_NNAP::ZERO;
    }
    int code;
// >>> NNAPGEN SWITCH
    JSE_NNAP::flt_t rLayers[__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__];
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE>(ctype,
        aNlSize, aNlDx, aNlDy, aNlDz, aNlType,
        rLayers, aFpHyperParam, aFpParam,
        rFpForwardCache
    );
    if (code!=0) return code;
    JSE_NNAP::flt_t rNnGradCache[__NNAPGENX_NN_SIZE_HB__];
    code = JSE_NNAP::normedNnForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE, JSE_NNAP::FALSE>(ctype,
        eng, rLayers,
        aNormParam, aNnParam, rNnGradCache, NULL
    );
    if (code!=0) return code;
    // manual clear required for backward in force
    JSE_NNAP::fill<__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__>(rLayers, JSE_NNAP::ZERO);
    code = JSE_NNAP::normedNnBackward<__NNAPGENS_ctype__, JSE_NNAP::FALSE, JSE_NNAP::FALSE>(ctype,
        JSE_NNAP::ONE, NULL, rLayers, NULL,
        aNormParam, aNnParam, NULL, rNnGradCache
    );
    if (code!=0) return code;
    code = JSE_NNAP::fpBackward<__NNAPGENS_ctype__, JSE_NNAP::FALSE, JSE_NNAP::FALSE, JSE_NNAP::FALSE>(ctype,
        aNlSize, aNlDx, aNlDy, aNlDz, aNlType,
        rAGradNlDx, rAGradNlDy, rAGradNlDz, rLayers,
        aFpHyperParam, aFpParam, NULL,
        rFpForwardCache, NULL, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}


__jsefunc__ int jse_nnap_forwardEnergy(int ctype,
    int aNlSize, JSE_NNAP::flt_t *aNlDx, JSE_NNAP::flt_t *aNlDy, JSE_NNAP::flt_t *aNlDz, int *aNlType,
    JSE_NNAP::flt_t *eng,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t **aNormParam,
    JSE_NNAP::flt_t *rFpForwardCache, JSE_NNAP::flt_t *rNnForwardCache) {
    
    int code;
// >>> NNAPGEN SWITCH
    JSE_NNAP::flt_t *rLayers = rNnForwardCache;
    JSE_NNAP::flt_t *rNnGradCache = rLayers + (__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__);
    
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE>(ctype,
        aNlSize, aNlDx, aNlDy, aNlDz, aNlType,
        rLayers, aFpHyperParam, aFpParam,
        rFpForwardCache
    );
    if (code!=0) return code;
    code = JSE_NNAP::normedNnForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE, JSE_NNAP::FALSE>(ctype,
        eng, rLayers,
        aNormParam, aNnParam, rNnGradCache, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}
__jsefunc__ int jse_nnap_backwardEnergy(int ctype,
    int aNlSize, JSE_NNAP::flt_t *aNlDx, JSE_NNAP::flt_t *aNlDy, JSE_NNAP::flt_t *aNlDz, int *aNlType,
    JSE_NNAP::flt_t aGradEng,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t **aNormParam,
    JSE_NNAP::flt_t **rGradFpParam, JSE_NNAP::flt_t **rGradNnParam,
    JSE_NNAP::flt_t *aFpForwardCache, JSE_NNAP::flt_t *aNnForwardCache) {
    
    int code;
// >>> NNAPGEN SWITCH
    JSE_NNAP::flt_t *tLayers = aNnForwardCache;
    JSE_NNAP::flt_t *tNnGradCache = tLayers + (__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__);
    JSE_NNAP::flt_t rGradLayers[__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__] = {0};
    
    code = JSE_NNAP::normedNnBackward<__NNAPGENS_ctype__, JSE_NNAP::TRUE, JSE_NNAP::FALSE>(ctype,
        aGradEng, tLayers, rGradLayers, NULL,
        aNormParam, aNnParam, rGradNnParam, tNnGradCache
    );
    if (code!=0) return code;
    code = JSE_NNAP::fpBackward<__NNAPGENS_ctype__, JSE_NNAP::TRUE, JSE_NNAP::FALSE, JSE_NNAP::FALSE>(ctype,
        aNlSize, aNlDx, aNlDy, aNlDz, aNlType,
        NULL, NULL, NULL, rGradLayers,
        aFpHyperParam, aFpParam, rGradFpParam,
        aFpForwardCache, NULL, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}

__jsefunc__ int jse_nnap_forwardEnergyForce(int ctype,
    int aNlSize, JSE_NNAP::flt_t *aNlDx, JSE_NNAP::flt_t *aNlDy, JSE_NNAP::flt_t *aNlDz, int *aNlType,
    JSE_NNAP::flt_t *rAGradNlDx, JSE_NNAP::flt_t *rAGradNlDy, JSE_NNAP::flt_t *rAGradNlDz, JSE_NNAP::flt_t *eng,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t **aNormParam,
    JSE_NNAP::flt_t *rFpForwardCache, JSE_NNAP::flt_t *rNnForwardCache, JSE_NNAP::flt_t *rFpBackwardCache, JSE_NNAP::flt_t *rNnBackwardCache) {
    
    // manual clear required for backward in force
    for (int jj = 0; jj < aNlSize; ++jj) {
        rAGradNlDx[jj] = JSE_NNAP::ZERO;
        rAGradNlDy[jj] = JSE_NNAP::ZERO;
        rAGradNlDz[jj] = JSE_NNAP::ZERO;
    }
    int code;
// >>> NNAPGEN SWITCH
    JSE_NNAP::flt_t *rLayers = rNnForwardCache;
    JSE_NNAP::flt_t *rNnGradCache = rLayers + (__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__);
    JSE_NNAP::flt_t *rNnGradGradCache = rNnBackwardCache;
    JSE_NNAP::flt_t *rAGradLayers = rNnGradGradCache + __NNAPGENX_NN_SIZE_HB__;
    JSE_NNAP::flt_t *rAGradLayersZ = rAGradLayers + (__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__);
    // manual clear required for backward in force
    JSE_NNAP::fill<__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__>(rAGradLayers, JSE_NNAP::ZERO);
    JSE_NNAP::fill<__NNAPGENX_NN_SIZE_HB__>(rAGradLayersZ, JSE_NNAP::ZERO);
    
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE>(ctype,
        aNlSize, aNlDx, aNlDy, aNlDz, aNlType,
        rLayers, aFpHyperParam, aFpParam,
        rFpForwardCache
    );
    if (code!=0) return code;
    code = JSE_NNAP::normedNnForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE, JSE_NNAP::TRUE>(ctype,
        eng, rLayers,
        aNormParam, aNnParam, rNnGradCache, rNnGradGradCache
    );
    if (code!=0) return code;
    code = JSE_NNAP::normedNnBackward<__NNAPGENS_ctype__, JSE_NNAP::FALSE, JSE_NNAP::TRUE>(ctype,
        JSE_NNAP::ONE, NULL, rAGradLayers, rAGradLayersZ,
        aNormParam, aNnParam, NULL, rNnGradCache
    );
    if (code!=0) return code;
    code = JSE_NNAP::fpBackward<__NNAPGENS_ctype__, JSE_NNAP::FALSE, JSE_NNAP::FALSE, JSE_NNAP::TRUE>(ctype,
        aNlSize, aNlDx, aNlDy, aNlDz, aNlType,
        rAGradNlDx, rAGradNlDy, rAGradNlDz, rAGradLayers,
        aFpHyperParam, aFpParam, NULL,
        rFpForwardCache, rFpBackwardCache, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}
__jsefunc__ int jse_nnap_backwardEnergyForce(int ctype,
    int aNlSize, JSE_NNAP::flt_t *aNlDx, JSE_NNAP::flt_t *aNlDy, JSE_NNAP::flt_t *aNlDz, int *aNlType,
    JSE_NNAP::flt_t *aBGradNlDx, JSE_NNAP::flt_t *aBGradNlDy, JSE_NNAP::flt_t *aBGradNlDz, JSE_NNAP::flt_t aBGradEng,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t **aNormParam,
    JSE_NNAP::flt_t **rBGradFpParam, JSE_NNAP::flt_t **rBGradNnParam,
    JSE_NNAP::flt_t *aFpForwardCache, JSE_NNAP::flt_t *aNnForwardCache, JSE_NNAP::flt_t *aFpBackwardCache, JSE_NNAP::flt_t *aNnBackwardCache,
    JSE_NNAP::flt_t *rFpBackwardBackwardCache) {
    
    int code;
// >>> NNAPGEN SWITCH
    JSE_NNAP::flt_t *tLayers = aNnForwardCache;
    JSE_NNAP::flt_t *tNnGradCache = tLayers + (__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__);
    JSE_NNAP::flt_t *tNnGradGradCache = aNnBackwardCache;
    JSE_NNAP::flt_t *tAGradLayers = tNnGradGradCache + __NNAPGENX_NN_SIZE_HB__;
    JSE_NNAP::flt_t *tAGradLayersZ = tAGradLayers + (__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__);
    JSE_NNAP::flt_t rBGradAGradLayers[__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__] = {0};
    JSE_NNAP::flt_t rBGradLayersZ[__NNAPGENX_NN_SIZE_HB__] = {0};
    JSE_NNAP::flt_t rBGradLayers[__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__] = {0};
    
    code = JSE_NNAP::fpBackwardBackward<__NNAPGENS_ctype__>(ctype,
        aNlSize, aNlDx, aNlDy, aNlDz, aNlType,
        aBGradNlDx, aBGradNlDy, aBGradNlDz,
        tAGradLayers, rBGradAGradLayers,
        aFpHyperParam, aFpParam, rBGradFpParam,
        aFpForwardCache, aFpBackwardCache, rFpBackwardBackwardCache
    );
    if (code!=0) return code;
    code = JSE_NNAP::normedNnBackwardBackward<__NNAPGENS_ctype__, JSE_NNAP::FALSE>(ctype,
        JSE_NNAP::ONE, NULL, tAGradLayers, rBGradAGradLayers, tAGradLayersZ, rBGradLayersZ,
        aNormParam, aNnParam, rBGradNnParam, tNnGradCache, tNnGradGradCache
    );
    if (code!=0) return code;
    code = JSE_NNAP::normedNnBackward<__NNAPGENS_ctype__, JSE_NNAP::TRUE, JSE_NNAP::TRUE>(ctype,
        aBGradEng, tLayers, rBGradLayers, rBGradLayersZ,
        aNormParam, aNnParam, rBGradNnParam, tNnGradCache
    );
    if (code!=0) return code;
    code = JSE_NNAP::fpBackward<__NNAPGENS_ctype__, JSE_NNAP::TRUE, JSE_NNAP::TRUE, JSE_NNAP::FALSE>(ctype,
        aNlSize, aNlDx, aNlDy, aNlDz, aNlType,
        NULL, NULL, NULL, rBGradLayers,
        aFpHyperParam, aFpParam, rBGradFpParam,
        aFpForwardCache, NULL, rFpBackwardBackwardCache
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}

__jsefunc__ int jse_nnap_forwardForceCollect(int i, int aNlSize,
    JSE_NNAP::flt_t *aNlDx, JSE_NNAP::flt_t *aNlDy, JSE_NNAP::flt_t *aNlDz, int *aNlIdx,
    JSE_NNAP::flt_t *aAGradNlDx, JSE_NNAP::flt_t *aAGradNlDy, JSE_NNAP::flt_t *aAGradNlDz,
    JSE_NNAP::flt_t *rFx, JSE_NNAP::flt_t *rFy, JSE_NNAP::flt_t *rFz, JSE_NNAP::flt_t *rV) {
    
    JSE_NNAP::flt_t f0xi = JSE_NNAP::ZERO, f0yi = JSE_NNAP::ZERO, f0zi = JSE_NNAP::ZERO;
    JSE_NNAP::flt_t v0xxi = JSE_NNAP::ZERO, v0yyi = JSE_NNAP::ZERO, v0zzi = JSE_NNAP::ZERO;
    JSE_NNAP::flt_t v0xyi = JSE_NNAP::ZERO, v0xzi = JSE_NNAP::ZERO, v0yzi = JSE_NNAP::ZERO;
    for (int jj = 0; jj < aNlSize; ++jj) {
        const JSE_NNAP::flt_t dx = aNlDx[jj];
        const JSE_NNAP::flt_t dy = aNlDy[jj];
        const JSE_NNAP::flt_t dz = aNlDz[jj];
        const JSE_NNAP::flt_t fxj = aAGradNlDx[jj];
        const JSE_NNAP::flt_t fyj = aAGradNlDy[jj];
        const JSE_NNAP::flt_t fzj = aAGradNlDz[jj];
        const int j = aNlIdx[jj];
        
        f0xi += fxj; f0yi += fyj; f0zi += fzj;
        rFx[j] -= fxj; rFy[j] -= fyj; rFz[j] -= fzj;
        v0xxi -= dx*fxj; v0yyi -= dy*fyj; v0zzi -= dz*fzj;
        v0xyi -= dx*fyj; v0xzi -= dx*fzj; v0yzi -= dy*fzj;
    }
    rFx[i] += f0xi; rFy[i] += f0yi; rFz[i] += f0zi;
    rV[0] += v0xxi; rV[1] += v0yyi; rV[2] += v0zzi;
    rV[3] += v0xyi; rV[4] += v0xzi; rV[5] += v0yzi;
    return 0;
}
__jsefunc__ int jse_nnap_backwardForceCollect(int i, int aNlSize,
    JSE_NNAP::flt_t *aNlDx, JSE_NNAP::flt_t *aNlDy, JSE_NNAP::flt_t *aNlDz, int *aNlIdx,
    JSE_NNAP::flt_t *rBGradAGradNlDx, JSE_NNAP::flt_t *rBGradAGradNlDy, JSE_NNAP::flt_t *rBGradAGradNlDz,
    JSE_NNAP::flt_t *aBGradFx, JSE_NNAP::flt_t *aBGradFy, JSE_NNAP::flt_t *aBGradFz, JSE_NNAP::flt_t *aBGradV) {
    
    const JSE_NNAP::flt_t tBGradFxi = aBGradFx[i];
    const JSE_NNAP::flt_t tBGradFyi = aBGradFy[i];
    const JSE_NNAP::flt_t tBGradFzi = aBGradFz[i];
    const JSE_NNAP::flt_t tBGradVxx = aBGradV[0], tBGradVyy = aBGradV[1], tBGradVzz = aBGradV[2];
    const JSE_NNAP::flt_t tBGradVxy = aBGradV[3], tBGradVxz = aBGradV[4], tBGradVyz = aBGradV[5];
    for (int jj = 0; jj < aNlSize; ++jj) {
        const JSE_NNAP::flt_t dx = aNlDx[jj];
        const JSE_NNAP::flt_t dy = aNlDy[jj];
        const JSE_NNAP::flt_t dz = aNlDz[jj];
        const int j = aNlIdx[jj];
        JSE_NNAP::flt_t rBGradFxj = JSE_NNAP::ZERO, rBGradFyj = JSE_NNAP::ZERO, rBGradFzj = JSE_NNAP::ZERO;
        rBGradFxj += tBGradFxi - aBGradFx[j];
        rBGradFyj += tBGradFyi - aBGradFy[j];
        rBGradFzj += tBGradFzi - aBGradFz[j];
        rBGradFxj -= dx*tBGradVxx;
        rBGradFyj -= dy*tBGradVyy + dx*tBGradVxy;
        rBGradFzj -= dz*tBGradVzz + dx*tBGradVxz + dy*tBGradVyz;
        // set here for auto clear
        rBGradAGradNlDx[jj] = rBGradFxj;
        rBGradAGradNlDy[jj] = rBGradFyj;
        rBGradAGradNlDz[jj] = rBGradFzj;
    }
    return 0;
}


__jsefunc__ int jse_nnap_statNlSizeLammps(int inum, int *ilist, int *numneigh, int *numneighMax) {
    int numneighMax_ = 0;
    for (int ii = 0; ii < inum; ++ii) {
        int jnum = numneigh[ilist[ii]];
        if (jnum > numneighMax_) numneighMax_ = jnum;
    }
    numneighMax[0] = numneighMax_;
    return 0;
}

#define JSE_LMP_NEIGHMASK 0x1FFFFFFF

__jsefunc__ int jse_nnap_computeLammps(
    int inum, int ntypes, int eflag, int eflagAtom, int vflag, int vflagAtom, int cvflagAtom,
    double **x, double **f, int *type, int *ilist,
    int *numneigh, int **firstneigh, double *cutsq,
    int *aLmpType2NNAPType, int **rTypeIlist, int *rTypeInum,
    double *engVdwl, double *eatom, double *virial, double **vatom, double **cvatom,
    JSE_NNAP::flt_t *rNlDx, JSE_NNAP::flt_t *rNlDy, JSE_NNAP::flt_t *rNlDz, int *rNlType, int *rNlIdx,
    JSE_NNAP::flt_t *rAGradNlDx, JSE_NNAP::flt_t *rAGradNlDy, JSE_NNAP::flt_t *rAGradNlDz,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t **aNormParam,
    JSE_NNAP::flt_t *rFpForwardCache) {
    
    /// reorder by types
    for (int typei = 1; typei <= ntypes; ++typei) {
        rTypeInum[typei] = 0;
    }
    for (int ii = 0; ii < inum; ++ii) {
        int i = ilist[ii];
        int typei = type[i];
        rTypeIlist[typei][rTypeInum[typei]] = i;
        ++rTypeInum[typei];
    }
    
    /// begin compute here
    for (int typei = 1; typei <= ntypes; ++typei) {
        int *subIlist = rTypeIlist[typei];
        int subInum = rTypeInum[typei];
        const int typeiNNAP = aLmpType2NNAPType[typei];
        
        for (int ii = 0; ii < subInum; ++ii) {
            const int i = subIlist[ii];
            const double xi = x[i][0];
            const double yi = x[i][1];
            const double zi = x[i][2];
            const int *jlist = firstneigh[i];
            const int jnum = numneigh[i];
            
            /// build neighbor list
            int tNlSize = 0;
            for (int jj = 0; jj < jnum; ++jj) {
                int j = jlist[jj];
                j &= JSE_LMP_NEIGHMASK;
                // Note that dxyz in jse and lammps are defined oppositely
                const double dx = x[j][0] - xi;
                const double dy = x[j][1] - yi;
                const double dz = x[j][2] - zi;
                const double rsq = dx*dx + dy*dy + dz*dz;
                if (rsq < cutsq[typei]) {
                    rNlDx[tNlSize] = (JSE_NNAP::flt_t)dx;
                    rNlDy[tNlSize] = (JSE_NNAP::flt_t)dy;
                    rNlDz[tNlSize] = (JSE_NNAP::flt_t)dz;
                    rNlType[tNlSize] = aLmpType2NNAPType[type[j]];
                    rNlIdx[tNlSize] = j;
                    ++tNlSize;
                }
            }
            
            /// begin nnap here
            JSE_NNAP::flt_t eng = JSE_NNAP::ZERO;
            // manual clear required for backward in force
            for (int jj = 0; jj < tNlSize; ++jj) {
                rAGradNlDx[jj] = JSE_NNAP::ZERO;
                rAGradNlDy[jj] = JSE_NNAP::ZERO;
                rAGradNlDz[jj] = JSE_NNAP::ZERO;
            }
            int code;
// >>> NNAPGEN SWITCH
            JSE_NNAP::flt_t rLayers[__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__];
            code = JSE_NNAP::fpForward<__NNAPGENS_typeiNNAP__, JSE_NNAP::TRUE>(typeiNNAP,
                tNlSize, rNlDx, rNlDy, rNlDz, rNlType,
                rLayers, aFpHyperParam, aFpParam,
                rFpForwardCache
            );
            if (code!=0) return code;
            JSE_NNAP::flt_t rNnGradCache[__NNAPGENX_NN_SIZE_HB__];
            code = JSE_NNAP::normedNnForward<__NNAPGENS_typeiNNAP__, JSE_NNAP::TRUE, JSE_NNAP::FALSE>(typeiNNAP,
                &eng, rLayers,
                aNormParam, aNnParam, rNnGradCache, NULL
            );
            if (code!=0) return code;
            // manual clear required for backward in force
            JSE_NNAP::fill<__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__>(rLayers, JSE_NNAP::ZERO);
            code = JSE_NNAP::normedNnBackward<__NNAPGENS_typeiNNAP__, JSE_NNAP::FALSE, JSE_NNAP::FALSE>(typeiNNAP,
                JSE_NNAP::ONE, NULL, rLayers, NULL,
                aNormParam, aNnParam, NULL, rNnGradCache
            );
            if (code!=0) return code;
            code = JSE_NNAP::fpBackward<__NNAPGENS_typeiNNAP__, JSE_NNAP::FALSE, JSE_NNAP::FALSE, JSE_NNAP::FALSE>(typeiNNAP,
                tNlSize, rNlDx, rNlDy, rNlDz, rNlType,
                rAGradNlDx, rAGradNlDy, rAGradNlDz, rLayers,
                aFpHyperParam, aFpParam, NULL,
                rFpForwardCache, NULL, NULL
            );
            if (code!=0) return code;
// <<< NNAPGEN SWITCH (typeiNNAP) [FP NN TYPE]
            
            /// collect results
            if (eflag) {
                *engVdwl += eng;
                if (eflagAtom) eatom[i] += eng;
            }
            double f0xi = 0.0, f0yi = 0.0, f0zi = 0.0;
            double v0xxi = 0.0, v0yyi = 0.0, v0zzi = 0.0;
            double v0xyi = 0.0, v0xzi = 0.0, v0yzi = 0.0;
            for (int jj = 0; jj < tNlSize; ++jj) {
                const int j = rNlIdx[jj];
                const JSE_NNAP::flt_t fxj = rAGradNlDx[jj];
                const JSE_NNAP::flt_t fyj = rAGradNlDy[jj];
                const JSE_NNAP::flt_t fzj = rAGradNlDz[jj];
                f0xi += fxj; f0yi += fyj; f0zi += fzj;
                f[j][0] -= fxj; f[j][1] -= fyj; f[j][2] -= fzj;
                if (vflag) {
                    const double dx = x[j][0] - xi;
                    const double dy = x[j][1] - yi;
                    const double dz = x[j][2] - zi;
                    const double vxxj = -dx*fxj, vyyj = -dy*fyj, vzzj = -dz*fzj;
                    const double vxyj = -dx*fyj, vxzj = -dx*fzj, vyzj = -dy*fzj;
                    v0xxi += vxxj; v0yyi += vyyj; v0zzi += vzzj;
                    v0xyi += vxyj; v0xzi += vxzj; v0yzi += vyzj;
                    if (vflagAtom) {
                        vatom[j][0] += vxxj; vatom[j][1] += vyyj; vatom[j][2] += vzzj;
                        vatom[j][3] += vxyj; vatom[j][4] += vxzj; vatom[j][5] += vyzj;
                    }
                    if (cvflagAtom) {
                        cvatom[j][0] += vxxj; cvatom[j][1] += vyyj; cvatom[j][2] += vzzj;
                        cvatom[j][3] += vxyj; cvatom[j][4] += vxzj; cvatom[j][5] += vyzj;
                        cvatom[j][6] -= dy*fxj;
                        cvatom[j][7] -= dz*fxj;
                        cvatom[j][8] -= dz*fyj;
                    }
                }
            }
            f[i][0] += f0xi; f[i][1] += f0yi; f[i][2] += f0zi;
            if (vflag) {
                virial[0] += v0xxi; virial[1] += v0yyi; virial[2] += v0zzi;
                virial[3] += v0xyi; virial[4] += v0xzi; virial[5] += v0yzi;
            }
        }
    }
    return 0;
}

}

