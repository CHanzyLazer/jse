package jtool.code.iterator;

/**
 * 返回 int 类型的设置迭代器，用来避免外套类型
 * @author liqa
 */
public interface IIntegerSetOnlyIterator {
    boolean hasNext();
    void nextOnly();
    void set(int aValue);
    /** 高性能接口，一次完成下一步和设置过程 */
    default void nextAndSet(int aValue) {
        nextOnly();
        set(aValue);
    }
    
    /** convert to Integer */
    default ISetOnlyIterator<Integer> toSetOnlyIterator() {
        return new ISetOnlyIterator<Integer>() {
            @Override public boolean hasNext() {return IIntegerSetOnlyIterator.this.hasNext();}
            @Override public void nextOnly() {IIntegerSetOnlyIterator.this.nextOnly();}
            @Override public void set(Integer aValue) {IIntegerSetOnlyIterator.this.set(aValue);}
            @Override public void nextAndSet(Integer aValue) {IIntegerSetOnlyIterator.this.nextAndSet(aValue);}
        };
    }
}
