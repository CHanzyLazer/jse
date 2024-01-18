package jtool.math.vector;

import jtool.math.IDataShell;
import org.jetbrains.annotations.Nullable;

public abstract class IntArrayVector extends AbstractIntVector implements IDataShell<int[]> {
    protected int[] mData;
    protected IntArrayVector(int[] aData) {mData = aData;}
    
    /** DataShell stuffs */
    @Override public void setInternalData(int[] aData) {mData = aData;}
    @Override public int[] internalData() {return mData;}
    @Override public int internalDataSize() {return size();}
    
    protected class IntArrayVectorOperation_ extends IntArrayVectorOperation {
        @Override protected IntArrayVector thisVector_() {return IntArrayVector.this;}
    }
    
    /** 向量运算实现 */
    @Override public IIntVectorOperation operation() {return new IntArrayVectorOperation_();}
    
    /** Optimize stuffs，重写这些接口来加速批量填充过程 */
    @Override public void fill(int[] aData) {System.arraycopy(aData, 0, internalData(), internalDataShift(), internalDataSize());}
    
    /** stuff to override */
    public abstract IntArrayVector newShell();
    public abstract int @Nullable[] getIfHasSameOrderData(Object aObj);
}

