package com.jtool.rareevent.atom;

import com.jtool.atom.IAtomData;
import com.jtool.atom.MonatomicParameterCalculator;
import com.jtool.code.collection.AbstractCollections;
import com.jtool.math.MathEX;
import com.jtool.math.vector.ILogicalVector;
import com.jtool.rareevent.IParameterCalculator;

import java.util.List;

import static com.jtool.code.CS.R_NEAREST_MUL;


/**
 * 抽象的参数计算器，计算体系中的最大的固体团簇的尺寸，可自定义内部使用的固体判断函数
 * @author liqa
 */
public abstract class AbstractClusterSizeCalculator implements IParameterCalculator<IAtomData> {
    @Override public final double lambdaOf(IAtomData aPoint) {
        // 还是使用 MPC 内部的方法来判断
        try (final MonatomicParameterCalculator tMPC = aPoint.getMonatomicParameterCalculator()) {
            final ILogicalVector tIsSolid = getIsSolid_(tMPC, aPoint);
            // 使用 getClustersDFS 获取所有的团簇（一般来说会比 BFS 更快，当然这个部分不是瓶颈）
            final double tRCluster = getRCluster_(tMPC);
            List<List<Integer>> tClusters = MathEX.Adv.getClustersDFS(AbstractCollections.filterIndex(tIsSolid.size(), tIsSolid), i -> AbstractCollections.filterIndex(tMPC.getNeighborList(i, tRCluster), tIsSolid));
            // 遍历团簇统计 lambda
            double rLambda = 0.0;
            for (List<Integer> subCluster : tClusters) {
                rLambda = Math.max(rLambda, subCluster.size());
            }
            return rLambda;
        }
    }
    
    /** stuff to override */
    protected double getRCluster_(MonatomicParameterCalculator aMPC) {return aMPC.unitLen()*R_NEAREST_MUL;}
    protected abstract ILogicalVector getIsSolid_(MonatomicParameterCalculator aMPC, IAtomData aPoint);
}
