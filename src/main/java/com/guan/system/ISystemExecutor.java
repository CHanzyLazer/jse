package com.guan.system;


import com.guan.code.IHasIOFiles;
import com.guan.code.UT;
import com.guan.parallel.IHasThreadPool;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Future;

/**
 * @author liqa
 * <p> 通用的系统指令执行器接口，实现类似 matlab 的 system 指令功能，
 * 在此基础上增加了类似 Executor 的功能，可以后台运行等 </p>
 */
public interface ISystemExecutor extends IHasThreadPool {
    int system_NO(String aCommand                                       ); // No Output
    int system   (String aCommand                                       );
    int system   (String aCommand, String aOutFilePath                  ) throws IOException;
    int system   (String aCommand, @Nullable PrintStream aOutPrintStream);
    
    /** 在原本的 system 基础上，增加了附加更多输入输出文件的功能，使用 IHasIOFiles 来附加 */
    int system_NO(String aCommand                                       , IHasIOFiles aIOFiles); // No Output
    int system   (String aCommand                                       , IHasIOFiles aIOFiles);
    int system   (String aCommand, String      aOutFilePath             , IHasIOFiles aIOFiles) throws IOException;
    int system   (String aCommand, @Nullable PrintStream aOutPrintStream, IHasIOFiles aIOFiles);
    
    /** submit stuffs */
    Future<Integer> submitSystem_NO(String aCommand                                                             ); // No Output
    Future<Integer> submitSystem   (String aCommand                                                             );
    Future<Integer> submitSystem   (String aCommand, String aOutFilePath                                        );
    Future<Integer> submitSystem   (String aCommand, @Nullable PrintStream aOutPrintStream                      );
    Future<Integer> submitSystem_NO(String aCommand                                       , IHasIOFiles aIOFiles); // No Output
    Future<Integer> submitSystem   (String aCommand                                       , IHasIOFiles aIOFiles);
    Future<Integer> submitSystem   (String aCommand, String      aOutFilePath             , IHasIOFiles aIOFiles);
    Future<Integer> submitSystem   (String aCommand, @Nullable PrintStream aOutPrintStream, IHasIOFiles aIOFiles);
}
