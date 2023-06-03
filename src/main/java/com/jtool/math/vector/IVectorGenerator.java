package com.jtool.math.vector;

/**
 * 通用的任意向量的生成器
 * @author liqa
 */
public interface IVectorGenerator {
    IVector ones();
    IVector zeros();
    IVector same();
    IVector from(IVectorGetter aVectorGetter);
    
    IVector ones(int aSize);
    IVector zeros(int aSize);
    IVector from(int aSize, IVectorGetter aVectorGetter);
    
    /** 增加一些 Vector 特有的生成 */
    IVector sequence(double aStart, double aEnd);
    IVector sequence(double aStart, double aStep, double aEnd);
    IVector sequenceByStep(double aStart, double aStep);
    IVector sequenceByStep(double aStart, double aStep, int aN);
}
