package jse.atom.pot;

import jse.atom.AbstractPairPotential;
import jse.code.collection.DoubleList;
import jse.code.collection.IntList;
import jse.math.MathEX;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * LJ 势（Lennard-Jones）的 jse 实现，采用公式：{@code E = 4ε[(σ/r)^12 - (σ/r)^6]}；
 * 默认情况下不对截断处进行能量的 shift。
 * @author liqa
 */
public class LJ extends AbstractPairPotential {
    private final double mCutMax;
    private final double[][] mCutsq;
    private final double[][] mLJ1, mLJ2, mLJ3, mLJ4;
    private final double[][] mOffset;
    private final int mNumTypes;
    private final String @Nullable[] mSymbols;
    
    public LJ(double aEpsilon, double aSigma, double aRCut, int aNumThreads) {
        super(aNumThreads);
        mNumTypes = -1;
        mSymbols = null;
        mLJ1 = new double[][]{{48.0 * aEpsilon * MathEX.Code.pow12(aSigma)}};
        mLJ2 = new double[][]{{24.0 * aEpsilon * MathEX.Code.pow6( aSigma)}};
        mLJ3 = new double[][]{{ 4.0 * aEpsilon * MathEX.Code.pow12(aSigma)}};
        mLJ4 = new double[][]{{ 4.0 * aEpsilon * MathEX.Code.pow6( aSigma)}};
        mCutsq = new double[][]{{aRCut*aRCut}};
        double tRatio = aSigma / aRCut;
        mOffset = new double[][]{{4.0 * aEpsilon * (MathEX.Code.pow12(tRatio) - MathEX.Code.pow6(tRatio))}};
        mCutMax = aRCut;
    }
    public LJ(double[][] aEpsilon, double[][] aSigma, double[][] aRCut, String @Nullable[] aSymbols, int aNumThreads) {
        super(aNumThreads);
        mNumTypes = aRCut.length;
        if (aSigma.length != mNumTypes) throw new IllegalArgumentException("Input Sigma size MUST be the same size of RCut");
        if (aEpsilon.length != mNumTypes) throw new IllegalArgumentException("Input Epsilon size MUST be the same size of RCut");
        if (aSymbols!=null && aSymbols.length!=mNumTypes) throw new IllegalArgumentException("Input Symbols size MUST be the same size of RCut");
        mSymbols = aSymbols;
        mLJ1 = new double[mNumTypes+1][mNumTypes+1];
        mLJ2 = new double[mNumTypes+1][mNumTypes+1];
        mLJ3 = new double[mNumTypes+1][mNumTypes+1];
        mLJ4 = new double[mNumTypes+1][mNumTypes+1];
        mCutsq = new double[mNumTypes+1][mNumTypes+1];
        mOffset = new double[mNumTypes+1][mNumTypes+1];
        double tCutMax = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < mNumTypes; ++i) for (int j = 0; j <= i; ++j) {
            double tEpsilon = aEpsilon[i][j];
            double tSigma = aSigma[i][j];
            mLJ1[i+1][j+1] = 48.0 * tEpsilon * MathEX.Code.pow12(tSigma);
            mLJ2[i+1][j+1] = 24.0 * tEpsilon * MathEX.Code.pow6( tSigma);
            mLJ3[i+1][j+1] =  4.0 * tEpsilon * MathEX.Code.pow12(tSigma);
            mLJ4[i+1][j+1] =  4.0 * tEpsilon * MathEX.Code.pow6( tSigma);
            double tRCut = aRCut[i][j];
            mCutsq[i+1][j+1] =  tRCut*tRCut;
            double tRatio = tSigma / tRCut;
            mOffset[i+1][j+1] = 4.0 * tEpsilon * (MathEX.Code.pow12(tRatio) - MathEX.Code.pow6(tRatio));
            if (tRCut > tCutMax) tCutMax = tRCut;
        }
        mCutMax = tCutMax;
        for (int j = 2; j <= mNumTypes; ++j) for (int i = 1; i < j; ++i) {
            mLJ1[i][j] = mLJ1[j][i];
            mLJ2[i][j] = mLJ2[j][i];
            mLJ3[i][j] = mLJ3[j][i];
            mLJ4[i][j] = mLJ4[j][i];
            mCutsq[i][j] = mCutsq[j][i];
            mOffset[i][j] = mOffset[j][i];
        }
    }
    /**
     * 创建一个 LJ 势函数，{@code E = 4ε[(σ/r)^12 - (σ/r)^6]}，
     * 不考虑原子种类都使用相同的参数
     * @param aEpsilon 公式中的 {@code ε} 值
     * @param aSigma 公式中的 {@code σ} 值
     * @param aRCut 需要的截断半径值
     */
    public LJ(double aEpsilon, double aSigma, double aRCut) {
        this(aEpsilon, aSigma, aRCut, 1);
    }
    /**
     * 创建一个 LJ 势函数，{@code E = 4ε[(σ/r)^12 - (σ/r)^6]}，
     * 不同原子种类使用不同的参数
     * @param aEpsilon 公式中的 {@code ε} 值，{@code aEpsilon[i][j]} 记录元素种类
     * {@code i+1} 和 {@code j+1} 之间的值，只会读取 {@code j <= i} 的部分（下三角）
     * @param aSigma 公式中的 {@code σ} 值，{@code aSigma[i][j]} 记录元素种类
     * {@code i+1} 和 {@code j+1} 之间的值，只会读取 {@code j <= i} 的部分（下三角）
     * @param aRCut 需要的截断半径值，{@code aRCut[i][j]} 记录元素种类
     * {@code i+1} 和 {@code j+1} 之间的值，只会读取 {@code j <= i} 的部分（下三角）
     * @param aSymbols 可选的元素符号信息，如果输入则会根据此元素符号自动映射输入的原子数据，默认为 {@code null}
     */
    public LJ(double[][] aEpsilon, double[][] aSigma, double[][] aRCut, String @Nullable[] aSymbols) {
        this(aEpsilon, aSigma, aRCut, aSymbols, 1);
    }
    /**
     * 创建一个 LJ 势函数，{@code E = 4ε[(σ/r)^12 - (σ/r)^6]}，
     * 不同原子种类使用不同的参数
     * @param aEpsilon 公式中的 {@code ε} 值，{@code aEpsilon[i][j]} 记录元素种类
     * {@code i+1} 和 {@code j+1} 之间的值，只会读取 {@code j <= i} 的部分（下三角）
     * @param aSigma 公式中的 {@code σ} 值，{@code aSigma[i][j]} 记录元素种类
     * {@code i+1} 和 {@code j+1} 之间的值，只会读取 {@code j <= i} 的部分（下三角）
     * @param aRCut 需要的截断半径值，{@code aRCut[i][j]} 记录元素种类
     * {@code i+1} 和 {@code j+1} 之间的值，只会读取 {@code j <= i} 的部分（下三角）
     */
    public LJ(double[][] aEpsilon, double[][] aSigma, double[][] aRCut) {
        this(aEpsilon, aSigma, aRCut, null);
    }
    
    private boolean mShift = false;
    /** @return 此 LJ 势是否势进行了能量平移，保证截断处能量为 {@code 0}，默认为 {@code false} */
    public boolean shift() {return mShift;}
    /**
     * 设置此 LJ 势进行能量平移，保证截断处能量为 {@code 0}
     * @return 自身方便链式调用
     */
    public LJ setShift() {return setShift(true);}
    /**
     * 设置此 LJ 势是否进行能量平移，保证截断处能量为 {@code 0}
     * @param aShift 是否进行能量平移，默认为 {@code false}
     * @return 自身方便链式调用
     */
    public LJ setShift(boolean aShift) {mShift = aShift; return this;}
    
    /** @return {@inheritDoc} */
    @Override public int ntypes() {return mNumTypes;}
    /** @return {@inheritDoc} */
    @Override public boolean hasSymbol() {return mSymbols!=null;}
    /**
     * {@inheritDoc}
     * @param aType {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override public @Nullable String symbol(int aType) {return mSymbols==null ? null : mSymbols[aType-1];}
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override public double rcutMax() {return mCutMax;}
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override public boolean manybody() {return false;}
    
    
    @ApiStatus.Experimental @Override
    public double calEnergySingle(int aThreadID, int aCType,
                                  DoubleList aNlDx, DoubleList aNlDy, DoubleList aNlDz, IntList aNlType) {
        if (mNumTypes <= 0) aCType = 0;
        checkType(aCType);
        double tEng = 0.0;
        final int tNlSize = aNlDx.size();
        for (int jj = 0; jj < tNlSize; ++jj) {
            int type = (mNumTypes<=0) ? 0 : aNlType.get(jj);
            double dx = aNlDx.get(jj);
            double dy = aNlDy.get(jj);
            double dz = aNlDz.get(jj);
            double rsq = dx*dx + dy*dy + dz*dz;
            if (rsq >= mCutsq[aCType][type]) continue;
            double r2inv = 1.0 / rsq;
            double r6inv = r2inv*r2inv*r2inv;
            double deng = r6inv*(mLJ3[aCType][type]*r6inv - mLJ4[aCType][type]);
            if (mShift) deng -= mOffset[aCType][type];
            tEng += deng*0.5;
        }
        return tEng;
    }
    @ApiStatus.Experimental @Override
    public double calEnergyForceSingle(int aThreadID, int aCType,
                                       DoubleList aNlDx, DoubleList aNlDy, DoubleList aNlDz, IntList aNlType,
                                       DoubleList rGradNlDx, DoubleList rGradNlDy, DoubleList rGradNlDz) {
        if (mNumTypes <= 0) aCType = 0;
        checkType(aCType);
        double tEng = 0.0;
        final int tNlSize = aNlDx.size();
        for (int jj = 0; jj < tNlSize; ++jj) {
            int type = (mNumTypes<=0) ? 0 : aNlType.get(jj);
            double dx = aNlDx.get(jj);
            double dy = aNlDy.get(jj);
            double dz = aNlDz.get(jj);
            double rsq = dx*dx + dy*dy + dz*dz;
            if (rsq >= mCutsq[aCType][type]) continue;
            double r2inv = 1.0 / rsq;
            double r6inv = r2inv*r2inv*r2inv;
            double fpair = r2inv*r6inv*(mLJ1[aCType][type]*r6inv - mLJ2[aCType][type]);
            rGradNlDx.set(jj, (-0.5)*dx*fpair);
            rGradNlDy.set(jj, (-0.5)*dy*fpair);
            rGradNlDz.set(jj, (-0.5)*dz*fpair);
            double deng = r6inv*(mLJ3[aCType][type]*r6inv - mLJ4[aCType][type]);
            if (mShift) deng -= mOffset[aCType][type];
            tEng += deng*0.5;
        }
        return tEng;
    }
}
