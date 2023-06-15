package com.jtool.system;

import com.jtool.code.UT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jtool.code.CS.WORKING_DIR;

/**
 * @author liqa
 * <p> 在 SLURM 内部提交子任务的提交器，主要用于 salloc 或者 sbatch 一个 jTool 任务后，
 * 在提交的 jTool 任务中提交子任务；因此认为此时已经有了 SLURM 的任务环境 </p>
 * <p> 由于是提交子任务的形式，这里依旧使用 java 线程池来提交后台任务 </p>
 */
public class InternalSLURMSystemExecutor extends LocalSystemExecutor {
    private final String mWorkingDir;
    
    private final long mSleepTime;
    private final int mTaskNum;
    private final int mNcmdsPerNode;
    private final List<String> mNodeList;
    
    /** 线程数现在由每个任务的并行数，申请到的节点数，以及每节点的核心数来确定 */
    public InternalSLURMSystemExecutor(int aTaskNum) throws Exception {this(aTaskNum, 500);}
    public InternalSLURMSystemExecutor(int aTaskNum, long aSleepTime) throws Exception {
        // 先随便设置一下线程池，因为要先构造父类
        super();
        mTaskNum = aTaskNum;
        mSleepTime = aSleepTime;
        try {
            // 直接根据环境变量获取每节点的核心数
            @Nullable String tRawCoresPerNode = System.getenv("SLURM_JOB_CPUS_PER_NODE");
            // 尝试读取结果，如果失败则抛出错误
            if (tRawCoresPerNode == null) throw new Exception("Load SLURM_JOB_CPUS_PER_NODE Fail, this class MUST init in SLURM environment");
            // 目前仅支持单一的 CoresPerNode，对于有多个的情况会选取最小值
            Pattern tPattern = Pattern.compile("(\\d+)(\\([^)]+\\))?"); // 匹配整数部分和可选的括号部分
            Matcher tMatcher = tPattern.matcher(tRawCoresPerNode);
            int tCoresPerNode = -1;
            while (tMatcher.find()) {
                int tResult = Integer.parseInt(tMatcher.group(1));
                tCoresPerNode = (tCoresPerNode < 0) ? tResult : Math.min(tCoresPerNode, tResult);
            }
            if (tCoresPerNode < 0) throw new Exception("Parse SLURM_JOB_CPUS_PER_NODE Fail");
            
            // 直接根据环境变量获取节点列表
            @Nullable String tRawNodeList = System.getenv("SLURM_NODELIST");
            // 尝试读取结果，如果失败则抛出错误
            if (tRawNodeList == null) throw new Exception("Load SLURM_NODELIST Fail, this class MUST init in SLURM environment");
            mNodeList = UT.Texts.splitNodeList(tRawNodeList);
            if (mNodeList.isEmpty()) throw new Exception("Parse SLURM_NODELIST Fail");
            
            // 根据结果设置线程池
            mNcmdsPerNode = tCoresPerNode / mTaskNum;
            if (mNcmdsPerNode == 0) throw new Exception("Task Number MUST be Less or Equal to Cores-Per-Node here"); // 这里不支持跨节点任务管理
            setPool(newPool(mNodeList.size() * mNcmdsPerNode));
            
            // 最后设置一下工作目录
            mWorkingDir = WORKING_DIR.replaceAll("%n", "INTERNAL_SLURM@"+UT.Code.randID());
            // 由于是本地的，这里不需要创建文件夹
        } catch (Exception e) {
            // 如果失败则需要关闭自身，虽然实际上可能并不需要
            this.shutdown();
            throw e;
        }
    }
    
    /** 内部使用的获取具体节点的方法 */
    private int mIdx = 0;
    private String assignNode() {
        int tNodeIdx = mIdx / mNcmdsPerNode;
        ++mIdx;
        if (mIdx == nThreads()) mIdx = 0;
        return mNodeList.get(tNodeIdx);
    }
    
    @Override protected int system_(String aCommand, @NotNull IPrintlnSupplier aPrintln) {
        // 对于空指令专门优化，不执行操作
        if (aCommand == null || aCommand.isEmpty()) return -1;
        // 为了兼容性，需要将实际需要执行的脚本写入 bash 后再执行
        String tTempScriptPath = mWorkingDir+UT.Code.randID()+".sh";
        try {UT.IO.write(tTempScriptPath, "#!/bin/bash\n"+aCommand);} catch (Exception e) {e.printStackTrace(); return -1;}
        // 组装指令
        List<String> rRunCommand = new ArrayList<>();
        rRunCommand.add("srun");
        rRunCommand.add("--nodelist");          rRunCommand.add(assignNode());
        rRunCommand.add("--nodes");             rRunCommand.add(String.valueOf(1));
        rRunCommand.add("--ntasks");            rRunCommand.add(String.valueOf(mTaskNum));
        rRunCommand.add("--ntasks-per-node");   rRunCommand.add(String.valueOf(mTaskNum));
        rRunCommand.add("bash");                rRunCommand.add(tTempScriptPath); // 使用 bash 执行不需要考虑权限的问题
        // 执行
        int tOut = super.system_(String.join(" ", rRunCommand), aPrintln);
        // 需要等待 srun 释放资源才能继续提交任务，避免 bus busy
        try {Thread.sleep(mSleepTime);} catch (InterruptedException ignored) {}
        return tOut;
    }
    
    /** 程序结束时删除自己的临时工作目录 */
    @Override protected void shutdownFinal() {
        try {removeDir(mWorkingDir);} catch (Exception ignored) {}
    }
}
