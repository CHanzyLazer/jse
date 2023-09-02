package com.jtool.code.filter;

@FunctionalInterface
public interface IIndexFilter {
    boolean accept(int aIdx);
}
