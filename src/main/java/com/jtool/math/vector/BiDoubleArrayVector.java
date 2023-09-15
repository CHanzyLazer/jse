package com.jtool.math.vector;

import com.jtool.math.IDataShell;
import org.jetbrains.annotations.Nullable;

/**
 * @author liqa
 * <p> 内部存储 double[][] 的复向量，会加速相关的运算 </p>
 */
public abstract class BiDoubleArrayVector extends AbstractComplexVector implements IDataShell<double[][]> {
    protected double[][] mData;
    protected BiDoubleArrayVector(double[][] aData) {mData = aData;}
    
    /** DataShell stuffs */
    @Override public void setData2this(double[][] aData) {mData = aData;}
    @Override public double[][] getData() {return mData;}
    @Override public int dataSize() {return size();}
    
    
//    protected class BiDoubleArrayVectorOperation_ extends BiDoubleArrayVectorOperation {
//        @Override protected BiDoubleArrayVector thisVector_() {return BiDoubleArrayVector.this;}
//        @Override protected BiDoubleArrayVector newVector_(int aSize) {return newZeros(aSize);}
//    }
//
//    /** 向量运算实现 */
//    @Override public IVectorOperation operation() {return new DoubleArrayVectorOperation_();}
    
    /** Optimize stuffs，重写这些接口来加速批量填充过程 */
    @Override public void fill(double[] aData) {
        double[][] tData = getData();
        final int tShift = shiftSize();
        final int tSize = dataSize();
        System.arraycopy(aData, 0, tData[0], tShift, tSize);
        final int tEnd = tShift + tSize;
        final double[] tImagData = tData[1];
        for (int i = tShift; i < tEnd; ++i) tImagData[i] = 0.0;
    }
    
    /** Optimize stuffs，重写这些接口来加速获取 data 的过程 */
    @Override public double[][] data() {
        final int tShift = shiftSize();
        final int tSize = dataSize();
        double[][] rData = new double[2][tSize];
        double[][] tData = getData();
        System.arraycopy(tData[0], tShift, rData[0], 0, tSize);
        System.arraycopy(tData[1], tShift, rData[1], 0, tSize);
        return rData;
    }
    
    /** stuff to override */
    public abstract BiDoubleArrayVector newZeros(int aSize);
    public abstract BiDoubleArrayVector newShell();
    public abstract double @Nullable[][] getIfHasSameOrderData(Object aObj);
}
