package jtool.parallel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.util.ArrayDeque;
import java.util.Deque;

import static jtool.code.CS.DEFAULT_CACHE_SIZE;

/**
 * 用于作为缓存的对象池，会在内存不足时自动回收缓存
 * <p>
 * 此类线程安全，包括多个线程同时访问同一个实例
 * <p>
 * 注意为了实现简洁和性能，此类对于 return 相同对象不进行检测
 * @author liqa
 */
public final class ObjectCachePool<T> implements IObjectPool<T> {
    private final Deque<SoftReference<T>> mCache;
    private final int mSize;
    
    public ObjectCachePool(int aSize) {
        aSize = Math.max(1, aSize);
        mSize = aSize;
        mCache = new ArrayDeque<>(aSize+1);
    }
    public ObjectCachePool() {this(DEFAULT_CACHE_SIZE);}
    
    
    @Override public @Nullable T getObject() {
        SoftReference<T> tObj;
        synchronized (this) {tObj = mCache.pollLast();}
        if (tObj == null) return null;
        return tObj.get();
    }
    @Override public void returnObject(@NotNull T aObject) {
        synchronized (this) {
            mCache.addLast(new SoftReference<>(aObject));
            if (mCache.size() > mSize) mCache.removeFirst();
        }
    }
}
