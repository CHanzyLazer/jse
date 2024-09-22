package jsex.nnap;

import jse.cache.ComplexVectorCache;
import jse.cache.MatrixCache;
import jse.cache.VectorCache;
import jse.math.MathEX;
import jse.math.matrix.RowMatrix;
import jse.math.vector.IComplexVector;
import jse.math.vector.IVector;

import java.util.List;

import static jse.math.MathEX.PI;

/**
 * 所有 nnap 的基组/描述符实现会放在这里
 * @author liqa
 */
public class Basis {
    
    @FunctionalInterface public interface IDxyzTypeIterable {void forEachDxyzType(IDxyzTypeDo aDxyzTypeDo);}
    @FunctionalInterface public interface IDxyzTypeDo {void run(double aDx, double aDy, double aDz, int aType);}
    
    /**
     * 一种基于 Chebyshev 多项式和球谐函数将原子局域环境展开成一个基组的方法，
     * 主要用于作为机器学习的输入向量；这是 NNAP 中默认使用的原子基组。
     * <p>
     * References:
     * <a href="https://arxiv.org/abs/2211.03350v3">
     * Efficient and accurate simulation of vitrification in multi-component metallic liquids with neural-network potentials </a>
     * @author Su Rui, liqa
     * @param aTypeNum 原子种类数目
     * @param aNMax Chebyshev 多项式选取的最大阶数
     * @param aLMax 球谐函数中 l 选取的最大阶数
     * @param aRCutOff 截断半径
     * @param aNL 近邻列表遍历器
     * @return 原子描述符矩阵组成的数组，n 为行，l 为列，因此 asVecRow 即为原本定义的基；如果存在超过一个种类则输出行数翻倍
     */
    public static RowMatrix sphericalChebyshev(final int aTypeNum, final int aNMax, final int aLMax, final double aRCutOff, IDxyzTypeIterable aNL) {
        if (aNMax < 0) throw new IllegalArgumentException("Input n_max MUST be Non-Negative, input: "+aNMax);
        if (aLMax < 0) throw new IllegalArgumentException("Input l_max MUST be Non-Negative, input: "+aLMax);
        
        final int tSizeN = aTypeNum>1 ? aNMax+aNMax+2 : aNMax+1;
        final RowMatrix rFingerPrint = MatrixCache.getMatRow(tSizeN, aLMax+1);
        
        // 需要存储所有的 l，n，m 的值来统一进行近邻求和
        final List<? extends IComplexVector> cnlm = ComplexVectorCache.getZeros((aLMax+1)*(aLMax+1), tSizeN);
        // 缓存 Rn 数组
        final IVector tRn = VectorCache.getVec(aNMax+1);
        // 全局暂存 Y 的数组，这样可以用来防止重复获取来提高效率
        final IComplexVector tY = ComplexVectorCache.getVec((aLMax+1)*(aLMax+1));
        
        // 遍历近邻计算 Ylm, Rn, fc
        aNL.forEachDxyzType((dx, dy, dz, type) -> {
            double dis = MathEX.Fast.hypot(dx, dy, dz);
            
            // 计算种类的权重
            double wt = ((type&1)==1) ? type : -type;
            // 计算截断函数 fc
            double fc = dis>=aRCutOff ? 0.0 : MathEX.Fast.powFast(1.0 - MathEX.Fast.pow2(dis/aRCutOff), 4);
            // 统一遍历一次计算 Rn
            final double tX = 1.0 - 2.0*dis/aRCutOff;
            tRn.fill(n -> MathEX.Func.chebyshev(n, tX));
            
            // 遍历求 n，l 的情况
            MathEX.Func.sphericalHarmonicsFull2DestXYZDis_(aLMax, dx, dy, dz, dis, tY);
            for (int tN = 0; tN <= aNMax; ++tN) {
                // 现在提供了 mplus2this 支持将数乘到 tY 中后再加到 cijm，可以不用中间变量；
                // 虽然看起来和使用 operate2this 效率基本一致，即使后者理论上应该还会创建一些 DoubleComplex；
                // 总之至少没有反向优化，并且这样包装后更加不吃编译器的优化，也不存在一大坨 lambda 表达式，以及传入的 DoubleComplex 一定不是引用等这种约定
                double tMul = fc * tRn.get(tN);
                cnlm.get(tN).operation().mplus2this(tY, tMul);
                if (aTypeNum > 1) cnlm.get(tN+aNMax+1).operation().mplus2this(tY, wt*tMul);
            }
        });
        // 做标量积消去 m 项，得到此原子的 FP
        for (int tN = 0; tN < tSizeN; ++tN) for (int tL = 0; tL <= aLMax; ++tL) {
            // 根据 sphericalHarmonicsFull2Dest 的约定这里需要这样索引
            int tStart = tL*tL;
            int tLen = tL+tL+1;
            rFingerPrint.set(tN, tL, (4.0*PI/(double)tLen) * cnlm.get(tN).subVec(tStart, tStart+tLen).operation().dot());
        }
        
        // 归还临时变量
        ComplexVectorCache.returnVec(tY);
        VectorCache.returnVec(tRn);
        ComplexVectorCache.returnVec(cnlm);
        
        return rFingerPrint;
    }
}
