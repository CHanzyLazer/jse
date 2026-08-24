package jsex.nnap;

import jse.atom.*;
import jse.cache.IntVectorCache;
import jse.cache.LogicalVectorCache;
import jse.cache.VectorCache;
import jse.code.Conf;
import jse.code.IO;
import jse.code.UT;
import jse.code.collection.*;
import jse.code.io.ISavable;
import jse.code.timer.AccumulatedTimer;
import jse.cptr.*;
import jse.math.MathEX;
import jse.math.matrix.IMatrix;
import jse.math.vector.*;
import jse.math.vector.Vector;
import jse.optim.Adam;
import jse.optim.IOptimizer;
import jse.optim.LBFGS;
import jse.parallel.ParforThreadPool;
import jsex.nnap.basis.MirrorBasis;
import jsex.nnap.basis.SharedBasis;
import org.apache.groovy.util.Maps;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

import static java.nio.file.StandardOpenOption.APPEND;
import static jse.code.CS.MASS;

/**
 * 纯 jse + jit 实现的 nnap 训练器，从而实现更高的优化效果
 * <p>
 * 新结构大幅简化了训练器的实现
 * @author liqa
 */
public class TrainerNNAP implements IHasSymbol, ISavable, AutoCloseable {
    protected final static String DEFAULT_UNITS = "metal";
    protected final static double DEFAULT_ENERGY_WEIGHT = 1.0;
    protected final static double DEFAULT_FORCE_WEIGHT = 0.1;
    protected final static double DEFAULT_STRESS_WEIGHT = 0.1;
    protected final static int DEFAULT_NTHREADS = 4;
    protected final static double DEFAULT_BASIS_MAX = 5.0;
    protected final static int DEFAULT_RFUSE_SIZE = 6;
    protected final static String DEFAULT_WTYPE = "rfuse";
    protected final static long DEFAULT_CACHE_LIMIT = 16*1024*1024; // 16MB
    
    public final static ILossFunc LOSS_SQUARE = (pred, real, grad) -> {
        double tErr = pred - real;
        grad.mValue = 2.0*tErr;
        return tErr*tErr;
    };
    public final static ILossFunc LOSS_ABSOLUTE = (pred, real, grad) -> {
        double tErr = pred - real;
        grad.mValue = (pred-real)>=0 ? 1.0 : -1.0;
        return Math.abs(tErr);
    };
    public final static ILossFunc LOSS_SMOOTHL1 = (pred, real, grad) -> {
        double tErr = pred - real;
        double tErrAbs = Math.abs(tErr);
        grad.mValue = tErrAbs>=1.0 ? (tErr>=0?1.0:-1.0) : tErr;
        return tErrAbs>=1.0 ? (tErrAbs-0.5) : (0.5*tErr*tErr);
    };
    
    protected static class TrainAtomData extends AbstractAtomData {
        private final Vector mPosX, mPosY, mPosZ;
        private final IntVector mType;
        private final IBox mBox;
        private final int mNumAtoms, mNumTypes;
        private final String @Nullable[] mSymbols;
        public TrainAtomData(IAtomData aData) {
            mNumAtoms = aData.natoms();
            mNumTypes = aData.ntypes();
            List<String> tSymbols = aData.symbols();
            if (tSymbols==null) {
                mSymbols = null;
            } else {
                mSymbols = new String[mNumTypes];
                for (int i = 0; i < mNumTypes; ++i) {
                    mSymbols[i] = tSymbols.get(i);
                }
            }
            IBox tBox = aData.box();
            if (tBox.isPrism()) {
                mBox = new BoxPrism(tBox.a(), tBox.b(), tBox.c());
            } else {
                mBox = new Box(tBox);
            }
            mPosX = Vector.zeros(mNumAtoms);
            mPosY = Vector.zeros(mNumAtoms);
            mPosZ = Vector.zeros(mNumAtoms);
            mType = IntVector.zeros(mNumAtoms);
            for (int i = 0; i < mNumAtoms; ++i) {
                IAtom tAtom = aData.atom(i);
                mPosX.set(i, tAtom.x());
                mPosY.set(i, tAtom.y());
                mPosZ.set(i, tAtom.z());
                mType.set(i, tAtom.type());
            }
        }
        
        
        @Override public IAtom atom(int aIdx) {
            return new AbstractAtom_() {
                @Override public int index() {return aIdx;}
                @Override public double x() {return mPosX.get(aIdx);}
                @Override public double y() {return mPosY.get(aIdx);}
                @Override public double z() {return mPosZ.get(aIdx);}
                @Override protected int type_() {return mType.get(aIdx);}
            };
        }
        @Override public IBox box() {
            return mBox;
        }
        @Override public int natoms() {
            return mNumAtoms;
        }
        @Override public int ntypes() {
            return mNumTypes;
        }
        @Override public boolean hasSymbol() {
            return mSymbols!=null;
        }
        @Override public @Nullable String symbol(int aType) {
            return mSymbols==null ? null : mSymbols[aType-1];
        }
        @Override public boolean hasMass() {
            return hasSymbol();
        }
        @Override public double mass(int aType) {
            @Nullable String tSymbol = symbol(aType);
            return tSymbol==null ? Double.NaN : MASS.getOrDefault(tSymbol, Double.NaN);
        }
    }
    
    protected static class DataSet {
        public int mSize = 0;
        /** 临时原子数据存储 */
        public final List<IAtomData> mDataTemp = new ArrayList<>(64);
        /** 原子数据存储，对应不缓存近邻使用 */
        public final List<IAtomData> mData = new ArrayList<>(64);
        /** 每个原子数据结构对应的每原子能量值 */
        public final DoubleList mEng = new DoubleList(64);
        /** 这里力数据使用 List 存储，每个原子结构一组 */
        public final List<Vector> mForceX = new ArrayList<>(64), mForceY = new ArrayList<>(64), mForceZ = new ArrayList<>(64);
        /** 每个原子数据结构对应的压力值 */
        public final DoubleList mStressXX = new DoubleList(64), mStressYY = new DoubleList(64), mStressZZ = new DoubleList(64),
                                mStressXY = new DoubleList(64), mStressXZ = new DoubleList(64), mStressYZ = new DoubleList(64);
        /** 原子种类，每个原子结构一组 */
        public final List<IntVector> mAtomType = new ArrayList<>(64);
        /** 每个原子数据结构对应的总体积值 */
        public final DoubleList mVolume = new DoubleList(64);
        
        /** 原子近邻原子数，每个原子结构一组 */
        public final List<IntVector> mNlSize = new ArrayList<>(64);
        /** 近邻列表，每个原子结构一组，每个原子对应一个近邻列表向量 */
        public final List<IntCPointer[]> mNlIdx = new ArrayList<>(64);
        /** 现在存储近邻的种类列表，这样不用每次遍历都临时构造 */
        public final List<IntCPointer[]> mNlType = new ArrayList<>(64);
        /** 近邻原子坐标差，每个原子结构一组，每个原子对应一个近邻列表向量 */
        public final List<IDoubleOrFloatCPointer[]> mNlDx = new ArrayList<>(64);
        public final List<IDoubleOrFloatCPointer[]> mNlDy = new ArrayList<>(64);
        public final List<IDoubleOrFloatCPointer[]> mNlDz = new ArrayList<>(64);
        
        long statNlSize() {
            long rNlSize = 0;
            for (int i = 0; i < mSize; ++i) {
                rNlSize += mNlSize.get(i).sum();
            }
            return rNlSize;
        }
        long statMaxCacheSize(NNAP aNNAP, boolean aEnergyOnly) {
            long rMaxCacheSize = 0;
            for (int i = 0; i < mSize; ++i) {
                IntVector tAtomType = mAtomType.get(i);
                IntVector tNlSize = mNlSize.get(i);
                int tNumAtoms = tAtomType.size();
                long rCacheSize = 0;
                for (int k = 0; k < tNumAtoms; ++k) {
                    if (aEnergyOnly) {
                        rCacheSize += aNNAP.forwardEnergyCacheSize(tNlSize.get(k), tAtomType.get(k));
                    } else {
                        rCacheSize += aNNAP.forwardEnergyForceCacheSize(tNlSize.get(k), tAtomType.get(k));
                    }
                }
                if (rCacheSize > rMaxCacheSize) {
                    rMaxCacheSize = rCacheSize;
                }
            }
            return rMaxCacheSize;
        }
    }
    
    /// ParforThreadPool stuffs
    protected final ParforThreadPool mPool;
    @Override public void close() throws Exception {
        mPool.close();
        mNNAP.close();
    }
    /// IHasSymbol stuffs
    @Override public int ntypes() {return mNNAP.ntypes();}
    @Override public boolean hasSymbol() {return true;}
    @Override public String symbol(int aType) {return mNNAP.symbol(aType);}
    public NNAP model() {return mNNAP;}
    public String units() {return mNNAP.units();}
    public String precision() {return mNNAP.precision();}
    
    
    protected final boolean mSingle;
    protected final DataSet mTrainData, mTestData;
    protected final boolean mIsRetrain;
    protected final NNAP mNNAP;
    protected final IVector mRefEngs, mRefEngScales;
    protected double mNormMuEng = 0.0, mNormSigmaEng = 1.0;
    protected double mUnitLen = 1.0;
    protected boolean mHasForce = false;
    protected boolean mHasStress = false;
    protected boolean mHasTest = false;
    protected final DoubleList mTrainLoss = new DoubleList(64);
    protected final DoubleList mTestLoss = new DoubleList(64);
    protected final DoubleList mTrainLossE = new DoubleList(64), mTrainLossF = new DoubleList(64), mTrainLossS = new DoubleList(64);
    protected final DoubleList mTestLossE = new DoubleList(64), mTestLossF = new DoubleList(64), mTestLossS = new DoubleList(64);
    protected final Vector mLossDetail = Vector.zeros(3);
    protected @Nullable String mLossOutPath = null;
    protected boolean mLossOutInit = false;
    protected boolean mNormInit = false;
    protected boolean mFirstTrain = true;
    
    protected int mStepsPerEpoch = 1;
    protected int mStep = -1;
    protected int mEpoch = -1, mNEpochs = -1;
    protected int mSelectEpoch = -1;
    protected double mMinLoss = Double.POSITIVE_INFINITY;
    protected final IVector mSelectParas;
    private IntVector mAllSliceTrain = null;
    
    /// buffer stuffs
    private final List<List<IDoubleOrFloatCPointer>> mCacheBuf;
    private final IDoubleOrFloatCPointer[] mAGradNlDxBuf, mAGradNlDyBuf, mAGradNlDzBuf;
    private final IDoubleOrFloatCPointer[] mBGradAGradNlDxBuf, mBGradAGradNlDyBuf, mBGradAGradNlDzBuf;
    private final IDoubleOrFloatCPointer[] mForceXBuf, mForceYBuf, mForceZBuf, mVirialBuf;
    private final IDoubleOrFloatCPointer[] mBGradForceXBuf, mBGradForceYBuf, mBGradForceZBuf, mBGradVirialBuf;
    private final List<List<IntCPointer>> mNlIdxBuf, mNlTypeBuf;
    private final List<List<IDoubleOrFloatCPointer>> mNlDxBuf, mNlDyBuf, mNlDzBuf;
    private final NeighborListGetter[] mNlBuf;
    
    protected boolean mCacheNl = true;
    private boolean mNlCached = false;
    public TrainerNNAP setCacheNl(boolean aFlag) {
        mCacheNl = aFlag;
        if (!mCacheNl && mNlCached) throw new IllegalStateException("Cannot turn off caching after caching neighbors");
        return this;
    }
    
    protected Boolean mCacheForward = null;
    public TrainerNNAP setCacheForward(boolean aFlag) {
        mCacheForward = aFlag;
        return this;
    }
    
    protected double mEnergyWeight = DEFAULT_ENERGY_WEIGHT;
    public TrainerNNAP setEnergyWeight(double aWeight) {
        mEnergyWeight = aWeight;
        mOptimizer.markLossFuncChanged();
        return this;
    }
    protected double mForceWeight = DEFAULT_FORCE_WEIGHT;
    public TrainerNNAP setForceWeight(double aWeight) {
        mForceWeight = aWeight;
        mOptimizer.markLossFuncChanged();
        return this;
    }
    protected double mStressWeight = DEFAULT_STRESS_WEIGHT;
    public TrainerNNAP setStressWeight(double aWeight) {
        mStressWeight = aWeight;
        mOptimizer.markLossFuncChanged();
        return this;
    }
    
    protected ILossFunc mLossFuncEng = LOSS_SMOOTHL1;
    public TrainerNNAP setLossFuncEnergy(ILossFunc aLossFunc) {
        mLossFuncEng = aLossFunc;
        mOptimizer.markLossFuncChanged();
        return this;
    }
    protected ILossFunc mLossFuncForce = LOSS_SMOOTHL1;
    public TrainerNNAP setLossFuncForce(ILossFunc aLossFunc) {
        mLossFuncForce = aLossFunc;
        mOptimizer.markLossFuncChanged();
        return this;
    }
    protected ILossFunc mLossFuncStress = LOSS_SMOOTHL1;
    public TrainerNNAP setLossFuncStress(ILossFunc aLossFunc) {
        mLossFuncStress = aLossFunc;
        mOptimizer.markLossFuncChanged();
        return this;
    }
    public TrainerNNAP setLossFunc(ILossFunc aLossFunc) {
        mLossFuncEng = aLossFunc;
        mLossFuncForce = aLossFunc;
        mLossFuncStress = aLossFunc;
        mOptimizer.markLossFuncChanged();
        return this;
    }
    public TrainerNNAP reset() {
        mOptimizer.reset();
        return this;
    }
    
    protected boolean mAutoBreak = true;
    public TrainerNNAP setAutoBreak(boolean aFlag) {
        mAutoBreak = aFlag;
        return this;
    }
    
    protected IOptimizer mOptimizer = new LBFGS(100).setLineSearch();
    protected int mBatchSize = -1;
    public TrainerNNAP setOptimizer(Map<String, ?> aOptArgs) {
        if (aOptArgs == null) {
            aOptArgs = Maps.of("type", "lbfgs");
        }
        Object tOptType = aOptArgs.get("type");
        if (tOptType == null) {
            tOptType = "lbfgs";
        }
        switch(tOptType.toString()) {
        case "lbfgs": case "LBFGS": {
            mBatchSize = ((Number)UT.Code.getWithDefault(aOptArgs, -1, "batch_size", "batchsize")).intValue();
            double tEta = ((Number)UT.Code.getWithDefault(aOptArgs, 0.001, "learning_rate", "lr", "eta")).doubleValue();
            int tM = ((Number)UT.Code.getWithDefault(aOptArgs, 100, "history_size", "history", "m")).intValue();
            mOptimizer = new LBFGS(tM).setLearningRate(tEta);
            if (mBatchSize > 0) mOptimizer.setNoLineSearch(); // batch 情况下不能线搜索，因为线搜索会使用上一步的梯度
            else mOptimizer.setLineSearch();
            break;
        }
        case "adam": case "Adam": {
            mBatchSize = ((Number)UT.Code.getWithDefault(aOptArgs, 512, "batch_size", "batchsize")).intValue();
            double tEta = ((Number)UT.Code.getWithDefault(aOptArgs, 0.001, "learning_rate", "lr", "eta")).doubleValue();
            double tBeta1 = ((Number)UT.Code.getWithDefault(aOptArgs, 0.9, "beta1")).doubleValue();
            double tBeta2 = ((Number)UT.Code.getWithDefault(aOptArgs, 0.999, "beta2")).doubleValue();
            double tEps = ((Number)UT.Code.getWithDefault(aOptArgs, 1e-8, "epsilon", "eps")).doubleValue();
            boolean tAMSGrad = (Boolean)UT.Code.getWithDefault(aOptArgs, false, "amsgrad");
            mOptimizer = new Adam(tEta, tBeta1, tBeta2, tEps, tAMSGrad);
            break;
        }
        default: {
            throw new IllegalArgumentException("Unsupported optimizer type: " + tOptType);
        }}
        initOptimizer_();
        return this;
    }
    public TrainerNNAP setLearningRate(double aLearningRate) {
        mOptimizer.setLearningRate(aLearningRate);
        return this;
    }
    public TrainerNNAP setBatchSize(int aBatchSize) {
        mBatchSize = aBatchSize;
        if (mOptimizer instanceof LBFGS) {
            if (mBatchSize > 0) mOptimizer.setNoLineSearch(); // batch 情况下不能线搜索，因为线搜索会使用上一步的梯度
            else mOptimizer.setLineSearch();
        }
        return this;
    }
    
    /**
     * 设置基组最大值限制，现在会根据此值在基组归一化时进行缩放限制，提高描述能力
     * @param aValue 设置值
     * @return 自身方便链式调用
     */
    public TrainerNNAP setBasisMax(double aValue) {
        mBasisMax = aValue;
        return this;
    }
    protected double mBasisMax = DEFAULT_BASIS_MAX;
    /**
     * 设置所有种类共享相同的归一化系数，只在初始化归一化系数之前设置才能影响初始化
     * <p>
     * 现在默认不会进行基组归一化，因此直接采用 {@link SharedBasis}
     * 的写法即可自动实现这个效果
     * @param aFlag 设置值
     * @return 自身方便链式调用
     */
    public TrainerNNAP setShareNorm(boolean aFlag) {
        if (mShareNorm!=null &&  mShareNorm==aFlag) return this;
        if (aFlag) {
            // 检测基组长度是否相等
            final int tNumTypes = ntypes();
            final int tBasisSize = mNNAP.mBasis[0].size();
            for (int i = 1; i < tNumTypes; ++i) {
                if (tBasisSize != mNNAP.mBasis[i].size()) throw new IllegalArgumentException("Basis sizes mismatch for share norm");
            }
        }
        mShareNorm = aFlag;
        return this;
    }
    protected @Nullable Boolean mShareNorm = null;
    protected boolean mSharedBasis = true;
    
    @SuppressWarnings({"unchecked"})
    TrainerNNAP(@Range(from=1, to=Integer.MAX_VALUE) int aNumThreads, Map<String, ?> aArgs, @Nullable Map<String, ?> aModelInfo) throws Exception {
        mPool = new ParforThreadPool(aNumThreads);
        if (aModelInfo != null) {
            mIsRetrain = true;
            argsCheckRetrain_(aArgs);
            mNNAP = new NNAP(aModelInfo, aNumThreads);
            // 重新实现部分读取来降低不必要的代码耦合
            List<? extends Map<String, ?>> tModelInfos = (List<? extends Map<String, ?>>)aModelInfo.get("models");
            if (tModelInfos == null) throw new IllegalArgumentException("No models in ModelInfo");
            mRefEngs = refEngsFromModelInfo_(mNNAP, tModelInfos);
            mRefEngScales = refEngScalesFromModelInfo_(mNNAP, tModelInfos);
            initNormFromModelInfo_(mNNAP, tModelInfos);
            // 标记 retrain 不需要重新设置归一化系数
            mNormInit = true;
        } else {
            mIsRetrain = false;
            mNNAP = nnapFromArgs_(aArgs, aNumThreads);
            mRefEngs = refEngsFromArgs_(mNNAP, aArgs);
            mRefEngScales = refEngScalesFromArgs_(mNNAP, aArgs);
            if (mNNAP.ntypes() != mRefEngs.size()) throw new IllegalArgumentException("Symbols length does not match reference energies length.");
            if (mNNAP.ntypes() != mRefEngScales.size()) throw new IllegalArgumentException("Symbols length does not match reference energy scales length.");
            // 初始化 nnap 内部参数
            mNNAP.initParameters();
        }
        mSingle = mNNAP.mSingle;
        final int tNumTypes = mNNAP.ntypes();
        mSelectParas = mNNAP.parameters().copy();
        
        // 简单遍历 basis 验证 mirror 的情况
        for (int i = 0; i < tNumTypes; ++i) if (mNNAP.mBasis[i] instanceof MirrorBasis) {
            MirrorBasis tBasis = (MirrorBasis)mNNAP.mBasis[i];
            int tMirrorType = tBasis.mirrorType();
            double oRefEng = mRefEngs.get(i);
            double tRefEng = mRefEngs.get(tMirrorType-1);
            if (!Double.isNaN(oRefEng) && !MathEX.Code.numericEqual(oRefEng, tRefEng)) {
                UT.Code.warning("RefEng of mirror mismatch for type: "+(i+1)+", overwrite with mirror values automatically");
            }
            mRefEngs.set(i, tRefEng);
            double oRefEngScale = mRefEngScales.get(i);
            double tRefEngScale = mRefEngScales.get(tMirrorType-1);
            if (!Double.isNaN(oRefEngScale) && !MathEX.Code.numericEqual(oRefEngScale, tRefEngScale)) {
                UT.Code.warning("RefEngScale of mirror mismatch for type: "+(i+1)+", overwrite with mirror values automatically");
            }
            mRefEngScales.set(i, tRefEngScale);
        }
        // 简单遍历识别 shared 基组情况
        for (int i = 1; i < tNumTypes; ++i) {
            if (!(mNNAP.mBasis[i] instanceof SharedBasis)) {
                mSharedBasis = false;
                break;
            }
        }
        
        mTrainData = new DataSet();
        mTestData = new DataSet();
        
        mCacheBuf = new ArrayList<>(aNumThreads);
        mAGradNlDxBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mAGradNlDyBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mAGradNlDzBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mBGradAGradNlDxBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mBGradAGradNlDyBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mBGradAGradNlDzBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mForceXBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mForceYBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mForceZBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mVirialBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mBGradForceXBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mBGradForceYBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mBGradForceZBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mBGradVirialBuf = new IDoubleOrFloatCPointer[aNumThreads];
        mNlIdxBuf = new ArrayList<>(aNumThreads);
        mNlTypeBuf = new ArrayList<>(aNumThreads);
        mNlDxBuf = new ArrayList<>(aNumThreads);
        mNlDyBuf = new ArrayList<>(aNumThreads);
        mNlDzBuf = new ArrayList<>(aNumThreads);
        mNlBuf = new NeighborListGetter[aNumThreads];
        for (int ti = 0; ti < aNumThreads; ++ti) {
            mCacheBuf.add(new ArrayList<>(16));
            mAGradNlDxBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle);
            mAGradNlDyBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle);
            mAGradNlDzBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle);
            mBGradAGradNlDxBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle);
            mBGradAGradNlDyBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle);
            mBGradAGradNlDzBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle);
            mForceXBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle);
            mForceYBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle);
            mForceZBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle);
            mVirialBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle, 6);
            mBGradForceXBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle);
            mBGradForceYBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle);
            mBGradForceZBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle);
            mBGradVirialBuf[ti] = mNNAP.mPtrMngPar[ti].newDoubleOrFloatCPointer(mSingle, 6);
            mNlIdxBuf.add(new ArrayList<>(16));
            mNlTypeBuf.add(new ArrayList<>(16));
            mNlDxBuf.add(new ArrayList<>(16));
            mNlDyBuf.add(new ArrayList<>(16));
            mNlDzBuf.add(new ArrayList<>(16));
            mNlBuf[ti] = new NeighborListGetter();
        }
        initOptimizer_();
    }
    /**
     * 创建一个 nnap 的训练器
     * @param aArgs 训练参数
     * @param aModelInfo 可选的旧的模型数据，用于继续训练。当传入时训练参数仅可选：
     * <dl>
     *   <dt>nthreads (可选，默认为 4):</dt>
     *     <dd>指定训练时使用的线程数</dd>
     *   <dt>energy_weight (可选，默认为 1.0):</dt>
     *     <dd>指定 loss 函数中能量的权重</dd>
     *   <dt>force_weight (可选，默认为 0.1):</dt>
     *     <dd>指定 loss 函数中力的权重</dd>
     *   <dt>stress_weight (可选，默认为 0.1):</dt>
     *     <dd>指定 loss 函数中压力的权重</dd>
     *   <dt>cache_nl (可选，默认为 true):</dt>
     *     <dd>是否缓存所有的近邻列表，这个会大幅提高速度但是会占用更多内存</dd>
     *   <dt>optimizer (可选):</dt>
     *     <dd>
     *       指定优化器的具体参数，包含：
     *       <dl>
     *       <dt>type (可选，默认为 "lbfgs", 可选 "adam"):</dt>
     *          <dd>指定优化器的种类</dd>
     *       <dt>lr (可选，默认为 0.001):</dt>
     *          <dd>指定随机优化器采用的学习率</dd>
     *       <dt>batch_size (可选，lbfgs 默认为 -1, adam 默认为 512):</dt>
     *          <dd>指定随机优化器采用的 batch_size</dd>
     *       </dl>
     *     </dd>
     * </dl>
     */
    @SuppressWarnings({"unchecked", "resource"})
    public TrainerNNAP(Map<String, ?> aArgs, @Nullable Map<String, ?> aModelInfo) throws Exception {
        this(nthreadsFromArgs_(aArgs), aArgs, aModelInfo);
        @Nullable Map<String, ?> tOptim = (Map<String, ?>)UT.Code.get(aArgs, "optimizer", "optim", "opt");
        if (tOptim != null) {
            setOptimizer(tOptim);
        }
        @Nullable Number tEnergyWeight = (Number)UT.Code.get(aArgs, "energy_weight", "eng_weight");
        if (tEnergyWeight != null) {
            setEnergyWeight(tEnergyWeight.doubleValue());
        }
        @Nullable Number tForceWeight = (Number)UT.Code.get(aArgs, "force_weight");
        if (tForceWeight != null) {
            setForceWeight(tForceWeight.doubleValue());
        }
        @Nullable Number tStressWeight = (Number)UT.Code.get(aArgs, "stress_weight");
        if (tStressWeight != null) {
            setStressWeight(tStressWeight.doubleValue());
        }
        @Nullable Boolean tShareNorm = (Boolean)UT.Code.get(aArgs, "share_norm");
        if (tShareNorm != null) {
            setShareNorm(tShareNorm);
        }
        @Nullable Boolean tCacheNl = (Boolean)UT.Code.get(aArgs, "cache_nl");
        if (tCacheNl != null) {
            setCacheNl(tCacheNl);
        }
    }
    /**
     * 创建一个 nnap 的训练器
     * @param aArgs 训练参数，具体为：
     * <dl>
     *   <dt>symbols:</dt>
     *     <dd>指定元素列表</dd>
     *   <dt>ref_engs (可选):</dt>
     *     <dd>指定每个元素的参考能量</dd>
     *   <dt>ref_eng_scales (可选):</dt>
     *     <dd>指定每个元素的参考能量缩放比例，和每原子能量的方差正相关</dd>
     *   <dt>nthreads (可选，默认为 4):</dt>
     *     <dd>指定训练时使用的线程数</dd>
     *   <dt>energy_weight (可选，默认为 1.0):</dt>
     *     <dd>指定 loss 函数中能量的权重</dd>
     *   <dt>force_weight (可选，默认为 0.1):</dt>
     *     <dd>指定 loss 函数中力的权重</dd>
     *   <dt>stress_weight (可选，默认为 0.1):</dt>
     *     <dd>指定 loss 函数中压力的权重</dd>
     *   <dt>units (可选，默认为 'metal'):</dt>
     *     <dd>指定势函数的单位</dd>
     *   <dt>cache_nl (可选，默认为 true):</dt>
     *     <dd>是否缓存所有的近邻列表，这个会大幅提高速度但是会占用更多内存</dd>
     *   <dt>basis (可选):</dt>
     *     <dd>
     *       指定基组的具体参数，包含：
     *       <dl>
     *       <dt>type (可选，默认为 "spherical_chebyshev"):</dt>
     *          <dd>指定基组的种类</dd>
     *       <dt>lmax (可选，默认为 6):</dt>
     *          <dd>指定基组角向序使用的最大 l</dd>
     *       <dt>nmax (可选，默认为 5):</dt>
     *          <dd>指定基组径向序使用的最大 n</dd>
     *       <dt>rcut (可选，默认为 6.0):</dt>
     *          <dd>指定基组的截断半径</dd>
     *       <dt>post_fuse_size (可选，默认为 6):</dt>
     *          <dd>指定基组针对等变量的线性混合后的维度</dd>
     *       <dt>wtype (可选，默认为 "exfull"):</dt>
     *          <dd>指定基组的化学编码方式</dd>
     *       </dl>
     *       输入列表形式则为每个种类单独设置不同的基组参数
     *     </dd>
     *   <dt>nn (可选):</dt>
     *     <dd>
     *       指定神经网络具体结构，包含：
     *       <dl>
     *       <dt>hidden_dims (可选，默认为 [32, 32]):</dt>
     *          <dd>指定神经网络每个隐藏层的神经元数目</dd>
     *       <dt>shared_hidden_dims (可选，默认不开启):</dt>
     *          <dd>指定神经网络每个隐藏层共享的神经元数目</dd>
     *       </dl>
     *       输入列表形式则为每个种类单独设置不同的神经网络参数
     *     </dd>
     *   <dt>optimizer (可选):</dt>
     *     <dd>
     *       指定优化器的具体参数，包含：
     *       <dl>
     *       <dt>type (可选，默认为 "lbfgs", 可选 "adam"):</dt>
     *          <dd>指定优化器的种类</dd>
     *       <dt>lr (可选，默认为 0.001):</dt>
     *          <dd>指定随机优化器采用的学习率</dd>
     *       <dt>batch_size (可选，lbfgs 默认为 -1, adam 默认为 512):</dt>
     *          <dd>指定随机优化器采用的 batch_size</dd>
     *       </dl>
     *     </dd>
     * </dl>
     */
    public TrainerNNAP(Map<String, ?> aArgs) throws Exception {
        this(aArgs, null);
    }
    
    private static int nthreadsFromArgs_(Map<String, ?> aArgs) {
        return ((Number)UT.Code.getWithDefault(aArgs, DEFAULT_NTHREADS, "number_of_threads", "nthreads")).intValue();
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static NNAP nnapFromArgs_(Map<String, ?> aArgs, int aNumThreads) throws Exception {
        /// symbols
        String[] aSymbols;
        @Nullable Object tSymbols = UT.Code.get(aArgs, "symbols", "elems", "species");
        if (tSymbols == null) throw new IllegalArgumentException("args of trainer MUST contain `symbols`");
        if (tSymbols instanceof Collection) {
            aSymbols = IO.Text.toArray((Collection)tSymbols);
        } else
        if (tSymbols instanceof Object[]) {
            aSymbols = IO.Text.toArray((List) AbstractCollections.from((Object[])tSymbols));
        } else {
            throw new IllegalArgumentException("invalid type of symbols: " + tSymbols.getClass().getName());
        }
        /// basis
        // 为了输入参数安全，这里统一采用创建新值的方式避免修改
        List<Map> aBasisSetting = new ArrayList<>(aSymbols.length);
        @Nullable Object tBasisSetting = UT.Code.get(aArgs, "basis");
        if (tBasisSetting == null) {
            tBasisSetting = Maps.of("type", "spherical_chebyshev");
        }
        // 遍历读取塞入 aBasisSetting
        if (tBasisSetting instanceof Map) {
            // 现在这种情况其余的种类采用 share 基组
            Map<?, ?> tSubBasis = (Map<?, ?>)tBasisSetting;
            aBasisSetting.add(new HashMap(tSubBasis));
            for (int i = 1; i < aSymbols.length; ++i) {
                aBasisSetting.add(Maps.of("type", "share", "share", 1));
            }
        } else {
            for (Object tObj : (List<?>)tBasisSetting) {
                aBasisSetting.add(new HashMap((Map<?, ?>)tObj));
            }
        }
        if (aSymbols.length != aBasisSetting.size()) throw new IllegalArgumentException("Symbols length does not match reference basis length.");
        // 现在默认塞入 rfull，这里现在就可以简单实现不用考虑引用问题
        Consumer<Map> tBasisValider = subBasis -> {
            // 在不存在 wtype 时塞入 rfull
            subBasis.putIfAbsent("wtype", DEFAULT_WTYPE);
            subBasis.putIfAbsent("rfuse_size", DEFAULT_RFUSE_SIZE);
            // 不再支持 post_fuse 训练
            if ((Boolean)UT.Code.getWithDefault(subBasis, false, "post_fuse") || subBasis.containsKey("post_fuse_size") || subBasis.containsKey("post_fuse_weight")) {
                throw new IllegalArgumentException("Training with post_fuse is invalid now, use wtype='rfuse'");
            }
        };
        // 顺便统计 mirror 来方便后续处理
        LogicalVector tIsMirror = LogicalVector.zeros(aSymbols.length);
        for (int i = 0; i < aSymbols.length; ++i) {
            Map tSubBasis = (Map)aBasisSetting.get(i);
            // 只塞 spherical_chebyshev 和 chebyshev
            Object tBasisType = tSubBasis.get("type");
            if (tBasisType==null) tBasisType = "spherical_chebyshev";
            switch(tBasisType.toString()) {
            case "mirror": case "mirror_basis": {
                tIsMirror.set(i, true);
                break;
            }
            case "chebyshev": case "cheby":
            case "spherical_chebyshev": case "sph_cheby": {
                tBasisValider.accept(tSubBasis);
                break;
            }
            case "merge": {
                List<?> tMergedBasis = (List<?>)tSubBasis.get("basis");
                List<Map> aMergedBasis = new ArrayList<>(tMergedBasis.size());
                tSubBasis.put("basis", aMergedBasis);
                for (Object tObj2 : tMergedBasis) {
                    Map tSubBasis2 = new HashMap((Map<?, ?>)tObj2); // 注意这里属于深度拷贝，因此需要重新手动拷贝
                    aMergedBasis.add(tSubBasis2);
                    // 只塞 spherical_chebyshev 和 chebyshev
                    Object tBasisType2 = tSubBasis2.get("type");
                    if (tBasisType2==null) tBasisType2 = "spherical_chebyshev";
                    switch(tBasisType2.toString()) {
                    case "chebyshev": case "cheby":
                    case "spherical_chebyshev": case "sph_cheby": {
                        tBasisValider.accept(tSubBasis2);
                        break;
                    }}
                }
                break;
            }}
        }
        /// nn
        // 为了输入参数安全，这里统一采用创建新值的方式避免修改
        List<Map> aNNSetting = new ArrayList<>(aSymbols.length);
        @Nullable Object tNNSetting = UT.Code.get(aArgs, "nn");
        if (tNNSetting == null) {
            tNNSetting = new HashMap<>();
        }
        // 遍历读取塞入 aNNSetting
        if (tNNSetting instanceof Map) {
            Map tSubNNSetting = new HashMap<>((Map<?, ?>)tNNSetting);
            // 单 map 输入下特殊处理，只需要单个 shared_hidden_dims 即可
            @Nullable Object tSharedHiddenDims = tSubNNSetting.remove("shared_hidden_dims");
            if (tSharedHiddenDims == null) {
                tSharedHiddenDims = tSubNNSetting.remove("shared_nnarch");
            }
            tSubNNSetting.put("type", "feed_forward");
            aNNSetting.add(tIsMirror.get(0) ? null : tSubNNSetting);
            if (tSharedHiddenDims != null) {
                if (tIsMirror.get(0)) throw new IllegalArgumentException("shared nn CAN NOT share the mirror type (1)");
                tSubNNSetting = Maps.of("type", "shared_feed_forward", "share", 1, "shared_hidden_dims", tSharedHiddenDims);
            }
            for (int i = 1; i < aSymbols.length; ++i) {
                aNNSetting.add(tIsMirror.get(i) ? null : tSubNNSetting);
            }
        } else {
            for (Object tObj : (List<?>)tNNSetting) {
                aNNSetting.add(new HashMap((Map<?, ?>)tObj));
            }
        }
        if (aSymbols.length != aNNSetting.size()) throw new IllegalArgumentException("Symbols length does not match neural network length.");
        /// nnap input
        Map rModelInfos = new HashMap<>();
        List rModels = new ArrayList();
        for (int i = 0; i < aSymbols.length; ++i) {
            Map rModel = new HashMap();
            rModel.put("symbol", aSymbols[i]);
            rModel.put("basis", aBasisSetting.get(i));
            rModel.put("nn", aNNSetting.get(i));
            rModels.add(rModel);
        }
        rModelInfos.put("version", NNAP.VERSION);
        rModelInfos.put("units", UT.Code.getWithDefault(aArgs, DEFAULT_UNITS, "units").toString());
        rModelInfos.put("models", rModels);
        return new NNAP(rModelInfos, aNumThreads);
    }
    @SuppressWarnings("unchecked")
    private static IVector refEngsFromArgs_(NNAP aNNAP, Map<String, ?> aArgs) {
        @Nullable Object tRefEngs = UT.Code.get(aArgs, "ref_engs", "reference_energies", "erefs");
        if (tRefEngs == null) return Vectors.zeros(aNNAP.ntypes());
        if (tRefEngs instanceof Collection) {
            return Vectors.from((Collection<? extends Number>)tRefEngs);
        } else
        if (tRefEngs instanceof double[]) {
            return Vectors.from((double[])tRefEngs);
        } else
        if (tRefEngs instanceof IVector) {
            return Vectors.from((IVector)tRefEngs);
        } else {
            throw new IllegalArgumentException("invalid type of ref_engs: " + tRefEngs.getClass().getName());
        }
    }
    @SuppressWarnings("unchecked")
    private static IVector refEngScalesFromArgs_(NNAP aNNAP, Map<String, ?> aArgs) {
        @Nullable Object tRefEngScales = UT.Code.get(aArgs, "ref_eng_scales", "reference_energy_scales");
        if (tRefEngScales == null) return Vectors.ones(aNNAP.ntypes());
        if (tRefEngScales instanceof Collection) {
            return Vectors.from((Collection<? extends Number>) tRefEngScales);
        } else
        if (tRefEngScales instanceof double[]) {
            return Vectors.from((double[]) tRefEngScales);
        } else
        if (tRefEngScales instanceof IVector) {
            return Vectors.from((IVector) tRefEngScales);
        } else {
            throw new IllegalArgumentException("invalid type of ref_eng_scales: " + tRefEngScales.getClass().getName());
        }
    }
    
    private static void argsCheckRetrain_(Map<String, ?> aArgs) {
        if (UT.Code.get(aArgs, "symbols", "elems", "species") != null) {
            throw new IllegalArgumentException("args of trainer can NOT contain `symbols` for retraining");
        }
        if (UT.Code.get(aArgs, "ref_engs", "reference_energies", "erefs") != null) {
            throw new IllegalArgumentException("args of trainer can NOT contain `ref_engs` for retraining");
        }
        if (UT.Code.get(aArgs, "ref_eng_scales", "reference_energy_scales") != null) {
            throw new IllegalArgumentException("args of trainer can NOT contain `ref_eng_scales` for retraining");
        }
        if (UT.Code.get(aArgs, "basis") != null) {
            throw new IllegalArgumentException("args of trainer can NOT contain `basis` for retraining");
        }
        if (UT.Code.get(aArgs, "nn") != null) {
            throw new IllegalArgumentException("args of trainer can NOT contain `nn` for retraining");
        }
        if (UT.Code.get(aArgs, "units") != null) {
            throw new IllegalArgumentException("args of trainer can NOT contain `units` for retraining");
        }
    }
    private static IVector refEngsFromModelInfo_(NNAP aNNAP, List<? extends Map<String, ?>> aModelInfos) {
        final int tNumTypes = aNNAP.ntypes();
        IVector tRefEngs = Vectors.zeros(tNumTypes);
        for (int i = 0; i < tNumTypes; ++i) {
            Number tRefEng = (Number)aModelInfos.get(i).get("ref_eng");
            if (aNNAP.mBasis[i] instanceof MirrorBasis) {
                // mirror 会强制这些额外值缺省
                if (tRefEng != null) throw new IllegalArgumentException("ref_eng in mirror_basis MUST be empty");
                tRefEngs.set(i, Double.NaN);
            } else {
                tRefEngs.set(i, tRefEng==null?0.0:tRefEng.doubleValue());
            }
        }
        return tRefEngs;
    }
    private static IVector refEngScalesFromModelInfo_(NNAP aNNAP, List<? extends Map<String, ?>> aModelInfos) {
        final int tNumTypes = aNNAP.ntypes();
        IVector tRefEngScales = Vectors.zeros(tNumTypes);
        for (int i = 0; i < tNumTypes; ++i) {
            Number tRefEngScale = (Number)aModelInfos.get(i).get("ref_eng_scale");
            if (aNNAP.mBasis[i] instanceof MirrorBasis) {
                // mirror 会强制这些额外值缺省
                if (tRefEngScale != null) throw new IllegalArgumentException("ref_eng_scale in mirror_basis MUST be empty");
                tRefEngScales.set(i, Double.NaN);
            } else {
                tRefEngScales.set(i, tRefEngScale==null?1.0:tRefEngScale.doubleValue());
            }
        }
        return tRefEngScales;
    }
    private void initNormFromModelInfo_(NNAP aNNAP, List<? extends Map<String, ?>> aModelInfos) {
        final int tNumTypes = aNNAP.ntypes();
        Number tNormSigmaEng = null, tNormMuEng = null;
        for (int i = 0; i < tNumTypes; ++i) {
            Map<String, ?> tModelInfo = aModelInfos.get(i);
            // 现在优先读取 norm eng
            if (tNormSigmaEng == null) tNormSigmaEng = (Number)tModelInfo.get("norm_sigma_eng");
            if (tNormMuEng == null) tNormMuEng = (Number)tModelInfo.get("norm_mu_eng");
        }
        mNormSigmaEng = tNormSigmaEng==null ? 1.0 : tNormSigmaEng.doubleValue();
        mNormMuEng = tNormMuEng==null ? 0.0 : tNormMuEng.doubleValue();
    }
    
    private String epochStr_(int aEpoch) {
        String tNEpochs = String.valueOf(mNEpochs);
        String tCEpochs = String.format("%0"+tNEpochs.length()+"d", aEpoch+1);
        return "epoch: "+tCEpochs;
    }
    private void initOptimizer_() {
        final int[] tLossDiv = {0};
        final double[] tLossTot = {0.0, 0.0, 0.0, 0.0};
        mOptimizer.setParameter(mNNAP.parameters())
        .setParameterUpdater(mNNAP::updateParameters)
        .setLossFunc(() -> calLossDetail(false, mLossDetail))
        .setLossFuncGrad(grad -> calLoss(false, false, mLossDetail, grad))
        .setLogPrinter((step, lineSearchStep, loss, printLog) -> {
            mStep = step;
            mEpoch = step / mStepsPerEpoch;
            int tStepIdx = step % mStepsPerEpoch;
            if (mBatchSize > 0) {
                int tRestSize = mTrainData.mSize % mBatchSize;
                int tBatchSize = (tStepIdx==mStepsPerEpoch-1) ? (tRestSize+mBatchSize) : mBatchSize;
                tLossDiv[0] += tBatchSize;
                tLossTot[0] += tBatchSize * loss;
                tLossTot[1] += tBatchSize * mLossDetail.get(0);
                tLossTot[2] += tBatchSize * mLossDetail.get(1);
                tLossTot[3] += tBatchSize * mLossDetail.get(2);
                if (tStepIdx == mStepsPerEpoch-1) {
                    if (mEpoch != mNEpochs-1) {
                        mAllSliceTrain.shuffle();
                        mSliceTrain = mAllSliceTrain.subVec(0, (mStepsPerEpoch==1) ? (tRestSize+mBatchSize) : mBatchSize);
                    }
                } else {
                    int tStart = (tStepIdx+1) * mBatchSize;
                    int tEnd = tStart + mBatchSize;
                    if (tStepIdx+1 == mStepsPerEpoch-1) tEnd += tRestSize;
                    mSliceTrain = mAllSliceTrain.subVec(tStart, tEnd);
                    if (printLog) UT.Timer.progressBar(String.format("loss: %.4g", tLossTot[0]/tLossDiv[0]));
                }
            } else {
                mSliceTrain = mFullSliceTrain;
            }
            if (tStepIdx == mStepsPerEpoch-1) {
                final double lossE, lossF, lossS;
                if (mBatchSize > 0) {
                    loss = tLossTot[0]/tLossDiv[0];
                    lossE = tLossTot[1]/tLossDiv[0];
                    lossF = tLossTot[2]/tLossDiv[0];
                    lossS = tLossTot[3]/tLossDiv[0];
                    tLossDiv[0] = 0;
                    tLossTot[0] = 0.0; tLossTot[1] = 0.0; tLossTot[2] = 0.0; tLossTot[3] = 0.0;
                } else {
                    lossE = mLossDetail.get(0);
                    lossF = mLossDetail.get(1);
                    lossS = mLossDetail.get(2);
                }
                mTrainLoss.add(loss);
                mTrainLossE.add(lossE);
                mTrainLossF.add(lossF);
                mTrainLossS.add(lossS);
                if (mHasTest) {
                    double tLossTest = calLossDetail(true, mLossDetail);
                    mTestLoss.add(tLossTest);
                    mTestLossE.add(mLossDetail.get(0));
                    mTestLossF.add(mLossDetail.get(1));
                    mTestLossS.add(mLossDetail.get(2));
                    if (tLossTest < mMinLoss) {
                        mSelectEpoch = mEpoch;
                        mMinLoss = tLossTest;
                        mSelectParas.fill(mNNAP.parameters());
                    }
                    if (printLog) UT.Timer.progressBar(String.format("loss: %.4g | %.4g", loss, tLossTest));
                } else {
                    if (printLog) UT.Timer.progressBar(String.format("loss: %.4g", loss));
                }
                try {writeLoss();}
                catch (IOException e) {throw new RuntimeException(e);}
                if (printLog && mBatchSize>0 && mEpoch!=mNEpochs-1) {
                    UT.Timer.progressBar(Maps.of(
                        "name", epochStr_(mEpoch+1),
                        "max", mStepsPerEpoch,
                        "length", 100
                    ));
                }
            }
        })
        .setBreakChecker((step, loss, lastLoss, parameterStep) -> {
            if (!mAutoBreak) return false; // 现在允许直接关闭自动跳出，在训练末期重新开始训练时步长会非常小
            if (mBatchSize > 0) return false; // 分 batch 情况永远不跳出，因为梯度随机
            if (step==0 || Double.isNaN(lastLoss)) return false;
            return Math.abs(lastLoss-loss) < Math.abs(lastLoss)*1e-7;
        });
    }
    
    @FunctionalInterface public interface ILossFunc {
        double call(double aPred, double aReal, DoubleWrapper rGrad);
    }
    
    private final ISlice mFullSliceTest = new ISlice() {
        @Override public int get(int aIdx) {
            if (aIdx >= mTestData.mSize) throw new IndexOutOfBoundsException();
            return aIdx;
        }
        @Override public int size() {
            return mTestData.mSize;
        }
    };
    private final ISlice mFullSliceTrain = new ISlice() {
        @Override public int get(int aIdx) {
            if (aIdx >= mTrainData.mSize) throw new IndexOutOfBoundsException();
            return aIdx;
        }
        @Override public int size() {
            return mTrainData.mSize;
        }
    };
    private ISlice mSliceTrain = null;
    
    protected double calLoss(boolean aTest) {
        return calLoss(aTest, null);
    }
    protected double calLoss(boolean aTest, @Nullable Vector rGrad) {
        return calLoss(aTest, false, null, rGrad);
    }
    protected double calLossDetail(boolean aTest, @Nullable Vector rLossDetail) {
        return calLoss(aTest, true, rLossDetail, null);
    }
    protected double calLoss(boolean aTest, boolean aFull, @Nullable Vector rLossDetail, @Nullable Vector rGrad) {
        return calLoss_(aTest, false, aTest ? mFullSliceTest : (aFull?mFullSliceTrain:mSliceTrain), rLossDetail, rGrad,
                        mLossFuncEng, mLossFuncForce, mLossFuncStress);
    }
    protected void calMAE(boolean aTest, Vector rMAE) {
        calLoss_(aTest, true, aTest?mFullSliceTest:mFullSliceTrain, rMAE, null,
                 LOSS_ABSOLUTE, LOSS_ABSOLUTE, LOSS_ABSOLUTE);
    }
    private double calLoss_(boolean aTest, boolean aRawLoss, ISlice aSlice, @Nullable Vector rLossDetail, @Nullable Vector rGrad,
                            ILossFunc aLossFuncEng, ILossFunc aLossFuncForce, ILossFunc aLossFuncStress) {
        final DataSet tData = aTest ? mTestData : mTrainData;
        final boolean tRequireGrad = rGrad!=null;
        if (aTest && tRequireGrad) throw new IllegalStateException();
        if (aRawLoss && tRequireGrad) throw new IllegalStateException();
        final int tNumThreads = mPool.nthreads();
        if (tRequireGrad) {
            mNNAP.requireGrad();
            mNNAP.zeroGrad();
        }
        // 遍历统计有效数据数目
        final int tSliceSize = aSlice.size();
        int tEngSize = 0, tForceSize = 0, tStressSize = 0;
        for (int si = 0; si < tSliceSize; ++si) {
            final int i = aSlice.get(si);
            if (!Double.isNaN(tData.mEng.get(i))) {
                ++tEngSize;
            }
            if (mHasForce) {
                Vector tFxReal = tData.mForceX.get(i);
                if (tFxReal!=null) {
                    tForceSize += tFxReal.size();
                }
            }
            if (mHasStress) {
                if (!Double.isNaN(tData.mStressXX.get(i))) {
                    ++tStressSize;
                }
            }
        }
        final int fEngSize = tEngSize, fForceSize = tForceSize, fStressSize = tStressSize;
        List<Vector> rLossPar = VectorCache.getZeros(3, tNumThreads);
        mPool.parfor(tSliceSize, (si, threadID) -> {
            final int i = aSlice.get(si);
            Vector rLoss = rLossPar.get(threadID);
            PointerManager tPtrMng = mNNAP.mPtrMngPar[threadID];
            
            double tEngReal = tData.mEng.get(i);
            final boolean tHasEng = !Double.isNaN(tEngReal);
            
            Vector tFxReal = null, tFyReal = null, tFzReal = null;
            if (mHasForce) {
                tFxReal = tData.mForceX.get(i); tFyReal = tData.mForceY.get(i); tFzReal = tData.mForceZ.get(i);
            }
            final boolean tHasForce = tFxReal!=null;
            
            double tSxxReal = Double.NaN, tSyyReal = Double.NaN, tSzzReal = Double.NaN;
            double tSxyReal = Double.NaN, tSxzReal = Double.NaN, tSyzReal = Double.NaN;
            if (mHasStress) {
                tSxxReal = tData.mStressXX.get(i); tSyyReal = tData.mStressYY.get(i); tSzzReal = tData.mStressZZ.get(i);
                tSxyReal = tData.mStressXY.get(i); tSxzReal = tData.mStressXZ.get(i); tSyzReal = tData.mStressYZ.get(i);
            }
            final boolean tHasStress = !Double.isNaN(tSxxReal);
            
            IntVector tAtomType = tData.mAtomType.get(i);
            IntVector tNlSize = tData.mNlSize.get(i);
            int tNumAtoms = tAtomType.size();
            
            // 通用方式获取近邻列表
            List<IntCPointer> tNlIdx = mNlIdxBuf.get(threadID);
            List<IntCPointer> tNlType = mNlTypeBuf.get(threadID);
            List<IDoubleOrFloatCPointer> tNlDx = mNlDxBuf.get(threadID);
            List<IDoubleOrFloatCPointer> tNlDy = mNlDyBuf.get(threadID);
            List<IDoubleOrFloatCPointer> tNlDz = mNlDzBuf.get(threadID);
            buildNl(aTest, i, threadID, tNlIdx, tNlType, tNlDx, tNlDy, tNlDz);
            
            DoubleWrapper rBGradEng = new DoubleWrapper(0.0);
            List<IDoubleOrFloatCPointer> rCache = mCacheBuf.get(threadID);
            IDoubleOrFloatCPointer rCache0 = null;
            if (mCacheForward) {
                while (rCache.size() < tNumAtoms) rCache.add(tPtrMng.newDoubleOrFloatCPointer(mSingle));
            } else {
                if (rCache.isEmpty()) rCache.add(tPtrMng.newDoubleOrFloatCPointer(mSingle));
                rCache0 = rCache.get(0);
            }
            
            if (!tHasForce && !tHasStress) {
                if (!tHasEng) return;
                
                double rEng = 0.0;
                for (int k = 0; k < tNumAtoms; ++k) {
                    final int tSubNlSize = tNlSize.get(k);
                    final int ctype = tAtomType.get(k);
                    IDoubleOrFloatCPointer tSubCache = mCacheForward ? rCache.get(k) : rCache0;
                    tPtrMng.ensureCapacity(tSubCache, mNNAP.forwardEnergyCacheSize(tSubNlSize, ctype));
                    rEng += mNNAP.forwardEnergy(
                        threadID, ctype, tSubNlSize,
                        tNlDx.get(k), tNlDy.get(k), tNlDz.get(k), tNlType.get(k),
                        tSubCache
                    );
                }
                // 注意 nnap 内部获取能量会是原始值，这里需要再次归一化；这里按照 initNormEng 一样的方式来归一化
                double tScaledNAtoms = 0.0;
                if (aRawLoss) {
                    tScaledNAtoms = tNumAtoms;
                } else {
                    for (int k = 0; k < tNumAtoms; ++k) {
                        int ctype = tAtomType.get(k);
                        double tRefEng = mRefEngs.get(ctype-1);
                        rEng -= tRefEng;
                        tEngReal -= tRefEng;
                        tScaledNAtoms += mRefEngScales.get(ctype-1);
                    }
                }
                rEng /= tScaledNAtoms;
                tEngReal /= tScaledNAtoms;
                if (!aRawLoss) {
                    // 采用 initNormEng 一样的方式来归一化可以确保至少此时是正态分布的
                    rEng = (rEng - mNormMuEng) / mNormSigmaEng;
                    tEngReal = (tEngReal - mNormMuEng) / mNormSigmaEng;
                }
                double tLossEng = aLossFuncEng.call(rEng, tEngReal, rBGradEng);
                if (!aRawLoss) {
                    tLossEng *= mEnergyWeight;
                }
                rLoss.add(0, tLossEng / fEngSize);
                /// backward
                if (!tRequireGrad) return;
                double tBGradEng = mEnergyWeight * rBGradEng.value() / fEngSize;
                tBGradEng /= (mNormSigmaEng * tScaledNAtoms);
                for (int k = 0; k < tNumAtoms; ++k) {
                    final int tSubNlSize = tNlSize.get(k);
                    final int ctype = tAtomType.get(k);
                    IntCPointer tSubNlType = tNlType.get(k);
                    IDoubleOrFloatCPointer tSubNlDx = tNlDx.get(k), tSubNlDy = tNlDy.get(k), tSubNlDz = tNlDz.get(k);
                    IDoubleOrFloatCPointer tSubCache = mCacheForward ? rCache.get(k) : rCache0;
                    if (!mCacheForward) {
                        mNNAP.forwardEnergy(
                            threadID, ctype, tSubNlSize,
                            tSubNlDx, tSubNlDy, tSubNlDz, tSubNlType,
                            tSubCache
                        );
                    }
                    mNNAP.backwardEnergy(
                        threadID, ctype, tSubNlSize,
                        tSubNlDx, tSubNlDy, tSubNlDz, tSubNlType,
                        tBGradEng, tSubCache
                    );
                }
                return;
            }
            double tVolume = tData.mVolume.get(i);
            
            DoubleWrapper rSubBGradFx = new DoubleWrapper(0.0), rSubBGradFy = new DoubleWrapper(0.0), rSubBGradFz = new DoubleWrapper(0.0);
            DoubleWrapper rBGradSxx = new DoubleWrapper(0.0), rBGradSyy = new DoubleWrapper(0.0), rBGradSzz = new DoubleWrapper(0.0);
            DoubleWrapper rBGradSxy = new DoubleWrapper(0.0), rBGradSxz = new DoubleWrapper(0.0), rBGradSyz = new DoubleWrapper(0.0);
            
            IDoubleOrFloatCPointer rAGradNlDx = mAGradNlDxBuf[threadID];
            IDoubleOrFloatCPointer rAGradNlDy = mAGradNlDyBuf[threadID];
            IDoubleOrFloatCPointer rAGradNlDz = mAGradNlDzBuf[threadID];
            IDoubleOrFloatCPointer rFx = mForceXBuf[threadID];
            IDoubleOrFloatCPointer rFy = mForceYBuf[threadID];
            IDoubleOrFloatCPointer rFz = mForceZBuf[threadID];
            IDoubleOrFloatCPointer rV = mVirialBuf[threadID];
            IDoubleOrFloatCPointer rBGradAGradNlDx = mBGradAGradNlDxBuf[threadID];
            IDoubleOrFloatCPointer rBGradAGradNlDy = mBGradAGradNlDyBuf[threadID];
            IDoubleOrFloatCPointer rBGradAGradNlDz = mBGradAGradNlDzBuf[threadID];
            IDoubleOrFloatCPointer rBGradFx = mBGradForceXBuf[threadID];
            IDoubleOrFloatCPointer rBGradFy = mBGradForceYBuf[threadID];
            IDoubleOrFloatCPointer rBGradFz = mBGradForceZBuf[threadID];
            IDoubleOrFloatCPointer rBGradV = mBGradVirialBuf[threadID];
            
            double rEng = 0.0;
            tPtrMng.ensureCapacity(rFx, tNumAtoms); rFx.fillD(0.0, tNumAtoms);
            tPtrMng.ensureCapacity(rFy, tNumAtoms); rFy.fillD(0.0, tNumAtoms);
            tPtrMng.ensureCapacity(rFz, tNumAtoms); rFz.fillD(0.0, tNumAtoms);
            rV.fillD(0.0, 6);
            for (int k = 0; k < tNumAtoms; ++k) {
                final int tSubNlSize = tNlSize.get(k);
                final int ctype = tAtomType.get(k);
                IntCPointer tSubNlIdx = tNlIdx.get(k), tSubNlType = tNlType.get(k);
                IDoubleOrFloatCPointer tSubNlDx = tNlDx.get(k), tSubNlDy = tNlDy.get(k), tSubNlDz = tNlDz.get(k);
                
                tPtrMng.ensureCapacity(rAGradNlDx, tSubNlSize);
                tPtrMng.ensureCapacity(rAGradNlDy, tSubNlSize);
                tPtrMng.ensureCapacity(rAGradNlDz, tSubNlSize);
                
                IDoubleOrFloatCPointer tSubCache = mCacheForward ? rCache.get(k) : rCache0;
                tPtrMng.ensureCapacity(tSubCache, mNNAP.forwardEnergyForceCacheSize(tSubNlSize, ctype));
                rEng += mNNAP.forwardEnergyForce(
                    threadID, ctype, tSubNlSize,
                    tSubNlDx, tSubNlDy, tSubNlDz, tSubNlType,
                    rAGradNlDx, rAGradNlDy, rAGradNlDz,
                    tSubCache
                );
                // 调用 native 累加得到力和压力值
                mNNAP.forwardForceCollect(
                    k, tSubNlSize,
                    tSubNlDx, tSubNlDy, tSubNlDz, tSubNlIdx,
                    rAGradNlDx, rAGradNlDy, rAGradNlDz,
                    rFx, rFy, rFz, rV
                );
            }
            // cal stress here
            double rSxx = 0.0, rSyy = 0.0, rSzz = 0.0, rSxy = 0.0, rSxz = 0.0, rSyz = 0.0;
            if (tHasStress) {
                rSxx = -rV.getAtD(0)/tVolume; rSyy = -rV.getAtD(1)/tVolume; rSzz = -rV.getAtD(2)/tVolume;
                rSxy = -rV.getAtD(3)/tVolume; rSxz = -rV.getAtD(4)/tVolume; rSyz = -rV.getAtD(5)/tVolume;
            }
            // 采用一致的归一化方式，那么这里会得到等效的缩放系数
            double tScaledNAtoms = 0.0;
            if (aRawLoss) {
                tScaledNAtoms = tNumAtoms;
            } else {
                for (int k = 0; k < tNumAtoms; ++k) {
                    tScaledNAtoms += mRefEngScales.get(tAtomType.get(k)-1);
                }
            }
            double tEngScale = tScaledNAtoms / tNumAtoms;
            // 能量队归一化以及 loss 计算
            if (tHasEng) {
                if (!aRawLoss) {
                    for (int k = 0; k < tNumAtoms; ++k) {
                        double tRefEng = mRefEngs.get(tAtomType.get(k)-1);
                        rEng -= tRefEng;
                        tEngReal -= tRefEng;
                    }
                }
                rEng /= tScaledNAtoms;
                tEngReal /= tScaledNAtoms;
                if (!aRawLoss) {
                    rEng = (rEng - mNormMuEng) / mNormSigmaEng;
                    tEngReal = (tEngReal - mNormMuEng) / mNormSigmaEng;
                }
                double tLossEng = aLossFuncEng.call(rEng, tEngReal, rBGradEng);
                if (!aRawLoss) {
                    tLossEng *= mEnergyWeight;
                }
                rLoss.add(0, tLossEng / fEngSize);
            }
            // 力和压力 loss 计算
            if (tRequireGrad) {
                tPtrMng.ensureCapacity(rBGradFx, tNumAtoms); rBGradFx.fillD(0.0, tNumAtoms);
                tPtrMng.ensureCapacity(rBGradFy, tNumAtoms); rBGradFy.fillD(0.0, tNumAtoms);
                tPtrMng.ensureCapacity(rBGradFz, tNumAtoms); rBGradFz.fillD(0.0, tNumAtoms);
                rBGradV.fillD(0.0, 6);
            }
            if (tHasForce) {
                double tLossForce = 0.0;
                final double tMul = aRawLoss ? 1.0 : (mUnitLen/(mNormSigmaEng*tEngScale));
                final double tMulG = tMul * mForceWeight / (fForceSize*3);
                for (int k = 0; k < tNumAtoms; ++k) {
                    tLossForce += aLossFuncForce.call(rFx.getAtD(k)*tMul, tFxReal.get(k)*tMul, rSubBGradFx);
                    tLossForce += aLossFuncForce.call(rFy.getAtD(k)*tMul, tFyReal.get(k)*tMul, rSubBGradFy);
                    tLossForce += aLossFuncForce.call(rFz.getAtD(k)*tMul, tFzReal.get(k)*tMul, rSubBGradFz);
                    if (tRequireGrad) {
                        rBGradFx.putAtD(k, tMulG*rSubBGradFx.value());
                        rBGradFy.putAtD(k, tMulG*rSubBGradFy.value());
                        rBGradFz.putAtD(k, tMulG*rSubBGradFz.value());
                    }
                }
                if (!aRawLoss) {
                    tLossForce *= mForceWeight;
                }
                rLoss.add(1, tLossForce / (fForceSize*3));
            }
            if (tHasStress) {
                double tLossStress = 0.0;
                final double tMul = aRawLoss ? 1.0 : (MathEX.Code.pow3(mUnitLen)/(mNormSigmaEng*tEngScale));
                tLossStress += aLossFuncStress.call(rSxx*tMul, tSxxReal*tMul, rBGradSxx);
                tLossStress += aLossFuncStress.call(rSyy*tMul, tSyyReal*tMul, rBGradSyy);
                tLossStress += aLossFuncStress.call(rSzz*tMul, tSzzReal*tMul, rBGradSzz);
                tLossStress += aLossFuncStress.call(rSxy*tMul, tSxyReal*tMul, rBGradSxy);
                tLossStress += aLossFuncStress.call(rSxz*tMul, tSxzReal*tMul, rBGradSxz);
                tLossStress += aLossFuncStress.call(rSyz*tMul, tSyzReal*tMul, rBGradSyz);
                if (tRequireGrad) {
                    final double tMulG = - tMul * mStressWeight / (tVolume * fStressSize*6);
                    rBGradV.putAtD(0, tMulG*rBGradSxx.value());
                    rBGradV.putAtD(1, tMulG*rBGradSyy.value());
                    rBGradV.putAtD(2, tMulG*rBGradSzz.value());
                    rBGradV.putAtD(3, tMulG*rBGradSxy.value());
                    rBGradV.putAtD(4, tMulG*rBGradSxz.value());
                    rBGradV.putAtD(5, tMulG*rBGradSyz.value());
                }
                if (!aRawLoss) {
                    tLossStress *= mStressWeight;
                }
                rLoss.add(2, tLossStress / (fStressSize*6));
            }
            /// backward
            if (!tRequireGrad) return;
            double tBGradEng = 0.0;
            if (tHasEng) {
                tBGradEng = mEnergyWeight * rBGradEng.value() / fEngSize;
                tBGradEng /= (mNormSigmaEng * tScaledNAtoms);
            }
            for (int k = 0; k < tNumAtoms; ++k) {
                final int tSubNlSize = tNlSize.get(k);
                final int cType = tAtomType.get(k);
                IntCPointer tSubNlIdx = tNlIdx.get(k), tSubNlType = tNlType.get(k);
                IDoubleOrFloatCPointer tSubNlDx = tNlDx.get(k), tSubNlDy = tNlDy.get(k), tSubNlDz = tNlDz.get(k);
                
                tPtrMng.ensureCapacity(rBGradAGradNlDx, tSubNlSize);
                tPtrMng.ensureCapacity(rBGradAGradNlDy, tSubNlSize);
                tPtrMng.ensureCapacity(rBGradAGradNlDz, tSubNlSize);
                // 调用 native 反向传播力和压力
                mNNAP.backwardForceCollect(
                    k, tSubNlSize,
                    tSubNlDx, tSubNlDy, tSubNlDz, tSubNlIdx,
                    rBGradAGradNlDx, rBGradAGradNlDy, rBGradAGradNlDz,
                    rBGradFx, rBGradFy, rBGradFz, rBGradV
                );
                IDoubleOrFloatCPointer tSubCache = mCacheForward ? rCache.get(k) : rCache0;
                if (!mCacheForward) {
                    mNNAP.forwardEnergyForce(
                        threadID, cType, tSubNlSize,
                        tSubNlDx, tSubNlDy, tSubNlDz, tSubNlType,
                        rAGradNlDx, rAGradNlDy, rAGradNlDz,
                        tSubCache
                    );
                }
                mNNAP.backwardEnergyForce(
                    threadID, cType, tSubNlSize,
                    tSubNlDx, tSubNlDy, tSubNlDz, tSubNlType,
                    rBGradAGradNlDx, rBGradAGradNlDy, rBGradAGradNlDz,
                    tBGradEng, tSubCache
                );
            }
        });
        if (tRequireGrad) {
            mNNAP.backwardParameter();
            rGrad.fill(mNNAP.gradParameters());
        }
        Vector rLoss = rLossPar.get(0);
        for (int ti = 1; ti < tNumThreads; ++ti) {
            rLoss.plus2this(rLossPar.get(ti));
        }
        if (rLossDetail != null) {
            rLossDetail.fill(rLoss);
        }
        double tLoss = rLoss.sum();
        VectorCache.returnVec(rLossPar);
        return tLoss;
    }
    
    
    private void addData_(IAtomData aAtomData, boolean aHasEnergy, double aEnergy,
                          boolean aHasForce, IVector aFx, IVector aFy, IVector aFz,
                          boolean aHasStress, double aSxx, double aSyy, double aSzz, double aSxy, double aSxz, double aSyz,
                          DataSet rData) {
        // 简单处理（不需要近邻列表）的数据添加在这里实现
        IntUnaryOperator tTypeMap = typeMap(aAtomData);
        final int tNumAtoms = aAtomData.natoms();
        IntList rAtomType = new IntList(tNumAtoms);
        for (int i = 0; i < tNumAtoms; ++i) {
            rAtomType.add(tTypeMap.applyAsInt(aAtomData.atom(i).type()));
        }
        rData.mDataTemp.add(new TrainAtomData(aAtomData));
        rData.mAtomType.add(rAtomType.asVec());
        rData.mVolume.append(aAtomData.volume());
        // 添加能量
        rData.mEng.append(aHasEnergy ? aEnergy : Double.NaN);
        // 添加力
        if (mHasForce) {
            if (aHasForce) {
                Vector rForceX = Vector.zeros(tNumAtoms);
                Vector rForceY = Vector.zeros(tNumAtoms);
                Vector rForceZ = Vector.zeros(tNumAtoms);
                rForceX.fill(aFx);
                rForceY.fill(aFy);
                rForceZ.fill(aFz);
                rData.mForceX.add(rForceX);
                rData.mForceY.add(rForceY);
                rData.mForceZ.add(rForceZ);
            } else {
                rData.mForceX.add(null);
                rData.mForceY.add(null);
                rData.mForceZ.add(null);
            }
        }
        // 应力
        if (mHasStress) {
            if (aHasStress) {
                rData.mStressXX.add(aSxx);
                rData.mStressYY.add(aSyy);
                rData.mStressZZ.add(aSzz);
                rData.mStressXY.add(aSxy);
                rData.mStressXZ.add(aSxz);
                rData.mStressYZ.add(aSyz);
            } else {
                rData.mStressXX.add(Double.NaN);
                rData.mStressYY.add(Double.NaN);
                rData.mStressZZ.add(Double.NaN);
                rData.mStressXY.add(Double.NaN);
                rData.mStressXZ.add(Double.NaN);
                rData.mStressYZ.add(Double.NaN);
            }
        }
        ++rData.mSize;
    }
    private void validForceData_() {
        if (mHasForce) return;
        for (int i = 0; i < mTrainData.mSize; ++i) {
            mTrainData.mForceX.add(null);
            mTrainData.mForceY.add(null);
            mTrainData.mForceZ.add(null);
        }
        if (mHasTest) {
            for (int i = 0; i < mTestData.mSize; ++i) {
                mTestData.mForceX.add(null);
                mTestData.mForceY.add(null);
                mTestData.mForceZ.add(null);
            }
        }
        mHasForce = true;
    }
    private void validStressData_() {
        if (mHasStress) return;
        for (int i = 0; i < mTrainData.mSize; ++i) {
            mTrainData.mStressXX.add(Double.NaN);
            mTrainData.mStressYY.add(Double.NaN);
            mTrainData.mStressZZ.add(Double.NaN);
            mTrainData.mStressXY.add(Double.NaN);
            mTrainData.mStressXZ.add(Double.NaN);
            mTrainData.mStressYZ.add(Double.NaN);
        }
        if (mHasTest) {
            for (int i = 0; i < mTestData.mSize; ++i) {
                mTestData.mStressXX.add(Double.NaN);
                mTestData.mStressYY.add(Double.NaN);
                mTestData.mStressZZ.add(Double.NaN);
                mTestData.mStressXY.add(Double.NaN);
                mTestData.mStressXZ.add(Double.NaN);
                mTestData.mStressYZ.add(Double.NaN);
            }
        }
        mHasStress = true;
    }
    /**
     * 增加一个训练集数据
     * @param aAtomData 原子结构数据
     * @param aEnergy 此原子结构数据的总能量
     * @param aFx 可选的每个原子的力 x 分量
     * @param aFy 可选的每个原子的力 y 分量
     * @param aFz 可选的每个原子的力 z 分量
     * @param aSxx 可选的原子结构数据的应力 xx 分量
     * @param aSyy 可选的原子结构数据的应力 yy 分量
     * @param aSzz 可选的原子结构数据的应力 zz 分量
     * @param aSxy 可选的原子结构数据的应力 xy 分量
     * @param aSxz 可选的原子结构数据的应力 xz 分量
     * @param aSyz 可选的原子结构数据的应力 yz 分量
     * @see IAtomData
     */
    public void addTrainData(IAtomData aAtomData, double aEnergy, IVector aFx, IVector aFy, IVector aFz,
                             double aSxx, double aSyy, double aSzz, double aSxy, double aSxz, double aSyz) {
        if (!mHasForce && aFx!=null) {
            // 现在支持部分数据缺省，因此这里需要补全缺省的数据占位
            validForceData_();
        }
        if (!mHasStress && !Double.isNaN(aSxx)) {
            // 现在支持部分数据缺省，因此这里需要补全缺省的数据占位
            validStressData_();
        }
        // 添加数据
        addData_(
            aAtomData, !Double.isNaN(aEnergy), aEnergy,
            aFx!=null, aFx, aFy, aFz,
            !Double.isNaN(aSxx), aSxx, aSyy, aSzz, aSxy, aSxz, aSyz,
            mTrainData
        );
    }
    /**
     * 增加一个训练集数据
     * @param aAtomData 原子结构数据
     * @param aEnergy 此原子结构数据的总能量
     * @param aForces 可选的每个原子的力，按行排列，每列对应 x,y,z 方向的力
     * @param aStress 可选的原子结构数据的应力值，按照 {@code [xx, yy, zz, xy, xz, yz]} 顺序排列
     * @see IAtomData
     * @see IMatrix
     */
    public void addTrainData(IAtomData aAtomData, double aEnergy, @Nullable IMatrix aForces, @Nullable IVector aStress) {
        addTrainData(
            aAtomData, aEnergy,
            aForces==null?null:aForces.col(0), aForces==null?null:aForces.col(1), aForces==null?null:aForces.col(2),
            aStress==null?Double.NaN:aStress.get(0), aStress==null?Double.NaN:aStress.get(1), aStress==null?Double.NaN:aStress.get(2),
            aStress==null?Double.NaN:aStress.get(3), aStress==null?Double.NaN:aStress.get(4), aStress==null?Double.NaN:aStress.get(5)
        );
    }
    /**
     * {@code addTrainData(aAtomData, aEnergy, aFx, aFy, aFz, nan, nan, nan, nan, nan, nan)}
     * @see #addTrainData(IAtomData, double, IVector, IVector, IVector, double, double, double, double, double, double)
     */
    public void addTrainData(IAtomData aAtomData, double aEnergy, IVector aFx, IVector aFy, IVector aFz) {
        addTrainData(aAtomData, aEnergy, aFx, aFy, aFz, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }
    /**
     * {@code addTrainData(aAtomData, aEnergy, aForces, null)}
     * @see #addTrainData(IAtomData, double, IMatrix, IVector)
     */
    public void addTrainData(IAtomData aAtomData, double aEnergy, IMatrix aForces) {
        addTrainData(aAtomData, aEnergy, aForces, null);
    }
    /**
     * {@code addTrainData(aAtomData, aEnergy, null, null)}
     * @see #addTrainData(IAtomData, double, IMatrix, IVector)
     */
    public void addTrainData(IAtomData aAtomData, double aEnergy) {
        addTrainData(aAtomData, aEnergy, null, null);
    }
    /**
     * 增加一个测试集数据
     * @param aAtomData 原子结构数据
     * @param aEnergy 此原子结构数据的总能量
     * @param aFx 可选的每个原子的力 x 分量
     * @param aFy 可选的每个原子的力 y 分量
     * @param aFz 可选的每个原子的力 z 分量
     * @param aSxx 可选的原子结构数据的应力 xx 分量
     * @param aSyy 可选的原子结构数据的应力 yy 分量
     * @param aSzz 可选的原子结构数据的应力 zz 分量
     * @param aSxy 可选的原子结构数据的应力 xy 分量
     * @param aSxz 可选的原子结构数据的应力 xz 分量
     * @param aSyz 可选的原子结构数据的应力 yz 分量
     * @see IAtomData
     */
    public void addTestData(IAtomData aAtomData, double aEnergy, IVector aFx, IVector aFy, IVector aFz,
                             double aSxx, double aSyy, double aSzz, double aSxy, double aSxz, double aSyz) {
        if (!mHasForce && aFx!=null) {
            // 现在支持部分数据缺省，因此这里需要补全缺省的数据占位
            validForceData_();
        }
        if (!mHasStress && !Double.isNaN(aSxx)) {
            // 现在支持部分数据缺省，因此这里需要补全缺省的数据占位
            validStressData_();
        }
        // 添加数据
        addData_(
            aAtomData, !Double.isNaN(aEnergy), aEnergy,
            aFx!=null, aFx, aFy, aFz,
            !Double.isNaN(aSxx), aSxx, aSyy, aSzz, aSxy, aSxz, aSyz,
            mTestData
        );
        if (!mHasTest) mHasTest = true;
    }
    /**
     * 增加一个测试集数据
     * @param aAtomData 原子结构数据
     * @param aEnergy 此原子结构数据的总能量
     * @see IAtomData
     * @see IMatrix
     */
    public void addTestData(IAtomData aAtomData, double aEnergy, @Nullable IMatrix aForces, @Nullable IVector aStress) {
        addTestData(
            aAtomData, aEnergy,
            aForces==null?null:aForces.col(0), aForces==null?null:aForces.col(1), aForces==null?null:aForces.col(2),
            aStress==null?Double.NaN:aStress.get(0), aStress==null?Double.NaN:aStress.get(1), aStress==null?Double.NaN:aStress.get(2),
            aStress==null?Double.NaN:aStress.get(3), aStress==null?Double.NaN:aStress.get(4), aStress==null?Double.NaN:aStress.get(5)
        );
    }
    /**
     * {@code addTestData(aAtomData, aEnergy, aFx, aFy, aFz, nan, nan, nan, nan, nan, nan)}
     * @see #addTestData(IAtomData, double, IVector, IVector, IVector, double, double, double, double, double, double)
     */
    public void addTestData(IAtomData aAtomData, double aEnergy, IVector aFx, IVector aFy, IVector aFz) {
        addTestData(aAtomData, aEnergy, aFx, aFy, aFz, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }
    /**
     * {@code addTestData(aAtomData, aEnergy, aForces, null)}
     * @see #addTestData(IAtomData, double, IMatrix, IVector)
     */
    public void addTestData(IAtomData aAtomData, double aEnergy, IMatrix aForces) {
        addTestData(aAtomData, aEnergy, aForces, null);
    }
    /**
     * {@code addTestData(aAtomData, aEnergy, null, null)}
     * @see #addTestData(IAtomData, double, IMatrix, IVector)
     */
    public void addTestData(IAtomData aAtomData, double aEnergy) {
        addTestData(aAtomData, aEnergy, null, null);
    }
    
    
    protected void buildNl(boolean aTest, int aDataIdx, int aThreadID, List<IntCPointer> rNlIdx, List<IntCPointer> rNlType,
                           List<IDoubleOrFloatCPointer> rNlDx, List<IDoubleOrFloatCPointer> rNlDy, List<IDoubleOrFloatCPointer> rNlDz) {
        DataSet tData = aTest ? mTestData : mTrainData;
        IntVector tNlSize = tData.mNlSize.get(aDataIdx);
        IntVector tAtomType = tData.mAtomType.get(aDataIdx);
        int tNumAtoms = tAtomType.size();
        if (mCacheNl) {
            rNlIdx.clear(); rNlType.clear();
            rNlDx.clear(); rNlDy.clear(); rNlDz.clear();
            IntCPointer[] tNlIdx = tData.mNlIdx.get(aDataIdx);
            IntCPointer[] tNlType = tData.mNlType.get(aDataIdx);
            IDoubleOrFloatCPointer[] tNlDx = tData.mNlDx.get(aDataIdx);
            IDoubleOrFloatCPointer[] tNlDy = tData.mNlDy.get(aDataIdx);
            IDoubleOrFloatCPointer[] tNlDz = tData.mNlDz.get(aDataIdx);
            for (int k = 0; k < tNumAtoms; ++k) {
                rNlIdx.add(tNlIdx[k]); rNlType.add(tNlType[k]);
                rNlDx.add(tNlDx[k]); rNlDy.add(tNlDy[k]); rNlDz.add(tNlDz[k]);
            }
            return;
        }
        PointerManager tPtrMng = mNNAP.mPtrMngPar[aThreadID];
        while (rNlIdx.size() < tNumAtoms) rNlIdx.add(tPtrMng.newIntCPointer());
        while (rNlType.size() < tNumAtoms) rNlType.add(tPtrMng.newIntCPointer());
        while (rNlDx.size() < tNumAtoms) rNlDx.add(tPtrMng.newDoubleOrFloatCPointer(mSingle));
        while (rNlDy.size() < tNumAtoms) rNlDy.add(tPtrMng.newDoubleOrFloatCPointer(mSingle));
        while (rNlDz.size() < tNumAtoms) rNlDz.add(tPtrMng.newDoubleOrFloatCPointer(mSingle));
        for (int k = 0; k < tNumAtoms; ++k) {
            int tSubNlSize = tNlSize.get(k);
            if (tSubNlSize > 0) {
                tPtrMng.ensureCapacity(rNlIdx.get(k), tSubNlSize);
                tPtrMng.ensureCapacity(rNlType.get(k), tSubNlSize);
                tPtrMng.ensureCapacity(rNlDx.get(k), tSubNlSize);
                tPtrMng.ensureCapacity(rNlDy.get(k), tSubNlSize);
                tPtrMng.ensureCapacity(rNlDz.get(k), tSubNlSize);
            }
        }
        NeighborListGetter tNl = mNlBuf[aThreadID];
        tNl.setData(tData.mData.get(aDataIdx)).setRCut(mNNAP.rcutMax()).build();
        for (int k = 0; k < tNumAtoms; ++k) {
            IntCPointer tSubNlIdxPtr = rNlIdx.get(k);
            IntCPointer tSubNlTypePtr = rNlType.get(k);
            IDoubleOrFloatCPointer tSubNlDxPtr = rNlDx.get(k);
            IDoubleOrFloatCPointer tSubNlDyPtr = rNlDy.get(k);
            IDoubleOrFloatCPointer tSubNlDzPtr = rNlDz.get(k);
            double tRCut = mNNAP.rcut(tAtomType.get(k));
            double tRCutSq = tRCut*tRCut;
            final int[] nli = {0};
            tNl.forEach(k, (dx, dy, dz, idx) -> {
                double dis2 = dx*dx + dy*dy + dz*dz;
                if (dis2 < tRCutSq) {
                    tSubNlIdxPtr.putAt(nli[0], idx);
                    tSubNlTypePtr.putAt(nli[0], tAtomType.get(idx));
                    tSubNlDxPtr.putAtD(nli[0], dx);
                    tSubNlDyPtr.putAtD(nli[0], dy);
                    tSubNlDzPtr.putAtD(nli[0], dz);
                    ++nli[0];
                }
            });
        }
    }
    
    protected void initDataNl() {
        final boolean tCacheNl = mCacheNl;
        if (!tCacheNl) {
            if (mNlCached) throw new IllegalStateException("Cannot turn off caching after caching neighbors");
        }
        mNlCached = true;
        final int tTrainSize = mTrainData.mDataTemp.size();
        final int tTestSize = mTestData.mDataTemp.size();
        final int tTrainStart = mTrainData.mSize - tTrainSize;
        final int tTestStart = mTestData.mSize - tTestSize;
        // 预先添加占位，因为需要保证并行线程安全且顺序一致
        for (int ai = 0; ai < tTrainSize; ++ai) {
            mTrainData.mNlSize.add(null);
            if (tCacheNl) {
                mTrainData.mNlIdx.add(null);
                mTrainData.mNlType.add(null);
                mTrainData.mNlDx.add(null);
                mTrainData.mNlDy.add(null);
                mTrainData.mNlDz.add(null);
            } else {
                mTrainData.mData.add(null);
            }
        }
        for (int ai = 0; ai < tTestSize; ++ai) {
            mTestData.mNlSize.add(null);
            if (tCacheNl) {
                mTestData.mNlIdx.add(null);
                mTestData.mNlType.add(null);
                mTestData.mNlDx.add(null);
                mTestData.mNlDy.add(null);
                mTestData.mNlDz.add(null);
            } else {
                mTestData.mData.add(null);
            }
        }
        mPool.parfor(tTrainSize+tTestSize, (ii, threadID) -> {
            DataSet rData = ii<tTrainSize ? mTrainData : mTestData;
            int ai = ii<tTrainSize ? ii : ii-tTrainSize;
            int i = ai + (ii<tTrainSize ? tTrainStart : tTestStart);
            
            IAtomData tAtomData = rData.mDataTemp.get(ai);
            NeighborListGetter tNl = mNlBuf[threadID];
            tNl.setData(tAtomData).setRCut(mNNAP.rcutMax()).build();
            rData.mDataTemp.set(ai, null); // 及时清理临时数据，降低理论的内存峰值
            
            IntVector tAtomType = rData.mAtomType.get(i);
            int tNumAtoms = tNl.natoms();
            
            IntVector rNlSize = IntVector.zeros(tNumAtoms);
            rData.mNlSize.set(i, rNlSize);
            // 不缓存则需要存储 data 值
            if (!tCacheNl) rData.mData.set(i, tAtomData);
            
            // 不论何种情况都先遍历一次记录近邻长度
            for (int k = 0; k < tNumAtoms; ++k) {
                double tRCut = mNNAP.rcut(tAtomType.get(k));
                double tRCutSq = tRCut*tRCut;
                final int[] tSubNlSize = {0};
                tNl.forEach(k, (dx, dy, dz, idx) -> {
                    double dis2 = dx*dx + dy*dy + dz*dz;
                    if (dis2 < tRCutSq) {
                        ++tSubNlSize[0];
                    }
                });
                rNlSize.set(k, tSubNlSize[0]);
            }
            // 对于需要缓存的情况再去缓存其余值
            if (tCacheNl) {
                IntCPointer[] rNlIdx = new IntCPointer[tNumAtoms];
                IntCPointer[] rNlType = new IntCPointer[tNumAtoms];
                IDoubleOrFloatCPointer[] rNlDx = new IDoubleOrFloatCPointer[tNumAtoms];
                IDoubleOrFloatCPointer[] rNlDy = new IDoubleOrFloatCPointer[tNumAtoms];
                IDoubleOrFloatCPointer[] rNlDz = new IDoubleOrFloatCPointer[tNumAtoms];
                rData.mNlIdx.set(i, rNlIdx);
                rData.mNlType.set(i, rNlType);
                rData.mNlDx.set(i, rNlDx);
                rData.mNlDy.set(i, rNlDy);
                rData.mNlDz.set(i, rNlDz);
                // 减少指针管理器的压力，这里需要合并近邻的指针
                int tTotNlSize = rNlSize.sum();
                IntCPointer tIntNlPtr = mNNAP.mPtrMngTot.newIntCPointer(tTotNlSize*2L);
                IDoubleOrFloatCPointer tFltNlPtr = mNNAP.mPtrMngTot.newDoubleOrFloatCPointer(mSingle, tTotNlSize*3L);
                for (int k = 0; k < tNumAtoms; ++k) {
                    int tSubNlSize = rNlSize.get(k);
                    // 增加近邻列表，这里直接重新添加
                    IntCPointer tNlIdxPtr = tIntNlPtr.copy(); tIntNlPtr.rightShift(tSubNlSize);
                    IntCPointer tNlTypePtr = tIntNlPtr.copy(); tIntNlPtr.rightShift(tSubNlSize);
                    IDoubleOrFloatCPointer tNlDxPtr = tFltNlPtr.copy(); tFltNlPtr.rightShift(tSubNlSize);
                    IDoubleOrFloatCPointer tNlDyPtr = tFltNlPtr.copy(); tFltNlPtr.rightShift(tSubNlSize);
                    IDoubleOrFloatCPointer tNlDzPtr = tFltNlPtr.copy(); tFltNlPtr.rightShift(tSubNlSize);
                    double tRCut = mNNAP.rcut(tAtomType.get(k));
                    double tRCutSq = tRCut*tRCut;
                    final int[] nli = {0};
                    tNl.forEach(k, false, (dx, dy, dz, idx) -> {
                        double dis2 = dx*dx + dy*dy + dz*dz;
                        if (dis2 < tRCutSq) {
                            tNlIdxPtr.putAt(nli[0], idx);
                            tNlTypePtr.putAt(nli[0], tAtomType.get(idx));
                            tNlDxPtr.putAtD(nli[0], dx);
                            tNlDyPtr.putAtD(nli[0], dy);
                            tNlDzPtr.putAtD(nli[0], dz);
                            ++nli[0];
                        }
                    });
                    rNlIdx[k] = tNlIdxPtr;
                    rNlType[k] = tNlTypePtr;
                    rNlDx[k] = tNlDxPtr;
                    rNlDy[k] = tNlDyPtr;
                    rNlDz[k] = tNlDzPtr;
                }
            }
        });
        // 总是清理 temp
        mTrainData.mDataTemp.clear();
        mTestData.mDataTemp.clear();
    }
    protected void initUnitLen() {
        // 通过近邻列表估计单位长度，这个值可以用来为力和应力无量纲化
        double rUnitLen = 0.0;
        int rNumTot = 0;
        for (int i = 0; i < mTrainData.mSize; ++i) {
            IntVector tNlSize = mTrainData.mNlSize.get(i);
            IntVector tAtomType = mTrainData.mAtomType.get(i);
            int tNumAtoms = tAtomType.size();
            for (int k = 0; k < tNumAtoms; ++k) {
                double tRCut = mNNAP.rcut(tAtomType.get(k));
                rUnitLen += Math.cbrt((4.0/3.0*MathEX.PI) * MathEX.Code.pow3(tRCut) / (tNlSize.get(k)+1));
            }
            rNumTot += tNumAtoms;
        }
        mUnitLen = rUnitLen / (double)rNumTot;
    }
    
    protected void initNormBasis() {
        final boolean tShareNorm = mShareNorm==null ? mSharedBasis : mShareNorm;
        final int tNumTypes = ntypes();
        final int tNumThreads = mPool.nthreads();
        Vector[][] tMuPar = new Vector[tNumThreads][tNumTypes];
        Vector[][] tSigmaPar = new Vector[tNumThreads][tNumTypes];
        Vector[][] tMaxPar = new Vector[tNumThreads][tNumTypes];
        Vector[][] tMinPar = new Vector[tNumThreads][tNumTypes];
        Vector[][] tFpPar = new Vector[tNumThreads][tNumTypes];
        IntVector[] tDivPar = new IntVector[tNumThreads];
        for (int ti = 0; ti < tNumThreads; ++ti) {
            for (int i = 0; i < tNumTypes; ++i) {
                int tBasisSize = mNNAP.mBasis[i].size();
                tMuPar[ti][i] = VectorCache.getZeros(tBasisSize);
                tSigmaPar[ti][i] = VectorCache.getZeros(tBasisSize);
                tMaxPar[ti][i] = VectorCache.getVec(tBasisSize); tMaxPar[ti][i].fill(Double.NEGATIVE_INFINITY);
                tMinPar[ti][i] = VectorCache.getVec(tBasisSize); tMinPar[ti][i].fill(Double.POSITIVE_INFINITY);
                tFpPar[ti][i] = VectorCache.getVec(tBasisSize);
            }
            tDivPar[ti] = IntVectorCache.getZeros(tNumTypes);
        }
        mPool.parfor(mTrainData.mSize, (i, threadID) -> {
            PointerManager tPtrMng = mNNAP.mPtrMngPar[threadID];
            
            IntVector tNlSize = mTrainData.mNlSize.get(i);
            IntVector tAtomType = mTrainData.mAtomType.get(i);
            int tNumAtoms = tAtomType.size();
            
            // 通用方式获取近邻列表
            List<IntCPointer> tNl = mNlIdxBuf.get(threadID);
            List<IntCPointer> tNlType = mNlTypeBuf.get(threadID);
            List<IDoubleOrFloatCPointer> tNlDx = mNlDxBuf.get(threadID);
            List<IDoubleOrFloatCPointer> tNlDy = mNlDyBuf.get(threadID);
            List<IDoubleOrFloatCPointer> tNlDz = mNlDzBuf.get(threadID);
            buildNl(false, i, threadID, tNl, tNlType, tNlDx, tNlDy, tNlDz);
            
            // 直接使用 nnap 内部的 cache 即可
            IDoubleOrFloatCPointer rFpPtr = mNNAP.mCache[threadID];
            Vector[] tFp = tFpPar[threadID];
            Vector[] tNormMu = tMuPar[threadID];
            Vector[] tNormSigma = tSigmaPar[threadID];
            Vector[] tMax = tMaxPar[threadID];
            Vector[] tMin = tMinPar[threadID];
            IntVector tDiv = tDivPar[threadID];
            
            for (int k = 0; k < tNumAtoms; ++k) {
                int tType = tAtomType.get(k);
                // NNAP 内部缓存使用 NNAP 内部的管理器
                tPtrMng.ensureCapacity(rFpPtr, mNNAP.mBasis[tType-1].size());
                mNNAP.calFpSingle(
                    threadID, tType, tNlSize.get(k),
                    tNlDx.get(k), tNlDy.get(k), tNlDz.get(k), tNlType.get(k),
                    rFpPtr
                );
                Vector tSubFp = tFp[tType-1];
                rFpPtr.parse2destD(tSubFp);
                // 归一化系数统计的位置，这里是这样的优先级
                int tNormIdx = tType-1;
                if (mNNAP.mBasis[tType-1] instanceof MirrorBasis) {
                    tNormIdx = ((MirrorBasis)mNNAP.mBasis[tType-1]).mirrorType() - 1;
                }
                if (tShareNorm) tNormIdx = 0;
                // 统计归一化系数
                tNormMu[tNormIdx].plus2this(tSubFp);
                tNormSigma[tNormIdx].operation().operate2this(tSubFp, (lhs, rhs) -> lhs + rhs * rhs);
                tMax[tNormIdx].operation().operate2this(tSubFp, Math::max);
                tMin[tNormIdx].operation().operate2this(tSubFp, Math::min);
                tDiv.increment(tNormIdx);
            }
        });
        for (int ti = 1; ti < tNumThreads; ++ti) {
            for (int i = 0; i < tNumTypes; ++i) {
                tMuPar[0][i].plus2this(tMuPar[ti][i]);
                tSigmaPar[0][i].plus2this(tSigmaPar[ti][i]);
                tMaxPar[0][i].operation().operate2this(tMaxPar[ti][i], Math::max);
                tMinPar[0][i].operation().operate2this(tMinPar[ti][i], Math::min);
            }
            tDivPar[0].plus2this(tDivPar[ti]);
        }
        for (int i = 0; i < tNumTypes; ++i) if ((tShareNorm && i==0) || (!tShareNorm && !(mNNAP.mBasis[i] instanceof MirrorBasis))) {
            int tDivI = tDivPar[0].get(i);
            if (tDivI == 0) {
                tMuPar[0][i].fill(0.0);
                tSigmaPar[0][i].fill(1.0);
            } else {
                tMuPar[0][i].div2this(tDivI);
                tSigmaPar[0][i].div2this(tDivI);
                tMaxPar[0][i].minus2this(tMuPar[0][i]);
                tMinPar[0][i].minus2this(tMuPar[0][i]);
                tMinPar[0][i].abs2this();
                tMaxPar[0][i].operation().operate2this(tMinPar[0][i], Math::max);
                tSigmaPar[0][i].operation().operate2this(tMuPar[0][i], (lhs, rhs) -> lhs - rhs*rhs);
                tSigmaPar[0][i].operation().operate2this(tMaxPar[0][i], (v, max) -> {
                    v = MathEX.Code.numericEqual(v, 0.0) ? 1.0 : Math.sqrt(v);
                    return Math.max(v, max/mBasisMax);
                });
            }
        }
        for (int i = 0; i < tNumTypes; ++i) if ((tShareNorm && i!=0) || (!tShareNorm && (mNNAP.mBasis[i] instanceof MirrorBasis))) {
            int tNormIdx = tShareNorm ? 0 : (((MirrorBasis)mNNAP.mBasis[i]).mirrorType()-1);
            tMuPar[0][i].fill(tMuPar[0][tNormIdx]);
            tSigmaPar[0][i].fill(tSigmaPar[0][tNormIdx]);
        }
        // put to nnap
        for (int i = 0; i < tNumTypes; ++i) {
            mNNAP.normMu(i+1).fillD(tMuPar[0][i]);
            mNNAP.normSigma(i+1).fillD(tSigmaPar[0][i]);
        }
        // return caches
        for (int ti = 0; ti < tNumThreads; ++ti) {
            for (int i = 0; i < tNumTypes; ++i) {
                VectorCache.returnVec(tFpPar[ti][i]);
                VectorCache.returnVec(tMaxPar[ti][i]);
                VectorCache.returnVec(tMinPar[ti][i]);
                VectorCache.returnVec(tMuPar[ti][i]);
                VectorCache.returnVec(tSigmaPar[ti][i]);
            }
            IntVectorCache.returnVec(tDivPar[ti]);
        }
    }
    protected void initNormEng() {
        // 这里采用中位数和上下四分位数来归一化能量
        // 手动遍历拷贝来去除掉 nan
        final Vector.Builder rBuilder = Vector.builder();
        final int tDataSize = mTrainData.mSize;
        for (int i = 0; i < tDataSize; ++i) {
            double tEng = mTrainData.mEng.get(i);
            if (Double.isNaN(tEng)) continue;
            // 采用如下方式来应用 RefEng 和 RefEngScale，这个不等价但是原则上形式应该是任意的
            IntVector tAtomType = mTrainData.mAtomType.get(i);
            final int tNumAtoms = tAtomType.size();
            double tScaledNAtoms = 0.0;
            for (int k = 0; k < tNumAtoms; ++k) {
                int tType = tAtomType.get(k);
                tEng -= mRefEngs.get(tType-1);
                tScaledNAtoms += mRefEngScales.get(tType-1);
            }
            rBuilder.add(tEng/tScaledNAtoms);
        }
        Vector tSortedEng = rBuilder.build();
        if (tSortedEng.size() < 10) {
            UT.Code.warning("too less input energy ("+tSortedEng.size()+"), check your input or dataset.");
            return;
        }
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
        final int tNumTypes = ntypes();
        for (int type = 1; type <= tNumTypes; ++type) {
            mNNAP.setNormMuEng(type, mNormMuEng+mRefEngs.get(type-1));
            mNNAP.setNormSigmaEng(type, mNormSigmaEng*mRefEngScales.get(type-1));
        }
        mOptimizer.markLossFuncChanged();
    }
    
    protected void checkDataSet() {
        final int tNumTypes = ntypes();
        ILogicalVector tHasData = LogicalVectorCache.getZeros(tNumTypes);
        for (int i = 0; i < mTrainData.mSize; ++i) {
            IntVector tAtomType = mTrainData.mAtomType.get(i);
            int tAtomNum = tAtomType.size();
            for (int k = 0; k < tAtomNum; ++k) {
                // 这里不特地考虑 mirror，虽然 mirror 原则上可以没有，但是这也是一种不太合适的数据集，给出警告没有问题
                tHasData.set(tAtomType.get(k)-1, true);
            }
        }
        for (int i = 0; i < tNumTypes; ++i) {
            if (!tHasData.get(i)) {
                UT.Code.warning("number of atoms of type `"+symbol(i+1)+"` is zero, check your input or dataset.");
            }
        }
        LogicalVectorCache.returnVec(tHasData);
    }
    
    
    /**
     * 统计势函数的计算力的速度，会强制串行来保证结果有效性
     * <p>
     * 一般需要二次调用确保预热来得到正确的测量结果
     *
     * @param aTest 是否使用测试集进行速度统计，默认在有测试集时总是使用测试集
     * @param aMaxTimeSecond 统计的最长时间，单位为秒
     * @return 统计得到的平均每毫秒（ms）调用的原子力的次数
     */
    public double statSpeed(boolean aTest, double aMaxTimeSecond) {
        DataSet tData = aTest ? mTestData : mTrainData;
        final int tDataSize = tData.mSize;
        IntVector tSlice = Vectors.range(tDataSize);
        tSlice.shuffle();
        
        PointerManager tPtrMng = mNNAP.mPtrMngPar[0];
        IDoubleOrFloatCPointer rAGradNlDx = mAGradNlDxBuf[0];
        IDoubleOrFloatCPointer rAGradNlDy = mAGradNlDyBuf[0];
        IDoubleOrFloatCPointer rAGradNlDz = mAGradNlDzBuf[0];
        List<IntCPointer> tNl = mNlIdxBuf.get(0);
        List<IntCPointer> tNlType = mNlTypeBuf.get(0);
        List<IDoubleOrFloatCPointer> tNlDx = mNlDxBuf.get(0);
        List<IDoubleOrFloatCPointer> tNlDy = mNlDyBuf.get(0);
        List<IDoubleOrFloatCPointer> tNlDz = mNlDzBuf.get(0);
        
        AccumulatedTimer tTimer = new AccumulatedTimer();
        AccumulatedTimer tTimerTotal = new AccumulatedTimer();
        long tSteps = 0;
        while (tTimerTotal.get() < aMaxTimeSecond) for (int si = 0; si < tDataSize; ++si) {
            final int i = tSlice.get(si);
            IntVector tNlSize = tData.mNlSize.get(i);
            IntVector tAtomType = tData.mAtomType.get(i);
            final int tNumAtoms = tAtomType.size();
            
            tTimerTotal.from();
            // 通用方式获取近邻列表
            buildNl(aTest, i, 0, tNl, tNlType, tNlDx, tNlDy, tNlDz);
            
            tTimer.from();
            for (int k = 0; k < tNumAtoms; ++k) {
                int tSubNlSize = tNlSize.get(k);
                tPtrMng.ensureCapacity(rAGradNlDx, tSubNlSize);
                tPtrMng.ensureCapacity(rAGradNlDy, tSubNlSize);
                tPtrMng.ensureCapacity(rAGradNlDz, tSubNlSize);
                
                mNNAP.calEnergyForceSingle(
                    0, tAtomType.get(k), tSubNlSize,
                    tNlDx.get(k), tNlDy.get(k), tNlDz.get(k), tNlType.get(k),
                    rAGradNlDx, rAGradNlDy, rAGradNlDz
                );
            }
            tTimer.to();
            tTimerTotal.to();
            tSteps += tNumAtoms;
            if (tTimerTotal.get() >= aMaxTimeSecond) break;
        }
        return tSteps / (tTimer.get()*1000);
    }
    /**
     * 统计势函数的计算力的速度，会强制串行来保证结果有效性
     * <p>
     * 一般需要二次调用确保预热来得到正确的测量结果
     *
     * @param aMaxTimeSecond 统计的最长时间，单位为秒
     * @return 统计得到的平均每毫秒（ms）调用的原子力的次数
     */
    public double statSpeed(double aMaxTimeSecond) {
        return statSpeed(mHasTest, aMaxTimeSecond);
    }
    
    
    /** 获取历史 loss 值 */
    public IVector trainLoss() {return mTrainLoss.asVec();}
    public IVector trainLossE() {return mTrainLossE.asVec();}
    public IVector trainLossF() {return mTrainLossF.asVec();}
    public IVector trainLossS() {return mTrainLossS.asVec();}
    public IVector testLoss() {return mTestLoss.asVec();}
    public IVector testLossE() {return mTestLossE.asVec();}
    public IVector testLossF() {return mTestLossF.asVec();}
    public IVector testLossS() {return mTestLossS.asVec();}
    
    public TrainerNNAP setLossOut(String aPath) {
        mLossOutPath = aPath;
        mLossOutInit = false;
        return this;
    }
    protected void writeLoss() throws IOException {
        if (mLossOutPath == null) return;
        if (!mLossOutInit) {
            IO.write(mLossOutPath, "epoch,loss-train,loss-test,lossE-train,lossE-test,lossF-train,lossF-test,lossS-train,lossS-test");
            mLossOutInit = true;
        }
        String tLine = (mEpoch+1) +
            "," + mTrainLoss.last() +
            "," + mTestLoss.last() +
            "," + mTrainLossE.last() +
            "," + mTestLossE.last() +
            "," + mTrainLossF.last() +
            "," + mTestLossF.last() +
            "," + mTrainLossS.last() +
            "," + mTestLossS.last();
        IO.write(mLossOutPath, tLine, APPEND);
    }
    
    /** 开始训练模型，这里直接训练给定的步数 */
    public void train(int aNEpochs, boolean aEarlyStop, boolean aPrintLog) {
        mNEpochs = aNEpochs;
        // 清空旧的早停存储
        mMinLoss = Double.POSITIVE_INFINITY;
        // 数据近邻列表初始化
        final boolean tNewTrainData = !mTrainData.mDataTemp.isEmpty();
        final boolean tNewTestData = !mTestData.mDataTemp.isEmpty();
        if (tNewTrainData || tNewTestData) {
            if (aPrintLog) System.out.println("Init nl...");
            initDataNl();
            if (tNewTrainData) {
                initUnitLen();
                mOptimizer.markLossFuncChanged();
            }
        }
        // 初始化归一化参数，现在只会初始化一次
        if (!mNormInit) {
            if (aPrintLog) System.out.println("Init norm...");
            initNormEng();
            initNormBasis();
            mNormInit = true;
        }
        if (mFirstTrain) {
            mFirstTrain = false;
            if (!mIsRetrain) {
                // 这里独立检测输入是否合适
                checkDataSet();
            }
        }
        if (mBatchSize > 0) {
            // 统计 batch 情况
            mStepsPerEpoch = mTrainData.mSize/mBatchSize;
            // 初始化 batch 分割
            mAllSliceTrain = Vectors.range(mTrainData.mSize);
            mAllSliceTrain.shuffle();
            mSliceTrain = mAllSliceTrain.subVec(0, mStepsPerEpoch==1 ? mTrainData.mSize : mBatchSize);
        } else {
            mStepsPerEpoch = 1;
            mSliceTrain = mFullSliceTrain;
        }
        // 简单自动检测切换 cache forward
        if (mCacheForward == null) {
            long tMaxCacheSize = mTrainData.statMaxCacheSize(mNNAP, !mHasForce && !mHasStress);
            tMaxCacheSize *= (mSingle?Float.BYTES:Double.BYTES);
            mCacheForward = (tMaxCacheSize < DEFAULT_CACHE_LIMIT);
        }
        if (Conf.DEBUG) {
            long tTrainNlSize = mTrainData.statNlSize();
            double tNlMul = 3.0*(mSingle?Float.BYTES:Double.BYTES) + 2.0*Integer.BYTES;
            System.out.printf("train nl size: %d (%.3g GB)\n", tTrainNlSize, (tTrainNlSize/1024.0/1024.0/1024.0*tNlMul));
            if (mHasTest) {
                long tTestNlSize = mTestData.statNlSize();
                System.out.printf("test nl size: %d (%.3g GB)\n", tTestNlSize, (tTestNlSize/1024.0/1024.0/1024.0*tNlMul));
            }
            long tMaxCacheSize = mTrainData.statMaxCacheSize(mNNAP, !mHasForce && !mHasStress);
            System.out.printf("max cache size: %d (%.3g MB)\n", tMaxCacheSize, (tMaxCacheSize/1024.0/1024.0*(mSingle?Float.BYTES:Double.BYTES)));
        }
        // 开始训练
        if (aPrintLog) {
            if (mBatchSize > 0) {
                UT.Timer.progressBar(Maps.of(
                    "name", epochStr_(0),
                    "max", mStepsPerEpoch,
                    "length", 100
                ));
            } else {
                UT.Timer.progressBar(Maps.of(
                    "name", mIsRetrain ? "retrain" : "train",
                    "max", aNEpochs,
                    "length", mHasTest ? 100 : 80
                ));
            }
        }
        mOptimizer.run(aNEpochs*mStepsPerEpoch, aPrintLog);
        if (aPrintLog) {
            // 只会在不分 batch 时需要补全进度条
            if (mBatchSize <= 0) for (int i = mEpoch + 1; i < aNEpochs; ++i) {
                UT.Timer.progressBar(mHasTest ? String.format("loss: %.4g | %.4g", mTrainLoss.last(), mTestLoss.last()) : String.format("loss: %.4g", mTrainLoss.last()));
            }
        }
        // 应用早停
        if (aEarlyStop && mSelectEpoch>=0) {
            mNNAP.parameters().fill(mSelectParas);
            mNNAP.updateParameters();
            mOptimizer.markParameterChanged();
            if (aPrintLog) System.out.printf("Model at epoch = %d selected, test loss = %.4g\n", mSelectEpoch+1, mMinLoss);
            mSelectEpoch = -1;
            mMinLoss = Double.POSITIVE_INFINITY;
        }
        // 打印训练结果信息
        if (!aPrintLog) return;
        double tLossTot = calLossDetail(false, mLossDetail);
        double tLossE = mLossDetail.get(0);
        double tLossF = mLossDetail.get(1);
        double tLossS = mLossDetail.get(2);
        System.out.printf("Loss-E : %.4g (%s)\n", tLossE, IO.Text.percent(tLossE/tLossTot));
        if (mHasForce) System.out.printf("Loss-F : %.4g (%s)\n", tLossF, IO.Text.percent(tLossF/tLossTot));
        if (mHasStress) System.out.printf("Loss-S : %.4g (%s)\n", tLossS, IO.Text.percent(tLossS/tLossTot));
        calMAE(false, mLossDetail);
        double tMAE_E = mLossDetail.get(0);
        double tMAE_F = mLossDetail.get(1);
        double tMAE_S = mLossDetail.get(2);
        String tUnits = units();
        if (tUnits==null) tUnits = "";
        if (!mHasTest) {
            switch(tUnits) {
            case "metal": {
                System.out.printf("MAE-E: %.4g meV\n", tMAE_E*1000);
                if (mHasForce) System.out.printf("MAE-F: %.4g meV/A\n", tMAE_F*1000);
                if (mHasStress) System.out.printf("MAE-S: %.4g meV/A^3\n", tMAE_S*1000);
                break;
            }
            case "real":{
                System.out.printf("MAE-E: %.4g kcal/mol\n", tMAE_E);
                if (mHasForce) System.out.printf("MAE-F: %.4g kcal/mol/A\n", tMAE_F);
                if (mHasStress) System.out.printf("MAE-S: %.4g kcal/mol/A^3\n", tMAE_S);
                break;
            }
            default: {
                System.out.printf("MAE-E: %.4g\n", tMAE_E);
                if (mHasForce) System.out.printf("MAE-F: %.4g\n", tMAE_F);
                if (mHasStress) System.out.printf("MAE-S: %.4g\n", tMAE_S);
                break;
            }}
        } else {
            Vector tTestMAE = VectorCache.getVec(3);
            calMAE(true, tTestMAE);
            double tTestMAE_E = tTestMAE.get(0);
            double tTestMAE_F = tTestMAE.get(1);
            double tTestMAE_S = tTestMAE.get(2);
            VectorCache.returnVec(tTestMAE);
            switch(tUnits) {
            case "metal": {
                System.out.printf("MAE-E: %.4g meV/atom | %.4g meV/atom\n", tMAE_E*1000, tTestMAE_E*1000);
                if (mHasForce) System.out.printf("MAE-F: %.4g meV/A | %.4g meV/A\n", tMAE_F*1000, tTestMAE_F*1000);
                if (mHasStress) System.out.printf("MAE-S: %.4g meV/A^3 | %.4g meV/A^3\n", tMAE_S*1000, tTestMAE_S*1000);
                break;
            }
            case "real":{
                System.out.printf("MAE-E: %.4g kcal/mol/atom | %.4g kcal/mol/atom\n", tMAE_E, tTestMAE_E);
                if (mHasForce) System.out.printf("MAE-F: %.4g kcal/mol/A | %.4g kcal/mol/A\n", tMAE_F, tTestMAE_F);
                if (mHasStress) System.out.printf("MAE-S: %.4g kcal/mol/A^3 | %.4g kcal/mol/A^3\n", tMAE_S, tTestMAE_S);
                break;
            }
            default: {
                System.out.printf("MAE-E: %.4g | %.4g\n", tMAE_E, tTestMAE_E);
                if (mHasForce) System.out.printf("MAE-F: %.4g | %.4g\n", tMAE_F, tTestMAE_F);
                if (mHasStress) System.out.printf("MAE-S: %.4g | %.4g\n", tMAE_S, tTestMAE_S);
                break;
            }}
        }
        // 打印参数数目信息
        System.out.printf("N-Paras: %,d\n", mNNAP.parameters().size());
        // 测试速度并打印速度信息
        statSpeed(1.0); // 预热 1 s
        double tSpeed = statSpeed(2.0);
        System.out.printf("Speed: %.4g atom-steps/ms\n", tSpeed);
    }
    public void train(int aEpochs, boolean aEarlyStop) {
        train(aEpochs, aEarlyStop, true);
    }
    public void train(int aEpochs) {
        train(aEpochs, true);
    }
    
    
    /** 保存训练的势函数 */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override public void save(Map rSaveTo) {
        final int tNumTypes = ntypes();
        List rModels = new ArrayList();
        for (int i = 0; i < tNumTypes; ++i) {
            Map rBasis = new LinkedHashMap();
            mNNAP.mBasis[i].save(rBasis);
            Map rModel = new LinkedHashMap();
            rModel.put("symbol", symbol(i+1));
            rModel.put("basis", rBasis);
            if (i == 0) {
                rModel.put("norm_mu_eng", mNormMuEng);
                rModel.put("norm_sigma_eng", mNormSigmaEng);
            }
            if (mNNAP.mBasis[i] instanceof MirrorBasis) {
                rModels.add(rModel);
                continue;
            }
            Map rNN = new LinkedHashMap();
            mNNAP.mNN[i].save(rNN);
            rModel.put("ref_eng", mRefEngs.get(i));
            rModel.put("ref_eng_scale", mRefEngScales.get(i));
            rModel.put("norm_mu", toList_(mNNAP.normMu(i+1), mNNAP.mBasis[i].size()));
            rModel.put("norm_sigma", toList_(mNNAP.normSigma(i+1), mNNAP.mBasis[i].size()));
            rModel.put("nn", rNN);
            rModels.add(rModel);
        }
        rSaveTo.put("version", NNAP.VERSION);
        String tUnits = units();
        if (tUnits != null) {
            rSaveTo.put("units", tUnits);
        }
        rSaveTo.put("models", rModels);
    }
    @SuppressWarnings({"rawtypes"})
    public void save(String aPath, boolean aPretty) throws IOException {
        Map rJson = new LinkedHashMap();
        save(rJson);
        IO.map2json(rJson, aPath, aPretty);
    }
    public void save(String aPath) throws IOException {save(aPath, false);}
    
    
    private static List<Double> toList_(IDoubleOrFloatCPointer aPtr, int aCount) {
        List<Double> rList = new ArrayList<>(aCount);
        for (int i = 0; i < aCount; ++i) {
            rList.add(aPtr.getAtD(i));
        }
        return rList;
    }
}
