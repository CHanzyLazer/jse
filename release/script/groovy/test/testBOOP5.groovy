package test

import com.jtool.atom.Structures
import com.jtool.lmp.Lmpdat
import com.jtool.math.vector.Vectors
import com.jtool.plot.Plotters
import com.jtool.vasp.POSCAR


/** 测试计算 BOOP，测试固液判断的阈值选择 */


// 首先导入 Lmpdat
def dataG = Lmpdat.read('lmp/data/data-glass');
def dataC = Structures.from(POSCAR.read('lmp/data/Zr7Cu10.poscar'), 4).opt().filterType(1).opt().perturbG(0.25);

// 计算连接数向量
def connectCountG, connectCountC;
try (def mpc = dataG.getMPC()) {
    connectCountG = mpc.calConnectCountABOOP(6, mpc.unitLen()*2.0, 12, 0.80);
}
try (def mpc = dataC.getMPC()) {
    connectCountC = mpc.calConnectCountABOOP(6, mpc.unitLen()*2.0, 12, 0.80);
}

// 统计结果
def distributionG = Vectors.zeros(13);
def distributionC = Vectors.zeros(13);
connectCountG.forEach {double count ->
    distributionG.increment(count as int);
}
connectCountC.forEach {double count ->
    distributionC.increment(count as int);
}

// 绘制结果
def plt = Plotters.get();

plt.plot(distributionG, 'glass');
plt.plot(distributionC, 'crystal');

plt.show();

