package jtool.lmp;

import jtool.code.UT;
import jtool.parallel.IAutoShutdown;
import jtool.parallel.MPI;

import static jtool.code.CS.Exec.JAR_DIR;
import static jtool.code.CS.IS_MAC;
import static jtool.code.CS.IS_WINDOWS;

/**
 * 基于 jni 的调用本地原生 lammps 的类，
 * 使用类似 python 调用 lammps 的结构，
 * 使用 {@link MPI} 实现并行。
 * <p>
 * References:
 * <a href="https://docs.lammps.org/Python_module.html">
 * The lammps Python module </a>,
 * <a href="https://docs.lammps.org/Library.html/">
 * LAMMPS Library Interfaces </a>,
 * @author liqa
 */
public class NativeLmp implements IAutoShutdown {
    private final static String LMPLIB_DIR = JAR_DIR+"lmp/";
    private final static String LMPLIB_PATH = LMPLIB_DIR + (IS_WINDOWS ? "lmp.dll" : (IS_MAC ? "lmp.jnilib" : "lmp.so"));
    
    /**
     * 使用这个子类来进行一些配置参数的设置，
     * 使用子类的成员不会触发 {@link NativeLmp} 的静态初始化
     */
    public static class Conf {
        private Conf() {}
        public static String LMP_HOME = null;
    }
    
    
    static {
        if (Conf.LMP_HOME == null) Conf.LMP_HOME = "G:\\CHanzy\\project\\lammps-stable_2Aug2023_update2\\build\\";
        if (IS_WINDOWS) System.load(UT.IO.toAbsolutePath(Conf.LMP_HOME+"lib/liblammps.dll"));
        // 设置库路径
        System.load(UT.IO.toAbsolutePath(LMPLIB_PATH));
    }
    
    
    private final long mLmpPtr;
    /**
     * Create an instance of the LAMMPS Java class.
     * <p>
     * This is a Java wrapper class that exposes the LAMMPS C-library interface to Java.
     * It either requires that LAMMPS has been compiled as shared library which is then
     * dynamically loaded via the jni, for example through the java {@code <init>} method.
     * When the class is instantiated it calls the {@code lammps_open()} function of the LAMMPS
     * C-library interface, which in turn will create an instance of the LAMMPS C++ class.
     * The handle to this C++ class is stored internally and automatically passed to the
     * calls to the C library interface.
     *
     * @param aArgs  array of command line arguments to be passed to the {@code lammps_open()}
     *               (or {@code lammps_open_no_mpi()} when no mpi) function.
     *               The executable name is automatically added.
     *
     * @param aComm MPI communicator as provided by {@link MPI} (or {@link MPI.Native}).
     *              null (or 0) means use {@link MPI.Comm#WORLD} implicitly.
     *
     * @param aPtr pointer to a LAMMPS C++ class instance when called from an embedded Python interpreter.
     *             0 means load symbols from shared library.
     *
     * @author liqa
     */
    public NativeLmp(String[] aArgs, long aComm, long aPtr) {
        mLmpPtr = aComm==0 ? lammpsOpen_(aArgs, aPtr) : lammpsOpen_(aArgs, aComm, aPtr);
    }
    public NativeLmp(String[] aArgs, long aComm) {this(aArgs, aComm, 0);}
    public NativeLmp(String[] aArgs) {this(aArgs, 0);}
    public NativeLmp(String[] aArgs, MPI.Comm aComm, long aPtr) {this(aArgs, aComm==null ? 0 : aComm.ptr_(), aPtr);}
    public NativeLmp(String[] aArgs, MPI.Comm aComm) {this(aArgs, aComm, 0);}
    private native static long lammpsOpen_(String[] aArgs, long aComm, long aPtr);
    private native static long lammpsOpen_(String[] aArgs, long aPtr);
    
    /**
     * Return a numerical representation of the LAMMPS version in use.
     * <p>
     * This is a wrapper around the {@code lammps_version()} function of the C-library interface.
     * @return version number
     */
    public int version() {
        return lammpsVersion_(mLmpPtr);
    }
    private native static int lammpsVersion_(long aLmpPtr);
    
    
    /**
     * Explicitly delete a LAMMPS instance through the C-library interface.
     * <p>
     * This is a wrapper around the {@code lammps_close()} function of the C-library interface.
     */
    public void shutdown() {
        lammpsClose_(mLmpPtr);
    }
    private native static void lammpsClose_(long aLmpPtr);
}
