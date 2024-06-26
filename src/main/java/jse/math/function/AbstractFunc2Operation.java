package jse.math.function;

import jse.code.functional.IDoubleQuaternionOperator;
import jse.code.functional.IDoubleTernaryOperator;
import jse.math.matrix.IMatrix;
import jse.math.vector.IVector;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

/**
 * 通用的的函数运算。
 * @author liqa
 */
@ApiStatus.Experimental
public abstract class AbstractFunc2Operation implements IFunc2Operation {
    /** 通用的一些运算 */
    @SuppressWarnings("Convert2MethodRef")
    @Override public IFunc2 plus    (IFunc2 aRHS) {return operate(aRHS, (lhs, rhs) -> (lhs + rhs));}
    @Override public IFunc2 minus   (IFunc2 aRHS) {return operate(aRHS, (lhs, rhs) -> (lhs - rhs));}
    @Override public IFunc2 lminus  (IFunc2 aRHS) {return operate(aRHS, (lhs, rhs) -> (rhs - lhs));}
    @Override public IFunc2 multiply(IFunc2 aRHS) {return operate(aRHS, (lhs, rhs) -> (lhs * rhs));}
    @Override public IFunc2 div     (IFunc2 aRHS) {return operate(aRHS, (lhs, rhs) -> (lhs / rhs));}
    @Override public IFunc2 ldiv    (IFunc2 aRHS) {return operate(aRHS, (lhs, rhs) -> (rhs / lhs));}
    @Override public IFunc2 mod     (IFunc2 aRHS) {return operate(aRHS, (lhs, rhs) -> (lhs % rhs));}
    @Override public IFunc2 lmod    (IFunc2 aRHS) {return operate(aRHS, (lhs, rhs) -> (rhs % lhs));}
    @Override public IFunc2 operate (IFunc2 aRHS, DoubleBinaryOperator aOpt) {
        IFunc2 tThis = thisFunc2_();
        IFunc2 rFunc2 = newFunc2_();
        final int tNx = rFunc2.Nx();
        final int tNy = rFunc2.Ny();
        for (int j = 0; j < tNy; ++j) {
            double tY = rFunc2.getY(j);
            for (int i = 0; i < tNx; ++i) {
                rFunc2.set(i, j, aOpt.applyAsDouble(tThis.get(i, j), aRHS.subs(rFunc2.getX(i), tY)));
            }
        }
        return rFunc2;
    }
    
    @Override public IFunc2 plus    (final double aRHS) {return map(lhs -> (lhs + aRHS));}
    @Override public IFunc2 minus   (final double aRHS) {return map(lhs -> (lhs - aRHS));}
    @Override public IFunc2 lminus  (final double aRHS) {return map(lhs -> (aRHS - lhs));}
    @Override public IFunc2 multiply(final double aRHS) {return map(lhs -> (lhs * aRHS));}
    @Override public IFunc2 div     (final double aRHS) {return map(lhs -> (lhs / aRHS));}
    @Override public IFunc2 ldiv    (final double aRHS) {return map(lhs -> (aRHS / lhs));}
    @Override public IFunc2 mod     (final double aRHS) {return map(lhs -> (lhs % aRHS));}
    @Override public IFunc2 lmod    (final double aRHS) {return map(lhs -> (aRHS % lhs));}
    @Override public IFunc2 map     (DoubleUnaryOperator aOpt) {
        IFunc2 tThis = thisFunc2_();
        IFunc2 rFunc2 = newFunc2_();
        final int tNx = rFunc2.Nx();
        final int tNy = rFunc2.Ny();
        for (int j = 0; j < tNy; ++j) for (int i = 0; i < tNx; ++i) {
            rFunc2.set(i, j, aOpt.applyAsDouble(tThis.get(i, j)));
        }
        return rFunc2;
    }
    
    @SuppressWarnings("Convert2MethodRef")
    @Override public void plus2this     (IFunc2 aRHS) {operate2this(aRHS, (lhs, rhs) -> (lhs + rhs));}
    @Override public void minus2this    (IFunc2 aRHS) {operate2this(aRHS, (lhs, rhs) -> (lhs - rhs));}
    @Override public void lminus2this   (IFunc2 aRHS) {operate2this(aRHS, (lhs, rhs) -> (rhs - lhs));}
    @Override public void multiply2this (IFunc2 aRHS) {operate2this(aRHS, (lhs, rhs) -> (lhs * rhs));}
    @Override public void div2this      (IFunc2 aRHS) {operate2this(aRHS, (lhs, rhs) -> (lhs / rhs));}
    @Override public void ldiv2this     (IFunc2 aRHS) {operate2this(aRHS, (lhs, rhs) -> (rhs / lhs));}
    @Override public void mod2this      (IFunc2 aRHS) {operate2this(aRHS, (lhs, rhs) -> (lhs % rhs));}
    @Override public void lmod2this     (IFunc2 aRHS) {operate2this(aRHS, (lhs, rhs) -> (rhs % lhs));}
    @Override public void operate2this  (IFunc2 aRHS, DoubleBinaryOperator aOpt) {
        IFunc2 rThis = thisFunc2_();
        final int tNx = rThis.Nx();
        final int tNy = rThis.Ny();
        for (int j = 0; j < tNy; ++j) {
            double tY = rThis.getY(j);
            for (int i = 0; i < tNx; ++i) {
                rThis.set(i, j, aOpt.applyAsDouble(rThis.get(i, j), aRHS.subs(rThis.getX(i), tY)));
            }
        }
    }
    
    @Override public void plus2this     (double aRHS) {thisFunc2_().f().operation().plus2this    (aRHS);}
    @Override public void minus2this    (double aRHS) {thisFunc2_().f().operation().minus2this   (aRHS);}
    @Override public void lminus2this   (double aRHS) {thisFunc2_().f().operation().lminus2this  (aRHS);}
    @Override public void multiply2this (double aRHS) {thisFunc2_().f().operation().multiply2this(aRHS);}
    @Override public void div2this      (double aRHS) {thisFunc2_().f().operation().div2this     (aRHS);}
    @Override public void ldiv2this     (double aRHS) {thisFunc2_().f().operation().ldiv2this    (aRHS);}
    @Override public void mod2this      (double aRHS) {thisFunc2_().f().operation().mod2this     (aRHS);}
    @Override public void lmod2this     (double aRHS) {thisFunc2_().f().operation().lmod2this    (aRHS);}
    @Override public void map2this      (DoubleUnaryOperator aOpt) {thisFunc2_().f().operation().map2this(aOpt);}
    
    @Override public void fill          (double aRHS) {thisFunc2_().f().operation().fill(aRHS);}
    @Override public void fill          (IMatrix aRHS) {thisFunc2_().f().operation().fill(aRHS);}
    @Override public void fill          (IFunc2 aRHS) {fill((IFunc2Subs)aRHS);}
    @Override public void fill          (IFunc2Subs aRHS) {
        IFunc2 rThis = thisFunc2_();
        final int tNx = rThis.Nx();
        final int tNy = rThis.Ny();
        for (int j = 0; j < tNy; ++j) {
            double tY = rThis.getY(j);
            for (int i = 0; i < tNx; ++i) {
                rThis.set(i, j, aRHS.subs(rThis.getX(i), tY));
            }
        }
    }
    
    /** 函数特有的运算 */
    @Override public IFunc2 operateFull(IFunc2 aRHS, IDoubleQuaternionOperator aOpt) {
        IFunc2 tThis = thisFunc2_();
        IFunc2 rFunc2 = newFunc2_();
        final int tNx = rFunc2.Nx();
        final int tNy = rFunc2.Ny();
        for (int j = 0; j < tNy; ++j) {
            double tY = rFunc2.getY(j);
            for (int i = 0; i < tNx; ++i) {
                double tX = rFunc2.getX(i);
                rFunc2.set(i, j, aOpt.applyAsDouble(tThis.get(i, j), aRHS.subs(tX, tY), tX, tY));
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 mapFull(IDoubleTernaryOperator aOpt) {
        IFunc2 tThis = thisFunc2_();
        IFunc2 rFunc2 = newFunc2_();
        final int tNx = rFunc2.Nx();
        final int tNy = rFunc2.Ny();
        for (int j = 0; j < tNy; ++j) {
            double tY = rFunc2.getY(j);
            for (int i = 0; i < tNx; ++i) {
                double tX = rFunc2.getX(i);
                rFunc2.set(i, j, aOpt.applyAsDouble(tThis.get(i, j), tX, tY));
            }
        }
        return rFunc2;
    }
    @Override public void operateFull2this(IFunc2 aRHS, IDoubleQuaternionOperator aOpt) {
        IFunc2 rThis = thisFunc2_();
        final int tNx = rThis.Nx();
        final int tNy = rThis.Ny();
        for (int j = 0; j < tNy; ++j) {
            double tY = rThis.getY(j);
            for (int i = 0; i < tNx; ++i) {
                double tX = rThis.getX(i);
                rThis.set(i, j, aOpt.applyAsDouble(rThis.get(i, j), aRHS.subs(tX, tY), tX, tY));
            }
        }
    }
    @Override public void mapFull2this(IDoubleTernaryOperator aOpt) {
        IFunc2 rThis = thisFunc2_();
        final int tNx = rThis.Nx();
        final int tNy = rThis.Ny();
        for (int j = 0; j < tNy; ++j) {
            double tY = rThis.getY(j);
            for (int i = 0; i < tNx; ++i) {
                double tX = rThis.getX(i);
                rThis.set(i, j, aOpt.applyAsDouble(rThis.get(i, j), tX, tY));
            }
        }
    }
    
    /** stuff to override */
    protected abstract IFunc2 thisFunc2_();
    protected abstract IFunc2 newFunc2_();
}
