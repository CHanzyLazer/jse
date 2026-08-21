package jse.atom;

import com.google.common.collect.Lists;
import jep.JepException;
import jep.python.PyObject;
import jse.code.SP;
import jse.math.vector.Vector;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

/**
 * 通用的势函数接口，用来统一计算原子结构的能量，力，压强的接口。
 * 功能类似 <a href="https://wiki.fysik.dtu.dk/ase/development/calculators.html">
 * ASE Calculator </a>
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
    /** @return 是否经过 {@link #setData} 使自身有合适的原子数据 */
    boolean dataValid();
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
    Map<String, Object> calculateAse_(Map<String, Object> rResults, PyObject aPyAseAtoms, String[] aProperties, boolean aSystemChanges) throws Exception;
    
    /**
     * 对内部的原子数据进行通用的任意计算并存储结果，计算完成的结果通过
     * {@link #energy()} 等接口获取。
     * <p>
     * 一般情况直接使用 {@link #calculate()} 来直接计算所有结果。
     *
     * @param aRequireTotalEnergy 需要计算总能量，对应 {@link #energy()} 合法
     * @param aRequirePerAtomEnergy 需要计算每原子能量，对应 {@link #energies()} 合法
     * @param aRequireForce 需要计算力，对应 {@link #forces()} 合法
     * @param aRequireTotalStress 需要计算总应力，对应 {@link #stress()} 合法
     * @param aRequirePerAtomStress 需要计算每原子应力，对应 {@link #stresses()} 合法
     * @throws Exception 特殊实现下可选的抛出异常
     */
    void calculate(boolean aRequireTotalEnergy, boolean aRequirePerAtomEnergy, boolean aRequireForce, boolean aRequireTotalStress, boolean aRequirePerAtomStress) throws Exception;
    /**
     * 对内部的原子数据计算所有可以计算的量并存储结果，计算完成的结果通过
     * {@link #energy()} 等接口获取。
     * <p>
     * 会根据 {@link #perAtomEnergySupport()} 和 {@link #perAtomStressSupport()}
     * 的支持情况自动开启相关的计算。
     *
     * @throws Exception 特殊实现下可选的抛出异常
     */
    default void calculate() throws Exception {
        calculate(true, perAtomEnergySupport(), true, true, perAtomStressSupport());
    }
    /** @return 是否经过 {@link #calculate} 计算并得到总能量，对应 {@link #energy()} 合法 */
    boolean totalEnergyValid();
    /** @return 是否经过 {@link #calculate} 计算并得到每原子能量，对应 {@link #energies()} 合法 */
    boolean perAtomEnergyValid();
    /** @return 是否经过 {@link #calculate} 计算并得到力，对应 {@link #forces()} 合法 */
    boolean forceValid();
    /** @return 是否经过 {@link #calculate} 计算并得到总应力，对应 {@link #stress()} 合法 */
    boolean totalStressValid();
    /** @return 是否经过 {@link #calculate} 计算并得到每原子应力，对应 {@link #stresses()} 合法 */
    boolean perAtomStressValid();
    
    /**
     * 获取此势计算得到的总能量
     * @return 总能量
     */
    double energy();
    /**
     * 获取此势计算得到的每原子能量
     * @return 每个原子能量组成的向量
     */
    Vector energies();
    
    /** @return 每原子受力 x 分量组成的向量 */
    Vector forcesX();
    /** @return 每原子受力 y 分量组成的向量 */
    Vector forcesY();
    /** @return 每原子受力 z 分量组成的向量 */
    Vector forcesZ();
    /**
     * 获取此势计算得到的每个原子的受力
     * @return 按照 {@code [fx, fy, fz]} 排列的每原子受力组成的向量
     */
    default List<Vector> forces() {
        return Lists.newArrayList(forcesX(), forcesY(), forcesZ());
    }
    
    /** @return 应力的 xx 分量值 */
    double stressXX();
    /** @return 应力的 yy 分量值 */
    double stressYY();
    /** @return 应力的 zz 分量值 */
    double stressZZ();
    /** @return 应力的 xy 分量值 */
    double stressXY();
    /** @return 应力的 xz 分量值 */
    double stressXZ();
    /** @return 应力的 yz 分量值 */
    double stressYZ();
    /**
     * 获取此势计算得到的应力，具体可以参见：
     * <a href="https://en.wikipedia.org/wiki/Virial_stress">
     * Virial stress - Wikipedia </a>
     * @return 按照 {@code [sxx, syy, szz, sxy, sxz, syz]} 排列的应力值
     */
    default List<Double> stress() {
        return Lists.newArrayList(stressXX(), stressYY(), stressZZ(), stressXY(), stressXZ(), stressYZ());
    }
    
    /** @return 每原子应力 xx 分量组成的向量 */
    Vector stressesXX();
    /** @return 每原子应力 yy 分量组成的向量 */
    Vector stressesYY();
    /** @return 每原子应力 zz 分量组成的向量 */
    Vector stressesZZ();
    /** @return 每原子应力 xy 分量组成的向量 */
    Vector stressesXY();
    /** @return 每原子应力 xz 分量组成的向量 */
    Vector stressesXZ();
    /** @return 每原子应力 yz 分量组成的向量 */
    Vector stressesYZ();
    /** @return 每原子应力 yx 分量组成的向量 */
    Vector stressesYX();
    /** @return 每原子应力 zx 分量组成的向量 */
    Vector stressesZX();
    /** @return 每原子应力 zy 分量组成的向量 */
    Vector stressesZY();
    /**
     * 获取此势计算得到的每原子应力，具体可以参见：
     * <a href="https://en.wikipedia.org/wiki/Virial_stress">
     * Virial stress - Wikipedia </a>
     * <p>
     * 每原子位力的定义具有一定任意性，这里的实现优先采用 GPUMD 中使用的更具对称性的定义，
     * 在多体势的情况下可能会和 LAMMPS 存在出入。具体可参考：
     * <a href="https://arxiv.org/abs/1503.06565">
     * Force and heat current formulas for many-body potentials in molecular dynamics simulation with
     * applications to thermal conductivity calculations </a>
     *
     * @return 按照 {@code [sxx, syy, szz, sxy, sxz, syz, syx, szx, szy]} 排列的每原子应力组成的向量，
     *         如果不支持 9 列的输出则只输出 {@code [sxx, syy, szz, sxy, sxz, syz]}
     */
    default List<Vector> stresses() {
        if (!perAtomStressSupport()) return null;
        if (centroidPerAtomStressSupport()) {
            return Lists.newArrayList(stressesXX(), stressesYY(), stressesZZ(), stressesXY(), stressesXZ(), stressesYZ(), stressesYX(), stressesZX(), stressesZY());
        } else {
            return Lists.newArrayList(stressesXX(), stressesYY(), stressesZZ(), stressesXY(), stressesXZ(), stressesYZ());
        }
    }
}
