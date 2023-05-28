package com.jtool.math.table;


import com.jtool.math.matrix.AbstractMatrix;
import com.jtool.math.vector.IVector;
import com.jtool.math.vector.Vector;

import java.util.*;

/**
 * 方便直接使用 csv 读取结果的数据格式
 * @author liqa
 */
public final class Table extends AbstractMatrix implements IMatrixTable {
    private final String[] mHands;
    private final List<double[]> mData;
    private final Map<String, Integer> mHand2Idx;
    private final boolean mNoHand;
    
    public Table(String[] aHands, List<double[]> aData) {
        mNoHand = false;
        mHands = aHands; mData = aData;
        mHand2Idx = new HashMap<>();
        for (int i = 0; i < mHands.length; ++i) mHand2Idx.put(mHands[i], i);
    }
    public Table(List<double[]> aData) {
        mNoHand = true;
        mData = aData;
        int tColNum = aData.get(0).length;
        mHands = new String[tColNum];
        mHand2Idx = new HashMap<>();
        for (int i = 0; i < tColNum; ++i) {
            mHands[i] = "C"+i; // 虽然 idea 默认读取 csv 是从 1 开始，这里为了和 getCol，getRow 保持一致，还是从 0 开始
            mHand2Idx.put(mHands[i], i);
        }
    }
    
    /** ITable stuffs */
    @Override public boolean noHand() {return mNoHand;}
    @Override public List<String> hands() {return Arrays.asList(mHands);}
    @Override public IVector get(String aHand) {return col(mHand2Idx.get(aHand));}
    @Override public boolean containsHand(String aHand) {return mHand2Idx.containsKey(aHand);}
    @Override public boolean setHand(String aOldHand, String aNewHand) {
        if (mHand2Idx.containsKey(aOldHand) && !mHand2Idx.containsKey(aNewHand)) {
            int tIdx = mHand2Idx.get(aOldHand);
            mHand2Idx.put(aNewHand, tIdx);
            return true;
        } else {
            return false;
        }
    }
    
    /** AbstractMatrix stuffs */
    @Override public double get_(int aRow, int aCol) {return mData.get(aRow)[aCol];}
    @Override public void set_(int aRow, int aCol, double aValue) {mData.get(aRow)[aCol] = aValue;}
    @Override public double getAndSet_(int aRow, int aCol, double aValue) {
        double[] tRow = mData.get(aRow);
        double oValue = tRow[aCol];
        tRow[aCol] = aValue;
        return oValue;
    }
    @Override public int rowNumber() {return mData.size();}
    @Override public int columnNumber() {return mHands.length;}
    
    /** Optimize stuffs，重写这个提高行向的索引速度 */
    @Override public IVector row(final int aRow) {
        if (aRow<0 || aRow>=rowNumber()) throw new IndexOutOfBoundsException("Row: "+aRow);
        return new Vector(mHands.length, mData.get(aRow));
    }
}
