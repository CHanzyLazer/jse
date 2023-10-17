package jtool.system;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

import static jtool.code.CS.SUC_FUTURE;

/**
 * @author liqa
 * <p> 在本地的 wsl 上执行，这里直接使用在命令前增加 wsl 来实现 </p>
 */
public class WSLSystemExecutor extends LocalSystemExecutor {
    public WSLSystemExecutor() {super();}
    
    @Override protected Future<Integer> submitSystem__(String aCommand, @NotNull IPrintlnSupplier aPrintln) {
        // 对于空指令专门优化，不执行操作
        if (aCommand == null || aCommand.isEmpty()) return SUC_FUTURE;
        
        return super.submitSystem__("wsl " + aCommand, aPrintln);
    }
}
