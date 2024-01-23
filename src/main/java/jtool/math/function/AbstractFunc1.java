package jtool.math.function;

import jtool.math.vector.IVector;

import java.util.Iterator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

public abstract class AbstractFunc1 implements IFunc1 {
    /** 批量修改的接口 */
    @Override public final void fill(double aValue) {operation().fill(aValue);}
    @Override public final void fill(IVector aVector) {operation().fill(aVector);}
    @Override public final void fill(IFunc1 aFunc1) {operation().fill(aFunc1);}
    @Override public final void fill(IFunc1Subs aFunc1Subs) {operation().fill(aFunc1Subs);}
    @Override public final void fill(Iterable<? extends Number> aList) {
        final Iterator<? extends Number> it = aList.iterator();
        assign(() -> it.next().doubleValue());
    }
    @Override public final void assign(DoubleSupplier aSup) {operation().assign(aSup);}
    @Override public final void forEach(DoubleConsumer aCon) {operation().forEach(aCon);}
    
    
    /** 获取结果，支持按照索引查找和按照 x 的值来查找 */
    @Override public double get(int aI) {
        if (aI<0 || aI>=Nx()) throw new IndexOutOfBoundsException(String.format("Index: %d", aI));
        return get_(aI);
    }
    /** 设置结果，简单起见只允许按照索引来设置 */
    @Override public void set(int aI, double aV) {
        if (aI<0 || aI>=Nx()) throw new IndexOutOfBoundsException(String.format("Index: %d", aI));
        set_(aI, aV);
    }
    
    
    /** 附加一些额外的单元素操作，对于一般的只提供一个 update 的接口 */
    protected void update_(int aI, DoubleUnaryOperator aOpt) {
        set_(aI, aOpt.applyAsDouble(get_(aI)));
    }
    protected double getAndUpdate_(int aI, DoubleUnaryOperator aOpt) {
        double tValue = get_(aI);
        set_(aI, aOpt.applyAsDouble(tValue));
        return tValue;
    }
    @Override public void update(int aI, DoubleUnaryOperator aOpt) {
        if (aI<0 || aI>=Nx()) throw new IndexOutOfBoundsException(String.format("Index: %d", aI));
        update_(aI, aOpt);
    }
    @Override public double getAndUpdate(int aI, DoubleUnaryOperator aOpt) {
        if (aI<0 || aI>=Nx()) throw new IndexOutOfBoundsException(String.format("Index: %d", aI));
        return getAndUpdate_(aI, aOpt);
    }
    
    
    /** stuff to override */
    protected abstract double get_(int aI);
    protected abstract void set_(int aI, double aV);
}
