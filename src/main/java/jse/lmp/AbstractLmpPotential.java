package jse.lmp;

import jse.atom.IAtomData;
import jse.atom.IBox;
import jse.atom.AbstractPotential;
import jse.atom.XYZ;
import jse.math.MathEX;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jse.code.CS.EV_TO_KCAL;
import static jse.code.CS.UNITS;

/**
 * 通用的 lammps 势函数抽象类，用于减少重复代码
 * @author liqa
 */
@SuppressWarnings({"UnknownLanguage", "RedundantSuppression"})
abstract class AbstractLmpPotential extends AbstractPotential {
    final static String DEFAULT_UNITS = "metal";
    /** 将 lammps metal 单位制中的 bar 转换成 ev/Å^3 需要乘的倍数 */
    public final static double BAR_TO_EV = UNITS.get("bar");
    /** 将 lammps real 单位制中的 atmospheres 转换成 (kcal/mol)/Å^3 需要乘的倍数 */
    public final static double ATM_TO_KCAL = 1.01325*BAR_TO_EV*EV_TO_KCAL;
    /** 将 lammps electron 单位制中的 Pascal 转换成 Hartree/Bohr^3 需要乘的倍数 */
    public final static double PA_TO_HARTREE = UNITS.get("Pascal") / UNITS.get("Hartree") * MathEX.Code.pow3(UNITS.get("Bohr"));
    
    @NotNull String mPairStyle;
    @NotNull String[] mPairCoeff;
    /**
     * 根据输入的 aPairStyle 和 aPairCoeff 创建一个运行 lammps 计算的势函数
     * @param aPairStyle 希望使用的 lammps 中的 pair 样式，对应 lammps 命令 {@code pair_style}
     * @param aPairCoeff lammps pair 需要设置的参数，对应 lammps 命令 {@code pair_coeff}
     */
    @SuppressWarnings("NullableProblems")
    public AbstractLmpPotential(String aPairStyle, String... aPairCoeff) {
        if (aPairStyle==null || aPairCoeff==null) throw new NullPointerException();
        for (String tPairCoeff : aPairCoeff) {
            if (tPairCoeff == null) throw new NullPointerException();
        }
        mPairStyle = aPairStyle;
        mPairCoeff = aPairCoeff;
    }
    /**
     * 设置 lammps 中的 pair 样式，对应 lammps 命令 {@code pair_style}
     * @param aPairStyle 需要设置的 {@code pair_style}
     * @return 自身方便链式调用
     */
    public AbstractLmpPotential setPairStyle(String aPairStyle) {
        if (aPairStyle==null) throw new NullPointerException();
        mPairStyle = aPairStyle;
        return this;
    }
    /**
     * 设置 lammps pair 需要设置的参数，对应 lammps 命令 {@code pair_coeff}
     * @param aPairCoeff 需要设置的 {@code pair_coeff}
     * @return 自身方便链式调用
     */
    public AbstractLmpPotential setPairCoeff(String... aPairCoeff) {
        if (aPairCoeff==null) throw new NullPointerException();
        for (String tPairCoeff : aPairCoeff) {
            if (tPairCoeff == null) throw new NullPointerException();
        }
        mPairCoeff = aPairCoeff;
        return this;
    }
    
    @Language("lmpin") @Nullable String mBeforeCommands = null;
    /**
     * 设置需要在 lammps 运行最开始执行的命令，可以用来进行加载插件等初始化，通过换行符
     * {@code \n} 来输入多个命令
     * @param aCommands 需要设置的最开始执行的 lammps 命令
     * @return 自身方便链式调用
     */
    public AbstractLmpPotential setBeforeCommands(@Language("lmpin") String aCommands) {
        mBeforeCommands = aCommands;
        return this;
    }
    @Language("lmpin") @Nullable String mLastCommands = null;
    /**
     * 设置需要在 lammps 运行最后执行的命令，可以用来设置 {@code pair_modify}
     * 等命令，通过换行符 {@code \n} 来输入多个命令
     * @param aCommands 需要设置的最后执行的 lammps 命令
     * @return 自身方便链式调用
     */
    public AbstractLmpPotential setLastCommands(@Language("lmpin") String aCommands) {
        mLastCommands = aCommands;
        return this;
    }
    
    String mUnits = DEFAULT_UNITS;
    /**
     * 设置内部 lammps 计算采用的单位，默认为 {@code metal}。
     * <p>
     * 注意设置单位仅仅影响能量和长度的单位，压强和力会统一采用推导的形式。
     * 因此计算输出的单位不一定会是 lammps 设定的单位，例如对于 {@code metal}
     * 单位，lammps 压力单位为 {@code bar}，而这里会统一进行单位转换，确保压力单位为
     * {@code eV/Å^3}（其余单位恰好一致）
     *
     * @param aUnits 需要设置的单位
     * @return 自身方便链式调用
     */
    public AbstractLmpPotential setUnits(String aUnits) {
        mUnits = aUnits;
        return this;
    }
    /** @return 内部 lammps 计算采用的单位，默认为 {@code metal} */
    public String units() {
        return mUnits;
    }
    
    IBox mBoxIn = null, mBoxOut = null;
    boolean mIsLmpStyle = false;
    @Override public AbstractLmpPotential setData(IAtomData aData) throws Exception {
        super.setData(aData);
        mIsLmpStyle = aData.isLmpStyle();
        mBoxOut = aData.box().copy();
        mBoxIn = LmpBox.of(mBoxOut);
        return this;
    }
    
    double validStressUnit(double aLmpStress) {
        switch (mUnits) {
        case "metal": {return aLmpStress*BAR_TO_EV;}
        case "real": {return aLmpStress*ATM_TO_KCAL;}
        case "electron": {return aLmpStress*PA_TO_HARTREE;}
        default: {return aLmpStress;}
        }
    }
    @SuppressWarnings("SuspiciousNameCombination")
    void rotateStress(XYZ rBuf0, XYZ rBuf1, XYZ rBuf2) {
        // 应力需要这样旋转变换两次
        mBoxIn.toDirect(rBuf0);
        mBoxIn.toDirect(rBuf1);
        mBoxIn.toDirect(rBuf2);
        mBoxOut.toCartesian(rBuf0);
        mBoxOut.toCartesian(rBuf1);
        mBoxOut.toCartesian(rBuf2);
        double
        tV = rBuf0.mY; rBuf0.mY = rBuf1.mX; rBuf1.mX = tV;
        tV = rBuf0.mZ; rBuf0.mZ = rBuf2.mX; rBuf2.mX = tV;
        tV = rBuf1.mZ; rBuf1.mZ = rBuf2.mY; rBuf2.mY = tV;
        mBoxIn.toDirect(rBuf0);
        mBoxIn.toDirect(rBuf1);
        mBoxIn.toDirect(rBuf2);
        mBoxOut.toCartesian(rBuf0);
        mBoxOut.toCartesian(rBuf1);
        mBoxOut.toCartesian(rBuf2);
    }
    void validBox(boolean aRequireForce, boolean aRequireTotalStress, boolean aRequirePreAtomStress) {
        // 如果模拟盒不是 lmpstyle，还需要对力以及压力进行转换
        if (!mIsLmpStyle) {
            XYZ tBuf0 = new XYZ(), tBuf1 = new XYZ(), tBuf2 = new XYZ();
            if (aRequireForce) for (int i = 0; i < mNumAtoms; ++i) {
                tBuf0.setXYZ(mForcesX.get(i), mForcesY.get(i), mForcesZ.get(i));
                mBoxIn.toDirect(tBuf0);
                mBoxOut.toCartesian(tBuf0);
                mForcesX.set(i, tBuf0.mX);
                mForcesY.set(i, tBuf0.mY);
                mForcesZ.set(i, tBuf0.mZ);
            }
            if (aRequireTotalStress) {
                tBuf0.setXYZ(mStressXX, mStressXY, mStressXZ);
                tBuf1.setXYZ(mStressXY, mStressYY, mStressYZ);
                tBuf2.setXYZ(mStressXZ, mStressYZ, mStressZZ);
                rotateStress(tBuf0, tBuf1, tBuf2);
                mStressXX = tBuf0.mX; mStressYY = tBuf1.mY; mStressZZ = tBuf2.mZ;
                mStressXY = tBuf0.mY; mStressXZ = tBuf0.mZ; mStressYZ = tBuf1.mZ;
            }
            if (aRequirePreAtomStress) for (int i = 0; i < mNumAtoms; ++i) {
                tBuf0.setXYZ(mStressesXX.get(i), mStressesXY.get(i), mStressesXZ.get(i));
                tBuf1.setXYZ(mStressesXY.get(i), mStressesYY.get(i), mStressesYZ.get(i));
                tBuf2.setXYZ(mStressesXZ.get(i), mStressesYZ.get(i), mStressesZZ.get(i));
                rotateStress(tBuf0, tBuf1, tBuf2);
                mStressesXX.set(i, tBuf0.mX); mStressesYY.set(i, tBuf1.mY); mStressesZZ.set(i, tBuf2.mZ);
                mStressesXY.set(i, tBuf0.mY); mStressesXZ.set(i, tBuf0.mZ); mStressesYZ.set(i, tBuf1.mZ);
            }
        }
    }
}
