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
public class AtomData extends AbstractAtomData {
    private final @Unmodifiable List<IAtom> mAtoms;
    private final IXYZ mBoxLo, mBoxHi;
    private final int mAtomTypeNum;
    
    public AtomData(List<IAtom> aAtoms, int aAtomTypeNum, IXYZ aBoxLo, IXYZ aBoxHi) {
        mAtoms = aAtoms;
        mBoxLo = aBoxLo;
        mBoxHi = aBoxHi;
        mAtomTypeNum = aAtomTypeNum;
    }
    public AtomData(List<IAtom> aAtoms, int aAtomTypeNum, IXYZ aBoxHi) {this(aAtoms, aAtomTypeNum, BOX_ZERO, aBoxHi);}
    public AtomData(List<IAtom> aAtoms, int aAtomTypeNum             ) {this(aAtoms, aAtomTypeNum, BOX_ONE);}
    public AtomData(List<IAtom> aAtoms, IXYZ aBoxLo     , IXYZ aBoxHi) {this(aAtoms, 1, aBoxLo, aBoxHi);}
    public AtomData(List<IAtom> aAtoms,                   IXYZ aBoxHi) {this(aAtoms, 1, aBoxHi);}
    public AtomData(List<IAtom> aAtoms                               ) {this(aAtoms, 1);}
    
    
    @Override public final List<IAtom> atoms() {return mAtoms;}
    @Override public final IXYZ boxLo() {return mBoxLo;}
    @Override public final IXYZ boxHi() {return mBoxHi;}
    @Override public final int atomNum() {return mAtoms.size();}
    @Override public final int atomTypeNum() {return mAtomTypeNum;}
}
