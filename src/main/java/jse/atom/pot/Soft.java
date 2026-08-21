package jse.atom.pot;

import jse.atom.AbstractPairPotential;
import jse.code.collection.DoubleList;
import jse.code.collection.IntList;
import jse.math.MathEX;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * soft 势的 jse 实现，采用公式：{@code E = A[1 + cos(πr/rc)]}；
 * 主要用于将过近的原子推开
 * @author liqa
 */
public class Soft extends AbstractPairPotential {
    private final double mCutMax;
    private final double[][] mPrefactor, mCut, mCutsq;
    private final int mNumTypes;
    private final String @Nullable[] mSymbols;
    
    public Soft(double aPrefactor, double aRCut, int aNumThreads) {
        super(aNumThreads);
        mNumTypes = -1;
        mSymbols = null;
        mPrefactor = new double[][]{{aPrefactor}};
        mCut = new double[][]{{aRCut}};
        mCutsq = new double[][]{{aRCut*aRCut}};
        mCutMax = aRCut;
    }
    public Soft(double[][] aPrefactor, double[][] aRCut, String @Nullable[] aSymbols, int aNumThreads) {
        super(aNumThreads);
        mNumTypes = aRCut.length;
        if (aPrefactor.length !=mNumTypes) throw new IllegalArgumentException("Input A size MUST be the same size of RCut");
        if (aSymbols!=null && aSymbols.length!=mNumTypes) throw new IllegalArgumentException("Input Symbols size MUST be the same size of RCut");
        mSymbols = aSymbols;
        mPrefactor = new double[mNumTypes+1][mNumTypes+1];
        mCut = new double[mNumTypes+1][mNumTypes+1];
        mCutsq = new double[mNumTypes+1][mNumTypes+1];
        double tCutMax = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < mNumTypes; ++i) for (int j = 0; j <= i; ++j) {
            double tA = aPrefactor[i][j];
            mPrefactor[i+1][j+1] = tA;
            double tRCut = aRCut[i][j];
            mCut[i+1][j+1] = tRCut;
            mCutsq[i+1][j+1] = tRCut*tRCut;
            if (tRCut > tCutMax) tCutMax = tRCut;
        }
        mCutMax = tCutMax;
        for (int j = 2; j <= mNumTypes; ++j) for (int i = 1; i < j; ++i) {
            mPrefactor[i][j] = mPrefactor[j][i];
            mCut[i][j] = mCut[j][i];
            mCutsq[i][j] = mCutsq[j][i];
        }
    }
    /**
     * 创建一个 soft 势函数，{@code E = A[1 + cos(πr/rc)]}，
     * 不考虑原子种类都使用相同的参数
     * @param aPrefactor 公式中 {@code A} 值
     * @param aRCut 需要的截断半径值
     */
    public Soft(double aPrefactor, double aRCut) {
        this(aPrefactor, aRCut, 1);
    }
    /**
     * 创建一个 soft 势函数，{@code E = A[1 + cos(πr/rc)]}，
     * 不同原子种类使用不同的参数
     * @param aPrefactor 公式中的 {@code A} 值，{@code aPrefactor[i][j]} 记录元素种类
     * {@code i+1} 和 {@code j+1} 之间的值，只会读取 {@code j <= i} 的部分（下三角）
     * @param aRCut 需要的截断半径值，{@code aRCut[i][j]} 记录元素种类
     * {@code i+1} 和 {@code j+1} 之间的值，只会读取 {@code j <= i} 的部分（下三角）
     * @param aSymbols 可选的元素符号信息，如果输入则会根据此元素符号自动映射输入的原子数据，默认为 {@code null}
     */
    public Soft(double[][] aPrefactor, double[][] aRCut, String @Nullable[] aSymbols) {
        this(aPrefactor, aRCut, aSymbols, 1);
    }
    /**
     * 创建一个 soft 势函数，{@code E = A[1 + cos(πr/rc)]}，
     * 不同原子种类使用不同的参数
     * @param aPrefactor 公式中的 {@code A} 值，{@code aPrefactor[i][j]} 记录元素种类
     * {@code i+1} 和 {@code j+1} 之间的值，只会读取 {@code j <= i} 的部分（下三角）
     * @param aRCut 需要的截断半径值，{@code aRCut[i][j]} 记录元素种类
     * {@code i+1} 和 {@code j+1} 之间的值，只会读取 {@code j <= i} 的部分（下三角）
     */
    public Soft(double[][] aPrefactor, double[][] aRCut) {
        this(aPrefactor, aRCut, null);
    }
    
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
            double deng = mPrefactor[aCType][type] * (1.0 + Math.cos(MathEX.PI * Math.sqrt(rsq) / mCut[aCType][type]));
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
            double r = Math.sqrt(rsq);
            double arg = MathEX.PI * r / mCut[aCType][type];
            double fpair = r<=0.0 ? 0.0 : (mPrefactor[aCType][type] * Math.sin(arg) * MathEX.PI/mCut[aCType][type]/r);
            rGradNlDx.set(jj, (-0.5)*dx*fpair);
            rGradNlDy.set(jj, (-0.5)*dy*fpair);
            rGradNlDz.set(jj, (-0.5)*dz*fpair);
            double deng = mPrefactor[aCType][type] * (1.0 + Math.cos(arg));
            tEng += deng*0.5;
        }
        return tEng;
    }
}
