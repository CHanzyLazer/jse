package com.guan.system;


import com.guan.code.UT;
import com.guan.parallel.AbstractHasThreadPool;
import com.guan.parallel.IExecutorEX;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.Future;

/**
 * @author liqa
 * <p> SystemExecutor 的一般实现，直接在本地运行，默认输出在 System.out </p>
 */
public class LocalSystemExecutor extends AbstractHasThreadPool<IExecutorEX> implements ISystemExecutor {
    public LocalSystemExecutor(int aThreadNum) {super(newPool(aThreadNum));}
    
    @Override public int system_NO(String aCommand) {return system(aCommand, (PrintStream)null);}
    @Override public int system(String aCommand) {return system(aCommand, System.out);}
    @Override public int system(String aCommand, String aOutFilePath) {
        PrintStream tFilePS;
        try {tFilePS = UT.IO.toPrintStream(aOutFilePath);} catch (IOException e) {throw new RuntimeException(e);}
        int tExitValue = system(aCommand, tFilePS);
        tFilePS.close(); // 记得关闭输出文件
        return tExitValue;
    }
    @Override public int system(String aCommand, @Nullable PrintStream aOutPrintStream) {
        int tExitValue;
        Process tProcess = null;
        try {
            // 获取 Runtime 实例
            Runtime tRuntime = Runtime.getRuntime();
            // 执行指令
            tProcess = tRuntime.exec(aCommand);
            // 读取执行的输出
            if (aOutPrintStream != null) {
                BufferedReader tReader = new BufferedReader(new InputStreamReader(tProcess.getInputStream()));
                String tLine;
                while ((tLine = tReader.readLine()) != null) {
                    aOutPrintStream.println(tLine);
                }
                tReader.close();
            }
            // 等待执行完成
            tExitValue = tProcess.waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 无论程序如何结束都停止进程
            if (tProcess != null) tProcess.destroyForcibly();
        }
        return tExitValue;
    }
    
    @Override public Future<Integer> submitSystem_NO(String aCommand) {return pool().submit(() -> system_NO(aCommand));}
    @Override public Future<Integer> submitSystem(String aCommand) {return pool().submit(() -> system(aCommand));}
    @Override public Future<Integer> submitSystem(String aCommand, String aOutFilePath) {return pool().submit(() -> system(aCommand, aOutFilePath));}
    @Override public Future<Integer> submitSystem(String aCommand, @Nullable PrintStream aOutPrintStream) {return pool().submit(() -> system(aCommand, aOutPrintStream));}
}
