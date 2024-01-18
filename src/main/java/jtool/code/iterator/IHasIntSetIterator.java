package jtool.code.iterator;

@FunctionalInterface
public interface IHasIntSetIterator extends IHasIntSetOnlyIterator {
    IIntSetIterator setIterator();
}
