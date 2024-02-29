package example.mpc

import jse.atom.MPC
import jse.atom.Structures
import jse.lmp.Data
import jse.math.function.Func1
import jse.plot.Plotters

import static jse.code.UT.Math.rng

// 这样设置种子来固定随机流
rng(123456789)

// 导入 data 文件
def dataG = Data.read('lmp/data/data-glass')
def dataC = Data.read('lmp/data/data-crystal')
// 创建固定结构
def dataB = Structures.BCC(2.0, 10).opt().perturbXYZ(0.1)
def dataF = Structures.FCC(3.0,  8).opt().perturbXYZ(0.1)

// 计算 Q4，Q6，W4
def Q4G, Q6G, W4G
try (def mpcG = new MPC(dataG)) {
    Q4G = mpcG.calBOOP(4)
    Q6G = mpcG.calBOOP(6)
    W4G = mpcG.calBOOP3(4)
}
def Q4C, Q6C, W4C
try (def mpcC = new MPC(dataC)) {
    Q4C = mpcC.calBOOP(4)
    Q6C = mpcC.calBOOP(6)
    W4C = mpcC.calBOOP3(4)
}
def Q4B, Q6B, W4B
try (def mpcB = new MPC(dataB)) {
    Q4B = mpcB.calBOOP(4)
    Q6B = mpcB.calBOOP(6)
    W4B = mpcB.calBOOP3(4)
}
def Q4F, Q6F, W4F
try (def mpcF = new MPC(dataF)) {
    Q4F = mpcF.calBOOP(4)
    Q6F = mpcF.calBOOP(6)
    W4F = mpcF.calBOOP3(4)
}

// 输出平均值
println('Mean of Q4 of glass: '   + Q4G.mean())
println('Mean of Q4 of crystal: ' + Q4C.mean())
println('Mean of Q4 of BCC: '     + Q4B.mean())
println('Mean of Q4 of FCC: '     + Q4F.mean())
println()
println('Mean of Q6 of glass: '   + Q6G.mean())
println('Mean of Q6 of crystal: ' + Q6C.mean())
println('Mean of Q6 of BCC: '     + Q6B.mean())
println('Mean of Q6 of FCC: '     + Q6F.mean())
println()
println('Mean of W4 of glass: '   + W4G.mean())
println('Mean of W4 of crystal: ' + W4C.mean())
println('Mean of W4 of BCC: '     + W4B.mean())
println('Mean of W4 of FCC: '     + W4F.mean())


// 统计分布，这里使用 Func1 提供的方法来直接获取分布
def distQ4G = Func1.distFrom(Q4G, 0.0, 0.3, 100)
def distQ4C = Func1.distFrom(Q4C, 0.0, 0.3, 100)
def distQ4B = Func1.distFrom(Q4B, 0.0, 0.3, 100)
def distQ4F = Func1.distFrom(Q4F, 0.0, 0.3, 100)

def distQ6G = Func1.distFrom(Q6G, 0.0, 0.6, 100)
def distQ6C = Func1.distFrom(Q6C, 0.0, 0.6, 100)
def distQ6B = Func1.distFrom(Q6B, 0.0, 0.6, 100)
def distQ6F = Func1.distFrom(Q6F, 0.0, 0.6, 100)

def distW4G = Func1.distFrom(W4G, -0.2, 0.2, 100)
def distW4C = Func1.distFrom(W4C, -0.2, 0.2, 100)
def distW4B = Func1.distFrom(W4B, -0.2, 0.2, 100)
def distW4F = Func1.distFrom(W4F, -0.2, 0.2, 100)


// 绘制统计分布，多张图这样绘制
def pltQ4 = Plotters.get()
pltQ4.plot(distQ4G, 'glass'  )
pltQ4.plot(distQ4C, 'crystal')
pltQ4.plot(distQ4B, 'BCC'    )
pltQ4.plot(distQ4F, 'FCC'    )
pltQ4.xlabel('Q4')
pltQ4.xrange(distQ4G.x().first(), distQ4G.x().last())
pltQ4.show('distribution of Q4')

def pltQ6 = Plotters.get()
pltQ6.plot(distQ6G, 'glass'  )
pltQ6.plot(distQ6C, 'crystal')
pltQ6.plot(distQ6B, 'BCC'    )
pltQ6.plot(distQ6F, 'FCC'    )
pltQ6.xlabel('Q6')
pltQ6.xrange(distQ6G.x().first(), distQ6G.x().last())
pltQ6.show('distribution of Q6')

def pltW4 = Plotters.get()
pltW4.plot(distW4G, 'glass'  )
pltW4.plot(distW4C, 'crystal')
pltW4.plot(distW4B, 'BCC'    )
pltW4.plot(distW4F, 'FCC'    )
pltW4.xlabel('W4')
pltW4.xrange(distW4G.x().first(), distW4G.x().last())
pltW4.show('distribution of W4')


//OUTPUT:
// Mean of Q4 of glass: 0.08728394247381817
// Mean of Q4 of crystal: 0.07240307373895653
// Mean of Q4 of BCC: 0.07485692305181178
// Mean of Q4 of FCC: 0.15600087055713363
//
// Mean of Q6 of glass: 0.3661153576744139
// Mean of Q6 of crystal: 0.42662427799188424
// Mean of Q6 of BCC: 0.4589953178875175
// Mean of Q6 of FCC: 0.49133802632044926
//
// Mean of W4 of glass: -0.025397134886252924
// Mean of W4 of crystal: -0.010124805423113597
// Mean of W4 of BCC: 0.021556528136214895
// Mean of W4 of FCC: -0.1037359751956782

