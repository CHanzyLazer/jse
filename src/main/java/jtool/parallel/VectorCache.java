package jtool.parallel;

import jtool.math.IDataShell;
import jtool.math.vector.IVector;
import jtool.math.vector.Vector;
import org.jetbrains.annotations.NotNull;

import static jtool.code.CS.NO_CACHE;

/**
 * 专门针对 {@link IVector} 的全局线程独立缓存，
 * 基于 {@link DoubleArrayCache} 实现
 * <p>
 * 会在内存不足时自动回收缓存
 * @author liqa
 */
public class VectorCache {
    private VectorCache() {}
    
    public static void returnVec(@NotNull IVector aVector) {
        if (NO_CACHE) return;
        if (aVector instanceof IDataShell) {
            Object tData = ((IDataShell<?>)aVector).getData();
            if (tData instanceof double[]) DoubleArrayCache.returnArray((double[])tData);
        }
    }
    public static @NotNull IVector getZeros(int aSize) {
        return new Vector(aSize, DoubleArrayCache.getZeros(aSize));
    }
    public static @NotNull IVector getVec(int aSize) {
        return new Vector(aSize, DoubleArrayCache.getArray(aSize));
    }
}
