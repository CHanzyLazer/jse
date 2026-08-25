package jsex.nnap;

import jse.code.OS;
import jse.gpu.CudaCore;
import jse.gpu.CudaJIT;
import jse.gpu.CudaNeighborListGetter;

import static jse.code.Conf.DEBUG;

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
    
    public final static class Conf {
        /**
         * 自定义 NNAP LAMMPS GPU 版本会使用的设备编号
         * <p>
         * 也可使用环境变量 {@code JSE_PAIR_NNAP_GPU_DEVICE} 来设置
         */
        public static int DEVICE = OS.envI("JSE_PAIR_NNAP_GPU_DEVICE", -1);
    }
    
    protected PairNNAP_gpu(long aPairPtr) {
        super(aPairPtr);
    }
    @SuppressWarnings("JavaPrintToLogpoint")
    @Override public void settings(String... aArgs) throws Exception {
        super.settings(aArgs);
        int tMe = commMe();
        CudaCore.assignDevice(tMe, Conf.DEVICE);
        if (DEBUG) {
            if (tMe==0) System.out.println("========NNAP GPU DEVICE========");
            commBarrier();
            System.out.println("rank: "+tMe+", device: "+CudaCore.cudaGetDevice());
            commBarrier();
            if (tMe==0) System.out.println("===============================");
        }
    }
    
    @Override public void initStyle() {
        if (!forceNewtonPair()) {
            throw new IllegalArgumentException("Pair style NNAP requires newton pair on");
        }
        // gpu 总是手动构造近邻列表
    }
    @Override public void compute() throws Exception {
        mNNAP.computeLammpsCuda(this);
    }
    @Override protected NNAP initNNAP(String aPath) throws Exception {
        return new NNAP(aPath, "cuda");
    }
    
    @SuppressWarnings("JavaPrintToLogpoint")
    @Override public void close() throws Exception {
        if (DEBUG && commMe()==0) {
            System.out.println("=========NNAP GPU TIME=========");
            System.out.printf("copy    time: %.4g s\n", mNNAP.cudaCopyTime());
            System.out.printf("compute time: %.4g s\n", mNNAP.cudaComputeTime());
            System.out.printf("nl      time: %.4g s\n", mNNAP.cudaNlTime());
            System.out.println("===============================");
        }
        super.close();
    }
}
