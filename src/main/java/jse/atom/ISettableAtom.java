package jse.atom;

import org.jetbrains.annotations.VisibleForTesting;

public interface ISettableAtom extends IAtom, ISettableXYZ {
    /** 返回自身用于链式调用 */
    ISettableAtom setX(double aX);
    ISettableAtom setY(double aY);
    ISettableAtom setZ(double aZ);
    ISettableAtom setID(int aID);
    ISettableAtom setType(int aType);
    
    default ISettableAtom setVx(double aVx) {throw new UnsupportedOperationException("setVx");}
    default ISettableAtom setVy(double aVy) {throw new UnsupportedOperationException("setVy");}
    default ISettableAtom setVz(double aVz) {throw new UnsupportedOperationException("setVz");}
    
    /**
     * Groovy stuffs
     * <p>
     * 实现这些方法从而可以在 groovy 中实现 atom.x += 10 之类的操作，
     * 但是这也导致回到了传统的 getter/setter 写法，并导致 java 部分代码存在冗余。
     * <p>
     * 可能并不希望保留两套接口，但是这种写法在 java 中也不便于使用，
     * 我也不希望在核心部分混合 java groovy 的编程，目前就只能这样写
     * <p>
     * 目前只对同时有修改和访问权限的属性提供这些方法
     */
    @VisibleForTesting default double getX() {return x();}
    @VisibleForTesting default double getY() {return y();}
    @VisibleForTesting default double getZ() {return z();}
    @VisibleForTesting default int getId() {return id();}
    @VisibleForTesting default int getType() {return type();}
    @VisibleForTesting default double getVx() {return vx();}
    @VisibleForTesting default double getVy() {return vy();}
    @VisibleForTesting default double getVz() {return vz();}
}
