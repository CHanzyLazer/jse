package jse.atom;

import jse.parallel.ParforThreadPool;
import org.jetbrains.annotations.ApiStatus;

public abstract class AbstractPairPotential implements IPairPotential {
    protected final NeighborListGetter mNL;
    protected final ParforThreadPool mPool;
    protected AbstractPairPotential(int aNumThreads) {
        mPool = new ParforThreadPool(aNumThreads);
        mNL = new NeighborListGetter();
    }
    
    private boolean mDead = false;
    @Override public void close() throws Exception {
        if (mDead) return;
        mDead = true;
        mPool.close();
    }
    public boolean isClosed() {
        return mDead;
    }
    @Override public final int nthreads() {
        return mPool.nthreads();
    }
    
    @Override @ApiStatus.Internal
    public final ParforThreadPool pool_() {
        return mPool;
    }
    @Override @ApiStatus.Internal
    public final NeighborListGetter nl_() {
        return mNL;
    }
}
