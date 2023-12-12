package jtool.parallel;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 对象池的通用接口
 * <p>
 * 此类要求线程安全，包括多个线程同时访问同一个实例
 * @author liqa
 */
@ApiStatus.Experimental
public interface IObjectPool<T> {
    /** 现在不预设返回值是否是 null */
    T getObject();
    void returnObject(@NotNull T aObject);
}
