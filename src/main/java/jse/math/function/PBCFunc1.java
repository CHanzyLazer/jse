package jse.math.function;

import jse.math.vector.IVector;
import jse.math.vector.Vector;

/**
 * @author liqa
 * <p> 一维数值函数默认实现，超出界限外认为是周期边界条件 </p>
 */
public final class PBCFunc1 extends VectorFunc1 {
    /** 在这里提供一些常用的构造 */
    public static PBCFunc1 zeros(double aX0, double aDx, int aNx) {return new PBCFunc1(aX0, aDx, Vector.zeros(aNx));}
    
    public PBCFunc1(double aX0, double aDx, Vector aF) {super(aX0, aDx, aF);}
    public PBCFunc1(IVector aX, IVector aF) {super(aX, aF);}
    
    /** DoubleArrayFunc1 stuffs */
    @Override protected double getOutL_(int aI) {
        int tNx = Nx();
        while (aI < 0) aI += tNx;
        return mData.get(aI);
    }
    @Override protected double getOutR_(int aI) {
        int tNx = Nx();
        while (aI >= tNx) aI -= tNx;
        return mData.get(aI);
    }
    @Override public int getINear(double aX) {
        int tNx = Nx();
        int tI = super.getINear(aX);
        while (tI < 0) tI += tNx;
        while (tI >= tNx) tI -= tNx;
        return tI;
    }
    
    @Override public PBCFunc1 newShell() {return new PBCFunc1(mX0, mDx, null);}
    @Override protected PBCFunc1 newInstance_(double aX0, double aDx, Vector aData) {return new PBCFunc1(aX0, aDx, aData);}
    
    
    /** 对于 PBC 函数，这些运算需要重新考虑 */
    @Override public VectorFunc1Operation operation() {
        return new VectorFunc1Operation_() {
            /** PBC 函数积分考虑右侧边界 */
            @Override public double integral() {return integral(false, true);}
            @Override public IFunc1 convolve(IFunc2Subs aConv) {return convolve(aConv, false, true);}
            @Override public IFunc1Subs refConvolve(IFunc2Subs aConv) {return refConvolve(aConv, false, true);}
            @Override public IFunc1 convolveFull(IFunc3Subs aConv) {return convolveFull(aConv, false, true);}
            @Override public IFunc1Subs refConvolveFull(IFunc3Subs aConv) {return refConvolveFull(aConv, false, true);}
        };
    }
}
