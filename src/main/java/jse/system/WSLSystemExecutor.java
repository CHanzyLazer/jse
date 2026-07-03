package jse.system;

import org.jetbrains.annotations.NotNull;

/**
 * 在本地的 wsl 上执行，这里直接使用在命令前增加 wsl 来实现
 * @author liqa
 */
public class WSLSystemExecutor extends LocalSystemExecutor {
    private final String[] mWslArgs;
    public WSLSystemExecutor(boolean aInteractive) {
        super();
        // 通过 --exec bash -i 的方式启动 bash，保证读取用户环境来产生大部分情况下的预期行为
        mWslArgs = aInteractive ?
            new String[]{"wsl", "--exec", "bash", "-i", "-c"} :
            new String[]{"wsl", "--exec", "bash", "-c"};
    }
    public WSLSystemExecutor() {this(true);}
    
    @Override protected String @NotNull[] programAndArgs_() {return mWslArgs;}
}
