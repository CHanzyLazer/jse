package com.jtool.code;

import java.util.Iterator;


/**
 * 带有 set 函数的迭代器，可以在迭代过程中进行设置
 * @author liqa
 * @param <E> 容器元素类型，一般有 E extends S（不强制）
 * @param <S> 用于设置的输入类型
 */
public interface ISetIterator<E, S> extends Iterator<E> {
    void set(S e);
}
