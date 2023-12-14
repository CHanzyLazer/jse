package jtool.parallel;

import jtool.code.collection.AbstractCollections;
import jtool.math.vector.DoubleArrayVector;
import jtool.math.vector.IVector;
import jtool.math.vector.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static jtool.code.CS.NO_CACHE;

/**
 * 专门针对 {@link IVector} 和 {@code List<IVector>} 的全局线程独立缓存，
 * 基于 {@link DoubleArrayCache} 实现
 * <p>
 * 会在内存不足时自动回收缓存
 * @author liqa
 */
public class VectorCache {
    private VectorCache() {}
    
    private static final IObjectPool<List<IVector>> CACHE = ThreadLocalObjectCachePool.withInitial(ArrayList::new);
    
    public static void returnVec(@NotNull IVector aVector) {
        if (NO_CACHE) return;
        DoubleArrayCache.returnArray(((DoubleArrayVector)aVector).getData());
    }
    public static void returnVec(final @NotNull List<@NotNull IVector> aVectorList) {
        if (NO_CACHE) return;
        // 这里不实际缓存 List<IVector>，而是直接统一归还内部值后缓存 clear 后的结构，这样实现会比较简单
        DoubleArrayCache.returnArrayFrom(aVectorList.size(), i -> ((DoubleArrayVector)aVectorList.get(i)).getData());
        aVectorList.clear();
        CACHE.returnObject(aVectorList);
    }
    
    
    public static @NotNull IVector getZeros(int aSize) {
        return new Vector(aSize, DoubleArrayCache.getZeros(aSize));
    }
    public static @NotNull List<IVector> getZeros(final int aSize, int aMultiple) {
        if (aMultiple <= 0) return AbstractCollections.zl();
        final List<IVector> rOut = NO_CACHE ? new ArrayList<>(aMultiple) : CACHE.getObject();
        DoubleArrayCache.getZerosTo(aSize, aMultiple, (i, arr) -> rOut.add(new Vector(aSize, arr)));
        return rOut;
    }
    public static @NotNull IVector getVec(int aSize) {
        return new Vector(aSize, DoubleArrayCache.getArray(aSize));
    }
    public static @NotNull List<IVector> getVec(final int aSize, int aMultiple) {
        if (aMultiple <= 0) return AbstractCollections.zl();
        final List<IVector> rOut = NO_CACHE ? new ArrayList<>(aMultiple) : CACHE.getObject();
        DoubleArrayCache.getArrayTo(aSize, aMultiple, (i, arr) -> rOut.add(new Vector(aSize, arr)));
        return rOut;
    }
}
