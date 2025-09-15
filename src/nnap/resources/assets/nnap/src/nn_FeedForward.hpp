#ifndef NN_FEED_FORWARD_H
#define NN_FEED_FORWARD_H

#include "nnap_util.hpp"

namespace JSE_NNAP {

static inline jdouble silu(jdouble aX) noexcept {
    return aX / (1.0 + exp(-aX));
}
static inline jdouble siluGrad(jdouble aX, jdouble *rGrad) noexcept {
    jdouble tSigmoid = 1.0 / (1.0 + exp(-aX));
    *rGrad = tSigmoid * (1 + aX * (1 - tSigmoid));
    return aX * tSigmoid;
}
static inline jdouble siluGradGrad(jdouble aX, jdouble *rGrad, jdouble *rGradGrad) noexcept {
    jdouble tSigmoid = 1.0 / (1.0 + exp(-aX));
    *rGrad = tSigmoid * (1 + aX * (1 - tSigmoid));
    *rGradGrad = tSigmoid * (1 - tSigmoid) * (2 + aX * (1 - tSigmoid - tSigmoid));
    return aX * tSigmoid;
}

static inline jdouble forward(jdouble *aX, jint aInputDim, jint *aHiddenDims, jint aHiddenNumber,
                              jdouble *aHiddenWeights, jdouble *aHiddenBiases, jdouble *aOutputWeight, jdouble aOutputBias,
                              jdouble *rHiddenOutputs, jdouble *rHiddenGrads, jdouble *rHiddenGradGrads) noexcept {
    jdouble *tInput = aX;
    jdouble *rOutput = rHiddenOutputs;
    jdouble *rGrad = rHiddenGrads;
    jdouble *rGradGrad = rHiddenGradGrads;
    jdouble *tWeights = aHiddenWeights;
    jdouble *tBiases = aHiddenBiases;
    jint tInSize = aInputDim;
    const jint tEnd = aHiddenNumber - 1;
    for (jint i = 0; i < tEnd; ++i) {
        const jint tOutSize = aHiddenDims[i];
        for (jint j = 0; j < tOutSize; ++j) {
            jdouble rDot = dot(tInput, tWeights, tInSize) + tBiases[j];
            if (rGrad == NULL) {
                rOutput[j] = silu(rDot);
            } else
            if (rGradGrad == NULL) {
                jdouble rGradDot;
                rOutput[j] = siluGrad(rDot, &rGradDot);
                rGrad[j] = rGradDot;
            } else {
                jdouble rGradDot, rGradGradDot;
                rOutput[j] = siluGradGrad(rDot, &rGradDot, &rGradGradDot);
                rGrad[j] = rGradDot;
                rGradGrad[j] = rGradGradDot;
            }
            tWeights += tInSize;
        }
        tInput = rOutput;
        rOutput += tOutSize;
        if (rGrad != NULL) rGrad += tOutSize;
        if (rGradGrad != NULL) rGradGrad += tOutSize;
        tBiases += tOutSize;
        tInSize = tOutSize;
    }
    // special optimize for last layer
    jdouble rOut = aOutputBias;
    const jint tOutSize = aHiddenDims[tEnd];
    for (jint j = 0; j < tOutSize; ++j) {
        jdouble rDot = dot(tInput, tWeights, tInSize) + tBiases[j];
        if (rGrad == NULL) {
            rOutput[j] = silu(rDot);
        } else
        if (rGradGrad == NULL) {
            jdouble rGradDot;
            rOutput[j] = siluGrad(rDot, &rGradDot);
            rGrad[j] = rGradDot;
        } else {
            jdouble rGradDot, rGradGradDot;
            rOutput[j] = siluGradGrad(rDot, &rGradDot, &rGradGradDot);
            rGrad[j] = rGradDot;
            rGradGrad[j] = rGradGradDot;
        }
        jdouble tOutputWeight = aOutputWeight[j];
        rOut += rOutput[j] * tOutputWeight;
        tWeights += tInSize;
    }
    return rOut;
}
static inline void backward(jdouble aYGrad, jdouble *aX, jdouble *rGradX, jdouble *rGradPara,
                            jint aInputDim, jint *aHiddenDims, jint aHiddenNumber, jdouble *aHiddenWeightsBackward, jdouble *aOutputWeight,
                            jdouble *aHiddenOutputs, jdouble *aHiddenGrads, jdouble *rHiddenGrads2, jdouble *rHiddenGrads3) noexcept {
    // switch to last layer
    const jint tEnd = aHiddenNumber - 1;
    jdouble *tGrad = aHiddenGrads;
    jdouble *rGrad2 = rHiddenGrads2;
    jdouble *rGrad3 = rHiddenGrads3;
    jdouble *tX = aHiddenOutputs;
    jdouble *rGradWeights = NULL, *rGradBiases = NULL;
    jint tInSize = -1;
    if (rGradPara != NULL) {
        rGradWeights = rGradPara;
        tInSize = aInputDim;
    }
    for (jint i = 0; i < tEnd; ++i) {
        const jint tOutSize = aHiddenDims[i];
        tGrad += tOutSize;
        rGrad2 += tOutSize;
        rGrad3 += tOutSize;
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
            rGradOutWeights[j] += aYGrad*tX[j];
        }
        rGradBiases = rGradOutWeights+tLastHiddenSize;
        for (jint i = 0; i < aHiddenNumber; ++i) {
            rGradBiases += aHiddenDims[i];
        }
        *rGradBiases += aYGrad;
    }
    // begin backward
    tInSize = aHiddenDims[tEnd];
    for (jint j = 0; j < tInSize; ++j) {
        rGrad3[j] = aYGrad * tGrad[j] * aOutputWeight[j];
    }
    jdouble *tWeights = aHiddenWeightsBackward;
    jdouble *tGrad3Before = rGrad3;
    for (jint i = tEnd-1; i >= 0; --i) {
        const jint tOutSize = aHiddenDims[i];
        tGrad -= tOutSize;
        rGrad2 -= tOutSize;
        rGrad3 -= tOutSize;
        tX -= tOutSize;
        if (rGradPara != NULL) {
            jint tWeightSize = tOutSize*tInSize;
            rGradWeights -= tWeightSize;
            rGradBiases -= tInSize;
            for (jint j = 0; j < tInSize; ++j) {
                jdouble tSubGrad3 = tGrad3Before[j];
                rGradBiases[j] += tSubGrad3;
                for (jint k = 0; k < tOutSize; ++k) {
                    rGradWeights[k] += tSubGrad3 * tX[k];
                }
                rGradWeights += tOutSize;
            }
            rGradWeights -= tWeightSize;
        }
        for (jint j = 0; j < tOutSize; ++j) {
            rGrad2[j] = dot(tGrad3Before, tWeights, tInSize);
            rGrad3[j] = rGrad2[j] * tGrad[j];
            tWeights += tInSize;
        }
        tGrad3Before = rGrad3;
        tInSize = tOutSize;
    }
    // to input layer
    if (rGradPara != NULL) {
        rGradWeights -= aInputDim*tInSize;
        rGradBiases -= tInSize;
        for (jint j = 0; j < tInSize; ++j) {
            jdouble tSubGrad3 = tGrad3Before[j];
            rGradBiases[j] += tSubGrad3;
            for (jint k = 0; k < aInputDim; ++k) {
                rGradWeights[k] += tSubGrad3 * aX[k];
            }
            rGradWeights += aInputDim;
        }
    }
    if (rGradX != NULL) {
        for (jint j = 0; j < aInputDim; ++j) {
            rGradX[j] += dot(tGrad3Before, tWeights, tInSize);
            tWeights += tInSize;
        }
    }
}
static inline void gradBackward(jdouble *aGradXGrad, jdouble *aX, jdouble *rGradX, jdouble *rGradPara,
                                jint aInputDim, jint *aHiddenDims, jint aHiddenNumber, jdouble *aHiddenWeights, jdouble *aHiddenWeightsBackward, jdouble *aOutputWeight,
                                jdouble *aHiddenOutputs, jdouble *aHiddenGrads, jdouble *aHiddenGrads2, jdouble *aHiddenGrads3, jdouble *aHiddenGradGrads,
                                jdouble *rHiddenOutputs2, jdouble *rHiddenGrads4, jdouble *rHiddenGrads5) noexcept {
    // ptr init
    jdouble *tGrad = aHiddenGrads;
    jdouble *tGrad2 = aHiddenGrads2;
    jdouble *tGrad3 = aHiddenGrads3;
    jdouble *tGradGrad = aHiddenGradGrads;
    jdouble *tX = aHiddenOutputs;
    jdouble *rX2 = rHiddenOutputs2;
    jdouble *rGrad4 = rHiddenGrads4;
    jdouble *rGrad5 = rHiddenGrads5;
    jdouble *rGradWeights = rGradPara;
    jdouble *rGradBiases = rGradPara;
    jint tColNum = aInputDim;
    for (jint i = 0; i < aHiddenNumber; ++i) {
        jint tHiddenDim = aHiddenDims[i];
        rGradBiases += tColNum * tHiddenDim;
        tColNum = tHiddenDim;
    }
    rGradBiases += aHiddenDims[aHiddenNumber-1];
    /// backward backward
    // diff W0 ij
    jint tInSize = aInputDim;
    jint tOutSize = aHiddenDims[0];
    for (jint i = 0; i < tOutSize; ++i) {
        jdouble tSubGrad3 = tGrad3[i];
        for (jint j = 0; j < tInSize; ++j) {
            rGradWeights[j] += aGradXGrad[j]*tSubGrad3;
        }
        rGradWeights += tInSize;
    }
    // G^0 i
    jdouble *tWeights = aHiddenWeights;
    for (jint i = 0; i < tOutSize; ++i) {
        rGrad5[i] = dot(aGradXGrad, tWeights, tInSize);
        tWeights += tInSize;
    }
    const jint tEnd = aHiddenNumber - 1;
    for (jint l = 0; l < tEnd; ++l) {
        // Gl i, G~l i
        for (jint i = 0; i < tOutSize; ++i) {
            rGrad4[i] = tGrad2[i] * rGrad5[i];
            rGrad5[i] = tGrad[i] * rGrad5[i];
        }
        tGrad += tOutSize;
        tGrad2 += tOutSize;
        tGrad3 += tOutSize;
        tGradGrad += tOutSize;
        tX += tOutSize;
        rGradBiases += tOutSize;
        // diff Wl+1 ij
        tInSize = tOutSize;
        tOutSize = aHiddenDims[l+1];
        for (jint i = 0; i < tOutSize; ++i) {
            jdouble tSubGrad3 = tGrad3[i];
            for (jint j = 0; j < tInSize; ++j) {
                rGradWeights[j] += tSubGrad3 * rGrad5[j];
            }
            rGradWeights += tInSize;
        }
        // G^l+1 ik
        jdouble *rGrad6 = rGrad5 + tInSize;
        for (jint i = 0; i < tOutSize; ++i) {
            rGrad6[i] = dot(tWeights, rGrad5, tInSize);
            tWeights += tInSize;
        }
        rGrad4 += tInSize;
        rGrad5 += tInSize;
    }
    // Gend i, Wo i
    tOutSize = aHiddenDims[tEnd];
    for (jint i = 0; i < tOutSize; ++i) {
        rGrad4[i] = aOutputWeight[i] * rGrad5[i];
        rGradWeights[i] += tGrad[i] * rGrad5[i];
    }
    /// backward forward
    // X^end ik
    for (jint i = 0; i < tOutSize; ++i) {
        rX2[i] = tGradGrad[i] * rGrad4[i];
    }
    rGradBiases += tOutSize;
    tWeights = aHiddenWeightsBackward;
    for (jint l = tEnd; l > 0; --l) {
        // bl i, Wl ij
        tInSize = aHiddenDims[l-1];
        tX -= tInSize;
        rGradBiases -= tOutSize;
        rGradWeights -= tInSize*tOutSize;
        for (jint i = 0; i < tOutSize; ++i) {
            jdouble tSubX2 = rX2[i];
            rGradBiases[i] += tSubX2;
            for (jint j = 0; j < tInSize; ++j) {
                rGradWeights[j] += tX[j] * tSubX2;
            }
            rGradWeights += tInSize;
        }
        rGradWeights -= tInSize*tOutSize;
        // Xl-1 ik
        jdouble *rX3 = rX2 + tOutSize;
        for (jint i = 0; i < tInSize; ++i) {
            rX3[i] = dot(tWeights, rX2, tOutSize);
            tWeights += tOutSize;
        }
        rX2 += tOutSize;
        // X^l-1 ik
        tOutSize = tInSize;
        tGrad -= tInSize;
        tGradGrad -= tInSize;
        rGrad4 -= tOutSize;
        for (jint i = 0; i < tOutSize; ++i) {
            rX2[i] = tGradGrad[i]*rGrad4[i] + tGrad[i]*rX2[i];
        }
    }
    // b0 i, W0 ij
    rGradBiases -= tOutSize;
    rGradWeights -= aInputDim*tOutSize;
    for (jint i = 0; i < tOutSize; ++i) {
        jdouble tSubX2 = rX2[i];
        rGradBiases[i] += tSubX2;
        for (jint j = 0; j < aInputDim; ++j) {
            rGradWeights[j] += aX[j]*tSubX2;
        }
        rGradWeights += aInputDim;
    }
    if (rGradX != NULL) {
        for (jint j = 0; j < aInputDim; ++j) {
            rGradX[j] += dot(rX2, tWeights, tInSize);
            tWeights += tInSize;
        }
    }
}

}

#endif //NN_FEED_FORWARD_H