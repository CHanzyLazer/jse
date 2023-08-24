package com.jtool.atom;

import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import static com.jtool.code.CS.BOX_ONE;
import static com.jtool.code.CS.BOX_ZERO;


/**
 * @author liqa
 * <p> 内部使用的通用的原子数据格式，直接使用 {@code List<IAtom>} 来存储数据 </p>
 * <p> 主要用于避免意义不大的匿名类的使用，并且也能减少意料之外的引用 </p>
 * <p> 这里所有的输入会直接作为成员，不进行值拷贝 </p>
 */
public final class AtomData extends AbstractAtomData {
    private final @Unmodifiable List<IAtom> mAtoms;
    private final IXYZ mBoxLo, mBoxHi;
    private final int mAtomTypeNum;
    private final boolean mHasVelocities;
    
    public AtomData(List<IAtom> aAtoms, int aAtomTypeNum, IXYZ aBoxLo, IXYZ aBoxHi, boolean aHasVelocities) {
        mAtoms = aAtoms;
        mBoxLo = aBoxLo;
        mBoxHi = aBoxHi;
        mAtomTypeNum = aAtomTypeNum;
        mHasVelocities = aHasVelocities;
    }
    public AtomData(List<IAtom> aAtoms, int aAtomTypeNum,              IXYZ aBoxHi, boolean aHasVelocities) {this(aAtoms, aAtomTypeNum, BOX_ZERO, aBoxHi, aHasVelocities);}
    public AtomData(List<IAtom> aAtoms, int aAtomTypeNum,                           boolean aHasVelocities) {this(aAtoms, aAtomTypeNum, BOX_ONE, aHasVelocities);}
    public AtomData(List<IAtom> aAtoms,                   IXYZ aBoxLo, IXYZ aBoxHi, boolean aHasVelocities) {this(aAtoms, 1, aBoxLo, aBoxHi, aHasVelocities);}
    public AtomData(List<IAtom> aAtoms,                                IXYZ aBoxHi, boolean aHasVelocities) {this(aAtoms, 1, aBoxHi, aHasVelocities);}
    public AtomData(List<IAtom> aAtoms,                                             boolean aHasVelocities) {this(aAtoms, 1, aHasVelocities);}
    public AtomData(List<IAtom> aAtoms, int aAtomTypeNum, IXYZ aBoxLo, IXYZ aBoxHi                        ) {this(aAtoms, aAtomTypeNum, aBoxLo, aBoxHi, false);}
    public AtomData(List<IAtom> aAtoms, int aAtomTypeNum,              IXYZ aBoxHi                        ) {this(aAtoms, aAtomTypeNum, aBoxHi, false);}
    public AtomData(List<IAtom> aAtoms, int aAtomTypeNum                                                  ) {this(aAtoms, aAtomTypeNum, false);}
    public AtomData(List<IAtom> aAtoms,                   IXYZ aBoxLo, IXYZ aBoxHi                        ) {this(aAtoms, aBoxLo, aBoxHi, false);}
    public AtomData(List<IAtom> aAtoms,                                IXYZ aBoxHi                        ) {this(aAtoms, aBoxHi, false);}
    public AtomData(List<IAtom> aAtoms                                                                    ) {this(aAtoms, false);}
    
    
    @Override public List<IAtom> atoms() {return mAtoms;}
    @Override public IXYZ boxLo() {return mBoxLo;}
    @Override public IXYZ boxHi() {return mBoxHi;}
    @Override public int atomNum() {return mAtoms.size();}
    @Override public int atomTypeNum() {return mAtomTypeNum;}
    @Override public boolean hasVelocities() {return mHasVelocities;}
}
