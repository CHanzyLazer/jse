package jtool.math.vector;


/**
 * 一般向量的接口的默认实现，实际返回向量类型为 {@link ComplexVector}，用来方便实现抽象的向量
 * @author liqa
 */
public abstract class RefComplexVector extends AbstractComplexVector {
    @Override protected final IComplexVector newZeros_(int aSize) {return ComplexVector.zeros(aSize);}
    
    /** stuff to override */
    public abstract double getReal_(int aIdx);
    public abstract double getImag_(int aIdx);
    public void setReal_(int aIdx, double aReal) {throw new UnsupportedOperationException("set");}
    public void setImag_(int aIdx, double aImag) {throw new UnsupportedOperationException("set");}
    public double getAndSetReal_(int aIdx, double aReal) {
        double oReal = getReal_(aIdx);
        setReal_(aIdx, aReal);
        return oReal;
    }
    public double getAndSetImag_(int aIdx, double aImag) {
        double oImag = getImag_(aIdx);
        setImag_(aIdx, aImag);
        return oImag;
    }
    public abstract int size();
}
