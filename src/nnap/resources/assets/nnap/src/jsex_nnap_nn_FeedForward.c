#include "jsex_nnap_nn_FeedForward.h"
#include "nnap_util.h"

#ifdef __cplusplus
extern "C" {
#endif

static inline jdouble silu(jdouble aX) {
    return aX / (1.0 + exp(-aX));
}
static inline jdouble siluGrad(jdouble aX, jdouble *rGrad) {
    jdouble tSigmoid = 1.0 / (1.0 + exp(-aX));
    *rGrad = tSigmoid * (1 + aX * (1 - tSigmoid));
    return aX * tSigmoid;
}
static inline jdouble siluGradGrad(jdouble aX, jdouble *rGrad, jdouble *rGradGrad) {
    jdouble tSigmoid = 1.0 / (1.0 + exp(-aX));
    *rGrad = tSigmoid * (1 + aX * (1 - tSigmoid));
    *rGradGrad = tSigmoid * (1 - tSigmoid) * (2 + aX * (1 - tSigmoid - tSigmoid));
    return aX * tSigmoid;
}

static inline jdouble forward(jdouble *aX, jint aInputDim, jint *aHiddenDims, jint aHiddenNumber,
                              jdouble *aHiddenWeights, jdouble *aHiddenBiases, jdouble *aOutputWeight, jdouble aOutputBias,
                              jdouble *rHiddenOutputs, jdouble *rHiddenGrads, jdouble *rHiddenGradGrads) {
    jdouble *tInput = aX;
    jdouble *tOutput = rHiddenOutputs;
    jdouble *tGrad = rHiddenGrads;
    jdouble *tGradGrad = rHiddenGradGrads;
    jdouble *tWeights = aHiddenWeights;
    jdouble *tBiases = aHiddenBiases;
    jint tInSize = aInputDim;
    const jint tEnd = aHiddenNumber - 1;
    for (jint i = 0; i < tEnd; ++i) {
        const jint tOutSize = aHiddenDims[i];
        for (jint j = 0; j < tOutSize; ++j) {
            jdouble rDot = dotAB_jse(tInput, tWeights, tInSize) + tBiases[j];
            if (tGrad == NULL) {
                tOutput[j] = silu(rDot);
            } else
            if (tGradGrad == NULL) {
                jdouble rGradDot;
                tOutput[j] = siluGrad(rDot, &rGradDot);
                tGrad[j] = rGradDot;
            } else {
                jdouble rGradDot, rGradGradDot;
                tOutput[j] = siluGradGrad(rDot, &rGradDot, &rGradGradDot);
                tGrad[j] = rGradDot;
                tGradGrad[j] = rGradGradDot;
            }
            tWeights+=tInSize;
        }
        tInput = tOutput;
        tOutput += tOutSize;
        if (tGrad != NULL) tGrad += tOutSize;
        if (tGradGrad != NULL) tGradGrad += tOutSize;
        tBiases += tOutSize;
        tInSize = tOutSize;
    }
    // special optimize for last layer
    jdouble rOut = aOutputBias;
    const jint tOutSize = aHiddenDims[tEnd];
    for (jint j = 0; j < tOutSize; ++j) {
        jdouble rDot = dotAB_jse(tInput, tWeights, tInSize) + tBiases[j];
        jdouble tOutputWeight = aOutputWeight[j];
        if (tGrad == NULL) {
            tOutput[j] = silu(rDot);
        } else
        if (tGradGrad == NULL) {
            jdouble rGradDot;
            tOutput[j] = siluGrad(rDot, &rGradDot);
            tGrad[j] = rGradDot;
        } else {
            jdouble rGradDot, rGradGradDot;
            tOutput[j] = siluGradGrad(rDot, &rGradDot, &rGradGradDot);
            tGrad[j] = rGradDot;
            tGradGrad[j] = rGradGradDot;
        }
        rOut += tOutput[j] * tOutputWeight;
        tWeights += tInSize;
    }
    return rOut;
}
static inline void backward(jdouble *aX, jdouble *rGradX, jdouble *rGradPara,
                            jint aInputDim, jint *aHiddenDims, jint aHiddenNumber, jdouble *aHiddenWeightsBackward, jdouble *aOutputWeight,
                            jdouble *rHiddenOutputs, jdouble *rHiddenGrads, jdouble *rHiddenGrads2, jdouble *rHiddenGrads3) {
    // switch to last layer
    const jint tEnd = aHiddenNumber - 1;
    jdouble *tGrad = rHiddenGrads;
    jdouble *tGrad2 = rHiddenGrads2!=NULL ? rHiddenGrads2 : rHiddenOutputs;
    jdouble *tGrad3 = rHiddenGrads3!=NULL ? rHiddenGrads3 : rHiddenOutputs;
    jdouble *tX = rHiddenOutputs;
    jdouble *rGradWeights = NULL, *rGradBiases = NULL;
    jint tInSize = -1;
    if (rGradPara != NULL) {
        rGradWeights = rGradPara;
        tInSize = aInputDim;
    }
    for (jint i = 0; i < tEnd; ++i) {
        const jint tOutSize = aHiddenDims[i];
        tGrad += tOutSize;
        tGrad2 += tOutSize;
        tGrad3 += tOutSize;
        tX += tOutSize;
        if (rGradPara != NULL) {
            rGradWeights += tInSize*tOutSize;
            tInSize = tOutSize;
        }
    }
    if (rGradPara != NULL) {
        jint tLastHiddenSize = aHiddenDims[tEnd];
        rGradWeights += tInSize*tLastHiddenSize;
        jdouble *rGradOutWeights = rGradWeights;
        for (jint j = 0; j < tLastHiddenSize; ++j) {
            rGradOutWeights[j] = tX[j]; // rGradOutWeights is the last output
        }
        rGradBiases = rGradOutWeights+tLastHiddenSize;
        for (jint i = 0; i < aHiddenNumber; ++i) {
            rGradBiases += aHiddenDims[i];
        }
        *rGradBiases = 1.0; // rGradOutBias always 1
    }
    // begin backward
    tInSize = aHiddenDims[tEnd];
    for (jint j = 0; j < tInSize; ++j) {
        tGrad3[j] = tGrad[j] * aOutputWeight[j];
    }
    jdouble *tWeights = aHiddenWeightsBackward;
    jdouble *tGrad3Before = tGrad3;
    for (jint i = tEnd-1; i >= 0; --i) {
        const jint tOutSize = aHiddenDims[i];
        tGrad -= tOutSize;
        tGrad2 -= tOutSize;
        tGrad3 -= tOutSize;
        tX -= tOutSize;
        if (rGradPara != NULL) {
            jint tWeightSize = tOutSize*tInSize;
            rGradWeights -= tWeightSize;
            rGradBiases -= tInSize;
            for (jint j = 0; j < tInSize; ++j) {
                jdouble tSubGrad3 = tGrad3Before[j];
                rGradBiases[j] = tSubGrad3;
                for (jint k = 0; k < tOutSize; ++k) {
                    rGradWeights[k] = tSubGrad3 * tX[k];
                }
                rGradWeights += tOutSize;
            }
            rGradWeights -= tWeightSize;
        }
        for (jint j = 0; j < tOutSize; ++j) {
            tGrad2[j] = dotAB_jse(tGrad3Before, tWeights, tInSize);
            tGrad3[j] = tGrad2[j] * tGrad[j];
            tWeights += tInSize;
        }
        tGrad3Before = tGrad3;
        tInSize = tOutSize;
    }
    // to input layer
    if (rGradPara != NULL) {
        rGradWeights -= aInputDim*tInSize;
        rGradBiases -= tInSize;
        for (jint j = 0; j < tInSize; ++j) {
            jdouble tSubGrad3 = tGrad3Before[j];
            rGradBiases[j] = tSubGrad3;
            for (jint k = 0; k < aInputDim; ++k) {
                rGradWeights[k] = tSubGrad3 * aX[k];
            }
            rGradWeights += aInputDim;
        }
    }
    if (rGradX != NULL) {
        for (jint j = 0; j < aInputDim; ++j) {
            rGradX[j] = dotAB_jse(tGrad3Before, tWeights, tInSize);
            tWeights += tInSize;
        }
    }
}

JNIEXPORT jdouble JNICALL Java_jsex_nnap_nn_FeedForward_forward1(JNIEnv *aEnv, jclass aClazz,
        jdoubleArray aX, jint aShiftX, jint aInputDim, jintArray aHiddenDims, jint aHiddenNumber,
        jdoubleArray aHiddenWeights, jdoubleArray aHiddenBiases, jdoubleArray aOutputWeight, jdouble aOutputBias,
        jdoubleArray rHiddenOutputs, jdoubleArray rHiddenGrads) {
    // java array init
    jdouble *tX = (jdouble *)getJArrayBuf(aEnv, aX);
    jint *tHiddenDims = (jint *)getJArrayBuf(aEnv, aHiddenDims);
    jdouble *tHiddenWeights = (jdouble *)getJArrayBuf(aEnv, aHiddenWeights);
    jdouble *tHiddenBiases = (jdouble *)getJArrayBuf(aEnv, aHiddenBiases);
    jdouble *tOutputWeight = (jdouble *)getJArrayBuf(aEnv, aOutputWeight);
    jdouble *tHiddenOutputs = (jdouble *)getJArrayBuf(aEnv, rHiddenOutputs);
    jdouble *tHiddenGrads = rHiddenGrads==NULL?NULL:(jdouble *)getJArrayBuf(aEnv, rHiddenGrads);
    
    jdouble tOut = forward(tX+aShiftX, aInputDim, tHiddenDims, aHiddenNumber,
                           tHiddenWeights, tHiddenBiases, tOutputWeight, aOutputBias,
                           tHiddenOutputs, tHiddenGrads, NULL);
    
    // release java array
    releaseJArrayBuf(aEnv, aX, tX, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenDims, tHiddenDims, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenWeights, tHiddenWeights, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenBiases, tHiddenBiases, JNI_ABORT);
    releaseJArrayBuf(aEnv, aOutputWeight, tOutputWeight, JNI_ABORT);
    releaseJArrayBuf(aEnv, rHiddenOutputs, tHiddenOutputs, rHiddenGrads==NULL?JNI_ABORT:0); // buffer only for no grad
    if (rHiddenGrads!=NULL) releaseJArrayBuf(aEnv, rHiddenGrads, tHiddenGrads, 0);
    
    return tOut;
}

JNIEXPORT jdouble JNICALL Java_jsex_nnap_nn_FeedForward_forwardGrad1(JNIEnv *aEnv, jclass aClazz,
        jdoubleArray aX, jint aShiftX, jdoubleArray rGradX, jint aShiftGradX, jdoubleArray rGradPara, jint aShiftGradPara, jint aInputDim, jintArray aHiddenDims, jint aHiddenNumber,
        jdoubleArray aHiddenWeights, jdoubleArray aHiddenWeightsBackward, jdoubleArray aHiddenBiases, jdoubleArray aOutputWeight, jdouble aOutputBias,
        jdoubleArray rHiddenOutputs, jdoubleArray rHiddenGrads, jdoubleArray rHiddenGrads2, jdoubleArray rHiddenGrads3, jdoubleArray rHiddenGradGrads) {
    // java array init
    jdouble *tX = (jdouble *)getJArrayBuf(aEnv, aX);
    jdouble *tGradX = (jdouble *)getJArrayBuf(aEnv, rGradX);
    jdouble *tGradPara = rGradPara==NULL?NULL:(jdouble *)getJArrayBuf(aEnv, rGradPara);
    jint *tHiddenDims = (jint *)getJArrayBuf(aEnv, aHiddenDims);
    jdouble *tHiddenWeights = (jdouble *)getJArrayBuf(aEnv, aHiddenWeights);
    jdouble *tHiddenWeightsBackward = (jdouble *)getJArrayBuf(aEnv, aHiddenWeightsBackward);
    jdouble *tHiddenBiases = (jdouble *)getJArrayBuf(aEnv, aHiddenBiases);
    jdouble *tOutputWeight = (jdouble *)getJArrayBuf(aEnv, aOutputWeight);
    jdouble *tHiddenOutputs = (jdouble *)getJArrayBuf(aEnv, rHiddenOutputs);
    jdouble *tHiddenGrads = (jdouble *)getJArrayBuf(aEnv, rHiddenGrads);
    jdouble *tHiddenGrads2 = rHiddenGrads2==NULL?NULL:(jdouble *)getJArrayBuf(aEnv, rHiddenGrads2);
    jdouble *tHiddenGrads3 = rHiddenGrads3==NULL?NULL:(jdouble *)getJArrayBuf(aEnv, rHiddenGrads3);
    jdouble *tHiddenGradGrads = rHiddenGradGrads==NULL?NULL:(jdouble *)getJArrayBuf(aEnv, rHiddenGradGrads);
    
    jdouble tOut = forward(tX+aShiftX, aInputDim, tHiddenDims, aHiddenNumber,
                           tHiddenWeights, tHiddenBiases, tOutputWeight, aOutputBias,
                           tHiddenOutputs, tHiddenGrads, tHiddenGradGrads);
    
    backward(tX+aShiftX, tGradX+aShiftGradX, tGradPara==NULL?NULL:(tGradPara+aShiftGradPara),
             aInputDim, tHiddenDims, aHiddenNumber, tHiddenWeightsBackward, tOutputWeight,
             tHiddenOutputs, tHiddenGrads, tHiddenGrads2, tHiddenGrads3);
    
    // release java array
    releaseJArrayBuf(aEnv, aX, tX, JNI_ABORT);
    releaseJArrayBuf(aEnv, rGradX, tGradX, 0);
    if (rGradPara!=NULL) releaseJArrayBuf(aEnv, rGradPara, tGradPara, 0);
    releaseJArrayBuf(aEnv, aHiddenDims, tHiddenDims, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenWeights, tHiddenWeights, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenWeightsBackward, tHiddenWeightsBackward, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenBiases, tHiddenBiases, JNI_ABORT);
    releaseJArrayBuf(aEnv, aOutputWeight, tOutputWeight, JNI_ABORT);
    releaseJArrayBuf(aEnv, rHiddenOutputs, tHiddenOutputs, rHiddenGradGrads==NULL?JNI_ABORT:0); // buffer only for no grad
    releaseJArrayBuf(aEnv, rHiddenGrads, tHiddenGrads, rHiddenGradGrads==NULL?JNI_ABORT:0); // buffer only for no grad
    if (rHiddenGrads2!=NULL) releaseJArrayBuf(aEnv, rHiddenGrads2, tHiddenGrads2, 0);
    if (rHiddenGrads3!=NULL) releaseJArrayBuf(aEnv, rHiddenGrads3, tHiddenGrads3, 0);
    if (rHiddenGradGrads!=NULL) releaseJArrayBuf(aEnv, rHiddenGradGrads, tHiddenGradGrads, 0);
    
    return tOut;
}

JNIEXPORT void JNICALL Java_jsex_nnap_nn_FeedForward_backward1(JNIEnv *aEnv, jclass aClazz,
        jdoubleArray aX, jint aShiftX, jdoubleArray rGradPara, jint aShiftGradPara, jint aInputDim, jintArray aHiddenDims, jint aHiddenNumber,
        jdoubleArray aHiddenWeightsBackward, jdoubleArray aOutputWeight,
        jdoubleArray rHiddenOutputs, jdoubleArray rHiddenGrads) {
    // java array init
    jdouble *tX = (jdouble *)getJArrayBuf(aEnv, aX);
    jdouble *tGradPara = (jdouble *)getJArrayBuf(aEnv, rGradPara);
    jint *tHiddenDims = (jint *)getJArrayBuf(aEnv, aHiddenDims);
    jdouble *tHiddenWeightsBackward = (jdouble *)getJArrayBuf(aEnv, aHiddenWeightsBackward);
    jdouble *tOutputWeight = (jdouble *)getJArrayBuf(aEnv, aOutputWeight);
    jdouble *tHiddenOutputs = (jdouble *)getJArrayBuf(aEnv, rHiddenOutputs);
    jdouble *tHiddenGrads = (jdouble *)getJArrayBuf(aEnv, rHiddenGrads);
    
    backward(tX+aShiftX, NULL, tGradPara+aShiftGradPara,
             aInputDim, tHiddenDims, aHiddenNumber, tHiddenWeightsBackward, tOutputWeight,
             tHiddenOutputs, tHiddenGrads, NULL, NULL);
    
    // release java array
    releaseJArrayBuf(aEnv, aX, tX, JNI_ABORT);
    releaseJArrayBuf(aEnv, rGradPara, tGradPara, 0);
    releaseJArrayBuf(aEnv, aHiddenDims, tHiddenDims, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenWeightsBackward, tHiddenWeightsBackward, JNI_ABORT);
    releaseJArrayBuf(aEnv, aOutputWeight, tOutputWeight, JNI_ABORT);
    releaseJArrayBuf(aEnv, rHiddenOutputs, tHiddenOutputs, JNI_ABORT); // buffer only
    releaseJArrayBuf(aEnv, rHiddenGrads, tHiddenGrads, JNI_ABORT); // buffer only
}

JNIEXPORT void JNICALL Java_jsex_nnap_nn_FeedForward_gradBackward1(JNIEnv *aEnv, jclass aClazz,
        jdoubleArray aX, jint aShiftX, jdoubleArray rGradX, jint aShiftGradX, jdoubleArray rGradXGradPara, jint aShiftGradXGradPara,
        jint aInputDim, jintArray aHiddenDims, jint aHiddenNumber, jdoubleArray aHiddenWeights, jdoubleArray aHiddenWeightsBackward, jdoubleArray aOutputWeight,
        jdoubleArray aHiddenOutputs, jdoubleArray aHiddenGrads, jdoubleArray aHiddenGrads2, jdoubleArray aHiddenGrads3, jdoubleArray aHiddenGradGrads,
        jdoubleArray rHiddenMatOutputGrads, jdoubleArray rHiddenMatGradGrads) {
    // java array init
    jdouble *tX = (jdouble *)getJArrayBuf(aEnv, aX);
    jdouble *tGradX = (jdouble *)getJArrayBuf(aEnv, rGradX);
    jdouble *tGradXGradPara = (jdouble *)getJArrayBuf(aEnv, rGradXGradPara);
    jint *tHiddenDims = (jint *)getJArrayBuf(aEnv, aHiddenDims);
    jdouble *tHiddenWeights = (jdouble *)getJArrayBuf(aEnv, aHiddenWeights);
    jdouble *tHiddenWeightsBackward = (jdouble *)getJArrayBuf(aEnv, aHiddenWeightsBackward);
    jdouble *tOutputWeight = (jdouble *)getJArrayBuf(aEnv, aOutputWeight);
    jdouble *tHiddenOutputs = (jdouble *)getJArrayBuf(aEnv, aHiddenOutputs);
    jdouble *tHiddenGrads = (jdouble *)getJArrayBuf(aEnv, aHiddenGrads);
    jdouble *tHiddenGrads2 = (jdouble *)getJArrayBuf(aEnv, aHiddenGrads2);
    jdouble *tHiddenGrads3 = (jdouble *)getJArrayBuf(aEnv, aHiddenGrads3);
    jdouble *tHiddenGradGrads = (jdouble *)getJArrayBuf(aEnv, aHiddenGradGrads);
    jdouble *tHiddenMatOutputGrads = (jdouble *)getJArrayBuf(aEnv, rHiddenMatOutputGrads);
    jdouble *tHiddenMatGradGrads = (jdouble *)getJArrayBuf(aEnv, rHiddenMatGradGrads);
    
    gradBackward(tX+aShiftX, tGradX+aShiftGradX, tGradXGradPara+aShiftGradXGradPara,
                 aInputDim, tHiddenDims, aHiddenNumber,
                 tHiddenWeights, tHiddenWeightsBackward, tOutputWeight,
                 tHiddenOutputs, tHiddenGrads, tHiddenGrads2, tHiddenGrads3, tHiddenGradGrads,
                 tHiddenMatOutputGrads, tHiddenMatGradGrads);
    
    // release java array
    releaseJArrayBuf(aEnv, aX, tX, JNI_ABORT);
    releaseJArrayBuf(aEnv, rGradX, tGradX, 0);
    releaseJArrayBuf(aEnv, rGradXGradPara, tGradXGradPara, 0);
    releaseJArrayBuf(aEnv, aHiddenDims, tHiddenDims, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenWeights, tHiddenWeights, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenWeightsBackward, tHiddenWeightsBackward, JNI_ABORT);
    releaseJArrayBuf(aEnv, aOutputWeight, tOutputWeight, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenOutputs, tHiddenOutputs, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenGrads, tHiddenGrads, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenGrads2, tHiddenGrads2, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenGrads3, tHiddenGrads3, JNI_ABORT);
    releaseJArrayBuf(aEnv, aHiddenGradGrads, tHiddenGradGrads, JNI_ABORT);
    releaseJArrayBuf(aEnv, rHiddenMatOutputGrads, tHiddenMatOutputGrads, JNI_ABORT); // buffer only
    releaseJArrayBuf(aEnv, rHiddenMatGradGrads, tHiddenMatGradGrads, JNI_ABORT); // buffer only
}

#ifdef __cplusplus
}
#endif
