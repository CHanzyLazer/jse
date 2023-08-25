package com.jtool.math;

public interface IComplexDouble {
    double real();
    double imag();
    
    /** 提供一些常见的复数运算 */
    default IComplexDouble plus(IComplexDouble aComplex   ) {return new ComplexDouble(real() + aComplex.real(), imag() + aComplex.imag());}
    default IComplexDouble plus(double aReal, double aImag) {return new ComplexDouble(real() + aReal          , imag() + aImag          );}
    default IComplexDouble plus(double aReal              ) {return new ComplexDouble(real() + aReal          , imag()                  );}
    
    default IComplexDouble minus(IComplexDouble aComplex   ) {return new ComplexDouble(real() - aComplex.real(), imag() - aComplex.imag());}
    default IComplexDouble minus(double aReal, double aImag) {return new ComplexDouble(real() - aReal          , imag() - aImag          );}
    default IComplexDouble minus(double aReal              ) {return new ComplexDouble(real() - aReal          , imag()                  );}
    
    default IComplexDouble multiply(IComplexDouble aComplex   ) {return new ComplexDouble(real()*aComplex.real() - imag()*aComplex.imag(), imag()*aComplex.real() + real()*aComplex.imag());}
    default IComplexDouble multiply(double aReal, double aImag) {return new ComplexDouble(real()*aReal           - imag()*aImag          , imag()*aReal           + real()*aImag          );}
    default IComplexDouble multiply(double aReal              ) {return new ComplexDouble(real()*aReal                                   , imag()*aReal                                   );}
    
    default IComplexDouble div(IComplexDouble aComplex   ) {double tDiv = aComplex.dot()           ; return new ComplexDouble((real()*aComplex.real() + imag()*aComplex.imag())/tDiv, (imag()*aComplex.real() - real()*aComplex.imag())/tDiv);}
    default IComplexDouble div(double aReal, double aImag) {double tDiv = aReal*aReal + aImag*aImag; return new ComplexDouble((real()*aReal           + imag()*aImag          )/tDiv, (imag()*aReal           - real()*aImag          )/tDiv);}
    default IComplexDouble div(double aReal              ) {return new ComplexDouble(real()/aReal, imag()/aReal);}
    
    default double abs() {return MathEX.Fast.sqrt(real()*real() + imag()*imag());}
    default double dot() {return real()*real() + imag()*imag();}
}
