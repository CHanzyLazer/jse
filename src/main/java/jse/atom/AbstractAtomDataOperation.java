package jse.atom;

import jse.code.collection.AbstractCollections;
import jse.code.collection.ISlice;
import jse.code.collection.NewCollections;
import jse.code.functional.IFilter;
import jse.code.functional.IIndexFilter;
import jse.code.functional.IUnaryFullOperator;
import jse.code.iterator.IIntIterator;
import jse.math.vector.IIntVector;
import jse.math.vector.IVector;
import jse.math.vector.IntVector;

import java.util.*;


/**
 * 一般的运算器的实现，默认会值拷贝一次并使用 {@code ArrayList<IAtom>} 来存储，尽管这会占据更多的内存
 * @author liqa
 */
public abstract class AbstractAtomDataOperation implements IAtomDataOperation {
    
    @Override public ISettableAtomData filter(IFilter<IAtom> aFilter) {
        IAtomData tThis = thisAtomData_();
        List<IAtom> tFilterAtoms = NewCollections.filter(tThis.atoms(), aFilter);
        ISettableAtomData rAtomData = newSettableAtomData_(tFilterAtoms.size());
        for (int i = 0; i < tFilterAtoms.size(); ++i) rAtomData.setAtom(i, tFilterAtoms.get(i));
        return rAtomData;
    }
    @Override public ISettableAtomData filterType(final int aType) {return filter(atom -> atom.type()==aType);}
    
    @Override public IAtomData refSlice(ISlice aIndices) {return refAtomData_(AbstractCollections.slice(thisAtomData_().atoms(), aIndices));}
    @Override public IAtomData refSlice(List<Integer> aIndices) {return refAtomData_(AbstractCollections.slice(thisAtomData_().atoms(), aIndices));}
    @Override public IAtomData refSlice(int[] aIndices) {return refAtomData_(AbstractCollections.slice(thisAtomData_().atoms(), aIndices));}
    @Override public IAtomData refSlice(IIndexFilter aIndices) {return refAtomData_(AbstractCollections.slice(thisAtomData_().atoms(), aIndices));}
    
    
    @Override public ISettableAtomData map(int aMinTypeNum, IUnaryFullOperator<? extends IAtom, ? super IAtom> aOperator) {
        final IAtomData tThis = thisAtomData_();
        final int tAtomNum = tThis.atomNumber();
        ISettableAtomData rAtomData = newSettableAtomData_(tAtomNum);
        for (int i = 0; i < tAtomNum; ++i) {
            // 保存修改后的原子，现在内部会自动更新种类计数
            rAtomData.setAtom(i, aOperator.apply(tThis.atom(i)));
        }
        // 这里不进行 try 包含，因为目前这里的实例都是支持的，并且手动指定了 aMinTypeNum 后才会调用，此时设置失败会希望抛出错误
        if (rAtomData.atomTypeNumber() < aMinTypeNum) rAtomData.setAtomTypeNumber(aMinTypeNum);
        return rAtomData;
    }
    
    
    @Override public ISettableAtomData mapType(int aMinTypeNum, IUnaryFullOperator<Integer, ? super IAtom> aOperator) {
        final IAtomData tThis = thisAtomData_();
        final int tAtomNum = tThis.atomNumber();
        ISettableAtomData rAtomData = newSameSettableAtomData_();
        for (int i = 0; i < tAtomNum; ++i) {
            // 保存修改后的原子，现在内部会自动更新种类计数
            rAtomData.atom(i).setType(aOperator.apply(tThis.atom(i)));
        }
        // 这里不进行 try 包含，因为目前这里的实例都是支持的，并且手动指定了 aMinTypeNum 后才会调用，此时设置失败会希望抛出错误
        if (rAtomData.atomTypeNumber() < aMinTypeNum) rAtomData.setAtomTypeNumber(aMinTypeNum);
        return rAtomData;
    }
    
    @Override public ISettableAtomData mapTypeRandom(Random aRandom, IVector aTypeWeights) {
        double tTotWeight = aTypeWeights.sum();
        if (tTotWeight <= 0.0) throw new RuntimeException("TypeWeights Must be Positive");
        
        int tAtomNum = thisAtomData_().atomNumber();
        int tMaxType = aTypeWeights.size();
        // 获得对应原子种类的 List
        final IntVector.Builder tBuilder = IntVector.builder(tAtomNum+tMaxType);
        for (int tType = 1; tType <= tMaxType; ++tType) {
            // 计算这种种类的粒子数目
            long tSteps = Math.round((aTypeWeights.get(tType-1) / tTotWeight) * tAtomNum);
            for (int i = 0; i < tSteps; ++i) tBuilder.add(tType);
        }
        // 简单处理，如果数量不够则添加最后一种种类
        while (tBuilder.size() < tAtomNum) tBuilder.add(tMaxType);
        IIntVector tTypeList = tBuilder.build();
        // 随机打乱这些种类标记
        tTypeList.shuffle(aRandom);
        final IIntIterator it = tTypeList.iterator();
        // 使用 mapType 获取种类修改后的 AtomData
        return mapType(tMaxType, atom -> it.next());
    }
    
    @Override public ISettableAtomData perturbXYZGaussian(Random aRandom, double aSigma) {
        final IAtomData tThis = thisAtomData_();
        final int tAtomNum = tThis.atomNumber();
        ISettableAtomData rAtomData = newSameSettableAtomData_();
        for (int i = 0; i < tAtomNum; ++i) {
            IAtom oAtom = tThis.atom(i);
            rAtomData.atom(i).setXYZ(
                oAtom.x() + aRandom.nextGaussian()*aSigma,
                oAtom.y() + aRandom.nextGaussian()*aSigma,
                oAtom.z() + aRandom.nextGaussian()*aSigma
            );
        }
        // 注意周期边界条件的处理
        rAtomData.operation().wrapPBC2this();
        return rAtomData;
    }
    
    @Override public ISettableAtomData wrapPBC() {
        final IAtomData tThis = thisAtomData_();
        final int tAtomNum = tThis.atomNumber();
        ISettableAtomData rAtomData = newSameSettableAtomData_();
        if (tThis.isPrism()) {
            // 斜方情况需要转为 Direct 再 wrap，
            // 完事后再转回 Cartesian
            final IBox tBox = tThis.box();
            for (int i = 0; i < tAtomNum; ++i) {
                XYZ tDirect = tBox.toDirect(tThis.atom(i));
                if      (tDirect.mX <  0.0) {++tDirect.mX; while (tDirect.mX <  0.0) ++tDirect.mX;}
                else if (tDirect.mX >= 1.0) {--tDirect.mX; while (tDirect.mX >= 1.0) --tDirect.mX;}
                if      (tDirect.mY <  0.0) {++tDirect.mY; while (tDirect.mY <  0.0) ++tDirect.mY;}
                else if (tDirect.mY >= 1.0) {--tDirect.mY; while (tDirect.mY >= 1.0) --tDirect.mY;}
                if      (tDirect.mZ <  0.0) {++tDirect.mZ; while (tDirect.mZ <  0.0) ++tDirect.mZ;}
                else if (tDirect.mZ >= 1.0) {--tDirect.mZ; while (tDirect.mZ >= 1.0) --tDirect.mZ;}
                XYZ tCartesian = tBox.toCartesian(tDirect);
                rAtomData.atom(i).setXYZ(tCartesian);
            }
        } else {
            final XYZ tBox = XYZ.toXYZ(tThis.box());
            for (int i = 0; i < tAtomNum; ++i) {
                IAtom oAtom = tThis.atom(i);
                double tX = oAtom.x();
                double tY = oAtom.y();
                double tZ = oAtom.z();
                if      (tX <  0.0    ) {tX += tBox.mX; while (tX <  0.0    ) tX += tBox.mX;}
                else if (tX >= tBox.mX) {tX -= tBox.mX; while (tX >= tBox.mX) tX -= tBox.mX;}
                if      (tY <  0.0    ) {tY += tBox.mY; while (tY <  0.0    ) tY += tBox.mY;}
                else if (tY >= tBox.mY) {tY -= tBox.mY; while (tY >= tBox.mY) tY -= tBox.mY;}
                if      (tZ <  0.0    ) {tZ += tBox.mZ; while (tZ <  0.0    ) tZ += tBox.mZ;}
                else if (tZ >= tBox.mZ) {tZ -= tBox.mZ; while (tZ >= tBox.mZ) tZ -= tBox.mZ;}
                rAtomData.atom(i).setXYZ(tX, tY, tZ);
            }
        }
        return rAtomData;
    }
    
    
    /** 用于方便内部使用 */
    private IAtomData refAtomData_(List<? extends IAtom> aAtoms) {
        IAtomData tThis = thisAtomData_();
        return new AtomData(aAtoms, tThis.atomTypeNumber(), tThis.box(), tThis.hasVelocities());
    }
    
    /** stuff to override */
    protected abstract IAtomData thisAtomData_();
    protected abstract ISettableAtomData newSameSettableAtomData_();
    protected abstract ISettableAtomData newSettableAtomData_(int aAtomNum);
}
