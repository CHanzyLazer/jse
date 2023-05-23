package com.jtool.math;

import com.jtool.math.operator.IOperator1Full;

import java.util.concurrent.Callable;

/**
 * 任意的通用的数据数据结构生成器
 * @author liqa
 * @param <V> 生成的一维数据类型，一般为向量
 */
public interface IDataGenerator1<V> extends IDataGenerator<V> {
    V from(IOperator1Full<? extends Number, Integer> aOpt);
    
    V ones(int aSize);
    V zeros(int aSize);
    V from(int aSize, Callable<? extends Number> aCall);
    V from(int aSize, IOperator1Full<? extends Number, Integer> aOpt);
}
