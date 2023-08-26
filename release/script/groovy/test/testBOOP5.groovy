package test

import com.jtool.atom.Structures
import com.jtool.lmp.Lmpdat
import com.jtool.math.vector.Vectors
import com.jtool.plot.Plotters
import com.jtool.vasp.POSCAR


/** 测试计算 BOOP，测试固液判断的阈值选择 */


// 首先导入 Lmpdat
def dataG = Lmpdat.read('lmp/data/data-glass');
def dataC = Lmpdat.read('lmp/data/data-crystal');
def dataZr7Cu10 = Structures.from(POSCAR.read('lmp/data/Zr7Cu10.poscar'), 4).opt().filterType(2).opt().perturbG(0.25);

// 计算连接数向量
def connectCountG, connectCountC;
try (def mpc = dataG.getMPC()) {
    connectCountG = mpc.calConnectCountABOOP(6, mpc.unitLen()*2.2, -1, 0.86);
}
try (def mpc = dataZr7Cu10.getMPC()) {
    connectCountC = mpc.calConnectCountABOOP(6, mpc.unitLen()*2.2, -1, 0.86);
}

// 统计结果
def distributionG = Vectors.zeros(21);
def distributionC = Vectors.zeros(21);
connectCountG.forEach {double count ->
    distributionG.increment(count>=20 ? 20 : (int)count);
}
connectCountC.forEach {double count ->
    distributionC.increment(count>=20 ? 20 : (int)count);
}

// 计算玻璃中判断为固体的百分比（保证在一个较小的不为零的值，如 0.5%）
println("solid prob in glass: ${distributionG[13..20].sum() / dataG.atomNum()}")

// 绘制结果
def plt = Plotters.get();

plt.plot(distributionG, 'glass');
plt.plot(distributionC, 'crystal');

plt.show();

