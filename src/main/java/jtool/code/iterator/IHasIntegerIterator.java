package jtool.code.iterator;

import jtool.code.functional.IIntegerConsumer1;

import java.util.Objects;

@FunctionalInterface
public interface IHasIntegerIterator {
    IIntegerIterator iterator();
    
    default Iterable<Integer> iterable() {return () -> iterator().toIterator();}
    
    /** Iterable like stuffs */
    default void forEach(IIntegerConsumer1 aCon) {
        Objects.requireNonNull(aCon);
        final IIntegerIterator it = iterator();
        while (it.hasNext()) aCon.run(it.next());
    }
}
