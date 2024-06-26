package jse.math.function;


import jse.math.MathEX;
import jse.math.matrix.ColumnMatrix;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * 针对包含 {@link ColumnMatrix} 的函数的运算。
 * @author liqa
 */
@ApiStatus.Experimental
public abstract class ColumnMatrixFunc2Operation extends AbstractFunc2Operation {
    /** 通用的一些运算 */
    @Override public IFunc2 plus(IFunc2 aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        ColumnMatrix tDataR = rFunc2.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().plus2dest(tDataR, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，并且考虑到代码量这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rFunc2.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rFunc2.set(i, j, tThis.get(i, j) + aRHS.subs(rFunc2.getX(i), tY));
                }
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 minus(IFunc2 aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        ColumnMatrix tDataR = rFunc2.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().minus2dest(tDataR, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，并且考虑到代码量这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rFunc2.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rFunc2.set(i, j, tThis.get(i, j) - aRHS.subs(rFunc2.getX(i), tY));
                }
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 lminus(IFunc2 aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        ColumnMatrix tDataR = rFunc2.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().lminus2dest(tDataR, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，并且考虑到代码量这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rFunc2.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rFunc2.set(i, j, aRHS.subs(rFunc2.getX(i), tY) - tThis.get(i, j));
                }
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 multiply(IFunc2 aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        ColumnMatrix tDataR = rFunc2.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().multiply2dest(tDataR, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rFunc2.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rFunc2.set(i, j, tThis.get(i, j) * aRHS.subs(rFunc2.getX(i), tY));
                }
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 div(IFunc2 aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        ColumnMatrix tDataR = rFunc2.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().div2dest(tDataR, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rFunc2.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rFunc2.set(i, j, tThis.get(i, j) / aRHS.subs(rFunc2.getX(i), tY));
                }
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 ldiv(IFunc2 aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        ColumnMatrix tDataR = rFunc2.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().ldiv2dest(tDataR, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rFunc2.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rFunc2.set(i, j, aRHS.subs(rFunc2.getX(i), tY) / tThis.get(i, j));
                }
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 mod(IFunc2 aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        ColumnMatrix tDataR = rFunc2.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().mod2dest(tDataR, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rFunc2.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rFunc2.set(i, j, tThis.get(i, j) % aRHS.subs(rFunc2.getX(i), tY));
                }
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 lmod(IFunc2 aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        ColumnMatrix tDataR = rFunc2.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().lmod2dest(tDataR, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rFunc2.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rFunc2.set(i, j, aRHS.subs(rFunc2.getX(i), tY) % tThis.get(i, j));
                }
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 operate(IFunc2 aRHS, DoubleBinaryOperator aOpt) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        ColumnMatrix tDataR = rFunc2.getIfHasSameOrderData(aRHS);
        if (tDataL != null && tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().operate2dest(tDataR, rFunc2.internalData(), aOpt);
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rFunc2.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rFunc2.set(i, j, aOpt.applyAsDouble(tThis.get(i, j), aRHS.subs(rFunc2.getX(i), tY)));
                }
            }
        }
        return rFunc2;
    }
    
    @Override public IFunc2 plus(double aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        if (tDataL != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().plus2dest(aRHS, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，并且考虑到代码量这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) for (int i = 0; i < tNx; ++i) {
                rFunc2.set(i, j, tThis.get(i, j) + aRHS);
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 minus(double aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        if (tDataL != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().minus2dest(aRHS, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，并且考虑到代码量这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) for (int i = 0; i < tNx; ++i) {
                rFunc2.set(i, j, tThis.get(i, j) - aRHS);
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 lminus(double aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        if (tDataL != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().lminus2dest(aRHS, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) for (int i = 0; i < tNx; ++i) {
                rFunc2.set(i, j, aRHS - tThis.get(i, j));
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 multiply(double aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        if (tDataL != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().multiply2dest(aRHS, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) for (int i = 0; i < tNx; ++i) {
                rFunc2.set(i, j, tThis.get(i, j) * aRHS);
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 div(double aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        if (tDataL != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().div2dest(aRHS, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) for (int i = 0; i < tNx; ++i) {
                rFunc2.set(i, j, tThis.get(i, j) / aRHS);
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 ldiv(double aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        if (tDataL != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().ldiv2dest(aRHS, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) for (int i = 0; i < tNx; ++i) {
                rFunc2.set(i, j, aRHS / tThis.get(i, j));
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 mod(double aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        if (tDataL != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().mod2dest(aRHS, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) for (int i = 0; i < tNx; ++i) {
                rFunc2.set(i, j, tThis.get(i, j) % aRHS);
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 lmod(double aRHS) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        if (tDataL != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().lmod2dest(aRHS, rFunc2.internalData());
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) for (int i = 0; i < tNx; ++i) {
                rFunc2.set(i, j, aRHS % tThis.get(i, j));
            }
        }
        return rFunc2;
    }
    @Override public IFunc2 map(DoubleUnaryOperator aOpt) {
        ColumnMatrixFunc2 tThis = thisFunc2_();
        ColumnMatrixFunc2 rFunc2 = newFunc2_();
        ColumnMatrix tDataL = rFunc2.getIfHasSameOrderData(tThis);
        if (tDataL != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            tDataL.operation().map2dest(rFunc2.internalData(), aOpt);
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rFunc2.Nx();
            final int tNy = rFunc2.Ny();
            for (int j = 0; j < tNy; ++j) for (int i = 0; i < tNx; ++i) {
                rFunc2.set(i, j, aOpt.applyAsDouble(tThis.get(i, j)));
            }
        }
        return rFunc2;
    }
    
    @Override public void plus2this(IFunc2 aRHS) {
        ColumnMatrixFunc2 rThis = thisFunc2_();
        ColumnMatrix tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            rThis.internalData().operation().plus2this(tDataR);
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构
            final int tNx = rThis.Nx();
            final int tNy = rThis.Ny();
            final int tStartX, tStartY, tEndX, tEndY;
            if (aRHS instanceof IZeroBoundFunc2) {
                // 对于零边界的特殊优化，只需要运算一部分
                IZeroBoundFunc2 tRHS = (IZeroBoundFunc2)aRHS;
                tStartX = Math.max(MathEX.Code.floor2int((tRHS.zeroBoundNegX() - rThis.x0())/rThis.dx()), 0);
                tStartY = Math.max(MathEX.Code.floor2int((tRHS.zeroBoundNegY() - rThis.y0())/rThis.dy()), 0);
                tEndX = Math.min(MathEX.Code.ceil2int((tRHS.zeroBoundPosX() - rThis.x0())/rThis.dx()) + 1, tNx);
                tEndY = Math.min(MathEX.Code.ceil2int((tRHS.zeroBoundPosY() - rThis.y0())/rThis.dy()) + 1, tNy);
            } else {
                tStartX = 0; tStartY = 0;
                tEndX = tNx; tEndY = tNy;
            }
            for (int j = tStartY; j < tEndY; ++j) {
                double tY = rThis.getY(j);
                for (int i = tStartX; i < tEndX; ++i) {
                    rThis.set(i, j, rThis.get(i, j) + aRHS.subs(rThis.getX(i), tY));
                }
            }
        }
    }
    @Override public void minus2this(IFunc2 aRHS) {
        ColumnMatrixFunc2 rThis = thisFunc2_();
        ColumnMatrix tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            rThis.internalData().operation().minus2this(tDataR);
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构
            final int tNx = rThis.Nx();
            final int tNy = rThis.Ny();
            final int tStartX, tStartY, tEndX, tEndY;
            if (aRHS instanceof IZeroBoundFunc2) {
                // 对于零边界的特殊优化，只需要运算一部分
                IZeroBoundFunc2 tRHS = (IZeroBoundFunc2)aRHS;
                tStartX = Math.max(MathEX.Code.floor2int((tRHS.zeroBoundNegX() - rThis.x0())/rThis.dx()), 0);
                tStartY = Math.max(MathEX.Code.floor2int((tRHS.zeroBoundNegY() - rThis.y0())/rThis.dy()), 0);
                tEndX = Math.min(MathEX.Code.ceil2int((tRHS.zeroBoundPosX() - rThis.x0())/rThis.dx()) + 1, tNx);
                tEndY = Math.min(MathEX.Code.ceil2int((tRHS.zeroBoundPosY() - rThis.y0())/rThis.dy()) + 1, tNy);
            } else {
                tStartX = 0; tStartY = 0;
                tEndX = tNx; tEndY = tNy;
            }
            for (int j = tStartY; j < tEndY; ++j) {
                double tY = rThis.getY(j);
                for (int i = tStartX; i < tEndX; ++i) {
                    rThis.set(i, j, rThis.get(i, j) - aRHS.subs(rThis.getX(i), tY));
                }
            }
        }
    }
    @Override public void lminus2this(IFunc2 aRHS) {
        ColumnMatrixFunc2 rThis = thisFunc2_();
        ColumnMatrix tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            rThis.internalData().operation().lminus2this(tDataR);
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rThis.Nx();
            final int tNy = rThis.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rThis.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rThis.set(i, j, aRHS.subs(rThis.getX(i), tY) - rThis.get(i, j));
                }
            }
        }
    }
    @Override public void multiply2this(IFunc2 aRHS) {
        ColumnMatrixFunc2 rThis = thisFunc2_();
        ColumnMatrix tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            rThis.internalData().operation().multiply2this(tDataR);
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rThis.Nx();
            final int tNy = rThis.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rThis.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rThis.set(i, j, rThis.get(i, j) * aRHS.subs(rThis.getX(i), tY));
                }
            }
        }
    }
    @Override public void div2this(IFunc2 aRHS) {
        ColumnMatrixFunc2 rThis = thisFunc2_();
        ColumnMatrix tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            rThis.internalData().operation().div2this(tDataR);
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rThis.Nx();
            final int tNy = rThis.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rThis.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rThis.set(i, j, rThis.get(i, j) / aRHS.subs(rThis.getX(i), tY));
                }
            }
        }
    }
    @Override public void ldiv2this(IFunc2 aRHS) {
        ColumnMatrixFunc2 rThis = thisFunc2_();
        ColumnMatrix tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            rThis.internalData().operation().ldiv2this(tDataR);
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rThis.Nx();
            final int tNy = rThis.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rThis.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rThis.set(i, j, aRHS.subs(rThis.getX(i), tY) / rThis.get(i, j));
                }
            }
        }
    }
    @Override public void mod2this(IFunc2 aRHS) {
        ColumnMatrixFunc2 rThis = thisFunc2_();
        ColumnMatrix tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            rThis.internalData().operation().mod2this(tDataR);
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rThis.Nx();
            final int tNy = rThis.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rThis.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rThis.set(i, j, rThis.get(i, j) % aRHS.subs(rThis.getX(i), tY));
                }
            }
        }
    }
    @Override public void lmod2this(IFunc2 aRHS) {
        ColumnMatrixFunc2 rThis = thisFunc2_();
        ColumnMatrix tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            rThis.internalData().operation().lmod2this(tDataR);
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rThis.Nx();
            final int tNy = rThis.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rThis.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rThis.set(i, j, aRHS.subs(rThis.getX(i), tY) % rThis.get(i, j));
                }
            }
        }
    }
    @Override public void operate2this(IFunc2 aRHS, DoubleBinaryOperator aOpt) {
        ColumnMatrixFunc2 rThis = thisFunc2_();
        ColumnMatrix tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            rThis.internalData().operation().operate2this(tDataR, aOpt);
        } else {
            // 其余情况不考虑 ColumnMatrix 的结构，这里不对零边界的情况做优化
            final int tNx = rThis.Nx();
            final int tNy = rThis.Ny();
            for (int j = 0; j < tNy; ++j) {
                double tY = rThis.getY(j);
                for (int i = 0; i < tNx; ++i) {
                    rThis.set(i, j, aOpt.applyAsDouble(rThis.get(i, j), aRHS.subs(rThis.getX(i), tY)));
                }
            }
        }
    }
    
    @Override public void fill(IFunc2 aRHS) {
        ColumnMatrixFunc2 rThis = thisFunc2_();
        final ColumnMatrix tDataR = rThis.getIfHasSameOrderData(aRHS);
        if (tDataR != null) {
            // 对于完全相同排列的特殊优化，简单起见这里不考虑零边界的情况，只考虑完全一致的情况
            rThis.internalData().operation().fill(tDataR);
        } else {
            rThis.internalData().fill((i, j) -> aRHS.subs(rThis.getX(i), rThis.getY(j)));
        }
    }
    
    
    /** stuff to override */
    protected abstract ColumnMatrixFunc2 thisFunc2_();
    protected abstract ColumnMatrixFunc2 newFunc2_();
}
