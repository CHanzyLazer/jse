package jse.system;

import org.jetbrains.annotations.NotNull;

/**
 * @author liqa
 * <p> 在本地的 pwsh 上执行，这里直接使用在命令前增加 pwsh 来实现 </p>
 * <p> pwsh 即为新版的 PowerShell </p>
 */
public class PWSHSystemExecutor extends LocalSystemExecutor {
    public PWSHSystemExecutor() {super();}
    
    private final static String[] PS_ARGS = {"pwsh", "-c"};
    @Override protected String @NotNull[] programAndArgs_() {return PS_ARGS;}
}
