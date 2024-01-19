package jtool.math.vector;

import jtool.code.functional.ISwapper;
import jtool.code.iterator.IHasLongIterator;
import jtool.code.iterator.ILongIterator;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.function.LongUnaryOperator;

/**
 * @author liqa
 * <p> 专用的长整数向量 </p>
 * <p> 由于完全实现工作量较大，这里暂只实现用到的接口 </p>
 * <p> 当然为了后续完善的方便，结构依旧保持一致 </p>
 */
public interface ILongVector extends ISwapper, IHasLongIterator {
    /** Iterable stuffs，虽然不继承 Iterable 但是会提供相关的直接获取的接口方便直接使用 */
    ILongIterator iterator();
    
    List<Long> asList();
    IVector asVec();
    
    /** ISwapper stuffs */
    void swap(int aIdx1, int aIdx2);
    
    /** 访问和修改部分，自带的接口 */
    long get_(int aIdx);
    void set_(int aIdx, long aValue);
    long getAndSet_(int aIdx, long aValue); // 返回修改前的值
    int size();
    
    long get(int aIdx);
    long getAndSet(int aIdx, long aValue);
    void set(int aIdx, long aValue);
    
    /** 附加一些额外的单元素操作，对于 IntegerVector 由于适用范围更广，提供更多的接口 */
    void increment_(int aIdx);
    long getAndIncrement_(int aIdx);
    void decrement_(int aIdx);
    long getAndDecrement_(int aIdx);
    void add_(int aIdx, long aDelta);
    long getAndAdd_(int aIdx, long aDelta);
    void update_(int aIdx, LongUnaryOperator aOpt);
    long getAndUpdate_(int aIdx, LongUnaryOperator aOpt);
    
    void increment(int aIdx);
    long getAndIncrement(int aIdx);
    void decrement(int aIdx);
    long getAndDecrement(int aIdx);
    void add(int aIdx, long aDelta);
    long getAndAdd(int aIdx, long aDelta);
    void update(int aIdx, LongUnaryOperator aOpt);
    long getAndUpdate(int aIdx, LongUnaryOperator aOpt);
    
    /** 向量的运算操作，默认返回新的向量 */
    ILongVectorOperation operation();
    @VisibleForTesting default ILongVectorOperation opt() {return operation();}
    
    /** 增加向量基本的运算操作，现在也归入内部使用 */
    double sum();
}
