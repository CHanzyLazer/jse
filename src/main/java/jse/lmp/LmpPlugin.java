package jse.lmp;

import jse.clib.*;
import jse.code.OS;
import jse.code.SP;
import jse.code.UT;
import jse.parallel.IAutoShutdown;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static jse.code.CS.VERSION;
import static jse.code.OS.*;

/**
 * 原生的 lammps jse 插件，提供 {@code LmpPair} 和
 * {@code LmpFix}，重写以实现自定义的 lammps {@code pair} 和 {@code fix} 指令
 * @author liqa
 */
public class LmpPlugin {
    /** 目前认为这些值永远都不会变，至少在 java 中这些操作肯定是可以达到目的 */
    public final static int SBBITS = 30, HISTBITS = 29, NEIGHMASK = 0x1FFFFFFF, HISTMASK = 0xDFFFFFFF, SPECIALMASK = 0x3FFFFFFF;
    
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
         * 自定义构建 lmpplugin 的 cmake 参数设置，
         * 会在构建时使用 -D ${key}=${value} 传入
         */
        public final static Map<String, String> CMAKE_SETTING = new LinkedHashMap<>();
        
        /**
         * 自定义构建 lmpplugin 时使用的编译器，
         * cmake 有时不能自动检测到希望使用的编译器
         */
        public static @Nullable String CMAKE_CXX_COMPILER = OS.env("JSE_CMAKE_CXX_COMPILER_LMPPLUGIN", jse.code.Conf.CMAKE_CXX_COMPILER);
        public static @Nullable String CMAKE_CXX_FLAGS    = OS.env("JSE_CMAKE_CXX_FLAGS_LMPPLUGIN"   , jse.code.Conf.CMAKE_CXX_FLAGS);
        
        /**
         * 对于 lmpplugin，是否使用 {@link MiMalloc} 来加速 c 的内存分配，
         * 这对于 java 数组和 c 数组的转换很有效
         */
        public static boolean USE_MIMALLOC = OS.envZ("JSE_USE_MIMALLOC_LMPPLUGIN", jse.code.Conf.USE_MIMALLOC);
        
        /** 启动的 jvm 的最大内存，默认为 1g 用来防止 mpi 运行 java 导致内存溢出 */
        public static String JVM_XMX = "1g";
        
        /** 重定向 lmpplugin 动态库的路径，用于自定义编译这个库的过程，或者重新实现 lmpplugin 的接口 */
        public static @Nullable String REDIRECT_LMPPLUGIN_LIB = OS.env("JSE_REDIRECT_LMPPLUGIN_LIB");
    }
    
    public final static String LIB_DIR = JAR_DIR+"lmpplugin/" + UT.Code.uniqueID(VERSION, Conf.CMAKE_CXX_COMPILER, Conf.CMAKE_CXX_FLAGS, Conf.CMAKE_SETTING) + "/";
    public final static String LIB_PATH;
    private final static String[] SRC_NAME = {
          "jse_lmp_LmpPlugin_Pair.cpp"
        , "jse_lmp_LmpPlugin_Pair.h"
        , "jseplugin.cpp"
        , "LmpPair.cpp"
        , "LmpPair.h"
        , "LmpPlugin.cpp"
        , "LmpPlugin.h"
        , "pair_jse.cpp"
        , "pair_jse.h"
        , "lammpsplugin.h"
        , "version.h"
    };
    
    static {
        InitHelper.INITIALIZED = true;
        
        // 依赖 jniutil
        JNIUtil.InitHelper.init();
        // 依赖 cpointer
        CPointer.InitHelper.init();
        // 依赖 lmpjni，且需要开启了 PLUGIN 插件
        NativeLmp.Conf.CMAKE_SETTING.put("PKG_PLUGIN", "ON");
        NativeLmp.InitHelper.init();
        // 现在直接使用 JNIUtil.buildLib 来统一初始化
        LIB_PATH = new JNIUtil.LibBuilder("lmpplugin", "LMPPLUGIN", LIB_DIR, Conf.CMAKE_SETTING)
            .setSrc("lmpplugin", SRC_NAME)
            .setCmakeCxxCompiler(Conf.CMAKE_CXX_COMPILER).setCmakeCxxFlags(Conf.CMAKE_CXX_FLAGS)
            .setUseMiMalloc(Conf.USE_MIMALLOC).setRedirectLibPath(Conf.REDIRECT_LMPPLUGIN_LIB)
            .setCmakeLineOpt(line -> {
                // 替换其中的 lammps 库路径为设置好的路径
                line = line.replace("$ENV{JSE_LMP_INCLUDE_DIR}", NativeLmp.NATIVELMP_INCLUDE_DIR.replace("\\", "\\\\"))  // 注意反斜杠的转义问题
                           .replace("$ENV{JSE_LMP_LIB_PATH}"   , NativeLmp.NATIVELMP_LLIB_PATH  .replace("\\", "\\\\")); // 注意反斜杠的转义问题
                // 替换其中的 jvm 库路径为自动检测到的路径
                line = line.replace("$ENV{JSE_JVM_LIB_PATH_DEF}", JVM.LIB_PATH.replace("\\", "\\\\\\\\")); // 注意反斜杠的转义问题
                // 替换 jvm 启动设置
                line = line.replace("$ENV{JSE_JAR_PATH_DEF}",  JAR_PATH.replace("\\", "\\\\\\\\"))  // 注意反斜杠的转义问题
                           .replace("$ENV{JSE_JVM_XMX}", Conf.JVM_XMX);
                return line;
            }).get();
        // 设置库路径，这里直接使用 System.load
        System.load(UT.IO.toAbsolutePath(LIB_PATH));
    }
    
    
    public static abstract class Pair implements IAutoShutdown {
        static {
            // 确保 LmpPlugin 已经确实初始化
            LmpPlugin.InitHelper.init();
        }
        
        
        /**
         * 通过反射来获取类，可以是文件路径，也可以是类路径；
         * 这里简单处理，先尝试当文件路径，然后再尝试当类路径
         * @param aClassNameOrPath 类路径的名称或者是 groovy 的脚本文件路径
         * @param aPairPtr lammps jse pair 对应类的指针
         * @return 需要的对象
         * @author liqa
         */
        public static Pair of(String aClassNameOrPath, long aPairPtr) throws Exception {
            Class<?> tClazz;
            try {
                tClazz = SP.Groovy.parseClass(aClassNameOrPath);
            } catch (Exception e) {
                try {
                    tClazz = SP.Groovy.getClass(aClassNameOrPath);
                } catch (Exception ex) {
                    // 这里简单判断然后抛出合适的错误
                    if (aClassNameOrPath.endsWith(".groovy") || aClassNameOrPath.contains("/") || aClassNameOrPath.contains("\\") || aClassNameOrPath.contains(File.separator)) throw e;
                    else throw ex;
                }
            }
            return (Pair)InvokerHelper.invokeConstructorOf(tClazz, aPairPtr);
        }
        
        
        protected final long mPairPtr;
        /**
         * @param aPairPtr lammps jse pair 对应类的指针
         * @author liqa
         */
        protected Pair(long aPairPtr) {mPairPtr = aPairPtr;}
        
        /**
         * 在这里执行主要的 pair 的计算部分
         * @author liqa
         */
        public abstract void compute() throws Exception;
        
        /**
         * lammps {@code pair_coeff} 会调用的方法，用于设置参数
         * @param aArgs 参数的字符串数组
         * @author liqa
         */
        public abstract void coeff(String... aArgs) throws Exception;
        
        /**
         * lammps pair 初始化调用，主要用于在这里设置需要的近邻列表样式
         * @author liqa
         */
        public void initStyle() throws Exception {neighborRequestDefault();}
        
        /**
         * lammps pair 初始化某两个种类 {@code i} {@code j}，用于获取这两个种类间的截断半径值
         * @param i 种类 {@code i}，从 {@code 1} 开始
         * @param j 种类 {@code j}，从 {@code 1} 开始
         * @return 种类 {@code i} {@code j} 之间的截断半径
         * @author liqa
         */
        public abstract double initOne(int i, int j) throws Exception;
        
        /**
         * lammps pair 析构时调用，用于手动释放一些资源
         * @author liqa
         */
        @Override public void shutdown() {/**/}
        
        
        /// lammps pair 提供的接口
        protected final void neighborRequestDefault() {neighborRequestDefault_(mPairPtr);}
        private native static void neighborRequestDefault_(long aPairPtr);
        
        protected final void neighborRequestFull() {neighborRequestFull_(mPairPtr);}
        private native static void neighborRequestFull_(long aPairPtr);
        
        protected final void noVirialFdotrCompute() {noVirialFdotrCompute_(mPairPtr);}
        private native static void noVirialFdotrCompute_(long aPairPtr);
        
        protected final NestedDoubleCPointer atomX() {return new NestedDoubleCPointer(atomX_(mPairPtr));}
        private native static long atomX_(long aPairPtr);
        
        protected final NestedDoubleCPointer atomF() {return new NestedDoubleCPointer(atomF_(mPairPtr));}
        private native static long atomF_(long aPairPtr);
        
        protected final IntCPointer atomType() {return new IntCPointer(atomType_(mPairPtr));}
        private native static long atomType_(long aPairPtr);
        
        protected final int atomNtypes() {return atomNtypes_(mPairPtr);}
        private native static int atomNtypes_(long aPairPtr);
        
        protected final int atomNlocal() {return atomNlocal_(mPairPtr);}
        private native static int atomNlocal_(long aPairPtr);
        
        protected final int atomNghost() {return atomNghost_(mPairPtr);}
        private native static int atomNghost_(long aPairPtr);
        
        protected final DoubleCPointer forceSpecialLj() {return new DoubleCPointer(forceSpecialLj_(mPairPtr));}
        private native static long forceSpecialLj_(long aPairPtr);
        
        protected final boolean forceNewtonPair() {return forceNewtonPair_(mPairPtr);}
        private native static boolean forceNewtonPair_(long aPairPtr);
        
        protected final int listInum() {return listInum_(mPairPtr);}
        private native static int listInum_(long aPairPtr);
        
        protected final IntCPointer listIlist() {return new IntCPointer(listIlist_(mPairPtr));}
        private native static long listIlist_(long aPairPtr);
        
        protected final IntCPointer listNumneigh() {return new IntCPointer(listNumneigh_(mPairPtr));}
        private native static long listNumneigh_(long aPairPtr);
        
        protected final NestedIntCPointer listFirstneigh() {return new NestedIntCPointer(listFirstneigh_(mPairPtr));}
        private native static long listFirstneigh_(long aPairPtr);
        
        protected static int sbmask(int j) {return (j >> SBBITS) & 3;}
        
        protected final double cutsq(int i, int j) {return cutsq_(mPairPtr, i, j);}
        private native static double cutsq_(long aPairPtr, int i, int j);
        
        protected final DoubleCPointer engVdwl() {return new DoubleCPointer(engVdwl_(mPairPtr));}
        private native static long engVdwl_(long aPairPtr);
        
        protected final DoubleCPointer engCoul() {return new DoubleCPointer(engCoul_(mPairPtr));}
        private native static long engCoul_(long aPairPtr);
        
        protected final DoubleCPointer eatom() {return new DoubleCPointer(eatom_(mPairPtr));}
        private native static long eatom_(long aPairPtr);
        
        protected final DoubleCPointer virial() {return new DoubleCPointer(virial_(mPairPtr));}
        private native static long virial_(long aPairPtr);
        
        protected final NestedDoubleCPointer vatom() {return new NestedDoubleCPointer(vatom_(mPairPtr));}
        private native static long vatom_(long aPairPtr);
        
        protected final void evTally(int i, int j, int nlocal, boolean newtonPair, double evdwl, double ecoul, double fpair, double delx, double dely, double delz) {evTally_(mPairPtr, i, j, nlocal, newtonPair, evdwl, ecoul, fpair, delx, dely, delz);}
        private native static void evTally_(long aPairPtr, int i, int j, int nlocal, boolean newtonPair, double evdwl, double ecoul, double fpair, double delx, double dely, double delz);
        
        protected final void evTallyFull(int i, double evdwl, double ecoul, double fpair, double delx, double dely, double delz) {evTallyFull_(mPairPtr, i, evdwl, ecoul, fpair, delx, dely, delz);}
        private native static void evTallyFull_(long aPairPtr, int i, double evdwl, double ecoul, double fpair, double delx, double dely, double delz);
        
        protected final void evTallyXYZ(int i, int j, int nlocal, boolean newtonPair, double evdwl, double ecoul, double fx, double fy, double fz, double delx, double dely, double delz) {evTallyXYZ_(mPairPtr, i, j, nlocal, newtonPair, evdwl, ecoul, fx, fy, fz, delx, dely, delz);}
        private native static void evTallyXYZ_(long aPairPtr, int i, int j, int nlocal, boolean newtonPair, double evdwl, double ecoul, double fx, double fy, double fz, double delx, double dely, double delz);
        
        protected final void evTallyXYZFull(int i, double evdwl, double ecoul, double fx, double fy, double fz, double delx, double dely, double delz) {evTallyXYZFull_(mPairPtr, i, evdwl, ecoul, fx, fy, fz, delx, dely, delz);}
        private native static void evTallyXYZFull_(long aPairPtr, int i, double evdwl, double ecoul, double fx, double fy, double fz, double delx, double dely, double delz);
        
        protected final boolean evflag() {return evflag_(mPairPtr);}
        private native static boolean evflag_(long aPairPtr);
        
        protected final boolean vflagEither() {return vflagEither_(mPairPtr);}
        private native static boolean vflagEither_(long aPairPtr);
        
        protected final boolean vflagGlobal() {return vflagGlobal_(mPairPtr);}
        private native static boolean vflagGlobal_(long aPairPtr);
        
        protected final boolean vflagAtom() {return vflagAtom_(mPairPtr);}
        private native static boolean vflagAtom_(long aPairPtr);
        
        protected final boolean eflagEither() {return eflagEither_(mPairPtr);}
        private native static boolean eflagEither_(long aPairPtr);
        
        protected final boolean eflagGlobal() {return eflagGlobal_(mPairPtr);}
        private native static boolean eflagGlobal_(long aPairPtr);
        
        protected final boolean eflagAtom() {return eflagAtom_(mPairPtr);}
        private native static boolean eflagAtom_(long aPairPtr);
        
        protected final boolean vflagFdotr() {return vflagFdotr_(mPairPtr);}
        private native static boolean vflagFdotr_(long aPairPtr);
        
        protected final void virialFdotrCompute() {virialFdotrCompute_(mPairPtr);}
        private native static void virialFdotrCompute_(long aPairPtr);
        
        protected final int commMe() {return commMe_(mPairPtr);}
        private native static int commMe_(long aPairPtr);
        
        protected final int commNprocs() {return commNprocs_(mPairPtr);}
        private native static int commNprocs_(long aPairPtr);
        
        protected final String unitStyle() {return unitStyle_(mPairPtr);}
        private native static String unitStyle_(long aPairPtr);
    }
}
