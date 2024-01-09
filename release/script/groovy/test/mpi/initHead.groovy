package test.mpi

import jtool.lmp.NativeLmp
import jtool.parallel.MPI

import static jtool.code.CS.Exec.*
import static jtool.code.UT.Exec.*

system("javah -cp ${JAR_PATH} -d ../src/main/resources/assets/mpi/src ${MPI.Native.name}");

