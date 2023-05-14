package com.guan.system;


import com.guan.code.UT;
import com.guan.io.IHasIOFiles;
import com.guan.parallel.IExecutorEX;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.util.concurrent.Future;

/**
 * @author liqa
 * <p> SystemExecutor 的一般实现，直接在本地运行 </p>
 */
public class LocalSystemExecutor extends AbstractSystemExecutor {
    public LocalSystemExecutor(int aThreadNum) {this(newPool(aThreadNum));}
    public LocalSystemExecutor() {this(SERIAL_EXECUTOR);}
    
    protected LocalSystemExecutor(IExecutorEX aPool) {super(aPool); mRuntime = Runtime.getRuntime();}
    final Runtime mRuntime;
    
    
    @Override public int system_(String aCommand, @NotNull IPrintlnSupplier aPrintln) {
        int tExitValue;
        Process tProcess = null;
        try (IPrintln tPrintln = aPrintln.get()) {
            // 执行指令
            tProcess = mRuntime.exec(aCommand);
            // 读取执行的输出（由于内部会对输出自动 buffer，获取 stream 和执行的顺序不重要）
            if (tPrintln != null) try (BufferedReader tReader = UT.IO.toReader(tProcess.getInputStream())) {
                String tLine;
                while ((tLine = tReader.readLine()) != null) tPrintln.println(tLine);
            }
            // 等待执行完成
            tExitValue = tProcess.waitFor();
        } catch (Exception e) {
            tExitValue = -1;
            e.printStackTrace();
        } finally {
            // 无论程序如何结束都停止进程
            if (tProcess != null) tProcess.destroyForcibly();
        }
        return tExitValue;
    }
    /** 这样保证只会在执行的时候获取 println */
    @Override protected Future<Integer> submitSystem_(final String aCommand, final @NotNull IPrintlnSupplier aPrintln) {return pool().submit(() -> system_(aCommand, aPrintln));}
    
    
    /** 对于本地的带有 IOFiles 的没有区别 */
    @Override public int system_(String aCommand, @NotNull IPrintlnSupplier aPrintln, IHasIOFiles aIOFiles) {return system_(aCommand, aPrintln);}
    @Override protected Future<Integer> submitSystem_(String aCommand, @NotNull IPrintlnSupplier aPrintln, IHasIOFiles aIOFiles) {return submitSystem_(aCommand, aPrintln);}
}
