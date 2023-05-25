package com.jtool.math;


/**
 * 任意的通用的数据和外壳的转换，用来方便进行不同数据类型的转换而不发生值拷贝，
 * 也用于运算来直接操作底层数据从而进行优化
 * @author liqa
 */
public interface IDataShell<T, D> {
    void setData2this(D aData);
    T newShell();
    D getData();
    D getIfHasSameOrderData(Object aObj);
}
