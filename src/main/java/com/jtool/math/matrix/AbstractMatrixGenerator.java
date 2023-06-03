package com.jtool.math.matrix;

import com.jtool.code.ISetIterator;

import java.util.Iterator;

/**
 * 矩阵生成器的一般实现，主要实现一些重复的接口
 */
public abstract class AbstractMatrixGenerator implements IMatrixGenerator {
    @Override public IMatrix ones() {return ones(thisRowNumber_(), thisColumnNumber_());}
    @Override public IMatrix zeros() {return zeros(thisRowNumber_(), thisColumnNumber_());}
    @Override public IMatrix from(IMatrixGetter aMatrixGetter) {return from(thisRowNumber_(), thisColumnNumber_(), aMatrixGetter);}
    
    @Override public IMatrix same() {
        IMatrix rMatrix = zeros();
        final ISetIterator<Double> si = rMatrix.setIterator();
        final Iterator<Double> it = thisIterator_();
        while (si.hasNext()) si.nextAndSet(it.next());
        return rMatrix;
    }
    @Override public IMatrix ones(int aRowNum, int aColNum) {
        IMatrix rMatrix = zeros(aRowNum, aColNum);
        rMatrix.fill(1);
        return rMatrix;
    }
    @Override public IMatrix from(int aRowNum, int aColNum, IMatrixGetter aMatrixGetter) {
        IMatrix rMatrix = zeros(aRowNum, aColNum);
        rMatrix.fill(aMatrixGetter);
        return rMatrix;
    }
    
    /** stuff to override */
    protected abstract Iterator<Double> thisIterator_();
    protected abstract int thisRowNumber_();
    protected abstract int thisColumnNumber_();
    public abstract IMatrix zeros(int aRowNum, int aColNum);
}
