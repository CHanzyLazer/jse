package test.system

import jtool.code.UT
import jtool.system.CMD

import static jtool.code.UT.Exec.*



UT.IO.write('.temp/testGBK', '卷的序列号是');
UT.IO.toWriteln('.temp/testGBK2').withCloseable {it.writeln('卷的序列号是')}

println('卷的序列号是');
system('echo 卷的序列号是');

new CMD().withCloseable {it.system('dir');}
