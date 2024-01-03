package jtool.math.matrix;

import jtool.math.ComplexDouble;
import jtool.math.IComplexDouble;
import jtool.math.vector.IComplexVector;
import jtool.math.vector.ShiftComplexVector;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;


/**
 * @author liqa
 * <p> 按照行排序的复数矩阵 </p>
 */
public final class RowComplexMatrix extends BiDoubleArrayMatrix {
    /** 提供默认的创建 */
    public static RowComplexMatrix ones(int aSize) {return ones(aSize, aSize);}
    public static RowComplexMatrix ones(int aRowNum, int aColNum) {
        double[][] tData = new double[2][aRowNum*aColNum];
        Arrays.fill(tData[0], 1.0);
        return new RowComplexMatrix(aRowNum, aColNum, tData);
    }
    public static RowComplexMatrix zeros(int aSize) {return zeros(aSize, aSize);}
    public static RowComplexMatrix zeros(int aRowNum, int aColNum) {return new RowComplexMatrix(aRowNum, aColNum, new double[2][aRowNum*aColNum]);}
    
    
    private final int mRowNum;
    private final int mColNum;
    
    public RowComplexMatrix(int aRowNum, int aColNum, double[][] aData) {
        super(aData);
        mRowNum = aRowNum;
        mColNum = aColNum;
    }
    public RowComplexMatrix(int aColNum, double[][] aData) {this(Math.min(aData[0].length, aData[1].length)/aColNum, aColNum, aData);}
    
    
    /** IComplexMatrix stuffs */
    @Override public ComplexDouble get_(int aRow, int aCol) {int tIdx = aCol + aRow*mColNum; return new ComplexDouble(mData[0][tIdx], mData[1][tIdx]);}
    @Override public double getReal_(int aRow, int aCol) {return mData[0][aCol + aRow*mColNum];}
    @Override public double getImag_(int aRow, int aCol) {return mData[1][aCol + aRow*mColNum];}
    @Override public void set_(int aRow, int aCol, IComplexDouble aValue) {int tIdx = aCol + aRow*mColNum; mData[0][tIdx] = aValue.real(); mData[1][tIdx] = aValue.imag();}
    @Override public void set_(int aRow, int aCol, ComplexDouble aValue) {int tIdx = aCol + aRow*mColNum; mData[0][tIdx] = aValue.mReal; mData[1][tIdx] = aValue.mImag;}
    @Override public void set_(int aRow, int aCol, double aValue) {int tIdx = aCol + aRow*mColNum; mData[0][tIdx] = aValue; mData[1][tIdx] = 0.0;}
    @Override public void setReal_(int aRow, int aCol, double aReal) {mData[0][aCol + aRow*mColNum] = aReal;}
    @Override public void setImag_(int aRow, int aCol, double aImag) {mData[1][aCol + aRow*mColNum] = aImag;}
    public ComplexDouble getAndSet_(int aRow, int aCol, IComplexDouble aValue) {int tIdx = aCol + aRow*mColNum; ComplexDouble oValue = new ComplexDouble(mData[0][tIdx], mData[1][tIdx]); mData[0][tIdx] = aValue.real(); mData[1][tIdx] = aValue.imag(); return oValue;}
    public ComplexDouble getAndSet_(int aRow, int aCol, ComplexDouble aValue) {int tIdx = aCol + aRow*mColNum; ComplexDouble oValue = new ComplexDouble(mData[0][tIdx], mData[1][tIdx]); mData[0][tIdx] = aValue.mReal; mData[1][tIdx] = aValue.mImag; return oValue;}
    public ComplexDouble getAndSet_(int aRow, int aCol, double aValue) {int tIdx = aCol + aRow*mColNum; ComplexDouble oValue = new ComplexDouble(mData[0][tIdx], mData[1][tIdx]); mData[0][tIdx] = aValue; mData[1][tIdx] = 0.0; return oValue;}
    @Override public double getAndSetReal_(int aRow, int aCol, double aReal) {int tIdx = aCol + aRow*mColNum; double oReal = mData[0][tIdx]; mData[0][tIdx] = aReal; return oReal;}
    @Override public double getAndSetImag_(int aRow, int aCol, double aImag) {int tIdx = aCol + aRow*mColNum; double oImag = mData[1][tIdx]; mData[1][tIdx] = aImag; return oImag;}
    @Override public int rowNumber() {return mRowNum;}
    @Override public int columnNumber() {return mColNum;}
    
    @Override public RowComplexMatrix newShell() {return new RowComplexMatrix(mRowNum, mColNum, null);}
    @Override public double @Nullable[][] getIfHasSameOrderData(Object aObj) {
        // 只有同样是 RowMatrix 并且列数相同才会返回 mData
        if (aObj instanceof RowComplexMatrix && ((RowComplexMatrix)aObj).mColNum == mColNum) return ((RowComplexMatrix)aObj).mData;
        return null;
    }
    
    
    /** Optimize stuffs，重写这个提高行向的索引速度 */
    @Override public IComplexVector row(final int aRow) {
        if (aRow<0 || aRow>=rowNumber()) throw new IndexOutOfBoundsException("Row: "+aRow);
        return new ShiftComplexVector(mColNum, aRow*mColNum, mData);
    }
}
