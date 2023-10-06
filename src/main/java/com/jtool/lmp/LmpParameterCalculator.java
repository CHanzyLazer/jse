package com.jtool.lmp;

import com.jtool.atom.IAtomData;
import com.jtool.atom.MultiFrameParameterCalculator;
import com.jtool.code.UT;
import com.jtool.code.collection.Pair;
import com.jtool.code.iterator.IDoubleIterator;
import com.jtool.math.MathEX;
import com.jtool.math.vector.IVector;
import com.jtool.math.vector.Vector;
import com.jtool.parallel.AbstractHasAutoShutdown;
import com.jtool.system.ISystemExecutor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;

import static com.jtool.code.CS.Exec.EXE;
import static com.jtool.code.CS.WORKING_DIR;

/**
 * 使用 lammps 来进行参数计算
 * <p>
 * 默认使用可读性更好的 data 文件作为输入输出，
 * 内部临时文件使用二进制的 restart 文件，
 * 可以减少各种类型文件的排列组合的接口
 * @author liqa
 */
public class LmpParameterCalculator extends AbstractHasAutoShutdown {
    private final String mWorkingDir;
    
    private final ILmpExecutor mLMP;
    private final boolean mIsTempLmp; // 标记此 lmp 是否是临时创建的，如果是则无论如何都要自动关闭
    private final String mPairStyle, mPairCoeff;
    
    /**
     * 创建一个 lammps 的参数计算器
     * @author liqa
     * @param aLMP 执行 lammps 的运行器
     * @param aPairStyle lammps 输入文件使用的势场类型
     * @param aPairCoeff lammps 输入文件使用的势场参数
     */
    public LmpParameterCalculator(ILmpExecutor aLMP, String aPairStyle, String aPairCoeff) {this(aLMP, false, aPairStyle, aPairCoeff);}
    public LmpParameterCalculator(String aLmpExe, @Nullable String aLogPath, String aPairStyle, String aPairCoeff) {this(new LmpExecutor(EXE, aLmpExe, aLogPath).setDoNotShutdown(true), true, aPairStyle, aPairCoeff);}
    public LmpParameterCalculator(String aLmpExe, String aPairStyle, String aPairCoeff) {this(aLmpExe, null, aPairStyle, aPairCoeff);}
    public LmpParameterCalculator(ISystemExecutor aEXE, String aLmpExe, @Nullable String aLogPath, String aPairStyle, String aPairCoeff) {this(new LmpExecutor(aEXE, aLmpExe, aLogPath), true, aPairStyle, aPairCoeff);}
    public LmpParameterCalculator(ISystemExecutor aEXE, String aLmpExe, String aPairStyle, String aPairCoeff) {this(aEXE, aLmpExe, null, aPairStyle, aPairCoeff);}
    LmpParameterCalculator(ILmpExecutor aLMP, boolean aIsTempLmp, String aPairStyle, String aPairCoeff) {
        mLMP = aLMP;
        mIsTempLmp = aIsTempLmp;
        mPairStyle = aPairStyle;
        mPairCoeff = aPairCoeff;
        // 最后设置一下工作目录
        mWorkingDir = WORKING_DIR.replaceAll("%n", "LPC@"+UT.Code.randID());
    }
    
    /** 是否在关闭此实例时顺便关闭内部 lmp */
    @Override public LmpParameterCalculator setDoNotShutdown(boolean aDoNotShutdown) {
        if (mIsTempLmp) {
            // 如果 lmp 是临时的，则此设置只影响 lmp 内部的 exe
            mLMP.setDoNotShutdown(aDoNotShutdown);
        } else {
            // 如果 lmp 是初始化时传入的，则直接影响 lmp 即可
            setDoNotShutdown_(aDoNotShutdown);
        }
        return this;
    }
    
    /** 程序结束时删除自己的临时工作目录，并且会关闭 EXE */
    private volatile boolean mDead = false;
    @Override protected void shutdown_() {
        mDead = true;
        try {
            UT.IO.removeDir(mWorkingDir);
            if (mLMP.exec().needSyncIOFiles()) mLMP.exec().removeDir(mWorkingDir);
        } catch (Exception ignored) {}
    }
    @Override protected void shutdownInternal_() {mLMP.shutdown();}
    @Override protected void closeInternal_() {mLMP.close();}
    
    
    
    /**
     * 将输入的原子数据在给定温度下进行熔融操作，将熔融结果输出到 aOutDataPath
     * <p>
     * 会忽略速度信息
     * @author liqa
     * @param aInDataPath 模拟作为输入的 data 文件路径
     * @param aOutDataPath 输出的 data 文件路径
     * @param aTemperature 熔融温度，K
     * @param aTimestep 模拟的时间步长，默认为 0.002
     * @param aRunStep 模拟步骤数，默认为 100000
     */
    public void runMelt(String aInDataPath, String aOutDataPath, double aTemperature, double aTimestep, int aRunStep) {
        if (mDead) throw new RuntimeException("This Calculator is dead");
        
        // 构造输入文件
        LmpIn rLmpIn = LmpIn.DATA2DATA_MELT_NPT_Cu();
        rLmpIn.put("vT", aTemperature);
        rLmpIn.put("vTimestep", aTimestep);
        rLmpIn.put("vRunStep", aRunStep);
        rLmpIn.put("pair_style", mPairStyle);
        rLmpIn.put("pair_coeff", mPairCoeff);
        rLmpIn.put("vSeed", UT.Code.randSeed());
        rLmpIn.put("vInDataPath", aInDataPath);
        rLmpIn.put("vOutDataPath", aOutDataPath);
        
        // 运行 lammps 获取输出
        int tExitValue = mLMP.run(rLmpIn);
        // 这里失败直接报错
        if (tExitValue != 0) throw new RuntimeException("LAMMPS run Failed, Exit Value: "+tExitValue);
    }
    public void runMelt(String aInDataPath, String aOutDataPath, double aTemperature, double aTimestep) {runMelt(aInDataPath, aOutDataPath, aTemperature, aTimestep, 100000);}
    public void runMelt(String aInDataPath, String aOutDataPath, double aTemperature) {runMelt(aInDataPath, aOutDataPath, aTemperature, 0.002);}
    
    public void runMelt(Lmpdat aLmpdat, String aOutDataPath, double aTemperature, double aTimestep, int aRunStep) throws IOException {
        // 由于可能存在外部并行，data 需要一个独立的名称
        String tInDataPath = mWorkingDir+"data@"+UT.Code.randID();
        aLmpdat.write(tInDataPath);
        runMelt(tInDataPath, aOutDataPath, aTemperature, aTimestep, aRunStep);
    }
    public void runMelt(Lmpdat aLmpdat, String aOutDataPath, double aTemperature, double aTimestep) throws IOException {runMelt(aLmpdat, aOutDataPath, aTemperature, aTimestep, 100000);}
    public void runMelt(Lmpdat aLmpdat, String aOutDataPath, double aTemperature) throws IOException {runMelt(aLmpdat, aOutDataPath, aTemperature, 0.002);}
    
    public void runMelt(IAtomData aAtomData, IVector                      aMasses, String aOutDataPath, double aTemperature, double aTimestep, int aRunStep) throws IOException {runMelt(Lmpdat.fromAtomData(aAtomData, aMasses), aOutDataPath, aTemperature, aTimestep, aRunStep);}
    public void runMelt(IAtomData aAtomData, Collection<? extends Number> aMasses, String aOutDataPath, double aTemperature, double aTimestep, int aRunStep) throws IOException {runMelt(Lmpdat.fromAtomData(aAtomData, aMasses), aOutDataPath, aTemperature, aTimestep, aRunStep);}
    public void runMelt(IAtomData aAtomData, double[]                     aMasses, String aOutDataPath, double aTemperature, double aTimestep, int aRunStep) throws IOException {runMelt(Lmpdat.fromAtomData(aAtomData, aMasses), aOutDataPath, aTemperature, aTimestep, aRunStep);}
    public void runMelt(IAtomData aAtomData, IVector                      aMasses, String aOutDataPath, double aTemperature, double aTimestep) throws IOException {runMelt(Lmpdat.fromAtomData(aAtomData, aMasses), aOutDataPath, aTemperature, aTimestep);}
    public void runMelt(IAtomData aAtomData, Collection<? extends Number> aMasses, String aOutDataPath, double aTemperature, double aTimestep) throws IOException {runMelt(Lmpdat.fromAtomData(aAtomData, aMasses), aOutDataPath, aTemperature, aTimestep);}
    public void runMelt(IAtomData aAtomData, double[]                     aMasses, String aOutDataPath, double aTemperature, double aTimestep) throws IOException {runMelt(Lmpdat.fromAtomData(aAtomData, aMasses), aOutDataPath, aTemperature, aTimestep);}
    public void runMelt(IAtomData aAtomData, IVector                      aMasses, String aOutDataPath, double aTemperature) throws IOException {runMelt(Lmpdat.fromAtomData(aAtomData, aMasses), aOutDataPath, aTemperature);}
    public void runMelt(IAtomData aAtomData, Collection<? extends Number> aMasses, String aOutDataPath, double aTemperature) throws IOException {runMelt(Lmpdat.fromAtomData(aAtomData, aMasses), aOutDataPath, aTemperature);}
    public void runMelt(IAtomData aAtomData, double[]                     aMasses, String aOutDataPath, double aTemperature) throws IOException {runMelt(Lmpdat.fromAtomData(aAtomData, aMasses), aOutDataPath, aTemperature);}
    
    
    /** 内部使用的熔融操作，输出 restart 文件 */
    public void runMelt_(String aInDataPath, String aOutRestartPath, double aTemperature, double aTimestep, int aRunStep) {
        if (mDead) throw new RuntimeException("This Calculator is dead");
        
        // 构造输入文件
        LmpIn rLmpIn = LmpIn.DATA2RESTART_MELT_NPT_Cu();
        rLmpIn.put("vT", aTemperature);
        rLmpIn.put("vTimestep", aTimestep);
        rLmpIn.put("vRunStep", aRunStep);
        rLmpIn.put("pair_style", mPairStyle);
        rLmpIn.put("pair_coeff", mPairCoeff);
        rLmpIn.put("vSeed", UT.Code.randSeed());
        rLmpIn.put("vInDataPath", aInDataPath);
        rLmpIn.put("vOutRestartPath", aOutRestartPath);
        
        // 运行 lammps 获取输出
        int tExitValue = mLMP.run(rLmpIn);
        // 这里失败直接报错
        if (tExitValue != 0) throw new RuntimeException("LAMMPS run Failed, Exit Value: "+tExitValue);
    }
    public void runMelt_(Lmpdat aLmpdat, String aOutRestartPath, double aTemperature, double aTimestep, int aRunStep) throws IOException {
        // 由于可能存在外部并行，data 需要一个独立的名称
        String tInDataPath = mWorkingDir+"data@"+UT.Code.randID();
        aLmpdat.write(tInDataPath);
        runMelt_(tInDataPath, aOutRestartPath, aTemperature, aTimestep, aRunStep);
    }
    
    
    /**
     * 计算 MSD (Mean Square Displacement)，
     * 会自动分划两组不同步长的 dump 分别计算
     * <p>
     * 会忽略速度信息统一进行预热处理
     * <p> 
     * TODO: 由于目前 Func1 只支持均匀间距的点，这里暂时直接输出两组向量
     * @author liqa
     * @param aInDataPath 模拟作为输入的 data 文件路径
     * @param aTemperature 模拟的温度，K
     * @param aDenseNormalized 是否在密度归一化后再进行计算，默认关闭
     * @param aN 期望计算的时间点数目，默认为 20
     * @param aTimestep 模拟的时间步长，默认为 0.002
     * @param aTimeGap 进行平均的时间间隔，认为这个时间间隔后的系统不再相关，默认为 100*aTimestep
     * @param aMaxStepNum 期望的最大模拟步骤数，默认为 100000
     * @return 对应时间下 MSD 的函数
     */
    public Pair<IVector, IVector> calMSD(String aInDataPath, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep, double aTimeGap, int aMaxStepNum) throws IOException {
        if (mDead) throw new RuntimeException("This Calculator is dead");
        // 由于可能存在外部并行，restart 需要一个独立的名称
        String tRestartPath = mWorkingDir+"restart@"+UT.Code.randID();
        // 先做预热处理
        runMelt_(aInDataPath, tRestartPath, aTemperature, aTimestep, 10000);
        // 然后计算 msd
        return calMSD_(tRestartPath, aTemperature, aDenseNormalized, aN, aTimestep, aTimeGap, aMaxStepNum);
    }
    public Pair<IVector, IVector> calMSD(String aInDataPath, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep, double aTimeGap) throws IOException {return calMSD(aInDataPath, aTemperature, aDenseNormalized, aN, aTimestep, aTimeGap, 100000);}
    public Pair<IVector, IVector> calMSD(String aInDataPath, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep) throws IOException {return calMSD(aInDataPath, aTemperature, aDenseNormalized, aN, aTimestep, 100*aTimestep);}
    public Pair<IVector, IVector> calMSD(String aInDataPath, double aTemperature, boolean aDenseNormalized, int aN) throws IOException {return calMSD(aInDataPath, aTemperature, aDenseNormalized, aN, 0.002);}
    public Pair<IVector, IVector> calMSD(String aInDataPath, double aTemperature, boolean aDenseNormalized) throws IOException {return calMSD(aInDataPath, aTemperature, aDenseNormalized, 20);}
    public Pair<IVector, IVector> calMSD(String aInDataPath, double aTemperature) throws IOException {return calMSD(aInDataPath, aTemperature, false);}
    
    public Pair<IVector, IVector> calMSD(Lmpdat aLmpdat, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep, double aTimeGap, int aMaxStepNum) throws IOException {
        if (mDead) throw new RuntimeException("This Calculator is dead");
        // 由于可能存在外部并行，restart 需要一个独立的名称
        String tLmpRestartPath = mWorkingDir+"restart@"+UT.Code.randID();
        // 先做预热处理
        runMelt_(aLmpdat, tLmpRestartPath, aTemperature, aTimestep, 10000);
        // 然后计算 msd
        return calMSD_(tLmpRestartPath, aTemperature, aDenseNormalized, aN, aTimestep, aTimeGap, aMaxStepNum);
    }
    public Pair<IVector, IVector> calMSD(Lmpdat aLmpdat, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep, double aTimeGap) throws IOException {return calMSD(aLmpdat, aTemperature, aDenseNormalized, aN, aTimestep, aTimeGap, 100000);}
    public Pair<IVector, IVector> calMSD(Lmpdat aLmpdat, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep) throws IOException {return calMSD(aLmpdat, aTemperature, aDenseNormalized, aN, aTimestep, 100*aTimestep);}
    public Pair<IVector, IVector> calMSD(Lmpdat aLmpdat, double aTemperature, boolean aDenseNormalized, int aN) throws IOException {return calMSD(aLmpdat, aTemperature, aDenseNormalized, aN, 0.002);}
    public Pair<IVector, IVector> calMSD(Lmpdat aLmpdat, double aTemperature, boolean aDenseNormalized) throws IOException {return calMSD(aLmpdat, aTemperature, aDenseNormalized, 20);}
    public Pair<IVector, IVector> calMSD(Lmpdat aLmpdat, double aTemperature) throws IOException {return calMSD(aLmpdat, aTemperature, false);}
    
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, IVector                      aMasses, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep, double aTimeGap, int aMaxStepNum) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized, aN, aTimestep, aTimeGap, aMaxStepNum);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, Collection<? extends Number> aMasses, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep, double aTimeGap, int aMaxStepNum) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized, aN, aTimestep, aTimeGap, aMaxStepNum);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, double[]                     aMasses, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep, double aTimeGap, int aMaxStepNum) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized, aN, aTimestep, aTimeGap, aMaxStepNum);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, IVector                      aMasses, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep, double aTimeGap) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized, aN, aTimestep, aTimeGap);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, Collection<? extends Number> aMasses, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep, double aTimeGap) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized, aN, aTimestep, aTimeGap);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, double[]                     aMasses, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep, double aTimeGap) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized, aN, aTimestep, aTimeGap);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, IVector                      aMasses, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized, aN, aTimestep);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, Collection<? extends Number> aMasses, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized, aN, aTimestep);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, double[]                     aMasses, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized, aN, aTimestep);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, IVector                      aMasses, double aTemperature, boolean aDenseNormalized, int aN) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized, aN);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, Collection<? extends Number> aMasses, double aTemperature, boolean aDenseNormalized, int aN) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized, aN);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, double[]                     aMasses, double aTemperature, boolean aDenseNormalized, int aN) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized, aN);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, IVector                      aMasses, double aTemperature, boolean aDenseNormalized) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, Collection<? extends Number> aMasses, double aTemperature, boolean aDenseNormalized) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, double[]                     aMasses, double aTemperature, boolean aDenseNormalized) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature, aDenseNormalized);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, IVector                      aMasses, double aTemperature) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, Collection<? extends Number> aMasses, double aTemperature) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature);}
    public Pair<IVector, IVector> calMSD(IAtomData aAtomData, double[]                     aMasses, double aTemperature) throws IOException {return calMSD(Lmpdat.fromAtomData(aAtomData, aMasses), aTemperature);}
    
    /** 内部使用的 restart 输入的不做预热处理的方法 */
    public Pair<IVector, IVector> calMSD_(String aInRestartPath, double aTemperature, boolean aDenseNormalized, int aN, double aTimestep, double aTimeGap, int aMaxStepNum) throws IOException {
        if (mDead) throw new RuntimeException("This Calculator is dead");
        
        // 由于可能存在外部并行，dump 需要一个独立的名称
        String tLmpDumpPath = mWorkingDir+"dump@"+UT.Code.randID();
        
        // 构造输入文件
        LmpIn rLmpIn = LmpIn.RESTART2DUMP_MELT_NPT_Cu();
        rLmpIn.put("vT", aTemperature);
        rLmpIn.put("vTimestep", aTimestep);
        rLmpIn.put("pair_style", mPairStyle);
        rLmpIn.put("pair_coeff", mPairCoeff);
        rLmpIn.put("vInRestartPath", aInRestartPath);
        rLmpIn.put("vDumpPath", tLmpDumpPath);
        
        Vector.Builder rMSD = Vector.builder();
        Vector.Builder rTime = Vector.builder();
        // 尝试分段，长时间固定取 200 帧
        int tLongDumpStep = MathEX.Code.divup(aMaxStepNum, 200);
        // 判断是否需要短时的，只要长间距大于 50 则需要
        if (tLongDumpStep > 50) {
            // 此时固定取 500 帧
            int tShotDumpStep = MathEX.Code.divup(tLongDumpStep, 500);
            // 固定最小间距为 10
            if (tShotDumpStep < 10) tShotDumpStep = 10;
            // 计算短时需要的分点
            int tN = (int)Math.round(MathEX.Fast.log(tLongDumpStep*0.2) / MathEX.Fast.log(aMaxStepNum*0.08) * (aN-1));
            if (tN > 0) {
                aN -= tN; ++tN;
                // 进行短时分段的统计，这里要保证能“刚好”接上
                rLmpIn.put("vRunStep", tShotDumpStep*500);
                rLmpIn.put("vDumpStep", tShotDumpStep);
                // 运行 lammps 获取输出
                int tExitValue = mLMP.run(rLmpIn);
                // 这里失败直接报错
                if (tExitValue != 0) throw new RuntimeException("LAMMPS run Failed, Exit Value: "+tExitValue);
                //  使用 mfpc 来计算 msd
                Lammpstrj tLammpstrj = Lammpstrj.read(tLmpDumpPath);
                if (aDenseNormalized) tLammpstrj.setDenseNormalized();
                try (MultiFrameParameterCalculator tMFPC = tLammpstrj.getMultiFrameParameterCalculator(aTimestep)) {
                    Pair<IVector, IVector> tOut = tMFPC.calMSD(tN, aTimeGap, aTimestep*tLongDumpStep*2.0);
                    tOut.mFirst.forEach(rMSD::add);
                    tOut.mSecond.forEach(rTime::add);
                }
            }
        }
        
        // 进行长时的统计
        rLmpIn.put("vRunStep", aMaxStepNum);
        rLmpIn.put("vDumpStep", tLongDumpStep);
        // 运行 lammps 获取输出
        int tExitValue = mLMP.run(rLmpIn);
        // 这里失败直接报错
        if (tExitValue != 0) throw new RuntimeException("LAMMPS run Failed, Exit Value: "+tExitValue);
        //  使用 mfpc 来计算 msd
        Lammpstrj tLammpstrj = Lammpstrj.read(tLmpDumpPath);
        if (aDenseNormalized) tLammpstrj.setDenseNormalized();
        try (MultiFrameParameterCalculator tMFPC = tLammpstrj.getMultiFrameParameterCalculator(aTimestep)) {
            Pair<IVector, IVector> tOut = tMFPC.calMSD(aN, aTimeGap);
            // 连接处平均
            if (rMSD.isEmpty()) {
                tOut.mFirst.forEach(rMSD::add);
                tOut.mSecond.forEach(rTime::add);
            } else {
                IDoubleIterator itMSD = tOut.mFirst.iterator();
                IDoubleIterator itTime = tOut.mSecond.iterator();
                itTime.next();
                rMSD.set(rMSD.size()-1, (rMSD.last() + itMSD.next())*0.5);
                itMSD.forEachRemaining(rMSD::add);
                itTime.forEachRemaining(rTime::add);
            }
        }
        
        // 返回结果
        rMSD.shrinkToFit();
        rTime.shrinkToFit();
        return new Pair<>(rMSD.build(), rTime.build());
    }
}
