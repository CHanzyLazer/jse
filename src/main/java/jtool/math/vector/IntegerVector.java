package jtool.math.vector;

import jtool.code.collection.IntegerList;
import jtool.code.iterator.IIntegerIterator;
import jtool.code.iterator.IIntegerSetIterator;
import jtool.math.MathEX;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;

import static jtool.math.vector.AbstractVector.subVecRangeCheck;


/**
 * @author liqa
 * <p> 整数向量的一般实现 </p>
 */
public final class IntegerVector extends IntegerArrayVector {
    /** 提供默认的创建 */
    public static IntegerVector ones(int aSize) {
        int[] tData = new int[aSize];
        Arrays.fill(tData, 1);
        return new IntegerVector(tData);
    }
    public static IntegerVector zeros(int aSize) {return new IntegerVector(new int[aSize]);}
    
    /** 提供 builder 方式的构建 */
    public static Builder builder() {return new Builder();}
    public static Builder builder(int aInitSize) {return new Builder(aInitSize);}
    public final static class Builder extends IntegerList {
        private final static int DEFAULT_INIT_SIZE = 8;
        private Builder() {super(DEFAULT_INIT_SIZE);}
        private Builder(int aInitSize) {super(aInitSize);}
        
        public IntegerVector build() {
            return new IntegerVector(mSize, mData);
        }
    }
    
    private int mSize;
    public IntegerVector(int aSize, int[] aData) {super(aData); mSize = aSize;}
    public IntegerVector(int[] aData) {this(aData.length, aData);}
    
    /** 提供额外的接口来直接设置底层参数 */
    public IntegerVector setSize(int aSize) {mSize = MathEX.Code.toRange(0, mData.length, aSize); return this;}
    public int dataLength() {return mData.length;}
    
    /** IIntegerVector stuffs */
    @Override public int get_(int aIdx) {return mData[aIdx];}
    @Override public void set_(int aIdx, int aValue) {mData[aIdx] = aValue;}
    @Override public int getAndSet_(int aIdx, int aValue) {
        int oValue = mData[aIdx];
        mData[aIdx] = aValue;
        return oValue;
    }
    @Override public int size() {return mSize;}
    
    @Override public IntegerVector newShell() {return new IntegerVector(mSize, null);}
    @Override public int @Nullable[] getIfHasSameOrderData(Object aObj) {
        if (aObj instanceof IntegerVector) return ((IntegerVector)aObj).mData;
        if (aObj instanceof ShiftIntegerVector) return ((ShiftIntegerVector)aObj).mData;
        if (aObj instanceof IntegerList) return ((IntegerList)aObj).internalData();
        if (aObj instanceof int[]) return (int[])aObj;
        return null;
    }
    
    /** Optimize stuffs，subVec 切片直接返回  {@link ShiftIntegerVector} */
    @Override public IntegerArrayVector subVec(final int aFromIdx, final int aToIdx) {
        subVecRangeCheck(aFromIdx, aToIdx, mSize);
        return aFromIdx==0 ? new IntegerVector(aToIdx, mData) : new ShiftIntegerVector(aToIdx-aFromIdx, aFromIdx, mData);
    }
    
    /** Optimize stuffs，重写加速遍历 */
    @Override public IIntegerVectorOperation operation() {
        return new IntegerArrayVectorOperation_() {
            @Override public void fill(IIntegerVectorGetter aRHS) {
                for (int i = 0; i < mSize; ++i) mData[i] = aRHS.get(i);
            }
            @Override public void assign(IntSupplier aSup) {
                for (int i = 0; i < mSize; ++i) mData[i] = aSup.getAsInt();
            }
            @Override public void forEach(IntConsumer aCon) {
                for (int i = 0; i < mSize; ++i) aCon.accept(mData[i]);
            }
        };
    }
    
    /** Optimize stuffs，重写加速这些操作 */
    @Override public void increment_(int aIdx) {++mData[aIdx];}
    @Override public int getAndIncrement_(int aIdx) {return mData[aIdx]++;}
    @Override public void decrement_(int aIdx) {--mData[aIdx];}
    @Override public int getAndDecrement_(int aIdx) {return mData[aIdx]--;}
    
    @Override public void add_(int aIdx, int aDelta) {mData[aIdx] += aDelta;}
    @Override public int getAndAdd_(int aIdx, int aDelta) {
        int tValue = mData[aIdx];
        mData[aIdx] += aDelta;
        return tValue;
    }
    @Override public void update_(int aIdx, IntUnaryOperator aOpt) {
        mData[aIdx] = aOpt.applyAsInt(mData[aIdx]);
    }
    @Override public int getAndUpdate_(int aIdx, IntUnaryOperator aOpt) {
        int tValue = mData[aIdx];
        mData[aIdx] = aOpt.applyAsInt(tValue);
        return tValue;
    }
    
    /** Optimize stuffs，重写迭代器来提高遍历速度（主要是省去隐函数的调用，以及保持和矩阵相同的写法格式）*/
    @Override public IIntegerIterator iterator() {
        return new IIntegerIterator() {
            private int mIdx = 0;
            @Override public boolean hasNext() {return mIdx < mSize;}
            @Override public int next() {
                if (hasNext()) {
                    int tNext = mData[mIdx];
                    ++mIdx;
                    return tNext;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }
    @Override public IIntegerSetIterator setIterator() {
        return new IIntegerSetIterator() {
            private int mIdx = 0, oIdx = -1;
            @Override public boolean hasNext() {return mIdx < mSize;}
            @Override public void set(int aValue) {
                if (oIdx < 0) throw new IllegalStateException();
                mData[oIdx] = aValue;
            }
            @Override public int next() {
                if (hasNext()) {
                    oIdx = mIdx;
                    ++mIdx;
                    return mData[oIdx];
                } else {
                    throw new NoSuchElementException();
                }
            }
            @Override public void nextOnly() {
                if (hasNext()) {
                    oIdx = mIdx;
                    ++mIdx;
                } else {
                    throw new NoSuchElementException();
                }
            }
            /** 高性能接口重写来进行专门优化 */
            @Override public void nextAndSet(int aValue) {
                if (hasNext()) {
                    oIdx = mIdx;
                    ++mIdx;
                    mData[oIdx] = aValue;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }
}
