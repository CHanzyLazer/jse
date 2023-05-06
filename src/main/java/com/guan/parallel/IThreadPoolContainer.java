package com.guan.parallel;

/**
 * @author liqa
 * <p> 用来统一管理包含 ThreadPool 的类 </p>
 */
public interface IThreadPoolContainer extends AutoCloseable {
    void shutdown();
    void shutdownNow();
    boolean isShutdown();
    boolean isTerminated();
    void awaitTermination() throws InterruptedException;
    
    void waitUntilDone() throws InterruptedException;
    int nTasks();
    int nThreads();
    
    @Deprecated default int getTaskNumber() {return nTasks();}
    
    /** AutoCloseable stuffs */
    default void close() {if (!isShutdown()) shutdown();}
}
