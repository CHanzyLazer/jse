package com.jtool.code;

/**
 * 主要用于和 Iterable 的容器进行区分，能够实现简单重载
 * @author liqa
 */
public interface IHasSetIterator<E> extends IHasIterator<E> {
    ISetIterator<E> setIterator();
}
