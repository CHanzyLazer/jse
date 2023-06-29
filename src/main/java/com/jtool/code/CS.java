package com.jtool.code;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jtool.atom.IHasXYZ;
import com.jtool.atom.XYZ;
import com.jtool.iofile.IHasIOFiles;
import com.jtool.iofile.IOFiles;
import com.jtool.parallel.CompletedFuture;
import com.jtool.system.*;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liqa
 * <p> Class containing useful Constants </p>
 */
public class CS {
    /** a Random generator so I don't need to instantiate a new one all the time. */
    public final static Random RNGSUS = new Random(), RANDOM = RNGSUS;
    public final static int MAX_SEED = 2147483647;
    
    public final static Object NULL = null;
    
    public final static XYZ BOX_ONE  = new XYZ(1.0, 1.0, 1.0);
    public final static XYZ BOX_ZERO = new XYZ(0.0, 0.0, 0.0);
    public static XYZ TO_BOX(IHasXYZ aXYZ) {
        if (aXYZ == BOX_ONE) return BOX_ONE;
        if (aXYZ == BOX_ZERO) return BOX_ZERO;
        return new XYZ(aXYZ);
    }
    
    public final static String WORKING_DIR = ".temp/%n/";
    
    public final static boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    public final static String NO_LOG_LINUX = "/dev/null";
    public final static String NO_LOG_WIN = "NUL";
    public final static String NO_LOG = IS_WINDOWS ? NO_LOG_WIN : NO_LOG_LINUX;
    
    
    /** MathEX stuffs */
    public enum SliceType {ALL}
    public final static SliceType ALL = SliceType.ALL;
    
    
    /** AtomData stuffs */
    public final static String[] ATOM_DATA_KEYS_XYZ = new String[] {"x", "y", "z"};
    public final static String[] ATOM_DATA_KEYS_XYZID = new String[] {"x", "y", "z", "id"};
    public final static String[] ATOM_DATA_KEYS_ID_TYPE_XYZ = new String[] {"id", "type", "x", "y", "z"};
    public final static String[] STD_ATOM_DATA_KEYS = ATOM_DATA_KEYS_ID_TYPE_XYZ; // 标准 AtomData 包含信息格式为 id type x y z，和 Lmpdat 保持一致
    public final static int STD_TYPE_COL = 1, STD_ID_COL = 0, STD_X_COL = 2, STD_Y_COL = 3, STD_Z_COL = 4;
    
    /** const arrays */
    public final static String[] ZL_STR = new String[0];
    public final static Object[] ZL_OBJ = new Object[0];
    public final static double[][] ZL_MAT = new double[0][];
    public final static double[]   ZL_VEC = new double[0];
    public final static byte[] ZL_BYTE = new byte[0];
    
    /** IOFiles Keys */
    public final static String OUTPUT_FILE_KEY = "<out>", INFILE_SELF_KEY = "<self>", OFILE_KEY = "<o>", IFILE_KEY = "<i>", LMP_LOG_KEY = "<lmp>";
    
    /** Relative atomic mass in this project */
    public final static Map<String, Double> MASS = (new ImmutableMap.Builder<String, Double>())
        .put("Cu", 63.546)
        .put("Zr", 91.224)
        .build();
    
    /** SystemExecutor Stuffs */
    public final static IHasIOFiles EPT_IOF = new IOFiles();
    public final static IFutureJob ERR_FUTURE = new CompletedFutureJob(-1);
    public final static Future<List<Integer>> ERR_FUTURES = new CompletedFuture<>(Collections.singletonList(-1));
    public final static Future<List<String>> EPT_STR_FUTURE = new CompletedFuture<>(ImmutableList.of());
    public final static PrintStream NUL_PRINT_STREAM = new PrintStream(new OutputStream() {public void write(int b) {/**/}});
    
    /** 内部运行相关，使用子类分割避免冗余初始化 */
    public static class Exec {
        public final static ISystemExecutor EXE;
        public final static String JAR_PATH;
        static {
            // 先手动加载 UT，会自动重新设置工作目录，保证路径的正确性
            UT.IO.init();
            // 获取此 jar 的路径
            JAR_PATH = System.getProperty("java.class.path");
            // 创建默认 EXE，无内部线程池，windows 下使用 powershell 统一指令
            EXE = IS_WINDOWS ? new PowerShellSystemExecutor() : new LocalSystemExecutor();
            // 在 JVM 关闭时关闭 EXE
            Runtime.getRuntime().addShutdownHook(new Thread(EXE::shutdown));
        }
    }
    
    /** SLURM 相关，使用子类分割避免冗余初始化 */
    public static class Slurm {
        public static final boolean IS_SLURM;
        public static final int PROCID;
        public static final int NTASKS;
        public static final int CORES_PER_NODE;
        public static final List<String> NODE_LIST;
        
        static {
            // 获取 ID，如果失败则不是 slurm
            int tId = -1;
            try {tId = Integer.parseInt(System.getenv("SLURM_PROCID"));} catch (Exception ignored) {}
            PROCID = tId;
            IS_SLURM = PROCID >= 0;
            // 是 slurm 则从环境变量中读取后续参数，否则使用默认非法值
            if (IS_SLURM) {
                // 获取任务总数
                NTASKS = Integer.parseInt(System.getenv("SLURM_NTASKS"));
                
                // 获取每节点的核心数
                String tRawCoresPerNode = System.getenv("SLURM_JOB_CPUS_PER_NODE");
                // 目前仅支持单一的 CoresPerNode，对于有多个的情况会选取最小值
                Pattern tPattern = Pattern.compile("(\\d+)(\\([^)]+\\))?"); // 匹配整数部分和可选的括号部分
                Matcher tMatcher = tPattern.matcher(tRawCoresPerNode);
                int tCoresPerNode = -1;
                while (tMatcher.find()) {
                    int tResult = Integer.parseInt(tMatcher.group(1));
                    tCoresPerNode = (tCoresPerNode < 0) ? tResult : Math.min(tCoresPerNode, tResult);
                }
                CORES_PER_NODE = tCoresPerNode;
                
                // 获取节点列表
                NODE_LIST = ImmutableList.copyOf(UT.Texts.splitNodeList(System.getenv("SLURM_NODELIST")));
            } else {
                NTASKS = -1;
                CORES_PER_NODE = -1;
                NODE_LIST = null;
            }
        }
    }
}
