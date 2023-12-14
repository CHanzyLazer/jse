package jtool.parallel;

import jtool.code.collection.AbstractCollections;
import jtool.math.vector.BiDoubleArrayVector;
import jtool.math.vector.ComplexVector;
import jtool.math.vector.IComplexVector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static jtool.code.CS.NO_CACHE;

/**
 * 专门针对 {@link IComplexVector} 和 {@code List<IComplexVector>} 的全局线程独立缓存，
 * 基于 {@link DoubleArrayCache} 实现
 * <p>
 * 会在内存不足时自动回收缓存
 * @author liqa
 */
public class ComplexVectorCache {
    private ComplexVectorCache() {}
    
    private static final IObjectPool<List<IComplexVector>> CACHE = ThreadLocalObjectCachePool.withInitial(ArrayList::new);
    
    public static void returnVec(@NotNull IComplexVector aComplexVector) {
        if (NO_CACHE) return;
        final double[][] tData = ((BiDoubleArrayVector)aComplexVector).getData();
        DoubleArrayCache.returnArrayFrom(2, i -> tData[1-i]);
    }
    public static void returnVec(final @NotNull List<@NotNull IComplexVector> aComplexVectorList) {
        if (NO_CACHE) return;
        // 这里不实际缓存 List<IComplexVector>，而是直接统一归还内部值后缓存 clear 后的结构，这样实现会比较简单
        final double[][] tArrayBuffer = {null};
        DoubleArrayCache.returnArrayFrom(aComplexVectorList.size()*2, i -> {
            double[] tArrayReal = tArrayBuffer[0];
            if (tArrayReal == null) {
                double[][] tData = ((BiDoubleArrayVector)aComplexVectorList.get(i/2)).getData();
                tArrayBuffer[0] = tData[0];
                return tData[1];
            } else {
                tArrayBuffer[0] = null;
                return tArrayReal;
            }
        });
        aComplexVectorList.clear();
        CACHE.returnObject(aComplexVectorList);
    }
    
    
    public static @NotNull IComplexVector getZeros(int aSize) {
        final double[][] rData = new double[2][];
        DoubleArrayCache.getZerosTo(aSize, 2, (i, arr) -> rData[i] = arr);
        return new ComplexVector(aSize, rData);
    }
    public static @NotNull List<IComplexVector> getZeros(final int aSize, int aMultiple) {
        if (aMultiple <= 0) return AbstractCollections.zl();
        final List<IComplexVector> rOut = NO_CACHE ? new ArrayList<>(aMultiple) : CACHE.getObject();
        final double[][] tArrayBuffer = {null};
        DoubleArrayCache.getZerosTo(aSize, aMultiple*2, (i, arr) -> {
            double[] tArrayReal = tArrayBuffer[0];
            if (tArrayReal == null) {
                tArrayBuffer[0] = arr;
            } else {
                rOut.add(new ComplexVector(aSize, new double[][]{tArrayReal, arr}));
                tArrayBuffer[0] = null;
            }
        });
        return rOut;
    }
    public static @NotNull IComplexVector getVec(int aSize) {
        final double[][] rData = new double[2][];
        DoubleArrayCache.getArrayTo(aSize, 2, (i, arr) -> rData[i] = arr);
        return new ComplexVector(aSize, rData);
    }
    public static @NotNull List<IComplexVector> getVec(final int aSize, int aMultiple) {
        if (aMultiple <= 0) return AbstractCollections.zl();
        final List<IComplexVector> rOut = NO_CACHE ? new ArrayList<>(aMultiple) : CACHE.getObject();
        final double[][] tArrayBuffer = {null};
        DoubleArrayCache.getArrayTo(aSize, aMultiple*2, (i, arr) -> {
            double[] tArrayReal = tArrayBuffer[0];
            if (tArrayReal == null) {
                tArrayBuffer[0] = arr;
            } else {
                rOut.add(new ComplexVector(aSize, new double[][]{tArrayReal, arr}));
                tArrayBuffer[0] = null;
            }
        });
        return rOut;
    }
}
