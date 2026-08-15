package jse.atom;

import jse.cache.VectorCache;
import jse.code.collection.ISlice;
import jse.math.vector.IVector;
import jse.math.vector.Vector;
import jse.parallel.ParforThreadPool;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntUnaryOperator;


public abstract class AbstractPairPotential extends AbstractPotential implements IPairPotential {
    protected final NeighborListGetter mNL;
    protected final ParforThreadPool mPool;
    protected AbstractPairPotential(int aNumThreads) {
        mPool = new ParforThreadPool(aNumThreads);
        mNL = new NeighborListGetter();
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
        return mNL;
    }
    protected IntUnaryOperator mTypeMap = type->type;
    @Override public AbstractPairPotential setData(IAtomData aData) throws Exception {
        super.setData(aData);
        mNL.setData(aData);
        if (hasSymbol()) mTypeMap = typeMap(aData);
        return this;
    }
    
    /** 能量累加接口，用于接收能量计算结果 */
    @FunctionalInterface protected interface IEnergyAccumulator {
        void add(int aJ, double aEnergy);
    }
    /** 力累加接口，用于接收力计算结果 */
    @FunctionalInterface protected interface IForceAccumulator {
        void add(int aJ, double aForceX, double aForceY, double aForceZ);
    }
    /** 位力累加接口，用于接收位力计算结果 */
    @FunctionalInterface protected interface IVirialAccumulator {
        void add(int aJ, double aForceX, double aForceY, double aForceZ, double aDx, double aDy, double aDz);
    }
    
    
    
    @Override public double calEnergyAt(ISlice aIndices) {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        mNL.setRCut(rcutMax()).build();
        final int tTypeNum = ntypes();
        final Vector rEngPar = VectorCache.getZeros(nthreads());
        calEnergyPart(aAtomData.natoms(), (initDo, finalDo, neighborListDo) -> {
            pool_().parforWithException(aIndices.size(), initDo, finalDo, (i, threadID) -> {
                final int cIdx = aIndices.get(i);
                final int cType = tTypeNum<=0 ? 0 : tTypeMap.applyAsInt(tNl.typeAt(cIdx));
                neighborListDo.run(threadID, i, cType, dxyzTypeDo -> {
                    tNl.forEach(i, false, (dx, dy, dz, idx) -> {
                        int tType = tTypeNum<=0 ? 0 : tTypeMap.applyAsInt(tNl.typeAt(idx));
                        dxyzTypeDo.run(dx, dy, dz, tType, idx);
                    });
                });
            });
        }, (threadID, cIdx, eng) -> {
            rEngPar.add(threadID, eng);
        });
        double rEng = rEngPar.sum();
        VectorCache.returnVec(rEngPar);
        return rEng;
    }
    @Override public double calEnergyDiffSwap(int aI, int aJ, boolean aRestoreData) throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        typeMapCheck(aAPC.ntypes(), aTypeMap);
        int oTypeI = aAPC.types().get(aI);
        int oTypeJ = aAPC.types().get(aJ);
        if (oTypeI == oTypeJ) return 0.0;
        double tRCut = rcutMax();
        if (tRCut <= 0) {
            double oEng = calEnergy(aAPC, aTypeMap);
            double nEng = calEnergy(aAPC.setAtomType(aI, oTypeJ).setAtomType(aJ, oTypeI), aTypeMap);
            if (aRestoreAPC) aAPC.setAtomType(aI, oTypeI).setAtomType(aJ, oTypeJ);
            return nEng - oEng;
        }
        // 非 manybody 势只需要考虑交换的两原子即可
        if (!manybody()) {
            ISlice tNL = ISlice.of(aI, aJ);
            double oEng = calEnergyAt(aAPC, tNL, aTypeMap);
            double nEng = calEnergyAt(aAPC.setAtomType(aI, oTypeJ).setAtomType(aJ, oTypeI), tNL, aTypeMap);
            if (aRestoreAPC) aAPC.setAtomType(aI, oTypeI).setAtomType(aJ, oTypeJ);
            return (nEng - oEng)*2.0;
        }
        // 采用最大的截断半径从而包含所有可能涉及发生了能量变换的原子
        IIntVector iNL = aAPC.getNeighborList(aI, tRCut);
        IIntVector jNL = aAPC.getNeighborList(aJ, tRCut);
        // 合并近邻列表，这里简单遍历实现
        final IntList tNL = new IntList(iNL.size()+1);
        tNL.add(aI);
        tNL.addAll(iNL);
        if (!tNL.contains(aJ)) tNL.add(aJ);
        jNL.forEach(idx -> {
            if (!tNL.contains(idx)) tNL.add(idx);
        });
        double oEng = calEnergyAt(aAPC, tNL, aTypeMap);
        double nEng = calEnergyAt(aAPC.setAtomType(aI, oTypeJ).setAtomType(aJ, oTypeI), tNL, aTypeMap);
        if (aRestoreAPC) aAPC.setAtomType(aI, oTypeI).setAtomType(aJ, oTypeJ);
        return nEng - oEng;
    }
    @Override public double calEnergyDiffFlip(int aI, int aType, boolean aRestoreData) throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        typeMapCheck(aAPC.ntypes(), aTypeMap);
        int oType = aAPC.types().get(aI);
        if (oType == aType) return 0.0;
        double tRCut = rcutMax();
        if (tRCut <= 0) {
            double oEng = calEnergy(aAPC, aTypeMap);
            double nEng = calEnergy(aAPC.setAtomType(aI, aType), aTypeMap);
            if (aRestoreAPC) aAPC.setAtomType(aI, oType);
            return nEng - oEng;
        }
        // 非 manybody 势只需要考虑翻转中心原子即可
        if (!manybody()) {
            ISlice tNL = ISlice.of(aI);
            double oEng = calEnergyAt(aAPC, tNL, aTypeMap);
            double nEng = calEnergyAt(aAPC.setAtomType(aI, aType), tNL, aTypeMap);
            if (aRestoreAPC) aAPC.setAtomType(aI, oType);
            return (nEng - oEng)*2.0;
        }
        // 采用最大的截断半径从而包含所有可能涉及发生了能量变换的原子
        IIntVector iNL = aAPC.getNeighborList(aI, tRCut);
        // 增加一个自身，这里简单创建新的列表实现
        final IntList tNL = new IntList(iNL.size()+1);
        tNL.add(aI);
        tNL.addAll(iNL);
        double oEng = calEnergyAt(aAPC, tNL, aTypeMap);
        double nEng = calEnergyAt(aAPC.setAtomType(aI, aType), tNL, aTypeMap);
        if (aRestoreAPC) aAPC.setAtomType(aI, oType);
        return nEng - oEng;
    }
    
    protected void initDo(int aThreadID) throws Exception {}
    protected void finalDo(int aThreadID) throws Exception {}
    protected abstract void calEnergySingle(int aThreadID, int aI, boolean aPart, IEnergyAccumulator rEnergyAccumulator) throws Exception;
    protected abstract void calEnergyForceVirialSingle(int aThreadID, int aI, @Nullable IEnergyAccumulator rEnergyAccumulator, @Nullable IForceAccumulator rForceAccumulator, @Nullable IVirialAccumulator rVirialAccumulator) throws Exception;
    
    @Override public void calEnergyForceVirials(@Nullable IVector rEnergies, @Nullable IVector rForcesX, @Nullable IVector rForcesY, @Nullable IVector rForcesZ, @Nullable IVector rVirialsXX, @Nullable IVector rVirialsYY, @Nullable IVector rVirialsZZ, @Nullable IVector rVirialsXY, @Nullable IVector rVirialsXZ, @Nullable IVector rVirialsYZ, @Nullable IVector rVirialsYX, @Nullable IVector rVirialsZX, @Nullable IVector rVirialsZY) throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        // 构建近邻列表，顺便会检查是否执行了 setData
        mNL.setRCut(rcutMax()).build();
        // 统一存储常量
        final boolean tCalEnergy = rEnergies!=null;
        final boolean tCalForce = rForcesX!=null || rForcesY!=null || rForcesZ!=null;
        final boolean tCalVirial = rVirialsXX!=null || rVirialsYY!=null || rVirialsZZ!=null || rVirialsXY!=null || rVirialsXZ!=null || rVirialsYZ!=null || rVirialsYX!=null || rVirialsZX!=null || rVirialsZY!=null;
        final int tNumThreads = nthreads();
        // 清空可能存在的旧值
        if (tCalEnergy) rEnergies.fill(0.0);
        // 并行情况下存在并行写入的问题，因此需要这样操作
        IVector @Nullable[] rEnergiesPar = rEnergies!=null ? new IVector[tNumThreads] : null;
        if (tCalEnergy) {
            rEnergiesPar[0] = rEnergies;
            for (int i = 1; i < tNumThreads; ++i) {
                rEnergiesPar[i] = VectorCache.getZeros(rEnergies.size());
            }
        }
        /// 特殊处理只需要计算能量的情况
        if (!tCalForce && !tCalVirial) {
            if (!tCalEnergy) {
                return;
            }
            mPool.parforWithException(mNumAtoms, this::initDo, this::finalDo, (i, threadID) -> {
                final IVector tEnergies = rEnergiesPar[threadID];
                calEnergySingle(threadID, i, false, (j, eng) -> {
                    if (tEnergies.size()==1) {
                        tEnergies.add(0, eng);
                    } else {
                        // 根据每个 idx 来控制能量具体累加的逻辑
                        if (i>=0 && j>=0) {
                            tEnergies.add(i, eng*0.5);
                            tEnergies.add(j, eng*0.5);
                        } else
                        if (i>=0) {
                            tEnergies.add(i, eng);
                        } else
                        if (j>=0) {
                            tEnergies.add(j, eng);
                        } else {
                            throw new IllegalStateException();
                        }
                    }
                });
            });
            for (int i = 1; i < tNumThreads; ++i) {
                rEnergies.plus2this(rEnergiesPar[i]);
                VectorCache.returnVec(rEnergiesPar[i]);
            }
            return;
        }
        /// 其余需要计算力或位力的情况
        // 清空可能存在的旧值
        if (rForcesX != null) rForcesX.fill(0.0);
        if (rForcesY != null) rForcesY.fill(0.0);
        if (rForcesZ != null) rForcesZ.fill(0.0);
        if (rVirialsXX != null) rVirialsXX.fill(0.0);
        if (rVirialsYY != null) rVirialsYY.fill(0.0);
        if (rVirialsZZ != null) rVirialsZZ.fill(0.0);
        if (rVirialsXY != null) rVirialsXY.fill(0.0);
        if (rVirialsXZ != null) rVirialsXZ.fill(0.0);
        if (rVirialsYZ != null) rVirialsYZ.fill(0.0);
        if (rVirialsYX != null) rVirialsYX.fill(0.0);
        if (rVirialsZX != null) rVirialsZX.fill(0.0);
        if (rVirialsZY != null) rVirialsZY.fill(0.0);
        // 并行情况下存在并行写入的问题，因此需要这样操作
        IVector @Nullable[] rForcesXPar = rForcesX!=null ? new IVector[tNumThreads] : null; if (rForcesX != null) {rForcesXPar[0] = rForcesX; for (int i = 1; i < tNumThreads; ++i) {rForcesXPar[i] = VectorCache.getZeros(mNumAtoms);}}
        IVector @Nullable[] rForcesYPar = rForcesY!=null ? new IVector[tNumThreads] : null; if (rForcesY != null) {rForcesYPar[0] = rForcesY; for (int i = 1; i < tNumThreads; ++i) {rForcesYPar[i] = VectorCache.getZeros(mNumAtoms);}}
        IVector @Nullable[] rForcesZPar = rForcesZ!=null ? new IVector[tNumThreads] : null; if (rForcesZ != null) {rForcesZPar[0] = rForcesZ; for (int i = 1; i < tNumThreads; ++i) {rForcesZPar[i] = VectorCache.getZeros(mNumAtoms);}}
        IVector @Nullable[] rVirialsXXPar = rVirialsXX!=null ? new IVector[tNumThreads] : null; if (rVirialsXX != null) {rVirialsXXPar[0] = rVirialsXX; for (int i = 1; i < tNumThreads; ++i) {rVirialsXXPar[i] = VectorCache.getZeros(rVirialsXX.size());}}
        IVector @Nullable[] rVirialsYYPar = rVirialsYY!=null ? new IVector[tNumThreads] : null; if (rVirialsYY != null) {rVirialsYYPar[0] = rVirialsYY; for (int i = 1; i < tNumThreads; ++i) {rVirialsYYPar[i] = VectorCache.getZeros(rVirialsYY.size());}}
        IVector @Nullable[] rVirialsZZPar = rVirialsZZ!=null ? new IVector[tNumThreads] : null; if (rVirialsZZ != null) {rVirialsZZPar[0] = rVirialsZZ; for (int i = 1; i < tNumThreads; ++i) {rVirialsZZPar[i] = VectorCache.getZeros(rVirialsZZ.size());}}
        IVector @Nullable[] rVirialsXYPar = rVirialsXY!=null ? new IVector[tNumThreads] : null; if (rVirialsXY != null) {rVirialsXYPar[0] = rVirialsXY; for (int i = 1; i < tNumThreads; ++i) {rVirialsXYPar[i] = VectorCache.getZeros(rVirialsXY.size());}}
        IVector @Nullable[] rVirialsXZPar = rVirialsXZ!=null ? new IVector[tNumThreads] : null; if (rVirialsXZ != null) {rVirialsXZPar[0] = rVirialsXZ; for (int i = 1; i < tNumThreads; ++i) {rVirialsXZPar[i] = VectorCache.getZeros(rVirialsXZ.size());}}
        IVector @Nullable[] rVirialsYZPar = rVirialsYZ!=null ? new IVector[tNumThreads] : null; if (rVirialsYZ != null) {rVirialsYZPar[0] = rVirialsYZ; for (int i = 1; i < tNumThreads; ++i) {rVirialsYZPar[i] = VectorCache.getZeros(rVirialsYZ.size());}}
        IVector @Nullable[] rVirialsYXPar = rVirialsYX!=null ? new IVector[tNumThreads] : null; if (rVirialsYX != null) {rVirialsYXPar[0] = rVirialsYX; for (int i = 1; i < tNumThreads; ++i) {rVirialsYXPar[i] = VectorCache.getZeros(rVirialsYX.size());}}
        IVector @Nullable[] rVirialsZXPar = rVirialsZX!=null ? new IVector[tNumThreads] : null; if (rVirialsZX != null) {rVirialsZXPar[0] = rVirialsZX; for (int i = 1; i < tNumThreads; ++i) {rVirialsZXPar[i] = VectorCache.getZeros(rVirialsZX.size());}}
        IVector @Nullable[] rVirialsZYPar = rVirialsZY!=null ? new IVector[tNumThreads] : null; if (rVirialsZY != null) {rVirialsZYPar[0] = rVirialsZY; for (int i = 1; i < tNumThreads; ++i) {rVirialsZYPar[i] = VectorCache.getZeros(rVirialsZY.size());}}
        // 遍历所有原子计算力
        mPool.parforWithException(mNumAtoms, this::initDo, this::finalDo, (i, threadID) -> {
            final IVector tEnergies = rEnergiesPar[threadID];
            final @Nullable IVector tForcesX = rForcesX!=null ? rForcesXPar[threadID] : null;
            final @Nullable IVector tForcesY = rForcesY!=null ? rForcesYPar[threadID] : null;
            final @Nullable IVector tForcesZ = rForcesZ!=null ? rForcesZPar[threadID] : null;
            final @Nullable IVector tVirialsXX = rVirialsXX!=null ? rVirialsXXPar[threadID] : null;
            final @Nullable IVector tVirialsYY = rVirialsYY!=null ? rVirialsYYPar[threadID] : null;
            final @Nullable IVector tVirialsZZ = rVirialsZZ!=null ? rVirialsZZPar[threadID] : null;
            final @Nullable IVector tVirialsXY = rVirialsXY!=null ? rVirialsXYPar[threadID] : null;
            final @Nullable IVector tVirialsXZ = rVirialsXZ!=null ? rVirialsXZPar[threadID] : null;
            final @Nullable IVector tVirialsYZ = rVirialsYZ!=null ? rVirialsYZPar[threadID] : null;
            final @Nullable IVector tVirialsYX = rVirialsYX!=null ? rVirialsYXPar[threadID] : null;
            final @Nullable IVector tVirialsZX = rVirialsZX!=null ? rVirialsZXPar[threadID] : null;
            final @Nullable IVector tVirialsZY = rVirialsZY!=null ? rVirialsZYPar[threadID] : null;
            calEnergyForceVirialSingle(threadID, i, !tCalEnergy ? null : (j, eng) -> {
                if (tEnergies.size()==1) {
                    tEnergies.add(0, eng);
                } else {
                    // 根据每个 idx 来控制能量具体累加的逻辑
                    if (i>=0 && j>=0) {
                        tEnergies.add(i, eng*0.5);
                        tEnergies.add(j, eng*0.5);
                    } else
                    if (i>=0) {
                        tEnergies.add(i, eng);
                    } else
                    if (j>=0) {
                        tEnergies.add(j, eng);
                    } else {
                        throw new IllegalStateException();
                    }
                }
            }, !tCalForce ? null : (j, fx, fy, fz) -> {
                // 根据每个 idx 来控制力具体累加的逻辑
                if (i>=0 && j>=0) {
                    if (tForcesX != null) {tForcesX.add(i, -fx); tForcesX.add(j, fx);}
                    if (tForcesY != null) {tForcesY.add(i, -fy); tForcesY.add(j, fy);}
                    if (tForcesZ != null) {tForcesZ.add(i, -fz); tForcesZ.add(j, fz);}
                } else
                if (i>=0) {
                    if (tForcesX != null) {tForcesX.add(i, -fx);}
                    if (tForcesY != null) {tForcesY.add(i, -fy);}
                    if (tForcesZ != null) {tForcesZ.add(i, -fz);}
                } else
                if (j>=0) {
                    if (tForcesX != null) {tForcesX.add(j, fx);}
                    if (tForcesY != null) {tForcesY.add(j, fy);}
                    if (tForcesZ != null) {tForcesZ.add(j, fz);}
                } else {
                    throw new IllegalStateException();
                }
            }, !tCalVirial ? null : (j, fx, fy, fz, dx, dy, dz) -> {
                // 根据每个 idx 来控制位力具体累加的逻辑
                if (i>=0 && j>=0) {
                    if (tVirialsXX != null) {if (tVirialsXX.size()==1) {tVirialsXX.add(0, dx*fx);} else {tVirialsXX.add(i, 0.5*dx*fx); tVirialsXX.add(j, 0.5*dx*fx);}}
                    if (tVirialsYY != null) {if (tVirialsYY.size()==1) {tVirialsYY.add(0, dy*fy);} else {tVirialsYY.add(i, 0.5*dy*fy); tVirialsYY.add(j, 0.5*dy*fy);}}
                    if (tVirialsZZ != null) {if (tVirialsZZ.size()==1) {tVirialsZZ.add(0, dz*fz);} else {tVirialsZZ.add(i, 0.5*dz*fz); tVirialsZZ.add(j, 0.5*dz*fz);}}
                    if (tVirialsXY != null) {if (tVirialsXY.size()==1) {tVirialsXY.add(0, dx*fy);} else {tVirialsXY.add(i, 0.5*dx*fy); tVirialsXY.add(j, 0.5*dx*fy);}}
                    if (tVirialsXZ != null) {if (tVirialsXZ.size()==1) {tVirialsXZ.add(0, dx*fz);} else {tVirialsXZ.add(i, 0.5*dx*fz); tVirialsXZ.add(j, 0.5*dx*fz);}}
                    if (tVirialsYZ != null) {if (tVirialsYZ.size()==1) {tVirialsYZ.add(0, dy*fz);} else {tVirialsYZ.add(i, 0.5*dy*fz); tVirialsYZ.add(j, 0.5*dy*fz);}}
                    if (tVirialsYX != null) {tVirialsYX.add(i, 0.5*dy*fx); tVirialsYX.add(j, 0.5*dy*fx);}
                    if (tVirialsZX != null) {tVirialsZX.add(i, 0.5*dz*fx); tVirialsZX.add(j, 0.5*dz*fx);}
                    if (tVirialsZY != null) {tVirialsZY.add(i, 0.5*dz*fy); tVirialsZY.add(j, 0.5*dz*fy);}
                } else
                if (i>=0) {
                    if (tVirialsXX != null) {if (tVirialsXX.size()==1) {tVirialsXX.add(0, dx*fx);} else {tVirialsXX.add(i, dx*fx);}}
                    if (tVirialsYY != null) {if (tVirialsYY.size()==1) {tVirialsYY.add(0, dy*fy);} else {tVirialsYY.add(i, dy*fy);}}
                    if (tVirialsZZ != null) {if (tVirialsZZ.size()==1) {tVirialsZZ.add(0, dz*fz);} else {tVirialsZZ.add(i, dz*fz);}}
                    if (tVirialsXY != null) {if (tVirialsXY.size()==1) {tVirialsXY.add(0, dx*fy);} else {tVirialsXY.add(i, dx*fy);}}
                    if (tVirialsXZ != null) {if (tVirialsXZ.size()==1) {tVirialsXZ.add(0, dx*fz);} else {tVirialsXZ.add(i, dx*fz);}}
                    if (tVirialsYZ != null) {if (tVirialsYZ.size()==1) {tVirialsYZ.add(0, dy*fz);} else {tVirialsYZ.add(i, dy*fz);}}
                    if (tVirialsYX != null) {tVirialsYX.add(i, dy*fx);}
                    if (tVirialsZX != null) {tVirialsZX.add(i, dz*fx);}
                    if (tVirialsZY != null) {tVirialsZY.add(i, dz*fy);}
                } else
                if (j>=0) {
                    if (tVirialsXX != null) {if (tVirialsXX.size()==1) {tVirialsXX.add(0, dx*fx);} else {tVirialsXX.add(j, dx*fx);}}
                    if (tVirialsYY != null) {if (tVirialsYY.size()==1) {tVirialsYY.add(0, dy*fy);} else {tVirialsYY.add(j, dy*fy);}}
                    if (tVirialsZZ != null) {if (tVirialsZZ.size()==1) {tVirialsZZ.add(0, dz*fz);} else {tVirialsZZ.add(j, dz*fz);}}
                    if (tVirialsXY != null) {if (tVirialsXY.size()==1) {tVirialsXY.add(0, dx*fy);} else {tVirialsXY.add(j, dx*fy);}}
                    if (tVirialsXZ != null) {if (tVirialsXZ.size()==1) {tVirialsXZ.add(0, dx*fz);} else {tVirialsXZ.add(j, dx*fz);}}
                    if (tVirialsYZ != null) {if (tVirialsYZ.size()==1) {tVirialsYZ.add(0, dy*fz);} else {tVirialsYZ.add(j, dy*fz);}}
                    if (tVirialsYX != null) {tVirialsYX.add(j, dy*fx);}
                    if (tVirialsZX != null) {tVirialsZX.add(j, dz*fx);}
                    if (tVirialsZY != null) {tVirialsZY.add(j, dz*fy);}
                } else {
                    throw new IllegalStateException();
                }
            });
        });
        // 累加其余线程的数据然后归还临时变量
        if (rEnergies != null) {for (int i = 1; i < tNumThreads; ++i) {rEnergies.plus2this(rEnergiesPar[i]); VectorCache.returnVec(rEnergiesPar[i]);}}
        if (rForcesZ != null) {for (int i = 1; i < tNumThreads; ++i) {rForcesZ.plus2this(rForcesZPar[i]); VectorCache.returnVec(rForcesZPar[i]);}}
        if (rForcesY != null) {for (int i = 1; i < tNumThreads; ++i) {rForcesY.plus2this(rForcesYPar[i]); VectorCache.returnVec(rForcesYPar[i]);}}
        if (rForcesX != null) {for (int i = 1; i < tNumThreads; ++i) {rForcesX.plus2this(rForcesXPar[i]); VectorCache.returnVec(rForcesXPar[i]);}}
        if (rVirialsZY != null) {for (int i = 1; i < tNumThreads; ++i) {rVirialsZY.plus2this(rVirialsZYPar[i]); VectorCache.returnVec(rVirialsZYPar[i]);}}
        if (rVirialsZX != null) {for (int i = 1; i < tNumThreads; ++i) {rVirialsZX.plus2this(rVirialsZXPar[i]); VectorCache.returnVec(rVirialsZXPar[i]);}}
        if (rVirialsYX != null) {for (int i = 1; i < tNumThreads; ++i) {rVirialsYX.plus2this(rVirialsYXPar[i]); VectorCache.returnVec(rVirialsYXPar[i]);}}
        if (rVirialsYZ != null) {for (int i = 1; i < tNumThreads; ++i) {rVirialsYZ.plus2this(rVirialsYZPar[i]); VectorCache.returnVec(rVirialsYZPar[i]);}}
        if (rVirialsXZ != null) {for (int i = 1; i < tNumThreads; ++i) {rVirialsXZ.plus2this(rVirialsXZPar[i]); VectorCache.returnVec(rVirialsXZPar[i]);}}
        if (rVirialsXY != null) {for (int i = 1; i < tNumThreads; ++i) {rVirialsXY.plus2this(rVirialsXYPar[i]); VectorCache.returnVec(rVirialsXYPar[i]);}}
        if (rVirialsZZ != null) {for (int i = 1; i < tNumThreads; ++i) {rVirialsZZ.plus2this(rVirialsZZPar[i]); VectorCache.returnVec(rVirialsZZPar[i]);}}
        if (rVirialsYY != null) {for (int i = 1; i < tNumThreads; ++i) {rVirialsYY.plus2this(rVirialsYYPar[i]); VectorCache.returnVec(rVirialsYYPar[i]);}}
        if (rVirialsXX != null) {for (int i = 1; i < tNumThreads; ++i) {rVirialsXX.plus2this(rVirialsXXPar[i]); VectorCache.returnVec(rVirialsXXPar[i]);}}
    }
}
