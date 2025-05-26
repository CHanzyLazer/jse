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
 * 一种基于 Chebyshev 多项式和球谐函数将原子局域环境展开成一个基组的方法，
 * 主要用于作为机器学习的输入向量；这是 NNAP 中默认使用的原子基组。
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
public class SphericalChebyshev extends NNAPWTypeBasis {
    final static int[] L3NCOLS = {0, 0, 2, 4, 9}, L3NCOLS_NOCROSS = {0, 0, 1, 1, 2};
    
    public final static int DEFAULT_NMAX = 5;
    public final static int DEFAULT_LMAX = 6;
    public final static int DEFAULT_L3MAX = 0;
    public final static boolean DEFAULT_NORADIAL = false;
    public final static boolean DEFAULT_L3CROSS = true;
    public final static double DEFAULT_RCUT = 6.0; // 现在默认值统一为 6
    
    final int mTypeNum;
    final String @Nullable[] mSymbols;
    final int mNMax, mLMax, mL3Max;
    final boolean mNoRadial, mL3Cross;
    final double mRCut;
    final int mWType;
    
    final int mSizeN, mSizeL, mSize;
    final int mLMaxMax, mLMAll;
    private final BasisCachePointers mCPointers;
    
    SphericalChebyshev(String @Nullable[] aSymbols, int aTypeNum, int aNMax, int aLMax, boolean aNoRadial, int aL3Max, boolean aL3Cross, double aRCut, int aWType) {
        if (aTypeNum <= 0) throw new IllegalArgumentException("Inpute ntypes MUST be Positive, input: "+aTypeNum);
        if (aNMax < 0) throw new IllegalArgumentException("Input nmax MUST be Non-Negative, input: "+aNMax);
        if (aLMax<0 || aLMax>20) throw new IllegalArgumentException("Input lmax MUST be in [0, 20], input: "+aLMax);
        if (aL3Max<0 || aL3Max>4) throw new IllegalArgumentException("Input l3max MUST be in [0, 4], input: "+aL3Max);
        if (!ALL_WTYPE.containsValue(aWType)) throw new IllegalArgumentException("Input wtype MUST be in {-1, 0, 1, 2, 3}, input: "+ aWType);
        mSymbols = aSymbols;
        mTypeNum = aTypeNum;
        mNMax = aNMax;
        mLMax = aLMax;
        mL3Max = aL3Max;
        mNoRadial = aNoRadial;
        mL3Cross = aL3Cross;
        mRCut = aRCut;
        mWType = aWType;
        
        mSizeN = sizeN_(mNMax, mTypeNum, mWType);
        mSizeL = (mNoRadial?mLMax:(mLMax+1)) + (mL3Cross?L3NCOLS:L3NCOLS_NOCROSS)[mL3Max];
        mSize = mSizeN*mSizeL;
        mLMaxMax = Math.max(mLMax, mL3Max);
        mLMAll = (mLMaxMax+1)*(mLMaxMax+1);
        
        // cnlm, cnlmPx, cnlmPy, cnlmPz,
        // Rn, RnPx, RnPy, RnPz, Cheby2,
        // Y, YPphi, YPtheta, YPx, YPy, YPz,
        // nlY, nlRn
        mCPointers = new BasisCachePointers(this, new GrowableDoubleCPointer[]{
            new GrowableDoubleCPointer(mSizeN*mLMAll), new GrowableDoubleCPointer(mLMAll), new GrowableDoubleCPointer(mLMAll), new GrowableDoubleCPointer(mLMAll), // cnlm, cnlmPx, cnlmPy, cnlmPz
            new GrowableDoubleCPointer(mNMax+1), new GrowableDoubleCPointer(mNMax+1), new GrowableDoubleCPointer(mNMax+1), new GrowableDoubleCPointer(mNMax+1), new GrowableDoubleCPointer(mNMax), // Rn, RnPx, RnPy, RnPz, Cheby2
            new GrowableDoubleCPointer(mLMAll), new GrowableDoubleCPointer(mLMAll), new GrowableDoubleCPointer(mLMAll), new GrowableDoubleCPointer(mLMAll), new GrowableDoubleCPointer(mLMAll), new GrowableDoubleCPointer(mLMAll), // Y, YPphi, YPtheta, YPx, YPy, YPz
            new GrowableDoubleCPointer(1024), new GrowableDoubleCPointer(128) // nlY, nlRn
        }, new GrowableIntCPointer[0]);
    }
    /**
     * @param aSymbols 基组需要的元素排序
     * @param aNMax Chebyshev 多项式选取的最大阶数
     * @param aLMax 球谐函数中 l 选取的最大阶数
     * @param aRCut 截断半径
     */
    public SphericalChebyshev(String @NotNull[] aSymbols, int aNMax, int aLMax, double aRCut) {
        this(aSymbols, aSymbols.length, aNMax, aLMax, DEFAULT_NORADIAL, DEFAULT_L3MAX, DEFAULT_L3CROSS, aRCut, WTYPE_DEFAULT);
    }
    /**
     * @param aTypeNum 原子种类数目
     * @param aNMax Chebyshev 多项式选取的最大阶数
     * @param aLMax 球谐函数中 l 选取的最大阶数
     * @param aRCut 截断半径
     */
    public SphericalChebyshev(int aTypeNum, int aNMax, int aLMax, double aRCut) {
        this(null, aTypeNum, aNMax, aLMax, DEFAULT_NORADIAL, DEFAULT_L3MAX, DEFAULT_L3CROSS, aRCut, WTYPE_DEFAULT);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override public void save(Map rSaveTo) {
        rSaveTo.put("type", "spherical_chebyshev");
        rSaveTo.put("nmax", mNMax);
        rSaveTo.put("lmax", mLMax);
        rSaveTo.put("noradial", mNoRadial);
        rSaveTo.put("l3max", mL3Max);
        rSaveTo.put("l3cross", mL3Cross);
        rSaveTo.put("rcut", mRCut);
        rSaveTo.put("wtype", ALL_WTYPE.inverse().get(mWType));
    }
    
    @SuppressWarnings("rawtypes")
    public static SphericalChebyshev load(String @NotNull[] aSymbols, Map aMap) {
        return new SphericalChebyshev(
            aSymbols, aSymbols.length,
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_NMAX, "nmax")).intValue(),
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_LMAX, "lmax")).intValue(),
            (Boolean)UT.Code.getWithDefault(aMap, DEFAULT_NORADIAL, "noradial"),
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_L3MAX, "l3max")).intValue(),
            (Boolean)UT.Code.getWithDefault(aMap, DEFAULT_L3CROSS, "l3cross"),
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_RCUT, "rcut")).doubleValue(),
            getWType_(UT.Code.get(aMap, "wtype"))
        );
    }
    @SuppressWarnings("rawtypes")
    public static SphericalChebyshev load(int aTypeNum, Map aMap) {
        return new SphericalChebyshev(
            null, aTypeNum,
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_NMAX, "nmax")).intValue(),
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_LMAX, "lmax")).intValue(),
            (Boolean)UT.Code.getWithDefault(aMap, DEFAULT_NORADIAL, "noradial"),
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_L3MAX, "l3max")).intValue(),
            (Boolean)UT.Code.getWithDefault(aMap, DEFAULT_L3CROSS, "l3cross"),
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
        
        GrowableDoubleCPointer[] tPtrs = mCPointers.mDoublePointers;
        
        // 现在直接计算基组
        eval0(aNlDx.ptr_(), aNlDy.ptr_(), aNlDz.ptr_(), aNlType.ptr_(), aNN,
              tPtrs[4].ptr_(), tPtrs[9].ptr_(), tPtrs[0].ptr_(), rFp.ptr_(),
              mTypeNum, mRCut, mNMax, mLMax, mNoRadial, mL3Max, mL3Cross, mWType);
    }
    
    @Override
    public void evalPartial_(DoubleCPointer aNlDx, DoubleCPointer aNlDy, DoubleCPointer aNlDz, IntCPointer aNlType, int aNN,
                             DoubleCPointer rFp, int aSizeFp, int aShiftFp, DoubleCPointer rFpPx, DoubleCPointer rFpPy, DoubleCPointer rFpPz) {
        if (isShutdown()) throw new IllegalStateException("This Basis is dead");
        
        GrowableDoubleCPointer[] tPtrs = mCPointers.mDoublePointers;
        
        // 确保 Rn Y 的长度
        GrowableDoubleCPointer tNlY = tPtrs[15];
        GrowableDoubleCPointer tNlRn = tPtrs[16];
        tNlY.ensureCapacity(aNN*mLMAll);
        tNlRn.ensureCapacity(aNN*(mNMax+1));
        
        // 现在直接计算基组偏导
        evalPartial0(aNlDx.ptr_(), aNlDy.ptr_(), aNlDz.ptr_(), aNlType.ptr_(), aNN,
                     tNlRn.ptr_(), tPtrs[5].ptr_(), tPtrs[6].ptr_(), tPtrs[7].ptr_(), tPtrs[8].ptr_(),
                     tNlY.ptr_(), tPtrs[10].ptr_(), tPtrs[11].ptr_(), tPtrs[12].ptr_(), tPtrs[13].ptr_(), tPtrs[14].ptr_(),
                     tPtrs[0].ptr_(), tPtrs[1].ptr_(), tPtrs[2].ptr_(), tPtrs[3].ptr_(),
                     rFp.ptr_(), aSizeFp, aShiftFp, rFpPx.ptr_(), rFpPy.ptr_(), rFpPz.ptr_(),
                     mTypeNum, mRCut, mNMax, mLMax, mNoRadial, mL3Max, mL3Cross, mWType);
    }
    
    private static native void eval0(long aNlDx, long aNlDy, long aNlDz, long aNlType, int aNN,
                                     long rRn, long rY, long rCnlm, long rFp,
                                     int aTypeNum, double aRCut, int aNMax, int aLMax, boolean aNoRadial, int aL3Max, boolean aL3Cross, int aWType);
    
    private static native void evalPartial0(long aNlDx, long aNlDy, long aNlDz, long aNlType, int aNN,
                                            long rNlRn, long rRnPx, long rRnPy, long rRnPz, long rCheby2,
                                            long rNlY, long rYPtheta, long rYPphi, long rYPx, long rYPy, long rYPz,
                                            long rCnlm, long rCnlmPx, long rCnlmPy, long rCnlmPz,
                                            long rFp, int aSizeFp, int aShiftFp, long rFpPx, long rFpPy, long rFpPz,
                                            int aTypeNum, double aRCut, int aNMax, int aLMax, boolean aNoRadial, int aL3Max, boolean aL3Cross, int aWType);
}
