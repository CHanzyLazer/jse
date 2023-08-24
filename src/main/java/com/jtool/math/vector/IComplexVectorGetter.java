package com.jtool.math.vector;

import com.jtool.math.IComplexDouble;

@FunctionalInterface
public interface IComplexVectorGetter {
    IComplexDouble get(int aIdx);
}
