package jtool.math.vector;

import jtool.code.iterator.ILongIterator;
import jtool.math.MathEX;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.LongUnaryOperator;


/**
 * @author liqa
 * <p> 整数向量的一般实现 </p>
 */
public final class LongVector extends LongArrayVector {
    /** 提供默认的创建 */
    public static LongVector ones(int aSize) {
        long[] tData = new long[aSize];
        Arrays.fill(tData, 1);
        return new LongVector(tData);
    }
    public static LongVector zeros(int aSize) {return new LongVector(new long[aSize]);}
    
//    /** 提供 builder 方式的构建 */
//    public static Builder builder() {return new Builder();}
//    public static Builder builder(int aInitSize) {return new Builder(aInitSize);}
//    public final static class Builder extends IntList {
//        private final static int DEFAULT_INIT_SIZE = 8;
//        private Builder() {super(DEFAULT_INIT_SIZE);}
//        private Builder(int aInitSize) {super(aInitSize);}
//
//        public LongVector build() {
//            return new LongVector(mSize, mData);
//        }
//    }
    
    private int mSize;
    public LongVector(int aSize, long[] aData) {super(aData); mSize = aSize;}
    public LongVector(long[] aData) {this(aData.length, aData);}
    
    /** 提供额外的接口来直接设置底层参数 */
    public LongVector setSize(int aSize) {mSize = MathEX.Code.toRange(0, mData.length, aSize); return this;}
    public int dataLength() {return mData.length;}
    
    /** IIntegerVector stuffs */
    @Override protected long get_(int aIdx) {return mData[aIdx];}
    @Override protected void set_(int aIdx, long aValue) {mData[aIdx] = aValue;}
    @Override protected long getAndSet_(int aIdx, long aValue) {
        long oValue = mData[aIdx];
        mData[aIdx] = aValue;
        return oValue;
    }
    @Override public int size() {return mSize;}
    
    @Override public LongVector newShell() {return new LongVector(mSize, null);}
    @Override public long @Nullable[] getIfHasSameOrderData(Object aObj) {
        if (aObj instanceof LongVector) return ((LongVector)aObj).mData;
        if (aObj instanceof long[]) return (long[])aObj;
        return null;
    }
    
    /** Optimize stuffs，重写加速这些操作 */
    @Override protected void swap_(int aIdx1, int aIdx2) {
        long tValue = mData[aIdx2];
        mData[aIdx2] = mData[aIdx1];
        mData[aIdx1] = tValue;
    }
    
    @Override protected void increment_(int aIdx) {++mData[aIdx];}
    @Override protected long getAndIncrement_(int aIdx) {return mData[aIdx]++;}
    @Override protected void decrement_(int aIdx) {--mData[aIdx];}
    @Override protected long getAndDecrement_(int aIdx) {return mData[aIdx]--;}
    
    @Override protected void add_(int aIdx, long aDelta) {mData[aIdx] += aDelta;}
    @Override protected long getAndAdd_(int aIdx, long aDelta) {
        long tValue = mData[aIdx];
        mData[aIdx] += aDelta;
        return tValue;
    }
    @Override protected void update_(int aIdx, LongUnaryOperator aOpt) {
        mData[aIdx] = aOpt.applyAsLong(mData[aIdx]);
    }
    @Override protected long getAndUpdate_(int aIdx, LongUnaryOperator aOpt) {
        long tValue = mData[aIdx];
        mData[aIdx] = aOpt.applyAsLong(tValue);
        return tValue;
    }
    
    /** Optimize stuffs，重写迭代器来提高遍历速度（主要是省去隐函数的调用，以及保持和矩阵相同的写法格式）*/
    @Override public ILongIterator iterator() {
        return new ILongIterator() {
            private int mIdx = 0;
            @Override public boolean hasNext() {return mIdx < mSize;}
            @Override public long next() {
                if (hasNext()) {
                    long tNext = mData[mIdx];
                    ++mIdx;
                    return tNext;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }
}
