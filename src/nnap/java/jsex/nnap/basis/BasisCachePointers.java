package jsex.nnap.basis;

import jse.clib.DoubleCPointer;
import jse.clib.GrowableDoubleCPointer;
import jse.clib.GrowableIntCPointer;
import jse.clib.IntCPointer;
import jse.code.ReferenceChecker;

/**
 * 用来自动回收 {@link Basis} 内部缓存的 c 指针，这里采取的策略是每次创建之前清理旧的数据
 * @see <a href="http://www.oracle.com/technetwork/articles/java/finalization-137655.htm">
 * How to Handle Java Finalization's Memory-Retention Issues </a>
 * @author liqa
 */
class BasisCachePointers extends ReferenceChecker {
    final GrowableDoubleCPointer[] mDoublePointers;
    final GrowableIntCPointer[] mIntPointers;
    BasisCachePointers(Basis aBasis, GrowableDoubleCPointer[] aDoublePointers, GrowableIntCPointer[] aIntPointers) {
        super(aBasis);
        mDoublePointers = aDoublePointers;
        mIntPointers = aIntPointers;
    }
    
    @Override protected void dispose_() {
        for (DoubleCPointer tDoubleCPointer : mDoublePointers) {
            tDoubleCPointer.free();
        }
        for (IntCPointer tIntCPointer : mIntPointers) {
            tIntCPointer.free();
        }
    }
}
