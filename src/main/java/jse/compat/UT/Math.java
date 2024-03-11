package jse.compat.UT;

import jse.code.UT;
import jse.math.IComplexDouble;
import org.jetbrains.annotations.VisibleForTesting;

@VisibleForTesting
public final class Math extends UT.Math {
    public final static double
          PI  = UT.Math.PI
        , pi  = UT.Math.pi
        , E   = UT.Math.E
        , e   = UT.Math.e
        , NaN = UT.Math.NaN
        , nan = UT.Math.nan
        , Inf = UT.Math.Inf
        , inf = UT.Math.inf
        ;
    public final static IComplexDouble
          i1 = UT.Math.i1
        , j1 = UT.Math.j1
        , i  = UT.Math.i
        , j  = UT.Math.j
        ;
}
