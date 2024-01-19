package jtool.math.vector;

import jtool.math.IDataShell;
import org.jetbrains.annotations.Nullable;

public abstract class LongArrayVector extends AbstractLongVector implements IDataShell<long[]> {
    protected long[] mData;
    protected LongArrayVector(long[] aData) {mData = aData;}
    
    /** DataShell stuffs */
    @Override public void setInternalData(long[] aData) {mData = aData;}
    @Override public long[] internalData() {return mData;}
    @Override public int internalDataSize() {return size();}
    
    protected class LongArrayVectorOperation_ extends LongArrayVectorOperation {
        @Override protected LongArrayVector thisVector_() {return LongArrayVector.this;}
    }
    
    /** 向量运算实现 */
    @Override public ILongVectorOperation operation() {return new LongArrayVectorOperation_();}
    
    /** stuff to override */
    public abstract LongArrayVector newShell();
    public abstract long @Nullable[] getIfHasSameOrderData(Object aObj);
}

