package com.jtool.lmp;

import com.jtool.math.table.ITable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Collection;

@VisibleForTesting
public final class Log extends Thermo {
    public Log(ITable... aTables) {super(aTables);}
    public Log(Collection<? extends ITable> aTables) {super(aTables);}
}
