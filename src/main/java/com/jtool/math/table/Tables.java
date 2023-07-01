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
    
    public static ITable from(int aSize, Iterable<? extends Iterable<? extends Number>> aRows, String... aHeads) {return fromRows(aSize, aRows, aHeads);}
    public static ITable from(int aRowNum, int aColNum, Iterable<? extends Iterable<? extends Number>> aRows, String... aHeads) {return fromRows(aRowNum, aColNum, aRows, aHeads);}
    public static ITable from(Collection<? extends Collection<? extends Number>> aRows, String... aHeads) {return fromRows(aRows, aHeads);}
    
    public static ITable fromRows(int aSize, Iterable<? extends Iterable<? extends Number>> aRows, String... aHeads) {return fromRows(aSize, aSize, aRows, aHeads);}
    public static ITable fromRows(int aRowNum, int aColNum, Iterable<? extends Iterable<? extends Number>> aRows, String... aHeads) {
        IMatrix tData = Matrices.fromRows(aRowNum, aColNum, aRows);
        return (aHeads!=null && aHeads.length>0) ? new Table(aHeads, tData) : new Table(aColNum, tData);
    }
    public static ITable fromRows(Collection<? extends Collection<? extends Number>> aRows, String... aHeads) {
        return fromRows(aRows.size(), aRows.iterator().next().size(), aRows, aHeads);
    }
    
    public static ITable fromCols(int aSize, Iterable<? extends Iterable<? extends Number>> aCols, String... aHeads) {return fromCols(aSize, aSize, aCols, aHeads);}
    public static ITable fromCols(int aRowNum, int aColNum, Iterable<? extends Iterable<? extends Number>> aCols, String... aHeads) {
        IMatrix tData = Matrices.fromCols(aRowNum, aColNum, aCols);
        return (aHeads!=null && aHeads.length>0) ? new Table(aHeads, tData) : new Table(aColNum, tData);
    }
    public static ITable fromCols(Collection<? extends Collection<? extends Number>> aCols, String... aHeads) {
        return fromCols(aCols.iterator().next().size(), aCols.size(), aCols, aHeads);
    }
}
