package jse.parallel;

public final class MPIException extends Exception {
    public final int mErrCode;
    
    public MPIException(int aErrCode, String aMessage) {
        super(aMessage);
        mErrCode = aErrCode;
    }
}
