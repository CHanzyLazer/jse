package com.jtool.parallel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 对象池的通用接口
 * <p>
 * 此类要求线程安全，包括多个线程同时访问同一个实例
 * @author liqa
 */
public interface IObjectPool<T> {
    /** 返回 null 代表获取失败 */
    @Nullable T getObject();
    void returnObject(@NotNull T aObject);
}
