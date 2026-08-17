package jse.atom;

import jse.code.collection.DoubleList;
import jse.code.collection.DoubleWrapper;
import jse.code.collection.IntList;
import jse.math.MathEX;
import jse.math.vector.Vector;
import jse.parallel.ParforThreadPool;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;


public abstract class AbstractPairPotential extends AbstractPotential implements IPairPotential {
    protected final NeighborListGetter mNl;
    protected final ParforThreadPool mPool;
    protected AbstractPairPotential(int aNumThreads) {
        mPool = new ParforThreadPool(aNumThreads);
        mNl = new NeighborListGetter();
    }
    
    private boolean mDead = false;
    @Override public void close() throws Exception {
        if (mDead) return;
        mDead = true;
        mPool.close();
    }
    public boolean isClosed() {
        return mDead;
    }
    @Override public final int nthreads() {
        return mPool.nthreads();
    }
    
    @Override @ApiStatus.Internal
    public final ParforThreadPool pool_() {
        return mPool;
    }
    @Override @ApiStatus.Internal
    public final NeighborListGetter nl_() {
        return mNl;
    }
    protected IntUnaryOperator mTypeMap = type->type;
    @Override public AbstractPairPotential setData(IAtomData aData) throws Exception {
        super.setData(aData);
        mNl.setData(aData);
        if (hasSymbol()) mTypeMap = typeMap(aData);
        return this;
    }
    
    protected void initDo(int aThreadID) throws Exception {}
    protected void finalDo(int aThreadID) throws Exception {}
    
    
    protected DoubleList[] mNlDxPar = null, mNlDyPar = null, mNlDzPar = null;
    protected IntList[] mNlTypePar = null, mNlIdxPar = null;
    protected DoubleList[] mGradNlDxPar = null, mGradNlDyPar = null, mGradNlDzPar = null;
    
    protected Vector[] mEnergiesPar = null;
    protected Vector[] mForcesXPar = null, mForcesYPar = null, mForcesZPar = null;
    protected Vector[] mVirialsXXPar = null, mVirialsYYPar = null, mVirialsZZPar = null;
    protected Vector[] mVirialsXYPar = null, mVirialsXZPar = null, mVirialsYZPar = null;
    protected Vector[] mVirialsYXPar = null, mVirialsZXPar = null, mVirialsZYPar = null;
    protected DoubleWrapper[] mEnergyPar = null;
    protected DoubleWrapper[] mVirialXXPar = null, mVirialYYPar = null, mVirialZZPar = null;
    protected DoubleWrapper[] mVirialXYPar = null, mVirialXZPar = null, mVirialYZPar = null;
    private final List<DoubleList> mEnergiesParRaw = new ArrayList<>();
    private final List<DoubleList> mForcesParRaw = new ArrayList<>();
    private final List<DoubleList> mVirialsParRaw = new ArrayList<>();
    
    protected final void checkType(int aType) {
        final int tNumTypes = ntypes();
        if (tNumTypes>0 && aType>tNumTypes) {
            throw new IllegalArgumentException("Exist type ("+aType+") greater than the input ntypes ("+tNumTypes+")");
        }
    }
    protected final void initBufNl(int aThreadID, int aI, boolean aRequireForce) {
        final DoubleList rNlDx = mNlDxPar[aThreadID], rNlDy = mNlDyPar[aThreadID], rNlDz = mNlDzPar[aThreadID];
        final IntList rNlType = mNlTypePar[aThreadID], rNlIdx = mNlIdxPar[aThreadID];
        rNlDx.clear(); rNlDy.clear(); rNlDz.clear();
        rNlType.clear(); rNlIdx.clear();
        if (typewiseCutoff()) {
            final double tRCutSq = MathEX.Code.pow2(rcut(mTypeMap.applyAsInt(mNl.typeAt(aI))));
            mNl.forEach(aI, (dx, dy, dz, idx) -> {
                double rsq = dx*dx + dy*dy + dz*dz;
                if (rsq >= tRCutSq) return;
                int type = mTypeMap.applyAsInt(mNl.typeAt(idx));
                checkType(type);
                rNlDx.add(dx); rNlDy.add(dy); rNlDz.add(dz);
                rNlType.add(type); rNlIdx.add(idx);
            });
        } else {
            mNl.forEach(aI, (dx, dy, dz, idx) -> {
                int type = mTypeMap.applyAsInt(mNl.typeAt(idx));
                checkType(type);
                rNlDx.add(dx); rNlDy.add(dy); rNlDz.add(dz);
                rNlType.add(type); rNlIdx.add(idx);
            });
        }
        if (aRequireForce) {
            final int mNlSize = rNlIdx.size();
            DoubleList rGradNlDx = mGradNlDxPar[aThreadID], rGradNlDy = mGradNlDyPar[aThreadID], rGradNlDz = mGradNlDzPar[aThreadID];
            rGradNlDx.clear(); rGradNlDx.addZeros(mNlSize);
            rGradNlDy.clear(); rGradNlDy.addZeros(mNlSize);
            rGradNlDz.clear(); rGradNlDz.addZeros(mNlSize);
        }
    }
    protected final void initBufPar(boolean aRequireNl, boolean aRequireTotalEnergy, boolean aRequirePreAtomEnergy, boolean aRequireForce, boolean aRequireTotalStress, boolean aRequirePreAtomStress) {
        final int tNumThreads = nthreads();
        if (aRequireNl) {
            if (mNlDxPar==null || mNlDxPar.length!=tNumThreads) {
                mNlDxPar = new DoubleList[tNumThreads];
                mNlDyPar = new DoubleList[tNumThreads];
                mNlDzPar = new DoubleList[tNumThreads];
                mNlTypePar = new IntList[tNumThreads];
                mNlIdxPar = new IntList[tNumThreads];
                mGradNlDxPar = new DoubleList[tNumThreads];
                mGradNlDyPar = new DoubleList[tNumThreads];
                mGradNlDzPar = new DoubleList[tNumThreads];
                for (int i = 0; i < tNumThreads; ++i) {
                    mNlDxPar[i] = new DoubleList(16);
                    mNlDyPar[i] = new DoubleList(16);
                    mNlDzPar[i] = new DoubleList(16);
                    mNlTypePar[i] = new IntList(16);
                    mNlIdxPar[i] = new IntList(16);
                    mGradNlDxPar[i] = new DoubleList(16);
                    mGradNlDyPar[i] = new DoubleList(16);
                    mGradNlDzPar[i] = new DoubleList(16);
                }
            }
        }
        if (aRequireTotalEnergy) {
            if (mEnergyPar==null || mEnergyPar.length!=tNumThreads) {
                mEnergyPar = new DoubleWrapper[tNumThreads];
                for (int i = 0; i < tNumThreads; ++i) {
                    mEnergyPar[i] = new DoubleWrapper(0.0);
                }
            } else {
                for (int i = 0; i < tNumThreads; ++i) {
                    mEnergyPar[i].mValue = 0.0;
                }
            }
        }
        if (aRequirePreAtomEnergy) {
            if (mEnergiesPar==null || mEnergiesPar.length!=tNumThreads) {
                mEnergiesPar = new Vector[tNumThreads];
                while (mEnergiesParRaw.size() < tNumThreads) mEnergiesParRaw.add(new DoubleList());
                mEnergiesPar[0] = mEnergies;
                for (int i = 1; i < tNumThreads; ++i) {
                    DoubleList tSubEnergiesParRaw = mEnergiesParRaw.get(i);
                    tSubEnergiesParRaw.clear();
                    tSubEnergiesParRaw.addZeros(mNumAtoms);
                    mEnergiesPar[i] = tSubEnergiesParRaw.asVec();
                }
            } else {
                for (int i = 0; i < tNumThreads; ++i) {
                    mEnergiesPar[i].fill(0.0);
                }
            }
        }
        if (aRequireForce) {
            if (mForcesXPar==null || mForcesXPar.length!=tNumThreads) {
                mForcesXPar = new Vector[tNumThreads];
                mForcesYPar = new Vector[tNumThreads];
                mForcesZPar = new Vector[tNumThreads];
                while (mForcesParRaw.size() < tNumThreads) mForcesParRaw.add(new DoubleList());
                mForcesXPar[0] = mForcesX;
                mForcesYPar[0] = mForcesY;
                mForcesZPar[0] = mForcesZ;
                for (int i = 1; i < tNumThreads; ++i) {
                    DoubleList tSubForcesParRaw = mForcesParRaw.get(i);
                    tSubForcesParRaw.clear();
                    tSubForcesParRaw.addZeros(mNumAtoms*3);
                    double[] tData = tSubForcesParRaw.internalData();
                    int tShift = 0;
                    mForcesXPar[i] = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                    mForcesYPar[i] = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                    mForcesZPar[i] = new Vector(mNumAtoms, tShift, tData);
                }
            } else {
                for (int i = 0; i < tNumThreads; ++i) {
                    mForcesXPar[i].fill(0.0);
                    mForcesYPar[i].fill(0.0);
                    mForcesZPar[i].fill(0.0);
                }
            }
        }
        if (aRequireTotalStress) {
            if (mVirialXXPar==null || mVirialXXPar.length!=tNumThreads) {
                mVirialXXPar = new DoubleWrapper[tNumThreads];
                mVirialYYPar = new DoubleWrapper[tNumThreads];
                mVirialZZPar = new DoubleWrapper[tNumThreads];
                mVirialXYPar = new DoubleWrapper[tNumThreads];
                mVirialXZPar = new DoubleWrapper[tNumThreads];
                mVirialYZPar = new DoubleWrapper[tNumThreads];
                for (int i = 0; i < tNumThreads; ++i) {
                    mVirialXXPar[i] = new DoubleWrapper(0.0);
                    mVirialYYPar[i] = new DoubleWrapper(0.0);
                    mVirialZZPar[i] = new DoubleWrapper(0.0);
                    mVirialXYPar[i] = new DoubleWrapper(0.0);
                    mVirialXZPar[i] = new DoubleWrapper(0.0);
                    mVirialYZPar[i] = new DoubleWrapper(0.0);
                }
            } else {
                for (int i = 0; i < tNumThreads; ++i) {
                    mVirialXXPar[i].mValue = 0.0;
                    mVirialYYPar[i].mValue = 0.0;
                    mVirialZZPar[i].mValue = 0.0;
                    mVirialXYPar[i].mValue = 0.0;
                    mVirialXZPar[i].mValue = 0.0;
                    mVirialYZPar[i].mValue = 0.0;
                }
            }
        }
        if (aRequirePreAtomStress) {
            boolean tCentroid = centroidPerAtomStressSupport();
            if (mVirialsXXPar==null || mVirialsXXPar.length!=tNumThreads) {
                mVirialsXXPar = new Vector[tNumThreads];
                mVirialsYYPar = new Vector[tNumThreads];
                mVirialsZZPar = new Vector[tNumThreads];
                mVirialsXYPar = new Vector[tNumThreads];
                mVirialsXZPar = new Vector[tNumThreads];
                mVirialsYZPar = new Vector[tNumThreads];
                if (tCentroid) {
                    mVirialsYXPar = new Vector[tNumThreads];
                    mVirialsZXPar = new Vector[tNumThreads];
                    mVirialsZYPar = new Vector[tNumThreads];
                }
                while (mVirialsParRaw.size() < tNumThreads) mVirialsParRaw.add(new DoubleList());
                mVirialsXXPar[0] = mStressesXX;
                mVirialsYYPar[0] = mStressesYY;
                mVirialsZZPar[0] = mStressesZZ;
                mVirialsXYPar[0] = mStressesXY;
                mVirialsXZPar[0] = mStressesXZ;
                mVirialsYZPar[0] = mStressesYZ;
                if (tCentroid) {
                    mVirialsYXPar[0] = mStressesYX;
                    mVirialsZXPar[0] = mStressesZX;
                    mVirialsZYPar[0] = mStressesZY;
                }
                for (int i = 1; i < tNumThreads; ++i) {
                    DoubleList tSubVirialsParRaw = mVirialsParRaw.get(i);
                    tSubVirialsParRaw.clear();
                    tSubVirialsParRaw.addZeros(mNumAtoms*(tCentroid?9:6));
                    double[] tData = tSubVirialsParRaw.internalData();
                    int tShift = 0;
                    mVirialsXXPar[i] = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                    mVirialsYYPar[i] = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                    mVirialsZZPar[i] = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                    mVirialsXYPar[i] = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                    mVirialsXZPar[i] = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                    mVirialsYZPar[i] = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                    if (tCentroid) {
                        mVirialsYXPar[i] = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                        mVirialsZXPar[i] = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                        mVirialsZYPar[i] = new Vector(mNumAtoms, tShift, tData);
                    }
                }
            } else {
                for (int i = 0; i < tNumThreads; ++i) {
                    mVirialsXXPar[i].fill(0.0);
                    mVirialsYYPar[i].fill(0.0);
                    mVirialsZZPar[i].fill(0.0);
                    mVirialsXYPar[i].fill(0.0);
                    mVirialsXZPar[i].fill(0.0);
                    mVirialsYZPar[i].fill(0.0);
                    if (tCentroid) {
                        mVirialsYXPar[i].fill(0.0);
                        mVirialsZXPar[i].fill(0.0);
                        mVirialsZYPar[i].fill(0.0);
                    }
                }
            }
        }
    }
    protected final void collectBufPar(boolean aRequireTotalEnergy, boolean aRequirePreAtomEnergy, boolean aRequireForce, boolean aRequireTotalStress, boolean aRequirePreAtomStress) {
        final int tNumThreads = nthreads();
        if (aRequireTotalEnergy) {
            mEnergy = 0.0;
            for (int i = 0; i < tNumThreads; ++i) {
                mEnergy += mEnergyPar[i].mValue;
            }
        }
        if (aRequirePreAtomEnergy) {
            for (int i = 1; i < tNumThreads; ++i) {
                mEnergies.plus2this(mEnergiesPar[i]);
            }
        }
        if (aRequireForce) {
            for (int i = 1; i < tNumThreads; ++i) {
                mForcesX.plus2this(mForcesXPar[i]);
                mForcesY.plus2this(mForcesYPar[i]);
                mForcesZ.plus2this(mForcesZPar[i]);
            }
        }
        if (aRequireTotalStress) {
            mStressXX = 0.0; mStressYY = 0.0; mStressZZ = 0.0;
            mStressXY = 0.0; mStressXZ = 0.0; mStressYZ = 0.0;
            for (int i = 0; i < tNumThreads; ++i) {
                mStressXX += mVirialXXPar[i].mValue;
                mStressYY += mVirialYYPar[i].mValue;
                mStressZZ += mVirialZZPar[i].mValue;
                mStressXY += mVirialXYPar[i].mValue;
                mStressXZ += mVirialXZPar[i].mValue;
                mStressYZ += mVirialYZPar[i].mValue;
            }
            mStressXX = -mStressXX/mVolume;
            mStressYY = -mStressYY/mVolume;
            mStressZZ = -mStressZZ/mVolume;
            mStressXY = -mStressXY/mVolume;
            mStressXZ = -mStressXZ/mVolume;
            mStressYZ = -mStressYZ/mVolume;
        }
        if (aRequirePreAtomStress) {
            boolean tCentroid = centroidPerAtomStressSupport();
            for (int i = 1; i < tNumThreads; ++i) {
                mStressesXX.plus2this(mVirialsXXPar[i]);
                mStressesYY.plus2this(mVirialsYYPar[i]);
                mStressesZZ.plus2this(mVirialsZZPar[i]);
                mStressesXY.plus2this(mVirialsXYPar[i]);
                mStressesXZ.plus2this(mVirialsXZPar[i]);
                mStressesYZ.plus2this(mVirialsYZPar[i]);
                if (tCentroid) {
                    mStressesYX.plus2this(mVirialsYXPar[i]);
                    mStressesZX.plus2this(mVirialsZXPar[i]);
                    mStressesZY.plus2this(mVirialsZYPar[i]);
                }
            }
            mStressesXX.negative2this();
            mStressesYY.negative2this();
            mStressesZZ.negative2this();
            mStressesXY.negative2this();
            mStressesXZ.negative2this();
            mStressesYZ.negative2this();
            if (tCentroid) {
                mStressesYX.negative2this();
                mStressesZX.negative2this();
                mStressesZY.negative2this();
            }
        }
    }
    
    
    @Override public void calculate(boolean aRequireTotalEnergy, boolean aRequirePreAtomEnergy, boolean aRequireForce, boolean aRequireTotalStress, boolean aRequirePreAtomStress) throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        if (aRequirePreAtomEnergy && !perAtomEnergySupport()) throw new UnsupportedOperationException("per-atom energy not supported");
        if (aRequirePreAtomStress && !perAtomStressSupport()) throw new UnsupportedOperationException("per-atom stress not supported");
        // 判断需要的计算等级
        final boolean tCalEnergyForce = aRequireForce || aRequireTotalStress || aRequirePreAtomStress;
        final boolean tCalEnergy = (!tCalEnergyForce) && (aRequireTotalEnergy || aRequirePreAtomEnergy);
        // 什么都不用计算的情况
        if (!tCalEnergy && !tCalEnergyForce) return;
        // 构建近邻列表，顺便会检查是否执行了 setData
        mNl.setRCut(rcutMax()).build();
        // 缓存初始化
        initBufPar(true, aRequireTotalEnergy, aRequirePreAtomEnergy, aRequireForce, aRequireTotalStress, aRequirePreAtomStress);
        // 执行计算，默认实现中直接基于 calEnergySingle calEnergyForceSingle
        // 做全近邻遍历的计算，对应非消息传递的局域多体势的通用实现，非多体势可以做进一步优化
        if (tCalEnergy) {
            mPool.parforWithException(mNumAtoms, this::initDo, this::finalDo, (i, threadID) -> {
                int ctype = mTypeMap.applyAsInt(mNl.typeAt(i));
                checkType(ctype);
                initBufNl(threadID, i, false);
                double tEng = calEnergySingle(
                    threadID, ctype,
                    mNlDxPar[threadID], mNlDyPar[threadID], mNlDzPar[threadID], mNlTypePar[threadID]
                );
                if (aRequireTotalEnergy) {
                    mEnergyPar[threadID].mValue += tEng;
                }
                if (aRequirePreAtomEnergy) {
                    mEnergiesPar[threadID].add(i, tEng);
                }
            });
        } else {
//            assert tCalEnergyForce;
            final boolean tCentroid = centroidPerAtomStressSupport();
            mPool.parforWithException(mNumAtoms, this::initDo, this::finalDo, (i, threadID) -> {
                int ctype = mTypeMap.applyAsInt(mNl.typeAt(i));
                checkType(ctype);
                initBufNl(threadID, i, true);
                DoubleList tNlDx = mNlDxPar[threadID], tNlDy = mNlDyPar[threadID], tNlDz = mNlDzPar[threadID];
                DoubleList rGradNlDx = mGradNlDxPar[threadID], rGradNlDy = mGradNlDyPar[threadID], rGradNlDz = mGradNlDzPar[threadID];
                double tEng = calEnergyForceSingle(
                    threadID, ctype,
                    tNlDx, tNlDy, tNlDz, mNlTypePar[threadID],
                    rGradNlDx, rGradNlDy, rGradNlDz
                );
                if (aRequireTotalEnergy) {
                    mEnergyPar[threadID].mValue += tEng;
                }
                if (aRequirePreAtomEnergy) {
                    mEnergiesPar[threadID].add(i, tEng);
                }
                // 累加交叉项到近邻
                IntList tNlIdx = mNlIdxPar[threadID];
                final int tNlSize = tNlIdx.size();
                double fx0 = 0.0, fy0 = 0.0, fz0 = 0.0;
                Vector tForcesX = aRequireForce ? mForcesXPar[threadID] : null;
                Vector tForcesY = aRequireForce ? mForcesYPar[threadID] : null;
                Vector tForcesZ = aRequireForce ? mForcesZPar[threadID] : null;
                double tVirialXX = 0.0, tVirialYY = 0.0, tVirialZZ = 0.0;
                double tVirialXY = 0.0, tVirialXZ = 0.0, tVirialYZ = 0.0;
                Vector tVirialsXX = aRequirePreAtomStress ? mVirialsXXPar[threadID] : null;
                Vector tVirialsYY = aRequirePreAtomStress ? mVirialsYYPar[threadID] : null;
                Vector tVirialsZZ = aRequirePreAtomStress ? mVirialsZZPar[threadID] : null;
                Vector tVirialsXY = aRequirePreAtomStress ? mVirialsXYPar[threadID] : null;
                Vector tVirialsXZ = aRequirePreAtomStress ? mVirialsXZPar[threadID] : null;
                Vector tVirialsYZ = aRequirePreAtomStress ? mVirialsYZPar[threadID] : null;
                Vector tVirialsYX = (tCentroid && aRequirePreAtomStress) ? mVirialsYXPar[threadID] : null;
                Vector tVirialsZX = (tCentroid && aRequirePreAtomStress) ? mVirialsZXPar[threadID] : null;
                Vector tVirialsZY = (tCentroid && aRequirePreAtomStress) ? mVirialsZYPar[threadID] : null;
                for (int jj = 0; jj < tNlSize; ++jj) {
                    final int j = tNlIdx.get(jj);
                    final double fx = rGradNlDx.get(jj);
                    final double fy = rGradNlDy.get(jj);
                    final double fz = rGradNlDz.get(jj);
                    if (aRequireForce) {
                        fx0 -= fx; tForcesX.add(j, fx);
                        fy0 -= fy; tForcesY.add(j, fy);
                        fz0 -= fz; tForcesZ.add(j, fz);
                    }
                    if (aRequireTotalStress || aRequirePreAtomStress) {
                        final double dx = tNlDx.get(jj);
                        final double dy = tNlDy.get(jj);
                        final double dz = tNlDz.get(jj);
                        final double vxx = dx*fx, vyy = dy*fy, vzz = dz*fz;
                        final double vxy = dx*fy, vxz = dx*fz, vyz = dy*fz;
                        if (aRequireTotalStress) {
                            tVirialXX += vxx; tVirialYY += vyy; tVirialZZ += vzz;
                            tVirialXY += vxy; tVirialXZ += vxz; tVirialYZ += vyz;
                        }
                        // GPUMD 给出的更具对称性的形式要求累加到近邻的 j 上
                        if (aRequirePreAtomStress) {
                            tVirialsXX.add(j, vxx);
                            tVirialsYY.add(j, vyy);
                            tVirialsZZ.add(j, vzz);
                            tVirialsXY.add(j, vxy);
                            tVirialsXZ.add(j, vxz);
                            tVirialsYZ.add(j, vyz);
                            if (tCentroid) {
                                tVirialsYX.add(j, dy*fx);
                                tVirialsZX.add(j, dz*fx);
                                tVirialsZY.add(j, dz*fy);
                            }
                        }
                    }
                }
                if (aRequireForce) {
                    tForcesX.add(i, fx0);
                    tForcesY.add(i, fy0);
                    tForcesZ.add(i, fz0);
                }
                if (aRequireTotalStress) {
                    mVirialXXPar[threadID].mValue += tVirialXX;
                    mVirialYYPar[threadID].mValue += tVirialYY;
                    mVirialZZPar[threadID].mValue += tVirialZZ;
                    mVirialXYPar[threadID].mValue += tVirialXY;
                    mVirialXZPar[threadID].mValue += tVirialXZ;
                    mVirialYZPar[threadID].mValue += tVirialYZ;
                }
            });
        }
        collectBufPar(aRequireTotalEnergy, aRequirePreAtomEnergy, aRequireForce, aRequireTotalStress, aRequirePreAtomStress);
    }
}
