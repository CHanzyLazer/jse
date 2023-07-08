package com.jtool.system;

import org.jetbrains.annotations.NotNull;

/**
 * @author liqa
 * <p> 执行 mpi 并行程序的执行器，会使用 mpiexec 来执行程序，并且强制设置资源分配器为 user 来绕开 slurm 的资源分配 </p>
 */
public class MPISystemExecutor extends LocalSystemExecutor {
    private final int mProcessNum;
    public MPISystemExecutor(int aProcessNum) {
        // 未来不会限制并行数，因此这里不再提供并行的设置
        super(SERIAL_EXECUTOR);
        mProcessNum = aProcessNum;
    }
    
    @Override protected int system_(String aCommand, @NotNull IPrintlnSupplier aPrintln) {
        // 对于空指令专门优化，不执行操作
        if (aCommand == null || aCommand.isEmpty()) return -1;
        
        return super.system_("mpiexec -np " + mProcessNum + " " + aCommand, aPrintln);
    }
}
