package jsex.nnap.basis;

import jse.clib.DoubleCPointer;
import jse.clib.GrowableDoubleCPointer;
import jse.clib.GrowableIntCPointer;
import jse.clib.IntCPointer;

import java.util.Map;

/**
 * 基于其他元素基组的一个镜像基组，其对于自身元素和对应的镜像元素种类会进行交换
 * <p>
 * 目前主要用于实现 ising 模型
 * @author liqa
 */
public class Mirror extends Basis {
    
    private final Basis mMirrorBasis;
    private final int mMirrorType, mThisType;
    public Mirror(Basis aMirrorBasis, int aMirrorType, int aThisType) {
        if (aMirrorBasis instanceof Mirror) throw new IllegalArgumentException("MirrorBasis MUST NOT be Mirror");
        mMirrorBasis = aMirrorBasis;
        mMirrorType = aMirrorType;
        mThisType = aThisType;
        
        mCPointers = new BasisCachePointers(this, new GrowableDoubleCPointer[0], new GrowableIntCPointer[] {
            new GrowableIntCPointer(16)
        });
    }
    public Basis mirrorBasis() {return mMirrorBasis;}
    public int mirrorType() {return mMirrorType;}
    public int thisType() {return mThisType;}
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override public void save(Map rSaveTo) {
        rSaveTo.put("type", "mirror");
        rSaveTo.put("mirror", mMirrorType);
    }
    @SuppressWarnings("rawtypes")
    public static Mirror load(Basis aMirrorBasis, int aThisType, Map aMap) {
        Object tMirror = aMap.get("mirror");
        if (tMirror == null) throw new IllegalArgumentException("Key `mirror` required for mirror load");
        int tMirrorType = ((Number)tMirror).intValue();
        return new Mirror(aMirrorBasis, tMirrorType, aThisType);
    }
    
    @Override public double rcut() {return mMirrorBasis.rcut();}
    @Override public int size() {return mMirrorBasis.size();}
    @Override public int atomTypeNumber() {return mMirrorBasis.atomTypeNumber();}
    @Override public boolean hasSymbol() {return mMirrorBasis.hasSymbol();}
    @Override public String symbol(int aType) {return mMirrorBasis.symbol(aType);}
    
    @Override protected void shutdown_() {
        mCPointers.dispose();
        mMirrorBasis.shutdown();
    }
    
    private final BasisCachePointers mCPointers;
    private IntCPointer buildNlType0(IntCPointer aNlType, int aNN) {
        GrowableIntCPointer tMirrorNlType = mCPointers.mIntPointers[0];
        tMirrorNlType.ensureCapacity(aNN);
        buildNlType1(aNlType.ptr_(), tMirrorNlType.ptr_(), aNN, mMirrorType, mThisType);
        return tMirrorNlType;
    }
    private static native void buildNlType1(long aNlType, long rMirrorNlType, int aNN, int aMirrorType, int aThisType);
    
    @Override
    public void eval_(DoubleCPointer aNlDx, DoubleCPointer aNlDy, DoubleCPointer aNlDz, IntCPointer aNlType, int aNN, DoubleCPointer rFp) {
        if (isShutdown()) throw new IllegalStateException("This Basis is dead");
        mMirrorBasis.eval_(aNlDx, aNlDy, aNlDz, buildNlType0(aNlType, aNN), aNN, rFp);
    }
    @Override
    public void evalPartial_(DoubleCPointer aNlDx, DoubleCPointer aNlDy, DoubleCPointer aNlDz, IntCPointer aNlType, int aNN,
                             DoubleCPointer rFp, int aSizeFp, int aShiftFp, DoubleCPointer rFpPx, DoubleCPointer rFpPy, DoubleCPointer rFpPz) {
        if (isShutdown()) throw new IllegalStateException("This Basis is dead");
        mMirrorBasis.evalPartial_(aNlDx, aNlDy, aNlDz, buildNlType0(aNlType, aNN), aNN, rFp, aSizeFp, aShiftFp, rFpPx, rFpPy, rFpPz);
    }
}
