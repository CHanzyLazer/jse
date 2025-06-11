#include "jsex_nnap_nn_FeedForward.h"
#include "nnap_util.h"

#ifdef __cplusplus
extern "C" {
#endif

static inline double silu(double aX) {
    return aX / (1.0 + exp(-aX));
}
static inline double siluGrad(double aX, double *aGrad) {
    double tSigmoid = 1.0 / (1.0 + exp(-aX));
    *aGrad = tSigmoid * (1 + aX * (1 - tSigmoid));
    return aX * tSigmoid;
}

static inline double forward(double *aX, jint aInputDim, jint *aHiddenDims, jint aHiddenNumber,
                             double *aHiddenWeights, double *aHiddenBiases, double *aOutputWeight, double aOutputBias,
                             double *rHiddenOutputs, double *rHiddenGrad) {
    double *tInput = aX;
    double *tOutput = rHiddenOutputs;
    double *tGrad = rHiddenGrad;
    double *tWeights = aHiddenWeights;
    double *tBiases = aHiddenBiases;
    jint tInSize = aInputDim;
    const jint tEnd = aHiddenNumber - 1;
    for (jint i = 0; i < tEnd; ++i) {
        const jint tOutSize = aHiddenDims[i];
        for (jint j = 0; j < tOutSize; ++j) {
            double rDot = dotAB_jse(tInput, tWeights, tInSize) + tBiases[j];
            if (tGrad == NULL) {
                tOutput[j] = silu(rDot);
            } else {
                double rGradDot;
                tOutput[j] = siluGrad(rDot, &rGradDot);
                tGrad[j] = rGradDot;
            }
            tWeights += tInSize;
        }
        tInput = tOutput;
        tOutput += tOutSize;
        if (tGrad != NULL) tGrad += tOutSize;
        tBiases += tOutSize;
        tInSize = tOutSize;
    }
    // special optimize for last layer
    double rOut = aOutputBias;
    const jint tOutSize = aHiddenDims[tEnd];
    for (jint j = 0; j < tOutSize; ++j) {
        double rDot = dotAB_jse(tInput, tWeights, tInSize) + tBiases[j];
        double tOutputWeight = aOutputWeight[j];
        if (tGrad == NULL) {
            rOut += silu(rDot) * tOutputWeight;
        } else {
            double rGradDot;
            rOut += siluGrad(rDot, &rGradDot) * tOutputWeight;
            tGrad[j] = rGradDot * tOutputWeight;
        }
        tWeights += tInSize;
    }
    return rOut;
}

JNIEXPORT jdouble JNICALL Java_jsex_nnap_nn_FeedForward_forward1(JNIEnv *aEnv, jclass aClazz,
        jdoubleArray aX, jint aShiftX, jint aInputDim, jintArray aHiddenDims, jint aHiddenNumber,
        jdoubleArray aHiddenWeights, jdoubleArray aHiddenBiases, jdoubleArray aOutputWeight, jdouble aOutputBias, jdoubleArray rHiddenOutputs) {
    // java array init
    double *tX = (double *)getJArrayBuf(aEnv, aX);
    jint *tHiddenDims = (jint *)getJArrayBuf(aEnv, aHiddenDims);
    double *tHiddenWeights = (double *)getJArrayBuf(aEnv, aHiddenWeights);
    double *tHiddenBiases = (double *)getJArrayBuf(aEnv, aHiddenBiases);
    double *tOutputWeight = (double *)getJArrayBuf(aEnv, aOutputWeight);
    double *tHiddenOutputs = (double *)getJArrayBuf(aEnv, rHiddenOutputs);
    
    double *tX_ = tX + aShiftX;
    double tOut = forward(tX_, aInputDim, tHiddenDims, aHiddenNumber,
                          tHiddenWeights, tHiddenBiases, tOutputWeight, aOutputBias,
                          tHiddenOutputs, NULL);
    
    // release java array
    releaseJArrayBuf(aEnv, aX, tX, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenDims, tHiddenDims, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenWeights, tHiddenWeights, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenBiases, tHiddenBiases, JNI_ABORT);
    releaseJArrayBuf(aEnv, aOutputWeight, tOutputWeight, JNI_ABORT);
    releaseJArrayBuf(aEnv, rHiddenOutputs, tHiddenOutputs, JNI_ABORT); // buffer only
    
    return tOut;
}

JNIEXPORT jdouble JNICALL Java_jsex_nnap_nn_FeedForward_backward1(JNIEnv *aEnv, jclass aClazz,
        jdoubleArray aX, jint aShiftX, jdoubleArray rGradX, jint aShiftGradX, jint aInputDim, jintArray aHiddenDims, jint aHiddenNumber,
        jdoubleArray aHiddenWeights, jdoubleArray aHiddenWeightsBackward, jdoubleArray aHiddenBiases, jdoubleArray aOutputWeight, jdouble aOutputBias, jdoubleArray rHiddenOutputs, jdoubleArray rHiddenGrads) {
    // java array init
    double *tX = (double *)getJArrayBuf(aEnv, aX);
    double *tGradX = (double *)getJArrayBuf(aEnv, rGradX);
    jint *tHiddenDims = (jint *)getJArrayBuf(aEnv, aHiddenDims);
    double *tHiddenWeights = (double *)getJArrayBuf(aEnv, aHiddenWeights);
    double *tHiddenWeightsBackward = (double *)getJArrayBuf(aEnv, aHiddenWeightsBackward);
    double *tHiddenBiases = (double *)getJArrayBuf(aEnv, aHiddenBiases);
    double *tOutputWeight = (double *)getJArrayBuf(aEnv, aOutputWeight);
    double *tHiddenOutputs = (double *)getJArrayBuf(aEnv, rHiddenOutputs);
    double *tHiddenGrads = (double *)getJArrayBuf(aEnv, rHiddenGrads);
    
    double *tX_ = tX + aShiftX;
    double *tGradX_ = tGradX + aShiftGradX;
    double tOut = forward(tX_, aInputDim, tHiddenDims, aHiddenNumber,
                          tHiddenWeights, tHiddenBiases, tOutputWeight, aOutputBias,
                          tHiddenOutputs, tHiddenGrads);
    // switch to last layer
    const jint tEnd = aHiddenNumber - 1;
    double *tGrad = tHiddenGrads;
    double *tOutput = tHiddenOutputs;
    for (jint i = 0; i < tEnd; ++i) {
        const jint tOutSize = tHiddenDims[i];
        tGrad += tOutSize;
        tOutput += tOutSize;
    }
    // begin backward, last layer has been specially optimized
    double *tInput = tGrad;
    double *tWeights = tHiddenWeightsBackward;
    jint tInSize = tHiddenDims[tEnd];
    for (jint i = tEnd-1; i >= 0; --i) {
        const jint tOutSize = tHiddenDims[i];
        tGrad -= tOutSize;
        tOutput -= tOutSize;
        for (jint j = 0; j < tOutSize; ++j) {
            tOutput[j] = dotAB_jse(tInput, tWeights, tInSize) * tGrad[j];
            tWeights += tInSize;
        }
        tInput = tOutput;
        tInSize = tOutSize;
    }
    // to input layer
    for (jint j = 0; j < aInputDim; ++j) {
        tGradX_[j] = dotAB_jse(tInput, tWeights, tInSize);
        tWeights += tInSize;
    }
    
    // release java array
    releaseJArrayBuf(aEnv, aX, tX, JNI_ABORT);
    releaseJArrayBuf(aEnv, rGradX, tGradX, 0);
    releaseJArrayBuf(aEnv, aHiddenDims, tHiddenDims, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenWeights, tHiddenWeights, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenWeightsBackward, tHiddenWeightsBackward, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenBiases, tHiddenBiases, JNI_ABORT);
    releaseJArrayBuf(aEnv, aOutputWeight, tOutputWeight, JNI_ABORT);
    releaseJArrayBuf(aEnv, rHiddenOutputs, tHiddenOutputs, JNI_ABORT); // buffer only
    releaseJArrayBuf(aEnv, rHiddenGrads, tHiddenGrads, JNI_ABORT); // buffer only
    
    return tOut;
}

#ifdef __cplusplus
}
#endif
