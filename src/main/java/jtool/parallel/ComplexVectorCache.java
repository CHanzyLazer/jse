package jtool.parallel;

import jtool.math.IDataShell;
import jtool.math.vector.ComplexVector;
import jtool.math.vector.IComplexVector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * 专门针对 {@link IComplexVector} 的全局线程独立缓存，
 * 基于 {@link DoubleArrayCache} 实现
 * <p>
 * 会在内存不足时自动回收缓存
 * @author liqa
 */
@ApiStatus.Experimental
public class ComplexVectorCache {
    private ComplexVectorCache() {}
    
    public static void returnVec(@NotNull IComplexVector aComplexVector) {
        if (aComplexVector instanceof IDataShell) {
            Object tData = ((IDataShell<?>)aComplexVector).getData();
            if (tData instanceof double[][]) {
                for (double[] subData : (double[][])tData) {
                    DoubleArrayCache.returnArray(subData);
                }
            } else
            if (tData instanceof double[]) {
                DoubleArrayCache.returnArray((double[])tData);
            }
        }
    }
    public static @NotNull IComplexVector getZeros(int aSize) {
        return new ComplexVector(aSize, new double[][]{DoubleArrayCache.getZeros(aSize), DoubleArrayCache.getZeros(aSize)});
    }
    public static @NotNull IComplexVector getVec(int aSize) {
        return new ComplexVector(aSize, new double[][]{DoubleArrayCache.getArray(aSize), DoubleArrayCache.getArray(aSize)});
    }
}
