package jtool.code.functional;

@FunctionalInterface
public interface IComparator {
    boolean apply(double aLHS, double RHS);
}
