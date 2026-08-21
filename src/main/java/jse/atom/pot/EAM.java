package jse.atom.pot;

import jse.atom.AbstractPairPotential;
import jse.code.IO;
import jse.code.collection.DoubleList;
import jse.code.collection.IntList;
import jse.math.MathEX;
import jse.math.function.ConstBoundFunc1;
import jse.math.function.IFunc1;
import jse.math.function.ZeroBoundFunc1;
import jse.math.vector.IVector;
import jse.math.vector.Vector;
import jse.math.vector.Vectors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;

import static jse.code.CS.ATOMIC_NUMBER_TO_SYMBOL;
import static jse.code.CS.UNITS;

/**
 * EAM 势（Embedded-Atom Method）的 jse 实现，具体形式为:
 * {@code E = F(Σρ(r)) + 0.5 ΣΦ(r)}；这里通过读取 DYNAMO
 * 格式的文件获取函数 {@code F, ρ, Φ} 的数值形式。
 * <p>
 * 具体类似
 * <a href="https://wiki.fysik.dtu.dk/ase/ase/calculators/eam.html">ase 的 EAM 计算器</a>，
 * 同样会自动识别势函数文件类型来选择 {@code eam}, {@code eam/alloy}, {@code eam/fs} 或
 * {@code adp}；但修复了 ase 实现中的许多问题，现在应该兼容更多 lammps 能读取的势函数文件。
 * <p>
 * 这里不去专门优化单粒子移动、翻转、种类交换时的能量差的计算，因为这需要缓存电荷密度值
 * <p>
 * 这里默认采用 lammps 使用的样条算法进行插值，考虑到样条插值并不总是收敛，有时需要主动关闭这个。
 *
 * @author liqa
 */
public class EAM extends AbstractPairPotential {
    public final static class Conf {
        /** 是否使用 lammps 中采用的低精度 hartree 和 bohr，从而让结果和 lammps 一致 */
        public static boolean USE_LAMMPS_PRECISION = true;
        /** 是否使用 lammps 中使用的样条插值，从而让结果和 lammps 一致；样条插值并不总是收敛，因此有时需要手动关闭 */
        public static boolean USE_SPLINE = true;
    }
    private final static double EAM_MUL;
    static {
        if (Conf.USE_LAMMPS_PRECISION) {
            EAM_MUL = 27.2*0.529;
        } else {
            EAM_MUL = UNITS.get("Hartree")*UNITS.get("Bohr");
        }
    }
    
    interface ISpline {
        double subs(double aX);
        double subsGrad(double aX);
    }
    
    /** 简单的线性样条插值 */
    static class LinearSpline implements ISpline {
        private final IFunc1 mFunc, mFuncGrad;
        LinearSpline(double aDx, IVector aData) {
            int tN = aData.size();
            mFunc = ConstBoundFunc1.zeros(0, aDx, tN);
            mFunc.fill(aData);
            mFuncGrad = ZeroBoundFunc1.zeros(aDx*0.5, aDx, tN-1);
            for (int i = 0; i < tN-1; ++i) {
                mFuncGrad.set(i, (aData.get(i+1) - aData.get(i)) / aDx);
            }
        }
        @Override public double subs(double aX) {
            return mFunc.subs(aX);
        }
        @Override public double subsGrad(double aX) {
            return mFuncGrad.subs(aX);
        }
    }
    
    /** lammps 中使用的快速样条 */
    static class FastSpline implements ISpline {
        private final double mDx;
        private final int mNx;
        private final double[][] mSplines;
        FastSpline(double aDx, IVector aData) {
            mDx = aDx;
            mNx = aData.size();
            mSplines = new double[mNx][7];
            
            // 样条参数计算，参考 lammps 相关代码
            for (int m = 0; m < mNx; ++m) {
                mSplines[m][6] = aData.get(m);
            }
            mSplines[0][5] = mSplines[1][6] - mSplines[0][6];
            mSplines[1][5] = 0.5 * (mSplines[2][6] - mSplines[0][6]);
            mSplines[mNx-2][5] = 0.5 * (mSplines[mNx-1][6] - mSplines[mNx-3][6]);
            mSplines[mNx-1][5] = mSplines[mNx-1][6] - mSplines[mNx-2][6];
            
            for (int m = 2; m < mNx-2; ++m) {
                mSplines[m][5] = ((mSplines[m-2][6] - mSplines[m+2][6]) + 8.0*(mSplines[m+1][6] - mSplines[m-1][6])) / 12.0;
            }
            for (int m = 0; m < mNx-1; ++m) {
                mSplines[m][4] = 3.0*(mSplines[m+1][6] - mSplines[m][6]) - 2.0*mSplines[m][5] - mSplines[m+1][5];
                mSplines[m][3] = mSplines[m][5] + mSplines[m+1][5] - 2.0*(mSplines[m+1][6] - mSplines[m][6]);
            }
            mSplines[mNx-1][4] = 0.0;
            mSplines[mNx-1][3] = 0.0;
            
            for (int m = 0; m < mNx; ++m) {
                mSplines[m][2] = mSplines[m][5]/mDx;
                mSplines[m][1] = 2.0*mSplines[m][4]/mDx;
                mSplines[m][0] = 3.0*mSplines[m][3]/mDx;
            }
        }
        
        @Override public double subs(double aX) {
            double p = aX/mDx;
            int m = MathEX.Code.floor2int(p);
            if (m >= mNx) m = mNx-1;
            p -= m;
            if (p > 1.0) p = 1.0;
            double[] coeff = mSplines[m];
            return ((coeff[3]*p + coeff[4])*p + coeff[5])*p + coeff[6];
        }
        @Override public double subsGrad(double aX) {
            double p = aX/mDx;
            int m = MathEX.Code.floor2int(p);
            if (m >= mNx) m = mNx-1;
            p -= m;
            if (p > 1.0) p = 1.0;
            double[] coeff = mSplines[m];
            return (coeff[0]*p + coeff[1])*p + coeff[2];
        }
    }
    
    private final double mCut, mCutsq;
    private final double mDRho, mDR;
    private final int mNRho, mNR;
    private final String mHeader;
    private final IVector[] mFRho;
    private final IVector[][] mRhoR, mRPhiR;
    private final IVector @Nullable[][] mUR, mWR;
    private final ISpline[] mFRhoSpline;
    private final ISpline[][] mRhoRSpline, mRPhiRSpline;
    private final ISpline @Nullable[][] mURSpline, mWRSpline;
    private final int mTypeNum;
    private final String[] mSymbols, mLatticeTypes;
    private final int[] mAtomicNumbers;
    private final double[] mMasses, mLatticeConsts;
    
    /**
     * 通过势函数文件创建一个 EAM 势函数
     * @param aFilePath EAM 势函数路径，要求 DYNAMO 格式的 lammps 支持的文件
     * @param aFormat 可选的 EAM 势函数文件格式，可选 {@code "eam", "alloy", "fs", "adp"}，默认根据后缀名自动检测
     */
    public EAM(String aFilePath, @Nullable String aFormat, int aNumThreads) throws IOException {
        super(aNumThreads);
        if (aFormat == null) {
            if (aFilePath.endsWith(".eam")) {
                aFormat = "eam";
            } else
            if (aFilePath.endsWith(".alloy")) {
                aFormat = "alloy";
            } else
            if (aFilePath.endsWith(".fs")) {
                aFormat = "fs";
            } else
            if (aFilePath.endsWith(".adp")) {
                aFormat = "adp";
            } else {
                throw new IllegalArgumentException("Unsupported EAM format: " + aFilePath);
            }
        }
        try (BufferedReader tReader = IO.toReader(aFilePath)) {
            switch(aFormat) {
            case "eam": {
                mHeader = tReader.readLine();
                String[] tTokens = IO.Text.splitBlank(tReader.readLine());
                mTypeNum = 1;
                mAtomicNumbers = new int[] {Integer.parseInt(tTokens[0])};
                mSymbols = new String[] {ATOMIC_NUMBER_TO_SYMBOL.get(mAtomicNumbers[0])};
                mMasses = new double[] {Double.parseDouble(tTokens[1])};
                mLatticeConsts = new double[] {Double.parseDouble(tTokens[2])};
                mLatticeTypes = new String[] {tTokens[3]};
                tTokens = IO.Text.splitBlank(tReader.readLine());
                mNRho = Integer.parseInt(tTokens[0]);
                mDRho = Double.parseDouble(tTokens[1]);
                mNR = Integer.parseInt(tTokens[2]);
                mDR = Double.parseDouble(tTokens[3]);
                double tRCut = Double.parseDouble(tTokens[4]);
                mCutsq = tRCut*tRCut;
                mCut = tRCut;
                mFRho = new IVector[1];
                mRhoR = new IVector[1][1];
                mRPhiR = new IVector[1][1];
                IVector tData = readData_(tReader, mNRho+mNR+mNR);
                mFRho[0] = tData.subVec(0, mNRho);
                mRPhiR[0][0] = tData.subVec(mNRho, mNRho+mNR);
                mRhoR[0][0] = tData.subVec(mNRho+mNR, mNRho+mNR+mNR);
                // Z(r) -> r*phi(r)
                mRPhiR[0][0].operation().map2this(z -> EAM_MUL * z*z);
                mFRhoSpline = new ISpline[] {Conf.USE_SPLINE ? new FastSpline(mDRho, mFRho[0]) : new LinearSpline(mDRho, mFRho[0])};
                mRhoRSpline = new ISpline[][] {{Conf.USE_SPLINE ? new FastSpline(mDR, mRhoR[0][0]) : new LinearSpline(mDR, mRhoR[0][0])}};
                mRPhiRSpline = new ISpline[][] {{Conf.USE_SPLINE ? new FastSpline(mDR, mRPhiR[0][0]) : new LinearSpline(mDR, mRPhiR[0][0])}};
                mUR = null; mWR = null; mURSpline = null; mWRSpline = null;
                break;
            }
            case "alloy": case "fs": case "adp": {
                boolean tIsFs = aFormat.equals("fs");
                boolean tIsAdp = aFormat.equals("adp");
                mHeader = tReader.readLine() + "\n" +
                          tReader.readLine() + "\n" +
                          tReader.readLine();
                String[] tTokens = IO.Text.splitBlank(tReader.readLine());
                mTypeNum = Integer.parseInt(tTokens[0]);
                mSymbols = new String[mTypeNum];
                System.arraycopy(tTokens, 1, mSymbols, 0, mTypeNum);
                mAtomicNumbers = new int[mTypeNum];
                mMasses = new double[mTypeNum];
                mLatticeConsts = new double[mTypeNum];
                mLatticeTypes = new String[mTypeNum];
                tTokens = IO.Text.splitBlank(tReader.readLine());
                mNRho = Integer.parseInt(tTokens[0]);
                mDRho = Double.parseDouble(tTokens[1]);
                mNR = Integer.parseInt(tTokens[2]);
                mDR = Double.parseDouble(tTokens[3]);
                double tRCut = Double.parseDouble(tTokens[4]);
                mCutsq = tRCut*tRCut;
                mCut = tRCut;
                mFRho = new IVector[mTypeNum];
                mRhoR = new IVector[tIsFs?mTypeNum:1][mTypeNum];
                mRPhiR = new IVector[mTypeNum][mTypeNum];
                mFRhoSpline = new ISpline[mTypeNum];
                mRhoRSpline = new ISpline[tIsFs?mTypeNum:1][mTypeNum];
                mRPhiRSpline = new ISpline[mTypeNum][mTypeNum];
                for (int i = 0; i < mTypeNum; ++i) {
                    tTokens = IO.Text.splitBlank(tReader.readLine());
                    mAtomicNumbers[i] = Integer.parseInt(tTokens[0]);
                    mMasses[i] = Double.parseDouble(tTokens[1]);
                    mLatticeConsts[i] = Double.parseDouble(tTokens[2]);
                    mLatticeTypes[i] = tTokens[3];
                    IVector tData = readData_(tReader, mNRho + (tIsFs?(mTypeNum*mNR):mNR));
                    mFRho[i] = tData.subVec(0, mNRho);
                    mFRhoSpline[i] = Conf.USE_SPLINE ? new FastSpline(mDRho, mFRho[i]) : new LinearSpline(mDRho, mFRho[i]);
                    if (tIsFs) {
                        int tShift = mNRho;
                        for (int j = 0; j < mTypeNum; ++j) {
                            mRhoR[j][i] = tData.subVec(tShift, tShift+mNR);
                            mRhoRSpline[j][i] = Conf.USE_SPLINE ? new FastSpline(mDR, mRhoR[j][i]) : new LinearSpline(mDR, mRhoR[j][i]);
                            tShift += mNR;
                        }
                    } else {
                        mRhoR[0][i] = tData.subVec(mNRho, mNRho+mNR);
                        mRhoRSpline[0][i] = Conf.USE_SPLINE ? new FastSpline(mDR, mRhoR[0][i]) : new LinearSpline(mDR, mRhoR[0][i]);
                    }
                }
                IVector tData = readData_(tReader, (1+mTypeNum)*mTypeNum/2 * mNR);
                int tShift = 0;
                for (int i = 0; i < mTypeNum; ++i) for (int j = 0; j <= i; ++j) {
                    mRPhiR[i][j] = tData.subVec(tShift, tShift+mNR);
                    mRPhiRSpline[i][j] = Conf.USE_SPLINE ? new FastSpline(mDR, mRPhiR[i][j]) : new LinearSpline(mDR, mRPhiR[i][j]);
                    if (j != i) {
                        mRPhiR[j][i] = mRPhiR[i][j];
                        mRPhiRSpline[j][i] = mRPhiRSpline[i][j];
                    }
                    tShift += mNR;
                }
                if (!tIsAdp) {
                    mUR = null; mWR = null; mURSpline = null; mWRSpline = null;
                    break;
                }
                mUR = new IVector[mTypeNum][mTypeNum];
                mWR = new IVector[mTypeNum][mTypeNum];
                mURSpline = new ISpline[mTypeNum][mTypeNum];
                mWRSpline = new ISpline[mTypeNum][mTypeNum];
                tData = readData_(tReader, (1+mTypeNum)*mTypeNum/2 * mNR);
                tShift = 0;
                for (int i = 0; i < mTypeNum; ++i) for (int j = 0; j <= i; ++j) {
                    mUR[i][j] = tData.subVec(tShift, tShift+mNR);
                    mURSpline[i][j] = Conf.USE_SPLINE ? new FastSpline(mDR, mUR[i][j]) : new LinearSpline(mDR, mUR[i][j]);
                    if (j != i) {
                        mUR[j][i] = mUR[i][j];
                        mURSpline[j][i] = mURSpline[i][j];
                    }
                    tShift += mNR;
                }
                tData = readData_(tReader, (1+mTypeNum)*mTypeNum/2 * mNR);
                tShift = 0;
                for (int i = 0; i < mTypeNum; ++i) for (int j = 0; j <= i; ++j) {
                    mWR[i][j] = tData.subVec(tShift, tShift+mNR);
                    mWRSpline[i][j] = Conf.USE_SPLINE ? new FastSpline(mDR, mWR[i][j]) : new LinearSpline(mDR, mWR[i][j]);
                    if (j != i) {
                        mWR[j][i] = mWR[i][j];
                        mWRSpline[j][i] = mWRSpline[i][j];
                    }
                    tShift += mNR;
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid EAM format: " + aFormat);
            }}
        }
    }
    /**
     * 通过势函数文件创建一个 EAM 势函数
     * @param aFilePath EAM 势函数路径，要求 DYNAMO 格式的 lammps 支持的文件
     * @param aFormat 可选的 EAM 势函数文件格式，可选 {@code "eam", "alloy", "fs", "adp"}，默认根据后缀名自动检测
     */
    public EAM(String aFilePath, @Nullable String aFormat) throws IOException {
        this(aFilePath, aFormat, 1);
    }
    /**
     * 通过势函数文件创建一个 EAM 势函数
     * @param aFilePath EAM 势函数路径，要求 DYNAMO 格式的 lammps 支持的文件
     */
    public EAM(String aFilePath) throws IOException {
        this(aFilePath, null);
    }
    
    @ApiStatus.Experimental
    public IFunc1 frho(int aType) {
        IFunc1 tOut = ConstBoundFunc1.zeros(0, mDRho, mNRho);
        tOut.fill(mFRho[aType-1]);
        return tOut;
    }
    @ApiStatus.Experimental
    public IFunc1 frhoSpline(int aType, int aN) {
        IFunc1 tOut = ConstBoundFunc1.zeros(0, (mDRho*mNRho)/aN, aN);
        tOut.fill(rho -> mFRhoSpline[aType-1].subs(rho));
        return tOut;
    }
    @ApiStatus.Experimental
    public IFunc1 frhoGradSpline(int aType, int aN) {
        IFunc1 tOut = ConstBoundFunc1.zeros(0, (mDRho*mNRho)/aN, aN);
        tOut.fill(rho -> mFRhoSpline[aType-1].subsGrad(rho));
        return tOut;
    }
    @ApiStatus.Experimental
    public IFunc1 rhor(int aType1, int aType2) {
        IFunc1 tOut = ConstBoundFunc1.zeros(0, mDR, mNR);
        tOut.fill(mRhoR[mRhoR.length==1?0:(aType1-1)][aType2-1]);
        return tOut;
    }
    @ApiStatus.Experimental
    public IFunc1 rhorSpline(int aType1, int aType2, int aN) {
        IFunc1 tOut = ConstBoundFunc1.zeros(0, (mDR*mNR)/aN, aN);
        tOut.fill(rho -> mRhoRSpline[mRhoR.length==1?0:(aType1-1)][aType2-1].subs(rho));
        return tOut;
    }
    @ApiStatus.Experimental
    public IFunc1 rhorGradSpline(int aType1, int aType2, int aN) {
        IFunc1 tOut = ConstBoundFunc1.zeros(0, (mDR*mNR)/aN, aN);
        tOut.fill(rho -> mRhoRSpline[mRhoR.length==1?0:(aType1-1)][aType2-1].subsGrad(rho));
        return tOut;
    }
    @ApiStatus.Experimental
    public IFunc1 rphir(int aType1, int aType2) {
        IFunc1 tOut = ConstBoundFunc1.zeros(0, mDR, mNR);
        tOut.fill(mRPhiR[aType1-1][aType2-1]);
        return tOut;
    }
    @ApiStatus.Experimental
    public IFunc1 rphirSpline(int aType1, int aType2, int aN) {
        IFunc1 tOut = ConstBoundFunc1.zeros(0, (mDR*mNR)/aN, aN);
        tOut.fill(rho -> mRPhiRSpline[aType1-1][aType2-1].subs(rho));
        return tOut;
    }
    @ApiStatus.Experimental
    public IFunc1 rphirGradSpline(int aType1, int aType2, int aN) {
        IFunc1 tOut = ConstBoundFunc1.zeros(0, (mDR*mNR)/aN, aN);
        tOut.fill(rho -> mRPhiRSpline[aType1-1][aType2-1].subsGrad(rho));
        return tOut;
    }
    @ApiStatus.Experimental
    public @Nullable IFunc1 ur(int aType1, int aType2) {
        if (mUR == null) return null;
        IFunc1 tOut = ConstBoundFunc1.zeros(0, mDR, mNR);
        tOut.fill(mUR[aType1-1][aType2-1]);
        return tOut;
    }
    @ApiStatus.Experimental
    public @Nullable IFunc1 urSpline(int aType1, int aType2, int aN) {
        if (mURSpline == null) return null;
        IFunc1 tOut = ConstBoundFunc1.zeros(0, (mDR*mNR)/aN, aN);
        tOut.fill(rho -> mURSpline[aType1-1][aType2-1].subs(rho));
        return tOut;
    }
    @ApiStatus.Experimental
    public @Nullable IFunc1 urGradSpline(int aType1, int aType2, int aN) {
        if (mURSpline == null) return null;
        IFunc1 tOut = ConstBoundFunc1.zeros(0, (mDR*mNR)/aN, aN);
        tOut.fill(rho -> mURSpline[aType1-1][aType2-1].subsGrad(rho));
        return tOut;
    }
    @ApiStatus.Experimental
    public @Nullable IFunc1 wr(int aType1, int aType2) {
        if (mWR == null) return null;
        IFunc1 tOut = ConstBoundFunc1.zeros(0, mDR, mNR);
        tOut.fill(mWR[aType1-1][aType2-1]);
        return tOut;
    }
    @ApiStatus.Experimental
    public @Nullable IFunc1 wrSpline(int aType1, int aType2, int aN) {
        if (mWRSpline == null) return null;
        IFunc1 tOut = ConstBoundFunc1.zeros(0, (mDR*mNR)/aN, aN);
        tOut.fill(rho -> mWRSpline[aType1-1][aType2-1].subs(rho));
        return tOut;
    }
    @ApiStatus.Experimental
    public @Nullable IFunc1 wrGradSpline(int aType1, int aType2, int aN) {
        if (mWRSpline == null) return null;
        IFunc1 tOut = ConstBoundFunc1.zeros(0, (mDR*mNR)/aN, aN);
        tOut.fill(rho -> mWRSpline[aType1-1][aType2-1].subsGrad(rho));
        return tOut;
    }
    
    private static IVector readData_(BufferedReader aReader, int aSize) throws IOException {
        Vector rOut = Vectors.NaN(aSize);
        for (int k = 0; k < aSize;) {
            String[] tTokens = IO.Text.splitBlank(aReader.readLine());
            for (String tToken : tTokens) {
                rOut.set(k, Double.parseDouble(tToken));
                ++k;
            }
        }
        return rOut;
    }
    
    /**
     * 输出成 lammps 支持的 eam 势函数文件
     * @param aFilePath 需要输出的路径
     * @param aFormat 可选的 EAM 势函数文件格式，可选 {@code "eam", "alloy", "fs", "adp"}，默认根据后缀名自动检测
     * @throws IOException 如果写入文件失败
     */
    public void write(String aFilePath, @Nullable String aFormat) throws IOException {
        if (aFormat == null) {
            if (aFilePath.endsWith(".eam")) {
                aFormat = "eam";
            } else
            if (aFilePath.endsWith(".alloy")) {
                aFormat = "alloy";
            } else
            if (aFilePath.endsWith(".fs")) {
                aFormat = "fs";
            } else
            if (aFilePath.endsWith(".adp")) {
                aFormat = "adp";
            } else {
                throw new IllegalArgumentException("Unsupported EAM format: " + aFilePath);
            }
        }
        try (IO.IWriteln tWriteln = IO.toWriteln(aFilePath)) {write(tWriteln, aFormat);}
    }
    /**
     * 输出成 lammps 支持的 eam 势函数文件
     * @param aFilePath 需要输出的路径
     * @throws IOException 如果写入文件失败
     */
    public void write(String aFilePath) throws IOException {
        write(aFilePath, null);
    }
    /** 提供 {@link IO.IWriteln} 的接口来实现边写入边处理，此方法不会自动关闭流 */
    public void write(IO.IWriteln aWriteln, String aFormat) throws IOException {
        String[] tHeaders = mHeader.split("\n");
        String tDNCut = mNRho+" "+mDRho+" "+mNR+" "+mDR+" "+mCut;
        switch(aFormat) {
        case "eam": {
            if (mTypeNum != 1) throw new IllegalStateException();
            aWriteln.writeln(tHeaders.length<1 ? "" : tHeaders[0]);
            aWriteln.writeln(mAtomicNumbers[0]+" "+mMasses[0]+" "+mLatticeConsts[0]+" "+mLatticeTypes[0]);
            aWriteln.writeln(tDNCut);
            for (int i = 0; i < mNRho; ++i) {
            aWriteln.writeln(String.valueOf(mFRho[0].get(i)));
            }
            for (int i = 0; i < mNR; ++i) {
            aWriteln.writeln(String.valueOf(Math.sqrt(mRPhiR[0][0].get(i)/EAM_MUL)));
            }
            for (int i = 0; i < mNR; ++i) {
            aWriteln.writeln(String.valueOf(mRhoR[0][0].get(i)));
            }
            break;
        }
        case "alloy": case "fs": case "adp": {
            boolean tIsFs = aFormat.equals("fs");
            boolean tIsAdp = aFormat.equals("adp");
            if (!tIsFs && mRhoR.length!=1) throw new IllegalStateException();
            if (!tIsAdp && (mUR!=null || mWR!=null)) throw new IllegalStateException();
            aWriteln.writeln(tHeaders.length<1 ? "" : tHeaders[0]);
            aWriteln.writeln(tHeaders.length<2 ? "" : tHeaders[1]);
            aWriteln.writeln(tHeaders.length<3 ? "" : tHeaders[2]);
            aWriteln.writeln(mTypeNum+" "+String.join(" ", mSymbols));
            aWriteln.writeln(tDNCut);
            for (int i = 0; i < mTypeNum; ++i) {
                aWriteln.writeln(mAtomicNumbers[i]+" "+mMasses[i]+" "+mLatticeConsts[i]+" "+mLatticeTypes[i]);
                for (int k = 0; k < mNRho; ++k) {
                aWriteln.writeln(String.valueOf(mFRho[i].get(k)));
                }
                if (tIsFs) {
                    for (int j = 0; j < mTypeNum; ++j) {
                        for (int k = 0; k < mNR; ++k) {
                        aWriteln.writeln(String.valueOf(mRhoR[mRhoR.length==1?0:j][i].get(k)));
                        }
                    }
                } else {
                    for (int k = 0; k < mNR; ++k) {
                    aWriteln.writeln(String.valueOf(mRhoR[0][i].get(k)));
                    }
                }
            }
            for (int i = 0; i < mTypeNum; ++i) for (int j = 0; j <= i; ++j) {
                for (int k = 0; k < mNR; ++k) {
                aWriteln.writeln(String.valueOf(mRPhiR[i][j].get(k)));
                }
            }
            if (!tIsAdp) break;
            for (int i = 0; i < mTypeNum; ++i) for (int j = 0; j <= i; ++j) {
                for (int k = 0; k < mNR; ++k) {
                aWriteln.writeln(mUR==null ? "0.0" : String.valueOf(mUR[i][j].get(k)));
                }
            }
            for (int i = 0; i < mTypeNum; ++i) for (int j = 0; j <= i; ++j) {
                for (int k = 0; k < mNR; ++k) {
                aWriteln.writeln(mWR==null ? "0.0" : String.valueOf(mWR[i][j].get(k)));
                }
            }
            break;
        }
        default: {
            throw new IllegalArgumentException("Invalid EAM format: " + aFormat);
        }}
    }
    
    /** @return {@inheritDoc} */
    @Override public int ntypes() {return mTypeNum;}
    /** @return {@inheritDoc} */
    @Override public boolean hasSymbol() {return true;}
    /**
     * {@inheritDoc}
     * @param aType {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override public @NotNull String symbol(int aType) {return mSymbols[aType-1];}
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override public double rcutMax() {return mCut;}
    
    
    @ApiStatus.Experimental @Override
    public double calEnergySingle(int aThreadID, int aCType,
                                  DoubleList aNlDx, DoubleList aNlDy, DoubleList aNlDz, IntList aNlType) {
        checkType(aCType);
        double rho = 0.0;
        double mx = 0.0, my = 0.0, mz = 0.0;
        double lxx = 0.0, lyy = 0.0, lzz = 0.0;
        double lxy = 0.0, lxz = 0.0, lyz = 0.0;
        double tEng = 0.0;
        final int tNlSize = aNlDx.size();
        for (int jj = 0; jj < tNlSize; ++jj) {
            int type = aNlType.get(jj);
            double dx = aNlDx.get(jj);
            double dy = aNlDy.get(jj);
            double dz = aNlDz.get(jj);
            double rsq = dx*dx + dy*dy + dz*dz;
            if (rsq >= mCutsq) continue;
            double r = Math.sqrt(rsq);
            double deng = mRPhiRSpline[aCType-1][type-1].subs(r) / r;
            tEng += deng*0.5;
            rho += mRhoRSpline[mRhoR.length==1?0:(aCType-1)][type-1].subs(r);
            if (mUR != null) {
                assert mURSpline != null;
                double u = mURSpline[aCType-1][type-1].subs(r);
                mx += u*dx; my += u*dy; mz += u*dz;
            }
            if (mWR != null) {
                assert mWRSpline != null;
                double w = mWRSpline[aCType-1][type-1].subs(r);
                lxx += w*dx*dx; lyy += w*dy*dy; lzz += w*dz*dz;
                lxy += w*dx*dy; lxz += w*dx*dz; lyz += w*dy*dz;
            }
        }
        double deng = mFRhoSpline[aCType-1].subs(rho);
        if (mUR != null) {
            deng += 0.5 * (mx*mx + my*my + mz*mz);
        }
        if (mWR != null) {
            deng += 0.5 * (lxx*lxx + lyy*lyy + lzz*lzz);
            deng += (lxy*lxy + lxz*lxz + lyz*lyz);
            double nu = lxx + lyy + lzz;
            deng -= (1.0/6.0) * nu*nu;
        }
        tEng += deng;
        return tEng;
    }
    @ApiStatus.Experimental @Override
    public double calEnergyForceSingle(int aThreadID, int aCType,
                                       DoubleList aNlDx, DoubleList aNlDy, DoubleList aNlDz, IntList aNlType,
                                       DoubleList rGradNlDx, DoubleList rGradNlDy, DoubleList rGradNlDz) {
        // 旧的 lammps/类lammps 实现存在一些问题，这里改为反向传播的方式来重新实现
        checkType(aCType);
        double rho = 0.0;
        double mx = 0.0, my = 0.0, mz = 0.0;
        double lxx = 0.0, lyy = 0.0, lzz = 0.0;
        double lxy = 0.0, lxz = 0.0, lyz = 0.0;
        double gmx = 0.0, gmy = 0.0, gmz = 0.0;
        double glxx = 0.0, glyy = 0.0, glzz = 0.0;
        double glxy = 0.0, glxz = 0.0, glyz = 0.0;
        double tEng = 0.0;
        final int tNlSize = aNlDx.size();
        for (int jj = 0; jj < tNlSize; ++jj) {
            int type = aNlType.get(jj);
            double dx = aNlDx.get(jj);
            double dy = aNlDy.get(jj);
            double dz = aNlDz.get(jj);
            double rsq = dx*dx + dy*dy + dz*dz;
            if (rsq >= mCutsq) continue;
            double r = Math.sqrt(rsq);
            rho += mRhoRSpline[mRhoR.length==1?0:(aCType-1)][type-1].subs(r);
            if (mUR != null) {
                assert mURSpline != null;
                double u = mURSpline[aCType-1][type-1].subs(r);
                mx += u*dx; my += u*dy; mz += u*dz;
            }
            if (mWR != null) {
                assert mWRSpline != null;
                double w = mWRSpline[aCType-1][type-1].subs(r);
                lxx += w*dx*dx; lyy += w*dy*dy; lzz += w*dz*dz;
                lxy += w*dx*dy; lxz += w*dx*dz; lyz += w*dy*dz;
            }
        }
        tEng += mFRhoSpline[aCType-1].subs(rho);
        double grho = mFRhoSpline[aCType-1].subsGrad(rho);
        if (mUR != null) {
            tEng += 0.5 * (mx*mx + my*my + mz*mz);
            gmx = mx; gmy = my; gmz = mz;
        }
        if (mWR != null) {
            tEng += 0.5 * (lxx*lxx + lyy*lyy + lzz*lzz);
            tEng += (lxy*lxy + lxz*lxz + lyz*lyz);
            double nu = lxx + lyy + lzz;
            tEng -= (1.0/6.0) * nu*nu;
            double gnu = (-1.0/3.0) * nu;
            glxx = lxx + gnu; glyy = lyy + gnu; glzz = lzz + gnu;
            glxy = lxy + lxy; glxz = lxz + lxz; glyz = lyz + lyz;
        }
        // backward nl
        for (int jj = 0; jj < tNlSize; ++jj) {
            int type = aNlType.get(jj);
            double dx = aNlDx.get(jj);
            double dy = aNlDy.get(jj);
            double dz = aNlDz.get(jj);
            double rsq = dx*dx + dy*dy + dz*dz;
            if (rsq >= mCutsq) continue;
            double r = Math.sqrt(rsq);
            double rinv = 1.0/r;
            double r2inv = rinv*rinv;
            double gdx = 0.0, gdy = 0.0, gdz = 0.0;
            double gdr = 0.0;
            if (mUR != null) {
                assert mURSpline != null;
                double u = mURSpline[aCType-1][type-1].subs(r);
                gdx += gmx*u;
                gdy += gmy*u;
                gdz += gmz*u;
                double gu = gmx*dx + gmy*dy + gmz*dz;
                gdr += gu*mURSpline[aCType-1][type-1].subsGrad(r)*rinv;
            }
            if (mWR != null) {
                assert mWRSpline != null;
                double w = mWRSpline[aCType-1][type-1].subs(r);
                gdx += w*(glxx*2.0*dx + glxy*dy + glxz*dz);
                gdy += w*(glyy*2.0*dy + glxy*dx + glyz*dz);
                gdz += w*(glzz*2.0*dz + glxz*dx + glyz*dy);
                double gw = glxx*dx*dx + glyy*dy*dy + glzz*dz*dz + glxy*dx*dy + glxz*dx*dz + glyz*dy*dz;
                gdr += gw*mWRSpline[aCType-1][type-1].subsGrad(r)*rinv;
            }
            gdr += grho*mRhoRSpline[mRhoR.length==1?0:(aCType-1)][type-1].subsGrad(r)*rinv;
            double phi = mRPhiRSpline[aCType-1][type-1].subs(r)*rinv;
            tEng += 0.5*phi;
            gdr += 0.5*mRPhiRSpline[aCType-1][type-1].subsGrad(r)*r2inv;
            gdr -= 0.5*phi*r2inv;
            gdx += gdr*dx;
            gdy += gdr*dy;
            gdz += gdr*dz;
            rGradNlDx.set(jj, gdx);
            rGradNlDy.set(jj, gdy);
            rGradNlDz.set(jj, gdz);
        }
        return tEng;
    }
}
