package jsex.nnap;

import jse.Main;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用来自动回收 {@link NNAP} 内部的 model 指针，这里采取的策略是每次创建之前清理旧的数据
 * @see <a href="http://www.oracle.com/technetwork/articles/java/finalization-137655.htm">
 * How to Handle Java Finalization's Memory-Retention Issues </a>
 * @author liqa
 */
class NNAPModelPointers extends WeakReference<NNAP.SingleNNAP> {
    final long[] mPtrs;
    NNAPModelPointers(NNAP.SingleNNAP aNNAP, long[] aPtrs) {
        super(aNNAP, getReferenceQueue());
        mPtrs = aPtrs;
        sPointers.add(this);
    }
    
    volatile boolean disposed = false;
    synchronized void dispose() {
        if (!disposed) {
            disposed = true;
            for (long tPtr : mPtrs) dispose0(tPtr);
            sPointers.remove(this);
        }
    }
    static native void dispose0(long aModelPtr);
    
    private final static ReferenceQueue<NNAP.SingleNNAP> sRefQueue = new ReferenceQueue<>();
    private final static Set<NNAPModelPointers> sPointers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static ReferenceQueue<NNAP.SingleNNAP> getReferenceQueue() {
        cleanupWeakReferences();
        return sRefQueue;
    }
    private static void cleanupWeakReferences() {
        NNAPModelPointers p = (NNAPModelPointers)sRefQueue.poll();
        while (p != null) {
            p.dispose();
            p = (NNAPModelPointers)sRefQueue.poll();
        }
    }
    private static void disposeAll() {
        Iterator<NNAPModelPointers> it = sPointers.iterator();
        while (it.hasNext()) {
            NNAPModelPointers ptr = it.next();
            /*
             * ptr.dispose() will remove from the set, so we remove it here
             * first to avoid ConcurrentModificationException
             */
            it.remove();
            ptr.dispose();
        }
    }
    // 在程序结束时清空所有指针
    static {Main.addGlobalAutoCloseable(NNAPModelPointers::disposeAll);}
}
