package jse.code.iterator;

import jse.code.functional.IBooleanConsumer;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;


/**
 * 返回 boolean 类型的迭代器，用来避免外套类型
 * @author liqa
 */
public interface IBooleanIterator {
    boolean hasNext();
    boolean next();
    
    /** Iterator default stuffs */
    default void remove() {throw new UnsupportedOperationException("remove");}
    default void forEachRemaining(IBooleanConsumer aCon) {
        Objects.requireNonNull(aCon);
        while (hasNext()) aCon.accept(next());
    }
    
    /** convert to Boolean */
    default Iterator<Boolean> toIterator() {
        return new Iterator<Boolean>() {
            @Override public boolean hasNext() {return IBooleanIterator.this.hasNext();}
            @Override public Boolean next() {return IBooleanIterator.this.next();}
            
            @Override public void remove() {IBooleanIterator.this.remove();}
            @Override public void forEachRemaining(Consumer<? super Boolean> action) {IBooleanIterator.this.forEachRemaining(action::accept);}
        };
    }
    
    default IDoubleIterator asDouble() {
        return new IDoubleIterator() {
            @Override public boolean hasNext() {return IBooleanIterator.this.hasNext();}
            @Override public double next() {return IBooleanIterator.this.next() ? 1.0 : 0.0;}
        };
    }
    default IIntIterator asInt() {
        return new IIntIterator() {
            @Override public boolean hasNext() {return IBooleanIterator.this.hasNext();}
            @Override public int next() {return IBooleanIterator.this.next() ? 1 : 0;}
        };
    }
}
