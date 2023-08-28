package test

import com.jtool.atom.Structures
import com.jtool.code.UT
import com.jtool.lmp.Lmpdat
import com.jtool.plot.Plotters
import com.jtool.vasp.POSCAR


/** 测试计算 BOOP */

final double cutoffMul = 2.20;
final int nnn = -1;
final double perturbMul = 1.0;

final boolean onlyCu = false;
final boolean onlyZr = false;

// 先计算玻璃态
data_G = Lmpdat.read('lmp/data/data-glass');
if (onlyCu) data_G = data_G.opt().filterType(1);
if (onlyZr) data_G = data_G.opt().filterType(2);
mpc_G = data_G.getMPC();
println("glass, u: ${mpc_G.unitLen()}");
UT.Timer.tic();
q4_G = mpc_G.calABOOP(4, mpc_G.unitLen()*cutoffMul, nnn);
UT.Timer.toc("glass, q4");
UT.Timer.tic();
q6_G = mpc_G.calABOOP(6, mpc_G.unitLen()*cutoffMul, nnn);
UT.Timer.toc("glass, q6");
mpc_G.shutdown();


// 再计算生成的结果
def data_FFS   = Lmpdat.read('lmp/.ffs-in/data-out-9');
if (onlyCu) data_FFS = data_FFS.opt().filterType(1);
if (onlyZr) data_FFS = data_FFS.opt().filterType(2);
mpc_FFS = data_FFS.getMPC();
println("FFS, u: ${mpc_FFS.unitLen()}");
UT.Timer.tic();
q4_FFS = mpc_FFS.calABOOP(4, mpc_FFS.unitLen()*cutoffMul, nnn);
UT.Timer.toc("FFS, q4");
UT.Timer.tic();
q6_FFS = mpc_FFS.calABOOP(6, mpc_FFS.unitLen()*cutoffMul, nnn);
UT.Timer.toc("FFS, q6");
mpc_FFS.shutdown();


data_MgCu2 = Structures.from(POSCAR.read('lmp/data/MgCu2.poscar'), 4).opt().perturbG(0.25*perturbMul);
if (onlyCu) data_MgCu2 = data_MgCu2.opt().filterType(2);
if (onlyZr) data_MgCu2 = data_MgCu2.opt().filterType(1);
mpc_MgCu2 = data_MgCu2.getMPC();
println("MgCu2, u: ${mpc_MgCu2.unitLen()}");
UT.Timer.tic();
q4_MgCu2 = mpc_MgCu2.calABOOP(4, mpc_MgCu2.unitLen()*cutoffMul, nnn);
UT.Timer.toc("MgCu2, q4");
UT.Timer.tic();
q6_MgCu2 = mpc_MgCu2.calABOOP(6, mpc_MgCu2.unitLen()*cutoffMul, nnn);
UT.Timer.toc("MgCu2, q6");
mpc_MgCu2.shutdown();

data_Zr3Cu8 = Structures.from(POSCAR.read('lmp/data/Zr3Cu8.poscar'), 3).opt().perturbG(0.25*perturbMul);
if (onlyCu) data_Zr3Cu8 = data_Zr3Cu8.opt().filterType(2);
if (onlyZr) data_Zr3Cu8 = data_Zr3Cu8.opt().filterType(1);
mpc_Zr3Cu8 = data_Zr3Cu8.getMPC();
println("Zr3Cu8, u: ${mpc_Zr3Cu8.unitLen()}");
UT.Timer.tic();
q4_Zr3Cu8 = mpc_Zr3Cu8.calABOOP(4, mpc_Zr3Cu8.unitLen()*cutoffMul, nnn);
UT.Timer.toc("Zr3Cu8, q4");
UT.Timer.tic();
q6_Zr3Cu8 = mpc_Zr3Cu8.calABOOP(6, mpc_Zr3Cu8.unitLen()*cutoffMul, nnn);
UT.Timer.toc("Zr3Cu8, q6");
mpc_Zr3Cu8.shutdown();

data_Zr7Cu10 = Structures.from(POSCAR.read('lmp/data/Zr7Cu10.poscar'), 3).opt().perturbG(0.25*perturbMul);
if (onlyCu) data_Zr7Cu10 = data_Zr7Cu10.opt().filterType(2);
if (onlyZr) data_Zr7Cu10 = data_Zr7Cu10.opt().filterType(1);
mpc_Zr7Cu10 = data_Zr7Cu10.getMPC();
println("Zr7Cu10, u: ${mpc_Zr7Cu10.unitLen()}");
UT.Timer.tic();
q4_Zr7Cu10 = mpc_Zr7Cu10.calABOOP(4, mpc_Zr7Cu10.unitLen()*cutoffMul, nnn);
UT.Timer.toc("Zr7Cu10, q4");
UT.Timer.tic();
q6_Zr7Cu10 = mpc_Zr7Cu10.calABOOP(6, mpc_Zr7Cu10.unitLen()*cutoffMul, nnn);
UT.Timer.toc("Zr7Cu10, q6");
mpc_Zr7Cu10.shutdown();


// 使用 Plotter 绘图
plt = Plotters.get();

plt.plot(q4_G      , q6_G      , 'glass'  ).lineType('none').markerType('o').markerSize(4);
plt.plot(q4_MgCu2  , q6_MgCu2  , 'MgCu2'  ).lineType('none').markerType('s').markerSize(4);
//plt.plot(q4_Zr3Cu8 , q6_Zr3Cu8 , 'Zr3Cu8' ).lineType('none').markerType('^').markerSize(4);
//plt.plot(q4_Zr7Cu10, q6_Zr7Cu10, 'Zr7Cu10').lineType('none').markerType('^').markerSize(4);
plt.plot(q4_FFS    , q6_FFS    , 'FFS'    ).lineType('none').markerType('o').markerSize(6);

plt.xlabel('q4').ylabel('q6');
plt.xTick(0.02).yTick(0.05);
plt.show();

