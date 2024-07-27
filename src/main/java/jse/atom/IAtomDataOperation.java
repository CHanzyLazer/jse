package jse.atom;


import jse.code.collection.ISlice;
import jse.code.functional.IFilter;
import jse.code.functional.IIndexFilter;
import jse.code.functional.IUnaryFullOperator;
import jse.math.vector.IVector;
import jse.math.vector.IntVector;
import jse.math.vector.Vectors;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.Random;

import static jse.code.CS.RANDOM;

/**
 * 现在改为通用的例子操作运算，命名和其余的 operation 保持类似格式；
 * 默认操作会返回新的 AtomData
 * @author liqa
 */
public interface IAtomDataOperation {
    /**
     * 根据通用的过滤器 aFilter 来过滤 aAtomData，保留满足 Filter 的原子
     * <p>
     * 这里会直接过滤一次构造完成过滤后的 List
     * @author liqa
     * @param aFilter 自定义的过滤器，输入 {@link IAtom}，返回是否保留
     * @return 新创建的过滤后的 AtomData
     */
    ISettableAtomData filter(IFilter<IAtom> aFilter);
    
    /**
     * 根据原子种类来过滤 aAtomData，只保留选定种类的原子
     * @author liqa
     * @param aType 选择保留的原子种类数
     * @return 新创建的过滤后的 AtomData
     */
    ISettableAtomData filterType(int aType);
    
    /**
     * 直接根据 {@code List<Integer>} 来引用切片 aAtomData，只保留选定种类的原子
     * @author liqa
     * @param aIndices 选择保留的原子的下标组成的数组
     * @return 新创建的切片后的 AtomData
     */
    IAtomData refSlice(ISlice aIndices);
    IAtomData refSlice(List<Integer> aIndices);
    IAtomData refSlice(int[] aIndices);
    IAtomData refSlice(IIndexFilter aIndices);
    
    
    /**
     * 根据通用的原子映射 aOperator 来遍历映射修改原子
     * @author liqa
     * @param aMinTypeNum 建议最小的种类数目
     * @param aOperator 自定义的原子映射运算，输入 {@link IAtom} 输出修改后的 {@link IAtom}
     * @return 新创建的修改后的 AtomData
     */
    ISettableAtomData map(int aMinTypeNum, IUnaryFullOperator<? extends IAtom, ? super IAtom> aOperator);
    default ISettableAtomData map(IUnaryFullOperator<? extends IAtom, ? super IAtom> aOperator) {return map(1, aOperator);}
    
    /**
     * 根据特殊的原子种类映射 aOperator 来遍历映射修改原子种类
     * @author liqa
     * @param aMinTypeNum 建议最小的种类数目
     * @param aOperator 自定义的原子映射运算，输入 {@link IAtom} 输出应该分配的种类
     * @return 新创建的修改后的 AtomData
     */
    ISettableAtomData mapType(int aMinTypeNum, IUnaryFullOperator<Integer, ? super IAtom> aOperator);
    default ISettableAtomData mapType(IUnaryFullOperator<Integer, ? super IAtom> aOperator) {return mapType(1, aOperator);}
    
    
    /**
     * 根据给定的权重来随机分配原子种类，主要用于创建合金的初始结构
     * @author liqa
     * @param aRandom 可选自定义的随机数生成器
     * @param aTypeWeights 每个种类的权重
     * @return 新创建的修改后的 AtomData
     */
    ISettableAtomData mapTypeRandom(Random aRandom, IVector aTypeWeights);
    default ISettableAtomData mapTypeRandom(IVector aTypeWeights) {return mapTypeRandom(RANDOM, aTypeWeights);}
    default ISettableAtomData mapTypeRandom(Random aRandom, double... aTypeWeights) {
        // 特殊输入直接抛出错误
        if (aTypeWeights == null || aTypeWeights.length == 0) throw new RuntimeException("TypeWeights Must be not empty");
        return mapTypeRandom(aRandom, Vectors.from(aTypeWeights));
    }
    default ISettableAtomData mapTypeRandom(double... aTypeWeights) {return mapTypeRandom(RANDOM, aTypeWeights);}
    
    /**
     * 使用高斯分布来随机扰动原子位置
     * @author liqa
     * @param aRandom 可选自定义的随机数生成器
     * @param aSigma 高斯分布的标准差
     * @return 新创建的扰动后的 AtomData
     */
    ISettableAtomData perturbXYZGaussian(Random aRandom, double aSigma);
    default ISettableAtomData perturbXYZGaussian(double aSigma) {return perturbXYZGaussian(RANDOM, aSigma);}
    @VisibleForTesting default ISettableAtomData perturbXYZ(Random aRandom, double aSigma) {return perturbXYZGaussian(aRandom, aSigma);}
    @VisibleForTesting default ISettableAtomData perturbXYZ(double aSigma) {return perturbXYZGaussian(aSigma);}
    
    /**
     * 使用周期边界条件将出界的原子移动回到盒内
     * @author liqa
     */
    ISettableAtomData wrapPBC();
    @VisibleForTesting default ISettableAtomData wrap() {return wrapPBC();}
    
    
    /**
     * 将结构重复指定次数，不会对出边界的原子作特殊处理
     * @author liqa
     * @param aNx x 方向的重复次数
     * @param aNy Y 方向的重复次数
     * @param aNz Z 方向的重复次数
     * @return 新创建的重复后的 atomData
     */
    ISettableAtomData repeat(int aNx, int aNy, int aNz);
    default ISettableAtomData repeat(int aN) {return repeat(aN, aN, aN);}
    
    /**
     * 将结构切分成小块，会直接移除掉出边界的原子
     * <p>
     * 结果按照 {@code x, y, z (i, j, k)}
     * 的顺序依次遍历，也就是说，如果需要访问给定 {@code (i, j, k)}
     * 位置的切片结果，需要使用：
     * <pre> {@code
     * def list = data.opt().slice(Nx, Ny, Nz)
     * int idx = i + j*Nx + k*Nx*Ny
     * def subData = list[idx]
     * } </pre>
     * 来获取，同理对于给定的列表位置 {@code idx}，
     * 需要使用：
     * <pre> {@code
     * int i = idx % Nx
     * int j = idx / Nx % Ny
     * int k = idx / Nx / Ny
     * } </pre>
     * 来获取相应的空间位置 {@code (i, j, k)}
     * @author liqa
     * @param aNx x 方向的切片次数
     * @param aNy Y 方向的切片次数
     * @param aNz Z 方向的切片次数
     * @return 新创建的切片后的 atomData 组成的列表
     */
    List<? extends ISettableAtomData> slice(int aNx, int aNy, int aNz);
    default List<? extends ISettableAtomData> slice(int aN) {return slice(aN, aN, aN);}
    
    
    /**
     * 通用的团簇分析
     * @author liqa
     * @param aRCut 用于判断团簇链接的截断半径
     * @return 每个团簇对应的原子索引列表
     */
    List<IntVector> clusterAnalyze(double aRCut);
    
    /**
     * 仅执行通过团簇来 unwrap 操作，这在很多时候会比较有用
     * @author liqa
     * @param aRCut 用于判断团簇链接的截断半径
     * @return 新创建的重复后的 atomData
     */
    ISettableAtomData unwrapByCluster(double aRCut);
}
