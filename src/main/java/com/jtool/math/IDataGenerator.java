package com.jtool.math;


/**
 * 任意的通用的数据生成器
 * @author liqa
 */
public interface IDataGenerator<T> {
    T ones();
    T zeros();
    T same();
}
