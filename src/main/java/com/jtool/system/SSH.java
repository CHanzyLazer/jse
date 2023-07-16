package com.jtool.system;

import org.jetbrains.annotations.VisibleForTesting;

import java.util.Map;

@VisibleForTesting
public final class SSH extends SSHSystemExecutor {
    public SSH(Map<?, ?> aArgs) throws Exception {super(aArgs);}
    public SSH(int aIOThreadNum, Map<?, ?> aArgs) throws Exception {super(aIOThreadNum, aArgs);}
}
