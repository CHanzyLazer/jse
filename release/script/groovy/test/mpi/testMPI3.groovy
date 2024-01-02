package test.mpi

import jtool.code.UT
import jtool.parallel.MPI


/**
 * 测试 MPI 给自己传信息的情况；
 * （会报错）
 */

MPI.init(args);

final int me = MPI.Comm.WORLD.rank();
final int np = MPI.Comm.WORLD.size();

double[] a = [1.0, 2.0, 3.0, 4.0];
double[] b = [0.0, 0.0, 0.0, 0.0];

println("a: $a, b: $b");

UT.Par.runAsync {
    MPI.Comm.WORLD.send(a, 4, me);
}
sleep(100);
MPI.Comm.WORLD.recv(b, 4, me);

println("a: $a, b: $b");

MPI.shutdown();

