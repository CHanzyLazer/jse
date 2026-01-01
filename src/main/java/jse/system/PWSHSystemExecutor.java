package jse.system;

import jse.code.IO;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Future;

import static jse.code.CS.SUC_FUTURE;

/**
 * 在本地的 pwsh 上执行，这里直接使用在命令前增加 pwsh 来实现
 * <p>
 * pwsh 即为新版的 PowerShell
 * @author liqa
 */
public class PWSHSystemExecutor extends LocalSystemExecutor {
    public PWSHSystemExecutor() {super();}
    
    private final static String[] PS_ARGS = {"pwsh", "-of", "Text", "-ec"};
    @Override protected String @NotNull[] programAndArgs_() {return PS_ARGS;}
    
    @Override protected Future<Integer> submitSystem__(String aCommand, @NotNull IO.IWriteln aWriteln) {
        // 对于空指令专门优化，不执行操作
        if (aCommand == null || aCommand.isEmpty()) return SUC_FUTURE;
        // powershell 需要进行编码成 base64 保证稳定
        String tBase64 = Base64.getEncoder().encodeToString(aCommand.getBytes(StandardCharsets.UTF_16LE));
        return super.submitSystem__(tBase64, aWriteln);
    }
}
