package com.jtool.rareevent;

import java.util.Iterator;

/**
 * 内部使用的迭代器，除了一般的迭代，还可以返回到此步时经过的时间，用来简化时间的统计
 * @author liqa
 */
interface ITimeIterator<E> extends Iterator<E> {
    double timeConsumed();
}
