package jsex.nnap;

import jse.atom.AtomicParameterCalculator;
import jse.atom.IAtomData;
import jse.code.collection.DoubleList;
import jse.math.vector.IVector;
import jse.math.vector.Vector;
import jse.math.vector.Vectors;
import jse.opt.Adam;
import jse.opt.IOptimizer;
import jsex.nnap.basis.Basis;
import jsex.nnap.nn.FeedForward;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

import static jse.code.CS.RANDOM;

/**
 * 纯 jse 实现的 nnap 训练器，不借助 pytorch
 * 来实现更高的优化效果
 * <p>
 * 由于是纯 jse 实现，写法可以更加灵活并且避免了重复代码
 *
 * @author liqa
 */
@ApiStatus.Experimental
public class TrainerNative {
    
    protected class DataSet {
        public int mSize = 0;
        /** 按照原子结构排列，每个原子结构中每个原子对应一个向量（不同种类可能长度不等） */
        public final List<Vector[]> mFp = new ArrayList<>(64);
        /** 每个原子数据结构对应的能量值 */
        public final DoubleList mEng = new DoubleList(64);
    }
    
    protected IOptimizer mOptimizer;
    protected final Basis mBasis;
    protected final FeedForward mNN;
    protected final DataSet mTrainData;
    protected final DataSet mTestData;
    protected final double mRefEng;
    
    private final Vector mGradParaBuf1, mGradParaBuf2;
    
    TrainerNative(double aRefEng, Basis aBasis, FeedForward aNN, IOptimizer aOptimizer) {
        mRefEng = aRefEng;
        mBasis = aBasis;
        mNN = aNN;
        
        mTrainData = new DataSet();
        mTestData = new DataSet();
        
        mOptimizer = aOptimizer;
        IVector tPara = mNN.parameters();
        mGradParaBuf1 = Vectors.zeros(tPara.size());
        mGradParaBuf2 = Vectors.zeros(tPara.size());
        mOptimizer.setParameter(tPara);
        mOptimizer.setLossFunc(para -> {
            double rLoss = 0.0;
            for (int i = 0; i < mTrainData.mSize; ++i) {
                double rEng = 0.0;
                Vector[] tFp = mTrainData.mFp.get(i);
                for (Vector tSubFp : tFp) {
                    rEng += aNN.forward(tSubFp);
                }
                rEng /= tFp.length;
                double tErr = rEng - mTrainData.mEng.get(i);
                rLoss += tErr*tErr;
            }
            return rLoss / mTrainData.mSize;
        });
        mOptimizer.setLossFuncGrad((para, grad) -> {
            grad.fill(0.0);
            double rLoss = 0.0;
            for (int i = 0; i < mTrainData.mSize; ++i) {
                double rEng = 0.0;
                Vector[] tFp = mTrainData.mFp.get(i);
                mGradParaBuf2.fill(0.0);
                for (Vector tSubFp : tFp) {
                    rEng += aNN.backwardFull(tSubFp, null, mGradParaBuf1);
                    mGradParaBuf2.plus2this(mGradParaBuf1);
                }
                rEng /= tFp.length;
                double tErr = rEng - mTrainData.mEng.get(i);
                grad.operation().mplus2this(mGradParaBuf2, 2.0 * tErr / tFp.length);
                rLoss += tErr*tErr;
            }
            grad.div2this(mTrainData.mSize);
            return rLoss / mTrainData.mSize;
        });
    }
    public TrainerNative(double aRefEng, Basis aBasis, IOptimizer aOptimizer) {
        this(aRefEng, aBasis, FeedForward.init(aBasis.size(), new int[]{32, 32}, RANDOM::nextGaussian), aOptimizer);
    }
    public TrainerNative(double aRefEng, Basis aBasis) {
        this(aRefEng, aBasis, new Adam());
    }
    
    
    public void addTrainData(IAtomData aAtomData, double aEnergy) {
        // 由于数据集不完整因此这里不去做归一化
        final int tAtomNum = aAtomData.atomNumber();
        try (final AtomicParameterCalculator tAPC = AtomicParameterCalculator.of(aAtomData)) {
            Vector[] rFp = new Vector[tAtomNum];
            mTrainData.mFp.add(rFp);
            for (int i = 0; i < tAtomNum; ++i) {
                Vector tFp = Vectors.zeros(mBasis.size());
                mBasis.eval(tAPC, i, tFp);
                rFp[i] = tFp;
                // 计算相对能量值
                aEnergy -= mRefEng;
            }
        }
        // 这里后添加能量，这样 rData.mEng.size() 对应正确的索引
        mTrainData.mEng.add(aEnergy/tAtomNum);
        ++mTrainData.mSize;
    }
    
    /** 开始训练模型，这里直接训练给定的步数 */
    public void train(int aEpochs, boolean aPrintLog) {
        mOptimizer.run(aEpochs, aPrintLog);
    }
}
