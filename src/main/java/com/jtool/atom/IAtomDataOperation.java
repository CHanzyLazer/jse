package com.jtool.atom;


import com.jtool.math.vector.IVector;
import com.jtool.math.vector.Vectors;

import java.util.Random;

import static com.jtool.code.CS.RANDOM;

/**
 * 现在改为通用的例子操作运算，命名和其余的 operation 保持类似格式；
 * 默认会返回新的 AtomData
 * @author liqa
 */
public interface IAtomDataOperation {
    
    interface IAtomSetter {
        IAtomSetter setX(double aX);
        IAtomSetter setY(double aY);
        IAtomSetter setZ(double aZ);
        IAtomSetter setID(int aID);
        IAtomSetter setType(int aType);
    }
    @FunctionalInterface
    interface IAtomUpdater {void update(IAtom aAtom, IAtomSetter aAtomSetter);}
    /**
     * 根据通用的原子更新器 aUpdater 来遍历修改粒子
     * @author liqa
     * @param aMinTypeNum 建议最小的种类数目
     * @param aUpdater 自定义的原子过滤器，输入 {@link IAtom} 以及用来设置修改的 {@link IAtomSetter}
     * @return 更新后的 AtomData
     */
    IAtomData mapUpdate(int aMinTypeNum, IAtomUpdater aUpdater);
    default IAtomData mapUpdate(IAtomUpdater aUpdater) {return mapUpdate(1, aUpdater);}
    
    
    /**
     * 根据给定的权重来随机修改原子种类，主要用于创建合金的初始结构
     * @author liqa
     * @param aRandom 可选自定义的随机数生成器
     * @param aTypeWeights 每个种类的权重
     * @return 更新后的 AtomData
     */
    IAtomData randomUpdateTypeByWeight(Random aRandom, IVector aTypeWeights);
    default IAtomData randomUpdateTypeByWeight(IVector aTypeWeights) {return randomUpdateTypeByWeight(RANDOM, aTypeWeights);}
    default IAtomData randomUpdateTypeByWeight(Random aRandom, double... aTypeWeights) {
        // 特殊输入直接抛出错误
        if (aTypeWeights == null || aTypeWeights.length == 0) throw new RuntimeException("TypeWeights Must be not empty");
        return randomUpdateTypeByWeight(aRandom, Vectors.from(aTypeWeights));
    }
    default IAtomData randomUpdateTypeByWeight(double... aTypeWeights) {return randomUpdateTypeByWeight(RANDOM, aTypeWeights);}
}
