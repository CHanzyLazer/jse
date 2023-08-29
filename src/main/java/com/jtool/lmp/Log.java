package com.jtool.lmp;

import com.jtool.math.table.ITable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;

@VisibleForTesting
public final class Log extends Thermo {
    public Log(ITable... aTableList) {super(aTableList);}
    public Log(List<ITable> aTableList) {super(aTableList);}
}
