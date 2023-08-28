package test

import com.jtool.lmp.Lmpdat
import com.jtool.math.vector.Vectors
import com.jtool.plot.Plotters


/** 测试计算 BOOP，测试固液判断的阈值选择 */

final double cutoffMul = 2.20;
final int nnn = -1;
final double connectThreshold = 0.88;
final int maxConnect = 64;

final boolean onlyCu = false;
final boolean onlyZr = false;


// 首先导入 Lmpdat
def dataG       = Lmpdat.read('lmp/data/data-glass');
if (onlyCu) dataG = dataG.opt().filterType(1);
if (onlyZr) dataG = dataG.opt().filterType(2);
def dataFFS   = Lmpdat.read('lmp/.ffs-in/data-out-9');
if (onlyCu) dataFFS = dataFFS.opt().filterType(1);
if (onlyZr) dataFFS = dataFFS.opt().filterType(2);

// 计算连接数向量
def connectCountG       = dataG      .getMPC().withCloseable {def mpc -> mpc.calConnectCountABOOP(6, mpc.unitLen()*cutoffMul, nnn, connectThreshold)}
def connectCountFFS     = dataFFS    .getMPC().withCloseable {def mpc -> mpc.calConnectCountABOOP(6, mpc.unitLen()*cutoffMul, nnn, connectThreshold)}

// 统计结果
def distributionG       = Vectors.zeros(maxConnect+1);
def distributionFFS     = Vectors.zeros(maxConnect+1);

connectCountG       .forEach {distributionG         .increment(Math.min(maxConnect, (int)it));}
connectCountFFS     .forEach {distributionFFS       .increment(Math.min(maxConnect, (int)it));}
distributionG       /= dataG        .atomNum();
distributionFFS     /= dataFFS      .atomNum();

// 绘制结果
def plt = Plotters.get();

plt.plot(distributionG      , 'glass');
plt.plot(distributionFFS    , 'ffs');

plt.xlabel('connect count, n').ylabel('p(n)');
plt.axis(0, 64, 0.0, 0.005);
plt.xTick(4).yTick(0.001);
plt.show();

