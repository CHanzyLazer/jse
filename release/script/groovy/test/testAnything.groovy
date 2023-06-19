package test

import com.jtool.code.UT
import com.jtool.iofile.InFiles
import com.jtool.lmp.Dump
import com.jtool.lmp.LmpIn
import com.jtool.lmp.Lmpdat
import com.jtool.math.MathEX
import com.jtool.system.SSH
import com.jtool.vasp.POSCAR

//// 测试一下远程删除文件夹失败的问题
//def ssh = new SSH(UT.IO.json2map('.SECRET/SSH_INFO.json'));
//
//ssh.rmdir('.temp/jTool@6ydvWO2h');
//
//ssh.shutdown();

//data = Lmpdat.read('lmp/data/data-glass');
//
//dump = Dump.fromAtomData(data);
//dump.write('lmp/data/data-glass.lammpstrj');

// 尝试使用 lammps 计算
def lmpIn = LmpIn.custom('lmp/in/cal-OOP');
lmpIn
    .i('vInDataPath', 'lmp/data/CuFCC108.lmpdat')
    .o('vDumpPath', 'lmp/.temp/Cu108.lammpstrj');

lmpIn.vInDataPath = 'lmp/data/data-glass';
lmpIn.vDumpPath = 'lmp/.temp/out-cal';
lmpIn.vCutoff = 3.45891;
lmpIn.write('lmp/.temp/in-cal');

def ssh = new SSH(UT.IO.json2map('.SECRET/SSH_INFO.json'));

ssh.system("srun -p debug -n 1 lmp_ann -in ${lmpIn.i('<self>')}", lmpIn);

ssh.shutdown();

// 读取 dump 计算平均值
def dump = Dump.read(lmpIn.vDumpPath as String);
println(dump.asTable()['c_Ql[1]'].opt().mean());
