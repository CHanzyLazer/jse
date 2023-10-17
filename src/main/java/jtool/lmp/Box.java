package jtool.lmp;

import jtool.atom.IXYZ;
import jtool.atom.XYZ;
import org.jetbrains.annotations.NotNull;

import static jtool.code.CS.*;
import static jtool.code.UT.Code.newBox;

/**
 * lammps 格式的模拟盒信息
 * @author liqa
 */
public class Box {
    private final @NotNull IXYZ mBoxLo, mBoxHi;
    
    public Box() {this(BOX_ONE);}
    public Box(double aSize) {this(aSize, aSize, aSize);}
    public Box(double aX, double aY, double aZ) {this(BOX_ZERO, new XYZ(aX, aY, aZ));}
    public Box(double aXlo, double aXhi, double aYlo, double aYhi, double aZlo, double aZhi) {this(new XYZ(aXlo, aYlo, aZlo), new XYZ(aXhi, aYhi, aZhi));}
    public Box(@NotNull IXYZ aBox) {this(BOX_ZERO, aBox);}
    public Box(Box aBox) {this(aBox.mBoxLo, aBox.mBoxHi);}
    public Box(@NotNull IXYZ aBoxLo, @NotNull IXYZ aBoxHi) {mBoxLo = newBox(aBoxLo); mBoxHi = newBox(aBoxHi);}
    
    /// 获取属性
    public double xlo() {return mBoxLo.x();}
    public double xhi() {return mBoxHi.x();}
    public double ylo() {return mBoxLo.y();}
    public double yhi() {return mBoxHi.y();}
    public double zlo() {return mBoxLo.z();}
    public double zhi() {return mBoxHi.z();}
    public IXYZ boxLo() {return mBoxLo;}
    public IXYZ boxHi() {return mBoxHi;}
    public boolean isShifted() {return mBoxLo==BOX_ZERO;}
    public @NotNull IXYZ shiftedBox() {return mBoxLo==BOX_ZERO ? mBoxHi : mBoxHi.minus(mBoxLo);}
    
    public Box copy() {return new Box(this);}
    
    // stuff to override
    protected Type type() {return Type.NORMAL;}
    
    public enum Type {
          NORMAL
        , PRISM
    }
}
