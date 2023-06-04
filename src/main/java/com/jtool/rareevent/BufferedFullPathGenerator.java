package com.jtool.rareevent;

import com.jtool.atom.IHasAtomData;

import java.util.Iterator;

/**
 * 将 {@link IPathGenerator} 使用内部 Buffer 的方法转换成 {@link IFullPathGenerator} 方便使用
 * <p>
 * 对于相同的实例线程安全，获取到的不同路径实例之间线程安全，获取的相同路径实例线程不安全
 * @author liqa
 * @param <T> 获取到点的类型，对于 lammps 模拟则是原子结构信息 {@link IHasAtomData}
 */
public class BufferedFullPathGenerator<T> implements IFullPathGenerator<T> {
    private final IPathGenerator<T> mPathGenerator;
    public BufferedFullPathGenerator(IPathGenerator<T> aPathGenerator) {mPathGenerator = aPathGenerator;}
    
    
    @Override public ITimeIterator<T> fullPathInit() {
        return new ITimeIterator<T>() {
            private Iterator<T> mBuffer = mPathGenerator.pathInit().iterator();
            private T mNext = null;
            private double mStartTime = Double.NaN;
            
            @Override public T next() {
                while (true) {
                    if (mBuffer.hasNext()) {
                        mNext = mBuffer.next();
                        // 获取初始时间
                        if (Double.isNaN(mStartTime)) mStartTime = mPathGenerator.timeOf(mNext);
                        return mNext;
                    }
                    // 对于没有 next 的情况，依旧使用 pathInit 来初始化
                    if (mNext == null) {mBuffer = mPathGenerator.pathInit().iterator(); continue;}
                    
                    mBuffer = mPathGenerator.pathFrom(mNext).iterator();
                    // 注意存在约定，继续的路径第一个是 mNext，这里不需要这个直接掉过
                    mBuffer.next();
                }
            }
            /** 获取当前位置点从初始开始消耗的时间，如果没有调用过 next 则会抛出错误 */
            @Override public double timeConsumed() {
                if (mNext == null) throw new IllegalStateException();
                return mPathGenerator.timeOf(mNext) - mStartTime;
            }
            
            /** 完整路径永远都有 next */
            @Override public boolean hasNext() {return true;}
        };
    }
    
    /** 这里还是保持一直，第一个值为 aStart */
    @Override public ITimeIterator<T> fullPathFrom(final T aStart) {
        return new ITimeIterator<T>() {
            private Iterator<T> mBuffer = mPathGenerator.pathFrom(aStart).iterator();
            private T mNext = null;
            private final double mStartTime = mPathGenerator.timeOf(aStart);
            
            @Override public T next() {
                while (true) {
                    if (mBuffer.hasNext()) {
                        mNext = mBuffer.next();
                        return mNext;
                    }
                    // 对于 From 的情况，理论上无论如何都不会有 mNext == null 的情况
                    mBuffer = mPathGenerator.pathFrom(mNext).iterator();
                    // 注意存在约定，继续的路径第一个是 mNext，这里不需要这个直接掉过
                    mBuffer.next();
                }
            }
            /** 获取当前位置点从初始开始消耗的时间，如果没有调用过 next 则会抛出错误 */
            @Override public double timeConsumed() {
                if (mNext == null) throw new IllegalStateException();
                return mPathGenerator.timeOf(mNext) - mStartTime;
            }
            
            /** 完整路径永远都有 next */
            @Override public boolean hasNext() {return true;}
        };
    }
}
