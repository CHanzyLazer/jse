package com.jtool.code.filter;

import com.jtool.math.vector.IVector;
import com.jtool.math.vector.Vector;

@FunctionalInterface
public interface IDoubleFilter {
    boolean accept(double aD);
    
    /**
     * 提供通用的执行过滤的接口
     * @author liqa
     */
    static Iterable<? extends Number> filter(Iterable<? extends Number> aList, IDoubleFilter aFilter) {
        return IFilter.filter(aList, v -> aFilter.accept(v.doubleValue()));
    }
    static IVector fixedFilter(IVector aVector, IDoubleFilter aFilter) {
        Vector.Builder rBuilder = Vector.builder();
        for (double tValue : aVector.iterable()) if (aFilter.accept(tValue)) rBuilder.add(tValue);
        rBuilder.shrinkToFit();
        return rBuilder.build();
    }
}
