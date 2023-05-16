package com.guan.parallel;

import java.util.concurrent.Future;

/**
 * @author liqa
 * <p> 支持批量提交的 pool 使用的接口，规定使用 putSubmit 和 getSubmit 的逻辑来实现批量提交 </p>
 */
public interface IBatchSubmit<R, T> {
    /** 批量提交的组装 */
    void putSubmit(T aTaskArg);
    /** 获取批量提交的组装 */
    Future<R> getSubmit();
    /** 单参数的还支持直接提交 Iterable */
    Future<R> batchSubmit(Iterable<T> aTaskArgs);
}
