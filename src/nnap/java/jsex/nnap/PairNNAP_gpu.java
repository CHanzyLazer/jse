package jsex.nnap;

import jse.gpu.CudaJIT;
import jse.gpu.CudaNeighborListGetter;
import org.jetbrains.annotations.ApiStatus;

/**
 * {@link PairNNAP} 的 GPU 版本，在 lammps
 * in 文件中添加：
 * <pre> {@code
 * pair_style   jse jsex.nnap.PairNNAP_gpu
 * pair_coeff   * * path/to/nnpot.json Cu Zr
 * } </pre>
 * 来使用
 */
public class PairNNAP_gpu extends PairNNAP {
    /** 用于判断是否进行了静态初始化以及方便的手动初始化 */
    public final static class InitHelper {
        private static volatile boolean INITIALIZED = false;
        public static boolean initialized() {return INITIALIZED;}
        @SuppressWarnings({"ResultOfMethodCallIgnored", "UnnecessaryCallToStringValueOf"})
        public static void init() {
            // 手动调用此值来强制初始化
            if (!INITIALIZED) String.valueOf(_INIT_FLAG);
        }
    }
    private final static boolean _INIT_FLAG;
    static {
        PairNNAP_gpu.InitHelper.INITIALIZED = true;
        // 需要 cuda jit 和 cuda nl
        CudaJIT.InitHelper.init();
        CudaNeighborListGetter.InitHelper.init();
        _INIT_FLAG = false;
    }
    
    protected PairNNAP_gpu(long aPairPtr) {
        super(aPairPtr);
    }
    
//    @Override public void initStyle() {
//        if (!forceNewtonPair()) {
//            throw new IllegalArgumentException("Pair style NNAP requires newton pair on");
//        }
//        // gpu 总是手动构造近邻列表
//    }
    @Override public void compute() throws Exception {
        mNNAP.computeLammpsCuda(this);
    }
    @Override protected NNAP initNNAP(String aPath) throws Exception {
        return new NNAP(aPath, "cuda");
    }
}
