package jse.atom.pot;

import jse.atom.AbstractPairPotential;
import jse.code.collection.DoubleList;
import jse.code.collection.DoubleWrapper;
import jse.code.collection.IntList;
import jse.math.MathEX;
import jse.math.vector.Vector;
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
            fpair *= 0.5;
            rGradNlDx.set(jj, dx*fpair);
            rGradNlDy.set(jj, dy*fpair);
            rGradNlDz.set(jj, dz*fpair);
            double deng = mPrefactor[aCType][type] * (1.0 + Math.cos(arg));
            tEng += deng*0.5;
        }
        return tEng;
    }
    
    @Override public void calculate(boolean aRequireTotalEnergy, boolean aRequirePreAtomEnergy, boolean aRequireForce, boolean aRequireTotalStress, boolean aRequirePreAtomStress) throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        // 判断需要的计算等级
        final boolean tCalEnergyForce = aRequireForce || aRequireTotalStress || aRequirePreAtomStress;
        final boolean tCalEnergy = (!tCalEnergyForce) && (aRequireTotalEnergy || aRequirePreAtomEnergy);
        // 什么都不用计算的情况
        if (!tCalEnergy && !tCalEnergyForce) return;
        // 构建近邻列表，顺便会检查是否执行了 setData
        mNl.setRCut(rcutMax()).build();
        // 缓存初始化
        initBufPar(false, aRequireTotalEnergy, aRequirePreAtomEnergy, aRequireForce, aRequireTotalStress, aRequirePreAtomStress);
        if (tCalEnergy) {
            mPool.parfor(mNumAtoms, (i, threadID) -> {
                int ctype = (mNumTypes<=0) ? 0 : mTypeMap.applyAsInt(mNl.typeAt(i));
                checkType(ctype);
                final DoubleWrapper tEnergy = aRequireTotalEnergy ? mEnergyPar[threadID] : null;
                final Vector tEnergies = aRequirePreAtomEnergy ? mEnergiesPar[threadID] : null;
                mNl.forEach(i, true, (dx, dy, dz, j) -> {
                    int type = (mNumTypes<=0) ? 0 : mTypeMap.applyAsInt(mNl.typeAt(j));
                    checkType(type);
                    double rsq = dx*dx + dy*dy + dz*dz;
                    if (rsq >= mCutsq[ctype][type]) return;
                    double deng = mPrefactor[ctype][type] * (1.0 + Math.cos(MathEX.PI * Math.sqrt(rsq) / mCut[ctype][type]));
                    if (aRequireTotalEnergy) {
                        tEnergy.mValue += deng;
                    }
                    if (aRequirePreAtomEnergy) {
                        tEnergies.add(i, deng*0.5);
                        tEnergies.add(j, deng*0.5);
                    }
                });
            });
        } else {
            final boolean tCentroid = centroidPerAtomStressSupport();
            mPool.parfor(mNumAtoms, (i, threadID) -> {
                int ctype = (mNumTypes<=0) ? 0 : mTypeMap.applyAsInt(mNl.typeAt(i));
                checkType(ctype);
                final DoubleWrapper tEnergy = aRequireTotalEnergy ? mEnergyPar[threadID] : null;
                final Vector tEnergies = aRequirePreAtomEnergy ? mEnergiesPar[threadID] : null;
                final Vector tForcesX = aRequireForce ? mForcesXPar[threadID] : null;
                final Vector tForcesY = aRequireForce ? mForcesYPar[threadID] : null;
                final Vector tForcesZ = aRequireForce ? mForcesZPar[threadID] : null;
                final DoubleWrapper tVirialXX = aRequireTotalStress ? mVirialXXPar[threadID] : null;
                final DoubleWrapper tVirialYY = aRequireTotalStress ? mVirialYYPar[threadID] : null;
                final DoubleWrapper tVirialZZ = aRequireTotalStress ? mVirialZZPar[threadID] : null;
                final DoubleWrapper tVirialXY = aRequireTotalStress ? mVirialXYPar[threadID] : null;
                final DoubleWrapper tVirialXZ = aRequireTotalStress ? mVirialXZPar[threadID] : null;
                final DoubleWrapper tVirialYZ = aRequireTotalStress ? mVirialYZPar[threadID] : null;
                final Vector tVirialsXX = aRequirePreAtomStress ? mVirialsXXPar[threadID] : null;
                final Vector tVirialsYY = aRequirePreAtomStress ? mVirialsYYPar[threadID] : null;
                final Vector tVirialsZZ = aRequirePreAtomStress ? mVirialsZZPar[threadID] : null;
                final Vector tVirialsXY = aRequirePreAtomStress ? mVirialsXYPar[threadID] : null;
                final Vector tVirialsXZ = aRequirePreAtomStress ? mVirialsXZPar[threadID] : null;
                final Vector tVirialsYZ = aRequirePreAtomStress ? mVirialsYZPar[threadID] : null;
                final Vector tVirialsYX = (tCentroid && aRequirePreAtomStress) ? mVirialsYXPar[threadID] : null;
                final Vector tVirialsZX = (tCentroid && aRequirePreAtomStress) ? mVirialsZXPar[threadID] : null;
                final Vector tVirialsZY = (tCentroid && aRequirePreAtomStress) ? mVirialsZYPar[threadID] : null;
                mNl.forEach(i, true, (dx, dy, dz, j) -> {
                    int type = (mNumTypes<=0) ? 0 : mTypeMap.applyAsInt(mNl.typeAt(j));
                    checkType(type);
                    double rsq = dx*dx + dy*dy + dz*dz;
                    if (rsq >= mCutsq[ctype][type]) return;
                    double r = Math.sqrt(rsq);
                    double arg = MathEX.PI * r / mCut[ctype][type];
                    double fpair = r<=0.0 ? 0.0 : (mPrefactor[ctype][type] * Math.sin(arg) * MathEX.PI/mCut[ctype][type]/r);
                    double fx = dx*fpair;
                    double fy = dy*fpair;
                    double fz = dz*fpair;
                    if (aRequireTotalEnergy || aRequirePreAtomEnergy) {
                        double deng = mPrefactor[ctype][type] * (1.0 + Math.cos(arg));
                        if (aRequireTotalEnergy) {
                            tEnergy.mValue += deng;
                        }
                        if (aRequirePreAtomEnergy) {
                            tEnergies.add(i, deng*0.5);
                            tEnergies.add(j, deng*0.5);
                        }
                    }
                    if (aRequireForce) {
                        tForcesX.add(i, -fx); tForcesX.add(j, fx);
                        tForcesY.add(i, -fy); tForcesY.add(j, fy);
                        tForcesZ.add(i, -fz); tForcesZ.add(j, fz);
                    }
                    if (aRequireTotalStress || aRequirePreAtomStress) {
                        double vxx = dx*fx, vyy = dy*fy, vzz = dz*fz;
                        double vxy = dx*fy, vxz = dx*fz, vyz = dy*fz;
                        if (aRequireTotalStress) {
                            tVirialXX.mValue += vxx;
                            tVirialYY.mValue += vyy;
                            tVirialZZ.mValue += vzz;
                            tVirialXY.mValue += vxy;
                            tVirialXZ.mValue += vxz;
                            tVirialYZ.mValue += vyz;
                        }
                        // GPUMD 给出的更具对称性的形式要求累加到近邻的 j 上
                        if (aRequirePreAtomStress) {
                            tVirialsXX.add(i, vxx*0.5); tVirialsXX.add(j, vxx*0.5);
                            tVirialsYY.add(i, vyy*0.5); tVirialsYY.add(j, vyy*0.5);
                            tVirialsZZ.add(i, vzz*0.5); tVirialsZZ.add(j, vzz*0.5);
                            tVirialsXY.add(i, vxy*0.5); tVirialsXY.add(j, vxy*0.5);
                            tVirialsXZ.add(i, vxz*0.5); tVirialsXZ.add(j, vxz*0.5);
                            tVirialsYZ.add(i, vyz*0.5); tVirialsYZ.add(j, vyz*0.5);
                            if (tCentroid) {
                                double vyx = dy*fx, vzx = dz*fx, vzy = dz*fy;
                                tVirialsYX.add(i, vyx*0.5); tVirialsYX.add(j, vyx*0.5);
                                tVirialsZX.add(i, vzx*0.5); tVirialsZX.add(j, vzx*0.5);
                                tVirialsZY.add(i, vzy*0.5); tVirialsZY.add(j, vzy*0.5);
                            }
                        }
                    }
                });
            });
        }
        collectBufPar(aRequireTotalEnergy, aRequirePreAtomEnergy, aRequireForce, aRequireTotalStress, aRequirePreAtomStress);
    }
}
