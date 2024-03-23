package jse.lmp;

import jse.atom.IXYZ;
import jse.atom.XYZ;
import org.jetbrains.annotations.NotNull;

import static jse.code.CS.*;

/**
 * lammps 格式的模拟盒信息
 * @author liqa
 */
public class LmpBox {
    private final @NotNull IXYZ mBoxLo, mBoxHi;
    
    public LmpBox() {this(BOX_ONE);}
    public LmpBox(double aSize) {this(aSize, aSize, aSize);}
    public LmpBox(double aX, double aY, double aZ) {this(XYZ_ZERO, new XYZ(aX, aY, aZ));}
    public LmpBox(double aXlo, double aXhi, double aYlo, double aYhi, double aZlo, double aZhi) {this(new XYZ(aXlo, aYlo, aZlo), new XYZ(aXhi, aYhi, aZhi));}
    public LmpBox(@NotNull IXYZ aBox) {this(XYZ_ZERO, aBox);}
    public LmpBox(LmpBox aBox) {this(aBox.mBoxLo, aBox.mBoxHi);}
    public LmpBox(@NotNull IXYZ aBoxLo, @NotNull IXYZ aBoxHi) {mBoxLo = newBox(aBoxLo); mBoxHi = newBox(aBoxHi);}
    
    /// 获取属性
    public final double xlo() {return mBoxLo.x();}
    public final double xhi() {return mBoxHi.x();}
    public final double ylo() {return mBoxLo.y();}
    public final double yhi() {return mBoxHi.y();}
    public final double zlo() {return mBoxLo.z();}
    public final double zhi() {return mBoxHi.z();}
    public final IXYZ boxLo() {return mBoxLo;}
    public final IXYZ boxHi() {return mBoxHi;}
    public final boolean isShifted() {return mBoxLo== XYZ_ZERO;}
    public final @NotNull IXYZ shiftedBox() {return mBoxLo== XYZ_ZERO ? mBoxHi : mBoxHi.minus(mBoxLo);}
    
    public LmpBox copy() {return new LmpBox(this);}
    
    @Override public String toString() {
        return String.format("{boxlo: (%.4g, %.4g, %.4g), boxhi: (%.4g, %.4g, %.4g)}", mBoxLo.x(), mBoxLo.y(), mBoxLo.z(), mBoxHi.x(), mBoxHi.y(), mBoxHi.z());
    }
}
