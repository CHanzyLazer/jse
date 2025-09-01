package jsex.nnap.basis;

import jse.code.Conf;
import jse.code.collection.DoubleList;
import jse.code.collection.IntList;
import jse.code.collection.NewCollections;
import jse.math.vector.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static jse.code.CS.*;

/**
 * 使用多个基组的合并基组，用于实现自定义的高效基组
 * @author liqa
 */
public class Merge extends Basis {
    
    private final MergeableBasis[] mMergeBasis;
    private final double mRCut;
    private final int mSize, mTotParaSize, mTypeNum;
    private final @Nullable IVector[] mParas;
    private final int[] mParaSizes;
    private final String @Nullable[] mSymbols;
    private final ShiftVector[] mFpShell, mNNGradShell, mParaShell;
    private final ShiftIntVector[] mFpNlSizeShell, mFpGradNlIndexShell, mFpGradFpIndexShell;
    private final ShiftVector[] mFpPxShell, mFpPyShell, mFpPzShell;
    
    public Merge(MergeableBasis... aMergeBasis) {
        if (aMergeBasis==null || aMergeBasis.length==0) throw new IllegalArgumentException("Merge basis can not be null or empty");
        double tRCut = Double.NEGATIVE_INFINITY;
        int tSize = 0;
        int tTypeNum = -1;
        @Nullable List<String> tSymbols = null;
        Boolean tHasSymbols = null;
        for (Basis tBasis : aMergeBasis) {
            if (!(tBasis instanceof SphericalChebyshev) && !(tBasis instanceof Chebyshev)) {
                throw new IllegalArgumentException("MergeBasis should be SphericalChebyshev or Chebyshev");
            }
            tRCut = Math.max(tBasis.rcut(), tRCut);
            tSize += tBasis.size();
            if (tTypeNum < 0) {
                tTypeNum = tBasis.atomTypeNumber();
            } else {
                if (tTypeNum != tBasis.atomTypeNumber()) throw new IllegalArgumentException("atom type number mismatch");
            }
            if (tHasSymbols == null) {
                tHasSymbols = tBasis.hasSymbol();
                if (tHasSymbols) {
                    tSymbols = tBasis.symbols();
                    if (tSymbols == null) throw new NullPointerException();
                }
            } else {
                if (tHasSymbols != tBasis.hasSymbol()) throw new IllegalArgumentException("symbols mismatch");
                List<String> tSymbols_ = tBasis.symbols();
                if (tHasSymbols) {
                    if (!tSymbols.equals(tSymbols_)) throw new IllegalArgumentException("symbols mismatch");
                } else {
                    if (tSymbols_ != null) throw new IllegalArgumentException("symbols mismatch");
                }
            }
            tTypeNum = Math.min(tBasis.atomTypeNumber(), tTypeNum);
        }
        mMergeBasis = aMergeBasis;
        mRCut = tRCut;
        mSize = tSize;
        mTypeNum = tTypeNum;
        mSymbols = tSymbols==null ? null : tSymbols.toArray(ZL_STR);
        // init para stuff
        mParas = new IVector[mMergeBasis.length];
        mParaSizes = new int[mMergeBasis.length];
        mParaShell = new ShiftVector[mMergeBasis.length];
        int tTotParaSize = 0;
        for (int i = 0; i < mMergeBasis.length; ++i) {
            IVector tPara = mMergeBasis[i].hasParameters() ? mMergeBasis[i].parameters() : null;
            mParas[i] = tPara;
            int tSizePara = tPara==null ? 0 : tPara.size();
            mParaSizes[i] = tSizePara;
            mParaShell[i] = new ShiftVector(tSizePara, 0, null);
            tTotParaSize += tSizePara;
        }
        mTotParaSize = tTotParaSize;
        // init fp shell
        mFpShell = new ShiftVector[mMergeBasis.length];
        mNNGradShell = new ShiftVector[mMergeBasis.length];
        mFpNlSizeShell = new ShiftIntVector[mMergeBasis.length];
        mFpGradNlIndexShell = new ShiftIntVector[mMergeBasis.length];
        mFpGradFpIndexShell = new ShiftIntVector[mMergeBasis.length];
        mFpPxShell = new ShiftVector[mMergeBasis.length];
        mFpPyShell = new ShiftVector[mMergeBasis.length];
        mFpPzShell = new ShiftVector[mMergeBasis.length];
        for (int i = 0; i < mMergeBasis.length; ++i) {
            int tSizeFp = mMergeBasis[i].size();
            mFpShell[i] = new ShiftVector(tSizeFp, 0, null);
            mNNGradShell[i] = new ShiftVector(tSizeFp, 0, null);
            mFpNlSizeShell[i] = new ShiftIntVector(tSizeFp, 0, null);
            mFpGradNlIndexShell[i]  = new ShiftIntVector(0, 0, null);
            mFpGradFpIndexShell[i]  = new ShiftIntVector(0, 0, null);
            mFpPxShell[i] = new ShiftVector(0, 0, null);
            mFpPyShell[i] = new ShiftVector(0, 0, null);
            mFpPzShell[i] = new ShiftVector(0, 0, null);
        }
    }
    @Override public Merge threadSafeRef() {
        MergeableBasis[] rBasis = new MergeableBasis[mMergeBasis.length];
        for (int i = 0; i < mMergeBasis.length; ++i) {
            rBasis[i] = mMergeBasis[i].threadSafeRef();
        }
        return new Merge(rBasis);
    }
    @Override public void initParameters() {
        for (Basis tBasis : mMergeBasis) tBasis.initParameters();
    }
    @Override public IVector parameters() {
        return new RefVector() {
            @Override public double get(int aIdx) {
                int tIdx = aIdx;
                for (int i = 0; i < mMergeBasis.length; ++i) {
                    int tParaSize = mParaSizes[i];
                    if (tIdx < tParaSize) {
                        IVector tPara = mParas[i];
                        assert tPara != null;
                        return tPara.get(tIdx);
                    }
                    tIdx -= tParaSize;
                }
                throw new IndexOutOfBoundsException(String.valueOf(aIdx));
            }
            @Override public void set(int aIdx, double aValue) {
                int tIdx = aIdx;
                for (int i = 0; i < mMergeBasis.length; ++i) {
                    int tParaSize = mParaSizes[i];
                    if (tIdx < tParaSize) {
                        IVector tPara = mParas[i];
                        assert tPara != null;
                        tPara.set(tIdx, aValue);
                        return;
                    }
                    tIdx -= tParaSize;
                }
                throw new IndexOutOfBoundsException(String.valueOf(aIdx));
            }
            @Override public int size() {
                return mTotParaSize;
            }
        };
    }
    @Override public boolean hasParameters() {
        for (Basis tBasis : mMergeBasis) {
            if (tBasis.hasParameters()) return true;
        }
        return false;
    }
    
    @Override public double rcut() {return mRCut;}
    @Override public int size() {return mSize;}
    @Override public int atomTypeNumber() {return mTypeNum;}
    @Override public boolean hasSymbol() {return mSymbols != null;}
    @Override public String symbol(int aType) {return mSymbols==null ? null : mSymbols[aType-1];}
    
    @Override protected void shutdown_() {
        for (Basis tBasis : mMergeBasis) {
            tBasis.shutdown();
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override public void save(Map rSaveTo) {
        rSaveTo.put("type", "merge");
        List<Map> tMergeBasis = NewCollections.from(mMergeBasis.length, i -> new LinkedHashMap<>());
        for (int i = 0; i < mMergeBasis.length; ++i) {
            mMergeBasis[i].save(tMergeBasis.get(i));
        }
        rSaveTo.put("basis", tMergeBasis);
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Merge load(String @NotNull[] aSymbols, Map aMap) {
        Object tObj = aMap.get("basis");
        if (tObj == null) throw new IllegalArgumentException("Key `basis` required for merge load");
        List<Map> tList = (List<Map>)tObj;
        MergeableBasis[] tMergeBasis = new MergeableBasis[tList.size()];
        for (int i = 0; i < tMergeBasis.length; ++i) {
            Map tMap = tList.get(i);
            Object tType = tMap.get("type");
            if (tType == null) {
                tType = "spherical_chebyshev";
            }
            switch(tType.toString()) {
            case "spherical_chebyshev": {
                tMergeBasis[i] = SphericalChebyshev.load(aSymbols, tMap);
                break;
            }
            case "chebyshev": {
                tMergeBasis[i] = Chebyshev.load(aSymbols, tMap);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported basis type: " + tType);
            }}
        }
        return new Merge(tMergeBasis);
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Merge load(int aTypeNum, Map aMap) {
        Object tObj = aMap.get("basis");
        if (tObj == null) throw new IllegalArgumentException("Key `basis` required for merge load");
        List<Map> tList = (List<Map>)tObj;
        MergeableBasis[] tMergeBasis = new MergeableBasis[tList.size()];
        for (int i = 0; i < tMergeBasis.length; ++i) {
            Map tMap = tList.get(i);
            Object tType = tMap.get("type");
            if (tType == null) {
                tType = "spherical_chebyshev";
            }
            switch(tType.toString()) {
            case "spherical_chebyshev": {
                tMergeBasis[i] = SphericalChebyshev.load(aTypeNum, tMap);
                break;
            }
            case "chebyshev": {
                tMergeBasis[i] = Chebyshev.load(aTypeNum, tMap);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported basis type: " + tType);
            }}
        }
        return new Merge(tMergeBasis);
    }
    
    @Override
    protected void eval_(DoubleList aNlDx, DoubleList aNlDy, DoubleList aNlDz, IntList aNlType, DoubleArrayVector rFp, @Nullable IntArrayVector rFpGradNlSize, boolean aBufferNl) {
        if (isShutdown()) throw new IllegalStateException("This Basis is dead");
        if (Conf.OPERATION_CHECK) {
            if (mSize != rFp.size()) throw new IllegalArgumentException("data size mismatch");
        } else {
            if (mSize > rFp.size()) throw new IllegalArgumentException("data size mismatch");
        }
        int tFpShift0 = rFp.internalDataShift();
        int tFpShift = 0;
        for (int i = 0; i < mMergeBasis.length; ++i) {
            ShiftVector tFp = mFpShell[i];
            tFp.setInternalData(rFp.internalData()); tFp.setInternalDataShift(tFpShift0+tFpShift);
            ShiftIntVector tFpNlSize = rFpGradNlSize ==null ? null : mFpNlSizeShell[i];
            if (tFpNlSize != null) {
                tFpNlSize.setInternalData(rFpGradNlSize.internalData());
                tFpNlSize.setInternalDataShift(tFpShift); // 这里输入的 rFpGradNlSize 没有 shift
            }
            mMergeBasis[i].eval_(aNlDx, aNlDy, aNlDz, aNlType, tFp, tFpNlSize, aBufferNl);
            tFpShift += tFp.internalDataSize();
        }
    }
    @Override @ApiStatus.Internal
    public void backward(DoubleList aNlDx, DoubleList aNlDy, DoubleList aNlDz, IntList aNlType, DoubleArrayVector aGradFp, DoubleArrayVector rGradPara) {
        if (isShutdown()) throw new IllegalStateException("This Basis is dead");
        if (Conf.OPERATION_CHECK) {
            if (mSize != aGradFp.size()) throw new IllegalArgumentException("data size mismatch");
            if (mTotParaSize != rGradPara.size()) throw new IllegalArgumentException("data size mismatch");
        } else {
            if (mSize > aGradFp.size()) throw new IllegalArgumentException("data size mismatch");
            if (mTotParaSize > rGradPara.size()) throw new IllegalArgumentException("data size mismatch");
        }
        int tFpShift = aGradFp.internalDataShift(), tParaShift = rGradPara.internalDataShift();
        for (int i = 0; i < mMergeBasis.length; ++i) {
            ShiftVector tGradFp = mFpShell[i];
            tGradFp.setInternalData(aGradFp.internalData()); tGradFp.setInternalDataShift(tFpShift);
            ShiftVector tGradPara = mParaShell[i];
            tGradPara.setInternalData(rGradPara.internalData()); tGradPara.setInternalDataShift(tParaShift);
            mMergeBasis[i].backward(aNlDx, aNlDy, aNlDz, aNlType, tGradFp, tGradPara);
            tFpShift += tGradFp.internalDataSize();
            tParaShift += tGradPara.internalDataSize();
        }
    }
    @Override
    protected void evalGrad_(DoubleList aNlDx, DoubleList aNlDy, DoubleList aNlDz, IntList aNlType, IntArrayVector aFpGradNlSize, IntArrayVector rFpGradNlIndex, IntArrayVector rFpGradFpIndex, DoubleArrayVector rFpPx, DoubleArrayVector rFpPy, DoubleArrayVector rFpPz) {
        if (isShutdown()) throw new IllegalStateException("This Basis is dead");
        int tFpShift = 0, tNlShift = 0; // 这里输入都是没有 shift
        for (int i = 0; i < mMergeBasis.length; ++i) {
            ShiftIntVector tFpNlSize = mFpNlSizeShell[i];
            tFpNlSize.setInternalData(aFpGradNlSize.internalData()); tFpNlSize.setInternalDataShift(tFpShift);
            int tSubSizeAll = tFpNlSize.sum();
            ShiftIntVector tFpGradNlIndex = mFpGradNlIndexShell[i];
            ShiftIntVector tFpGradFpIndex = mFpGradFpIndexShell[i];
            tFpGradNlIndex.setInternalData(rFpGradNlIndex.internalData()); tFpGradNlIndex.setInternalDataShift(tNlShift); tFpGradNlIndex.setInternalDataSize(tSubSizeAll);
            tFpGradFpIndex.setInternalData(rFpGradFpIndex.internalData()); tFpGradFpIndex.setInternalDataShift(tNlShift); tFpGradFpIndex.setInternalDataSize(tSubSizeAll);
            ShiftVector tFpPx = mFpPxShell[i];
            ShiftVector tFpPy = mFpPyShell[i];
            ShiftVector tFpPz = mFpPzShell[i];
            tFpPx.setInternalData(rFpPx.internalData()); tFpPx.setInternalDataShift(tNlShift); tFpPx.setInternalDataSize(tSubSizeAll);
            tFpPy.setInternalData(rFpPy.internalData()); tFpPy.setInternalDataShift(tNlShift); tFpPy.setInternalDataSize(tSubSizeAll);
            tFpPz.setInternalData(rFpPz.internalData()); tFpPz.setInternalDataShift(tNlShift); tFpPz.setInternalDataSize(tSubSizeAll);
            mMergeBasis[i].evalGrad_(aNlDx, aNlDy, aNlDz, aNlType, tFpNlSize, tFpGradNlIndex, tFpGradFpIndex, tFpPx, tFpPy, tFpPz);
            tFpGradFpIndex.plus2this(tFpNlSize.internalDataShift());
            tNlShift += tSubSizeAll;
            tFpShift += tFpNlSize.internalDataSize();
        }
    }
    @Override
    protected void evalForce_(DoubleList aNlDx, DoubleList aNlDy, DoubleList aNlDz, IntList aNlType, DoubleArrayVector aNNGrad, DoubleList rFx, DoubleList rFy, DoubleList rFz) {
        if (isShutdown()) throw new IllegalStateException("This Basis is dead");
        // 这里需要手动清空旧值
        MergeableBasis.clearForce_(rFx, rFy, rFz);
        int tFpShift = aNNGrad.internalDataShift();
        for (int i = 0; i < mMergeBasis.length; ++i) {
            ShiftVector tNNGrad = mNNGradShell[i];
            tNNGrad.setInternalData(aNNGrad.internalData()); tNNGrad.setInternalDataShift(tFpShift);
            mMergeBasis[i].evalForceAccumulate_(aNlDx, aNlDy, aNlDz, aNlType, tNNGrad, rFx, rFy, rFz);
            tFpShift += tNNGrad.internalDataSize();
        }
    }
    @Override @Deprecated
    protected void evalGrad_(DoubleList aNlDx, DoubleList aNlDy, DoubleList aNlDz, IntList aNlType, DoubleList rFpPx, DoubleList rFpPy, DoubleList rFpPz) {
        if (isShutdown()) throw new IllegalStateException("This Basis is dead");
        int tFpShift = 0;
        for (int i = 0; i < mMergeBasis.length; ++i) {
            int tFpSize = mFpShell[i].internalDataSize();
            mMergeBasis[i].evalGradWithShift_(aNlDx, aNlDy, aNlDz, aNlType, tFpShift, mSize-tFpShift-tFpSize, rFpPx, rFpPy, rFpPz);
            tFpShift += tFpSize;
        }
    }
}
