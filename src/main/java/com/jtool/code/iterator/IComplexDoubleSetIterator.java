package com.jtool.code.iterator;

import com.jtool.math.ComplexDouble;


/**
 * 支持使用两个 double 输入的复数迭代器，用来减少外套类型的使用
 * @author liqa
 */
public interface IComplexDoubleSetIterator extends ISetIterator<ComplexDouble>, IComplexDoubleIterator, IComplexDoubleSetOnlyIterator {
    void nextOnly();
}
