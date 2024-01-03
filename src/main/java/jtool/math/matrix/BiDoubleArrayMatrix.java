package jtool.math.matrix;

import jtool.math.IDataShell;
import org.jetbrains.annotations.Nullable;

public abstract class BiDoubleArrayMatrix extends AbstractComplexMatrix implements IDataShell<double[][]> {
    protected double[][] mData;
    protected BiDoubleArrayMatrix(double[][] aData) {mData = aData;}
    
    /** DataShell stuffs */
    @Override public void setData2this(double[][] aData) {mData = aData;}
    @Override public double[][] getData() {return mData;}
    @Override public int dataSize() {return columnNumber()*rowNumber();}
    
    
    /** stuff to override */
    public abstract BiDoubleArrayMatrix newShell();
    public abstract double @Nullable[][] getIfHasSameOrderData(Object aObj);
}

