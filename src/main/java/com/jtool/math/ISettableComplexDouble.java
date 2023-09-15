package com.jtool.math;


/**
 * 复数类，实际是类似结构体的设计，因此所有成员直接 public，
 * 为了使用方便这里不使用常规结构使用的命名规范
 * @author liqa
 */
public interface ISettableComplexDouble extends IComplexDouble {
    void setReal(double aReal);
    void setImag(double aImag);
    
    
    default void plus2this(IComplexDouble aComplex) {setReal(real() + aComplex.real()); setImag(imag() + aComplex.imag());}
    default void plus2this(ComplexDouble aComplex ) {setReal(real() + aComplex.mReal ); setImag(imag() + aComplex.mImag );}
    default void plus2this(double aReal           ) {setReal(real() + aReal          );                                   }
    
    default void minus2this(IComplexDouble aComplex) {setReal(real() - aComplex.real()); setImag(imag() - aComplex.imag());}
    default void minus2this(ComplexDouble aComplex ) {setReal(real() - aComplex.mReal ); setImag(imag() - aComplex.mImag );}
    default void minus2this(double aReal           ) {setReal(real() - aReal          );                                   }
    
    default void lminus2this(IComplexDouble aComplex) {setReal(aComplex.real() - real()); setImag(aComplex.imag() - imag());}
    default void lminus2this(ComplexDouble aComplex ) {setReal(aComplex.mReal  - real()); setImag(aComplex.mImag  - imag());}
    default void lminus2this(double aReal           ) {setReal(aReal           - real()); setImag(                - imag());}
    
    default void multiply2this(IComplexDouble aComplex) {
        final double lReal = real(),          lImag = imag();
        final double rReal = aComplex.real(), rImag = aComplex.imag();
        setReal(lReal*rReal - lImag*rImag);
        setImag(lImag*rReal + lReal*rImag);
    }
    default void multiply2this(ComplexDouble aComplex ) {
        final double lReal = real(),         lImag = imag();
        final double rReal = aComplex.mReal, rImag = aComplex.mImag;
        setReal(lReal*rReal - lImag*rImag);
        setImag(lImag*rReal + lReal*rImag);
    }
    default void multiply2this(double aReal           ) {
        setReal(real() * aReal); setImag(imag() * aReal);
    }
    
    default void div2this(IComplexDouble aComplex) {
        final double lReal = real(),          lImag = imag();
        final double rReal = aComplex.real(), rImag = aComplex.imag();
        final double div = rReal*rReal + rImag*rImag;
        setReal((lReal*rReal + lImag*rImag)/div);
        setImag((lImag*rReal - lReal*rImag)/div);
    }
    default void div2this(ComplexDouble aComplex ) {
        final double lReal = real(),         lImag = imag();
        final double rReal = aComplex.mReal, rImag = aComplex.mImag;
        final double div = rReal*rReal + rImag*rImag;
        setReal((lReal*rReal + lImag*rImag)/div);
        setImag((lImag*rReal - lReal*rImag)/div);
    }
    default void div2this(double aReal          ) {
        setReal(real() / aReal); setImag(imag() / aReal);
    }
    
    default void ldiv2this(IComplexDouble aComplex) {
        final double lReal = real(),          lImag = imag();
        final double rReal = aComplex.real(), rImag = aComplex.imag();
        final double div = lReal*lReal + lImag*lImag;
        setReal((rReal*lReal + rImag*lImag)/div);
        setImag((rImag*lReal - rReal*lImag)/div);
    }
    default void ldiv2this(ComplexDouble aComplex ) {
        final double lReal = real(),         lImag = imag();
        final double rReal = aComplex.mReal, rImag = aComplex.mImag;
        final double div = lReal*lReal + lImag*lImag;
        setReal((rReal*lReal + rImag*lImag)/div);
        setImag((rImag*lReal - rReal*lImag)/div);
    }
    default void ldiv2this(double aReal          ) {
        final double lReal = real(), lImag = imag();
        final double div = lReal*lReal + lImag*lImag;
        setReal((aReal*lReal)/div);
        setImag((-aReal*lImag)/div);
    }
}
