package run

import com.jtool.code.UT
import com.jtool.system.SRUN


/** SLURM 在 node 中的任务再次创建任务的实例 */
def exe = new SRUN(18);

/** 跑几轮 lammps */
UT.Timer.tic();
exe.system('lmp_ann -in lmp/.temp/CuZr-dump/Cu60Zr40/melt-fast/in > lmp/.temp/CuZr-dump/Cu60Zr40/melt-fast/thermo');
UT.Timer.toc('lmp run 1');
UT.Timer.tic();
exe.system('lmp_ann -in lmp/.temp/CuZr-dump/Cu60Zr40/melt-fast/in > lmp/.temp/CuZr-dump/Cu60Zr40/melt-fast/thermo');
UT.Timer.toc('lmp run 2');

exe.shutdown();
exe.awaitTermination();
