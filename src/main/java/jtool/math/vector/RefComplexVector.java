package jtool.math.vector;


/**
 * 一般向量的接口的默认实现，实际返回向量类型为 {@link ComplexVector}，用来方便实现抽象的向量
 * @author liqa
 */
public abstract class RefComplexVector extends AbstractComplexVector {
    @Override protected final IComplexVector newZeros_(int aSize) {return ComplexVector.zeros(aSize);}
    
    /** stuff to override */
    protected abstract double getReal_(int aIdx);
    protected abstract double getImag_(int aIdx);
    protected void setReal_(int aIdx, double aReal) {throw new UnsupportedOperationException("set");}
    protected void setImag_(int aIdx, double aImag) {throw new UnsupportedOperationException("set");}
    protected double getAndSetReal_(int aIdx, double aReal) {
        double oReal = getReal_(aIdx);
        setReal_(aIdx, aReal);
        return oReal;
    }
    protected double getAndSetImag_(int aIdx, double aImag) {
        double oImag = getImag_(aIdx);
        setImag_(aIdx, aImag);
        return oImag;
    }
    public abstract int size();
}
