package com.jtool.code;

import java.util.Iterator;

/**
 * 对 {@link Iterable} 的再次实现，主要用于和 Iterable 的容器进行区分，能够实现简单重载
 * @author liqa
 */
public interface IHasIterator<E> {
    Iterator<E> iterator();
}
