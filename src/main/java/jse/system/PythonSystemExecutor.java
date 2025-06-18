package jse.system;

import org.jetbrains.annotations.NotNull;

/**
 * @author liqa
 * <p> 在本地的 python 上执行 </p>
 */
public class PythonSystemExecutor extends LocalSystemExecutor {
    private final String[] mPyArgs;
    public PythonSystemExecutor(boolean aUsePython3) {
        super();
        mPyArgs = new String[]{aUsePython3 ? "python3" : "python", "-c"};
    }
    public PythonSystemExecutor() {this(false);}
    
    @Override protected String @NotNull[] programAndArgs_() {return mPyArgs;}
}
