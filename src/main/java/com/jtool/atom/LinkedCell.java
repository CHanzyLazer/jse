package com.jtool.atom;

import com.jtool.code.collection.AbstractRandomAccessList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liqa
 * <p> 更加通用易用的 LinkedCell 类 </p>
 * <p> 分区粒子的 cell，并且提供获取周围 cell 链接的 cell 的方法 </p>
 * <p> 目前认为所有边界都是周期边界条件，并且只考虑最近邻的 cell </p>
 * <p> 此类线程安全，包括多个线程同时访问同一个实例 </p>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
final class LinkedCell<A extends IXYZ> {
    private final @Unmodifiable List[] mCells;
    private final int mSizeX, mSizeY, mSizeZ;
    private final XYZ mCellBox;
    private final XYZ mBox;
    
    private final double mMaxDis; // 此 cell 能使用的最大的近邻距离
    public double maxDis() {return mMaxDis;}
    
    /** 指定三维的分划份数来初始化，同样目前暂不支持外部创建 */
    LinkedCell(Iterable<? extends A> aAtoms, List[] rCellsAlloc, XYZ aBox, int aSizeX, int aSizeY, int aSizeZ) {
        mSizeX = aSizeX; mSizeY = aSizeY; mSizeZ = aSizeZ;
        mBox = aBox;
        mCellBox = mBox.div(mSizeX, mSizeY, mSizeZ);
        mMaxDis = mCellBox.min();
        // 初始化 cell
        int tSize = aSizeX * aSizeY * aSizeZ;
        mCells = rCellsAlloc;
        for (int i = 0; i < tSize; ++i) mCells[i].clear(); // 直接清空旧数据即可
        // 遍历添加 Atom
        for (A tAtom : aAtoms) {
            int i = (int) Math.floor(tAtom.x() / mCellBox.mX); if (i >= mSizeX) continue;
            int j = (int) Math.floor(tAtom.y() / mCellBox.mY); if (j >= mSizeY) continue;
            int k = (int) Math.floor(tAtom.z() / mCellBox.mZ); if (k >= mSizeZ) continue;
            add(i, j, k, tAtom);
        }
    }
    private int idx(int i, int j, int k) {
        if (i<0 || i>=mSizeX || j<0 || j>=mSizeY || k<0 || k>=mSizeZ) throw new IndexOutOfBoundsException(String.format("Index: (%d, %d, %d)", i, j, k));
        return (i + mSizeX*j + mSizeX*mSizeY*k);
    }
    private void add(int i, int j, int k, A aAtom) {mCells[idx(i, j, k)].add(aAtom);}
    // 获取任意 ijk 的 link，自动判断是否是镜像的并计算镜像的附加值
    private Link link(int i, int j, int k) {
        double tDirX = 0.0, tDirY = 0.0, tDirZ = 0.0;
        boolean tIsMirror = false;
        
        if (i >= mSizeX) {tIsMirror = true; i -= mSizeX; tDirX =  mBox.mX;}
        else if (i < 0)  {tIsMirror = true; i += mSizeX; tDirX = -mBox.mX;}
        
        if (j >= mSizeY) {tIsMirror = true; j -= mSizeY; tDirY =  mBox.mY;}
        else if (j < 0)  {tIsMirror = true; j += mSizeY; tDirY = -mBox.mY;}
        
        if (k >= mSizeZ) {tIsMirror = true; k -= mSizeZ; tDirZ =  mBox.mZ;}
        else if (k < 0)  {tIsMirror = true; k += mSizeZ; tDirZ = -mBox.mZ;}
        
        return tIsMirror ? new Link(cell(i, j, k), new XYZ(tDirX, tDirY, tDirZ)) : new Link(cell(i, j, k));
    }
    private @Unmodifiable List cell(int i, int j, int k) {return mCells[idx(i, j, k)];}
    private @Unmodifiable List cell(IXYZ aXYZ) {return cell((int) Math.floor(aXYZ.x() / mCellBox.mX), (int) Math.floor(aXYZ.y() / mCellBox.mY), (int) Math.floor(aXYZ.z() / mCellBox.mZ));}
    
    
    // 获取的接口
    private @Unmodifiable List<Link> links_(IXYZ aXYZ) {return links_((int) Math.floor(aXYZ.x() / mCellBox.mX), (int) Math.floor(aXYZ.y() / mCellBox.mY), (int) Math.floor(aXYZ.z() / mCellBox.mZ));}
    private @Unmodifiable List<Link> links_(final int i, final int j, final int k) {
        return new AbstractRandomAccessList<Link>() {
            @Override public Link get(int index) {
                switch (index) {
                case  0: return link(i  , j  , k  );
                case  1: return link(i+1, j  , k  );
                case  2: return link(i-1, j  , k  );
                case  3: return link(i  , j+1, k  );
                case  4: return link(i  , j-1, k  );
                case  5: return link(i  , j  , k+1);
                case  6: return link(i  , j  , k-1);
                case  7: return link(i+1, j+1, k  );
                case  8: return link(i+1, j-1, k  );
                case  9: return link(i-1, j+1, k  );
                case 10: return link(i-1, j-1, k  );
                case 11: return link(i  , j+1, k+1);
                case 12: return link(i  , j+1, k-1);
                case 13: return link(i  , j-1, k+1);
                case 14: return link(i  , j-1, k-1);
                case 15: return link(i+1, j  , k+1);
                case 16: return link(i-1, j  , k+1);
                case 17: return link(i+1, j  , k-1);
                case 18: return link(i-1, j  , k-1);
                case 19: return link(i+1, j+1, k+1);
                case 20: return link(i+1, j+1, k-1);
                case 21: return link(i+1, j-1, k+1);
                case 22: return link(i+1, j-1, k-1);
                case 23: return link(i-1, j+1, k+1);
                case 24: return link(i-1, j+1, k-1);
                case 25: return link(i-1, j-1, k+1);
                case 26: return link(i-1, j-1, k-1);
                default: throw new IndexOutOfBoundsException(String.format("Index: %d", index));
                }
            }
            @Override public int size() {return 27;}
        };
    }
    
    
    // Link 类，多存储一个 mDirection 来标记镜像偏移，避免重复创建对象
    public static final class Link {
        private final @Unmodifiable List mCell;
        private final @Nullable XYZ mDirection;
        private Link(List aSubCell) {this(aSubCell, null);}
        private Link(List aSubCell, @Nullable XYZ aDirection) {
            mCell = aSubCell;
            mDirection = aDirection;
        }
        public boolean isMirror() {return mDirection!=null;}
        public XYZ direction() {return mDirection;}
    }
    
    @FunctionalInterface
    public interface ILinkedCellDo<A extends IXYZ> {
        void run(A aAtom, Link aLink);
    }
    /** 现在改为 for-each 的形式来避免单一返回值的问题 */
    public void forEachNeighbor(IXYZ aXYZ, ILinkedCellDo<A> aLinkedCellDo) {
        for (Link tLink : links_(aXYZ)) for (Object tAtom : tLink.mCell) aLinkedCellDo.run((A)tAtom, tLink);
    }
}
