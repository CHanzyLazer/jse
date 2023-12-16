package test.mpi

import jtool.parallel.MPI;

import static jtool.code.UT.Math.*;

/**
 * 测试 MPI
 */

MPI.init(args);

int me = MPI.Comm.WORLD.rank();
int size = MPI.Comm.WORLD.size();
double r = rand();
println("rand of <$me>: $r");
double [] recv = new double[size];
MPI.Comm.WORLD.allgather([r] as double[], 1, recv, 1);
println("recv of <$me>: $recv");

MPI.shutdown();

