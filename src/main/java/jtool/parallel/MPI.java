package jtool.parallel;

import jtool.code.UT;

import static jtool.code.CS.Exec.JAR_DIR;
import static jtool.code.CS.IS_WINDOWS;

/**
 * 基于 jni 实现的 MPI wrapper, 介绍部分基于
 * <a href="https://learn.microsoft.com/zh-cn/message-passing-interface/microsoft-mpi">
 * Microsoft MPI </a> 的标准；
 * 所有函数名称按照原始的 MPI 标准而不是流行的 java binding 中使用的标准，
 * 从而保证对 c 风格的 MPI 有更好的一致性；
 * 在此基础上提供一套完全按照 java 风格的接口来方便使用（而不是原本的不伦不类的风格）。
 * <p>
 * 为了保证接口简洁，这里不再返回错误码，并且暂时不进行错误抛出
 * <p>
 * 使用：
 * <pre> {@code
 * import static jtool.parallel.MPI.*;
 *
 * MPI_Init(args);
 * int me = MPI_Comm_rank(MPI_COMM_WORLD);
 * System.out.println("Hi from <"+me+">");
 * MPI_Finalize();
 * } </pre>
 * <p>
 * References:
 * <a href="https://docs.open-mpi.org/en/v5.0.x/features/java.html">
 * Open MPI Java bindings </a>,
 * <a href="http://www.mpjexpress.org/">
 * MPJ Express Project </a>,
 * <a href="https://www.mpi-forum.org/docs/mpi-3.1/mpi31-report.pdf">
 * MPI: A Message-Passing Interface Standard Version 3.1 </a>
 * @author liqa
 */
public class MPI {
    private MPI() {}
    
    private final static String MPILIB_DIR = JAR_DIR+"mpi/";
    private final static String MPILIB_PATH = MPILIB_DIR + (IS_WINDOWS ? "mpi.dll" : "mpi.so");
    
    public final static class MPI_Comm {
        private final long mPointer;
        MPI_Comm(long aPointer) {mPointer = aPointer;}
        
        /** @return the number of calling process within the group of the communicator. */
        public int getRank() {return MPI_Comm_rank0(mPointer);}
        /** @return the number of processes in the group for the communicator. */
        public int getSize() {return MPI_Comm_size0(mPointer);}
    }
    public static MPI_Comm MPI_COMM_WORLD, MPI_COMM_NULL, MPI_COMM_SELF;
    
    private native static long getMpiCommWorld0();
    private native static long getMpiCommNull0();
    private native static long getMpiCommSelf0();
    
    
    
    /// 基础功能
    /**
     * Initializes the calling MPI process’s execution environment for single threaded execution.
     * @param aArgs the argument list for the program
     */
    public static void MPI_Init(String[] aArgs) {
        // 检测 jni lib 以及编译相关操作
        System.load(UT.IO.toAbsolutePath(MPILIB_PATH));
        
        MPI_Init0(aArgs);
        
        // 在初始化后再获取 Comm，避免意料外的问题
        MPI_COMM_WORLD = new MPI_Comm(getMpiCommWorld0());
        MPI_COMM_NULL = new MPI_Comm(getMpiCommNull0());
        MPI_COMM_SELF = new MPI_Comm(getMpiCommSelf0());
    }
    private native static void MPI_Init0(String[] aArgs);
    
    /**
     * Indicates whether {@link #MPI_Init} has been called.
     * @return true if {@link #MPI_Init} or {@link #MPI_Init_thread} has been called and false otherwise.
     */
    public native static boolean MPI_Initialized();
    
    /**
     * Retrieves the rank of the calling process in the group of the specified communicator.
     * @param aComm The communicator.
     * @return the number of calling process within the group of the communicator.
     */
    public static int MPI_Comm_rank(MPI_Comm aComm) {return MPI_Comm_rank0(aComm.mPointer);}
    public native static int MPI_Comm_rank0(long aPointer);
    
    /**
     * Retrieves the number of processes involved in a communicator, or the total number of
     * processes available.
     * @param aComm The communicator to evaluate. Specify the {@link #MPI_COMM_WORLD} constant to retrieve
     *              the total number of processes available.
     * @return the number of processes in the group for the communicator.
     */
    public static int MPI_Comm_size(MPI_Comm aComm) {return MPI_Comm_size0(aComm.mPointer);}
    public native static int MPI_Comm_size0(long aPointer);
    
    /**
     * Terminates the calling MPI process’s execution environment.
     */
    public native static void MPI_Finalize();
    /**
     * Indicates whether {@link #MPI_Finalize} has been called.
     * @return true if MPI_Finalize has been called and false otherwise.
     */
    public native static boolean MPI_Finalized();
    
    
    
    /// MPI Caching Functions
    
}
