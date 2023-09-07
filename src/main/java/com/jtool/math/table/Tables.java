package com.jtool.math.table;

import com.jtool.math.matrix.IMatrix;
import com.jtool.math.matrix.IMatrixGetter;
import com.jtool.math.matrix.Matrices;

import java.util.Collection;

/**
 * @author liqa
 * <p> 获取列表的类，默认获取 {@link Table} </p>
 */
public class Tables {
    private Tables() {}
    
    
    public static ITable from(int aSize, IMatrixGetter aMatrixGetter, String... aHeads) {return from(aSize, aSize, aMatrixGetter, aHeads);}
    public static ITable from(int aRowNum, int aColNum, IMatrixGetter aMatrixGetter, String... aHeads) {
        IMatrix tData = Matrices.from(aRowNum, aColNum, aMatrixGetter);
        return (aHeads!=null && aHeads.length>0) ? new Table(aHeads, tData) : new Table(aColNum, tData);
    }
    public static ITable from(IMatrix aMatrix, String... aHeads) {
        IMatrix tData = Matrices.from(aMatrix);
        return (aHeads!=null && aHeads.length>0) ? new Table(aHeads, tData) : new Table(aMatrix.columnNumber(), tData);
    }
    
    public static ITable from(Collection<? extends Collection<? extends Number>> aRows, String... aHeads) {return fromRows(aRows, aHeads);}
    public static ITable fromRows(Collection<? extends Collection<? extends Number>> aRows, String... aHeads) {
        IMatrix tData = Matrices.fromRows(aRows);
        return (aHeads!=null && aHeads.length>0) ? new Table(aHeads, tData) : new Table(tData);
    }
    public static ITable fromCols(Collection<? extends Collection<? extends Number>> aCols, String... aHeads) {
        IMatrix tData = Matrices.fromCols(aCols);
        return (aHeads!=null && aHeads.length>0) ? new Table(aHeads, tData) : new Table(tData);
    }
}
