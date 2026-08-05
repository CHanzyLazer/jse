package jse.atom;

import jse.code.collection.DoubleList;
import jse.code.collection.IntList;
import jse.math.MathEX;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * jse 中的近邻列表实现，具体采用了 Neighbor Cell List (NCL)
 * 算法来实现一个 O(N) 复杂度的近邻搜索，具体可以参考 Julia 中的例子：
 * <a href="https://jaantollander.com/post/searching-for-fixed-radius-near-neighbors-with-cell-lists-algorithm-in-julia-language/">
 * Searching for Fixed-Radius Near Neighbors with Cell Lists Algorithm in Julia Language </a>
 * <p>
 * 相比旧版更加高效，缓存 ghost 从而避免每次获取时重复计算来大幅加速
 * <p>
 * 目前统一认为边界条件为 ppp
 * <p>
 * 现在此类不再提供冗余的线程安全支持，并一次性只维护一种 rcut，不同实例之间线程安全
 * @author liqa
 */
@ApiStatus.Experimental
public class NeighborListGetter2 {
    public final static int MAX_SLICE = 256;
    
    private boolean mValid = false;
    private final IntList mIdx;
    private final DoubleList mPosX, mPosY, mPosZ;
    private final List<IntList> mCells;
    private int mSliceX = 0, mSliceY = 0, mSliceZ = 0;
    private int mNumAtoms = -1;
    private int mNumGhost = -1;
    
    private boolean mPrism;
    private final XYZ mA = new XYZ(), mB = new XYZ(), mC = new XYZ();
    private final XYZ mBC = new XYZ(), mCA = new XYZ(), mAB = new XYZ();
    private double mBCNorm = Double.NaN, mCANorm = Double.NaN, mABNorm = Double.NaN;
    private double mVolume = Double.NaN;
    private final XYZ mPlaneXYZ = new XYZ(); // 表示三个方向的模拟盒平面之间的距离，用于确定 cell 需要分划的份数
    private final XYZ mBuf = new XYZ();
    
    private double mRCut = Double.NaN, mRCutSq = Double.NaN;
    
    
    public NeighborListGetter2() {
        mIdx = new IntList();
        mPosX = new DoubleList();
        mPosY = new DoubleList();
        mPosZ = new DoubleList();
        mCells = new ArrayList<>();
    }
    public NeighborListGetter2(IAtomData aData, double aRCut) {
        this();
        setData(aData).setRCut(aRCut).initCell();
    }
    
    
    public NeighborListGetter2 setRCut(double aRCut) {
        if (aRCut <= 0.0) throw new IllegalArgumentException("rcut MUST > 0.0, input: "+aRCut);
        mValid = false;
        mRCut = aRCut;
        mRCutSq = aRCut*aRCut;
        return this;
    }
    
    public NeighborListGetter2 setData(IAtomData aData) {
        mValid = false;
        mNumAtoms = aData.natoms();
        IBox tBox = aData.box();
        mPrism = tBox.isPrism();
        mA.setXYZ(tBox.a());
        mB.setXYZ(tBox.b());
        mC.setXYZ(tBox.c());
        mVolume = mA.mixed(mB, mC);
        if (mPrism) {
            mB.cross2dest(mC, mBC);
            mC.cross2dest(mA, mCA);
            mA.cross2dest(mB, mAB);
            mBCNorm = mBC.norm();
            mCANorm = mCA.norm();
            mABNorm = mAB.norm();
            mPlaneXYZ.setXYZ(
                mA.dot(mBC) / mBCNorm,
                mB.dot(mCA) / mCANorm,
                mC.dot(mAB) / mABNorm
            );
        } else {
            mBC.setXYZ(Double.NaN, Double.NaN, Double.NaN);
            mCA.setXYZ(Double.NaN, Double.NaN, Double.NaN);
            mAB.setXYZ(Double.NaN, Double.NaN, Double.NaN);
            mBCNorm = Double.NaN;
            mCANorm = Double.NaN;
            mABNorm = Double.NaN;
            mPlaneXYZ.setXYZ(mA.mX, mB.mY, mC.mZ);
        }
        mIdx.clear(); mIdx.ensureCapacity(mNumAtoms);
        mPosX.clear(); mPosX.ensureCapacity(mNumAtoms);
        mPosY.clear(); mPosY.ensureCapacity(mNumAtoms);
        mPosZ.clear(); mPosZ.ensureCapacity(mNumAtoms);
        for (int i = 0; i < mNumAtoms; ++i) {
            IAtom tAtom = aData.atom(i);
            mIdx.add(i);
            mBuf.setXYZ(tAtom.x(), tAtom.y(), tAtom.z());
            wrapPBC(mBuf);
            mPosX.add(mBuf.mX);
            mPosY.add(mBuf.mY);
            mPosZ.add(mBuf.mZ);
        }
        return this;
    }
    
    void initCell() {
        if (mValid) return;
        // 简单检查合法性
        if (Double.isNaN(mRCut)) {
            throw new IllegalStateException("Need `setRCut` first");
        }
        if (mNumAtoms < 0) {
            throw new IllegalStateException("Need `setData` first");
        }
        // 简单估算带上 ghost 后的大小，无所谓精度
        final int tNumInit = MathEX.Code.ceil2int(mNumAtoms * MathEX.Fast.pow3(MathEX.Fast.cbrt(mVolume)+mRCut+mRCut)/mVolume * 1.25);
        mIdx.setInternalDataSize(mNumAtoms); mIdx.ensureCapacity(tNumInit);
        mPosX.setInternalDataSize(mNumAtoms); mPosX.ensureCapacity(tNumInit);
        mPosY.setInternalDataSize(mNumAtoms); mPosY.ensureCapacity(tNumInit);
        mPosZ.setInternalDataSize(mNumAtoms); mPosZ.ensureCapacity(tNumInit);
        // 确定分划份数
        mSliceX = MathEX.Code.toRange(1, MAX_SLICE, MathEX.Code.floor2int(mPlaneXYZ.mX/mRCut));
        mSliceY = MathEX.Code.toRange(1, MAX_SLICE, MathEX.Code.floor2int(mPlaneXYZ.mY/mRCut));
        mSliceZ = MathEX.Code.toRange(1, MAX_SLICE, MathEX.Code.floor2int(mPlaneXYZ.mZ/mRCut));
        // 参数初始化
        final int tCellCount = (mSliceX+2)*(mSliceY+2)*(mSliceZ+2);
        final int tCellSizeInit = MathEX.Code.ceil2int(mNumAtoms / (double)(mSliceX*mSliceY*mSliceZ) * 1.25);
        for (IntList tCell : mCells) {
            tCell.clear();
            tCell.ensureCapacity(tCellSizeInit);
        }
        while (mCells.size() < tCellCount) {
            mCells.add(new IntList(tCellSizeInit));
        }
        // 先构建中心的 cell
        if (mPrism) {
            for (int i = 0; i < mNumAtoms; ++i) {
                mBuf.setXYZ(mPosX.get(i), mPosY.get(i), mPosZ.get(i));
                toDirect(mBuf);
                int ci = MathEX.Code.floor2int(mBuf.mX * mSliceX);
                int cj = MathEX.Code.floor2int(mBuf.mY * mSliceY);
                int ck = MathEX.Code.floor2int(mBuf.mZ * mSliceZ);
                cell(ci, cj, ck, true).add(i);
            }
        } else {
            mBuf.setXYZ(mSliceX/mA.mX, mSliceY/mB.mY, mSliceZ/mC.mZ);
            for (int idx = 0; idx < mNumAtoms; ++idx) {
                int ci = MathEX.Code.floor2int(mPosX.get(idx) * mBuf.mX);
                int cj = MathEX.Code.floor2int(mPosY.get(idx) * mBuf.mY);
                int ck = MathEX.Code.floor2int(mPosZ.get(idx) * mBuf.mZ);
                cell(ci, cj, ck, true).add(idx);
            }
        }
        // 添加 ghost 原子，这里使用遍历的方式实现
        mNumGhost = 0;
        for (int ck0 = 0; ck0 < mSliceZ; ++ck0) for (int cj0 = 0; cj0 < mSliceY; ++cj0) for (int ci0 = 0; ci0 < mSliceX; ++ci0) {
            // 只考虑最外围的 cell 会存在 ghost
            if (ci0>0 && ci0<(mSliceX-1) && cj0>0 && cj0<(mSliceY-1) && ck0>0 && ck0<(mSliceZ-1)) continue;
            final IntList tCell0 = cell(ci0, cj0, ck0, true);
            final int tCellSize0 = tCell0.size();
            // 总是有 26 个可能方向需要增加 ghost，并包括可能的扩胞，这里遍历实现
            final int tRepX = MathEX.Code.ceil2int(mRCut/mPlaneXYZ.mX);
            final int tRepY = MathEX.Code.ceil2int(mRCut/mPlaneXYZ.mY);
            final int tRepZ = MathEX.Code.ceil2int(mRCut/mPlaneXYZ.mZ);
            for (int rk = -tRepZ; rk <= tRepZ; ++rk) for (int rj = -tRepY; rj <= tRepY; ++rj) for (int ri = -tRepX; ri <= tRepX; ++ri) {
                // 注意排除自身
                if (ri==0 && rj==0 && rk==0) continue;
                // 简单的方向性判断排除不需要遍历的 cell
                if ((ri>0 && ci0!=0) || (ri<0 && ci0!=(mSliceX-1))) continue;
                if ((rj>0 && cj0!=0) || (rj<0 && cj0!=(mSliceY-1))) continue;
                if ((rk>0 && ck0!=0) || (rk<0 && ck0!=(mSliceZ-1))) continue;
                    
                int ci = ri==0 ? ci0 : (ri>0 ? ci0+mSliceX : ci0-mSliceX);
                int cj = rj==0 ? cj0 : (rj>0 ? cj0+mSliceY : cj0-mSliceY);
                int ck = rk==0 ? ck0 : (rk>0 ? ck0+mSliceZ : ck0-mSliceZ);
                IntList tCellG = cell(ci, cj, ck);
                
                for (int ii = 0; ii < tCellSize0; ++ii) {
                    final int i = tCell0.get(ii);
                    mBuf.setXYZ(mPosX.get(i), mPosY.get(i), mPosZ.get(i));
                    if (mPrism) {
                        mBuf.mplus2this(mA, ri);
                        mBuf.mplus2this(mB, rj);
                        mBuf.mplus2this(mC, rk);
                        if ((ri>0 ? ((mBuf.dot(mBC)/mBCNorm - mPlaneXYZ.mX) >= mRCut) : ((-mBuf.dot(mBC)/mBCNorm) >= mRCut)) ||
                            (rj>0 ? ((mBuf.dot(mCA)/mCANorm - mPlaneXYZ.mY) >= mRCut) : ((-mBuf.dot(mCA)/mCANorm) >= mRCut)) ||
                            (rk>0 ? ((mBuf.dot(mAB)/mABNorm - mPlaneXYZ.mZ) >= mRCut) : ((-mBuf.dot(mAB)/mABNorm) >= mRCut))) {
                            continue;
                        }
                    } else {
                        mBuf.mX += ri * mA.mX;
                        mBuf.mY += rj * mB.mY;
                        mBuf.mZ += rk * mC.mZ;
                        if ((ri>0 ? ((mBuf.mX-mPlaneXYZ.mX) >= mRCut) : ((-mBuf.mX) >= mRCut)) ||
                            (rj>0 ? ((mBuf.mY-mPlaneXYZ.mY) >= mRCut) : ((-mBuf.mY) >= mRCut)) ||
                            (rk>0 ? ((mBuf.mZ-mPlaneXYZ.mZ) >= mRCut) : ((-mBuf.mZ) >= mRCut))) {
                            continue;
                        }
                    }
                    tCellG.add(mIdx.size());
                    mIdx.add(mIdx.get(i));
                    mPosX.add(mBuf.mX);
                    mPosY.add(mBuf.mY);
                    mPosZ.add(mBuf.mZ);
                    ++mNumGhost;
                }
            }
        }
        mValid = true;
    }
    
    
    void wrapPBC(XYZ rXYZ) {
        toDirect(rXYZ);
        if (rXYZ.mX<0.0 || rXYZ.mX>=1.0) {rXYZ.mX -= MathEX.Code.floor(rXYZ.mX);}
        if (rXYZ.mY<0.0 || rXYZ.mY>=1.0) {rXYZ.mY -= MathEX.Code.floor(rXYZ.mY);}
        if (rXYZ.mZ<0.0 || rXYZ.mZ>=1.0) {rXYZ.mZ -= MathEX.Code.floor(rXYZ.mZ);}
        toCartesian(rXYZ);
    }
    void toCartesian(XYZ rDirect) {
        if (mPrism) {
            rDirect.setXYZ(
                mA.mX*rDirect.mX + mB.mX*rDirect.mY + mC.mX*rDirect.mZ,
                mA.mY*rDirect.mX + mB.mY*rDirect.mY + mC.mY*rDirect.mZ,
                mA.mZ*rDirect.mX + mB.mZ*rDirect.mY + mC.mZ*rDirect.mZ
            );
        } else {
            rDirect.multiply2this(mA.mX, mB.mY, mC.mZ);
        }
    }
    void toDirect(XYZ rCartesian) {
        if (mPrism) {
            rCartesian.setXYZ(
                mBC.dot(rCartesian) / mVolume,
                mCA.dot(rCartesian) / mVolume,
                mAB.dot(rCartesian) / mVolume
            );
        } else {
            rCartesian.div2this(mA.mX, mB.mY, mC.mZ);
        }
        // direct 需要考虑计算误差带来的出边界的问题，现在支持自动靠近所有整数值
        int tIntX = MathEX.Code.round2int(rCartesian.mX);
        if (Math.abs(rCartesian.mX-tIntX) < MathEX.Code.DBL_EPSILON) rCartesian.mX = tIntX;
        int tIntY = MathEX.Code.round2int(rCartesian.mY);
        if (Math.abs(rCartesian.mY-tIntY) < MathEX.Code.DBL_EPSILON) rCartesian.mY = tIntY;
        int tIntZ = MathEX.Code.round2int(rCartesian.mZ);
        if (Math.abs(rCartesian.mZ-tIntZ) < MathEX.Code.DBL_EPSILON) rCartesian.mZ = tIntZ;
    }
    
    IntList cell(int ci, int cj, int ck) {
        return cell(ci, cj, ck, false);
    }
    IntList cell(int ci, int cj, int ck, boolean in) {
        if (in) {
            if (ci<0 || ci>=mSliceX || cj<0 || cj>=mSliceY || ck<0 || ck>=mSliceZ) {
                throw new IndexOutOfBoundsException(String.format("Index: (%d, %d, %d)", ci, cj, ck));
            }
        } else {
            if (ci<-1 || ci>mSliceX || cj<-1 || cj>mSliceY || ck<-1 || ck>mSliceZ) {
                throw new IndexOutOfBoundsException(String.format("Index: (%d, %d, %d)", ci, cj, ck));
            }
        }
        return mCells.get((ci+1) + (mSliceX+2)*(cj+1) + (mSliceX+2)*(mSliceY+2)*(ck+1));
    }
    
    
    public int natoms() {
        return mNumAtoms;
    }
    public int nghost() {
        return mNumGhost;
    }
    public double rcut() {
        return mRCut;
    }
    
    @FunctionalInterface public interface IDxyzIdxDo {void run(double aDx, double aDy, double aDz, int aIdx);}
    
    void forEachCell(int index, double x0, double y0, double z0, int ci, int cj, int ck, boolean in, boolean aCheck, IDxyzIdxDo aDxyzIdxDo) {
        IntList tCell = cell(ci, cj, ck, in);
        final int tCellSize = tCell.size();
        for (int ii = 0; ii < tCellSize; ++ii) {
            int i = tCell.get(ii);
            if (index>=0 && i ==index) continue;
            double dx = mPosX.get(i) - x0;
            double dy = mPosY.get(i) - y0;
            double dz = mPosZ.get(i) - z0;
            if (aCheck) {
                double rsq = dx*dx + dy*dy + dz*dz;
                if (rsq < mRCutSq) aDxyzIdxDo.run(dx, dy, dz, mIdx.get(i));
            } else {
                aDxyzIdxDo.run(dx, dy, dz, mIdx.get(i));
            }
        }
    }
    void forEachCell(double x0, double y0, double z0, int ci, int cj, int ck, boolean aCheck, IDxyzIdxDo aDxyzIdxDo) {
        forEachCell(-1, x0, y0, z0, ci, cj, ck, false, aCheck, aDxyzIdxDo);
    }
    
    public void forEachNeighbor(int aIndex, boolean aCheck, IDxyzIdxDo aDxyzIdxDo) {
        initCell();
        final double x0 = mPosX.get(aIndex);
        final double y0 = mPosY.get(aIndex);
        final double z0 = mPosZ.get(aIndex);
        final int ci, cj, ck;
        if (mPrism) {
            mBuf.setXYZ(x0, y0, z0);
            toDirect(mBuf);
            ci = MathEX.Code.floor2int(mBuf.mX*mSliceX);
            cj = MathEX.Code.floor2int(mBuf.mY*mSliceY);
            ck = MathEX.Code.floor2int(mBuf.mZ*mSliceZ);
        } else {
            ci = MathEX.Code.floor2int(x0*mSliceX/mA.mX);
            cj = MathEX.Code.floor2int(y0*mSliceY/mB.mY);
            ck = MathEX.Code.floor2int(z0*mSliceZ/mC.mZ);
        }
        forEachCell(aIndex, x0, y0, z0, ci, cj, ck, true, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj  , ck+1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj  , ck-1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj+1, ck  , aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj+1, ck+1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj+1, ck-1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj-1, ck  , aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj-1, ck+1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj-1, ck-1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj  , ck  , aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj  , ck+1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj  , ck-1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj+1, ck  , aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj+1, ck+1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj+1, ck-1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj-1, ck  , aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj-1, ck+1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj-1, ck-1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj  , ck  , aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj  , ck+1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj  , ck-1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj+1, ck  , aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj+1, ck+1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj+1, ck-1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj-1, ck  , aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj-1, ck+1, aCheck, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj-1, ck-1, aCheck, aDxyzIdxDo);
    }
    public void forEachNeighbor(int aIndex, IDxyzIdxDo aDxyzIdxDo) {
        forEachNeighbor(aIndex, true, aDxyzIdxDo);
    }
}
