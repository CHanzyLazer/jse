package jtool.parallel;

import jtool.code.UT;
import org.jetbrains.annotations.ApiStatus;

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
 * 为了保证接口简洁，这里不再返回错误码，并且暂时不进行错误抛出；
 * 为了避免错误并且加速上线，这里不实现我还没理解的功能
 * <p>
 * 使用方法：
 * <pre> {@code
 * import static jtool.parallel.MPI.Native.*;
 *
 * MPI_Init(args);
 * int me = MPI_Comm_rank(MPI_COMM_WORLD);
 * System.out.println("Hi from <"+me+">");
 * MPI_Finalize();
 * } </pre>
 * <p>
 * 或者更加 java 风格的使用：
 * <pre> {@code
 * import jtool.parallel.MPI;
 *
 * MPI.init(args);
 * int me = MPI.Comm.WORLD.rank();
 * System.out.println("Hi from <"+me+">");
 * MPI.shutdown(); // "finalize()" has been used in java
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
@ApiStatus.Experimental
public class MPI {
    private MPI() {}
    
    private final static String MPILIB_DIR = JAR_DIR+"mpi/";
    private final static String MPILIB_PATH = MPILIB_DIR + (IS_WINDOWS ? "mpi.dll" : "mpi.so");
    
    public enum Comm {
          NULL (Native.MPI_COMM_NULL )
        , WORLD(Native.MPI_COMM_WORLD)
        , SELF (Native.MPI_COMM_SELF )
        ;
        
        private final long mPtr;
        Comm(long aPtr) {mPtr = aPtr;}
        
        /** @return the number of calling process within the group of the communicator. */
        public int rank() {return Native.MPI_Comm_rank(mPtr);}
        /** @return the number of processes in the group for the communicator. */
        public int size() {return Native.MPI_Comm_size(mPtr);}
        
        
        /// MPI Collective Functions
        public <R, S> void allgather(S aSendBuf, int aSendCount, R rRecvBuf, int aRecvCount) {
            Native.MPI_Allgather(aSendBuf, aSendCount, rRecvBuf, aRecvCount, mPtr);
        }
    }
    
    public enum Datatype {
          SIGNED_CHAR   (Native.MPI_SIGNED_CHAR   )
        , DOUBLE        (Native.MPI_DOUBLE        )
        , UNSIGNED_CHAR (Native.MPI_UNSIGNED_CHAR )
        , UNSIGNED_SHORT(Native.MPI_UNSIGNED_SHORT)
        , SHORT         (Native.MPI_SHORT         )
        , INT32_T       (Native.MPI_INT32_T       )
        , INT64_T       (Native.MPI_INT64_T       )
        , FLOAT         (Native.MPI_FLOAT         )
        ;
        
        private final long mPtr;
        Datatype(long aPtr) {mPtr = aPtr;}
    }
    
    /// 基础功能
    /**
     * Initializes the calling MPI process’s execution environment for single threaded execution.
     * @param aArgs the argument list for the program
     */
    public static void init(String[] aArgs) {Native.MPI_Init(aArgs);}
    /**
     * Indicates whether {@link MPI#init} has been called.
     * @return true if {@link MPI#init} or {@link MPI#initThread} has been called and false otherwise.
     */
    public static boolean initialized() {return Native.MPI_Initialized();}
    
    /**
     * Terminates the calling MPI process’s execution environment.
     */
    public static void shutdown() {Native.MPI_Finalize();}
    /**
     * Indicates whether {@link MPI#shutdown} has been called.
     * @return true if MPI_Finalize has been called and false otherwise.
     */
    public static boolean isShutdown() {return Native.MPI_Finalized();}
    
    
    
    /**
     * 提供按照原始的 MPI 标准格式的接口以及对应的 native 实现
     * @author liqa
     */
    public static class Native {
        private Native() {}
        
        // 直接进行初始化，虽然原则上会在 MPI_Init() 之前获取，
        // 但是得到的是 final 值，可以避免意外的修改，并且简化代码；
        // 这对于一般的 MPI 实现应该都是没有问题的
        static {
            // 检测 jni lib 以及编译相关操作
            System.load(UT.IO.toAbsolutePath(MPILIB_PATH));
            
            // 初始化 final 常量
            MPI_COMM_NULL  = getMpiCommNull_();
            MPI_COMM_WORLD = getMpiCommWorld_();
            MPI_COMM_SELF  = getMpiCommSelf_();
            
            MPI_DATATYPE_NULL      = getMpiDatatypeNull_();
            MPI_CHAR               = getMpiChar_();
            MPI_UNSIGNED_CHAR      = getMpiUnsignedChar_();
            MPI_SHORT              = getMpiShort_();
            MPI_UNSIGNED_SHORT     = getMpiUnsignedShort_();
            MPI_INT                = getMpiInt_();
            MPI_UNSIGNED           = getMpiUnsigned_();
            MPI_LONG               = getMpiLong_();
            MPI_UNSIGNED_LONG      = getMpiUnsignedLong_();
            MPI_LONG_LONG          = getMpiLongLong_();
            MPI_FLOAT              = getMpiFloat_();
            MPI_DOUBLE             = getMpiDouble_();
            MPI_BYTE               = getMpiByte_();
            MPI_SIGNED_CHAR        = getMpiSignedChar_();
            MPI_UNSIGNED_LONG_LONG = getMpiUnsignedLongLong_();
            MPI_INT8_T             = getMpiInt8T_();
            MPI_INT16_T            = getMpiInt16T_();
            MPI_INT32_T            = getMpiInt32T_();
            MPI_INT64_T            = getMpiInt64T_();
            MPI_UINT8_T            = getMpiUint8T_();
            MPI_UINT16_T           = getMpiUint16T_();
            MPI_UINT32_T           = getMpiUint32T_();
            MPI_UINT64_T           = getMpiUint64T_();
        }
        
        public final static long MPI_COMM_NULL, MPI_COMM_WORLD, MPI_COMM_SELF;
        private native static long getMpiCommNull_();
        private native static long getMpiCommWorld_();
        private native static long getMpiCommSelf_();
        
        // 只保留部分必要的，因为 MPI 实现中都不尽相同
        public final static long MPI_DATATYPE_NULL, MPI_CHAR, MPI_UNSIGNED_CHAR, MPI_SHORT, MPI_UNSIGNED_SHORT, MPI_INT, MPI_UNSIGNED, MPI_LONG, MPI_UNSIGNED_LONG, MPI_LONG_LONG, MPI_FLOAT, MPI_DOUBLE, MPI_BYTE, MPI_SIGNED_CHAR, MPI_UNSIGNED_LONG_LONG, MPI_INT8_T, MPI_INT16_T, MPI_INT32_T, MPI_INT64_T, MPI_UINT8_T, MPI_UINT16_T, MPI_UINT32_T, MPI_UINT64_T;
        private native static long getMpiDatatypeNull_();
        private native static long getMpiChar_();
        private native static long getMpiUnsignedChar_();
        private native static long getMpiShort_();
        private native static long getMpiUnsignedShort_();
        private native static long getMpiInt_();
        private native static long getMpiUnsigned_();
        private native static long getMpiLong_();
        private native static long getMpiUnsignedLong_();
        private native static long getMpiLongLong_();
        private native static long getMpiFloat_();
        private native static long getMpiDouble_();
        private native static long getMpiByte_();
        private native static long getMpiSignedChar_();
        private native static long getMpiUnsignedLongLong_();
        private native static long getMpiInt8T_();
        private native static long getMpiInt16T_();
        private native static long getMpiInt32T_();
        private native static long getMpiInt64T_();
        private native static long getMpiUint8T_();
        private native static long getMpiUint16T_();
        private native static long getMpiUint32T_();
        private native static long getMpiUint64T_();
        
        private static long datatypeOf_(Object aBuf) {
            if (aBuf instanceof byte[]) {
                return MPI_SIGNED_CHAR;
            } else
            if (aBuf instanceof double[]) {
                return MPI_DOUBLE;
            } else
            if (aBuf instanceof boolean[]) {
                return MPI_UNSIGNED_CHAR;
            } else
            if (aBuf instanceof char[]) {
                return MPI_UNSIGNED_SHORT;
            } else
            if (aBuf instanceof short[]) {
                return MPI_SHORT;
            } else
            if (aBuf instanceof int[]) {
                return MPI_INT32_T;
            } else
            if (aBuf instanceof long[]) {
                return MPI_INT64_T;
            } else
            if (aBuf instanceof float[]) {
                return MPI_FLOAT;
            } else {
                throw new RuntimeException("Unexpected datatype: "+aBuf.getClass().getName());
            }
        }
        
        
        /// 基础功能
        /**
         * Initializes the calling MPI process’s execution environment for single threaded execution.
         * @param aArgs the argument list for the program
         */
        public native static void MPI_Init(String[] aArgs);
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
        public native static int MPI_Comm_rank(long aComm);
        
        /**
         * Retrieves the number of processes involved in a communicator, or the total number of
         * processes available.
         * @param aComm The communicator to evaluate. Specify the {@link #MPI_COMM_WORLD} constant to retrieve
         *              the total number of processes available.
         * @return the number of processes in the group for the communicator.
         */
        public native static int MPI_Comm_size(long aComm);
        
        /**
         * Terminates the calling MPI process’s execution environment.
         */
        public native static void MPI_Finalize();
        /**
         * Indicates whether {@link #MPI_Finalize} has been called.
         * @return true if MPI_Finalize has been called and false otherwise.
         */
        public native static boolean MPI_Finalized();
        
        
        
        /// MPI Collective Functions
        public static <R, S> void MPI_Allgather(S aSendBuf, int aSendCount, R rRecvBuf, int aRecvCount, long aComm) {
            MPI_Allgather0(aSendBuf, aSendCount, datatypeOf_(aSendBuf), rRecvBuf, aRecvCount, datatypeOf_(rRecvBuf), aComm);
        }
        private native static void MPI_Allgather0(Object aSendBuf, int aSendCount, long aSendType, Object rRecvBuf, int aRecvCount, long aRecvType, long aComm);
        
        
        
//        /// MPI Caching Functions
//        @FunctionalInterface public interface MPI_Comm_copy_attr_function {
//            /**
//             * a placeholder for the application-defined function name.
//             * @param aOldComm Original communicator.
//             * @param aCommKeyval Key value.
//             * @param aExtraState Extra state.
//             * @param aAttributeValIn Source attribute value.
//             * @param aAttributeValOut Destination attribute value.
//             * @return if false, then the attribute is deleted in the duplicated communicator.
//             * Otherwise (true), the new attribute value is set to the value returned in
//             * aAttributeValOut.
//             */
//            boolean call(MPI_Comm aOldComm, int aCommKeyval, Object aExtraState, Object aAttributeValIn, Object aAttributeValOut);
//        }
//
//        @FunctionalInterface public interface MPI_Comm_delete_attr_function {
//            /**
//             * a placeholder for the application-defined function name.
//             * @param aComm Communicator.
//             * @param aCommKeyval Key value.
//             * @param aAttributeVal Attribute value.
//             * @param aExtraState Extra state.
//             */
//            void call(MPI_Comm aComm, int aCommKeyval, Object aAttributeVal, Object aExtraState);
//        }
//
//        /**
//         * Creates a new attribute key.
//         * @param aCommCopyAttrFn Copy callback function for keyval.
//         * @param aCommDeleteAttrFn Delete callback function for keyval.
//         * @param aExtraState Extra state for callback functions.
//         * @return Key value for future access.
//         */
//        public native static int MPI_Comm_create_keyval(MPI_Comm_copy_attr_function aCommCopyAttrFn, MPI_Comm_delete_attr_function aCommDeleteAttrFn, Object aExtraState);
    }
}
