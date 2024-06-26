package jse.math.function;

import jse.code.functional.IDoubleQuaternionOperator;
import jse.code.functional.IDoubleTernaryOperator;
import jse.math.matrix.IMatrix;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * 任意一维数值函数的运算
 * @author liqa
 */
public interface IFunc2Operation {
    /** 通用的运算 */
    IFunc2 plus         (IFunc2 aRHS);
    IFunc2 minus        (IFunc2 aRHS);
    IFunc2 lminus       (IFunc2 aRHS);
    IFunc2 multiply     (IFunc2 aRHS);
    IFunc2 div          (IFunc2 aRHS);
    IFunc2 ldiv         (IFunc2 aRHS);
    IFunc2 mod          (IFunc2 aRHS);
    IFunc2 lmod         (IFunc2 aRHS);
    IFunc2 operate      (IFunc2 aRHS, DoubleBinaryOperator aOpt);
    
    IFunc2 plus         (double aRHS);
    IFunc2 minus        (double aRHS);
    IFunc2 lminus       (double aRHS);
    IFunc2 multiply     (double aRHS);
    IFunc2 div          (double aRHS);
    IFunc2 ldiv         (double aRHS);
    IFunc2 mod          (double aRHS);
    IFunc2 lmod         (double aRHS);
    IFunc2 map          (DoubleUnaryOperator aOpt);
    
    void plus2this      (IFunc2 aRHS);
    void minus2this     (IFunc2 aRHS);
    void lminus2this    (IFunc2 aRHS);
    void multiply2this  (IFunc2 aRHS);
    void div2this       (IFunc2 aRHS);
    void ldiv2this      (IFunc2 aRHS);
    void mod2this       (IFunc2 aRHS);
    void lmod2this      (IFunc2 aRHS);
    void operate2this   (IFunc2 aRHS, DoubleBinaryOperator aOpt);
    
    void plus2this      (double aRHS);
    void minus2this     (double aRHS);
    void lminus2this    (double aRHS);
    void multiply2this  (double aRHS);
    void div2this       (double aRHS);
    void ldiv2this      (double aRHS);
    void mod2this       (double aRHS);
    void lmod2this      (double aRHS);
    void map2this       (DoubleUnaryOperator aOpt);
    
    /** 这两个方法名默认是作用到自身的，这里为了保持 operation 的使用简洁不在函数名上特殊说明 */
    void fill           (double aValue);
    void fill           (IMatrix aMatrix);
    void fill           (IFunc2 aFunc2);
    void fill           (IFunc2Subs aFunc2Subs);
    
    /** 函数特有的运算，最后增加 x,y 的值传入 */
    IFunc2 operateFull      (IFunc2 aRHS, IDoubleQuaternionOperator aOpt);
    IFunc2 mapFull          (IDoubleTernaryOperator aOpt);
    void operateFull2this   (IFunc2 aRHS, IDoubleQuaternionOperator aOpt);
    void mapFull2this       (IDoubleTernaryOperator aOpt);
}
