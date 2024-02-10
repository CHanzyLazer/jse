package jse.code;

/**
 * 全局的可以设置的常量值
 * @author liqa
 */
public class Conf {
    private Conf() {}
    
    /** 设置 jse 许多类工作的临时目录，一般来说使用 .jse/ 目录应该会更加合适，这里为了兼容并减少目录数还是默认保持使用 .temp */
    public static String TEMP_WORKING_DIR = ".temp/%n/";
    
    /** {@link System#out} 和 {@link System#err} 是否支持复杂的 unicode 字符，禁用后可以避免乱码问题 */
    public static boolean UNICODE_SUPPORT = true;
    
    /** 控制 parfor 的模式，在竞争模式下不同线程分配到的任务是不一定的，而关闭后是一定的，有时需要保证重复运行结果一致 */
    public static boolean PARFOR_NO_COMPETITIVE = false;
    /** 设置 parfor 默认的线程数 */
    public static int PARFOR_THREAD_NUM = Runtime.getRuntime().availableProcessors();
    
    /** 设置是否开启缓存，关闭后可以让内存管理全权交给 jvm，目前 jse 的内存效率和 jvm 基本类似 */
    public static boolean NO_CACHE = false;
}
