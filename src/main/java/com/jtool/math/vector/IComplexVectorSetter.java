package com.jtool.math.vector;

import com.jtool.math.IComplexDouble;

@FunctionalInterface
public interface IComplexVectorSetter {
    void set(int aIdx, IComplexDouble aValue);
}
