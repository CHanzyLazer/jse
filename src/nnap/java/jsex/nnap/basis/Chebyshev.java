package jsex.nnap.basis;

import jse.clib.DoubleCPointer;
import jse.clib.GrowableDoubleCPointer;
import jse.clib.GrowableIntCPointer;
import jse.clib.IntCPointer;
import jse.code.UT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 一种仅使用 Chebyshev 多项式将原子局域环境展开成一个基组的方法，
 * 主要用于作为机器学习的输入向量；这不会包含角向序，但是速度可以很快。
 * <p>
 * 为了中间变量缓存利用效率，此类相同实例线程不安全，而不同实例之间线程安全
 * <p>
 * 现在统一通过调用 c 并借助 avx 指令优化来得到最佳的性能
 * <p>
 * References:
 * <a href="https://link.springer.com/article/10.1007/s40843-024-2953-9">
 * Efficient and accurate simulation of vitrification in multi-component metallic liquids with neural-network potentials </a>
 * @author Su Rui, liqa
 */
public class Chebyshev extends NNAPWTypeBasis {
    public final static int DEFAULT_NMAX = 5;
    public final static double DEFAULT_RCUT = 6.0; // 现在默认值统一为 6
    
    final int mTypeNum;
    final String @Nullable[] mSymbols;
    final int mNMax;
    final double mRCut;
    final int mWType;
    
    final int mSize;
    private final BasisCachePointers mCPointers;
    
    Chebyshev(String @Nullable[] aSymbols, int aTypeNum, int aNMax, double aRCut, int aWType) {
        if (aTypeNum <= 0) throw new IllegalArgumentException("Inpute ntypes MUST be Positive, input: "+aTypeNum);
        if (aNMax < 0) throw new IllegalArgumentException("Input nmax MUST be Non-Negative, input: "+aNMax);
        if (!ALL_WTYPE.containsValue(aWType)) throw new IllegalArgumentException("Input wtype MUST be in {-1, 0, 1, 2, 3}, input: "+ aWType);
        mSymbols = aSymbols;
        mTypeNum = aTypeNum;
        mNMax = aNMax;
        mRCut = aRCut;
        mWType = aWType;
        
        mSize = sizeN_(mNMax, mTypeNum, mWType);
        
        // Rn, RnPx, RnPy, RnPz, Cheby2,
        // nlRn
        mCPointers = new BasisCachePointers(this, new GrowableDoubleCPointer[]{
            new GrowableDoubleCPointer(mNMax+1), // Rn
            new GrowableDoubleCPointer(mNMax+1), new GrowableDoubleCPointer(mNMax+1), new GrowableDoubleCPointer(mNMax+1), // RnPx, RnPy, RnPz
            new GrowableDoubleCPointer(mNMax), // Cheby2
            new GrowableDoubleCPointer(128) // nlRn
        }, new GrowableIntCPointer[0]);
    }
    /**
     * @param aSymbols 基组需要的元素排序
     * @param aNMax Chebyshev 多项式选取的最大阶数
     * @param aRCut 截断半径
     */
    public Chebyshev(String @NotNull[] aSymbols, int aNMax, double aRCut) {
        this(aSymbols, aSymbols.length, aNMax, aRCut, WTYPE_DEFAULT);
    }
    /**
     * @param aTypeNum 原子种类数目
     * @param aNMax Chebyshev 多项式选取的最大阶数
     * @param aRCut 截断半径
     */
    public Chebyshev(int aTypeNum, int aNMax, double aRCut) {
        this(null, aTypeNum, aNMax, aRCut, WTYPE_DEFAULT);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override public void save(Map rSaveTo) {
        rSaveTo.put("type", "chebyshev");
        rSaveTo.put("nmax", mNMax);
        rSaveTo.put("rcut", mRCut);
        rSaveTo.put("wtype", ALL_WTYPE.inverse().get(mWType));
    }
    
    @SuppressWarnings("rawtypes")
    public static Chebyshev load(String @NotNull[] aSymbols, Map aMap) {
        return new Chebyshev(
            aSymbols, aSymbols.length,
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_NMAX, "nmax")).intValue(),
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_RCUT, "rcut")).doubleValue(),
            getWType_(UT.Code.get(aMap, "wtype"))
        );
    }
    @SuppressWarnings("rawtypes")
    public static Chebyshev load(int aTypeNum, Map aMap) {
        return new Chebyshev(
            null, aTypeNum,
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_NMAX, "nmax")).intValue(),
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_RCUT, "rcut")).doubleValue(),
            getWType_(UT.Code.get(aMap, "wtype"))
        );
    }
    
    
    /** @return {@inheritDoc} */
    @Override public double rcut() {return mRCut;}
    /**
     * @return {@inheritDoc}；如果只有一个种类则为
     * {@code (nmax+1)(lmax+1)}，如果超过一个种类则为
     * {@code 2(nmax+1)(lmax+1)}
     */
    @Override public int size() {return mSize;}
    /** @return {@inheritDoc} */
    @Override public int atomTypeNumber() {return mTypeNum;}
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override public boolean hasSymbol() {return mSymbols!=null;}
    /**
     * {@inheritDoc}
     * @param aType
     * @return {@inheritDoc}
     */
    @Override public @Nullable String symbol(int aType) {return mSymbols==null ? null : mSymbols[aType-1];}
    
    @Override protected void shutdown_() {
        mCPointers.dispose();
    }
    
    @Override
    public void eval_(DoubleCPointer aNlDx, DoubleCPointer aNlDy, DoubleCPointer aNlDz, IntCPointer aNlType, int aNN, DoubleCPointer rFp) {
        if (isShutdown()) throw new IllegalStateException("This Basis is dead");
        
        // 现在直接计算基组
        eval0(aNlDx.ptr_(), aNlDy.ptr_(), aNlDz.ptr_(), aNlType.ptr_(), aNN,
              mCPointers.mDoublePointers[0].ptr_(), rFp.ptr_(),
              mTypeNum, mRCut, mNMax, mWType);
    }
    
    @Override
    public void evalPartial_(DoubleCPointer aNlDx, DoubleCPointer aNlDy, DoubleCPointer aNlDz, IntCPointer aNlType, int aNN,
                             DoubleCPointer rFp, int aSizeFp, int aShiftFp, DoubleCPointer rFpPx, DoubleCPointer rFpPy, DoubleCPointer rFpPz) {
        if (isShutdown()) throw new IllegalStateException("This Basis is dead");
        
        GrowableDoubleCPointer[] tPtrs = mCPointers.mDoublePointers;
        
        // 确保 Rn 的长度
        GrowableDoubleCPointer tNlRn = tPtrs[5];
        tNlRn.ensureCapacity(aNN*(mNMax+1));
        
        // 现在直接计算基组偏导
        evalPartial0(aNlDx.ptr_(), aNlDy.ptr_(), aNlDz.ptr_(), aNlType.ptr_(), aNN,
                     tNlRn.ptr_(), tPtrs[1].ptr_(), tPtrs[2].ptr_(), tPtrs[3].ptr_(), tPtrs[4].ptr_(),
                     rFp.ptr_(), aSizeFp, aShiftFp,
                     rFpPx.ptr_(), rFpPy.ptr_(), rFpPz.ptr_(),
                     mTypeNum, mRCut, mNMax, mWType);
    }
    
    private static native void eval0(long aNlDx, long aNlDy, long aNlDz, long aNlType, int aNN, long rRn, long rFp,
                                     int aTypeNum, double aRCut, int aNMax, int aWType);
    
    private static native void evalPartial0(long aNlDx, long aNlDy, long aNlDz, long aNlType, int aNN,
                                            long rNlRn, long rRnPx, long rRnPy, long rRnPz, long rCheby2,
                                            long rFp, int aSizeFp, int aShiftFp, long rFpPx, long rFpPy, long rFpPz,
                                            int aTypeNum, double aRCut, int aNMax, int aWType);
}
