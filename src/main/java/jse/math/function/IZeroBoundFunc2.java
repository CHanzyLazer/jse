package jse.math.function;

/**
 * @author liqa
 * <p> 二维数值函数的扩展，超出界限外使用 0 值，可以加速一些运算 </p>
 */
public interface IZeroBoundFunc2 extends IFunc2 {
    /** 提供额外的接口用于检测两端 */
    double zeroBoundNegX();
    double zeroBoundPosX();
    double zeroBoundNegY();
    double zeroBoundPosY();
}
