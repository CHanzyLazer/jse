package jse.atom;

/**
 * 通用的模拟盒类，现在包含更多信息，支持斜方的模拟盒；
 * 只支持右手系的基组
 * @author liqa
 */
public interface IBox extends IXYZ {
    /** 是否是斜方的，很多时候这只是一个标记，即使实际是正交的也可以获取到 {@code true} */
    default boolean isPrism() {return false;}
    /** 是否是 lammps 的风格，也就是 {@code ay = az = bz = 0}，当然很多时候这只是一个标记 */
    default boolean isLmpStyle() {return true;}
    
    /** 一般的接口，获取三个基，要求这三个基满足右手系，也就是 {@code (a x b) · c > 0} */
    default IXYZ a() {return new XYZ( x(),  0.0, 0.0);}
    default IXYZ b() {return new XYZ(xy(),  y(), 0.0);}
    default IXYZ c() {return new XYZ(xz(), yz(), z());}
    /** 还需要提供一个复制的方法方便拷贝 */
    IBox copy();
    
    /** IXYZ stuffs */
    double x();
    double y();
    double z();
    
    /** lammps prism box stuffs */
    default double xy() {return 0.0;}
    default double xz() {return 0.0;}
    default double yz() {return 0.0;}
    
    default double volume() {
        if (isLmpStyle()) return prod();
        return a().mixed(b(), c());
    }
    
    /** 提供一个简单通用的坐标相互转换接口，这里不直接创建新的 XYZ 而是修改输入的 XYZ，可以保证性能，减少重复代码 */
    default void toCartesian(XYZ rDirect) {
        if (!isPrism()) {
            rDirect.multiply2this(this);
        } else
        if (isLmpStyle()) {
            rDirect.setXYZ(
                x()*rDirect.mX + xy()*rDirect.mY + xz()*rDirect.mZ,
                y()*rDirect.mY + yz()*rDirect.mZ,
                z()*rDirect.mZ
            );
        } else {
            IXYZ tA = a();
            IXYZ tB = b();
            IXYZ tC = c();
            rDirect.setXYZ(
                tA.x()*rDirect.mX + tB.x()*rDirect.mY + tC.x()*rDirect.mZ,
                tA.y()*rDirect.mX + tB.y()*rDirect.mY + tC.y()*rDirect.mZ,
                tA.z()*rDirect.mX + tB.z()*rDirect.mY + tC.z()*rDirect.mZ
            );
        }
    }
    default void toDirect(XYZ rCartesian) {
        if (!isPrism()) {
            rCartesian.div2this(this);
        } else
        if (isLmpStyle()) {
            double tX  =  x(), tY  =  y(), tZ  =  z();
            double tXY = xy(), tXZ = xz(), tYZ = yz();
            rCartesian.setXYZ(
                rCartesian.mX/tX - tXY*rCartesian.mY/(tX*tY) + (tXY*tYZ - tXZ*tY)*rCartesian.mZ/(tX*tY*tZ),
                rCartesian.mY/tY - tYZ*rCartesian.mZ/(tY*tZ),
                rCartesian.mZ/tZ
            );
        } else {
            // 默认实现不缓存中间结果
            XYZ tA = XYZ.toXYZ(a());
            XYZ tB = XYZ.toXYZ(b());
            XYZ tC = XYZ.toXYZ(c());
            double tV = tA.mixed(tB, tC);
            rCartesian.setXYZ(
                tB.mixed(tC, rCartesian) / tV,
                tC.mixed(tA, rCartesian) / tV,
                tA.mixed(tB, rCartesian) / tV
            );
        }
    }
}
