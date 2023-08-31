package com.jtool.rareevent.atom;

import com.jtool.atom.IAtomData;
import com.jtool.atom.MonatomicParameterCalculator;
import com.jtool.rareevent.IParameterCalculator;

import static com.jtool.code.CS.R_NEAREST_MUL;


/**
 * 一种参数计算器，计算体系中的固体部分的尺寸
 * @author liqa
 */
public class SolidSizeCalculator implements IParameterCalculator<IAtomData> {
    @Override public double lambdaOf(IAtomData aPoint) {
        try (MonatomicParameterCalculator tMPC = aPoint.getMonatomicParameterCalculator()) {
            return tMPC.checkSolidQ6().count();
        }
    }
}
