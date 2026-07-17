package jse.clib;

public final class CLibException extends Exception {
    public CLibException(String aMessage) {
        super(aMessage);
    }
    public CLibException(int aErrCode, String aMessage) {
        super(aMessage+", error=" + aErrCode);
    }
}
