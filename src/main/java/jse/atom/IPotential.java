package jse.atom;

import jep.JepException;
import jep.python.PyObject;
import jse.code.SP;
import jse.math.matrix.RowMatrix;
import jse.math.vector.IVector;
import jse.math.vector.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * 通用的势函数接口，用来统一计算原子结构的能量，力，压强的接口。
 * 功能类似 <a href="https://wiki.fysik.dtu.dk/ase/development/calculators.html">
 * ASE Calculator </a>，但这里实现不依靠原子结构，并且不缓存计算结果。
 * 因此每次计算都直接传入原子数据 {@link IAtomData} 实时计算。
 * <p>
 * 支持通过 {@link #ase()} 来将此势函数转换为一个
 * ase 计算器，用来接入使用 ase 计算器的代码。
 *
 * @see IAtomData IAtomData: 通用的原子数据接口
 * @see IPairPotential IPairPotential: 通用的基于截断半径内原子相互作用（pair）实现的势函数
 * @author liqa
 */
public interface IPotential extends AutoCloseable {
    /**
     * 现在总是需要设置原子数据后进行计算，从而和 APC 等接口使用保持一致
     * @param aData 需要计算的原子数据
     * @return 自身方便链式调用
     */
    IPotential setData(IAtomData aData) throws Exception;
    /**
     * 检测此势函数是否已经关闭，默认永远为 {@code false}（即使手动调用了
     * {@link #close()}），即默认不会去进行是否关闭的检测；
     * 重写此函数来在调用计算时检测是否关闭
     * @return 此势函数是否已经关闭
     */
    default boolean isClosed() {return false;}
    @Override default void close() throws Exception {/**/}
    
    /** @return 是否支持计算每原子的能量 */
    default boolean perAtomEnergySupport() {return true;}
    /** @return 是否支持计算每原子的压力 */
    default boolean perAtomStressSupport() {return true;}
    /** @return 是否支持计算 9 列的每原子压力 */
    default boolean centroidPerAtomStressSupport() {return false;}
    
    /**
     * 转换为一个 <a href="https://wiki.fysik.dtu.dk/ase/development/calculators.html">
     * ase 的计算器 </a>，可以方便接入已有的代码直接计算；这里计算的压力统一按照 ase 的排序，也就是
     * {@code [xx, yy, zz, yz, xz, xy]}，确保兼容
     * <p>
     * 为了支持需要关闭的势函数，创建的 ase 计算器也提供了 {@code close()} 方法来进行关闭，
     * 此时也会同步关闭内部引用的此势函数。不会实现 {@code __del__} 方法自动关闭，避免 java 这边的引用意外关闭。
     *
     * @return ase 计算器的 python 对象
     */
    default PyObject ase() throws JepException {
        SP.Python.exec("from jsepy.atom import PotentialCalculator");
        return (PyObject)SP.Python.invoke("PotentialCalculator", this);
    }
    @ApiStatus.Internal
    Map<String, Object> calculate_(Map<String, Object> rResults, PyObject aPyAseAtoms, String[] aProperties, boolean aSystemChanges) throws Exception;
    
    
    /**
     * 通过此势函数计算给定原子数据中每个原子的能量值
     * @return 每个原子能量组成的向量
     * @throws Exception 特殊实现下可选的抛出异常
     */
    Vector calEnergies() throws Exception;
    
    /**
     * 使用此势函数计算给定原子数据的总能量
     * @return 总能量
     * @throws Exception 特殊实现下可选的抛出异常
     */
    double calEnergy() throws Exception;
    
    /**
     * 使用此势函数计算给定原子数据中每个原子的受力
     * @return 每个原子力组成的矩阵，按行排列
     * @throws Exception 特殊实现下可选的抛出异常
     */
    RowMatrix calForces() throws Exception;
    
    /**
     * 使用此势函数计算给定原子数据中所有原子的单独应力，具体可以参见：
     * <a href="https://en.wikipedia.org/wiki/Virial_stress">
     * Virial stress - Wikipedia </a>
     * <p>
     * 每原子位力的定义具有一定任意性，这里的实现优先采用 GPUMD 中使用的更具对称性的定义，
     * 在多体势的情况下可能会和 LAMMPS 存在出入。具体可参考：
     * <a href="https://arxiv.org/abs/1503.06565">
     * Force and heat current formulas for many-body potentials in molecular dynamics simulation with
     * applications to thermal conductivity calculations </a>
     *
     * @return 按照 {@code [xx, yy, zz, xy, xz, yz, yx, zx, zy]} 顺序排列的应力向量，
     *         如果不支持 9 列的输出则只输出 {@code [xx, yy, zz, xy, xz, yz]}
     * @throws Exception 特殊实现下可选的抛出异常
     */
    List<Vector> calStresses() throws Exception;
    
    /**
     * 使用此势函数计算给定原子数据原子结构的应力，具体可以参见：
     * <a href="https://en.wikipedia.org/wiki/Virial_stress">
     * Virial stress - Wikipedia </a>
     * @return 按照 {@code [xx, yy, zz, xy, xz, yz]} 顺序排列的应力值
     * @throws Exception 特殊实现下可选的抛出异常
     */
    List<Double> calStress() throws Exception;
    
    
    /**
     * 使用此势函数计算所有需要的性质，需要注意的是，这里位力需要采用
     * lammps 一致的定义，具体可以参见：
     * <a href="https://en.wikipedia.org/wiki/Virial_stress">
     * Virial stress - Wikipedia </a>
     * <p>
     * 每原子位力的定义具有一定任意性，这里的实现优先采用 GPUMD 中使用的更具对称性的定义，
     * 在多体势的情况下可能会和 LAMMPS 存在出入。具体可参考：
     * <a href="https://arxiv.org/abs/1503.06565">
     * Force and heat current formulas for many-body potentials in molecular dynamics simulation with
     * applications to thermal conductivity calculations </a>
     *
     * @param rEnergies 存储计算输出的每原子能量值，{@code null} 表示不需要能量，长度为 {@code 1} 表示只需要体系的总能量
     * @param rForcesX 存储计算输出的 x 方向力值，{@code null} 表示不需要此值
     * @param rForcesY 存储计算输出的 y 方向力值，{@code null} 表示不需要此值
     * @param rForcesZ 存储计算输出的 z 方向力值，{@code null} 表示不需要此值
     * @param rVirialsXX 存储计算输出的 xx 分量的每原子位力值，{@code null} 表示不需要此值，长度为 {@code 1} 表示只需要此分量下体系的总位力值
     * @param rVirialsYY 存储计算输出的 yy 分量的每原子位力值，{@code null} 表示不需要此值，长度为 {@code 1} 表示只需要此分量下体系的总位力值
     * @param rVirialsZZ 存储计算输出的 zz 分量的每原子位力值，{@code null} 表示不需要此值，长度为 {@code 1} 表示只需要此分量下体系的总位力值
     * @param rVirialsXY 存储计算输出的 xy 分量的每原子位力值，{@code null} 表示不需要此值，长度为 {@code 1} 表示只需要此分量下体系的总位力值
     * @param rVirialsXZ 存储计算输出的 xz 分量的每原子位力值，{@code null} 表示不需要此值，长度为 {@code 1} 表示只需要此分量下体系的总位力值
     * @param rVirialsYZ 存储计算输出的 yz 分量的每原子位力值，{@code null} 表示不需要此值，长度为 {@code 1} 表示只需要此分量下体系的总位力值
     * @param rVirialsYX 存储计算输出的 yx 分量的每原子位力值，默认为 {@code null} 表示不需要此值
     * @param rVirialsZX 存储计算输出的 zx 分量的每原子位力值，默认为 {@code null} 表示不需要此值
     * @param rVirialsZY 存储计算输出的 zy 分量的每原子位力值，默认为 {@code null} 表示不需要此值
     * @throws Exception 特殊实现下可选的抛出异常
     */
    void calEnergyForceVirials(@Nullable IVector rEnergies, @Nullable IVector rForcesX, @Nullable IVector rForcesY, @Nullable IVector rForcesZ, @Nullable IVector rVirialsXX, @Nullable IVector rVirialsYY, @Nullable IVector rVirialsZZ, @Nullable IVector rVirialsXY, @Nullable IVector rVirialsXZ, @Nullable IVector rVirialsYZ, @Nullable IVector rVirialsYX, @Nullable IVector rVirialsZX, @Nullable IVector rVirialsZY) throws Exception;
    /**
     * 使用此势函数计算所有需要的性质，需要注意的是，这里位力需要采用
     * lammps 一致的定义，具体可以参见：
     * <a href="https://en.wikipedia.org/wiki/Virial_stress">
     * Virial stress - Wikipedia </a>
     * <p>
     * 每原子位力的定义具有一定任意性，这里的实现优先采用 GPUMD 中使用的更具对称性的定义，
     * 在多体势的情况下可能会和 LAMMPS 存在出入。具体可参考：
     * <a href="https://arxiv.org/abs/1503.06565">
     * Force and heat current formulas for many-body potentials in molecular dynamics simulation with
     * applications to thermal conductivity calculations </a>
     *
     * @param rEnergies 存储计算输出的每原子能量值，{@code null} 表示不需要能量，长度为 {@code 1} 表示只需要体系的总能量
     * @param rForcesX 存储计算输出的 x 方向力值，{@code null} 表示不需要此值
     * @param rForcesY 存储计算输出的 y 方向力值，{@code null} 表示不需要此值
     * @param rForcesZ 存储计算输出的 z 方向力值，{@code null} 表示不需要此值
     * @param rVirialsXX 存储计算输出的 xx 分量的每原子位力值，{@code null} 表示不需要此值，长度为 {@code 1} 表示只需要此分量下体系的总位力值
     * @param rVirialsYY 存储计算输出的 yy 分量的每原子位力值，{@code null} 表示不需要此值，长度为 {@code 1} 表示只需要此分量下体系的总位力值
     * @param rVirialsZZ 存储计算输出的 zz 分量的每原子位力值，{@code null} 表示不需要此值，长度为 {@code 1} 表示只需要此分量下体系的总位力值
     * @param rVirialsXY 存储计算输出的 xy 分量的每原子位力值，{@code null} 表示不需要此值，长度为 {@code 1} 表示只需要此分量下体系的总位力值
     * @param rVirialsXZ 存储计算输出的 xz 分量的每原子位力值，{@code null} 表示不需要此值，长度为 {@code 1} 表示只需要此分量下体系的总位力值
     * @param rVirialsYZ 存储计算输出的 yz 分量的每原子位力值，{@code null} 表示不需要此值，长度为 {@code 1} 表示只需要此分量下体系的总位力值
     * @throws Exception 特殊实现下可选的抛出异常
     */
    default void calEnergyForceVirials(@Nullable IVector rEnergies, @Nullable IVector rForcesX, @Nullable IVector rForcesY, @Nullable IVector rForcesZ, @Nullable IVector rVirialsXX, @Nullable IVector rVirialsYY, @Nullable IVector rVirialsZZ, @Nullable IVector rVirialsXY, @Nullable IVector rVirialsXZ, @Nullable IVector rVirialsYZ) throws Exception {
        calEnergyForceVirials(rEnergies, rForcesX, rForcesY, rForcesZ, rVirialsXX, rVirialsYY, rVirialsZZ, rVirialsXY, rVirialsXZ, rVirialsYZ, null, null, null);
    }
}
