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
    
    protected double mEnergy = Double.NaN;
    protected double mStressXX = Double.NaN, mStressYY = Double.NaN, mStressZZ = Double.NaN;
    protected double mStressXY = Double.NaN, mStressXZ = Double.NaN, mStressYZ = Double.NaN;
    protected Vector mEnergies = null;
    protected Vector mForcesX = null, mForcesY = null, mForcesZ = null;
    protected Vector mStressesXX = null, mStressesYY = null, mStressesZZ = null;
    protected Vector mStressesXY = null, mStressesXZ = null, mStressesYZ = null;
    protected Vector mStressesYX = null, mStressesZX = null, mStressesZY = null;
    private final DoubleList mEnergiesRaw = new DoubleList();
    private final DoubleList mForcesRaw = new DoubleList();
    private final DoubleList mStressesRaw = new DoubleList();
    
    @Override public AbstractPotential setData(IAtomData aData) throws Exception {
        mNumAtoms = aData.natoms();
        mVolume = aData.volume();
        // 目前约定 setData 后统一初始化为 0
        mEnergy = 0.0;
        mStressXX = mStressYY = mStressZZ = mStressXY = mStressXZ = mStressYZ = 0.0;
        mForcesRaw.clear();
        mForcesRaw.addZeros(mNumAtoms*3);
        double[] tData = mForcesRaw.internalData();
        int tShift = 0;
        mForcesX = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
        mForcesY = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
        mForcesZ = new Vector(mNumAtoms, tShift, tData);
        if (perAtomEnergySupport()) {
            mEnergiesRaw.clear();
            mEnergiesRaw.addZeros(mNumAtoms);
            mEnergies = mEnergiesRaw.asVec();
        }
        if (perAtomStressSupport()) {
            mStressesRaw.clear();
            boolean tCentroid = centroidPerAtomStressSupport();
            mStressesRaw.addZeros(mNumAtoms*(tCentroid?9:6));
            tData = mStressesRaw.internalData();
            tShift = 0;
            mStressesXX = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            mStressesYY = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            mStressesZZ = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            mStressesXY = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            mStressesXZ = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            mStressesYZ = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
            if (tCentroid) {
                mStressesYX = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                mStressesZX = new Vector(mNumAtoms, tShift, tData); tShift += mNumAtoms;
                mStressesZY = new Vector(mNumAtoms, tShift, tData);
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
        return mEnergy;
    }
    @Override public final Vector energies() {
        if (!perAtomEnergySupport()) return null;
        return mEnergies;
    }
    @Override public final Vector forcesX() {return mForcesX;}
    @Override public final Vector forcesY() {return mForcesY;}
    @Override public final Vector forcesZ() {return mForcesZ;}
    
    @Override public final double stressXX() {return mStressXX;}
    @Override public final double stressYY() {return mStressYY;}
    @Override public final double stressZZ() {return mStressZZ;}
    @Override public final double stressXY() {return mStressXY;}
    @Override public final double stressXZ() {return mStressXZ;}
    @Override public final double stressYZ() {return mStressYZ;}
    
    @Override public final Vector stressesXX() {return mStressesXX;}
    @Override public final Vector stressesYY() {return mStressesYY;}
    @Override public final Vector stressesZZ() {return mStressesZZ;}
    @Override public final Vector stressesXY() {return mStressesXY;}
    @Override public final Vector stressesXZ() {return mStressesXZ;}
    @Override public final Vector stressesYZ() {return mStressesYZ;}
    @Override public final Vector stressesYX() {return mStressesYX;}
    @Override public final Vector stressesZX() {return mStressesZX;}
    @Override public final Vector stressesZY() {return mStressesZY;}
}
