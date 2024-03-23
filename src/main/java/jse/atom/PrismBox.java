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
}
