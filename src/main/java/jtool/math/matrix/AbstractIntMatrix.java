package jtool.math.matrix;

import jtool.code.collection.AbstractCollections;
import jtool.code.collection.AbstractRandomAccessList;
import jtool.math.vector.IIntVector;
import jtool.math.vector.RefIntVector;

import java.util.List;

public abstract class AbstractIntMatrix implements IIntMatrix {
    /** print */
    @Override public String toString() {
        final StringBuilder rStr  = new StringBuilder();
        rStr.append(String.format("%d x %d Complex Matrix:", rowNumber(), columnNumber()));
        List<IIntVector> tRows = rows();
        for (IIntVector tRow : tRows) {
            rStr.append("\n");
            tRow.forEach(v -> rStr.append(toString_(v)));
        }
        return rStr.toString();
    }
    
    /** 转换为其他类型 */
    @Override public List<List<Integer>> asListCols() {return AbstractCollections.map(cols(), IIntVector::asList);}
    @Override public List<List<Integer>> asListRows() {return AbstractCollections.map(rows(), IIntVector::asList);}
    @Override public IIntVector asVecCol() {
        return new RefIntVector() {
            private final int mRowNum = rowNumber(), mColNum = columnNumber();
            @Override public int get_(int aIdx) {return AbstractIntMatrix.this.get_(aIdx%mRowNum, aIdx/mRowNum);}
            @Override public void set_(int aIdx, int aValue) {AbstractIntMatrix.this.set_(aIdx%mRowNum, aIdx/mRowNum, aValue);}
            @Override public int getAndSet_(int aIdx, int aValue) {return AbstractIntMatrix.this.getAndSet_(aIdx%mRowNum, aIdx/mRowNum, aValue);}
            @Override public int size() {return mRowNum * mColNum;}
//            @Override public IDoubleIterator iterator() {return iteratorCol();}
//            @Override public IDoubleSetIterator setIterator() {return setIteratorCol();}
        };
    }
    @Override public IIntVector asVecRow() {
        return new RefIntVector() {
            private final int mRowNum = rowNumber(), mColNum = columnNumber();
            @Override public int get_(int aIdx) {return AbstractIntMatrix.this.get_(aIdx/mColNum, aIdx%mColNum);}
            @Override public void set_(int aIdx, int aValue) {AbstractIntMatrix.this.set_(aIdx/mColNum, aIdx%mColNum, aValue);}
            @Override public int getAndSet_(int aIdx, int aValue) {return AbstractIntMatrix.this.getAndSet_(aIdx/mColNum, aIdx%mColNum, aValue);}
            @Override public int size() {return mRowNum * mColNum;}
//            @Override public IDoubleIterator iterator() {return iteratorRow();}
//            @Override public IDoubleSetIterator setIterator() {return setIteratorRow();}
        };
    }
    
    
    @Override public int get(int aRow, int aCol) {
        if (aRow<0 || aRow>=rowNumber() || aCol<0 || aCol>=columnNumber()) throw new IndexOutOfBoundsException(String.format("Row: %d, Col: %d", aRow, aCol));
        return get_(aRow, aCol);
    }
    @Override public int getAndSet(int aRow, int aCol, int aValue) {
        if (aRow<0 || aRow>=rowNumber() || aCol<0 || aCol>=columnNumber()) throw new IndexOutOfBoundsException(String.format("Row: %d, Col: %d", aRow, aCol));
        return getAndSet_(aRow, aCol, aValue);
    }
    @Override public void set(int aRow, int aCol, int aValue) {
        if (aRow<0 || aRow>=rowNumber() || aCol<0 || aCol>=columnNumber()) throw new IndexOutOfBoundsException(String.format("Row: %d, Col: %d", aRow, aCol));
        set_(aRow, aCol, aValue);
    }
    @Override public IMatrix.ISize size() {
        return new IMatrix.ISize() {
            @Override public int row() {return rowNumber();}
            @Override public int col() {return columnNumber();}
        };
    }
    
    
    @Override public List<IIntVector> rows() {
        return new AbstractRandomAccessList<IIntVector>() {
            @Override public int size() {return rowNumber();}
            @Override public IIntVector get(int aRow) {return row(aRow);}
        };
    }
    @Override public IIntVector row(final int aRow) {
        if (aRow<0 || aRow>=rowNumber()) throw new IndexOutOfBoundsException("Row: "+aRow);
        return new RefIntVector() {
            @Override public int get_(int aIdx) {return AbstractIntMatrix.this.get_(aRow, aIdx);}
            @Override public void set_(int aIdx, int aValue) {AbstractIntMatrix.this.set_(aRow, aIdx, aValue);}
            @Override public int getAndSet_(int aIdx, int aValue) {return AbstractIntMatrix.this.getAndSet_(aRow, aIdx, aValue);}
            @Override public int size() {return columnNumber();}
//            @Override public IDoubleIterator iterator() {return iteratorRowAt(aRow);}
//            @Override public IDoubleSetIterator setIterator() {return setIteratorRowAt(aRow);}
        };
    }
    @Override public List<IIntVector> cols() {
        return new AbstractRandomAccessList<IIntVector>() {
            @Override public int size() {return columnNumber();}
            @Override public IIntVector get(int aCol) {return col(aCol);}
        };
    }
    @Override public IIntVector col(final int aCol) {
        if (aCol<0 || aCol>=columnNumber()) throw new IndexOutOfBoundsException("Col: "+aCol);
        return new RefIntVector() {
            @Override public int get_(int aIdx) {return AbstractIntMatrix.this.get_(aIdx, aCol);}
            @Override public void set_(int aIdx, int aValue) {AbstractIntMatrix.this.set_(aIdx, aCol, aValue);}
            @Override public int getAndSet_(int aIdx, int aValue) {return AbstractIntMatrix.this.getAndSet_(aIdx, aCol, aValue);}
            @Override public int size() {return rowNumber();}
//            @Override public IDoubleIterator iterator() {return iteratorColAt(aCol);}
//            @Override public IDoubleSetIterator setIterator() {return setIteratorColAt(aCol);}
        };
    }
    
    
    /** stuff to override */
    public abstract int get_(int aRow, int aCol);
    public abstract void set_(int aRow, int aCol, int aValue);
    public abstract int getAndSet_(int aRow, int aCol, int aValue); // 返回修改前的值
    public abstract int rowNumber();
    public abstract int columnNumber();
    
    protected String toString_(int aValue) {return " "+aValue;}
}
