package com.jtool.math;

import com.jtool.math.operator.IOperator2Full;

import java.util.concurrent.Callable;

/**
 * 任意的通用的数据数据结构生成器
 * @author liqa
 * @param <M> 生成的二维数据类型，一般为矩阵
 */
public interface IDataGenerator2<M> extends IDataGenerator<M> {
    M from(IOperator2Full<? extends Number, Integer, Integer> aOpt);
    
    M ones(int aRowNum, int aColNum);
    M zeros(int aRowNum, int aColNum);
    M from(int aRowNum, int aColNum, Callable<? extends Number> aCall);
    M from(int aRowNum, int aColNum, IOperator2Full<? extends Number, Integer, Integer> aOpt);
}
