package jse.clib;

import jse.code.CS;
import jse.code.OS;
import jse.code.UT;
import org.apache.groovy.util.Maps;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static jse.code.OS.JAR_DIR;
import static jse.code.Conf.*;

/**
 * 其他 jni 库或者此项目需要依赖的 c 库；
 * 一种加速 c 中 malloc 和 free 的库。
 * @see <a href="https://github.com/microsoft/mimalloc"> microsoft/mimalloc </a>
 * @author liqa
 */
public class MiMalloc {
    private MiMalloc() {}
    
    /** 用于判断是否进行了静态初始化以及方便的手动初始化 */
    public final static class InitHelper {
        private static volatile boolean INITIALIZED = false;
        
        public static boolean initialized() {return INITIALIZED;}
        @SuppressWarnings({"ResultOfMethodCallIgnored", "UnnecessaryCallToStringValueOf"})
        public static void init() {
            // 手动调用此值来强制初始化
            if (!INITIALIZED) String.valueOf(HOME);
        }
    }
    
    public final static class Conf {
        /**
         * 自定义构建 mimalloc 的 cmake 参数设置，
         * 会在构建时使用 -D ${key}=${value} 传入
         */
        public final static Map<String, String> CMAKE_SETTING = new LinkedHashMap<>();
        
        /**
         * 自定义构建 mimalloc 时使用的编译器，
         * cmake 有时不能自动检测到希望使用的编译器
         */
        public static @Nullable String CMAKE_C_COMPILER   = OS.env("JSE_CMAKE_C_COMPILER_MIMALLOC"  , jse.code.Conf.CMAKE_C_COMPILER);
        public static @Nullable String CMAKE_CXX_COMPILER = OS.env("JSE_CMAKE_CXX_COMPILER_MIMALLOC", jse.code.Conf.CMAKE_CXX_COMPILER);
        public static @Nullable String CMAKE_C_FLAGS      = OS.env("JSE_CMAKE_C_FLAGS_MIMALLOC"     , jse.code.Conf.CMAKE_C_FLAGS);
        public static @Nullable String CMAKE_CXX_FLAGS    = OS.env("JSE_CMAKE_CXX_FLAGS_MIMALLOC"   , jse.code.Conf.CMAKE_CXX_FLAGS);
        
        /** 重定向 mimalloc 动态库的路径，主要用于作为重定向的 mpijni, lmpjni 等库的依赖导入 */
        public static @Nullable String REDIRECT_MIMALLOC_LIB = OS.env("JSE_REDIRECT_MIMALLOC_LIB");
        public static @Nullable String REDIRECT_MIMALLOC_LLIB = OS.env("JSE_REDIRECT_MIMALLOC_LLIB", REDIRECT_MIMALLOC_LIB);
    }
    
    
    public final static String VERSION = "2.1.7";
    
    public final static String HOME = JAR_DIR+"mimalloc/" + UT.Code.uniqueID(CS.VERSION, MiMalloc.VERSION, Conf.CMAKE_C_COMPILER, Conf.CMAKE_CXX_COMPILER, Conf.CMAKE_C_FLAGS, Conf.CMAKE_CXX_FLAGS, Conf.CMAKE_SETTING) + "/";
    public final static String LIB_DIR = HOME+"lib/";
    public final static String INCLUDE_DIR = HOME+"include/";
    public final static String LIB_PATH;
    public final static String LLIB_PATH;
    
    static {
        InitHelper.INITIALIZED = true;
        // 这样来统一增加 mimalloc 需要的默认额外设置，
        // 先添加额外设置，从而可以通过 Conf.CMAKE_SETTING 来覆盖这些设置
        Map<String, String> rCmakeSetting = Maps.of(
            "MI_BUILD_SHARED",  "ON",
            "MI_BUILD_STATIC",  "OFF",
            "MI_BUILD_OBJECT",  "OFF",
            "MI_BUILD_TESTS",   "OFF",
            "MI_OVERRIDE",      "OFF",
            "MI_WIN_REDIRECT",  "OFF",
            "MI_OSX_INTERPOSE", "OFF",
            "MI_OSX_ZONE",      "OFF",
            "CMAKE_BUILD_TYPE", "Release");
        rCmakeSetting.putAll(Conf.CMAKE_SETTING);
        // 现在直接使用 JNIUtil.buildLib 来统一初始化
        LIB_PATH = JNIUtil.buildLib("mimalloc", "MIMALLOC", wd -> {
                                        // 首先获取源码路径，这里直接从 resource 里输出
                                        String tMiZipPath = wd+"mimalloc-"+VERSION+".zip";
                                        UT.IO.copy(UT.IO.getResource("mimalloc/mimalloc-"+VERSION+".zip"), tMiZipPath);
                                        // 解压 mimalloc 包到临时目录，如果已经存在则直接清空此目录
                                        String tMiDir = wd+"mimalloc/";
                                        UT.IO.removeDir(tMiDir);
                                        UT.IO.zip2dir(tMiZipPath, tMiDir);
                                        // 手动拷贝头文件到指定目录，现在也放在这里
                                        UT.IO.copy(tMiDir+"include/mimalloc.h", INCLUDE_DIR+"mimalloc.h");
                                        return tMiDir;
                                    }, LIB_DIR,
                                    Conf.CMAKE_C_COMPILER, Conf.CMAKE_CXX_COMPILER, Conf.CMAKE_C_FLAGS, Conf.CMAKE_CXX_FLAGS,
                                    false, false, rCmakeSetting, Conf.REDIRECT_MIMALLOC_LIB, null);
        if (Conf.REDIRECT_MIMALLOC_LIB == null) {
            @Nullable String tLLibName = LLIB_NAME_IN(LIB_DIR, "mimalloc");
            LLIB_PATH = tLLibName==null ? LIB_PATH : (LIB_DIR+tLLibName);
        } else {
            LLIB_PATH = Conf.REDIRECT_MIMALLOC_LLIB==null ? Conf.REDIRECT_MIMALLOC_LIB : Conf.REDIRECT_MIMALLOC_LLIB;
        }
        // 设置库路径
        System.load(UT.IO.toAbsolutePath(LIB_PATH));
    }
}
