package com.jtool.code.filter;

import com.jtool.code.iterator.IDoubleIterator;
import com.jtool.code.iterator.IHasDoubleIterator;
import com.jtool.math.vector.IVector;
import com.jtool.math.vector.Vector;

import java.util.List;
import java.util.NoSuchElementException;

@FunctionalInterface
public interface IDoubleFilter {
    boolean accept(double aD);
    
    default <N extends Number> Iterable<N> filter(Iterable<N> aIterable) {return filter(aIterable, this);}
    default IHasDoubleIterator filter(IHasDoubleIterator aIterable) {return filter(aIterable, this);}
    default <N extends Number> List<N> fixedFilter(Iterable<N> aIterable) {return fixedFilter(aIterable, this);}
    default IVector fixedFilter(IHasDoubleIterator aIterable) {return fixedFilter(aIterable, this);}
    
    /**
     * 提供通用的执行过滤的接口
     * @author liqa
     */
    static <N extends Number> Iterable<N> filter(Iterable<N> aIterable, final IDoubleFilter aFilter) {
        return IFilter.filter(aIterable, v -> aFilter.accept(v.doubleValue()));
    }
    static IHasDoubleIterator filter(final IHasDoubleIterator aIterable, final IDoubleFilter aFilter) {
        return () -> new IDoubleIterator() {
            private final IDoubleIterator mIt = aIterable.iterator();
            private boolean mNextValid = false;
            private double mNext = Double.NaN;
            
            @Override public boolean hasNext() {
                while (true) {
                    if (mNextValid) return true;
                    if (mIt.hasNext()) {
                        mNext = mIt.next();
                        // 过滤器通过则设为合法跳过
                        if (aFilter.accept(mNext)) {
                            mNextValid = true;
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
            }
            @Override public double next() {
                if (hasNext()) {
                    mNextValid = false; // 设置非法表示此时不再有 Next
                    return mNext;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }
    
    static <N extends Number> List<N> fixedFilter(Iterable<N> aIterable, final IDoubleFilter aFilter) {
        return IFilter.fixedFilter(aIterable, v -> aFilter.accept(v.doubleValue()));
    }
    static IVector fixedFilter(IHasDoubleIterator aIterable, IDoubleFilter aFilter) {
        Vector.Builder rBuilder = Vector.builder();
        final IDoubleIterator it = aIterable.iterator();
        while (it.hasNext()) {
            double tValue = it.next();
            if (aFilter.accept(tValue)) rBuilder.add(tValue);
        }
        rBuilder.shrinkToFit();
        return rBuilder.build();
    }
}
