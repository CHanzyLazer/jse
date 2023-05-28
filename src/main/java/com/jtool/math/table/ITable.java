package com.jtool.math.table;

import com.jtool.math.vector.IVector;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Collection;
import java.util.List;

/**
 * 通用的列表类，仅实现列表的额外功能
 * @author liqa
 */
public interface ITable {
    boolean noHand();
    Collection<String> hands();
    
    /** Map like stuffs */
    IVector get(String aHand);
    boolean containsHand(String aHand);
    @SuppressWarnings("UnusedReturnValue")
    boolean setHand(String aOldHand, String aNewHand);
    
    /** Groovy 的部分，重载一些运算符方便操作 */
    @VisibleForTesting default IVector getAt(String aHand) {return get(aHand);}
    @VisibleForTesting default void putAt(String aOldHand, String aNewHand) {setHand(aOldHand, aNewHand);}
}
