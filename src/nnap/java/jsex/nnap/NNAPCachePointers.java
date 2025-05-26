package jsex.nnap;

import jse.clib.DoubleCPointer;
import jse.clib.GrowableDoubleCPointer;
import jse.clib.GrowableIntCPointer;
import jse.code.ReferenceChecker;

/**
 * 用来自动回收 {@link NNAP} 内部缓存的 c 指针，这里采取的策略是每次创建之前清理旧的数据
 * @see <a href="http://www.oracle.com/technetwork/articles/java/finalization-137655.htm">
 * How to Handle Java Finalization's Memory-Retention Issues </a>
 * @author liqa
 */
class NNAPCachePointers extends ReferenceChecker {
    private final int mThreadNum, mBatchSize;
    
    final DoubleCPointer[][] mFp;
    final GrowableDoubleCPointer[][] mFpPx, mFpPy, mFpPz;
    final GrowableDoubleCPointer[][] mNlDx, mNlDy, mNlDz;
    final GrowableIntCPointer[][] mNlType, mNlIdx;
    
    NNAPCachePointers(NNAP.SingleNNAP aNNAP, int aThreadNum, int aBatchSize, int aBasisSize) {
        super(aNNAP);
        mThreadNum = aThreadNum;
        mBatchSize = aBatchSize;
        
        mFp = new DoubleCPointer[mThreadNum][mBatchSize];
        mFpPx = new GrowableDoubleCPointer[mThreadNum][mBatchSize];
        mFpPy = new GrowableDoubleCPointer[mThreadNum][mBatchSize];
        mFpPz = new GrowableDoubleCPointer[mThreadNum][mBatchSize];
        mNlDx = new GrowableDoubleCPointer[mThreadNum][mBatchSize];
        mNlDy = new GrowableDoubleCPointer[mThreadNum][mBatchSize];
        mNlDz = new GrowableDoubleCPointer[mThreadNum][mBatchSize];
        mNlType = new GrowableIntCPointer[mThreadNum][mBatchSize];
        mNlIdx = new GrowableIntCPointer[mThreadNum][mBatchSize];
        for (int i = 0; i < mThreadNum; ++i) for (int j = 0; j < mBatchSize; ++j) {
            mFp[i][j] = DoubleCPointer.malloc(aBasisSize);
            mFpPx[i][j] = new GrowableDoubleCPointer(1024);
            mFpPy[i][j] = new GrowableDoubleCPointer(1024);
            mFpPz[i][j] = new GrowableDoubleCPointer(1024);
            mNlDx[i][j] = new GrowableDoubleCPointer(16);
            mNlDy[i][j] = new GrowableDoubleCPointer(16);
            mNlDz[i][j] = new GrowableDoubleCPointer(16);
            mNlType[i][j] = new GrowableIntCPointer(16);
            mNlIdx[i][j] = new GrowableIntCPointer(16);
        }
    }
    
    @Override protected void dispose_() {
        for (int i = 0; i < mThreadNum; ++i) for (int j = 0; j < mBatchSize; ++j) {
            mFp[i][j].free();
            mFpPx[i][j].free();
            mFpPy[i][j].free();
            mFpPz[i][j].free();
            mNlDx[i][j].free();
            mNlDy[i][j].free();
            mNlDz[i][j].free();
            mNlType[i][j].free();
            mNlIdx[i][j].free();
        }
    }
}
