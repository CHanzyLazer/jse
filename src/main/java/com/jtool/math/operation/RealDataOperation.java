package com.jtool.math.operation;


import com.jtool.code.IFatIterable;

/**
 * 对于实数（Double）相关运算的一般实现，默认实现没有做任何优化
 * @author liqa
 */
public abstract class RealDataOperation<R extends IFatIterable<? super T, Number, Double, Number>, T> extends AbstractDataOperation<R, T, Double, Number> {
    @SuppressWarnings("Convert2MethodRef")
    @Override protected void ebeAdd2Dest_       (T aLHS, T aRHS, R rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs + rhs));}
    @Override protected void ebeMinus2Dest_     (T aLHS, T aRHS, R rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs - rhs));}
    @Override protected void ebeMultiply2Dest_  (T aLHS, T aRHS, R rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs * rhs));}
    @Override protected void ebeDivide2Dest_    (T aLHS, T aRHS, R rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs / rhs));}
    @Override protected void ebeMod2Dest_       (T aLHS, T aRHS, R rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs % rhs));}
    
    @Override protected void mapAdd2Dest_       (T aLHS, final Double aRHS, R rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs + aRHS));}
    @Override protected void mapMinus2Dest_     (T aLHS, final Double aRHS, R rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs - aRHS));}
    @Override protected void mapLMinus2Dest_    (T aLHS, final Double aRHS, R rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (aRHS - lhs));}
    @Override protected void mapMultiply2Dest_  (T aLHS, final Double aRHS, R rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs * aRHS));}
    @Override protected void mapDivide2Dest_    (T aLHS, final Double aRHS, R rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs / aRHS));}
    @Override protected void mapLDivide2Dest_   (T aLHS, final Double aRHS, R rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (aRHS / lhs));}
    @Override protected void mapMod2Dest_       (T aLHS, final Double aRHS, R rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs % aRHS));}
    @Override protected void mapLMod2Dest_      (T aLHS, final Double aRHS, R rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (aRHS % lhs));}
    
    
    /** 此时由于已经没有专门优化，为了代码简洁这里不专门展开 lambda，减少重复代码 */
    @SuppressWarnings("Convert2MethodRef")
    @Override protected void ebeAdd2this_       (R rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs + rhs));}
    @Override protected void ebeMinus2this_     (R rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs - rhs));}
    @Override protected void ebeLMinus2this_    (R rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (rhs - lhs));}
    @Override protected void ebeMultiply2this_  (R rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs * rhs));}
    @Override protected void ebeDivide2this_    (R rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs / rhs));}
    @Override protected void ebeLDivide2this_   (R rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (rhs / lhs));}
    @Override protected void ebeMod2this_       (R rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs % rhs));}
    @Override protected void ebeLMod2this_      (R rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (rhs % lhs));}
    
    @Override protected void mapAdd2this_       (R rThis, final Double aRHS) {mapDo2this_(rThis, lhs -> (lhs + aRHS));}
    @Override protected void mapMinus2this_     (R rThis, final Double aRHS) {mapDo2this_(rThis, lhs -> (lhs - aRHS));}
    @Override protected void mapLMinus2this_    (R rThis, final Double aRHS) {mapDo2this_(rThis, lhs -> (aRHS - lhs));}
    @Override protected void mapMultiply2this_  (R rThis, final Double aRHS) {mapDo2this_(rThis, lhs -> (lhs * aRHS));}
    @Override protected void mapDivide2this_    (R rThis, final Double aRHS) {mapDo2this_(rThis, lhs -> (lhs / aRHS));}
    @Override protected void mapLDivide2this_   (R rThis, final Double aRHS) {mapDo2this_(rThis, lhs -> (aRHS / lhs));}
    @Override protected void mapMod2this_       (R rThis, final Double aRHS) {mapDo2this_(rThis, lhs -> (lhs % aRHS));}
    @Override protected void mapLMod2this_      (R rThis, final Double aRHS) {mapDo2this_(rThis, lhs -> (aRHS % lhs));}
    
    @Override final protected Double upcast_(Number aValue) {
        if (aValue instanceof Double) return (Double)aValue;
        return aValue.doubleValue();
    }
}
