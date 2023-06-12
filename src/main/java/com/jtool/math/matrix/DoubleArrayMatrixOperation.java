package com.jtool.math.matrix;

import com.jtool.code.operator.IDoubleOperator1;
import com.jtool.code.operator.IDoubleOperator2;
import com.jtool.math.operation.ARRAY;

/**
 * 对于内部含有 double[] 的向量的运算使用专门优化后的函数
 * @author liqa
 */
public abstract class DoubleArrayMatrixOperation extends AbstractMatrixOperation {
    /** 通用的一些运算 */
    @Override public IMatrix ebePlus        (IMatrixGetter aLHS, IMatrixGetter aRHS) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS, aRHS)); ARRAY.ebePlus2Dest_       (aLHS, aRHS, rMatrix); return rMatrix;}
    @Override public IMatrix ebeMinus       (IMatrixGetter aLHS, IMatrixGetter aRHS) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS, aRHS)); ARRAY.ebeMinus2Dest_      (aLHS, aRHS, rMatrix); return rMatrix;}
    @Override public IMatrix ebeMultiply    (IMatrixGetter aLHS, IMatrixGetter aRHS) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS, aRHS)); ARRAY.ebeMultiply2Dest_   (aLHS, aRHS, rMatrix); return rMatrix;}
    @Override public IMatrix ebeDiv         (IMatrixGetter aLHS, IMatrixGetter aRHS) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS, aRHS)); ARRAY.ebeDiv2Dest_        (aLHS, aRHS, rMatrix); return rMatrix;}
    @Override public IMatrix ebeMod         (IMatrixGetter aLHS, IMatrixGetter aRHS) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS, aRHS)); ARRAY.ebeMod2Dest_        (aLHS, aRHS, rMatrix); return rMatrix;}
    @Override public IMatrix ebeDo          (IMatrixGetter aLHS, IMatrixGetter aRHS, IDoubleOperator2 aOpt) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS, aRHS)); ARRAY.ebeDo2Dest_(aLHS, aRHS, rMatrix, aOpt); return rMatrix;}
    
    @Override public IMatrix mapPlus        (IMatrixGetter aLHS, double aRHS) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS)); ARRAY.mapPlus2Dest_        (aLHS, aRHS, rMatrix); return rMatrix;}
    @Override public IMatrix mapMinus       (IMatrixGetter aLHS, double aRHS) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS)); ARRAY.mapMinus2Dest_       (aLHS, aRHS, rMatrix); return rMatrix;}
    @Override public IMatrix mapLMinus      (IMatrixGetter aLHS, double aRHS) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS)); ARRAY.mapLMinus2Dest_      (aLHS, aRHS, rMatrix); return rMatrix;}
    @Override public IMatrix mapMultiply    (IMatrixGetter aLHS, double aRHS) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS)); ARRAY.mapMultiply2Dest_    (aLHS, aRHS, rMatrix); return rMatrix;}
    @Override public IMatrix mapDiv         (IMatrixGetter aLHS, double aRHS) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS)); ARRAY.mapDiv2Dest_         (aLHS, aRHS, rMatrix); return rMatrix;}
    @Override public IMatrix mapLDiv        (IMatrixGetter aLHS, double aRHS) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS)); ARRAY.mapLDiv2Dest_        (aLHS, aRHS, rMatrix); return rMatrix;}
    @Override public IMatrix mapMod         (IMatrixGetter aLHS, double aRHS) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS)); ARRAY.mapMod2Dest_         (aLHS, aRHS, rMatrix); return rMatrix;}
    @Override public IMatrix mapLMod        (IMatrixGetter aLHS, double aRHS) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS)); ARRAY.mapLMod2Dest_        (aLHS, aRHS, rMatrix); return rMatrix;}
    @Override public IMatrix mapDo          (IMatrixGetter aLHS, IDoubleOperator1 aOpt) {DoubleArrayMatrix rMatrix = newMatrix_(newMatrixSize_(aLHS)); ARRAY.mapDo2Dest_(aLHS, rMatrix, aOpt); return rMatrix;}
    
    @Override public void ebePlus2this      (IMatrixGetter aRHS) {ARRAY.ebePlus2this_       (thisMatrix_(), aRHS);}
    @Override public void ebeMinus2this     (IMatrixGetter aRHS) {ARRAY.ebeMinus2this_      (thisMatrix_(), aRHS);}
    @Override public void ebeLMinus2this    (IMatrixGetter aRHS) {ARRAY.ebeLMinus2this_     (thisMatrix_(), aRHS);}
    @Override public void ebeMultiply2this  (IMatrixGetter aRHS) {ARRAY.ebeMultiply2this_   (thisMatrix_(), aRHS);}
    @Override public void ebeDiv2this       (IMatrixGetter aRHS) {ARRAY.ebeDiv2this_        (thisMatrix_(), aRHS);}
    @Override public void ebeLDiv2this      (IMatrixGetter aRHS) {ARRAY.ebeLDiv2this_       (thisMatrix_(), aRHS);}
    @Override public void ebeMod2this       (IMatrixGetter aRHS) {ARRAY.ebeMod2this_        (thisMatrix_(), aRHS);}
    @Override public void ebeLMod2this      (IMatrixGetter aRHS) {ARRAY.ebeLMod2this_       (thisMatrix_(), aRHS);}
    @Override public void ebeDo2this        (IMatrixGetter aRHS, IDoubleOperator2 aOpt) {ARRAY.ebeDo2this_(thisMatrix_(), aRHS, aOpt);}
    
    @Override public void mapPlus2this      (double aRHS) {ARRAY.mapPlus2this_      (thisMatrix_(), aRHS);}
    @Override public void mapMinus2this     (double aRHS) {ARRAY.mapMinus2this_     (thisMatrix_(), aRHS);}
    @Override public void mapLMinus2this    (double aRHS) {ARRAY.mapLMinus2this_    (thisMatrix_(), aRHS);}
    @Override public void mapMultiply2this  (double aRHS) {ARRAY.mapMultiply2this_  (thisMatrix_(), aRHS);}
    @Override public void mapDiv2this       (double aRHS) {ARRAY.mapDiv2this_       (thisMatrix_(), aRHS);}
    @Override public void mapLDiv2this      (double aRHS) {ARRAY.mapLDiv2this_      (thisMatrix_(), aRHS);}
    @Override public void mapMod2this       (double aRHS) {ARRAY.mapMod2this_       (thisMatrix_(), aRHS);}
    @Override public void mapLMod2this      (double aRHS) {ARRAY.mapLMod2this_      (thisMatrix_(), aRHS);}
    @Override public void mapDo2this        (IDoubleOperator1 aOpt) {ARRAY.mapDo2this_(thisMatrix_(), aOpt);}
    
    @Override public void mapFill2this      (double aRHS) {ARRAY.mapFill2this_(thisMatrix_(), aRHS);}
    @Override public void ebeFill2this      (IMatrixGetter aRHS) {ARRAY.ebeFill2this_(thisMatrix_(), aRHS);}
    
    @Override public double sum () {return ARRAY.sumOfThis_ (thisMatrix_());}
    @Override public double mean() {return ARRAY.meanOfThis_(thisMatrix_());}
    @Override public double max () {return ARRAY.maxOfThis_ (thisMatrix_());}
    @Override public double min () {return ARRAY.minOfThis_ (thisMatrix_());}
    
    
    /** stuff to override */
    @Override protected abstract DoubleArrayMatrix thisMatrix_();
    @Override protected abstract DoubleArrayMatrix newMatrix_(IMatrix.ISize aSize);
}
