package jse.math;


/**
 * 复数类，实际是类似结构体的设计，因此所有成员直接 public，
 * 为了使用方便这里不使用常规结构使用的命名规范
 * @author liqa
 */
public final class ComplexDouble extends AbstractSettableComplexDouble {
    public double mReal;
    public double mImag;
    public ComplexDouble(double aReal, double aImag) {mReal = aReal; mImag = aImag;}
    public ComplexDouble(double aReal              ) {this(aReal, 0.0);}
    public ComplexDouble(                          ) {this(0.0, 0.0);}
    public ComplexDouble(IComplexDouble aValue     ) {this(aValue.real(), aValue.imag());}
    public ComplexDouble(ComplexDouble aValue      ) {this(aValue.mReal, aValue.mImag);}
    
    @Override public double real() {return mReal;}
    @Override public double imag() {return mImag;}
    @Override public void setReal(double aReal) {mReal = aReal;}
    @Override public void setImag(double aImag) {mImag = aImag;}
    @Override public void setRealImag(double aReal, double aImag) {mReal = aReal; mImag = aImag;}
    
    
    /** 提供一些常见的复数运算 */
    @Override public ComplexDouble plus(double aReal, double aImag) {return new ComplexDouble(mReal+aReal, mImag+aImag);}
    @Override public ComplexDouble plus(double aReal) {return new ComplexDouble(mReal+aReal, mImag);}
    @Override public void plus2this(double aReal, double aImag) {mReal += aReal; mImag += aImag;}
    @Override public void plus2this(double aReal) {mReal += aReal;}
    
    @Override public ComplexDouble minus(double aReal, double aImag) {return new ComplexDouble(mReal-aReal, mImag-aImag);}
    @Override public ComplexDouble minus(double aReal) {return new ComplexDouble(mReal-aReal, mImag);}
    @Override public void minus2this(double aReal, double aImag) {mReal -= aReal; mImag -= aImag;}
    @Override public void minus2this(double aReal) {mReal -= aReal;}
    
    @Override public ComplexDouble lminus(double aReal, double aImag) {return new ComplexDouble(aReal-mReal, aImag-mImag);}
    @Override public ComplexDouble lminus(double aReal) {return new ComplexDouble(aReal-mReal, -mImag);}
    @Override public void lminus2this(double aReal, double aImag) {mReal = aReal-mReal; mImag = aImag-mImag;}
    @Override public void lminus2this(double aReal) {mReal = aReal-mReal; mImag = -mImag;}
    
    @Override public ComplexDouble multiply(double aReal, double aImag) {
        return new ComplexDouble(mReal*aReal - mImag*aImag, mImag*aReal + mReal*aImag);
    }
    @Override public ComplexDouble multiply(double aReal) {
        return new ComplexDouble(mReal*aReal, mImag*aReal);
    }
    @Override public void multiply2this(double aReal, double aImag) {
        final double lReal = mReal, lImag = mImag;
        mReal = lReal*aReal - lImag*aImag;
        mImag = lImag*aReal + lReal*aImag;
    }
    @Override public void multiply2this(double aReal) {
        mReal *= aReal; mImag *= aReal;
    }
    
    @Override public ComplexDouble div(double aReal, double aImag) {
        final double lReal = mReal, lImag = mImag;
        final double div = aReal*aReal + aImag*aImag;
        return new ComplexDouble((lReal*aReal + lImag*aImag)/div, (lImag*aReal - lReal*aImag)/div);
    }
    @Override public ComplexDouble div(double aReal) {
        return new ComplexDouble(mReal/aReal, mImag/aReal);
    }
    @Override public void div2this(double aReal, double aImag) {
        final double lReal = mReal, lImag = mImag;
        final double div = aReal*aReal + aImag*aImag;
        mReal = (lReal*aReal + lImag*aImag)/div;
        mImag = (lImag*aReal - lReal*aImag)/div;
    }
    @Override public void div2this(double aReal) {
        mReal /= aReal; mImag /= aReal;
    }
    
    @Override public ComplexDouble ldiv(double aReal, double aImag) {
        final double lReal = mReal, lImag = mImag;
        final double div = lReal*lReal + lImag*lImag;
        return new ComplexDouble((aReal*lReal + aImag*lImag)/div, (aImag*lReal - aReal*lImag)/div);
    }
    @Override public ComplexDouble ldiv(double aReal) {
        final double lReal = mReal, lImag = mImag;
        final double div = lReal*lReal + lImag*lImag;
        return new ComplexDouble((aReal*lReal)/div, (-aReal*lImag)/div);
    }
    @Override public void ldiv2this(double aReal, double aImag) {
        final double lReal = mReal, lImag = mImag;
        final double div = lReal*lReal + lImag*lImag;
        mReal = (aReal*lReal + aImag*lImag)/div;
        mImag = (aImag*lReal - aReal*lImag)/div;
    }
    @Override public void ldiv2this(double aReal) {
        final double lReal = mReal, lImag = mImag;
        final double div = lReal*lReal + lImag*lImag;
        mReal = (aReal*lReal)/div;
        mImag = (-aReal*lImag)/div;
    }
    @Override public ComplexDouble negative() {return new ComplexDouble(-mReal, -mImag);}
    @Override public void negative2this() {mReal = -mReal; mImag = -mImag;}
    
    @Override public double norm() {return MathEX.Fast.hypot(mReal, mImag);}
    @Override public double phase() {return MathEX.Fast.atan2(mImag, mReal);}
    @Override public ComplexDouble conj() {return new ComplexDouble(mReal, -mImag);}
    @Override public void conj2this() {mImag = -mImag;}
}
