package test

import com.guan.code.UT
import com.guan.lmp.LmpIn
import com.guan.system.SLURM
import com.guan.system.SSH


/** 测试使用 SLURM 运行 lammps，以后会有 LmpRunner 直接执行 */
// 创建输出目录
UT.IO.mkdir('lmp/.temp');
// 各种目录
logPath = 'lmp/.temp/log-lmp';
inPath = 'lmp/.temp/in-lmp';

// 创建 ssh，用来监控
SSH_INFO = UT.IO.json2map('.SECRET/SSH_INFO.json');
ssh = new SSH(SSH_INFO.csrc as Map);
// 创建 slurm，可以这样覆盖设定的工作区间和使用核心数
slurm = new SLURM('debug', 20, 20, SSH_INFO.csrc as Map);


// 获取 LmpIn 文件
lmpIn = LmpIn.INIT_MELT_NPT_Cu();
// 修改输出目录，lammps 输出时不会自动创建目录，这个在 LmpRunner 中会自动解决（遍历一次输出文件获取要创建目录）
lmpIn.vOutRestartPath = 'lmp/.temp/melt-Cu108-init';
// 设置势函数
lmpIn.pair_style = 'eam/alloy';
lmpIn.pair_coeff = '* * lmp/potential/ZrCu.lammps.eam Cu Zr';
// 附加势函数文件
lmpIn.i('eam', 'lmp/potential/ZrCu.lammps.eam'); // 同样 key 可以随便起
// 写入输入文件
lmpIn.write(inPath);

// 提交任务，指定 log 输出路径，附加的输入输出文件路径
slurm.submitSystem("lmp_ann -in ${inPath}", logPath, lmpIn);
sleep(10000);

// 这里使用 linux 的 tail 指令来监控输出文件，因为这个不是主要需求，并且要等到 slurm 排到队才会有输出文件，因此不会在内部实现类似功能
ssh.system("tail -f -n 100 ${logPath}");


// 关闭 slurm
slurm.shutdown();
// 关闭 ssh
ssh.shutdown();


