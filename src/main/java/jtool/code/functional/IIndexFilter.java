package jtool.code.functional;

@FunctionalInterface
public interface IIndexFilter {
    boolean accept(int aIdx);
}
