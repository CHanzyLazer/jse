package example.mpc

import jse.atom.IAtomData
import jse.atom.MPC
import jse.code.UT
import jse.lmp.Dump

import static jse.code.UT.Plot.*

// 这里这样定义一个函数更加方便
static def calRDF(IAtomData data) {
    try (def mpc = new MPC(data)) {
        return mpc.calRDF()
    }
}


// 导入 dump 文件
def dump = Dump.read('lmp/dump/CuFCC108.lammpstrj')
// 去除开头的数据不进行统计
dump.cutFront(1)

// 对所有帧统计平均的 gr
def gr = calRDF(dump.first())
for (i in 1..<dump.size()) {
    gr.plus2this(calRDF(dump[i]))
}
gr.div2this(dump.size())

// 获取 r 值和 g 值
println('r = ' + gr.x())
println('g = ' + gr.f())
// 获取峰值的位置
println('maxR = ' + gr.opt().maxX())
// 保存到 csv
UT.IO.data2csv(gr, '.temp/example/mpc/rdf2.csv')
// 绘制
plot(gr)


//OUTPUT:
// r = 160-length Vector:
//    0.000   0.08940   0.1788   0.2682  ...  13.95   14.04   14.13   14.22
// g = 160-length Vector:
//    0.000   0.000   0.000   0.000  ...  1.028   1.012   1.011   1.010
// maxR = 2.503321227389768

