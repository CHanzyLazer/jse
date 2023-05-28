package com.jtool.math.matrix;

import com.jtool.math.operation.AbstractDataOperation;
import com.jtool.math.vector.IVectorFull;

/**
 * 一般的实矩阵运算的实现，默认没有做任何优化
 */
public abstract class AbstractMatrixOperation<M extends IMatrixFull<?, ?>, MS extends IMatrixFull<?, V>, V extends IVectorFull<?>> extends AbstractDataOperation<IMatrixFull<?, ?>, M, MS, IMatrixGetter> implements IMatrixOperation<M, V> {
    
    /** 矩阵的一些额外运算的默认实现 */
    @Override public V sumOfCols() {
        MS tThis = thisInstance_();
        
        int tColNum = tThis.columnNumber();
        V rVector = tThis.generatorVec().zeros(tColNum);
        for (int col = 0; col < tColNum; ++col) {
            rVector.set_(col, tThis.col(col).operation().sum());
        }
        return rVector;
    }
    @Override public V sumOfRows() {
        MS tThis = thisInstance_();
        
        int tRowNum = tThis.rowNumber();
        V rVector = tThis.generatorVec().zeros(tRowNum);
        for (int row = 0; row < tRowNum; ++row) {
            rVector.set_(row, tThis.row(row).operation().sum());
        }
        return rVector;
    }
    
    @Override public V meanOfCols() {
        MS tThis = thisInstance_();
        
        int tColNum = tThis.columnNumber();
        V rVector = tThis.generatorVec().zeros(tColNum);
        for (int col = 0; col < tColNum; ++col) {
            rVector.set_(col, tThis.col(col).operation().mean());
        }
        return rVector;
    }
    @Override public V meanOfRows() {
        MS tThis = thisInstance_();
        
        int tRowNum = tThis.rowNumber();
        V rVector = tThis.generatorVec().zeros(tRowNum);
        for (int row = 0; row < tRowNum; ++row) {
            rVector.set_(row, tThis.row(row).operation().mean());
        }
        return rVector;
    }
}
