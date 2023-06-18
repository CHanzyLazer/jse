package com.jtool.code.iterator;

import com.jtool.math.ComplexDouble;

import java.util.Iterator;

/**
 * 支持将下一步和获取数据分开，然后分别获取实部和虚部，用来减少外套类型的使用
 * @author liqa
 */
public interface IComplexDoubleIterator extends Iterator<ComplexDouble> {
    void nextOnly();
    double real();
    double imag();
}
