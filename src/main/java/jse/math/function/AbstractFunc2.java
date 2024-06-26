package jse.math.function;

import java.util.function.DoubleUnaryOperator;

public abstract class AbstractFunc2 implements IFunc2 {
    
    /** 附加一些额外的单元素操作，对于一般的只提供一个 update 的接口 */
    @Override public void update(int aI, int aJ, DoubleUnaryOperator aOpt) {
        rangeCheck(aI, aJ, Nx(), Ny());
        set(aI, aJ, aOpt.applyAsDouble(get(aI, aJ)));
    }
    @Override public double getAndUpdate(int aI, int aJ, DoubleUnaryOperator aOpt) {
        rangeCheck(aI, aJ, Nx(), Ny());
        double tValue = get(aI, aJ);
        set(aI, aJ, aOpt.applyAsDouble(tValue));
        return tValue;
    }
    static void rangeCheck(int aI, int aJ, int aNx, int aNy) {
        if (aI<0 || aI>=aNx) throw new IndexOutOfBoundsException("i = " + aI + ", Nx = " + aNx);
        if (aJ<0 || aJ>=aNy) throw new IndexOutOfBoundsException("j = " + aJ + ", Ny = " + aNy);
    }
    
    /** near stuffs */
    @Override public final double getNear(double aX, double aY) {return get(getINear(aX), getJNear(aY));}
    @Override public final void setNear(double aX, double aY, double aV) {set(getINear(aX), getJNear(aY), aV);}
    @Override public final void updateNear(double aX, double aY, DoubleUnaryOperator aOpt) {update(getINear(aX), getJNear(aY), aOpt);}
    @Override public final double getAndUpdateNear(double aX, double aY, DoubleUnaryOperator aOpt) {return getAndUpdate(getINear(aX), getJNear(aY), aOpt);}
    
    /** stuff to override */
    public abstract double get(int aI, int aJ);
    public abstract void set(int aI, int aJ, double aV);
    public abstract int getINear(double aX);
    public abstract int getJNear(double aY);
}
