package com.jtool.math.vector;

import com.jtool.code.ISetIterator;

import java.util.Iterator;

/**
 * 向量生成器的一般实现，主要实现一些重复的接口
 */
public abstract class AbstractVectorGenerator implements IVectorGenerator {
    @Override public IVector ones() {return ones(thisSize_());}
    @Override public IVector zeros() {return zeros(thisSize_());}
    @Override public IVector from(IVectorGetter aVectorGetter) {return from(thisSize_(), aVectorGetter);}
    @Override public IVector same() {
        IVector rVector = zeros();
        final ISetIterator<Double> si = rVector.setIterator();
        final Iterator<Double> it = thisIterator_();
        while (si.hasNext()) si.nextAndSet(it.next());
        return rVector;
    }
    
    @Override public IVector ones(int aSize) {
        IVector rVector = zeros(aSize);
        rVector.fill(1);
        return rVector;
    }
    @Override public IVector from(int aSize, IVectorGetter aVectorGetter) {
        IVector rVector = zeros(aSize);
        rVector.fill(aVectorGetter);
        return rVector;
    }
    
    
    
    @Override public IVector sequence(double aStart, double aEnd) {
        int tSize = thisSize_();
        return sequenceByStep(aStart, (aEnd-aStart)/(tSize-1), tSize);
    }
    @Override public IVector sequence(double aStart, double aStep, double aEnd) {
        int tSize = (int)Math.floor((aEnd-aStart)/aStep) + 1;
        return sequenceByStep(aStart, aStep, tSize);
    }
    
    @Override public IVector sequenceByStep(double aStart, double aStep) {return sequenceByStep(aStart, aStep, thisSize_());}
    @Override public IVector sequenceByStep(double aStart, double aStep, int aN) {
        final IVector rVector = zeros(aN);
        final ISetIterator<Double> si = rVector.setIterator();
        double tValue = aStart;
        while (si.hasNext()) {
            si.nextAndSet(tValue);
            tValue += aStep;
        }
        return rVector;
    }
    
    
    /** stuff to override */
    protected abstract Iterator<Double> thisIterator_();
    protected abstract int thisSize_();
    public abstract IVector zeros(int aSize);
}
