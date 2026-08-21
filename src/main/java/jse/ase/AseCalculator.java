package jse.ase;

import jep.JepException;
import jep.NDArray;
import jep.python.PyCallable;
import jep.python.PyObject;
import jse.atom.AbstractPotential;
import jse.atom.IAtomData;
import jse.atom.IPotential;
import jse.math.matrix.RowMatrix;
import jse.math.vector.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * <a href="https://wiki.fysik.dtu.dk/ase/_modules/ase/calculators/calculator.html#Calculator">
 * {@code ase.calculators.calculator.Calculator} </a> 的 jse
 * 实现，继承了 {@link IPotential} 用于保证使用方法和 jse 中的势函数一致。
 * <p>
 * 此实现仅仅只是 ase 计算器对象的包装类，用于方便使用 jse 中计算能量和力的模式来使用 ase 计算器。
 * <p>
 * 要求系统有 python 环境并且安装了
 * <a href="https://wiki.fysik.dtu.dk/ase/">
 * Atomic Simulation Environment (ASE) </a>
 * <p>
 * 通过：
 * <pre> {@code
 * def jseCalc = new AseCalculator(pyCalc)
 * } </pre>
 * 来将 {@link PyObject} 的 ase calculator 转换成 jse 的计算器（势函数），通过：
 * <pre> {@code
 * def pyCalc = jseCalc.ase()
 * } </pre>
 * 通过 {@link IPotential} 中的通用接口来将势函数转换成 ase 计算器。
 *
 * @see IPotential IPotential: 势函数通用接口
 * @see PyObject
 * @author liqa
 */
public class AseCalculator extends AbstractPotential {
    private final PyObject mCalc;
    private @Nullable PyObject mAtoms = null;
    private final boolean mEnergySupport, mForceSupport, mStressSupport;
    private final boolean mPerAtomEnergySupport, mPerAtomStressSupport;
    
    /**
     * 通过一个 ase 计算器的 python 对象创建一个兼容 jse 的
     * {@link IPotential} 的计算器包装对象
     * <p>
     * 当调用 {@link #close()} 关闭时会同时尝试关闭内部的
     * ase 计算器引用，并移除 java 对此 python 对象的引用；
     * 这在大多数时候可以简化使用，例如在包装后不再需要保留原本的
     * calc 来后续手动关闭
     * @param aCalc 需要包装的 ase 计算器
     */
    public AseCalculator(PyObject aCalc) {
        mCalc = aCalc;
        List<?> tImplementedProperties = mCalc.getAttr("implemented_properties", List.class);
        boolean tEnergySupport = false;
        boolean tForceSupport = false;
        boolean tStressSupport = false;
        boolean tPerAtomEnergySupport = false;
        boolean tPerAtomStressSupport = false;
        for (Object tProperty : tImplementedProperties) {
            if ("energy".equals(tProperty)) tEnergySupport = true;
            if ("forces".equals(tProperty)) tForceSupport = true;
            if ("stress".equals(tProperty)) tStressSupport = true;
            if ("energies".equals(tProperty)) tPerAtomEnergySupport = true;
            if ("stresses".equals(tProperty)) tPerAtomStressSupport = true;
        }
        mEnergySupport = tEnergySupport;
        mForceSupport = tForceSupport;
        mStressSupport = tStressSupport;
        mPerAtomEnergySupport = tPerAtomEnergySupport;
        mPerAtomStressSupport = tPerAtomStressSupport;
    }
    @Override public AseCalculator setData(IAtomData aData) throws Exception {
        super.setData(aData);
        mAtoms = AseAtoms.of(aData).toPyObject(true);
        mAtoms.setAttr("calc", mCalc);
        return this;
    }
    
    private boolean mDead = false;
    /** @return 此 ase 计算器是否已经关闭 */
    @Override public boolean isClosed() {return mDead;}
    /**
     * 关闭此 ase 计算器，会时尝试调用内部引用的 ase
     * 计算器的 {@code release} 或 {@code close}
     * 进行关闭，并同时会移除 java 对其的引用。
     */
    @Override public void close() throws Exception {
        if (mDead) return;
        mDead = true;
        // 清理 java 对于 atoms 的引用
        if (mAtoms != null) mAtoms.close();
        // 尝试调用 release 和 close 来关闭，由于 python 特性只能直接用 try 的写法
        try (PyCallable tRelease = mCalc.getAttr("release", PyCallable.class)) {
            tRelease.call();
        } catch (JepException e) {
            try (PyCallable tClose = mCalc.getAttr("close", PyCallable.class)) {
                tClose.call();
            } catch (JepException ignored) {}
        }
        mCalc.close();
    }
    
    /**
     * {@link #setData} 后获取内部创建的 ase Atoms 对象，用于方便获取计算的其他属性，
     * 或者借助 ase 来保存所有计算结果。
     * @return 内部创建的 ase Atoms 对象，如果没有则返回 {@code null}
     */
    public @Nullable PyObject atoms() {return mAtoms;}
    
    /** @return {@inheritDoc} */
    @Override public boolean perAtomEnergySupport() {return mPerAtomEnergySupport;}
    /** @return {@inheritDoc} */
    @Override public boolean perAtomStressSupport() {return mPerAtomStressSupport;}
    /**
     * 转换为 ase 计算器，这里直接返回创建时使用的 ase 计算器对象
     * @return {@inheritDoc}
     */
    @Override public PyObject ase() {return mCalc;}
    
    /**
     * {@inheritDoc}
     * <p>
     * ase 的默认计算会自动根据支持程度调整能量和力的 require
     * @throws JepException 触发 jep 异常
     */
    @Override public void calculate() throws JepException {
        calculate(mEnergySupport, mPerAtomEnergySupport, mForceSupport, mStressSupport, mPerAtomStressSupport);
    }
    /**
     * {@inheritDoc}
     * @param aRequireTotalEnergy {@inheritDoc}
     * @param aRequirePerAtomEnergy {@inheritDoc}
     * @param aRequireForce {@inheritDoc}
     * @param aRequireTotalStress {@inheritDoc}
     * @param aRequirePerAtomStress {@inheritDoc}
     * @throws JepException 触发 jep 异常
     */
    @Override public void calculate(boolean aRequireTotalEnergy, boolean aRequirePerAtomEnergy, boolean aRequireForce, boolean aRequireTotalStress, boolean aRequirePerAtomStress) throws JepException {
        if (mDead) throw new IllegalStateException("This Potential is dead");
        if (!dataValid()) throw new IllegalStateException("data invalid");
        assert mAtoms!=null;
        // 按照难度逆序计算，可以利用 ase 计算器的缓存特性避免重复计算
        if (aRequirePerAtomStress) {
            if (!mPerAtomStressSupport) throw new UnsupportedOperationException("per-atom stress not supported");
            NDArray<?> tPyStresses;
            try (PyCallable tGetStresses = mAtoms.getAttr("get_stresses", PyCallable.class)) {
                tPyStresses = tGetStresses.callAs(NDArray.class);
            }
            RowMatrix tStresses = new RowMatrix(tPyStresses.getDimensions()[0], tPyStresses.getDimensions()[1], (double[])tPyStresses.getData());
            for (int i = 0; i < mNumAtoms; ++i) {
                mStressesXX.set(i, tStresses.get(i, 0));
                mStressesYY.set(i, tStresses.get(i, 1));
                mStressesZZ.set(i, tStresses.get(i, 2));
                mStressesYZ.set(i, tStresses.get(i, 3)); // 注意 ase 的 stress 顺序问题
                mStressesXZ.set(i, tStresses.get(i, 4));
                mStressesXY.set(i, tStresses.get(i, 5));
            }
        }
        if (aRequireTotalStress) {
            if (!mStressSupport) throw new UnsupportedOperationException("stress not supported");
            NDArray<?> tPyStress;
            try (PyCallable tGetStress = mAtoms.getAttr("get_stress", PyCallable.class)) {
                tPyStress = tGetStress.callAs(NDArray.class);
            }
            Vector tStress = new Vector(tPyStress.getDimensions()[0], (double[])tPyStress.getData());
            mStressXX = tStress.get(0);
            mStressYY = tStress.get(1);
            mStressZZ = tStress.get(2);
            mStressYZ = tStress.get(3); // 注意 ase 的 stress 顺序问题
            mStressXZ = tStress.get(4);
            mStressXY = tStress.get(5);
        }
        if (aRequireForce) {
            if (!mForceSupport) throw new UnsupportedOperationException("forces not supported");
            NDArray<?> tPyForces;
            try (PyCallable tGetForces = mAtoms.getAttr("get_forces", PyCallable.class)) {
                tPyForces = tGetForces.callAs(NDArray.class);
            }
            RowMatrix tForces = new RowMatrix(tPyForces.getDimensions()[0], tPyForces.getDimensions()[1], (double[])tPyForces.getData());
            for (int i = 0; i < mNumAtoms; ++i) {
                mForcesX.set(i, tForces.get(i, 0));
                mForcesY.set(i, tForces.get(i, 1));
                mForcesZ.set(i, tForces.get(i, 2));
            }
        }
        if (aRequirePerAtomEnergy) {
            if (!mPerAtomEnergySupport) throw new UnsupportedOperationException("per-atom energy not supported");
            NDArray<?> tPyEnergies;
            try (PyCallable tGetEnergies = mAtoms.getAttr("get_potential_energies", PyCallable.class)) {
                tPyEnergies = tGetEnergies.callAs(NDArray.class);
            }
            Vector tEnergies = new Vector(tPyEnergies.getDimensions()[0], (double[])tPyEnergies.getData());
            mEnergies.fill(tEnergies);
        }
        if (aRequireTotalEnergy) {
            if (!mEnergySupport) throw new UnsupportedOperationException("energy not supported");
            try (PyCallable tGetEnergy = mAtoms.getAttr("get_potential_energy", PyCallable.class)) {
                mEnergy = tGetEnergy.callAs(Number.class).doubleValue();
            }
        }
        // 设置对应值合法
        if (aRequireTotalEnergy) mTotalEnergyValid = true;
        if (aRequirePerAtomEnergy) mPerAtomEnergyValid = true;
        if (aRequireForce) mForceValid = true;
        if (aRequireTotalStress) mTotalStressValid = true;
        if (aRequirePerAtomStress) mPerAtomStressValid = true;
    }
}
