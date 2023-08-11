package com.jtool.code.iterator;

public interface IHasDoubleIterator {
    IDoubleIterator iterator();
    
    default Iterable<Double> iterable() {return () -> iterator().toIterator();}
}
