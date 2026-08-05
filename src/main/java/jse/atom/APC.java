package jse.atom;

import org.jetbrains.annotations.VisibleForTesting;

/** @see AtomicParameterCalculator */
@VisibleForTesting
public final class APC extends AtomicParameterCalculator {
    /** @deprecated use {@link #of(IAtomData)} */ @Deprecated private APC(int aNumThreads) {super(aNumThreads);}
}
