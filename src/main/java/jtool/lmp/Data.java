package jtool.lmp;

import jtool.math.matrix.IMatrix;
import jtool.math.vector.IVector;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

@VisibleForTesting
public final class Data extends Lmpdat {
    Data(int aAtomTypeNum, Box aBox, @Nullable IVector aMasses, IMatrix aAtomData, @Nullable IMatrix aVelocities) {super(aAtomTypeNum, aBox, aMasses, aAtomData, aVelocities);}
}
