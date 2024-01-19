package jtool.math.matrix;

import jtool.math.vector.IIntVector;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;

/**
 * @author liqa
 * <p> 专用的整数矩阵 </p>
 * <p> 由于完全实现工作量较大，这里暂只实现用到的接口 </p>
 * <p> 当然为了后续完善的方便，结构依旧保持一致 </p>
 */
public interface IIntMatrix {
    List<List<Integer>> asListCols();
    List<List<Integer>> asListRows();
    
    IIntVector asVecCol();
    IIntVector asVecRow();
    
    /** 访问和修改部分，自带的接口 */
    int get_(int aRow, int aCol);
    void set_(int aRow, int aCol, int aValue);
    int getAndSet_(int aRow, int aCol, int aValue); // 返回修改前的值
    int rowNumber();
    int columnNumber();
    
    int get(int aRow, int aCol);
    int getAndSet(int aRow, int aCol, int aValue);
    void set(int aRow, int aCol, int aValue);
    IMatrix.ISize size();
    default @VisibleForTesting int nrows() {return rowNumber();}
    default @VisibleForTesting int ncols() {return columnNumber();}
    
    
    List<IIntVector> rows();
    IIntVector row(int aRow);
    List<IIntVector> cols();
    IIntVector col(int aCol);
}
