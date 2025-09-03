package jsex.nnap.basis;

import jse.code.UT;
import jse.code.collection.DoubleList;
import jse.code.collection.IntList;
import jse.math.IDataShell;
import jse.math.matrix.RowMatrix;
import jse.math.vector.DoubleArrayVector;
import jse.math.vector.Vectors;
import org.jetbrains.annotations.ApiStatus;
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
public class SphericalChebyshev extends WTypeBasis {
    final static int[] L3NCOLS = {0, 0, 2, 4, 9}, L3NCOLS_NOCROSS = {0, 0, 1, 1, 2};
    
    public final static int DEFAULT_NMAX = 5;
    public final static int DEFAULT_LMAX = 6;
    public final static int DEFAULT_L3MAX = 0;
    public final static boolean DEFAULT_NORADIAL = false;
    public final static boolean DEFAULT_L3CROSS = true;
    public final static double DEFAULT_RCUT = 6.0; // 现在默认值统一为 6
    
    final String @Nullable[] mSymbols;
    final int mLMax, mL3Max;
    final boolean mNoRadial, mL3Cross;
    final double mRCut;
    
    final int mSizeL, mSize;
    final int mLMaxMax, mLMAll;
    
    /** 一些缓存的中间变量，现在统一作为对象存储，对于这种大规模的缓存情况可以进一步提高效率 */
    private final IDataShell<double[]> mCnlm, mGradCnlm, mGradBnlm;
    private final IDataShell<double[]> mRnPx, mRnPy, mRnPz, mCheby2;
    private final IDataShell<double[]> mYPx, mYPy, mYPz, mYPphi, mYPtheta;
    
    private final DoubleList mNlBnlm = new DoubleList(1024);
    private final DoubleList mNlY = new DoubleList(1024);
    private final DoubleList mNlRn = new DoubleList(128);
    
    SphericalChebyshev(String @Nullable[] aSymbols, int aTypeNum, int aNMax, int aLMax, boolean aNoRadial, int aL3Max, boolean aL3Cross, double aRCut, int aWType, @Nullable RowMatrix aFuseWeight) {
        super(aTypeNum, aNMax, aWType, aFuseWeight);
        if (aLMax<0 || aLMax>8) throw new IllegalArgumentException("Input lmax MUST be in [0, 8], input: "+aLMax);
        if (aL3Max<0 || aL3Max>4) throw new IllegalArgumentException("Input l3max MUST be in [0, 4], input: "+aL3Max);
        mSymbols = aSymbols;
        mLMax = aLMax;
        mL3Max = aL3Max;
        mNoRadial = aNoRadial;
        mL3Cross = aL3Cross;
        mRCut = aRCut;
        
        mSizeL = (mNoRadial?mLMax:(mLMax+1)) + (mL3Cross?L3NCOLS:L3NCOLS_NOCROSS)[mL3Max];
        mSize = mSizeN*mSizeL;
        mLMaxMax = Math.max(mLMax, mL3Max);
        mLMAll = (mLMaxMax+1)*(mLMaxMax+1);
        
        mCnlm = Vectors.zeros(mSizeN*mLMAll);
        mGradCnlm = Vectors.zeros(mSizeN*mLMAll);
        mGradBnlm = Vectors.zeros((mNMax+1)*mLMAll);
        
        mRnPx = Vectors.zeros(mNMax+1);
        mRnPy = Vectors.zeros(mNMax+1);
        mRnPz = Vectors.zeros(mNMax+1);
        mCheby2 = Vectors.zeros(mNMax+1);
        
        mYPx = Vectors.zeros(mLMAll);
        mYPy = Vectors.zeros(mLMAll);
        mYPz = Vectors.zeros(mLMAll);
        mYPphi = Vectors.zeros(mLMAll);
        mYPtheta = Vectors.zeros(mLMAll);
    }
    /**
     * @param aSymbols 基组需要的元素排序
     * @param aNMax Chebyshev 多项式选取的最大阶数
     * @param aLMax 球谐函数中 l 选取的最大阶数
     * @param aRCut 截断半径
     */
    public SphericalChebyshev(String @NotNull[] aSymbols, int aNMax, int aLMax, double aRCut) {
        this(aSymbols, aSymbols.length, aNMax, aLMax, DEFAULT_NORADIAL, DEFAULT_L3MAX, DEFAULT_L3CROSS, aRCut, WTYPE_DEFAULT, null);
    }
    /**
     * @param aTypeNum 原子种类数目
     * @param aNMax Chebyshev 多项式选取的最大阶数
     * @param aLMax 球谐函数中 l 选取的最大阶数
     * @param aRCut 截断半径
     */
    public SphericalChebyshev(int aTypeNum, int aNMax, int aLMax, double aRCut) {
        this(null, aTypeNum, aNMax, aLMax, DEFAULT_NORADIAL, DEFAULT_L3MAX, DEFAULT_L3CROSS, aRCut, WTYPE_DEFAULT, null);
    }
    
    @Override public SphericalChebyshev threadSafeRef() {
        return new SphericalChebyshev(mSymbols, mTypeNum, mNMax, mLMax, mNoRadial, mL3Max, mL3Cross, mRCut, mWType, mFuseWeight);
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
        if (mFuseWeight!=null) rSaveTo.put("fuse_weight", mFuseWeight.asListRows());
    }
    
    @SuppressWarnings("rawtypes")
    public static SphericalChebyshev load(String @NotNull[] aSymbols, Map aMap) {
        int aTypeNum = aSymbols.length;
        int aNMax = ((Number)UT.Code.getWithDefault(aMap, DEFAULT_NMAX, "nmax")).intValue();
        int aWType = getWType_(aMap);
        RowMatrix aFuseWeight = getFuseWeight_(aMap, aWType, aTypeNum, aNMax);
        return new SphericalChebyshev(
            aSymbols, aTypeNum, aNMax,
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_LMAX, "lmax")).intValue(),
            (Boolean)UT.Code.getWithDefault(aMap, DEFAULT_NORADIAL, "noradial"),
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_L3MAX, "l3max")).intValue(),
            (Boolean)UT.Code.getWithDefault(aMap, DEFAULT_L3CROSS, "l3cross"),
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_RCUT, "rcut")).doubleValue(),
            aWType, aFuseWeight
        );
    }
    @SuppressWarnings("rawtypes")
    public static SphericalChebyshev load(int aTypeNum, Map aMap) {
        int aNMax = ((Number)UT.Code.getWithDefault(aMap, DEFAULT_NMAX, "nmax")).intValue();
        int aWType = getWType_(aMap);
        RowMatrix aFuseWeight = getFuseWeight_(aMap, aWType, aTypeNum, aNMax);
        return new SphericalChebyshev(
            null, aTypeNum, aNMax,
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_LMAX, "lmax")).intValue(),
            (Boolean)UT.Code.getWithDefault(aMap, DEFAULT_NORADIAL, "noradial"),
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_L3MAX, "l3max")).intValue(),
            (Boolean)UT.Code.getWithDefault(aMap, DEFAULT_L3CROSS, "l3cross"),
            ((Number)UT.Code.getWithDefault(aMap, DEFAULT_RCUT, "rcut")).doubleValue(),
            aWType, aFuseWeight
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
    
    @Override
    protected void eval_(DoubleList aNlDx, DoubleList aNlDy, DoubleList aNlDz, IntList aNlType, DoubleArrayVector rFp, boolean aBufferNl) {
        if (isShutdown()) throw new IllegalStateException("This Basis is dead");
        
        final int tNN = aNlDx.size();
        // 确保 Rn Y 的长度
        validSize_(mNlY, tNN*mLMAll);
        validSize_(mNlRn, tNN*(mNMax+1));
        
        // 现在直接计算基组
        eval0(aNlDx, aNlDy, aNlDz, aNlType, rFp, aBufferNl);
    }
    @Override @ApiStatus.Internal
    public void backward(DoubleList aNlDx, DoubleList aNlDy, DoubleList aNlDz, IntList aNlType, DoubleArrayVector aGradFp, DoubleArrayVector rGradPara) {
        if (isShutdown()) throw new IllegalStateException("This Basis is dead");
        
        // 如果不是 fuse 直接返回不走 native
        if (mWType != WTYPE_FUSE) return;
        
        final int tNN = aNlDx.size();
        // 确保 bnlm 的长度
        validSize_(mNlBnlm, tNN*(mNMax+1)*mLMAll);
        
        backward0(aNlDx, aNlDy, aNlDz, aNlType, aGradFp, rGradPara);
    }
    @Override
    protected void evalForceAccumulate_(DoubleList aNlDx, DoubleList aNlDy, DoubleList aNlDz, IntList aNlType,
                                        DoubleArrayVector aNNGrad, DoubleList rFx, DoubleList rFy, DoubleList rFz) {
        if (isShutdown()) throw new IllegalStateException("This Basis is dead");
        
        // 现在直接计算力
        evalForce0(aNlDx, aNlDy, aNlDz, aNlType, aNNGrad, rFx, rFy, rFz);
    }
    
    
    void eval0(IDataShell<double[]> aNlDx, IDataShell<double[]> aNlDy, IDataShell<double[]> aNlDz, IDataShell<int[]> aNlType, IDataShell<double[]> rFp, boolean aBufferNl) {
        int tNN = aNlDx.internalDataSize();
        eval1(aNlDx.internalDataWithLengthCheck(tNN, 0), aNlDy.internalDataWithLengthCheck(tNN, 0), aNlDz.internalDataWithLengthCheck(tNN, 0), aNlType.internalDataWithLengthCheck(tNN, 0), tNN,
              mNlRn.internalDataWithLengthCheck(tNN*(mNMax+1), 0), mNlY.internalDataWithLengthCheck(tNN*mLMAll, 0), mCnlm.internalDataWithLengthCheck(mSizeN*mLMAll, 0),
              rFp.internalDataWithLengthCheck(mSize), rFp.internalDataShift(),
              aBufferNl, mTypeNum, mRCut, mNMax, mLMax, mNoRadial, mL3Max, mL3Cross, mWType, mFuseWeight==null?null:mFuseWeight.internalDataWithLengthCheck(), mFuseSize);
    }
    private static native void eval1(double[] aNlDx, double[] aNlDy, double[] aNlDz, int[] aNlType, int aNN,
                                     double[] rNlRn, double[] rNlY, double[] rCnlm, double[] rFp, int aShiftFp,
                                     boolean aBufferNl, int aTypeNum, double aRCut, int aNMax, int aLMax, boolean aNoRadial, int aL3Max, boolean aL3Cross, int aWType, double[] aFuseWeight, int aFuseSize);
    
    void backward0(IDataShell<double[]> aNlDx, IDataShell<double[]> aNlDy, IDataShell<double[]> aNlDz, IDataShell<int[]> aNlType, IDataShell<double[]> aGradFp, IDataShell<double[]> rGradPara) {
        assert mFuseWeight != null;
        int tNN = aNlDx.internalDataSize();
        backward1(aNlDx.internalDataWithLengthCheck(tNN, 0), aNlDy.internalDataWithLengthCheck(tNN, 0), aNlDz.internalDataWithLengthCheck(tNN, 0), aNlType.internalDataWithLengthCheck(tNN, 0), tNN,
                  mRnPx.internalDataWithLengthCheck(mNMax+1, 0), mYPx.internalDataWithLengthCheck(mLMAll, 0), mCnlm.internalDataWithLengthCheck(mSizeN*mLMAll, 0), mNlBnlm.internalDataWithLengthCheck(tNN*(mNMax+1)*mLMAll, 0),
                  mGradCnlm.internalDataWithLengthCheck(mSizeN*mLMAll, 0), aGradFp.internalDataWithLengthCheck(mSize), aGradFp.internalDataShift(),
                  rGradPara.internalDataWithLengthCheck(mFuseWeight.internalDataSize()), rGradPara.internalDataShift(),
                  mTypeNum, mRCut, mNMax, mLMax, mNoRadial, mL3Max, mL3Cross, mWType, mFuseWeight.internalDataWithLengthCheck(), mFuseSize);
    }
    private static native void backward1(double[] aNlDx, double[] aNlDy, double[] aNlDz, int[] aNlType, int aNN,
                                         double[] rRn, double[] rY, double[] rCnlm, double[] rNlBnlm, double[] rGradCnlm,
                                         double[] aGradFp, int aShiftGradFp, double[] aGradPara, int aShiftGradPara,
                                         int aTypeNum, double aRCut, int aNMax, int aLMax, boolean aNoRadial, int aL3Max, boolean aL3Cross, int aWType, double[] aFuseWeight, int aFuseSize);
    
    void evalForce0(IDataShell<double[]> aNlDx, IDataShell<double[]> aNlDy, IDataShell<double[]> aNlDz, IDataShell<int[]> aNlType,
                    IDataShell<double[]> aNNGrad, IDataShell<double[]> rFx, IDataShell<double[]> rFy, IDataShell<double[]> rFz) {
        int tNN = aNlDx.internalDataSize();
        evalForce1(aNlDx.internalDataWithLengthCheck(tNN, 0), aNlDy.internalDataWithLengthCheck(tNN, 0), aNlDz.internalDataWithLengthCheck(tNN, 0), aNlType.internalDataWithLengthCheck(tNN, 0), tNN,
                   mNlRn.internalDataWithLengthCheck(tNN*(mNMax+1), 0), mRnPx.internalDataWithLengthCheck(mNMax+1, 0), mRnPy.internalDataWithLengthCheck(mNMax+1, 0), mRnPz.internalDataWithLengthCheck(mNMax+1, 0), mCheby2.internalDataWithLengthCheck(mNMax+1, 0),
                   mNlY.internalDataWithLengthCheck(tNN*mLMAll, 0), mYPtheta.internalDataWithLengthCheck(mLMAll, 0), mYPphi.internalDataWithLengthCheck(mLMAll, 0),
                   mYPx.internalDataWithLengthCheck(mLMAll, 0), mYPy.internalDataWithLengthCheck(mLMAll, 0), mYPz.internalDataWithLengthCheck(mLMAll, 0),
                   mCnlm.internalDataWithLengthCheck(mSizeN*mLMAll, 0), mGradCnlm.internalDataWithLengthCheck(mSizeN*mLMAll, 0), mGradBnlm.internalDataWithLengthCheck((mNMax+1)*mLMAll, 0),
                   aNNGrad.internalDataWithLengthCheck(mSize), aNNGrad.internalDataShift(), rFx.internalDataWithLengthCheck(tNN, 0), rFy.internalDataWithLengthCheck(tNN, 0), rFz.internalDataWithLengthCheck(tNN, 0),
                   mTypeNum, mRCut, mNMax, mLMax, mNoRadial, mL3Max, mL3Cross, mWType, mFuseWeight==null?null:mFuseWeight.internalDataWithLengthCheck(), mFuseSize);
    }
    private static native void evalForce1(double[] aNlDx, double[] aNlDy, double[] aNlDz, int[] aNlType, int aNN,
                                          double[] aNlRn, double[] rRnPx, double[] rRnPy, double[] rRnPz, double[] rCheby2,
                                          double[] aNlY, double[] rYPtheta, double[] rYPphi, double[] rYPx, double[] rYPy, double[] rYPz,
                                          double[] aCnlm, double[] rGradCnlm, double[] rGradBnlm,
                                          double[] aNNGrad, int aShiftFp, double[] rFx, double[] rFy, double[] rFz,
                                          int aTypeNum, double aRCut, int aNMax, int aLMax, boolean aNoRadial, int aL3Max, boolean aL3Cross, int aWType, double[] aFuseWeight, int aFuseSize);
}
