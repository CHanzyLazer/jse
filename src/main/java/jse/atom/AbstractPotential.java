package jse.atom;

import com.google.common.collect.Lists;
import jep.python.PyObject;
import jse.ase.AseAtoms;
import jse.cache.MatrixCache;
import jse.cache.VectorCache;
import jse.math.matrix.RowMatrix;
import jse.math.vector.Vector;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

public abstract class AbstractPotential implements IPotential {
    protected int mNumAtoms = -1;
    protected double mVolume = Double.NaN;
    
    @Override public AbstractPotential setData(IAtomData aData) throws Exception {
        mNumAtoms = aData.natoms();
        mVolume = aData.volume();
        return this;
    }
    @ApiStatus.Internal
    @Override public Map<String, Object> calculate_(Map<String, Object> rResults, PyObject aPyAseAtoms, String[] aProperties, boolean aSystemChanges) throws Exception {
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
        boolean tRequireEnergy = false, tRequirePreAtomEnergy = false;
        boolean tRequireForces = false;
        boolean tRequireStress = false, tRequirePreAtomStress = false;
        for (String tProperty : aProperties) {
            if (tProperty.equals("energy") || tProperty.equals("energies")) tRequireEnergy = true;
            if (tProperty.equals("forces")) tRequireForces = true;
            if (tProperty.equals("stress") || tProperty.equals("stresses")) tRequireStress = true;
            if (tProperty.equals("energies")) tRequirePreAtomEnergy = true;
            if (tProperty.equals("stresses")) tRequirePreAtomStress = true;
        }
        // 只需要能量则直接使用简单的计算能量接口
        if (!tRequireForces && !tRequireStress) {
            if (!tRequireEnergy) return rResults;
            if (!tRequirePreAtomEnergy) {
                double tEnergy = calEnergy();
                rResults.put("energy", tEnergy);
                return rResults;
            }
            Vector tEnergies = calEnergies();
            double tEnergy = tEnergies.sum();
            rResults.put("energy", tEnergy);
            rResults.put("energies", tEnergies.numpy());
            VectorCache.returnVec(tEnergies);
            return rResults;
        }
        // 其余情况则统一全部计算
        Vector rEnergies = VectorCache.getZeros(tRequirePreAtomEnergy?mNumAtoms:1);
        RowMatrix rForces = MatrixCache.getZerosRow(mNumAtoms, 3);
        RowMatrix rStresses = MatrixCache.getZerosRow(tRequirePreAtomStress?mNumAtoms:1, 6);
        calEnergyForceVirials(rEnergies, rForces.col(0), rForces.col(1), rForces.col(2),
                              rStresses.col(0), rStresses.col(1), rStresses.col(2), rStresses.col(5), rStresses.col(4), rStresses.col(3));
        rStresses.operation().negative2this();
        Vector rStress = VectorCache.getZeros(6);
        for (int i = 0; i < 6; ++i) {
            rStress.set(i, rStresses.col(i).sum());
        }
        rStress.div2this(mVolume);
        double tEnergy = rEnergies.sum();
        rResults.put("energy", tEnergy);
        if (tRequirePreAtomEnergy) {
            rResults.put("energies", rEnergies.numpy());
        }
        rResults.put("forces", rForces.numpy());
        rResults.put("stress", rStress.numpy());
        if (tRequirePreAtomStress) {
            rResults.put("stresses", rStresses.numpy());
        }
        VectorCache.returnVec(rEnergies);
        MatrixCache.returnMat(rForces);
        VectorCache.returnVec(rStress);
        MatrixCache.returnMat(rStresses);
        return rResults;
    }
    
    @Override public Vector calEnergies() throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        Vector rEnergies = VectorCache.getVec(mNumAtoms);
        calEnergyForceVirials(rEnergies, null, null, null, null, null, null, null, null, null);
        return rEnergies;
    }
    @Override public double calEnergy() throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        Vector rTotEng = VectorCache.getVec(1);
        calEnergyForceVirials(rTotEng, null, null, null, null, null, null, null, null, null);
        double tTotEng = rTotEng.get(0);
        VectorCache.returnVec(rTotEng);
        return tTotEng;
    }
    @Override public RowMatrix calForces() throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        RowMatrix rForces = MatrixCache.getMatRow(mNumAtoms, 3);
        calEnergyForceVirials(null, rForces.col(0), rForces.col(1), rForces.col(2), null, null, null, null, null, null);
        return rForces;
    }
    @Override public List<Vector> calStresses() throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        final boolean tCentroid = centroidPerAtomStressSupport();
        final int tColNum = tCentroid ? 9 : 6;
        List<Vector> rStresses = VectorCache.getVec(mNumAtoms, tColNum);
        calEnergyForceVirials(null, null, null, null, rStresses.get(0), rStresses.get(1), rStresses.get(2), rStresses.get(3), rStresses.get(4), rStresses.get(5),
                              tCentroid?rStresses.get(6):null, tCentroid?rStresses.get(7):null, tCentroid?rStresses.get(8):null);
        for (int i = 0; i < tColNum; ++i) {
            rStresses.get(i).operation().negative2this();
        }
        return rStresses;
        // 由于存在单位转换问题，这里不再计算原本错误处理的速度部分。需要则需要使用 CS.VOLE_TO_EV 手转换和计算（metal）
    }
    @Override public List<Double> calStress() throws Exception {
        if (isClosed()) throw new IllegalStateException("This Potential is dead");
        List<Vector> rStresses = VectorCache.getVec(1, 6);
        calEnergyForceVirials(null, null, null, null, rStresses.get(0), rStresses.get(1), rStresses.get(2), rStresses.get(3), rStresses.get(4), rStresses.get(5));
        double rStressXX = -rStresses.get(0).get(0);
        double rStressYY = -rStresses.get(1).get(0);
        double rStressZZ = -rStresses.get(2).get(0);
        double rStressXY = -rStresses.get(3).get(0);
        double rStressXZ = -rStresses.get(4).get(0);
        double rStressYZ = -rStresses.get(5).get(0);
        VectorCache.returnVec(rStresses);
        return Lists.newArrayList(rStressXX/mVolume, rStressYY/mVolume, rStressZZ/mVolume, rStressXY/mVolume, rStressXZ/mVolume, rStressYZ/mVolume);
        // 由于存在单位转换问题，这里不再计算原本错误处理的速度部分。需要则需要使用 CS.VOLE_TO_EV 手转换和计算（metal）
    }
}
