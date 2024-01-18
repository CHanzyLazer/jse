package jtool.code.iterator;

import java.util.Objects;
import java.util.function.IntSupplier;

@FunctionalInterface
public interface IHasIntegerSetOnlyIterator {
    IIntegerSetOnlyIterator setIterator();
    
    /** Iterable like stuffs */
    default void assign(IntSupplier aSup) {
        Objects.requireNonNull(aSup);
        final IIntegerSetOnlyIterator si = setIterator();
        while (si.hasNext()) si.nextAndSet(aSup.getAsInt());
    }
}
