package com.jtool.math.matrix;

import java.util.Collection;

/**
 * @author liqa
 * <p> 矩阵一般实现，定义为 {@link RealColumnMatrix} </p>
 */
public final class Matrix extends RealColumnMatrix {
    public Matrix(int aRowNum, int aColNum, double[] aData) {super(aRowNum, aColNum, aData);}
    public Matrix(double[][] aMat) {super(aMat);}
    public Matrix(Collection<? extends Collection<? extends Number>> aRows) {super(aRows);}
}
