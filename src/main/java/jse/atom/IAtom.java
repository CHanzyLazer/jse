package jse.atom;

import org.jetbrains.annotations.VisibleForTesting;

/** 现在认为原子无论怎样都会拥有这些属性 */
public interface IAtom extends IXYZ {
    double x();
    double y();
    double z();
    int id();
    int type();
    /** 增加一项专门用于获取在 AtomData 中的位置，可能存在某些结构在修改后位置会发生改变 */
    default int index() {return -1;}
    
    default double vx() {return 0.0;}
    default double vy() {return 0.0;}
    default double vz() {return 0.0;}
    default boolean hasVelocities() {return false;}
    
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
