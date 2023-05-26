package com.jtool.math.table;

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
    List<Double> get(String aHand);
    boolean containsHand(String aHand);
    @SuppressWarnings("UnusedReturnValue")
    boolean setHand(String aOldHand, String aNewHand);
}
