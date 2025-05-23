#include "jsex_nnap_basis_Chebyshev.h"
#include "basis_util.h"

#ifdef __cplusplus
extern "C" {
#endif

static inline void calFp(double *aNlDx, double *aNlDy, double *aNlDz, jint *aNlType, jint aNN,
                         double *rNlRn, double *rFp, jboolean aBufferNl,
                         jint aTypeNum, double aRCut, jint aNMax, jint aWType) {
    // loop for neighbor
    for (jint j = 0; j < aNN; ++j) {
        jint type = aNlType[j];
        double dx = aNlDx[j], dy = aNlDy[j], dz = aNlDz[j];
        double dis = sqrt(dx*dx + dy*dy + dz*dz);
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


JNIEXPORT void JNICALL Java_jsex_nnap_basis_Chebyshev_eval1(JNIEnv *aEnv, jclass aClazz,
        jdoubleArray aNlDx, jdoubleArray aNlDy, jdoubleArray aNlDz, jintArray aNlType, jint aNN,
        jdoubleArray rRn, jdoubleArray rFp,
        jint aTypeNum, jdouble aRCut, jint aNMax, jint aWType) {
        // java array init
#ifdef __cplusplus
    double *tNlDx = (double *)aEnv->GetPrimitiveArrayCritical(aNlDx, NULL);
    double *tNlDy = (double *)aEnv->GetPrimitiveArrayCritical(aNlDy, NULL);
    double *tNlDz = (double *)aEnv->GetPrimitiveArrayCritical(aNlDz, NULL);
    jint *tNlType = (jint *)aEnv->GetPrimitiveArrayCritical(aNlType, NULL);
    double *tRn = (double *)aEnv->GetPrimitiveArrayCritical(rRn, NULL);
    double *tFp = (double *)aEnv->GetPrimitiveArrayCritical(rFp, NULL);
#else
    double *tNlDx = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, aNlDx, NULL);
    double *tNlDy = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, aNlDy, NULL);
    double *tNlDz = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, aNlDz, NULL);
    jint *tNlType = (jint *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, aNlType, NULL);
    double *tRn = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rRn, NULL);
    double *tFp = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rFp, NULL);
#endif
    
    // do cal
    calFp(tNlDx, tNlDy, tNlDz, tNlType, aNN,
          tRn, tFp, JNI_FALSE,
          aTypeNum, aRCut, aNMax, aWType);
    
    // release java array
#ifdef __cplusplus
    aEnv->ReleasePrimitiveArrayCritical(aNlDx, tNlDx, JNI_ABORT);
    aEnv->ReleasePrimitiveArrayCritical(aNlDy, tNlDy, JNI_ABORT);
    aEnv->ReleasePrimitiveArrayCritical(aNlDz, tNlDz, JNI_ABORT);
    aEnv->ReleasePrimitiveArrayCritical(aNlType, tNlType, JNI_ABORT);
    aEnv->ReleasePrimitiveArrayCritical(rRn, tRn, JNI_ABORT); // buffer only
    aEnv->ReleasePrimitiveArrayCritical(rFp, tFp, 0);
#else
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, aNlDx, tNlDx, JNI_ABORT);
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, aNlDy, tNlDy, JNI_ABORT);
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, aNlDz, tNlDz, JNI_ABORT);
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, aNlType, tNlType, JNI_ABORT);
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rRn, rRn, JNI_ABORT); // buffer only
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rFp, tFp, 0);
#endif
}

JNIEXPORT void JNICALL Java_jsex_nnap_basis_Chebyshev_evalPartial1(JNIEnv *aEnv, jclass aClazz,
        jdoubleArray aNlDx, jdoubleArray aNlDy, jdoubleArray aNlDz, jintArray aNlType, jint aNN,
        jdoubleArray rNlRn, jdoubleArray rRnPx, jdoubleArray rRnPy, jdoubleArray rRnPz, jdoubleArray rCheby2,
        jdoubleArray rFp, jdoubleArray rFpPx, jdoubleArray rFpPy, jdoubleArray rFpPz,
        jdoubleArray rFpPxCross, jdoubleArray rFpPyCross, jdoubleArray rFpPzCross,
        jint aTypeNum, jdouble aRCut, jint aNMax, jint aWType) {
    // java array init
#ifdef __cplusplus
    double *tNlDx = (double *)aEnv->GetPrimitiveArrayCritical(aNlDx, NULL);
    double *tNlDy = (double *)aEnv->GetPrimitiveArrayCritical(aNlDy, NULL);
    double *tNlDz = (double *)aEnv->GetPrimitiveArrayCritical(aNlDz, NULL);
    jint *tNlType = (jint *)aEnv->GetPrimitiveArrayCritical(aNlType, NULL);
    double *tNlRn = (double *)aEnv->GetPrimitiveArrayCritical(rNlRn, NULL);
    double *tRnPx = (double *)aEnv->GetPrimitiveArrayCritical(rRnPx, NULL);
    double *tRnPy = (double *)aEnv->GetPrimitiveArrayCritical(rRnPy, NULL);
    double *tRnPz = (double *)aEnv->GetPrimitiveArrayCritical(rRnPz, NULL);
    double *tCheby2 = (double *)aEnv->GetPrimitiveArrayCritical(rCheby2, NULL);
    double *tFp = (double *)aEnv->GetPrimitiveArrayCritical(rFp, NULL);
    double *tFpPx = (double *)aEnv->GetPrimitiveArrayCritical(rFpPx, NULL);
    double *tFpPy = (double *)aEnv->GetPrimitiveArrayCritical(rFpPy, NULL);
    double *tFpPz = (double *)aEnv->GetPrimitiveArrayCritical(rFpPz, NULL);
    double *tFpPxCross = rFpPxCross==NULL ? NULL : (double *)aEnv->GetPrimitiveArrayCritical(rFpPxCross, NULL);
    double *tFpPyCross = rFpPyCross==NULL ? NULL : (double *)aEnv->GetPrimitiveArrayCritical(rFpPyCross, NULL);
    double *tFpPzCross = rFpPzCross==NULL ? NULL : (double *)aEnv->GetPrimitiveArrayCritical(rFpPzCross, NULL);
#else
    double *tNlDx = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, aNlDx, NULL);
    double *tNlDy = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, aNlDy, NULL);
    double *tNlDz = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, aNlDz, NULL);
    jint *tNlType = (jint *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, aNlType, NULL);
    double *tNlRn = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rNlRn, NULL);
    double *tRnPx = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rRnPx, NULL);
    double *tRnPy = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rRnPy, NULL);
    double *tRnPz = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rRnPz, NULL);
    double *tCheby2 = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rCheby2, NULL);
    double *tFp = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rFp, NULL);
    double *tFpPx = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rFpPx, NULL);
    double *tFpPy = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rFpPy, NULL);
    double *tFpPz = (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rFpPz, NULL);
    double *tFpPxCross = rFpPxCross==NULL ? NULL : (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rFpPxCross, NULL);
    double *tFpPyCross = rFpPyCross==NULL ? NULL : (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rFpPyCross, NULL);
    double *tFpPzCross = rFpPzCross==NULL ? NULL : (double *)(*aEnv)->GetPrimitiveArrayCritical(aEnv, rFpPzCross, NULL);
#endif

    // const init
    jint tSizeN;
    switch(aWType) {
    case jsex_nnap_basis_Chebyshev_WTYPE_EXFULL: {
        tSizeN = aTypeNum>1 ? (aTypeNum+1)*(aNMax+1) : (aNMax+1);
        break;
    }
    case jsex_nnap_basis_Chebyshev_WTYPE_FULL: {
        tSizeN = aTypeNum*(aNMax+1);
        break;
    }
    case jsex_nnap_basis_Chebyshev_WTYPE_NONE:
    case jsex_nnap_basis_Chebyshev_WTYPE_SINGLE: {
        tSizeN = aNMax+1;
        break;
    }
    case jsex_nnap_basis_Chebyshev_WTYPE_DEFAULT: {
        tSizeN = aTypeNum>1 ? (aNMax+aNMax+2) : (aNMax+1);
        break;
    }
    default: {
        tSizeN = 0;
        break;
    }}
    // cal fp first
    calFp(tNlDx, tNlDy, tNlDz, tNlType, aNN,
          tNlRn, tFp, JNI_TRUE,
          aTypeNum, aRCut, aNMax, aWType);
    
    // loop for neighbor
    for (jint j = 0; j < aNN; ++j) {
        jint type = tNlType[j];
        double dx = tNlDx[j], dy = tNlDy[j], dz = tNlDz[j];
        double dis = sqrt(dx*dx + dy*dy + dz*dz);
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
            jint tShiftFPC = j*tSizeN;
            double *tFpPxCrossj = tFpPxCross==NULL ? NULL : (tFpPxCross+tShiftFPC);
            double *tFpPyCrossj = tFpPyCross==NULL ? NULL : (tFpPyCross+tShiftFPC);
            double *tFpPzCrossj = tFpPzCross==NULL ? NULL : (tFpPzCross+tShiftFPC);
            if (aTypeNum == 1) {
                for (jint tN=0; tN <= aNMax; ++tN) {
                    // cal subFpPxyz first
                    const double tRnn = tRn[tN];
                    const double subFpPx = -(fc*tRnPx[tN] + fcPx*tRnn);
                    const double subFpPy = -(fc*tRnPy[tN] + fcPy*tRnn);
                    const double subFpPz = -(fc*tRnPz[tN] + fcPz*tRnn);
                    // accumulate to fp
                    putFpPxyz(tFpPx, tFpPy, tFpPz,
                              tFpPxCrossj, tFpPyCrossj, tFpPzCrossj,
                              subFpPx, subFpPy, subFpPz, tN);
                }
            } else {
                jint tShiftFP = (aNMax+1)*type;
                double *tFpPxWt = tFpPx+tShiftFP;
                double *tFpPyWt = tFpPy+tShiftFP;
                double *tFpPzWt = tFpPz+tShiftFP;
                double *tFpPxCrossjWt = tFpPxCrossj==NULL ? NULL : (tFpPxCrossj+tShiftFP);
                double *tFpPyCrossjWt = tFpPyCrossj==NULL ? NULL : (tFpPyCrossj+tShiftFP);
                double *tFpPzCrossjWt = tFpPzCrossj==NULL ? NULL : (tFpPzCrossj+tShiftFP);
                for (jint tN=0; tN <= aNMax; ++tN) {
                    // cal subFpPxyz first
                    const double tRnn = tRn[tN];
                    const double subFpPx = -(fc*tRnPx[tN] + fcPx*tRnn);
                    const double subFpPy = -(fc*tRnPy[tN] + fcPy*tRnn);
                    const double subFpPz = -(fc*tRnPz[tN] + fcPz*tRnn);
                    // accumulate to fp
                    putFpPxyz(tFpPx, tFpPy, tFpPz,
                              tFpPxCrossj, tFpPyCrossj, tFpPzCrossj,
                              subFpPx, subFpPy, subFpPz, tN);
                    putFpPxyz(tFpPxWt, tFpPyWt, tFpPzWt,
                              tFpPxCrossjWt, tFpPyCrossjWt, tFpPzCrossjWt,
                              subFpPx, subFpPy, subFpPz, tN);
                }
            }
            break;
        }
        case jsex_nnap_basis_Chebyshev_WTYPE_FULL: {
            jint tShiftFP = (aNMax+1)*(type-1);
            double *tFpPxWt = tFpPx+tShiftFP;
            double *tFpPyWt = tFpPy+tShiftFP;
            double *tFpPzWt = tFpPz+tShiftFP;
            jint tShiftFPC = tShiftFP + j*tSizeN;
            double *tFpPxCrossjWt = tFpPxCross==NULL ? NULL : (tFpPxCross+tShiftFPC);
            double *tFpPyCrossjWt = tFpPyCross==NULL ? NULL : (tFpPyCross+tShiftFPC);
            double *tFpPzCrossjWt = tFpPzCross==NULL ? NULL : (tFpPzCross+tShiftFPC);
            for (jint tN=0; tN <= aNMax; ++tN) {
                // cal subFpPxyz first
                const double tRnn = tRn[tN];
                const double subFpPx = -(fc*tRnPx[tN] + fcPx*tRnn);
                const double subFpPy = -(fc*tRnPy[tN] + fcPy*tRnn);
                const double subFpPz = -(fc*tRnPz[tN] + fcPz*tRnn);
                // accumulate to fp
                putFpPxyz(tFpPxWt, tFpPyWt, tFpPzWt,
                          tFpPxCrossjWt, tFpPyCrossjWt, tFpPzCrossjWt,
                          subFpPx, subFpPy, subFpPz, tN);
            }
            break;
        }
        case jsex_nnap_basis_Chebyshev_WTYPE_NONE:
        case jsex_nnap_basis_Chebyshev_WTYPE_SINGLE: {
            jint tShiftFPC = j*tSizeN;
            double *tFpPxCrossj = tFpPxCross==NULL ? NULL : (tFpPxCross+tShiftFPC);
            double *tFpPyCrossj = tFpPyCross==NULL ? NULL : (tFpPyCross+tShiftFPC);
            double *tFpPzCrossj = tFpPzCross==NULL ? NULL : (tFpPzCross+tShiftFPC);
            for (jint tN=0; tN <= aNMax; ++tN) {
                // cal subFpPxyz first
                const double tRnn = tRn[tN];
                const double subFpPx = -(fc*tRnPx[tN] + fcPx*tRnn);
                const double subFpPy = -(fc*tRnPy[tN] + fcPy*tRnn);
                const double subFpPz = -(fc*tRnPz[tN] + fcPz*tRnn);
                // accumulate to fp
                putFpPxyz(tFpPx, tFpPy, tFpPz,
                          tFpPxCrossj, tFpPyCrossj, tFpPzCrossj,
                          subFpPx, subFpPy, subFpPz, tN);
            }
            break;
        }
        case jsex_nnap_basis_Chebyshev_WTYPE_DEFAULT: {
            jint tShiftFPC = j*tSizeN;
            double *tFpPxCrossj = tFpPxCross==NULL ? NULL : (tFpPxCross+tShiftFPC);
            double *tFpPyCrossj = tFpPyCross==NULL ? NULL : (tFpPyCross+tShiftFPC);
            double *tFpPzCrossj = tFpPzCross==NULL ? NULL : (tFpPzCross+tShiftFPC);
            if (aTypeNum == 1) {
                for (jint tN=0; tN <= aNMax; ++tN) {
                    // cal subFpPxyz first
                    const double tRnn = tRn[tN];
                    const double subFpPx = -(fc*tRnPx[tN] + fcPx*tRnn);
                    const double subFpPy = -(fc*tRnPy[tN] + fcPy*tRnn);
                    const double subFpPz = -(fc*tRnPz[tN] + fcPz*tRnn);
                    // accumulate to fp
                    putFpPxyz(tFpPx, tFpPy, tFpPz,
                              tFpPxCrossj, tFpPyCrossj, tFpPzCrossj,
                              subFpPx, subFpPy, subFpPz, tN);
                }
            } else {
                // cal weight of type here
                double wt = ((type&1)==1) ? type : -type;
                jint tShiftFP = aNMax+1;
                double *tFpPxWt = tFpPx+tShiftFP;
                double *tFpPyWt = tFpPy+tShiftFP;
                double *tFpPzWt = tFpPz+tShiftFP;
                double *tFpPxCrossjWt = tFpPxCrossj==NULL ? NULL : (tFpPxCrossj+tShiftFP);
                double *tFpPyCrossjWt = tFpPyCrossj==NULL ? NULL : (tFpPyCrossj+tShiftFP);
                double *tFpPzCrossjWt = tFpPzCrossj==NULL ? NULL : (tFpPzCrossj+tShiftFP);
                for (jint tN=0; tN <= aNMax; ++tN) {
                    // cal subFpPxyz first
                    const double tRnn = tRn[tN];
                    const double subFpPx = -(fc*tRnPx[tN] + fcPx*tRnn);
                    const double subFpPy = -(fc*tRnPy[tN] + fcPy*tRnn);
                    const double subFpPz = -(fc*tRnPz[tN] + fcPz*tRnn);
                    // accumulate to fp
                    putFpPxyz(tFpPx, tFpPy, tFpPz,
                              tFpPxCrossj, tFpPyCrossj, tFpPzCrossj,
                              subFpPx, subFpPy, subFpPz, tN);
                    putFpPxyz(tFpPxWt, tFpPyWt, tFpPzWt,
                              tFpPxCrossjWt, tFpPyCrossjWt, tFpPzCrossjWt,
                              subFpPx*wt, subFpPy*wt, subFpPz*wt, tN);
                }
            }
            break;
        }
        default: {
            break;
        }}
    }
    // release java array
#ifdef __cplusplus
    aEnv->ReleasePrimitiveArrayCritical(aNlDx, tNlDx, JNI_ABORT);
    aEnv->ReleasePrimitiveArrayCritical(aNlDy, tNlDy, JNI_ABORT);
    aEnv->ReleasePrimitiveArrayCritical(aNlDz, tNlDz, JNI_ABORT);
    aEnv->ReleasePrimitiveArrayCritical(aNlType, tNlType, JNI_ABORT);
    aEnv->ReleasePrimitiveArrayCritical(rNlRn, tNlRn, JNI_ABORT); // buffer only
    aEnv->ReleasePrimitiveArrayCritical(rRnPx, tRnPx, JNI_ABORT); // buffer only
    aEnv->ReleasePrimitiveArrayCritical(rRnPy, tRnPy, JNI_ABORT); // buffer only
    aEnv->ReleasePrimitiveArrayCritical(rRnPz, tRnPz, JNI_ABORT); // buffer only
    aEnv->ReleasePrimitiveArrayCritical(rCheby2, tCheby2, JNI_ABORT); // buffer only
    aEnv->ReleasePrimitiveArrayCritical(rFp, tFp, 0);
    aEnv->ReleasePrimitiveArrayCritical(rFpPx, tFpPx, 0);
    aEnv->ReleasePrimitiveArrayCritical(rFpPy, tFpPy, 0);
    aEnv->ReleasePrimitiveArrayCritical(rFpPz, tFpPz, 0);
    if (rFpPxCross != NULL) aEnv->ReleasePrimitiveArrayCritical(rFpPxCross, tFpPxCross, 0);
    if (rFpPyCross != NULL) aEnv->ReleasePrimitiveArrayCritical(rFpPyCross, tFpPyCross, 0);
    if (rFpPzCross != NULL) aEnv->ReleasePrimitiveArrayCritical(rFpPzCross, tFpPzCross, 0);
#else
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, aNlDx, tNlDx, JNI_ABORT);
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, aNlDy, tNlDy, JNI_ABORT);
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, aNlDz, tNlDz, JNI_ABORT);
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, aNlType, tNlType, JNI_ABORT);
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rNlRn, tNlRn, JNI_ABORT); // buffer only
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rRnPx, tRnPx, JNI_ABORT); // buffer only
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rRnPy, tRnPy, JNI_ABORT); // buffer only
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rRnPz, tRnPz, JNI_ABORT); // buffer only
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rCheby2, tCheby2, JNI_ABORT); // buffer only
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rFp, tFp, 0);
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rFpPx, tFpPx, 0);
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rFpPy, tFpPy, 0);
    (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rFpPz, tFpPz, 0);
    if (rFpPxCross != NULL) (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rFpPxCross, tFpPxCross, 0);
    if (rFpPyCross != NULL) (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rFpPyCross, tFpPyCross, 0);
    if (rFpPzCross != NULL) (*aEnv)->ReleasePrimitiveArrayCritical(aEnv, rFpPzCross, tFpPzCross, 0);
#endif
}

#ifdef __cplusplus
}
#endif
