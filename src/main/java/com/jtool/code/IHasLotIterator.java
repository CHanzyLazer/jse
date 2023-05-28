package com.jtool.code;

import java.util.Iterator;

/**
 * 对 {@link IFatIterable} 的再次实现，主要用于和 Iterable 的容器进行区分，能够实现简单重载
 * @author liqa
 * @param <T> 可以获取的对方适应自身的迭代器方法，一般是不能继承 Iterable 的类
 * @param <E> 容器元素类型
 */
public interface IHasLotIterator<T, E> extends IHasIterator<E>, IHasSetIterator<E> {
    Iterator<E> iterator();
    ISetIterator<E> setIterator();
    
    Iterator<E> iteratorOf(T aContainer);
}
