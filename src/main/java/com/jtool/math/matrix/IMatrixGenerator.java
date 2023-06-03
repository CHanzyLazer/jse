package com.jtool.math.matrix;


/**
 * 通用的任意矩阵的生成器
 * @author liqa
 */
public interface IMatrixGenerator {
    IMatrix ones();
    IMatrix zeros();
    IMatrix same();
    IMatrix from(IMatrixGetter aMatrixGetter);
    
    IMatrix ones(int aRowNum, int aColNum);
    IMatrix zeros(int aRowNum, int aColNum);
    IMatrix from(int aRowNum, int aColNum, IMatrixGetter aMatrixGetter);
}
