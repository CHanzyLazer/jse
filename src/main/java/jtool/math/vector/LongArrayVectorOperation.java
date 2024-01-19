package jtool.math.vector;

import jtool.math.operation.ARRAY;

public abstract class LongArrayVectorOperation extends AbstractLongVectorOperation {
    @Override public double sum ()                      {LongArrayVector tThis = thisVector_(); return ARRAY.sumOfThis (tThis.internalData(), tThis.internalDataShift(), tThis.internalDataSize());}
    
    /** stuff to override */
    @Override protected abstract LongArrayVector thisVector_();
}
