package com.jtool.math.matrix;

import java.util.Collection;

/**
 * @author liqa
 * <p> 获取矩阵的类，默认获取 {@link RealColumnMatrix} </p>
 */
public class Matrices {
    public static RealColumnMatrix ones(int aSize) {return RealColumnMatrix.ones(aSize);}
    public static RealColumnMatrix ones(int aRowNum, int aColNum) {return RealColumnMatrix.ones(aRowNum, aColNum);}
    public static RealColumnMatrix zeros(int aSize) {return RealColumnMatrix.zeros(aSize);}
    public static RealColumnMatrix zeros(int aRowNum, int aColNum) {return RealColumnMatrix.zeros(aRowNum, aColNum);}
    
    
    public static RealColumnMatrix from(int aSize, IMatrixGetter<? extends Number> aMatrixGetter) {return from(aSize, aSize, aMatrixGetter);}
    public static RealColumnMatrix from(int aRowNum, int aColNum, IMatrixGetter<? extends Number> aMatrixGetter) {
        RealColumnMatrix rMatrix = zeros(aRowNum, aColNum);
        rMatrix.fillWith(aMatrixGetter);
        return rMatrix;
    }
    public static RealColumnMatrix from(IMatrix<? extends Number> aMatrix) {
        if (aMatrix instanceof RealColumnMatrix) {
            return ((RealColumnMatrix) aMatrix).generator().same();
        } else {
            RealColumnMatrix rMatrix = zeros(aMatrix.rowNumber(), aMatrix.columnNumber());
            rMatrix.fillWith(aMatrix);
            return rMatrix;
        }
    }
    
    public static RealColumnMatrix from(int aSize, Iterable<? extends Iterable<? extends Number>> aRows) {return from(aSize, aSize, aRows);}
    public static RealColumnMatrix from(int aRowNum, int aColNum, Iterable<? extends Iterable<? extends Number>> aRows) {
        RealColumnMatrix rMatrix = zeros(aRowNum, aColNum);
        rMatrix.fill(aRows);
        return rMatrix;
    }
    public static RealColumnMatrix from(Collection<? extends Collection<? extends Number>> aRows) {
        int tRowNum = aRows.size();
        int tColNum = aRows.iterator().next().size();
        RealColumnMatrix rMatrix = zeros(tRowNum, tColNum);
        rMatrix.fill(aRows);
        return rMatrix;
    }
}
