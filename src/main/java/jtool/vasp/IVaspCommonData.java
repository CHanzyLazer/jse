package jtool.vasp;

import jtool.math.matrix.IMatrix;
import jtool.math.vector.IVector;

public interface IVaspCommonData {
    String dataName();
    String[] atomTypes();
    IVector atomNumbers();
    IMatrix vaspBox();
    double vaspBoxScale();
}
