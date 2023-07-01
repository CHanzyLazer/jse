package com.jtool.math.table;


import com.jtool.math.matrix.IMatrix;

/**
 * 方便直接使用 csv 读取结果的数据格式
 * @author liqa
 */
public final class Table extends AbstractTable {
    private final IMatrix mMatrix;
    
    /** 从矩阵来创建 */
    public Table(String[] aHeads, IMatrix aData) {
        super(aHeads);
        mMatrix = aData;
    }
    public Table(int aColNum, IMatrix aData) {
        super(aColNum);
        mMatrix = aData;
    }
    public Table(IMatrix aData) {
        this(aData.columnNumber(), aData);
    }
    
    /** AbstractTable stuffs */
    @Override public IMatrix matrix() {return mMatrix;}
}
