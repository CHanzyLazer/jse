package com.jtool.math.matrix;

import com.jtool.math.AbstractRealDataOperation;
import com.jtool.math.operator.IOperator1;
import com.jtool.math.operator.IOperator2;
import com.jtool.math.vector.IVector;


public abstract class AbstractRealMatrix<M extends IMatrix<Double>, V extends IVector<Double>> extends AbstractMatrixFull<Double, M, V> {
    
    /** 矩阵的运算操作，默认返回新的矩阵，refOperation 则会返回引用计算结果 */
    protected abstract class AbstractRealMatrixOperation<RM extends IMatrixGetter<? extends Number>> extends AbstractRealDataOperation<RM, IMatrixGetter<? extends Number>> implements IMatrixOperation<RM, Double> {
        /** 2this 的操作对于 Ref 和 一般的都是一样的 */
        @Override public final void ebeDo2this(IMatrixGetter<? extends Number> aRHS, IOperator2<Double> aOpt) {ebeDo2this_(aRHS, aOpt);}
        @Override public final void mapDo2this(IOperator1<Double> aOpt) {mapDo2this_(aOpt);}
    }
    
    protected class RealMatrixOperation extends AbstractRealMatrixOperation<M> {
        @Override public M ebeDo(final IMatrixGetter<? extends Number> aLHS, final IMatrixGetter<? extends Number> aRHS, final IOperator2<Double> aOpt) {return generatorMat().from((row, col) -> aOpt.cal(aLHS.get(row, col).doubleValue(), aRHS.get(row, col).doubleValue()));}
        @Override public M mapDo(final IMatrixGetter<? extends Number> aLHS, final IOperator1<Double> aOpt) {return generatorMat().from((row, col) -> aOpt.cal(aLHS.get(row, col).doubleValue()));}
    }
    
    protected class RealMatrixRefOperation extends AbstractRealMatrixOperation<IMatrixGetterFull<M, Double>> {
        @Override public IMatrixGetterFull<M, Double> ebeDo(final IMatrixGetter<? extends Number> aLHS, final IMatrixGetter<? extends Number> aRHS, final IOperator2<Double> aOpt) {return new MatrixGetterFull() {@Override public Double get(int aRow, int aCol) {return aOpt.cal(aLHS.get(aRow, aCol).doubleValue(), aRHS.get(aRow, aCol).doubleValue());}};}
        @Override public IMatrixGetterFull<M, Double> mapDo(final IMatrixGetter<? extends Number> aLHS, final IOperator1<Double> aOpt) {return new MatrixGetterFull() {@Override public Double get(int aRow, int aCol) {return aOpt.cal(aLHS.get(aRow, aCol).doubleValue());}};}
    }
    
    @Override public IMatrixOperation<M, Double> operation() {return new RealMatrixOperation();}
    @Override public IMatrixOperation<IMatrixGetterFull<M, Double>, Double> refOperation() {return new RealMatrixRefOperation();}
    
    /** override to optimize */
    protected void ebeDo2this_(final IMatrixGetter<? extends Number> aRHS, final IOperator2<Double> aOpt) {fillWith((row, col) -> aOpt.cal(get_(row, col), aRHS.get(row, col).doubleValue()));}
    protected void mapDo2this_(final IOperator1<Double> aOpt) {fillWith((row, col) -> aOpt.cal(get_(row, col)));}
}
