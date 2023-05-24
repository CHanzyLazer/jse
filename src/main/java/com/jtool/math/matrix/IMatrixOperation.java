package com.jtool.math.matrix;


import com.jtool.math.IDataOperation;

/**
 * 任意的矩阵的运算
 * @author liqa
 * @param <M> 返回矩阵类型
 * @param <T> 自身矩阵的元素类型
 */
public interface IMatrixOperation<M extends IMatrixGetter<? extends Number>, T extends Number> extends IDataOperation<M, IMatrixGetter<? extends Number>, T, Number> {
    /**/
}
