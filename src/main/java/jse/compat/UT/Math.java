package jse.compat.UT;

import jse.code.UT;
import jse.math.IComplexDouble;
import org.jetbrains.annotations.VisibleForTesting;

@VisibleForTesting
public final class Math extends UT.Math {
    public final static double
          pi  = UT.Math.pi
        , e   = UT.Math.e
        , nan = UT.Math.nan
        , inf = UT.Math.inf
        ;
    public final static IComplexDouble
          i1 = UT.Math.i1
        , j1 = UT.Math.j1
        ;
}
