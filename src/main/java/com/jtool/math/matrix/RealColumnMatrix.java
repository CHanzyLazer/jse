package com.jtool.math.matrix;

import com.jtool.math.operator.IOperator1;
import com.jtool.math.operator.IOperator2;
import com.jtool.math.vector.AbstractVector;
import com.jtool.math.vector.IVector;
import com.jtool.math.vector.IVectorGenerator;
import com.jtool.math.vector.RealVector;

import java.util.Arrays;


/**
 * @author liqa
 * <p> 矩阵一般实现，按照列排序 </p>
 */
public class RealColumnMatrix extends AbstractRealMatrix<RealColumnMatrix, RealVector> {
    /** 提供默认的创建 */
    public static RealColumnMatrix ones(int aSize) {return ones(aSize, aSize);}
    public static RealColumnMatrix ones(int aRowNum, int aColNum) {
        double[] tData = new double[aRowNum*aColNum];
        Arrays.fill(tData, 1.0);
        return new RealColumnMatrix(aRowNum, aColNum, tData);
    }
    public static RealColumnMatrix zeros(int aSize) {return zeros(aSize, aSize);}
    public static RealColumnMatrix zeros(int aRowNum, int aColNum) {return new RealColumnMatrix(aRowNum, aColNum, new double[aRowNum*aColNum]);}
    
    
    private final double[] mData;
    private final int mRowNum;
    private final int mColNum;
    
    public RealColumnMatrix(int aRowNum, int aColNum, double[] aData) {
        mRowNum = aRowNum;
        mData = aData;
        mColNum = aColNum;
    }
    public RealColumnMatrix(int aRowNum, double[] aData) {this(aRowNum, aData.length/aRowNum, aData);}
    
    
    /** IMatrix stuffs */
    @Override public Double get_(int aRow, int aCol) {return mData[aRow + aCol*mRowNum];}
    @Override public void set_(int aRow, int aCol, Number aValue) {mData[aRow + aCol*mRowNum] = aValue.doubleValue();}
    @Override public Double getAndSet_(int aRow, int aCol, Number aValue) {
        int tIdx = aRow + aCol*mRowNum;
        Double oValue = mData[tIdx];
        mData[tIdx] = aValue.doubleValue();
        return oValue;
    }
    @Override public int rowNumber() {return mRowNum;}
    @Override public int columnNumber() {return mColNum;}
    
    @Override protected RealColumnMatrix newZeros(int aRowNum, int aColNum) {return RealColumnMatrix.zeros(aRowNum, aColNum);}
    @Override protected RealVector newZeros(int aSize) {return RealVector.zeros(aSize);}
    
    
    /** Optimize stuffs，重写这个提高列向的索引速度 */
    @Override public IVector<Double> col(final int aCol) {
        if (aCol<0 || aCol>=columnNumber()) throw new IndexOutOfBoundsException("Col: "+aCol);
        return new AbstractVector<Double>() {
            private final int mShift = aCol*mRowNum;
            @Override public Double get_(int aIdx) {return mData[aIdx + mShift];}
            @Override public void set_(int aIdx, Number aValue) {mData[aIdx + mShift] = aValue.doubleValue();}
            @Override public Double getAndSet_(int aIdx, Number aValue) {
                int tIdx = aIdx + mShift;
                Double oValue = mData[tIdx];
                mData[tIdx] = aValue.doubleValue();
                return oValue;
            }
            @Override public int size() {return mRowNum;}
        };
    }
    
    /** Optimize stuffs，重写这些接口来加速批量填充过程 */
    @Override public void fill(Number aValue) {Arrays.fill(mData, aValue.doubleValue());}
    @Override public void fillWith(IMatrixGetter<? extends Number> aMatrixGetter) {
        int tRowNum = rowNumber();
        int tColNum = columnNumber();
        int i = 0;
        for (int col = 0; col < tColNum; ++col) for (int row = 0; row < tRowNum; ++row) {
            mData[i] = aMatrixGetter.get(row, col).doubleValue();
            ++i;
        }
    }
    
    /** Optimize stuffs，重写这些接口来加速运算的填充到自身的过程 */
    @Override protected void ebeDo2this_(final IMatrixGetter<? extends Number> aRHS, final IOperator2<Double> aOpt) {
        if (aRHS instanceof RealColumnMatrix && ((RealColumnMatrix)aRHS).mRowNum == mRowNum) {
            double[] tData = ((RealColumnMatrix)aRHS).mData;
            for (int i = 0; i < mData.length; ++i) mData[i] = aOpt.cal(mData[i], tData[i]);
        } else {
            int tRowNum = rowNumber();
            int tColNum = columnNumber();
            int i = 0;
            for (int col = 0; col < tColNum; ++col) for (int row = 0; row < tRowNum; ++row) {
                mData[i] = aOpt.cal(mData[i], aRHS.get(row, col).doubleValue());
                ++i;
            }
        }
    }
    @Override protected void mapDo2this_(final IOperator1<Double> aOpt) {
        for (int i = 0; i < mData.length; ++i) mData[i] = aOpt.cal(mData[i]);
    }
    
    /** Optimize stuffs，重写简单运算从而可以让编译器更好做 SIMD 相关的优化 */
    @Override public IMatrixOperation<RealColumnMatrix, Double> operation() {
        return new RealMatrixOperation() {
            @Override public RealColumnMatrix ebeAdd(IMatrixGetter<? extends Number> aLHS, IMatrixGetter<? extends Number> aRHS) {
                if (aLHS instanceof RealColumnMatrix && aRHS instanceof RealColumnMatrix && ((RealColumnMatrix) aLHS).mRowNum == mRowNum && ((RealColumnMatrix) aRHS).mRowNum == mRowNum) {
                    double[] tData1 = ((RealColumnMatrix) aLHS).mData;
                    double[] tData2 = ((RealColumnMatrix) aRHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = tData1[i] + tData2[i];
                    return rMatrix;
                }
                return super.ebeAdd(aLHS, aRHS);
            }
            @Override public RealColumnMatrix ebeMinus(IMatrixGetter<? extends Number> aLHS, IMatrixGetter<? extends Number> aRHS) {
                if (aLHS instanceof RealColumnMatrix && aRHS instanceof RealColumnMatrix && ((RealColumnMatrix) aLHS).mRowNum == mRowNum && ((RealColumnMatrix) aRHS).mRowNum == mRowNum) {
                    double[] tData1 = ((RealColumnMatrix) aLHS).mData;
                    double[] tData2 = ((RealColumnMatrix) aRHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = tData1[i] - tData2[i];
                    return rMatrix;
                }
                return super.ebeMinus(aLHS, aRHS);
            }
            @Override public RealColumnMatrix ebeMultiply(IMatrixGetter<? extends Number> aLHS, IMatrixGetter<? extends Number> aRHS) {
                if (aLHS instanceof RealColumnMatrix && aRHS instanceof RealColumnMatrix && ((RealColumnMatrix) aLHS).mRowNum == mRowNum && ((RealColumnMatrix) aRHS).mRowNum == mRowNum) {
                    double[] tData1 = ((RealColumnMatrix) aLHS).mData;
                    double[] tData2 = ((RealColumnMatrix) aRHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = tData1[i] * tData2[i];
                    return rMatrix;
                }
                return super.ebeMultiply(aLHS, aRHS);
            }
            @Override public RealColumnMatrix ebeDivide(IMatrixGetter<? extends Number> aLHS, IMatrixGetter<? extends Number> aRHS) {
                if (aLHS instanceof RealColumnMatrix && aRHS instanceof RealColumnMatrix && ((RealColumnMatrix) aLHS).mRowNum == mRowNum && ((RealColumnMatrix) aRHS).mRowNum == mRowNum) {
                    double[] tData1 = ((RealColumnMatrix) aLHS).mData;
                    double[] tData2 = ((RealColumnMatrix) aRHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = tData1[i] / tData2[i];
                    return rMatrix;
                }
                return super.ebeDivide(aLHS, aRHS);
            }
            @Override public RealColumnMatrix ebeMod(IMatrixGetter<? extends Number> aLHS, IMatrixGetter<? extends Number> aRHS) {
                if (aLHS instanceof RealColumnMatrix && aRHS instanceof RealColumnMatrix && ((RealColumnMatrix) aLHS).mRowNum == mRowNum && ((RealColumnMatrix) aRHS).mRowNum == mRowNum) {
                    double[] tData1 = ((RealColumnMatrix) aLHS).mData;
                    double[] tData2 = ((RealColumnMatrix) aRHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = tData1[i] % tData2[i];
                    return rMatrix;
                }
                return super.ebeMod(aLHS, aRHS);
            }
            
            @Override public RealColumnMatrix mapAdd(IMatrixGetter<? extends Number> aLHS, Number aRHS) {
                final double rhs = aRHS.doubleValue();
                if (aLHS instanceof RealColumnMatrix && ((RealColumnMatrix)aLHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aLHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = tData[i] + rhs;
                    return rMatrix;
                }
                return mapDo(aLHS, lhs -> (lhs + rhs));
            }
            @Override public RealColumnMatrix mapMinus(IMatrixGetter<? extends Number> aLHS, Number aRHS) {
                final double rhs = aRHS.doubleValue();
                if (aLHS instanceof RealColumnMatrix && ((RealColumnMatrix)aLHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aLHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = tData[i] - rhs;
                    return rMatrix;
                }
                return mapDo(aLHS, lhs -> (lhs - rhs));
            }
            @Override public RealColumnMatrix mapLMinus(IMatrixGetter<? extends Number> aLHS, Number aRHS) {
                final double rhs = aRHS.doubleValue();
                if (aLHS instanceof RealColumnMatrix && ((RealColumnMatrix)aLHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aLHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = rhs - tData[i];
                    return rMatrix;
                }
                return mapDo(aLHS, lhs -> (rhs - lhs));
            }
            @Override public RealColumnMatrix mapMultiply(IMatrixGetter<? extends Number> aLHS, Number aRHS) {
                final double rhs = aRHS.doubleValue();
                if (aLHS instanceof RealColumnMatrix && ((RealColumnMatrix)aLHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aLHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = tData[i] * rhs;
                    return rMatrix;
                }
                return mapDo(aLHS, lhs -> (lhs * rhs));
            }
            @Override public RealColumnMatrix mapDivide(IMatrixGetter<? extends Number> aLHS, Number aRHS) {
                final double rhs = aRHS.doubleValue();
                if (aLHS instanceof RealColumnMatrix && ((RealColumnMatrix)aLHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aLHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = tData[i] / rhs;
                    return rMatrix;
                }
                return mapDo(aLHS, lhs -> (lhs / rhs));
            }
            @Override public RealColumnMatrix mapLDivide(IMatrixGetter<? extends Number> aLHS, Number aRHS) {
                final double rhs = aRHS.doubleValue();
                if (aLHS instanceof RealColumnMatrix && ((RealColumnMatrix)aLHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aLHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = rhs / tData[i];
                    return rMatrix;
                }
                return mapDo(aLHS, lhs -> (rhs / lhs));
            }
            @Override public RealColumnMatrix mapMod(IMatrixGetter<? extends Number> aLHS, Number aRHS) {
                final double rhs = aRHS.doubleValue();
                if (aLHS instanceof RealColumnMatrix && ((RealColumnMatrix)aLHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aLHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = tData[i] % rhs;
                    return rMatrix;
                }
                return mapDo(aLHS, lhs -> (lhs % rhs));
            }
            @Override public RealColumnMatrix mapLMod(IMatrixGetter<? extends Number> aLHS, final Number aRHS) {
                final double rhs = aRHS.doubleValue();
                if (aLHS instanceof RealColumnMatrix && ((RealColumnMatrix)aLHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aLHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = rhs % tData[i];
                    return rMatrix;
                }
                return mapDo(aLHS, lhs -> (rhs % lhs));
            }
            
            // TODO else 由于已经不能做 SIMD，为了减少重复代码应该直接用 super，需要测试一下性能损失
            @Override public void ebeAdd2this(IMatrixGetter<? extends Number> aRHS) {
                if (aRHS instanceof RealColumnMatrix && ((RealColumnMatrix)aRHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aRHS).mData;
                    for (int i = 0; i < mData.length; ++i) mData[i] += tData[i];
                } else {
                    int tRowNum = rowNumber();
                    int tColNum = columnNumber();
                    int i = 0;
                    for (int col = 0; col < tColNum; ++col) for (int row = 0; row < tRowNum; ++row) {
                        mData[i] += aRHS.get(row, col).doubleValue();
                        ++i;
                    }
                }
            }
            @Override public void ebeMinus2this(IMatrixGetter<? extends Number> aRHS) {
                if (aRHS instanceof RealColumnMatrix && ((RealColumnMatrix)aRHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aRHS).mData;
                    for (int i = 0; i < mData.length; ++i) mData[i] -= tData[i];
                } else {
                    int tRowNum = rowNumber();
                    int tColNum = columnNumber();
                    int i = 0;
                    for (int col = 0; col < tColNum; ++col) for (int row = 0; row < tRowNum; ++row) {
                        mData[i] -= aRHS.get(row, col).doubleValue();
                        ++i;
                    }
                }
            }
            @Override public void ebeLMinus2this(IMatrixGetter<? extends Number> aRHS) {
                if (aRHS instanceof RealColumnMatrix && ((RealColumnMatrix)aRHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aRHS).mData;
                    for (int i = 0; i < mData.length; ++i) mData[i] = tData[i] - mData[i];
                } else {
                    int tRowNum = rowNumber();
                    int tColNum = columnNumber();
                    int i = 0;
                    for (int col = 0; col < tColNum; ++col) for (int row = 0; row < tRowNum; ++row) {
                        mData[i] = aRHS.get(row, col).doubleValue() - mData[i];
                        ++i;
                    }
                }
            }
            @Override public void ebeMultiply2this(IMatrixGetter<? extends Number> aRHS) {
                if (aRHS instanceof RealColumnMatrix && ((RealColumnMatrix)aRHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aRHS).mData;
                    for (int i = 0; i < mData.length; ++i) mData[i] *= tData[i];
                } else {
                    int tRowNum = rowNumber();
                    int tColNum = columnNumber();
                    int i = 0;
                    for (int col = 0; col < tColNum; ++col) for (int row = 0; row < tRowNum; ++row) {
                        mData[i] *= aRHS.get(row, col).doubleValue();
                        ++i;
                    }
                }
            }
            @Override public void ebeDivide2this(IMatrixGetter<? extends Number> aRHS) {
                if (aRHS instanceof RealColumnMatrix && ((RealColumnMatrix)aRHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aRHS).mData;
                    for (int i = 0; i < mData.length; ++i) mData[i] /= tData[i];
                } else {
                    int tRowNum = rowNumber();
                    int tColNum = columnNumber();
                    int i = 0;
                    for (int col = 0; col < tColNum; ++col) for (int row = 0; row < tRowNum; ++row) {
                        mData[i] /= aRHS.get(row, col).doubleValue();
                        ++i;
                    }
                }
            }
            @Override public void ebeLDivide2this(IMatrixGetter<? extends Number> aRHS) {
                if (aRHS instanceof RealColumnMatrix && ((RealColumnMatrix)aRHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aRHS).mData;
                    for (int i = 0; i < mData.length; ++i) mData[i] = tData[i] / mData[i];
                } else {
                    int tRowNum = rowNumber();
                    int tColNum = columnNumber();
                    int i = 0;
                    for (int col = 0; col < tColNum; ++col) for (int row = 0; row < tRowNum; ++row) {
                        mData[i] = aRHS.get(row, col).doubleValue() / mData[i];
                        ++i;
                    }
                }
            }
            @Override public void ebeMod2this(IMatrixGetter<? extends Number> aRHS) {
                if (aRHS instanceof RealColumnMatrix && ((RealColumnMatrix)aRHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aRHS).mData;
                    for (int i = 0; i < mData.length; ++i) mData[i] %= tData[i];
                } else {
                    int tRowNum = rowNumber();
                    int tColNum = columnNumber();
                    int i = 0;
                    for (int col = 0; col < tColNum; ++col) for (int row = 0; row < tRowNum; ++row) {
                        mData[i] %= aRHS.get(row, col).doubleValue();
                        ++i;
                    }
                }
            }
            @Override public void ebeLMod2this(IMatrixGetter<? extends Number> aRHS) {
                if (aRHS instanceof RealColumnMatrix && ((RealColumnMatrix)aRHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aRHS).mData;
                    for (int i = 0; i < mData.length; ++i) mData[i] = tData[i] % mData[i];
                } else {
                    int tRowNum = rowNumber();
                    int tColNum = columnNumber();
                    int i = 0;
                    for (int col = 0; col < tColNum; ++col) for (int row = 0; row < tRowNum; ++row) {
                        mData[i] = aRHS.get(row, col).doubleValue() % mData[i];
                        ++i;
                    }
                }
            }
            
            @Override public void mapAdd2this       (Number aRHS) {final double rhs = aRHS.doubleValue(); for (int i = 0; i < mData.length; ++i) mData[i] += rhs;}
            @Override public void mapMinus2this     (Number aRHS) {final double rhs = aRHS.doubleValue(); for (int i = 0; i < mData.length; ++i) mData[i] -= rhs;}
            @Override public void mapLMinus2this    (Number aRHS) {final double rhs = aRHS.doubleValue(); for (int i = 0; i < mData.length; ++i) mData[i] = rhs - mData[i];}
            @Override public void mapMultiply2this  (Number aRHS) {final double rhs = aRHS.doubleValue(); for (int i = 0; i < mData.length; ++i) mData[i] *= rhs;}
            @Override public void mapDivide2this    (Number aRHS) {final double rhs = aRHS.doubleValue(); for (int i = 0; i < mData.length; ++i) mData[i] /= rhs;}
            @Override public void mapLDivide2this   (Number aRHS) {final double rhs = aRHS.doubleValue(); for (int i = 0; i < mData.length; ++i) mData[i] = rhs / mData[i];}
            @Override public void mapMod2this       (Number aRHS) {final double rhs = aRHS.doubleValue(); for (int i = 0; i < mData.length; ++i) mData[i] %= rhs;}
            @Override public void mapLMod2this      (Number aRHS) {final double rhs = aRHS.doubleValue(); for (int i = 0; i < mData.length; ++i) mData[i] = rhs % mData[i];}
            
            @Override public RealColumnMatrix ebeDo(IMatrixGetter<? extends Number> aLHS, IMatrixGetter<? extends Number> aRHS, IOperator2<Double> aOpt) {
                if (aLHS instanceof RealColumnMatrix && ((RealColumnMatrix)aLHS).mRowNum == mRowNum) {
                    double[] tData1 = ((RealColumnMatrix)aLHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    if (aRHS instanceof RealColumnMatrix && ((RealColumnMatrix)aRHS).mRowNum == mRowNum) {
                        double[] tData2 = ((RealColumnMatrix)aRHS).mData;
                        int tLen = rMatrix.mData.length;
                        for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = aOpt.cal(tData1[i], tData2[i]);
                    } else {
                        int tRowNum = rowNumber();
                        int tColNum = columnNumber();
                        int i = 0;
                        for (int col = 0; col < tColNum; ++col) for (int row = 0; row < tRowNum; ++row) {
                            rMatrix.mData[i] = aOpt.cal(tData1[i], aRHS.get(row, col).doubleValue());
                            ++i;
                        }
                    }
                    return rMatrix;
                } else {
                    if (aRHS instanceof RealColumnMatrix && ((RealColumnMatrix)aRHS).mRowNum == mRowNum) {
                        double[] tData2 = ((RealColumnMatrix)aRHS).mData;
                        RealColumnMatrix rMatrix = generatorMat().zeros();
                        int tRowNum = rowNumber();
                        int tColNum = columnNumber();
                        int i = 0;
                        for (int col = 0; col < tColNum; ++col) for (int row = 0; row < tRowNum; ++row) {
                            rMatrix.mData[i] = aOpt.cal(aLHS.get(row, col).doubleValue(), tData2[i]);
                            ++i;
                        }
                        return rMatrix;
                    } else {
                        return super.ebeDo(aLHS, aRHS, aOpt);
                    }
                }
            }
            @Override public RealColumnMatrix mapDo(IMatrixGetter<? extends Number> aLHS, IOperator1<Double> aOpt) {
                if (aLHS instanceof RealColumnMatrix && ((RealColumnMatrix)aLHS).mRowNum == mRowNum) {
                    double[] tData = ((RealColumnMatrix)aLHS).mData;
                    RealColumnMatrix rMatrix = generatorMat().zeros();
                    int tLen = rMatrix.mData.length;
                    for (int i = 0; i < tLen; ++i) rMatrix.mData[i] = aOpt.cal(tData[i]);
                    return rMatrix;
                } else {
                    return super.mapDo(aLHS, aOpt);
                }
            }
        };
    }
    
    
    /** Optimize stuffs，重写 same 接口专门优化拷贝部分 */
    @Override public IMatrixGenerator<RealColumnMatrix> generatorMat() {
        return new MatrixGenerator() {
            @Override public RealColumnMatrix same() {
                double[] rData = new double[mData.length];
                System.arraycopy(mData, 0, rData, 0, mData.length);
                return new RealColumnMatrix(mRowNum, mColNum, rData);
            }
        };
    }
    @Override public IVectorGenerator<RealVector> generatorVec() {
        return new VectorGenerator() {
            @Override public RealVector same() {
                double[] rData = new double[mData.length];
                System.arraycopy(mData, 0, rData, 0, mData.length);
                return new RealVector(rData);
            }
        };
    }
}
