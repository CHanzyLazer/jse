package jtool.parallel;

import jtool.math.MathEX;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static jtool.code.CS.ZL_VEC;

/**
 * 专门针对 {@code double[]} 的全局线程独立缓存，
 * 返回大于等于要求长度的 {@code double[]}
 * <p>
 * 会在内存不足时自动回收缓存
 * @author liqa
 */
@ApiStatus.Experimental
public class DoubleArrayCache {
    private DoubleArrayCache() {}
    
    private static final ThreadLocal<Map<Integer, ObjectCachePool<double[]>>> CACHE = ThreadLocal.withInitial(TreeMap::new);
    
    
    /**
     * 归还数组，会根据数组长度自动选择缓存存放的位置
     * @param aArray 需要归还的数组
     * @author liqa
     */
    public static void returnArray(double @NotNull[] aArray) {
        int tSizeKey = MathEX.Code.floorPower2(aArray.length);
        if (tSizeKey <= 0) return;
        CACHE.get().computeIfAbsent(tSizeKey, key -> new ObjectCachePool<>()).returnObject(aArray);
    }
    
    /**
     * @param aMinSize 要求的最小长度
     * @return 大于等于要求长度的 {@code double[]}，并且所有成员都为 0.0
     * @author liqa
     */
    public static double @NotNull[] getZeros(int aMinSize) {
        if (aMinSize <= 0) return ZL_VEC;
        int tSize = MathEX.Code.ceilPower2(aMinSize);
        ObjectCachePool<double[]> tPool = CACHE.get().get(tSize);
        if (tPool == null) return new double[tSize];
        // 如果是缓存值需要手动设置为 0.0
        double @Nullable[] tOut = tPool.getObject();
        if (tOut == null) return new double[tSize];
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
        int tSize = MathEX.Code.ceilPower2(aMinSize);
        ObjectCachePool<double[]> tPool = CACHE.get().get(tSize);
        if (tPool == null) return new double[tSize];
        double @Nullable[] tOut = tPool.getObject();
        if (tOut == null) return new double[tSize];
        return tOut;
    }
}
