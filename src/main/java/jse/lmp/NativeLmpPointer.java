package jse.lmp;

import jse.Main;
import jse.code.UT;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用来自动回收 {@link NativeLmp} 内部的指针，这里采取的策略是每次创建之前清理旧的数据
 * @see <a href="http://www.oracle.com/technetwork/articles/java/finalization-137655.htm">
 * How to Handle Java Finalization's Memory-Retention Issues </a>
 * @author liqa
 */
class NativeLmpPointer extends WeakReference<NativeLmp> {
    final long mPtr;
    final Thread mInitThead; // lammps 需要保证初始化时的线程和释放时是相同的
    NativeLmpPointer(NativeLmp aNativeLmp, long aPtr) {
        super(aNativeLmp, getReferenceQueue());
        mPtr = aPtr;
        mInitThead = Thread.currentThread();
        sPointers.add(this);
    }
    
    volatile boolean disposed = false;
    synchronized void dispose() {
        if (!disposed) {
            disposed = true;
            try {
                checkThread();
                try {lammpsClose_(mPtr);} catch (LmpException ignored) {}
            } catch (LmpException e) {
                UT.Code.printStackTrace(e);
            }
            sPointers.remove(this);
        }
    }
    void checkThread() throws LmpException {
        Thread tCurrentThread = Thread.currentThread();
        if (tCurrentThread != mInitThead) throw new LmpException("Thread of NativeLmp MUST be SAME: "+tCurrentThread+" vs "+mInitThead);
    }
    static native void lammpsClose_(long aModelPtr) throws LmpException;
    
    private final static ReferenceQueue<NativeLmp> sRefQueue = new ReferenceQueue<>();
    private final static Set<NativeLmpPointer> sPointers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static ReferenceQueue<NativeLmp> getReferenceQueue() {
        cleanupWeakReferences();
        return sRefQueue;
    }
    private static void cleanupWeakReferences() {
        NativeLmpPointer p = (NativeLmpPointer)sRefQueue.poll();
        while (p != null) {
            p.dispose();
            p = (NativeLmpPointer)sRefQueue.poll();
        }
    }
    private static void disposeAll() {
        Iterator<NativeLmpPointer> it = sPointers.iterator();
        while (it.hasNext()) {
            NativeLmpPointer ptr = it.next();
            /*
             * ptr.dispose() will remove from the set, so we remove it here
             * first to avoid ConcurrentModificationException
             */
            it.remove();
            ptr.dispose();
        }
    }
    // 在程序结束时清空所有指针
    static {Main.addGlobalAutoCloseable(NativeLmpPointer::disposeAll);}
}
