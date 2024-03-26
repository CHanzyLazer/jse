package jse.vasp;

import jse.atom.XYZ;
import jse.math.MathEX;

public final class VaspBoxPrism extends VaspBox {
    private final double mIAy, mIAz;
    private final double mIBx, mIBz;
    private final double mICx, mICy;
    
    public VaspBoxPrism(double aIAx, double aIAy, double aIAz, double aIBx, double aIBy, double aIBz, double aICx, double aICy, double aICz, double aScale) {
        super(aIAx, aIBy, aICz, aScale);
        mIAy = aIAy; mIAz = aIAz;
        mIBx = aIBx; mIBz = aIBz;
        mICx = aICx; mICy = aICy;
    }
    public VaspBoxPrism(double aIAx, double aIAy, double aIAz, double aIBx, double aIBy, double aIBz, double aICx, double aICy, double aICz) {
        super(aIAx, aIBy, aICz);
        mIAy = aIAy; mIAz = aIAz;
        mIBx = aIBx; mIBz = aIBz;
        mICx = aICx; mICy = aICy;
    }
    VaspBoxPrism(VaspBox aVaspBox, double aIAy, double aIAz, double aIBx, double aIBz, double aICx, double aICy) {
        super(aVaspBox);
        mIAy = aIAy; mIAz = aIAz;
        mIBx = aIBx; mIBz = aIBz;
        mICx = aICx; mICy = aICy;
    }
    @SuppressWarnings("CopyConstructorMissesField")
    VaspBoxPrism(VaspBoxPrism aVaspBoxPrism) {
        super(aVaspBoxPrism);
        mIAy = aVaspBoxPrism.mIAy; mIAz = aVaspBoxPrism.mIAz;
        mIBx = aVaspBoxPrism.mIBx; mIBz = aVaspBoxPrism.mIBz;
        mICx = aVaspBoxPrism.mICx; mICy = aVaspBoxPrism.mICy;
    }
    
    /** VaspBox stuffs */
    @Override public double iay() {return mIAy;}
    @Override public double iaz() {return mIAz;}
    @Override public double ibx() {return mIBx;}
    @Override public double ibz() {return mIBz;}
    @Override public double icx() {return mICx;}
    @Override public double icy() {return mICy;}
    
    /** IBox stuffs */
    @Override public boolean isLmpStyle() {return false;}
    @Override public boolean isPrism() {return true;}
    
    @Override public VaspBoxPrism copy() {return new VaspBoxPrism(this);}
    
    
    /** 为了加速运算，内部会缓存中间变量，再修改 scale 时会让这些缓存失效 */
    private XYZ mBC = null, mCA = null, mAB = null;
    private double mV = Double.NaN;
    @Override public void toDirect(XYZ rCartesian) {
        if (mBC == null) {
            XYZ tA = XYZ.toXYZ(a());
            XYZ tB = XYZ.toXYZ(b());
            XYZ tC = XYZ.toXYZ(c());
            mBC = tB.cross(tC);
            mCA = tC.cross(tA);
            mAB = tA.cross(tB);
            mV = tA.mixed(tB, tC);
        }
        rCartesian.setXYZ(
            mBC.dot(rCartesian) / mV,
            mCA.dot(rCartesian) / mV,
            mAB.dot(rCartesian) / mV
        );
        // direct 需要考虑计算误差带来的出边界的问题
        if (Math.abs(rCartesian.mX) < MathEX.Code.DBL_EPSILON) rCartesian.mX = 0.0;
        if (Math.abs(rCartesian.mY) < MathEX.Code.DBL_EPSILON) rCartesian.mY = 0.0;
        if (Math.abs(rCartesian.mZ) < MathEX.Code.DBL_EPSILON) rCartesian.mZ = 0.0;
    }
    @Override protected void onAnyChange_() {mBC = null;}
}
