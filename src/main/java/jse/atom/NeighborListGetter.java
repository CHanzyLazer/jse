package jse.atom;

import com.google.common.collect.Lists;
import jse.cache.DoubleArrayCache;
import jse.cache.IntArrayCache;
import jse.code.collection.DoubleList;
import jse.code.collection.IntList;
import jse.math.MathEX;
import jse.math.vector.IntVector;
import jse.math.vector.Vector;
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
 * 目前统一认为边界条件为 ppp，并一次性只维护一种 rcut
 * <p>
 * 设置和构建近邻列表过程线程不安全，而只读的获取近邻列表线程安全，而不同实例之间总是线程安全
 * @author liqa
 */
@ApiStatus.Experimental
public class NeighborListGetter {
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
    
    private double mRCut = Double.NaN, mRCutSq = Double.NaN;
    
    
    public NeighborListGetter() {
        mIdx = new IntList();
        mPosX = new DoubleList();
        mPosY = new DoubleList();
        mPosZ = new DoubleList();
        mCells = new ArrayList<>();
    }
    public NeighborListGetter(IAtomData aData, double aRCut) {
        this();
        setData(aData).setRCut(aRCut).build();
    }
    
    
    public NeighborListGetter setRCut(double aRCut) {
        if (aRCut <= 0.0) throw new IllegalArgumentException("rcut MUST > 0.0, input: "+aRCut);
        if (!Double.isNaN(mRCut) && MathEX.Code.numericEqual(mRCut, aRCut)) {
            return this;
        }
        mValid = false;
        mRCut = aRCut;
        mRCutSq = aRCut*aRCut;
        return this;
    }
    
    public NeighborListGetter setData(IAtomData aData) {
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
        final XYZ tBuf = new XYZ();
        for (int i = 0; i < mNumAtoms; ++i) {
            IAtom tAtom = aData.atom(i);
            mIdx.add(i);
            tBuf.setXYZ(tAtom.x(), tAtom.y(), tAtom.z());
            wrapPBC(tBuf);
            mPosX.add(tBuf.mX);
            mPosY.add(tBuf.mY);
            mPosZ.add(tBuf.mZ);
        }
        return this;
    }
    
    public void build() {
        build(false);
    }
    public void build(boolean aForce) {
        if (!aForce && mValid) return;
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
        final XYZ tBuf = new XYZ();
        if (mPrism) {
            for (int i = 0; i < mNumAtoms; ++i) {
                tBuf.setXYZ(mPosX.get(i), mPosY.get(i), mPosZ.get(i));
                toDirect(tBuf);
                int ci = MathEX.Code.floor2int(tBuf.mX * mSliceX);
                int cj = MathEX.Code.floor2int(tBuf.mY * mSliceY);
                int ck = MathEX.Code.floor2int(tBuf.mZ * mSliceZ);
                cell(ci, cj, ck, true).add(i);
            }
        } else {
            tBuf.setXYZ(mSliceX/mA.mX, mSliceY/mB.mY, mSliceZ/mC.mZ);
            for (int i = 0; i < mNumAtoms; ++i) {
                int ci = MathEX.Code.floor2int(mPosX.get(i) * tBuf.mX);
                int cj = MathEX.Code.floor2int(mPosY.get(i) * tBuf.mY);
                int ck = MathEX.Code.floor2int(mPosZ.get(i) * tBuf.mZ);
                cell(ci, cj, ck, true).add(i);
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
                    tBuf.setXYZ(mPosX.get(i), mPosY.get(i), mPosZ.get(i));
                    if (mPrism) {
                        tBuf.mplus2this(mA, ri);
                        tBuf.mplus2this(mB, rj);
                        tBuf.mplus2this(mC, rk);
                        if ((ri>0 ? ((tBuf.dot(mBC)/mBCNorm - mPlaneXYZ.mX) >= mRCut) : ((-tBuf.dot(mBC)/mBCNorm) >= mRCut)) ||
                            (rj>0 ? ((tBuf.dot(mCA)/mCANorm - mPlaneXYZ.mY) >= mRCut) : ((-tBuf.dot(mCA)/mCANorm) >= mRCut)) ||
                            (rk>0 ? ((tBuf.dot(mAB)/mABNorm - mPlaneXYZ.mZ) >= mRCut) : ((-tBuf.dot(mAB)/mABNorm) >= mRCut))) {
                            continue;
                        }
                    } else {
                        tBuf.mX += ri * mA.mX;
                        tBuf.mY += rj * mB.mY;
                        tBuf.mZ += rk * mC.mZ;
                        if ((ri>0 ? ((tBuf.mX-mPlaneXYZ.mX) >= mRCut) : ((-tBuf.mX) >= mRCut)) ||
                            (rj>0 ? ((tBuf.mY-mPlaneXYZ.mY) >= mRCut) : ((-tBuf.mY) >= mRCut)) ||
                            (rk>0 ? ((tBuf.mZ-mPlaneXYZ.mZ) >= mRCut) : ((-tBuf.mZ) >= mRCut))) {
                            continue;
                        }
                    }
                    tCellG.add(mIdx.size());
                    mIdx.add(mIdx.get(i));
                    mPosX.add(tBuf.mX);
                    mPosY.add(tBuf.mY);
                    mPosZ.add(tBuf.mZ);
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
    
    public IntList cell(int ci, int cj, int ck) {
        return cell(ci, cj, ck, false);
    }
    public IntList cell(int ci, int cj, int ck, boolean in) {
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
    public int sliceX() {
        return mSliceX;
    }
    public int sliceY() {
        return mSliceY;
    }
    public int sliceZ() {
        return mSliceZ;
    }
    
    public boolean isPrism() {
        return mPrism;
    }
    public XYZ a() {
        return mA;
    }
    public XYZ b() {
        return mB;
    }
    public XYZ c() {
        return mC;
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
    public DoubleList posX() {
        return mPosX;
    }
    public DoubleList posY() {
        return mPosY;
    }
    public DoubleList posZ() {
        return mPosZ;
    }
    public IntList index() {
        return mIdx;
    }
    
    @FunctionalInterface public interface IDxyzIdxDo {void run(double aDx, double aDy, double aDz, int aIdx);}
    
    // 限制最近邻数目的近邻构建，现在使用简单的遍历方式来实现
    private static class NearestNeighborList implements AutoCloseable {
        private final double[] mDx, mDy, mDz, mRsq;
        private final int[] mIdx;
        private final int mNnn;
        private int mSize;
        private int mMaxIdx;
        private double mMaxRsq;
        
        NearestNeighborList(int aNnn) {
            if (aNnn <= 0) throw new IllegalStateException();
            mSize = 0;
            mNnn = aNnn;
            mDx = DoubleArrayCache.getArray(aNnn);
            mDy = DoubleArrayCache.getArray(aNnn);
            mDz = DoubleArrayCache.getArray(aNnn);
            mRsq = DoubleArrayCache.getArray(aNnn);
            mIdx = IntArrayCache.getArray(aNnn);
            mMaxIdx = -1;
            mMaxRsq = Double.NEGATIVE_INFINITY;
        }
        @Override public void close() {
            IntArrayCache.returnArray(mIdx);
            DoubleArrayCache.returnArray(mRsq);
            DoubleArrayCache.returnArray(mDz);
            DoubleArrayCache.returnArray(mDy);
            DoubleArrayCache.returnArray(mDx);
        }
        
        void put(double aDx, double aDy, double aDz, int aIdx) {
            double tRsq = aDx*aDx + aDy*aDy + aDz*aDz;
            // 没达到容量直接添加
            if (mSize < mNnn) {
                mDx[mSize] = aDx; mDy[mSize] = aDy; mDz[mSize] = aDz;
                mRsq[mSize] = tRsq;
                mIdx[mSize] = aIdx;
                if (tRsq > mMaxRsq) {
                    mMaxRsq = tRsq;
                    mMaxIdx = mSize;
                }
                ++mSize;
                return;
            }
            // 超过容量了替换掉最远的
            if (tRsq >= mMaxRsq) return;
            mDx[mMaxIdx] = aDx; mDy[mMaxIdx] = aDy; mDz[mMaxIdx] = aDz;
            mRsq[mMaxIdx] = tRsq;
            mIdx[mMaxIdx] = aIdx;
            // 遍历确定新的最远位置
            mMaxIdx = -1;
            mMaxRsq = Double.NEGATIVE_INFINITY;
            for (int ji = 0; ji < mNnn; ++ji) {
                double tDis = mRsq[ji];
                if (tDis > mMaxRsq) {
                    mMaxRsq = tDis;
                    mMaxIdx = ji;
                }
            }
        }
        void forEachNeighbor(IDxyzIdxDo aDxyzIdxDo) {
            for (int ji = 0; ji < mNnn; ++ji) {
                aDxyzIdxDo.run(mDx[ji], mDy[ji], mDz[ji], mIdx[ji]);
            }
        }
    }
    
    void forEachCell(int index, double x0, double y0, double z0, int ci, int cj, int ck, boolean in, boolean aHalf, IDxyzIdxDo aDxyzIdxDo) {
        IntList tCell = cell(ci, cj, ck, in);
        final int tCellSize = tCell.size();
        if (in) {
            if (aHalf) {
                for (int ji = 0; ji < tCellSize; ++ji) {
                    int j = tCell.get(ji);
                    if (j>=index) continue;
                    double dx = mPosX.get(j) - x0;
                    double dy = mPosY.get(j) - y0;
                    double dz = mPosZ.get(j) - z0;
                    double rsq = dx*dx + dy*dy + dz*dz;
                    if (rsq < mRCutSq) aDxyzIdxDo.run(dx, dy, dz, mIdx.get(j));
                }
            } else {
                for (int ji = 0; ji < tCellSize; ++ji) {
                    int j = tCell.get(ji);
                    if (j==index) continue;
                    double dx = mPosX.get(j) - x0;
                    double dy = mPosY.get(j) - y0;
                    double dz = mPosZ.get(j) - z0;
                    double rsq = dx*dx + dy*dy + dz*dz;
                    if (rsq < mRCutSq) aDxyzIdxDo.run(dx, dy, dz, mIdx.get(j));
                }
            }
        } else {
            if (aHalf) {
                final boolean tSkipHalfGhost = ci<0 || (ci<mSliceX && (cj<0 || cj<mSliceY && ck<0));
                for (int ji = 0; ji < tCellSize; ++ji) {
                    int j = tCell.get(ji);
                    int jdx = mIdx.get(j);
                    if (jdx>index || (jdx==index && tSkipHalfGhost)) continue;
                    double dx = mPosX.get(j) - x0;
                    double dy = mPosY.get(j) - y0;
                    double dz = mPosZ.get(j) - z0;
                    double rsq = dx*dx + dy*dy + dz*dz;
                    if (rsq < mRCutSq) aDxyzIdxDo.run(dx, dy, dz, jdx);
                }
            } else {
                for (int ji = 0; ji < tCellSize; ++ji) {
                    int j = tCell.get(ji);
                    double dx = mPosX.get(j) - x0;
                    double dy = mPosY.get(j) - y0;
                    double dz = mPosZ.get(j) - z0;
                    double rsq = dx*dx + dy*dy + dz*dz;
                    if (rsq < mRCutSq) aDxyzIdxDo.run(dx, dy, dz, mIdx.get(j));
                }
            }
        }
    }
    void forEachCell(double x0, double y0, double z0, int ci, int cj, int ck, boolean in, IDxyzIdxDo aDxyzIdxDo) {
        IntList tCell = cell(ci, cj, ck, in);
        final int tCellSize = tCell.size();
        for (int ji = 0; ji < tCellSize; ++ji) {
            int j = tCell.get(ji);
            double dx = mPosX.get(j) - x0;
            double dy = mPosY.get(j) - y0;
            double dz = mPosZ.get(j) - z0;
            double rsq = dx*dx + dy*dy + dz*dz;
            if (rsq < mRCutSq) aDxyzIdxDo.run(dx, dy, dz, mIdx.get(j));
        }
    }
    
    public void forEachNeighbor(int aIndex, boolean aHalf, IDxyzIdxDo aDxyzIdxDo) {
        if (!mValid) throw new IllegalStateException("Need `build` first");
        final double x0 = mPosX.get(aIndex);
        final double y0 = mPosY.get(aIndex);
        final double z0 = mPosZ.get(aIndex);
        final int ci, cj, ck;
        if (mPrism) {
            final XYZ tBuf = new XYZ();
            tBuf.setXYZ(x0, y0, z0);
            toDirect(tBuf);
            ci = MathEX.Code.floor2int(tBuf.mX*mSliceX);
            cj = MathEX.Code.floor2int(tBuf.mY*mSliceY);
            ck = MathEX.Code.floor2int(tBuf.mZ*mSliceZ);
        } else {
            ci = MathEX.Code.floor2int(x0*mSliceX/mA.mX);
            cj = MathEX.Code.floor2int(y0*mSliceY/mB.mY);
            ck = MathEX.Code.floor2int(z0*mSliceZ/mC.mZ);
        }
        forEachCell(aIndex, x0, y0, z0, ci  , cj  , ck  , true , aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci  , cj  , ck+1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci  , cj  , ck-1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci  , cj+1, ck  , false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci  , cj+1, ck+1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci  , cj+1, ck-1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci  , cj-1, ck  , false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci  , cj-1, ck+1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci  , cj-1, ck-1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci+1, cj  , ck  , false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci+1, cj  , ck+1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci+1, cj  , ck-1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci+1, cj+1, ck  , false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci+1, cj+1, ck+1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci+1, cj+1, ck-1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci+1, cj-1, ck  , false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci+1, cj-1, ck+1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci+1, cj-1, ck-1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci-1, cj  , ck  , false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci-1, cj  , ck+1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci-1, cj  , ck-1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci-1, cj+1, ck  , false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci-1, cj+1, ck+1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci-1, cj+1, ck-1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci-1, cj-1, ck  , false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci-1, cj-1, ck+1, false, aHalf, aDxyzIdxDo);
        forEachCell(aIndex, x0, y0, z0, ci-1, cj-1, ck-1, false, aHalf, aDxyzIdxDo);
    }
    public void forEachNeighbor(double aX, double aY, double aZ, IDxyzIdxDo aDxyzIdxDo) {
        if (!mValid) throw new IllegalStateException("Need `build` first");
        
        // 注意对于一般情况需要将超出边界的进行平移
        final XYZ tBuf = new XYZ(aX, aY, aZ);
        wrapPBC(tBuf); // 存在部分重复计算，不过不关键
        
        final int ci, cj, ck;
        if (mPrism) {
            toDirect(tBuf);
            ci = MathEX.Code.floor2int(tBuf.mX*mSliceX);
            cj = MathEX.Code.floor2int(tBuf.mY*mSliceY);
            ck = MathEX.Code.floor2int(tBuf.mZ*mSliceZ);
        } else {
            ci = MathEX.Code.floor2int(tBuf.mX*mSliceX/mA.mX);
            cj = MathEX.Code.floor2int(tBuf.mY*mSliceY/mB.mY);
            ck = MathEX.Code.floor2int(tBuf.mZ*mSliceZ/mC.mZ);
        }
        final double x0 = tBuf.mX;
        final double y0 = tBuf.mY;
        final double z0 = tBuf.mZ;
        forEachCell(x0, y0, z0, ci  , cj  , ck  , true , aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj  , ck+1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj  , ck-1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj+1, ck  , false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj+1, ck+1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj+1, ck-1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj-1, ck  , false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj-1, ck+1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci  , cj-1, ck-1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj  , ck  , false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj  , ck+1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj  , ck-1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj+1, ck  , false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj+1, ck+1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj+1, ck-1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj-1, ck  , false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj-1, ck+1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci+1, cj-1, ck-1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj  , ck  , false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj  , ck+1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj  , ck-1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj+1, ck  , false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj+1, ck+1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj+1, ck-1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj-1, ck  , false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj-1, ck+1, false, aDxyzIdxDo);
        forEachCell(x0, y0, z0, ci-1, cj-1, ck-1, false, aDxyzIdxDo);
    }
    
    public void forEachNeighbor(int aIndex, IDxyzIdxDo aDxyzIdxDo) {
        forEachNeighbor(aIndex, false, aDxyzIdxDo);
    }
    public void forEachNeighbor(int aIndex, int aNnn, IDxyzIdxDo aDxyzIdxDo) {
        if (!mValid) throw new IllegalStateException("Need `build` first");
        if (aNnn < 0) {
            forEachNeighbor(aIndex, aDxyzIdxDo);
            return;
        }
        if (aNnn == 0) return;
        try (NearestNeighborList tNNL = new NearestNeighborList(aNnn)) {
            forEachNeighbor(aIndex, false, tNNL::put);
            tNNL.forEachNeighbor(aDxyzIdxDo);
        }
    }
    public void forEachNeighbor(double aX, double aY, double aZ, int aNnn, IDxyzIdxDo aDxyzIdxDo) {
        if (!mValid) throw new IllegalStateException("Need `build` first");
        if (aNnn < 0) {
            forEachNeighbor(aX, aY, aZ, aDxyzIdxDo);
            return;
        }
        if (aNnn == 0) return;
        try (NearestNeighborList tNNL = new NearestNeighborList(aNnn)) {
            forEachNeighbor(aX, aY, aZ, tNNL::put);
            tNNL.forEachNeighbor(aDxyzIdxDo);
        }
    }
    
    
    /// 直接构建获取值拷贝后的近邻列表
    /**
     * 获取给定索引原子的近邻原子索引组成的列表，不包括自身
     * <p>
     * 如果需要近邻原子的相对坐标需要使用
     * {@link #getFull(int, int)}
     *
     * @param aIdx 需要获取近邻列表的原子索引
     * @param aNnn 需要的最近的近邻原子数目
     * @return 近邻原子索引组成的向量，不包括自身
     * @see IntVector
     */
    public IntVector get(int aIdx, int aNnn) {
        if (!mValid) throw new IllegalStateException("Need `build` first");
        final IntVector.Builder rNL = IntVector.builder();
        forEachNeighbor(aIdx, aNnn, (dx, dy, dz, idx) -> rNL.add(idx));
        return rNL.build();
    }
    /**
     * 获取给定索引原子的近邻原子索引组成的列表，不包括自身
     * <p>
     * 如果需要获取指定数目的最近的近邻原子列表，则使用
     * {@link #get(int, int)}
     * 来增加一个参数 aNnn
     * <p>
     * 如果需要近邻原子的坐标需要使用
     * {@link #getFull(int)}
     *
     * @param aIdx 需要获取近邻列表的原子索引
     * @return 近邻原子索引组成的向量，不包括自身
     * @see IntVector
     */
    public IntVector get(int aIdx) {
        return get(aIdx, -1);
    }
    
    /**
     * 内部使用的直接通过三个坐标值获取近邻列表接口，
     * 目前来说如果需要类似功能则需使用 {@link #get(IXYZ, int)}
     * @see #get(IXYZ, int)
     */
    @ApiStatus.Internal public IntVector get_(double aX, double aY, double aZ, int aNnn) {
        if (!mValid) throw new IllegalStateException("Need `build` first");
        final IntVector.Builder rNL = IntVector.builder();
        forEachNeighbor(aX, aY, aZ, aNnn, (dx, dy, dz, idx) -> rNL.add(idx));
        return rNL.build();
    }
    /**
     * 获取给定坐标近邻原子索引组成的列表，不会特意排除恰好位于输入坐标的点
     * <p>
     * 如果需要近邻原子的相对坐标需要使用
     * {@link #getFull(IXYZ, int)}
     *
     * @param aXYZ 需要获取近邻列表的 xyz 坐标
     * @param aNnn 需要的最近的近邻原子数目
     * @return 近邻原子索引组成的向量
     * @see IntVector
     * @see IXYZ
     */
    public IntVector get(IXYZ aXYZ, int aNnn) {
        return get_(aXYZ.x(), aXYZ.y(), aXYZ.z(), aNnn);
    }
    /**
     * 获取给定坐标近邻原子索引组成的列表，不会特意排除恰好位于输入坐标的点
     * <p>
     * 如果需要获取指定数目的最近的近邻原子列表，则使用
     * {@link #get(IXYZ, int)}
     * 来增加一个参数 aNnn
     * <p>
     * 如果需要近邻原子的相对坐标需要使用
     * {@link #getFull(IXYZ)}
     *
     * @param aXYZ 需要获取近邻列表的 xyz 坐标
     * @return 近邻原子索引组成的向量
     * @see IntVector
     * @see IXYZ
     */
    public IntVector get(IXYZ aXYZ) {
        return get(aXYZ, -1);
    }
    
    /**
     * 获取给定索引原子的近邻原子的相对坐标以及索引组成的列表，不包括自身。
     * 其中相对坐标按照定义：{@code dr = rj - ri}，其中 {@code i}
     * 为中心索引，{@code j} 为近邻索引。
     * <p>
     * 使用此方法直接获取近邻原子相对坐标可以自动考虑 pbc
     * 下镜像原子的情况
     *
     * @param aIdx 需要获取近邻列表的原子索引
     * @param aNnn 需要的最近的近邻原子数目
     * @return 按照 {@code [dx, dy, dz, idx]} 顺序排列的向量列表，不包括自身
     * @see Vector
     */
    public List<Vector> getFull(int aIdx, int aNnn) {
        if (!mValid) throw new IllegalStateException("Need `build` first");
        // 目前这种情况都需要遍历一下
        final Vector.Builder rNL = Vector.builder();
        final Vector.Builder rDx = Vector.builder();
        final Vector.Builder rDy = Vector.builder();
        final Vector.Builder rDz = Vector.builder();
        forEachNeighbor(aIdx, aNnn, (dx, dy, dz, idx) -> {
            rNL.add(idx);
            rDx.add(dx); rDy.add(dy); rDz.add(dz);
        });
        return Lists.newArrayList(rDx.build(), rDy.build(), rDz.build(), rNL.build());
    }
    /**
     * 获取给定索引原子的近邻原子的相对坐标以及索引组成的列表，不包括自身。
     * 其中相对坐标按照定义：{@code dr = rj - ri}，其中 {@code i}
     * 为中心索引，{@code j} 为近邻索引。
     * <p>
     * 使用此方法直接获取近邻原子相对坐标可以自动考虑 pbc
     * 下镜像原子的情况
     * <p>
     * 如果需要获取指定数目的最近的近邻原子列表，则使用
     * {@link #getFull(int, int)}
     * 来增加一个参数 aNnn
     *
     * @param aIdx 需要获取近邻列表的原子索引
     * @return 按照 {@code [dx, dy, dz, idx]} 顺序排列的向量列表，不包括自身
     * @see Vector
     */
    public List<Vector> getFull(int aIdx) {
        return getFull(aIdx, -1);
    }
    
    /**
     * 内部使用的直接通过三个坐标值获取完整近邻列表接口，
     * 目前来说如果需要类似功能则需使用 {@link #getFull(IXYZ, int)}
     * @see #getFull(IXYZ, int)
     */
    @ApiStatus.Internal public List<Vector> getFull_(double aX, double aY, double aZ, int aNnn) {
        if (!mValid) throw new IllegalStateException("Need `build` first");
        
        final Vector.Builder rNL = Vector.builder();
        final Vector.Builder rDx = Vector.builder();
        final Vector.Builder rDy = Vector.builder();
        final Vector.Builder rDz = Vector.builder();
        forEachNeighbor(aX, aY, aZ, aNnn, (dx, dy, dz, idx) -> {
            rNL.add(idx);
            rDx.add(dx); rDy.add(dy); rDz.add(dz);
        });
        return Lists.newArrayList(rDx.build(), rDy.build(), rDz.build(), rNL.build());
    }
    /**
     * 获取给定索引原子的近邻原子的相对坐标以及索引组成的列表，不包括自身。
     * 其中相对坐标按照定义：{@code dr = rj - ri}，其中 {@code i}
     * 为中心索引，{@code j} 为近邻索引。
     * <p>
     * 使用此方法直接获取近邻原子相对坐标可以自动考虑 pbc
     * 下镜像原子的情况
     *
     * @param aXYZ 需要获取近邻列表的 xyz 坐标
     * @param aNnn 需要的最近的近邻原子数目
     * @return 按照 {@code [dx, dy, dz, idx]} 顺序排列的向量列表
     * @see Vector
     * @see IXYZ
     */
    public List<Vector> getFull(IXYZ aXYZ, int aNnn) {
        return getFull_(aXYZ.x(), aXYZ.y(), aXYZ.z(), aNnn);
    }
    /**
     * 获取给定索引原子的近邻原子的相对坐标以及索引组成的列表，不包括自身。
     * 其中相对坐标按照定义：{@code dr = rj - ri}，其中 {@code i}
     * 为中心索引，{@code j} 为近邻索引。
     * <p>
     * 使用此方法直接获取近邻原子相对坐标可以自动考虑 pbc
     * 下镜像原子的情况
     * <p>
     * 如果需要获取指定数目的最近的近邻原子列表，则使用
     * {@link #getFull(IXYZ, int)}
     * 来增加一个参数 aNnn
     *
     * @param aXYZ 需要获取近邻列表的 xyz 坐标
     * @return 按照 {@code [dx, dy, dz, idx]} 顺序排列的向量列表
     * @see Vector
     * @see IXYZ
     */
    public List<Vector> getFull(IXYZ aXYZ) {
        return getFull(aXYZ, -1);
    }
}
