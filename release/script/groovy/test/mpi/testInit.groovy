package test.mpi

import jtool.parallel.MPI

/**
 * 测试静态初始化
 */

println(MPI.InitHelper.initialized());
MPI.InitHelper.init();
println(MPI.InitHelper.initialized());

