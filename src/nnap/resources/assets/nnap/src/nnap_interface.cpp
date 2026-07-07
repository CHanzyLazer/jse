#include "nnap_main.hpp"

// >>> NNAPGEN REMOVE
#define __NNAPGENS_typeiNNAP__ 1
// <<< NNAPGEN REMOVE

#define __jsefunc__

extern "C" {

__jsefunc__ int jse_nnap_calFp(int bi,
    JSE_NNAP::flt_t *aPosX, JSE_NNAP::flt_t *aPosY, JSE_NNAP::flt_t *aPosZ, int *aType, int aNlSize, int *aNl,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t *rFp) {
    
    const int ctype = aType[bi];
    int code;
// >>> NNAPGEN SWITCH
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::FALSE>(bi, ctype,
        aPosX, aPosY, aPosZ, aType, aNlSize, aNl,
        rFp, aFpHyperParam, aFpParam, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP TYPE]
    return 0;
}

__jsefunc__ int jse_nnap_calEnergy(int bi,
    JSE_NNAP::flt_t *aPosX, JSE_NNAP::flt_t *aPosY, JSE_NNAP::flt_t *aPosZ, int *aType, int aNlSize, int *aNl,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t *aNormParam,
    JSE_NNAP::flt_t *rOutEng) {
    
    const int ctype = aType[bi];
    int code;
// >>> NNAPGEN SWITCH
    JSE_NNAP::flt_t rLayers[__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__];
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::FALSE>(bi, ctype,
        aPosX, aPosY, aPosZ, aType, aNlSize, aNl,
        rLayers, aFpHyperParam, aFpParam, NULL
    );
    if (code!=0) return code;
    code = JSE_NNAP::normedNnForward<__NNAPGENS_ctype__, JSE_NNAP::FALSE, JSE_NNAP::FALSE>(ctype,
        rOutEng, rLayers,
        aNormParam, aNnParam, NULL, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}

__jsefunc__ int jse_nnap_calEnergyForce(int bi,
    JSE_NNAP::flt_t *aPosX, JSE_NNAP::flt_t *aPosY, JSE_NNAP::flt_t *aPosZ, int *aType, int aNlSize, int *aNl,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t *aNormParam,
    JSE_NNAP::flt_t *rOutEng, JSE_NNAP::flt_t *rGradNlDx, JSE_NNAP::flt_t *rGradNlDy, JSE_NNAP::flt_t *rGradNlDz,
    JSE_NNAP::flt_t *rFpForwardCache) {
    
    const int ctype = aType[bi];
    // manual clear required for backward in force
    for (int j = 0; j < aNlSize; ++j) {
        rGradNlDx[j] = JSE_NNAP::ZERO;
        rGradNlDy[j] = JSE_NNAP::ZERO;
        rGradNlDz[j] = JSE_NNAP::ZERO;
    }
    int code;
// >>> NNAPGEN SWITCH
    JSE_NNAP::flt_t rLayers[__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__];
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE>(bi, ctype,
        aPosX, aPosY, aPosZ, aType, aNlSize, aNl,
        rLayers, aFpHyperParam, aFpParam, rFpForwardCache
    );
    if (code!=0) return code;
    JSE_NNAP::flt_t rNnGradCache[__NNAPGENX_NN_SIZE_HB__];
    code = JSE_NNAP::normedNnForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE, JSE_NNAP::FALSE>(ctype,
        rOutEng, rLayers,
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
    code = JSE_NNAP::fpBackward<__NNAPGENS_ctype__, JSE_NNAP::FALSE, JSE_NNAP::FALSE, JSE_NNAP::FALSE>(bi, ctype,
        aPosX, aPosY, aPosZ, aType, aNlSize, aNl,
        rLayers, rGradNlDx, rGradNlDy, rGradNlDz, aFpHyperParam,
        aFpParam, NULL, rFpForwardCache, NULL, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}


__jsefunc__ int jse_nnap_forwardEnergy(int bi,
    JSE_NNAP::flt_t *aPosX, JSE_NNAP::flt_t *aPosY, JSE_NNAP::flt_t *aPosZ, int *aType, int aNlSize, int *aNl,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t *aNormParam,
    JSE_NNAP::flt_t *rOutEng, JSE_NNAP::flt_t *rFpForwardCache, JSE_NNAP::flt_t *rNnForwardCache) {
    
    const int ctype = aType[bi];
    int code;
// >>> NNAPGEN SWITCH
    JSE_NNAP::flt_t *rLayers = rNnForwardCache;
    JSE_NNAP::flt_t *rNnGradCache = rLayers + (__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__);
    
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE>(bi, ctype,
        aPosX, aPosY, aPosZ, aType, aNlSize, aNl,
        rLayers, aFpHyperParam, aFpParam, rFpForwardCache
    );
    if (code!=0) return code;
    code = JSE_NNAP::normedNnForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE, JSE_NNAP::FALSE>(ctype,
        rOutEng, rLayers,
        aNormParam, aNnParam, rNnGradCache, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}
__jsefunc__ int jse_nnap_backwardEnergy(int bi,
    JSE_NNAP::flt_t *aPosX, JSE_NNAP::flt_t *aPosY, JSE_NNAP::flt_t *aPosZ, int *aType, int aNlSize, int *aNl,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t *aNormParam,
    JSE_NNAP::flt_t aGradEng, JSE_NNAP::flt_t **rGradFpParam, JSE_NNAP::flt_t **rGradNnParam,
    JSE_NNAP::flt_t *aFpForwardCache, JSE_NNAP::flt_t *aNnForwardCache) {
    
    const int ctype = aType[bi];
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
    code = JSE_NNAP::fpBackward<__NNAPGENS_ctype__, JSE_NNAP::TRUE, JSE_NNAP::FALSE, JSE_NNAP::FALSE>(bi, ctype,
        aPosX, aPosY, aPosZ, aType, aNlSize, aNl,
        rGradLayers, NULL, NULL, NULL, aFpHyperParam,
        aFpParam, rGradFpParam, aFpForwardCache, NULL, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}

__jsefunc__ int jse_nnap_forwardEnergyForce(int bi,
    JSE_NNAP::flt_t *aPosX, JSE_NNAP::flt_t *aPosY, JSE_NNAP::flt_t *aPosZ, int *aType, int aNlSize, int *aNl,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t *aNormParam,
    JSE_NNAP::flt_t *rOutEng, JSE_NNAP::flt_t *rGradNlDx, JSE_NNAP::flt_t *rGradNlDy, JSE_NNAP::flt_t *rGradNlDz,
    JSE_NNAP::flt_t *rFpForwardCache, JSE_NNAP::flt_t *rNnForwardCache, JSE_NNAP::flt_t *rFpBackwardCache, JSE_NNAP::flt_t *rNnBackwardCache) {
    
    const int ctype = aType[bi];
    // manual clear required for backward in force
    for (int j = 0; j < aNlSize; ++j) {
        rGradNlDx[j] = JSE_NNAP::ZERO;
        rGradNlDy[j] = JSE_NNAP::ZERO;
        rGradNlDz[j] = JSE_NNAP::ZERO;
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
    
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE>(bi, ctype,
        aPosX, aPosY, aPosZ, aType, aNlSize, aNl,
        rLayers, aFpHyperParam, aFpParam, rFpForwardCache
    );
    if (code!=0) return code;
    code = JSE_NNAP::normedNnForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE, JSE_NNAP::TRUE>(ctype,
        rOutEng, rLayers,
        aNormParam, aNnParam, rNnGradCache, rNnGradGradCache
    );
    if (code!=0) return code;
    code = JSE_NNAP::normedNnBackward<__NNAPGENS_ctype__, JSE_NNAP::FALSE, JSE_NNAP::TRUE>(ctype,
        JSE_NNAP::ONE, NULL, rAGradLayers, rAGradLayersZ,
        aNormParam, aNnParam, NULL, rNnGradCache
    );
    if (code!=0) return code;
    code = JSE_NNAP::fpBackward<__NNAPGENS_ctype__, JSE_NNAP::FALSE, JSE_NNAP::FALSE, JSE_NNAP::TRUE>(bi, ctype,
        aPosX, aPosY, aPosZ, aType, aNlSize, aNl,
        rAGradLayers, rGradNlDx, rGradNlDy, rGradNlDz, aFpHyperParam,
        aFpParam, NULL, rFpForwardCache, rFpBackwardCache, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}
__jsefunc__ int jse_nnap_backwardEnergyForce(int bi,
    JSE_NNAP::flt_t *aPosX, JSE_NNAP::flt_t *aPosY, JSE_NNAP::flt_t *aPosZ, int *aType, int aNlSize, int *aNl,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t *aNormParam,
    JSE_NNAP::flt_t aBGradEng, JSE_NNAP::flt_t *aBGradAGradNlDx, JSE_NNAP::flt_t *aBGradAGradNlDy, JSE_NNAP::flt_t *aBGradAGradNlDz,
    JSE_NNAP::flt_t **rBGradFpParam, JSE_NNAP::flt_t **rBGradNnParam,
    JSE_NNAP::flt_t *aFpForwardCache, JSE_NNAP::flt_t *aNnForwardCache, JSE_NNAP::flt_t *aFpBackwardCache, JSE_NNAP::flt_t *aNnBackwardCache,
    JSE_NNAP::flt_t *rFpBackwardBackwardCache) {
    
    const int ctype = aType[bi];
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
    
    code = JSE_NNAP::fpBackwardBackward<__NNAPGENS_ctype__>(bi, ctype,
        aPosX, aPosY, aPosZ, aType, aNlSize, aNl,
        tAGradLayers, rBGradAGradLayers,
        aBGradAGradNlDx, aBGradAGradNlDy, aBGradAGradNlDz,
        aFpHyperParam, aFpParam, rBGradFpParam, aFpForwardCache, aFpBackwardCache, rFpBackwardBackwardCache
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
    code = JSE_NNAP::fpBackward<__NNAPGENS_ctype__, JSE_NNAP::TRUE, JSE_NNAP::TRUE, JSE_NNAP::FALSE>(bi, ctype,
        aPosX, aPosY, aPosZ, aType, aNlSize, aNl,
        rBGradLayers, NULL, NULL, NULL, aFpHyperParam,
        aFpParam, rBGradFpParam, aFpForwardCache, NULL, rFpBackwardBackwardCache
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}


#define JSE_LMP_NEIGHMASK 0x1FFFFFFF

__jsefunc__ int jse_nnap_statNeiNumLammps(int *ilist, int *numneigh, int inum, int *numneighMax) {
    int numneighMax_ = 0;
    for (int ii = 0; ii < inum; ++ii) {
        int i = ilist[ii];
        int jnum = numneigh[i];
        if (jnum > numneighMax_) numneighMax_ = jnum;
    }
    numneighMax[0] = numneighMax_;
    return 0;
}

__jsefunc__ int jse_nnap_computeLammps(
    int nlocalghost, int inum, int ntypes,
    int eflag, int vflag, int eflagAtom, int vflagAtom, int cvflagAtom,
    double **x, double **f, int *type, int *ilist,
    int *numneigh, int **firstneigh, double *cutsq,
    int *aLmpType2NNAPType, int **rTypeIlist, int *rTypeInum,
    double *engVdwl, double *eatom, double *virial, double **vatom, double **cvatom,
    JSE_NNAP::flt_t *rPosX, JSE_NNAP::flt_t *rPosY, JSE_NNAP::flt_t *rPosZ, int *rType, int *rNl,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t **aNormParam,
    JSE_NNAP::flt_t *rGradNlDx, JSE_NNAP::flt_t *rGradNlDy, JSE_NNAP::flt_t *rGradNlDz,
    JSE_NNAP::flt_t *rFpForwardCache) {
    
    /// lammps x type -> jse pos xyz type
    for (int i = 0; i < nlocalghost; ++i) {
        rPosX[i] = (JSE_NNAP::flt_t)x[i][0];
        rPosY[i] = (JSE_NNAP::flt_t)x[i][1];
        rPosZ[i] = (JSE_NNAP::flt_t)x[i][2];
        rType[i] = aLmpType2NNAPType[type[i]];
    }
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
        JSE_NNAP::flt_t *subNormParam = aNormParam[typeiNNAP-1];
        
        for (int ii = 0; ii < subInum; ++ii) {
            int i = subIlist[ii];
            const JSE_NNAP::flt_t xtmp = rPosX[i];
            const JSE_NNAP::flt_t ytmp = rPosY[i];
            const JSE_NNAP::flt_t ztmp = rPosZ[i];
            int *jlist = firstneigh[i];
            int jnum = numneigh[i];
            
            /// build neighbor list
            int rNlSize = 0;
            for (int jj = 0; jj < jnum; ++jj) {
                int j = jlist[jj];
                j &= JSE_LMP_NEIGHMASK;
                // Note that dxyz in jse and lammps are defined oppositely
                const JSE_NNAP::flt_t delx = rPosX[j] - xtmp;
                const JSE_NNAP::flt_t dely = rPosY[j] - ytmp;
                const JSE_NNAP::flt_t delz = rPosZ[j] - ztmp;
                const JSE_NNAP::flt_t rsq = delx*delx + dely*dely + delz*delz;
                if (rsq < cutsq[typei]) {
                    rNl[rNlSize] = j;
                    ++rNlSize;
                }
            }
            
            /// begin nnap here
            JSE_NNAP::flt_t rEng;
            // manual clear required for backward in force
            for (int jj = 0; jj < rNlSize; ++jj) {
                rGradNlDx[jj] = JSE_NNAP::ZERO;
                rGradNlDy[jj] = JSE_NNAP::ZERO;
                rGradNlDz[jj] = JSE_NNAP::ZERO;
            }
            int code;
// >>> NNAPGEN SWITCH
            JSE_NNAP::flt_t rLayers[__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__];
            code = JSE_NNAP::fpForward<__NNAPGENS_typeiNNAP__, JSE_NNAP::TRUE>(i, typeiNNAP,
                rPosX, rPosY, rPosZ, rType, rNlSize, rNl,
                rLayers, aFpHyperParam, aFpParam, rFpForwardCache
            );
            if (code!=0) return code;
            JSE_NNAP::flt_t rNnGradCache[__NNAPGENX_NN_SIZE_HB__];
            code = JSE_NNAP::normedNnForward<__NNAPGENS_typeiNNAP__, JSE_NNAP::TRUE, JSE_NNAP::FALSE>(typeiNNAP,
                &rEng, rLayers,
                subNormParam, aNnParam, rNnGradCache, NULL
            );
            if (code!=0) return code;
            // manual clear required for backward in force
            JSE_NNAP::fill<__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__>(rLayers, JSE_NNAP::ZERO);
            code = JSE_NNAP::normedNnBackward<__NNAPGENS_typeiNNAP__, JSE_NNAP::FALSE, JSE_NNAP::FALSE>(typeiNNAP,
                JSE_NNAP::ONE, NULL, rLayers, NULL,
                subNormParam, aNnParam, NULL, rNnGradCache
            );
            if (code!=0) return code;
            code = JSE_NNAP::fpBackward<__NNAPGENS_typeiNNAP__, JSE_NNAP::FALSE, JSE_NNAP::FALSE, JSE_NNAP::FALSE>(i, typeiNNAP,
                rPosX, rPosY, rPosZ, rType, rNlSize, rNl,
                rLayers, rGradNlDx, rGradNlDy, rGradNlDz,
                aFpHyperParam, aFpParam, NULL, rFpForwardCache, NULL, NULL
            );
            if (code!=0) return code;
// <<< NNAPGEN SWITCH (typeiNNAP) [FP NN TYPE]
            
            /// collect results
            if (eflag) {
                *engVdwl += rEng;
                if (eflagAtom) eatom[i] += rEng;
            }
            for (int jj = 0; jj < rNlSize; ++jj) {
                const int j = rNl[jj];
                const JSE_NNAP::flt_t fx = rGradNlDx[jj];
                const JSE_NNAP::flt_t fy = rGradNlDy[jj];
                const JSE_NNAP::flt_t fz = rGradNlDz[jj];
                f[i][0] -= fx;
                f[i][1] -= fy;
                f[i][2] -= fz;
                f[j][0] += fx;
                f[j][1] += fy;
                f[j][2] += fz;
                if (vflag) {
                    const JSE_NNAP::flt_t dx = rPosX[j] - xtmp;
                    const JSE_NNAP::flt_t dy = rPosY[j] - ytmp;
                    const JSE_NNAP::flt_t dz = rPosZ[j] - ztmp;
                    virial[0] += dx*fx;
                    virial[1] += dy*fy;
                    virial[2] += dz*fz;
                    virial[3] += dx*fy;
                    virial[4] += dx*fz;
                    virial[5] += dy*fz;
                    if (vflagAtom) {
                        vatom[j][0] += dx*fx;
                        vatom[j][1] += dy*fy;
                        vatom[j][2] += dz*fz;
                        vatom[j][3] += dx*fy;
                        vatom[j][4] += dx*fz;
                        vatom[j][5] += dy*fz;
                    }
                    if (cvflagAtom) {
                        cvatom[j][0] += dx*fx;
                        cvatom[j][1] += dy*fy;
                        cvatom[j][2] += dz*fz;
                        cvatom[j][3] += dx*fy;
                        cvatom[j][4] += dx*fz;
                        cvatom[j][5] += dy*fz;
                        cvatom[j][6] += dy*fx;
                        cvatom[j][7] += dz*fx;
                        cvatom[j][8] += dz*fy;
                    }
                }
            }
        }
    }
    return 0;
}

}

