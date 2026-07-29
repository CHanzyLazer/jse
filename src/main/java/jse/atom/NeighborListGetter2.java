package jse.atom;

import jse.code.collection.DoubleList;
import jse.code.collection.IntList;
import jse.code.functional.IBinaryFullOperator;
import jse.code.functional.IUnaryFullOperator;
import jse.math.MathEX;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * 更加高效的 jse 近邻列表实现，缓存 ghost 从而避免每次获取时重复计算来大幅加速
 * <p>
 * 目前还是统一 ppp，对于后续要做 LAMMPS/GPUMD 的近邻列表替代则还需要支持其他边界；
 * 不过那个也是独立的 GPU 版本实现了，很长时间应该两者都不会影响
 * <p>
 * 现在此类不再提供冗余的线程安全支持以及可变 rcut 支持，不同实例之间线程安全
 * @author liqa
 */
@ApiStatus.Experimental
public class NeighborListGetter2 {
    public final static int MAX_SLICE = 256;
    private final static int MARK_X = 0, MARK_Y = 1, MARK_Z = 2;
    
    protected final IntList mIdx;
    protected final DoubleList mPosX, mPosY, mPosZ;
    protected final IntList[] mCells;
    protected final int mSliceX, mSliceY, mSliceZ;
    protected final int mNumAtoms;
    protected int mNumGhost;
    
    protected final IBox mBox;
    protected final boolean mPrism;
    protected final @Nullable XYZ mBoxA, mBoxB, mBoxC; // null for normal
    protected final @Nullable XYZ mBoxBC, mBoxCA, mBoxAB; // null for normal
    protected final @Nullable XYZ mCellXYZ; // null for prism
    protected final XYZ mBoxXYZ; // 表示三个方向的模拟盒平面之间的距离，用于确定 cell 需要分划的份数
    
    protected final double mRCut, mRCutSq;
    
    public NeighborListGetter2(IAtomData aData, double aRCut) {
        if (aRCut <= 0.0) throw new IllegalArgumentException("rcut MUST > 0.0, input: "+aRCut);
        mRCut = aRCut;
        mRCutSq = mRCut*mRCut;
        mNumAtoms = aData.natoms();
        mBox = aData.box().copy();
        mPrism = mBox.isPrism();
        if (mPrism) {
            // 计算距离，这里涉及一些重复计算，不过不关键就是
            mBoxA = XYZ.toXYZ(mBox.a());
            mBoxB = XYZ.toXYZ(mBox.b());
            mBoxC = XYZ.toXYZ(mBox.c());
            mBoxBC = mBoxB.cross(mBoxC);
            mBoxCA = mBoxC.cross(mBoxA);
            mBoxAB = mBoxA.cross(mBoxB);
            mBoxXYZ = new XYZ(
                mBoxA.dot(mBoxBC) / mBoxBC.norm(),
                mBoxB.dot(mBoxCA) / mBoxCA.norm(),
                mBoxC.dot(mBoxAB) / mBoxAB.norm()
            );
        } else {
            mBoxA = mBoxB = mBoxC = null;
            mBoxBC = mBoxCA = mBoxAB = null;
            mBoxXYZ = XYZ.toXYZ(mBox);
        }
        // 确定分划份数
        mSliceX = MathEX.Code.toRange(1, MAX_SLICE, MathEX.Code.floor2int(mBoxXYZ.mX/mRCut));
        mSliceY = MathEX.Code.toRange(1, MAX_SLICE, MathEX.Code.floor2int(mBoxXYZ.mY/mRCut));
        mSliceZ = MathEX.Code.toRange(1, MAX_SLICE, MathEX.Code.floor2int(mBoxXYZ.mZ/mRCut));
        // 参数初始化
        mCells = new IntList[(mSliceX+2)*(mSliceY+2)*(mSliceZ+2)];
        final int tCellsCount = mCells.length;
        final int tCellInitCount = MathEX.Code.ceil2int(mNumAtoms / (double)(mSliceX*mSliceY*mSliceZ) * 1.25);
        for (int i = 0; i < tCellsCount; ++i) {
            mCells[i] = new IntList(tCellInitCount);
        }
        final int tDataInitCount = MathEX.Code.ceil2int(mNumAtoms*1.25);
        mIdx = new IntList(tDataInitCount);
        mPosX = new DoubleList(tDataInitCount);
        mPosY = new DoubleList(tDataInitCount);
        mPosZ = new DoubleList(tDataInitCount);
        final XYZ tBuf = new XYZ();
        for (int idx = 0; idx < mNumAtoms; ++idx) {
            IAtom tAtom = aData.atom(idx);
            mIdx.add(idx);
            tBuf.setXYZ(tAtom.x(), tAtom.y(), tAtom.z());
            mBox.wrapPBC(tBuf);
            mPosX.add(tBuf.mX);
            mPosY.add(tBuf.mY);
            mPosZ.add(tBuf.mZ);
        }
        // 构建 cell
        if (mPrism) {
            mCellXYZ = null;
            for (int idx = 0; idx < mNumAtoms; ++idx) {
                tBuf.setXYZ(mPosX.get(idx), mPosY.get(idx), mPosZ.get(idx));
                mBox.toDirect(tBuf);
                int i = MathEX.Code.floor2int(tBuf.mX * mSliceX);
                int j = MathEX.Code.floor2int(tBuf.mY * mSliceY);
                int k = MathEX.Code.floor2int(tBuf.mZ * mSliceZ);
                cell(i, j, k, true).add(idx);
            }
        } else {
            mCellXYZ = mBoxXYZ.div(mSliceX, mSliceY, mSliceZ);
            for (int idx = 0; idx < mNumAtoms; ++idx) {
                int i = MathEX.Code.floor2int(mPosX.get(idx) / mCellXYZ.mX);
                int j = MathEX.Code.floor2int(mPosY.get(idx) / mCellXYZ.mY);
                int k = MathEX.Code.floor2int(mPosZ.get(idx) / mCellXYZ.mZ);
                cell(i, j, k, true).add(idx);
            }
        }
        // 添加 ghost
        mNumGhost = 0;
        // 六个平面
        initGhostPlane_(tBuf, MARK_X);
        initGhostPlane_(tBuf, MARK_Y);
        initGhostPlane_(tBuf, MARK_Z);
        // 十二个边
        initGhostEdge_(tBuf, MARK_X);
        initGhostEdge_(tBuf, MARK_Y);
        initGhostEdge_(tBuf, MARK_Z);
        // 八个角
        initGhostCorner_(tBuf);
        // 清理大小
        mIdx.trimToSize();
        mPosX.trimToSize();
        mPosY.trimToSize();
        mPosZ.trimToSize();
        for (int i = 0; i < tCellsCount; ++i) {
            mCells[i].trimToSize();
        }
    }
    
    protected final IntList cell(int i, int j, int k) {
        return cell(i, j, k, false);
    }
    protected final IntList cell(int i, int j, int k, boolean in) {
        if (in) {
            if (i<0 || i>=mSliceX || j<0 || j>=mSliceY || k<0 || k>=mSliceZ) {
                throw new IndexOutOfBoundsException(String.format("Index: (%d, %d, %d)", i, j, k));
            }
        } else {
            if (i<-1 || i>mSliceX || j<-1 || j>mSliceY || k<-1 || k>mSliceZ) {
                throw new IndexOutOfBoundsException(String.format("Index: (%d, %d, %d)", i, j, k));
            }
        }
        return mCells[(i+1) + (mSliceX+2)*(j+1) + (mSliceX+2)*(mSliceY+2)*(k+1)];
    }
    
    private void initGhostPlane_(XYZ rBuf, int aMark) {
        final int tRep, tSliceA, tSliceB;
        final IUnaryFullOperator<Boolean, Integer> tShiftBufAndCheckR, tShiftBufAndCheckL;
        switch(aMark) {
        case MARK_X: {
            tRep = MathEX.Code.ceil2int(mRCut/mBoxXYZ.mX);
            tSliceA = mSliceY;
            tSliceB = mSliceZ;
            tShiftBufAndCheckR = r -> {
                if (mPrism) {
                    assert mBoxA!=null && mBoxBC!=null;
                    rBuf.mplus2this(mBoxA, r);
                    return (rBuf.dot(mBoxBC)/mBoxBC.norm() - mBoxXYZ.mX) >= mRCut;
                } else {
                    rBuf.mX += r * mBoxXYZ.mX;
                    return (rBuf.mX - mBoxXYZ.mX) >= mRCut;
                }
            };
            tShiftBufAndCheckL = r -> {
                if (mPrism) {
                    assert mBoxA!=null && mBoxBC!=null;
                    rBuf.mplus2this(mBoxA, -r);
                    return (-rBuf.dot(mBoxBC)/mBoxBC.norm()) >= mRCut;
                } else {
                    rBuf.mX -= r * mBoxXYZ.mX;
                    return (-rBuf.mX) >= mRCut;
                }
            };
            break;
        }
        case MARK_Y: {
            tRep = MathEX.Code.ceil2int(mRCut/mBoxXYZ.mY);
            tSliceA = mSliceX;
            tSliceB = mSliceZ;
            tShiftBufAndCheckR = r -> {
                if (mPrism) {
                    assert mBoxB!=null && mBoxCA!=null;
                    rBuf.mplus2this(mBoxB, r);
                    return (rBuf.dot(mBoxCA)/mBoxCA.norm() - mBoxXYZ.mY) >= mRCut;
                } else {
                    rBuf.mY += r * mBoxXYZ.mY;
                    return (rBuf.mY - mBoxXYZ.mY) >= mRCut;
                }
            };
            tShiftBufAndCheckL = r -> {
                if (mPrism) {
                    assert mBoxB!=null && mBoxCA!=null;
                    rBuf.mplus2this(mBoxB, -r);
                    return (-rBuf.dot(mBoxCA)/mBoxCA.norm()) >= mRCut;
                } else {
                    rBuf.mY -= r * mBoxXYZ.mY;
                    return (-rBuf.mY) >= mRCut;
                }
            };
            break;
        }
        case MARK_Z: {
            tRep = MathEX.Code.ceil2int(mRCut/mBoxXYZ.mZ);
            tSliceA = mSliceX;
            tSliceB = mSliceY;
            tShiftBufAndCheckR = r -> {
                if (mPrism) {
                    assert mBoxC!=null && mBoxAB!=null;
                    rBuf.mplus2this(mBoxC, r);
                    return (rBuf.dot(mBoxAB)/mBoxAB.norm() - mBoxXYZ.mZ) >= mRCut;
                } else {
                    rBuf.mZ += r * mBoxXYZ.mZ;
                    return (rBuf.mZ - mBoxXYZ.mZ) >= mRCut;
                }
            };
            tShiftBufAndCheckL = r -> {
                if (mPrism) {
                    assert mBoxC!=null && mBoxAB!=null;
                    rBuf.mplus2this(mBoxC, -r);
                    return (-rBuf.dot(mBoxAB)/mBoxAB.norm()) >= mRCut;
                } else {
                    rBuf.mZ -= r * mBoxXYZ.mZ;
                    return (-rBuf.mZ) >= mRCut;
                }
            };
            break;
        }
        default: {
            throw new IllegalStateException();
        }}
        
        for (int ia = 0; ia < tSliceA; ++ia) for (int ib = 0; ib < tSliceB; ++ib) {
            final IntList tCell0, tCellN;
            final IntList tCellG, tCellG1;
            switch(aMark) {
            case MARK_X: {
                tCell0 = cell(0, ia, ib, true);
                tCellG = cell(mSliceX, ia, ib);
                tCellN = cell(mSliceX-1, ia, ib, true);
                tCellG1 = cell(-1, ia, ib);
                break;
            }
            case MARK_Y: {
                tCell0 = cell(ia, 0, ib, true);
                tCellG = cell(ia, mSliceY, ib);
                tCellN = cell(ia, mSliceY-1, ib, true);
                tCellG1 = cell(ia, -1, ib);
                break;
            }
            case MARK_Z: {
                tCell0 = cell(ia, ib, 0, true);
                tCellG = cell(ia, ib, mSliceZ);
                tCellN = cell(ia, ib, mSliceZ-1, true);
                tCellG1 = cell(ia, ib, -1);
                break;
            }
            default: {
                throw new IllegalStateException();
            }}
            for (int r = 1; r <= tRep; ++r) {
                final int tCellCount = tCell0.size();
                for (int iii = 0; iii < tCellCount; ++iii) {
                    int ii = tCell0.get(iii);
                    rBuf.setXYZ(mPosX.get(ii), mPosY.get(ii), mPosZ.get(ii));
                    if (tShiftBufAndCheckR.apply(r)) continue;
                    tCellG.add(mIdx.size());
                    mIdx.add(mIdx.get(ii));
                    mPosX.add(rBuf.mX);
                    mPosY.add(rBuf.mY);
                    mPosZ.add(rBuf.mZ);
                    ++mNumGhost;
                }
            }
            for (int r = 1; r <= tRep; ++r) {
                final int tCellCount = tCellN.size();
                for (int iii = 0; iii < tCellCount; ++iii) {
                    int ii = tCellN.get(iii);
                    rBuf.setXYZ(mPosX.get(ii), mPosY.get(ii), mPosZ.get(ii));
                    if (tShiftBufAndCheckL.apply(r)) continue;
                    tCellG1.add(mIdx.size());
                    mIdx.add(mIdx.get(ii));
                    mPosX.add(rBuf.mX);
                    mPosY.add(rBuf.mY);
                    mPosZ.add(rBuf.mZ);
                    ++mNumGhost;
                }
            }
        }
    }
    
    private void initGhostEdge_(XYZ rBuf, int aMark) {
        final int tRepA, tRepB, tSlice;
        final IBinaryFullOperator<Boolean, Integer, Integer>
            tShiftBufAndCheckRR, tShiftBufAndCheckRL, tShiftBufAndCheckLR, tShiftBufAndCheckLL;
        switch(aMark) {
        case MARK_X: {
            tRepA = MathEX.Code.ceil2int(mRCut/mBoxXYZ.mY);
            tRepB = MathEX.Code.ceil2int(mRCut/mBoxXYZ.mZ);
            tSlice = mSliceX;
            tShiftBufAndCheckRR = (ra, rb) -> {
                if (mPrism) {
                    assert mBoxB!=null && mBoxC!=null && mBoxCA!=null && mBoxAB!=null;
                    rBuf.mplus2this(mBoxB, ra);
                    rBuf.mplus2this(mBoxC, rb);
                    return ((rBuf.dot(mBoxCA)/mBoxCA.norm() - mBoxXYZ.mY) >= mRCut) ||
                           ((rBuf.dot(mBoxAB)/mBoxAB.norm() - mBoxXYZ.mZ) >= mRCut);
                } else {
                    rBuf.mY += ra * mBoxXYZ.mY;
                    rBuf.mZ += rb * mBoxXYZ.mZ;
                    return ((rBuf.mY - mBoxXYZ.mY) >= mRCut) ||
                           ((rBuf.mZ - mBoxXYZ.mZ) >= mRCut);
                }
            };
            tShiftBufAndCheckRL = (ra, rb) -> {
                if (mPrism) {
                    assert mBoxB!=null && mBoxC!=null && mBoxCA!=null && mBoxAB!=null;
                    rBuf.mplus2this(mBoxB, ra);
                    rBuf.mplus2this(mBoxC, -rb);
                    return ((rBuf.dot(mBoxCA)/mBoxCA.norm() - mBoxXYZ.mY) >= mRCut) ||
                           ((-rBuf.dot(mBoxAB)/mBoxAB.norm()) >= mRCut);
                } else {
                    rBuf.mY += ra * mBoxXYZ.mY;
                    rBuf.mZ -= rb * mBoxXYZ.mZ;
                    return ((rBuf.mY - mBoxXYZ.mY) >= mRCut) ||
                           ((-rBuf.mZ) >= mRCut);
                }
            };
            tShiftBufAndCheckLR = (ra, rb) -> {
                if (mPrism) {
                    assert mBoxB!=null && mBoxC!=null && mBoxCA!=null && mBoxAB!=null;
                    rBuf.mplus2this(mBoxB, -ra);
                    rBuf.mplus2this(mBoxC, rb);
                    return ((-rBuf.dot(mBoxCA)/mBoxCA.norm()) >= mRCut) ||
                           ((rBuf.dot(mBoxAB)/mBoxAB.norm() - mBoxXYZ.mZ) >= mRCut);
                } else {
                    rBuf.mY -= ra * mBoxXYZ.mY;
                    rBuf.mZ += rb * mBoxXYZ.mZ;
                    return ((-rBuf.mY) >= mRCut) ||
                           ((rBuf.mZ - mBoxXYZ.mZ) >= mRCut);
                }
            };
            tShiftBufAndCheckLL = (ra, rb) -> {
                if (mPrism) {
                    assert mBoxB!=null && mBoxC!=null && mBoxCA!=null && mBoxAB!=null;
                    rBuf.mplus2this(mBoxB, -ra);
                    rBuf.mplus2this(mBoxC, -rb);
                    return ((-rBuf.dot(mBoxCA)/mBoxCA.norm()) >= mRCut) ||
                           ((-rBuf.dot(mBoxAB)/mBoxAB.norm()) >= mRCut);
                } else {
                    rBuf.mY -= ra * mBoxXYZ.mY;
                    rBuf.mZ -= rb * mBoxXYZ.mZ;
                    return ((-rBuf.mY) >= mRCut) ||
                           ((-rBuf.mZ) >= mRCut);
                }
            };
            break;
        }
        case MARK_Y: {
            tRepA = MathEX.Code.ceil2int(mRCut/mBoxXYZ.mX);
            tRepB = MathEX.Code.ceil2int(mRCut/mBoxXYZ.mZ);
            tSlice = mSliceY;
            tShiftBufAndCheckRR = (ra, rb) -> {
                if (mPrism) {
                    assert mBoxA!=null && mBoxC!=null && mBoxBC!=null && mBoxAB!=null;
                    rBuf.mplus2this(mBoxA, ra);
                    rBuf.mplus2this(mBoxC, rb);
                    return ((rBuf.dot(mBoxBC)/mBoxBC.norm() - mBoxXYZ.mX) >= mRCut) ||
                           ((rBuf.dot(mBoxAB)/mBoxAB.norm() - mBoxXYZ.mZ) >= mRCut);
                } else {
                    rBuf.mX += ra * mBoxXYZ.mX;
                    rBuf.mZ += rb * mBoxXYZ.mZ;
                    return ((rBuf.mX - mBoxXYZ.mX) >= mRCut) ||
                           ((rBuf.mZ - mBoxXYZ.mZ) >= mRCut);
                }
            };
            tShiftBufAndCheckRL = (ra, rb) -> {
                if (mPrism) {
                    assert mBoxA!=null && mBoxC!=null && mBoxBC!=null && mBoxAB!=null;
                    rBuf.mplus2this(mBoxA, ra);
                    rBuf.mplus2this(mBoxC, -rb);
                    return ((rBuf.dot(mBoxBC)/mBoxBC.norm() - mBoxXYZ.mX) >= mRCut) ||
                           ((-rBuf.dot(mBoxAB)/mBoxAB.norm()) >= mRCut);
                } else {
                    rBuf.mX += ra * mBoxXYZ.mX;
                    rBuf.mZ -= rb * mBoxXYZ.mZ;
                    return ((rBuf.mX - mBoxXYZ.mX) >= mRCut) ||
                           ((-rBuf.mZ) >= mRCut);
                }
            };
            tShiftBufAndCheckLR = (ra, rb) -> {
                if (mPrism) {
                    assert mBoxA!=null && mBoxC!=null && mBoxBC!=null && mBoxAB!=null;
                    rBuf.mplus2this(mBoxA, -ra);
                    rBuf.mplus2this(mBoxC, rb);
                    return ((-rBuf.dot(mBoxBC)/mBoxBC.norm()) >= mRCut) ||
                           ((rBuf.dot(mBoxAB)/mBoxAB.norm() - mBoxXYZ.mZ) >= mRCut);
                } else {
                    rBuf.mX -= ra * mBoxXYZ.mX;
                    rBuf.mZ += rb * mBoxXYZ.mZ;
                    return ((-rBuf.mX) >= mRCut) ||
                           ((rBuf.mZ - mBoxXYZ.mZ) >= mRCut);
                }
            };
            tShiftBufAndCheckLL = (ra, rb) -> {
                if (mPrism) {
                    assert mBoxA!=null && mBoxC!=null && mBoxBC!=null && mBoxAB!=null;
                    rBuf.mplus2this(mBoxA, -ra);
                    rBuf.mplus2this(mBoxC, -rb);
                    return ((-rBuf.dot(mBoxBC)/mBoxBC.norm()) >= mRCut) ||
                           ((-rBuf.dot(mBoxAB)/mBoxAB.norm()) >= mRCut);
                } else {
                    rBuf.mX -= ra * mBoxXYZ.mX;
                    rBuf.mZ -= rb * mBoxXYZ.mZ;
                    return ((-rBuf.mX) >= mRCut) ||
                           ((-rBuf.mZ) >= mRCut);
                }
            };
            break;
        }
        case MARK_Z: {
            tRepA = MathEX.Code.ceil2int(mRCut/mBoxXYZ.mX);
            tRepB = MathEX.Code.ceil2int(mRCut/mBoxXYZ.mY);
            tSlice = mSliceZ;
            tShiftBufAndCheckRR = (ra, rb) -> {
                if (mPrism) {
                    assert mBoxA!=null && mBoxB!=null && mBoxBC!=null && mBoxCA!=null;
                    rBuf.mplus2this(mBoxA, ra);
                    rBuf.mplus2this(mBoxB, rb);
                    return ((rBuf.dot(mBoxBC)/mBoxBC.norm() - mBoxXYZ.mX) >= mRCut) ||
                           ((rBuf.dot(mBoxCA)/mBoxCA.norm() - mBoxXYZ.mY) >= mRCut);
                } else {
                    rBuf.mX += ra * mBoxXYZ.mX;
                    rBuf.mY += rb * mBoxXYZ.mY;
                    return ((rBuf.mX - mBoxXYZ.mX) >= mRCut) ||
                           ((rBuf.mY - mBoxXYZ.mY) >= mRCut);
                }
            };
            tShiftBufAndCheckRL = (ra, rb) -> {
                if (mPrism) {
                    assert mBoxA!=null && mBoxB!=null && mBoxBC!=null && mBoxCA!=null;
                    rBuf.mplus2this(mBoxA, ra);
                    rBuf.mplus2this(mBoxB, -rb);
                    return ((rBuf.dot(mBoxBC)/mBoxBC.norm() - mBoxXYZ.mX) >= mRCut) ||
                           ((-rBuf.dot(mBoxCA)/mBoxCA.norm()) >= mRCut);
                } else {
                    rBuf.mX += ra * mBoxXYZ.mX;
                    rBuf.mY -= rb * mBoxXYZ.mY;
                    return ((rBuf.mX - mBoxXYZ.mX) >= mRCut) ||
                           ((-rBuf.mY) >= mRCut);
                }
            };
            tShiftBufAndCheckLR = (ra, rb) -> {
                if (mPrism) {
                    assert mBoxA!=null && mBoxB!=null && mBoxBC!=null && mBoxCA!=null;
                    rBuf.mplus2this(mBoxA, -ra);
                    rBuf.mplus2this(mBoxB, rb);
                    return ((-rBuf.dot(mBoxBC)/mBoxBC.norm()) >= mRCut) ||
                           ((rBuf.dot(mBoxCA)/mBoxCA.norm() - mBoxXYZ.mY) >= mRCut);
                } else {
                    rBuf.mX -= ra * mBoxXYZ.mX;
                    rBuf.mY += rb * mBoxXYZ.mY;
                    return ((-rBuf.mX) >= mRCut) ||
                           ((rBuf.mY - mBoxXYZ.mY) >= mRCut);
                }
            };
            tShiftBufAndCheckLL = (ra, rb) -> {
                if (mPrism) {
                    assert mBoxA!=null && mBoxB!=null && mBoxBC!=null && mBoxCA!=null;
                    rBuf.mplus2this(mBoxA, -ra);
                    rBuf.mplus2this(mBoxB, -rb);
                    return ((-rBuf.dot(mBoxBC)/mBoxBC.norm()) >= mRCut) ||
                           ((-rBuf.dot(mBoxCA)/mBoxCA.norm()) >= mRCut);
                } else {
                    rBuf.mX -= ra * mBoxXYZ.mX;
                    rBuf.mY -= rb * mBoxXYZ.mY;
                    return ((-rBuf.mX) >= mRCut) ||
                           ((-rBuf.mY) >= mRCut);
                }
            };
            break;
        }
        default: {
            throw new IllegalStateException();
        }}
        for (int i = 0; i < tSlice; ++i) {
            final IntList tCell00, tCell0N, tCellN0, tCellNN;
            final IntList tCellG, tCellG1, tCellG2, tCellG3;
            switch(aMark) {
            case MARK_X: {
                tCell00 = cell(i, 0, 0, true);
                tCellG  = cell(i, mSliceY, mSliceZ);
                tCell0N = cell(i, 0, mSliceZ-1, true);
                tCellG1 = cell(i, mSliceY, -1);
                tCellN0 = cell(i, mSliceY-1, 0, true);
                tCellG2 = cell(i, -1, mSliceZ);
                tCellNN = cell(i, mSliceY-1, mSliceZ-1, true);
                tCellG3 = cell(i, -1, -1);
                break;
            }
            case MARK_Y: {
                tCell00 = cell(0, i, 0, true);
                tCellG  = cell(mSliceX, i, mSliceZ);
                tCell0N = cell(0, i, mSliceZ-1, true);
                tCellG1 = cell(mSliceX, i, -1);
                tCellN0 = cell(mSliceX-1, i, 0, true);
                tCellG2 = cell(-1, i, mSliceZ);
                tCellNN = cell(mSliceX-1, i, mSliceZ-1, true);
                tCellG3 = cell(-1, i, -1);
                break;
            }
            case MARK_Z: {
                tCell00 = cell(0, 0, i, true);
                tCellG  = cell(mSliceX, mSliceY, i);
                tCell0N = cell(0, mSliceY-1, i, true);
                tCellG1 = cell(mSliceX, -1, i);
                tCellN0 = cell(mSliceX-1, 0, i, true);
                tCellG2 = cell(-1, mSliceY, i);
                tCellNN = cell(mSliceX-1, mSliceY-1, i, true);
                tCellG3 = cell(-1, -1, i);
                break;
            }
            default: {
                throw new IllegalStateException();
            }}
            for (int ra = 1; ra <= tRepA; ++ra) for (int rb = 1; rb <= tRepB; ++rb) {
                final int tCellCount = tCell00.size();
                for (int iii = 0; iii < tCellCount; ++iii) {
                    int ii = tCell00.get(iii);
                    rBuf.setXYZ(mPosX.get(ii), mPosY.get(ii), mPosZ.get(ii));
                    if (tShiftBufAndCheckRR.apply(ra, rb)) continue;
                    tCellG.add(mIdx.size());
                    mIdx.add(mIdx.get(ii));
                    mPosX.add(rBuf.mX);
                    mPosY.add(rBuf.mY);
                    mPosZ.add(rBuf.mZ);
                    ++mNumGhost;
                }
            }
            for (int ra = 1; ra <= tRepA; ++ra) for (int rb = 1; rb <= tRepB; ++rb) {
                final int tCellCount = tCell0N.size();
                for (int iii = 0; iii < tCellCount; ++iii) {
                    int ii = tCell0N.get(iii);
                    rBuf.setXYZ(mPosX.get(ii), mPosY.get(ii), mPosZ.get(ii));
                    if (tShiftBufAndCheckRL.apply(ra, rb)) continue;
                    tCellG1.add(mIdx.size());
                    mIdx.add(mIdx.get(ii));
                    mPosX.add(rBuf.mX);
                    mPosY.add(rBuf.mY);
                    mPosZ.add(rBuf.mZ);
                    ++mNumGhost;
                }
            }
            for (int ra = 1; ra <= tRepA; ++ra) for (int rb = 1; rb <= tRepB; ++rb) {
                final int tCellCount = tCellN0.size();
                for (int iii = 0; iii < tCellCount; ++iii) {
                    int ii = tCellN0.get(iii);
                    rBuf.setXYZ(mPosX.get(ii), mPosY.get(ii), mPosZ.get(ii));
                    if (tShiftBufAndCheckLR.apply(ra, rb)) continue;
                    tCellG2.add(mIdx.size());
                    mIdx.add(mIdx.get(ii));
                    mPosX.add(rBuf.mX);
                    mPosY.add(rBuf.mY);
                    mPosZ.add(rBuf.mZ);
                    ++mNumGhost;
                }
            }
            for (int ra = 1; ra <= tRepA; ++ra) for (int rb = 1; rb <= tRepB; ++rb) {
                final int tCellCount = tCellNN.size();
                for (int iii = 0; iii < tCellCount; ++iii) {
                    int ii = tCellNN.get(iii);
                    rBuf.setXYZ(mPosX.get(ii), mPosY.get(ii), mPosZ.get(ii));
                    if (tShiftBufAndCheckLL.apply(ra, rb)) continue;
                    tCellG3.add(mIdx.size());
                    mIdx.add(mIdx.get(ii));
                    mPosX.add(rBuf.mX);
                    mPosY.add(rBuf.mY);
                    mPosZ.add(rBuf.mZ);
                    ++mNumGhost;
                }
            }
        }
    }
    
    private void initGhostCornerInter_(XYZ rBuf, int aRepX, int aRepY, int aRepZ,
                                       boolean xp, boolean yp, boolean zp) {
        IntList tCell0 = cell(xp?0:(mSliceX-1), yp?0:(mSliceY-1), zp?0:(mSliceZ-1), true);
        IntList tCellG = cell(xp?mSliceX:-1, yp?mSliceY:-1, zp?mSliceZ:-1);
        for (int ri = 1; ri <= aRepX; ++ri) for (int rj = 1; rj <= aRepY; ++rj) for (int rk = 1; rk <= aRepZ; ++rk) {
            final int ri_ = xp?ri:-ri;
            final int rj_ = yp?rj:-rj;
            final int rk_ = zp?rk:-rk;
            final int tCellCount = tCell0.size();
            for (int iii = 0; iii < tCellCount; ++iii) {
                int ii = tCell0.get(iii);
                rBuf.setXYZ(mPosX.get(ii), mPosY.get(ii), mPosZ.get(ii));
                if (mPrism) {
                    assert mBoxA!=null && mBoxB!=null && mBoxC!=null && mBoxBC!=null && mBoxCA!=null && mBoxAB!=null;
                    rBuf.mplus2this(mBoxA, ri_);
                    rBuf.mplus2this(mBoxB, rj_);
                    rBuf.mplus2this(mBoxC, rk_);
                    if ((xp ? ((rBuf.dot(mBoxBC)/mBoxBC.norm() - mBoxXYZ.mX) >= mRCut) : ((-rBuf.dot(mBoxBC)/mBoxBC.norm()) >= mRCut)) ||
                        (yp ? ((rBuf.dot(mBoxCA)/mBoxCA.norm() - mBoxXYZ.mY) >= mRCut) : ((-rBuf.dot(mBoxCA)/mBoxCA.norm()) >= mRCut)) ||
                        (zp ? ((rBuf.dot(mBoxAB)/mBoxAB.norm() - mBoxXYZ.mZ) >= mRCut) : ((-rBuf.dot(mBoxAB)/mBoxAB.norm()) >= mRCut))) {
                        continue;
                    }
                } else {
                    rBuf.mX += ri_ * mBoxXYZ.mX;
                    rBuf.mY += rj_ * mBoxXYZ.mY;
                    rBuf.mZ += rk_ * mBoxXYZ.mZ;
                    if ((xp ? ((rBuf.mX - mBoxXYZ.mX) >= mRCut) : ((-rBuf.mX) >= mRCut)) ||
                        (yp ? ((rBuf.mY - mBoxXYZ.mY) >= mRCut) : ((-rBuf.mY) >= mRCut)) ||
                        (zp ? ((rBuf.mZ - mBoxXYZ.mZ) >= mRCut) : ((-rBuf.mZ) >= mRCut))) {
                        continue;
                    }
                }
                tCellG.add(mIdx.size());
                mIdx.add(mIdx.get(ii));
                mPosX.add(rBuf.mX);
                mPosY.add(rBuf.mY);
                mPosZ.add(rBuf.mZ);
                ++mNumGhost;
            }
        }
    }
    private void initGhostCorner_(final XYZ rBuf) {
        final int tRepX = MathEX.Code.ceil2int(mRCut/mBoxXYZ.mX);
        final int tRepY = MathEX.Code.ceil2int(mRCut/mBoxXYZ.mY);
        final int tRepZ = MathEX.Code.ceil2int(mRCut/mBoxXYZ.mZ);
        initGhostCornerInter_(rBuf, tRepX, tRepY, tRepZ, true, true, true);
        initGhostCornerInter_(rBuf, tRepX, tRepY, tRepZ, true, true, false);
        initGhostCornerInter_(rBuf, tRepX, tRepY, tRepZ, true, false, true);
        initGhostCornerInter_(rBuf, tRepX, tRepY, tRepZ, true, false, false);
        initGhostCornerInter_(rBuf, tRepX, tRepY, tRepZ, false, true, true);
        initGhostCornerInter_(rBuf, tRepX, tRepY, tRepZ, false, true, false);
        initGhostCornerInter_(rBuf, tRepX, tRepY, tRepZ, false, false, true);
        initGhostCornerInter_(rBuf, tRepX, tRepY, tRepZ, false, false, false);
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
    
    protected void forEachCell(int index, double cx, double cy, double cz, int i, int j, int k, boolean in, boolean aCheck, IDxyzIdxDo aDxyzIdxDo) {
        IntList tCell = cell(i, j, k, in);
        final int tCellCount = tCell.size();
        for (int iii = 0; iii < tCellCount; ++iii) {
            int ii = tCell.get(iii);
            if (index>=0 && ii==index) continue;
            double dx = mPosX.get(ii) - cx;
            double dy = mPosY.get(ii) - cy;
            double dz = mPosZ.get(ii) - cz;
            if (aCheck) {
                double dis2 = dx*dx + dy*dy + dz*dz;
                if (dis2 < mRCutSq) aDxyzIdxDo.run(dx, dy, dz, mIdx.get(ii));
            } else {
                aDxyzIdxDo.run(dx, dy, dz, mIdx.get(ii));
            }
        }
    }
    protected void forEachCell(double cx, double cy, double cz, int i, int j, int k, boolean aCheck, IDxyzIdxDo aDxyzIdxDo) {
        forEachCell(-1, cx, cy, cz, i, j, k, false, aCheck, aDxyzIdxDo);
    }
    
    public void forEachNeighbor(int aIndex, boolean aCheck, IDxyzIdxDo aDxyzIdxDo) {
        final double cx = mPosX.get(aIndex);
        final double cy = mPosY.get(aIndex);
        final double cz = mPosZ.get(aIndex);
        final int i, j, k;
        if (mPrism) {
            XYZ tBuf = new XYZ(cx, cy, cz);
            mBox.toDirect(tBuf);
            i = MathEX.Code.floor2int(tBuf.mX * mSliceX);
            j = MathEX.Code.floor2int(tBuf.mY * mSliceY);
            k = MathEX.Code.floor2int(tBuf.mZ * mSliceZ);
        } else {
            assert mCellXYZ!=null;
            i = MathEX.Code.floor2int(cx / mCellXYZ.mX);
            j = MathEX.Code.floor2int(cy / mCellXYZ.mY);
            k = MathEX.Code.floor2int(cz / mCellXYZ.mZ);
        }
        forEachCell(aIndex, cx, cy, cz, i, j, k, true, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i  , j  , k+1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i  , j  , k-1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i  , j+1, k  , aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i  , j+1, k+1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i  , j+1, k-1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i  , j-1, k  , aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i  , j-1, k+1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i  , j-1, k-1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i+1, j  , k  , aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i+1, j  , k+1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i+1, j  , k-1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i+1, j+1, k  , aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i+1, j+1, k+1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i+1, j+1, k-1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i+1, j-1, k  , aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i+1, j-1, k+1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i+1, j-1, k-1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i-1, j  , k  , aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i-1, j  , k+1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i-1, j  , k-1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i-1, j+1, k  , aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i-1, j+1, k+1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i-1, j+1, k-1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i-1, j-1, k  , aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i-1, j-1, k+1, aCheck, aDxyzIdxDo);
        forEachCell(cx, cy, cz, i-1, j-1, k-1, aCheck, aDxyzIdxDo);
    }
    public void forEachNeighbor(int aIndex, IDxyzIdxDo aDxyzIdxDo) {
        forEachNeighbor(aIndex, true, aDxyzIdxDo);
    }
}
