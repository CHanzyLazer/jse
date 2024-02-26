package example.lmp

import jse.lmp.Dump

// 读取现有的 lammps dump 文件
def dump = Dump.read('lmp/dump/CuFCC108.lammpstrj')

// 获取帧数
println('frame number: ' + dump.size())
// 直接获取某一帧
def frame = dump[4]
// 获取属性
println('atom number: ' + frame.natoms())
println('time step: ' + frame.timeStep())
println('atom at 10: ' + frame.atom(10))

// 写入文本
dump.write('.temp/example/lmp/dumpFCC')


//OUTPUT:
// frame number: 21
// atom number: 108
// time step: 4000
// atom at 10: {id: 50, type: 1, xyz: (2.623, 4.457, 2.003), vxvyvz: (-2.409, 0.3910, -0.5409)}

