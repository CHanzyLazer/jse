package com.jtool.math.table;


import com.jtool.math.matrix.IMatrix;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;

/**
 * 通用的列表类，扩展 IMatrix 用于方便的获取 csv 的结果
 * @author liqa
 */
public interface IMatrixTable extends ITable, IMatrix<Double> {
    /** Groovy 的部分，重载一些运算符方便操作 */
    @VisibleForTesting default List<Double> getAt(String aHand) {return get(aHand);}
    @VisibleForTesting default void putAt(String aOldHand, String aNewHand) {setHand(aOldHand, aNewHand);}
}
