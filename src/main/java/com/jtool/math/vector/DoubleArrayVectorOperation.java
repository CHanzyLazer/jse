package com.jtool.math.vector;

import com.jtool.code.operator.IOperator1;
import com.jtool.code.operator.IOperator2;
import com.jtool.math.operation.ARRAY;

/**
 * 对于内部含有 double[] 的向量的运算使用专门优化后的函数
 * @author liqa
 */
public abstract class DoubleArrayVectorOperation extends AbstractVectorOperation {
    /** 通用的一些运算 */
    @Override public IVector ebePlus        (IVectorGetter aLHS, IVectorGetter aRHS) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS, aRHS)); ARRAY.ebePlus2Dest_       (aLHS, aRHS, rVector); return rVector;}
    @Override public IVector ebeMinus       (IVectorGetter aLHS, IVectorGetter aRHS) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS, aRHS)); ARRAY.ebeMinus2Dest_      (aLHS, aRHS, rVector); return rVector;}
    @Override public IVector ebeMultiply    (IVectorGetter aLHS, IVectorGetter aRHS) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS, aRHS)); ARRAY.ebeMultiply2Dest_   (aLHS, aRHS, rVector); return rVector;}
    @Override public IVector ebeDiv         (IVectorGetter aLHS, IVectorGetter aRHS) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS, aRHS)); ARRAY.ebeDiv2Dest_        (aLHS, aRHS, rVector); return rVector;}
    @Override public IVector ebeMod         (IVectorGetter aLHS, IVectorGetter aRHS) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS, aRHS)); ARRAY.ebeMod2Dest_        (aLHS, aRHS, rVector); return rVector;}
    @Override public IVector ebeDo          (IVectorGetter aLHS, IVectorGetter aRHS, IOperator2<Double> aOpt) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS, aRHS)); ARRAY.ebeDo2Dest_(aLHS, aRHS, rVector, aOpt); return rVector;}
    
    @Override public IVector mapPlus        (IVectorGetter aLHS, double aRHS) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS)); ARRAY.mapPlus2Dest_        (aLHS, aRHS, rVector); return rVector;}
    @Override public IVector mapMinus       (IVectorGetter aLHS, double aRHS) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS)); ARRAY.mapMinus2Dest_       (aLHS, aRHS, rVector); return rVector;}
    @Override public IVector mapLMinus      (IVectorGetter aLHS, double aRHS) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS)); ARRAY.mapLMinus2Dest_      (aLHS, aRHS, rVector); return rVector;}
    @Override public IVector mapMultiply    (IVectorGetter aLHS, double aRHS) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS)); ARRAY.mapMultiply2Dest_    (aLHS, aRHS, rVector); return rVector;}
    @Override public IVector mapDiv         (IVectorGetter aLHS, double aRHS) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS)); ARRAY.mapDiv2Dest_         (aLHS, aRHS, rVector); return rVector;}
    @Override public IVector mapLDiv        (IVectorGetter aLHS, double aRHS) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS)); ARRAY.mapLDiv2Dest_        (aLHS, aRHS, rVector); return rVector;}
    @Override public IVector mapMod         (IVectorGetter aLHS, double aRHS) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS)); ARRAY.mapMod2Dest_         (aLHS, aRHS, rVector); return rVector;}
    @Override public IVector mapLMod        (IVectorGetter aLHS, double aRHS) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS)); ARRAY.mapLMod2Dest_        (aLHS, aRHS, rVector); return rVector;}
    @Override public IVector mapDo          (IVectorGetter aLHS, IOperator1<Double> aOpt) {DoubleArrayVector rVector = newVector_(newVectorSize_(aLHS)); ARRAY.mapDo2Dest_(aLHS, rVector, aOpt); return rVector;}
    
    @Override public void ebePlus2this      (IVectorGetter aRHS) {ARRAY.ebePlus2this_       (thisVector_(), aRHS);}
    @Override public void ebeMinus2this     (IVectorGetter aRHS) {ARRAY.ebeMinus2this_      (thisVector_(), aRHS);}
    @Override public void ebeLMinus2this    (IVectorGetter aRHS) {ARRAY.ebeLMinus2this_     (thisVector_(), aRHS);}
    @Override public void ebeMultiply2this  (IVectorGetter aRHS) {ARRAY.ebeMultiply2this_   (thisVector_(), aRHS);}
    @Override public void ebeDiv2this       (IVectorGetter aRHS) {ARRAY.ebeDiv2this_        (thisVector_(), aRHS);}
    @Override public void ebeLDiv2this      (IVectorGetter aRHS) {ARRAY.ebeLDiv2this_       (thisVector_(), aRHS);}
    @Override public void ebeMod2this       (IVectorGetter aRHS) {ARRAY.ebeMod2this_        (thisVector_(), aRHS);}
    @Override public void ebeLMod2this      (IVectorGetter aRHS) {ARRAY.ebeLMod2this_       (thisVector_(), aRHS);}
    @Override public void ebeDo2this        (IVectorGetter aRHS, IOperator2<Double> aOpt) {ARRAY.ebeDo2this_(thisVector_(), aRHS, aOpt);}
    
    @Override public void mapPlus2this      (double aRHS) {ARRAY.mapPlus2this_      (thisVector_(), aRHS);}
    @Override public void mapMinus2this     (double aRHS) {ARRAY.mapMinus2this_     (thisVector_(), aRHS);}
    @Override public void mapLMinus2this    (double aRHS) {ARRAY.mapLMinus2this_    (thisVector_(), aRHS);}
    @Override public void mapMultiply2this  (double aRHS) {ARRAY.mapMultiply2this_  (thisVector_(), aRHS);}
    @Override public void mapDiv2this       (double aRHS) {ARRAY.mapDiv2this_       (thisVector_(), aRHS);}
    @Override public void mapLDiv2this      (double aRHS) {ARRAY.mapLDiv2this_      (thisVector_(), aRHS);}
    @Override public void mapMod2this       (double aRHS) {ARRAY.mapMod2this_       (thisVector_(), aRHS);}
    @Override public void mapLMod2this      (double aRHS) {ARRAY.mapLMod2this_      (thisVector_(), aRHS);}
    @Override public void mapDo2this        (IOperator1<Double> aOpt) {ARRAY.mapDo2this_(thisVector_(), aOpt);}
    
    @Override public void mapFill2this      (double aRHS) {ARRAY.mapFill2this_(thisVector_(), aRHS);}
    @Override public void ebeFill2this      (IVectorGetter aRHS) {ARRAY.ebeFill2this_(thisVector_(), aRHS);}
    
    @Override public double sum () {return ARRAY.sumOfThis_ (thisVector_());}
    @Override public double mean() {return ARRAY.meanOfThis_(thisVector_());}
    @Override public double max () {return ARRAY.maxOfThis_ (thisVector_());}
    @Override public double min () {return ARRAY.minOfThis_ (thisVector_());}
    
    
    /** stuff to override */
    @Override protected abstract DoubleArrayVector thisVector_();
    @Override protected abstract DoubleArrayVector newVector_(int aSize);
}
