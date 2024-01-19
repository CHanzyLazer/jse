package jtool.lmp;

import jtool.math.matrix.RowMatrix;
import jtool.math.vector.IVector;
import jtool.math.vector.IntVector;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

@VisibleForTesting
public final class Data extends Lmpdat {
    public Data(int aAtomTypeNum, Box aBox, @Nullable IVector aMasses, IntVector aAtomID, IntVector aAtomType, RowMatrix aAtomXYZ, @Nullable RowMatrix aVelocities) {super(aAtomTypeNum, aBox, aMasses, aAtomID, aAtomType, aAtomXYZ, aVelocities);}
}
