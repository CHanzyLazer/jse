package com.jtool.rareevent.atom;

import com.jtool.atom.IAtom;
import com.jtool.atom.IAtomData;
import com.jtool.atom.MonatomicParameterCalculator;
import com.jtool.code.collection.FixedCollections;
import com.jtool.math.vector.ILogicalVector;

import java.util.List;

import static com.jtool.code.CS.R_NEAREST_MUL;


/**
 * 一种参数计算器，计算体系中的最大的固体团簇的尺寸
 * <p>
 * 除了会考虑整体，也会只考虑其中一种主要种类的来进行判断
 * @author liqa
 */
public final class MainTypeClusterSizeCalculator extends AbstractClusterSizeCalculator {
    private final ISolidChecker mAllSolidChecker, mMainTypeSolidChecker;
    private final int mMainType;
    private final double mMainTypeRangeMul;
    
    /**
     * 构造一个 MainTypeClusterSizeCalculator
     * @author liqa
     * @param aAllSolidChecker 适用于所有原子的 Checker，传入的 MPC 为所有原子的体系
     * @param aMainTypeSolidChecker 适用于主要种类的 Checker，传入的 MPC 为选定的种类的原子的体系
     * @param aMainType 选定的种类
     * @param aMainTypeRangeMul 主要种类会影响周围多大的距离也成为固体，默认为 R_NEAREST_MUL
     */
    public MainTypeClusterSizeCalculator(ISolidChecker aAllSolidChecker, ISolidChecker aMainTypeSolidChecker, int aMainType, double aMainTypeRangeMul) {
        mAllSolidChecker = aAllSolidChecker;
        mMainTypeSolidChecker = aMainTypeSolidChecker;
        mMainType = aMainType;
        mMainTypeRangeMul = aMainTypeRangeMul;
    }
    public MainTypeClusterSizeCalculator(ISolidChecker aAllSolidChecker, ISolidChecker aMainTypeSolidChecker, int aMainType) {this(aAllSolidChecker, aMainTypeSolidChecker, aMainType, R_NEAREST_MUL);}
    
    @Override protected ILogicalVector getIsSolid_(MonatomicParameterCalculator aMPC, IAtomData aPoint) {
        ILogicalVector rIsSolid = mAllSolidChecker.checkSolid(aMPC);
        // 手动遍历过滤
        final List<IAtom> tAtoms = aPoint.atoms();
        List<Integer> tTypeIndices = FixedCollections.filterIndex(aPoint.atomNum(), idx -> tAtoms.get(idx).type()==mMainType);
        // 过滤得到只有这种元素的 MPC 然后进行计算
        try (MonatomicParameterCalculator tMPC = aPoint.operation().filterIndices(tTypeIndices).getMonatomicParameterCalculator()) {
            ILogicalVector tTypeIsSolid = mMainTypeSolidChecker.checkSolid(tMPC);
            // 使用 refSlicer 来合并两者结果
            rIsSolid.refSlicer().get(tTypeIndices).or2this(tTypeIsSolid);
            // 周围中有一半的 Main Type 为 solid 则也要设为 solid
            for (int idx = 0; idx < aPoint.atomNum(); ++idx) if (!rIsSolid.get(idx) && tAtoms.get(idx).type()!=mMainType) {
                List<Integer> tNL = tMPC.getNeighborList(tAtoms.get(idx), tMPC.unitLen()*mMainTypeRangeMul);
                int rMainTypeSolidNum = 0;
                for (int i : tNL) if (tTypeIsSolid.get(i)) ++rMainTypeSolidNum;
                if (rMainTypeSolidNum!=0 && rMainTypeSolidNum+rMainTypeSolidNum>=tNL.size()) rIsSolid.set(idx, true);
            }
        }
        return rIsSolid;
    }
}
