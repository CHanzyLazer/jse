package jtool.code.iterator;

import jtool.code.functional.IBooleanSupplier;

import java.util.Objects;

@FunctionalInterface
public interface IHasBooleanSetOnlyIterator {
    IBooleanSetOnlyIterator setIterator();
    
    /** Iterable like stuffs */
    default void assign(IBooleanSupplier aSup) {
        Objects.requireNonNull(aSup);
        final IBooleanSetOnlyIterator si = setIterator();
        while (si.hasNext()) si.nextAndSet(aSup.getAsBoolean());
    }
}
