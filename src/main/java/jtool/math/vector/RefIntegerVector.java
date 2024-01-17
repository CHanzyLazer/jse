package jtool.math.vector;

/**
 * 一般向量的接口的默认实现，实际返回向量类型为 {@link IntegerVector}，用来方便实现抽象的向量
 * @author liqa
 */
public abstract class RefIntegerVector extends AbstractIntegerVector {
    
    /** stuff to override */
    public abstract int get_(int aIdx);
    public void set_(int aIdx, int aValue) {throw new UnsupportedOperationException("set");}
    public int getAndSet_(int aIdx, int aValue) {
        int oValue = get_(aIdx);
        set_(aIdx, aValue);
        return oValue;
    }
    public abstract int size();
}
