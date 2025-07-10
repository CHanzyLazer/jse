package jsex.nnap;

import jse.code.collection.DoubleList;
import jse.math.matrix.RowMatrix;
import jse.math.vector.Vector;
import jse.opt.Adam;
import jse.opt.IOptimizer;
import jsex.nnap.basis.Basis;
import jsex.nnap.nn.FeedForward;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    
    TrainerNative(Basis aBasis, FeedForward aNN, IOptimizer aOptimizer) {
        mBasis = aBasis;
        mNN = aNN;
        
        mTrainData = new DataSet();
        mTestData = new DataSet();
        
        mOptimizer = aOptimizer;
        mOptimizer.setParameter(mNN.parameters());
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
    }
    public TrainerNative(Basis aBasis, IOptimizer aOptimizer) {
        this(aBasis, FeedForward.init(aBasis.size(), new int[]{32, 32}, RANDOM::nextGaussian), aOptimizer);
    }
    public TrainerNative(Basis aBasis) {
        this(aBasis, new Adam());
    }
}
