package com.jtool.math.operation;

import com.jtool.code.IFatIterable;
import com.jtool.code.operator.IOperator1;
import com.jtool.code.operator.IOperator2;
import com.jtool.math.IDataShell;


/**
 * 对于内部含有 double[] 的数据的运算做专门的优化，方便编译器做 SIMD 的相关优化
 * @author liqa
 */
public abstract class DoubleArrayOperation<R extends IFatIterable<? super T, Number, Double, Number> & IDataShell<?, double[]>, T> extends RealDataOperation<R, T> {
    /** add, minus, multiply, divide stuffs */
    @Override protected void ebeAdd2Dest_(T aLHS, T aRHS, R rDest) {
        double[] tData1 = rDest.getIfHasSameOrderData(aLHS);
        double[] tData2 = rDest.getIfHasSameOrderData(aRHS);
        if (tData1 != null && tData2 != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = tData1[i] + tData2[i];
        } else {
            super.ebeAdd2Dest_(aLHS, aRHS, rDest);
        }
    }
    @Override protected void ebeMinus2Dest_(T aLHS, T aRHS, R rDest) {
        double[] tData1 = rDest.getIfHasSameOrderData(aLHS);
        double[] tData2 = rDest.getIfHasSameOrderData(aRHS);
        if (tData1 != null && tData2 != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = tData1[i] - tData2[i];
        } else {
            super.ebeMinus2Dest_(aLHS, aRHS, rDest);
        }
    }
    @Override protected void ebeMultiply2Dest_(T aLHS, T aRHS, R rDest) {
        double[] tData1 = rDest.getIfHasSameOrderData(aLHS);
        double[] tData2 = rDest.getIfHasSameOrderData(aRHS);
        if (tData1 != null && tData2 != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = tData1[i] * tData2[i];
        } else {
            super.ebeMultiply2Dest_(aLHS, aRHS, rDest);
        }
    }
    @Override protected void ebeDivide2Dest_(T aLHS, T aRHS, R rDest) {
        double[] tData1 = rDest.getIfHasSameOrderData(aLHS);
        double[] tData2 = rDest.getIfHasSameOrderData(aRHS);
        if (tData1 != null && tData2 != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = tData1[i] / tData2[i];
        } else {
            super.ebeDivide2Dest_(aLHS, aRHS, rDest);
        }
    }
    @Override protected void ebeMod2Dest_(T aLHS, T aRHS, R rDest) {
        double[] tData1 = rDest.getIfHasSameOrderData(aLHS);
        double[] tData2 = rDest.getIfHasSameOrderData(aRHS);
        if (tData1 != null && tData2 != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = tData1[i] % tData2[i];
        } else {
            super.ebeMod2Dest_(aLHS, aRHS, rDest);
        }
    }
    
    
    @Override protected void mapAdd2Dest_(T aLHS, Double aRHS, R rDest) {
        double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = tData[i] + aRHS;
        } else {
            super.mapAdd2Dest_(aLHS, aRHS, rDest);
        }
    }
    @Override protected void mapMinus2Dest_(T aLHS, Double aRHS, R rDest) {
        double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = tData[i] - aRHS;
        } else {
            super.mapMinus2Dest_(aLHS, aRHS, rDest);
        }
    }
    @Override protected void mapLMinus2Dest_(T aLHS, Double aRHS, R rDest) {
        double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = aRHS - tData[i];
        } else {
            super.mapLMinus2Dest_(aLHS, aRHS, rDest);
        }
    }
    @Override protected void mapMultiply2Dest_(T aLHS, Double aRHS, R rDest) {
        double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = tData[i] * aRHS;
        } else {
            super.mapMultiply2Dest_(aLHS, aRHS, rDest);
        }
    }
    @Override protected void mapDivide2Dest_(T aLHS, Double aRHS, R rDest) {
        double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = tData[i] / aRHS;
        } else {
            super.mapDivide2Dest_(aLHS, aRHS, rDest);
        }
    }
    @Override protected void mapLDivide2Dest_(T aLHS, Double aRHS, R rDest) {
        double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = aRHS / tData[i];
        } else {
            super.mapLDivide2Dest_(aLHS, aRHS, rDest);
        }
    }
    @Override protected void mapMod2Dest_(T aLHS, Double aRHS, R rDest) {
        double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = tData[i] % aRHS;
        } else {
            super.mapMod2Dest_(aLHS, aRHS, rDest);
        }
    }
    @Override protected void mapLMod2Dest_(T aLHS, Double aRHS, R rDest) {
        double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = aRHS % tData[i];
        } else {
            super.mapLMod2Dest_(aLHS, aRHS, rDest);
        }
    }
    
    
    @Override protected void ebeAdd2this_(R rThis, T aRHS) {
        double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            double[] rData = rThis.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] += tData[i];
        } else {
            super.ebeAdd2this_(rThis, aRHS);
        }
    }
    @Override protected void ebeMinus2this_(R rThis, T aRHS) {
        double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            double[] rData = rThis.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] -= tData[i];
        } else {
            super.ebeMinus2this_(rThis, aRHS);
        }
    }
    @Override protected void ebeLMinus2this_(R rThis, T aRHS) {
        double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            double[] rData = rThis.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = tData[i] - rData[i];
        } else {
            super.ebeLMinus2this_(rThis, aRHS);
        }
    }
    @Override protected void ebeMultiply2this_(R rThis, T aRHS) {
        double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            double[] rData = rThis.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] *= tData[i];
        } else {
            super.ebeMultiply2this_(rThis, aRHS);
        }
    }
    @Override protected void ebeDivide2this_(R rThis, T aRHS) {
        double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            double[] rData = rThis.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] /= tData[i];
        } else {
            super.ebeDivide2this_(rThis, aRHS);
        }
    }
    @Override protected void ebeLDivide2this_(R rThis, T aRHS) {
        double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            double[] rData = rThis.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = tData[i] / rData[i];
        } else {
            super.ebeLDivide2this_(rThis, aRHS);
        }
    }
    @Override protected void ebeMod2this_(R rThis, T aRHS) {
        double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            double[] rData = rThis.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] %= tData[i];
        } else {
            super.ebeMod2this_(rThis, aRHS);
        }
    }
    @Override protected void ebeLMod2this_(R rThis, T aRHS) {
        double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            double[] rData = rThis.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = tData[i] % rData[i];
        } else {
            super.ebeLMod2this_(rThis, aRHS);
        }
    }
    
    
    @Override protected void mapAdd2this_       (R rThis, Double aRHS) {double[] rData = rThis.getData(); for (int i = 0; i < rData.length; ++i) rData[i] += aRHS;}
    @Override protected void mapMinus2this_     (R rThis, Double aRHS) {double[] rData = rThis.getData(); for (int i = 0; i < rData.length; ++i) rData[i] -= aRHS;}
    @Override protected void mapLMinus2this_    (R rThis, Double aRHS) {double[] rData = rThis.getData(); for (int i = 0; i < rData.length; ++i) rData[i] = aRHS - rData[i];}
    @Override protected void mapMultiply2this_  (R rThis, Double aRHS) {double[] rData = rThis.getData(); for (int i = 0; i < rData.length; ++i) rData[i] *= aRHS;}
    @Override protected void mapDivide2this_    (R rThis, Double aRHS) {double[] rData = rThis.getData(); for (int i = 0; i < rData.length; ++i) rData[i] /= aRHS;}
    @Override protected void mapLDivide2this_   (R rThis, Double aRHS) {double[] rData = rThis.getData(); for (int i = 0; i < rData.length; ++i) rData[i] = aRHS / rData[i];}
    @Override protected void mapMod2this_       (R rThis, Double aRHS) {double[] rData = rThis.getData(); for (int i = 0; i < rData.length; ++i) rData[i] %= aRHS;}
    @Override protected void mapLMod2this_      (R rThis, Double aRHS) {double[] rData = rThis.getData(); for (int i = 0; i < rData.length; ++i) rData[i] = aRHS % rData[i];}
    
    
    
    /** do stuff */
    @Override protected void ebeDo2Dest_(T aLHS, T aRHS, R rDest, IOperator2<Double> aOpt) {
        double[] tData1 = rDest.getIfHasSameOrderData(aLHS);
        double[] tData2 = rDest.getIfHasSameOrderData(aRHS);
        if (tData1 != null && tData2 != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = aOpt.cal(tData1[i], tData2[i]);
        } else {
            super.ebeDo2Dest_(aLHS, aRHS, rDest, aOpt);
        }
    }
    @Override protected void mapDo2Dest_(T aLHS, R rDest, IOperator1<Double> aOpt) {
        double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            double[] rData = rDest.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = aOpt.cal(tData[i]);
        } else {
            super.mapDo2Dest_(aLHS, rDest, aOpt);
        }
    }
    @Override protected void ebeDo2this_(R rThis, T aRHS, IOperator2<Double> aOpt) {
        double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            double[] rData = rThis.getData();
            for (int i = 0; i < rData.length; ++i) rData[i] = aOpt.cal(rData[i], tData[i]);
        } else {
            super.ebeDo2this_(rThis, aRHS, aOpt);
        }
    }
    @Override protected void mapDo2this_(R rThis, IOperator1<Double> aOpt) {
        double[] rData = rThis.getData();
        for (int i = 0; i < rData.length; ++i) rData[i] = aOpt.cal(rData[i]);
    }
}
