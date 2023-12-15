package test.mpi

import static jtool.parallel.MPI.*;

/**
 * 测试 MPI
 */

MPI_Init(args);
int me = MPI_Comm_rank(MPI_COMM_WORLD);
System.out.println("Hi from <"+me+">");
MPI_Finalize();

