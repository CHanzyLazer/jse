package jtool.code.iterator;

@FunctionalInterface
public interface IHasLongSetIterator extends IHasLongSetOnlyIterator {
    ILongSetIterator setIterator();
}
