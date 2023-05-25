package com.jtool.math.operation;


import com.jtool.code.operator.IOperator1;
import com.jtool.code.operator.IOperator2;

/**
 * 任意的通用的数据运算操作，不会特地和 groovy 的重载运算符选择相同的名称，也不特地避开，也不特地考虑英文语法
 * @author liqa
 * @param <R> 返回数据类型，一般有 R extends T（不强制）
 * @param <T> 输入的数据类型
 * @param <E> 数据中单个数据的类型，一般有 E extends N（不强制）
 * @param <N> 输入的单个数据的类型
 */
public interface IDataOperation<R, T, E, N> {
    R ebeAdd        (T aLHS, T aRHS);
    R ebeMinus      (T aLHS, T aRHS);
    R ebeMultiply   (T aLHS, T aRHS);
    R ebeDivide     (T aLHS, T aRHS);
    R ebeMod        (T aLHS, T aRHS);
    R ebeDo         (T aLHS, T aRHS, IOperator2<E> aOpt);
    
    R mapAdd        (T aLHS, N aRHS);
    R mapMinus      (T aLHS, N aRHS);
    R mapLMinus     (T aLHS, N aRHS);
    R mapMultiply   (T aLHS, N aRHS);
    R mapDivide     (T aLHS, N aRHS);
    R mapLDivide    (T aLHS, N aRHS);
    R mapMod        (T aLHS, N aRHS);
    R mapLMod       (T aLHS, N aRHS);
    R mapDo         (T aLHS, IOperator1<E> aOpt);
    
    void ebeAdd2this        (T aRHS);
    void ebeMinus2this      (T aRHS);
    void ebeLMinus2this     (T aRHS);
    void ebeMultiply2this   (T aRHS);
    void ebeDivide2this     (T aRHS);
    void ebeLDivide2this    (T aRHS);
    void ebeMod2this        (T aRHS);
    void ebeLMod2this       (T aRHS);
    void ebeDo2this         (T aRHS, IOperator2<E> aOpt);
    
    void mapAdd2this        (N aRHS);
    void mapMinus2this      (N aRHS);
    void mapLMinus2this     (N aRHS);
    void mapMultiply2this   (N aRHS);
    void mapDivide2this     (N aRHS);
    void mapLDivide2this    (N aRHS);
    void mapMod2this        (N aRHS);
    void mapLMod2this       (N aRHS);
    void mapDo2this         (IOperator1<E> aOpt);
}
