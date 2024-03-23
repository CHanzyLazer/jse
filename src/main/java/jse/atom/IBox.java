package jse.atom;

/**
 * 通用的模拟盒类，现在包含更多信息，支持斜方的模拟盒；
 * 只支持右手系的基组
 * @author liqa
 */
public interface IBox extends IXYZ {
    /** 是否是斜方的，很多时候这只是一个标记，即使实际是正交的也可以获取到 {@code true} */
    default boolean isPrism() {return false;}
    
    /** 一般的接口，获取三个基，要求这三个基满足右手系，也就是 {@code (a x b) · c > 0} */
    IXYZ a();
    IXYZ b();
    IXYZ c();
    default double volume() {
        if (!isPrism()) return prod();
        return a().mixed(b(), c());
    }
    /** 还需要提供一个复制的方法方便拷贝 */
    IBox copy();
    
    /** IXYZ stuffs */
    default double x() {return a().x();}
    default double y() {return b().y();}
    default double z() {return c().z();}
    
    /** lammps prism box stuffs */
    default double xy() {return b().y();}
    default double xz() {return c().x();}
    default double yz() {return c().y();}
    /** 是否是 lammps 的风格，也就是 {@code ay = az = bz = 0}，当然很多时候这只是一个标记 */
    default boolean isLmpStyle() {return true;}
    
    /** 提供一个通用的坐标相互转换接口 */
    default XYZ toCartesian(IXYZ aDirect) {
        if (!isPrism()) return aDirect.multiply(this);
        XYZ rCartesian = new XYZ(0.0, 0.0, 0.0);
        rCartesian.mplus2this(a(), aDirect.x());
        rCartesian.mplus2this(b(), aDirect.y());
        rCartesian.mplus2this(c(), aDirect.z());
        return rCartesian;
    }
    default XYZ toDirect(IXYZ aCartesian) {
        if (!isPrism()) return aCartesian.div(this);
        XYZ tCartesian = XYZ.toXYZ(aCartesian);
        XYZ tA = XYZ.toXYZ(a());
        XYZ tB = XYZ.toXYZ(b());
        XYZ tC = XYZ.toXYZ(c());
        double tV = tA.mixed(tB, tC);
        return new XYZ(
            tB.mixed(tC, tCartesian) / tV,
            tC.mixed(tA, tCartesian) / tV,
            tA.mixed(tB, tCartesian) / tV
        );
    }
}
