package com.jtool.math.vector;


import com.jtool.code.functional.IChecker;
import com.jtool.code.functional.IComparator;
import com.jtool.code.functional.IDoubleOperator1;
import com.jtool.code.functional.IDoubleOperator2;

/**
 * 任意的实向量的运算
 * @author liqa
 */
public interface IVectorOperation {
    /** 通用的一些运算 */
    IVector ebePlus         (IVectorGetter aLHS, IVectorGetter aRHS);
    IVector ebeMinus        (IVectorGetter aLHS, IVectorGetter aRHS);
    IVector ebeMultiply     (IVectorGetter aLHS, IVectorGetter aRHS);
    IVector ebeDiv          (IVectorGetter aLHS, IVectorGetter aRHS);
    IVector ebeMod          (IVectorGetter aLHS, IVectorGetter aRHS);
    IVector ebeDo           (IVectorGetter aLHS, IVectorGetter aRHS, IDoubleOperator2 aOpt);
    
    IVector mapPlus         (IVectorGetter aLHS, double aRHS);
    IVector mapMinus        (IVectorGetter aLHS, double aRHS);
    IVector mapLMinus       (IVectorGetter aLHS, double aRHS);
    IVector mapMultiply     (IVectorGetter aLHS, double aRHS);
    IVector mapDiv          (IVectorGetter aLHS, double aRHS);
    IVector mapLDiv         (IVectorGetter aLHS, double aRHS);
    IVector mapMod          (IVectorGetter aLHS, double aRHS);
    IVector mapLMod         (IVectorGetter aLHS, double aRHS);
    IVector mapDo           (IVectorGetter aLHS, IDoubleOperator1 aOpt);
    
    void ebePlus2this       (IVectorGetter aRHS);
    void ebeMinus2this      (IVectorGetter aRHS);
    void ebeLMinus2this     (IVectorGetter aRHS);
    void ebeMultiply2this   (IVectorGetter aRHS);
    void ebeDiv2this        (IVectorGetter aRHS);
    void ebeLDiv2this       (IVectorGetter aRHS);
    void ebeMod2this        (IVectorGetter aRHS);
    void ebeLMod2this       (IVectorGetter aRHS);
    void ebeDo2this         (IVectorGetter aRHS, IDoubleOperator2 aOpt);
    
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
    void ebeFill2this       (IVectorGetter aRHS);
    
    double sum();
    double mean();
    double prod();
    double max();
    double min();
    double stat(IDoubleOperator2 aOpt);
    
    IVector cumsum();
    IVector cummean();
    IVector cumprod();
    IVector cummax();
    IVector cummin();
    IVector cumstat(IDoubleOperator2 aOpt);
    
    /** 获取逻辑结果的运算 */
    ILogicalVector ebeEqual         (IVectorGetter aLHS, IVectorGetter aRHS);
    ILogicalVector ebeGreater       (IVectorGetter aLHS, IVectorGetter aRHS);
    ILogicalVector ebeGreaterOrEqual(IVectorGetter aLHS, IVectorGetter aRHS);
    ILogicalVector ebeLess          (IVectorGetter aLHS, IVectorGetter aRHS);
    ILogicalVector ebeLessOrEqual   (IVectorGetter aLHS, IVectorGetter aRHS);
    
    ILogicalVector mapEqual         (IVectorGetter aLHS, double aRHS);
    ILogicalVector mapGreater       (IVectorGetter aLHS, double aRHS);
    ILogicalVector mapGreaterOrEqual(IVectorGetter aLHS, double aRHS);
    ILogicalVector mapLess          (IVectorGetter aLHS, double aRHS);
    ILogicalVector mapLessOrEqual   (IVectorGetter aLHS, double aRHS);
    
    ILogicalVector compare(IVectorGetter aRHS, IComparator aOpt);
    ILogicalVector check  (IChecker aOpt);
    
    /** 向量的一些额外的运算 */
    double dot(IVectorGetter aRHS);
    double dot2this();
    double norm();
    
    IVector reverse();
    IVector refReverse();
}
