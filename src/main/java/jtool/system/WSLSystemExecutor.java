package jtool.system;

import org.jetbrains.annotations.NotNull;

/**
 * @author liqa
 * <p> 在本地的 wsl 上执行，这里直接使用在命令前增加 wsl 来实现 </p>
 */
public class WSLSystemExecutor extends LocalSystemExecutor {
    public WSLSystemExecutor() {super();}
    
    private final static String[] WSL_ARGS = {"wsl"};
    @Override protected String @NotNull[] programAndArgs_() {return WSL_ARGS;}
}
