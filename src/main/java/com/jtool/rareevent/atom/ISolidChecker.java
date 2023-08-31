package com.jtool.rareevent.atom;

import com.jtool.atom.MonatomicParameterCalculator;
import com.jtool.math.vector.ILogicalVector;

@FunctionalInterface
public interface ISolidChecker {ILogicalVector checkSolid(MonatomicParameterCalculator aMPC);}
