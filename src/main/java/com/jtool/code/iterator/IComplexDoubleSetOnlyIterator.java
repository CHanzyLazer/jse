package com.jtool.code.iterator;

import com.jtool.math.IComplexDouble;

/**
 * 支持使用两个 double 输入的复数迭代器，用来减少外套类型的使用
 * @author liqa
 */
public interface IComplexDoubleSetOnlyIterator extends ISetOnlyIterator<IComplexDouble> {
    /** 额外添加使用两个 double 输入的设置接口 */
    void set(double aReal, double aImag);
    /** 高性能接口，一次完成下一步和设置过程 */
    default void nextAndSet(double aReal, double aImag) {
        nextOnly();
        set(aReal, aImag);
    }
}
