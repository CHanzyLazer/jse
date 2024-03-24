package jse.atom;

import org.jetbrains.annotations.VisibleForTesting;

public interface ISettableXYZ extends IXYZ {
    /** 批量设置的接口，返回自身方便链式调用 */
    ISettableXYZ setX(double aX);
    ISettableXYZ setY(double aY);
    ISettableXYZ setZ(double aZ);
    default ISettableXYZ setXYZ(double aX, double aY, double aZ) {return setX(aX).setY(aY).setZ(aZ);}
    default ISettableXYZ setXYZ(IXYZ aXYZ) {return setXYZ(aXYZ.x(), aXYZ.y(), aXYZ.z());}
    
    /** Groovy stuffs */
    @VisibleForTesting default double getX() {return x();}
    @VisibleForTesting default double getY() {return y();}
    @VisibleForTesting default double getZ() {return z();}
}
