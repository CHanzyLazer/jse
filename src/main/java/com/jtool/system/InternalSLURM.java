package com.jtool.system;

import org.jetbrains.annotations.VisibleForTesting;

@VisibleForTesting
public final class InternalSLURM extends InternalSLURMSystemExecutor {
    public InternalSLURM(int aTaskNum) throws Exception {super(aTaskNum);}
    public InternalSLURM(int aTaskNum, boolean aInternalThreadPool) throws Exception {super(aTaskNum, aInternalThreadPool);}
}
