package jse.gpu;

import jse.atom.XYZ;
import jse.clib.Compiler;
import jse.clib.JNIUtil;
import jse.clib.NVCC;
import jse.code.IO;
import jse.code.OS;
import jse.code.UT;
import jse.cptr.AnyCPointer;
import jse.cptr.IntCPointer;
import jse.cptr.PointerManager;
import jse.lmp.LmpPlugin;
import jse.math.MathEX;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static jse.code.CS.VERSION_NUMBER;
import static jse.code.Conf.VERSION_MASK;
import static jse.code.OS.JAR_DIR;
import static jse.code.OS.JAVA_HOME;

/**
 * 基于 JNI 调用 cuda 实现的高效近邻列表获取器，用于最大限度提高 GPU 上的效率。
 * <p>
 * 目前实现主要针对 LAMMPS 已有的信息和数据格式设计
 *
 * @author liqa
 */
@ApiStatus.Experimental
public class CudaNeighborListGetter {
    public final static class InitHelper {
        private static volatile boolean INITIALIZED = false;
        /** @return {@link CudaNeighborListGetter} 相关的 JNI 库是否已经初始化完成 */
        public static boolean initialized() {return INITIALIZED;}
        /** 初始化 {@link CudaNeighborListGetter} 相关的 JNI 库 */
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public static void init() {
            if (!INITIALIZED) String.valueOf(INIT_FLAG_);
        }
    }
    
    public final static class Conf {
        /**
         * 自定义 cudanl 中使用的 block_size 值，这可能会影响速度；
         * 默认为 {@code 256}
         */
        public static int BLOCKSIZE = OS.envI("JSE_CUDANL_BLOCKSIZE", 256);
        
        /**
         * 自定义构建 cudanl 的 cmake 参数设置，
         * 会在构建时使用 -D ${key}=${value} 传入
         */
        public final static Map<String, String> CMAKE_SETTING = OS.envMap("JSE_CMAKE_SETTING_CUDANL");
        
        /**
         * 自定义构建 cudanl 时使用的编译器，
         * cmake 有时不能自动检测到希望使用的编译器
         * <p>
         * 也可使用环境变量 {@code JSE_CMAKE_CXX_COMPILER_CUDANL} 来设置
         */
        public static @Nullable String CMAKE_CXX_COMPILER = OS.env("JSE_CMAKE_CXX_COMPILER_CUDANL", jse.code.Conf.CMAKE_CXX_COMPILER);
        /**
         * 自定义构建 cudanl 时使用的编译器，
         * cmake 有时不能自动检测到希望使用的编译器
         * <p>
         * 也可使用环境变量 {@code JSE_CMAKE_CXX_FLAGS_CUDANL} 来设置
         */
        public static @Nullable String CMAKE_CXX_FLAGS = OS.env("JSE_CMAKE_CXX_FLAGS_CUDANL", jse.code.Conf.CMAKE_CXX_FLAGS);
        /**
         * 自定义构建 cudanl 时使用的编译器，
         * cmake 有时不能自动检测到希望使用的编译器
         * <p>
         * 也可使用环境变量 {@code JSE_CMAKE_CUDA_COMPILER_CUDANL} 来设置
         */
        public static @Nullable String CMAKE_CUDA_COMPILER = OS.env("JSE_CMAKE_CUDA_COMPILER_CUDANL");
        /**
         * 自定义构建 cudanl 时使用的编译器，
         * cmake 有时不能自动检测到希望使用的编译器
         * <p>
         * 也可使用环境变量 {@code JSE_CMAKE_CUDA_FLAGS_CUDANL} 来设置
         */
        public static @Nullable String CMAKE_CUDA_FLAGS = OS.env("JSE_CMAKE_CUDA_FLAGS_CUDANL");
        /**
         * 自定义构建 cudanl 时的 cuda 架构，用于覆盖默认的 75
         * <p>
         * 也可使用环境变量 {@code JSE_CMAKE_CUDA_ARCHITECTURES_CUDANL} 来设置
         */
        public static @Nullable String CMAKE_CUDA_ARCHITECTURES = OS.env("JSE_CMAKE_CUDA_ARCHITECTURES_CUDANL");
        
        /**
         * cudanl 是否开启 debug 模式
         * <p>
         * 也可使用环境变量 {@code JSE_DEBUG_CUDANL} 来设置
         */
        public static boolean DEBUG = OS.envZ("JSE_DEBUG_CUDANL", jse.code.Conf.DEBUG);
    }
    
    /** 当前 {@link CudaNeighborListGetter} JNI 库所在的文件夹路径，结尾一定存在 {@code '/'} */
    public final static String LIB_DIR = JAR_DIR+"gpu/nl/" +
        UT.Code.uniqueID(OS.OS_NAME, Compiler.EXE_PATH, NVCC.EXE_PATH, JAVA_HOME, VERSION_NUMBER, VERSION_MASK,
                         Conf.CMAKE_CXX_COMPILER, Conf.CMAKE_CXX_FLAGS, Conf.CMAKE_CUDA_COMPILER, Conf.CMAKE_CUDA_FLAGS,
                         Conf.CMAKE_CUDA_ARCHITECTURES, Conf.CMAKE_SETTING) + "/";
    /** 当前 {@link CudaNeighborListGetter} JNI 库的路径 */
    public final static String LIB_PATH;
    private final static String[] SRC_NAME = {
          "jse_gpu_CudaNeighborListGetter.cu"
        , "jse_gpu_CudaNeighborListGetter.h"
    };
    
    private static final boolean INIT_FLAG_;
    static {
        InitHelper.INITIALIZED = true;
        INIT_FLAG_ = true;
        // 依赖 CudaCore
        CudaCore.InitHelper.init();
        
        Map<String, String> rCmakeSetting = new LinkedHashMap<>(Conf.CMAKE_SETTING);
        if (Conf.DEBUG) rCmakeSetting.put("JSE_DEBUG_MODE", "ON");
        LIB_PATH = new JNIUtil.LibBuilder("cudanl", "CUDA_NL", LIB_DIR, rCmakeSetting)
            .setSrc("cudanl", SRC_NAME)
            .setEnvChecker(NVCC::printInfo) // 在这里输出 nvcc 信息，保证只在第一次构建时输出一次；可能存在和 cmake 检测不一致的问题
            .setCmakeCxxCompiler(Conf.CMAKE_CXX_COMPILER).setCmakeCxxFlags(Conf.CMAKE_CXX_FLAGS)
            .setCmakeCudaCompiler(Conf.CMAKE_CUDA_COMPILER).setCmakeCudaFlags(Conf.CMAKE_CUDA_FLAGS)
            .setCmakeCudaArch(Conf.CMAKE_CUDA_ARCHITECTURES)
            .get();
        // 设置库路径，这里直接使用 System.load
        System.load(IO.toAbsolutePath(LIB_PATH));
    }
    
    /// OOP sutffs
    final double mRCut, mRCutSq;
    final PointerManager mPtrMng;
    private final IntCudaPointer mErrorGpu;
    private final IntCPointer mErrorCpu;
    private final CudaPointer mCells;
    private final AnyCPointer mCellsCpu;
    private final IntCudaPointer mCellTot, mCellSize;
    private int mLocalCellCapacity = -1, mGhostCellCapacity = -1;
    
    
    public CudaNeighborListGetter(double aRCut) throws CudaException {
        mRCut = aRCut;
        mRCutSq = aRCut*aRCut;
        mPtrMng = new PointerManager();
        
        mErrorGpu = mPtrMng.newIntCudaPointer(1);
        mErrorCpu = mPtrMng.newIntCPointer(1);
        mCells = mPtrMng.newCudaPointer(0);
        mCellsCpu = mPtrMng.newAnyCPointer();
        mCellTot = mPtrMng.newIntCudaPointer();
        mCellSize = mPtrMng.newIntCudaPointer();
    }
    public final static int MAX_SLICE = 256;
    private int mSliceX = 0, mSliceY = 0, mSliceZ = 0;
    private  boolean mPrism = false;
    private final XYZ mA = new XYZ(), mB = new XYZ(), mC = new XYZ();
    private final XYZ mBC = new XYZ(), mCA = new XYZ(), mAB = new XYZ();
    
    void initBox(double ax, double ay, double az,
                 double bx, double by, double bz,
                 double cx, double cy, double cz) {
        mPrism = true;
        mA.setXYZ(ax, ay, az);
        mB.setXYZ(bx, by, bz);
        mC.setXYZ(cx, cy, cz);
        
        mB.cross2dest(mC, mBC);
        mC.cross2dest(mA, mCA);
        mA.cross2dest(mB, mAB);
        double mPx = mA.dot(mBC) / mBC.norm();
        double mPy = mB.dot(mCA) / mCA.norm();
        double mPz = mC.dot(mAB) / mAB.norm();
        
        mSliceX = MathEX.Code.toRange(1, MAX_SLICE, MathEX.Code.floor2int(mPx/mRCut));
        mSliceY = MathEX.Code.toRange(1, MAX_SLICE, MathEX.Code.floor2int(mPy/mRCut));
        mSliceZ = MathEX.Code.toRange(1, MAX_SLICE, MathEX.Code.floor2int(mPz/mRCut));
    }
    void initBox(double x, double y, double z) {
        mPrism = false;
        mA.setXYZ(x, 0, 0);
        mB.setXYZ(0, y, 0);
        mC.setXYZ(0, 0, z);
        
        mSliceX = MathEX.Code.toRange(1, MAX_SLICE, MathEX.Code.floor2int(x/mRCut));
        mSliceY = MathEX.Code.toRange(1, MAX_SLICE, MathEX.Code.floor2int(y/mRCut));
        mSliceZ = MathEX.Code.toRange(1, MAX_SLICE, MathEX.Code.floor2int(z/mRCut));
    }
    
    void initCells(int nlocal, int nghost) throws CudaException {
        final int tCellCount = (mSliceX+2)*(mSliceY+2)*(mSliceZ+2);
        final int tLocalCellCount = mSliceX*mSliceY*mSliceZ;
        final int tGhostCellCount = tCellCount-tLocalCellCount;
        final int tLocalCap = MathEX.Code.ceil2int(nlocal / (double)tLocalCellCount * 1.25);
        final int tGhostCap = MathEX.Code.ceil2int(nghost / (double)tGhostCellCount * 1.25);
        if (tLocalCap>mLocalCellCapacity || tGhostCap>mGhostCellCapacity) {
            mLocalCellCapacity = tLocalCap;
            mGhostCellCapacity = tLocalCap;
            mPtrMng.ensureCapacity(mCellTot, (long)tLocalCellCount*tLocalCap + (long)tGhostCellCount*tGhostCap, false);
        }
        mPtrMng.ensureCapacity(mCellSize, tCellCount);
        mPtrMng.ensureCapacity(mCells, tCellCount*AnyCPointer.TYPE_SIZE);
        mPtrMng.ensureCapacity(mCellsCpu, tCellCount);
        int tCode = initCells0(
            mSliceX, mSliceY, mSliceZ,
            mCellTot.ptr_(), mCells.ptr_(), mCellsCpu.ptr_(),
            mLocalCellCapacity, mGhostCellCapacity
        );
        CudaCore.cudaExceptionCheck(tCode);
    }
    
    void buildCells(int nlocal, int nghost) throws CudaException {
        int tCode = buildCells0(
            Conf.BLOCKSIZE, nlocal, nghost, mPrism, mA.mX, mA.mY, mA.mZ,
            mB.mX, mB.mY, mB.mZ, mC.mX, mC.mY, mC.mZ,
            mXlo, mYlo, mZlo, posX, posY, posZ,
            mSliceX, mSliceY, mSliceZ, mCells.ptr_(), mCellSize.ptr_(), mLocalCellCapacity, mGhostCellCapacity,
            mErrorGpu, mErrorCpu
        );
        CudaCore.cudaExceptionCheck(tCode);
        int tError = mErrorCpu.get();
        if (tError != 0) throw new IllegalStateException("error: " + tError);
    }
    
    
    public void build(LmpPlugin.Pair aPair) throws CudaException {
        final int nlocal = aPair.atomNlocal();
        final int nghost = aPair.atomNghost();
        
        initBox();
        initCells(nlocal, nghost);
        buildCells(nlocal, nghost);
    }
    
    
    private static native int initCells0(
        int sliceX, int sliceY, int sliceZ, long cellsTot, long cells, long cellsCpu,
        int localCellCapacity, int ghostCellCapacity);
    
    private static native int buildCells0(
        int aBlockSize, int nlocal, int nghost, boolean aPrism, float ax, float ay, float az,
        float bx, float by, float bz, float cx, float cy, float cz,
        float xlo, float ylo, float zlo, long posX, long posY, long posZ,
        int sliceX, int sliceY, int sliceZ, long cells, long cellSize, int localCellCapacity, int ghostCellCapacity,
        long errorGpu, long errorCpu);
    
    private static native int buildNl0(
        int aBlockSize, int nlocal, boolean aPrism, float ax, float ay, float az,
        float bx, float by, float bz, float cx, float cy, float cz,
        float xlo, float ylo, float zlo, long posX, long posY, long posZ,
        int sliceX, int sliceY, int sliceZ, long cells, long cellSize,
        float rcutsq, long nl, long nlSize, int nlCapacity,
        long errorGpu, long errorCpu);
}
