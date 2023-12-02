package test.ml

import jtool.atom.Structures
import jtool.code.UT
import jtool.code.collection.ArrayLists
import jtool.lmp.Lmpdat
import jtool.math.vector.IVector
import jtool.math.vector.LogicalVector
import jtool.vasp.POSCAR
import jtoolex.ml.RandomForest
import atom.ClassifyCe;

import static jtool.code.UT.Plot.axis
import static jtool.code.UT.Plot.plot

/**
 * 测试使用基组 + 随机森林来区分 CuZr 中的晶相，
 * 这里只区分 laves 相，可以方便对比
 */

final int nmax = 2;
final int lmax = 4;
final double cutoff = 1.5;



// 构造样本
def dataIn = new ArrayList<IVector>();
def dataOut = LogicalVector.builder();
// 玻璃样本
try (def mpc = Lmpdat.read('lmp/.ffs-in/data-fs1-sc').getMPC()) {
    println("AtomNum of glass: ${mpc.atomNum()}");
    def basis = mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff);
    for (fp in basis) {
        dataIn.add(fp.asVecRow());
        dataOut.add(false);
    }
}
try (def mpc = Lmpdat.read('lmp/.ffs-in/data-fs1-init').getMPC()) {
    println("AtomNum of glass: ${mpc.atomNum()}");
    def basis = mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff);
    for (fp in basis) {
        dataIn.add(fp.asVecRow());
        dataOut.add(false);
    }
}
try (def mpc = Lmpdat.read('lmp/.ffs-in/data-fs1-init').opt().perturbXYZ(0.30).getMPC()) {
    println("AtomNum of glass: ${mpc.atomNum()}");
    def basis = mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff);
    for (fp in basis) {
        dataIn.add(fp.asVecRow());
        dataOut.add(false);
    }
}
// 三种 laves 相样本
try (def mpc = Structures.from(POSCAR.read('vasp/data/MgCu2.poscar'), 7).opt().perturbXYZ(0.05).getMPC()) {
    println("AtomNum of MgCu2: ${mpc.atomNum()}");
    def basis = mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff);
    for (fp in basis) {
        dataIn.add(fp.asVecRow());
        dataOut.add(true);
    }
}
try (def mpc = Structures.from(POSCAR.read('vasp/data/re_MgNi2.poscar'), 5).opt().perturbXYZ(0.05).getMPC()) {
    println("AtomNum of MgNi2: ${mpc.atomNum()}");
    def basis = mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff);
    for (fp in basis) {
        dataIn.add(fp.asVecRow());
        dataOut.add(true);
    }
}
try (def mpc = Structures.from(POSCAR.read('vasp/data/re_MgZn2.poscar'), 7).opt().perturbXYZ(0.05).getMPC()) {
    println("AtomNum of MgZn2: ${mpc.atomNum()}");
    def basis = mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff);
    for (fp in basis) {
        dataIn.add(fp.asVecRow());
        dataOut.add(true);
    }
}
try (def mpc = Structures.from(POSCAR.read('vasp/data/MgCu2.poscar'), 6).opt().perturbXYZ(0.15).getMPC()) {
    println("AtomNum of MgCu2: ${mpc.atomNum()}");
    def basis = mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff);
    for (fp in basis) {
        dataIn.add(fp.asVecRow());
        dataOut.add(true);
    }
}
try (def mpc = Structures.from(POSCAR.read('vasp/data/re_MgNi2.poscar'), 4).opt().perturbXYZ(0.15).getMPC()) {
    println("AtomNum of MgNi2: ${mpc.atomNum()}");
    def basis = mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff);
    for (fp in basis) {
        dataIn.add(fp.asVecRow());
        dataOut.add(true);
    }
}
try (def mpc = Structures.from(POSCAR.read('vasp/data/re_MgZn2.poscar'), 6).opt().perturbXYZ(0.15).getMPC()) {
    println("AtomNum of MgZn2: ${mpc.atomNum()}");
    def basis = mpc.calFPSuRui(nmax, lmax, mpc.unitLen()*cutoff);
    for (fp in basis) {
        dataIn.add(fp.asVecRow());
        dataOut.add(true);
    }
}
dataOut = dataOut.build();


// 抽取部分作为测试集
int end = dataOut.size();
int mid = (int)Math.round(end * 0.8);
def randIndex = ArrayLists.from(end, {i->i}); randIndex.shuffle();
def trainIn = dataIn[randIndex[0..<mid]];
def trainOut = dataOut[randIndex[0..<mid]];
def testIn = dataIn[randIndex[mid..<end]];
def testOut = dataOut[randIndex[mid..<end]];

// 训练随机森林
def rf = new RandomForest(trainIn, trainOut, 200, 0.04);

// 保存训练结果
UT.IO.map2json(rf.asMap(), 'lmp/.CuZr/rf.json');
println("训练模型已保存至 'lmp/.CuZr/rf.json'");

// 绘制 ROC
axis(0, 1, 0, 1);
plot([0, 1], [0, 1], null).lineType('--').width(1.0).color(0);
UT.Timer.tic();
ClassifyCe.plotROC(rf, trainIn, trainOut, 'rf-train', 3, '-.');
ClassifyCe.plotROC(rf, testIn , testOut , 'rf-test' , 3);
UT.Timer.toc();

rf.shutdown();







