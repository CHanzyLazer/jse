package jse.atom;

/**
 * 增加一个中间层来统一 {@link #toString()} 方法的实现
 * @see IXYZ
 * @author liqa
 */
public abstract class AbstractXYZ implements IXYZ {
    /** @return 此 xyz 坐标的字符串表示，这里转换只保留 4 位有效数字（不影响实际精度）*/
    @Override public String toString() {return String.format("(%.4g, %.4g, %.4g)", x(), y(), z());}
    /** @return {@inheritDoc} */
    @Override public XYZ copy() {return new XYZ(this);}
}
