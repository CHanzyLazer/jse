package jtool.code.iterator;

import jtool.code.functional.IIntegerSupplier;

import java.util.Objects;

@FunctionalInterface
public interface IHasIntegerSetOnlyIterator {
    IIntegerSetOnlyIterator setIterator();
    
    /** Iterable like stuffs */
    default void assign(IIntegerSupplier aSup) {
        Objects.requireNonNull(aSup);
        final IIntegerSetOnlyIterator si = setIterator();
        while (si.hasNext()) si.nextAndSet(aSup.get());
    }
}
