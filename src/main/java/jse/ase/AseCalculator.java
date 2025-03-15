package jse.ase;

import jep.JepException;
import jep.python.PyObject;
import jse.atom.IAtomData;
import jse.atom.IPotential;
import jse.code.UT;
import jse.code.collection.ISlice;
import jse.math.vector.IVector;
import org.jetbrains.annotations.NotNull;
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
 * def jseCalc = AseCalculator.of(pyCalc)
 * } </pre>
 * 来将 {@link PyObject} 的 ase calculator 转换成 jse 的计算器（势函数），通过：
 * <pre> {@code
 * def pyCalc = jseCalc.asAseCalculator()
 * } </pre>
 * 通过 {@link IPotential} 中的通用接口来将势函数转换成 ase 计算器。
 *
 * @see IPotential IPotential: 势函数通用接口
 * @see PyObject
 * @author liqa
 */
public class AseCalculator implements IPotential {
    private final PyObject mCalc;
    private PyObject mLastAtoms = null;
    private final String[] mImplementedProperties;
    private final boolean mPerAtomEnergySupport, mPerAtomStressSupport;
    AseCalculator(PyObject aCalc) {
        mCalc = aCalc;
        List<?> tImplementedProperties = mCalc.getAttr("implemented_properties", List.class);
        mImplementedProperties = new String[tImplementedProperties.size()];
        for (int i = 0; i < mImplementedProperties.length; ++i) {
            mImplementedProperties[i] = UT.Code.toString(tImplementedProperties.get(i));
        }
        boolean tPerAtomEnergySupport = false;
        boolean tPerAtomStressSupport = false;
        for (String tProperty : mImplementedProperties) {
            if (tProperty.equals("energies")) tPerAtomEnergySupport = true;
            if (tProperty.equals("stresses")) tPerAtomStressSupport = true;
        }
        mPerAtomEnergySupport = tPerAtomEnergySupport;
        mPerAtomStressSupport = tPerAtomStressSupport;
    }
    
    /**
     * 通过一个 ase 计算器的 python 对象创建一个兼容 jse 的
     * {@link IPotential} 的计算器包装对象
     * @param aCalc 需要包装的 ase 计算器
     * @return 兼容 {@link IPotential} 的 ase 计算器包装对象 {@link AseCalculator}
     */
    public static AseCalculator of(PyObject aCalc) {return new AseCalculator(aCalc);}
    
    /**
     * 获取上次进行计算内部创建的 ase Atoms 对象，用于方便获取计算的其他属性，
     * 或者借助 ase 来保存所有计算结果。
     * @return 上次进行计算内部创建的 ase Atoms 对象，如果没有进行任何计算则返回 {@code null}
     */
    public @Nullable PyObject lastAtoms() {return mLastAtoms;}
    
    /** @return {@inheritDoc} */
    @Override public boolean perAtomEnergySupport() {return mPerAtomEnergySupport;}
    /** @return {@inheritDoc} */
    @Override public boolean perAtomStressSupport() {return mPerAtomStressSupport;}
    
    /**
     * 转换为 ase 计算器，这里直接返回创建时使用的 ase 计算器对象
     * @return {@inheritDoc}
     */
    @Override public PyObject asAseCalculator() throws JepException {return mCalc;}
    /** 对于已经是 ase 计算器不能自定义 python 解释器，因此应当使用 {@link #asAseCalculator()} */
    @Override public PyObject asAseCalculator(@NotNull jep.Interpreter aInterpreter) throws JepException {
        throw new UnsupportedOperationException("custom interpreter for AseCalculator");
    }
    
    /** 常规的 ase 计算器不支持计算部分原子能量，因此会直接抛出 {@link UnsupportedOperationException} */
    @Override public double calEnergyAt(IAtomData aAPC, ISlice aIndices) {
        throw new UnsupportedOperationException();
    }
    
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
     */
    @Override public void calEnergyForceVirials(IAtomData aAtomData, @Nullable IVector rEnergies, @Nullable IVector rForcesX, @Nullable IVector rForcesY, @Nullable IVector rForcesZ, @Nullable IVector rVirialsXX, @Nullable IVector rVirialsYY, @Nullable IVector rVirialsZZ, @Nullable IVector rVirialsXY, @Nullable IVector rVirialsXZ, @Nullable IVector rVirialsYZ) throws JepException {
        // 这里为了兼容性，还是采用通过 atoms 来调用计算参数的实现方法
        AseAtoms tAtoms = AseAtoms.of(aAtomData);
        mLastAtoms = tAtoms.toPyObject();
        
    }
}
