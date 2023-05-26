package com.jtool.lmp;

import com.jtool.math.table.Table;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Collection;

@VisibleForTesting
public final class Log extends Thermo {
    public Log(Table... aTables) {super(aTables);}
    public Log(Collection<Table> aTables) {super(aTables);}
}
