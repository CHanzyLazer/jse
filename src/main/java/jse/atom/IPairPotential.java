package jse.atom;

import jse.math.vector.IntVector;
import jse.math.vector.Vector;
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
     * 可选的对于不同中心原子种类不同的截断半径设定，默认为 {@link #rcutMax()}
     * <p>
     * 注意此项设计上不依赖 {@link #setData(IAtomData)}，因此输入的种类编号为此 {@link IPairPotential}
     * 中使用的种类编号（即 typeMap 后）。
     *
     * @param aType 获取截断半径的种类编号，注意为此 potential 内使用编号
     * @return 对于种类 {@code aType} 的截断半径
     */
    @ApiStatus.Experimental
    default double rcut(int aType) {return rcutMax();}
    /**
     * 标记此势函数是否不同种类会采用不同的截断半径，
     * 此时构建近邻列表缓存时会根据种类进一步筛选近邻
     * @return 此势函数是否不同种类采用不同截断半径
     */
    default boolean typewiseCutoff() {return false;}
    /**
     * 标记此势函数是否是消息传递势，当不是消息传递时，能量总是局域的，
     * 从而直接通过单个近邻列表来计算单个原子的能量和受力是可行的。
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
    
    
    @ApiStatus.Internal ParforThreadPool pool_();
    @ApiStatus.Internal NeighborListGetter nl_();
    
    /**
     * 获取内部原子数据的拷贝，从而得到修改种类后的构型
     * @return 内部原子数据的拷贝
     */
    @ApiStatus.Experimental
    default ISettableAtomData data() {
        return nl_().data();
    }
    /**
     * 获取内部原子数据索引 {@code i} 处的种类编号，注意为
     * {@link #setData(IAtomData)} 中直出的种类编号
     * @param i 需要获取种类编号的索引
     * @return 索引 {@code i} 处的种类编号，从 {@code 1} 开始
     */
    @ApiStatus.Experimental
    default int typeAt(int i) {
        return nl_().typeAt(i);
    }
    /**
     * 设置内部原子数据索引 {@code i} 处的种类编号，注意为
     * {@link #setData(IAtomData)} 中直出的种类编号
     * @param i 需要设置种类编号的索引
     * @param type 设置的目标种类编号
     */
    @ApiStatus.Experimental
    default void setTypeAt(int i, int type) {
        nl_().setTypeAt(i, type);
    }
    
    /**
     * 计算给定索引的单个原子能量接口，可以用于简单实现 MC 算法中进行部分修改后的能量更新
     *
     * @param aThreadID 可选当前调用的线程 id，对于串行情况总是传入 0
     * @param aI 需要计算的原子索引
     * @return 计算得到的此原子能量
     * @throws Exception 特殊实现下可选的抛出异常
     */
    @ApiStatus.Experimental
    double calEnergySingle(int aThreadID, int aI) throws Exception;
    /**
     * 输入一个通用的近邻列表后计算单个原子能量的接口，此接口设计上为不依赖 {@link #setData(IAtomData)}
     * 的通用接口，可以用于实现通用的高效 MC 算法中进行部分修改后的能量更新
     * <p>
     * 注意由于不依赖 {@link #setData(IAtomData)}，因此所有种类编号为此 {@link IPairPotential}
     * 中使用的种类编号（即 typeMap 后）。
     *
     * @param aThreadID 可选当前调用的线程 id，对于串行情况总是传入 0
     * @param aCType 需要计算的中心原子种类编号，注意为此 potential 内使用编号
     * @param aNlDx 近邻的 dx 组成的列表，{@code dx = xj - xi}
     * @param aNlDy 近邻的 dy 组成的列表，{@code dy = yj - yi}
     * @param aNlDz 近邻的 dz 组成的列表，{@code dz = zj - zi}
     * @param aNlType 近邻的种类编号组成的列表，注意为此 potential 内使用编号
     * @return 计算得到的此原子能量
     * @throws UnsupportedOperationException 对于不支持仅根据单个近邻计算单个原子能量的情况
     * @throws Exception 特殊实现下可选的抛出异常
     */
    @ApiStatus.Experimental
    double calEnergySingle(int aThreadID, int aCType, Vector aNlDx, Vector aNlDy, Vector aNlDz, IntVector aNlType) throws Exception;
    
    /**
     * 计算给定索引的单个原子能量以及对近邻的力的接口
     *
     * @param aThreadID 可选当前调用的线程 id，对于串行情况总是传入 0
     * @param aI 需要计算的原子索引
     * @param rGradNlDx 能量关于近邻 dx 梯度组成的列表，物理上对应中心原子对近邻之间的力
     * @param rGradNlDy 能量关于近邻 dy 梯度组成的列表，物理上对应中心原子对近邻之间的力
     * @param rGradNlDz 能量关于近邻 dz 梯度组成的列表，物理上对应中心原子对近邻之间的力
     * @return 计算得到的此原子能量
     * @throws Exception 特殊实现下可选的抛出异常
     */
    @ApiStatus.Experimental
    double calEnergyForceSingle(int aThreadID, int aI, Vector rGradNlDx, Vector rGradNlDy, Vector rGradNlDz) throws Exception;
    /**
     * 输入一个通用的近邻列表后计算单个原子能量以及对近邻的力的接口，此接口设计上为不依赖
     * {@link #setData(IAtomData)} 的通用接口。
     * <p>
     * 注意由于不依赖 {@link #setData(IAtomData)}，因此所有种类编号为此 {@link IPairPotential}
     * 中使用的种类编号（即 typeMap 后）。
     *
     * @param aThreadID 可选当前调用的线程 id，对于串行情况总是传入 0
     * @param aCType 需要计算的中心原子种类编号，注意为此 potential 内使用编号
     * @param aNlDx 近邻的 dx 组成的列表，{@code dx = xj - xi}
     * @param aNlDy 近邻的 dy 组成的列表，{@code dy = yj - yi}
     * @param aNlDz 近邻的 dz 组成的列表，{@code dz = zj - zi}
     * @param aNlType 近邻的种类编号组成的列表，注意为此 potential 内使用编号
     * @param rGradNlDx 能量关于近邻 dx 梯度组成的列表，物理上对应中心原子对近邻之间的力
     * @param rGradNlDy 能量关于近邻 dy 梯度组成的列表，物理上对应中心原子对近邻之间的力
     * @param rGradNlDz 能量关于近邻 dz 梯度组成的列表，物理上对应中心原子对近邻之间的力
     * @return 计算得到的此原子能量
     * @throws UnsupportedOperationException 对于不支持仅根据单个近邻计算单个原子能量的情况
     * @throws Exception 特殊实现下可选的抛出异常
     */
    @ApiStatus.Experimental
    double calEnergyForceSingle(int aThreadID, int aCType, Vector aNlDx, Vector aNlDy, Vector aNlDz, IntVector aNlType,
                                Vector rGradNlDx, Vector rGradNlDy, Vector rGradNlDz) throws Exception;
}
