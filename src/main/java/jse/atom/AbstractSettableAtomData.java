package jse.atom;

import jse.code.collection.AbstractRandomAccessList;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;

/**
 * @author liqa
 * <p> 抽象的拥有原子数据的类，方便子类实现接口 </p>
 */
public abstract class AbstractSettableAtomData extends AbstractAtomData implements ISettableAtomData {
    /** stuff to override */
    public abstract ISettableAtom atom(int aIdx);
    public abstract AbstractSettableAtomData setAtomTypeNumber(int aAtomTypeNum);
    
    
    @Override @Deprecated @SuppressWarnings("deprecation") public List<? extends ISettableAtom> asList() {return atoms();}
    @Override public void setAtom(int aIdx, IAtom aAtom) {
        ISettableAtom tAtom = this.atom(aIdx);
        tAtom.setXYZ(aAtom).setID(aAtom.id()).setType(aAtom.type());
        if (aAtom.hasVelocities()) tAtom.setVxyz(aAtom.vx(), aAtom.vy(), aAtom.vz());
    }
    
    @Override public List<? extends ISettableAtom> atoms() {
        return new AbstractRandomAccessList<ISettableAtom>() {
            @Override public ISettableAtom get(int index) {return AbstractSettableAtomData.this.atom(index);}
            @Override public ISettableAtom set(final int index, ISettableAtom element) {
                ISettableAtom oAtom = AbstractSettableAtomData.this.atom(index).copy();
                setAtom(index, element);
                return oAtom;
            }
            @Override public int size() {return atomNumber();}
        };
    }
    
    @Override public ISettableAtomDataOperation operation() {return new AbstractSettableAtomDataOperation() {
        @Override protected ISettableAtomData thisAtomData_() {return AbstractSettableAtomData.this;}
        @Override protected ISettableAtomData newSameSettableAtomData_() {return newSame_();}
        @Override protected ISettableAtomData newSettableAtomData_(int aAtomNum) {return newZeros_(aAtomNum);}
        @Override protected ISettableAtomData newSettableAtomData_(int aAtomNum, IBox aBox) {return newZeros_(aAtomNum, aBox);}
    };}
    @VisibleForTesting @Override public ISettableAtomDataOperation opt() {return operation();}
}
