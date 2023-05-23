package com.jtool.math.vector;

import java.util.Arrays;

/**
 * @author liqa
 * <p> 向量的一般实现 </p>
 */
public class RealVector extends AbstractVector<Double> {
    /** 提供默认的创建 */
    public static RealVector ones(int aSize) {
        double[] tData = new double[aSize];
        Arrays.fill(tData, 1.0);
        return new RealVector(tData);
    }
    public static RealVector zeros(int aSize) {return new RealVector(new double[aSize]);}
    
    
    private final double[] mData;
    public RealVector(double[] aData) {mData = aData;}
    
    /** IVector stuffs */
    @Override public Double get_(int aIdx) {return mData[aIdx];}
    @Override public void set_(int aIdx, Number aValue) {mData[aIdx] = aValue.doubleValue();}
    @Override public Double getAndSet_(int aIdx, Number aValue) {
        Double oValue = mData[aIdx];
        mData[aIdx] = aValue.doubleValue();
        return oValue;
    }
    @Override public int size() {return mData.length;}
}
