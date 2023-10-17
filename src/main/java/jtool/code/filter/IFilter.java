package jtool.code.filter;

@FunctionalInterface
public interface IFilter<T> {
    boolean accept(T aInput);
}
