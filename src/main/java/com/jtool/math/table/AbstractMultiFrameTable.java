package com.jtool.math.table;

import com.jtool.math.vector.IVector;

import java.util.AbstractList;
import java.util.Collection;

/**
 * @author liqa
 * <p> 抽象的拥有多个帧的列表的类，方便子类实现接口 </p>
 * <p> 注意这里每一帧的 Table 都是完全独立的，继承 ITable 只是为了方便只有一帧的情况直接使用 </p>
 */
public abstract class AbstractMultiFrameTable<T extends ITable> extends AbstractList<T> implements ITable {
    /** ITable stuffs */
    @Override public boolean noHand() {return defaultFrame().noHand();}
    @Override public Collection<String> hands() {return defaultFrame().hands();}
    @Override public IVector get(String aHand) {return defaultFrame().get(aHand);}
    @Override public boolean containsHand(String aHand) {return defaultFrame().containsHand(aHand);}
    @Override public boolean setHand(String aOldHand, String aNewHand) {return defaultFrame().setHand(aOldHand, aNewHand);}
    
    /** stuff to override */
    public T defaultFrame() {return get(0);}
    
    /** AbstractList stuffs */
    public abstract T get(int index);
    public abstract int size();
}
