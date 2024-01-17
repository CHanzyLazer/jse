package jtool.code.iterator;

import jtool.code.functional.IIntegerConsumer1;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;


/**
 * 返回 int 类型的迭代器，用来避免外套类型
 * @author liqa
 */
public interface IIntegerIterator {
    boolean hasNext();
    int next();
    
    /** Iterator default stuffs */
    default void remove() {throw new UnsupportedOperationException("remove");}
    default void forEachRemaining(IIntegerConsumer1 aCon) {
        Objects.requireNonNull(aCon);
        while (hasNext()) aCon.run(next());
    }
    
    /** convert to Integer */
    default Iterator<Integer> toIterator() {
        return new Iterator<Integer>() {
            @Override public boolean hasNext() {return IIntegerIterator.this.hasNext();}
            @Override public Integer next() {return IIntegerIterator.this.next();}
            
            @Override public void remove() {IIntegerIterator.this.remove();}
            @Override public void forEachRemaining(Consumer<? super Integer> action) {IIntegerIterator.this.forEachRemaining(action::accept);}
        };
    }
}
