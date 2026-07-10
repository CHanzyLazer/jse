#include "nnap_main.hpp"

// >>> NNAPGEN REMOVE
#define __NNAPGENS_typeiNNAP__ 1
// <<< NNAPGEN REMOVE

#define __jsefunc__

extern "C" {

__jsefunc__ int jse_nnap_calFp(int bi,
    int aNlSize, int *aNl,
    JSE_NNAP::flt_t *pos, int *type,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t *rFp) {
    
    const int ctype = type[bi];
    int code;
// >>> NNAPGEN SWITCH
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::FALSE>(bi, ctype,
        aNlSize, aNl, rFp,
        pos, type,
        aFpHyperParam, aFpParam, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP TYPE]
    return 0;
}

__jsefunc__ int jse_nnap_calEnergy(int bi,
    int aNlSize, int *aNl,
    JSE_NNAP::flt_t *pos, int *type,
    JSE_NNAP::flt_t *eng,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t *aNormParam) {
    
    const int ctype = type[bi];
    int code;
// >>> NNAPGEN SWITCH
    JSE_NNAP::flt_t rLayers[__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__];
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::FALSE>(bi, ctype,
        aNlSize, aNl, rLayers,
        pos, type,
        aFpHyperParam, aFpParam, NULL
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

__jsefunc__ int jse_nnap_calEnergyForce(int bi,
    int aNlSize, int *aNl,
    JSE_NNAP::flt_t *pos, int *type,
    JSE_NNAP::flt_t *eng, JSE_NNAP::flt_t *f, JSE_NNAP::flt_t *v0, JSE_NNAP::flt_t *v1,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t *aNormParam,
    JSE_NNAP::flt_t *rFpForwardCache) {
    
    const int ctype = type[bi];
    int code;
// >>> NNAPGEN SWITCH
    JSE_NNAP::flt_t rLayers[__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__];
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE>(bi, ctype,
        aNlSize, aNl, rLayers,
        pos, type,
        aFpHyperParam, aFpParam, rFpForwardCache
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
    code = JSE_NNAP::fpBackward<__NNAPGENS_ctype__, JSE_NNAP::FALSE, JSE_NNAP::FALSE, JSE_NNAP::FALSE>(bi, ctype,
        aNlSize, aNl, rLayers,
        pos, type,
        f, v0, v1,
        aFpHyperParam, aFpParam, NULL,
        rFpForwardCache, NULL, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}


__jsefunc__ int jse_nnap_forwardEnergy(int bi,
    int aNlSize, int *aNl,
    JSE_NNAP::flt_t *pos, int *type,
    JSE_NNAP::flt_t *eng,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t *aNormParam,
    JSE_NNAP::flt_t *rFpForwardCache, JSE_NNAP::flt_t *rNnForwardCache) {
    
    const int ctype = type[bi];
    int code;
// >>> NNAPGEN SWITCH
    JSE_NNAP::flt_t *rLayers = rNnForwardCache;
    JSE_NNAP::flt_t *rNnGradCache = rLayers + (__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__);
    
    code = JSE_NNAP::fpForward<__NNAPGENS_ctype__, JSE_NNAP::TRUE>(bi, ctype,
        aNlSize, aNl, rLayers,
        pos, type,
        aFpHyperParam, aFpParam, rFpForwardCache
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
__jsefunc__ int jse_nnap_backwardEnergy(int bi,
    int aNlSize, int *aNl,
    JSE_NNAP::flt_t *pos, int *type,
    JSE_NNAP::flt_t aGradEng,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t *aNormParam,
    JSE_NNAP::flt_t **rGradFpParam, JSE_NNAP::flt_t **rGradNnParam,
    JSE_NNAP::flt_t *aFpForwardCache, JSE_NNAP::flt_t *aNnForwardCache) {
    
    const int ctype = type[bi];
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
        aNlSize, aNl, rGradLayers,
        pos, type,
        NULL, NULL, NULL, aFpHyperParam,
        aFpParam, rGradFpParam, aFpForwardCache, NULL, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}

__jsefunc__ int jse_nnap_forwardEnergyForce(int bi,
    int aNlSize, int *aNl,
    JSE_NNAP::flt_t *pos, int *type,
    JSE_NNAP::flt_t *eng, JSE_NNAP::flt_t *f, JSE_NNAP::flt_t *v0, JSE_NNAP::flt_t *v1,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t *aNormParam,
    JSE_NNAP::flt_t *rFpForwardCache, JSE_NNAP::flt_t *rNnForwardCache, JSE_NNAP::flt_t *rFpBackwardCache, JSE_NNAP::flt_t *rNnBackwardCache) {
    
    const int ctype = type[bi];
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
        aNlSize, aNl, rLayers,
        pos, type,
        aFpHyperParam, aFpParam, rFpForwardCache
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
    code = JSE_NNAP::fpBackward<__NNAPGENS_ctype__, JSE_NNAP::FALSE, JSE_NNAP::FALSE, JSE_NNAP::TRUE>(bi, ctype,
        aNlSize, aNl, rAGradLayers,
        pos, type,
        f, v0, v1,
        aFpHyperParam, aFpParam, NULL,
        rFpForwardCache, rFpBackwardCache, NULL
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}
__jsefunc__ int jse_nnap_backwardEnergyForce(int bi,
    int aNlSize, int *aNl,
    JSE_NNAP::flt_t *pos, int *type,
    JSE_NNAP::flt_t aBGradEng, JSE_NNAP::flt_t *aBGradF, JSE_NNAP::flt_t *aBGradV0,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t *aNormParam,
    JSE_NNAP::flt_t **rBGradFpParam, JSE_NNAP::flt_t **rBGradNnParam,
    JSE_NNAP::flt_t *aFpForwardCache, JSE_NNAP::flt_t *aNnForwardCache, JSE_NNAP::flt_t *aFpBackwardCache, JSE_NNAP::flt_t *aNnBackwardCache,
    JSE_NNAP::flt_t *rFpBackwardBackwardCache) {
    
    const int ctype = type[bi];
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
        aNlSize, aNl, tAGradLayers, rBGradAGradLayers,
        pos, type,
        aBGradF, aBGradV0,
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
        aNlSize, aNl, rBGradLayers,
        pos, type,
        NULL, NULL, NULL, aFpHyperParam,
        aFpParam, rBGradFpParam, aFpForwardCache, NULL, rFpBackwardBackwardCache
    );
    if (code!=0) return code;
// <<< NNAPGEN SWITCH (ctype) [FP NN TYPE]
    return 0;
}


__jsefunc__ int jse_nnap_statNlSizeLammps(int nlocal, int *numneigh, int *numneighMax) {
    int numneighMax_ = 0;
    for (int i = 0; i < nlocal; ++i) {
        int jnum = numneigh[i];
        if (jnum > numneighMax_) numneighMax_ = jnum;
    }
    numneighMax[0] = numneighMax_;
    return 0;
}

#define JSE_LMP_NEIGHMASK 0x1FFFFFFF

__jsefunc__ int jse_nnap_computeLammps(
    int inum, int nlocal, int nghost, int ntypes,
    int eflag, int eflagAtom, int vflag, int vflagAtom, int cvflagAtom,
    double **x, double **f, int *type, int *ilist,
    int *numneigh, int **firstneigh, double *cutsq,
    int *aLmpType2NNAPType, int **rTypeIlist, int *rTypeInum,
    double *engVdwl, double *eatom, double *virial, double **vatom, double **cvatom,
    JSE_NNAP::flt_t *posBuf, int *typeBuf, int *nlBuf,
    JSE_NNAP::flt_t *fBuf, JSE_NNAP::flt_t *v1Buf,
    JSE_NNAP::flt_t **aFpHyperParam, JSE_NNAP::flt_t **aFpParam, JSE_NNAP::flt_t **aNnParam, JSE_NNAP::flt_t **aNormParam,
    JSE_NNAP::flt_t *rFpForwardCache) {
    
    const int nlocalghost = nlocal + nghost;
    /// lammps x type -> jse pos xyz type
    for (int i = 0; i < nlocalghost; ++i) {
        posBuf[3*i + 0] = (JSE_NNAP::flt_t)x[i][0];
        posBuf[3*i + 1] = (JSE_NNAP::flt_t)x[i][1];
        posBuf[3*i + 2] = (JSE_NNAP::flt_t)x[i][2];
        typeBuf[i] = aLmpType2NNAPType[type[i]];
    }
    /// clear force buf required for backward in force
    for (int i = 0; i < nlocalghost; ++i) {
        fBuf[3*i + 0] = JSE_NNAP::ZERO;
        fBuf[3*i + 1] = JSE_NNAP::ZERO;
        fBuf[3*i + 2] = JSE_NNAP::ZERO;
    }
    JSE_NNAP::flt_t v0[6] = {0};
    for (int i = 0; i < nlocalghost; ++i) {
        v1Buf[9*i + 0] = JSE_NNAP::ZERO; v1Buf[9*i + 1] = JSE_NNAP::ZERO; v1Buf[9*i + 2] = JSE_NNAP::ZERO;
        v1Buf[9*i + 3] = JSE_NNAP::ZERO; v1Buf[9*i + 4] = JSE_NNAP::ZERO; v1Buf[9*i + 5] = JSE_NNAP::ZERO;
        v1Buf[9*i + 6] = JSE_NNAP::ZERO; v1Buf[9*i + 7] = JSE_NNAP::ZERO; v1Buf[9*i + 8] = JSE_NNAP::ZERO;
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
            const int i = subIlist[ii];
            const JSE_NNAP::flt_t xi = posBuf[3*i + 0];
            const JSE_NNAP::flt_t yi = posBuf[3*i + 1];
            const JSE_NNAP::flt_t zi = posBuf[3*i + 2];
            const int *jlist = firstneigh[i];
            const int jnum = numneigh[i];
            
            /// build neighbor list
            int rNlSize = 0;
            for (int jj = 0; jj < jnum; ++jj) {
                int j = jlist[jj];
                j &= JSE_LMP_NEIGHMASK;
                // Note that dxyz in jse and lammps are defined oppositely
                const JSE_NNAP::flt_t dx = posBuf[3*j + 0] - xi;
                const JSE_NNAP::flt_t dy = posBuf[3*j + 1] - yi;
                const JSE_NNAP::flt_t dz = posBuf[3*j + 2] - zi;
                const JSE_NNAP::flt_t rsq = dx*dx + dy*dy + dz*dz;
                if (rsq < cutsq[typei]) {
                    nlBuf[rNlSize] = j;
                    ++rNlSize;
                }
            }
            
            /// begin nnap here
            JSE_NNAP::flt_t eng = JSE_NNAP::ZERO;
            int code;
// >>> NNAPGEN SWITCH
            JSE_NNAP::flt_t rLayers[__NNAPGENX_NN_SIZE_IN__+__NNAPGENX_NN_SIZE_HB__];
            code = JSE_NNAP::fpForward<__NNAPGENS_typeiNNAP__, JSE_NNAP::TRUE>(i, typeiNNAP,
                rNlSize, nlBuf, rLayers,
                posBuf, typeBuf,
                aFpHyperParam, aFpParam, rFpForwardCache
            );
            if (code!=0) return code;
            JSE_NNAP::flt_t rNnGradCache[__NNAPGENX_NN_SIZE_HB__];
            code = JSE_NNAP::normedNnForward<__NNAPGENS_typeiNNAP__, JSE_NNAP::TRUE, JSE_NNAP::FALSE>(typeiNNAP,
                &eng, rLayers,
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
                rNlSize, nlBuf, rLayers,
                posBuf, typeBuf,
                fBuf, v0, v1Buf,
                aFpHyperParam, aFpParam, NULL, rFpForwardCache, NULL, NULL
            );
            if (code!=0) return code;
// <<< NNAPGEN SWITCH (typeiNNAP) [FP NN TYPE]
            
            /// collect results
            if (eflag) {
                *engVdwl += eng;
                if (eflagAtom) eatom[i] += eng;
            }
        }
    }
    /// collect results
    for (int i = 0; i < nlocalghost; ++i) {
        f[i][0] += (double)fBuf[3*i + 0];
        f[i][1] += (double)fBuf[3*i + 1];
        f[i][2] += (double)fBuf[3*i + 2];
    }
    if (vflag) {
        virial[0] += v0[0]; virial[1] += v0[1]; virial[2] += v0[2];
        virial[3] += v0[3]; virial[4] += v0[4]; virial[5] += v0[5];
    }
    if (cvflagAtom) {
        for (int i = 0; i < nlocalghost; ++i) {
            cvatom[i][0] += v1Buf[9*i + 0]; cvatom[i][1] += v1Buf[9*i + 1]; cvatom[i][2] += v1Buf[9*i + 2];
            cvatom[i][3] += v1Buf[9*i + 3]; cvatom[i][4] += v1Buf[9*i + 4]; cvatom[i][5] += v1Buf[9*i + 5];
            cvatom[i][6] += v1Buf[9*i + 6]; cvatom[i][7] += v1Buf[9*i + 7]; cvatom[i][8] += v1Buf[9*i + 8];
        }
    }
    if (vflagAtom) {
        for (int i = 0; i < nlocalghost; ++i) {
            vatom[i][0] += v1Buf[9*i + 0]; vatom[i][1] += v1Buf[9*i + 1]; vatom[i][2] += v1Buf[9*i + 2];
            vatom[i][3] += v1Buf[9*i + 3]; vatom[i][4] += v1Buf[9*i + 4]; vatom[i][5] += v1Buf[9*i + 5];
        }
    }
    return 0;
}

}

