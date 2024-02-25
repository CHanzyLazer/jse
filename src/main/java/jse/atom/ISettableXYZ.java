package jse.atom;

import org.jetbrains.annotations.VisibleForTesting;

public interface ISettableXYZ extends IXYZ {
    /** 批量设置的接口，返回自身方便链式调用 */
    ISettableXYZ setX(double aX);
    ISettableXYZ setY(double aY);
    ISettableXYZ setZ(double aZ);
    
    /** Groovy stuffs */
    @VisibleForTesting default double getX() {return x();}
    @VisibleForTesting default double getY() {return y();}
    @VisibleForTesting default double getZ() {return z();}
}
