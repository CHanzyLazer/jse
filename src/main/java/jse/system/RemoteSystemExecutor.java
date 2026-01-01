package jse.system;


import jse.code.IO;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.Future;

/**
 * SystemExecutor 的另一种抽象实现，在远程运行的情况
 * @author liqa
 */
public abstract class RemoteSystemExecutor extends AbstractSystemExecutor {
    public RemoteSystemExecutor() {super();}
    
    /** stuff to override */
    @Override protected abstract Future<Integer> submitSystem__(String aCommand, @NotNull IO.IWriteln aWriteln);
    @Override protected abstract void putFiles_(Iterable<String> aFiles) throws Exception;
    @Override protected abstract void getFiles_(Iterable<String> aFiles) throws Exception;
}
