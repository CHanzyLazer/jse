package com.jtool.rareevent.atom;

import com.jtool.atom.IAtomData;
import com.jtool.atom.MonatomicParameterCalculator;
import com.jtool.math.vector.ILogicalVector;
import com.jtool.rareevent.IParameterCalculator;


/**
 * 一种参数计算机，计算体系中的固体部分的尺寸
 * @author liqa
 */
public class SolidSizeCalculator implements IParameterCalculator<IAtomData> {
    @Override public double lambdaOf(IAtomData aPoint) {
        // 进行类固体判断
        ILogicalVector tIsSolid;
        try (MonatomicParameterCalculator tMPC = aPoint.getMPC()) {tIsSolid = tMPC.checkSolidQ6();}
        // 统计 lambda
        return tIsSolid.count();
    }
}
