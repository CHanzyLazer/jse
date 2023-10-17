package jtool.system;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

import static jtool.code.CS.SUC_FUTURE;

/**
 * @author liqa
 * <p> 执行 mpi 并行程序的执行器，会使用 mpiexec 来执行程序，并且强制设置资源分配器为 user 来绕开 slurm 的资源分配 </p>
 */
public class MPISystemExecutor extends LocalSystemExecutor {
    private final int mProcessNum;
    public MPISystemExecutor(int aProcessNum) {
        super();
        mProcessNum = aProcessNum;
    }
    
    @Override protected Future<Integer> submitSystem__(String aCommand, @NotNull IPrintlnSupplier aPrintln) {
        // 对于空指令专门优化，不执行操作
        if (aCommand == null || aCommand.isEmpty()) return SUC_FUTURE;
        
        return super.submitSystem__("mpiexec -np " + mProcessNum + " " + aCommand, aPrintln);
    }
}
