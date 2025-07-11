package jsex.nnap;

import jse.atom.AtomicParameterCalculator;
import jse.atom.IAtomData;
import jse.code.collection.DoubleList;
import jse.math.MathEX;
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
    protected final Vector mNormMu, mNormSigma;
    protected double mNormMuEng = 0.0, mNormSigmaEng = 0.0;
    protected final double mRefEng;
    
    private final Vector mFpBuf;
    private final Vector mGradParaBuf1, mGradParaBuf2;
    
    TrainerNative(double aRefEng, Basis aBasis, FeedForward aNN, IOptimizer aOptimizer) {
        mRefEng = aRefEng;
        mBasis = aBasis;
        mNN = aNN;
        
        mTrainData = new DataSet();
        mTestData = new DataSet();
        int tSize = mBasis.size();
        mNormMu = Vector.zeros(tSize);
        mNormSigma = Vector.zeros(tSize);
        mFpBuf = Vector.zeros(tSize);
        
        mOptimizer = aOptimizer;
        IVector tPara = mNN.parameters();
        mGradParaBuf1 = Vectors.zeros(tPara.size());
        mGradParaBuf2 = Vectors.zeros(tPara.size());
        mOptimizer.setParameter(tPara);
        mOptimizer.setLossFunc(() -> {
            double rLoss = 0.0;
            for (int i = 0; i < mTrainData.mSize; ++i) {
                double rEng = 0.0;
                Vector[] tFp = mTrainData.mFp.get(i);
                for (Vector tSubFp : tFp) {
                    mFpBuf.fill(j -> (tSubFp.get(j) - mNormMu.get(j)) / mNormSigma.get(j));
                    rEng += mNN.forward(mFpBuf);
                }
                rEng /= tFp.length;
                double tErr = rEng - (mTrainData.mEng.get(i) - mNormMuEng)/mNormSigmaEng;
                rLoss += tErr*tErr;
            }
            return rLoss / mTrainData.mSize;
        });
        mOptimizer.setLossFuncGrad(grad -> {
            grad.fill(0.0);
            double rLoss = 0.0;
            for (int i = 0; i < mTrainData.mSize; ++i) {
                double rEng = 0.0;
                Vector[] tFp = mTrainData.mFp.get(i);
                mGradParaBuf2.fill(0.0);
                for (Vector tSubFp : tFp) {
                    mFpBuf.fill(j -> (tSubFp.get(j) - mNormMu.get(j)) / mNormSigma.get(j));
                    rEng += aNN.backwardFull(mFpBuf, null, mGradParaBuf1);
                    mGradParaBuf2.plus2this(mGradParaBuf1);
                }
                rEng /= tFp.length;
                double tErr = rEng - (mTrainData.mEng.get(i) - mNormMuEng)/mNormSigmaEng;
                grad.operation().mplus2this(mGradParaBuf2, 2.0 * tErr / tFp.length);
                rLoss += tErr*tErr;
            }
            grad.div2this(mTrainData.mSize);
            return rLoss / mTrainData.mSize;
        });
    }
    public TrainerNative(double aRefEng, Basis aBasis, IOptimizer aOptimizer) {
        this(aRefEng, aBasis, FeedForward.init(aBasis.size(), new int[]{32, 32}), aOptimizer);
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
    
    protected void initNormBasis() {
        mNormMu.fill(0.0);
        mNormSigma.fill(0.0);
        double tDiv = 0.0;
        for (Vector[] tFp : mTrainData.mFp) for (Vector tSubFp : tFp) {
            mNormMu.plus2this(tSubFp);
            mNormSigma.operation().operate2this(tSubFp, (lhs, rhs) -> lhs + rhs*rhs);
            ++tDiv;
        }
        mNormMu.div2this(tDiv);
        mNormSigma.div2this(tDiv);
        mNormSigma.operation().operate2this(mNormMu, (lhs, rhs) -> lhs - rhs*rhs);
        mNormSigma.operation().map2this(MathEX.Fast::sqrt);
    }
    protected void initNormEng() {
        // 这里采用中位数和上下四分位数来归一化能量
        Vector tSortedEng = mTrainData.mEng.copy2vec();
        tSortedEng.sort();
        int tSize = tSortedEng.size();
        int tSize2 = tSize/2;
        mNormMuEng = tSortedEng.get(tSize2);
        if ((tSize&1)==1) {
            mNormMuEng = (mNormMuEng + tSortedEng.get(tSize2+1))*0.5;
        }
        int tSize4 = tSize2/2;
        double tEng14 = tSortedEng.get(tSize4);
        double tEng14R = tSortedEng.get(tSize4+1);
        int tSize34 = tSize2+tSize4;
        if ((tSize&1)==1) ++tSize34;
        double tEng34 = tSortedEng.get(tSize34);
        double tEng34R = tSortedEng.get(tSize34+1);
        if ((tSize&1)==1) {
            if ((tSize2&1)==1) {
                tEng14 = (tEng14 + 3*tEng14R)*0.25;
                tEng34 = (3*tEng34 + tEng34R)*0.25;
            } else {
                tEng14 = (3*tEng14 + tEng14R)*0.25;
                tEng34 = (tEng34 + 3*tEng34R)*0.25;
            }
        } else {
            if ((tSize2&1)==1) {
                tEng14 = (tEng14 + tEng14R)*0.5;
                tEng34 = (tEng34 + tEng34R)*0.5;
            }
        }
        mNormSigmaEng = tEng34 - tEng14;
    }
    
    /** 开始训练模型，这里直接训练给定的步数 */
    public void train(int aEpochs, boolean aPrintLog) {
        // 重新构建归一化参数
        initNormBasis();
        initNormEng();
        // 开始训练
        mOptimizer.run(aEpochs, aPrintLog);
    }
}
