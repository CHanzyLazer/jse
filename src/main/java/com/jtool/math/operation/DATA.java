package com.jtool.math.operation;

import com.jtool.code.*;
import com.jtool.code.operator.IDoubleOperator1;
import com.jtool.code.operator.IDoubleOperator2;

import java.util.Iterator;

/**
 * 对于运算操作的一般实现，主要用于减少重复代码；
 * 输出仅支持 {@link IHasLotIterator} 而不是 {@link IFatIterable}，
 * 表明这个运算器仅用于计算内部的一些数据结构
 * @author liqa
 */
public class DATA {
    private DATA() {}
    
    
    /** add, minus, multiply, divide stuffs */
    @SuppressWarnings("Convert2MethodRef")
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebePlus2Dest_     (T aLHS, T aRHS, RS rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs + rhs));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeMinus2Dest_    (T aLHS, T aRHS, RS rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs - rhs));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeMultiply2Dest_ (T aLHS, T aRHS, RS rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs * rhs));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeDiv2Dest_      (T aLHS, T aRHS, RS rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs / rhs));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeMod2Dest_      (T aLHS, T aRHS, RS rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs % rhs));}
    
    public static <T, RS extends IHasLotIterator<? super T, Double>> void mapPlus2Dest_     (T aLHS, double aRHS, RS rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs + aRHS));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void mapMinus2Dest_    (T aLHS, double aRHS, RS rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs - aRHS));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void mapLMinus2Dest_   (T aLHS, double aRHS, RS rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (aRHS - lhs));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void mapMultiply2Dest_ (T aLHS, double aRHS, RS rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs * aRHS));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void mapDiv2Dest_      (T aLHS, double aRHS, RS rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs / aRHS));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void mapLDiv2Dest_     (T aLHS, double aRHS, RS rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (aRHS / lhs));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void mapMod2Dest_      (T aLHS, double aRHS, RS rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs % aRHS));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void mapLMod2Dest_     (T aLHS, double aRHS, RS rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (aRHS % lhs));}
    
    @SuppressWarnings("Convert2MethodRef")
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebePlus2this_     (RS rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs + rhs));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeMinus2this_    (RS rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs - rhs));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeLMinus2this_   (RS rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (rhs - lhs));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeMultiply2this_ (RS rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs * rhs));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeDiv2this_      (RS rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs / rhs));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeLDiv2this_     (RS rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (rhs / lhs));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeMod2this_      (RS rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs % rhs));}
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeLMod2this_     (RS rThis, T aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (rhs % lhs));}
    
    public static <RS extends IHasSetIterator<Double>> void mapPlus2this_     (RS rThis, double aRHS) {mapDo2this_(rThis, lhs -> (lhs + aRHS));}
    public static <RS extends IHasSetIterator<Double>> void mapMinus2this_    (RS rThis, double aRHS) {mapDo2this_(rThis, lhs -> (lhs - aRHS));}
    public static <RS extends IHasSetIterator<Double>> void mapLMinus2this_   (RS rThis, double aRHS) {mapDo2this_(rThis, lhs -> (aRHS - lhs));}
    public static <RS extends IHasSetIterator<Double>> void mapMultiply2this_ (RS rThis, double aRHS) {mapDo2this_(rThis, lhs -> (lhs * aRHS));}
    public static <RS extends IHasSetIterator<Double>> void mapDiv2this_      (RS rThis, double aRHS) {mapDo2this_(rThis, lhs -> (lhs / aRHS));}
    public static <RS extends IHasSetIterator<Double>> void mapLDiv2this_     (RS rThis, double aRHS) {mapDo2this_(rThis, lhs -> (aRHS / lhs));}
    public static <RS extends IHasSetIterator<Double>> void mapMod2this_      (RS rThis, double aRHS) {mapDo2this_(rThis, lhs -> (lhs % aRHS));}
    public static <RS extends IHasSetIterator<Double>> void mapLMod2this_     (RS rThis, double aRHS) {mapDo2this_(rThis, lhs -> (aRHS % lhs));}
    
    
    /** do stuff */
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeDo2Dest_(T aLHS, T aRHS, RS rDest, IDoubleOperator2 aOpt) {
        final ISetIterator<Double> si = rDest.setIterator();
        final Iterator<Double> li = rDest.iteratorOf(aLHS);
        final Iterator<Double> ri = rDest.iteratorOf(aRHS);
        while (si.hasNext()) si.nextAndSet(aOpt.cal(li.next(), ri.next()));
    }
    public static <T, RS extends IHasLotIterator<? super T, Double>> void mapDo2Dest_(T aLHS, RS rDest, IDoubleOperator1 aOpt) {
        final ISetIterator<Double> si = rDest.setIterator();
        final Iterator<Double> li = rDest.iteratorOf(aLHS);
        while (si.hasNext()) si.nextAndSet(aOpt.cal(li.next()));
    }
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeDo2this_(RS rThis, T aRHS, IDoubleOperator2 aOpt) {
        final ISetIterator<Double> si = rThis.setIterator();
        final Iterator<Double> ri = rThis.iteratorOf(aRHS);
        while (si.hasNext()) si.set(aOpt.cal(si.next(), ri.next()));
    }
    public static <RS extends IHasSetIterator<Double>> void mapDo2this_(RS rThis, IDoubleOperator1 aOpt) {
        final ISetIterator<Double> si = rThis.setIterator();
        while (si.hasNext()) si.set(aOpt.cal(si.next()));
    }
    
    public static <RS extends IHasSetIterator<Double>> void mapFill2this_(RS rThis, double aRHS) {
        final ISetIterator<Double> si = rThis.setIterator();
        while (si.hasNext()) si.nextAndSet(aRHS);
    }
    public static <T, RS extends IHasLotIterator<? super T, Double>> void ebeFill2this_(RS rThis, T aRHS) {
        final ISetIterator<Double> si = rThis.setIterator();
        final Iterator<Double> ri = rThis.iteratorOf(aRHS);
        while (si.hasNext()) si.nextAndSet(ri.next());
    }
    
    
    /** stat stuff */
    public static <RS extends IHasIterator<Double>> double sumOfThis_(RS tThis) {
        double rSum = 0.0;
        final Iterator<Double> it = tThis.iterator();
        while (it.hasNext()) rSum += it.next();
        return rSum;
    }
    public static <RS extends IHasIterator<Double>> double meanOfThis_(RS tThis) {
        double rSum = 0.0;
        double tNum = 0.0;
        final Iterator<Double> it = tThis.iterator();
        while (it.hasNext()) {
            rSum += it.next();
            ++tNum;
        }
        return rSum / tNum;
    }
    public static <RS extends IHasIterator<Double>> double productOfThis_(RS tThis) {
        double rProduct = 1.0;
        final Iterator<Double> it = tThis.iterator();
        while (it.hasNext()) rProduct *= it.next();
        return rProduct;
    }
    public static <RS extends IHasIterator<Double>> double maxOfThis_(RS tThis) {
        double rMax = Double.NEGATIVE_INFINITY;
        final Iterator<Double> it = tThis.iterator();
        while (it.hasNext()) {
            double tValue = it.next();
            if (tValue > rMax) rMax = tValue;
        }
        return rMax;
    }
    public static <RS extends IHasIterator<Double>> double minOfThis_(RS tThis) {
        double rMin = Double.POSITIVE_INFINITY;
        final Iterator<Double> it = tThis.iterator();
        while (it.hasNext()) {
            double tValue = it.next();
            if (tValue < rMin) rMin = tValue;
        }
        return rMin;
    }
}
