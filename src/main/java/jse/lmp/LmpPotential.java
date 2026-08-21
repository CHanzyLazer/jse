package jse.lmp;

import jse.atom.IAtomData;
import jse.atom.IPotential;
import jse.code.IO;
import jse.math.matrix.RowMatrix;
import jse.math.vector.IntVector;
import jse.math.vector.Vector;
import jse.math.vector.Vectors;
import jse.parallel.MPI;
import jse.parallel.MPIException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 基于 {@link NativeLmp} 实现的势函数，用来方便使用 lammps
 * 计算各种 lammps 中支持的势。
 * <p>
 * 仅用于直接计算属性，如果希望进行 lammps 模拟则应当直接使用
 * {@link NativeLmp} 中的相关接口。
 * <p>
 * 会自动进行单位转换，确保和通用 {@link IPotential}
 * 的结果一致，即力和应力的单位会统一通过能量和距离来得到。
 * 例如对于 {@code metal} 单位，lammps 压力单位为
 * {@code bar}，而这里会统一进行单位转换，确保单位为 {@code eV/Å^3}
 * <p>
 * 由于 {@link NativeLmp} 的特性，此类线程不安全，并且要求访问线程和创建线程一致
 *
 * @see NativeLmp NativeLmp: 原生调用 lammps 接口
 * @see IPotential IPotential: 通用的势函数接口
 * @author liqa
 */
public class LmpPotential extends AbstractLmpPotential {
    public final static class InitHelper {
        private static volatile boolean INITIALIZED = false;
        /** @return {@link LmpPotential} 相关的 JNI 库是否已经初始化完成，主要用于辅助初始化 {@link NativeLmp} */
        public static boolean initialized() {return INITIALIZED;}
        /** 初始化 {@link LmpPotential} 相关的 JNI 库，主要用于辅助初始化 {@link NativeLmp} */
        @SuppressWarnings({"ResultOfMethodCallIgnored", "UnnecessaryCallToStringValueOf"})
        public static void init() {
            if (!INITIALIZED) String.valueOf(_INIT_FLAG);
        }
    }
    private final static boolean _INIT_FLAG;
    static {
        InitHelper.INITIALIZED = true;
        // 确保 NativeLmp 已经确实初始化
        NativeLmp.InitHelper.init();
        _INIT_FLAG = false;
    }
    private final static String[] LMP_ARGS = {"-log", "none", "-screen", "none"};
    
    private final NativeLmp mLmp;
    /**
     * 根据输入的 aPairStyle 和 aPairCoeff 创建一个原生调用 lammps 计算的势函数
     * @param aPairStyle 希望使用的 lammps 中的 pair 样式，对应 lammps 命令 {@code pair_style}
     * @param aPairCoeff lammps pair 需要设置的参数，对应 lammps 命令 {@code pair_coeff}
     * @param aComm 希望使用的 {@link MPI.Comm}，默认为 {@code null}，当有 MPI 支持时会采用 {@link MPI.Comm#WORLD}
     */
    public LmpPotential(String aPairStyle, String[] aPairCoeff, @Nullable MPI.Comm aComm) throws LmpException {
        super(aPairStyle, aPairCoeff);
        mLmp = new NativeLmp(LMP_ARGS, aComm);
    }
    /**
     * 根据输入的 aPairStyle 和 aPairCoeff 创建一个原生调用 lammps 计算的势函数
     * @param aPairStyle 希望使用的 lammps 中的 pair 样式，对应 lammps 命令 {@code pair_style}
     * @param aPairCoeff lammps pair 需要设置的参数，对应 lammps 命令 {@code pair_coeff}
     * @param aComm 希望使用的 {@link MPI.Comm}，默认为 {@code null}，当有 MPI 支持时会采用 {@link MPI.Comm#WORLD}
     */
    public LmpPotential(String aPairStyle, String aPairCoeff, @Nullable MPI.Comm aComm) throws LmpException {
        this(aPairStyle, new String[]{aPairCoeff}, aComm);
    }
    /**
     * 根据输入的 aPairStyle 和 aPairCoeff 创建一个原生调用 lammps 计算的势函数
     * @param aPairStyle 希望使用的 lammps 中的 pair 样式，对应 lammps 命令 {@code pair_style}
     * @param aPairCoeff lammps pair 需要设置的参数，对应 lammps 命令 {@code pair_coeff}
     * @param aComm 希望使用的 {@link MPI.Comm}，默认为 {@code null}，当有 MPI 支持时会采用 {@link MPI.Comm#WORLD}
     */
    public LmpPotential(String aPairStyle, Collection<? extends CharSequence> aPairCoeff, @Nullable MPI.Comm aComm) throws LmpException {
        this(aPairStyle, IO.Text.toArray(aPairCoeff), aComm);
    }
    /**
     * 根据输入的 aPairStyle 和 aPairCoeff 创建一个原生调用 lammps 计算的势函数
     * @param aPairStyle 希望使用的 lammps 中的 pair 样式，对应 lammps 命令 {@code pair_style}
     * @param aPairCoeff lammps pair 需要设置的参数，对应 lammps 命令 {@code pair_coeff}
     */
    public LmpPotential(String aPairStyle, String... aPairCoeff) throws LmpException {
        this(aPairStyle, aPairCoeff, null);
    }
    private @Nullable Lmpdat mData = null;
    @Override public LmpPotential setData(IAtomData aData) throws Exception {
        super.setData(aData);
        mData = Lmpdat.of(aData, Vectors.ones(aData.ntypes()));
        return this;
    }
    
    private boolean mDead = false;
    /** @return 此 lammps 势函数是否已经关闭 */
    @Override public boolean isClosed() {return mDead;}
    /**
     * 关闭此 lammps 势函数，会同时关闭内部使用的
     * {@link NativeLmp} 对象
     */
    @Override public void close() throws Exception {
        if (mDead) return;
        mDead = true;
        mLmp.close();
    }
    
    /**
     * {@inheritDoc}
     * @param aRequireTotalEnergy {@inheritDoc}
     * @param aRequirePerAtomEnergy {@inheritDoc}
     * @param aRequireForce {@inheritDoc}
     * @param aRequireTotalStress {@inheritDoc}
     * @param aRequirePerAtomStress {@inheritDoc}
     * @throws LmpException 触发 LAMMPS 异常
     * @throws MPIException 触发 MPI 异常
     */
    @Override public void calculate(boolean aRequireTotalEnergy, boolean aRequirePerAtomEnergy, boolean aRequireForce, boolean aRequireTotalStress, boolean aRequirePerAtomStress) throws LmpException, MPIException {
        if (mDead) throw new IllegalStateException("This Potential is dead");
        if (!dataValid()) throw new IllegalStateException("data invalid");
        assert mData!=null;
        // 除了保持简单，pair style 等参数也可能发生改变，因此这里总是触发清理和重新计算
        mLmp.clear();
        if (mBeforeCommands != null) mLmp.commands(mBeforeCommands);
        mLmp.command("units  "+mUnits);
        mLmp.command("boundary  p p p");
        mLmp.loadData(mData, true); // 统一不需要 id 信息，简化排序问题
        mLmp.command("pair_style   "+mPairStyle);
        for (String tPairCoeff : mPairCoeff) {
            mLmp.command("pair_coeff   "+tPairCoeff);
        }
        if (mLastCommands != null) mLmp.commands(mLastCommands);
        // 增加这个 thermo 确保势能和应力可以获取到
        List<String> rThermoStyle = new ArrayList<>(8);
        if (aRequireTotalEnergy) {
            rThermoStyle.add("pe");
        }
        if (aRequireTotalStress) {
            mLmp.command("compute p_tot all pressure NULL virial");
        }
        mLmp.command("thermo_style  custom step "+String.join(" ", rThermoStyle));
        // 按需增加对应的 compute
        if (aRequirePerAtomEnergy) {
            mLmp.command("compute eng_atom all pe/atom");
        }
        if (aRequirePerAtomStress) {
            mLmp.command("compute stress_atom all stress/atom NULL virial");
        }
        // 通过 run 0 来触发计算
        mLmp.command("run  0");
        // lammps 会乱序，需要重新排序，这里可以确定可以按照 id 来排序
        IntVector tLmpIdx2Idx = null;
        if (aRequireForce || aRequirePerAtomEnergy || aRequirePerAtomStress) {
            tLmpIdx2Idx = mLmp.atomIntDataOf("id").asVecRow();
            tLmpIdx2Idx.minus2this(1);
        }
        // 直接获取结果
        if (aRequireTotalEnergy) {
            mEnergy = mLmp.thermoOf("pe");
        }
        if (aRequireTotalStress) {
            Vector tPress = mLmp.computeOf("p_tot", NativeLmp.LMP_STYLE_GLOBAL, NativeLmp.LMP_TYPE_VECTOR).asVecRow();
            mStressXX = -validStressUnit(tPress.get(0));
            mStressYY = -validStressUnit(tPress.get(1));
            mStressZZ = -validStressUnit(tPress.get(2));
            mStressXY = -validStressUnit(tPress.get(3));
            mStressXZ = -validStressUnit(tPress.get(4));
            mStressYZ = -validStressUnit(tPress.get(5));
        }
        if (aRequireForce) {
            RowMatrix tForces = mLmp.atomDataOf("f");
            mForcesX.putAt(tLmpIdx2Idx, tForces.col(0));
            mForcesY.putAt(tLmpIdx2Idx, tForces.col(1));
            mForcesZ.putAt(tLmpIdx2Idx, tForces.col(2));
        }
        if (aRequirePerAtomEnergy) {
            Vector tEnergies = mLmp.computeOf("eng_atom", NativeLmp.LMP_STYLE_ATOM, NativeLmp.LMP_TYPE_VECTOR).asVecRow();
            mEnergies.putAt(tLmpIdx2Idx, tEnergies);
        }
        if (aRequirePerAtomStress) {
            RowMatrix tStresses = mLmp.computeOf("stress_atom", NativeLmp.LMP_STYLE_ATOM, NativeLmp.LMP_TYPE_ARRAY);
            tStresses.operation().map2this(this::validStressUnit);
            mStressesXX.putAt(tLmpIdx2Idx, tStresses.col(0));
            mStressesYY.putAt(tLmpIdx2Idx, tStresses.col(1));
            mStressesZZ.putAt(tLmpIdx2Idx, tStresses.col(2));
            mStressesXY.putAt(tLmpIdx2Idx, tStresses.col(3));
            mStressesXZ.putAt(tLmpIdx2Idx, tStresses.col(4));
            mStressesYZ.putAt(tLmpIdx2Idx, tStresses.col(5));
        }
        mLmp.clear();
        // 最后调整 box 变化导致的力和应力方向变化
        validBox(aRequireForce, aRequireTotalStress, aRequirePerAtomStress);
        // 设置对应值合法
        if (aRequireTotalEnergy) mTotalEnergyValid = true;
        if (aRequirePerAtomEnergy) mPerAtomEnergyValid = true;
        if (aRequireForce) mForceValid = true;
        if (aRequireTotalStress) mTotalStressValid = true;
        if (aRequirePerAtomStress) mPerAtomStressValid = true;
    }
}
