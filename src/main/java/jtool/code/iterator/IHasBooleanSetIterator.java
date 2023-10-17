package jtool.code.iterator;

@FunctionalInterface
public interface IHasBooleanSetIterator extends IHasBooleanSetOnlyIterator {
    IBooleanSetIterator setIterator();
}
