package com.jtool.math.function;


import com.jtool.code.functional.IDoubleOperator1;
import com.jtool.code.functional.IDoubleOperator2;
import com.jtool.code.functional.IDoubleOperator3;
import com.jtool.math.vector.IVectorSetter;

/**
 * 任意一维数值函数的运算
 * @author liqa
 */
public interface IFunc1Operation {
    /** 通用的运算 */
    IFunc1 ebePlus          (IFunc1Subs aLHS, IFunc1Subs aRHS);
    IFunc1 ebeMinus         (IFunc1Subs aLHS, IFunc1Subs aRHS);
    IFunc1 ebeMultiply      (IFunc1Subs aLHS, IFunc1Subs aRHS);
    IFunc1 ebeDiv           (IFunc1Subs aLHS, IFunc1Subs aRHS);
    IFunc1 ebeMod           (IFunc1Subs aLHS, IFunc1Subs aRHS);
    IFunc1 ebeDo            (IFunc1Subs aLHS, IFunc1Subs aRHS, IDoubleOperator2 aOpt);
    
    IFunc1 mapPlus          (IFunc1Subs aLHS, double aRHS);
    IFunc1 mapMinus         (IFunc1Subs aLHS, double aRHS);
    IFunc1 mapLMinus        (IFunc1Subs aLHS, double aRHS);
    IFunc1 mapMultiply      (IFunc1Subs aLHS, double aRHS);
    IFunc1 mapDiv           (IFunc1Subs aLHS, double aRHS);
    IFunc1 mapLDiv          (IFunc1Subs aLHS, double aRHS);
    IFunc1 mapMod           (IFunc1Subs aLHS, double aRHS);
    IFunc1 mapLMod          (IFunc1Subs aLHS, double aRHS);
    IFunc1 mapDo            (IFunc1Subs aLHS, IDoubleOperator1 aOpt);
    
    void ebePlus2this       (IFunc1Subs aRHS);
    void ebeMinus2this      (IFunc1Subs aRHS);
    void ebeLMinus2this     (IFunc1Subs aRHS);
    void ebeMultiply2this   (IFunc1Subs aRHS);
    void ebeDiv2this        (IFunc1Subs aRHS);
    void ebeLDiv2this       (IFunc1Subs aRHS);
    void ebeMod2this        (IFunc1Subs aRHS);
    void ebeLMod2this       (IFunc1Subs aRHS);
    void ebeDo2this         (IFunc1Subs aRHS, IDoubleOperator2 aOpt);
    
    void mapPlus2this       (double aRHS);
    void mapMinus2this      (double aRHS);
    void mapLMinus2this     (double aRHS);
    void mapMultiply2this   (double aRHS);
    void mapDiv2this        (double aRHS);
    void mapLDiv2this       (double aRHS);
    void mapMod2this        (double aRHS);
    void mapLMod2this       (double aRHS);
    void mapDo2this         (IDoubleOperator1 aOpt);
    
    
    void mapFill2this       (double aRHS);
    void ebeFill2this       (IFunc1Subs aRHS);
    
    
    /** 函数特有的运算，最后增加一项 x 的值传入 */
    IFunc1 ebeDoFull        (IFunc1Subs aLHS, IFunc1Subs aRHS, IDoubleOperator3 aOpt);
    IFunc1 mapDoFull        (IFunc1Subs aLHS, IDoubleOperator2 aOpt);
    void ebeDoFull2this     (IFunc1Subs aRHS, IDoubleOperator3 aOpt);
    void mapDoFull2this     (IDoubleOperator2 aOpt);
    
    /** 微分积分运算 */
    IFunc1 laplacian();
    void laplacian2Dest(IVectorSetter rDest);
    
    /**
     * 卷积运算，通过输入的卷积核来对自身函数进行卷积运算，输出得到的结果
     * <p>
     * 注意这里卷积核输入格式为 (x, k) -> out，自身函数为 f(x)，经过卷积后得到函数 g(k)
     * <p>
     * 执行的卷积运算为：g(k) = int(conv(x, k) * f(x), x);
     */
    IFunc1 convolve(IFunc2Subs aConv);
    IFunc1Subs refConvolve(IFunc2Subs aConv);
    
    /**
     * 完整的卷积运算，通过输入的卷积核来对自身函数进行卷积运算，输出得到的结果
     * <p>
     * 注意这里卷积核输入格式为 (f(x), x, k) -> out，自身函数为 f(x)，经过卷积后得到函数 g(k)
     * <p>
     * 执行的卷积运算为：g(k) = int(conv(f(x), x, k), x);
     */
    IFunc1 convolveFull(IFunc3Subs aConv);
    IFunc1Subs refConvolveFull(IFunc3Subs aConv);
    
    /** 返回峰值所在的 x 坐标 */
    double maxX();
    double minX();
}
