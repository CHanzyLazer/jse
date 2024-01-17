package jtool.code.iterator;

@FunctionalInterface
public interface IHasIntegerSetIterator extends IHasIntegerSetOnlyIterator {
    IIntegerSetIterator setIterator();
}
