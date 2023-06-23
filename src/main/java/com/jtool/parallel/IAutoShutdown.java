package com.jtool.parallel;

import org.jetbrains.annotations.VisibleForTesting;

public interface IAutoShutdown extends AutoCloseable {
    /** AutoClosable stuffs */
    default void shutdown() {/**/}
    @VisibleForTesting default void close() {shutdown();}
}
