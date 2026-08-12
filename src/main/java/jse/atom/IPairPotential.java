package jse.atom;

import jse.cache.VectorCache;
import jse.code.collection.ISlice;
import jse.math.vector.IVector;
import jse.math.vector.Vector;
import jse.parallel.ParforThreadPool;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * 通用的基于截断半径内原子相互作用（pair）实现的势函数，
 * 内部会统一采用 {@link NeighborListGetter}
 * 来获取近邻列表信息
 * <p>
 * 有些势函数依赖元素种类符号，因此存在符号接口 {@link IHasSymbol}，
 * 如果势函数实现了相关接口，且输入的原子数据 {@link IAtomData}
 * 包含符号信息，则会自动根据这些元素符号来重新映射种类。
 *
 * @see IPotential IPotential: 通用的势函数接口
 * @author liqa
 */
public interface IPairPotential extends IPotential, IHasSymbol {
    
    /** @return 是否支持计算 9 列的每原子压力 */
    @Override default boolean centroidPerAtomStressSupport() {return true;}
    /** @return 此势函数支持的原子种类数目，默认为 {@code -1} 表示没有种类数目限制 */
    @Override default int ntypes() {return -1;}
    /** @return {@inheritDoc}；如果存在则会自动根据元素符号重新映射种类 */
    @Override default boolean hasSymbol() {return false;}
    /**
     * {@inheritDoc}
     * @param aType {@inheritDoc}
     * @return {@inheritDoc}；如果存在则会自动根据元素符号重新映射种类
     */
    @Override default @Nullable String symbol(int aType) {return null;}
    /** @return {@inheritDoc}；如果存在则会自动根据元素符号重新映射种类 */
    @Override default @Nullable List<@Nullable String> symbols() {return IHasSymbol.super.symbols();}
    
    /** 用于计算原子相互作用势能的近邻列表获取器 */
    @FunctionalInterface interface INeighborListGetter {
        void forEachNLWithException(@Nullable ParforThreadPool.ITaskWithIDAndException aInitDo, @Nullable ParforThreadPool.ITaskWithIDAndException aFinalDo, INeighborListDoWithException aNeighborListDo) throws Exception;
        default void forEachNL(INeighborListDo aNeighborListDo) {
            try {forEachNLWithException(null, null, aNeighborListDo::run);}
            catch (Exception e) {throw new RuntimeException(e);}
        }
    }
    /** 获取到每个原子的近邻列表需要进行的操作 */
    @FunctionalInterface interface INeighborListDoWithException {
        void run(int aThreadID, int cIdx, int cType, IDxyzTypeIdxIterable aNL) throws Exception;
    }
    @FunctionalInterface interface INeighborListDo {
        void run(int aThreadID, int cIdx, int cType, IDxyzTypeIdxIterable aNL);
    }
    
    /** 用于计算原子相互作用势能的近邻列表 */
    @FunctionalInterface interface IDxyzTypeIdxIterable {
        void forEachDxyzTypeIdx(IDxyzTypeIdxDo aDxyzTypeIdxDo);
    }
    /** 每个原子获取到近邻后的后续操作 */
    @FunctionalInterface interface IDxyzTypeIdxDo {
        void run(double aDx, double aDy, double aDz, int aType, int aIdx);
    }
    
    /** 能量累加接口，用于接收能量计算结果 */
    @FunctionalInterface interface IEnergyAccumulator {
        void add(int aThreadID, int cIdx, int aIdx, double aEnergy);
    }
    @FunctionalInterface interface IEnergyPartAccumulator {
        void add(int aThreadID, int cIdx, double aEnergy);
    }
    /** 力累加接口，用于接收力计算结果 */
    @FunctionalInterface interface IForceAccumulator {
        void add(int aThreadID, int cIdx, int aIdx, double aForceX, double aForceY, double aForceZ);
    }
    /** 位力累加接口，用于接收位力计算结果 */
    @FunctionalInterface interface IVirialAccumulator {
        void add(int aThreadID, int cIdx, int aIdx, double aForceX, double aForceY, double aForceZ, double aDx, double aDy, double aDz);
    }
    
    /**
     * 此势函数期望使用的线程数，默认永远为 {@code 1}
     * @return 此势函数期望使用的线程数
     */
    default int nthreads() {return 1;}
    /**
     * 获取此势函数的（最大）截断半径，用来在计算单粒子移动、翻转、种类交换时获取较小的影响原子范围，
     * 从而加速这些计算。默认为 {@code -1}，表示不能进行这些优化
     * @return 势函数的（最大）截断半径
     */
    default double rcutMax() {return -1;}
    /**
     * 标记此势函数是否是多体势，当不是多体势时，能量可以表示为每两个原子对之间的能量和。
     * 此时计算单粒子移动、翻转、种类交换时的能量差可以更进一步的优化，
     * 直接只需要考虑修改的原子自身的能量变化即可
     * @return 此势函数是否是多体势
     */
    default boolean manybody() {return true;}
    /**
     * 标记此势函数内部的通用近邻遍历内核实现是否总是认为是一半的近邻列表遍历
     * @return 此势函数内核实现中是否认为是一半的近邻列表遍历
     */
    default boolean neighborListHalf() {return false;}
    
    
    @ApiStatus.Internal ParforThreadPool pool_();
    @ApiStatus.Internal NeighborListGetter nl_();
    
    /**
     * {@inheritDoc}
     * @param aAtomData {@inheritDoc}
     * @param aIndices {@inheritDoc}
     * @return {@inheritDoc}
     * @throws Exception {@inheritDoc}
     */
    @Override default double calEnergyAt(IAtomData aAtomData, ISlice aIndices) throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        final IntUnaryOperator tTypeMap = hasSymbol() ? typeMap(aAtomData) : type->type;
        final NeighborListGetter tNl = nl_().setData(aAtomData).setRCut(rcutMax()); tNl.build();
        final int tTypeNum = ntypes();
        final Vector rEngPar = VectorCache.getZeros(nthreads());
        calEnergyPart(aAtomData.natoms(), (initDo, finalDo, neighborListDo) -> {
            pool_().parforWithException(aIndices.size(), initDo, finalDo, (i, threadID) -> {
                final int cIdx = aIndices.get(i);
                final int cType = tTypeNum<=0 ? 0 : tTypeMap.applyAsInt(tNl.typeAt(cIdx));
                neighborListDo.run(threadID, i, cType, dxyzTypeDo -> {
                    tNl.forEachNeighbor(i, false, (dx, dy, dz, idx) -> {
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
    
    /**
     * 通过此势函数计算给定的部分的近邻列表获取器包含的原子能量，将计算值传入 rEnergyAccumulator；
     * 由于是部分的，因此近邻列表总是完整的
     * @param aAtomNumber 遍历中涉及到的最大的原子数 {@code (idx < atomNumber)}
     * @param aNeighborListGetter 通用的近邻列表获取器
     * @param rEnergyAccumulator 接受能量的收集器
     * @throws Exception 特殊实现下可选的抛出异常
     */
    @ApiStatus.Experimental
    default void calEnergyPart(int aAtomNumber, INeighborListGetter aNeighborListGetter, IEnergyPartAccumulator rEnergyAccumulator) throws Exception {
        final double tMul = neighborListHalf() ? 0.5 : 1.0;
        calEnergy(aAtomNumber, aNeighborListGetter, (threadID, cIdx, idx, eng) -> {
            // 对于根据键累加的能量增加二次累加修正
            if (cIdx>=0 && idx>=0) {
                rEnergyAccumulator.add(threadID, cIdx, eng*tMul);
            } else
            if (cIdx>=0) {
                rEnergyAccumulator.add(threadID, cIdx, eng);
            } else {
                throw new IllegalStateException();
            }
        });
    }
    
    /**
     * 通过此势函数计算给定近邻列表获取器包含的原子能量，将计算值传入 rEnergyAccumulator
     * @param aAtomNumber 遍历中涉及到的最大的原子数 {@code (idx < atomNumber)}
     * @param aNeighborListGetter 通用的近邻列表获取器
     * @param rEnergyAccumulator 接受能量的收集器
     * @throws Exception 特殊实现下可选的抛出异常
     */
    @ApiStatus.Experimental
    void calEnergy(int aAtomNumber, INeighborListGetter aNeighborListGetter, IEnergyAccumulator rEnergyAccumulator) throws Exception;
    
    /**
     * 通过此势函数计算给定近邻列表获取器包含的原子的能量力和位力，分别将计算值传入 rEnergyAccumulator, rForceAccumulator, rVirialAccumulator
     * @param aAtomNumber 遍历中涉及到的最大的原子数 {@code (idx < atomNumber)}
     * @param aNeighborListGetter 通用的近邻列表获取器
     * @param rEnergyAccumulator 接受能量的收集器，{@code null} 表示不需要此值
     * @param rForceAccumulator 接受力的收集器，{@code null} 表示不需要此值
     * @param rVirialAccumulator 接受位力的收集器，{@code null} 表示不需要此值
     * @throws Exception 特殊实现下可选的抛出异常
     */
    @ApiStatus.Experimental
    void calEnergyForceVirial(int aAtomNumber, INeighborListGetter aNeighborListGetter, @Nullable IEnergyAccumulator rEnergyAccumulator, @Nullable IForceAccumulator rForceAccumulator, @Nullable IVirialAccumulator rVirialAccumulator) throws Exception;
    
    
    /**
     * {@inheritDoc}
     * @param aAtomData {@inheritDoc}
     * @param rEnergies {@inheritDoc}
     * @param rForcesX {@inheritDoc}
     * @param rForcesY {@inheritDoc}
     * @param rForcesZ {@inheritDoc}
     * @param rVirialsXX {@inheritDoc}
     * @param rVirialsYY {@inheritDoc}
     * @param rVirialsZZ {@inheritDoc}
     * @param rVirialsXY {@inheritDoc}
     * @param rVirialsXZ {@inheritDoc}
     * @param rVirialsYZ {@inheritDoc}
     * @param rVirialsYX {@inheritDoc}
     * @param rVirialsZX {@inheritDoc}
     * @param rVirialsZY {@inheritDoc}
     * @throws Exception {@inheritDoc}
     */
    @Override default void calEnergyForceVirials(IAtomData aAtomData, @Nullable IVector rEnergies, @Nullable IVector rForcesX, @Nullable IVector rForcesY, @Nullable IVector rForcesZ, @Nullable IVector rVirialsXX, @Nullable IVector rVirialsYY, @Nullable IVector rVirialsZZ, @Nullable IVector rVirialsXY, @Nullable IVector rVirialsXZ, @Nullable IVector rVirialsYZ, @Nullable IVector rVirialsYX, @Nullable IVector rVirialsZX, @Nullable IVector rVirialsZY) throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        final IntUnaryOperator tTypeMap = hasSymbol() ? typeMap(aAtomData) : type->type;
        final NeighborListGetter tNl = nl_().setData(aAtomData).setRCut(rcutMax()); tNl.build();
        // 统一存储常量
        final boolean tCalEnergy = rEnergies!=null;
        final boolean tCalForce = rForcesX!=null || rForcesY!=null || rForcesZ!=null;
        final boolean tCalVirial = rVirialsXX!=null || rVirialsYY!=null || rVirialsZZ!=null || rVirialsXY!=null || rVirialsXZ!=null || rVirialsYZ!=null || rVirialsYX!=null || rVirialsZX!=null || rVirialsZY!=null;
        final int tTypeNum = ntypes();
        final int tAtomNum = aAtomData.natoms();
        final int tThreadNum = nthreads();
        final boolean tNLHalf = neighborListHalf();
        // 清空可能存在的旧值
        if (tCalEnergy) rEnergies.fill(0.0);
        // 并行情况下存在并行写入的问题，因此需要这样操作
        IVector @Nullable[] rEnergiesPar = rEnergies!=null ? new IVector[tThreadNum] : null;
        if (tCalEnergy) {
            rEnergiesPar[0] = rEnergies;
            for (int i = 1; i < tThreadNum; ++i) {
                rEnergiesPar[i] = VectorCache.getZeros(rEnergies.size());
            }
        }
        /// 特殊处理只需要计算能量的情况
        if (!tCalForce && !tCalVirial) {
            if (!tCalEnergy) {
                return;
            }
            calEnergy(tAtomNum, (initDo, finalDo, neighborListDo) -> {
                pool_().parforWithException(tAtomNum, initDo, finalDo, (i, threadID) -> {
                    final int cType = tTypeNum<=0 ? 0 : tTypeMap.applyAsInt(tNl.typeAt(i));
                    neighborListDo.run(threadID, i, cType, dxyzTypeDo -> {
                        // 根据 neighborListHalf 来确定是否开启半数优化
                        tNl.forEachNeighbor(i, tNLHalf, (dx, dy, dz, idx) -> {
                            int tType = tTypeNum<=0 ? 0 : tTypeMap.applyAsInt(tNl.typeAt(idx));
                            dxyzTypeDo.run(dx, dy, dz, tType, idx);
                        });
                    });
                });
            }, (threadID, cIdx, idx, eng) -> {
                final IVector tEnergies = rEnergiesPar[threadID];
                if (tEnergies.size()==1) {
                    tEnergies.add(0, eng);
                } else {
                    // 根据每个 idx 来控制能量具体累加的逻辑
                    if (cIdx>=0 && idx>=0) {
                        tEnergies.add(cIdx, eng*0.5);
                        tEnergies.add(idx, eng*0.5);
                    } else
                    if (cIdx>=0) {
                        tEnergies.add(cIdx, eng);
                    } else
                    if (idx>=0) {
                        tEnergies.add(idx, eng);
                    } else {
                        throw new IllegalStateException();
                    }
                }
            });
            for (int i = 1; i < tThreadNum; ++i) {
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
        IVector @Nullable[] rForcesXPar = rForcesX!=null ? new IVector[tThreadNum] : null; if (rForcesX != null) {rForcesXPar[0] = rForcesX; for (int i = 1; i < tThreadNum; ++i) {rForcesXPar[i] = VectorCache.getZeros(tAtomNum);}}
        IVector @Nullable[] rForcesYPar = rForcesY!=null ? new IVector[tThreadNum] : null; if (rForcesY != null) {rForcesYPar[0] = rForcesY; for (int i = 1; i < tThreadNum; ++i) {rForcesYPar[i] = VectorCache.getZeros(tAtomNum);}}
        IVector @Nullable[] rForcesZPar = rForcesZ!=null ? new IVector[tThreadNum] : null; if (rForcesZ != null) {rForcesZPar[0] = rForcesZ; for (int i = 1; i < tThreadNum; ++i) {rForcesZPar[i] = VectorCache.getZeros(tAtomNum);}}
        IVector @Nullable[] rVirialsXXPar = rVirialsXX!=null ? new IVector[tThreadNum] : null; if (rVirialsXX != null) {rVirialsXXPar[0] = rVirialsXX; for (int i = 1; i < tThreadNum; ++i) {rVirialsXXPar[i] = VectorCache.getZeros(rVirialsXX.size());}}
        IVector @Nullable[] rVirialsYYPar = rVirialsYY!=null ? new IVector[tThreadNum] : null; if (rVirialsYY != null) {rVirialsYYPar[0] = rVirialsYY; for (int i = 1; i < tThreadNum; ++i) {rVirialsYYPar[i] = VectorCache.getZeros(rVirialsYY.size());}}
        IVector @Nullable[] rVirialsZZPar = rVirialsZZ!=null ? new IVector[tThreadNum] : null; if (rVirialsZZ != null) {rVirialsZZPar[0] = rVirialsZZ; for (int i = 1; i < tThreadNum; ++i) {rVirialsZZPar[i] = VectorCache.getZeros(rVirialsZZ.size());}}
        IVector @Nullable[] rVirialsXYPar = rVirialsXY!=null ? new IVector[tThreadNum] : null; if (rVirialsXY != null) {rVirialsXYPar[0] = rVirialsXY; for (int i = 1; i < tThreadNum; ++i) {rVirialsXYPar[i] = VectorCache.getZeros(rVirialsXY.size());}}
        IVector @Nullable[] rVirialsXZPar = rVirialsXZ!=null ? new IVector[tThreadNum] : null; if (rVirialsXZ != null) {rVirialsXZPar[0] = rVirialsXZ; for (int i = 1; i < tThreadNum; ++i) {rVirialsXZPar[i] = VectorCache.getZeros(rVirialsXZ.size());}}
        IVector @Nullable[] rVirialsYZPar = rVirialsYZ!=null ? new IVector[tThreadNum] : null; if (rVirialsYZ != null) {rVirialsYZPar[0] = rVirialsYZ; for (int i = 1; i < tThreadNum; ++i) {rVirialsYZPar[i] = VectorCache.getZeros(rVirialsYZ.size());}}
        IVector @Nullable[] rVirialsYXPar = rVirialsYX!=null ? new IVector[tThreadNum] : null; if (rVirialsYX != null) {rVirialsYXPar[0] = rVirialsYX; for (int i = 1; i < tThreadNum; ++i) {rVirialsYXPar[i] = VectorCache.getZeros(rVirialsYX.size());}}
        IVector @Nullable[] rVirialsZXPar = rVirialsZX!=null ? new IVector[tThreadNum] : null; if (rVirialsZX != null) {rVirialsZXPar[0] = rVirialsZX; for (int i = 1; i < tThreadNum; ++i) {rVirialsZXPar[i] = VectorCache.getZeros(rVirialsZX.size());}}
        IVector @Nullable[] rVirialsZYPar = rVirialsZY!=null ? new IVector[tThreadNum] : null; if (rVirialsZY != null) {rVirialsZYPar[0] = rVirialsZY; for (int i = 1; i < tThreadNum; ++i) {rVirialsZYPar[i] = VectorCache.getZeros(rVirialsZY.size());}}
        // 遍历所有原子计算力
        calEnergyForceVirial(tAtomNum, (initDo, finalDo, neighborListDo) -> {
            pool_().parforWithException(tAtomNum, initDo, finalDo, (i, threadID) -> {
                final int cType = tTypeNum<=0 ? 0 : tTypeMap.applyAsInt(tNl.typeAt(i));
                neighborListDo.run(threadID, i, cType, dxyzTypeDo -> {
                    // 根据 neighborListHalf 来确定是否开启半数优化
                    tNl.forEachNeighbor(i, tNLHalf, (dx, dy, dz, idx) -> {
                        int tType = tTypeNum<=0 ? 0 : tTypeMap.applyAsInt(tNl.typeAt(idx));
                        dxyzTypeDo.run(dx, dy, dz, tType, idx);
                    });
                });
            });
        }, !tCalEnergy ? null : (threadID, cIdx, idx, eng) -> {
            final IVector tEnergies = rEnergiesPar[threadID];
            if (tEnergies.size()==1) {
                tEnergies.add(0, eng);
            } else {
                // 根据每个 idx 来控制能量具体累加的逻辑
                if (cIdx>=0 && idx>=0) {
                    tEnergies.add(cIdx, eng*0.5);
                    tEnergies.add(idx, eng*0.5);
                } else
                if (cIdx>=0) {
                    tEnergies.add(cIdx, eng);
                } else
                if (idx>=0) {
                    tEnergies.add(idx, eng);
                } else {
                    throw new IllegalStateException();
                }
            }
        }, !tCalForce ? null : (threadID, cIdx, idx, fx, fy, fz) -> {
            final @Nullable IVector tForcesX = rForcesX!=null ? rForcesXPar[threadID] : null;
            final @Nullable IVector tForcesY = rForcesY!=null ? rForcesYPar[threadID] : null;
            final @Nullable IVector tForcesZ = rForcesZ!=null ? rForcesZPar[threadID] : null;
            // 根据每个 idx 来控制力具体累加的逻辑
            if (cIdx>=0 && idx>=0) {
                if (tForcesX != null) {tForcesX.add(cIdx, -fx); tForcesX.add(idx, fx);}
                if (tForcesY != null) {tForcesY.add(cIdx, -fy); tForcesY.add(idx, fy);}
                if (tForcesZ != null) {tForcesZ.add(cIdx, -fz); tForcesZ.add(idx, fz);}
            } else
            if (cIdx>=0) {
                if (tForcesX != null) {tForcesX.add(cIdx, -fx);}
                if (tForcesY != null) {tForcesY.add(cIdx, -fy);}
                if (tForcesZ != null) {tForcesZ.add(cIdx, -fz);}
            } else
            if (idx>=0) {
                if (tForcesX != null) {tForcesX.add(idx, fx);}
                if (tForcesY != null) {tForcesY.add(idx, fy);}
                if (tForcesZ != null) {tForcesZ.add(idx, fz);}
            } else {
                throw new IllegalStateException();
            }
        }, !tCalVirial ? null : (threadID, cIdx, idx, fx, fy, fz, dx, dy, dz) -> {
            final @Nullable IVector tVirialsXX = rVirialsXX!=null ? rVirialsXXPar[threadID] : null;
            final @Nullable IVector tVirialsYY = rVirialsYY!=null ? rVirialsYYPar[threadID] : null;
            final @Nullable IVector tVirialsZZ = rVirialsZZ!=null ? rVirialsZZPar[threadID] : null;
            final @Nullable IVector tVirialsXY = rVirialsXY!=null ? rVirialsXYPar[threadID] : null;
            final @Nullable IVector tVirialsXZ = rVirialsXZ!=null ? rVirialsXZPar[threadID] : null;
            final @Nullable IVector tVirialsYZ = rVirialsYZ!=null ? rVirialsYZPar[threadID] : null;
            final @Nullable IVector tVirialsYX = rVirialsYX!=null ? rVirialsYXPar[threadID] : null;
            final @Nullable IVector tVirialsZX = rVirialsZX!=null ? rVirialsZXPar[threadID] : null;
            final @Nullable IVector tVirialsZY = rVirialsZY!=null ? rVirialsZYPar[threadID] : null;
            // 根据每个 idx 来控制位力具体累加的逻辑
            if (cIdx>=0 && idx>=0) {
                if (tVirialsXX != null) {if (tVirialsXX.size()==1) {tVirialsXX.add(0, dx*fx);} else {tVirialsXX.add(cIdx, 0.5*dx*fx); tVirialsXX.add(idx, 0.5*dx*fx);}}
                if (tVirialsYY != null) {if (tVirialsYY.size()==1) {tVirialsYY.add(0, dy*fy);} else {tVirialsYY.add(cIdx, 0.5*dy*fy); tVirialsYY.add(idx, 0.5*dy*fy);}}
                if (tVirialsZZ != null) {if (tVirialsZZ.size()==1) {tVirialsZZ.add(0, dz*fz);} else {tVirialsZZ.add(cIdx, 0.5*dz*fz); tVirialsZZ.add(idx, 0.5*dz*fz);}}
                if (tVirialsXY != null) {if (tVirialsXY.size()==1) {tVirialsXY.add(0, dx*fy);} else {tVirialsXY.add(cIdx, 0.5*dx*fy); tVirialsXY.add(idx, 0.5*dx*fy);}}
                if (tVirialsXZ != null) {if (tVirialsXZ.size()==1) {tVirialsXZ.add(0, dx*fz);} else {tVirialsXZ.add(cIdx, 0.5*dx*fz); tVirialsXZ.add(idx, 0.5*dx*fz);}}
                if (tVirialsYZ != null) {if (tVirialsYZ.size()==1) {tVirialsYZ.add(0, dy*fz);} else {tVirialsYZ.add(cIdx, 0.5*dy*fz); tVirialsYZ.add(idx, 0.5*dy*fz);}}
                if (tVirialsYX != null) {tVirialsYX.add(cIdx, 0.5*dy*fx); tVirialsYX.add(idx, 0.5*dy*fx);}
                if (tVirialsZX != null) {tVirialsZX.add(cIdx, 0.5*dz*fx); tVirialsZX.add(idx, 0.5*dz*fx);}
                if (tVirialsZY != null) {tVirialsZY.add(cIdx, 0.5*dz*fy); tVirialsZY.add(idx, 0.5*dz*fy);}
            } else
            if (cIdx>=0) {
                if (tVirialsXX != null) {if (tVirialsXX.size()==1) {tVirialsXX.add(0, dx*fx);} else {tVirialsXX.add(cIdx, dx*fx);}}
                if (tVirialsYY != null) {if (tVirialsYY.size()==1) {tVirialsYY.add(0, dy*fy);} else {tVirialsYY.add(cIdx, dy*fy);}}
                if (tVirialsZZ != null) {if (tVirialsZZ.size()==1) {tVirialsZZ.add(0, dz*fz);} else {tVirialsZZ.add(cIdx, dz*fz);}}
                if (tVirialsXY != null) {if (tVirialsXY.size()==1) {tVirialsXY.add(0, dx*fy);} else {tVirialsXY.add(cIdx, dx*fy);}}
                if (tVirialsXZ != null) {if (tVirialsXZ.size()==1) {tVirialsXZ.add(0, dx*fz);} else {tVirialsXZ.add(cIdx, dx*fz);}}
                if (tVirialsYZ != null) {if (tVirialsYZ.size()==1) {tVirialsYZ.add(0, dy*fz);} else {tVirialsYZ.add(cIdx, dy*fz);}}
                if (tVirialsYX != null) {tVirialsYX.add(cIdx, dy*fx);}
                if (tVirialsZX != null) {tVirialsZX.add(cIdx, dz*fx);}
                if (tVirialsZY != null) {tVirialsZY.add(cIdx, dz*fy);}
            } else
            if (idx>=0) {
                if (tVirialsXX != null) {if (tVirialsXX.size()==1) {tVirialsXX.add(0, dx*fx);} else {tVirialsXX.add(idx, dx*fx);}}
                if (tVirialsYY != null) {if (tVirialsYY.size()==1) {tVirialsYY.add(0, dy*fy);} else {tVirialsYY.add(idx, dy*fy);}}
                if (tVirialsZZ != null) {if (tVirialsZZ.size()==1) {tVirialsZZ.add(0, dz*fz);} else {tVirialsZZ.add(idx, dz*fz);}}
                if (tVirialsXY != null) {if (tVirialsXY.size()==1) {tVirialsXY.add(0, dx*fy);} else {tVirialsXY.add(idx, dx*fy);}}
                if (tVirialsXZ != null) {if (tVirialsXZ.size()==1) {tVirialsXZ.add(0, dx*fz);} else {tVirialsXZ.add(idx, dx*fz);}}
                if (tVirialsYZ != null) {if (tVirialsYZ.size()==1) {tVirialsYZ.add(0, dy*fz);} else {tVirialsYZ.add(idx, dy*fz);}}
                if (tVirialsYX != null) {tVirialsYX.add(idx, dy*fx);}
                if (tVirialsZX != null) {tVirialsZX.add(idx, dz*fx);}
                if (tVirialsZY != null) {tVirialsZY.add(idx, dz*fy);}
            } else {
                throw new IllegalStateException();
            }
        });
        // 累加其余线程的数据然后归还临时变量
        if (rEnergies != null) {for (int i = 1; i < tThreadNum; ++i) {rEnergies.plus2this(rEnergiesPar[i]); VectorCache.returnVec(rEnergiesPar[i]);}}
        if (rForcesZ != null) {for (int i = 1; i < tThreadNum; ++i) {rForcesZ.plus2this(rForcesZPar[i]); VectorCache.returnVec(rForcesZPar[i]);}}
        if (rForcesY != null) {for (int i = 1; i < tThreadNum; ++i) {rForcesY.plus2this(rForcesYPar[i]); VectorCache.returnVec(rForcesYPar[i]);}}
        if (rForcesX != null) {for (int i = 1; i < tThreadNum; ++i) {rForcesX.plus2this(rForcesXPar[i]); VectorCache.returnVec(rForcesXPar[i]);}}
        if (rVirialsZY != null) {for (int i = 1; i < tThreadNum; ++i) {rVirialsZY.plus2this(rVirialsZYPar[i]); VectorCache.returnVec(rVirialsZYPar[i]);}}
        if (rVirialsZX != null) {for (int i = 1; i < tThreadNum; ++i) {rVirialsZX.plus2this(rVirialsZXPar[i]); VectorCache.returnVec(rVirialsZXPar[i]);}}
        if (rVirialsYX != null) {for (int i = 1; i < tThreadNum; ++i) {rVirialsYX.plus2this(rVirialsYXPar[i]); VectorCache.returnVec(rVirialsYXPar[i]);}}
        if (rVirialsYZ != null) {for (int i = 1; i < tThreadNum; ++i) {rVirialsYZ.plus2this(rVirialsYZPar[i]); VectorCache.returnVec(rVirialsYZPar[i]);}}
        if (rVirialsXZ != null) {for (int i = 1; i < tThreadNum; ++i) {rVirialsXZ.plus2this(rVirialsXZPar[i]); VectorCache.returnVec(rVirialsXZPar[i]);}}
        if (rVirialsXY != null) {for (int i = 1; i < tThreadNum; ++i) {rVirialsXY.plus2this(rVirialsXYPar[i]); VectorCache.returnVec(rVirialsXYPar[i]);}}
        if (rVirialsZZ != null) {for (int i = 1; i < tThreadNum; ++i) {rVirialsZZ.plus2this(rVirialsZZPar[i]); VectorCache.returnVec(rVirialsZZPar[i]);}}
        if (rVirialsYY != null) {for (int i = 1; i < tThreadNum; ++i) {rVirialsYY.plus2this(rVirialsYYPar[i]); VectorCache.returnVec(rVirialsYYPar[i]);}}
        if (rVirialsXX != null) {for (int i = 1; i < tThreadNum; ++i) {rVirialsXX.plus2this(rVirialsXXPar[i]); VectorCache.returnVec(rVirialsXXPar[i]);}}
    }
}
