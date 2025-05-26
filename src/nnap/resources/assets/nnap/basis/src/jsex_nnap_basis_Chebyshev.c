#include "jsex_nnap_basis_Chebyshev.h"
#include "basis_util.h"

#ifdef __cplusplus
extern "C" {
#endif

static inline void calFp(double *aNlDx, double *aNlDy, double *aNlDz, int *aNlType, jint aNN,
                         double *rNlRn, double *rFp, jboolean aBufferNl,
                         jint aTypeNum, double aRCut, jint aNMax, jint aWType) {
    // loop for neighbor
    for (jint j = 0; j < aNN; ++j) {
        jint type = (jint)aNlType[j];
        double dx = aNlDx[j], dy = aNlDy[j], dz = aNlDz[j];
        double dis = sqrt(dx*dx + dy*dy + dz*dz);
        // check rcut for merge
        if (dis >= aRCut) continue;
        // cal fc
        double fc = pow4_jse(1.0 - pow2_jse(dis/aRCut));
        // cal Rn
        double tRnX = 1.0 - 2.0*dis/aRCut;
        double *tRn = aBufferNl ? (rNlRn + j*(aNMax+1)) : rNlRn;
        chebyshevFull(aNMax, tRnX, tRn);
        switch(aWType) {
        case jsex_nnap_basis_Chebyshev_WTYPE_SINGLE: {
            // cal weight of type here
            double wt = ((type&1)==1) ? type : -type;
            for (jint n = 0; n <= aNMax; ++n) {
                tRn[n] *= wt;
            }
            break;
        }
        default: {
            break;
        }}
        // cal fp
        switch(aWType) {
        case jsex_nnap_basis_Chebyshev_WTYPE_EXFULL: {
            if (aTypeNum == 1) {
                for (jint tN = 0; tN <= aNMax; ++tN) {
                    rFp[tN] += fc*tRn[tN];
                }
            } else {
                double *tFpWt = rFp + (aNMax+1)*type;
                for (jint tN = 0; tN <= aNMax; ++tN) {
                    double tFpn = fc*tRn[tN];
                    rFp[tN] += tFpn;
                    tFpWt[tN] += tFpn;
                }
            }
            break;
        }
        case jsex_nnap_basis_Chebyshev_WTYPE_FULL: {
            double *tFp = rFp + (aNMax+1)*(type-1);
            for (jint tN = 0; tN <= aNMax; ++tN) {
                tFp[tN] += fc*tRn[tN];
            }
            break;
        }
        case jsex_nnap_basis_Chebyshev_WTYPE_NONE:
        case jsex_nnap_basis_Chebyshev_WTYPE_SINGLE: {
            for (jint tN = 0; tN <= aNMax; ++tN) {
                rFp[tN] += fc*tRn[tN];
            }
            break;
        }
        case jsex_nnap_basis_Chebyshev_WTYPE_DEFAULT: {
            if (aTypeNum == 1) {
                for (jint tN = 0; tN <= aNMax; ++tN) {
                    rFp[tN] += fc*tRn[tN];
                }
            } else {
                // cal weight of type here
                double wt = ((type&1)==1) ? type : -type;
                double *tFpWt = rFp + (aNMax+1);
                for (jint tN = 0; tN <= aNMax; ++tN) {
                    double tFpn = fc*tRn[tN];
                    rFp[tN] += tFpn;
                    tFpWt[tN] += wt*tFpn;
                }
            }
            break;
        }
        default: {
            break;
        }}
    }
}


JNIEXPORT void JNICALL Java_jsex_nnap_basis_Chebyshev_eval0(JNIEnv *aEnv, jclass aClazz,
        jlong aNlDx, jlong aNlDy, jlong aNlDz, jlong aNlType, jint aNN, jlong rRn, jlong rFp,
        jint aTypeNum, jdouble aRCut, jint aNMax, jint aWType) {
    // array init
    double *tNlDx = (double *)(intptr_t)aNlDx;
    double *tNlDy = (double *)(intptr_t)aNlDy;
    double *tNlDz = (double *)(intptr_t)aNlDz;
    int *tNlType = (int *)(intptr_t)aNlType;
    double *tRn = (double *)(intptr_t)rRn;
    double *tFp = (double *)(intptr_t)rFp;

    // const init
    jint tSize;
    switch(aWType) {
    case jsex_nnap_basis_Chebyshev_WTYPE_EXFULL: {
        tSize = aTypeNum>1 ? (aTypeNum+1)*(aNMax+1) : (aNMax+1);
        break;
    }
    case jsex_nnap_basis_Chebyshev_WTYPE_FULL: {
        tSize = aTypeNum*(aNMax+1);
        break;
    }
    case jsex_nnap_basis_Chebyshev_WTYPE_NONE:
    case jsex_nnap_basis_Chebyshev_WTYPE_SINGLE: {
        tSize = aNMax+1;
        break;
    }
    case jsex_nnap_basis_Chebyshev_WTYPE_DEFAULT: {
        tSize = aTypeNum>1 ? (aNMax+aNMax+2) : (aNMax+1);
        break;
    }
    default: {
        tSize = 0;
        break;
    }}
    // clear fp first
    for (jint i = 0; i < tSize; ++i) {
        tFp[i] = 0.0;
    }
    // do cal
    calFp(tNlDx, tNlDy, tNlDz, tNlType, aNN,
          tRn, tFp, JNI_FALSE,
          aTypeNum, aRCut, aNMax, aWType);
}

JNIEXPORT void JNICALL Java_jsex_nnap_basis_Chebyshev_evalPartial0(JNIEnv *aEnv, jclass aClazz,
        jlong aNlDx, jlong aNlDy, jlong aNlDz, jlong aNlType, jint aNN,
        jlong rNlRn, jlong rRnPx, jlong rRnPy, jlong rRnPz, jlong rCheby2,
        jlong rFp, jint aSizeFp, jint aShiftFp, jlong rFpPx, jlong rFpPy, jlong rFpPz,
        jint aTypeNum, jdouble aRCut, jint aNMax, jint aWType) {
    // array init
    double *tNlDx = (double *)(intptr_t)aNlDx;
    double *tNlDy = (double *)(intptr_t)aNlDy;
    double *tNlDz = (double *)(intptr_t)aNlDz;
    int *tNlType = (int *)(intptr_t)aNlType;
    double *tNlRn = (double *)(intptr_t)rNlRn;
    double *tRnPx = (double *)(intptr_t)rRnPx;
    double *tRnPy = (double *)(intptr_t)rRnPy;
    double *tRnPz = (double *)(intptr_t)rRnPz;
    double *tCheby2 = (double *)(intptr_t)rCheby2;
    double *tFp = (double *)(intptr_t)rFp;
    double *tFpPx = (double *)(intptr_t)rFpPx;
    double *tFpPy = (double *)(intptr_t)rFpPy;
    double *tFpPz = (double *)(intptr_t)rFpPz;

    // const init
    jint tSize;
    switch(aWType) {
    case jsex_nnap_basis_Chebyshev_WTYPE_EXFULL: {
        tSize = aTypeNum>1 ? (aTypeNum+1)*(aNMax+1) : (aNMax+1);
        break;
    }
    case jsex_nnap_basis_Chebyshev_WTYPE_FULL: {
        tSize = aTypeNum*(aNMax+1);
        break;
    }
    case jsex_nnap_basis_Chebyshev_WTYPE_NONE:
    case jsex_nnap_basis_Chebyshev_WTYPE_SINGLE: {
        tSize = aNMax+1;
        break;
    }
    case jsex_nnap_basis_Chebyshev_WTYPE_DEFAULT: {
        tSize = aTypeNum>1 ? (aNMax+aNMax+2) : (aNMax+1);
        break;
    }
    default: {
        tSize = 0;
        break;
    }}
    // clear fp first
    for (jint i = 0; i < tSize; ++i) {
        tFp[i] = 0.0;
    }
    // cal fp
    calFp(tNlDx, tNlDy, tNlDz, tNlType, aNN,
          tNlRn, tFp, JNI_TRUE,
          aTypeNum, aRCut, aNMax, aWType);
    
    // loop for neighbor
    for (jint j = 0; j < aNN; ++j) {
        // clear fpPxyz here
        jint tShiftFpP = j*(aSizeFp+aShiftFp) + aShiftFp;
        double *tFpPx_ = tFpPx+tShiftFpP;
        double *tFpPy_ = tFpPy+tShiftFpP;
        double *tFpPz_ = tFpPz+tShiftFpP;
        for (jint i = 0; i < tSize; ++i) {
            tFpPx_[i] = 0.0;
            tFpPy_[i] = 0.0;
            tFpPz_[i] = 0.0;
        }
        // init nl
        jint type = (jint)tNlType[j];
        double dx = tNlDx[j], dy = tNlDy[j], dz = tNlDz[j];
        double dis = sqrt(dx*dx + dy*dy + dz*dz);
        // check rcut for merge
        if (dis >= aRCut) continue;
        // cal fc
        double fcMul = 1.0 - pow2_jse(dis/aRCut);
        double fcMul3 = pow3_jse(fcMul);
        double fc = fcMul3 * fcMul;
        double fcPMul = 8.0 * fcMul3 / (aRCut*aRCut);
        double fcPx = dx * fcPMul;
        double fcPy = dy * fcPMul;
        double fcPz = dz * fcPMul;
        // cal Rn
        double *tRn = tNlRn + j*(aNMax+1);
        const double tRnX = 1.0 - 2.0*dis/aRCut;
        chebyshev2Full(aNMax-1, tRnX, tCheby2);
        switch(aWType) {
        case jsex_nnap_basis_Chebyshev_WTYPE_SINGLE: {
            // cal weight of type here
            double wt = ((type&1)==1) ? type : -type;
            calRnPxyz(tRnPx, tRnPy, tRnPz, tCheby2, aNMax, dis, aRCut, wt, dx, dy, dz);
            break;
        }
        case jsex_nnap_basis_Chebyshev_WTYPE_EXFULL:
        case jsex_nnap_basis_Chebyshev_WTYPE_FULL:
        case jsex_nnap_basis_Chebyshev_WTYPE_NONE:
        case jsex_nnap_basis_Chebyshev_WTYPE_DEFAULT: {
            calRnPxyz(tRnPx, tRnPy, tRnPz, tCheby2, aNMax, dis, aRCut, 1.0, dx, dy, dz);
            break;
        }
        default: {
            break;
        }}
        // cal fpPxyz
        switch(aWType) {
        case jsex_nnap_basis_Chebyshev_WTYPE_EXFULL: {
            if (aTypeNum == 1) {
                for (jint n = 0; n <= aNMax; ++n) {
                    // cal subFpPxyz and accumulate to fp
                    const double tRnn = tRn[n];
                    tFpPx_[n] -= (fc*tRnPx[n] + fcPx*tRnn);
                    tFpPy_[n] -= (fc*tRnPy[n] + fcPy*tRnn);
                    tFpPz_[n] -= (fc*tRnPz[n] + fcPz*tRnn);
                }
            } else {
                jint tShiftFp = (aNMax+1)*type;
                double *tFpPxWt = tFpPx_+tShiftFp;
                double *tFpPyWt = tFpPy_+tShiftFp;
                double *tFpPzWt = tFpPz_+tShiftFp;
                for (jint n = 0; n <= aNMax; ++n) {
                    // cal subFpPxyz first
                    const double tRnn = tRn[n];
                    const double subFpPx = -(fc*tRnPx[n] + fcPx*tRnn);
                    const double subFpPy = -(fc*tRnPy[n] + fcPy*tRnn);
                    const double subFpPz = -(fc*tRnPz[n] + fcPz*tRnn);
                    // accumulate to fp
                    tFpPx_[n] += subFpPx; tFpPxWt[n] += subFpPx;
                    tFpPy_[n] += subFpPy; tFpPyWt[n] += subFpPy;
                    tFpPz_[n] += subFpPz; tFpPzWt[n] += subFpPz;
                }
            }
            break;
        }
        case jsex_nnap_basis_Chebyshev_WTYPE_FULL: {
            jint tShiftFp = (aNMax+1)*(type-1);
            double *tFpPxWt = tFpPx_+tShiftFp;
            double *tFpPyWt = tFpPy_+tShiftFp;
            double *tFpPzWt = tFpPz_+tShiftFp;
            for (jint n = 0; n <= aNMax; ++n) {
                // cal subFpPxyz and accumulate to fp
                const double tRnn = tRn[n];
                tFpPxWt[n] -= (fc*tRnPx[n] + fcPx*tRnn);
                tFpPyWt[n] -= (fc*tRnPy[n] + fcPy*tRnn);
                tFpPzWt[n] -= (fc*tRnPz[n] + fcPz*tRnn);
            }
            break;
        }
        case jsex_nnap_basis_Chebyshev_WTYPE_NONE:
        case jsex_nnap_basis_Chebyshev_WTYPE_SINGLE: {
            for (jint n = 0; n <= aNMax; ++n) {
                // cal subFpPxyz and accumulate to fp
                const double tRnn = tRn[n];
                tFpPx_[n] -= (fc*tRnPx[n] + fcPx*tRnn);
                tFpPy_[n] -= (fc*tRnPy[n] + fcPy*tRnn);
                tFpPz_[n] -= (fc*tRnPz[n] + fcPz*tRnn);
            }
            break;
        }
        case jsex_nnap_basis_Chebyshev_WTYPE_DEFAULT: {
            if (aTypeNum == 1) {
                for (jint n = 0; n <= aNMax; ++n) {
                    // cal subFpPxyz and accumulate to fp
                    const double tRnn = tRn[n];
                    tFpPx_[n] -= (fc*tRnPx[n] + fcPx*tRnn);
                    tFpPy_[n] -= (fc*tRnPy[n] + fcPy*tRnn);
                    tFpPz_[n] -= (fc*tRnPz[n] + fcPz*tRnn);
                }
            } else {
                // cal weight of type here
                double wt = ((type&1)==1) ? type : -type;
                jint tShiftFp = aNMax+1;
                double *tFpPxWt = tFpPx_+tShiftFp;
                double *tFpPyWt = tFpPy_+tShiftFp;
                double *tFpPzWt = tFpPz_+tShiftFp;
                for (jint n = 0; n <= aNMax; ++n) {
                    // cal subFpPxyz first
                    const double tRnn = tRn[n];
                    const double subFpPx = -(fc*tRnPx[n] + fcPx*tRnn);
                    const double subFpPy = -(fc*tRnPy[n] + fcPy*tRnn);
                    const double subFpPz = -(fc*tRnPz[n] + fcPz*tRnn);
                    // accumulate to fp
                    tFpPx_[n] += subFpPx; tFpPxWt[n] += wt*subFpPx;
                    tFpPy_[n] += subFpPy; tFpPyWt[n] += wt*subFpPy;
                    tFpPz_[n] += subFpPz; tFpPzWt[n] += wt*subFpPz;
                }
            }
            break;
        }
        default: {
            break;
        }}
    }
}

#ifdef __cplusplus
}
#endif
