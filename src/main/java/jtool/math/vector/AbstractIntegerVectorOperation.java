package jtool.math.vector;

import jtool.code.iterator.IIntegerSetOnlyIterator;
import jtool.math.operation.DATA;

import java.util.Collections;
import java.util.Comparator;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;

import static jtool.code.CS.RANDOM;

public abstract class AbstractIntegerVectorOperation implements IIntegerVectorOperation {
    @Override public void fill              (int                  aRHS) {DATA.mapFill2This (thisVector_(), aRHS);}
    @Override public void fill              (IIntegerVector       aRHS) {DATA.ebeFill2This (thisVector_(), aRHS);}
    @Override public void assign            (IntSupplier          aSup) {DATA.assign2This  (thisVector_(), aSup);}
    @Override public void forEach           (IntConsumer          aCon) {DATA.forEachOfThis(thisVector_(), aCon);}
    @Override public void fill              (IIntegerVectorGetter aRHS) {
        final IIntegerVector tThis = thisVector_();
        final IIntegerSetOnlyIterator si = tThis.setIterator();
        final int tSize = tThis.size();
        for (int i = 0; i < tSize; ++i) si.nextAndSet(aRHS.get(i));
    }
    
    
    /** 排序不自己实现 */
    @Override public void sort() {
        Collections.sort(thisVector_().asList());
    }
    @Override public void sort(Comparator<? super Integer> aComp) {
        thisVector_().asList().sort(aComp);
    }
    
    @Override public final void shuffle() {shuffle(RANDOM::nextInt);}
    @Override public void shuffle(IntUnaryOperator aRng) {
        final IIntegerVector tThis = thisVector_();
        final int tSize = tThis.size();
        for (int i = tSize; i > 1; --i) {
            swap(tThis, i-1, aRng.applyAsInt(i));
        }
    }
    
    static void swap(IIntegerVector rVector, int i, int j) {
        rVector.set(i, rVector.getAndSet(j, rVector.get(i)));
    }
    
    /** stuff to override */
    protected abstract IIntegerVector thisVector_();
}
