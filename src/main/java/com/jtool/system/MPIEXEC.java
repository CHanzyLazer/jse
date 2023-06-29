package com.jtool.system;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

@VisibleForTesting
@ApiStatus.Obsolete
public final class MPIEXEC extends MPISystemExecutor {
    public MPIEXEC(int aProcessNum) {super(aProcessNum);}
}
