package test.mpc

import jtool.code.UT
import jtool.math.vector.IVector
import jtool.vasp.XDATCAR

/**
 * 测试基组正确性
 */


data = XDATCAR.read('vasp/.lll-in/XDATCAR').last();

UT.Timer.tic();
fp = data.getMPC().withCloseable {it.calFPSuRui(5, 6, 6.5)}
UT.Timer.toc();

def lines = UT.IO.readAllLines('vasp/.lll-out/.XDATCAR-out/399.dat');
int colNum = UT.Texts.splitBlank(lines.first()).size();
// 手动读取到 datas
def datas = new ArrayList<IVector>();
for (i in 0..<lines.size()) {
    datas.add(UT.Texts.str2data(lines[i], colNum));
}

for (i in 0..<fp.size()) {
    def diff = fp[i].asVecRow() - datas[i];
    if (diff.opt().norm() > 0.0001) println("$i, $diff");
}

