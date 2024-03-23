package jse.lmp;

import jse.atom.IXYZ;
import org.jetbrains.annotations.NotNull;

public class LmpBoxPrism extends LmpBox {
    private final double mXY, mXZ, mYZ;
    
    public LmpBoxPrism(double aSize, double aXY, double aXZ, double aYZ) {super(aSize); mXY = aXY; mXZ = aXZ; mYZ = aYZ;}
    public LmpBoxPrism(double aX, double aY, double aZ, double aXY, double aXZ, double aYZ) {super(aX, aY, aZ); mXY = aXY; mXZ = aXZ; mYZ = aYZ;}
    public LmpBoxPrism(double aXlo, double aXhi, double aYlo, double aYhi, double aZlo, double aZhi, double aXY, double aXZ, double aYZ) {super(aXlo, aXhi, aYlo, aYhi, aZlo, aZhi); mXY = aXY; mXZ = aXZ; mYZ = aYZ;}
    public LmpBoxPrism(@NotNull IXYZ aBox, double aXY, double aXZ, double aYZ) {super(aBox); mXY = aXY; mXZ = aXZ; mYZ = aYZ;}
    public LmpBoxPrism(@NotNull IXYZ aBoxLo, @NotNull IXYZ aBoxHi, double aXY, double aXZ, double aYZ) {super(aBoxLo, aBoxHi); mXY = aXY; mXZ = aXZ; mYZ = aYZ;}
    public LmpBoxPrism(LmpBox aBox, double aXY, double aXZ, double aYZ) {super(aBox); mXY = aXY; mXZ = aXZ; mYZ = aYZ;}
    public LmpBoxPrism(LmpBoxPrism aBoxPrism) {super(aBoxPrism); mXY = aBoxPrism.mXY; mXZ = aBoxPrism.mXZ; mYZ = aBoxPrism.mYZ;}
    
    /// 获取属性
    public final double xy() {return mXY;}
    public final double xz() {return mXZ;}
    public final double yz() {return mYZ;}
    
    @Override public LmpBoxPrism copy() {return new LmpBoxPrism(this);}
    
    @Override public String toString() {
        return String.format("{boxlo: (%.4g, %.4g, %.4g), boxhi: (%.4g, %.4g, %.4g), xy: %.4g, xz: %.4g, yz: %.4g}",
                             boxLo().x(), boxLo().y(), boxLo().z(), boxHi().x(), boxHi().y(), boxHi().z(), mXY, mXZ, mYZ);
    }
}
