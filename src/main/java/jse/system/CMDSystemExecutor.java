package jse.system;

import org.jetbrains.annotations.NotNull;

/**
 * @author liqa
 * <p> 在本地的 cmd 上执行 </p>
 */
public class CMDSystemExecutor extends LocalSystemExecutor {
    public CMDSystemExecutor() {super();}
    
    private final static String[] CMD_ARGS = {"cmd", "/c"};
    @Override protected String @NotNull[] programAndArgs_() {return CMD_ARGS;}
}
