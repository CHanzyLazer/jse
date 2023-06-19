package com.jtool.rareevent.atom;

import com.jtool.atom.IHasAtomData;
import com.jtool.atom.MonatomicParameterCalculator;
import com.jtool.atom.MonatomicParameterCalculator.INeighborListGetter;
import com.jtool.code.UT;
import com.jtool.code.collection.Pair;
import com.jtool.code.iterator.IDoubleIterator;
import com.jtool.math.MathEX;
import com.jtool.math.matrix.IMatrix;
import com.jtool.math.vector.IVector;
import com.jtool.rareevent.IParameterCalculator;

import java.util.ArrayList;
import java.util.List;

import static com.jtool.code.CS.Q6_SOLID_MAX;
import static com.jtool.code.CS.Q6_SOLID_MIN;

/**
 * 一种参数计算机，计算体系中的最大的固体团簇的尺寸；
 * 这里使用 AOOP 的 q6 来进行判断，并且直接使用 q6 值来得到连续的结果
 * @author liqa
 */
public class ClusterSizeCalculator implements IParameterCalculator<IHasAtomData> {
    @Override public double lambdaOf(IHasAtomData aPoint) {
        // 先计算所有的 q6 以及获取顺便产生的近邻列表，因此需要使用较为内部的接口
        IVector q6All;
        final INeighborListGetter tNeighborList;
        try (MonatomicParameterCalculator tMPC = aPoint.getMPC()) {
            Pair<Pair<IMatrix, IMatrix>, INeighborListGetter> tPair = tMPC.calYlmMeanAndGetNeighborList(6);
            q6All = tMPC.calAOOP(tPair.first, tPair.second);
            tNeighborList = tPair.second;
        }
        // 获取所有需要考虑的原子列表
        List<Integer> rSolidList = new ArrayList<>();
        IDoubleIterator it = q6All.iterator();
        int tIdx = 0;
        while (it.hasNext()) {
            if (it.next() > Q6_SOLID_MIN) rSolidList.add(tIdx);
            ++tIdx;
        }
        // 如果没有需要考虑的则结果为 0.0
        if (rSolidList.isEmpty()) return 0.0;
        // 使用 getClustersBFS 获取所有的团簇
        List<List<Integer>> tClusters = MathEX.Adv.getClustersBFS(rSolidList, i -> UT.Code.filter(tNeighborList.get(i), j -> q6All.get_(j)>Q6_SOLID_MIN));
        
        // 遍历团簇统计 lambda
        double rLambda = 0.0;
        for (List<Integer> subCluster : tClusters) {
            double subLambda = 0.0;
            for (int i : subCluster) {
                double q6 = q6All.get_(i);
                if (q6 > Q6_SOLID_MIN) {
                    if (q6 < Q6_SOLID_MAX) {
                        subLambda += (q6 - Q6_SOLID_MIN) / (Q6_SOLID_MAX - Q6_SOLID_MIN);
                    } else {
                        subLambda += 1.0;
                    }
                }
            }
            rLambda = Math.max(rLambda, subLambda);
        }
        return rLambda;
    }
}
