package jse.atom;

import org.jetbrains.annotations.NotNull;

public class NormalBox implements IBox {
    private final @NotNull IXYZ mBox;
    public NormalBox(@NotNull IXYZ aBox) {mBox = aBox;}
    public NormalBox(double aX, double aY, double aZ) {mBox = new XYZ(aX, aY, aZ);}
    
    @Override public IXYZ a() {return new XYZ(mBox.x(), 0.0, 0.0);}
    @Override public IXYZ b() {return new XYZ(0.0, mBox.y(), 0.0);}
    @Override public IXYZ c() {return new XYZ(0.0, 0.0, mBox.z());}
    @Override public double volume() {return mBox.prod();}
    @Override public IBox copy() {return new NormalBox(mBox.copy());}
    
    @Override public double x() {return mBox.x();}
    @Override public double y() {return mBox.y();}
    @Override public double z() {return mBox.z();}
    
    @Override public XYZ toCartesian(IXYZ aDirect) {return aDirect.multiply(mBox);}
    @Override public XYZ toDirect(IXYZ aCartesian) {return aCartesian.div(mBox);}
    
    @Override public String toString() {
        return String.format("(%.4g, %.4g, %.4g)", mBox.x(), mBox.y(), mBox.z());
    }
}
