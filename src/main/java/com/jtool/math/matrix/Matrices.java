package com.jtool.math.matrix;

import java.util.Collection;

/**
 * @author liqa
 * <p> 获取矩阵的类，默认获取 {@link ColumnMatrix} </p>
 */
public class Matrices {
    public static IMatrix ones(int aSize) {return ColumnMatrix.ones(aSize);}
    public static IMatrix ones(int aRowNum, int aColNum) {return ColumnMatrix.ones(aRowNum, aColNum);}
    public static IMatrix zeros(int aSize) {return ColumnMatrix.zeros(aSize);}
    public static IMatrix zeros(int aRowNum, int aColNum) {return ColumnMatrix.zeros(aRowNum, aColNum);}
    
    
    public static IMatrix from(int aSize, IMatrixGetter aMatrixGetter) {return from(aSize, aSize, aMatrixGetter);}
    public static IMatrix from(int aRowNum, int aColNum, IMatrixGetter aMatrixGetter) {
        IMatrix rMatrix = zeros(aRowNum, aColNum);
        rMatrix.fill(aMatrixGetter);
        return rMatrix;
    }
    public static IMatrix from(IMatrixFull<?, ?> aMatrix) {
        if (aMatrix instanceof ColumnMatrix) {
            return ((ColumnMatrix) aMatrix).generator().same();
        } else {
            IMatrix rMatrix = zeros(aMatrix.rowNumber(), aMatrix.columnNumber());
            rMatrix.fill(aMatrix);
            return rMatrix;
        }
    }
    
    public static IMatrix from(int aSize, Iterable<? extends Iterable<? extends Number>> aRows) {return from(aSize, aSize, aRows);}
    public static IMatrix from(int aRowNum, int aColNum, Iterable<? extends Iterable<? extends Number>> aRows) {
        IMatrix rMatrix = zeros(aRowNum, aColNum);
        rMatrix.fill(aRows);
        return rMatrix;
    }
    public static IMatrix from(Collection<? extends Collection<? extends Number>> aRows) {
        int tRowNum = aRows.size();
        int tColNum = aRows.iterator().next().size();
        IMatrix rMatrix = zeros(tRowNum, tColNum);
        rMatrix.fill(aRows);
        return rMatrix;
    }
}
