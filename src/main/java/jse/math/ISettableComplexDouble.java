package jse.math;


import org.jetbrains.annotations.VisibleForTesting;

/**
 * 复数类，实际是类似结构体的设计，因此所有成员直接 public，
 * 为了使用方便这里不使用常规结构使用的命名规范
 * @author liqa
 */
public interface ISettableComplexDouble extends IComplexDouble {
    /** 考虑到性能以及复数的简单性，不返回自身 */
    void setReal(double aReal);
    void setImag(double aImag);
    default void setRealImag(double aReal, double aImag) {setReal(aReal); setImag(aImag);}
    default void setNormPhase(double aNorm, double aPhase) {setRealImag(aNorm * MathEX.Fast.cos(aPhase), aNorm * MathEX.Fast.sin(aPhase));}
    
    /** Groovy stuffs */
    @VisibleForTesting default double getReal() {return real();}
    @VisibleForTesting default double getImag() {return imag();}
    
    default void plus2this(IComplexDouble aComplex) {plus2this(aComplex.real(), aComplex.imag());}
    default void plus2this(ComplexDouble aComplex) {plus2this(aComplex.mReal, aComplex.mImag);}
    default void plus2this(double aReal, double aImag) {setRealImag(real()+aReal, imag()+aImag);}
    default void plus2this(double aReal) {setReal(real()+aReal);}
    
    default void minus2this(IComplexDouble aComplex) {minus2this(aComplex.real(), aComplex.imag());}
    default void minus2this(ComplexDouble aComplex) {minus2this(aComplex.mReal, aComplex.mImag);}
    default void minus2this(double aReal, double aImag) {setRealImag(real()-aReal, imag()-aImag);}
    default void minus2this(double aReal) {setReal(real()-aReal);}
    
    default void lminus2this(IComplexDouble aComplex) {lminus2this(aComplex.real(), aComplex.imag());}
    default void lminus2this(ComplexDouble aComplex) {lminus2this(aComplex.mReal, aComplex.mImag);}
    default void lminus2this(double aReal, double aImag) {setRealImag(aReal-real(), aImag-imag());}
    default void lminus2this(double aReal) {setRealImag(aReal-real(), -imag());}
    
    default void multiply2this(IComplexDouble aComplex) {multiply2this(aComplex.real(), aComplex.imag());}
    default void multiply2this(ComplexDouble aComplex) {multiply2this(aComplex.mReal, aComplex.mImag);}
    default void multiply2this(double aReal, double aImag) {
        final double lReal = real(), lImag = imag();
        setRealImag(lReal*aReal - lImag*aImag, lImag*aReal + lReal*aImag);
    }
    default void multiply2this(double aReal) {
        setRealImag(real()*aReal, imag()*aReal);
    }
    
    default void div2this(IComplexDouble aComplex) {div2this(aComplex.real(), aComplex.imag());}
    default void div2this(ComplexDouble aComplex) {div2this(aComplex.mReal, aComplex.mImag);}
    default void div2this(double aReal, double aImag) {
        final double lReal = real(), lImag = imag();
        final double div = aReal*aReal + aImag*aImag;
        setRealImag((lReal*aReal + lImag*aImag)/div, (lImag*aReal - lReal*aImag)/div);
    }
    default void div2this(double aReal) {
        setRealImag(real()/aReal, imag()/aReal);
    }
    
    default void ldiv2this(IComplexDouble aComplex) {ldiv2this(aComplex.real(), aComplex.imag());}
    default void ldiv2this(ComplexDouble aComplex ) {ldiv2this(aComplex.mReal, aComplex.mImag);}
    default void ldiv2this(double aReal, double aImag) {
        final double lReal = real(), lImag = imag();
        final double div = lReal*lReal + lImag*lImag;
        setRealImag((aReal*lReal + aImag*lImag)/div, (aImag*lReal - aReal*lImag)/div);
    }
    default void ldiv2this(double aReal) {
        final double lReal = real(), lImag = imag();
        final double div = lReal*lReal + lImag*lImag;
        setRealImag((aReal*lReal)/div, (-aReal*lImag)/div);
    }
    
    default void negative2this() {setRealImag(-real(), -imag());}
    default void conj2this() {setImag(-imag());}
}
