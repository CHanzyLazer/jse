package com.jtool.system;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

@VisibleForTesting
@ApiStatus.ScheduledForRemoval
@Deprecated
public final class SRUN extends SRUNSystemExecutor {
    public SRUN(int aTaskNum) throws Exception {super(aTaskNum);}
}
