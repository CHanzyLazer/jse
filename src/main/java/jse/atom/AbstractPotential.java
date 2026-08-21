package jse.atom;

import jep.python.PyObject;
import jse.ase.AseAtoms;
import jse.code.collection.DoubleList;
import jse.math.matrix.RowMatrix;
import jse.math.vector.Vector;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

public abstract class AbstractPotential implements IPotential {
    protected int mNumAtoms = -1;
    protected double mVolume = Double.NaN;
    @Override public final boolean dataValid() {
        return mNumAtoms >= 0;
    }
    
    protected double mEnergy = Double.NaN;
    protected double mStressXX = Double.NaN, mStressYY = Double.NaN, mStressZZ = Double.NaN;
    protected double mStressXY = Double.NaN, mStressXZ = Double.NaN, mStressYZ = Double.NaN;
    protected final Vector mEnergies = new Vector(0, null);
    protected final Vector mForcesX = new Vector(0, null), mForcesY = new Vector(0, null), mForcesZ = new Vector(0, null);
    protected final Vector mStressesXX = new Vector(0, null), mStressesYY = new Vector(0, null), mStressesZZ = new Vector(0, null);
    protected final Vector mStressesXY = new Vector(0, null), mStressesXZ = new Vector(0, null), mStressesYZ = new Vector(0, null);
    protected final Vector mStressesYX = new Vector(0, null), mStressesZX = new Vector(0, null), mStressesZY = new Vector(0, null);
    private final DoubleList mEnergiesRaw = new DoubleList();
    private final DoubleList mForcesRaw = new DoubleList();
    private final DoubleList mStressesRaw = new DoubleList();
    
    protected boolean mTotalEnergyValid = false, mPerAtomEnergyValid = false;
    protected boolean mTotalStressValid = false, mPerAtomStressValid = false;
    protected boolean mForceValid = false;
    @Override public final boolean totalEnergyValid() {
        return mTotalEnergyValid;
    }
    @Override public final boolean perAtomEnergyValid() {
        return mPerAtomEnergyValid;
    }
    @Override public final boolean totalStressValid() {
        return mTotalStressValid;
    }
    @Override public final boolean perAtomStressValid() {
        return mPerAtomStressValid;
    }
    @Override public final boolean forceValid() {
        return mForceValid;
    }
    
    @Override public AbstractPotential setData(IAtomData aData) throws Exception {
        mNumAtoms = aData.natoms();
        mVolume = aData.volume();
        mTotalEnergyValid = false;
        mPerAtomEnergyValid = false;
        mTotalStressValid = false;
        mPerAtomStressValid = false;
        mForceValid = false;
        // 目前约定 setData 后统一初始化为 0
        mEnergy = 0.0;
        mStressXX = mStressYY = mStressZZ = mStressXY = mStressXZ = mStressYZ = 0.0;
        mForcesRaw.clear();
        mForcesRaw.addZeros(mNumAtoms*3);
        double[] tData = mForcesRaw.internalData();
        int tShift = 0;
        mForcesX.setInternalData(mNumAtoms, tShift, tData); tShift += mNumAtoms;
        mForcesY.setInternalData(mNumAtoms, tShift, tData); tShift += mNumAtoms;
        mForcesZ.setInternalData(mNumAtoms, tShift, tData);
        if (perAtomEnergySupport()) {
            mEnergiesRaw.clear();
            mEnergiesRaw.addZeros(mNumAtoms);
            mEnergies.setInternalData(mEnergiesRaw);
        }
        if (perAtomStressSupport()) {
            boolean tCentroid = centroidPerAtomStressSupport();
            mStressesRaw.clear();
            mStressesRaw.addZeros(mNumAtoms*(tCentroid?9:6));
            tData = mStressesRaw.internalData();
            tShift = 0;
            mStressesXX.setInternalData(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            mStressesYY.setInternalData(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            mStressesZZ.setInternalData(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            mStressesXY.setInternalData(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            mStressesXZ.setInternalData(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            mStressesYZ.setInternalData(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            if (tCentroid) {
                mStressesYX.setInternalData(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                mStressesZX.setInternalData(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                mStressesZY.setInternalData(mNumAtoms, tShift, tData);
            }
        }
        return this;
    }
    
    private final DoubleList mForcesAse = new DoubleList();
    private final DoubleList mStressAse = new DoubleList(), mStressesAse = new DoubleList();
    @ApiStatus.Internal
    @Override public final Map<String, Object> calculateAse_(Map<String, Object> rResults, PyObject aPyAseAtoms, String[] aProperties, boolean aSystemChanges) throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        boolean tAllInResults = true;
        for (String tProperty : aProperties) {
            if (!rResults.containsKey(tProperty)) {
                tAllInResults = false;
                break;
            }
        }
        if (!aSystemChanges && tAllInResults) return rResults;
        setData(AseAtoms.of(aPyAseAtoms, true));
        // 遍历统计需要的量
        boolean tRequireTotalEnergy = false, tRequirePreAtomEnergy = false;
        boolean tRequireForces = false;
        boolean tRequireTotalStress = false, tRequirePreAtomStress = false;
        for (String tProperty : aProperties) {
            if (tProperty.equals("energy")) tRequireTotalEnergy = true;
            if (tProperty.equals("energies")) tRequirePreAtomEnergy = true;
            if (tProperty.equals("forces")) tRequireForces = true;
            if (tProperty.equals("stress")) tRequireTotalStress = true;
            if (tProperty.equals("stresses")) tRequirePreAtomStress = true;
        }
        // 执行计算并获取结果
        calculate(tRequireTotalEnergy, tRequirePreAtomEnergy, tRequireForces, tRequireTotalStress, tRequirePreAtomStress);
        if (tRequireTotalEnergy) {
            rResults.put("energy", mEnergy);
        }
        if (tRequirePreAtomEnergy) {
            rResults.put("energies", mEnergies.numpy());
        }
        if (tRequireForces) {
            mForcesAse.ensureCapacity(mNumAtoms*3);
            RowMatrix rForces = new RowMatrix(mNumAtoms, 3, mForcesAse.internalData());
            for (int i = 0; i < mNumAtoms; ++i) {
                rForces.set(i, 0, mForcesX.get(i));
                rForces.set(i, 1, mForcesY.get(i));
                rForces.set(i, 2, mForcesZ.get(i));
            }
            rResults.put("forces", rForces.numpy());
        }
        if (tRequireTotalStress) {
            mStressAse.ensureCapacity(6);
            Vector rStress = new Vector(6, mStressAse.internalData());
            rStress.set(0, mStressXX);
            rStress.set(1, mStressYY);
            rStress.set(2, mStressZZ);
            rStress.set(3, mStressYZ); // 注意 ase 的 stress 顺序问题
            rStress.set(4, mStressXZ);
            rStress.set(5, mStressXY);
            rResults.put("stress", rStress.numpy());
        }
        if (tRequirePreAtomStress) {
            mStressesAse.ensureCapacity(mNumAtoms*6);
            RowMatrix rStresses = new RowMatrix(mNumAtoms, 6, mStressesAse.internalData());
            for (int i = 0; i < mNumAtoms; ++i) {
                rStresses.set(i, 0, mStressesXX.get(i));
                rStresses.set(i, 1, mStressesYY.get(i));
                rStresses.set(i, 2, mStressesZZ.get(i));
                rStresses.set(i, 3, mStressesYZ.get(i)); // 注意 ase 的 stress 顺序问题
                rStresses.set(i, 4, mStressesXZ.get(i));
                rStresses.set(i, 5, mStressesXY.get(i));
            }
            rResults.put("stresses", rStresses.numpy());
        }
        return rResults;
    }
    
    @Override public final double energy() {
        if (!mTotalEnergyValid) throw new IllegalStateException("total energy invalid");
        return mEnergy;
    }
    @Override public final Vector energies() {
        if (!mPerAtomEnergyValid) throw new IllegalStateException("per-atom energy invalid");
        return mEnergies;
    }
    
    @Override public final Vector forcesX() {
        if (!mForceValid) throw new IllegalStateException("force invalid");
        return mForcesX;
    }
    @Override public final Vector forcesY() {
        if (!mForceValid) throw new IllegalStateException("force invalid");
        return mForcesY;
    }
    @Override public final Vector forcesZ() {
        if (!mForceValid) throw new IllegalStateException("force invalid");
        return mForcesZ;
    }
    
    @Override public final double stressXX() {
        if (!mTotalStressValid) throw new IllegalStateException("total stress invalid");
        return mStressXX;
    }
    @Override public final double stressYY() {
        if (!mTotalStressValid) throw new IllegalStateException("total stress invalid");
        return mStressYY;
    }
    @Override public final double stressZZ() {
        if (!mTotalStressValid) throw new IllegalStateException("total stress invalid");
        return mStressZZ;
    }
    @Override public final double stressXY() {
        if (!mTotalStressValid) throw new IllegalStateException("total stress invalid");
        return mStressXY;
    }
    @Override public final double stressXZ() {
        if (!mTotalStressValid) throw new IllegalStateException("total stress invalid");
        return mStressXZ;
    }
    @Override public final double stressYZ() {
        if (!mTotalStressValid) throw new IllegalStateException("total stress invalid");
        return mStressYZ;
    }
    
    @Override public final Vector stressesXX() {
        if (!mPerAtomStressValid) throw new IllegalStateException("per-atom stress invalid");
        return mStressesXX;
    }
    @Override public final Vector stressesYY() {
        if (!mPerAtomStressValid) throw new IllegalStateException("per-atom stress invalid");
        return mStressesYY;
    }
    @Override public final Vector stressesZZ() {
        if (!mPerAtomStressValid) throw new IllegalStateException("per-atom stress invalid");
        return mStressesZZ;
    }
    @Override public final Vector stressesXY() {
        if (!mPerAtomStressValid) throw new IllegalStateException("per-atom stress invalid");
        return mStressesXY;
    }
    @Override public final Vector stressesXZ() {
        if (!mPerAtomStressValid) throw new IllegalStateException("per-atom stress invalid");
        return mStressesXZ;
    }
    @Override public final Vector stressesYZ() {
        if (!mPerAtomStressValid) throw new IllegalStateException("per-atom stress invalid");
        return mStressesYZ;
    }
    @Override public final Vector stressesYX() {
        if (!mPerAtomStressValid) throw new IllegalStateException("per-atom stress invalid");
        if (!centroidPerAtomStressSupport()) throw new UnsupportedOperationException("stressesYX for no centroid stresses support");
        return mStressesYX;
    }
    @Override public final Vector stressesZX() {
        if (!mPerAtomStressValid) throw new IllegalStateException("per-atom stress invalid");
        if (!centroidPerAtomStressSupport()) throw new UnsupportedOperationException("stressesZX for no centroid stresses support");
        return mStressesZX;
    }
    @Override public final Vector stressesZY() {
        if (!mPerAtomStressValid) throw new IllegalStateException("per-atom stress invalid");
        if (!centroidPerAtomStressSupport()) throw new UnsupportedOperationException("stressesZY for no centroid stresses support");
        return mStressesZY;
    }
}
