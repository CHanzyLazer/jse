package jtool.math.matrix;

import jtool.math.IDataShell;
import org.jetbrains.annotations.Nullable;

/**
 * @author liqa
 * <p> 内部存储 int[] 的矩阵，会加速相关的运算 </p>
 */
public abstract class IntArrayMatrix extends AbstractIntMatrix implements IDataShell<int[]> {
    protected int[] mData;
    protected IntArrayMatrix(int[] aData) {mData = aData;}
    
    /** DataShell stuffs */
    @Override public void setInternalData(int[] aData) {mData = aData;}
    @Override public int[] internalData() {return mData;}
    @Override public int internalDataSize() {return columnNumber()*rowNumber();}
    
    
    /** stuff to override */
    public abstract IntArrayMatrix newShell();
    public abstract int @Nullable[] getIfHasSameOrderData(Object aObj);
}
