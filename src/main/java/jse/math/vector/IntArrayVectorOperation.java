package jse.math.vector;

import jse.code.functional.ISwapper;
import jse.math.IDataShell;
import jse.math.operation.ARRAY;
import jse.math.operation.DATA;

import java.util.function.*;

import static jse.math.vector.AbstractVectorOperation.ebeCheck;

public abstract class IntArrayVectorOperation extends AbstractIntVectorOperation {
    /** 通用的一些运算 */
    @Override public IIntVector plus(IIntVector aRHS) {
        IntArrayVector tThis = thisVector_();
        ebeCheck(tThis.size(), aRHS.size());
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        int[] tDataR = rVector.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) ARRAY.ebePlus2Dest(tDataL, tThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.ebePlus2Dest(tThis, aRHS, rVector);
        return rVector;
    }
    @Override public IIntVector minus(IIntVector aRHS) {
        IntArrayVector tThis = thisVector_();
        ebeCheck(tThis.size(), aRHS.size());
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        int[] tDataR = rVector.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) ARRAY.ebeMinus2Dest(tDataL, tThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.ebeMinus2Dest(tThis, aRHS, rVector);
        return rVector;
    }
    @Override public IIntVector lminus(IIntVector aRHS) {
        IntArrayVector tThis = thisVector_();
        ebeCheck(tThis.size(), aRHS.size());
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        int[] tDataR = rVector.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) ARRAY.ebeMinus2Dest(tDataR, IDataShell.internalDataShift(aRHS), tDataL, tThis.internalDataShift(), rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.ebeMinus2Dest(aRHS, tThis, rVector);
        return rVector;
    }
    @Override public IIntVector multiply(IIntVector aRHS) {
        IntArrayVector tThis = thisVector_();
        ebeCheck(tThis.size(), aRHS.size());
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        int[] tDataR = rVector.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) ARRAY.ebeMultiply2Dest(tDataL, tThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.ebeMultiply2Dest(tThis, aRHS, rVector);
        return rVector;
    }
    @Override public IIntVector div(IIntVector aRHS) {
        IntArrayVector tThis = thisVector_();
        ebeCheck(tThis.size(), aRHS.size());
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        int[] tDataR = rVector.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) ARRAY.ebeDiv2Dest(tDataL, tThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.ebeDiv2Dest(tThis, aRHS, rVector);
        return rVector;
    }
    @Override public IIntVector ldiv(IIntVector aRHS) {
        IntArrayVector tThis = thisVector_();
        ebeCheck(tThis.size(), aRHS.size());
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        int[] tDataR = rVector.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) ARRAY.ebeDiv2Dest(tDataR, IDataShell.internalDataShift(aRHS), tDataL, tThis.internalDataShift(), rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.ebeDiv2Dest(aRHS, tThis, rVector);
        return rVector;
    }
    @Override public IIntVector mod(IIntVector aRHS) {
        IntArrayVector tThis = thisVector_();
        ebeCheck(tThis.size(), aRHS.size());
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        int[] tDataR = rVector.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) ARRAY.ebeMod2Dest(tDataL, tThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.ebeMod2Dest(tThis, aRHS, rVector);
        return rVector;
    }
    @Override public IIntVector lmod(IIntVector aRHS) {
        IntArrayVector tThis = thisVector_();
        ebeCheck(tThis.size(), aRHS.size());
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        int[] tDataR = rVector.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) ARRAY.ebeMod2Dest(tDataR, IDataShell.internalDataShift(aRHS), tDataL, tThis.internalDataShift(), rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.ebeMod2Dest(aRHS, tThis, rVector);
        return rVector;
    }
    @Override public IIntVector operate(IIntVector aRHS, IntBinaryOperator aOpt) {
        IntArrayVector tThis = thisVector_();
        ebeCheck(tThis.size(), aRHS.size());
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        int[] tDataR = rVector.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) ARRAY.ebeDo2Dest(tDataL, tThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize(), aOpt);
        else DATA.ebeDo2Dest(tThis, aRHS, rVector, aOpt);
        return rVector;
    }
    
    @Override public IIntVector plus(int aRHS) {
        IntArrayVector tThis = thisVector_();
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        if (tDataL != null) ARRAY.mapPlus2Dest(tDataL, tThis.internalDataShift(), aRHS, rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.mapPlus2Dest(tThis, aRHS, rVector);
        return rVector;
    }
    @Override public IIntVector minus(int aRHS) {
        IntArrayVector tThis = thisVector_();
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        if (tDataL != null) ARRAY.mapMinus2Dest(tDataL, tThis.internalDataShift(), aRHS, rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.mapMinus2Dest(tThis, aRHS, rVector);
        return rVector;
    }
    @Override public IIntVector lminus(int aRHS) {
        IntArrayVector tThis = thisVector_();
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        if (tDataL != null) ARRAY.mapLMinus2Dest(tDataL, tThis.internalDataShift(), aRHS, rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.mapLMinus2Dest(tThis, aRHS, rVector);
        return rVector;
    }
    @Override public IIntVector multiply(int aRHS) {
        IntArrayVector tThis = thisVector_();
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        if (tDataL != null) ARRAY.mapMultiply2Dest(tDataL, tThis.internalDataShift(), aRHS, rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.mapMultiply2Dest(tThis, aRHS, rVector);
        return rVector;
    }
    @Override public IIntVector div(int aRHS) {
        IntArrayVector tThis = thisVector_();
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        if (tDataL != null) ARRAY.mapDiv2Dest(tDataL, tThis.internalDataShift(), aRHS, rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.mapDiv2Dest(tThis, aRHS, rVector);
        return rVector;
    }
    @Override public IIntVector ldiv(int aRHS) {
        IntArrayVector tThis = thisVector_();
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        if (tDataL != null) ARRAY.mapLDiv2Dest(tDataL, tThis.internalDataShift(), aRHS, rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.mapLDiv2Dest(tThis, aRHS, rVector);
        return rVector;
    }
    @Override public IIntVector mod(int aRHS) {
        IntArrayVector tThis = thisVector_();
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        if (tDataL != null) ARRAY.mapMod2Dest(tDataL, tThis.internalDataShift(), aRHS, rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.mapMod2Dest(tThis, aRHS, rVector);
        return rVector;
    }
    @Override public IIntVector lmod(int aRHS) {
        IntArrayVector tThis = thisVector_();
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        if (tDataL != null) ARRAY.mapLMod2Dest(tDataL, tThis.internalDataShift(), aRHS, rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.mapLMod2Dest(tThis, aRHS, rVector);
        return rVector;
    }
    @Override public IIntVector map(IntUnaryOperator aOpt) {
        IntArrayVector tThis = thisVector_();
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        if (tDataL != null) ARRAY.mapDo2Dest(tDataL, tThis.internalDataShift(), rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize(), aOpt);
        else DATA.mapDo2Dest(tThis, rVector, aOpt);
        return rVector;
    }
    
    @Override public void plus2this(IIntVector aRHS) {
        IntArrayVector rThis = thisVector_();
        ebeCheck(rThis.size(), aRHS.size());
        int[] tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) ARRAY.ebePlus2This(rThis.internalData(), rThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rThis.internalDataSize());
        else DATA.ebePlus2This(rThis, aRHS);
    }
    @Override public void minus2this(IIntVector aRHS) {
        IntArrayVector rThis = thisVector_();
        ebeCheck(rThis.size(), aRHS.size());
        int[] tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) ARRAY.ebeMinus2This(rThis.internalData(), rThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rThis.internalDataSize());
        else DATA.ebeMinus2This(rThis, aRHS);
    }
    @Override public void lminus2this(IIntVector aRHS) {
        IntArrayVector rThis = thisVector_();
        ebeCheck(rThis.size(), aRHS.size());
        int[] tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) ARRAY.ebeLMinus2This(rThis.internalData(), rThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rThis.internalDataSize());
        else DATA.ebeLMinus2This(rThis, aRHS);
    }
    @Override public void multiply2this(IIntVector aRHS) {
        IntArrayVector rThis = thisVector_();
        ebeCheck(rThis.size(), aRHS.size());
        int[] tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) ARRAY.ebeMultiply2This(rThis.internalData(), rThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rThis.internalDataSize());
        else DATA.ebeMultiply2This(rThis, aRHS);
    }
    @Override public void div2this(IIntVector aRHS) {
        IntArrayVector rThis = thisVector_();
        ebeCheck(rThis.size(), aRHS.size());
        int[] tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) ARRAY.ebeDiv2This(rThis.internalData(), rThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rThis.internalDataSize());
        else DATA.ebeDiv2This(rThis, aRHS);
    }
    @Override public void ldiv2this(IIntVector aRHS) {
        IntArrayVector rThis = thisVector_();
        ebeCheck(rThis.size(), aRHS.size());
        int[] tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) ARRAY.ebeLDiv2This(rThis.internalData(), rThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rThis.internalDataSize());
        else DATA.ebeLDiv2This(rThis, aRHS);
    }
    @Override public void mod2this(IIntVector aRHS) {
        IntArrayVector rThis = thisVector_();
        ebeCheck(rThis.size(), aRHS.size());
        int[] tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) ARRAY.ebeMod2This(rThis.internalData(), rThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rThis.internalDataSize());
        else DATA.ebeMod2This(rThis, aRHS);
    }
    @Override public void lmod2this(IIntVector aRHS) {
        IntArrayVector rThis = thisVector_();
        ebeCheck(rThis.size(), aRHS.size());
        int[] tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) ARRAY.ebeLMod2This(rThis.internalData(), rThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rThis.internalDataSize());
        else DATA.ebeLMod2This(rThis, aRHS);
    }
    @Override public void operate2this(IIntVector aRHS, IntBinaryOperator aOpt) {
        IntArrayVector rThis = thisVector_();
        ebeCheck(rThis.size(), aRHS.size());
        int[] tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) ARRAY.ebeDo2This(rThis.internalData(), rThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rThis.internalDataSize(), aOpt);
        else DATA.ebeDo2This(rThis, aRHS, aOpt);
    }
    
    @Override public void plus2this     (int aRHS) {IntArrayVector rThis = thisVector_(); ARRAY.mapPlus2This    (rThis.internalData(), rThis.internalDataShift(), aRHS, rThis.internalDataSize());}
    @Override public void minus2this    (int aRHS) {IntArrayVector rThis = thisVector_(); ARRAY.mapMinus2This   (rThis.internalData(), rThis.internalDataShift(), aRHS, rThis.internalDataSize());}
    @Override public void lminus2this   (int aRHS) {IntArrayVector rThis = thisVector_(); ARRAY.mapLMinus2This  (rThis.internalData(), rThis.internalDataShift(), aRHS, rThis.internalDataSize());}
    @Override public void multiply2this (int aRHS) {IntArrayVector rThis = thisVector_(); ARRAY.mapMultiply2This(rThis.internalData(), rThis.internalDataShift(), aRHS, rThis.internalDataSize());}
    @Override public void div2this      (int aRHS) {IntArrayVector rThis = thisVector_(); ARRAY.mapDiv2This     (rThis.internalData(), rThis.internalDataShift(), aRHS, rThis.internalDataSize());}
    @Override public void ldiv2this     (int aRHS) {IntArrayVector rThis = thisVector_(); ARRAY.mapLDiv2This    (rThis.internalData(), rThis.internalDataShift(), aRHS, rThis.internalDataSize());}
    @Override public void mod2this      (int aRHS) {IntArrayVector rThis = thisVector_(); ARRAY.mapMod2This     (rThis.internalData(), rThis.internalDataShift(), aRHS, rThis.internalDataSize());}
    @Override public void lmod2this     (int aRHS) {IntArrayVector rThis = thisVector_(); ARRAY.mapLMod2This    (rThis.internalData(), rThis.internalDataShift(), aRHS, rThis.internalDataSize());}
    @Override public void map2this      (IntUnaryOperator aOpt) {IntArrayVector rThis = thisVector_(); ARRAY.mapDo2This(rThis.internalData(), rThis.internalDataShift(), rThis.internalDataSize(), aOpt);}
    
    @Override public IIntVector abs() {
        IntArrayVector tThis = thisVector_();
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        if (tDataL != null) ARRAY.mapAbs2Dest(tDataL, tThis.internalDataShift(), rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.mapAbs2Dest(tThis, rVector);
        return rVector;
    }
    @Override public void abs2this() {IntArrayVector rThis = thisVector_(); ARRAY.mapAbs2This(rThis.internalData(), rThis.internalDataShift(), rThis.internalDataSize());}
    @Override public IIntVector negative() {
        IntArrayVector tThis = thisVector_();
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        if (tDataL != null) ARRAY.mapNegative2Dest(tDataL, tThis.internalDataShift(), rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.mapNegative2Dest(tThis, rVector);
        return rVector;
    }
    @Override public void negative2this() {IntArrayVector rThis = thisVector_(); ARRAY.mapNegative2This(rThis.internalData(), rThis.internalDataShift(), rThis.internalDataSize());}
    
    @Override public void fill          (int aRHS) {IntArrayVector rThis = thisVector_(); ARRAY.mapFill2This(rThis.internalData(), rThis.internalDataShift(), aRHS, rThis.internalDataSize());}
    @Override public void fill          (IIntVector aRHS) {
        final IntArrayVector rThis = thisVector_();
        ebeCheck(rThis.size(), aRHS.size());
        int[] tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) ARRAY.ebeFill2This(rThis.internalData(), rThis.internalDataShift(), tDataR, IDataShell.internalDataShift(aRHS), rThis.internalDataSize());
        else DATA.ebeFill2This(rThis, aRHS);
    }
    @Override public void fill          (IIntVectorGetter aRHS) {IntArrayVector rThis = thisVector_(); ARRAY.vecFill2This (rThis.internalData(), rThis.internalDataShift(), rThis.internalDataSize(), aRHS);}
    @Override public void assign        (IntSupplier      aSup) {IntArrayVector rThis = thisVector_(); ARRAY.assign2This  (rThis.internalData(), rThis.internalDataShift(), rThis.internalDataSize(), aSup);}
    @Override public void forEach       (IntConsumer      aCon) {IntArrayVector rThis = thisVector_(); ARRAY.forEachOfThis(rThis.internalData(), rThis.internalDataShift(), rThis.internalDataSize(), aCon);}
    
    @Override public int    sum  ()                         {IntArrayVector tThis = thisVector_(); return ARRAY.sumOfThis  (tThis.internalData(), tThis.internalDataShift(), tThis.internalDataSize());}
    @Override public long   exsum()                         {IntArrayVector tThis = thisVector_(); return ARRAY.exsumOfThis(tThis.internalData(), tThis.internalDataShift(), tThis.internalDataSize());}
    @Override public double mean ()                         {IntArrayVector tThis = thisVector_(); return ARRAY.meanOfThis (tThis.internalData(), tThis.internalDataShift(), tThis.internalDataSize());}
    @Override public double prod ()                         {IntArrayVector tThis = thisVector_(); return ARRAY.prodOfThis (tThis.internalData(), tThis.internalDataShift(), tThis.internalDataSize());}
    @Override public int    max  ()                         {IntArrayVector tThis = thisVector_(); return ARRAY.maxOfThis  (tThis.internalData(), tThis.internalDataShift(), tThis.internalDataSize());}
    @Override public int    min  ()                         {IntArrayVector tThis = thisVector_(); return ARRAY.minOfThis  (tThis.internalData(), tThis.internalDataShift(), tThis.internalDataSize());}
    @Override public double stat(DoubleBinaryOperator aOpt) {IntArrayVector tThis = thisVector_(); return ARRAY.statOfThis (tThis.internalData(), tThis.internalDataShift(), tThis.internalDataSize(), aOpt);}
    
    
    @Override public IIntVector reverse() {
        IntArrayVector tThis = thisVector_();
        IntArrayVector rVector = newVector_();
        int[] tDataL = rVector.getIfHasSameOrderData(tThis);
        if (tDataL != null) ARRAY.reverse2Dest(tDataL, tThis.internalDataShift(), rVector.internalData(), rVector.internalDataShift(), rVector.internalDataSize());
        else DATA.reverse2Dest(tThis, rVector);
        return rVector;
    }
    
    /** 排序不自己实现 */
    @Override public void sort() {
        final IntArrayVector rThis = thisVector_();
        ARRAY.sort(rThis.internalData(), rThis.internalDataShift(), rThis.internalDataSize());
    }
    @Override public void biSort(ISwapper aSwapper) {
        final IntArrayVector rThis = thisVector_();
        final int tSize = rThis.internalDataSize();
        ARRAY.biSort(rThis.internalData(), rThis.internalDataShift(), tSize, aSwapper.undata(rThis));
    }
    
    
    /** 方便内部使用，减少一些重复代码 */
    private IntArrayVector newVector_() {return newVector_(thisVector_().size());}
    
    /** stuff to override */
    @Override protected abstract IntArrayVector thisVector_();
    @Override protected abstract IntArrayVector newVector_(int aSize);
}
