package jsex.nnap.basis;

import com.google.common.collect.Lists;
import jse.cache.VectorCache;
import jse.clib.JNIUtil;
import jse.code.CS;
import jse.code.IO;
import jse.code.OS;
import jse.code.UT;
import jse.code.collection.DoubleList;
import jse.math.matrix.RowMatrix;
import jse.math.vector.ShiftVector;
import jse.math.vector.Vector;
import jsex.nnap.NNAP;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static jse.code.OS.JAR_DIR;

/**
 * 使用 JNI 来调用 c 来加速部分运算的 {@link SphericalChebyshev}
 *
 * @see SphericalChebyshev
 * @author liqa
 */
public class SphericalChebyshevNative extends SphericalChebyshev {
    /** 用于判断是否进行了静态初始化以及方便的手动初始化 */
    public final static class InitHelper {
        private static volatile boolean INITIALIZED = false;
        
        public static boolean initialized() {return INITIALIZED;}
        @SuppressWarnings({"ResultOfMethodCallIgnored", "UnnecessaryCallToStringValueOf"})
        public static void init() {
            // 手动调用此值来强制初始化
            if (!INITIALIZED) String.valueOf(LIB_PATH);
        }
    }
    
    public final static class Conf {
        /**
         * 自定义构建 native nnap basis 的 cmake 参数设置，
         * 会在构建时使用 -D ${key}=${value} 传入
         */
        public final static Map<String, String> CMAKE_SETTING = new LinkedHashMap<>();
        
        public static final int NONE = -1;
        public static final int COMPAT = 0;
        public static final int BASE = 1;
        public static final int MAX = 2;
        /**
         * 自定义 native nnap basis 需要采用的优化等级，默认为 1（基础优化），
         * 会开启 AVX2 指令集，在大多数现代处理器上能兼容运行
         */
        public static int OPT_LEVEL = OS.envI("JSE_NNAPBASIS_OPT_LEVEL", BASE);
        
        /**
         * 自定义构建 native nnap basis 时使用的编译器，
         * cmake 有时不能自动检测到希望使用的编译器
         */
        public static @Nullable String CMAKE_C_COMPILER   = OS.env("JSE_CMAKE_C_COMPILER_NNAPBASIS"  , jse.code.Conf.CMAKE_C_COMPILER);
        public static @Nullable String CMAKE_C_FLAGS      = OS.env("JSE_CMAKE_C_FLAGS_NNAPBASIS"     , jse.code.Conf.CMAKE_C_FLAGS);
        public static @Nullable String CMAKE_CXX_COMPILER = OS.env("JSE_CMAKE_CXX_COMPILER_NNAPBASIS", jse.code.Conf.CMAKE_CXX_COMPILER);
        public static @Nullable String CMAKE_CXX_FLAGS    = OS.env("JSE_CMAKE_CXX_FLAGS_NNAPBASIS"   , jse.code.Conf.CMAKE_CXX_FLAGS);
        
        /** 重定向 native nnap basis 动态库的路径 */
        public static @Nullable String REDIRECT_NNAPBASIS_LIB = OS.env("JSE_REDIRECT_NNAPBASIS_LIB");
    }
    
    public final static String LIB_DIR = JAR_DIR+"nnap/basis/" + UT.Code.uniqueID(CS.VERSION, NNAP.VERSION, Conf.OPT_LEVEL, Conf.CMAKE_C_COMPILER, Conf.CMAKE_C_FLAGS, Conf.CMAKE_CXX_COMPILER, Conf.CMAKE_CXX_FLAGS, Conf.CMAKE_SETTING) + "/";
    public final static String LIB_PATH;
    private final static String[] SRC_NAME = {
          "jsex_nnap_basis_SphericalChebyshevNative.c"
        , "jsex_nnap_basis_SphericalChebyshevNative.h"
    };
    
    static {
        InitHelper.INITIALIZED = true;
        // 不直接依赖 nnap
        
        // 先添加 Conf.CMAKE_SETTING，这样保证确定的优先级
        Map<String, String> rCmakeSetting = new LinkedHashMap<>(Conf.CMAKE_SETTING);
        switch(Conf.OPT_LEVEL) {
        case Conf.MAX: {
            rCmakeSetting.put("JSE_OPT_MAX",    "ON");
            rCmakeSetting.put("JSE_OPT_BASE",   "OFF");
            rCmakeSetting.put("JSE_OPT_COMPAT", "OFF");
            break;
        }
        case Conf.BASE: {
            rCmakeSetting.put("JSE_OPT_MAX",    "OFF");
            rCmakeSetting.put("JSE_OPT_BASE",   "ON");
            rCmakeSetting.put("JSE_OPT_COMPAT", "OFF");
            break;
        }
        case Conf.COMPAT: {
            rCmakeSetting.put("JSE_OPT_MAX",    "OFF");
            rCmakeSetting.put("JSE_OPT_BASE",   "OFF");
            rCmakeSetting.put("JSE_OPT_COMPAT", "ON");
            break;
        }
        case Conf.NONE: {
            rCmakeSetting.put("JSE_OPT_MAX",    "OFF");
            rCmakeSetting.put("JSE_OPT_BASE",   "OFF");
            rCmakeSetting.put("JSE_OPT_COMPAT", "OFF");
            break;
        }}
        // 现在直接使用 JNIUtil.buildLib 来统一初始化
        LIB_PATH = new JNIUtil.LibBuilder("nnapbasis", "NATIVE_NNAP_BASIS", LIB_DIR, rCmakeSetting)
            .setSrc("nnap/basis", SRC_NAME)
            .setCmakeCCompiler(Conf.CMAKE_C_COMPILER).setCmakeCFlags(Conf.CMAKE_C_FLAGS)
            .setCmakeCxxCompiler(Conf.CMAKE_CXX_COMPILER).setCmakeCxxFlags(Conf.CMAKE_CXX_FLAGS)
            .get();
        // 设置库路径
        System.load(IO.toAbsolutePath(LIB_PATH));
    }
    
    
    public SphericalChebyshevNative(String @NotNull[] aSymbols, int aNMax, int aLMax, int aL3Max, boolean aL3Cross, double aRCut) {super(aSymbols, aNMax, aLMax, aL3Max, aL3Cross, aRCut);}
    public SphericalChebyshevNative(String @NotNull[] aSymbols, int aNMax, int aLMax, int aL3Max, double aRCut) {super(aSymbols, aNMax, aLMax, aL3Max, aRCut);}
    public SphericalChebyshevNative(String @NotNull[] aSymbols, int aNMax, int aLMax, double aRCut) {super(aSymbols, aNMax, aLMax, aRCut);}
    public SphericalChebyshevNative(int aTypeNum, int aNMax, int aLMax, int aL3Max, boolean aL3Cross, double aRCut) {super(aTypeNum, aNMax, aLMax, aL3Max, aL3Cross, aRCut);}
    public SphericalChebyshevNative(int aTypeNum, int aNMax, int aLMax, int aL3Max, double aRCut) {super(aTypeNum, aNMax, aLMax, aL3Max, aRCut);}
    public SphericalChebyshevNative(int aTypeNum, int aNMax, int aLMax, double aRCut) {super(aTypeNum, aNMax, aLMax, aRCut);}
    
    @SuppressWarnings("rawtypes")
    public static SphericalChebyshevNative load(String @NotNull[] aSymbols, Map aMap) {
        return new SphericalChebyshevNative(
            aSymbols,
            ((Number) UT.Code.getWithDefault(aMap, DEFAULT_NMAX, "nmax")).intValue(),
            ((Number) UT.Code.getWithDefault(aMap, DEFAULT_LMAX, "lmax")).intValue(),
            ((Number) UT.Code.getWithDefault(aMap, DEFAULT_L3MAX, "l3max")).intValue(),
            (Boolean)UT.Code.getWithDefault(aMap, DEFAULT_L3CROSS, "l3cross"),
            ((Number) UT.Code.getWithDefault(aMap, DEFAULT_RCUT, "rcut")).doubleValue()
        );
    }
    @SuppressWarnings("rawtypes")
    public static SphericalChebyshevNative load(int aTypeNum, Map aMap) {
        return new SphericalChebyshevNative(
            aTypeNum,
            ((Number) UT.Code.getWithDefault(aMap, DEFAULT_NMAX, "nmax")).intValue(),
            ((Number) UT.Code.getWithDefault(aMap, DEFAULT_LMAX, "lmax")).intValue(),
            ((Number) UT.Code.getWithDefault(aMap, DEFAULT_L3MAX, "l3max")).intValue(),
            (Boolean)UT.Code.getWithDefault(aMap, DEFAULT_L3CROSS, "l3cross"),
            ((Number) UT.Code.getWithDefault(aMap, DEFAULT_RCUT, "rcut")).doubleValue()
        );
    }
    
    
    private final DoubleList mFingerPrintPxCross = new DoubleList(1024), mFingerPrintPyCross = new DoubleList(1024), mFingerPrintPzCross = new DoubleList(1024);
    
    /**
     * {@inheritDoc}
     * @param aCalCross 控制是否同时计算基组对于近邻原子坐标的偏导值，默认为 {@code false}
     * @param aNL 近邻列表遍历器
     * @return {@inheritDoc}
     */
    @Override public List<@NotNull Vector> evalPartial(boolean aCalCross, IDxyzTypeIterable aNL) {
        if (mDead) throw new IllegalStateException("This Basis is dead");
        
        final int tSizeN = sizeN();
        final int tSizeL = sizeL();
        final int tSizeFP = tSizeN*tSizeL;
        Vector rFingerPrint = VectorCache.getVec(tSizeFP);
        // 先统一计算 cnlm 并缓存近邻
        final RowMatrix cnlm = calCnlm(aNL, true);
        final int tNN = mDxAll.size();
        // 做标量积消去 m 项，得到此原子的 FP
        cnlm2fp(cnlm.internalData(), rFingerPrint.internalData(),
                tSizeN, mLMax, mL3Max, mL3Cross);
        
        // 下面计算偏导部分
        final Vector rFingerPrintPx = VectorCache.getZeros(tSizeFP);
        final Vector rFingerPrintPy = VectorCache.getZeros(tSizeFP);
        final Vector rFingerPrintPz = VectorCache.getZeros(tSizeFP);
        final @Nullable List<Vector> rFingerPrintPxCross = aCalCross ? VectorCache.getVec(tSizeFP, tNN) : null;
        final @Nullable List<Vector> rFingerPrintPyCross = aCalCross ? VectorCache.getVec(tSizeFP, tNN) : null;
        final @Nullable List<Vector> rFingerPrintPzCross = aCalCross ? VectorCache.getVec(tSizeFP, tNN) : null;
        // 缓存 cnlm 偏导数数据，现在只需要特定 n 下的一行即可
        final Vector cnlmPx = bufCnlmPx(false);
        final Vector cnlmPy = bufCnlmPy(false);
        final Vector cnlmPz = bufCnlmPz(false);
        // 缓存 Rn 数组
        final Vector tRnPx = bufRnPx(false);
        final Vector tRnPy = bufRnPy(false);
        final Vector tRnPz = bufRnPz(false);
        // 全局暂存 Y 的数组，这样可以用来防止重复获取来提高效率
        final Vector tYPtheta = bufYPtheta(false);
        final Vector tYPphi = bufYPphi(false);
        final Vector tYPx = bufYPx(false);
        final Vector tYPy = bufYPy(false);
        final Vector tYPz = bufYPz(false);
        
        // mFingerPrintPxCross 需要传入完整单个数组，这里采用内部对象的方法实现
        if (aCalCross) {
            mFingerPrintPxCross.clear(); mFingerPrintPxCross.addZeros(tNN*tSizeFP);
            mFingerPrintPyCross.clear(); mFingerPrintPyCross.addZeros(tNN*tSizeFP);
            mFingerPrintPzCross.clear(); mFingerPrintPzCross.addZeros(tNN*tSizeFP);
        }
        
        evalPartial0(mDxAll.internalData(), mDyAll.internalData(), mDzAll.internalData(), mTypeAll.internalData(), tNN,
                     mRnAll.internalData(), tRnPx.internalData(), tRnPy.internalData(), tRnPz.internalData(),
                     mYAll.internalData(), tYPtheta.internalData(), tYPphi.internalData(), tYPx.internalData(), tYPy.internalData(), tYPz.internalData(),
                     cnlm.internalData(), cnlmPx.internalData(), cnlmPy.internalData(), cnlmPz.internalData(),
                     rFingerPrintPx.internalData(), rFingerPrintPy.internalData(), rFingerPrintPz.internalData(),
                     aCalCross?mFingerPrintPxCross.internalData():null,
                     aCalCross?mFingerPrintPyCross.internalData():null,
                     aCalCross?mFingerPrintPzCross.internalData():null,
                     mTypeNum, mRCut, mNMax, mLMax, mL3Max, mL3Cross);
        
        if (aCalCross) {
            for (int j = 0; j < tNN; ++j) rFingerPrintPxCross.get(j).fill(new ShiftVector(tSizeFP, j*tSizeFP, mFingerPrintPxCross.internalData()));
            for (int j = 0; j < tNN; ++j) rFingerPrintPyCross.get(j).fill(new ShiftVector(tSizeFP, j*tSizeFP, mFingerPrintPyCross.internalData()));
            for (int j = 0; j < tNN; ++j) rFingerPrintPzCross.get(j).fill(new ShiftVector(tSizeFP, j*tSizeFP, mFingerPrintPzCross.internalData()));
        }
        
        List<Vector> rOut = Lists.newArrayList(rFingerPrint, rFingerPrintPx, rFingerPrintPy, rFingerPrintPz);
        if (aCalCross) {
            rOut.addAll(rFingerPrintPxCross);
            rOut.addAll(rFingerPrintPyCross);
            rOut.addAll(rFingerPrintPzCross);
        }
        return rOut;
    }
    
    static void evalPartial0(double[] aNlDx, double[] aNlDy, double[] aNlDz, int[] aNlType, int aNN,
                             double[] aNlRn, double[] rRnPx, double[] rRnPy, double[] rRnPz,
                             double[] aNlY, double[] rYPtheta, double[] rYPphi, double[] rYPx, double[] rYPy, double[] rYPz,
                             double[] aCnlm, double[] rCnlmPx, double[] rCnlmPy, double[] rCnlmPz,
                             double[] rFingerPrintPx, double[] rFingerPrintPy, double[] rFingerPrintPz,
                             double @Nullable[] rFingerPrintPxCross, double @Nullable[] rFingerPrintPyCross, double @Nullable[] rFingerPrintPzCross,
                             int aTypeNum, double aRCut, int aNMax, int aLMax, int aL3Max, boolean aL3Cross) {
        if (aL3Max > 4) throw new IllegalArgumentException("l3max > 4 for native SphericalChebyshev");
        final int tSizeN = aTypeNum>1 ? aNMax+aNMax+2 : aNMax+1;
        final int tSizeL = aLMax+1 + (aL3Cross?L3NCOLS:L3NCOLS_NOCROSS)[aL3Max];
        final int tLMax = Math.max(aLMax, aL3Max);
        final int tLMAll = (tLMax+1)*(tLMax+1);
        if (tLMax > 20) throw new IllegalArgumentException("lmax > 20 for native SphericalChebyshev");
        rangeCheck(aNlDx.length, aNN);
        rangeCheck(aNlDy.length, aNN);
        rangeCheck(aNlDz.length, aNN);
        rangeCheck(aNlType.length, aNN);
        rangeCheck(aNlRn.length, aNN*(aNMax+1));
        rangeCheck(rRnPx.length, aNMax+1);
        rangeCheck(rRnPy.length, aNMax+1);
        rangeCheck(rRnPz.length, aNMax+1);
        rangeCheck(aNlY.length, aNN*tLMAll);
        rangeCheck(rYPtheta.length, tLMAll);
        rangeCheck(rYPphi.length, tLMAll);
        rangeCheck(rYPx.length, tLMAll);
        rangeCheck(rYPy.length, tLMAll);
        rangeCheck(rYPz.length, tLMAll);
        rangeCheck(aCnlm.length, tSizeN*tLMAll);
        rangeCheck(rCnlmPx.length, tLMAll);
        rangeCheck(rCnlmPy.length, tLMAll);
        rangeCheck(rCnlmPz.length, tLMAll);
        rangeCheck(rFingerPrintPx.length, tSizeN*tSizeL);
        rangeCheck(rFingerPrintPy.length, tSizeN*tSizeL);
        rangeCheck(rFingerPrintPz.length, tSizeN*tSizeL);
        if (rFingerPrintPxCross != null) rangeCheck(rFingerPrintPxCross.length, aNN*tSizeN*tSizeL);
        if (rFingerPrintPyCross != null) rangeCheck(rFingerPrintPyCross.length, aNN*tSizeN*tSizeL);
        if (rFingerPrintPzCross != null) rangeCheck(rFingerPrintPzCross.length, aNN*tSizeN*tSizeL);
        evalPartial1(aNlDx, aNlDy, aNlDz, aNlType, aNN,
                     aNlRn, rRnPx, rRnPy, rRnPz,
                     aNlY, rYPtheta, rYPphi, rYPx, rYPy, rYPz,
                     aCnlm, rCnlmPx, rCnlmPy, rCnlmPz,
                     rFingerPrintPx, rFingerPrintPy, rFingerPrintPz,
                     rFingerPrintPxCross, rFingerPrintPyCross, rFingerPrintPzCross,
                     aTypeNum, aRCut, aNMax, aLMax, aL3Max, aL3Cross);
    }
    private static native void evalPartial1(double[] aNlDx, double[] aNlDy, double[] aNlDz, int[] aNlType, int aNN,
                                            double[] aNlRn, double[] rRnPx, double[] rRnPy, double[] rRnPz,
                                            double[] aNlY, double[] rYPtheta, double[] rYPphi, double[] rYPx, double[] rYPy, double[] rYPz,
                                            double[] aCnlm, double[] rCnlmPx, double[] rCnlmPy, double[] rCnlmPz,
                                            double[] rFingerPrintPx, double[] rFingerPrintPy, double[] rFingerPrintPz,
                                            double @Nullable[] rFingerPrintPxCross, double @Nullable[] rFingerPrintPyCross, double @Nullable[] rFingerPrintPzCross,
                                            int aTypeNum, double aRCut, int aNMax, int aLMax, int aL3Max, boolean aL3Cross);
    
    static void rangeCheck(int jArraySize, int aCount) {
        if (aCount > jArraySize) throw new IndexOutOfBoundsException(aCount+" > "+jArraySize);
    }
}
