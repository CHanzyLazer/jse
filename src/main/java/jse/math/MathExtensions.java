package jse.math;

import jse.math.vector.IVector;
import org.jetbrains.annotations.ApiStatus;

/**
 * 用于 Groovy 使用的扩展方法，
 * 用于对数字增加一些扩展运算
 * @author liqa
 */
@ApiStatus.Experimental
public class MathExtensions {
    public static IVector plus    (Number aLHS, IVector aRHS) {return aRHS.plus              (aLHS.doubleValue());}
    public static IVector minus   (Number aLHS, IVector aRHS) {return aRHS.operation().lminus(aLHS.doubleValue());}
    public static IVector multiply(Number aLHS, IVector aRHS) {return aRHS.multiply          (aLHS.doubleValue());}
    public static IVector div     (Number aLHS, IVector aRHS) {return aRHS.operation().ldiv  (aLHS.doubleValue());}
    public static IVector mod     (Number aLHS, IVector aRHS) {return aRHS.operation().lmod  (aLHS.doubleValue());}
}
