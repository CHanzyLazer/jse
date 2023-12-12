package jtool.parallel;

import jtool.math.IDataShell;
import jtool.math.matrix.ColumnMatrix;
import jtool.math.matrix.IMatrix;
import org.jetbrains.annotations.NotNull;

/**
 * 专门针对 {@link IMatrix} 的全局线程独立缓存，
 * 基于 {@link DoubleArrayCache} 实现
 * <p>
 * 会在内存不足时自动回收缓存
 * @author liqa
 */
public class MatrixCache {
    private MatrixCache() {}
    
    public static void returnMat(@NotNull IMatrix aMatrix) {
        if (aMatrix instanceof IDataShell) {
            Object tData = ((IDataShell<?>)aMatrix).getData();
            if (tData instanceof double[]) {
                DoubleArrayCache.returnArray((double[])tData);
            } else
            if (tData instanceof double[][]) {
                for (double[] subData : (double[][])tData) {
                    DoubleArrayCache.returnArray(subData);
                }
            }
        }
    }
    public static @NotNull IMatrix getZeros(int aRowNum, int aColNum) {
        return new ColumnMatrix(aRowNum, aColNum, DoubleArrayCache.getZeros(aRowNum*aColNum));
    }
    public static @NotNull IMatrix getMat(int aRowNum, int aColNum) {
        return new ColumnMatrix(aRowNum, aColNum, DoubleArrayCache.getArray(aRowNum*aColNum));
    }
}
