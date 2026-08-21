package jse.atom;

import jse.code.collection.DoubleList;
import jse.code.collection.DoubleWrapper;
import jse.code.collection.IntList;
import jse.math.MathEX;
import jse.math.vector.Vector;
import jse.parallel.ParforThreadPool;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.IntUnaryOperator;


public abstract class AbstractPairPotential extends AbstractPotential implements IPairPotential {
    protected final NeighborListGetter mNl;
    protected final ParforThreadPool mPool;
    
    protected final DoubleList[] mNlDxPar, mNlDyPar, mNlDzPar;
    protected final IntList[] mNlTypePar, mNlIdxPar;
    protected final DoubleList[] mGradNlDxPar, mGradNlDyPar, mGradNlDzPar;
    
    protected final Vector[] mEnergiesPar;
    protected final Vector[] mForcesXPar, mForcesYPar, mForcesZPar;
    protected final Vector[] mVirialsXXPar, mVirialsYYPar, mVirialsZZPar;
    protected final Vector[] mVirialsXYPar, mVirialsXZPar, mVirialsYZPar;
    protected final Vector[] mVirialsYXPar, mVirialsZXPar, mVirialsZYPar;
    protected final DoubleWrapper[] mEnergyPar;
    protected final DoubleWrapper[] mVirialXXPar, mVirialYYPar, mVirialZZPar;
    protected final DoubleWrapper[] mVirialXYPar, mVirialXZPar, mVirialYZPar;
    private final DoubleList[] mEnergiesParRaw;
    private final DoubleList[] mForcesParRaw;
    private final DoubleList[] mVirialsParRaw;
    
    protected AbstractPairPotential(int aNumThreads) {
        mPool = new ParforThreadPool(aNumThreads);
        mNl = new NeighborListGetter();
        
        // 现在在这里初始化缓存的并行占用部分，认为线程数是不变的
        mNlDxPar = new DoubleList[aNumThreads];
        mNlDyPar = new DoubleList[aNumThreads];
        mNlDzPar = new DoubleList[aNumThreads];
        mNlTypePar = new IntList[aNumThreads];
        mNlIdxPar = new IntList[aNumThreads];
        mGradNlDxPar = new DoubleList[aNumThreads];
        mGradNlDyPar = new DoubleList[aNumThreads];
        mGradNlDzPar = new DoubleList[aNumThreads];
        for (int i = 0; i < aNumThreads; ++i) {
            mNlDxPar[i] = new DoubleList(16);
            mNlDyPar[i] = new DoubleList(16);
            mNlDzPar[i] = new DoubleList(16);
            mNlTypePar[i] = new IntList(16);
            mNlIdxPar[i] = new IntList(16);
            mGradNlDxPar[i] = new DoubleList(16);
            mGradNlDyPar[i] = new DoubleList(16);
            mGradNlDzPar[i] = new DoubleList(16);
        }
        mEnergiesPar = new Vector[aNumThreads];
        mForcesXPar = new Vector[aNumThreads];
        mForcesYPar = new Vector[aNumThreads];
        mForcesZPar = new Vector[aNumThreads];
        mVirialsXXPar = new Vector[aNumThreads];
        mVirialsYYPar = new Vector[aNumThreads];
        mVirialsZZPar = new Vector[aNumThreads];
        mVirialsXYPar = new Vector[aNumThreads];
        mVirialsXZPar = new Vector[aNumThreads];
        mVirialsYZPar = new Vector[aNumThreads];
        mVirialsYXPar = new Vector[aNumThreads];
        mVirialsZXPar = new Vector[aNumThreads];
        mVirialsZYPar = new Vector[aNumThreads];
        mEnergyPar = new DoubleWrapper[aNumThreads];
        mVirialXXPar = new DoubleWrapper[aNumThreads];
        mVirialYYPar = new DoubleWrapper[aNumThreads];
        mVirialZZPar = new DoubleWrapper[aNumThreads];
        mVirialXYPar = new DoubleWrapper[aNumThreads];
        mVirialXZPar = new DoubleWrapper[aNumThreads];
        mVirialYZPar = new DoubleWrapper[aNumThreads];
        mEnergiesParRaw = new DoubleList[aNumThreads];
        mForcesParRaw = new DoubleList[aNumThreads];
        mVirialsParRaw = new DoubleList[aNumThreads];
        for (int i = 0; i < aNumThreads; ++i) {
            mEnergyPar[i] = new DoubleWrapper(0.0);
            mVirialXXPar[i] = new DoubleWrapper(0.0);
            mVirialYYPar[i] = new DoubleWrapper(0.0);
            mVirialZZPar[i] = new DoubleWrapper(0.0);
            mVirialXYPar[i] = new DoubleWrapper(0.0);
            mVirialXZPar[i] = new DoubleWrapper(0.0);
            mVirialYZPar[i] = new DoubleWrapper(0.0);
            mEnergiesParRaw[i] = new DoubleList();
            mForcesParRaw[i] = new DoubleList();
            mVirialsParRaw[i] = new DoubleList();
        }
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
        // 现在在这里初始化缓存的占用
        final int tNumThreads = nthreads();
        mForcesXPar[0] = mForcesX;
        mForcesYPar[0] = mForcesY;
        mForcesZPar[0] = mForcesZ;
        for (int i = 1; i < tNumThreads; ++i) {
            DoubleList tSubForcesParRaw = mForcesParRaw[i];
            tSubForcesParRaw.clear();
            tSubForcesParRaw.addZeros(mNumAtoms*3);
            double[] tData = tSubForcesParRaw.internalData();
            int tShift = 0;
            mForcesXPar[i] = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            mForcesYPar[i] = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            mForcesZPar[i] = new Vector(mNumAtoms, tShift, tData);
        }
        if (perAtomEnergySupport()) {
            mEnergiesPar[0] = mEnergies;
            for (int i = 1; i < tNumThreads; ++i) {
                DoubleList tSubEnergiesParRaw = mEnergiesParRaw[i];
                tSubEnergiesParRaw.clear();
                tSubEnergiesParRaw.addZeros(mNumAtoms);
                mEnergiesPar[i] = tSubEnergiesParRaw.asVec();
            }
        }
        if (perAtomStressSupport()) {
            boolean tCentroid = centroidPerAtomStressSupport();
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
                DoubleList tSubVirialsParRaw = mVirialsParRaw[i];
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
        }
        return this;
    }
    
    protected void initDo(int aThreadID) throws Exception {}
    protected void finalDo(int aThreadID) throws Exception {}
    
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
    protected final void initBufPar(boolean aRequireTotalEnergy, boolean aRequirePerAtomEnergy, boolean aRequireForce, boolean aRequireTotalStress, boolean aRequirePerAtomStress) {
        final int tNumThreads = nthreads();
        if (aRequireTotalEnergy) {
            for (int i = 0; i < tNumThreads; ++i) {
                mEnergyPar[i].mValue = 0.0;
            }
        }
        if (aRequirePerAtomEnergy) {
            for (int i = 0; i < tNumThreads; ++i) {
                mEnergiesPar[i].fill(0.0);
            }
        }
        if (aRequireForce) {
            for (int i = 0; i < tNumThreads; ++i) {
                mForcesXPar[i].fill(0.0);
                mForcesYPar[i].fill(0.0);
                mForcesZPar[i].fill(0.0);
            }
        }
        if (aRequireTotalStress) {
            for (int i = 0; i < tNumThreads; ++i) {
                mVirialXXPar[i].mValue = 0.0;
                mVirialYYPar[i].mValue = 0.0;
                mVirialZZPar[i].mValue = 0.0;
                mVirialXYPar[i].mValue = 0.0;
                mVirialXZPar[i].mValue = 0.0;
                mVirialYZPar[i].mValue = 0.0;
            }
        }
        if (aRequirePerAtomStress) {
            boolean tCentroid = centroidPerAtomStressSupport();
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
    protected final void collectBufPar(boolean aRequireTotalEnergy, boolean aRequirePerAtomEnergy, boolean aRequireForce, boolean aRequireTotalStress, boolean aRequirePerAtomStress) {
        final int tNumThreads = nthreads();
        if (aRequireTotalEnergy) {
            mEnergy = 0.0;
            for (int i = 0; i < tNumThreads; ++i) {
                mEnergy += mEnergyPar[i].mValue;
            }
        }
        if (aRequirePerAtomEnergy) {
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
        if (aRequirePerAtomStress) {
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
    
    @ApiStatus.Experimental @Override
    public final double calEnergySingle(int aThreadID, int aI) throws Exception {
        int ctype = mTypeMap.applyAsInt(mNl.typeAt(aI));
        checkType(ctype);
        initBufNl(aThreadID, aI, false);
        return calEnergySingle(
            aThreadID, ctype,
            mNlDxPar[aThreadID], mNlDyPar[aThreadID], mNlDzPar[aThreadID], mNlTypePar[aThreadID]
        );
    }
    @ApiStatus.Experimental @Override
    public final double calEnergyForceSingle(int aThreadID, int aI, DoubleList rGradNlDx, DoubleList rGradNlDy, DoubleList rGradNlDz) throws Exception {
        int ctype = mTypeMap.applyAsInt(mNl.typeAt(aI));
        checkType(ctype);
        initBufNl(aThreadID, aI, true);
        return calEnergyForceSingle(
            aThreadID, ctype,
            mNlDxPar[aThreadID], mNlDyPar[aThreadID], mNlDzPar[aThreadID], mNlTypePar[aThreadID],
            rGradNlDx, rGradNlDy, rGradNlDz
        );
    }
    
    @Override public void calculate(boolean aRequireTotalEnergy, boolean aRequirePerAtomEnergy, boolean aRequireForce, boolean aRequireTotalStress, boolean aRequirePerAtomStress) throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        if (!dataValid()) throw new IllegalStateException("data invalid");
        if (aRequirePerAtomEnergy && !perAtomEnergySupport()) throw new UnsupportedOperationException("per-atom energy not supported");
        if (aRequirePerAtomStress && !perAtomStressSupport()) throw new UnsupportedOperationException("per-atom stress not supported");
        // 判断需要的计算等级
        final boolean tCalEnergyForce = aRequireForce || aRequireTotalStress || aRequirePerAtomStress;
        final boolean tCalEnergy = (!tCalEnergyForce) && (aRequireTotalEnergy || aRequirePerAtomEnergy);
        // 构建近邻列表
        mNl.setRCut(rcutMax()).build();
        // 缓存初始化
        initBufPar(aRequireTotalEnergy, aRequirePerAtomEnergy, aRequireForce, aRequireTotalStress, aRequirePerAtomStress);
        // 执行计算，默认实现中直接基于 calEnergySingle calEnergyForceSingle
        // 做全近邻遍历的计算，对应非消息传递的局域多体势的通用实现，非多体势可以做进一步优化
        if (tCalEnergy) {
            mPool.parforWithException(mNumAtoms, this::initDo, this::finalDo, (i, threadID) -> {
                double tEng = calEnergySingle(threadID, i);
                if (aRequireTotalEnergy) {
                    mEnergyPar[threadID].mValue += tEng;
                }
                if (aRequirePerAtomEnergy) {
                    mEnergiesPar[threadID].add(i, tEng);
                }
            });
        } else
        if (tCalEnergyForce) {
            final boolean tCentroid = centroidPerAtomStressSupport();
            mPool.parforWithException(mNumAtoms, this::initDo, this::finalDo, (i, threadID) -> {
                DoubleList tNlDx = mNlDxPar[threadID], tNlDy = mNlDyPar[threadID], tNlDz = mNlDzPar[threadID];
                DoubleList rGradNlDx = mGradNlDxPar[threadID], rGradNlDy = mGradNlDyPar[threadID], rGradNlDz = mGradNlDzPar[threadID];
                double tEng = calEnergyForceSingle(threadID, i, rGradNlDx, rGradNlDy, rGradNlDz);
                if (aRequireTotalEnergy) {
                    mEnergyPar[threadID].mValue += tEng;
                }
                if (aRequirePerAtomEnergy) {
                    mEnergiesPar[threadID].add(i, tEng);
                }
                // 累加交叉项到近邻
                IntList tNlIdx = mNlIdxPar[threadID];
                final int tNlSize = tNlIdx.size();
                Vector tForcesX = aRequireForce ? mForcesXPar[threadID] : null;
                Vector tForcesY = aRequireForce ? mForcesYPar[threadID] : null;
                Vector tForcesZ = aRequireForce ? mForcesZPar[threadID] : null;
                double tVirialXX = 0.0, tVirialYY = 0.0, tVirialZZ = 0.0;
                double tVirialXY = 0.0, tVirialXZ = 0.0, tVirialYZ = 0.0;
                Vector tVirialsXX = aRequirePerAtomStress ? mVirialsXXPar[threadID] : null;
                Vector tVirialsYY = aRequirePerAtomStress ? mVirialsYYPar[threadID] : null;
                Vector tVirialsZZ = aRequirePerAtomStress ? mVirialsZZPar[threadID] : null;
                Vector tVirialsXY = aRequirePerAtomStress ? mVirialsXYPar[threadID] : null;
                Vector tVirialsXZ = aRequirePerAtomStress ? mVirialsXZPar[threadID] : null;
                Vector tVirialsYZ = aRequirePerAtomStress ? mVirialsYZPar[threadID] : null;
                Vector tVirialsYX = (tCentroid && aRequirePerAtomStress) ? mVirialsYXPar[threadID] : null;
                Vector tVirialsZX = (tCentroid && aRequirePerAtomStress) ? mVirialsZXPar[threadID] : null;
                Vector tVirialsZY = (tCentroid && aRequirePerAtomStress) ? mVirialsZYPar[threadID] : null;
                for (int jj = 0; jj < tNlSize; ++jj) {
                    final int j = tNlIdx.get(jj);
                    final double fx = rGradNlDx.get(jj);
                    final double fy = rGradNlDy.get(jj);
                    final double fz = rGradNlDz.get(jj);
                    if (aRequireForce) {
                        tForcesX.add(i, fx); tForcesX.add(j, -fx);
                        tForcesY.add(i, fy); tForcesY.add(j, -fy);
                        tForcesZ.add(i, fz); tForcesZ.add(j, -fz);
                    }
                    if (aRequireTotalStress || aRequirePerAtomStress) {
                        final double dx = tNlDx.get(jj);
                        final double dy = tNlDy.get(jj);
                        final double dz = tNlDz.get(jj);
                        final double vxx = -dx*fx, vyy = -dy*fy, vzz = -dz*fz;
                        final double vxy = -dx*fy, vxz = -dx*fz, vyz = -dy*fz;
                        if (aRequireTotalStress) {
                            tVirialXX += vxx; tVirialYY += vyy; tVirialZZ += vzz;
                            tVirialXY += vxy; tVirialXZ += vxz; tVirialYZ += vyz;
                        }
                        // GPUMD 给出的更具对称性的形式要求累加到近邻的 j 上
                        if (aRequirePerAtomStress) {
                            tVirialsXX.add(j, vxx);
                            tVirialsYY.add(j, vyy);
                            tVirialsZZ.add(j, vzz);
                            tVirialsXY.add(j, vxy);
                            tVirialsXZ.add(j, vxz);
                            tVirialsYZ.add(j, vyz);
                            if (tCentroid) {
                                tVirialsYX.add(j, -dy*fx);
                                tVirialsZX.add(j, -dz*fx);
                                tVirialsZY.add(j, -dz*fy);
                            }
                        }
                    }
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
        collectBufPar(aRequireTotalEnergy, aRequirePerAtomEnergy, aRequireForce, aRequireTotalStress, aRequirePerAtomStress);
        // 设置对应值合法
        if (aRequireTotalEnergy) mTotalEnergyValid = true;
        if (aRequirePerAtomEnergy) mPerAtomEnergyValid = true;
        if (aRequireForce) mForceValid = true;
        if (aRequireTotalStress) mTotalStressValid = true;
        if (aRequirePerAtomStress) mPerAtomStressValid = true;
    }
}
