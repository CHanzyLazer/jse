package jse.code.timer;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface ITimer {
    long getNanos();
    long getMillis();
    double get();
    void reset();
}
