package com.jtool.lmp;

import com.jtool.atom.IXYZ;
import com.jtool.atom.XYZ;
import org.jetbrains.annotations.NotNull;

import static com.jtool.code.CS.*;
import static com.jtool.code.UT.Code.newBox;
import static com.jtool.code.UT.Code.toXYZ;

/**
 * lammps 格式的模拟盒信息
 * @author liqa
 */
public class Box {
    private final @NotNull XYZ mBoxLo, mBoxHi;
    
    public Box() {this(BOX_ONE);}
    public Box(double aSize) {this(aSize, aSize, aSize);}
    public Box(double aX, double aY, double aZ) {this(BOX_ZERO, new XYZ(aX, aY, aZ));}
    public Box(double aXlo, double aXhi, double aYlo, double aYhi, double aZlo, double aZhi) {this(new XYZ(aXlo, aYlo, aZlo), new XYZ(aXhi, aYhi, aZhi));}
    public Box(@NotNull IXYZ aBox) {this(BOX_ZERO, aBox);}
    public Box(Box aBox) {this(newBox(aBox.mBoxLo), newBox(aBox.mBoxHi));}
    public Box(@NotNull IXYZ aBoxLo, @NotNull IXYZ aBoxHi) {mBoxLo = toXYZ(newBox(aBoxLo)); mBoxHi = toXYZ(newBox(aBoxHi));}
    
    /// 获取属性
    public double xlo() {return mBoxLo.mX;}
    public double xhi() {return mBoxHi.mX;}
    public double ylo() {return mBoxLo.mY;}
    public double yhi() {return mBoxHi.mY;}
    public double zlo() {return mBoxLo.mZ;}
    public double zhi() {return mBoxHi.mZ;}
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
