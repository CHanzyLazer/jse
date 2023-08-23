package com.jtool.code.filter;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@FunctionalInterface
public interface IFilter<T> {
    boolean accept(T aInput);
    
    default <R extends T> Iterable<R> filter(Iterable<R> aIterable) {return filter(aIterable, this);}
    default <R extends T> List<R> fixedFilter(Iterable<R> aIterable) {return fixedFilter(aIterable, this);}
    
    /**
     * 提供通用的执行过滤的接口
     * @author liqa
     */
    static <T> Iterable<T> filter(final Iterable<T> aIterable, final IFilter<? super T> aFilter) {
        return () -> new Iterator<T>() {
            private final Iterator<T> mIt = aIterable.iterator();
            private boolean mNextValid = false;
            private @Nullable T mNext = null;
            
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
            @Override public T next() {
                if (hasNext()) {
                    T tNext = mNext;
                    mNext = null;
                    mNextValid = false; // 设置非法表示此时不再有 Next
                    return tNext;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }
    static <T> List<T> fixedFilter(Iterable<? extends T> aIterable, IFilter<? super T> aFilter) {
        List<T> rList = new ArrayList<>();
        for (T tValue : aIterable) if (aFilter.accept(tValue)) rList.add(tValue);
        return rList;
    }
}
