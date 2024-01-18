package jtool.code.iterator;

import java.util.Objects;
import java.util.function.IntConsumer;

@FunctionalInterface
public interface IHasIntIterator {
    IIntIterator iterator();
    
    default Iterable<Integer> iterable() {return () -> iterator().toIterator();}
    
    /** Iterable like stuffs */
    default void forEach(IntConsumer aCon) {
        Objects.requireNonNull(aCon);
        final IIntIterator it = iterator();
        while (it.hasNext()) aCon.accept(it.next());
    }
}
