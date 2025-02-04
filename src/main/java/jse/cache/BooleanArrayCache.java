package jse.cache;

import jse.code.collection.IListGetter;
import jse.code.collection.IListSetter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static jse.cache.ByteArrayCache.entryInvalid;
import static jse.code.CS.ZL_BOOL;
import static jse.code.Conf.NO_CACHE;

/**
 * 专门针对 {@code boolean[]} 的全局线程独立缓存，
 * 返回大于等于要求长度的 {@code boolean[]}
 * <p>
 * 会在内存不足时自动回收缓存
 * <p>
 * 注意归还线程和借用线程一致
 * @author liqa
 */
public class BooleanArrayCache {
    private BooleanArrayCache() {}
    
    private static final ThreadLocal<NavigableMap<Integer, IObjectPool<boolean[]>>> CACHE = ThreadLocal.withInitial(TreeMap::new);
    
    /**
     * 归还数组，会根据数组长度自动选择缓存存放的位置
     * @param aArray 需要归还的数组
     */
    public static void returnArray(boolean @NotNull[] aArray) {
        if (NO_CACHE) return;
        final int tSizeKey = aArray.length;
        if (tSizeKey == 0) return;
        CACHE.get().computeIfAbsent(tSizeKey, key -> new ObjectCachePool<>()).returnObject(aArray);
    }
    
    
    /**
     * 从缓存获取全零的数组
     * @param aMinSize 要求的最小长度
     * @return 大于等于要求长度的 {@code boolean[]}，并且所有成员都为 0
     */
    public static boolean @NotNull[] getZeros(int aMinSize) {
        if (aMinSize <= 0) return ZL_BOOL;
        if (NO_CACHE) return new boolean[aMinSize];
        Map.Entry<Integer, IObjectPool<boolean[]>> tEntry = CACHE.get().ceilingEntry(aMinSize);
        if (entryInvalid(tEntry, aMinSize)) return new boolean[aMinSize];
        IObjectPool<boolean[]> tPool = tEntry.getValue();
        // 如果是缓存值需要手动设置为 0
        boolean @Nullable[] tOut = tPool.getObject();
        if (tOut == null) return new boolean[aMinSize];
        Arrays.fill(tOut, false);
        return tOut;
    }
    
    /**
     * 从缓存获取一个数组，不保证内部为零
     * @param aMinSize 要求的最小长度
     * @return 大于等于要求长度的 {@code boolean[]}，成员为任意值
     */
    public static boolean @NotNull[] getArray(int aMinSize) {
        if (aMinSize <= 0) return ZL_BOOL;
        if (NO_CACHE) return new boolean[aMinSize];
        Map.Entry<Integer, IObjectPool<boolean[]>> tEntry = CACHE.get().ceilingEntry(aMinSize);
        if (entryInvalid(tEntry, aMinSize)) return new boolean[aMinSize];
        IObjectPool<boolean[]> tPool = tEntry.getValue();
        if (tPool == null) return new boolean[aMinSize];
        boolean @Nullable[] tOut = tPool.getObject();
        if (tOut == null) return new boolean[aMinSize];
        return tOut;
    }
    
    
    
    /**
     * {@link #returnArray(boolean[])} 的批量操作的接口，注意实际不保证数组等长
     * @param aMultiple 需要批量归还的数组数目
     * @param aArrayGetter 获取需要归还的数组的接口
     */
    public static void returnArrayFrom(int aMultiple, IListGetter<boolean @NotNull[]> aArrayGetter) {
        if (NO_CACHE) return;
        if (aMultiple <= 0) return;
        boolean[] tFirst = aArrayGetter.get(0);
        int tSizeKey = tFirst.length;
        if (tSizeKey == 0) return;
        IObjectPool<boolean[]> tPool = CACHE.get().computeIfAbsent(tSizeKey, key -> new ObjectCachePool<>());
        tPool.returnObject(tFirst);
        for (int i = 1; i < aMultiple; ++i) {
            boolean[] tData = aArrayGetter.get(i);
            int nSizeKey = tData.length;
            if (nSizeKey == 0) continue;
            if (nSizeKey < tSizeKey) {
                tSizeKey = nSizeKey;
                tPool = CACHE.get().computeIfAbsent(tSizeKey, key -> new ObjectCachePool<>());
            }
            tPool.returnObject(tData);
        }
    }
    
    /**
     * {@link #getZeros(int)} 的批量操作的接口，注意不保证获取的数组等长
     * @param aMinSize 需要的数组的最小长度
     * @param aMultiple 需要批量获取的数组数目
     * @param aZerosConsumer 需要获取数据的存储接口
     */
    public static void getZerosTo(int aMinSize, int aMultiple, IListSetter<boolean @NotNull[]> aZerosConsumer) {
        if (aMultiple <= 0) return;
        if (aMinSize <= 0) {
            for (int i = 0; i < aMultiple; ++i) aZerosConsumer.set(i, ZL_BOOL);
            return;
        }
        if (NO_CACHE) {
            for (int i = 0; i < aMultiple; ++i) aZerosConsumer.set(i, new boolean[aMinSize]);
            return;
        }
        Map.Entry<Integer, IObjectPool<boolean[]>> tEntry = CACHE.get().ceilingEntry(aMinSize);
        if (entryInvalid(tEntry, aMinSize)) {
            for (int i = 0; i < aMultiple; ++i) aZerosConsumer.set(i, new boolean[aMinSize]);
            return;
        }
        IObjectPool<boolean[]> tPool = tEntry.getValue();
        // 如果是缓存值需要手动设置为 0.0
        boolean tNoCache = false;
        for (int i = 0; i < aMultiple; ++i) {
            if (tNoCache) {
                aZerosConsumer.set(i, new boolean[aMinSize]);
                continue;
            }
            boolean @Nullable[] subOut = tPool.getObject();
            if (subOut == null) {
                tNoCache = true;
                subOut = new boolean[aMinSize];
            } else {
                Arrays.fill(subOut, false);
            }
            aZerosConsumer.set(i, subOut);
        }
    }
    
    /**
     * {@link #getArray(int)} 的批量操作的接口，注意不保证获取的数组等长
     * @param aMinSize 需要的数组的最小长度
     * @param aMultiple 需要批量获取的数组数目
     * @param aArrayConsumer 需要获取数据的存储接口
     */
    public static void getArrayTo(int aMinSize, int aMultiple, IListSetter<boolean @NotNull[]> aArrayConsumer) {
        if (aMultiple <= 0) return;
        if (aMinSize <= 0) {
            for (int i = 0; i < aMultiple; ++i) aArrayConsumer.set(i, ZL_BOOL);
            return;
        }
        if (NO_CACHE) {
            for (int i = 0; i < aMultiple; ++i) aArrayConsumer.set(i, new boolean[aMinSize]);
            return;
        }
        Map.Entry<Integer, IObjectPool<boolean[]>> tEntry = CACHE.get().ceilingEntry(aMinSize);
        if (entryInvalid(tEntry, aMinSize)) {
            for (int i = 0; i < aMultiple; ++i) aArrayConsumer.set(i, new boolean[aMinSize]);
            return;
        }
        IObjectPool<boolean[]> tPool = tEntry.getValue();
        boolean tNoCache = false;
        for (int i = 0; i < aMultiple; ++i) {
            if (tNoCache) {
                aArrayConsumer.set(i, new boolean[aMinSize]);
                continue;
            }
            boolean @Nullable[] subOut = tPool.getObject();
            if (subOut == null) {
                tNoCache = true;
                subOut = new boolean[aMinSize];
            }
            aArrayConsumer.set(i, subOut);
        }
    }
}
