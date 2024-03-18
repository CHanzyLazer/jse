package jse.lmp;

import jse.system.ISystemExecutor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

@VisibleForTesting
public final class LPC extends LmpParameterCalculator {
    private LPC(ILmpExecutor aLMP, String aPairStyle, String aPairCoeff) {super(aLMP, aPairStyle, aPairCoeff);}
    private LPC(String aLmpExe, @Nullable String aLogPath, String aPairStyle, String aPairCoeff) {super(aLmpExe, aLogPath, aPairStyle, aPairCoeff);}
    private LPC(String aLmpExe, String aPairStyle, String aPairCoeff) {super(aLmpExe, aPairStyle, aPairCoeff);}
    private LPC(ISystemExecutor aEXE, String aLmpExe, @Nullable String aLogPath, String aPairStyle, String aPairCoeff) {super(aEXE, aLmpExe, aLogPath, aPairStyle, aPairCoeff);}
    private LPC(ISystemExecutor aEXE, String aLmpExe, String aPairStyle, String aPairCoeff) {super(aEXE, aLmpExe, aPairStyle, aPairCoeff);}
}
