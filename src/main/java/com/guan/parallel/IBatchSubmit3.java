package com.guan.parallel;

import java.util.concurrent.Future;

/**
 * @author liqa
 * <p> 支持批量提交的 pool 使用的接口，规定使用 putSubmit 和 getSubmit 的逻辑来实现批量提交 </p>
 */
public interface IBatchSubmit3<R, T1, T2, T3> {
    /** 批量提交的组装 */
    void putSubmit(T1 aTaskArg1, T2 aTaskArg2, T3 aTaskArg3);
    /** 获取批量提交的组装 */
    Future<R> getSubmit();
}
