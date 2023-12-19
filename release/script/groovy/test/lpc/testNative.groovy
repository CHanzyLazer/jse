package test.lpc

import jtool.lmp.NativeLmp

try (def lammps = new NativeLmp('liblammps', '-log', 'none')) {
    println(lammps.version());
}

