package jse.atom;

import org.jetbrains.annotations.NotNull;

public class PrismBox implements IBox {
    private final @NotNull IXYZ mA, mB, mC;
    public PrismBox(@NotNull IXYZ aA, @NotNull IXYZ aB, @NotNull IXYZ aC) {mA = aA; mB = aB; mC = aC;}
    
    @Override public boolean isPrism() {return true;}
    @Override public IXYZ a() {return mA;}
    @Override public IXYZ b() {return mB;}
    @Override public IXYZ c() {return mC;}
    
    @Override public IBox copy() {return new PrismBox(mA.copy(), mB.copy(), mC.copy());}
    
    @Override public boolean isLmpStyle() {return false;}
    
    @Override public String toString() {
        return String.format("a: (%.4g, %.4g, %.4g)\n", mA.x(), mA.y(), mA.z())
             + String.format("b: (%.4g, %.4g, %.4g)\n", mB.x(), mB.y(), mB.z())
             + String.format("c: (%.4g, %.4g, %.4g)"  , mC.x(), mC.y(), mC.z());
    }
}
