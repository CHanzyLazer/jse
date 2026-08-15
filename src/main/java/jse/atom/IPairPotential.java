package jse.atom;

import jse.code.collection.ISlice;
import jse.parallel.ParforThreadPool;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    
    /**
     * 此势函数期望使用的线程数，默认永远为 {@code 1}
     * @return 此势函数期望使用的线程数
     */
    default int nthreads() {return 1;}
    /**
     * 获取此势函数的（最大）截断半径，用于构建内部近邻列表
     * @return 势函数的（最大）截断半径
     */
    double rcutMax();
    /**
     * 标记此势函数是否是消息传递势，当不是消息传递时，能量总是局域的。
     * 此时计算单粒子移动、翻转、种类交换时的能量差可以进行截断半径优化
     * @return 此势函数是否是消息传递势
     */
    default boolean messagePass() {return false;}
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
     * 获取内部原子数据的拷贝，从而得到 swap，flip 后的构型
     * @return 内部原子数据的拷贝
     */
    default ISettableAtomData data() {
        return nl_().data();
    }
    
    /**
     * 通过此势函数计算给定原子数据指定原子的总能量
     * @param aIndices 需要计算的原子的索引（从 0 开始）
     * @return 指定原子的总能量
     * @throws Exception 特殊实现下可选的抛出异常
     */
    double calEnergyAt(ISlice aIndices) throws Exception;
    
    /**
     * 计算交换种类前后的能量差，是否会考虑截断半径的优化则取决于具体的势函数的实现
     *
     * @param aI 需要交换种类的第一个原子索引
     * @param aJ 需要交换种类的第二个原子索引
     * @param aRestoreData 计算完成后是否还原内部原子数据的状态，默认为 {@code true}；如果关闭则会中保留交换后的结构
     * @return 交换后能量 - 交换前能量
     * @throws Exception 特殊实现下可选的抛出异常
     */
    double calEnergyDiffSwap(int aI, int aJ, boolean aRestoreData) throws Exception;
    /**
     * 计算交换种类前后的能量差，是否会考虑截断半径的优化则取决于具体的势函数的实现
     *
     * @param aI 需要交换种类的第一个原子索引
     * @param aJ 需要交换种类的第二个原子索引
     * @return 交换后能量 - 交换前能量
     * @throws Exception 特殊实现下可选的抛出异常
     */
    default double calEnergyDiffSwap(int aI, int aJ) throws Exception {
        return calEnergyDiffSwap(aI, aJ, true);
    }
    /**
     * 计算翻转某个元素种类前后的能量差，是否会考虑截断半径的优化则取决于具体的势函数的实现
     *
     * @param aI 需要翻转种类的原子索引
     * @param aType 此原子需要翻转的种类编号，对应输入原子数据原始的种类编号，没有经过 aTypeMap（如果有的话）
     * @param aRestoreData 计算完成后是否还原内部原子数据的状态，默认为 {@code true}；如果关闭则会保留翻转后的结构
     * @return 翻转后能量 - 翻转前能量
     * @throws Exception 特殊实现下可选的抛出异常
     */
    double calEnergyDiffFlip(int aI, int aType, boolean aRestoreData) throws Exception;
    /**
     * 计算翻转某个元素种类前后的能量差，是否会考虑截断半径的优化则取决于具体的势函数的实现
     *
     * @param aI 需要翻转种类的原子索引
     * @param aType 此原子需要翻转的种类编号，对应输入原子数据原始的种类编号，没有经过 aTypeMap（如果有的话）
     * @return 翻转后能量 - 翻转前能量
     * @throws Exception 特殊实现下可选的抛出异常
     */
    default double calEnergyDiffFlip(int aI, int aType) throws Exception {
        return calEnergyDiffFlip(aI, aType, true);
    }
}
