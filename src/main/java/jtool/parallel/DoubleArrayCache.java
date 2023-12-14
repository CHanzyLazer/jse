package jtool.parallel;

import jtool.code.functional.IConsumer2;
import jtool.code.functional.IOperator1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static jtool.code.CS.*;

/**
 * 专门针对 {@code double[]} 的全局线程独立缓存，
 * 返回大于等于要求长度的 {@code double[]}
 * <p>
 * 会在内存不足时自动回收缓存
 * @author liqa
 */
public class DoubleArrayCache {
    private DoubleArrayCache() {}
    
    private static final ThreadLocal<NavigableMap<Integer, IObjectPool<double[]>>> CACHE = ThreadLocal.withInitial(TreeMap::new);
    
    /**
     * 归还数组，会根据数组长度自动选择缓存存放的位置
     * @param aArray 需要归还的数组
     * @author liqa
     */
    public static void returnArray(double @NotNull[] aArray) {
        if (NO_CACHE) return;
        final int tSizeKey = aArray.length;
        if (tSizeKey == 0) return;
        CACHE.get().computeIfAbsent(tSizeKey, key -> new ObjectCachePool<>()).returnObject(aArray);
    }
    
    
    /**
     * @param aMinSize 要求的最小长度
     * @return 大于等于要求长度的 {@code double[]}，并且所有成员都为 0.0
     * @author liqa
     */
    public static double @NotNull[] getZeros(int aMinSize) {
        if (aMinSize <= 0) return ZL_VEC;
        if (NO_CACHE) return new double[aMinSize];
        Map.Entry<Integer, IObjectPool<double[]>> tEntry = CACHE.get().ceilingEntry(aMinSize);
        if (tEntry == null || tEntry.getKey()>=aMinSize*2) return new double[aMinSize];
        IObjectPool<double[]> tPool = tEntry.getValue();
        // 如果是缓存值需要手动设置为 0.0
        double @Nullable[] tOut = tPool.getObject();
        if (tOut == null) return new double[aMinSize];
        Arrays.fill(tOut, 0.0);
        return tOut;
    }
    
    /**
     * @param aMinSize 要求的最小长度
     * @return 大于等于要求长度的 {@code double[]}，成员为任意值
     * @author liqa
     */
    public static double @NotNull[] getArray(int aMinSize) {
        if (aMinSize <= 0) return ZL_VEC;
        if (NO_CACHE) return new double[aMinSize];
        Map.Entry<Integer, IObjectPool<double[]>> tEntry = CACHE.get().ceilingEntry(aMinSize);
        if (tEntry == null || tEntry.getKey()>=aMinSize*2) return new double[aMinSize];
        IObjectPool<double[]> tPool = tEntry.getValue();
        if (tPool == null) return new double[aMinSize];
        double @Nullable[] tOut = tPool.getObject();
        if (tOut == null) return new double[aMinSize];
        return tOut;
    }
    
    
    
    /** 内部使用的批量操作的接口，约定所有数组都等长 */
    static void returnArrayFrom(double aMultiple, IOperator1<double @NotNull[], Integer> aArrayGetter) {
        if (NO_CACHE) return;
        if (aMultiple <= 0) return;
        double[] tFirst = aArrayGetter.cal(0);
        final int tSizeKey = tFirst.length;
        if (tSizeKey == 0) return;
        IObjectPool<double[]> tPool = CACHE.get().computeIfAbsent(tSizeKey, key -> new ObjectCachePool<>());
        tPool.returnObject(tFirst);
        for (int i = 1; i < aMultiple; ++i) tPool.returnObject(aArrayGetter.cal(i));
    }
    
    static void getZerosTo(int aMinSize, int aMultiple, IConsumer2<Integer, double @NotNull[]> aZerosConsumer) {
        if (aMultiple <= 0) return;
        if (aMinSize <= 0) {
            for (int i = 0; i < aMultiple; ++i) aZerosConsumer.run(i, ZL_VEC);
            return;
        }
        if (NO_CACHE) {
            for (int i = 0; i < aMultiple; ++i) aZerosConsumer.run(i, new double[aMinSize]);
            return;
        }
        Map.Entry<Integer, IObjectPool<double[]>> tEntry = CACHE.get().ceilingEntry(aMinSize);
        if (tEntry == null || tEntry.getKey()>=aMinSize*2) {
            for (int i = 0; i < aMultiple; ++i) aZerosConsumer.run(i, new double[aMinSize]);
            return;
        }
        IObjectPool<double[]> tPool = tEntry.getValue();
        // 如果是缓存值需要手动设置为 0.0
        boolean tNoCache = false;
        for (int i = 0; i < aMultiple; ++i) {
            if (tNoCache) {
                aZerosConsumer.run(i, new double[aMinSize]);
                continue;
            }
            double @Nullable[] subOut = tPool.getObject();
            if (subOut == null) {
                tNoCache = true;
                subOut = new double[aMinSize];
            } else {
                Arrays.fill(subOut, 0.0);
            }
            aZerosConsumer.run(i, subOut);
        }
    }
    
    static void getArrayTo(int aMinSize, int aMultiple, IConsumer2<Integer, double @NotNull[]> aArrayConsumer) {
        if (aMultiple <= 0) return;
        if (aMinSize <= 0) {
            for (int i = 0; i < aMultiple; ++i) aArrayConsumer.run(i, ZL_VEC);
            return;
        }
        if (NO_CACHE) {
            for (int i = 0; i < aMultiple; ++i) aArrayConsumer.run(i, new double[aMinSize]);
            return;
        }
        Map.Entry<Integer, IObjectPool<double[]>> tEntry = CACHE.get().ceilingEntry(aMinSize);
        if (tEntry == null || tEntry.getKey()>=aMinSize*2) {
            for (int i = 0; i < aMultiple; ++i) aArrayConsumer.run(i, new double[aMinSize]);
            return;
        }
        IObjectPool<double[]> tPool = tEntry.getValue();
        boolean tNoCache = false;
        for (int i = 0; i < aMultiple; ++i) {
            if (tNoCache) {
                aArrayConsumer.run(i, new double[aMinSize]);
                continue;
            }
            double @Nullable[] subOut = tPool.getObject();
            if (subOut == null) {
                tNoCache = true;
                subOut = new double[aMinSize];
            }
            aArrayConsumer.run(i, subOut);
        }
    }
}
