package jse.lmp;

import jse.atom.IAtomData;
import jse.atom.IPotential;
import jse.code.IO;
import jse.code.OS;
import jse.code.UT;
import jse.math.table.ITable;
import jse.math.vector.*;
import jse.system.ISystemExecutor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 通过执行系统命令执行 lammps 实现的 lammps 势函数，用来方便使用
 * lammps 计算各种 lammps 中支持的势。
 * <p>
 * 由于需要通过系统命令执行，因此相比 {@link LmpPotential}
 * 效率更低，但会有更高的兼容性。
 * <p>
 * 仅用于直接计算属性，如果希望进行 lammps 模拟则应当直接使用
 * {@link ISystemExecutor} 来直接运行 lammps。
 * <p>
 * 会自动进行单位转换，确保和通用 {@link IPotential}
 * 的结果一致，即力和应力的单位会统一通过能量和距离来得到。
 * 例如对于 {@code metal} 单位，lammps 压力单位为
 * {@code bar}，而这里会统一进行单位转换，确保单位为 {@code eV/Å^3}
 * <p>
 * 此类线程安全，包括多个线程同时访问同一个实例
 *
 * @see LmpPotential LmpPotential: 原生调用 lammps 实现的 lammps 势函数
 * @see IPotential IPotential: 通用的势函数接口
 * @author liqa
 */
public class SystemLmpPotential extends AbstractLmpPotential {
    private final SystemLmpChecker mChecker;
    private final ISystemExecutor mExec;
    private final boolean mCloseExec;
    /**
     * 根据输入的 aPairStyle 和 aPairCoeff 创建一个命令运行 lammps 计算的势函数
     * @param aPairStyle 希望使用的 lammps 中的 pair 样式，对应 lammps 命令 {@code pair_style}
     * @param aPairCoeff lammps pair 需要设置的参数，对应 lammps 命令 {@code pair_coeff}
     * @param aExec 希望使用的系统命令执行器 {@link ISystemExecutor}，默认为 {@link OS#EXEC}
     * @param aCloseExec 是否会在关闭此势函数时，自动关闭内部的系统执行器，默认在手动传入 aExec 时为 {@code true}，不传入时为 {@code false}
     */
    public SystemLmpPotential(String aPairStyle, String[] aPairCoeff, ISystemExecutor aExec, boolean aCloseExec) {
        super(aPairStyle, aPairCoeff);
        mExec = aExec;
        mCloseExec = aCloseExec;
        // 使用相对路径提高 exec 的兼容性
        mChecker = new SystemLmpChecker(this);
    }
    /**
     * 根据输入的 aPairStyle 和 aPairCoeff 创建一个命令运行 lammps 计算的势函数
     * @param aPairStyle 希望使用的 lammps 中的 pair 样式，对应 lammps 命令 {@code pair_style}
     * @param aPairCoeff lammps pair 需要设置的参数，对应 lammps 命令 {@code pair_coeff}
     * @param aExec 希望使用的系统命令执行器 {@link ISystemExecutor}，默认为 {@link OS#EXEC}，默认在关闭时会同时自动关闭
     * @param aCloseExec 是否会在关闭此势函数时，自动关闭内部的系统执行器，默认在手动传入 aExec 时为 {@code true}，不传入时为 {@code false}
     */
    public SystemLmpPotential(String aPairStyle, String aPairCoeff, ISystemExecutor aExec, boolean aCloseExec) {
        this(aPairStyle, new String[]{aPairCoeff}, aExec, aCloseExec);
    }
    /**
     * 根据输入的 aPairStyle 和 aPairCoeff 创建一个命令运行 lammps 计算的势函数
     * @param aPairStyle 希望使用的 lammps 中的 pair 样式，对应 lammps 命令 {@code pair_style}
     * @param aPairCoeff lammps pair 需要设置的参数，对应 lammps 命令 {@code pair_coeff}
     * @param aExec 希望使用的系统命令执行器 {@link ISystemExecutor}，默认为 {@link OS#EXEC}，默认在关闭时会同时自动关闭
     * @param aCloseExec 是否会在关闭此势函数时，自动关闭内部的系统执行器，默认在手动传入 aExec 时为 {@code true}，不传入时为 {@code false}
     */
    public SystemLmpPotential(String aPairStyle, Collection<? extends CharSequence> aPairCoeff, ISystemExecutor aExec, boolean aCloseExec) {
        this(aPairStyle, IO.Text.toArray(aPairCoeff), aExec, aCloseExec);
    }
    /**
     * 根据输入的 aPairStyle 和 aPairCoeff 创建一个命令运行 lammps 计算的势函数
     * @param aPairStyle 希望使用的 lammps 中的 pair 样式，对应 lammps 命令 {@code pair_style}
     * @param aPairCoeff lammps pair 需要设置的参数，对应 lammps 命令 {@code pair_coeff}
     * @param aExec 希望使用的系统命令执行器 {@link ISystemExecutor}，默认为 {@link OS#EXEC}，默认在关闭时会同时自动关闭
     */
    public SystemLmpPotential(String aPairStyle, String[] aPairCoeff, ISystemExecutor aExec) {
        this(aPairStyle, aPairCoeff, aExec, true);
    }
    /**
     * 根据输入的 aPairStyle 和 aPairCoeff 创建一个命令运行 lammps 计算的势函数
     * @param aPairStyle 希望使用的 lammps 中的 pair 样式，对应 lammps 命令 {@code pair_style}
     * @param aPairCoeff lammps pair 需要设置的参数，对应 lammps 命令 {@code pair_coeff}
     * @param aExec 希望使用的系统命令执行器 {@link ISystemExecutor}，默认为 {@link OS#EXEC}，默认在关闭时会同时自动关闭
     */
    public SystemLmpPotential(String aPairStyle, String aPairCoeff, ISystemExecutor aExec) {
        this(aPairStyle, new String[]{aPairCoeff}, aExec);
    }
    /**
     * 根据输入的 aPairStyle 和 aPairCoeff 创建一个命令运行 lammps 计算的势函数
     * @param aPairStyle 希望使用的 lammps 中的 pair 样式，对应 lammps 命令 {@code pair_style}
     * @param aPairCoeff lammps pair 需要设置的参数，对应 lammps 命令 {@code pair_coeff}
     * @param aExec 希望使用的系统命令执行器 {@link ISystemExecutor}，默认为 {@link OS#EXEC}，默认在关闭时会同时自动关闭
     */
    public SystemLmpPotential(String aPairStyle, Collection<? extends CharSequence> aPairCoeff, ISystemExecutor aExec) {
        this(aPairStyle, IO.Text.toArray(aPairCoeff), aExec);
    }
    /**
     * 根据输入的 aPairStyle 和 aPairCoeff 创建一个命令运行 lammps 计算的势函数
     * @param aPairStyle 希望使用的 lammps 中的 pair 样式，对应 lammps 命令 {@code pair_style}
     * @param aPairCoeff lammps pair 需要设置的参数，对应 lammps 命令 {@code pair_coeff}
     */
    public SystemLmpPotential(String aPairStyle, String... aPairCoeff) {
        this(aPairStyle, aPairCoeff, OS.EXEC, false);
    }
    
    private @Nullable String mDataPath = null;
    @Override public SystemLmpPotential setData(IAtomData aData) throws Exception {
        super.setData(aData);
        String tUniqueID = UT.Code.randID();
        IO.makeDir(mChecker.mWorkingDir);
        Lmpdat tData = Lmpdat.of(aData, Vectors.ones(aData.ntypes()));
        tData.ids().fill(i -> i+1); // 清空可能存在的 id，简化排序问题
        mDataPath = mChecker.mWorkingDir+"data-"+tUniqueID;
        tData.write(mDataPath);
        return this;
    }
    
    private String mLmpCommand = "lmp";
    /**
     * 设置运行 lammps 的命令，默认为 {@code lmp}
     * @param aLmpCommand 需要设置的运行 lammps 的命令
     * @return 自身方便链式调用
     */
    public SystemLmpPotential setLmpCommand(String aLmpCommand) {
        mLmpCommand = aLmpCommand;
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
        mChecker.dispose();
        if (mCloseExec) mExec.close();
    }
    
    /**
     * {@inheritDoc}
     * @param aRequireTotalEnergy {@inheritDoc}
     * @param aRequirePreAtomEnergy {@inheritDoc}
     * @param aRequireForce {@inheritDoc}
     * @param aRequireTotalStress {@inheritDoc}
     * @param aRequirePreAtomStress {@inheritDoc}
     * @throws IOException 读写临时文件时触发异常
     */
    @Override public void calculate(boolean aRequireTotalEnergy, boolean aRequirePreAtomEnergy, boolean aRequireForce, boolean aRequireTotalStress, boolean aRequirePreAtomStress) throws IOException {
        if (mDead) throw new IllegalStateException("This Potential is dead");
        // 没有设置 data 抛出异常
        if (mDataPath == null) throw new IllegalStateException("Need `setData` first");
        // 除了保持简单，pair style 等参数也可能发生改变，因此这里总是触发重新计算
        String tUniqueID = UT.Code.randID();
        // 准备输入 in 文件
        List<String> rLmpIn = new ArrayList<>();
        if (mBeforeCommands != null) rLmpIn.add(mBeforeCommands);
        rLmpIn.add("units  "+mUnits);
        rLmpIn.add("boundary  p p p");
        rLmpIn.add("read_data  "+mDataPath);
        rLmpIn.add("pair_style   "+mPairStyle);
        for (String tPairCoeff : mPairCoeff) {
            rLmpIn.add("pair_coeff   "+tPairCoeff);
        }
        if (mLastCommands != null) rLmpIn.add(mLastCommands);
        // 增加这个 thermo 确保势能和应力可以获取到
        List<String> rThermoStyle = new ArrayList<>(8);
        if (aRequireTotalEnergy) {
            rThermoStyle.add("pe");
        }
        if (aRequireTotalStress) {
            rLmpIn.add("compute p_tot all pressure NULL virial");
            rThermoStyle.add("c_p_tot[1]");
            rThermoStyle.add("c_p_tot[2]");
            rThermoStyle.add("c_p_tot[3]");
            rThermoStyle.add("c_p_tot[4]");
            rThermoStyle.add("c_p_tot[5]");
            rThermoStyle.add("c_p_tot[6]");
        }
        rLmpIn.add("thermo_style  custom step "+String.join(" ", rThermoStyle));
        rLmpIn.add("thermo  1");
        rLmpIn.add("thermo_modify  format float %24.18g"); // 调整输出精度
        // 按需增加对应的 compute
        if (aRequirePreAtomEnergy) {
            rLmpIn.add("compute eng_atom all pe/atom");
        }
        if (aRequirePreAtomStress) {
            rLmpIn.add("compute stress_atom all stress/atom NULL virial");
        }
        String tDumpPath = mChecker.mWorkingDir+"dump-"+tUniqueID;
        List<String> rDumpCustom = new ArrayList<>(10);
        if (aRequireForce) {
            rDumpCustom.add("fx");
            rDumpCustom.add("fy");
            rDumpCustom.add("fz");
        }
        if (aRequirePreAtomEnergy) {
            rDumpCustom.add("c_eng_atom");
        }
        if (aRequirePreAtomStress) {
            rDumpCustom.add("c_stress_atom[1]");
            rDumpCustom.add("c_stress_atom[2]");
            rDumpCustom.add("c_stress_atom[3]");
            rDumpCustom.add("c_stress_atom[4]");
            rDumpCustom.add("c_stress_atom[5]");
            rDumpCustom.add("c_stress_atom[6]");
        }
        rLmpIn.add("dump  1 all custom 1 "+tDumpPath+" id "+String.join(" ", rDumpCustom));
        rLmpIn.add("dump_modify  1 format float %24.18g"); // 调整输出精度
        // 通过 run 0 来触发计算
        rLmpIn.add("run  0");
        // 运行 lammps
        String tInPath = mChecker.mWorkingDir+"in-"+tUniqueID;
        IO.write(tInPath, rLmpIn);
        String tLogPath = mChecker.mWorkingDir+"log-"+tUniqueID;
        int tExitCode = mExec.system(mLmpCommand+" -in "+tInPath+" -log "+tLogPath+" -screen none");
        if (tExitCode != 0) throw new RuntimeException("Lammps run failed, exit code: " + tExitCode);
        
        // 直接获取结果
        Thermo tLog = Thermo.read(tLogPath);
        ITable tDump = SubLammpstrj.read(tDumpPath).asTable();
        // lammps 会乱序，需要重新排序，这里可以确定可以按照 id 来排序
        IIntVector tLmpIdx2Idx = null;
        if (aRequireForce || aRequirePreAtomEnergy || aRequirePreAtomStress) {
            tLmpIdx2Idx = tDump.col("id").asIntVec().copy();
            tLmpIdx2Idx.minus2this(1);
        }
        if (aRequireTotalEnergy) {
            mEnergy = tLog.get(0, "PotEng");
        }
        if (aRequireTotalStress) {
            mStressXX = -validStressUnit(tLog.get(0, "c_p_tot[1]"));
            mStressYY = -validStressUnit(tLog.get(0, "c_p_tot[2]"));
            mStressZZ = -validStressUnit(tLog.get(0, "c_p_tot[3]"));
            mStressXY = -validStressUnit(tLog.get(0, "c_p_tot[4]"));
            mStressXZ = -validStressUnit(tLog.get(0, "c_p_tot[5]"));
            mStressYZ = -validStressUnit(tLog.get(0, "c_p_tot[6]"));
        }
        if (aRequireForce) {
            mForcesX.putAt(tLmpIdx2Idx, tDump.col("fx"));
            mForcesY.putAt(tLmpIdx2Idx, tDump.col("fy"));
            mForcesZ.putAt(tLmpIdx2Idx, tDump.col("fz"));
        }
        if (aRequirePreAtomEnergy) {
            mEnergies.putAt(tLmpIdx2Idx, tDump.col("c_eng_atom"));
        }
        if (aRequirePreAtomStress) {
            mStressesXX.putAt(tLmpIdx2Idx, tDump.col("c_stress_atom[1]")); mStressesXX.operation().map2this(this::validStressUnit);
            mStressesYY.putAt(tLmpIdx2Idx, tDump.col("c_stress_atom[2]")); mStressesYY.operation().map2this(this::validStressUnit);
            mStressesZZ.putAt(tLmpIdx2Idx, tDump.col("c_stress_atom[3]")); mStressesZZ.operation().map2this(this::validStressUnit);
            mStressesXY.putAt(tLmpIdx2Idx, tDump.col("c_stress_atom[4]")); mStressesXY.operation().map2this(this::validStressUnit);
            mStressesXZ.putAt(tLmpIdx2Idx, tDump.col("c_stress_atom[5]")); mStressesXZ.operation().map2this(this::validStressUnit);
            mStressesYZ.putAt(tLmpIdx2Idx, tDump.col("c_stress_atom[6]")); mStressesYZ.operation().map2this(this::validStressUnit);
        }
        // 最后调整 box 变化导致的力和应力方向变化
        validBox(aRequireForce, aRequireTotalStress, aRequirePreAtomStress);
    }
}
