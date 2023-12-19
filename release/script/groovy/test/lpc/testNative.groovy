package test.lpc

import jtool.lmp.NativeLmp

try (def lammps = new NativeLmp()) {
    println(lammps.version());
}

