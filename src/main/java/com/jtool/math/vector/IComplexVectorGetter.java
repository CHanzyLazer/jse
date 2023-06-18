package com.jtool.math.vector;

import com.jtool.math.ComplexDouble;

@FunctionalInterface
public interface IComplexVectorGetter {
    ComplexDouble get(int aIdx);
}
