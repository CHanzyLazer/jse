package jse.system;

import org.jetbrains.annotations.NotNull;

/**
 * @author liqa
 * <p> 在本地的 powershell 上执行，这里直接使用在命令前增加 PowerShell 来实现 </p>
 */
public class PowerShellSystemExecutor extends LocalSystemExecutor {
    public PowerShellSystemExecutor() {super();}
    
    private final static String[] PS_ARGS = {"PowerShell", "-Command"};
    @Override protected String @NotNull[] programAndArgs_() {return PS_ARGS;}
}
