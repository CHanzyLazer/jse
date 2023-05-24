package com.jtool.math;


import com.jtool.math.operator.IOperator1;
import com.jtool.math.operator.IOperator2;

/**
 * 对于实数（Double）相关运算的一般实现，为了扩展性不做过多优化
 * @author liqa
 */
public abstract class AbstractRealDataOperation<R extends T, T> implements IDataOperation<R, T, Double, Number> {
    @SuppressWarnings("Convert2MethodRef")
    @Override public R ebeAdd       (T aLHS, T aRHS) {return ebeDo(aLHS, aRHS, (lhs, rhs) -> (lhs + rhs));}
    @Override public R ebeMinus     (T aLHS, T aRHS) {return ebeDo(aLHS, aRHS, (lhs, rhs) -> (lhs - rhs));}
    @Override public R ebeMultiply  (T aLHS, T aRHS) {return ebeDo(aLHS, aRHS, (lhs, rhs) -> (lhs * rhs));}
    @Override public R ebeDivide    (T aLHS, T aRHS) {return ebeDo(aLHS, aRHS, (lhs, rhs) -> (lhs / rhs));}
    @Override public R ebeMod       (T aLHS, T aRHS) {return ebeDo(aLHS, aRHS, (lhs, rhs) -> (lhs % rhs));}
    
    @Override public R mapAdd       (T aLHS, final Number aRHS) {return mapDo(aLHS, lhs -> (lhs + aRHS.doubleValue()));}
    @Override public R mapMinus     (T aLHS, final Number aRHS) {return mapDo(aLHS, lhs -> (lhs - aRHS.doubleValue()));}
    @Override public R mapLMinus    (T aLHS, final Number aRHS) {return mapDo(aLHS, lhs -> (aRHS.doubleValue()) - lhs);}
    @Override public R mapMultiply  (T aLHS, final Number aRHS) {return mapDo(aLHS, lhs -> (lhs * aRHS.doubleValue()));}
    @Override public R mapDivide    (T aLHS, final Number aRHS) {return mapDo(aLHS, lhs -> (lhs / aRHS.doubleValue()));}
    @Override public R mapLDivide   (T aLHS, final Number aRHS) {return mapDo(aLHS, lhs -> (aRHS.doubleValue() / lhs));}
    @Override public R mapMod       (T aLHS, final Number aRHS) {return mapDo(aLHS, lhs -> (lhs % aRHS.doubleValue()));}
    @Override public R mapLMod      (T aLHS, final Number aRHS) {return mapDo(aLHS, lhs -> (aRHS.doubleValue() % lhs));}
    
    @SuppressWarnings("Convert2MethodRef")
    @Override public void ebeAdd2this       (T aRHS) {ebeDo2this(aRHS, (lhs, rhs) -> (lhs + rhs));}
    @Override public void ebeMinus2this     (T aRHS) {ebeDo2this(aRHS, (lhs, rhs) -> (lhs - rhs));}
    @Override public void ebeLMinus2this    (T aRHS) {ebeDo2this(aRHS, (lhs, rhs) -> (rhs - lhs));}
    @Override public void ebeMultiply2this  (T aRHS) {ebeDo2this(aRHS, (lhs, rhs) -> (lhs * rhs));}
    @Override public void ebeDivide2this    (T aRHS) {ebeDo2this(aRHS, (lhs, rhs) -> (lhs / rhs));}
    @Override public void ebeLDivide2this   (T aRHS) {ebeDo2this(aRHS, (lhs, rhs) -> (rhs / lhs));}
    @Override public void ebeMod2this       (T aRHS) {ebeDo2this(aRHS, (lhs, rhs) -> (lhs % rhs));}
    @Override public void ebeLMod2this      (T aRHS) {ebeDo2this(aRHS, (lhs, rhs) -> (rhs % lhs));}
    
    @Override public void mapAdd2this       (final Number aRHS) {mapDo2this(lhs -> (lhs + aRHS.doubleValue()));}
    @Override public void mapMinus2this     (final Number aRHS) {mapDo2this(lhs -> (lhs - aRHS.doubleValue()));}
    @Override public void mapLMinus2this    (final Number aRHS) {mapDo2this(lhs -> (aRHS.doubleValue()) - lhs);}
    @Override public void mapMultiply2this  (final Number aRHS) {mapDo2this(lhs -> (lhs * aRHS.doubleValue()));}
    @Override public void mapDivide2this    (final Number aRHS) {mapDo2this(lhs -> (lhs / aRHS.doubleValue()));}
    @Override public void mapLDivide2this   (final Number aRHS) {mapDo2this(lhs -> (aRHS.doubleValue() / lhs));}
    @Override public void mapMod2this       (final Number aRHS) {mapDo2this(lhs -> (lhs % aRHS.doubleValue()));}
    @Override public void mapLMod2this      (final Number aRHS) {mapDo2this(lhs -> (aRHS.doubleValue() % lhs));}
    
    
    /** stuff to override */
    public abstract R ebeDo(T aLHS, T aRHS, IOperator2<Double> aOpt);
    public abstract R mapDo(T aLHS, IOperator1<Double> aOpt);
    public abstract void ebeDo2this(T aRHS, IOperator2<Double> aOpt);
    public abstract void mapDo2this(IOperator1<Double> aOpt);
}
