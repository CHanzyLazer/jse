package com.jtool.math.vector;

import com.jtool.math.ComplexDouble;

@FunctionalInterface
public interface IComplexVectorSetter {
    void set(int aIdx, ComplexDouble aValue);
}
