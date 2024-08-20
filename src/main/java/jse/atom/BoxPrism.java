package jse.atom;

import jse.math.MathEX;
import org.jetbrains.annotations.NotNull;

public final class BoxPrism implements IBox {
    private final @NotNull XYZ mA, mB, mC;
    public BoxPrism(@NotNull IXYZ aA, @NotNull IXYZ aB, @NotNull IXYZ aC) {
        mA = new XYZ(aA); mB = new XYZ(aB); mC = new XYZ(aC);
        // 现在直接在创建时初始化缓存，从根本上杜绝线程读取不安全的问题
        initCache_();
    }
    public BoxPrism(double aAx, double aAy, double aAz, double aBx, double aBy, double aBz, double aCx, double aCy, double aCz) {
        mA = new XYZ(aAx, aAy, aAz);
        mB = new XYZ(aBx, aBy, aBz);
        mC = new XYZ(aCx, aCy, aCz);
        // 现在直接在创建时初始化缓存，从根本上杜绝线程读取不安全的问题
        initCache_();
    }
    
    @Override public boolean isLmpStyle() {return false;}
    @Override public boolean isPrism() {return true;}
    
    @Override public double ax() {return mA.mX;}
    @Override public double ay() {return mA.mY;}
    @Override public double az() {return mA.mZ;}
    @Override public double bx() {return mB.mX;}
    @Override public double by() {return mB.mY;}
    @Override public double bz() {return mB.mZ;}
    @Override public double cx() {return mC.mX;}
    @Override public double cy() {return mC.mY;}
    @Override public double cz() {return mC.mZ;}
    
    @Override public BoxPrism copy() {return new BoxPrism(mA, mB, mC);}
    
    @Override public String toString() {
        return String.format("a: (%.4g, %.4g, %.4g)\n", mA.mX, mA.mY, mA.mZ)
             + String.format("b: (%.4g, %.4g, %.4g)\n", mB.mX, mB.mY, mB.mZ)
             + String.format("c: (%.4g, %.4g, %.4g)"  , mC.mX, mC.mY, mC.mZ);
    }
    
    /** optimize stuffs */
    @Override public double volume() {return mA.mixed(mB, mC);}
    
    /** 为了加速运算，内部会缓存中间变量，因此这个实现的 mA，mB，mC 都是不能修改的 */
    private XYZ mBC = null, mCA = null, mAB = null;
    private double mV = Double.NaN;
    private void initCache_() {
        mBC = mB.cross(mC);
        mCA = mC.cross(mA);
        mAB = mA.cross(mB);
        mV = mA.mixed(mB, mC);
    }
    @Override public void toDirect(XYZ rCartesian) {
        rCartesian.setXYZ(
            mBC.dot(rCartesian) / mV,
            mCA.dot(rCartesian) / mV,
            mAB.dot(rCartesian) / mV
        );
        // direct 需要考虑计算误差带来的出边界的问题，现在支持自动靠近所有整数值
        if (Math.abs(rCartesian.mX) < MathEX.Code.DBL_EPSILON) {
            rCartesian.mX = 0.0;
        } else {
            int tIntX = MathEX.Code.round2int(rCartesian.mX);
            if (MathEX.Code.numericEqual(rCartesian.mX, tIntX)) rCartesian.mX = tIntX;
        }
        if (Math.abs(rCartesian.mY) < MathEX.Code.DBL_EPSILON) {
            rCartesian.mY = 0.0;
        } else {
            int tIntY = MathEX.Code.round2int(rCartesian.mY);
            if (MathEX.Code.numericEqual(rCartesian.mY, tIntY)) rCartesian.mY = tIntY;
        }
        if (Math.abs(rCartesian.mZ) < MathEX.Code.DBL_EPSILON) {
            rCartesian.mZ = 0.0;
        } else {
            int tIntZ = MathEX.Code.round2int(rCartesian.mZ);
            if (MathEX.Code.numericEqual(rCartesian.mZ, tIntZ)) rCartesian.mZ = tIntZ;
        }
    }
}
