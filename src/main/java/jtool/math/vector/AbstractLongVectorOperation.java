package jtool.math.vector;

import jtool.math.operation.DATA;

public abstract class AbstractLongVectorOperation implements ILongVectorOperation {
    @Override public double sum ()                      {return DATA.sumOfThis(thisVector_().asDouble());}
    
    /** stuff to override */
    protected abstract ILongVector thisVector_();
}
