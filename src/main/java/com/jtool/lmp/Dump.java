package com.jtool.lmp;

import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;

@VisibleForTesting
public final class Dump extends Lammpstrj {
    Dump(SubLammpstrj... aData) {super(aData);}
    Dump(List<SubLammpstrj> aData) {super(aData);}
}
