package com.jtool.math.operation;

import com.jtool.code.iterator.IDoubleIterator;
import com.jtool.code.iterator.IDoubleSetIterator;
import com.jtool.code.iterator.IDoubleSetOnlyIterator;
import com.jtool.code.operator.IDoubleOperator1;
import com.jtool.code.operator.IDoubleOperator2;

/**
 * 对于运算操作的一般实现，主要用于减少重复代码；
 * 直接使用 {@link IDoubleIterator} 避免泛型的使用。
 * <p>
 * 由于传入都是迭代器，因此调用后输入的迭代器都会失效
 * @author liqa
 */
public class DATA {
    private DATA() {}
    
    
    /** add, minus, multiply, divide stuffs */
    @SuppressWarnings("Convert2MethodRef")
    public static void ebePlus2Dest_    (IDoubleIterator aLHS, IDoubleIterator aRHS, IDoubleSetOnlyIterator rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs + rhs));}
    public static void ebeMinus2Dest_   (IDoubleIterator aLHS, IDoubleIterator aRHS, IDoubleSetOnlyIterator rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs - rhs));}
    public static void ebeMultiply2Dest_(IDoubleIterator aLHS, IDoubleIterator aRHS, IDoubleSetOnlyIterator rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs * rhs));}
    public static void ebeDiv2Dest_     (IDoubleIterator aLHS, IDoubleIterator aRHS, IDoubleSetOnlyIterator rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs / rhs));}
    public static void ebeMod2Dest_     (IDoubleIterator aLHS, IDoubleIterator aRHS, IDoubleSetOnlyIterator rDest) {ebeDo2Dest_(aLHS, aRHS, rDest, (lhs, rhs) -> (lhs % rhs));}
    
    public static void mapPlus2Dest_    (IDoubleIterator aLHS, double aRHS, IDoubleSetOnlyIterator rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs + aRHS));}
    public static void mapMinus2Dest_   (IDoubleIterator aLHS, double aRHS, IDoubleSetOnlyIterator rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs - aRHS));}
    public static void mapLMinus2Dest_  (IDoubleIterator aLHS, double aRHS, IDoubleSetOnlyIterator rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (aRHS - lhs));}
    public static void mapMultiply2Dest_(IDoubleIterator aLHS, double aRHS, IDoubleSetOnlyIterator rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs * aRHS));}
    public static void mapDiv2Dest_     (IDoubleIterator aLHS, double aRHS, IDoubleSetOnlyIterator rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs / aRHS));}
    public static void mapLDiv2Dest_    (IDoubleIterator aLHS, double aRHS, IDoubleSetOnlyIterator rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (aRHS / lhs));}
    public static void mapMod2Dest_     (IDoubleIterator aLHS, double aRHS, IDoubleSetOnlyIterator rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (lhs % aRHS));}
    public static void mapLMod2Dest_    (IDoubleIterator aLHS, double aRHS, IDoubleSetOnlyIterator rDest) {mapDo2Dest_(aLHS, rDest, lhs -> (aRHS % lhs));}
    
    @SuppressWarnings("Convert2MethodRef")
    public static void ebePlus2this_    (IDoubleSetIterator rThis, IDoubleIterator aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs + rhs));}
    public static void ebeMinus2this_   (IDoubleSetIterator rThis, IDoubleIterator aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs - rhs));}
    public static void ebeLMinus2this_  (IDoubleSetIterator rThis, IDoubleIterator aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (rhs - lhs));}
    public static void ebeMultiply2this_(IDoubleSetIterator rThis, IDoubleIterator aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs * rhs));}
    public static void ebeDiv2this_     (IDoubleSetIterator rThis, IDoubleIterator aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs / rhs));}
    public static void ebeLDiv2this_    (IDoubleSetIterator rThis, IDoubleIterator aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (rhs / lhs));}
    public static void ebeMod2this_     (IDoubleSetIterator rThis, IDoubleIterator aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (lhs % rhs));}
    public static void ebeLMod2this_    (IDoubleSetIterator rThis, IDoubleIterator aRHS) {ebeDo2this_(rThis, aRHS, (lhs, rhs) -> (rhs % lhs));}
    
    public static void mapPlus2this_    (IDoubleSetIterator rThis, double aRHS) {mapDo2this_(rThis, lhs -> (lhs + aRHS));}
    public static void mapMinus2this_   (IDoubleSetIterator rThis, double aRHS) {mapDo2this_(rThis, lhs -> (lhs - aRHS));}
    public static void mapLMinus2this_  (IDoubleSetIterator rThis, double aRHS) {mapDo2this_(rThis, lhs -> (aRHS - lhs));}
    public static void mapMultiply2this_(IDoubleSetIterator rThis, double aRHS) {mapDo2this_(rThis, lhs -> (lhs * aRHS));}
    public static void mapDiv2this_     (IDoubleSetIterator rThis, double aRHS) {mapDo2this_(rThis, lhs -> (lhs / aRHS));}
    public static void mapLDiv2this_    (IDoubleSetIterator rThis, double aRHS) {mapDo2this_(rThis, lhs -> (aRHS / lhs));}
    public static void mapMod2this_     (IDoubleSetIterator rThis, double aRHS) {mapDo2this_(rThis, lhs -> (lhs % aRHS));}
    public static void mapLMod2this_    (IDoubleSetIterator rThis, double aRHS) {mapDo2this_(rThis, lhs -> (aRHS % lhs));}
    
    
    /** do stuff */
    public static void ebeDo2Dest_(IDoubleIterator aLHS, IDoubleIterator aRHS, IDoubleSetOnlyIterator rDest, IDoubleOperator2 aOpt) {
        while (rDest.hasNext()) rDest.nextAndSet(aOpt.cal(aLHS.next(), aRHS.next()));
    }
    public static void mapDo2Dest_(IDoubleIterator aLHS, IDoubleSetOnlyIterator rDest, IDoubleOperator1 aOpt) {
        while (rDest.hasNext()) rDest.nextAndSet(aOpt.cal(aLHS.next()));
    }
    public static void ebeDo2this_(IDoubleSetIterator rThis, IDoubleIterator aRHS, IDoubleOperator2 aOpt) {
        while (rThis.hasNext()) rThis.set(aOpt.cal(rThis.next(), aRHS.next()));
    }
    public static void mapDo2this_(IDoubleSetIterator rThis, IDoubleOperator1 aOpt) {
        while (rThis.hasNext()) rThis.set(aOpt.cal(rThis.next()));
    }
    
    public static void mapFill2this_(IDoubleSetOnlyIterator rThis, double aRHS) {
        while (rThis.hasNext()) rThis.nextAndSet(aRHS);
    }
    public static void ebeFill2this_(IDoubleSetOnlyIterator rThis, IDoubleIterator aRHS) {
        while (rThis.hasNext()) rThis.nextAndSet(aRHS.next());
    }
    
    
    /** stat stuff */
    public static double sumOfThis_(IDoubleIterator tThis) {
        double rSum = 0.0;
        while (tThis.hasNext()) rSum += tThis.next();
        return rSum;
    }
    public static double meanOfThis_(IDoubleIterator tThis) {
        double rSum = 0.0;
        double tNum = 0.0;
        while (tThis.hasNext()) {
            rSum += tThis.next();
            ++tNum;
        }
        return rSum / tNum;
    }
    public static double prodOfThis_(IDoubleIterator tThis) {
        double rProd = 1.0;
        while (tThis.hasNext()) rProd *= tThis.next();
        return rProd;
    }
    public static double maxOfThis_(IDoubleIterator tThis) {
        double rMax = Double.NaN;
        while (tThis.hasNext()) {
            double tValue = tThis.next();
            if (Double.isNaN(rMax) || tValue > rMax) rMax = tValue;
        }
        return rMax;
    }
    public static double minOfThis_(IDoubleIterator tThis) {
        double rMin = Double.NaN;
        while (tThis.hasNext()) {
            double tValue = tThis.next();
            if (Double.isNaN(rMin) || tValue < rMin) rMin = tValue;
        }
        return rMin;
    }
    public static double statOfThis_(IDoubleIterator tThis, IDoubleOperator2 aOpt) {
        double rStat = Double.NaN;
        while (tThis.hasNext()) rStat = aOpt.cal(rStat, tThis.next());
        return rStat;
    }
    
    public static void cumsum2Dest_(IDoubleIterator tThis, IDoubleSetOnlyIterator rDest) {
        double rSum = 0.0;
        while (tThis.hasNext()) {
            rSum += tThis.next();
            rDest.nextAndSet(rSum);
        }
    }
    public static void cummean2Dest_(IDoubleIterator tThis, IDoubleSetOnlyIterator rDest) {
        double rSum = 0.0;
        double tNum = 0.0;
        while (tThis.hasNext()) {
            rSum += tThis.next();
            ++tNum;
            rDest.nextAndSet(rSum / tNum);
        }
    }
    public static void cumprod2Dest_(IDoubleIterator tThis, IDoubleSetOnlyIterator rDest) {
        double rProd = 1.0;
        while (tThis.hasNext()) {
            rProd *= tThis.next();
            rDest.nextAndSet(rProd);
        }
    }
    public static void cummax2Dest_(IDoubleIterator tThis, IDoubleSetOnlyIterator rDest) {
        double rMax = Double.NaN;
        while (tThis.hasNext()) {
            double tValue = tThis.next();
            if (Double.isNaN(rMax) || tValue > rMax) rMax = tValue;
            rDest.nextAndSet(rMax);
        }
    }
    public static void cummin2Dest_(IDoubleIterator tThis, IDoubleSetOnlyIterator rDest) {
        double rMin = Double.NaN;
        while (tThis.hasNext()) {
            double tValue = tThis.next();
            if (Double.isNaN(rMin) || tValue < rMin) rMin = tValue;
            rDest.nextAndSet(rMin);
        }
    }
    public static void cumstat2Dest_(IDoubleIterator tThis, IDoubleSetOnlyIterator rDest, IDoubleOperator2 aOpt) {
        double rStat = Double.NaN;
        while (tThis.hasNext()) {
            rStat = aOpt.cal(rStat, tThis.next());
            rDest.nextAndSet(rStat);
        }
    }
}
