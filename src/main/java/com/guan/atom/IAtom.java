package com.guan.atom;

public interface IAtom extends IHasXYZ {
    double[] xyz();
    int type();
    int id();
    
    boolean hasType();
    boolean hasID();
}
