package com.jtool.code.operator;

@FunctionalInterface
public interface IOperator2<R, TL, TR> {
    R cal(TL aLHS, TR aRHS);
}
