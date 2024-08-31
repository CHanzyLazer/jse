package jse.clib;

import jse.code.OS;
import jse.code.UT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jse.code.CS.VERSION;
import static jse.code.Conf.*;
import static jse.code.OS.EXEC;
import static jse.code.OS.JAR_DIR;

/**
 * 直接调用 {@code <dlfcn.h>} 中的 {@code dlopen()}
 * 来加载动态库的类，最大增加 jni 库的兼容性。
 * <p>
 * 采用 jep 中完全一致的实现，即只提升库到 global 的水平，不做其他操作；
 * 对于 unix 以外的系统不会做任何操作，如果这样操作后依旧无法正常工作，
 * 那么依旧可能需要使用{@code LD_PRELOAD} 来加载动态库。
 * @author liqa
 */
public class Dlfcn {
    /** 用于判断是否进行了静态初始化以及方便的手动初始化 */
    public final static class InitHelper {
        private static volatile boolean INITIALIZED = false;
        
        public static boolean initialized() {return INITIALIZED;}
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public static void init() {
            if (!INITIALIZED) String.valueOf(LIB_PATH);
        }
    }
    
    public final static class Conf {
        /**
         * 自定义构建 dlfcnjni 的 cmake 参数设置，
         * 会在构建时使用 -D ${key}=${value} 传入
         */
        public final static Map<String, String> CMAKE_SETTING = new HashMap<>();
        
        /**
         * 自定义构建 dlfcnjni 时使用的编译器，
         * cmake 有时不能自动检测到希望使用的编译器
         */
        public static @Nullable String CMAKE_C_COMPILER = OS.env("JSE_CMAKE_C_COMPILER_DLFCN", jse.code.Conf.CMAKE_C_COMPILER);
        public static @Nullable String CMAKE_C_FLAGS    = OS.env("JSE_CMAKE_C_FLAGS_DLFCN"   , jse.code.Conf.CMAKE_C_FLAGS);
        
        /** 重定向 dlfcnjni 动态库的路径，用于自定义编译这个库的过程，或者重新实现 dlfcnjni 的接口 */
        public static @Nullable String REDIRECT_DLFCN_LIB = OS.env("JSE_REDIRECT_DLFCN_LIB");
    }
    
    private final static String LIB_DIR = JAR_DIR+"dlfcn/" + UT.Code.uniqueID(VERSION, Conf.CMAKE_C_COMPILER, Conf.CMAKE_C_FLAGS, Conf.CMAKE_SETTING) + "/";
    private final static String LIB_PATH;
    private final static String[] SRC_NAME = {
          "jse_clib_Dlfcn.c"
        , "jse_clib_Dlfcn.h"
    };
    
    private static String cmakeInitCmd_() {
        // 设置参数，这里使用 List 来构造这个长指令
        List<String> rCommand = new ArrayList<>();
        rCommand.add("cmake");
        // 这里设置 C/C++ 编译器（如果有）
        if (Conf.CMAKE_C_COMPILER != null) {rCommand.add("-D"); rCommand.add("CMAKE_C_COMPILER="+ Conf.CMAKE_C_COMPILER);}
        if (Conf.CMAKE_C_FLAGS    != null) {rCommand.add("-D"); rCommand.add("CMAKE_C_FLAGS='"  + Conf.CMAKE_C_FLAGS +"'");}
        // 初始化使用上一个目录的 CMakeList.txt
        rCommand.add("..");
        return String.join(" ", rCommand);
    }
    private static String cmakeSettingCmd_() throws IOException {
        // 设置参数，这里使用 List 来构造这个长指令
        List<String> rCommand = new ArrayList<>();
        rCommand.add("cmake");
        // 设置构建输出目录为 lib
        UT.IO.makeDir(LIB_DIR); // 初始化一下这个目录避免意料外的问题
        rCommand.add("-D"); rCommand.add("CMAKE_ARCHIVE_OUTPUT_DIRECTORY:PATH='"+ LIB_DIR +"'");
        rCommand.add("-D"); rCommand.add("CMAKE_LIBRARY_OUTPUT_DIRECTORY:PATH='"+ LIB_DIR +"'");
        rCommand.add("-D"); rCommand.add("CMAKE_RUNTIME_OUTPUT_DIRECTORY:PATH='"+ LIB_DIR +"'");
        rCommand.add("-D"); rCommand.add("CMAKE_ARCHIVE_OUTPUT_DIRECTORY_RELEASE:PATH='"+ LIB_DIR +"'");
        rCommand.add("-D"); rCommand.add("CMAKE_LIBRARY_OUTPUT_DIRECTORY_RELEASE:PATH='"+ LIB_DIR +"'");
        rCommand.add("-D"); rCommand.add("CMAKE_RUNTIME_OUTPUT_DIRECTORY_RELEASE:PATH='"+ LIB_DIR +"'");
        // 添加额外的设置参数
        for (Map.Entry<String, String> tEntry : Conf.CMAKE_SETTING.entrySet()) {
        rCommand.add("-D"); rCommand.add(String.format("%s=%s", tEntry.getKey(), tEntry.getValue()));
        }
        rCommand.add(".");
        return String.join(" ", rCommand);
    }
    
    private static @NotNull String initDlfcn_() throws Exception {
        // 检测 cmake，为了简洁并避免问题，现在要求一定要有 cmake 环境
        EXEC.setNoSTDOutput().setNoERROutput();
        boolean tNoCmake = EXEC.system("cmake --version") != 0;
        EXEC.setNoSTDOutput(false).setNoERROutput(false);
        if (tNoCmake) throw new Exception("DLFCN BUILD ERROR: No cmake environment.");
        // 从内部资源解压到临时目录
        String tWorkingDir = WORKING_DIR_OF("dlfcnjni");
        // 如果已经存在则先删除
        UT.IO.removeDir(tWorkingDir);
        for (String tName : SRC_NAME) {
            UT.IO.copy(UT.IO.getResource("dlfcn/src/"+tName), tWorkingDir+tName);
        }
        // 这里可以直接拷贝 CMakeLists.txt
        UT.IO.copy(UT.IO.getResource("dlfcn/src/CMakeLists.txt"), tWorkingDir+"CMakeLists.txt");
        System.out.println("DLFCN INIT INFO: Building dlfcnjni from source code...");
        String tBuildDir = tWorkingDir+"build/";
        UT.IO.makeDir(tBuildDir);
        // 直接通过系统指令来编译 dlfcn 的库，关闭输出
        EXEC.setNoSTDOutput().setWorkingDir(tBuildDir);
        // 初始化 cmake
        EXEC.system(cmakeInitCmd_());
        // 设置参数
        EXEC.system(cmakeSettingCmd_());
        // 最后进行构造操作
        EXEC.system("cmake --build . --config Release");
        EXEC.setNoSTDOutput(false).setWorkingDir(null);
        // 简单检测一下是否编译成功
        @Nullable String tLibName = LIB_NAME_IN(LIB_DIR, "dlfcnjni");
        if (tLibName == null) throw new Exception("DLFCN BUILD ERROR: Build Failed, No dlfcnjni lib in '"+LIB_DIR+"'");
        // 完事后移除临时解压得到的源码
        UT.IO.removeDir(tWorkingDir);
        System.out.println("DLFCN INIT INFO: dlfcnjni successfully installed.");
        // 输出安装完成后的库名称
        return tLibName;
    }
    
    static {
        InitHelper.INITIALIZED = true;
        
        if (Conf.REDIRECT_DLFCN_LIB == null) {
            @Nullable String tLibName = LIB_NAME_IN(LIB_DIR, "dlfcnjni");
            // 如果不存在 jni lib 则需要重新通过源码编译
            if (tLibName == null) {
                System.out.println("DLFCN INIT INFO: dlfcnjni libraries not found. Reinstalling...");
                try {tLibName = initDlfcn_();} catch (Exception e) {throw new RuntimeException(e);}
            }
            LIB_PATH = LIB_DIR + tLibName;
        } else {
            if (DEBUG) System.out.println("DLFCN INIT INFO: dlfcnjni libraries are redirected to '" + Conf.REDIRECT_DLFCN_LIB + "'");
            LIB_PATH = Conf.REDIRECT_DLFCN_LIB;
        }
        // 设置库路径，这里直接使用 System.load
        System.load(UT.IO.toAbsolutePath(LIB_PATH));
    }
    
    /** 这里简单处理，直接在 C 中实现这个打开部分，不把所有接口都实现；使用和 jep 同样的实现方式，即只提升库到 global 的水平，不做其他操作 */
    public native static void dlopen(String aPath);
}
