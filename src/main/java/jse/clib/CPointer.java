package jse.clib;

import jse.code.OS;
import jse.code.UT;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import static jse.code.CS.VERSION;
import static jse.code.OS.JAR_DIR;

/**
 * 直接访问使用 C 指针的类，不进行自动内存回收和各种检查从而保证最大的兼容性；
 * 此类因此是 {@code Unsafe} 的。
 * <p>
 * 内部默认会统一使用 {@link MiMalloc} 来加速内存分配和释放的过程。
 * @author liqa
 */
public class CPointer {
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
         * 自定义构建 cpointer 的 cmake 参数设置，
         * 会在构建时使用 -D ${key}=${value} 传入
         */
        public final static Map<String, String> CMAKE_SETTING = new LinkedHashMap<>();
        
        /**
         * 自定义构建 cpointer 时使用的编译器，
         * cmake 有时不能自动检测到希望使用的编译器
         */
        public static @Nullable String CMAKE_C_COMPILER   = OS.env("JSE_CMAKE_C_COMPILER_CPOINTER"  , jse.code.Conf.CMAKE_C_COMPILER);
        public static @Nullable String CMAKE_CXX_COMPILER = OS.env("JSE_CMAKE_CXX_COMPILER_CPOINTER", jse.code.Conf.CMAKE_CXX_COMPILER);
        public static @Nullable String CMAKE_C_FLAGS      = OS.env("JSE_CMAKE_C_FLAGS_CPOINTER"     , jse.code.Conf.CMAKE_C_FLAGS);
        public static @Nullable String CMAKE_CXX_FLAGS    = OS.env("JSE_CMAKE_CXX_FLAGS_CPOINTER"   , jse.code.Conf.CMAKE_CXX_FLAGS);
        
        /**
         * 对于 cpointer，是否使用 {@link MiMalloc} 来加速 c 的内存分配，
         * 这对于 java 数组和 c 数组的转换很有效
         */
        public static boolean USE_MIMALLOC = OS.envZ("JSE_USE_MIMALLOC_CPOINTER", jse.code.Conf.USE_MIMALLOC);
        
        /** 重定向 cpointer 动态库的路径，用于自定义编译这个库的过程，或者重新实现 cpointer 的接口 */
        public static @Nullable String REDIRECT_CPOINTER_LIB = OS.env("JSE_REDIRECT_CPOINTER_LIB");
    }
    
    private final static String LIB_DIR = JAR_DIR+"cpointer/" + UT.Code.uniqueID(VERSION, Conf.USE_MIMALLOC, Conf.CMAKE_C_COMPILER, Conf.CMAKE_CXX_COMPILER, Conf.CMAKE_C_FLAGS, Conf.CMAKE_CXX_FLAGS, Conf.CMAKE_SETTING) + "/";
    private final static String LIB_PATH;
    private final static String[] SRC_NAME = {
          "jse_clib_CPointer.c"
        , "jse_clib_CPointer.h"
        , "jse_clib_IntCPointer.c"
        , "jse_clib_IntCPointer.h"
        , "jse_clib_DoubleCPointer.c"
        , "jse_clib_DoubleCPointer.h"
        , "jse_clib_NestedCPointer.c"
        , "jse_clib_NestedCPointer.h"
        , "jse_clib_NestedIntCPointer.c"
        , "jse_clib_NestedIntCPointer.h"
        , "jse_clib_NestedDoubleCPointer.c"
        , "jse_clib_NestedDoubleCPointer.h"
    };
    
    static {
        InitHelper.INITIALIZED = true;
        // 依赖 jniutil
        JNIUtil.InitHelper.init();
        // 现在直接使用 JNIUtil.buildLib 来统一初始化
        LIB_PATH = new JNIUtil.LibBuilder("cpointer", "CPOINTER", LIB_DIR, Conf.CMAKE_SETTING)
            .setSrc("cpointer", SRC_NAME)
            .setCmakeCCompiler(Conf.CMAKE_C_COMPILER).setCmakeCxxCompiler(Conf.CMAKE_CXX_COMPILER).setCmakeCFlags(Conf.CMAKE_C_FLAGS).setCmakeCxxFlags(Conf.CMAKE_CXX_FLAGS)
            .setUseMiMalloc(Conf.USE_MIMALLOC).setRedirectLibPath(Conf.REDIRECT_CPOINTER_LIB)
            .get();
        // 设置库路径
        System.load(UT.IO.toAbsolutePath(LIB_PATH));
    }
    
    
    protected long mPtr;
    @ApiStatus.Internal public CPointer(long aPtr) {mPtr = aPtr;}
    @ApiStatus.Internal public final long ptr_() {return mPtr;}
    
    public boolean isNull() {return mPtr==0 || mPtr==-1;}
    
    public static CPointer malloc(int aCount) {
        return new CPointer(malloc_(aCount));
    }
    protected native static long malloc_(int aCount);
    
    public static CPointer calloc(int aCount) {
        return new CPointer(calloc_(aCount));
    }
    protected native static long calloc_(int aCount);
    
    public void free() {
        if (isNull()) throw new IllegalStateException("Cannot free a NULL pointer");
        free_(mPtr);
        mPtr = 0;
    }
    private native static void free_(long aPtr);
    
    public CPointer copy() {
        return new CPointer(mPtr);
    }
    @Override public final boolean equals(Object aRHS) {
        if (this == aRHS) return true;
        if (!(aRHS instanceof CPointer)) return false;
        
        CPointer tCPointer = (CPointer)aRHS;
        return mPtr == tCPointer.mPtr;
    }
    @Override public final int hashCode() {
        return Long.hashCode(mPtr);
    }
    
    public IntCPointer asIntCPointer() {return new IntCPointer(mPtr);}
    public DoubleCPointer asDoubleCPointer() {return new DoubleCPointer(mPtr);}
    public NestedCPointer asNestedCPointer() {return new NestedCPointer(mPtr);}
    public NestedIntCPointer asNestedIntCPointer() {return new NestedIntCPointer(mPtr);}
    public NestedDoubleCPointer asNestedDoubleCPointer() {return new NestedDoubleCPointer(mPtr);}
    
    static void rangeCheck(int jArraySize, int aCount) {
        if (aCount > jArraySize) throw new IndexOutOfBoundsException(aCount+" > "+jArraySize);
    }
}
