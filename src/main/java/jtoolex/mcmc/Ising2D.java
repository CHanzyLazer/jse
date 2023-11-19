package jtoolex.mcmc;

import jtool.math.MathEX;
import jtool.math.matrix.IMatrix;
import jtool.math.matrix.Matrices;
import jtool.math.random.LocalRandom;
import jtool.parallel.AbstractThreadPool;
import jtool.parallel.ParforThreadPool;

import java.util.Collections;
import java.util.List;

import static jtool.code.CS.RANDOM;


/**
 * 简单的蒙特卡洛模拟二维 Ising 模型，
 * 支持并行
 * @author liqa
 */
public class Ising2D extends AbstractThreadPool<ParforThreadPool> {
    private final double mJ, mH;
    /** 线程独立的随机数生成器 */
    private final LocalRandom[] mRNG;
    
    /**
     * 按照给定参数构造一个模拟二维 Ising 模型的模拟器，有：
     * {@code Ham = -JΣᵢσᵢσᵢ₊₁ + HΣᵢσᵢ}
     * @author liqa
     * @param aJ 哈密顿量中的参数 J
     * @param aH 哈密顿量中的参数 H
     * @param aThreadNum 计算使用的线程数，默认为 1
     * @param aRNG 可自定义的随机数生成器，这里默认会新建一个线程独立的随机数生成器保证并行效率
     */
    Ising2D(double aJ, double aH, int aThreadNum, LocalRandom[] aRNG) {
        super(new ParforThreadPool(aThreadNum));
        mJ = aJ; mH = aH;
        mRNG = aRNG;
    }
    public Ising2D(double aJ, double aH, int aThreadNum, long[] aSeeds) {this(aJ, aH, aThreadNum, initRNG_(aThreadNum, aSeeds));}
    public Ising2D(double aJ, double aH, int aThreadNum) {this(aJ, aH, aThreadNum, initRNG_(aThreadNum));}
    public Ising2D(double aJ, double aH) {this(aJ, aH, 1);}

    private static LocalRandom[] initRNG_(int aSize, long[] aSeeds) {
        LocalRandom[] rRNG = new LocalRandom[aSize];
        for (int i = 0; i < aSize; ++i) rRNG[i] = new LocalRandom(aSeeds[i]);
        return rRNG;
    }
    private static LocalRandom[] initRNG_(int aSize) {
        LocalRandom[] rRNG = new LocalRandom[aSize];
        for (int i = 0; i < aSize; ++i) rRNG[i] = new LocalRandom(RANDOM.nextLong());
        return rRNG;
    }
    
    /** 获取一个随机的初始结构 */
    public static IMatrix initSpins(int aRowNum, int aColNum) {
        IMatrix rSpins = Matrices.zeros(aRowNum, aColNum);
        rSpins.assignCol(() -> RANDOM.nextBoolean() ? 1.0 : -1.0);
        return rSpins;
    }
    public static IMatrix initSpins(int aL) {return initSpins(aL, aL);}
    
    
    /** 统计能量，注意自旋相互作用只需要考虑一半 */
    public double statE(IMatrix aSpins) {
        int tRowNum = aSpins.rowNumber();
        int tColNum = aSpins.columnNumber();
        double rE = 0.0;
        for (int i = 0; i < tRowNum; ++i) for (int j = 0; j < tColNum; ++j)  {
            // 先考虑周期边界条件
            int ipp = i + 1; if (ipp >= tRowNum) ipp -= tRowNum;
            int jpp = j + 1; if (jpp >= tColNum) jpp -= tColNum;
            // 获取周围自旋值
            double tSpinC = aSpins.get(i  , j  );
            double tSpinR = aSpins.get(ipp, j  );
            double tSpinU = aSpins.get(i  , jpp);
            // 计算能量
            rE += tSpinC * mH;
            rE -= tSpinC*tSpinR * mJ;
            rE -= tSpinC*tSpinU * mJ;
        }
        return rE;
    }
    
    /** 用于返回的统计物理量 */
    public static final class Data {
        double mE, mM, mE2, mM2;
        Data(double aE, double aM, double aE2, double aM2) {
            mE = aE; mM = aM; mE2 = aE2; mM2 = aM2;
        }
        public double E () {return mE ;}
        public double M () {return mM ;}
        public double E2() {return mE2;}
        public double M2() {return mM2;}
    }
    
    /**
     * 对输入的自旋结构执行 N 步蒙特卡洛模拟，并统计
     * {@code <E>, <|M|>, <E^2>, <|M|^2>}
     * @author liqa
     * @param aSpinsList 所有自旋结构组成的数组，会并行执行模拟
     * @param aN 需要的模拟步骤数目
     * @param aT 需要的模拟温度，认为 kB = 1.0
     * @param aStat 是否顺便统计物理量，默认为 true
     * @return 统计得到的物理量数据，会将输入所有结构的物理量进行平均，如果关闭了统计则输出 null
     */
    public Data startMonteCarlo(final List<? extends IMatrix> aSpinsList, final long aN, final double aT, final boolean aStat) {
        // 需要返回的统计物理量，
        // 注意虽然 parfor 支持写入不同位置的数组时的线程安全，
        // 但由于隔离性不够，这会严重影响性能，因此还是希望实际写入的是不同的对象
        int tThreadNum = nThreads();
        final Data[] rDatas;
        if (aStat) {
            rDatas = new Data[tThreadNum];
            for (int i = 0; i < tThreadNum; ++i) rDatas[i] = new Data(0.0, 0.0, 0.0, 0.0);
        } else {
            rDatas = null;
        }
        
        pool().parfor(aSpinsList.size(), (l, threadID) -> {
            IMatrix tSpins = aSpinsList.get(l);
            LocalRandom tRNG = mRNG[threadID];
            
            int tRowNum = tSpins.rowNumber();
            int tColNum = tSpins.columnNumber();
            // 开始之前先初始统计物理量，初始值不计入统计
            double tE = Double.NaN;
            double tM = Double.NaN;
            if (aStat) {
                tE = statE(tSpins);
                tM = tSpins.operation().sum();
            }
            // 开始蒙特卡洛模拟
            for (long k = 0; k < aN; ++k) {
                int i = tRNG.nextInt(tRowNum);
                int j = tRNG.nextInt(tColNum);
                // 先考虑周期边界条件
                int ipp = i + 1; if (ipp >= tRowNum) ipp -= tRowNum;
                int inn = i - 1; if (inn <  0      ) inn += tRowNum;
                int jpp = j + 1; if (jpp >= tColNum) jpp -= tColNum;
                int jnn = j - 1; if (jnn <  0      ) jnn += tColNum;
                // 获取周围自旋值
                double tSpinC = tSpins.get(i  , j  );
                double tSpinL = tSpins.get(inn, j  );
                double tSpinR = tSpins.get(ipp, j  );
                double tSpinU = tSpins.get(i  , jpp);
                double tSpinD = tSpins.get(i  , jnn);
                // 计算翻转前后能量差
                double dE = -tSpinC * mH;
                dE += tSpinC*tSpinL * mJ;
                dE += tSpinC*tSpinR * mJ;
                dE += tSpinC*tSpinU * mJ;
                dE += tSpinC*tSpinD * mJ;
                dE *= 2.0;
                // 如果能量差小于 0 则 100% 接受翻转，否则概率接受
                if ((dE <= 0) || (tRNG.nextDouble() < MathEX.Fast.exp(-dE/aT))) {
                    tSpins.update(i, j, v->-v);
                    if (aStat) {
                        // 更新物理量
                        tE += dE;
                        tM -= 2.0*tSpinC;
                    }
                }
                if (aStat) {
                    // 累加统计结果
                    Data subData = rDatas[threadID];
                    subData.mE  += tE;
                    subData.mM  += Math.abs(tM); // 磁矩需要取绝对值
                    subData.mE2 += tE*tE;
                    subData.mM2 += tM*tM;
                }
            }
        });
        if (aStat) {
            double tDiv = aN*aSpinsList.size();
            Data rData = rDatas[0];
            for (int i = 1; i < tThreadNum; ++i) {
                Data subData = rDatas[i];
                rData.mE  += subData.mE ;
                rData.mM  += subData.mM ;
                rData.mE2 += subData.mE2;
                rData.mM2 += subData.mM2;
            }
            rData.mE  /= tDiv;
            rData.mM  /= tDiv;
            rData.mE2 /= tDiv;
            rData.mM2 /= tDiv;
            return rData;
        } else {
            return null;
        }
    }
    public Data startMonteCarlo(List<? extends IMatrix> aSpinsList, long aN, double aT) {return startMonteCarlo(aSpinsList, aN, aT, true);}
    public Data startMonteCarlo(IMatrix aSpins, long aN, double aT, boolean aStat) {return startMonteCarlo(Collections.singletonList(aSpins), aN, aT, aStat);}
    public Data startMonteCarlo(IMatrix aSpins, long aN, double aT) {return startMonteCarlo(aSpins, aN, aT, true);}
}
