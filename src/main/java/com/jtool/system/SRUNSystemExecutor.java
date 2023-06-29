package com.jtool.system;

import com.jtool.code.UT;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.jtool.code.CS.Exec.EXE;
import static com.jtool.code.CS.Slurm.IS_SLURM;
import static com.jtool.code.CS.Slurm.NODE_LIST;
import static com.jtool.code.CS.WORKING_DIR;

/**
 * @author liqa
 * <p> 用于在 slurm 上执行 srun 指令，创建时一次性分配资源，避免重复分配 </p>
 * <p> 经过测试无法实现 </p>
 */
@ApiStatus.ScheduledForRemoval
@Deprecated
public class SRUNSystemExecutor extends LocalSystemExecutor {
    /** 一些目录设定， %n: unique job name */
    public final static String LONG_TIME_SRUN_SCRIPT_PATH = WORKING_DIR+"longTimeSRUN.sh";
    public final static String RUN_SCRIPT_PATH = WORKING_DIR+"run.sh";
    public final static String SHUTDOWN_FILE_PATH = WORKING_DIR+"shutdown";
    
    private final String mWorkingDir, mRunScriptPath, mShutdownFilePath;
    private final Future<Void> mSRUNTask;
    public SRUNSystemExecutor(final int aTaskNum) throws Exception {
        // 未来不会限制并行数，因此这里不再提供并行的设置
        super(SERIAL_EXECUTOR);
        // 设置一下工作目录
        String tUniqueJobName = "SRUN@"+UT.Code.randID();
        mWorkingDir = WORKING_DIR.replaceAll("%n", tUniqueJobName);
        mRunScriptPath = RUN_SCRIPT_PATH.replaceAll("%n", tUniqueJobName);
        mShutdownFilePath = SHUTDOWN_FILE_PATH.replaceAll("%n", tUniqueJobName);
        String tLongTimeSRUNScriptPath = LONG_TIME_SRUN_SCRIPT_PATH.replaceAll("%n", tUniqueJobName);
        // 仅 slurm 可用
        if (!IS_SLURM) {
            this.shutdown();
            throw new Exception("SRUN can Only be used in SLURM");
        }
        // 从资源文件中创建已经准备好的 longTimeSRUN
        try (BufferedReader tReader = UT.IO.toReader(UT.IO.getResource("slurm/longTimeSRUN.sh")); PrintStream tPS = UT.IO.toPrintStream(tLongTimeSRUNScriptPath)) {
            String tLine;
            while ((tLine = tReader.readLine()) != null) tPS.println(tLine);
        } catch (Exception e) {
            // 出现任何错误直接抛出错误退出
            this.shutdown();
            throw e;
        }
        // 组装指令
        List<String> rRunCommand = new ArrayList<>();
        rRunCommand.add("srun");
        rRunCommand.add("--nodelist");          rRunCommand.add(NODE_LIST.get(0)); // 可行性测试，单节点。TODO 等全局 SLURM 资源管理实现后进行修改
        rRunCommand.add("--nodes");             rRunCommand.add(String.valueOf(1));
        rRunCommand.add("--ntasks");            rRunCommand.add(String.valueOf(aTaskNum));
        rRunCommand.add("--ntasks-per-node");   rRunCommand.add(String.valueOf(aTaskNum));
        rRunCommand.add("bash");                rRunCommand.add(tLongTimeSRUNScriptPath); rRunCommand.add(mWorkingDir); // 使用 bash 执行不需要考虑权限的问题，并传入工作目录
        // TODO 后续移除一般 exec 的线程池后，这个协程可以省去
        mSRUNTask = CompletableFuture.runAsync(() -> EXE.system(String.join(" ", rRunCommand)));
    }
    
    // 可行性测试，直接继承，TODO 以后需要创建时指定最大并行数
    @SuppressWarnings("BusyWait")
    @Override protected synchronized int system_(String aCommand, @NotNull IPrintlnSupplier aPrintln) {
        // 对于空指令专门优化，不执行操作
        if (aCommand == null || aCommand.isEmpty()) return -1;
        // 如果长时任务挂了则报错
        if (mSRUNTask.isDone()) {
            System.err.println("ERROR: Long Time SRUN Server is Dead");
            return -1;
        }
        
        // 将实际需要执行的脚本写入 run.sh 即会自动执行
        try {UT.IO.write(mRunScriptPath, "#!/bin/bash\n"+aCommand);}
        catch (Exception e) {e.printStackTrace(); return -1;}
        
        // 等待脚本文件被删除后表明执行完成
        try {while (UT.IO.exists(mRunScriptPath)) Thread.sleep(100);}
        catch (Exception e) {e.printStackTrace(); return -1;}
        
        // 永远返回 0，简单起见不去捕获错误
        return 0;
    }
    
    /** 直接关闭 mMPIWorker，删除自己的临时工作目录，认为调用此方法时所有的计算已经完成 */
    @Override protected void shutdownFinal() {
        // 创建 shutdown 文件来停止，并等待结束（不会一直等待）
        try {UT.IO.write(mShutdownFilePath, "");} catch (Exception ignored) {}
        CompletableFuture.runAsync(() -> {
            try {
                mSRUNTask.get(5000, TimeUnit.MILLISECONDS);
                removeDir(mWorkingDir);
            } catch (Exception ignored) {}
        });
    }
}
