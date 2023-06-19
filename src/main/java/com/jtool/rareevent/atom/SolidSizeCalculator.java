package com.jtool.rareevent.atom;

import com.jtool.atom.IHasAtomData;
import com.jtool.atom.MonatomicParameterCalculator;
import com.jtool.code.iterator.IDoubleIterator;
import com.jtool.math.vector.IVector;
import com.jtool.rareevent.IParameterCalculator;

import static com.jtool.code.CS.Q6_SOLID_MAX;
import static com.jtool.code.CS.Q6_SOLID_MIN;

/**
 * 一种参数计算机，计算体系中的固体部分的尺寸；
 * 这里使用 AOOP 的 q6 来进行判断，并且直接使用 q6 值来得到连续的结果
 * @author liqa
 */
public class SolidSizeCalculator implements IParameterCalculator<IHasAtomData> {
    @Override public double lambdaOf(IHasAtomData aPoint) {
        // 先计算所有的 q6
        IVector q6All;
        try (MonatomicParameterCalculator tMPC = aPoint.getMPC()) {q6All = tMPC.calAOOP(6);}
        // 统计 lambda
        double rLambda = 0.0;
        IDoubleIterator it = q6All.iterator();
        while (it.hasNext()) {
            double q6 = it.next();
            if (q6 > Q6_SOLID_MIN) {
                if (q6 < Q6_SOLID_MAX) {
                    rLambda += (q6 - Q6_SOLID_MIN) / (Q6_SOLID_MAX - Q6_SOLID_MIN);
                } else {
                    rLambda += 1.0;
                }
            }
        }
        return rLambda;
    }
}
