package com.jtool.math.operation;

import com.jtool.code.IHasIterator;
import com.jtool.code.IHasLotIterator;
import com.jtool.code.operator.IOperator1;
import com.jtool.code.operator.IOperator2;
import com.jtool.math.IDataShell;

import java.util.Iterator;


/**
 * 对于内部含有 double[] 的数据的运算做专门优化，方便编译器做 SIMD 的相关优化
 * @author liqa
 */
public class ARRAY {
    private ARRAY() {}
    
    /** add, minus, multiply, divide stuffs */
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebePlus2Dest_(T aLHS, T aRHS, RS rDest) {
        final double[] tData1 = rDest.getIfHasSameOrderData(aLHS);
        final double[] tData2 = rDest.getIfHasSameOrderData(aRHS);
        if (tData1 != null && tData2 != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift1 = IDataShell.shiftSize(aLHS);
            final int tShift2 = IDataShell.shiftSize(aRHS);
            if (rShift == tShift1) {
                if (rShift == tShift2) for (int i = rShift; i < rEnd; ++i) rData[i] = tData1[i] + tData2[i];
                else for (int i = rShift, k = tShift2; i < rEnd; ++i, ++k) rData[i] = tData1[i] + tData2[k];
            } else {
                if (rShift == tShift2) for (int i = rShift, j = tShift1; i < rEnd; ++i, ++j) rData[i] = tData1[j] + tData2[i];
                else for (int i = rShift, j = tShift1, k = tShift2; i < rEnd; ++i, ++j, ++k) rData[i] = tData1[j] + tData2[k];
            }
        } else {
            DATA.ebePlus2Dest_(aLHS, aRHS, rDest);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeMinus2Dest_(T aLHS, T aRHS, RS rDest) {
        final double[] tData1 = rDest.getIfHasSameOrderData(aLHS);
        final double[] tData2 = rDest.getIfHasSameOrderData(aRHS);
        if (tData1 != null && tData2 != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift1 = IDataShell.shiftSize(aLHS);
            final int tShift2 = IDataShell.shiftSize(aRHS);
            if (rShift == tShift1) {
                if (rShift == tShift2) for (int i = rShift; i < rEnd; ++i) rData[i] = tData1[i] - tData2[i];
                else for (int i = rShift, k = tShift2; i < rEnd; ++i, ++k) rData[i] = tData1[i] - tData2[k];
            } else {
                if (rShift == tShift2) for (int i = rShift, j = tShift1; i < rEnd; ++i, ++j) rData[i] = tData1[j] - tData2[i];
                else for (int i = rShift, j = tShift1, k = tShift2; i < rEnd; ++i, ++j, ++k) rData[i] = tData1[j] - tData2[k];
            }
        } else {
            DATA.ebeMinus2Dest_(aLHS, aRHS, rDest);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeMultiply2Dest_(T aLHS, T aRHS, RS rDest) {
        final double[] tData1 = rDest.getIfHasSameOrderData(aLHS);
        final double[] tData2 = rDest.getIfHasSameOrderData(aRHS);
        if (tData1 != null && tData2 != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift1 = IDataShell.shiftSize(aLHS);
            final int tShift2 = IDataShell.shiftSize(aRHS);
            if (rShift == tShift1) {
                if (rShift == tShift2) for (int i = rShift; i < rEnd; ++i) rData[i] = tData1[i] * tData2[i];
                else for (int i = rShift, k = tShift2; i < rEnd; ++i, ++k) rData[i] = tData1[i] * tData2[k];
            } else {
                if (rShift == tShift2) for (int i = rShift, j = tShift1; i < rEnd; ++i, ++j) rData[i] = tData1[j] * tData2[i];
                else for (int i = rShift, j = tShift1, k = tShift2; i < rEnd; ++i, ++j, ++k) rData[i] = tData1[j] * tData2[k];
            }
        } else {
            DATA.ebeMultiply2Dest_(aLHS, aRHS, rDest);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeDiv2Dest_(T aLHS, T aRHS, RS rDest) {
        final double[] tData1 = rDest.getIfHasSameOrderData(aLHS);
        final double[] tData2 = rDest.getIfHasSameOrderData(aRHS);
        if (tData1 != null && tData2 != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift1 = IDataShell.shiftSize(aLHS);
            final int tShift2 = IDataShell.shiftSize(aRHS);
            if (rShift == tShift1) {
                if (rShift == tShift2) for (int i = rShift; i < rEnd; ++i) rData[i] = tData1[i] / tData2[i];
                else for (int i = rShift, k = tShift2; i < rEnd; ++i, ++k) rData[i] = tData1[i] / tData2[k];
            } else {
                if (rShift == tShift2) for (int i = rShift, j = tShift1; i < rEnd; ++i, ++j) rData[i] = tData1[j] / tData2[i];
                else for (int i = rShift, j = tShift1, k = tShift2; i < rEnd; ++i, ++j, ++k) rData[i] = tData1[j] / tData2[k];
            }
        } else {
            DATA.ebeDiv2Dest_(aLHS, aRHS, rDest);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeMod2Dest_(T aLHS, T aRHS, RS rDest) {
        final double[] tData1 = rDest.getIfHasSameOrderData(aLHS);
        final double[] tData2 = rDest.getIfHasSameOrderData(aRHS);
        if (tData1 != null && tData2 != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift1 = IDataShell.shiftSize(aLHS);
            final int tShift2 = IDataShell.shiftSize(aRHS);
            if (rShift == tShift1) {
                if (rShift == tShift2) for (int i = rShift; i < rEnd; ++i) rData[i] = tData1[i] % tData2[i];
                else for (int i = rShift, k = tShift2; i < rEnd; ++i, ++k) rData[i] = tData1[i] % tData2[k];
            } else {
                if (rShift == tShift2) for (int i = rShift, j = tShift1; i < rEnd; ++i, ++j) rData[i] = tData1[j] % tData2[i];
                else for (int i = rShift, j = tShift1, k = tShift2; i < rEnd; ++i, ++j, ++k) rData[i] = tData1[j] % tData2[k];
            }
        } else {
            DATA.ebeMod2Dest_(aLHS, aRHS, rDest);
        }
    }
    
    
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void mapPlus2Dest_(T aLHS, double aRHS, RS rDest) {
        final double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aLHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] = tData[i] + aRHS;
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] = tData[j] + aRHS;
        } else {
            DATA.mapPlus2Dest_(aLHS, aRHS, rDest);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void mapMinus2Dest_(T aLHS, double aRHS, RS rDest) {
        final double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aLHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] = tData[i] - aRHS;
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] = tData[j] - aRHS;
        } else {
            DATA.mapMinus2Dest_(aLHS, aRHS, rDest);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void mapLMinus2Dest_(T aLHS, double aRHS, RS rDest) {
        final double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aLHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] = aRHS - tData[i];
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] = aRHS - tData[j];
        } else {
            DATA.mapLMinus2Dest_(aLHS, aRHS, rDest);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void mapMultiply2Dest_(T aLHS, double aRHS, RS rDest) {
        final double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aLHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] = tData[i] * aRHS;
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] = tData[j] * aRHS;
        } else {
            DATA.mapMultiply2Dest_(aLHS, aRHS, rDest);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void mapDiv2Dest_(T aLHS, double aRHS, RS rDest) {
        final double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aLHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] = tData[i] / aRHS;
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] = tData[j] / aRHS;
        } else {
            DATA.mapDiv2Dest_(aLHS, aRHS, rDest);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void mapLDiv2Dest_(T aLHS, double aRHS, RS rDest) {
        final double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aLHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] = aRHS / tData[i];
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] = aRHS / tData[j];
        } else {
            DATA.mapLDiv2Dest_(aLHS, aRHS, rDest);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void mapMod2Dest_(T aLHS, double aRHS, RS rDest) {
        final double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aLHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] = tData[i] % aRHS;
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] = tData[j] % aRHS;
        } else {
            DATA.mapMod2Dest_(aLHS, aRHS, rDest);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void mapLMod2Dest_(T aLHS, double aRHS, RS rDest) {
        final double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aLHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] = aRHS % tData[i];
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] = aRHS % tData[j];
        } else {
            DATA.mapLMod2Dest_(aLHS, aRHS, rDest);
        }
    }
    
    
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebePlus2this_(RS rThis, T aRHS) {
        final double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            final double[] rData = rThis.getData();
            final int rShift = rThis.shiftSize();
            final int rEnd = rThis.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aRHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] += tData[i];
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] += tData[j];
        } else {
            DATA.ebePlus2this_(rThis, aRHS);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeMinus2this_(RS rThis, T aRHS) {
        final double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            final double[] rData = rThis.getData();
            final int rShift = rThis.shiftSize();
            final int rEnd = rThis.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aRHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] -= tData[i];
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] -= tData[j];
        } else {
            DATA.ebeMinus2this_(rThis, aRHS);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeLMinus2this_(RS rThis, T aRHS) {
        final double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            final double[] rData = rThis.getData();
            final int rShift = rThis.shiftSize();
            final int rEnd = rThis.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aRHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] = tData[i] - rData[i];
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] = tData[j] - rData[i];
        } else {
            DATA.ebeLMinus2this_(rThis, aRHS);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeMultiply2this_(RS rThis, T aRHS) {
        final double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            final double[] rData = rThis.getData();
            final int rShift = rThis.shiftSize();
            final int rEnd = rThis.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aRHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] *= tData[i];
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] *= tData[j];
        } else {
            DATA.ebeMultiply2this_(rThis, aRHS);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeDiv2this_(RS rThis, T aRHS) {
        final double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            final double[] rData = rThis.getData();
            final int rShift = rThis.shiftSize();
            final int rEnd = rThis.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aRHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] /= tData[i];
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] /= tData[j];
        } else {
            DATA.ebeDiv2this_(rThis, aRHS);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeLDiv2this_(RS rThis, T aRHS) {
        final double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            final double[] rData = rThis.getData();
            final int rShift = rThis.shiftSize();
            final int rEnd = rThis.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aRHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] = tData[i] / rData[i];
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] = tData[j] / rData[i];
        } else {
            DATA.ebeLDiv2this_(rThis, aRHS);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeMod2this_(RS rThis, T aRHS) {
        final double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            final double[] rData = rThis.getData();
            final int rShift = rThis.shiftSize();
            final int rEnd = rThis.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aRHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] %= tData[i];
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] %= tData[j];
        } else {
            DATA.ebeMod2this_(rThis, aRHS);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeLMod2this_(RS rThis, T aRHS) {
        final double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            final double[] rData = rThis.getData();
            final int rShift = rThis.shiftSize();
            final int rEnd = rThis.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aRHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] = tData[i] % rData[i];
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] = tData[j] % rData[i];
        } else {
            DATA.ebeLMod2this_(rThis, aRHS);
        }
    }
    
    
    public static <RS extends IDataShell<double[]>> void mapPlus2this_    (RS rThis, double aRHS) {final double[] rData = rThis.getData(); final int rShift = rThis.shiftSize(); final int rEnd = rThis.dataSize() + rShift; for (int i = rShift; i < rEnd; ++i) rData[i] += aRHS;}
    public static <RS extends IDataShell<double[]>> void mapMinus2this_   (RS rThis, double aRHS) {final double[] rData = rThis.getData(); final int rShift = rThis.shiftSize(); final int rEnd = rThis.dataSize() + rShift; for (int i = rShift; i < rEnd; ++i) rData[i] -= aRHS;}
    public static <RS extends IDataShell<double[]>> void mapLMinus2this_  (RS rThis, double aRHS) {final double[] rData = rThis.getData(); final int rShift = rThis.shiftSize(); final int rEnd = rThis.dataSize() + rShift; for (int i = rShift; i < rEnd; ++i) rData[i] = aRHS - rData[i];}
    public static <RS extends IDataShell<double[]>> void mapMultiply2this_(RS rThis, double aRHS) {final double[] rData = rThis.getData(); final int rShift = rThis.shiftSize(); final int rEnd = rThis.dataSize() + rShift; for (int i = rShift; i < rEnd; ++i) rData[i] *= aRHS;}
    public static <RS extends IDataShell<double[]>> void mapDiv2this_     (RS rThis, double aRHS) {final double[] rData = rThis.getData(); final int rShift = rThis.shiftSize(); final int rEnd = rThis.dataSize() + rShift; for (int i = rShift; i < rEnd; ++i) rData[i] /= aRHS;}
    public static <RS extends IDataShell<double[]>> void mapLDiv2this_    (RS rThis, double aRHS) {final double[] rData = rThis.getData(); final int rShift = rThis.shiftSize(); final int rEnd = rThis.dataSize() + rShift; for (int i = rShift; i < rEnd; ++i) rData[i] = aRHS / rData[i];}
    public static <RS extends IDataShell<double[]>> void mapMod2this_     (RS rThis, double aRHS) {final double[] rData = rThis.getData(); final int rShift = rThis.shiftSize(); final int rEnd = rThis.dataSize() + rShift; for (int i = rShift; i < rEnd; ++i) rData[i] %= aRHS;}
    public static <RS extends IDataShell<double[]>> void mapLMod2this_    (RS rThis, double aRHS) {final double[] rData = rThis.getData(); final int rShift = rThis.shiftSize(); final int rEnd = rThis.dataSize() + rShift; for (int i = rShift; i < rEnd; ++i) rData[i] = aRHS % rData[i];}
    
    
    
    /** do stuff */
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeDo2Dest_(T aLHS, T aRHS, RS rDest, IOperator2<Double> aOpt) {
        final double[] tData1 = rDest.getIfHasSameOrderData(aLHS);
        final double[] tData2 = rDest.getIfHasSameOrderData(aRHS);
        if (tData1 != null && tData2 != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift1 = IDataShell.shiftSize(aLHS);
            final int tShift2 = IDataShell.shiftSize(aRHS);
            if (rShift == tShift1) {
                if (rShift == tShift2) for (int i = rShift; i < rEnd; ++i) rData[i] = aOpt.cal(tData1[i], tData2[i]);
                else for (int i = rShift, k = tShift2; i < rEnd; ++i, ++k) rData[i] = aOpt.cal(tData1[i], tData2[k]);
            } else {
                if (rShift == tShift2) for (int i = rShift, j = tShift1; i < rEnd; ++i, ++j) rData[i] = aOpt.cal(tData1[j], tData2[i]);
                else for (int i = rShift, j = tShift1, k = tShift2; i < rEnd; ++i, ++j, ++k) rData[i] = aOpt.cal(tData1[j], tData2[k]);
            }
        } else {
            DATA .ebeDo2Dest_(aLHS, aRHS, rDest, aOpt);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void mapDo2Dest_(T aLHS, RS rDest, IOperator1<Double> aOpt) {
        final double[] tData = rDest.getIfHasSameOrderData(aLHS);
        if (tData != null) {
            final double[] rData = rDest.getData();
            final int rShift = rDest.shiftSize();
            final int rEnd = rDest.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aLHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] = aOpt.cal(tData[i]);
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] = aOpt.cal(tData[j]);
        } else {
            DATA.mapDo2Dest_(aLHS, rDest, aOpt);
        }
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeDo2this_(RS rThis, T aRHS, IOperator2<Double> aOpt) {
        final double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            final double[] rData = rThis.getData();
            final int rShift = rThis.shiftSize();
            final int rEnd = rThis.dataSize() + rShift;
            final int tShift = IDataShell.shiftSize(aRHS);
            if (rShift == tShift) for (int i = rShift; i < rEnd; ++i) rData[i] = aOpt.cal(rData[i], tData[i]);
            else for (int i = rShift, j = tShift; i < rEnd; ++i, ++j) rData[i] = aOpt.cal(rData[i], tData[j]);
        } else {
            DATA.ebeDo2this_(rThis, aRHS, aOpt);
        }
    }
    public static <RS extends IDataShell<double[]>> void mapDo2this_(RS rThis, IOperator1<Double> aOpt) {
        final double[] rData = rThis.getData();
        final int rShift = rThis.shiftSize();
        final int rEnd = rThis.dataSize() + rShift;
        for (int i = rShift; i < rEnd; ++i) rData[i] = aOpt.cal(rData[i]);
    }
    
    
    public static <RS extends IDataShell<double[]>> void mapFill2this_(RS rThis, double aRHS) {
        final double[] rData = rThis.getData();
        final int rShift = rThis.shiftSize();
        final int rEnd = rThis.dataSize() + rShift;
        for (int i = rShift; i < rEnd; ++i) rData[i] = aRHS; // 注意在指定区域外不能填充，因此不能使用 Arrays.fill
    }
    public static <T, RS extends IHasLotIterator<? super T, Double> & IDataShell<double[]>> void ebeFill2this_(RS rThis, T aRHS) {
        final double[] tData = rThis.getIfHasSameOrderData(aRHS);
        if (tData != null) {
            System.arraycopy(tData, IDataShell.shiftSize(aRHS), rThis.getData(), rThis.shiftSize(), rThis.dataSize());
        } else {
            DATA.ebeFill2this_(rThis, aRHS);
        }
    }
    
    
    
    /** stat stuff */
    public static <RS extends IDataShell<double[]>> double sumOfThis_(RS tThis) {
        final double[] tData = tThis.getData();
        final int tShift = tThis.shiftSize();
        final int tEnd = tThis.dataSize() + tShift;
        
        double rSum = 0.0;
        for (int i = tShift; i < tEnd; ++i) rSum += tData[i];
        return rSum;
    }
    public static <RS extends IDataShell<double[]>> double meanOfThis_(RS tThis) {
        final double[] tData = tThis.getData();
        final int tShift = tThis.shiftSize();
        final int tSize = tThis.dataSize();
        final int tEnd = tSize + tShift;
        
        double rSum = 0.0;
        for (int i = tShift; i < tEnd; ++i) rSum += tData[i];
        return rSum / (double)tSize;
    }
    public static <RS extends IDataShell<double[]>> double productOfThis_(RS tThis) {
        final double[] tData = tThis.getData();
        final int tShift = tThis.shiftSize();
        final int tEnd = tThis.dataSize() + tShift;
        
        double rProduct = 1.0;
        for (int i = tShift; i < tEnd; ++i) rProduct *= tData[i];
        return rProduct;
    }
    public static <RS extends IDataShell<double[]>> double maxOfThis_(RS tThis) {
        final double[] tData = tThis.getData();
        final int tShift = tThis.shiftSize();
        final int tEnd = tThis.dataSize() + tShift;
        
        double rMax = Double.NEGATIVE_INFINITY;
        for (int i = tShift; i < tEnd; ++i) {
            double tValue = tData[i];
            if (tValue > rMax) rMax = tValue;
        }
        return rMax;
    }
    public static <RS extends IDataShell<double[]>> double minOfThis_(RS tThis) {
        final double[] tData = tThis.getData();
        final int tShift = tThis.shiftSize();
        final int tEnd = tThis.dataSize() + tShift;
        
        double rMin = Double.POSITIVE_INFINITY;
        for (int i = tShift; i < tEnd; ++i) {
            double tValue = tData[i];
            if (tValue < rMin) rMin = tValue;
        }
        return rMin;
    }
}
