package com.jtool.system;

import com.jtool.code.UT;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jtool.code.CS.Slurm.*;
import static com.jtool.code.CS.WORKING_DIR;

/**
 * @author liqa
 * <p> 在 SLURM 内部提交子任务的提交器，主要用于 salloc 或者 sbatch 一个 jTool 任务后，
 * 在提交的 jTool 任务中提交子任务；因此认为此时已经有了 SLURM 的任务环境 </p>
 * <p> 由于是提交子任务的形式，这里依旧使用 java 线程池来提交后台任务 </p>
 */
@ApiStatus.Obsolete
public class InternalSLURMSystemExecutor extends LocalSystemExecutor {
    private final String mWorkingDir;
    
    private final int mTaskNum;
    private final int mNcmdsPerNode;
    private final Map<String, Integer> mJobsPerNode;
    
    /** 线程数现在由每个任务的并行数，申请到的节点数，以及每节点的核心数来确定 */
    public InternalSLURMSystemExecutor(int aTaskNum, boolean aInternalThreadPool) throws Exception {
        // 先随便设置一下线程池，因为要先构造父类
        super();
        // 设置一下工作目录
        mWorkingDir = WORKING_DIR.replaceAll("%n", "INTERNAL_SLURM@"+UT.Code.randID());
        // 由于是本地的，这里不需要创建文件夹
        // 仅 slurm 可用
        if (!IS_SLURM) {
            this.shutdown();
            throw new Exception("InternalSLURM can Only be used in SLURM");
        }
        // 根据环境变量设置参数
        mTaskNum = aTaskNum;
        mJobsPerNode = new HashMap<>();
        for (String tNode : NODE_LIST) mJobsPerNode.put(tNode, 0);
        
        // 根据结果设置线程池，同样需要预留一个核给 srun 本身
        mNcmdsPerNode = (CORES_PER_NODE-1) / mTaskNum;
        // 这里不支持跨节点任务管理
        if (mNcmdsPerNode == 0) {
            this.shutdown();
            throw new Exception("Task Number MUST be Less or Equal to Cores-Per-Node - 1 here");
        }
        if (aInternalThreadPool) setPool(newPool(NODE_LIST.size() * mNcmdsPerNode));
        else setPool(SERIAL_EXECUTOR);
    }
    public InternalSLURMSystemExecutor(int aTaskNum) throws Exception {this(aTaskNum, false);}
    
    /** 内部使用的向任务分配节点的方法 */
    private synchronized @Nullable String assignNode() {
        for (Map.Entry<String, Integer> tEntry : mJobsPerNode.entrySet()) {
            int tJobNum = tEntry.getValue();
            if (tJobNum < mNcmdsPerNode) {
                tEntry.setValue(tJobNum+1);
                return tEntry.getKey();
            }
        }
        // 所有节点的任务都分配满了，输出 null
        return null;
    }
    /** 内部使用的任务完成归还节点的方法 */
    private synchronized void returnNode(String aNode) {
        mJobsPerNode.computeIfPresent(aNode, (node, jobNum) -> jobNum-1);
    }
    
    @Override protected int system_(String aCommand, @NotNull IPrintlnSupplier aPrintln) {
        // 对于空指令专门优化，不执行操作
        if (aCommand == null || aCommand.isEmpty()) return -1;
        // 先尝试获取节点
        String tNode = assignNode();
        if (tNode == null) {
            System.err.println("WARNING: Can NOT to assign node for this job temporarily, this job blocks until there are any free node.");
            System.err.println("It may be caused by too large number of parallels.");
        }
        while (tNode == null) {
            try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace(); return -1;}
            tNode = assignNode();
        }
        // 为了兼容性，需要将实际需要执行的脚本写入 bash 后再执行（srun 特有的问题）
        String tTempScriptPath = mWorkingDir+UT.Code.randID()+".sh";
        try {UT.IO.write(tTempScriptPath, "#!/bin/bash\n"+aCommand);}
        catch (Exception e) {e.printStackTrace(); return -1;}
        // 组装指令
        List<String> rRunCommand = new ArrayList<>();
        rRunCommand.add("srun");
        rRunCommand.add("--nodelist");          rRunCommand.add(tNode);
        rRunCommand.add("--nodes");             rRunCommand.add(String.valueOf(1));
        rRunCommand.add("--ntasks");            rRunCommand.add(String.valueOf(mTaskNum));
        rRunCommand.add("--ntasks-per-node");   rRunCommand.add(String.valueOf(mTaskNum));
        rRunCommand.add("bash");                rRunCommand.add(tTempScriptPath); // 使用 bash 执行不需要考虑权限的问题
        // 执行
        int tOut = super.system_(String.join(" ", rRunCommand), aPrintln);
        // 任务完成后需要归还任务
        returnNode(tNode);
        return tOut;
    }
    
    /** 程序结束时删除自己的临时工作目录 */
    @Override protected void shutdownFinal() {
        if (mWorkingDir != null) try {removeDir(mWorkingDir);} catch (Exception ignored) {}
    }
}
