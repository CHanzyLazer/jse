package com.jtool.code.operator;

@FunctionalInterface
public interface IOperator3<R, TA, TB, TC> {
    R cal(TA aA, TB aB, TC aC);
}
