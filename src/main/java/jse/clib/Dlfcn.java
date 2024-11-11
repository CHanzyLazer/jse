package jse.clib;

import jse.code.OS;
import jse.code.UT;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import static jse.code.CS.VERSION;
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
        public final static Map<String, String> CMAKE_SETTING = new LinkedHashMap<>();
        
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
    
    static {
        InitHelper.INITIALIZED = true;
        LIB_PATH = JNIUtil.buildLib("dlfcnjni", "dlfcn", "DLFCN", SRC_NAME, LIB_DIR,
                                    Conf.CMAKE_C_COMPILER, null, Conf.CMAKE_C_FLAGS, null,
                                    false, false, Conf.CMAKE_SETTING, Conf.REDIRECT_DLFCN_LIB, null);
        // 设置库路径，这里直接使用 System.load
        System.load(UT.IO.toAbsolutePath(LIB_PATH));
    }
    
    /** 这里简单处理，直接在 C 中实现这个打开部分，不把所有接口都实现；使用和 jep 同样的实现方式，即只提升库到 global 的水平，不做其他操作 */
    public native static void dlopen(String aPath);
}
