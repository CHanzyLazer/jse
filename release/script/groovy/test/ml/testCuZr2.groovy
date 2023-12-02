package test.ml

import jtool.atom.Structures
import jtool.code.UT
import jtool.lmp.Lmpdat
import jtool.math.function.Func1
import jtool.plot.Plotters
import jtool.vasp.POSCAR
import jtoolex.ml.RandomForest


/**
 * 测试使用基组 + 随机森林来区分 CuZr 中的晶相，
 * 这里只区分 laves 相，可以方便对比
 */


final int nmax = 2;
final int lmax = 4;
final double cutoff = 1.5;

// 首先导入 Lmpdat
def dataG       = Lmpdat.read('lmp/.ffs-in/data-fs1-init');
def dataFCC     = Structures.FCC(4.0, 8).opt().perturbXYZ(0.10);
def dataBCC     = Structures.BCC(4.0, 12).opt().perturbXYZ(0.10);
def dataHCP     = Structures.HCP(4.0, 8).opt().perturbXYZ(0.10);

def dataMgCu2   = Structures.from(POSCAR.read('vasp/data/MgCu2.poscar'), 5).opt().perturbXYZ(0.10);
def dataZr3Cu8  = Structures.from(POSCAR.read('vasp/data/Zr3Cu8.poscar'), 4).opt().perturbXYZ(0.10);
def dataZr7Cu10 = Structures.from(POSCAR.read('vasp/data/Zr7Cu10.poscar'), 4).opt().perturbXYZ(0.10);
def dataZrCu2   = Structures.from(POSCAR.read('vasp/data/ZrCu2.poscar'), 6).opt().perturbXYZ(0.10);
def dataZr14Cu51= Structures.from(POSCAR.read('vasp/data/re_Zr14Cu51.poscar'), 3).opt().perturbXYZ(0.10);

// 然后导入已保存的分类器
def rf = RandomForest.load(UT.IO.json2map('lmp/.CuZr/rf.json'));

// 计算分类的概率
def predG       = dataG       .getMPC().withCloseable {def mpc -> rf.predict(mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff).collect {it.asVecRow()})}
def predFCC     = dataFCC     .getMPC().withCloseable {def mpc -> rf.predict(mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff).collect {it.asVecRow()})}
def predBCC     = dataBCC     .getMPC().withCloseable {def mpc -> rf.predict(mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff).collect {it.asVecRow()})}
def predHCP     = dataHCP     .getMPC().withCloseable {def mpc -> rf.predict(mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff).collect {it.asVecRow()})}
def predMgCu2   = dataMgCu2   .getMPC().withCloseable {def mpc -> rf.predict(mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff).collect {it.asVecRow()})}
def predZr3Cu8  = dataZr3Cu8  .getMPC().withCloseable {def mpc -> rf.predict(mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff).collect {it.asVecRow()})}
def predZr7Cu10 = dataZr7Cu10 .getMPC().withCloseable {def mpc -> rf.predict(mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff).collect {it.asVecRow()})}
def predZrCu2   = dataZrCu2   .getMPC().withCloseable {def mpc -> rf.predict(mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff).collect {it.asVecRow()})}
def predZr14Cu51= dataZr14Cu51.getMPC().withCloseable {def mpc -> rf.predict(mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff).collect {it.asVecRow()})}

rf.shutdown();

// 统计结果
def distributionG       = Func1.zeros(0.05, 0.1, 10);
def distributionFCC     = Func1.zeros(0.05, 0.1, 10);
def distributionBCC     = Func1.zeros(0.05, 0.1, 10);
def distributionHCP     = Func1.zeros(0.05, 0.1, 10);
def distributionMgCu2   = Func1.zeros(0.05, 0.1, 10);
def distributionZr3Cu8  = Func1.zeros(0.05, 0.1, 10);
def distributionZr7Cu10 = Func1.zeros(0.05, 0.1, 10);
def distributionZrCu2   = Func1.zeros(0.05, 0.1, 10);
def distributionZr14Cu51= Func1.zeros(0.05, 0.1, 10);

predG       .forEach {double p -> distributionG         .update(Math.min(9, (int)Math.floor(p/0.1)), {it+1});}
predFCC     .forEach {double p -> distributionFCC       .update(Math.min(9, (int)Math.floor(p/0.1)), {it+1});}
predBCC     .forEach {double p -> distributionBCC       .update(Math.min(9, (int)Math.floor(p/0.1)), {it+1});}
predHCP     .forEach {double p -> distributionHCP       .update(Math.min(9, (int)Math.floor(p/0.1)), {it+1});}
predMgCu2   .forEach {double p -> distributionMgCu2     .update(Math.min(9, (int)Math.floor(p/0.1)), {it+1});}
predZr3Cu8  .forEach {double p -> distributionZr3Cu8    .update(Math.min(9, (int)Math.floor(p/0.1)), {it+1});}
predZr7Cu10 .forEach {double p -> distributionZr7Cu10   .update(Math.min(9, (int)Math.floor(p/0.1)), {it+1});}
predZrCu2   .forEach {double p -> distributionZrCu2     .update(Math.min(9, (int)Math.floor(p/0.1)), {it+1});}
predZr14Cu51.forEach {double p -> distributionZr14Cu51  .update(Math.min(9, (int)Math.floor(p/0.1)), {it+1});}
distributionG       /= dataG        .atomNum();
distributionFCC     /= dataFCC      .atomNum();
distributionBCC     /= dataBCC      .atomNum();
distributionHCP     /= dataHCP      .atomNum();
distributionMgCu2   /= dataMgCu2    .atomNum();
distributionZr3Cu8  /= dataZr3Cu8   .atomNum();
distributionZr7Cu10 /= dataZr7Cu10  .atomNum();
distributionZrCu2   /= dataZrCu2    .atomNum();
distributionZr14Cu51/= dataZr14Cu51 .atomNum();

// 绘制结果
def plt = Plotters.get();

plt.plot(distributionG       , 'glass'       ).marker('o').lineType('-' );
plt.plot(distributionFCC     , 'fcc'         ).marker('s').lineType('--').lineWidth(1.0).markerSize(10.0);
plt.plot(distributionBCC     , 'bcc'         ).marker('d').lineType('--').lineWidth(1.0).markerSize(10.0);
plt.plot(distributionHCP     , 'hcp'         ).marker('^').lineType('--').lineWidth(1.0).markerSize(10.0);
plt.plot(distributionMgCu2   , 'laves-MgCu2' ).marker('*').lineType('-' );
plt.plot(distributionZr3Cu8  , 'Cu8Zr3'      ).marker('s').lineType('-' );
plt.plot(distributionZr7Cu10 , 'Cu10Zr7'     ).marker('d').lineType('-' );
plt.plot(distributionZrCu2   , 'Cu2Zr'       ).marker('^').lineType('-' );
plt.plot(distributionZr14Cu51, 'Cu51Zr14'    ).marker('s').lineType('-' );

plt.xlabel('pred').ylabel('p(n)');
plt.axis(0, 1, 0.0, 0.501);
plt.xTick(4);
plt.show();

