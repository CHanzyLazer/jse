package jse.atom;

import jse.code.collection.ISlice;
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
    
    
    /** 默认计算器实现不需要支持计算某一部分原子的能量，直接抛出 {@link UnsupportedOperationException} */
    @Override public double calEnergyAt(ISlice aIndices) {
        throw new UnsupportedOperationException();
    }
    @Override public double calEnergyDiffSwap(int aI, int aJ, boolean aRestoreData) throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        ISettableAtom tAtomI = aAtomData.atom(aI);
        ISettableAtom tAtomJ = aAtomData.atom(aJ);
        int oTypeI = tAtomI.type();
        int oTypeJ = tAtomJ.type();
        if (oTypeI == oTypeJ) return 0.0;
        double oEng = calEnergy();
        tAtomI.setType(oTypeJ);
        tAtomJ.setType(oTypeI);
        double nEng = calEnergy();
        if (aRestoreData) {
            tAtomI.setType(oTypeI);
            tAtomJ.setType(oTypeJ);
        }
        return nEng - oEng;
    }
    @Override public double calEnergyDiffFlip(int aI, int aType, boolean aRestoreData) throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        ISettableAtom tAtom = aAtomData.atom(aI);
        int oType = tAtom.type();
        if (oType == aType) return 0.0;
        double oEng = calEnergy(aAtomData);
        tAtom.setType(aType);
        double nEng = calEnergy(aAtomData);
        if (aRestoreData) {
            tAtom.setType(oType);
        }
        return nEng - oEng;
    }
}
