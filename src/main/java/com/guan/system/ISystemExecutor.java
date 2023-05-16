package com.guan.system;


import com.guan.io.IHasIOFiles;
import com.guan.parallel.IBatchSubmit;
import com.guan.parallel.IBatchSubmit2;
import com.guan.parallel.IBatchSubmit3;
import com.guan.parallel.IHasThreadPool;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @author liqa
 * <p> 通用的系统指令执行器接口，实现类似 matlab 的 system 指令功能，
 * 在此基础上增加了类似 Executor 的功能，可以后台运行等 </p>
 */
@SuppressWarnings("UnusedReturnValue")
public interface ISystemExecutor extends IHasThreadPool, IBatchSubmit<Integer, String>, IBatchSubmit2<Integer, String, IHasIOFiles> {
    /** 用来处理各种程序因为输出的目录不存在而报错的情况，方便在任何位置直接创建目录或者移除目录 */
    boolean makeDir(String aDir);
    @VisibleForTesting default boolean mkdir(String aDir) {return makeDir(aDir);}
    @ApiStatus.Internal boolean removeDir(String aDir);
    @VisibleForTesting @ApiStatus.Internal default boolean rmdir(String aDir) {return removeDir(aDir);}
    
    // TODO 砍掉 _NO 的接口改为属性实现，有点混淆并且实现起来重复代码太多
    
    @ApiStatus.Obsolete int system_NO(String aCommand); // No Output
    int system   (String aCommand                     );
    int system   (String aCommand, String aOutFilePath);
    
    /** 在原本的 system 基础上，增加了附加更多输入输出文件的功能，使用 IHasIOFiles 来附加 */
    @ApiStatus.Obsolete int system_NO(String aCommand                     , IHasIOFiles aIOFiles); // No Output
    int system   (String aCommand                     , IHasIOFiles aIOFiles);
    int system   (String aCommand, String aOutFilePath, IHasIOFiles aIOFiles);
    
    /** submit stuffs */
    @ApiStatus.Obsolete Future<Integer> submitSystem_NO(String aCommand                                           ); // No Output
    Future<Integer> submitSystem   (String aCommand                                           );
    Future<Integer> submitSystem   (String aCommand, String aOutFilePath                      );
    @ApiStatus.Obsolete Future<Integer> submitSystem_NO(String aCommand                     , IHasIOFiles aIOFiles); // No Output
    Future<Integer> submitSystem   (String aCommand                     , IHasIOFiles aIOFiles);
    Future<Integer> submitSystem   (String aCommand, String aOutFilePath, IHasIOFiles aIOFiles);
    
    /** BatchSubmit stuffs，不获取输出，不保证 Future 获取到的退出码是正确的 */
    Future<Integer> getSubmit();
    Future<Integer> batchSubmit(Iterable<String> aCommands);
    void putSubmit(String aCommand);
    void putSubmit(String aCommand, IHasIOFiles aIOFiles);
    
    
    /** 获取字符串输出而不是退出代码 */
    List<String> system_str(String aCommand);
    List<String> system_str(String aCommand, IHasIOFiles aIOFiles);
    Future<List<String>> submitSystem_str(String aCommand);
    Future<List<String>> submitSystem_str(String aCommand, IHasIOFiles aIOFiles);
}
