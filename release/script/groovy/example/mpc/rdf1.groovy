package example.mpc

import jse.atom.MPC
import jse.code.UT
import jse.lmp.Data

import static jse.code.UT.Plot.*

// 导入 data 文件
def data = Data.read('lmp/data/data-glass')

def gr
// 根据 data 创建参数计算器 mpc
try (def mpc = new MPC(data)) {
    // 计算 rdf
    gr = mpc.calRDF()
}
// 获取 r 值和 g 值
println('r = ' + gr.x())
println('g = ' + gr.f())
// 获取峰值的位置
println('maxR = ' + gr.opt().maxX())
// 保存到 csv
UT.IO.data2csv(gr, '.temp/example/mpc/rdf1.csv')
// 绘制
plot(gr)


//OUTPUT:
// r = 160-length Vector:
//    0.000   0.09780   0.1956   0.2934  ...  15.26   15.35   15.45   15.55
// g = 160-length Vector:
//    0.000   0.000   0.000   0.000  ...  0.9981   0.9943   0.9963   0.9874
// maxR = 2.738433663246569

