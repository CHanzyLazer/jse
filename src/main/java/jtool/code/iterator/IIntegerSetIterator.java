package jtool.code.iterator;

import java.util.function.Consumer;

/**
 * 返回 int 类型的设置迭代器，用来避免外套类型
 * @author liqa
 */
public interface IIntegerSetIterator extends IIntegerIterator, IIntegerSetOnlyIterator {
    /** convert to Integer */
    default ISetIterator<Integer> toSetIterator() {
        return new ISetIterator<Integer>() {
            @Override public boolean hasNext() {return IIntegerSetIterator.this.hasNext();}
            @Override public Integer next() {return IIntegerSetIterator.this.next();}
            
            @Override public void remove() {IIntegerSetIterator.this.remove();}
            @Override public void forEachRemaining(Consumer<? super Integer> action) {IIntegerSetIterator.this.forEachRemaining(action::accept);}
            
            @Override public void nextOnly() {IIntegerSetIterator.this.nextOnly();}
            @Override public void set(Integer aValue) {IIntegerSetIterator.this.set(aValue);}
            @Override public void nextAndSet(Integer aValue) {IIntegerSetIterator.this.nextAndSet(aValue);}
        };
    }
}
