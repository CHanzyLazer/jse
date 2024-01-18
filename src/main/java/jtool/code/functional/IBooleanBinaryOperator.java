package jtool.code.functional;

@FunctionalInterface
public interface IBooleanBinaryOperator {
    boolean applyAsBoolean(boolean aLHS, boolean aRHS);
}
